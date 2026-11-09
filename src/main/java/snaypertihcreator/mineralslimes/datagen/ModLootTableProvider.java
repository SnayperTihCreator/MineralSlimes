package snaypertihcreator.mineralslimes.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.common.blocks.ModBlocks;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup){
        super(output, Set.of(), List.of(new SubProviderEntry(ModBlockLootTables::new, LootContextParamSets.BLOCK)), lookup);
    }

    public static class ModBlockLootTables extends BlockLootSubProvider {
        protected ModBlockLootTables(HolderLookup.Provider registries){
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(),  registries);
        }

        @Override
        protected void generate() {
            this.add(ModBlocks.CORRAL.get(), LootTable.lootTable());
        }

        @Override
        protected @NotNull Iterable<Block> getKnownBlocks() {
            return List.of(ModBlocks.CORRAL.get());
        }
    }
}
