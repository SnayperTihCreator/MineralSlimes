package snaypertihcreator.mineralslimes.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import guideme.scene.GuidebookLevelRenderer;
import guideme.scene.level.GuidebookLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static snaypertihcreator.mineralslimes.common.guide.SlimeSceneElement.GUIDE_SCALE;

@Mixin(value = GuidebookLevelRenderer.class, remap = false)
public abstract class GuidebookLevelRendererMixin {
    @Unique
    private static final String mineralSlimes$PREPARE_DISPATCHER = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";
    @Unique
    private static final String mineralSlimes$SCALE_BEFORE_RENDER = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";
    @Unique
    private static final String mineralSlimes$SCALE_AFTER_RENDER = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";

    @Inject(method = "handleEntity", at = @At(value = "INVOKE", target = mineralSlimes$PREPARE_DISPATCHER))
    private <E extends Entity> void mineralslimes$prepareDispatcher(GuidebookLevel level, PoseStack poseStack, E entity, MultiBufferSource buffers, float partialTicks, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();

        dispatcher.prepare(level, minecraft.gameRenderer.getMainCamera(), entity);
    }

    @Inject(method = "handleEntity", at = @At(value = "INVOKE", target = mineralSlimes$SCALE_BEFORE_RENDER))
    private <E extends Entity> void mineralslimes$scaleBeforeRender(GuidebookLevel level, PoseStack poseStack, E entity, MultiBufferSource buffers, float partialTicks, CallbackInfo ci) {
        float scale = entity.getPersistentData().getFloat(GUIDE_SCALE);

        if (scale > 0.0F && scale != 1.0F) {
            poseStack.pushPose();
            poseStack.translate(0.0F, entity.getBbHeight() * (1.0F - scale) / 2.0F, 0.0F);
            poseStack.scale(scale, scale, scale);
        }
    }

    @Inject(method = "handleEntity", at = @At(value = "INVOKE", target = mineralSlimes$SCALE_AFTER_RENDER, shift = At.Shift.AFTER))
    private <E extends Entity> void mineralslimes$scaleAfterRender(GuidebookLevel level, PoseStack poseStack, E entity, MultiBufferSource buffers, float partialTicks, CallbackInfo ci) {
        float scale = entity.getPersistentData().getFloat(GUIDE_SCALE);

        if (scale != 0.0F && scale != 1.0F) {
            poseStack.popPose();
        }
    }
}
