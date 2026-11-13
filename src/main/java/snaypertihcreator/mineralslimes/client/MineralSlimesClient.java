package snaypertihcreator.mineralslimes.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.client.block.CorralRenderer;
import snaypertihcreator.mineralslimes.client.block.InductorRenderer;
import snaypertihcreator.mineralslimes.client.entity.SlimeRenderer;
import snaypertihcreator.mineralslimes.client.item.VakPakRenderer;
import snaypertihcreator.mineralslimes.client.models.FenceModelRegister;
import snaypertihcreator.mineralslimes.client.models.VakPakModel;
import snaypertihcreator.mineralslimes.client.texture.AnimatedTextureManager;
import snaypertihcreator.mineralslimes.common.blockentity.ModBlockEntity;
import snaypertihcreator.mineralslimes.common.entity.ModSlimes;
import snaypertihcreator.mineralslimes.common.fance.ModPartTypes;
import snaypertihcreator.mineralslimes.common.items.ModItems;

import static snaypertihcreator.mineralslimes.client.models.FenceModelRegister.modelKey;

@Mod(value = MineralSlimes.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MineralSlimes.MODID, value = Dist.CLIENT)
public class MineralSlimesClient {

    public MineralSlimesClient(@NotNull ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        FenceModelRegister.registerModel(event);
    }

    @SubscribeEvent
    public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        FenceModelRegister.bake(event);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (var slime: ModSlimes.all())
            event.registerEntityRenderer(slime.entityType(), context -> new SlimeRenderer<>(context, slime.slimeType()));

        event.registerBlockEntityRenderer(ModBlockEntity.CORRAL_BE.get(), CorralRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntity.INDUCTOR_BE.get(), InductorRenderer::new);
    }



    @SubscribeEvent
    public static void onRegisterExtensions(@NotNull RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private VakPakRenderer renderer;

            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) this.renderer = new VakPakRenderer();
                return this.renderer;
            }
        }, ModItems.VAK_PAK.get());
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                AnimatedTextureManager.registerAll(VakPakModel.TEXTURE_USED, VakPakModel.TEXTURE_SHOT));
    }

    @SubscribeEvent
    public static void onRegisterReload(RegisterClientReloadListenersEvent event) {
        AnimatedTextureManager.onRegisterReload(event);
    }
}
