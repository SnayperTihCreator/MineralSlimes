package snaypertihcreator.mineralslimes.client.hub;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakSlot;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakSlotType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UntilSlotRenderers {
    private UntilSlotRenderers() {}

    private static final Map<VakPakSlotType, VakPakSlotRenderers.VakPakSlotRenderer> RENDERERS = new ConcurrentHashMap<>();

    static final VakPakSlotRenderers.VakPakSlotRenderer FALLBACK = (slot, gfx, mc, x, y, size) ->
            gfx.drawString(mc.font, "?", x + (size - mc.font.width("?")) / 2, y + (size - mc.font.lineHeight) / 2, 0xFF5555, false);

    static {
        register(VakPakSlotType.ITEM, new VakPakSlotRenderers.Item());
        register(VakPakSlotType.ENTITY, new VakPakSlotRenderers.Entity());
        register(VakPakSlotType.WATER, new VakPakSlotRenderers.Water());
        register(VakPakSlotType.EMPTY, new VakPakSlotRenderers.Empty());
    }

    public static void register(VakPakSlotType type, VakPakSlotRenderers.VakPakSlotRenderer renderer) {
        RENDERERS.put(type, renderer);
    }

    public static void  render (VakPakSlot slot, GuiGraphics gfx, Minecraft mc, int x, int y, int size) {
        RENDERERS.getOrDefault(slot.getType(), FALLBACK).render(slot, gfx, mc, x, y, size);
    }
}
