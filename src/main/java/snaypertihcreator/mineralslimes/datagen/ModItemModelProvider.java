package snaypertihcreator.mineralslimes.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.blocks.ModBlocks;
import snaypertihcreator.mineralslimes.common.entity.ModSlimes;
import snaypertihcreator.mineralslimes.common.fance.ModPartTypes;
import snaypertihcreator.mineralslimes.common.items.ModItems;
import snaypertihcreator.mineralslimes.datagen.extra.ExtraItemModelProvider;

public class ModItemModelProvider extends ExtraItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper helper){
        super(output, MineralSlimes.MODID, helper);
    }

    @Override
    protected void registerModels() {
        basicSubFolderItem(ModItems.PINK_PLORT, "plorts");
        basicSubFolderItem(ModItems.ROCKY_PLORT, "plorts");
        basicSubFolderItem(ModItems.TUBBY_PLORT, "plorts");
        basicSubFolderItem(ModItems.QUANTUM_PLORT, "plorts");
        basicSubFolderItem(ModItems.PUDDLE_PLORT, "plorts");

        basicSubFolderItem(ModItems.ODD_ONION, "food");
        basicSubFolderItem(ModItems.CUBERRY, "food");
        basicSubFolderItem(ModItems.MINT_MANGO, "food");
        basicSubFolderItem(ModItems.PRICKLE_PEAR, "food");
        basicSubFolderItem(ModItems.OCA_OCA, "food");
        basicSubFolderItem(ModItems.PHASE_LEMON, "food"); // TODO Возможные изменения текстуры
        basicSubFolderItem(ModItems.POLARICHERRY, "food");

        slimeIcon(ModSlimes.PINK);
        stubSlimeIcon(ModSlimes.ROCKY);
        stubSlimeIcon(ModSlimes.TUBBY);
        stubSlimeIcon(ModSlimes.QUANTUM);
        stubSlimeIcon(ModSlimes.PUDDLE);

        basicHandheldItem(ModItems.TOOL);

        simpleBlockItem(ModBlocks.CORRAL.get());
        simpleBlockItem(ModBlocks.INDUCTOR.get(), 0.75F);

        simplePartItem(ModPartTypes.GUIDE_PART.get(), ModItems.GUIDE_PART);
        simplePartItem(ModPartTypes.EMITTER_PART.get(), ModItems.EMITTER_PART);


    }
}
