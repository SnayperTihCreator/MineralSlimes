package snaypertihcreator.mineralslimes.client.texture;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import snaypertihcreator.mineralslimes.MineralSlimes;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(value = Dist.CLIENT, modid = MineralSlimes.MODID)
public class AnimatedTextureManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, AnimatedDirectTexture> REGISTRY = new ConcurrentHashMap<>();

    public static void register(ResourceLocation location) {
        if (REGISTRY.containsKey(location)) return;
        AnimatedDirectTexture tex = new AnimatedDirectTexture(location);
        Minecraft.getInstance().getTextureManager().register(location, tex);
        REGISTRY.put(location, tex);
    }

    public static void registerAll(ResourceLocation @NotNull ... locations) {
        for (ResourceLocation loc : locations) register(loc);
    }

    public static void onRegisterReload(@NotNull RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) rm -> {
            for (Map.Entry<ResourceLocation, AnimatedDirectTexture> e : REGISTRY.entrySet()) {
                try {
                    e.getValue().load(rm);
                } catch (IOException ex) {
                    LOGGER.warn("Failed to reload animated texture {}, keeping last version", e.getKey(), ex);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        for (AnimatedDirectTexture tex : REGISTRY.values()) tex.tick();
    }
}