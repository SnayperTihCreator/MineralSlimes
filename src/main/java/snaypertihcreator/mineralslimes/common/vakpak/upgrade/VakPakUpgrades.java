package snaypertihcreator.mineralslimes.common.vakpak.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class VakPakUpgrades {

    public static final VakPakUpgrades EMPTY = new VakPakUpgrades(Map.of());

    public static final Codec<VakPakUpgrades> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.unboundedMap(VakPakUpgradeType.CODEC, VakPakUpgrade.CODEC)
                    .fieldOf("upgrades")
                    .forGetter(VakPakUpgrades::upgradesMap)
    ).apply(inst, VakPakUpgrades::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VakPakUpgrades> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull VakPakUpgrades decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();
            Map<VakPakUpgradeType, VakPakUpgrade> map = new EnumMap<>(VakPakUpgradeType.class);
            for (int i = 0; i < size; i++) {
                VakPakUpgrade upgrade = VakPakUpgrade.STREAM_CODEC.decode(buf);
                map.put(upgrade.type(), upgrade);
            }
            return new VakPakUpgrades(map);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, VakPakUpgrades upgrades) {
            Map<VakPakUpgradeType, VakPakUpgrade> map = upgrades.upgradesMap();
            buf.writeVarInt(map.size());
            for (VakPakUpgrade upgrade : map.values()) {
                VakPakUpgrade.STREAM_CODEC.encode(buf, upgrade);
            }
        }
    };

    private final Map<VakPakUpgradeType, VakPakUpgrade> upgrades;

    public VakPakUpgrades(Map<VakPakUpgradeType, VakPakUpgrade> upgrades) {
        this.upgrades = Map.copyOf(upgrades);
    }

    public Map<VakPakUpgradeType, VakPakUpgrade> upgradesMap() {
        return upgrades;
    }

    public int level(VakPakUpgradeType type) {
        VakPakUpgrade u = upgrades.get(type);
        return u == null ? 0 : u.level();
    }

    public boolean has(VakPakUpgradeType type) { return level(type) > 0; }

    public boolean isMaxed(VakPakUpgradeType type) { return level(type) >= type.getMaxLevel(); }

    public VakPakUpgrades withLevel(VakPakUpgradeType type, int newLevel) {
        Map<VakPakUpgradeType, VakPakUpgrade> copy = new EnumMap<>(VakPakUpgradeType.class);
        copy.putAll(upgrades);
        copy.put(type, new VakPakUpgrade(type, newLevel));
        return new VakPakUpgrades(copy);
    }

    public VakPakUpgrades addLevel(VakPakUpgradeType type) {
        return withLevel(type, level(type) + 1);
    }

    public VakPakUpgrades reset(VakPakUpgradeType type) {
        Map<VakPakUpgradeType, VakPakUpgrade> copy = new EnumMap<>(VakPakUpgradeType.class);
        copy.putAll(upgrades);
        copy.remove(type);
        return new VakPakUpgrades(copy);
    }

    public VakPakUpgrades resetAll() {
        return EMPTY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VakPakUpgrades other)) return false;
        return upgrades.equals(other.upgrades);
    }

    @Override
    public int hashCode() {
        return upgrades.hashCode();
    }

    @Override
    public String toString() {
        return upgrades.values().stream()
                .map(u -> u.type() + "=" + u.level())
                .collect(Collectors.joining(", ", "Upgrades[", "]"));
    }
}