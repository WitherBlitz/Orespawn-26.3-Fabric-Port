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
import orespawn.client.original.OriginalModels;

/**
 * Ported from OreSpawn's ModelCrab (geometry converted by tools/convert_model.py, boxes, pivots and rotations
 * unchanged). OreSpawn drew its one three-piece leg (leg1 to leg3) eight times, moving it between draws; here those
 * are eight leg parts, each a group turned about its pivot holding the three pieces, which renders the same. The
 * other parts are grouped (body, each eye stalk, each claw) under parts that don't move, so the crab has 15 top-level
 * parts. Reads {@code state.attacking} (faster eye, mouth and claw motion).
 */
@Environment(EnvType.CLIENT)
public class CrabModel extends EntityModel<OreSpawnRenderState> {
	/** OreSpawn's leg spots: x 36 (one side) or -36, z 0, 10, 20, 30. */
	private static final float[] LEG_X = {36.0F, 36.0F, 36.0F, 36.0F, -36.0F, -36.0F, -36.0F, -36.0F};
	private static final float[] LEG_Z = {0.0F, 10.0F, 20.0F, 30.0F, 0.0F, 10.0F, 20.0F, 30.0F};
	private final ModelPart[] legs = new ModelPart[8];
	private final ModelPart leye1;
	private final ModelPart leye2;
	private final ModelPart reye1;
	private final ModelPart reye2;
	private final ModelPart lclaw3;
	private final ModelPart lclaw4;
	private final ModelPart lclaw5;
	private final ModelPart rclaw3;
	private final ModelPart rclaw4;
	private final ModelPart rclaw5;
	private final ModelPart rmouth;
	private final ModelPart lmouth;

	public CrabModel(ModelPart root) {
		super(root);
		for (int i = 0; i < 8; i++) legs[i] = root.getChild("leg" + i);
		ModelPart leye = root.getChild("leye");
		ModelPart reye = root.getChild("reye");
		ModelPart lclaw = root.getChild("lclaw");
		ModelPart rclaw = root.getChild("rclaw");
		this.leye1 = leye.getChild("leye1");
		this.leye2 = leye.getChild("leye2");
		this.reye1 = reye.getChild("reye1");
		this.reye2 = reye.getChild("reye2");
		this.lclaw3 = lclaw.getChild("lclaw3");
		this.lclaw4 = lclaw.getChild("lclaw4");
		this.lclaw5 = lclaw.getChild("lclaw5");
		this.rclaw3 = rclaw.getChild("rclaw3");
		this.rclaw4 = rclaw.getChild("rclaw4");
		this.rclaw5 = rclaw.getChild("rclaw5");
		this.rmouth = root.getChild("rmouth");
		this.lmouth = root.getChild("lmouth");
	}

	public static LayerDefinition createBodyLayer() {
		OriginalModels.Geometry g = OriginalModels.geometry("ModelCrab");
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
		for (String name : new String[] {"body1", "body2", "body3", "body4", "body5", "body6"}) OriginalModels.copy(body, g.part(name), name);
		// OreSpawn draws its one leg (three parts) eight times, moved and turned; each copy turns about its own pivot.
		float legY = g.part("leg1").y;
		for (int i = 0; i < 8; i++) {
			float side = LEG_X[i] > 0.0F ? -1.0F : 1.0F;
			PartDefinition leg = root.addOrReplaceChild("leg" + i, CubeListBuilder.create(),
				PartPose.offsetAndRotation(LEG_X[i], legY, LEG_Z[i], 0.0F, side * (float) (Math.PI / 2), 0.0F));
			for (String name : new String[] {"leg1", "leg2", "leg3"}) {
				OriginalModels.Part p = g.part(name);
				leg.addOrReplaceChild(name, p.cubes(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, p.xRot, 0.0F, 0.0F));
			}
		}
		group(root, g, "leye", "leye1", "leye2");
		group(root, g, "reye", "reye1", "reye2");
		group(root, g, "lclaw", "lclaw1", "lclaw2", "lclaw3", "lclaw4", "lclaw5");
		group(root, g, "rclaw", "rclaw1", "rclaw2", "rclaw3", "rclaw4", "rclaw5");
		OriginalModels.copy(root, g.part("rmouth"), "rmouth");
		OriginalModels.copy(root, g.part("lmouth"), "lmouth");
		return g.layer(mesh);
	}

