package snaypertihcreator.mineralslimes.common.inventory;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import snaypertihcreator.mineralslimes.common.items.vakpak.VakPakVacuumContext;

@SuppressWarnings("unused")
public final class VakPakSlotEmpty extends VakPakSlot{
    public static final VakPakSlotEmpty INSTANCE = new VakPakSlotEmpty();
    public static final Component DISPLAY_NAME = Component.translatable("mineralslimes.vakpak.empty_slot");
    private VakPakSlotEmpty() {}

    public static final MapCodec<VakPakSlotEmpty> CODEC = MapCodec.unit(INSTANCE);

    @Override public VakPakSlotType getType() { return VakPakSlotType.EMPTY; }
    @Override public boolean isEmpty() { return true; }
    @Override public VakPakSlot copy() { return INSTANCE; }
    @Override public Component displayName() {
        return DISPLAY_NAME;
    }

    @Override
    public boolean shoot(Level level, Player player, Vec3 spawnPos, Vec3 shootVec) {
        return true;
    }

    public static boolean canCreateFromEntity(Entity e) { return false; }
    public static VakPakSlotEmpty createFromEntity(Entity e) { return null; }

    public static boolean canCreateFromBlock(BlockState s) { return false; }
    public static VakPakSlotEmpty createFromBlock(VakPakVacuumContext c) { return null; }
}
