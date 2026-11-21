package snaypertihcreator.mineralslimes.client.hub;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import snaypertihcreator.mineralslimes.common.entity.ModSlimes;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakSlot;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakSlotEntity;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakSlotItem;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakSlotWater;

public final class VakPakSlotRenderers {
    private VakPakSlotRenderers() {}

    public interface VakPakSlotRenderer {
        void render(VakPakSlot slot, GuiGraphics gfx, Minecraft mc, int x, int y, int size);
    }

    public static void renderCount(int count, GuiGraphics gfx, Minecraft mc, int x, int y, int size) {
        renderCount(count, gfx, mc, x, y, size, false);
    }

    public static void renderCount(int count, GuiGraphics gfx, Minecraft mc, int x, int y, int size, boolean isZero) {
        if ((count <= 1) && !isZero) return;
        String s = String.valueOf(count);
        gfx.pose().pushPose();
        gfx.pose().translate(0.0F, 0.0F, 200.0F);
        gfx.drawString(mc.font, s, x + size - mc.font.width(s), y + size - mc.font.lineHeight, 16777215, true);
        gfx.pose().popPose();
    }


    public static final class Empty implements VakPakSlotRenderer {
        @Override
        public void render(VakPakSlot slot, GuiGraphics gfx, Minecraft mc, int x, int y, int size) {
            gfx.blit(VakPakHud.TEXTURE, x, y, 1, 43, size, size, VakPakHud.TEXTURE_SIZE, VakPakHud.TEXTURE_SIZE);
        }
    }

    public static final class Entity implements VakPakSlotRenderer{
        @Override
        public void render(VakPakSlot slot, GuiGraphics gfx, Minecraft mc, int x, int y, int size) {
            if (!(slot instanceof VakPakSlotEntity entitySlot)) return;

            renderIcon(entitySlot, gfx, mc, x, y, size);
            renderCount(entitySlot.count(), gfx, mc, x, y, size);
        }

        private void renderIcon(VakPakSlotEntity slot, GuiGraphics gfx, Minecraft mc, int x, int y, int size) {
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

            if (icon.isEmpty()) UntilSlotRenderers.FALLBACK.render(slot, gfx, mc, x, y, size);
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
            renderCount(stack.getCount(), gfx, mc, x, y, size);
        }
    }

    public static final class Water implements VakPakSlotRenderer {
        @Override
        public void render(VakPakSlot slot, GuiGraphics gfx, Minecraft mc, int x, int y, int size) {
            if (!(slot instanceof VakPakSlotWater waterSlot)) return;

            gfx.blit(VakPakHud.TEXTURE, x, y, 19, 43, size, size, VakPakHud.TEXTURE_SIZE, VakPakHud.TEXTURE_SIZE);

            float k = Math.clamp(waterSlot.millibuckets() / (float) waterSlot.milliCapacity(), 0F, 1F);

            if (k > 0f) {
                int fillHeight = Math.max(1, (int) (size * k));
                int filledTopY = y + (size - fillHeight);

                int srcHeight = Math.max(1, Math.round(16f * fillHeight / size));
                int srcV = 61 + (16 - srcHeight);

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                gfx.blit(VakPakHud.TEXTURE, x, filledTopY, size, fillHeight, 1, srcV, 16, srcHeight, VakPakHud.TEXTURE_SIZE, VakPakHud.TEXTURE_SIZE);
                RenderSystem.disableBlend();
            }

            renderCount(waterSlot.millibuckets(), gfx, mc, x, y, size, true);
        }
    }
}
