package snaypertihcreator.mineralslimes.common.vakpak.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class VakPakUpgrades {

    public static final VakPakUpgrades EMPTY = new VakPakUpgrades(Map.of());

    public static final Codec<VakPakUpgrades> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.unboundedMap(VakPakUpgradeType.CODEC, VakPakUpgrade.CODEC)
                    .fieldOf("upgrades")
                    .forGetter(VakPakUpgrades::entries)
    ).apply(inst, VakPakUpgrades::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VakPakUpgrades> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull VakPakUpgrades decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();
            Map<VakPakUpgradeType, VakPakUpgrade> map = new HashMap<>();
            for (int i = 0; i < size; i++) {
                VakPakUpgrade upgrade = VakPakUpgrade.STREAM_CODEC.decode(buf);
                map.put(upgrade.getType(), upgrade);
            }
            return new VakPakUpgrades(map);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, VakPakUpgrades upgrades) {
            Map<VakPakUpgradeType, VakPakUpgrade> map = upgrades.entries();
            buf.writeVarInt(map.size());
            for (VakPakUpgrade upgrade : map.values()) {
                VakPakUpgrade.STREAM_CODEC.encode(buf, upgrade);
            }
        }
    };

    private final Map<VakPakUpgradeType, VakPakUpgrade> upgrades;

    public VakPakUpgrades(Map<VakPakUpgradeType, VakPakUpgrade> upgrades) {
        this.upgrades = new HashMap<>(upgrades);
    }

    public Map<VakPakUpgradeType, VakPakUpgrade> entries() {
        return Collections.unmodifiableMap(upgrades);
    }

    public int level(VakPakUpgradeLike typeLike) {
        VakPakUpgrade u = upgrades.get(typeLike.getType());
        return u == null ? 0 : u.getLevel();
    }

    public boolean has(VakPakUpgradeLike type) { return level(type) > 0; }
    public boolean isMaxed(VakPakUpgradeType type) { return level(type) >= type.maxLevel(); }

    public VakPakUpgrades withLevel(VakPakUpgradeLike type, int newLevel) {
        VakPakUpgrade up = upgrades.get(type.getType());
        VakPakUpgrade next = up == null ? new VakPakUpgrade(type.getType(), newLevel) : up.withLevel(newLevel);
        Map<VakPakUpgradeType, VakPakUpgrade> copy = new HashMap<>(upgrades);
        copy.put(type.getType(), next);
        return new VakPakUpgrades(copy);
    }

    public VakPakUpgrades addLevel(VakPakUpgradeLike type) {
        return withLevel(type, level(type) + 1);
    }

    public VakPakUpgrades reset(VakPakUpgradeType type) {
        if (!upgrades.containsKey(type)) return this;
        Map<VakPakUpgradeType, VakPakUpgrade> copy = new HashMap<>(upgrades);
        copy.remove(type);
        return new VakPakUpgrades(copy);
    }

    public VakPakUpgrades resetAll() { return EMPTY; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VakPakUpgrades other)) return false;
        return upgrades.equals(other.upgrades);
    }

    @Override
    public int hashCode() { return upgrades.hashCode(); }

    @Override
    public String toString() {
        return upgrades.values().stream().map(u -> u.getType() + "=" + u.getLevel()).collect(Collectors.joining(", ", "Upgrades[", "]"));
    }
}