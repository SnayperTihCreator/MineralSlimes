package snaypertihcreator.mineralslimes.datagen.lang;

import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.neoforge.common.data.LanguageProvider;
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

public class ModRuLangProvider extends LanguageProvider {
    public ModRuLangProvider(PackOutput output) {
        super(output, MineralSlimes.MODID, "ru_ru");
    }

    @Override
    protected void addTranslations() {
        add(ModItems.PINK_PLORT.get(), "Розовый плорт");
        add(ModItems.ROCKY_PLORT.get(), "Скалистый плорт");
        add(ModItems.TUBBY_PLORT.get(), "Мяуровый плорт");

        add(ModItems.ODD_ONION.get(), "Чудной лук");
        add(ModItems.CUBERRY.get(), "Кубника");
        add(ModItems.MINT_MANGO.get(), "Мятное манго");
        add(ModItems.PRICKLE_PEAR.get(), "Грушовник");
        add(ModItems.OCA_OCA.get(), "Ока-ока");
        add(ModItems.PHASE_LEMON.get(), "Фазовый лимон");
        add(ModItems.POLARICHERRY.get(), "Полярная вишня");

        add(ModItems.VAK_PAK.get(), "Вак-Пак");

        add(Diet.NONE.getDisplayName(), "Нету");
        add(Diet.MEAT.getDisplayName(), "Мясо");
        add(Diet.VEGGIE.getDisplayName(), "Овощи");
        add(Diet.FRUIT.getDisplayName(), "Фрукты");
        add(Diet.NECTAR.getDisplayName(), "Нектар");
        add(Diet.WATER.getDisplayName(), "Вода");
        add(Diet.ASH.getDisplayName(), "Пепел");

        add(VakPakHud.DISPLAY_KEY, "Захват скролла хотбара");
        add(VakPakHud.DISPLAY_CATEGORY, "Mineral Slimes");
        add(VakPakSlotEmpty.DISPLAY_NAME, "Пусто");
        add(VakPakSlotEntity.UNKNOWN_ENTITY, "Безымяное существо");
        add(VakPakSlotWater.DISPLAY_NAME, "Вода (%s/%s)");

        add(ModSlimes.PINK.slimeType().displayName(), "Розовый слайм");
        add(ModSlimes.ROCKY.slimeType().displayName(), "Скалистый слайм");
        add(ModSlimes.TUBBY.slimeType().displayName(), "Мяуровый слайм");

        add(ModCreativeTabs.PLORT_TAB.get().getDisplayName(), "Плорты");
        add(ModCreativeTabs.SLIMES_TAB.get().getDisplayName(), "Слаймы");
        add(SlimeContextTag.LIKE_FOODS_NONE, "Нет");

    }

    public void add(Component text, String value) {
        if (text.getContents() instanceof TranslatableContents tc)
            add(tc.getKey(), value);
        else add(text.getString(), value);
    }

}
