package snaypertihcreator.mineralslimes.datagen.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.ModTags;
import snaypertihcreator.mineralslimes.common.entity.ModSlimes;

import java.util.concurrent.CompletableFuture;

public class ModEntityTagsProvider extends EntityTypeTagsProvider {
    public ModEntityTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper helper){
        super(output, provider, MineralSlimes.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        for (var entry: ModSlimes.all())
            tag(ModTags.Entities.SLIMES).add(entry.entityType());

        tag(ModTags.Entities.CHICKENS).add(EntityType.CHICKEN);
        tag(ModTags.Entities.VAKED).addTag(ModTags.Entities.SLIMES);

    }
}
