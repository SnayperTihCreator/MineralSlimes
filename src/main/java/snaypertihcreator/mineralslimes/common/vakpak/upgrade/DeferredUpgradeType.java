package snaypertihcreator.mineralslimes.common.vakpak.upgrade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import snaypertihcreator.mineralslimes.common.registries.ModRegistries;

public class DeferredUpgradeType extends DeferredHolder<VakPakUpgradeType, VakPakUpgradeType> implements VakPakUpgradeLike {

    protected DeferredUpgradeType(ResourceKey<VakPakUpgradeType> key) {
        super(key);
    }

    public static DeferredUpgradeType create(ResourceLocation key) {
        return new DeferredUpgradeType(ResourceKey.create(ModRegistries.VAKPAK_UPGRADE_TYPE, key));
    }

    public ResourceLocation id() { return getId(); }
    public VakPakUpgradeType type() { return get(); }

    @Override public VakPakUpgradeType getType() { return type(); }
    @Override public Component displayName() { return get().displayName(); }
    @Override public Component levelName(int level) { return get().levelName(level); }
}
