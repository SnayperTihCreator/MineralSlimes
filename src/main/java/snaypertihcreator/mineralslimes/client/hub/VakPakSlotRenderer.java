package snaypertihcreator.mineralslimes.client.hub;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import snaypertihcreator.mineralslimes.common.inventory.VakPakSlot;

public interface VakPakSlotRenderer {
    void render(VakPakSlot slot, GuiGraphics gfx, Minecraft mc, int x, int y, int size);
}
