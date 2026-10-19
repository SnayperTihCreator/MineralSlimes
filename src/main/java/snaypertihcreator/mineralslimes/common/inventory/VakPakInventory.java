package snaypertihcreator.mineralslimes.common.inventory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import snaypertihcreator.mineralslimes.common.items.vakpak.VakPakVacuumContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class VakPakInventory {
    public static final int MAX_SLOTS = 4;

    public static final Codec<VakPakInventory> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            VakPakSlot.CODEC.listOf().fieldOf("slots").forGetter(i -> i.slots),
            Codec.INT.lenientOptionalFieldOf("selected", 0).forGetter(i -> i.selected)
    ).apply(inst, VakPakInventory::new));

    private final List<VakPakSlot> slots;
    private int selected;

    public VakPakInventory(@Nullable List<VakPakSlot> slots, int selected) {
        this.slots = slots != null ? new ArrayList<>(slots) : new ArrayList<>();
        while (this.slots.size() < MAX_SLOTS) this.slots.add(VakPakSlotEmpty.INSTANCE);
        while (this.slots.size() > MAX_SLOTS) this.slots.removeLast();
        this.selected = Math.clamp(selected, 0, this.slots.size() - 1);
    }

    public VakPakInventory() { this(null, 0); }

    public VakPakInventory(@Nullable VakPakInventory other) {
        this(other == null ? null : deepCopySlots(other.slots),
                other == null ? 0 : other.selected);
    }

    private static List<VakPakSlot> deepCopySlots(List<VakPakSlot> src) {
        List<VakPakSlot> out = new ArrayList<>(src.size());
        for (VakPakSlot slot : src) out.add(slot.copy());
        return out;
    }

    public int selectedIndex() { return selected; }
    public VakPakSlot peekSelected() { return slots.get(selected); }
    public void clearSelected() { slots.set(selected, VakPakSlotEmpty.INSTANCE); }

    public void selectNext() { selected = (selected + 1) % slots.size(); }
    public void selectPrev() { selected = Math.floorMod(selected - 1, slots.size()); }
    public void select(int index) { if (index >= 0 && index < slots.size()) selected = index; }

    public VakPakSlot getSlot(int index) { return slots.get(index); }
    public int size() { return slots.size(); }

    public boolean canVacuumEntity(Entity entity) {
        for (VakPakSlot slot : slots) {
            if (slot.isEmpty()) continue;
            if (slot.canStackEntity(entity)) return true;
        }
        boolean hasEmpty = slots.stream().anyMatch(VakPakSlot::isEmpty);
        if (hasEmpty) for (VakPakSlotType type : VakPakSlotType.allTypes())
                if (type.canCreateFromEntity(entity)) return true;
        return false;
    }

    public boolean tryVacuumEntity(Entity entity) {
        for (VakPakSlot slot : slots) {
            if (slot.isEmpty()) continue;
            if (slot.canStackEntity(entity) && slot.vacuumEntity(entity)) return true;
        }
        for (int i = 0; i < slots.size(); i++) {
            if (!slots.get(i).isEmpty()) continue;
            for (VakPakSlotType type : VakPakSlotType.allTypes()) {
                if (!type.canCreateFromEntity(entity)) continue;
                VakPakSlot newSlot = type.createFromEntity(entity);
                if (newSlot != null) {
                    slots.set(i, newSlot);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean tryVacuumBlock(VakPakVacuumContext ctx) {
        for (VakPakSlot slot : slots) {
            if (slot.isEmpty()) continue;
            if (slot.canStackBlock(ctx) && slot.vacuumBlock(ctx)) return true;
        }

        for (int i = 0; i < slots.size(); i++) {
            if (!slots.get(i).isEmpty()) continue;
            for (VakPakSlotType type : VakPakSlotType.allTypes()) {
                if (!type.canCreateFromBlock(ctx.getState())) continue;
                VakPakSlot newSlot = type.createFromBlock(ctx);
                if (newSlot != null) {
                    slots.set(i, newSlot);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof VakPakInventory other)) return false;
        return this.selected == other.selected && this.slots.equals(other.slots);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(slots, selected);
    }
}
