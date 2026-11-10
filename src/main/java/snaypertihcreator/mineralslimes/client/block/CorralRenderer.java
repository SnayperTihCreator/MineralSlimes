package snaypertihcreator.mineralslimes.client.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.client.parts.FencePartRenderers;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.fance.FencePart;

public class CorralRenderer implements BlockEntityRenderer<CorralBlockEntity> {

    @SuppressWarnings("unused")
    public CorralRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@NotNull CorralBlockEntity corral, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay) {
        renderFence(corral, corral.getFenceState(), pose, buffers, packedLight, packedOverlay);

        for (FencePart part : corral.getParts()) {
            var renderer =  FencePartRenderers.get(part.getType());

            pose.pushPose();
            transformToPart(pose, part);
            renderer.render(part, partialTick,  pose, buffers, packedLight, packedOverlay);
            pose.popPose();
        }
    }

    @SuppressWarnings("deprecation")
    private void renderFence(CorralBlockEntity corral, BlockState fence, PoseStack pose, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (fence == null) return;
        int light = corral.getLevel() == null? packedLight:
                LevelRenderer.getLightColor(corral.getLevel(), corral.getBlockPos());
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(fence, pose, buffers, light, packedOverlay);
    }

    protected static void transformToPart(PoseStack pose, FencePart part) {
        pose.translate(0.5, 0, 0.5);
        if (part.getAngle() >= 0){
            pose.mulPose(Axis.YN.rotationDegrees(part.getAngle()));
            pose.translate(0, 0, -part.getAttachedRadius());
        }
        pose.translate(-0.5, 0, -0.5);
    }

    @Override public boolean shouldRenderOffScreen(@NotNull CorralBlockEntity be) { return true; }
    @Override public int getViewDistance() { return 96; }
}
