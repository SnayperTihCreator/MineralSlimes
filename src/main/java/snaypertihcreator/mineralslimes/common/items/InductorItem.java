package snaypertihcreator.mineralslimes.common.items;

import net.minecraft.world.level.block.WallBlock;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snaypertihcreator.mineralslimes.common.blocks.CorralBlock;
import snaypertihcreator.mineralslimes.common.fance.CorralHelper;

public class InductorItem extends BlockItem {

    public InductorItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean canPlace(@NotNull BlockPlaceContext context, @NotNull BlockState state) {
        if (super.canPlace(context, state)) return true;
        Level level = context.getLevel();
        BlockPos below = context.getClickedPos().below();
        BlockState support = level.getBlockState(below);
        return context.getClickedFace() == Direction.UP && (support.is(BlockTags.FENCES) || support.getBlock() instanceof WallBlock);
    }

    @Override
    protected boolean placeBlock(@NotNull BlockPlaceContext context, @NotNull BlockState state) {
        Level level = context.getLevel();
        BlockPos below = context.getClickedPos().below();
        if (!(level.getBlockState(below).getBlock() instanceof CorralBlock))
            CorralHelper.convertFenceToCorral(level, below, !level.isClientSide);
        boolean placed = super.placeBlock(context, state);
        if (placed) CorralHelper.rescan(level, below);
        return placed;
    }
}