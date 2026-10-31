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

@EventBusSubscriber(modid = MineralSlimes.MODID)
public final class ModRegistries {
    private ModRegistries() {}

    public static final ResourceKey<Registry<SlimeType>> SLIME_TYPE =
            ResourceKey.createRegistryKey(MineralSlimes.modLoc("slime_type"));
    public static final ResourceKey<Registry<FencePartType<?>>> FENCE_PART_TYPE =
            ResourceKey.createRegistryKey(MineralSlimes.modLoc("fence_part_type"));

    public static final Registry<SlimeType> SLIME_TYPES_REGISTRY =
            new RegistryBuilder<>(SLIME_TYPE).sync(true).create();
    public static final Registry<FencePartType<?>> FENCE_PART_TYPES_REGISTRY =
            new RegistryBuilder<>(FENCE_PART_TYPE).sync(true).create();

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) {
        event.register(SLIME_TYPES_REGISTRY);
        event.register(FENCE_PART_TYPES_REGISTRY);
    }
}
