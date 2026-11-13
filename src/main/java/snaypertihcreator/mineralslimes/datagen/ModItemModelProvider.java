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
        basicItem(ModItems.PINK_PLORT.get());
        basicItem(ModItems.ROCKY_PLORT.get());
        basicItem(ModItems.TUBBY_PLORT.get());
        basicItem(ModItems.QUANTUM_PLORT.get());

        foodItem(ModItems.ODD_ONION);
        foodItem(ModItems.CUBERRY);
        foodItem(ModItems.MINT_MANGO);
        foodItem(ModItems.PRICKLE_PEAR);
        foodItem(ModItems.OCA_OCA);
        // TODO Возможные изменения текстуры
        foodItem(ModItems.PHASE_LEMON);
        foodItem(ModItems.POLARICHERRY);

        slimeIcon(ModSlimes.PINK);
        stubSlimeIcon(ModSlimes.ROCKY);
        stubSlimeIcon(ModSlimes.TUBBY);

        simpleBlockItem(ModBlocks.CORRAL.get());
        simpleBlockItem(ModBlocks.INDUCTOR.get(), 0.75F);
        simplePartItem(ModPartTypes.GUIDE_PART.get(), ModItems.GUIDE_PART);

        basicItem(ModItems.TOOL.get());
    }
}
