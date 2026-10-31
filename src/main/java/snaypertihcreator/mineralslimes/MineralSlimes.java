package snaypertihcreator.mineralslimes;

import guideme.Guide;
import guideme.compiler.TagCompiler;
import guideme.scene.element.SceneElementTagCompiler;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import snaypertihcreator.mineralslimes.common.ModDataComponents;
import snaypertihcreator.mineralslimes.common.blockentity.ModBlockEntity;
import snaypertihcreator.mineralslimes.common.blocks.ModBlocks;
import snaypertihcreator.mineralslimes.common.entity.ModSlimes;
import snaypertihcreator.mineralslimes.common.fance.ModPartTypes;
import snaypertihcreator.mineralslimes.common.guide.SlimeContextTag;
import snaypertihcreator.mineralslimes.common.guide.SlimeSceneElement;
import snaypertihcreator.mineralslimes.common.items.ModCreativeTabs;
import snaypertihcreator.mineralslimes.common.config.Config;
import snaypertihcreator.mineralslimes.common.entity.ModEntities;
import snaypertihcreator.mineralslimes.common.items.ModItems;
import snaypertihcreator.mineralslimes.common.recipes.ModRecipes;
import snaypertihcreator.mineralslimes.datagen.ModRecipeProvider;
import snaypertihcreator.mineralslimes.datagen.ModSlimeAnimationProvider;
import snaypertihcreator.mineralslimes.datagen.lang.ModEnLangProvider;
import snaypertihcreator.mineralslimes.datagen.tags.ModBlockTagsProvider;
import snaypertihcreator.mineralslimes.datagen.ModItemModelProvider;
import snaypertihcreator.mineralslimes.datagen.tags.ModEntityTagsProvider;
import snaypertihcreator.mineralslimes.datagen.tags.ModItemTagsProvider;
import snaypertihcreator.mineralslimes.datagen.lang.ModRuLangProvider;

import java.util.concurrent.CompletableFuture;

@Mod(MineralSlimes.MODID)
public class MineralSlimes {
    public static final String MODID = "mineralslimes";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation GUIDE_ID = modLoc("wiki");

    public MineralSlimes(IEventBus modEventBus, @NotNull ModContainer modContainer) {
        ModDataComponents.COMPONENTS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModPartTypes.FENCE_PART_TYPE.register(modEventBus);
        ModBlockEntity.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModSlimes.SLIME_TYPE.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModRecipes.SERIALIZERS.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::gatherData);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        Guide.builder(GUIDE_ID).folder("slimewiki")
                .extension(TagCompiler.EXTENSION_POINT, new SlimeContextTag())
                .extension(SceneElementTagCompiler.EXTENSION_POINT, new SlimeSceneElement())
                .build();
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void gatherData(@NotNull GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();
        boolean includeClient = event.includeClient();

        generator.addProvider(includeClient, new ModItemModelProvider(output, helper));
        generator.addProvider(includeClient, new ModEnLangProvider(output));
        generator.addProvider(includeClient, new ModRuLangProvider(output));
        generator.addProvider(includeClient, new ModSlimeAnimationProvider(output));

        boolean includeServer = event.includeServer();
        ModBlockTagsProvider blockTags = new ModBlockTagsProvider(output, provider, helper);
        generator.addProvider(includeServer, blockTags);
        generator.addProvider(includeServer, new ModItemTagsProvider(output, provider, blockTags.contentsGetter(), helper));
        generator.addProvider(includeServer, new ModEntityTagsProvider(output, provider, helper));
        generator.addProvider(includeServer, new ModRecipeProvider(output, provider));

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {}
}
