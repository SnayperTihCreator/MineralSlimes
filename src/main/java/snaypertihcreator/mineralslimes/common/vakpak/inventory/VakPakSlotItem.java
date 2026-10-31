package snaypertihcreator.mineralslimes.common.vakpak.inventory;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import snaypertihcreator.mineralslimes.common.vakpak.vacuum.VakPakVacuumContext;

public final class VakPakSlotItem extends VakPakSlot {
    private final ItemStack stack;

    public static final MapCodec<VakPakSlotItem> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ItemStack.CODEC.fieldOf("data").forGetter(VakPakSlotItem::getStack)
    ).apply(inst, VakPakSlotItem::new));

    public VakPakSlotItem(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override public VakPakSlotType getType() { return VakPakSlotType.ITEM; }
    @Override public boolean isEmpty() { return stack.isEmpty(); }
    @Override public VakPakSlot copy() { return new VakPakSlotItem(stack.copy()); }
    @Override public Component displayName() {
        return stack.getHoverName();
    }
    @Override public boolean canBeReplaced() { return isEmpty(); }

    @Override public boolean equals(Object obj) {
        return obj instanceof VakPakSlotItem other && ItemStack.matches(this.stack, other.stack);
    }
    @Override public int hashCode() { return this.stack.hashCode(); }

    @Override
    public boolean shoot(Level level, Player player, Vec3 spawnPos, Vec3 shootVec) {
        ItemStack toShoot = stack.copyWithCount(1);

        ItemEntity itemEntity = new ItemEntity(level, spawnPos.x, spawnPos.y, spawnPos.z, toShoot);
        itemEntity.setDeltaMovement(shootVec);
        itemEntity.setPickUpDelay(20);
        level.addFreshEntity(itemEntity);

        stack.shrink(1);
        return true;
    }

    public static boolean canCreateFromEntity(Entity entity) {
        return entity instanceof ItemEntity ie && !ie.getItem().isEmpty();
    }

    public static VakPakSlotItem createFromEntity(Entity entity) {
        ItemEntity ie = (ItemEntity) entity;
        VakPakSlotItem slot = new VakPakSlotItem(ie.getItem().copy());
        ie.getItem().setCount(0);
        ie.discard();
        return slot;
    }

    @Override
    public boolean canStackEntity(Entity entity) {
        return entity instanceof ItemEntity ie && canAccept(ie.getItem());
    }

    @Override
    public boolean vacuumEntity(Entity entity) {
        if (!(entity instanceof ItemEntity ie)) return false;
        int before = ie.getItem().getCount();
        boolean added = tryStack(ie.getItem());
        if (ie.getItem().isEmpty()) ie.discard();
        else ie.setItem(ie.getItem());
        return added && ie.getItem().getCount() != before;
    }


    @SuppressWarnings("unused")
    public static boolean canCreateFromBlock(BlockState state) { return false; }
    @SuppressWarnings("unused")
    public static VakPakSlotItem createFromBlock(VakPakVacuumContext ctx) { return null; }


    public boolean canAccept(ItemStack incoming) {
        return ItemStack.isSameItemSameComponents(this.stack, incoming) && this.stack.getCount() < this.stack.getMaxStackSize();
    }

    public boolean tryStack(ItemStack incoming) {
        if (!canAccept(incoming)) return false;
        int space = this.stack.getMaxStackSize() - this.stack.getCount();
        int toTransfer = Math.min(space, incoming.getCount());
        this.stack.grow(toTransfer);
        incoming.shrink(toTransfer);
        return true;
    }
}