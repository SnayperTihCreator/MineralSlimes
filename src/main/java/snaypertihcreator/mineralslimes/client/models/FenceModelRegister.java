package snaypertihcreator.mineralslimes.client.models;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.FencePartType;
import snaypertihcreator.mineralslimes.common.fance.ModPartTypes;

import java.util.HashMap;
import java.util.Map;

public final class FenceModelRegister {
    private static final Map<FencePartType<?>, PartModel<?>> MODELS = new HashMap<>();
    private FenceModelRegister() {}

    public static ModelResourceLocation modelKey(ResourceLocation location) {
        return ModelResourceLocation.standalone(location);
    }

    @SuppressWarnings("unchecked")
    public static <T extends FencePart> PartModel<T> getModel(FencePartType<T> type) {
        return (PartModel<T>) MODELS.computeIfAbsent(type, PartModel::new);
    }

    public static @NotNull BakedModel missingModel() {
        return Minecraft.getInstance().getModelManager().getMissingModel();
    }

    public static @NotNull BakedModel baked(ResourceLocation id) {
        return Minecraft.getInstance().getModelManager().getModel(modelKey(id));
    }

    public static void registerModel(ModelEvent.RegisterAdditional event) {
        for (var holder : ModPartTypes.FENCE_PART_TYPE.getEntries())
            getModel(holder.get()).registerModel(event);
    }

    public static void bake(ModelEvent.BakingCompleted event) {
        for (var holder : ModPartTypes.FENCE_PART_TYPE.getEntries()) {
            var type = holder.get();
            check(event, type.modelId());
            for (String variant : type.getVariants()) check(event, type.modelId(variant));
        }
    }

    private static void check(ModelEvent.BakingCompleted event, ResourceLocation id) {
        if (event.getModels().get(modelKey(id)) == null)
            MineralSlimes.LOGGER.warn("Missing baked model for fence part: {}", id);
    }

}
