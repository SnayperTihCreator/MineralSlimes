package snaypertihcreator.mineralslimes.client.hub;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.ModDataComponents;
import snaypertihcreator.mineralslimes.common.inventory.VakPakInventory;
import snaypertihcreator.mineralslimes.common.items.vakpak.*;
import snaypertihcreator.mineralslimes.common.network.VakPakPayload;

import java.util.Set;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

@EventBusSubscriber(modid = MineralSlimes.MODID, value = Dist.CLIENT)
public class VakPakHud {
    private static final Set<ResourceLocation> HIDDEN_LAYERS = Set.of(
            VanillaGuiLayers.HOTBAR,
            VanillaGuiLayers.EXPERIENCE_BAR,
            VanillaGuiLayers.EXPERIENCE_LEVEL,
            VanillaGuiLayers.PLAYER_HEALTH,
            VanillaGuiLayers.ARMOR_LEVEL,
            VanillaGuiLayers.FOOD_LEVEL,
            VanillaGuiLayers.AIR_LEVEL,
            VanillaGuiLayers.VEHICLE_HEALTH,
            VanillaGuiLayers.JUMP_METER,
            VanillaGuiLayers.SELECTED_ITEM_NAME
    );
    private static final int SLOT_SIZE = 22;
    private static final int SLOT_GAP = 2;
    private static final int TEXTURE_SIZE = 256;
    private static final int SHOW_TIME_TITLE = 1500;
    public static final ResourceLocation ID = modLoc("vakpak_hud");
    private static final ResourceLocation TEXTURE = modLoc("textures/gui/vakpakhub.png");

    public static final String DISPLAY_CATEGORY = "key.categories.mineralslimes";
    public static final String DISPLAY_KEY = "key.mineralslimes.vakpak_scroll";
    public static final KeyMapping VAKPAK_SCROLL_KEY = new KeyMapping(
            DISPLAY_KEY, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, DISPLAY_CATEGORY
    );

    private static int lastSelected = -1;
    private static long nameShowUntil = 0;

    @SubscribeEvent
    public static void onGuiLayer(RenderGuiLayerEvent.Pre event){
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null) return;
        if (!(player.getMainHandItem().getItem() instanceof VakPakItem)) return;
        if (HIDDEN_LAYERS.contains(event.getName())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRegisterLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ID, VakPakHud::render);
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(VAKPAK_SCROLL_KEY);
    }

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!(player.getMainHandItem().getItem() instanceof VakPakItem)) return;
        if (!VAKPAK_SCROLL_KEY.isDown()) return;

        if (event.getScrollDeltaY() == 0) return;
        event.setCanceled(true);

        int delta = event.getScrollDeltaY() < 0 ? 1 : -1;
        PacketDistributor.sendToServer(new VakPakPayload.Select(delta));
    }

    private static void render(GuiGraphics gfx, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof VakPakItem)) return;

        VakPakInventory inv = stack.get(ModDataComponents.VAKPAK_INVENTORY);
        if (inv == null) inv = new VakPakInventory();

        int count = inv.size();
        int sel = inv.selectedIndex();
        int totalWidth = count * SLOT_SIZE;
        int x = gfx.guiWidth() / 2 - totalWidth / 2;
        int y = gfx.guiHeight() - 21;
        int stride = SLOT_SIZE + SLOT_GAP;

        if (sel != lastSelected) {
            lastSelected = sel;
            nameShowUntil = Util.getMillis() + SHOW_TIME_TITLE;
        }

        if (Util.getMillis() < nameShowUntil) {
            Component name = inv.getSlot(sel).displayName();
            if (name != null) {
                gfx.drawCenteredString(mc.font, name, gfx.guiWidth() / 2, y - 12, 0xFFFFFF);
            }
        }

        if (VAKPAK_SCROLL_KEY.isDown()) {
            int sx = x + (count+2) * stride;
            gfx.blit(TEXTURE, sx, y, 0, 0, 20, 20, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        for (int i = 0; i < count; i++) {
            int sx = x + i * stride;

            gfx.blit(TEXTURE, sx, y, 0, 20, SLOT_SIZE, SLOT_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
            VakPakSlotRenderers.render(inv.getSlot(i), gfx, mc, sx + 3, y + 3, SLOT_SIZE - 2);

            if (i == sel)
                gfx.blit(TEXTURE, sx, y, 0, 42, SLOT_SIZE, SLOT_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }
}
