package snaypertihcreator.mineralslimes.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.fance.CorralHelper;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.items.CorralPartItem;

import java.util.Map;

public class CorralBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 16, 12);

    public static final Map<Direction, BooleanProperty> CONNECTIONS = Map.of(
            Direction.NORTH, PipeBlock.NORTH,
            Direction.EAST, PipeBlock.EAST,
            Direction.SOUTH, PipeBlock.SOUTH,
            Direction.WEST, PipeBlock.WEST
    );

    public CorralBlock(Properties properties) {
        super(properties.strength(1.5F).noOcclusion().sound(SoundType.METAL).dynamicShape());
        registerDefaultState(this.defaultBlockState()
                .setValue(PipeBlock.NORTH, false).setValue(PipeBlock.EAST, false)
                .setValue(PipeBlock.SOUTH, false).setValue(PipeBlock.WEST, false)
                .setValue(BlockStateProperties.WATERLOGGED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PipeBlock.EAST, PipeBlock.NORTH, PipeBlock.SOUTH, PipeBlock.WEST, BlockStateProperties.WATERLOGGED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        BlockState state = this.defaultBlockState()
                .setValue(BlockStateProperties.WATERLOGGED, level.getFluidState(context.getClickedPos()).getType() == Fluids.WATER);

        for (var entry : CONNECTIONS.entrySet()) {
            Direction dir = entry.getKey();
            BlockPos nPos = pos.relative(dir);
            BlockState nState = level.getBlockState(nPos);
            state = state.setValue(entry.getValue(), connectsTo(nState, nState.isFaceSturdy(level, nPos, dir.getOpposite()), dir));
        }
        return state;
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED))
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        BooleanProperty prop = CONNECTIONS.get(direction);
        if (prop == null) return state;

        if (level.getBlockEntity(pos) instanceof CorralBlockEntity corral) {
            BlockState fence = corral.getFenceState();
            if (fence != null && fence.hasProperty(prop)){
                BlockState updated = fence.updateShape(direction, neighborState, level, pos, neighborPos);
                return state.setValue(prop, updated.getValue(prop));
            }
        }

        return state.setValue(prop, connectsTo(neighborState, neighborState.isFaceSturdy(level, neighborPos, direction.getOpposite()), direction));
    }

    private static boolean connectsTo(BlockState state, boolean sideSolid, Direction dir) {
        boolean fence = state.is(BlockTags.FENCES) && !state.is(Blocks.NETHER_BRICK_FENCE);
        boolean gate = state.is(BlockTags.FENCE_GATES) && !state.getValue(FenceGateBlock.FACING).getAxis().equals(dir.getAxis());
        return !isExceptionForConnection(state) && sideSolid || fence || gate;
    }

    @Override
    protected @NotNull FluidState getFluidState(@NotNull BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED)? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new CorralBlockEntity(pos, state);
    }

    @Override
    protected void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        CorralHelper.onScheduledTick(level, pos);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, stt, be) -> {
            if (be instanceof CorralBlockEntity corral) corral.tickParts();
        };
    }

    @Nullable
    public BlockState fenceState(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof  CorralBlockEntity corral? corral.getFenceState(): null;
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        if (level.isClientSide) return;
        if (!(level.getBlockEntity(pos) instanceof CorralBlockEntity corral)) return;
        BlockPos controller = corral.getControllerPos();
        if (controller == null) return;

        if (level.getBlockState(controller).getBlock() instanceof CorralBlock)
            CorralHelper.scheduleRescan(level, controller);
        else corral.setControllerPos(null);

    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level,
                                                       @NotNull BlockPos pos, @NotNull Player player,
                                                       @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (stack.getItem() instanceof CorralPartItem<?> part && !player.isShiftKeyDown()){
            if (level.isClientSide || !(level.getBlockEntity(pos) instanceof CorralBlockEntity corral))
                return ItemInteractionResult.SUCCESS;

            float angle = FencePart.snap(angleFromHit(hit), FencePart.STEP);
            if (!corral.addPart(part.getType(), angle)) return ItemInteractionResult.CONSUME;

            if (!player.isCreative()) stack.shrink(1);
            var sound = state.getSoundType(level, pos, player).getPlaceSound();
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            return ItemInteractionResult.CONSUME;
        }

        if (!player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof CorralBlockEntity corral) {
            FencePart part = corral.getPartNear(angleFromHit(hit), FencePart.STEP/2F);
            if (part != null) {
                ItemInteractionResult result = toItemResult(part.onUse(level, player, hand, hit), level);
                if (result.consumesAction()) return result;
            }

            BlockState fence = corral.getStoredFence() == null? null: corral.getFenceState();
            if (fence != null) {
                ItemInteractionResult result = fence.useItemOn(stack, level, player, hand, hit);
                if (result.consumesAction()) return result;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;

        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CorralBlockEntity corral) {
            FencePart part = corral.getPartNear(angleFromHit(hit), FencePart.STEP/2F);
            if (part != null) {
                Containers.dropItemStack(level, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, part.getType().createStack());
                corral.removePart(part);
                var sound = state.getSoundType(level, pos, player).getBreakSound();
                level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.sidedSuccess(false);
            }

            BlockState fence = corral.getStoredFence() == null? null: corral.getFenceState();
            if (fence != null) {
                InteractionResult result = fence.useWithoutItem(level, player, hit);
                if (result.consumesAction()) return result;
            }

        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private VoxelShape combineShapes(BlockGetter level, BlockPos pos) {
        VoxelShape shape = Shapes.empty();
        if (level.getBlockEntity(pos) instanceof CorralBlockEntity corral) {
            BlockState fence = corral.getFenceState();
            if (fence != null) shape = fence.getShape(level, pos);
            for (var part : corral.getParts()) shape = Shapes.or(shape, part.getShape());
        }
        return shape.isEmpty()? SHAPE: shape;
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
    protected @NotNull VoxelShape getOcclusionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public @NotNull SoundType getSoundType(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos, @Nullable Entity entity) {
        BlockState fence = fenceState(level, pos);
        return fence != null ? fence.getSoundType(level, pos, entity) : super.getSoundType(state, level, pos, entity);
    }

    @Override
    protected float getDestroyProgress(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        BlockState fence = fenceState(level, pos);
        return fence != null ? fence.getDestroyProgress(player, level, pos) : super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public boolean canHarvestBlock(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Player player) {
        BlockState fence = fenceState(level, pos);
        return fence != null ? fence.canHarvestBlock(level, pos, player) : super.canHarvestBlock(state, level, pos, player);
    }

    @Override
    public int getFlammability(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        BlockState fence = fenceState(level, pos);
        return fence != null ? fence.getFlammability(level, pos, direction) : super.getFlammability(state, level, pos, direction);
    }

    @Override
    public int getFireSpreadSpeed(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        BlockState fence = fenceState(level, pos);
        return fence != null ? fence.getFireSpreadSpeed(level, pos, direction) : super.getFireSpreadSpeed(state, level, pos, direction);
    }

    @Override
    public float getExplosionResistance(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Explosion explosion) {
        BlockState fence = fenceState(level, pos);
        return fence != null ? fence.getBlock().getExplosionResistance(fence, level, pos, explosion): super.getExplosionResistance(state, level, pos, explosion);
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CorralBlockEntity corral)
            corral.dropAndClearParts(level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static float angleFromHit(@NotNull BlockHitResult hit){
        double dx = hit.getLocation().x - (hit.getBlockPos().getX() + 0.5);
        double dz = hit.getLocation().z - (hit.getBlockPos().getZ() + 0.5);
        float angle = (float) Math.toDegrees(Math.atan2(dx, -dz));
        return angle < 0? angle + 360F : angle;
    }

    private static ItemInteractionResult toItemResult(InteractionResult result, Level level) {
        return switch (result){
            case SUCCESS, SUCCESS_NO_ITEM_USED -> level.isClientSide ? ItemInteractionResult.SUCCESS : ItemInteractionResult.CONSUME;
            case CONSUME -> ItemInteractionResult.CONSUME;
            default -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }

    public static boolean isConvertibleFence(@Nullable BlockState state) {
        return state != null && state.is(BlockTags.FENCES) && !(state.getBlock() instanceof CorralBlock);
    }

    public static boolean convertFenceToCorral(Level level, BlockPos pos) {
        BlockState target = level.getBlockState(pos);
        if (!isConvertibleFence(target)) return false;

        BlockState corralState = ModBlocks.CORRAL.get().defaultBlockState();
        if (target.hasProperty(BlockStateProperties.WATERLOGGED) && target.getValue(BlockStateProperties.WATERLOGGED))
            corralState = corralState.setValue(BlockStateProperties.WATERLOGGED, true);

        level.setBlock(pos, corralState, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof CorralBlockEntity corral)
            corral.setStoredFence(target.getBlock());
        return true;
    }
}
