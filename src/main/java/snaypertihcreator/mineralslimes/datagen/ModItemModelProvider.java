package snaypertihcreator.mineralslimes.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.blocks.ModBlocks;
import snaypertihcreator.mineralslimes.common.entity.ModSlimes;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.FencePartType;
import snaypertihcreator.mineralslimes.common.fance.ModPartTypes;
import snaypertihcreator.mineralslimes.common.items.CorralPartItem;
import snaypertihcreator.mineralslimes.common.items.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper helper){
        super(output, MineralSlimes.MODID, helper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.PINK_PLORT.get());
        basicItem(ModItems.ROCKY_PLORT.get());
        basicItem(ModItems.TOBBY_PLORT.get());

        foodItem(ModItems.ODD_ONION);
        foodItem(ModItems.CUBERRY);
        foodItem(ModItems.MINT_MANGO);
        foodItem(ModItems.PRICKLE_PEAR);
        foodItem(ModItems.OCA_OCA);
        // TODO Возможные изменения текстуры
        foodItem(ModItems.PHASE_LEMON);
        foodItem(ModItems.POLARICHERRY);

        slimeIcon(ModSlimes.PINK);

        simpleBlockItem(ModBlocks.CORRAL.get());
        simplePartItem(ModPartTypes.GUIDE_PART.get(), ModItems.GUIDE_PART);
    }

    protected void foodItem(DeferredItem<?> holder) {
        ResourceLocation id = holder.getId();
        getBuilder(id.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "item/food/" + id.getPath()));
    }

    @SuppressWarnings("SameParameterValue")
    protected <T extends FencePart> void simplePartItem(@NotNull FencePartType<T> type, DeferredItem<?> part) {
        getBuilder(part.getId().toString()).parent(new ModelFile.UncheckedModelFile(type.getModelId()));
    }


    @SuppressWarnings("SameParameterValue")
    protected void slimeIcon(ModSlimes.Entry<?> entry) {
        String itemName = entry.icon().getId().getPath();
        String slimeName = itemName.replace("_slime_icon", "");
        ResourceLocation texture = MineralSlimes.modLoc("item/slime_icons/" + slimeName);

        if (!existingFileHelper.exists(texture, PackType.CLIENT_RESOURCES, ".png", "textures/")) {
            LOGGER.warn("[Datagen] Missing texture {} for {}", texture, itemName);
            existingFileHelper.trackGenerated(texture, PackType.CLIENT_RESOURCES, ".png", "textures/");
        }

        withExistingParent(itemName, "item/generated")
                .texture("layer0", texture);
    }

    @SuppressWarnings({"SameParameterValue", "unused"})
    protected void geckolibItem(DeferredItem<?> item){
        getBuilder(item.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile("builtin/entity"));
    }
}
