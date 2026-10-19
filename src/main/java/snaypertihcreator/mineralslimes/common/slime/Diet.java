package snaypertihcreator.mineralslimes.common.slime;

import net.minecraft.network.chat.Component;

import java.util.EnumSet;

@SuppressWarnings("unused")
public enum Diet {
    NONE("none"),
    MEAT("meat"),
    VEGGIE("veggie"),
    FRUIT("fruit"),
    NECTAR("nectar"),
    WATER("water"),
    ASH("ash");

    private final int value;
    private final Component displayName;

    Diet(String key){
        this.value = 1 << ordinal();
        this.displayName = Component.translatable("diet.%s".formatted(key));
    }

    public int getValue(){
        return value;
    }

    public static boolean hasFlag(int flags, Diet diet) {
        return (flags & diet.value) == diet.value;
    }

    public static int addFlag(int flags, Diet diet) {
        return flags | diet.value;
    }

    public static int removeFlag(int flags, Diet diet) {
        return flags & ~diet.value;
    }

    public static int of(Diet... diets) {
        int result = 0;
        for (Diet diet : diets) {
            result |= diet.value;
        }
        return result;
    }

    public static EnumSet<Diet> getDiets(int flags) {
        EnumSet<Diet> result = EnumSet.noneOf(Diet.class);
        for (Diet diet : values()) {
            if (diet != NONE && (flags & diet.value) == diet.value) result.add(diet);
        }
        return result;
    }


    public Component getDisplayName() {
        return displayName.copy();
    }
}
