package snaypertihcreator.mineralslimes.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.common.blocks.CorralBlock;
import snaypertihcreator.mineralslimes.common.blocks.InductorBlock;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.parts.EmitterPart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InductorBlockEntity extends CorralBlockEntity {
    public static final int MAX_FIELD_RANGE = 8;
    public static final int MAX_COLUMN_SCAN = 16;
    public static final float FACE_TOLERANCE = FencePart.STEP*1.5F;
    public static final double WALL_SURFACE = 0.06F;
    public static final double WALL_DETECT = 0.5F;

    public record Link(BlockPos emitter1, BlockPos emitter2, BlockPos inductor2, float angle1, float angle2) {}
    private record EmitterRef(BlockPos pos, float angle) {}

    private final List<Link> links = new ArrayList<>();

    public InductorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.INDUCTOR_BE.get(), pos, state);
    }

    public List<Link> getLinks() {
        return Collections.unmodifiableList(links);
    }

    public void serverTick(ServerLevel level) {
        long t = level.getGameTime() + Math.floorMod(getBlockPos().hashCode(), 20L);
        if (t%5L == 0L)
            if (links.removeIf(l -> !linkValid(level, l))) syncToClient();
        if (t%100 == 0L) updateLinks(level);
        applyWallCollision(level);
    }

    private void updateLinks(ServerLevel level) {
        boolean changed = links.removeIf(l -> !linkValid(level, l));

        for (EmitterRef e : collectEmitters(level, getBlockPos())) {
            if (hasLinkFrom(e.pos())) continue;
            BlockPos best = null, bestInd = null;
            float bestFAngle = 0f;
            double bestD = Double.MAX_VALUE;
            for (BlockPos ind : inductorsAround(level, e.pos())) {
                if (ind.equals(getBlockPos())) continue;
                if (!(level.getBlockEntity(ind) instanceof InductorBlockEntity other)) continue;
                if (other.columnHeight(level) != columnHeight(level)) continue;   // правило 2
                for (EmitterRef f : other.collectEmitters(level, ind)) {
                    if (f.pos().getY() != e.pos().getY()) continue;               // правило 1
                    if (other.hasLinkFrom(f.pos())) continue;
                    if (!faceEachOther(e, f)) continue;                           // правило 3
                    double d = distSqr(e.pos(), f.pos());
                    if (d > (double) MAX_FIELD_RANGE * MAX_FIELD_RANGE) continue;
                    if (!pathClear(level, e.pos(), f.pos())) continue;
                    if (d < bestD) { bestD = d; best = f.pos(); bestFAngle = f.angle(); bestInd = ind; }
                }
            }
            if (best != null) {
                links.add(new Link(e.pos().immutable(), best.immutable(), bestInd.immutable(), e.angle(), bestFAngle));
                if (level.getBlockEntity(bestInd) instanceof InductorBlockEntity other) {
                    other.links.add(new Link(best.immutable(), e.pos().immutable(),
                            getBlockPos().immutable(), bestFAngle, e.angle()));
                    other.syncToClient();
                }
                changed = true;
            }
        }
        if (changed) syncToClient();
    }

    private boolean hasLinkFrom(BlockPos emitter) {
        for (Link l : links) if (l.emitter1.equals(emitter)) return true;
        return false;
    }

    private boolean isFieldOwner(Link l) {
        return getBlockPos().compareTo(l.inductor2) < 0;
    }

    private static Vec3 facing(float angleDeg) {
        double rad = Math.toRadians(angleDeg);
        return new Vec3(Math.sin(rad), 0, -Math.cos(rad));
    }

    private static Vec3 anchor(BlockPos pos, float angleDeg) {
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

    private int columnHeight(ServerLevel level) {
        int h = 0;
        BlockPos cursor = getBlockPos().below();
        for (int i = 0; i < MAX_COLUMN_SCAN && cursor.getY() >= level.getMinBuildHeight(); i++) {
            if (!(level.getBlockState(cursor).getBlock() instanceof CorralBlock)) break;
            h++;
            cursor = cursor.below();
        }
        return h;
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

    private static EmitterRef emitterRefAt(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockState(pos).getBlock() instanceof CorralBlock)) return null;
        if (!(level.getBlockEntity(pos) instanceof CorralBlockEntity corral)) return null;
        for (FencePart part : corral.getParts())
            if (part instanceof EmitterPart ep) return new EmitterRef(pos, ep.getAngle());
        return null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean linkValid(ServerLevel level, Link l) {
        if (!(level.getBlockEntity(l.inductor2) instanceof InductorBlockEntity other)) return false;
        if (other.columnHeight(level) != columnHeight(level)) return false;
        EmitterRef ea = emitterRefAt(level, l.emitter1);
        EmitterRef eb = emitterRefAt(level, l.emitter2);
        if (ea == null || eb == null) return false;
        if (!faceEachOther(ea, eb)) return false;
        return l.emitter1.getY() == l.emitter2.getY()
                && distSqr(l.emitter1, l.emitter2) <= (double) MAX_FIELD_RANGE * MAX_FIELD_RANGE
                && pathClear(level, l.emitter1, l.emitter2);
    }

    private List<BlockPos> inductorsAround(ServerLevel level, BlockPos e) {
        List<BlockPos> out = new ArrayList<>();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dy = 1; dy <= MAX_COLUMN_SCAN; dy++) {
            int y = e.getY() + dy;
            if (y > level.getMaxBuildHeight()) break;
            for (int dx = -MAX_FIELD_RANGE; dx <= MAX_FIELD_RANGE; dx++) {
                for (int dz = -MAX_FIELD_RANGE; dz <= MAX_FIELD_RANGE; dz++) {
                    if (dx * dx + dz * dz > MAX_FIELD_RANGE * MAX_FIELD_RANGE) continue;
                    m.set(e.getX() + dx, y, e.getZ() + dz);
                    if (!level.hasChunkAt(m)) continue;
                    if (level.getBlockState(m).getBlock() instanceof InductorBlock) out.add(m.immutable());
                }
            }
        }
        return out;
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
            if (block instanceof CorralBlock || block instanceof InductorBlock) continue; // столбы пропускаем
            if (!state.isAir() && !state.getCollisionShape(level, pos).isEmpty()) return false;
        }
        return true;
    }

    private static double distSqr(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX(), dz = a.getZ() - b.getZ();
        return dx * dx + dz * dz;
    }

    private void applyWallCollision(ServerLevel level) {
        if (links.isEmpty()) return;
        for (Link l : links) {
            if (!isFieldOwner(l)) continue;
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, detectBox(l))) {
                if (e instanceof Animal) collideWithPlane(e, l);
            }
        }
    }

    private AABB detectBox(Link l) {
        Vec3 a = anchor(l.emitter1, l.angle1);
        Vec3 b = anchor(l.emitter2, l.angle2);
        double minX = Math.min(a.x, b.x) - 0.6, maxX = Math.max(a.x, b.x) + 0.6;
        double minZ = Math.min(a.z, b.z) - 0.6, maxZ = Math.max(a.z, b.z) + 0.6;
        return new AABB(minX, l.emitter1.getY() - 0.5, minZ, maxX, getBlockPos().getY() + 0.5, maxZ);
    }

    private void collideWithPlane(LivingEntity e, Link l) {
        Vec3 a = anchor(l.emitter1, l.angle1);
        Vec3 b = anchor(l.emitter2, l.angle2);
        Vec3 seg = new Vec3(b.x - a.x, 0, b.z - a.z);
        double len = seg.length();
        if (len < 0.5) return;
        Vec3 dir = seg.scale(1.0 / len);
        Vec3 nrm = new Vec3(-dir.z, 0, dir.x);

        AABB bb = e.getBoundingBox();
        double y0 = l.emitter1.getY();
        double y1 = getBlockPos().getY();
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
        for (Link l : links) {
            CompoundTag c = new CompoundTag();
            c.putLong("E", l.emitter1.asLong());
            c.putLong("O", l.emitter2.asLong());
            c.putLong("I", l.inductor2.asLong());
            c.putFloat("EA", l.angle1);
            c.putFloat("EB", l.angle2);
            list.add(c);
        }
        tag.put("FieldLinks", list);
    }

    public void loadFieldData(CompoundTag tag) {
        links.clear();
        ListTag list = tag.getList("FieldLinks", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            links.add(new Link(BlockPos.of(c.getLong("E")), BlockPos.of(c.getLong("O")),
                    BlockPos.of(c.getLong("I")), c.getFloat("EA"), c.getFloat("EB")));
        }
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
