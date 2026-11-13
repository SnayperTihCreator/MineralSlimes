package snaypertihcreator.mineralslimes.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
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
import snaypertihcreator.mineralslimes.common.ModTags;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.blockentity.InductorBlockEntity;
import snaypertihcreator.mineralslimes.common.blockentity.ModBlockEntity;
import snaypertihcreator.mineralslimes.common.fance.CorralHelper;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.PartRotator;
import snaypertihcreator.mineralslimes.common.items.CorralPartItem;

public class InductorBlock extends Block implements EntityBlock {

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
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CorralBlockEntity inductor))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (stack.is(ModTags.Items.WRENCH)) {
            float angle = FencePart.angleFrom(hit.getBlockPos(), hit.getLocation());
            if (player.isShiftKeyDown()) {
                if (level.isClientSide) return ItemInteractionResult.SUCCESS;
                if (inductor.removePartNear(level, angle, FencePart.STEP / 2F) == null)
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                var sound = state.getSoundType(level, pos, player).getBreakSound();
                level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                return ItemInteractionResult.CONSUME;
            }
            boolean hovered = inductor.getPartNear(angle, FencePart.STEP / 2F) != null;
            if (!level.isClientSide) if (!PartRotator.wrenchUse(player, inductor, angle))
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            else if (!hovered)
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

            return ItemInteractionResult.CONSUME;
        }

        if (stack.getItem() instanceof CorralPartItem<?> part && !player.isShiftKeyDown()) {
            if (level.isClientSide) return ItemInteractionResult.SUCCESS;
            float angle = FencePart.snap(FencePart.angleFrom(hit.getBlockPos(), hit.getLocation()), FencePart.STEP);
            if (!inductor.addPart(part.getType(), angle)) return ItemInteractionResult.CONSUME;
            if (!player.isCreative()) stack.shrink(1);
            var sound = state.getSoundType(level, pos, player).getPlaceSound();
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            return ItemInteractionResult.CONSUME;
        }

        if (!player.isShiftKeyDown()) {
            FencePart part = inductor.getPartNear(FencePart.angleFrom(hit.getBlockPos(), hit.getLocation()), FencePart.STEP / 2F);
            if (part != null) {
                ItemInteractionResult result = CorralBlock.toItemResult(part.onUse(level, player, hand, hit), level);
                if (result.consumesAction()) return result;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        return InteractionResult.PASS;
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

    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> serverType, BlockEntityType<E> clientType, BlockEntityTicker<? super E> ticker) {
        return clientType == serverType ? (BlockEntityTicker<A>)ticker : null;
    }
}