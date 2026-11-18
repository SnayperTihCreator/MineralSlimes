package snaypertihcreator.mineralslimes.client.entity;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import snaypertihcreator.mineralslimes.client.ModRenderType;
import snaypertihcreator.mineralslimes.common.entity.ModSlimes;
import snaypertihcreator.mineralslimes.common.entity.slimes.QuantumSlimeEntity;
import software.bernie.geckolib.cache.object.GeoBone;

public class QuantumSlimeRenderer<T extends QuantumSlimeEntity> extends SlimeRenderer<T>{
    public QuantumSlimeRenderer(EntityRendererProvider.Context ctx){
        super(ctx, ModSlimes.QUANTUM.slimeType());
    }

    @Override
    protected @Nullable RenderType getRenderTypeForBone(GeoBone bone, T animatable, @Nullable ResourceLocation boneTexture) {
        if (animatable.isQuantumPhase() && !"face".equals(bone.getName())) {
            return ModRenderType.quantum.apply(boneTexture != null ? boneTexture : this.baseTexture);
        }
        return super.getRenderTypeForBone(bone, animatable, boneTexture);
    }
}
