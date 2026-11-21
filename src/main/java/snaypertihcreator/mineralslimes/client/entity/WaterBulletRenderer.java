package snaypertihcreator.mineralslimes.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.client.models.WaterBulletModel;
import snaypertihcreator.mineralslimes.common.entity.WaterBullet;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

public class WaterBulletRenderer extends EntityRenderer<WaterBullet> {
    private static final ResourceLocation TEXTURE = modLoc("textures/entity/water.png");

    private final WaterBulletModel model;

    public WaterBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new WaterBulletModel(context.bakeLayer(WaterBulletModel.LAYER));
    }

    @Override
    public void render(@NotNull WaterBullet entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.scale(1.0F, 1.0F, 1.0F);

        this.model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTick, 0.0F, 0.0F);
        VertexConsumer consumer = buffers.getBuffer(this.model.renderType(getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull WaterBullet entity) {
        return TEXTURE;
    }
}
