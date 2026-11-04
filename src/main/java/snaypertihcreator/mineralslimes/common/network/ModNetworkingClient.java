package snaypertihcreator.mineralslimes.common.network;

import com.mojang.blaze3d.platform.InputConstants;
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
import snaypertihcreator.mineralslimes.common.items.VakPakItem;

@EventBusSubscriber(modid = MineralSlimes.MODID, value = Dist.CLIENT)
public class ModNetworkingClient {
    private static boolean vaccumActive = false;

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || mc.screen != null) return;
        if (!(player.getMainHandItem().getItem() instanceof VakPakItem)) return;

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == InputConstants.PRESS) {
            event.setCanceled(true);
            PacketDistributor.sendToServer(VakPakPayload.Shoot.INSTANCE);
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
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) { stopVacuum(); return; }

        boolean holdingVakPak = mc.screen == null && mc.isWindowActive()
                && player.getMainHandItem().getItem() instanceof VakPakItem;

        if (!holdingVakPak) { stopVacuum(); return; }

        long window = mc.getWindow().getWindow();
        boolean lmb = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rmb = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        boolean  hasAmmo = !VakPakItem.getInventory(player.getMainHandItem()).peekSelected().isEmpty();
        if (lmb && hasAmmo) PacketDistributor.sendToServer(VakPakPayload.Shoot.INSTANCE);

        if (rmb && !vaccumActive){
            vaccumActive = true;
            PacketDistributor.sendToServer(new VakPakPayload.Vacuum(true));
        } else if (!rmb && vaccumActive) stopVacuum();
    }

    private static void stopVacuum() {
        if (vaccumActive) {
            vaccumActive = false;
            PacketDistributor.sendToServer(new VakPakPayload.Vacuum(false));
        }
    }
}