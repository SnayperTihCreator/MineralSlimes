package snaypertihcreator.mineralslimes.common.entity.slimes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class QuantumSlimeEntity extends AbstractSlimeEntity {
    public static final int PHASE_TICKS = 200;

    private static final EntityDataAccessor<Boolean> DATA_QUANTUM_PHASE =
            SynchedEntityData.defineId(QuantumSlimeEntity.class, EntityDataSerializers.BOOLEAN);

    private int phaseTimer;


    public QuantumSlimeEntity (EntityType<? extends QuantumSlimeEntity> entityType, Level level) {
        super(entityType, level);
        this.phaseTimer = PHASE_TICKS + this.random.nextInt(PHASE_TICKS);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_QUANTUM_PHASE, false);
    }

    public boolean isQuantumPhase() {
        return this.entityData.get(DATA_QUANTUM_PHASE);
    }

    public void setQuantumPhase(boolean phase) {
        this.entityData.set(DATA_QUANTUM_PHASE, phase);
    }

    public int phaseTicksLeft() {
        return this.phaseTimer;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        if (--this.phaseTimer <= 0) {
            this.phaseTimer = PHASE_TICKS;
            setQuantumPhase(!isQuantumPhase());
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("QuantumPhaseTimer", this.phaseTimer);
        tag.putBoolean("QuantumPhase", isQuantumPhase());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.phaseTimer = Math.max(1, tag.getInt("QuantumPhaseTimer"));
        setQuantumPhase(tag.getBoolean("QuantumPhase"));
    }
}
