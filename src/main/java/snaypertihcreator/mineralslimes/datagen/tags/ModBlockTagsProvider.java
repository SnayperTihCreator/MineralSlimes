package snaypertihcreator.mineralslimes.datagen.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper helper){
        super(output, provider, MineralSlimes.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {

    }
}
