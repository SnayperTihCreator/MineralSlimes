package snaypertihcreator.mineralslimes.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.common.entity.WaterBullet;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

public class WaterBulletModel extends EntityModel<WaterBullet> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(modLoc("water_bullet"), "main");

    private final ModelPart root;

    public WaterBulletModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2F, -2F, -2F, 4F, 4F, 4F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public void setupAnim(@NotNull WaterBullet entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float wobble = 1.0F + Mth.sin(ageInTicks * 0.35F) * 0.08F;
        root.xScale = wobble;
        root.yScale = 1.0F - Mth.sin(ageInTicks * 0.35F) * 0.08F;
        root.zScale = wobble;
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
