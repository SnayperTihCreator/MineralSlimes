package snaypertihcreator.mineralslimes.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import snaypertihcreator.mineralslimes.client.models.SlimeModel;
import snaypertihcreator.mineralslimes.common.ColorUtil;
import snaypertihcreator.mineralslimes.common.entity.slimes.AbstractSlimeEntity;
import snaypertihcreator.mineralslimes.common.slime.SlimeType;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.RenderUtil;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static snaypertihcreator.mineralslimes.client.RenderUtil.isTranslucentRenderType;

public abstract class AbstractSlimeRenderer<T extends AbstractSlimeEntity> extends GeoEntityRenderer<T> {
    public record UvRect(int u, int v, int width, int height) {}

    public record UvRemap(float srcMinU, float srcMinV, float srcSpanU, float srcSpanV,
                          float dstMinU, float dstMinV, float dstSpanU, float dstSpanV) {
        float mapU(float u) {
            return dstMinU + (u - srcMinU) / srcSpanU * dstSpanU;
        }

        float mapV(float v) {
            return dstMinV + (v - srcMinV) / srcSpanV * dstSpanV;
        }
    }

    private static final String MAIN = "main";
    private static final Map<ResourceLocation, IntIntPair> TEXTURE_DIMENSIONS = new HashMap<>();

    private int pass = 0;
    protected GeoBone currentBone = null;
    protected ResourceLocation currentBoneTexture = null;
    protected int currentColour = 0xFFFFFFFF;

    protected ResourceLocation baseTexture = null;
    @Nullable
    private UvRemap uvRemap = null;

    public AbstractSlimeRenderer(EntityRendererProvider.Context ctx, SlimeType type) {
        super(ctx, new SlimeModel<>(type.modelKey()));
    }

    @Nullable
    protected RenderType getRenderTypeForBone(GeoBone bone, T animatable, @Nullable ResourceLocation boneTexture) {
        return null;
    }

    @Nullable
    protected ResourceLocation getTextureForBone(GeoBone bone, T animatable) {
        return null;
    }

    protected int getColourForBone(GeoBone bone, T animatable) {
        return 0xFFFFFFFF;
    }

    @Nullable
    protected UvRect getUvRectForBone(GeoBone bone, T animatable, @Nullable ResourceLocation texture) {
        return null;
    }

    protected UvRect fullTexture(ResourceLocation texture) {
        IntIntPair dim = textureDimensions(texture);
        return new UvRect(0, 0, dim.firstInt(), dim.secondInt());
    }

