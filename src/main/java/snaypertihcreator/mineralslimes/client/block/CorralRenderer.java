package snaypertihcreator.mineralslimes.client.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.client.parts.FencePartRenderers;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.fance.FencePart;

public class CorralRenderer implements BlockEntityRenderer<CorralBlockEntity> {

    @SuppressWarnings("unused")
    public CorralRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@NotNull CorralBlockEntity blockEntity, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay) {
        for (FencePart part : blockEntity.getParts()) {
            FencePartRenderers.FencePartRenderer<FencePart> renderer =  FencePartRenderers.get(part.getType());

            pose.pushPose();
            transformToPart(pose, part);
            renderer.render(part, partialTick,  pose, buffers, packedLight, packedOverlay);
            pose.popPose();
        }
    }

    private static void transformToPart(PoseStack pose, FencePart part) {
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
