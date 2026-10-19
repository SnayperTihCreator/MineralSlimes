
package snaypertihcreator.mineralslimes.common.guide;

import guideme.compiler.PageCompiler;
import guideme.compiler.tags.MdxAttrs;
import guideme.document.LytErrorSink;
import guideme.libs.mdast.mdx.model.MdxJsxElementFields;
import guideme.scene.GuidebookScene;
import guideme.scene.element.SceneElementTagCompiler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.joml.Vector3f;
import snaypertihcreator.mineralslimes.common.entity.ModSlimes;

import java.util.Set;
import java.util.function.Function;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

public class SlimeSceneElement implements SceneElementTagCompiler {
    public static final String NAME = "SlimeScene";
    public static final String GUIDE_SCALE = modLoc("guide_scale").toString();
    public static final String GUIDE_WIDTH = modLoc("guide_width").toString();
    public static final String GUIDE_HEIGHT = modLoc("guide_height").toString();

    @Override
    public Set<String> getTagNames() {
        return Set.of(NAME);
    }

    @Override
    public void compile(GuidebookScene scene, PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el) {
        String slimeId = MdxAttrs.getString(compiler, errorSink, el, "id", null);
        if (slimeId == null) {
            errorSink.appendError(compiler, "Missing attribute 'id'", el);
            return;
        }

        var entry = ModSlimes.find(slimeId);
        if (entry.isEmpty()) {
            errorSink.appendError(compiler, "Unknown slime: " + slimeId, el);
            return;
        }

        CompoundTag data = MdxAttrs.getCompoundTag(compiler, errorSink, el, "data", new CompoundTag());
        var entityId = entry.get().entity().getKey().location();
        data.putString("id", entityId.toString());

        Entity entity = EntityType.loadEntityRecursive(data, scene.getLevel(), Function.identity());
        if (entity == null) {
            errorSink.appendError(compiler, "Failed to load entity for slime '" + slimeId + "'", el);
            return;
        }

        float scale = MdxAttrs.getFloat(compiler, errorSink, el, "scale", 0.75F);
        float width = MdxAttrs.getFloat(compiler, errorSink, el, "width", 100.0F);
        float height = MdxAttrs.getFloat(compiler, errorSink, el, "height", 100.0F);

        if (!Float.isFinite(scale) || scale <= 0.0F || !Float.isFinite(width) || width < 10.0F
                || !Float.isFinite(height) || height < 10.0F) {
            errorSink.appendError(compiler, "Invalid SlimeScene dimensions or scale", el);
            return;
        }

        entity.getPersistentData().putFloat(GUIDE_SCALE, scale);
        entity.getPersistentData().putFloat(GUIDE_WIDTH, width);
        entity.getPersistentData().putFloat(GUIDE_HEIGHT, height);

        Vector3f pos = new Vector3f(0.5f, 0, 0.5f);
        MdxAttrs.getFloatPos(compiler, errorSink, el, pos);
        entity.setPos(pos.x, pos.y, pos.z);

        float rotationY = MdxAttrs.getFloat(compiler, errorSink, el, "rotationY", -90.0F);
        float rotationX = MdxAttrs.getFloat(compiler, errorSink, el, "rotationX", 0.0F);
        entity.setYRot(rotationY);
        entity.setXRot(rotationX);
        entity.setOldPosAndRot();
        entity.setYHeadRot(entity.getYRot());
        entity.setYBodyRot(entity.getYRot());

        scene.getLevel().addEntity(entity);
        entity.tick();
        fitCamera(scene, width, height);
    }

    private static void fitCamera(GuidebookScene scene, float width, float height) {
        var camera = scene.getCameraSettings();
        var original = camera.save();

        camera.setZoom(1.0F);
        var bounds = scene.getScreenBounds();

        float sceneWidth = Math.abs(bounds.z - bounds.x);
        float sceneHeight = Math.abs(bounds.w - bounds.y);

        if (sceneWidth <= 0.0F || sceneHeight <= 0.0F) {
            camera.restore(original);
            return;
        }

        float zoom = Math.min(width / sceneWidth, height / sceneHeight) * 0.85F;
        camera.setZoom(Math.clamp(zoom, 0.1F, 8.0F));
    }
}
