package snaypertihcreator.mineralslimes.client.models;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.FencePartType;

public class PartModel<T extends FencePart>{
    protected final FencePartType<T> type;

    public PartModel(FencePartType<T> type) {
        this.type = type;
    }

    public FencePartType<T> getType() {
        return type;
    }

    public ResourceLocation getModelResource(@NotNull T part){
        return part.getModelId();
    }

    public RenderType getRenderType(T part) {
        return RenderType.cutout();
    }

    public BakedModel getModel(T part) {return FenceModelRegister.baked(getModelResource(part));}

    public void registerModel(@NotNull ModelEvent.RegisterAdditional event) {
        event.register(FenceModelRegister.modelKey(type.modelId()));
        for (String variant : type.getVariants()) event.register(FenceModelRegister.modelKey(type.modelId(variant)));
    }

}
