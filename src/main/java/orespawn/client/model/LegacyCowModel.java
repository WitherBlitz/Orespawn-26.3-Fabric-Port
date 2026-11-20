package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;

/**
 * Minecraft 1.7.10's cow (ModelCow on ModelQuadruped), which OreSpawn's apple cows used. 26.3's cow has a different
 * texture layout, so OreSpawn's 64x32 cow textures need this older shape.
 */
@Environment(EnvType.CLIENT)
public class LegacyCowModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart leg1;
	private final ModelPart leg2;
	private final ModelPart leg3;
	private final ModelPart leg4;

	public LegacyCowModel(ModelPart root) {
		super(root);
		head = root.getChild("head");
		body = root.getChild("body");
		leg1 = root.getChild("leg1");
		leg2 = root.getChild("leg2");
		leg3 = root.getChild("leg3");
		leg4 = root.getChild("leg4");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F)
				.texOffs(22, 0).addBox(-5.0F, -5.0F, -4.0F, 1.0F, 3.0F, 1.0F)
				.texOffs(22, 0).addBox(4.0F, -5.0F, -4.0F, 1.0F, 3.0F, 1.0F),
			PartPose.offset(0.0F, 4.0F, -8.0F));
		root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(18, 4).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F)
				.texOffs(52, 0).addBox(-2.0F, 2.0F, -8.0F, 4.0F, 6.0F, 1.0F),
			PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, Mth.HALF_PI, 0.0F, 0.0F));
		CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F);
		root.addOrReplaceChild("leg1", leg, PartPose.offset(-4.0F, 12.0F, 7.0F));
		root.addOrReplaceChild("leg2", leg, PartPose.offset(4.0F, 12.0F, 7.0F));
		root.addOrReplaceChild("leg3", leg, PartPose.offset(-4.0F, 12.0F, -6.0F));
		root.addOrReplaceChild("leg4", leg, PartPose.offset(4.0F, 12.0F, -6.0F));
		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed;
		head.xRot = state.xRot * Mth.DEG_TO_RAD;
		head.yRot = state.yRot * Mth.DEG_TO_RAD;
		leg1.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
		leg2.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * f1;
		leg3.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * f1;
		leg4.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
	}
}
