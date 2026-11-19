package snaypertihcreator.mineralslimes.common.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import snaypertihcreator.mineralslimes.MineralSlimes;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MineralSlimes.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<WaterBullet>> WATER_BULLET =
            ENTITY_TYPES.register("vakpak_water_shot", () -> EntityType.Builder
                    .of(WaterBullet::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("vakpak_water_shot"));

}
