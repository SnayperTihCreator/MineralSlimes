package snaypertihcreator.mineralslimes.mixin;

import guideme.color.ColorValue;
import guideme.color.ConstantColor;
import guideme.document.LytRect;
import guideme.internal.screen.GuideScreen;
import guideme.render.GuiSprite;
import guideme.render.RenderContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import snaypertihcreator.mineralslimes.client.CustomGuideUi;

@Mixin(value = GuideScreen.class, remap = false)
public abstract class GuideScreenMixin {
    @Unique
    private static final String SCALE_RENDER = "Lguideme/render/RenderContext;fillIcon(Lguideme/document/LytRect;Lguideme/render/GuiSprite;Lguideme/color/ColorValue;)V";

    @Redirect(method = "scaledRender", at= @At(value = "INVOKE", target = SCALE_RENDER))
    private void mineralslimes$background(RenderContext context, LytRect bounds, GuiSprite guiSprite, ColorValue color) {
        CustomGuideUi.drawScreenBackground(context, bounds);
    }

    @Redirect(method = "scaledRender", at = @At(value = "NEW", target = "guideme/color/ConstantColor"))
    private ConstantColor mineralslimes$overlayColor(int color) {
        return new ConstantColor(CustomGuideUi.documentOverlayColor());
    }
}
