package snaypertihcreator.mineralslimes.common.recipes;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;

public final class ModRecipes {
    private ModRecipes() {}

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MineralSlimes.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<VakPakUpgradeRecipe>>
            VAKPAK_UPGRADE_SERIALIZER = SERIALIZERS.register(
            "vakpak_upgrade",
            () -> new RecipeSerializer<>() {
                @Override
                public @NotNull MapCodec<VakPakUpgradeRecipe> codec() {
                    return VakPakUpgradeRecipe.CODEC;
                }

                @Override
                public @NotNull StreamCodec<RegistryFriendlyByteBuf, VakPakUpgradeRecipe> streamCodec() {
                    return VakPakUpgradeRecipe.STREAM_CODEC;
                }
            }
    );
}