package snaypertihcreator.mineralslimes.common.vakpak.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record VakPakUpgrade(VakPakUpgradeType type, int level) {

    public static final Codec<VakPakUpgrade> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            VakPakUpgradeType.CODEC.fieldOf("type").forGetter(VakPakUpgrade::type),
            Codec.intRange(0, 255).fieldOf("level").forGetter(VakPakUpgrade::level)
    ).apply(inst, VakPakUpgrade::new));

    public static final StreamCodec<ByteBuf, VakPakUpgrade> STREAM_CODEC = StreamCodec.composite(
            VakPakUpgradeType.STREAM_CODEC, VakPakUpgrade::type,
            ByteBufCodecs.VAR_INT,           VakPakUpgrade::level,
            VakPakUpgrade::new
    );

    public VakPakUpgrade {
        if (level < 0) level = 0;
        if (level > type.getMaxLevel()) level = type.getMaxLevel();
    }

    public VakPakUpgrade(VakPakUpgradeType type) { this(type, 0); }

    public boolean isMaxed() { return level >= type.getMaxLevel(); }

    public VakPakUpgrade inc() {
        return new VakPakUpgrade(type, Math.min(level + 1, type.getMaxLevel()));
    }

    public VakPakUpgrade withLevel(int newLevel) { return new VakPakUpgrade(type, newLevel); }
}