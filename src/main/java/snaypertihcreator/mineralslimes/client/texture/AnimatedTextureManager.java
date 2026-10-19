package snaypertihcreator.mineralslimes.client.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(value = Dist.CLIENT, modid = MineralSlimes.MODID)
public class AnimatedTextureManager {
    private static final Set<ResourceLocation> TO_REGISTER = ConcurrentHashMap.newKeySet();
    private static final Map<ResourceLocation, AnimatedDirectTexture> REGISTRY = new ConcurrentHashMap<>();

    public static void register(ResourceLocation location) {
        TO_REGISTER.add(location);
        doRegister(location);
    }

    public static void registerAll(ResourceLocation @NotNull ... locations) {
        for (ResourceLocation loc : locations) register(loc);
    }

    private static void doRegister(ResourceLocation location) {
        AnimatedDirectTexture tex = new AnimatedDirectTexture(location);
        Minecraft.getInstance().getTextureManager().register(location, tex);
        REGISTRY.put(location, tex);
    }

    public static void onRegisterReload(@NotNull RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) rm -> {
            for (ResourceLocation loc : TO_REGISTER) doRegister(loc);
        });
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        for (AnimatedDirectTexture tex : REGISTRY.values()) tex.tick();
    }
}