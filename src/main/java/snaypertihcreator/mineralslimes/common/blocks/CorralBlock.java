package snaypertihcreator.mineralslimes.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.items.CorralPartItem;

public class CorralBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(5, 0, 5, 11, 16, 11);

    public CorralBlock(Properties properties) {
        super(properties.strength(1.5F).noOcclusion().sound(SoundType.METAL));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new CorralBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, stt, be) -> {
            if (be instanceof CorralBlockEntity corral) corral.tickParts();
        };
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level,
                                                       @NotNull BlockPos pos, @NotNull Player player,
                                                       @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!(stack.getItem() instanceof CorralPartItem<?> part) || player.isShiftKeyDown())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof CorralBlockEntity corral))
            return ItemInteractionResult.SUCCESS;

        float angle = snap(angleFromHit(hit), FencePart.STEP);
        if (!corral.addPart(part.getType(), angle))
            return ItemInteractionResult.CONSUME;
        if (!player.isCreative()) stack.shrink(1);
        var sound = state.getSoundType(level, pos, null).getPlaceSound();
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        return ItemInteractionResult.CONSUME;
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
            }
        }
        return level.isClientSide?  InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        var shape = SHAPE;
        if (level.getBlockEntity(pos) instanceof CorralBlockEntity corral)
            for (FencePart part : corral.getParts())
                shape = Shapes.or(shape, part.getShape());
        return shape;
    }

    @Override
    protected @NotNull VoxelShape getOcclusionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CorralBlockEntity corral)
            corral.dropAndClearParts(level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @SuppressWarnings("SameParameterValue")
    private static float snap(float angle, float step) {
        return ((Math.round(angle / step) *step)%360F + 360F)%360F;
    }

    private static float angleFromHit(BlockHitResult hit){
        double dx = hit.getLocation().x - (hit.getBlockPos().getX() + 0.5);
        double dz = hit.getLocation().z - (hit.getBlockPos().getZ() + 0.5);
        float angle = (float) Math.toDegrees(Math.atan2(dx, -dz));
        if (angle < 0) angle += 360F;
        return angle;
    }
}
