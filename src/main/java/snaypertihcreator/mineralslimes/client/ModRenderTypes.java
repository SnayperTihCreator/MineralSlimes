package snaypertihcreator.mineralslimes.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;

import java.util.function.Function;

public final class ModRenderTypes extends RenderStateShard{
    @SuppressWarnings("DataFlowIssue")
    private ModRenderTypes(){
        super("", null, null);
    }

    @Contract(pure = true)
    private static @NotNull String names(String name){
        return "%s_%s".formatted(MineralSlimes.MODID, name);
    }

    private static final ShaderStateShard GLITCH_SLIME_SHADER =
            new ShaderStateShard(() -> ClientShaders.glitchSlime);
    private static final ShaderStateShard FIELD_WALL_SHADER =
            new ShaderStateShard(() -> ClientShaders.fieldWall);

    public static final Function<ResourceLocation, RenderType> glitchSlime = Util.memoize(resource -> RenderType.create(
            names("quantum_slime"),
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256,
            false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(GLITCH_SLIME_SHADER)
                    .setTextureState(new TextureStateShard(resource, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setCullState(NO_CULL)
                    .createCompositeState(true)
            ));

    public static final RenderType FIELD_WALL = RenderType.create(
            names("field_wall"),
            DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 4096,
            true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(FIELD_WALL_SHADER)
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setTextureState(NO_TEXTURE)
                    .createCompositeState(false)
    );
}
