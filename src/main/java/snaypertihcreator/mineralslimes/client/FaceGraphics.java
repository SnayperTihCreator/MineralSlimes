package snaypertihcreator.mineralslimes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;


public final class FaceGraphics implements AutoCloseable {

    private sealed interface RenderData {
        record Str(String value) implements RenderData {}
        record Comp(Component value) implements RenderData {}
    }

    private final PoseStack pose;
    private final MultiBufferSource buffers;
    private final Font font;
    private final int light;
    private boolean active;

    public FaceGraphics(PoseStack pose, MultiBufferSource buffers, Font font, int light) {
        this.pose = pose;
        this.buffers = buffers;
        this.font = font;
        this.light = light;
    }

    public static float px(float pixels){
        return pixels/16F;
    }

    public FaceGraphics face(Direction face){
        return face(face, 0.002F);
    }

    public FaceGraphics face(Direction face, float offset){
        if (active) pose.popPose();
        pose.pushPose();
        pose.mulPose(basic(face));
        pose.translate(0, 0, offset);
        active = true;
        return this;
    }

    public FaceGraphics faceYaw(float yawDeg){
        return faceYaw(yawDeg, 0.002F);
    }

    public FaceGraphics faceYaw(float yawDeg, float offset){
        if (active) pose.popPose();
        pose.pushPose();
        float t = (float) Math.toRadians(yawDeg);
        float sinYaw = Mth.sin(t);
        float cosYaw = Mth.cos(t);
        pose.mulPose(new Matrix4f(
                cosYaw, 0, -sinYaw, 0,
                0, -1, 0, 0,
                sinYaw, 0, cosYaw, 0,
                0, 0, 0, 1
        ));
        pose.translate(0, 0, offset);
        active = true;
        return this;
    }

    public FaceGraphics faceAngle(float angle){
        return faceYaw((float) Math.toDegrees(angle) + 180F);
    }

    public FaceGraphics faceAngleDeg(float angle){
        return faceYaw(angle + 180F);
    }

    public FaceGraphics faceCamera(){
        if (active) pose.popPose();
        pose.pushPose();
        pose.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        pose.scale(-1F, -1F, 1F);
        active = true;
        return this;

    }

    @SuppressWarnings("UnusedReturnValue")
    public FaceGraphics endFace(){
        if (active){
            pose.popPose();
            active = false;
        }
        return this;
    }

    @SuppressWarnings("resource")
    @Override
    public void close() {
        endFace();
    }


    public FaceGraphics drawText(String text, float x, float y, int argb,
                                 float height, boolean centerX, boolean shadow){
        return drawTextRaw(Component.literal(text), x, y, argb, height/font.lineHeight, centerX, shadow);
    }

    public FaceGraphics drawText(Component text, float x, float y, int argb,
                                 float height, boolean centerX, boolean shadow){
        return drawTextRaw(text, x, y, argb, height/font.lineHeight, centerX, shadow);
    }

    private FaceGraphics drawTextRaw(Component text, float x, float y, int argb,
                                     float scale, boolean centerX, boolean shadow){
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, scale);
        if (centerX) pose.translate(-font.width(text)/2F, 0, 0);

        font.drawInBatch(text, 0, 0, argb, shadow, pose.last().pose(), buffers, Font.DisplayMode.POLYGON_OFFSET, 0, light);
        pose.popPose();
        return this;
    }

    public FaceGraphics blit(ResourceLocation texture, float x, float y, float w, float h, int argb){
        return blit(texture, x, y, w, h, 0, 0, 1, 1, argb);
    }

    public FaceGraphics blit(ResourceLocation texture, float x, float y, float w, float h,
                             float u0, float v0, float u1, float v1, int argb){
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        PoseStack.Pose p = pose.last();

        vc.addVertex(p, x, y, 0).setUv(u0, v0).setColor(argb).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(p, 0, 0, 1);
        vc.addVertex(p, x, y+h, 0).setUv(u0, v1).setColor(argb).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(p, 0, 0, 1);
        vc.addVertex(p, x+w, y+h, 0).setUv(u1, v1).setColor(argb).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(p, 0, 0, 1);
        vc.addVertex(p, x+w, y, 0).setUv(u1, v0).setColor(argb).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(p, 0, 0, 1);
        return this;
    }

    public FaceGraphics fill(float x, float y, float w, float h, int argb){
        VertexConsumer vc = buffers.getBuffer(RenderType.gui());
        Matrix4f matrix = pose.last().pose();
        vc.addVertex(matrix, x, y, 0).setColor(argb);
        vc.addVertex(matrix, x, y+h, 0).setColor(argb);
        vc.addVertex(matrix, x+w, y+h, 0).setColor(argb);
        vc.addVertex(matrix, x+w, y, 0).setColor(argb);
        return this;
    }

    private static Matrix4f basic(Direction face){
        Matrix4f matrix = new Matrix4f();
        switch (face){
            case DOWN -> matrix.set(1, 0, 0, 0,
                    0, 0, -1, 0,
                    0, 1, 0, 0,
                    0, 0, 0, 1);
            case UP -> matrix.set(1, 0, 0, 0,
                    0, 0, 1, 1,
                    0, 1, 0, 0,
                    0, 0, 0, 1);
            case NORTH -> matrix.set(-1, 0, 0, 1,
                    0, -1, 0, 1,
                    0, 0, -1, 0,
                    0, 0, 0, 1);
            case SOUTH -> matrix.set(1, 0, 0, 0,
                    0, -1, 0, 1,
                    0, 0, 1, 1,
                    0, 0, 0, 1);
            case WEST -> matrix.set(0, 0, -1, 0,
                    0, -1, 0, 1,
                    1, 0, 0, 0,
                    0, 0, 0, 1);
            case EAST -> matrix.set(0, 0, 1, 1,
                    0, -1, 0, 1,
                    -1, 0, 0, 1,
                    0, 0, 0, 1);
        }
        return matrix;
    }
}
