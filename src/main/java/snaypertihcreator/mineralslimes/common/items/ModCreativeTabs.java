package snaypertihcreator.mineralslimes.common.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import snaypertihcreator.mineralslimes.MineralSlimes;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MineralSlimes.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PLORT_TAB =
            CREATIVE_MODE_TABS.register("plort_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("ctabs.plort_tab"))
            .icon(() -> ModItems.PINK_PLORT.get().getDefaultInstance())
            .displayItems((param, output) -> {
                for (var item : ModItems.ITEMS.getEntries())
                    if (item.getId().getPath().endsWith("_plort"))
                        output.accept(item.get());
            }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SLIMES_TAB =
            CREATIVE_MODE_TABS.register("slimes_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("ctabs.slimes_tab"))
            .displayItems((param, output) -> {
                output.accept(ModItems.VAK_PAK);

                for (var item : ModItems.ITEMS.getEntries())
                    if (item.get().components().has(DataComponents.FOOD)) output.accept(item.get());
            }).build());
}
