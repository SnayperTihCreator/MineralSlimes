package snaypertihcreator.mineralslimes.client;

import guideme.color.ConstantColor;
import guideme.document.LytRect;
import guideme.render.RenderContext;

public class CustomGuideUi {
    private static final int DOCUMENT_OVERLAY = 0x40202028;

    private CustomGuideUi() {}

    public static void drawScreenBackground(RenderContext context, LytRect rect) {
        context.fillRect(rect, new ConstantColor(0xFF01df45));
    }

    public static int documentOverlayColor() {
        return DOCUMENT_OVERLAY;
    }


}
