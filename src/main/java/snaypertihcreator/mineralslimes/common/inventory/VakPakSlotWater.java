package snaypertihcreator.mineralslimes.common.inventory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import snaypertihcreator.mineralslimes.common.items.vakpak.VakPakVacuumContext;

public final class VakPakSlotWater extends VakPakSlot{
    public static final int CAPACITY = 8;
    public static final String DISPLAY_NAME = "mineralslimes.vakpak.water_slot";

    public static final MapCodec<VakPakSlotWater> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.intRange(0, CAPACITY).fieldOf("buckets").forGetter(VakPakSlotWater::buckets)
    ).apply(inst, VakPakSlotWater::new));

    private int buckets;

    public VakPakSlotWater(int buskets){
        this.buckets = Math.clamp(buskets, 0, CAPACITY);
    }

    @SuppressWarnings("unused")
    public VakPakSlotWater(){ this(0); }

    public int buckets()     { return buckets; }
    public boolean isFull()  { return buckets >= CAPACITY; }

    @Override public VakPakSlotType getType() { return VakPakSlotType.WATER; }
    @Override public boolean isEmpty()         { return buckets <= 0; }
    @Override public VakPakSlot copy()         { return new VakPakSlotWater(buckets); }

    @Override
    public Component displayName() {
        return Component.translatable(DISPLAY_NAME, buckets, CAPACITY);
    }

    @Override
    public boolean shoot(Level level, Player player, Vec3 spawnPos, Vec3 shootVec) {
        if (buckets <= 0) return false;
        buckets--;

        BlockPos placePos = BlockPos.containing(spawnPos);
        BlockState existing = level.getBlockState(placePos);
        if (existing.isAir() || existing.canBeReplaced())
            level.setBlock(placePos, Blocks.WATER.defaultBlockState(), 3);

        level.playSound(null, placePos, SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 1.0f, 1.0f);
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
        ctx.getLevel().playSound(null, ctx.getPos(), SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
        return new VakPakSlotWater(1);
    }

    @Override
    public boolean canStackBlock(VakPakVacuumContext ctx) {
        return !isFull() && canCreateFromBlock(ctx.getState());
    }

    @Override
    public boolean vacuumBlock(VakPakVacuumContext ctx) {
        if (!canStackBlock(ctx)) return false;
        ctx.getLevel().setBlock(ctx.getPos(), Blocks.AIR.defaultBlockState(), 3);
        ctx.getLevel().playSound(null, ctx.getPos(), SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
        buckets++;
        return true;
    }


}
