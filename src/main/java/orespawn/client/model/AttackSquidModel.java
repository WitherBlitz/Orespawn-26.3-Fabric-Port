package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelAttackSquid (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class AttackSquidModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart tent1;
	private final ModelPart tent2;
	private final ModelPart tent3;
	private final ModelPart tent4;
	private final ModelPart tent5;
	private final ModelPart tent6;
	private final ModelPart tent7;
	private final ModelPart body;
	private final ModelPart tent8;
	private final float wingspeed;

	public AttackSquidModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.tent1 = root.getChild("tent1");
		this.tent2 = root.getChild("tent2");
		this.tent3 = root.getChild("tent3");
		this.tent4 = root.getChild("tent4");
		this.tent5 = root.getChild("tent5");
		this.tent6 = root.getChild("tent6");
		this.tent7 = root.getChild("tent7");
		this.body = root.getChild("body");
		this.tent8 = root.getChild("tent8");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelAttackSquid");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangleA = 0.0F;
		float newangleB = 0.0F;
		float newangle8 = 0.0F;
		float newangle1 = 0.0F;
		float newangle2 = 0.0F;
		float newangle3 = 0.0F;
		float newangle4 = 0.0F;
		float newangle5 = 0.0F;
		float newangle6 = 0.0F;
		float newangle7 = 0.0F;
		float pi4 = (float) (Math.PI / 4);
		if (f1 > 0.1) {
		newangleA = Mth.cos(f2 * 0.25F * this.wingspeed) * (float) Math.PI * 0.04F * f1;
		newangleB = Mth.cos(f2 * 0.39F * this.wingspeed) * (float) Math.PI * 0.04F * f1;
		newangle1 = Mth.cos(f2 * 1.2F * this.wingspeed) * (float) Math.PI * 0.4F * f1;
		newangle2 = Mth.cos(f2 * 1.1F * this.wingspeed) * (float) Math.PI * 0.4F * f1;
		newangle3 = Mth.cos(f2 * 1.0F * this.wingspeed) * (float) Math.PI * 0.4F * f1;
		newangle4 = Mth.cos(f2 * 1.9F * this.wingspeed) * (float) Math.PI * 0.4F * f1;
		newangle5 = Mth.cos(f2 * 1.8F * this.wingspeed) * (float) Math.PI * 0.4F * f1;
		newangle6 = Mth.cos(f2 * 1.7F * this.wingspeed) * (float) Math.PI * 0.4F * f1;
		newangle7 = Mth.cos(f2 * 1.6F * this.wingspeed) * (float) Math.PI * 0.4F * f1;
		newangle8 = Mth.cos(f2 * 1.5F * this.wingspeed) * (float) Math.PI * 0.4F * f1;
		} else {
		newangleA = Mth.cos(f2 * 0.25F * this.wingspeed) * (float) Math.PI * 0.01F;
		newangleB = Mth.cos(f2 * 0.39F * this.wingspeed) * (float) Math.PI * 0.01F;
		newangle1 = Mth.cos(f2 * 1.2F * this.wingspeed) * (float) Math.PI * 0.1F;
		newangle2 = Mth.cos(f2 * 1.1F * this.wingspeed) * (float) Math.PI * 0.1F;
		newangle3 = Mth.cos(f2 * 1.0F * this.wingspeed) * (float) Math.PI * 0.1F;
		newangle4 = Mth.cos(f2 * 1.9F * this.wingspeed) * (float) Math.PI * 0.1F;
		newangle5 = Mth.cos(f2 * 1.8F * this.wingspeed) * (float) Math.PI * 0.1F;
		newangle6 = Mth.cos(f2 * 1.7F * this.wingspeed) * (float) Math.PI * 0.1F;
		newangle7 = Mth.cos(f2 * 1.6F * this.wingspeed) * (float) Math.PI * 0.1F;
		newangle8 = Mth.cos(f2 * 1.5F * this.wingspeed) * (float) Math.PI * 0.1F;
		}
		this.tent1.xRot = newangle1 - 1.03F;
		this.tent7.zRot = newangle2 + 0.37F;
		this.tent5.xRot = newangle3 + 0.6F;
		this.tent6.xRot = newangle4 - 0.48F;
		this.tent4.xRot = newangle5 + 0.63F;
		this.tent2.zRot = newangle6 - 0.26F;
		this.tent3.xRot = newangle7 - 1.03F;
		this.tent8.xRot = newangle8 + 0.43F;
		this.body.xRot = newangleA;
		this.body.zRot = newangleB;
		newangleA = (float)Math.toRadians(f3) * 0.75F;
		this.body.yRot = newangleA;
	}
}
