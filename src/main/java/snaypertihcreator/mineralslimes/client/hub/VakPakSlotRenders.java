package snaypertihcreator.mineralslimes.client.hub;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import snaypertihcreator.mineralslimes.common.entity.ModSlimes;
import snaypertihcreator.mineralslimes.common.inventory.VakPakSlot;
import snaypertihcreator.mineralslimes.common.inventory.VakPakSlotEntity;
import snaypertihcreator.mineralslimes.common.inventory.VakPakSlotItem;
import snaypertihcreator.mineralslimes.common.inventory.VakPakSlotWater;

public final class VakPakSlotRenders {
    private VakPakSlotRenders() {}

    public static final class Entity implements VakPakSlotRenderer{
        @Override
        public void render(VakPakSlot slot, GuiGraphics gfx, Minecraft mc, int x, int y, int size) {
            if (!(slot instanceof VakPakSlotEntity entitySlot)) return;

            renderIcon(entitySlot, gfx, mc, x, y);
            if (entitySlot.count() > 1) {
                ItemStack countProxy = new ItemStack(Items.STONE, entitySlot.count());
                countProxy.set(DataComponents.CUSTOM_NAME, Component.empty());
                gfx.renderItemDecorations(mc.font, countProxy, x, y);
            }
        }

        private void renderIcon(VakPakSlotEntity slot, GuiGraphics gfx, Minecraft mc, int x, int y) {
            String id = slot.getEntityData().getString("id");
            EntityType<?> type = EntityType.byString(id).orElse(null);

            ItemStack icon = ItemStack.EMPTY;
            if (type != null) {
                var slimeIcon = ModSlimes.iconOf(type);
                if (slimeIcon.isPresent()) icon = new ItemStack(slimeIcon.get());
                else {
                    SpawnEggItem egg = SpawnEggItem.byId(type);
                    if (egg != null) icon = new ItemStack(egg);
                }
            }

            if (icon.isEmpty()) gfx.drawString(mc.font, "?", x + 7, y + 6, 0xFFFFFF, false);
            else gfx.renderItem(icon, x, y);
        }
    }

    public static final class Item implements VakPakSlotRenderer {
        @Override
        public void render(VakPakSlot slot, GuiGraphics gfx, Minecraft mc, int x, int y, int size) {
            if (!(slot instanceof VakPakSlotItem itemSlot)) return;
            ItemStack stack = itemSlot.getStack();
            if (stack.isEmpty()) return;
            gfx.renderItem(stack, x, y);
            gfx.renderItemDecorations(mc.font, stack, x, y);
        }
    }

    public static final class Water implements VakPakSlotRenderer {
        @Override
        public void render(VakPakSlot slot, GuiGraphics gfx, Minecraft mc, int x, int y, int size) {
            if (!(slot instanceof VakPakSlotWater waterSlot)) return;
            if (waterSlot.buckets() <= 0) return;

            gfx.renderItem(new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET), x, y);

            if (waterSlot.buckets() > 1) {
                ItemStack countProxy = new ItemStack(net.minecraft.world.item.Items.STONE, waterSlot.buckets());
                countProxy.set(DataComponents.CUSTOM_NAME, Component.empty());
                gfx.renderItemDecorations(mc.font, countProxy, x, y);
            }
        }
    }
}
