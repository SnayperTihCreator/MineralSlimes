package snaypertihcreator.mineralslimes.client;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import snaypertihcreator.mineralslimes.MineralSlimes;

import java.io.IOException;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

@EventBusSubscriber(modid = MineralSlimes.MODID, value = Dist.CLIENT)
public final class ClientShaders {
    private ClientShaders() {}

    public static class QuantumSlimeShader extends ShaderInstance {
        public QuantumSlimeShader(ResourceProvider provider, ResourceLocation location, VertexFormat format) throws IOException {
            super(provider, location, format);
        }

        @Override
        public void apply() {
            Camera cam = Minecraft.getInstance().gameRenderer.getMainCamera();

            Matrix3f worldToView = new Matrix3f().rotation(cam.rotation());
            Matrix3f viewToWorld = worldToView.invert(new Matrix3f());

            Vector3f col = new Vector3f();
            for (int i = 0; i < 3; i++) {
                Uniform u = getUniform("QV2W" + i);
                if (u != null) {
                    viewToWorld.getColumn(i, col);
                    u.set(col.x(), col.y(), col.z());
                }
            }

            super.apply();
        }
    }

    public static final ResourceLocation QuantumSlimeID = modLoc("quantum_slime");
    public static ShaderInstance quantumSlime;

    @SubscribeEvent
    public static void onRegisterShaders(@NotNull RegisterShadersEvent event) throws IOException {
        var loader = new QuantumSlimeShader(event.getResourceProvider(), QuantumSlimeID, DefaultVertexFormat.NEW_ENTITY);
        event.registerShader(loader, shader -> quantumSlime=shader);
    }
}
