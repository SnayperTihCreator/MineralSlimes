package snaypertihcreator.mineralslimes.common.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import snaypertihcreator.mineralslimes.MineralSlimes;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MineralSlimes.MODID);
}
