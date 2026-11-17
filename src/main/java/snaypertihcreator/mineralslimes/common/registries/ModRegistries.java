package snaypertihcreator.mineralslimes.common.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.fance.FencePartType;
import snaypertihcreator.mineralslimes.common.slime.SlimeType;
import snaypertihcreator.mineralslimes.common.vakpak.upgrade.VakPakUpgradeType;

@EventBusSubscriber(modid = MineralSlimes.MODID)
public final class ModRegistries {
    private ModRegistries() {}

    public static final class Keys {
        private Keys() {}

        public static final ResourceKey<Registry<SlimeType>> SLIME_TYPE =
                ResourceKey.createRegistryKey(MineralSlimes.modLoc("slime_type"));
        public static final ResourceKey<Registry<FencePartType<?>>> FENCE_PART_TYPE =
                ResourceKey.createRegistryKey(MineralSlimes.modLoc("fence_part_type"));
        public static final ResourceKey<Registry<VakPakUpgradeType>> VAKPAK_UPGRADE_TYPE =
                ResourceKey.createRegistryKey(MineralSlimes.modLoc("vakpak_upgrade_type"));
    }

    public static final class Registries {
        private Registries() {}

        public static final Registry<SlimeType> SLIME_TYPES =
                new RegistryBuilder<>(Keys.SLIME_TYPE).sync(true).create();
        public static final Registry<FencePartType<?>> FENCE_PART_TYPES =
                new RegistryBuilder<>(Keys.FENCE_PART_TYPE).sync(true).create();
        public static final Registry<VakPakUpgradeType> VAKPAK_UPGRADE_TYPES =
                new RegistryBuilder<>(Keys.VAKPAK_UPGRADE_TYPE).sync(true).create();
    }

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) {
        event.register(Registries.SLIME_TYPES);
        event.register(Registries.FENCE_PART_TYPES);
        event.register(Registries.VAKPAK_UPGRADE_TYPES);
    }
}
