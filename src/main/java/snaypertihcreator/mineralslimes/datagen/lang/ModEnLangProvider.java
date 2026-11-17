package snaypertihcreator.mineralslimes.datagen.lang;

import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.neoforge.common.data.LanguageProvider;
import snaypertihcreator.mineralslimes.LangKeys;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.client.hub.VakPakHud;
import snaypertihcreator.mineralslimes.common.entity.ModSlimes;
import snaypertihcreator.mineralslimes.common.guide.SlimeContextTag;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakSlotWater;
import snaypertihcreator.mineralslimes.common.items.ModCreativeTabs;
import snaypertihcreator.mineralslimes.common.items.ModItems;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakSlotEmpty;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakSlotEntity;
import snaypertihcreator.mineralslimes.common.slime.Diet;
import snaypertihcreator.mineralslimes.common.vakpak.upgrade.ModUpgradeTypes;

public class ModEnLangProvider extends LanguageProvider {
    public ModEnLangProvider(PackOutput output) {
        super(output, MineralSlimes.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ModItems.PINK_PLORT.get(), "Pink Plort");
        add(ModItems.ROCKY_PLORT.get(), "Rocky Plort");
        add(ModItems.TUBBY_PLORT.get(), "Tubby Plort");
        add(ModItems.QUANTUM_PLORT.get(), "Quantum Plort");

        add(ModItems.ODD_ONION.get(), "Odd Onion");
        add(ModItems.CUBERRY.get(), "Cuberry");
        add(ModItems.MINT_MANGO.get(), "Mint Mango");
        add(ModItems.PRICKLE_PEAR.get(), "Prickle Pear");
        add(ModItems.OCA_OCA.get(), "Oca Oca");
        add(ModItems.PHASE_LEMON.get(), "Phase Lemon");
        add(ModItems.POLARICHERRY.get(), "Polaricherry");

        add(ModItems.VAK_PAK.get(), "Vak-Pak");

        add(Diet.NONE.getDisplayName(), "None");
        add(Diet.MEAT.getDisplayName(), "Meat");
        add(Diet.VEGGIE.getDisplayName(), "Veggies");
        add(Diet.FRUIT.getDisplayName(), "Fruit");
        add(Diet.NECTAR.getDisplayName(), "Nectar");
        add(Diet.WATER.getDisplayName(), "Water");
        add(Diet.ASH.getDisplayName(), "Ash");

        add(VakPakHud.DISPLAY_KEY, "Hotbar scroll capture");
        add(VakPakHud.DISPLAY_CATEGORY, "Mineral Slimes");
        add(VakPakSlotEmpty.DISPLAY_NAME, "Empty");
        add(VakPakSlotEntity.UNKNOWN_ENTITY, "Unnamed Entity");
        add(VakPakSlotWater.DISPLAY_NAME, "Water (%s/%s)");
        add(LangKeys.ShiftInfo, "Press Shift for detailed info");

        for (var holder: ModUpgradeTypes.all()){
            addUpgrade(holder.displayName(), holder.levelName(0), switch (holder.id().getPath()){
                case "slot_stack"   -> "Slot Stack";
                case "slot_count"   -> "Slot Count";
                case "liquid_slot"  -> "Liquid Slot";
                case "world_harvest" -> "World Harvest";
                case "pulse_wave"   -> "Pulse Wave";
                case "loot_mul"     -> "Loot Multiplier";
                default -> "Unknown";
            });
        }

        add(ModSlimes.PINK.slimeType().displayName(), "Pink Slime");
        add(ModSlimes.ROCKY.slimeType().displayName(), "Rocky Slime");
        add(ModSlimes.TUBBY.slimeType().displayName(), "Tubby Slime");

        add(ModCreativeTabs.PLORT_TAB.get().getDisplayName(), "Plorts");
        add(ModCreativeTabs.SLIMES_TAB.get().getDisplayName(), "Slimes");
        add(SlimeContextTag.LIKE_FOODS_NONE, "None");
    }

    protected void addUpgrade(Component display, Component levelName, String name) {
        add(display, name);
        add(levelName, "%s %%s/%%s".formatted(name));
    }

    public void add(Component text, String value) {
        if (text.getContents() instanceof TranslatableContents tc)
            add(tc.getKey(), value);
        else add(text.getString(), value);
    }
}
