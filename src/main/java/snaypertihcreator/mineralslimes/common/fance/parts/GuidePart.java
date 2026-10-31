package snaypertihcreator.mineralslimes.common.fance.parts;

import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.FencePartType;
import snaypertihcreator.mineralslimes.common.fance.ModPartTypes;

public class GuidePart extends FencePart {
    private static final VoxelShape[] SHAPES = circleShapes(6/16F, 3/16F, 10/16F);

    public GuidePart(CorralBlockEntity host, float angle) {
        super(host, angle);
    }

    @Override
    public @NotNull FencePartType<?> getType() {
        return ModPartTypes.GUIDE_PART.get();
    }

    @Override
    public VoxelShape getShape() {
        return SHAPES[angleIndex(getAngle())];
    }

    @Override
    public float getAttachedRadius() {
        return 10/16F;
    }
}
