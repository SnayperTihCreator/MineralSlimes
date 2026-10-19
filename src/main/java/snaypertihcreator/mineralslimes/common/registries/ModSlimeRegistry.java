package snaypertihcreator.mineralslimes.common.registries;

import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.slime.SlimeType;

@EventBusSubscriber(modid = MineralSlimes.MODID)
public final class ModSlimeRegistry {
    private ModSlimeRegistry() {}

    public static Registry<SlimeType> SLIME_TYPES = new RegistryBuilder<>(ModRegistries.SLIME_TYPE).sync(true).create();

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) {
        event.register(SLIME_TYPES);
    }
}
