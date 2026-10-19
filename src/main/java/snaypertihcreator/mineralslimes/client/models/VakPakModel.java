package snaypertihcreator.mineralslimes.client.models;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import snaypertihcreator.mineralslimes.common.ModDataComponents;
import snaypertihcreator.mineralslimes.common.items.vakpak.VakPakItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

public class VakPakModel extends BaseModel<VakPakItem> {
    public static final ResourceLocation MODEL = modLoc("geo/item/vak_pak.geo.json");
    public static final ResourceLocation ANIMATION = modLoc("animations/item/vakpak.animation.json");

    public static final ResourceLocation TEXTURE_NORMAL = modLoc("textures/item/vak_pak/normal.png");
    public static final ResourceLocation TEXTURE_USED = modLoc("textures/item/vak_pak/used.png");
    public static final ResourceLocation TEXTURE_SHOT = modLoc("textures/item/vak_pak/shot.png");

    @Override
    public ResourceLocation getModelResource(VakPakItem animatable, @Nullable GeoRenderer<VakPakItem> renderer) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(VakPakItem animatable, @Nullable GeoRenderer<VakPakItem> renderer) {
        if (!(renderer instanceof GeoItemRenderer<VakPakItem> render)) return TEXTURE_NORMAL;

        var stack = render.getCurrentItemStack();
        if (stack == null) return TEXTURE_NORMAL;

        var state = stack.getOrDefault(ModDataComponents.VAKPAK_STATE, VakPakItem.VakPakState.NORMAL);
        return switch (state){
            case SHOT ->  TEXTURE_SHOT;
            case USED ->  TEXTURE_USED;
            default -> TEXTURE_NORMAL;
        };
    }

    @Override
    public ResourceLocation getAnimationResource(VakPakItem animatable) {
        return ANIMATION;
    }

    @Override
    public @Nullable RenderType getRenderType(VakPakItem animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}
