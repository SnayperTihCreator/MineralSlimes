package snaypertihcreator.mineralslimes.common.fance;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.items.CorralPartItem;
import snaypertihcreator.mineralslimes.common.registries.ModRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

public class FencePartType<T extends FencePart> {
    private final BiFunction<CorralBlockEntity, Float, T> factory;
    private final List<String> variants;
    @Nullable private CorralPartItem<T> cachedItem;

    public FencePartType(BiFunction<CorralBlockEntity, Float, T> factory) {
        this(factory, List.of());
    }

    public FencePartType(BiFunction<CorralBlockEntity, Float, T> factory, List<String> variants) {
        this.factory = Objects.requireNonNull(factory, "factory");
        this.variants = List.copyOf(variants);
    }

    public ResourceLocation getId() {
        return ModRegistries.FENCE_PART_TYPES_REGISTRY.getKey(this);
    }

    public String getName() {
        ResourceLocation id = getId();
        return id == null ? "unregistered" : id.getPath();
    }

    public List<String> getVariants() {
        return variants;
    }

    public boolean hasVariant(@Nullable String variant) {
        return variant == null || variant.isEmpty() || variants.contains(variant);
    }

    public ResourceLocation modelId(@Nullable String variant) {
        String name = getName();
        return (variant == null || variant.isEmpty())? modLoc("corral/%s".formatted(name)):
                modLoc("corral/%s_%s".formatted(name, variant));
    }

    public ResourceLocation modelId() {
        return modLoc("corral/%s".formatted(getName()));
    }

    public Item asItem() {
        if (cachedItem == null)
            cachedItem = CorralPartItem.byType(this);
        return cachedItem;
    }

    public T create(CorralBlockEntity host, float angle) { return factory.apply(host, angle); }

    public ItemStack createStack() { return new ItemStack(asItem()); }
}
