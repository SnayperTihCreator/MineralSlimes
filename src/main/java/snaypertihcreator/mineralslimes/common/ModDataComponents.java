package snaypertihcreator.mineralslimes.common;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakInventory;
import snaypertihcreator.mineralslimes.common.items.VakPakItem;
import snaypertihcreator.mineralslimes.common.vakpak.upgrade.VakPakUpgrades;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MineralSlimes.MODID);


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<VakPakInventory>> VAKPAK_INVENTORY =
            COMPONENTS.registerComponentType("vakpak_inventory", builder -> builder
                    .persistent(VakPakInventory.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(VakPakInventory.CODEC))
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<VakPakItem.VakPakState>> VAKPAK_STATE =
            COMPONENTS.registerComponentType("vakpak_state", builder -> builder
                    .persistent(VakPakItem.VakPakState.VAKPAK_STATE_CODEC)
                    .networkSynchronized(VakPakItem.VakPakState.VAKPAK_STATE_STREAM_CODEC)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> VAKPAK_STATE_TICK =
            COMPONENTS.registerComponentType("vakpak_state_tick", builder -> builder
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<VakPakUpgrades>> VAKPAK_UPGRADES =
            COMPONENTS.registerComponentType("vakpak_upgrades", builder -> builder
                    .persistent(VakPakUpgrades.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(VakPakUpgrades.CODEC))
            );
}
