package snaypertihcreator.mineralslimes.client.entity;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import snaypertihcreator.mineralslimes.common.entity.slimes.AbstractSlimeEntity;
import snaypertihcreator.mineralslimes.common.slime.SlimeType;
import software.bernie.geckolib.cache.object.GeoBone;
import snaypertihcreator.mineralslimes.common.slime.FaceExpression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

public class SlimeRenderer<T extends AbstractSlimeEntity> extends AbstractSlimeRenderer<T> {
    private static final String FACE = "face";
    private static final String COVER = "cover";

    private static final List<FaceExpression> faces = List.of(
            FaceExpression.NORMAL, FaceExpression.OPEN,
            FaceExpression.HUNGRY, FaceExpression.CHEW
    );

    private static final ResourceLocation TEX_NORMAL = modLoc("textures/entity/mood/normal.png");

    private final Map<FaceExpression, ResourceLocation> moods = new HashMap<>();
    private final int tint;

    private void handleCustomMood(SlimeType type, FaceExpression face){
        @Nullable String texId = type.moods().getOrDefault(face.getTextureName(), null);
        if (texId != null) moods.put(face, modLoc("textures/entity/mood/%s.png".formatted(texId)));
        else moods.put(face, modLoc("textures/entity/mood/%s.png".formatted(face.getTextureName())));
    }

    public SlimeRenderer(EntityRendererProvider.Context ctx, SlimeType type) {
        super(ctx, type);
        tint = type.tint();
        for (var face : faces) handleCustomMood(type, face);
    }

    private FaceExpression faceOf(T slime) {
        FaceExpression kf = slime.animations().keyframeFace();
        if (kf != null) return kf;
        return switch (slime.vitals().getState()) {
            case NORMAL -> FaceExpression.NORMAL;
            case HUNGRY -> FaceExpression.HUNGRY;
        };
    }

    @Override
    protected @Nullable RenderType getRenderTypeForBone(GeoBone bone, T animatable, @Nullable ResourceLocation boneTexture) {
        return switch (bone.getName()) {
            case COVER -> RenderType.entityTranslucentCull(this.baseTexture);
            case FACE  -> RenderType.entityTranslucent(Objects.requireNonNull(boneTexture));
            default    -> null;
        };
    }

    @Override
    protected @Nullable ResourceLocation getTextureForBone(GeoBone bone, T animatable) {
        return FACE.equals(bone.getName()) ? moods.getOrDefault(faceOf(animatable), TEX_NORMAL) : null;
    }

    @Override
    protected int getColourForBone(GeoBone bone, T animatable) {
        if (FACE.equals(bone.getName())) return tint;
        return 0xFFFFFFFF;
    }

    @Override
    protected @Nullable UvRect getUvRectForBone(GeoBone bone, T animatable, @org.jetbrains.annotations.Nullable ResourceLocation texture) {
        return FACE.equals(bone.getName())? fullTexture(texture) : null;
    }
}