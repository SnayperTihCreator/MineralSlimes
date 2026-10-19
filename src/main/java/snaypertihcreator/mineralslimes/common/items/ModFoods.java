package snaypertihcreator.mineralslimes.common.items;

import net.minecraft.world.food.FoodProperties;

// TODO Возможно создать эффекты и поменять параметры

public class ModFoods {

    public static final FoodProperties ODD_ONION_FOOD = new FoodProperties.Builder()
            .nutrition(6) // (1 единица = полноги)
            .saturationModifier(0.6f) // модификатор насыщения (насыщения = модификатор*сытость*2)
            .build();
    public static final FoodProperties CUBERRY_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.6f)
            .build();
    public static final FoodProperties MINT_MANGO_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.6f)
            .build();
    public static final FoodProperties PRICKLE_PEAR_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.6f)
            .build();
    public static final FoodProperties OCA_OCA_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.6f)
            .build();
    public static final FoodProperties PHASE_LEMON_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.6f)
            .build();
    public static final FoodProperties POLARICHERRY_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.6f)
            .build();

}
