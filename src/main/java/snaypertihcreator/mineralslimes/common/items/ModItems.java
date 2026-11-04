package snaypertihcreator.mineralslimes.common.items;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.ModPartTypes;
import snaypertihcreator.mineralslimes.common.holders.DeferredPartType;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MineralSlimes.MODID);

    public static final DeferredItem<?> PINK_PLORT = createPlort("pink");
    public static final DeferredItem<?> ROCKY_PLORT = createPlort("rocky");
    public static final DeferredItem<?> TUBBY_PLORT = createPlort("tubby");

    public static final DeferredItem<?> ODD_ONION = createFood("odd_onion", ModFoods.ODD_ONION_FOOD);
    public static final DeferredItem<?> CUBERRY = createFood("cuberry", ModFoods.CUBERRY_FOOD);
    public static final DeferredItem<?> MINT_MANGO = createFood("mint_mango", ModFoods.MINT_MANGO_FOOD);
    public static final DeferredItem<?> PRICKLE_PEAR = createFood("prickle_pear", ModFoods.PRICKLE_PEAR_FOOD);
    public static final DeferredItem<?> OCA_OCA = createFood("oca_oca", ModFoods.OCA_OCA_FOOD);
    public static final DeferredItem<?> PHASE_LEMON = createFood("phase_lemon", ModFoods.PHASE_LEMON_FOOD);
    public static final DeferredItem<?> POLARICHERRY = createFood("polaricherry", ModFoods.POLARICHERRY_FOOD);

    public static final DeferredItem<?> VAK_PAK = ITEMS.register("vak_pak", VakPakItem::new);

    public static final DeferredItem<?> SUPPORT_PART = createPartItem("support", ModPartTypes.SUPPORT_PART_TYPE);
    public static final DeferredItem<?> GUIDE_PART = createPartItem("guide", ModPartTypes.GUIDE_PART);

    private static DeferredItem<Item> createPlort(String name){
        return ITEMS.register(name+"_plort", () -> new Item(new Item.Properties()));
    }

    private static DeferredItem<Item> createFood(String name, FoodProperties p){
        return ITEMS.registerSimpleItem(name, new Item.Properties().food(p));
    }

    private static <T extends FencePart> DeferredItem<Item> createPartItem(String name, DeferredPartType<T> holder) {
        return ITEMS.register(name + "_part", () -> new CorralPartItem<>(holder, new Item.Properties()));
    }


}
