package snaypertihcreator.mineralslimes.common.blocks;

import net.minecraft.world.level.block.SoundType;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.fance.CorralHelper;

public class InductorBlock extends Block {

    public InductorBlock(Properties properties) {
        super(properties.strength(1.0F).sound(SoundType.GLASS).lightLevel(state -> 15));
    }

    @Override
    protected boolean canSurvive(@NotNull BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).getBlock() instanceof CorralBlock;
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!state.canSurvive(level, pos)) level.destroyBlock(pos, true);
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !(newState.getBlock() instanceof InductorBlock)
                && level.getBlockEntity(pos.below()) instanceof CorralBlockEntity corral && corral.isStructureRoot()) {
            CorralHelper.dissolve(level, pos.below());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}