package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelSeaViper (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class SeaViperModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart TailTip;
	private final ModelPart Neck;
	private final ModelPart tBase;
	private final ModelPart t2;
	private final ModelPart t3;
	private final ModelPart t4;
	private final ModelPart t5;
	private final ModelPart t6;
	private final ModelPart t7;
	private final ModelPart t8;
	private final ModelPart t9;
	private final ModelPart t10;
	private final ModelPart t12;
	private final ModelPart t11;
	private final ModelPart t13;
	private final ModelPart t14;
	private final ModelPart t15;
	private final ModelPart t16;
	private final ModelPart t17;
	private final ModelPart t18;
	private final ModelPart t19;
	private final ModelPart t20;
	private final ModelPart t21;
	private final ModelPart MouthBottom;
	private final ModelPart ToungBase;
	private final ModelPart MiddleTounge;
	private final ModelPart EyeRight;
	private final ModelPart EyeLeft;
	private final ModelPart MouthTop;
	private final ModelPart Head;
	private final ModelPart FangRight;
	private final ModelPart FangLeft;
	private final ModelPart ForkRight;
	private final ModelPart ForkLeft;
	private final float wingspeed;

	public SeaViperModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.TailTip = root.getChild("tailtip");
		this.Neck = root.getChild("neck");
		this.tBase = root.getChild("tbase");
		this.t2 = root.getChild("t2");
		this.t3 = root.getChild("t3");
		this.t4 = root.getChild("t4");
		this.t5 = root.getChild("t5");
		this.t6 = root.getChild("t6");
		this.t7 = root.getChild("t7");
		this.t8 = root.getChild("t8");
		this.t9 = root.getChild("t9");
		this.t10 = root.getChild("t10");
		this.t12 = root.getChild("t12");
		this.t11 = root.getChild("t11");
		this.t13 = root.getChild("t13");
		this.t14 = root.getChild("t14");
		this.t15 = root.getChild("t15");
		this.t16 = root.getChild("t16");
		this.t17 = root.getChild("t17");
		this.t18 = root.getChild("t18");
		this.t19 = root.getChild("t19");
		this.t20 = root.getChild("t20");
		this.t21 = root.getChild("t21");
		this.MouthBottom = root.getChild("mouthbottom");
		this.ToungBase = root.getChild("toungbase");
		this.MiddleTounge = root.getChild("middletounge");
		this.EyeRight = root.getChild("eyeright");
		this.EyeLeft = root.getChild("eyeleft");
		this.MouthTop = root.getChild("mouthtop");
		this.Head = root.getChild("head");
		this.FangRight = root.getChild("fangright");
		this.FangLeft = root.getChild("fangleft");
		this.ForkRight = root.getChild("forkright");
		this.ForkLeft = root.getChild("forkleft");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelSeaViper");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		if (f1 < 0.0F) {
		f1 = 0.0F;
		}
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.1F * f1;
		this.tBase.yRot = newangle;
		this.doseg(this.tBase, this.t2, 2.0F, f1, f2);
		this.doseg(this.t2, this.t3, 2.0F, f1, f2);
		this.doseg(this.t3, this.t4, 3.0F, f1, f2);
		this.doseg(this.t4, this.t5, 4.0F, f1, f2);
		this.doseg(this.t5, this.t6, 5.0F, f1, f2);
		this.doseg(this.t6, this.t7, 6.0F, f1, f2);
		this.doseg(this.t7, this.t8, 7.0F, f1, f2);
		this.doseg(this.t8, this.t9, 8.0F, f1, f2);
		this.doseg(this.t9, this.t10, 9.0F, f1, f2);
		this.doseg(this.t10, this.t11, 10.0F, f1, f2);
		this.doseg(this.t11, this.t12, 11.0F, f1, f2);
		this.doseg(this.t12, this.t13, 12.0F, f1, f2);
		this.doseg(this.t13, this.t14, 13.0F, f1, f2);
		this.doseg(this.t14, this.t15, 14.0F, f1, f2);
		this.doseg(this.t15, this.t16, 15.0F, f1, f2);
		this.doseg(this.t16, this.t17, 16.0F, f1, f2);
		this.doseg(this.t17, this.t18, 17.0F, f1, f2);
		this.doseg(this.t18, this.t19, 18.0F, f1, f2);
		this.doseg(this.t19, this.t20, 19.0F, f1, f2);
		this.doseg(this.t20, this.t21, 20.0F, f1, f2);
		this.doseg(this.t21, this.TailTip, 21.0F, f1, f2);
		if (state.attacking) {
		newangle = Mth.cos(f2 * 1.7F * this.wingspeed) * (float) Math.PI * 0.17F;
		this.MouthBottom.xRot = 0.65F + newangle;
		newangle = Mth.cos(f2 * 4.7F * this.wingspeed) * (float) Math.PI * 0.07F;
		this.ToungBase.xRot = 0.261F + newangle;
		this.MiddleTounge.xRot = 0.174F + newangle;
		this.ForkLeft.xRot = 0.087F + newangle;
		this.ForkRight.xRot = 0.087F + newangle;
		newangle = Mth.cos(f2 * 1.5F * this.wingspeed) * (float) Math.PI * 0.05F;
		// 1.7.10's offsetZ (in blocks) on the tongue: moves its pivot by newangle * 16 model units.
		this.ToungBase.z += newangle * 16.0F;
		this.MiddleTounge.z += newangle * 16.0F;
		this.ForkLeft.z += newangle * 16.0F;
		this.ForkRight.z += newangle * 16.0F;
		} else {
		newangle = Mth.cos(f2 * 0.2F * this.wingspeed) * (float) Math.PI * 0.02F;
		this.MouthBottom.xRot = 0.45F + newangle;
		newangle = Mth.cos(f2 * 1.7F * this.wingspeed) * (float) Math.PI * 0.03F;
		this.ToungBase.xRot = 0.261F + newangle;
		this.MiddleTounge.xRot = 0.174F + newangle;
		this.ForkLeft.xRot = 0.087F + newangle;
		this.ForkRight.xRot = 0.087F + newangle;
		newangle = Mth.cos(f2 * 0.5F * this.wingspeed) * (float) Math.PI * 0.05F;
		// 1.7.10's offsetZ (in blocks) on the tongue: moves its pivot by newangle * 16 model units.
		this.ToungBase.z += newangle * 16.0F;
		this.MiddleTounge.z += newangle * 16.0F;
		this.ForkLeft.z += newangle * 16.0F;
		this.ForkRight.z += newangle * 16.0F;
		}
		newangle = (float)Math.toRadians(f3) * 0.5F;
		this.Head.yRot = this.MouthTop.yRot = this.EyeLeft.yRot = this.EyeRight.yRot = newangle;
		this.FangLeft.yRot = this.FangRight.yRot = newangle;
		this.MouthBottom.yRot = newangle;
		this.MouthBottom.z = this.Head.z - (float)Math.cos(this.Head.yRot) * 2.0F;
		this.MouthBottom.x = this.Head.x - (float)Math.sin(this.Head.yRot) * 2.0F;
		this.ToungBase.yRot = newangle;
		this.MiddleTounge.yRot = newangle;
		this.ForkLeft.yRot = newangle - 0.436F;
		this.ForkRight.yRot = newangle + 0.436F;
	}

	/** Moves a body segment to the end of the one before it and swings it, a little later down the body each time. */
	private void doseg(ModelPart inn, ModelPart notinn, float f, float f1, float f2) {
		float pi4 = (float) (Math.PI / 4);
		notinn.z = (float) (inn.z + (float) Math.cos(inn.yRot) * (9.0 * Math.abs(Math.cos(inn.xRot))));
		notinn.x = (float) (inn.x + (float) Math.sin(inn.yRot) * 9.0F * Math.abs(Math.cos(inn.xRot)));
		float newangle = Mth.cos(f2 * 1.3F * this.wingspeed - pi4 * f) * (float) Math.PI * 0.2F * f1;
		float a = Mth.cos(-(pi4 * f));
		notinn.yRot = newangle + a - a * f1;
	}
}
