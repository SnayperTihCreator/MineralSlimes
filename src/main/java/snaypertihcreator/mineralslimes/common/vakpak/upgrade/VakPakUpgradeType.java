package snaypertihcreator.mineralslimes.common.vakpak.upgrade;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;

public enum VakPakUpgradeType implements StringRepresentable {
    SLOT_STACK("slot_stack", 5),
    SLOT_COUNT("slot_count", 2),
    LIQUID_SLOT("liquid_slot", 1),
    WORLD_HARVEST("world_harvest", 1),
    PULSE_WAVE("pulse_wave", 1),
    LOOT_MUL("loot_mult", 3);

    private static final IntFunction<VakPakUpgradeType> BY_ID =
            ByIdMap.continuous(VakPakUpgradeType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

    public static VakPakUpgradeType byId(int id) { return BY_ID.apply(id); }

    public static final Codec<VakPakUpgradeType> CODEC =
            StringRepresentable.fromEnum(VakPakUpgradeType::values);
    public static final StreamCodec<ByteBuf, VakPakUpgradeType> STREAM_CODEC =
            ByteBufCodecs.idMapper(VakPakUpgradeType::byId, VakPakUpgradeType::ordinal);

    private final String name;
    private final int maxLevel;

    VakPakUpgradeType(String name, int maxLevel) {
        this.name = name;
        this.maxLevel = maxLevel;
    }

    public int getMaxLevel() { return maxLevel; }

    public Component displayName() {
        return Component.translatable("mineralslimes.vakpak.upgrade.%s".formatted(name));
    }

    public Component levelName(int level) {
        return Component.translatable("mineralslimes.vakpak.upgrade.%s.level".formatted(name), level, maxLevel);
    }

    @Override public @NotNull String getSerializedName() { return name; }
}