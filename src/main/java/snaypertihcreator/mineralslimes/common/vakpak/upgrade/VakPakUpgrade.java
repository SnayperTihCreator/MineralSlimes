package snaypertihcreator.mineralslimes.common.vakpak.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class VakPakUpgrade {

    public static final Codec<VakPakUpgrade> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            VakPakUpgradeType.CODEC.fieldOf("type").forGetter(VakPakUpgrade::getType),
            Codec.intRange(0, 255).fieldOf("level").forGetter(VakPakUpgrade::getLevel)
    ).apply(inst, VakPakUpgrade::new));

    public static final StreamCodec<ByteBuf, VakPakUpgrade> STREAM_CODEC = StreamCodec.composite(
            VakPakUpgradeType.STREAM_CODEC, VakPakUpgrade::getType,
            ByteBufCodecs.VAR_INT, VakPakUpgrade::getLevel,
            VakPakUpgrade::new
    );

    private final VakPakUpgradeType type;
    private final int level;

    public VakPakUpgrade(@NotNull VakPakUpgradeType type, int level) {
        this.type = type;
        this.level = Math.max(0, Math.min(level, type.maxLevel()));
    }

    public VakPakUpgrade(VakPakUpgradeType type) { this(type, 0); }

    @Contract("_ -> new")
    public @NotNull VakPakUpgrade withLevel(int newLevel) { return new VakPakUpgrade(type, newLevel); }
    @Contract(" -> new")
    public @NotNull VakPakUpgrade inc() { return new VakPakUpgrade(type, level + 1); }
    public boolean isMaxed() { return level >= type.maxLevel(); }

    public VakPakUpgradeType getType() { return type; }
    public int getLevel() { return level; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VakPakUpgrade other)) return false;
        return type.equals(other.type) && level == other.level;
    }

    @Override
    public int hashCode() { return 31 * type.hashCode() + level; }

    @Override
    public String toString() { return type.id() + "@" + level; }

}