package snaypertihcreator.mineralslimes.common.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.common.ModDataComponents;
import snaypertihcreator.mineralslimes.common.items.ModItems;
import snaypertihcreator.mineralslimes.common.items.VakPakItem;
import snaypertihcreator.mineralslimes.common.vakpak.upgrade.VakPakUpgradeType;
import snaypertihcreator.mineralslimes.common.vakpak.upgrade.VakPakUpgrades;

import java.util.ArrayList;
import java.util.List;

public final class VakPakUpgradeRecipe implements CraftingRecipe {

    public static final MapCodec<VakPakUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients")
                    .forGetter(r -> r.ingredients),
            VakPakUpgradeType.CODEC.fieldOf("upgrade")
                    .forGetter(r -> r.upgrade),
            com.mojang.serialization.Codec.intRange(1, 5).optionalFieldOf("levels", 1)
                    .forGetter(r -> r.levels)
    ).apply(inst, VakPakUpgradeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VakPakUpgradeRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
                    r -> r.ingredients,
                    VakPakUpgradeType.STREAM_CODEC,
                    r -> r.upgrade,
                    ByteBufCodecs.VAR_INT,
                    r -> r.levels,
                    VakPakUpgradeRecipe::new
            );

    private final List<Ingredient> ingredients;
    private final VakPakUpgradeType upgrade;
    private final int levels;

    public VakPakUpgradeRecipe(List<Ingredient> ingredients, VakPakUpgradeType upgrade, int levels) {
        this.ingredients = List.copyOf(ingredients);
        this.upgrade = upgrade;
        this.levels = levels;
    }

    @Override
    public boolean matches(CraftingInput input, @NotNull Level level) {
        ItemStack vakpak = ItemStack.EMPTY;
        List<ItemStack> others = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof VakPakItem) {
                if (!vakpak.isEmpty()) return false;
                vakpak = stack;
            } else {
                others.add(stack);
            }
        }

        if (vakpak.isEmpty() || others.size() != ingredients.size()) return false;

        boolean[] used = new boolean[ingredients.size()];
        for (ItemStack other : others) {
            boolean matched = false;
            for (int j = 0; j < ingredients.size(); j++) {
                if (used[j] || !ingredients.get(j).test(other)) continue;
                used[j] = true;
                matched = true;
                break;
            }
            if (!matched) return false;
        }

        VakPakUpgrades current = vakpak.getOrDefault(ModDataComponents.VAKPAK_UPGRADES, VakPakUpgrades.EMPTY);
        return current.level(upgrade) + levels <= upgrade.getMaxLevel();
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input, HolderLookup.@NotNull Provider registries) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (!(stack.getItem() instanceof VakPakItem)) continue;

            ItemStack result = stack.copy();
            VakPakUpgrades upgrades = result.getOrDefault(
                    ModDataComponents.VAKPAK_UPGRADES,
                    VakPakUpgrades.EMPTY
            );

            for (int j = 0; j < levels; j++) {
                upgrades = upgrades.addLevel(upgrade);
            }

            result.set(ModDataComponents.VAKPAK_UPGRADES, upgrades);
            VakPakItem.refreshInventory(result);
            return result;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingInput input) {
        return CraftingRecipe.super.getRemainingItems(input);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= ingredients.size() + 1;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return ModItems.VAK_PAK.toStack();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.VAKPAK_UPGRADE_SERIALIZER.get();
    }

    @Override
    public @NotNull String getGroup() {
        return "vakpak_upgrade";
    }

    public List<Ingredient> ingredients() {
        return ingredients;
    }

    public VakPakUpgradeType upgrade() {
        return upgrade;
    }

    public int levels() {
        return levels;
    }

    @Override
    public @NotNull CraftingBookCategory category() {
        return CraftingBookCategory.EQUIPMENT;
    }
}