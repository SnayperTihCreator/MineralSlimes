package snaypertihcreator.mineralslimes.datagen.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.ModTags;
import snaypertihcreator.mineralslimes.common.items.ModItems;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, CompletableFuture<TagLookup<Block>> tagLookup, @Nullable ExistingFileHelper helper){
        super(output, provider, tagLookup, MineralSlimes.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(ModTags.Items.FRUITS).add(
                Items.APPLE, Items.CHORUS_FRUIT,
                Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE,

                Items.SWEET_BERRIES, Items.GLOW_BERRIES,
                Items.MELON_SLICE, Items.GLISTERING_MELON_SLICE
                );

        tag(ModTags.Items.VEGGIES).add(
                Items.CARROT, Items.POTATO, Items.BEETROOT,
                Items.BAKED_POTATO, Items.POISONOUS_POTATO, Items.BEETROOT_SOUP
        );

        tag(ModTags.Items.MEAT).addTag(ItemTags.WOLF_FOOD);

        for(var item: ModItems.ITEMS.getEntries())
            if (item.getId().getPath().endsWith("_plort"))
                tag(ModTags.Items.PLORTS).add(item.get());

        tag(ModTags.Items.WRENCH).add(ModItems.TOOL.get());

    }
}
