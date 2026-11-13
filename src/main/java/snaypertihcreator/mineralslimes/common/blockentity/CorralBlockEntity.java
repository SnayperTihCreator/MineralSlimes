package snaypertihcreator.mineralslimes.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.common.blocks.CorralBlock;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.FencePartType;
import snaypertihcreator.mineralslimes.common.vakpak.vacuum.IVakPakVacuumReactableBE;
import snaypertihcreator.mineralslimes.common.vakpak.vacuum.VakPakVacuumContext;

import javax.annotation.Nullable;
import java.util.*;

public class CorralBlockEntity extends BlockEntity implements IVakPakVacuumReactableBE {

    @Nullable private Block storedFence;
    private final List<FencePart> parts =  new ArrayList<>();
    @Nullable private BlockPos controllerPos;
    @Nullable private Set<BlockPos> structure;

    public CorralBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntity.CORRAL_BE.get(), pos, blockState);
    }

    public CorralBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Nullable
    public Block getStoredFence() {
        return storedFence;
    }

    public void setStoredFence(@Nullable Block storedFence) {
        this.storedFence = storedFence;
        syncToClient();
    }

    @Nullable public BlockState getFenceState() {
        if (storedFence == null) return null;

        BlockState state = storedFence.defaultBlockState();
        BlockState self = getBlockState();
        for (var entry: CorralBlock.CONNECTIONS.entrySet())
            if (state.hasProperty(entry.getValue()))
                state = state.setValue(entry.getValue(), self.getValue(entry.getValue()));

        if (state.hasProperty(BlockStateProperties.WATERLOGGED))
            state = state.setValue(BlockStateProperties.WATERLOGGED, self.getValue(BlockStateProperties.WATERLOGGED));
        return state;
    }

    public List<FencePart> getParts() { return Collections.unmodifiableList(parts); }

    public boolean addPart(FencePartType<?> type, float angle){
        if (getPart(type, angle) != null) return false;
        FencePart part = type.create(this, angle);
        parts.add(part);
        part.onAttached();
        syncToClient();
        return true;
    }

    public void removePart(FencePart part){
        if (!parts.remove(part)) return;
        part.onRemoved();
        syncToClient();
    }

    @Nullable
    public FencePart getPart(FencePartType<?> type, float angle){
        for(FencePart part: parts){
            if (part.getType().equals(type) && Math.abs(angleDiff(part.getAngle(), angle)) < 0.01F)
                return part;
        }
        return null;
    }

    @Nullable
    public FencePart getPartNear(float angle, float tolerance){
        FencePart best = null;
        float bestDiff = Float.MAX_VALUE;

        for (FencePart part : parts){
            float diff = Math.abs(angleDiff(part.getAngle(), angle));

            if (diff <= tolerance && diff < bestDiff){
                best = part;
                bestDiff = diff;
            }
        }
        return best;
    }

    public boolean rotatePart(FencePart part, float newAngle) {
        if (!parts.contains(part)) return false;
        FencePart occupant = getPart(part.getType(), newAngle);
        if (occupant != null && occupant != part) return false;
        part.setAngle(FencePart.snap(newAngle, FencePart.STEP));
        return true;
    }

    @Nullable
    public FencePart removePartNear(Level level, float angle, float tolerance) {
        FencePart part = getPartNear(angle, tolerance);
        if (part == null) return null;
        Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5, part.getType().createStack());
        removePart(part);
        return part;
    }

    public static float angleDiff(float a, float b){
        float d = (a-b + 180F) % 360F;
        if (d < 0) d += 360F;
        return d - 180F;
    }

    public void tickParts() {
        if (level == null || level.isClientSide) return;
        for (FencePart part : parts) part.tick();
    }

    public void syncToClient(){
        setChanged();
        if(level==null) return;
        if(level.isClientSide) return;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public void onVakPakVacuum(VakPakVacuumContext ctx) {
        Vec3 hit = ctx.getHitLocation() != null ? ctx.getHitLocation(): ctx.getPos().getCenter();
        FencePart part = getPartNear(FencePart.angleFrom(ctx.getPos(), hit), FencePart.STEP/2F);
        if (part != null) part.onVakPakVacuum(ctx);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (FencePart part : parts) list.add(part.save(registries));
        tag.put("parts", list);
        if (storedFence != null)
            tag.putString("fence", BuiltInRegistries.BLOCK.getKey(storedFence).toString());
        if (this.controllerPos != null) tag.putLong("controller", this.controllerPos.asLong());
        if (this.structure != null) tag.putLongArray("structure", structure.stream().mapToLong(BlockPos::asLong).toArray());
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("fence", Tag.TAG_STRING)){
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("fence"));
            Block block = id == null ? null : BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
            if (block != null && block != Blocks.AIR) storedFence = block;
        }

        parts.clear();
        ListTag list = tag.getList("parts", ListTag.TAG_COMPOUND);
        for (var i = 0; i < list.size(); i++) {
            try {
                FencePart part = FencePart.load(this, registries, list.getCompound(i));
                if (part != null) this.parts.add(part);
            } catch (IllegalArgumentException ignored) {} // ИГНОРИМ
        }

        if (tag.contains("controller")) controllerPos = BlockPos.of(tag.getLong("controller"));
        if (tag.contains("structure")){
            long[] packed = tag.getLongArray("structure");
            Set<BlockPos> structure = new HashSet<>(packed.length);
            for (var packedPos: packed) structure.add(BlockPos.of(packedPos));
            this.structure = structure;
        }
    }

    public void dropAndClearParts(Level level, BlockPos pos){
        for (FencePart part : parts){
            part.onRemoved();
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), part.getType().createStack());
        }
        if (storedFence != null)
            Containers.dropItemStack(level, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, new ItemStack(storedFence));
        parts.clear();
    }

    public boolean isStructureRoot() {
        return controllerPos != null && controllerPos.equals(getBlockPos());
    }

    @Nullable
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public void setControllerPos(@Nullable BlockPos rootPos) {
        this.controllerPos = rootPos;
        setChanged();
    }

    public @Nullable Set<BlockPos> getStructure() {
        return structure;
    }

    public void setStructure(@Nullable Set<BlockPos> structure) {
        this.structure = structure;
        setChanged();
    }
}
