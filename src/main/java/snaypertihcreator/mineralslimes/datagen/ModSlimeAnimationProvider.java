package snaypertihcreator.mineralslimes.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.datagen.extra.SlimeAnimationProvider;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

public class ModSlimeAnimationProvider extends SlimeAnimationProvider {
    private static final ResourceLocation DEFAULT =
            modLoc("animations/entity/slime_default.animation.json");
    private static final ResourceLocation EMPTY =
            modLoc("animations/entity/slime_empty.animation.json");

    public ModSlimeAnimationProvider(PackOutput output){
        super(output, MineralSlimes.MODID);
    }

    @Contract("_ -> new")
    @SuppressWarnings("SameParameterValue")
    private @NotNull ResourceLocation preset(String name){
        return modLoc("template/animations/entity/%s.animation.json".formatted(name));
    }

    @Override
    protected void addAnimations() {
        merge(DEFAULT, preset("rocky"), "rocky_slime");
        merge(DEFAULT, EMPTY, "pink_slime");
        merge(DEFAULT, EMPTY, "tubby_slime");
        merge(DEFAULT, EMPTY, "quantum_slime");
        merge(DEFAULT, EMPTY, "puddle_slime");
    }
}