	/** A group (no boxes of its own) holding OreSpawn's named parts, so they can move together. */
	private static void group(PartDefinition root, OriginalModels.Geometry g, String group, String... names) {
		PartDefinition parent = root.addOrReplaceChild(group, CubeListBuilder.create(), PartPose.ZERO);
		for (String name : names) OriginalModels.copy(parent, g.part(name), name);
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f1 = state.walkAnimationSpeed, f2 = state.ageInTicks;
		// ModelCrab.render: legs at z 0 and 20 swing one way, z 10 and 30 the other; the -36 side is mirrored.
		float swing = Mth.cos(f2 * 1.7F) * (float) Math.PI * 0.15F * f1;
		for (int i = 0; i < 8; i++) {
			float angle = (float) (-Math.PI / 2) + (i % 2 == 0 ? swing : -swing);
			legs[i].yRot = LEG_X[i] > 0.0F ? angle : -angle;
		}
		if (!state.attacking) {
			this.leye1.xRot = this.leye2.xRot = Mth.cos(f2 * 0.35F) * (float) Math.PI * 0.05F;
			this.leye1.zRot = this.leye2.zRot = 0.54F + Mth.cos(f2 * 0.25F) * (float) Math.PI * 0.05F;
			this.reye1.xRot = this.reye2.xRot = Mth.cos(f2 * 0.3F) * (float) Math.PI * 0.05F;
			this.reye1.zRot = this.reye2.zRot = -0.54F + Mth.cos(f2 * 0.45F) * (float) Math.PI * 0.05F;
			this.lmouth.yRot = -0.72F + Mth.cos(f2 * 0.25F) * (float) Math.PI * 0.05F;
			this.rmouth.yRot = 0.72F - Mth.cos(f2 * 0.25F) * (float) Math.PI * 0.05F;
			float newangle = Mth.cos(f2 * 0.15F) * (float) Math.PI * 0.03F;
			this.lclaw3.yRot = -0.453F + newangle;
			this.lclaw4.yRot = -0.349F + newangle;
			this.lclaw5.yRot = 0.384F - newangle;
			newangle = Mth.cos(f2 * 0.13F) * (float) Math.PI * 0.02F;
			this.rclaw3.yRot = 0.453F + newangle;
			this.rclaw4.yRot = 0.349F + newangle;
			this.rclaw5.yRot = -0.384F - newangle;
		} else {
			this.leye1.xRot = this.leye2.xRot = Mth.cos(f2 * 0.45F) * (float) Math.PI * 0.1F;
			this.leye1.zRot = this.leye2.zRot = 0.54F + Mth.cos(f2 * 0.35F) * (float) Math.PI * 0.1F;
			this.reye1.xRot = this.reye2.xRot = Mth.cos(f2 * 0.4F) * (float) Math.PI * 0.1F;
			this.reye1.zRot = this.reye2.zRot = -0.54F + Mth.cos(f2 * 0.55F) * (float) Math.PI * 0.1F;
			this.lmouth.yRot = -0.72F + Mth.cos(f2 * 0.45F) * (float) Math.PI * 0.15F;
			this.rmouth.yRot = 0.72F - Mth.cos(f2 * 0.45F) * (float) Math.PI * 0.15F;
			float newangle = Mth.cos(f2 * 0.35F) * (float) Math.PI * 0.13F;
			this.lclaw3.yRot = -0.453F + newangle;
			this.lclaw4.yRot = -0.349F + newangle;
			this.lclaw5.yRot = 0.384F - newangle;
			newangle = Mth.cos(f2 * 0.43F) * (float) Math.PI * 0.12F;
			this.rclaw3.yRot = 0.453F + newangle;
			this.rclaw4.yRot = 0.349F + newangle;
			this.rclaw5.yRot = -0.384F - newangle;
		}
	}
}
