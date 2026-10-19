package snaypertihcreator.mineralslimes.client.entity;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import snaypertihcreator.mineralslimes.common.entity.slimes.AbstractSlimeEntity;
import snaypertihcreator.mineralslimes.common.slime.SlimeType;
import software.bernie.geckolib.cache.object.GeoBone;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

public class SlimeRenderer<T extends AbstractSlimeEntity> extends AbstractSlimeRenderer<T> {
    private static final String FACE = "face";
    private static final String COVER = "cover";

    private static final ResourceLocation TEX_HUNGRY = modLoc("textures/entity/mood/hungry.png");
    private static final ResourceLocation TEX_OPEN = modLoc("textures/entity/mood/open.png");
    private static final ResourceLocation TEX_CHEW = modLoc("textures/entity/mood/chew.png");
    private static final ResourceLocation TEX_NORMAL = modLoc("textures/entity/mood/normal.png");

    private final Map<AbstractSlimeEntity.FaceExpression, ResourceLocation> moods;
    private final int tint;

    public SlimeRenderer(EntityRendererProvider.Context ctx, SlimeType type) {
        super(ctx, type);
        tint = type.tint();
        ResourceLocation tex_normal =
                type.customNormalMood() ? modLoc("textures/entity/mood/%s_normal.png".formatted(type.modelKey())) : TEX_NORMAL;
        this.moods = Map.of(
                AbstractSlimeEntity.FaceExpression.NORMAL,  tex_normal,
                AbstractSlimeEntity.FaceExpression.HUNGRY,   TEX_HUNGRY,
                AbstractSlimeEntity.FaceExpression.OPEN, TEX_OPEN,
                AbstractSlimeEntity.FaceExpression.CHEW, TEX_CHEW);
    }

    private AbstractSlimeEntity.FaceExpression faceOf(T slime) {
        AbstractSlimeEntity.FaceExpression kf = slime.animations().keyframeFace();
        if (kf != null) return kf;
        return switch (slime.vitals().getState()) {
            case NORMAL -> AbstractSlimeEntity.FaceExpression.NORMAL;
            case HUNGRY -> AbstractSlimeEntity.FaceExpression.HUNGRY;
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
        return FACE.equals(bone.getName()) ? moods.get(faceOf(animatable)) : null;
    }

    @Override
    protected int getColourForBone(GeoBone bone, T animatable) {
        if (FACE.equals(bone.getName())) return tint;
        return 0xFFFFFFFF;
    }
}