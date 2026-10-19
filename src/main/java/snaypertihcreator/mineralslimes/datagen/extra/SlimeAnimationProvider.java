package snaypertihcreator.mineralslimes.datagen.extra;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

public class SlimeAnimationProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final PackOutput output;
    private final String modId;
    private final List<MergeTask> tasks = new ArrayList<>();

    public SlimeAnimationProvider(PackOutput output, String modId) {
        this.output = output;
        this.modId = modId;
        this.addAnimations();
    }

    protected void addAnimations() {}

    private record MergeTask(ResourceLocation base, ResourceLocation patch, String outputName) {}

    protected void merge(ResourceLocation base, ResourceLocation patch, String outputName) {
        tasks.add(new MergeTask(base, patch, outputName));
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        for (MergeTask task : tasks) {
            JsonObject merged = mergeAnimations(task.base(), task.patch());

            Path out = output.getOutputFolder()
                    .resolve("assets").resolve(modId)
                    .resolve("animations").resolve("entity")
                    .resolve(task.outputName() + ".animation.json");

            writeJson(cache, out, merged);
        }

        return CompletableFuture.completedFuture(null);
    }

    private static void writeJson(CachedOutput cache, Path path, JsonObject json) {
        byte[] data = GSON.toJson(json).getBytes(StandardCharsets.UTF_8);
        @SuppressWarnings("deprecation") HashCode hash = Hashing.sha1().hashBytes(data);
        try {
            cache.writeIfNeeded(path, data, hash);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write " + path, e);
        }
    }

    @Nullable
    private static JsonObject readOptionalJson(ResourceLocation location) {
        String path = "/assets/" + location.getNamespace() + "/" + location.getPath();
        try (var in = SlimeAnimationProvider.class.getResourceAsStream(path)) {
            if (in == null) return null;
            return GSON.fromJson(new String(in.readAllBytes(), StandardCharsets.UTF_8), JsonObject.class);
        } catch (Exception e) {
            throw new IllegalStateException("SlimeAnimationProvider: failed to read " + location, e);
        }
    }

    private static JsonObject readJson(ResourceLocation location) {
        String path = "/assets/" + location.getNamespace() + "/" + location.getPath();
        try (var in = SlimeAnimationProvider.class.getResourceAsStream(path)) {
            if (in == null)
                throw new IllegalStateException("SlimeAnimationProvider: missing source json " + location);
            return GSON.fromJson(new String(in.readAllBytes(), StandardCharsets.UTF_8), JsonObject.class);
        } catch (Exception e) {
            throw new IllegalStateException("SlimeAnimationProvider: failed to read " + location, e);
        }
    }

    private static JsonObject mergeAnimations(ResourceLocation baseLoc, ResourceLocation patchLoc) {
        JsonObject base = readJson(baseLoc);
        JsonObject patch = readOptionalJson(patchLoc);
        if (patch == null) return base;

        JsonObject baseAnims = base.getAsJsonObject("animations");
        JsonObject patchAnims = patch.getAsJsonObject("animations");
        if (baseAnims == null) throw new IllegalStateException("No 'animations' in " + baseLoc);
        if (patchAnims == null) throw new IllegalStateException("No 'animations' in " + patchLoc);

        for (var e : patchAnims.entrySet())
            baseAnims.add(e.getKey(), e.getValue());
        return base;
    }

    @Override
    public @NotNull String getName() {
        return "Slime Animations (" + modId + ")";
    }
}
