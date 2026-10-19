package snaypertihcreator.mineralslimes.client.models;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

@SuppressWarnings("RedundantSuppression")
public abstract class BaseModel<T extends GeoAnimatable> extends GeoModel<T> {

    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return getTextureResource(animatable, null);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getModelResource(T animatable) {
        return getModelResource(animatable, null);
    }
}
