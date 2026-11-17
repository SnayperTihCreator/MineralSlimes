package snaypertihcreator.mineralslimes.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import snaypertihcreator.mineralslimes.MineralSlimes;

import java.io.IOException;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

@EventBusSubscriber(modid = MineralSlimes.MODID, value = Dist.CLIENT)
public final class ClientShaders {
    private ClientShaders() {}

    public static final ResourceLocation QuantumSlimeID = modLoc("quantum_slime");
    public static ShaderInstance quantumSlime;

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        var loader = new ShaderInstance(event.getResourceProvider(), QuantumSlimeID, DefaultVertexFormat.NEW_ENTITY);
        event.registerShader(loader, shader -> quantumSlime=shader);
    }
}
