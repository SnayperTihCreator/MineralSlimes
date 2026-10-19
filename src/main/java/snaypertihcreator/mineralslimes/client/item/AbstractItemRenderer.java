package snaypertihcreator.mineralslimes.client.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import snaypertihcreator.mineralslimes.common.ColorUtil;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtil;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static snaypertihcreator.mineralslimes.client.RenderUtil.isTranslucentRenderType;

public abstract class AbstractItemRenderer<T extends Item & GeoAnimatable> extends GeoItemRenderer<T> {
    private static final String MAIN = "main";
    private static final Map<ResourceLocation, IntIntPair> TEXTURE_DIMENSIONS = new HashMap<>();

    private int pass = 0;
    protected GeoBone currentBone = null;
    protected ResourceLocation baseTexture = null;
    private float uvWidthRatio = 1.0f;
    private float uvHeightRatio = 1.0f;

    public AbstractItemRenderer(GeoModel<T> model){
        super(model);
    }

    @SuppressWarnings("unused")
    @Nullable
    protected RenderType getRenderTypeForBone(GeoBone bone, T animatable, @Nullable ResourceLocation boneTexture) {
        return null;
    }

    @SuppressWarnings("unused")
    @Nullable
    protected ResourceLocation getTextureForBone(GeoBone bone, T animatable) {
        return null;
    }

    @SuppressWarnings("unused")
    protected int getColourForBone(GeoBone bone, T animatable) {
        return 0xFFFFFFFF;
    }

    private static IntIntPair textureDimensions(ResourceLocation texture) {
        return TEXTURE_DIMENSIONS.computeIfAbsent(texture, RenderUtil::getTextureDimensions);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void actuallyRender(PoseStack poseStack, T animatable, BakedGeoModel model, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (!isReRender) {
            AnimationState<T> animationState = new AnimationState<>(animatable, 0, 0, partialTick, false);
            long instanceId = getInstanceId(animatable);
            GeoModel<T> currentModel = getGeoModel();

            animationState.setData(DataTickets.TICK, animatable.getTick(this.currentItemStack));
            animationState.setData(DataTickets.ITEM_RENDER_PERSPECTIVE, this.renderPerspective);
            animationState.setData(DataTickets.ITEMSTACK, this.currentItemStack);
            animatable.getAnimatableInstanceCache().getManagerForId(instanceId)
                    .setData(DataTickets.ITEM_RENDER_PERSPECTIVE, this.renderPerspective);
            currentModel.addAdditionalStateData(animatable, instanceId, animationState::setData);
            currentModel.handleAnimations(animatable, instanceId, animationState, partialTick);
        }

        this.modelRenderTranslations = new Matrix4f(poseStack.last().pose());
        this.baseTexture = getTextureLocation(animatable);

        GeoBone mainBone = getGeoModel().getBone(MAIN).orElse(null);
        if (mainBone == null && !model.topLevelBones().isEmpty())
            mainBone = model.topLevelBones().getFirst();

        if (mainBone != null) {
            this.pass = 0;
            renderRecursively(poseStack, animatable, mainBone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

            this.pass = 1;
            renderRecursively(poseStack, animatable, mainBone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

            this.pass = 0;
        }
    }

    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {

        if (bone.isHidden()) return;

        ResourceLocation boneTexture = getTextureForBone(bone, animatable);
        RenderType override = getRenderTypeForBone(bone, animatable, boneTexture);

        RenderType modelRt = getGeoModel().getRenderType(animatable, this.baseTexture);
        if (modelRt == null) {
            modelRt = RenderType.entitySolid(this.baseTexture);
        }
        RenderType rt = override != null ? override : modelRt;
        boolean isTranslucent = isTranslucentRenderType(rt);
        boolean drawThisPass = isTranslucent ? this.pass == 1 : this.pass == 0;

        poseStack.pushPose();

        RenderUtil.translateMatrixToBone(poseStack, bone);
        RenderUtil.translateToPivotPoint(poseStack, bone);
        RenderUtil.rotateMatrixAroundBone(poseStack, bone);
        RenderUtil.scaleMatrixForBone(poseStack, bone);

        if (bone.isTrackingMatrices()) {
            Matrix4f poseState = new Matrix4f(poseStack.last().pose());
            bone.setModelSpaceMatrix(RenderUtil.invertAndMultiplyMatrices(poseState, this.modelRenderTranslations));
            bone.setLocalSpaceMatrix(RenderUtil.invertAndMultiplyMatrices(poseState, this.itemRenderTranslations));
        }

        RenderUtil.translateAwayFromPivotPoint(poseStack, bone);

        if (drawThisPass) {
            VertexConsumer buf = bufferSource.getBuffer(rt);

            boolean remapUv = boneTexture != null && !boneTexture.equals(this.baseTexture);
            if (remapUv) {
                IntIntPair baseSize = textureDimensions(this.baseTexture);
                IntIntPair boneSize = textureDimensions(boneTexture);
                this.uvWidthRatio = baseSize.firstInt() / (float) boneSize.firstInt();
                this.uvHeightRatio = baseSize.secondInt() / (float) boneSize.secondInt();
            } else {
                this.uvWidthRatio = 1.0f;
                this.uvHeightRatio = 1.0f;
            }

            this.currentBone = bone;
            int boneColour = ColorUtil.of(colour).multiply(ColorUtil.of(getColourForBone(bone, animatable))).toInt();
            super.renderCubesOfBone(poseStack, bone, buf, packedLight, packedOverlay, boneColour);

            this.currentBone = null;
        }

        for (GeoBone child : bone.getChildBones()) {
            renderRecursively(poseStack, animatable, child, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        }

        poseStack.popPose();
    }

    @Override
    public void createVerticesOfQuad(GeoQuad quad, Matrix4f poseState, Vector3f normal, VertexConsumer buffer, int packedLight, int packedOverlay, int colour) {
        if (this.currentBone == null) {
            super.createVerticesOfQuad(quad, poseState, normal, buffer, packedLight, packedOverlay, colour);
            return;
        }

        for (GeoVertex vertex : quad.vertices()) {
            Vector4f vector4f = poseState.transform(new Vector4f(vertex.position().x(), vertex.position().y(), vertex.position().z(), 1.0f));

            float u = vertex.texU() * this.uvWidthRatio;
            float v = vertex.texV() * this.uvHeightRatio;

            buffer.addVertex(vector4f.x(), vector4f.y(), vector4f.z(), colour, u, v, packedOverlay, packedLight, normal.x(), normal.y(), normal.z());
        }
    }
}
