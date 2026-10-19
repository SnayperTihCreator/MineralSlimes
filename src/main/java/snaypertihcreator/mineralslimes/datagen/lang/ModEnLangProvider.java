package snaypertihcreator.mineralslimes.datagen.lang;

import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.data.LanguageProvider;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.client.hub.VakPakHud;
import snaypertihcreator.mineralslimes.common.entity.ModSlimes;
import snaypertihcreator.mineralslimes.common.guide.SlimeContextTag;
import snaypertihcreator.mineralslimes.common.inventory.VakPakSlotWater;
import snaypertihcreator.mineralslimes.common.items.ModCreativeTabs;
import snaypertihcreator.mineralslimes.common.items.ModItems;
import snaypertihcreator.mineralslimes.common.inventory.VakPakSlotEmpty;
import snaypertihcreator.mineralslimes.common.inventory.VakPakSlotEntity;
import snaypertihcreator.mineralslimes.common.slime.Diet;

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

        add(ModSlimes.PINK.slimeType().displayName(), "Pink Slime");
        add(ModSlimes.ROCKY.slimeType().displayName(), "Rocky Slime");

        add(ModCreativeTabs.PLORT_TAB.get().getDisplayName(), "Plorts");
        add(ModCreativeTabs.SLIMES_TAB.get().getDisplayName(), "Slimes");
        add(SlimeContextTag.LIKE_FOODS_NONE, "None");
    }

    public void add(Component text, String translationKey) {
        add(text.getString(), translationKey);
    }
}
