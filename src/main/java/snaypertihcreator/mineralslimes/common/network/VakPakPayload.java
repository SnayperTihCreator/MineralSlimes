package snaypertihcreator.mineralslimes.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

public final class VakPakPayload {
    private VakPakPayload() {}

    public record Select(int delta) implements CustomPacketPayload{
        public static final Type<Select> TYPE = new Type<>(modLoc("vakpak_select"));
        public static final StreamCodec<ByteBuf, Select> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, Select::delta, Select::new);
        @Override public @NotNull Type<Select> type() { return TYPE;}
    }

    public record Shoot() implements CustomPacketPayload{
        public static final Shoot INSTANCE = new Shoot();
        public static final Type<Shoot> TYPE = new Type<>(modLoc("vakpak_shoot"));

        public static final StreamCodec<ByteBuf, Shoot> STREAM_CODEC = StreamCodec.unit(INSTANCE);
        @Override public @NotNull Type<Shoot> type() { return TYPE; }
    }

    public record Vacuum() implements CustomPacketPayload{
        public static final Vacuum INSTANCE = new Vacuum();
        public static final Type<Vacuum> TYPE = new Type<>(modLoc("vakpak_vacuum"));

        public static final StreamCodec<ByteBuf, Vacuum> STREAM_CODEC = StreamCodec.unit(INSTANCE);
        @Override public @NotNull Type<Vacuum> type() { return TYPE; }
    }
}
