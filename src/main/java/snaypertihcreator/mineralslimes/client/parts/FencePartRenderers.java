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
import snaypertihcreator.mineralslimes.client.models.PartModel;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.FencePartType;

import java.util.HashMap;
import java.util.Map;

public final class FencePartRenderers {
    private FencePartRenderers() {}

    private static final Map<FencePartType<?>,  FencePartRenderer<?>> RENDERERS = new HashMap<>();

    public static void register(FencePartType<?> type, FencePartRenderer<?> renderer) {
        RENDERERS.put(type, renderer);
    }

    @SuppressWarnings("unchecked")
    public static FencePartRenderer<FencePart> get(FencePartType<?> type) {
        return (FencePartRenderer<FencePart>) RENDERERS.computeIfAbsent(
                type, t -> new DefaultFencePartRenderer<>(FenceModelRegister.getModel(t)));
    }

    public interface FencePartRenderer<T extends FencePart> {
        void render(T part, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay);
        PartModel<T> getModel();
    }

    public static void drawModel(BakedModel model, RenderType renderType, PoseStack pose, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        VertexConsumer vc = buffers.getBuffer(renderType);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer()
                .renderModel(pose.last(), vc, null, model, 1F, 1F, 1F, packedLight, packedOverlay, ModelData.EMPTY, renderType);
    }

    @SuppressWarnings("DataFlowIssue")
    public static void drawState(BlockState state, PoseStack pose, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Minecraft.getInstance().getBlockRenderer()
                .renderSingleBlock(state, pose, buffers, packedLight, packedOverlay, ModelData.EMPTY, null);
    }

    public static <T extends FencePart> void drawPart(PartModel<T> model, T part,
                                                      PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BakedModel baked = model.getModel(part);
        if (baked != FenceModelRegister.missingModel())
            drawModel(baked, model.getRenderType(part), pose, buffers, light, overlay);
        else if (part.getDisplayState() != null)
            drawState(part.getDisplayState(), pose, buffers, light, overlay);
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class DefaultFencePartRenderer<T extends FencePart> implements FencePartRenderer<T> {
        private final PartModel<T> model;

        public DefaultFencePartRenderer(PartModel<T> model) { this.model = model; }

        @Override public PartModel<T> getModel() { return model; }

        @Override
        public void render(T part, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            drawPart(getModel(), part, pose, buffers, light, overlay);
        }
    }
}
