package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelUrchin (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class UrchinModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart if1;
	private final ModelPart if2;
	private final ModelPart if3;
	private final ModelPart if4;
	private final ModelPart of1;
	private final ModelPart of2;
	private final ModelPart of3;
	private final ModelPart of4;
	private final ModelPart center;
	private final ModelPart tis1;
	private final ModelPart tis2;
	private final ModelPart tis3;
	private final ModelPart tis4;
	private final ModelPart tos1;
	private final ModelPart tos2;
	private final ModelPart tos3;
	private final ModelPart tos4;
	private final float wingspeed;

	public UrchinModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.if1 = root.getChild("if1");
		this.if2 = root.getChild("if2");
		this.if3 = root.getChild("if3");
		this.if4 = root.getChild("if4");
		this.of1 = root.getChild("of1");
		this.of2 = root.getChild("of2");
		this.of3 = root.getChild("of3");
		this.of4 = root.getChild("of4");
		this.center = root.getChild("center");
		this.tis1 = root.getChild("tis1");
		this.tis2 = root.getChild("tis2");
		this.tis3 = root.getChild("tis3");
		this.tis4 = root.getChild("tis4");
		this.tos1 = root.getChild("tos1");
		this.tos2 = root.getChild("tos2");
		this.tos3 = root.getChild("tos3");
		this.tos4 = root.getChild("tos4");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelUrchin");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle;
		float newangle1;
		float newangle2;
		float newangle3;
		float newangle4;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 0.7F * this.wingspeed) * (float) Math.PI * 0.15F * f1;
		newangle1 = Mth.cos(f2 * 1.7F * this.wingspeed) * (float) Math.PI * 0.15F * f1;
		newangle2 = Mth.cos(f2 * 1.65F * this.wingspeed) * (float) Math.PI * 0.15F * f1;
		newangle3 = Mth.cos(f2 * 1.75F * this.wingspeed) * (float) Math.PI * 0.15F * f1;
		newangle4 = Mth.cos(f2 * 1.8F * this.wingspeed) * (float) Math.PI * 0.15F * f1;
		} else {
		newangle = 0.0F;
		newangle1 = 0.0F;
		newangle2 = 0.0F;
		newangle3 = 0.0F;
		newangle4 = 0.0F;
		}
		this.if1.xRot = 0.261F + newangle1;
		this.if2.xRot = -0.261F - newangle2;
		this.if3.xRot = newangle3;
		this.if4.xRot = -newangle4;
		this.of1.zRot = -0.523F + newangle;
		this.of2.zRot = 0.523F - newangle;
		this.of3.xRot = -0.523F + newangle;
		this.of4.xRot = 0.523F - newangle;
		float newangle5;
		float newangle6;
		float newangle7;
		float newangle8;
		if (state.attacking) {
		newangle = (float)(f2 * 0.2F % (Math.PI * 2));
		newangle1 = Mth.cos(f2 * 0.7F * this.wingspeed) * (float) Math.PI * 0.06F;
		newangle2 = Mth.cos(f2 * 0.65F * this.wingspeed) * (float) Math.PI * 0.06F;
		newangle3 = Mth.cos(f2 * 0.75F * this.wingspeed) * (float) Math.PI * 0.06F;
		newangle4 = Mth.cos(f2 * 0.8F * this.wingspeed) * (float) Math.PI * 0.06F;
		newangle5 = Mth.cos(f2 * 0.55F * this.wingspeed) * (float) Math.PI * 0.06F;
		newangle6 = Mth.cos(f2 * 0.45F * this.wingspeed) * (float) Math.PI * 0.06F;
		newangle7 = Mth.cos(f2 * 0.35F * this.wingspeed) * (float) Math.PI * 0.06F;
		newangle8 = Mth.cos(f2 * 0.4F * this.wingspeed) * (float) Math.PI * 0.06F;
		} else {
		newangle = (float)(f2 * 0.02F % (Math.PI * 2));
		newangle1 = Mth.cos(f2 * 0.07F * this.wingspeed) * (float) Math.PI * 0.02F;
		newangle2 = Mth.cos(f2 * 0.065F * this.wingspeed) * (float) Math.PI * 0.02F;
		newangle3 = Mth.cos(f2 * 0.075F * this.wingspeed) * (float) Math.PI * 0.02F;
		newangle4 = Mth.cos(f2 * 0.08F * this.wingspeed) * (float) Math.PI * 0.02F;
		newangle5 = Mth.cos(f2 * 0.055F * this.wingspeed) * (float) Math.PI * 0.02F;
		newangle6 = Mth.cos(f2 * 0.045F * this.wingspeed) * (float) Math.PI * 0.02F;
		newangle7 = Mth.cos(f2 * 0.035F * this.wingspeed) * (float) Math.PI * 0.02F;
		newangle8 = Mth.cos(f2 * 0.04F * this.wingspeed) * (float) Math.PI * 0.02F;
		}
		this.center.yRot = newangle;
		this.tis1.xRot = 0.261F + newangle1;
		this.tis2.xRot = -0.261F + newangle2;
		this.tis3.xRot = newangle3;
		this.tis4.xRot = newangle4;
		this.tis1.zRot = newangle5;
		this.tis2.zRot = newangle6;
		this.tis3.zRot = 0.261F + newangle7;
		this.tis4.zRot = -0.261F + newangle8;
		this.tos1.xRot = -0.532F + newangle1;
		this.tos2.xRot = newangle7;
		this.tos3.xRot = newangle3;
		this.tos4.xRot = 0.532F + newangle5;
		this.tos1.zRot = newangle4;
		this.tos2.zRot = -0.523F + newangle6;
		this.tos3.zRot = 0.523F + newangle2;
		this.tos4.zRot = newangle8;
	}
}
