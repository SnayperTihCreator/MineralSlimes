package snaypertihcreator.mineralslimes.client;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.blockentity.InductorBlockEntity;

@EventBusSubscriber(modid = MineralSlimes.MODID, value = Dist.CLIENT)
public final class FieldChunkRenderer {
    private static final int CHUNK_RADIUS = 2;
    private static final int EDGE_ALPHA = 90;
    private static final int MID_ALPHA = 200;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null || ClientShaders.fieldWall == null) return;

        Vec3 cam = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer vc = buffers.getBuffer(ModRenderTypes.FIELD_WALL);
        ChunkPos center = mc.player.chunkPosition();
        boolean visible = false;

        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);

        for (int cx = center.x - CHUNK_RADIUS; cx <= center.x + CHUNK_RADIUS; cx++) {
            for (int cz = center.z - CHUNK_RADIUS; cz <= center.z + CHUNK_RADIUS; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (var be : chunk.getBlockEntities().values()) {
                    if (be instanceof InductorBlockEntity ind && drawLinks(ind, level, pose, vc))
                        visible = true;
                }
            }
        }
        pose.popPose();

        if (visible){
            float animTime = (float) ((System.currentTimeMillis() % 3600000L) / 1000.0);
            Uniform u = ClientShaders.fieldWall.getUniform("AnimTime");
            if (u != null) u.set(animTime);
            buffers.endBatch(ModRenderTypes.FIELD_WALL);
        }
    }

    private static boolean drawLinks(InductorBlockEntity be, ClientLevel level, PoseStack pose, VertexConsumer vc) {
        boolean visible = false;
        for (BlockPos other : be.getNeighbors()) {
            if (be.getBlockPos().compareTo(other) >= 0) continue;
            AABB vol = be.fieldVolume(level, other);
            if (vol != null) {
                renderFieldVolume(pose.last(), vc, vol);
                visible = true;
            }
        }
        return visible;
    }

    private static void renderFieldVolume(PoseStack.Pose pose, VertexConsumer vc, AABB box) {
        float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
        float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;
        float ym = (y0 + y1) * 0.5F;

        verticalFace(pose, vc, x0, z0, x1, z0, y0, ym, y1);
        verticalFace(pose, vc, x0, z1, x1, z1, y0, ym, y1);
        verticalFace(pose, vc, x1, z0, x1, z1, y0, ym, y1);
        verticalFace(pose, vc, x0, z0, x0, z1, y0, ym, y1);
        capFace(pose, vc, x0, x1, z0, z1, y1);
        capFace(pose, vc, x0, x1, z0, z1, y0);
    }

    private static void verticalFace(PoseStack.Pose pose, VertexConsumer vc, float ax, float az, float bx, float bz, float y0, float ym, float y1) {
        float v0 = 0.0F, vm = ym - y0, v1 = y1 - y0;
        vc.addVertex(pose, ax, y0, az).setUv(ax, v0).setColor(255, 255, 255, EDGE_ALPHA);
        vc.addVertex(pose, bx, y0, bz).setUv(bx, v0).setColor(255, 255, 255, EDGE_ALPHA);
        vc.addVertex(pose, bx, ym, bz).setUv(bx, vm).setColor(255, 255, 255, MID_ALPHA);
        vc.addVertex(pose, ax, ym, az).setUv(ax, vm).setColor(255, 255, 255, MID_ALPHA);
        vc.addVertex(pose, ax, ym, az).setUv(ax, vm).setColor(255, 255, 255, MID_ALPHA);
        vc.addVertex(pose, bx, ym, bz).setUv(bx, vm).setColor(255, 255, 255, MID_ALPHA);
        vc.addVertex(pose, bx, y1, bz).setUv(bx, v1).setColor(255, 255, 255, EDGE_ALPHA);
        vc.addVertex(pose, ax, y1, az).setUv(ax, v1).setColor(255, 255, 255, EDGE_ALPHA);
    }

    private static void capFace(PoseStack.Pose pose, VertexConsumer vc, float x0, float x1, float z0, float z1, float y) {
        vc.addVertex(pose, x0, y, z0).setUv(x0, z0).setColor(255, 255, 255, MID_ALPHA);
        vc.addVertex(pose, x1, y, z0).setUv(x1, z0).setColor(255, 255, 255, MID_ALPHA);
        vc.addVertex(pose, x1, y, z1).setUv(x1, z1).setColor(255, 255, 255, MID_ALPHA);
        vc.addVertex(pose, x0, y, z1).setUv(x0, z1).setColor(255, 255, 255, MID_ALPHA);
    }
}

