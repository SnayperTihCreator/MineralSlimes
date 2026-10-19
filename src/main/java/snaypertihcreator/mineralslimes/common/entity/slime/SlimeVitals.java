package snaypertihcreator.mineralslimes.common.entity.slime;

import net.minecraft.nbt.CompoundTag;
import snaypertihcreator.mineralslimes.common.entity.slimes.AbstractSlimeEntity;

public class SlimeVitals {
    public enum SlimeState { NORMAL, HUNGRY }

    private static final String NBT_STATE = "STATE";
    private static final String NBT_SATURATION = "Saturation";

    private final AbstractSlimeEntity slime;

    public SlimeVitals(AbstractSlimeEntity slime) {
        this.slime = slime;
    }

    public SlimeState getState() {
        int ordinal = slime.getEntityData().get(AbstractSlimeEntity.DATA_STATE);
        SlimeState[] values = SlimeState.values();
        if (ordinal < 0 || ordinal >= values.length) return SlimeState.NORMAL;
        return values[ordinal];
    }

    public void setState(SlimeState state) {
        slime.getEntityData().set(AbstractSlimeEntity.DATA_STATE, state.ordinal());
    }

    public int getSaturation() { return slime.getEntityData().get(AbstractSlimeEntity.DATA_SATURATION); }
    public int getMaxSaturation() { return slime.slimeType().maxSaturation(); }
    public boolean isFull() {
        return getSaturation() >= getMaxSaturation();
    }

    public void setSaturation(int value) {
        slime.getEntityData().set(AbstractSlimeEntity.DATA_SATURATION,
                Math.max(0, Math.min(slime.slimeType().maxSaturation(), value)));
    }

    public void setSaturationToMax() { setSaturation(getMaxSaturation()); }
    public boolean isHungry() { return getSaturation() < slime.slimeType().hungryThreshold(); }
    public void feed(int amount) { setSaturation(getSaturation() + amount); }

    public void tick() {
        if (slime.tickCount % slime.slimeType().saturationTickRate() == 0 && getSaturation() > 0) {
            setSaturation(getSaturation() - 1);
        }
        SlimeState target = isHungry() ? SlimeState.HUNGRY : SlimeState.NORMAL;
        if (getState() != target) setState(target);
    }

    public void save(CompoundTag tag) {
        tag.putInt(NBT_STATE, getState().ordinal());
        tag.putInt(NBT_SATURATION, getSaturation());
    }

    public void load(CompoundTag tag) {
        int ordinal = tag.getInt(NBT_STATE);
        var values = SlimeState.values();
        if (ordinal >= 0 && ordinal < values.length) setState(values[ordinal]);
        setSaturation(tag.getInt(NBT_SATURATION));
    }
}
