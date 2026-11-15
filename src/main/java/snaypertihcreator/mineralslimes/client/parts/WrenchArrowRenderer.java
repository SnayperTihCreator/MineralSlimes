package snaypertihcreator.mineralslimes.client.parts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import snaypertihcreator.mineralslimes.common.ModTags;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.parts.EmitterPart;

public final class WrenchArrowRenderer {

    private WrenchArrowRenderer() {}

    private static BlockPos focusPos = null;
    private static float focusAngle = 0F;

    public static void onPartClicked(BlockPos pos, float angle) {
        focusPos = pos.immutable();
        focusAngle = angle;
    }

    public static void clearFocus() {
        focusPos = null;
    }

    public static void tryRender(BlockPos blockPos, PoseStack pose, MultiBufferSource buffers) {
        if (focusPos == null || !focusPos.equals(blockPos)) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) { clearFocus(); return; }

        if (!player.getMainHandItem().is(ModTags.Items.WRENCH) && !player.getOffhandItem().is(ModTags.Items.WRENCH))
        { clearFocus(); return; }

        if (mc.screen != null) { clearFocus(); return; }

        if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() == HitResult.Type.MISS
                || !hit.getBlockPos().equals(blockPos)) { clearFocus(); return; }

        if (!(mc.level.getBlockEntity(blockPos) instanceof CorralBlockEntity corral)) { clearFocus(); return; }

        FencePart focus = corral.getPartNear(focusAngle, FencePart.STEP);
        if (focus == null) { clearFocus(); return; }
        focusAngle = focus.getAngle();

        double rad = Math.toRadians(focus.getAngle());
        double dx = Math.sin(rad), dz = -Math.cos(rad);
        double start = Math.max(0.25, EmitterPart.ATTACHED_RADIUS - 0.25);
        Vec3 o = new Vec3(0.5 + dx * start, 0.55, 0.5 + dz * start);
        Vec3 u = new Vec3(dx, 0, dz);

        var p = pose.last();
        VertexConsumer buf = buffers.getBuffer(RenderType.debugQuads());
        arrow(buf, p, o, u, new Vec3(-dz, 0, dx));
        arrow(buf, p, o, u, new Vec3(0, 1, 0));
    }

    private static Vec3 p(Vec3 o, Vec3 u, Vec3 v, double du, double dv) {
        return new Vec3(o.x + u.x * du + v.x * dv, o.y + u.y * du + v.y * dv, o.z + u.z * du + v.z * dv);
    }

    private static void arrow(VertexConsumer buf, PoseStack.Pose pose, Vec3 o, Vec3 u, Vec3 v) {
        quad(buf, pose, o, u, v, 0.05, 0.30, 0.035);
        tri(buf, pose, o, u, v, 0.30, 0.48, 0.11);
    }

    private static void vert(VertexConsumer buf, PoseStack.Pose pose, Vec3 pos) {
        buf.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z).setColor(64, 224, 255, 185);
    }

    private static void quad(VertexConsumer buf, PoseStack.Pose pose, Vec3 o, Vec3 u, Vec3 v, double u0, double u1, double w) {
        Vec3 a = p(o, u, v, u0, w), b = p(o, u, v, u1, w), c = p(o, u, v, u1, -w), d = p(o, u, v, u0, -w);
        vert(buf, pose, a); vert(buf, pose, b); vert(buf, pose, c); vert(buf, pose, d);
        vert(buf, pose, d); vert(buf, pose, c); vert(buf, pose, b); vert(buf, pose, a);
    }

    private static void tri(VertexConsumer buf, PoseStack.Pose pose, Vec3 o, Vec3 u, Vec3 v, double u0, double uTip, double w) {
        Vec3 l = p(o, u, v, u0, w), r = p(o, u, v, u0, -w), t = p(o, u, v, uTip, 0);
        vert(buf, pose, l); vert(buf, pose, t); vert(buf, pose, t); vert(buf, pose, r);
        vert(buf, pose, r); vert(buf, pose, t); vert(buf, pose, t); vert(buf, pose, l);
    }
}