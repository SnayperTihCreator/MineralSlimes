package snaypertihcreator.mineralslimes.common.network;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.items.vakpak.VakPakItem;

@EventBusSubscriber(modid = MineralSlimes.MODID, value = Dist.CLIENT)
public class ModNetworkingClient {
    private static final long SHOT_INTERVAL_MS = 250;
    private static long lastShotTime = -1000;

    private static boolean attackHeld = false;
    private static boolean useHeld = false;

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (mc.screen != null) return;
        if (!(player.getMainHandItem().getItem() instanceof VakPakItem)) return;

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (event.getAction() == InputConstants.PRESS) {
                event.setCanceled(true);
                attackHeld = true;
                fireOnce();
            } else if (event.getAction() == InputConstants.RELEASE)
                attackHeld = false;
        }
    }

    @SubscribeEvent
    public static void onUseKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (mc.screen != null) return;
        if (!(player.getMainHandItem().getItem() instanceof VakPakItem)) return;

        event.setSwingHand(false);
        event.setCanceled(true);
        useHeld = true;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        long window = Minecraft.getInstance().getWindow().getWindow();

        if (attackHeld && GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS)
            attackHeld = false;
        if (useHeld && GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) != GLFW.GLFW_PRESS)
            useHeld = false;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        boolean holdingVakPak = player != null && mc.screen == null && player.getMainHandItem().getItem() instanceof VakPakItem;

        if (attackHeld) {
            if (!holdingVakPak) attackHeld = false;
            else fireOnce();
        }

        if (useHeld) {
            if (!holdingVakPak) useHeld = false;
            else PacketDistributor.sendToServer(VakPakPayload.Vacuum.INSTANCE);
        }
    }

    private static void fireOnce() {
        long now = Util.getMillis();
        if (now - lastShotTime < SHOT_INTERVAL_MS) return;
        lastShotTime = now;
        PacketDistributor.sendToServer(VakPakPayload.Shoot.INSTANCE);
    }
}
