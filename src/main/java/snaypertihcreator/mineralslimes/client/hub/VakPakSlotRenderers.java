package snaypertihcreator.mineralslimes.client.hub;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import snaypertihcreator.mineralslimes.common.entity.ModSlimes;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakSlot;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakSlotEntity;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakSlotItem;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakSlotWater;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

public final class VakPakSlotRenderers {
    private VakPakSlotRenderers() {}

    public interface VakPakSlotRenderer {
        void render(VakPakSlot slot, GuiGraphics gfx, Minecraft mc, int x, int y, int size);
    }

    public static void renderCount(int count, GuiGraphics gfx, Minecraft mc, int x, int y){
        renderCount(count, gfx, mc, x, y, false);
    }

    public static void renderCount(int count, GuiGraphics gfx, Minecraft mc, int x, int y, boolean isZero) {
        if ((count <= 1) && !isZero) return;
        String s = String.valueOf(count);
        gfx.pose().pushPose();
        gfx.pose().translate(0.0F, 0.0F, 200.0F);
        gfx.drawString(mc.font, s, x + 19 - 2 - mc.font.width(s), y + 6 + 3, 16777215, true);
        gfx.pose().popPose();
    }

    public static final class Empty implements VakPakSlotRenderer {
        @Override
        public void render(VakPakSlot slot, GuiGraphics gfx, Minecraft mc, int x, int y, int size) {
            gfx.blit(VakPakHud.TEXTURE, x, y, 41, 45, size, size, VakPakHud.TEXTURE_SIZE, VakPakHud.TEXTURE_SIZE);
        }
    }

    public static final class Entity implements VakPakSlotRenderer{
        @Override
        public void render(VakPakSlot slot, GuiGraphics gfx, Minecraft mc, int x, int y, int size) {
            if (!(slot instanceof VakPakSlotEntity entitySlot)) return;

            renderIcon(entitySlot, gfx, mc, x, y);
            renderCount(entitySlot.count(), gfx, mc, x, y);
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
            renderCount(stack.getCount(), gfx, mc, x, y);
        }
    }

    public static final class Water implements VakPakSlotRenderer {
        private static final ResourceLocation TEXTURE_WATER = modLoc("textures/gui/water.png");

        @Override
        public void render(VakPakSlot slot, GuiGraphics gfx, Minecraft mc, int x, int y, int size) {
            if (!(slot instanceof VakPakSlotWater waterSlot)) return;

            gfx.blit(VakPakHud.TEXTURE, x, y, 23, 45, size, size, VakPakHud.TEXTURE_SIZE, VakPakHud.TEXTURE_SIZE);

            float k = Math.clamp(waterSlot.buckets() / (float) waterSlot.capacity(), 0F, 1F);

            if (k > 0f) {
                int padding = 2;

                int drawX = x + padding - 2;
                int drawY = y + padding - 2;
                int drawSize = size - (padding * 2);

                int fillHeight = (int) (drawSize * k);
                if (fillHeight == 0) fillHeight = 1;

                int filledTopY = drawY + (drawSize - fillHeight);

                int srcHeight = Math.max(1, Math.round(16f * fillHeight / drawSize));
                int srcY = 16 - srcHeight;
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                gfx.blit(TEXTURE_WATER, drawX, filledTopY, drawSize, fillHeight, 0, srcY, 16, srcHeight, 16, 16);
                RenderSystem.disableBlend();
            }

            renderCount(waterSlot.buckets(), gfx, mc, x, y, true);
        }
    }
}
