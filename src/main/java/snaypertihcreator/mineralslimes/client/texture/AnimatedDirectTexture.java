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
    private boolean interpolate = false;
    private boolean closed = false;
    private NativeImage scratch;   // буфер для смешанного кадра, ленивый

    public AnimatedDirectTexture(ResourceLocation location) {
        this.location = location;
    }

    @SuppressWarnings({"resource", "DataFlowIssue"})
    @Override
    public void load(ResourceManager rm) throws IOException {
        Resource resource = rm.getResourceOrThrow(location);

        TextureMetadataSection tm = resource.metadata().getSection(TextureMetadataSection.SERIALIZER).orElse(null);
        boolean newBlur  = tm != null && tm.isBlur();
        boolean newClamp = tm != null && tm.isClamp();

        AnimationMetadataSection am = resource.metadata().getSection(AnimationMetadataSection.SERIALIZER).orElse(null);
        boolean newInterpolate = am != null && am != AnimationMetadataSection.EMPTY && am.isInterpolatedFrames();

        NativeImage[] newFrames = null;
        int[] newTimes;
        int newFrameWidth, newFrameHeight;
        boolean newAnimated;

        try (InputStream is = resource.open()) {
            NativeImage full = NativeImage.read(is);

            int imgW = full.getWidth();
            int imgH = full.getHeight();

            boolean hasAnim = am != null && am != AnimationMetadataSection.EMPTY;

            if (hasAnim) {
                var size = am.calculateFrameSize(imgW, imgH);
                newFrameWidth  = size.width();
                newFrameHeight = size.height();
            } else if (imgH > imgW && imgW > 0 && imgH % imgW == 0) {
                newFrameWidth  = imgW;
                newFrameHeight = imgW;
            } else {
                newFrameWidth  = imgW;
                newFrameHeight = imgH;
            }

            int cols = imgW / newFrameWidth;
            int rows = imgH / newFrameHeight;
            int totalCells = cols * rows;

            final List<Integer> indices = new ArrayList<>();
            final List<Integer> times   = new ArrayList<>();

            if (hasAnim) {
                am.forEachFrame((index, time) -> {
                    if (index >= 0) {
                        indices.add(index);
                        times.add(time > 0 ? time : am.getDefaultFrameTime());
                    }
                });
            }
            if (indices.isEmpty()) {
                int defaultTime = hasAnim ? am.getDefaultFrameTime() : 1;
                if (defaultTime <= 0) defaultTime = 1;
                for (int i = 0; i < totalCells; i++) {
                    indices.add(i);
                    times.add(defaultTime);
                }
            }

            newAnimated = indices.size() > 1;
            int count = indices.size();
            if (count == 0) {
                count = 1;
                indices.add(0);
                times.add(1);
                newAnimated = false;
            }

            newFrames = new NativeImage[count];
            newTimes = new int[count];

            for (int i = 0; i < count; i++) {
                int idx = indices.get(i);
                newTimes[i] = times.get(i);
                int col = idx % cols;
                int row = idx / cols;
                newFrames[i] = new NativeImage(newFrameWidth, newFrameHeight, false);
                full.copyRect(newFrames[i], col * newFrameWidth, row * newFrameHeight, 0, 0, newFrameWidth, newFrameHeight, false, false);
            }

            full.close();
        } catch (Throwable t) {
            if (newFrames != null) for (NativeImage f : newFrames) if (f != null) f.close();
            throw t;
        }
        if (frames != null) {
            for (NativeImage img : frames) if (img != null) img.close();
        }
        frames       = newFrames;
        frameTimes   = newTimes;
        frameWidth   = newFrameWidth;
        frameHeight  = newFrameHeight;
        animated     = newAnimated;
        blur         = newBlur;
        clamp        = newClamp;
        interpolate  = newInterpolate;
        currentFrame = 0;
        tickCounter  = 0;

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
        if (!animated || frames == null) return;
        tickCounter++;
        int duration = frameTimes[currentFrame];
        if (tickCounter >= duration) {
            tickCounter = 0;
            currentFrame = (currentFrame + 1) % frames.length;
            uploadFrame(currentFrame);
        } else if (interpolate && duration > 1) {
            int next = (currentFrame + 1) % frames.length;
            uploadBlended(currentFrame, next, (float) tickCounter / duration);
        }
    }

    private void uploadBlended(int cur, int next, float t) {
        if (scratch == null || scratch.getWidth() != frameWidth || scratch.getHeight() != frameHeight) {
            if (scratch != null) scratch.close();
            scratch = new NativeImage(frameWidth, frameHeight, false);
        }
        NativeImage a = frames[cur];
        NativeImage b = frames[next];
        for (int y = 0; y < frameHeight; y++) {
            for (int x = 0; x < frameWidth; x++) {
                scratch.setPixelRGBA(x, y, lerpPixel(a.getPixelRGBA(x, y), b.getPixelRGBA(x, y), t));
            }
        }
        GlStateManager._bindTexture(this.getId());
        scratch.upload(0, 0, 0, 0, 0, frameWidth, frameHeight, blur, clamp);
    }

    private static int lerpPixel(int ca, int cb, float t) {
        int r = lerpChannel(ca          & 0xFF, cb          & 0xFF, t);
        int g = lerpChannel((ca >>> 8)  & 0xFF, (cb >>> 8)  & 0xFF, t);
        int b = lerpChannel((ca >>> 16) & 0xFF, (cb >>> 16) & 0xFF, t);
        int a = lerpChannel((ca >>> 24) & 0xFF, (cb >>> 24) & 0xFF, t);
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    private static int lerpChannel(int ca, int cb, float t) {
        return Math.round(ca + (cb - ca) * t);
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        if (scratch != null) {
            scratch.close();
            scratch = null;
        }
        if (frames != null) {
            for (NativeImage img : frames) if (img != null) img.close();
            frames = null;
        }
        this.releaseId();
    }
}