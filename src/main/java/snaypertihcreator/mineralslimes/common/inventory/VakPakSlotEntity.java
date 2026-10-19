package snaypertihcreator.mineralslimes.common.inventory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import snaypertihcreator.mineralslimes.common.ModTags;
import snaypertihcreator.mineralslimes.common.items.vakpak.VakPakVacuumContext;

import java.util.Objects;

public final class VakPakSlotEntity extends VakPakSlot{
    public static final int MAX_COUNT = 64;
    public static final Component UNKNOWN_ENTITY = Component.translatable("mineralslimes.vakpak.unknown_entity");
    private static final String[] REMOVES_TAG = {"Pos", "Motion", "Rotation", "OnGround", "FallDistance",
            "PortalCooldown", "FallFlying", "Fire", "HurtByTimestamp", "HurtTime", "DeathTime", "UUID"};

    public static final MapCodec<VakPakSlotEntity> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            CompoundTag.CODEC.fieldOf("data").forGetter(VakPakSlotEntity::getEntityData),
            Codec.intRange(1, MAX_COUNT).optionalFieldOf("count", 1).forGetter(VakPakSlotEntity::count)
    ).apply(inst, VakPakSlotEntity::new));

    private final CompoundTag entityData;
    private int count;

    public VakPakSlotEntity(CompoundTag entityData, int count) {
        this.entityData = entityData;
        this.count = count;
    }

    public VakPakSlotEntity(Entity entity) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", EntityType.getKey(entity.getType()).toString());
        entity.saveWithoutId(tag);
        for (String key : REMOVES_TAG) tag.remove(key);
        this.entityData = tag;
        this.count = 1;
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof VakPakSlotEntity other
                && this.entityData.equals(other.entityData)
                && this.count == other.count;
    }
    @Override public int hashCode() { return Objects.hash(entityData, count); }
    @Override public VakPakSlot copy() { return new VakPakSlotEntity(entityData.copy(), count); }
    @Override
    public Component displayName() {
        String id = entityData.getString("id");
        EntityType<?> type = EntityType.byString(id).orElse(null);
        return type != null ? type.getDescription() : UNKNOWN_ENTITY;
    }

    @Override public VakPakSlotType getType() { return VakPakSlotType.ENTITY; }
    @Override public boolean isEmpty() { return count <= 0; }

    @Override
    public boolean shoot(Level level, Player player, Vec3 spawnPos, Vec3 shootVec) {
        Entity entity = EntityType.loadEntityRecursive(entityData, level, spawned -> {
            spawned.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot(), player.getXRot());
            spawned.setDeltaMovement(shootVec);
            return spawned;
        });
        if (entity == null) return false;
        level.addFreshEntity(entity);
        return shrink();
    }

    public static boolean canCreateFromEntity(Entity entity) {
        return entity.getType().is(ModTags.Entities.VAKED);
    }

    public static VakPakSlotEntity createFromEntity(Entity entity) {
        VakPakSlotEntity slot = new VakPakSlotEntity(entity);
        entity.discard();
        return slot;
    }

    @Override
    public boolean canStackEntity(Entity entity) {
        return entity.getType().is(ModTags.Entities.VAKED) && canStackWith(entity.getType()) && count() < MAX_COUNT;
    }

    @Override
    public boolean vacuumEntity(Entity entity) {
        if (!canStackEntity(entity)) return false;
        if (!tryStack(1)) return false;
        entity.discard();
        return true;
    }

    @SuppressWarnings("unused")
    public static boolean canCreateFromBlock(BlockState state) { return false; }
    @SuppressWarnings("unused")
    public static VakPakSlotEntity createFromBlock(VakPakVacuumContext ctx) { return null; }

    public boolean canStackWith(EntityType<?> type) {
        return entityData.getString("id").equals(EntityType.getKey(type).toString());
    }
    public boolean tryStack(int amount) {
        if (count + amount > MAX_COUNT) return false;
        count += amount;
        return true;
    }
    public boolean shrink() {
        return --count <= 0;
    }


    public CompoundTag getEntityData() { return entityData; }
    public int count() { return count; }

}
