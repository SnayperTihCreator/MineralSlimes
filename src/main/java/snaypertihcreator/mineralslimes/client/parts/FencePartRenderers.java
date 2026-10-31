package snaypertihcreator.mineralslimes.client.parts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import snaypertihcreator.mineralslimes.client.models.FenceModelRegister;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.FencePartType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class FencePartRenderers {
    private FencePartRenderers() {}

    private static final Map<FencePartType<?>,  FencePartRenderer<?>> RENDERERS = new HashMap<>();

    public static void registerRenderer(FencePartType<?> type, FencePartRenderer<?> renderer) {
        RENDERERS.put(type, renderer);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static  FencePartRenderer<FencePart> get(FencePartType<?> type) {
        return (FencePartRenderers.FencePartRenderer<FencePart>) RENDERERS.get(type);
    }

    public interface FencePartRenderer<T extends FencePart> {
        void render(T part, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay);
    }

    public static class DefaultFencePartRenderer implements FencePartRenderer<FencePart> {
        public static final DefaultFencePartRenderer INSTANCE = new DefaultFencePartRenderer();

        @Override
        public void render(FencePart part, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            if (FenceModelRegister.hasModel(part))
                drawModel(FenceModelRegister.getModel(part), pose, buffers, light, overlay);
            else if (part.getDisplayState() != null)
                drawState(part.getDisplayState(), pose, buffers, light, overlay);
        }

        @SuppressWarnings("deprecation")
        private void drawState(BlockState state, PoseStack pose, MultiBufferSource buffers, int packedLight, int packedOverlay) {
            Minecraft.getInstance().getBlockRenderer()
                    .renderSingleBlock(state, pose, buffers, packedLight, packedOverlay);
        }

        private static void drawModel(BakedModel model, PoseStack pose, MultiBufferSource buffers, int packedLight, int packedOverlay) {
            VertexConsumer vc = buffers.getBuffer(RenderType.cutout());
            Minecraft.getInstance().getBlockRenderer().getModelRenderer()
                    .renderModel(pose.last(), vc, null, model, 1F, 1F, 1F, packedLight, packedOverlay, ModelData.EMPTY, RenderType.cutout());
        }
    }
}
