package snaypertihcreator.mineralslimes.common.vakpak.upgrade;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.registries.ModRegistries;

import java.util.List;

public final class ModUpgradeTypes {
    public static final Register UPGRADE_TYPES = new Register(MineralSlimes.MODID);

    public static final DeferredUpgradeType SLOT_STACK = register("slot_stack", 5);
    public static final DeferredUpgradeType SLOT_COUNT = register("slot_count", 2);
    public static final DeferredUpgradeType LIQUID_SLOT = register("liquid_slot", 1);
    public static final DeferredUpgradeType WORLD_HARVEST = register("world_harvest", 1);
    public static final DeferredUpgradeType PULSE_WAVE = register("pulse_wave", 1);
    public static final DeferredUpgradeType LOOT_MUL = register("loot_mul", 3);

    private static DeferredUpgradeType register(String name, int maxLevel) {
        return UPGRADE_TYPES.register(name, maxLevel);
    }

    public static List<DeferredUpgradeType> all() {
        return UPGRADE_TYPES.getEntries().stream().map(h -> (DeferredUpgradeType) h).toList();
    }

    public static class Register extends DeferredRegister<VakPakUpgradeType> {
        public Register(String namespace) {
            super(ModRegistries.Keys.VAKPAK_UPGRADE_TYPE, namespace);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected <I extends VakPakUpgradeType> @NotNull DeferredHolder<VakPakUpgradeType, I> createHolder(@NotNull ResourceKey<? extends Registry<VakPakUpgradeType>> registryKey, @NotNull ResourceLocation key) {
            return (DeferredHolder<VakPakUpgradeType, I>) DeferredUpgradeType.create(key);
        }

        public DeferredUpgradeType register(String name, int maxLevel) {
            VakPakUpgradeType type = new VakPakUpgradeType(
                    ResourceLocation.fromNamespaceAndPath(getNamespace(), name), maxLevel);
            return (DeferredUpgradeType) register(name, () -> type);
        }
    }
}
