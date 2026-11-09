package snaypertihcreator.mineralslimes.common.fance;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.blocks.CorralBlock;
import snaypertihcreator.mineralslimes.common.blocks.InductorBlock;
import snaypertihcreator.mineralslimes.common.blocks.ModBlocks;

public final class CorralHelper {

    public static final int MAX_STRUCTURE = 256;

    private static final Direction[] SCAN_DIRECTIONS = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST,
            Direction.DOWN
    };

    private CorralHelper() {}

    public static boolean isConvertibleFence(BlockState state) {
        return state.is(BlockTags.FENCES) && !(state.getBlock() instanceof CorralBlock);
    }

    public static BlockState makeCorralState(BlockState fenceState) {
        BlockState corral = ModBlocks.CORRAL.get().defaultBlockState();
        if (fenceState.hasProperty(BlockStateProperties.WATERLOGGED)
                && fenceState.getValue(BlockStateProperties.WATERLOGGED)) {
            corral = corral.setValue(BlockStateProperties.WATERLOGGED, true);
        }
        return corral;
    }

    @Nullable
    public static CorralBlockEntity convertFenceToCorral(Level level, BlockPos pos, boolean playSound) {
        BlockState fence = level.getBlockState(pos);
        if (!isConvertibleFence(fence)) return null;

        level.setBlock(pos, makeCorralState(fence), Block.UPDATE_ALL);

        CorralBlockEntity corral = level.getBlockEntity(pos) instanceof CorralBlockEntity be ? be : null;
        if (corral != null) corral.setStoredFence(fence.getBlock());

        if (playSound && !level.isClientSide) {
            level.playSound(null, pos, fence.getSoundType(level, pos, null).getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return corral;
    }

    public static void scheduleRescan(Level level, BlockPos rootPos) {
        level.scheduleTick(rootPos, ModBlocks.CORRAL.get(), 2);
    }

    public static void onScheduledTick(Level level, BlockPos rootPos) {
        if (!(level.getBlockEntity(rootPos) instanceof CorralBlockEntity root)) return;
        if (!root.isStructureRoot()) return;

        if (!(level.getBlockState(rootPos.above()).getBlock() instanceof InductorBlock)) {
            dissolve(level, rootPos);
            return;
        }
        rescan(level, rootPos);
    }

    public static void rescan(Level level, BlockPos rootPos) {
        if (!(level.getBlockState(rootPos).getBlock() instanceof CorralBlock)) return;
        if (!(level.getBlockEntity(rootPos) instanceof CorralBlockEntity root)) return;

        Set<BlockPos> structure = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        structure.add(rootPos);
        queue.add(rootPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (Direction dir : SCAN_DIRECTIONS) {
                tryAbsorb(level, structure, queue, rootPos, current.relative(dir));
            }
        }

        for (BlockPos pos : structure)
            if (level.getBlockEntity(pos) instanceof CorralBlockEntity corral && !rootPos.equals(corral.getControllerPos()))
                corral.setControllerPos(rootPos);

        Set<BlockPos> old = root.getStructure();
        if (old != null) for (BlockPos pos : old)
            if (!structure.contains(pos) && level.getBlockEntity(pos) instanceof CorralBlockEntity orphan && rootPos.equals(orphan.getControllerPos()))
                orphan.setControllerPos(null);

        root.setStructure(structure);
    }

    private static void tryAbsorb(Level level, Set<BlockPos> structure, Queue<BlockPos> queue,
                                  BlockPos rootPos, BlockPos pos) {
        if (structure.contains(pos)) return;

        BlockState state = level.getBlockState(pos);

        if (isConvertibleFence(state)) {
            if (structure.size() >= MAX_STRUCTURE) return;
            convertFenceToCorral(level, pos, false);
            structure.add(pos);
            queue.add(pos);
        } else if (state.getBlock() instanceof CorralBlock
                && level.getBlockEntity(pos) instanceof CorralBlockEntity corral
                && (corral.getControllerPos() == null || rootPos.equals(corral.getControllerPos()))) {
            structure.add(pos);
            queue.add(pos);
        }
    }

    public static void dissolve(Level level, BlockPos rootPos) {
        if (!(level.getBlockEntity(rootPos) instanceof CorralBlockEntity root)) return;

        Set<BlockPos> old = root.getStructure();
        if (old != null) {
            for (BlockPos pos : old) {
                if (level.getBlockEntity(pos) instanceof CorralBlockEntity corral) {
                    corral.setControllerPos(null);
                }
            }
        }
        root.setStructure(null);
        root.setControllerPos(null);
    }
}