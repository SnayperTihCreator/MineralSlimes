package snaypertihcreator.mineralslimes.common.vakpak.upgrade;

import net.minecraft.network.chat.Component;

public interface VakPakUpgradeLike {
    VakPakUpgradeType getType();
    Component displayName();
    Component levelName(int level);
}
