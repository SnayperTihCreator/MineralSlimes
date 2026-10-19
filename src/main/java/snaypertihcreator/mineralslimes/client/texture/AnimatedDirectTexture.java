package snaypertihcreator.mineralslimes.client.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AnimatedDirectTexture extends AbstractTexture {

    private static final int GL_TEXTURE_2D = 3553;
    private static final int GL_TEXTURE_WRAP_S = 10242;
    private static final int GL_TEXTURE_WRAP_T = 10243;
    private static final int GL_TEXTURE_MIN_FILTER = 10241;
    private static final int GL_TEXTURE_MAG_FILTER = 10240;
    private static final int GL_REPEAT = 10497;
    private static final int GL_CLAMP_TO_EDGE = 33071;
    private static final int GL_NEAREST = 9728;
    private static final int GL_LINEAR = 9729;

    private final ResourceLocation location;
    private NativeImage[] frames;
    private int[] frameTimes;
    private int frameWidth, frameHeight;
    private int currentFrame = 0;
    private int tickCounter = 0;
    private boolean animated = false;
    private boolean blur = false;
    private boolean clamp = false;

    public AnimatedDirectTexture(ResourceLocation location) {
        this.location = location;
    }

    @Override
    public void load(ResourceManager rm) throws IOException {
        Resource resource = rm.getResourceOrThrow(location);

        TextureMetadataSection tm = resource.metadata().getSection(TextureMetadataSection.SERIALIZER).orElse(null);
        blur  = tm != null && tm.isBlur();
        clamp = tm != null && tm.isClamp();

        AnimationMetadataSection am = resource.metadata().getSection(AnimationMetadataSection.SERIALIZER).orElse(null);

        try (InputStream is = resource.open()) {
            NativeImage full = NativeImage.read(is);

            int imgW = full.getWidth();
            int imgH = full.getHeight();

            boolean hasAnim = am != null && am != AnimationMetadataSection.EMPTY;

            if (hasAnim) {
                var size = am.calculateFrameSize(imgW, imgH);
                frameWidth  = size.width();
                frameHeight = size.height();
            } else if (imgH > imgW && imgW > 0 && imgH % imgW == 0) {
                frameWidth  = imgW;
                frameHeight = imgW;
            } else {
                frameWidth  = imgW;
                frameHeight = imgH;
            }

            int cols = imgW / frameWidth;
            int rows = imgH / frameHeight;
            int totalCells = cols * rows;

            final List<Integer> indices = new ArrayList<>();
            final List<Integer> times   = new ArrayList<>();

            if (hasAnim)
                am.forEachFrame((index, time) -> {indices.add(index);times.add(time > 0 ? time : am.getDefaultFrameTime());});
            if (indices.isEmpty()) {
                int defaultTime = hasAnim ? am.getDefaultFrameTime() : 1;
                if (defaultTime <= 0) defaultTime = 1;
                for (int i = 0; i < totalCells; i++) {
                    indices.add(i);
                    times.add(defaultTime);
                }
            }

            animated = indices.size() > 1;
            int count = indices.size();
            if (count == 0) {
                count = 1;
                indices.add(0);
                times.add(1);
                //noinspection DataFlowIssue
                animated = false;
            }

            frames = new NativeImage[count];
            frameTimes = new int[count];

            for (int i = 0; i < count; i++) {
                int idx = indices.get(i);
                frameTimes[i] = times.get(i);
                int col = idx % cols;
                int row = idx / cols;
                NativeImage frame = new NativeImage(frameWidth, frameHeight, false);
                full.copyRect(frame, col * frameWidth, row * frameHeight, 0, 0, frameWidth, frameHeight, false, false);
                frames[i] = frame;
            }

            full.close();
        }

        TextureUtil.prepareImage(this.getId(), 0, frameWidth, frameHeight);

        GlStateManager._bindTexture(this.getId());
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,     clamp ? GL_CLAMP_TO_EDGE : GL_REPEAT);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,     clamp ? GL_CLAMP_TO_EDGE : GL_REPEAT);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, blur  ? GL_LINEAR         : GL_NEAREST);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, blur  ? GL_LINEAR         : GL_NEAREST);

        uploadFrame(0);
    }

    private void uploadFrame(int index) {
        GlStateManager._bindTexture(this.getId());
        NativeImage frame = frames[index];
        frame.upload(0, 0, 0, 0, 0, frameWidth, frameHeight, blur, clamp);
    }

    public void tick() {
        if (!animated) return;
        tickCounter++;
        if (tickCounter >= frameTimes[currentFrame]) {
            tickCounter = 0;
            currentFrame = (currentFrame + 1) % frames.length;
            uploadFrame(currentFrame);
        }
    }

    @Override
    public void close() {
        if (frames != null) {
            for (NativeImage img : frames) if (img != null) img.close();
            frames = null;
        }
        this.releaseId();
    }
}