package snaypertihcreator.mineralslimes.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import snaypertihcreator.mineralslimes.MineralSlimes;

public class ModTags {
    public static class Items {
        public static final TagKey<Item> FRUITS = createTagItem("fruits");
        public static final TagKey<Item> VEGGIES = createTagItem("veggies");
        public static final TagKey<Item> MEAT = createTagItem("meat");

        public static final TagKey<Item> WRENCH = TagKey.create(
                Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "tools/wrench"));

        public static final TagKey<Item> PLORTS = createTagItem("plorts");
    }

    public static class Entities {
        public static final TagKey<EntityType<?>> CHICKENS = createTagEntity("chickens");
        public static final TagKey<EntityType<?>> SLIMES = createTagEntity("slimes");

        public static final TagKey<EntityType<?>> VAKED = createTagEntity("vaked");
    }

    private static TagKey<Item> createTagItem(String key){
        return TagKey.create(Registries.ITEM, MineralSlimes.modLoc(key));
    }

    private static TagKey<EntityType<?>> createTagEntity(String key){
        return TagKey.create(Registries.ENTITY_TYPE,  MineralSlimes.modLoc(key));
    }
}
