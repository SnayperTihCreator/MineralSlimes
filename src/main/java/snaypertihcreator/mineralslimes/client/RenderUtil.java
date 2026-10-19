package snaypertihcreator.mineralslimes.client;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public final class RenderUtil {
    private static final Class<?> COMPOSITE_RT_CLASS;
    private static final Class<?> COMPOSITE_STATE_CLASS;
    private static final Field STATE_FIELD;
    private static final Field TRANSPARENCY_FIELD;
    private static final Object TRANSLUCENT_REF;

    static {
        try {
            String base = "net.minecraft.client.renderer.RenderType";
            COMPOSITE_RT_CLASS  = Class.forName("%s$CompositeRenderType".formatted(base));
            COMPOSITE_STATE_CLASS = Class.forName("%s$CompositeState".formatted(base));

            STATE_FIELD = COMPOSITE_RT_CLASS.getDeclaredField("state");
            STATE_FIELD.setAccessible(true);

            TRANSPARENCY_FIELD = COMPOSITE_STATE_CLASS.getDeclaredField("transparencyState");
            TRANSPARENCY_FIELD.setAccessible(true);

            Field f = RenderStateShard.class.getDeclaredField("TRANSLUCENT_TRANSPARENCY");
            f.setAccessible(true);
            TRANSLUCENT_REF = f.get(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to init translucent RenderType detector", e);
        }
    }

    public static boolean isTranslucentRenderType(@Nullable RenderType rt) {
        if (rt == null) return false;
        if (!COMPOSITE_RT_CLASS.isInstance(rt)) return false;
        try {
            Object state = STATE_FIELD.get(rt);
            if (state == null) return false;
            return TRANSPARENCY_FIELD.get(state) == TRANSLUCENT_REF;
        } catch (IllegalAccessException e) {
            return false;
        }
    }
}
