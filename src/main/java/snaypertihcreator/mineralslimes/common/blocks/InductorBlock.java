package snaypertihcreator.mineralslimes.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.blockentity.InductorBlockEntity;
import snaypertihcreator.mineralslimes.common.blockentity.ModBlockEntity;
import snaypertihcreator.mineralslimes.common.fance.CorralHelper;

public class InductorBlock extends PartHolderBlock {

    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 20, 13);

    public InductorBlock(Properties properties) {
        super(properties.strength(1.0F).sound(SoundType.GLASS).lightLevel(state -> 15).noOcclusion());
    }

    @Override
    protected boolean canSurvive(@NotNull BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.getBlock() instanceof CorralBlock || CorralHelper.isConvertibleFence(below);
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!state.canSurvive(level, pos)) level.destroyBlock(pos, true);
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {if (level.getBlockEntity(pos) instanceof CorralBlockEntity inductor)
                inductor.dropAndClearParts(level, pos);
            if (!level.isClientSide && level.getBlockEntity(pos.below()) instanceof CorralBlockEntity corral
                    && corral.isStructureRoot()) {
                CorralHelper.dissolve(level, pos.below());
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new InductorBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, ModBlockEntity.INDUCTOR_BE.get(),
                (lvl, pos, st, be) -> {
                    if (lvl instanceof ServerLevel server && be instanceof InductorBlockEntity inductor) {
                        inductor.serverTick(server);
                        inductor.tickParts();
                    }
                });
    }

    @Override
    protected VoxelShape baseShape(BlockGetter level, BlockPos pos, @Nullable CorralBlockEntity holder) {
        return SHAPE;
    }

    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                           @NotNull BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide || movedByPiston) return;
        convertColumnBelow(level, pos);
    }

    private static void convertColumnBelow(Level level, BlockPos inductorPos) {
        boolean first = true;
        BlockPos cursor = inductorPos.below();
        while (cursor.getY() >= level.getMinBuildHeight()) {
            BlockState state = level.getBlockState(cursor);
            if (CorralHelper.isConvertibleFence(state)) {
                CorralHelper.convertFenceToCorral(level, cursor, first);
                first = false;
            } else if (!(state.getBlock() instanceof CorralBlock)) break;
            cursor = cursor.below();
        }
    }
}