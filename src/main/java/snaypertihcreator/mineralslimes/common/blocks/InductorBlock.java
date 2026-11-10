package snaypertihcreator.mineralslimes.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.blockentity.InductorBlockEntity;
import snaypertihcreator.mineralslimes.common.fance.CorralHelper;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.items.CorralPartItem;

public class InductorBlock extends Block implements EntityBlock {

    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 20, 13);

    public InductorBlock(Properties properties) {
        super(properties.strength(1.0F).sound(SoundType.GLASS).lightLevel(state -> 15).noOcclusion());
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
        return (lvl, pos, stt, be) -> {
            if (be instanceof CorralBlockEntity inductor) inductor.tickParts();
        };
    }

    private VoxelShape combineShapes(BlockGetter level, BlockPos pos) {
        VoxelShape shape = SHAPE;
        if (level.getBlockEntity(pos) instanceof CorralBlockEntity inductor)
            for (var part : inductor.getParts()) shape = Shapes.or(shape, part.getShape());
        return shape;
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return combineShapes(level, pos);
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return combineShapes(level, pos);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level,
                                                       @NotNull BlockPos pos, @NotNull Player player,
                                                       @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (stack.getItem() instanceof CorralPartItem<?> part && !player.isShiftKeyDown()) {
            if (level.isClientSide || !(level.getBlockEntity(pos) instanceof CorralBlockEntity inductor))
                return ItemInteractionResult.SUCCESS;

            float angle = FencePart.snap(FencePart.angleFrom(hit.getBlockPos(), hit.getLocation()), FencePart.STEP);
            if (!inductor.addPart(part.getType(), angle)) return ItemInteractionResult.CONSUME;

            if (!player.isCreative()) stack.shrink(1);
            var sound = state.getSoundType(level, pos, player).getPlaceSound();
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            return ItemInteractionResult.CONSUME;
        }

        if (!player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof CorralBlockEntity inductor) {
            FencePart part = inductor.getPartNear(FencePart.angleFrom(hit.getBlockPos(), hit.getLocation()), FencePart.STEP / 2F);
            if (part != null) {
                ItemInteractionResult result = CorralBlock.toItemResult(part.onUse(level, player, hand, hit), level);
                if (result.consumesAction()) return result;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;

        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CorralBlockEntity inductor) {
            FencePart part = inductor.getPartNear(FencePart.angleFrom(hit.getBlockPos(), hit.getLocation()), FencePart.STEP / 2F);
            if (part != null) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, part.getType().createStack());
                inductor.removePart(part);
                var sound = state.getSoundType(level, pos, player).getBreakSound();
                level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.sidedSuccess(false);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}