    private static IntIntPair textureDimensions(ResourceLocation texture) {
        return TEXTURE_DIMENSIONS.computeIfAbsent(texture, RenderUtil::getTextureDimensions);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void actuallyRender(PoseStack poseStack, T animatable, BakedGeoModel model,
                               @Nullable RenderType renderType, MultiBufferSource bufferSource,
                               @Nullable VertexConsumer buffer, boolean isReRender, float partialTick,
                               int packedLight, int packedOverlay, int colour) {

        poseStack.pushPose();

        LivingEntity livingEntity = animatable instanceof LivingEntity entity ? entity : null;
        boolean shouldSit = animatable.isPassenger() && animatable.getVehicle() != null;
        float lerpBodyRot = livingEntity == null ? 0 : Mth.rotLerp(partialTick, livingEntity.yBodyRotO, livingEntity.yBodyRot);
        float lerpHeadRot = livingEntity == null ? 0 : Mth.rotLerp(partialTick, livingEntity.yHeadRotO, livingEntity.yHeadRot);
        float netHeadYaw = lerpHeadRot - lerpBodyRot;

        if (shouldSit && animatable.getVehicle() instanceof LivingEntity vehicle) {
            lerpBodyRot = Mth.rotLerp(partialTick, vehicle.yBodyRotO, vehicle.yBodyRot);
            netHeadYaw = lerpHeadRot - lerpBodyRot;
            float clampedHeadYaw = Mth.clamp(Mth.wrapDegrees(netHeadYaw), -85, 85);
            lerpBodyRot = lerpHeadRot - clampedHeadYaw;

            if (clampedHeadYaw * clampedHeadYaw > 2500f)
                lerpBodyRot += clampedHeadYaw * 0.2f;

            netHeadYaw = lerpHeadRot - lerpBodyRot;
        }

        if (animatable.getPose() == Pose.SLEEPING && livingEntity != null) {
            Direction bedDirection = livingEntity.getBedOrientation();
            if (bedDirection != null) {
                float eyePosOffset = livingEntity.getEyeHeight(Pose.STANDING) - 0.1F;
                poseStack.translate(-bedDirection.getStepX() * eyePosOffset, 0, -bedDirection.getStepZ() * eyePosOffset);
            }
        }

        float nativeScale = livingEntity != null ? livingEntity.getScale() : 1;
        float ageInTicks = animatable.tickCount + partialTick;
        float limbSwingAmount = 0;
        float limbSwing = 0;

        poseStack.scale(nativeScale, nativeScale, nativeScale);
        applyRotations(animatable, poseStack, ageInTicks, lerpBodyRot, partialTick, nativeScale);

        if (!shouldSit && animatable.isAlive() && livingEntity != null) {
            limbSwingAmount = livingEntity.walkAnimation.speed(partialTick);
            limbSwing = livingEntity.walkAnimation.position(partialTick);
            if (livingEntity.isBaby()) limbSwing *= 3f;
            if (limbSwingAmount > 1f) limbSwingAmount = 1f;
        }

        if (!isReRender) {
            float headPitch = Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot());
            float motionThreshold = getMotionAnimThreshold(animatable);
            Vec3 velocity = animatable.getDeltaMovement();
            float avgVelocity = (float) ((Math.abs(velocity.x) + Math.abs(velocity.z)) / 2f);
            AnimationState<T> animationState = new AnimationState<>(animatable, limbSwing, limbSwingAmount, partialTick, avgVelocity >= motionThreshold && limbSwingAmount != 0);
            long instanceId = getInstanceId(animatable);
            GeoModel<T> currentModel = getGeoModel();

            animationState.setData(DataTickets.TICK, animatable.getTick(animatable));
            animationState.setData(DataTickets.ENTITY, animatable);
            animationState.setData(DataTickets.ENTITY_MODEL_DATA, new EntityModelData(shouldSit, livingEntity != null && livingEntity.isBaby(), -netHeadYaw, -headPitch));
            currentModel.addAdditionalStateData(animatable, instanceId, animationState::setData);
            currentModel.handleAnimations(animatable, instanceId, animationState, partialTick);
        }

        poseStack.translate(0, 0.01f, 0);

        this.modelRenderTranslations = new Matrix4f(poseStack.last().pose());
        this.baseTexture = getTextureLocation(animatable);

        GeoBone mainBone = getGeoModel().getBone(MAIN).orElse(null);
        if (mainBone == null && !model.topLevelBones().isEmpty())
            mainBone = model.topLevelBones().getFirst();

        if (mainBone != null) {
            this.pass = 0;
            renderRecursively(poseStack, animatable, mainBone, renderType, bufferSource, buffer,
                    isReRender, partialTick, packedLight, packedOverlay, colour);

            this.pass = 1;
            renderRecursively(poseStack, animatable, mainBone, renderType, bufferSource, buffer,
                    isReRender, partialTick, packedLight, packedOverlay, colour);

            this.pass = 0;
        }

        poseStack.popPose();
    }

    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                                  float partialTick, int packedLight, int packedOverlay, int colour) {

        if (bone.isHidden()) return;

        ResourceLocation boneTexture = getTextureForBone(bone, animatable);
        RenderType override = getRenderTypeForBone(bone, animatable, boneTexture);

        boolean isTranslucent = isTranslucentRenderType(override);
        boolean drawThisPass  = isTranslucent ? this.pass == 1 : this.pass == 0;

        RenderType effectiveRt = override != null ? override : RenderType.entitySolid(this.baseTexture);

        poseStack.pushPose();

        RenderUtil.translateMatrixToBone(poseStack, bone);
        RenderUtil.translateToPivotPoint(poseStack, bone);
        RenderUtil.rotateMatrixAroundBone(poseStack, bone);
        RenderUtil.scaleMatrixForBone(poseStack, bone);
        RenderUtil.translateAwayFromPivotPoint(poseStack, bone);

        if (drawThisPass) {
            VertexConsumer buf = bufferSource.getBuffer(effectiveRt);
            this.currentBone = bone;
            this.currentBoneTexture = boneTexture != null ? boneTexture : this.baseTexture;
            var colorBone = ColorUtil.of(getColourForBone(bone, animatable));
            this.currentColour = ColorUtil.of(colour).multiply(colorBone).toInt();

            this.uvRemap = null;
            if (boneTexture != null && !boneTexture.equals(this.baseTexture)) {
                UvRect zone = getUvRectForBone(bone, animatable, boneTexture);
                if (zone != null)  this.uvRemap = buildUvRemap(bone, zone, boneTexture);
            }

            super.renderCubesOfBone(poseStack, bone, buf, packedLight, packedOverlay, this.currentColour);

            this.currentBone = null;
            this.currentBoneTexture = null;
            this.uvRemap = null;
        }

        for (GeoBone child : bone.getChildBones())
            renderRecursively(poseStack, animatable, child, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

        poseStack.popPose();
    }

    @Nullable
    private UvRemap buildUvRemap(GeoBone bone, UvRect zone, ResourceLocation texture) {
        float minU = Float.MAX_VALUE, minV = Float.MAX_VALUE;
        float maxU = -Float.MAX_VALUE, maxV = -Float.MAX_VALUE;

        for (GeoCube cube : bone.getCubes())
            for (GeoQuad quad : cube.quads()) {
                if (quad == null) continue;
                for (GeoVertex vertex : quad.vertices()) {
                    minU = Math.min(minU, vertex.texU());
                    minV = Math.min(minV, vertex.texV());
                    maxU = Math.max(maxU, vertex.texU());
                    maxV = Math.max(maxV, vertex.texV());
                }
            }

        float srcSpanU = maxU - minU;
        float srcSpanV = maxV - minV;
        if (srcSpanU <= 0 || srcSpanV <= 0) return null;

        IntIntPair dim = textureDimensions(texture);
        float texW = dim.firstInt();
        float texH = dim.secondInt();
        return new UvRemap(minU, minV, srcSpanU, srcSpanV,
                zone.u / texW, zone.v / texH, zone.width / texW, zone.height / texH);
    }

    @Override
    public void createVerticesOfQuad(GeoQuad quad, Matrix4f poseState, Vector3f normal, VertexConsumer buffer, int packedLight, int packedOverlay, int colour) {
        if (this.currentBone == null) {
            super.createVerticesOfQuad(quad, poseState, normal, buffer, packedLight, packedOverlay, colour);
            return;
        }

        for (GeoVertex vertex : quad.vertices()) {
            Vector4f vector4f = poseState.transform(new Vector4f(
                    vertex.position().x(), vertex.position().y(), vertex.position().z(), 1.0f));

            float u = this.uvRemap != null? uvRemap.mapU(vertex.texU()): vertex.texU();
            float v = this.uvRemap != null? uvRemap.mapV(vertex.texV()): vertex.texV();

            buffer.addVertex(vector4f.x(), vector4f.y(), vector4f.z(), colour, u, v, packedOverlay, packedLight, normal.x(), normal.y(), normal.z());
        }
    }
}