package snaypertihcreator.mineralslimes.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.common.recipes.VakPakUpgradeRecipe;
import snaypertihcreator.mineralslimes.common.vakpak.upgrade.VakPakUpgradeType;
import snaypertihcreator.mineralslimes.common.vakpak.upgrade.VakPakUpgrades;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        output.accept(
                modLoc("vakpak_upgrade/damage"),
                new VakPakUpgradeRecipe(List.of(Ingredient.of(Items.WATER_BUCKET)), VakPakUpgradeType.LIQUID_SLOT, 1),
                null // без advancement
        );
    }
}