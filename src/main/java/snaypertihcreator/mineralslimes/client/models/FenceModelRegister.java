package snaypertihcreator.mineralslimes.client.models;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.ModPartTypes;

import java.util.HashMap;
import java.util.Map;

public final class FenceModelRegister {
    private static final Map<ResourceLocation, BakedModel> BY_ID = new HashMap<>();
    private FenceModelRegister() {}

    public static ModelResourceLocation modelKey(ResourceLocation location) {
        return ModelResourceLocation.standalone(location);
    }

    public static void registerModel(ModelEvent.BakingCompleted event){
        BY_ID.clear();
        for (var part : ModPartTypes.FENCE_PART_TYPE.getEntries()) {
            var model_id = part.get().getModelId();
            BY_ID.put(model_id, event.getModels().get(modelKey(model_id)));
        }
    }

    public static boolean hasModel(FencePart part){
        ResourceLocation id = part.getType().getModelId();
        return id != null && BY_ID.containsKey(id);
    }

    public static BakedModel getModel(FencePart part){
        ResourceLocation id = part.getType().getModelId();
        if (id == null) return Minecraft.getInstance().getModelManager().getMissingModel();
        return BY_ID.getOrDefault(id, Minecraft.getInstance().getModelManager().getMissingModel());
    }

}
