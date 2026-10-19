package snaypertihcreator.mineralslimes.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.datagen.extra.SlimeAnimationProvider;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

public class ModSlimeAnimationProvider extends SlimeAnimationProvider {
    private static final ResourceLocation DEFAULT =
            modLoc("animations/entity/slime_default.animation.json");

    public ModSlimeAnimationProvider(PackOutput output){
        super(output, MineralSlimes.MODID);
    }

    private ResourceLocation preset(String name){
        return modLoc("template/animations/entity/%s.animation.json".formatted(name));
    }

    @Override
    protected void addAnimations() {
        merge(DEFAULT, preset("rocky"), "rocky_slime");
        merge(DEFAULT, preset("pink"), "pink_slime");
    }
}
