package snaypertihcreator.mineralslimes.common.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.entity.slimes.AbstractSlimeEntity;
import snaypertihcreator.mineralslimes.common.entity.slimes.PinkSlimeEntity;
import snaypertihcreator.mineralslimes.common.entity.slimes.RockySlimeEntity;
import snaypertihcreator.mineralslimes.common.items.ModItems;
import snaypertihcreator.mineralslimes.common.registries.ModRegistries;
import snaypertihcreator.mineralslimes.common.slime.Diet;
import snaypertihcreator.mineralslimes.common.slime.SlimeType;

import java.util.*;
import java.util.function.Consumer;

import static snaypertihcreator.mineralslimes.common.entity.ModEntities.ENTITY_TYPES;

public class ModSlimes {
    public static final DeferredRegister<SlimeType> SLIME_TYPE =
            DeferredRegister.create(ModRegistries.SLIME_TYPE, MineralSlimes.MODID);
    private static final List<Entry<?>> SLIMES = new ArrayList<>();

    public record Entry<T extends AbstractSlimeEntity>(
            DeferredHolder<SlimeType, SlimeType> type,
            DeferredHolder<EntityType<?>, EntityType<T>> entity,
            DeferredItem<Item> icon
    ) {
        public SlimeType slimeType() { return type.value(); }
        public EntityType<T> entityType() { return entity.value(); }
        public Item iconEntity() { return icon.value(); }
    }

    public static <T extends AbstractSlimeEntity> Entry<T> register(String name, int priority,  int tint, EntityType.EntityFactory<T> factory, Consumer<SlimeType.Builder> config) {
        SlimeType type = buildType(name, priority, tint, config);
        DeferredHolder<SlimeType, SlimeType> typeHolder = SLIME_TYPE.register(name, () -> type);
        DeferredItem<Item> icon = ModItems.ITEMS.registerSimpleItem("%s_slime_icon".formatted(name));

        DeferredHolder<EntityType<?>, EntityType<T>> entityHolder =
                ENTITY_TYPES.register(name + "_slime", () -> EntityType.Builder
                        .of(factory, MobCategory.CREATURE)
                        .sized(type.width(), type.height())
                        .clientTrackingRange(10)
                        .build(name + "_slime"));
        var entry = new Entry<>(typeHolder, entityHolder, icon);
        SLIMES.add(entry);
        return entry;
    }

    public static List<Entry<?>> all() { return Collections.unmodifiableList(SLIMES); }
    public static Optional<Entry<?>> find(EntityType<?> entity) {
        return SLIMES.stream().filter(e -> e.entity().get() == entity).findFirst();
    }
    @SuppressWarnings("unused")
    public static SlimeType slimeTypeOf(EntityType<?> entity) {
        return find(entity).map(Entry::slimeType)
                .orElseThrow(() -> new IllegalStateException("No SlimeType for " + entity));
    }
    public static Optional<Item> iconOf(EntityType<?> entity) {
        return find(entity).map(Entry::iconEntity);
    }
    private static SlimeType buildType(String name, int priority, int tint, Consumer<SlimeType.Builder> config) {
        SlimeType.Builder b = SlimeType.builder(name, priority, tint);
        config.accept(b);
        return b.build();
    }

    public static Optional<Entry<?>> find(ResourceLocation id) {
        return SLIMES.stream().filter(entry -> entry.type().getId().equals(id)).findFirst();
    }

    public static Optional<Entry<?>> find(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        return location == null ? Optional.empty() : find(location);
    }

    public static final Entry<PinkSlimeEntity> PINK = register("pink", 1, 0xFFFFCCE7, PinkSlimeEntity::new, b -> b
            .diet(Diet.of(Diet.MEAT, Diet.FRUIT, Diet.VEGGIE))
            .products(new SlimeType.Product(ModItems.PINK_PLORT, 1))
    );

    public static final Entry<RockySlimeEntity> ROCKY = register("rocky", 2, 0xFFCCCEFF, RockySlimeEntity::new, b -> b
            .diet(Diet.of(Diet.VEGGIE))
            .likeFood(() -> Items.BEETROOT)
            .products(new SlimeType.Product(ModItems.ROCKY_PLORT, 1))
            .triggerable("roll")
    );

}
