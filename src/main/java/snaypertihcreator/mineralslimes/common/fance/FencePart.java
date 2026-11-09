package snaypertihcreator.mineralslimes.common.fance;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.registries.ModRegistries;
import snaypertihcreator.mineralslimes.common.vakpak.vacuum.VakPakVacuumContext;


public abstract class FencePart {
    public static final float CENTER = -1.0f;
    public static final int STEPS = 24;
    public static final float STEP = 360.0F/STEPS;

    protected final CorralBlockEntity host;
    protected float angle;

    protected FencePart(CorralBlockEntity host, float angle) {
        this.host = host;
        this.angle = angle < 0 ? CENTER : ((angle % 360F) + 360F) % 360F;
    }

    @NotNull
    public abstract FencePartType<?> getType();
    public abstract VoxelShape getShape();

    @Nullable
    public BlockState getDisplayState() { return null; }
    public float getAttachedRadius() { return 0.0F; }
    @SuppressWarnings("unused")
    public CorralBlockEntity getHost() { return host; }
    public float getAngle() { return angle; }

    public void tick(){}
    public void onAttached(){}
    public void onRemoved(){}
    public void onVakPakVacuum(VakPakVacuumContext ctx) {}
    public InteractionResult onUse(Level level, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.PASS;
    }


    @SuppressWarnings("unused")
    public CompoundTag saveData(HolderLookup.Provider registries, CompoundTag tag) { return tag; }


    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", getType().getId().toString());
        tag.putFloat("angle", angle);
        return saveData(registries, tag);
    }

    @SuppressWarnings("unused")
    public void loadData(HolderLookup.Provider registries, CompoundTag tag){}

    @Nullable
    public static FencePart load(CorralBlockEntity host, HolderLookup.Provider registries, CompoundTag tag){
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("type"));
        FencePartType<?> type = id == null ? null : ModRegistries.FENCE_PART_TYPES_REGISTRY.get(id);
        if (type == null) {
            MineralSlimes.LOGGER.warn("Skipping fence part with unknown type '{}'", tag.getString("type"));
            return null;
        }

        FencePart part = type.create(host, readAngle(tag));
        part.loadData(registries, tag);
        return part;
    }

    private static float readAngle(CompoundTag tag) {
        if (tag.contains("angle")) return tag.getFloat("angle");
        return CENTER;
    }

    public static VoxelShape shapeAt(float angle, float radius, float halfWidth, float height){
        double dx = Math.sin(Math.toRadians(angle)) * radius;
        double dz = -Math.cos(Math.toRadians(angle)) * radius;
        double x = (0.5 + dx) * 16.0;
        double z = (0.5 + dz) * 16.0;
        return Block.box(x - halfWidth * 16, 0, z - halfWidth * 16, x + halfWidth * 16, height * 16, z + halfWidth * 16);
    }

    public static VoxelShape[] circleShapes(float radius, float halfWidth, float height){
        VoxelShape[] shapes = new VoxelShape[STEPS];
        for (int i = 0; i < STEPS; i++)
            shapes[i] = shapeAt(i*STEP, radius, halfWidth, height);
        return shapes;
    }

    public static int angleIndex(float angle){
        return ((Math.round(angle/STEP)%STEPS) + STEPS)%STEPS;
    }

    public static float angleFrom(BlockPos center, Vec3 location){
        double dx = location.x - (center.getX() + 0.5);
        double dz = location.z - (center.getZ() + 0.5);
        float angle = (float) Math.toDegrees(Math.atan2(dx, -dz));
        return angle < 0? angle + 360F : angle;
    }

    public static float snap(float angle, float step) {
        return ((Math.round(angle / step) * step) % 360F + 360F) % 360F;
    }

}
