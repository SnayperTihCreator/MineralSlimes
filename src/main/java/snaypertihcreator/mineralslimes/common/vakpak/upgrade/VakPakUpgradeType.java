package snaypertihcreator.mineralslimes.common.vakpak.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.registries.ModRegistries;

import java.util.Objects;

public record VakPakUpgradeType(ResourceLocation id, int maxLevel) implements VakPakUpgradeLike {

    public static final Codec<VakPakUpgradeType> CODEC = ResourceLocation.CODEC.flatXmap(
            VakPakUpgradeType::byIdChecked,
            type -> DataResult.success(type.id)
    );

    public static final StreamCodec<ByteBuf, VakPakUpgradeType> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull VakPakUpgradeType decode(@NotNull ByteBuf buf) {
            ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buf);
            VakPakUpgradeType type = byId(id);
            if (type == null) throw new IllegalStateException("Unknown vak pak upgrade type: " + id);
            return type;
        }

        @Override
        public void encode(@NotNull ByteBuf buf, @NotNull VakPakUpgradeType type) {
            ResourceLocation.STREAM_CODEC.encode(buf, type.id);
        }
    };

    public static VakPakUpgradeType byId(ResourceLocation id) {
        VakPakUpgradeType type = ModRegistries.VAKPAK_UPGRADE_TYPES_REGISTRY.get(id);
        if (type == null && id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
            type = ModRegistries.VAKPAK_UPGRADE_TYPES_REGISTRY.get(
                    ResourceLocation.fromNamespaceAndPath(MineralSlimes.MODID, id.getPath())
            );
        }
        return type;
    }

    private static DataResult<VakPakUpgradeType> byIdChecked(ResourceLocation id) {
        VakPakUpgradeType type = byId(id);
        return type == null ? DataResult.error(() -> "Unknown vak pak upgrade type: " + id) : DataResult.success(type);
    }

    @Contract(pure = true)
    public @NotNull String name() {
        return id.getPath();
    }

    @Contract(" -> new")
    public @NotNull Component displayName() {
        return Component.translatable("mineralslimes.vakpak.upgrade.%s".formatted(name()));
    }

    @Override
    public VakPakUpgradeType getType() { return this; }

    @Contract("_ -> new")
    public @NotNull Component levelName(int level) {
        return Component.translatable("mineralslimes.vakpak.upgrade.%s.level".formatted(name()), level, maxLevel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VakPakUpgradeType other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public @NotNull String toString() {
        return id.toString();
    }
}
