package snaypertihcreator.mineralslimes.common.fance.parts;

import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.FencePartType;
import snaypertihcreator.mineralslimes.common.fance.ModPartTypes;

public class EmitterPart extends FencePart {
    public static final float ATTACHED_RADIUS = 0.45F;
    public static final float HALF_WIDTH = 0.12F;
    public static final float HEIGHT = 0.55F;

    private static final VoxelShape[] SHAPES = circleShapes(ATTACHED_RADIUS, HALF_WIDTH, HEIGHT);

    public EmitterPart(CorralBlockEntity host, float angle){
        super(host, angle);
    }

    @Override
    public @NotNull FencePartType<?> getType() {
        return ModPartTypes.EMITTER_PART.get();
    }

    @Override
    public VoxelShape getShape() {
        return SHAPES[angleIndex(getAngle())];
    }

    @Override
    public float getAttachedRadius() {
        return ATTACHED_RADIUS;
    }
}
