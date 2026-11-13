package snaypertihcreator.mineralslimes.client.parts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.ModTags;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.parts.EmitterPart;

@EventBusSubscriber(modid = MineralSlimes.MODID, value = Dist.CLIENT)
public final class WrenchArrowRenderer {

    private WrenchArrowRenderer() {}

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || !(mc.hitResult instanceof BlockHitResult hit)) return;
        if (!player.getMainHandItem().is(ModTags.Items.WRENCH)) return;
        if (!(mc.level.getBlockEntity(hit.getBlockPos()) instanceof CorralBlockEntity corral)) return;

        float angle = FencePart.angleFrom(hit.getBlockPos(), hit.getLocation());
        FencePart part = corral.getPartNear(angle, FencePart.STEP / 2F);
        if (part == null) return;

        double rad = Math.toRadians(part.getAngle());
        Vec3 dir = new Vec3(Math.sin(rad), 0, -Math.cos(rad));
        Vec3 center = Vec3.atCenterOf(hit.getBlockPos());
        double start = Math.max(0.25, EmitterPart.ATTACHED_RADIUS - 0.25);
        Vec3 origin = center.add(dir.scale(start)).add(0, 0.05, 0);

        PoseStack pose = event.getPoseStack();
        Vec3 cam = event.getCamera().getPosition();
        pose.pushPose();
        pose.translate((float) -cam.x, (float) -cam.y, (float) -cam.z);

        VertexConsumer buf = mc.renderBuffers().bufferSource().getBuffer(RenderType.debugQuads());
        arrow(buf, pose, origin, dir, new Vec3(-dir.z, 0, dir.x));
        arrow(buf, pose, origin, dir, new Vec3(0, 1, 0));
        mc.renderBuffers().bufferSource().endBatch(RenderType.debugQuads());

        pose.popPose();
    }

    private static void arrow(VertexConsumer buf, PoseStack pose, Vec3 o, Vec3 u, Vec3 v) {
        quad(buf, pose, o, u, v, 0.05, 0.30, 0.035);
        tri(buf, pose, o, u, v, 0.30, 0.48, 0.11);
    }

    private static Vec3 p(Vec3 o, Vec3 u, Vec3 v, double du, double dv) {
        return new Vec3(o.x + u.x * du + v.x * dv, o.y + u.y * du + v.y * dv, o.z + u.z * du + v.z * dv);
    }

    private static void vert(VertexConsumer buf, PoseStack pose, Vec3 pos) {
        buf.addVertex(pose.last(), (float) pos.x, (float) pos.y, (float) pos.z).setColor(64, 224, 255, 185);
    }

    private static void quad(VertexConsumer buf, PoseStack pose, Vec3 o, Vec3 u, Vec3 v, double u0, double u1, double w) {
        Vec3 a = p(o, u, v, u0, w), b = p(o, u, v, u1, w), c = p(o, u, v, u1, -w), d = p(o, u, v, u0, -w);
        vert(buf, pose, a); vert(buf, pose, b); vert(buf, pose, c); vert(buf, pose, d);
        vert(buf, pose, d); vert(buf, pose, c); vert(buf, pose, b); vert(buf, pose, a);
    }

    private static void tri(VertexConsumer buf, PoseStack pose, Vec3 o, Vec3 u, Vec3 v, double u0, double uTip, double w) {
        Vec3 l = p(o, u, v, u0, w), r = p(o, u, v, u0, -w), t = p(o, u, v, uTip, 0);
        vert(buf, pose, l); vert(buf, pose, t); vert(buf, pose, t); vert(buf, pose, r);
        vert(buf, pose, r); vert(buf, pose, t); vert(buf, pose, t); vert(buf, pose, l);
    }
}
