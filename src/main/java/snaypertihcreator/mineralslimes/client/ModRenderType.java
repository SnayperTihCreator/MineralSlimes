package snaypertihcreator.mineralslimes.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public final class ModRenderType {
    private ModRenderType() {}

    private static final RenderStateShard.ShaderStateShard QUANTUM_SHADER =
            new RenderStateShard.ShaderStateShard(() -> ClientShaders.quantumSlime);

    public static final Function<ResourceLocation, RenderType> quantumSlime = Util.memoize(resource -> RenderType.create(
            "mineralslimes_quantum_slime",
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256,
            false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(QUANTUM_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(resource, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(true)
            ));
}
