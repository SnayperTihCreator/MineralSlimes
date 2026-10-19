package snaypertihcreator.mineralslimes.client.models;

import net.minecraft.resources.ResourceLocation;
import snaypertihcreator.mineralslimes.common.entity.slimes.AbstractSlimeEntity;
import software.bernie.geckolib.renderer.GeoRenderer;

import javax.annotation.Nullable;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

public class SlimeModel<T extends AbstractSlimeEntity> extends BaseModel<T> {
    private final ResourceLocation model;
    private final ResourceLocation animations;
    private final ResourceLocation texture;

    public SlimeModel(String modelKey) {
        this.model = modLoc("geo/entity/%s.geo.json".formatted(modelKey));
        this.texture = modLoc("textures/entity/%s.png".formatted(modelKey));
        this.animations = modLoc("animations/entity/%s.animation.json".formatted(modelKey));
    }

    @Override public ResourceLocation getModelResource(T slime, @Nullable GeoRenderer<T> r) {
        return model;
    }

    @Override public ResourceLocation getTextureResource(T slime, @Nullable GeoRenderer<T> r) {
        return texture;
    }

    @Override public ResourceLocation getAnimationResource(T slime) {
        return animations;
    }
}
