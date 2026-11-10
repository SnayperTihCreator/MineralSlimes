package snaypertihcreator.mineralslimes.client.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.client.parts.FencePartRenderers;
import snaypertihcreator.mineralslimes.common.blockentity.InductorBlockEntity;

public class InductorRenderer implements BlockEntityRenderer<InductorBlockEntity> {
    @SuppressWarnings("unused")
    public InductorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@NotNull InductorBlockEntity inductor, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay) {
        for (var part : inductor.getParts()) {
            var renderer = FencePartRenderers.get(part.getType());
            pose.pushPose();
            CorralRenderer.transformToPart(pose, part);
            renderer.render(part, partialTick, pose, buffers, packedLight, packedOverlay);
            pose.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull InductorBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
