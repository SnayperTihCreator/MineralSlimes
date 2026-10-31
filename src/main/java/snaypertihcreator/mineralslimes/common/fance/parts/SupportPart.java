package snaypertihcreator.mineralslimes.common.fance.parts;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.FencePartType;
import snaypertihcreator.mineralslimes.common.fance.ModPartTypes;

public class SupportPart extends FencePart {
    public static final VoxelShape[] SHAPES = circleShapes(6/16F, 2/16F, 12/16F);

    public SupportPart(CorralBlockEntity host, float angle){
        super(host, angle);
    }

    @Override
    public @NotNull FencePartType<?> getType() {
        return ModPartTypes.SUPPORT_PART_TYPE.get();
    }

    @Override
    public VoxelShape getShape() {
        return SHAPES[angleIndex(getAngle())];
    }

    @Override
    public @Nullable BlockState getDisplayState() {
        return Blocks.CHAIN.defaultBlockState();
    }

    @Override
    public float getAttachedRadius() {
        return 6 / 16F;
    }
}
