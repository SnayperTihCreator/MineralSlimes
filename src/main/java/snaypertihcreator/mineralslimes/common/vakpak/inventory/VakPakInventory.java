package snaypertihcreator.mineralslimes.common.vakpak.inventory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import snaypertihcreator.mineralslimes.common.vakpak.upgrade.VakPakUpgradeType;
import snaypertihcreator.mineralslimes.common.vakpak.upgrade.VakPakUpgrades;
import snaypertihcreator.mineralslimes.common.vakpak.vacuum.VakPakVacuumContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class VakPakInventory {
    public static final int MIN_SLOTS = 4;

    public static final Codec<VakPakInventory> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            VakPakSlot.CODEC.listOf().fieldOf("slots").forGetter(i -> i.slots),
            Codec.INT.lenientOptionalFieldOf("selected", 0).forGetter(i -> i.selected)
    ).apply(inst, VakPakInventory::new));

    private final List<VakPakSlot> slots;
    private int selected;

    public VakPakInventory(@Nullable List<VakPakSlot> slots, int selected) {
        this.slots = slots != null ? new ArrayList<>(slots) : new ArrayList<>();
        while (this.slots.size() < MIN_SLOTS) this.slots.add(VakPakSlotEmpty.INSTANCE);
        this.selected = Math.clamp(selected, 0, this.slots.size() - 1);
    }

    public VakPakInventory() {
        this(null, 0);
    }

    @SuppressWarnings("unused")
    public VakPakInventory(@Nullable VakPakInventory other) {
        this(other == null ? null : deepCopySlots(other.slots), other == null ? 0 : other.selected);
    }

    private static @NotNull List<VakPakSlot> deepCopySlots(@NotNull List<VakPakSlot> src) {
        List<VakPakSlot> out = new ArrayList<>(src.size());
        for (VakPakSlot slot : src) out.add(slot.copy());
        return out;
    }

    public static int maxRegularSlots(@Nullable VakPakUpgrades upgrades) {
        int result = MIN_SLOTS;
        if (upgrades != null) result += upgrades.level(VakPakUpgradeType.SLOT_COUNT);
        return result;
    }

    public static int waterSlotCount(@Nullable VakPakUpgrades upgrades) {
        return upgrades == null ? 0 : upgrades.level(VakPakUpgradeType.LIQUID_SLOT);
    }

    @SuppressWarnings("unused")
    public static int maxSlots(@Nullable VakPakUpgrades upgrades) {
        return maxRegularSlots(upgrades) + waterSlotCount(upgrades);
    }

    public boolean updateForUpgrades(@Nullable VakPakUpgrades upgrades) {
        int regularCap = maxRegularSlots(upgrades);
        int waterCap   = waterSlotCount(upgrades);

        List<VakPakSlot> regular = new ArrayList<>();
        List<VakPakSlot> water = new ArrayList<>();
        for (VakPakSlot slot : slots)
            (slot.getType() == VakPakSlotType.WATER ? water : regular).add(slot);

        while (regular.size() > regularCap && regular.getLast().canBeReplaced()) regular.removeLast();
        while (water.size() > waterCap && water.getLast().isEmpty()) water.removeLast();

        while (regular.size() < regularCap) regular.add(VakPakSlotEmpty.INSTANCE);
        while (water.size() < waterCap)     water.add(new VakPakSlotWater(0));

        List<VakPakSlot> result = new ArrayList<>(regular.size() + water.size());
        result.addAll(regular);
        result.addAll(water);

        boolean changed = !result.equals(slots);
        if (changed) { slots.clear(); slots.addAll(result); }
        if (selected >= slots.size()) { selected = slots.size() - 1; changed = true; }
        return changed;
    }

    public boolean canVacuumEntity(Entity entity, @Nullable VakPakUpgrades upgrades) {
        for (VakPakSlot slot : slots)
            if (!slot.isEmpty() && slot.canStackEntity(entity)) return true;
        for (VakPakSlotType type : VakPakSlotType.allTypes()) {
            if (!type.canCreateFromEntity(entity)) continue;
            if (!type.canCreateWith(upgrades)) continue;
            if (hasReceiverFor(type)) return true;
        }
        return false;
    }

    public boolean tryVacuumEntity(Entity entity, @Nullable VakPakUpgrades upgrades) {
        for (VakPakSlot slot : slots) {
            if (slot.isEmpty()) continue;
            if (slot.canStackEntity(entity) && slot.vacuumEntity(entity)) return true;
        }

        VakPakSlot newSlot = createSlotFor(entity, upgrades);
        return newSlot != null && putIntoReplaceable(newSlot);
    }

    @SuppressWarnings("unused")
    public boolean canVacuumBlock(VakPakVacuumContext ctx, @Nullable VakPakUpgrades upgrades) {
        for (VakPakSlot slot : slots)
            if (!slot.isEmpty() && slot.canStackBlock(ctx)) return true;
        for (VakPakSlotType type : VakPakSlotType.allTypes()) {
            if (!type.canCreateFromBlock(ctx.getState())) continue;
            if (!type.canCreateWith(upgrades)) continue;
            if (hasReceiverFor(type)) return true;
        }
        return false;
    }

    public boolean tryVacuumBlock(VakPakVacuumContext ctx, @Nullable VakPakUpgrades upgrades) {
        for (VakPakSlot slot : slots) {
            if (slot.isEmpty()) continue;
            if (slot.canStackBlock(ctx) && slot.vacuumBlock(ctx)) return true;
        }

        VakPakSlot newSlot = createSlotFor(ctx, upgrades);
        return newSlot != null && putIntoReplaceable(newSlot);
    }

    @Nullable
    private VakPakSlot createSlotFor(Entity entity, @Nullable VakPakUpgrades upgrades) {
        for (VakPakSlotType type : VakPakSlotType.allTypes()) {
            if (!type.canCreateFromEntity(entity)) continue;
            if (!type.canCreateWith(upgrades)) continue;
            if (!hasReceiverFor(type)) continue;
            VakPakSlot slot = type.createFromEntity(entity);
            if (slot != null) return slot;
        }
        return null;
    }

    @Nullable
    private VakPakSlot createSlotFor(VakPakVacuumContext ctx, @Nullable VakPakUpgrades upgrades) {
        for (VakPakSlotType type : VakPakSlotType.allTypes()) {
            if (!type.canCreateFromBlock(ctx.getState())) continue;
            if (!type.canCreateWith(upgrades)) continue;
            if (!hasReceiverFor(type)) continue;
            VakPakSlot slot = type.createFromBlock(ctx);
            if (slot != null) return slot;
        }
        return null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasReceiverFor(VakPakSlotType newType) {
        for (VakPakSlot slot : slots)
            if (slot.canAcceptNewSlot(newType)) return true;
        return false;
    }

    private boolean putIntoReplaceable(VakPakSlot newSlot) {
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).canAcceptNewSlot(newSlot.getType())) {
                slots.set(i, newSlot);
                return true;
            }
        }
        return false;
    }

    @Contract(pure = true)
    @SuppressWarnings("unused")
    public @NotNull @Unmodifiable List<VakPakSlot> slots() { return List.copyOf(slots); }
    public int selectedIndex() { return selected; }
    public void clearSelected() {
        if (!slots.get(selected).canBeReplaced()) return;
        slots.set(selected, VakPakSlotEmpty.INSTANCE);
    }

    public void selectNext() { selected = (selected + 1) % slots.size(); }
    public void selectPrev() { selected = Math.floorMod(selected - 1, slots.size()); }
    public VakPakSlot peekSelected() { return slots.get(selected); }

    public VakPakSlot getSlot(int index) { return slots.get(index); }
    public int size() { return slots.size(); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof VakPakInventory other)) return false;
        return this.selected == other.selected && this.slots.equals(other.slots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slots, selected);
    }
}