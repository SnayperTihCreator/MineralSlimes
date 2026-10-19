package snaypertihcreator.mineralslimes.common.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.slime.SlimeType;

public final class ModRegistries {
    private ModRegistries() {}

    public static final ResourceKey<Registry<SlimeType>> SLIME_TYPE =
            ResourceKey.createRegistryKey(MineralSlimes.modLoc("slime_type"));
}
