package snaypertihcreator.mineralslimes.datagen.lang;

import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.neoforge.common.data.LanguageProvider;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.client.hub.VakPakHud;
import snaypertihcreator.mineralslimes.common.entity.ModSlimes;
import snaypertihcreator.mineralslimes.common.guide.SlimeContextTag;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakSlotType;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakSlotWater;
import snaypertihcreator.mineralslimes.common.items.ModCreativeTabs;
import snaypertihcreator.mineralslimes.common.items.ModItems;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakSlotEmpty;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakSlotEntity;
import snaypertihcreator.mineralslimes.common.slime.Diet;
import snaypertihcreator.mineralslimes.common.vakpak.upgrade.VakPakUpgradeType;

public class ModEnLangProvider extends LanguageProvider {
    public ModEnLangProvider(PackOutput output) {
        super(output, MineralSlimes.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ModItems.PINK_PLORT.get(), "Pink Plort");
        add(ModItems.ROCKY_PLORT.get(), "Rocky Plort");

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

        for (var type : VakPakUpgradeType.values()) {
            add(type.displayName(), switch (type) {
                case SLOT_STACK   -> "Slot Stack";
                case SLOT_COUNT   -> "Slot Count";
                case LIQUID_SLOT  -> "Liquid Slot";
                case WORLD_HARVEST -> "World Harvest";
                case PULSE_WAVE   -> "Pulse Wave";
                case LOOT_MUL     -> "Loot Multiplier";
            });
            add(type.levelName(0), switch (type) {
                case SLOT_STACK   -> "Slot Stack %s/%s";
                case SLOT_COUNT   -> "Slot Count %s/%s";
                case LIQUID_SLOT  -> "Liquid Slot %s/%s";
                case WORLD_HARVEST -> "World Harvest %s/%s";
                case PULSE_WAVE   -> "Pulse Wave %s/%s";
                case LOOT_MUL     -> "Loot Multiplier %s/%s";
            });
        }

        add(ModSlimes.PINK.slimeType().displayName(), "Pink Slime");
        add(ModSlimes.ROCKY.slimeType().displayName(), "Rocky Slime");

        add(ModCreativeTabs.PLORT_TAB.get().getDisplayName(), "Plorts");
        add(ModCreativeTabs.SLIMES_TAB.get().getDisplayName(), "Slimes");
        add(SlimeContextTag.LIKE_FOODS_NONE, "None");
    }

    public void add(Component text, String value) {
        if (text.getContents() instanceof TranslatableContents tc)
            add(tc.getKey(), value);
        else add(text.getString(), value);
    }
}
