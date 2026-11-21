package snaypertihcreator.mineralslimes.common.vakpak.inventory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import snaypertihcreator.mineralslimes.common.entity.ModEntities;
import snaypertihcreator.mineralslimes.common.entity.WaterBullet;
import snaypertihcreator.mineralslimes.common.vakpak.vacuum.VakPakVacuumContext;

import java.util.Objects;
import java.util.Optional;

public final class VakPakSlotWater extends VakPakSlot {

    public static final int CAPACITY_MB = 8000;
    public static final int BUCKET_MB = 1000;
    public static final int SHOOT_COST_MB = 10;
    public static final float PROJECTILE_SPEED = 2.5F;
    public static final float PROJECTILE_INACCURACY = 1.0F;

    public static final int CAPACITY = CAPACITY_MB / BUCKET_MB;
    public static final String DISPLAY_NAME = "mineralslimes.vakpak.water_slot";

    public static final MapCodec<VakPakSlotWater> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.intRange(0, CAPACITY_MB).optionalFieldOf("millibuckets").forGetter(VakPakSlotWater::savedMillibuckets),
            Codec.intRange(0, CAPACITY).optionalFieldOf("buckets").forGetter(w -> Optional.<Integer>empty())
    ).apply(inst, VakPakSlotWater::read));

    private int millibuckets;

    public VakPakSlotWater(int millibuckets) {
        this.millibuckets = Math.clamp(millibuckets, 0, CAPACITY_MB);
    }

    private static VakPakSlotWater read(Optional<Integer> mb, Optional<Integer> legacyBuckets) {
        if (mb.isPresent()) return new VakPakSlotWater(mb.get());
        if (legacyBuckets.isPresent()) return new VakPakSlotWater(legacyBuckets.get() * BUCKET_MB);
        return new VakPakSlotWater(0);
    }

    private Optional<Integer> savedMillibuckets() {
        return millibuckets > 0 ? Optional.of(millibuckets) : Optional.empty();
    }

    public int millibuckets() { return millibuckets; }
    public int milliCapacity() { return CAPACITY_MB; }
    public boolean isFull() { return millibuckets >= CAPACITY_MB; }
    public boolean canShoot() { return millibuckets >= SHOOT_COST_MB; }

    public int addMillibuckets(int amount) {
        int added = Math.clamp(amount, 0, CAPACITY_MB - millibuckets);
        millibuckets += added;
        return added;
    }

    @Override public VakPakSlotType getType() { return VakPakSlotType.WATER; }
    @Override public boolean isEmpty() { return millibuckets <= 0; }
    @Override public VakPakSlot copy() { return new VakPakSlotWater(millibuckets); }

    @Override
    public Component displayName() {
        return Component.translatable(DISPLAY_NAME, millibuckets, CAPACITY_MB);
    }

    @Override
    public boolean shoot(Level level, Player player, Vec3 spawnPos, Vec3 shootVec) {
        if (!canShoot()) return false;

        WaterBullet shot = new WaterBullet(ModEntities.WATER_BULLET.get(), level);
        shot.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot(), player.getXRot());
        shot.setOwner(player);
        shot.shoot(shootVec.x, shootVec.y, shootVec.z, PROJECTILE_SPEED, PROJECTILE_INACCURACY);
        level.addFreshEntity(shot);

        millibuckets -= SHOOT_COST_MB;

        level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.8F, 1.2F);
        return true;
    }

    @SuppressWarnings("unused")
    public static boolean canCreateFromEntity(Entity entity) { return false; }
    @SuppressWarnings("unused")
    public static VakPakSlotWater createFromEntity(Entity entity) { return null; }

    public static boolean canCreateFromBlock(BlockState state) {
        return state.getBlock() == Blocks.WATER;
    }

    public static VakPakSlotWater createFromBlock(VakPakVacuumContext ctx) {
        ctx.getLevel().setBlock(ctx.getPos(), Blocks.AIR.defaultBlockState(), 3);
        ctx.getLevel().playSound(null, ctx.getPos(), SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        return new VakPakSlotWater(BUCKET_MB);
    }

    @Override
    public boolean canStackBlock(VakPakVacuumContext ctx) {
        return !isFull() && ctx.getState().getBlock() == Blocks.WATER;
    }

    @Override
    public boolean vacuumBlock(VakPakVacuumContext ctx) {
        if (!canStackBlock(ctx)) return false;
        ctx.getLevel().setBlock(ctx.getPos(), Blocks.AIR.defaultBlockState(), 3);
        ctx.getLevel().playSound(null, ctx.getPos(), SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        addMillibuckets(BUCKET_MB);
        return true;
    }

    @Override
    public boolean canAcceptNewSlot(VakPakSlotType newType) {
        return isEmpty() && newType == getType();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VakPakSlotWater other && this.millibuckets == other.millibuckets;
    }

    @Override
    public int hashCode() {
        return Objects.hash(millibuckets);
    }
}