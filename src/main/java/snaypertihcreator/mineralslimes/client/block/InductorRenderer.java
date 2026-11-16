package snaypertihcreator.mineralslimes.client.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.client.parts.FencePartRenderers;
import snaypertihcreator.mineralslimes.client.parts.WrenchArrowRenderer;
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

        WrenchArrowRenderer.tryRender(inductor.getBlockPos(), pose, buffers);
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
