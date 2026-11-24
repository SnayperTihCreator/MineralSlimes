package snaypertihcreator.mineralslimes.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snaypertihcreator.mineralslimes.common.blocks.CorralBlock;
import snaypertihcreator.mineralslimes.common.blocks.InductorBlock;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.parts.EmitterPart;

import java.util.*;

public class InductorBlockEntity extends CorralBlockEntity {
    public static final int MAX_FIELD_RANGE = 8;
    public static final int MAX_COLUMN_SCAN = 16;
    public static final float FACE_TOLERANCE = FencePart.STEP * 1.5F;
    public static final double WALL_SURFACE = 0.06F;
    public static final double WALL_DETECT = 0.5F;
    public static final double WALL_HEIGHT = 0.95;
    public static final double WALL_END = EmitterPart.ATTACHED_RADIUS;
    private static final double WALL_DEPTH = 0.05;

    private record EmitterRef(BlockPos pos, float angle) {}

    private final Set<BlockPos> neighbors = new HashSet<>();
    private boolean fieldDirty = false;
    private boolean scannedOnce = false;

    public Set<BlockPos> getNeighbors() {
        return Collections.unmodifiableSet(neighbors);
    }

    public InductorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.INDUCTOR_BE.get(), pos, state);
    }

    public void serverTick(ServerLevel level) {
        long t = level.getGameTime() + Math.floorMod(getBlockPos().hashCode(), 20L);
        if (!scannedOnce) { scannedOnce = true; fieldDirty = true; }
        if (fieldDirty) { fieldDirty = false; rebuildLinks(level); }
        else if (t % 100L == 0L) validateLinks(level);
        applyWallCollision(level);
    }

    public void markFieldDirty() {
        fieldDirty = true;
        setChanged();
    }

    public static void markColumnFieldDirty(Level level, BlockPos anyPosOfColumn) {
        if (level.isClientSide) return;
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos(anyPosOfColumn.getX(), anyPosOfColumn.getY(), anyPosOfColumn.getZ());
        for (int i = 0; i <= MAX_COLUMN_SCAN; i++) {
            if (level.getBlockEntity(m) instanceof InductorBlockEntity ind) { ind.markFieldDirty(); return; }
            if (!(level.getBlockState(m).getBlock() instanceof CorralBlock)) return;
            m.move(Direction.UP);
        }
    }

    private void rebuildLinks(ServerLevel level) {
        boolean changed = false;
        List<EmitterRef> mine = collectEmitters(level, getBlockPos());

        for (BlockPos other : List.copyOf(neighbors))
            if (!canLink(level, other, mine)) { removeEdge(level, other); changed = true; }

        for (BlockPos ind : inductorsAround(level)) {
            if (ind.equals(getBlockPos()) || neighbors.contains(ind)) continue;
            if (canLink(level, ind, mine)) { addEdge(level, ind); changed = true; }
        }
        if (changed) syncToClient();
    }

    private void validateLinks(ServerLevel level) {
        if (neighbors.isEmpty()) return;
        List<EmitterRef> mine = collectEmitters(level, getBlockPos());
        boolean changed = false;
        for (BlockPos other : List.copyOf(neighbors))
            if (!canLink(level, other, mine)) { removeEdge(level, other); changed = true; }
        if (changed) syncToClient();
    }

    private boolean canLink(ServerLevel level, BlockPos otherInd, List<EmitterRef> mine) {
        if (!(level.getBlockEntity(otherInd) instanceof InductorBlockEntity other)) return false;
        if (other.columnHeight(level) != columnHeight(level)) return false;

        List<EmitterRef> theirs = other.collectEmitters(level, otherInd);
        if (mine.isEmpty() || mine.size() != theirs.size()) return false;

        for (EmitterRef e : mine) {
            boolean ok = false;
            for (EmitterRef f : theirs) {
                if (f.pos().getY() == e.pos().getY()
                        && faceEachOther(e, f)
                        && distSqr(e.pos(), f.pos()) <= (double) MAX_FIELD_RANGE * MAX_FIELD_RANGE
                        && pathClear(level, e.pos(), f.pos())) { ok = true; break; }
            }
            if (!ok) return false;
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    private List<BlockPos> inductorsAround(ServerLevel level) {
        List<BlockPos> out = new ArrayList<>();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dy = -MAX_COLUMN_SCAN; dy <= MAX_COLUMN_SCAN; dy++) {
            int y = getBlockPos().getY() + dy;
            if (y < level.getMinBuildHeight() || y > level.getMaxBuildHeight()) continue;
            for (int dx = -MAX_FIELD_RANGE; dx <= MAX_FIELD_RANGE; dx++) {
                for (int dz = -MAX_FIELD_RANGE; dz <= MAX_FIELD_RANGE; dz++) {
                    if (dx * dx + dz * dz > (double) MAX_FIELD_RANGE * MAX_FIELD_RANGE) continue;
                    m.set(getBlockPos().getX() + dx, y, getBlockPos().getZ() + dz);
                    if (!level.hasChunkAt(m)) continue;
                    if (level.getBlockState(m).getBlock() instanceof InductorBlock) out.add(m.immutable());
                }
            }
        }
        return out;
    }

    private void addEdge(ServerLevel level, BlockPos other) {
        neighbors.add(other.immutable());
        if (level.getBlockEntity(other) instanceof InductorBlockEntity o) {
            o.neighbors.add(getBlockPos().immutable());
            o.syncToClient();
        }
        setChanged();
    }

    private void removeEdge(ServerLevel level, BlockPos other) {
        neighbors.remove(other);
        if (level.getBlockEntity(other) instanceof InductorBlockEntity o) {
            o.neighbors.remove(getBlockPos());
            o.syncToClient();
        }
        setChanged();
    }

    private static Vec3 facing(float angleDeg) {
        double rad = Math.toRadians(angleDeg);
        return new Vec3(Math.sin(rad), 0, -Math.cos(rad));
    }

    public static Vec3 anchor(BlockPos pos, float angleDeg) {
        Vec3 dir = facing(angleDeg);
        return new Vec3(
                pos.getX() + 0.5 + dir.x * EmitterPart.ATTACHED_RADIUS,
                pos.getY() + 0.5,
                pos.getZ() + 0.5 + dir.z * EmitterPart.ATTACHED_RADIUS
        );
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean faceEachOther(EmitterRef a, EmitterRef b) {
        Vec3 pa = anchor(a.pos(), a.angle());
        Vec3 pb = anchor(b.pos(), b.angle());
        Vec3 toB = new Vec3(pb.x - pa.x, 0, pb.z - pa.z);
        if (toB.lengthSqr() < 1.0) return false;
        toB = toB.normalize();
        double cosTol = Math.cos(Math.toRadians(FACE_TOLERANCE));
        Vec3 dirA = facing(a.angle());
        Vec3 dirB = facing(b.angle());
        if (dirA.x * toB.x + dirA.z * toB.z < cosTol) return false;
        return dirB.x * -toB.x + dirB.z * -toB.z >= cosTol;
    }

    private List<EmitterRef> collectEmitters(ServerLevel level, BlockPos indPos) {
        List<EmitterRef> out = new ArrayList<>();
        if (level.getBlockEntity(indPos) instanceof CorralBlockEntity self)
            for (FencePart part : self.getParts())
                if (part instanceof EmitterPart ep) { out.add(new EmitterRef(indPos.immutable(), ep.getAngle())); break; }
        BlockPos cursor = indPos.below();
        for (int i = 0; i < MAX_COLUMN_SCAN && cursor.getY() >= level.getMinBuildHeight(); i++) {
            if (!(level.getBlockState(cursor).getBlock() instanceof CorralBlock)) break;
            if (level.getBlockEntity(cursor) instanceof CorralBlockEntity corral)
                for (FencePart part : corral.getParts())
                    if (part instanceof EmitterPart ep) {
                        out.add(new EmitterRef(cursor.immutable(), ep.getAngle()));
                        break;
                    }
            cursor = cursor.below();
        }
        return out;
    }

    public static int columnHeight(Level level, BlockPos top) {
        int h = 0;
        BlockPos cursor = top.below();
        for (int i = 0; i < MAX_COLUMN_SCAN && cursor.getY() >= level.getMinBuildHeight(); i++) {
            if (!(level.getBlockState(cursor).getBlock() instanceof CorralBlock)) break;
            h++;
            cursor = cursor.below();
        }
        return h;
    }

    private int columnHeight(ServerLevel level) {
        return columnHeight(level, getBlockPos());
    }

    private static boolean pathClear(ServerLevel level, BlockPos from, BlockPos to) {
        Vec3 a = Vec3.atCenterOf(from);
        Vec3 b = Vec3.atCenterOf(to);
        Vec3 dir = b.subtract(a);
        double dist = dir.length();
        if (dist < 0.01) return false;
        dir = dir.scale(1.0 / dist);
        for (double d = 0.6; d < dist - 0.3; d += 0.5) {
            Vec3 p = a.add(dir.scale(d));
            BlockPos pos = BlockPos.containing(p.x, p.y, p.z);
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof CorralBlock || block instanceof InductorBlock) continue;
            if (!state.isAir() && !state.getCollisionShape(level, pos).isEmpty()) return false;
        }
        return true;
    }

    private static double distSqr(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX(), dz = a.getZ() - b.getZ();
        return dx * dx + dz * dz;
//        a.distSqr(b);
    }

    private void applyWallCollision(ServerLevel level) {
        if (neighbors.isEmpty()) return;
        for (BlockPos other : neighbors) {
            if (getBlockPos().compareTo(other) >= 0) continue;
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, detectBox(other, level)))
                if (e instanceof PathfinderMob) collideWithPlane(e, other, level);
        }
    }

    private AABB detectBox(BlockPos otherInd, ServerLevel level) {
        double h = columnHeight(level, getBlockPos());
        double y0 = getBlockPos().getY() - h;
        double y1 = y0 + (h + 1) * WALL_HEIGHT;
        Vec3 a = Vec3.atCenterOf(getBlockPos());
        Vec3 b = Vec3.atCenterOf(otherInd);
        return new AABB(Math.min(a.x, b.x) - 0.6, y0 - 0.5, Math.min(a.z, b.z) - 0.6, Math.max(a.x, b.x) + 0.6, y1, Math.max(a.z, b.z) + 0.6);
    }

    @Nullable
    public AABB fieldVolume(Level level, BlockPos other) {
        Vec3 a = Vec3.atCenterOf(getBlockPos());
        Vec3 b = Vec3.atCenterOf(other);
        double dx = b.x - a.x, dz = b.z - a.z;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 1.0) return null;

        double drawn = len - WALL_END * 2;
        if (drawn < 0.25) return null;

        double h = columnHeight(level, getBlockPos());
        double y0 = getBlockPos().getY() - h;
        double y1 = y0 + (h + 1) * WALL_HEIGHT;

        double nx = dx / len, nz = dz / len;
        double midX = (a.x + b.x) * 0.5, midZ = (a.z + b.z) * 0.5;
        double halfLen = drawn * 0.5;

        double ex = Math.abs(nx) * halfLen + Math.abs(nz) * WALL_DEPTH;
        double ez = Math.abs(nz) * halfLen + Math.abs(nx) * WALL_DEPTH;

        return new AABB(midX - ex, y0, midZ - ez, midX + ex, y1, midZ + ez);
    }

    private void collideWithPlane(LivingEntity e, BlockPos otherInd, ServerLevel level) {
        Vec3 a = Vec3.atCenterOf(getBlockPos());
        Vec3 b = Vec3.atCenterOf(otherInd);
        Vec3 seg = new Vec3(b.x - a.x, 0, b.z - a.z);
        double len = seg.length();
        if (len < 1.0) return;
        Vec3 dir = seg.scale(1.0 / len);
        Vec3 nrm = new Vec3(-dir.z, 0, dir.x);

        double h = columnHeight(level, getBlockPos());
        double y0 = getBlockPos().getY() - h;
        double y1 = y0 + (h + 1) * WALL_HEIGHT;

        AABB bb = e.getBoundingBox();
        if (bb.maxY <= y0 || bb.minY >= y1) return;

        double halfW = e.getBbWidth() * 0.5;
        Vec3 rel = e.position().subtract(a);
        double t = rel.x * dir.x + rel.z * dir.z;
        if (t < -halfW || t > len + halfW) return;

        double d = rel.x * nrm.x + rel.z * nrm.z;
        if (Math.abs(d) >= WALL_DETECT + halfW) return;
        double s = d >= 0 ? 1.0 : -1.0;

        double pd = (e.xo - a.x) * nrm.x + (e.zo - a.z) * nrm.z;
        if (Math.abs(pd) <= 1.5 && Math.signum(pd) != 0 && Math.signum(pd) != Math.signum(d)) {
            Vec3 prevRel = new Vec3(e.xo - a.x, 0, e.zo - a.z);
            double pt = prevRel.x * dir.x + prevRel.z * dir.z;
            if (pt >= 0 && pt <= len && e.yo >= y0 && e.yo <= y1) {
                e.setPos(e.xo, e.yo, e.zo);
                return;
            }
        }

        Vec3 dm = e.getDeltaMovement();
        double vInto = (dm.x * nrm.x + dm.z * nrm.z) * s;
        if (vInto < 0) e.setDeltaMovement(dm.subtract(nrm.scale(s * vInto)));

        double surface = WALL_SURFACE + halfW;
        if (Math.abs(d) < surface) {
            double shift = surface * s - d;
            e.setPos(e.getX() + nrm.x * shift, e.getY(), e.getZ() + nrm.z * shift);
        }
        e.hurtMarked = true;
    }

    public void saveFieldData(CompoundTag tag) {
        ListTag list = new ListTag();
        for (BlockPos p : neighbors) {
            CompoundTag c = new CompoundTag();
            c.putLong("I", p.asLong());
            list.add(c);
        }
        tag.put("FieldNeighbors", list);
    }

    public void loadFieldData(CompoundTag tag) {
        neighbors.clear();
        ListTag list = tag.getList("FieldNeighbors", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
            neighbors.add(BlockPos.of(list.getCompound(i).getLong("I")));
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveFieldData(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        saveFieldData(tag);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        loadFieldData(tag);
    }
}
