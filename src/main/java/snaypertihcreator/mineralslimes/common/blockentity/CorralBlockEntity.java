package snaypertihcreator.mineralslimes.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.common.blocks.CorralBlock;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.FencePartType;
import snaypertihcreator.mineralslimes.common.vakpak.vacuum.IVakPakVacuumReactableBE;
import snaypertihcreator.mineralslimes.common.vakpak.vacuum.VakPakVacuumContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CorralBlockEntity extends BlockEntity implements IVakPakVacuumReactableBE {

    private final List<FencePart> parts =  new ArrayList<>();

    public CorralBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntity.CORRAL_BE.get(), pos, blockState);
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
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        parts.clear();
        ListTag list = tag.getList("parts", ListTag.TAG_COMPOUND);
        for (var i = 0; i < list.size(); i++) {
            try {
                FencePart part = FencePart.load(this, registries, list.getCompound(i));
                if (part != null) this.parts.add(part);
            } catch (IllegalArgumentException ignored) {} // ИГНОРИМ
        }
    }

    public void dropAndClearParts(Level level, BlockPos pos){
        for (FencePart part : parts){
            part.onRemoved();
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), part.getType().createStack());
        }
        parts.clear();
    }
}
