package snaypertihcreator.mineralslimes.common.vakpak.inventory;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import snaypertihcreator.mineralslimes.common.vakpak.vacuum.VakPakVacuumContext;

public abstract class VakPakSlot {
    public abstract VakPakSlotType getType();
    public abstract boolean isEmpty();
    public abstract VakPakSlot copy();
    public abstract Component displayName();

    public abstract boolean shoot(Level level, Player player, Vec3 spawnPos, Vec3 shootVec);

    public boolean canBeReplaced() { return false; }
    public boolean canAcceptNewSlot(VakPakSlotType newType) {
        return canBeReplaced() && !newType.isDedicatedZone();
    }

    public boolean canStackEntity(Entity entity) { return false; }
    public boolean vacuumEntity(Entity entity) { return false; }

    public boolean canStackBlock(VakPakVacuumContext ctx) { return false; }
    public boolean vacuumBlock(VakPakVacuumContext ctx) { return false; }

    public static final Codec<VakPakSlot> CODEC = VakPakSlotType.CODEC.dispatch(VakPakSlot::getType, VakPakSlotType::getCodec);
}
