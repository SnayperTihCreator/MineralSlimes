package snaypertihcreator.mineralslimes.datagen.extra;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.entity.ModSlimes;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.FencePartType;

public abstract class ExtraItemModelProvider extends ItemModelProvider {

    public ExtraItemModelProvider(PackOutput output, String modid, ExistingFileHelper helper){
        super(output, modid, helper);
    }

    protected ItemModelBuilder simpleBlockItem(Block block, float scale) {
        return simpleBlockItem(BuiltInRegistries.BLOCK.getKey(block), scale);
    }

    protected ItemModelBuilder simpleBlockItem(@NotNull ResourceLocation block, float scale) {
        return withBlockDisplay(withExistingParent(block.toString(),
                ResourceLocation.fromNamespaceAndPath(block.getNamespace(), "block/" + block.getPath())), scale);
    }

    protected ItemModelBuilder withBlockDisplay(@NotNull ItemModelBuilder builder, float scale){
        return builder.transforms()
                .transform(ItemDisplayContext.GUI)
                    .rotation(30, 225, 0).scale(0.625F*scale).end()
                .transform(ItemDisplayContext.GROUND)
                    .rotation(0, 3, 0).scale(0.25F*scale).end()
                .transform(ItemDisplayContext.FIXED)
                    .scale(0.5F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                    .rotation(75, 45, 0).translation(0, 2.5F, 0).scale(0.375F*scale).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                    .rotation(0, 45, 0).scale(0.4F*scale).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                    .rotation(0, 255, 0).scale(0.4F*scale).end()
                .end();
    }

    protected void foodItem(@NotNull DeferredItem<?> holder) {
        ResourceLocation id = holder.getId();
        getBuilder(id.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "item/food/" + id.getPath()));
    }

    @SuppressWarnings("SameParameterValue")
    protected <T extends FencePart> void simplePartItem(@NotNull FencePartType<T> type, @NotNull DeferredItem<?> part) {
        getBuilder(part.getId().toString()).parent(new ModelFile.UncheckedModelFile(type.modelId()));
    }

    @SuppressWarnings("SameParameterValue")
    protected void slimeIcon(ModSlimes.@NotNull Entry<?> entry) {
        String itemName = entry.icon().getId().getPath();
        String slimeName = itemName.replace("_slime_icon", "");
        ResourceLocation texture = MineralSlimes.modLoc("item/slime_icons/" + slimeName);

        if (!existingFileHelper.exists(texture, PackType.CLIENT_RESOURCES, ".png", "textures/")) {
            LOGGER.warn("[Datagen] Missing texture {} for {}", texture, itemName);
            existingFileHelper.trackGenerated(texture, PackType.CLIENT_RESOURCES, ".png", "textures/");
        }

        withExistingParent(itemName, "item/generated").texture("layer0", texture);
    }

    protected void stubSlimeIcon(ModSlimes.@NotNull Entry<?> entry) {
        String itemName = entry.icon().getId().getPath();
        ResourceLocation texture = MineralSlimes.modLoc("item/slime_icons/stub");

        withExistingParent(itemName, "item/generated").texture("layer0", texture);
    }


    protected void geckolibItem(@NotNull DeferredItem<?> item){
        getBuilder(item.getId().getPath()).parent(new ModelFile.UncheckedModelFile("builtin/entity"));
    }
}
