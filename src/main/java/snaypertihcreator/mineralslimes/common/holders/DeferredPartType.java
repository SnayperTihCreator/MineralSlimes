package snaypertihcreator.mineralslimes.common.holders;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.FencePartType;
import snaypertihcreator.mineralslimes.common.registries.ModRegistries;


public class DeferredPartType<T extends FencePart> extends DeferredHolder<FencePartType<?>, FencePartType<T>> {

    protected DeferredPartType(ResourceKey<FencePartType<?>> key) {
        super(key);
    }

    protected DeferredPartType(DeferredHolder<FencePartType<?>, FencePartType<T>> holder) {
        super(holder.getKey());
    }

    public static <T extends FencePart> DeferredPartType<T> create(ResourceLocation id) {
        return new DeferredPartType<>(ResourceKey.create(ModRegistries.FENCE_PART_TYPE, id));
    }

    public FencePartType<T> type() {
        return get();
    }

    public ItemStack createStack() {
        return get().createStack();
    }

    public ResourceLocation modelId() {
        return get().getModelId();
    }
}
