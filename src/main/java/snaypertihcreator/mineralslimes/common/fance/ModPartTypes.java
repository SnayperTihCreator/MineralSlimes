package snaypertihcreator.mineralslimes.common.fance;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.fance.parts.GuidePart;
import snaypertihcreator.mineralslimes.common.fance.parts.SupportPart;
import snaypertihcreator.mineralslimes.common.holders.DeferredPartType;
import snaypertihcreator.mineralslimes.common.registries.ModRegistries;

import java.util.function.BiFunction;

public final class ModPartTypes {
    public static final Register FENCE_PART_TYPE = new Register(MineralSlimes.MODID);

    public static final DeferredPartType<SupportPart> SUPPORT_PART_TYPE =
            register("support", SupportPart::new);

    public static final DeferredPartType<GuidePart> GUIDE_PART =
            register("guide", GuidePart::new);

    private static <T extends FencePart> DeferredPartType<T> register(String name, BiFunction<CorralBlockEntity, Float, T> factory) {
        return FENCE_PART_TYPE.register(name, factory);
    }

    public static class Register extends DeferredRegister<FencePartType<?>>{
        public Register(String namespace) {
            super(ModRegistries.FENCE_PART_TYPE, namespace);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected <I extends FencePartType<?>> @NotNull DeferredHolder<FencePartType<?>, I> createHolder(@NotNull ResourceKey<? extends Registry<FencePartType<?>>> registryKey, @NotNull ResourceLocation key) {
            return (DeferredHolder<FencePartType<?>, I>) DeferredPartType.create(key);
        }

        public <T extends FencePart> DeferredPartType<T> register(String name, BiFunction<CorralBlockEntity, Float, T> factory) {
            return (DeferredPartType<T>) register(name, () -> new FencePartType<>(factory));
        }
    }
}