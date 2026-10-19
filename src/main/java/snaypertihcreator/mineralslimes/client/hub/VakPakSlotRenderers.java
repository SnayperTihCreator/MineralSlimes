package snaypertihcreator.mineralslimes.client.hub;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import snaypertihcreator.mineralslimes.common.inventory.VakPakSlot;
import snaypertihcreator.mineralslimes.common.inventory.VakPakSlotType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class VakPakSlotRenderers {
    private VakPakSlotRenderers() {}

    private static final Map<VakPakSlotType, VakPakSlotRenderer> RENDERERS = new ConcurrentHashMap<>();

    private static final VakPakSlotRenderer FALLBACK = (slot, gfx, mc, x, y, size) ->
            gfx.drawString(mc.font, "?", x + 8, y + 6, 0xFF5555, false);

    static {
        register(VakPakSlotType.ITEM, new VakPakSlotRenders.Item());
        register(VakPakSlotType.ENTITY, new VakPakSlotRenders.Entity());
        register(VakPakSlotType.WATER, new VakPakSlotRenders.Water());
        register(VakPakSlotType.EMPTY, ((slot, gfx, mc, x, y, size) -> {}));
    }

    public static void register(VakPakSlotType type, VakPakSlotRenderer renderer) {
        RENDERERS.put(type, renderer);
    }

    public static void  render (VakPakSlot slot, GuiGraphics gfx, Minecraft mc, int x, int y, int size) {
        RENDERERS.getOrDefault(slot.getType(), FALLBACK).render(slot, gfx, mc, x, y, size);
    }
}
