package snaypertihcreator.mineralslimes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
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
    private FieldChunkRenderer() {}

    private static final int CHUNK_RADIUS = 2;
    private static final double HALF_T = 0.01;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event){
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;

        Vec3 cam = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        VertexConsumer lines = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
        ChunkPos center = mc.player.chunkPosition();
        boolean visible = false;

        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);

        for (int cx = center.x - CHUNK_RADIUS; cx <= center.x + CHUNK_RADIUS; cx++) {
            for (int cz = center.z - CHUNK_RADIUS; cz <= center.z + CHUNK_RADIUS; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (var be : chunk.getBlockEntities().values()) {
                    if (be instanceof InductorBlockEntity ind && !ind.getNeighbors().isEmpty() && drawLinks(ind, level, pose, lines))
                        visible = true;
                }
            }
        }
        pose.popPose();

        if (visible) mc.renderBuffers().bufferSource().endBatch(RenderType.lines());
    }

    private static boolean drawLinks(InductorBlockEntity be, ClientLevel level, PoseStack pose, VertexConsumer lines) {
        boolean visible = false;
        for (BlockPos other : be.getNeighbors()) {
            if (be.getBlockPos().compareTo(other) >= 0) continue;
            if (drawPrism(be.getBlockPos(), other, level, pose, lines)) visible = true;
        }
        return visible;
    }

    private static boolean drawPrism(BlockPos a, BlockPos b, ClientLevel level, PoseStack pose, VertexConsumer lines) {
        double ax = a.getX() + 0.5, az = a.getZ() + 0.5;
        double dx = b.getX() + 0.5 - ax, dz = b.getZ() + 0.5 - az;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 1.0) return false;

        int h = InductorBlockEntity.columnHeight(level, a);
        if (h < 0) return false;
        double baseY = a.getY() - h;
        double wallH = (h + 1) * InductorBlockEntity.WALL_HEIGHT;

        float yaw = (float) Math.toDegrees(Math.atan2(-dz, dx));

        pose.pushPose();
        pose.translate(ax, baseY, az);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        LevelRenderer.renderLineBox(pose, lines,
                new AABB(0, 0, -HALF_T, len, wallH, HALF_T),
                1.0F, 1.0F, 0.0F, 1.0F);
        pose.popPose();
        return true;
    }
}
