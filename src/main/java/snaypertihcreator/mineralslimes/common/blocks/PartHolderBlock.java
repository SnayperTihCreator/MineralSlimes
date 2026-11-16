package snaypertihcreator.mineralslimes.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.client.parts.WrenchArrowRenderer;
import snaypertihcreator.mineralslimes.common.ModTags;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.blockentity.InductorBlockEntity;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.PartRotator;
import snaypertihcreator.mineralslimes.common.items.CorralPartItem;

import javax.annotation.Nullable;

public abstract class PartHolderBlock extends Block implements EntityBlock {
    protected PartHolderBlock(Properties properties) {
        super(properties);
    }

    protected abstract VoxelShape baseShape(BlockGetter level, BlockPos pos, @Nullable CorralBlockEntity holder);

    protected VoxelShape combineShapes(BlockGetter level, BlockPos pos) {
        CorralBlockEntity holder = level.getBlockEntity(pos) instanceof CorralBlockEntity be ? be : null;
        VoxelShape shape = baseShape(level, pos, holder);
        if (holder != null)
            for (FencePart part : holder.getParts())
                shape = Shapes.or(shape, part.getShape());
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
        if (!(level.getBlockEntity(pos) instanceof CorralBlockEntity holder))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (stack.is(ModTags.Items.WRENCH))
            return wrenchUse(holder, state, level, pos, player, hit);

        if (stack.getItem() instanceof CorralPartItem<?> part && !player.isShiftKeyDown())
            return partItemUse(holder, part, stack, state, level, pos, player, hit);

        if (!player.isShiftKeyDown()) {
            float angle = angleOf(hit);
            FencePart part = holder.getPartNear(angle, FencePart.STEP / 2F);
            if (part != null) {
                ItemInteractionResult result = toItemResult(part.onUse(level, player, hand, hit), level);
                if (result.consumesAction()) return result;
            }
            ItemInteractionResult extra = extraUseItemOn(holder, stack, state, level, pos, player, hand, hit);
            if (extra.consumesAction()) return extra;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    protected ItemInteractionResult wrenchUse(CorralBlockEntity holder, BlockState state, Level level,
                                            BlockPos pos, Player player, BlockHitResult hit) {
        float angle = angleOf(hit);
        if (player.isShiftKeyDown()) {
            if (level.isClientSide) {
                if (holder.getPartNear(angle, PartRotator.GRAB_TOLERANCE) == null)
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                WrenchArrowRenderer.clearFocus();
                return ItemInteractionResult.SUCCESS;
            }
            if (holder.removePartNear(level, angle, PartRotator.GRAB_TOLERANCE) == null)
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            InductorBlockEntity.markColumnFieldDirty(level, pos);
            var sound = state.getSoundType(level, pos, player).getBreakSound();
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            return ItemInteractionResult.CONSUME;
        }
        if (level.isClientSide) {
            if (holder.getPartNear(angle, PartRotator.GRAB_TOLERANCE) == null) {
                WrenchArrowRenderer.clearFocus();
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            WrenchArrowRenderer.onPartClicked(pos, angle);
            return ItemInteractionResult.SUCCESS;
        } else if (!PartRotator.wrenchUse(player, holder, angle))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        InductorBlockEntity.markColumnFieldDirty(level, pos);
        return ItemInteractionResult.CONSUME;
    }

    protected ItemInteractionResult partItemUse(CorralBlockEntity holder, CorralPartItem<?> item, ItemStack stack,
                                              BlockState state, Level level, BlockPos pos,
                                              Player player, BlockHitResult hit) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        float angle = FencePart.snap(angleOf(hit), FencePart.STEP);
        if (!holder.addPart(item.getType(), angle)) return ItemInteractionResult.CONSUME;
        InductorBlockEntity.markColumnFieldDirty(level, pos);
        if (!player.isCreative()) stack.shrink(1);
        var sound = state.getSoundType(level, pos, player).getPlaceSound();
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        return ItemInteractionResult.CONSUME;
    }

    protected static float angleOf(BlockHitResult hit) {
        return FencePart.angleFrom(hit.getBlockPos(), hit.getLocation());
    }

    @SuppressWarnings("unused")
    protected ItemInteractionResult extraUseItemOn(CorralBlockEntity holder, ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public static ItemInteractionResult toItemResult(InteractionResult result, Level level) {
        return switch (result) {
            case SUCCESS, SUCCESS_NO_ITEM_USED -> level.isClientSide ? ItemInteractionResult.SUCCESS : ItemInteractionResult.CONSUME;
            case CONSUME -> ItemInteractionResult.CONSUME;
            default -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> serverType, BlockEntityType<E> clientType, BlockEntityTicker<? super E> ticker) {
        return clientType == serverType ? (BlockEntityTicker<A>)ticker : null;
    }
}
