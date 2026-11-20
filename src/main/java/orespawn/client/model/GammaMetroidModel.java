package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelGammaMetroid (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class GammaMetroidModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Shell3;
	private final ModelPart Shell4;
	private final ModelPart Head;
	private final ModelPart BeakUpper;
	private final ModelPart BeakLower;
	private final ModelPart LeftTusk;
	private final ModelPart MiddleTusk;
	private final ModelPart RightTusk;
	private final ModelPart LeftFrontUpperLeg;
	private final ModelPart LeftFrontLowerLeg;
	private final ModelPart LeftRearUpperLeg;
	private final ModelPart LeftRearLowerLeg;
	private final ModelPart RightFrontUpperLeg;
	private final ModelPart RightFrontLowerLeg;
	private final ModelPart RightRearUpperLeg;
	private final ModelPart RightRearLowerLeg;
	private final ModelPart Core;
	private final ModelPart Bellyinside;
	private final ModelPart Bellyoutside;
	private final ModelPart Shell1;
	private final ModelPart Shell2;
	private final float wingspeed;

	public GammaMetroidModel(ModelPart root, float wingspeed) {
		// OreSpawn drew the whole creature with alpha blending.
		super(root, RenderTypes::entityTranslucent);
		this.wingspeed = wingspeed;
		this.Shell3 = root.getChild("shell3");
		this.Shell4 = root.getChild("shell4");
		this.Head = root.getChild("head");
		this.BeakUpper = root.getChild("beakupper");
		this.BeakLower = root.getChild("beaklower");
		this.LeftTusk = root.getChild("lefttusk");
		this.MiddleTusk = root.getChild("middletusk");
		this.RightTusk = root.getChild("righttusk");
		this.LeftFrontUpperLeg = root.getChild("leftfrontupperleg");
		this.LeftFrontLowerLeg = root.getChild("leftfrontlowerleg");
		this.LeftRearUpperLeg = root.getChild("leftrearupperleg");
		this.LeftRearLowerLeg = root.getChild("leftrearlowerleg");
		this.RightFrontUpperLeg = root.getChild("rightfrontupperleg");
		this.RightFrontLowerLeg = root.getChild("rightfrontlowerleg");
		this.RightRearUpperLeg = root.getChild("rightrearupperleg");
		this.RightRearLowerLeg = root.getChild("rightrearlowerleg");
		this.Core = root.getChild("core");
		this.Bellyinside = root.getChild("bellyinside");
		this.Bellyoutside = root.getChild("bellyoutside");
		this.Shell1 = root.getChild("shell1");
		this.Shell2 = root.getChild("shell2");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelGammaMetroid");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		newangle = Mth.cos(f2 * 0.81F * this.wingspeed) * (float) Math.PI * 0.08F;
		this.LeftTusk.xRot = newangle;
		newangle = Mth.cos(f2 * 0.87F * this.wingspeed) * (float) Math.PI * 0.08F;
		this.RightTusk.xRot = newangle;
		newangle = Mth.cos(f2 * 0.99F * this.wingspeed) * (float) Math.PI * 0.08F;
		this.MiddleTusk.xRot = newangle;
		newangle = Mth.cos(f2 * 1.11F * this.wingspeed) * (float) Math.PI * 0.08F;
		this.LeftTusk.yRot = newangle;
		newangle = Mth.cos(f2 * 1.17F * this.wingspeed) * (float) Math.PI * 0.08F;
		this.RightTusk.yRot = newangle;
		newangle = Mth.cos(f2 * 1.25F * this.wingspeed) * (float) Math.PI * 0.08F;
		this.MiddleTusk.yRot = newangle;
		float nextangle = 0.0F;
		float upangle = 0.0F;
		newangle = Mth.cos(f2 * 2.0F * this.wingspeed) * (float) Math.PI * 0.12F * f1;
		nextangle = Mth.cos((f2 + 0.1F) * 2.0F * this.wingspeed) * (float) Math.PI * 0.12F * f1;
		upangle = 0.0F;
		if (nextangle > newangle) {
		upangle = 0.47F * f1 - Math.abs(newangle);
		}
		this.doLeftFLeg(this.LeftFrontUpperLeg, this.LeftFrontLowerLeg, newangle, upangle);
		this.doRightFLeg(this.RightFrontUpperLeg, this.RightFrontLowerLeg, -newangle, upangle);
		this.doLeftRLeg(this.LeftRearUpperLeg, this.LeftRearLowerLeg, -newangle, upangle);
		this.doRightRLeg(this.RightRearUpperLeg, this.RightRearLowerLeg, newangle, upangle);
		newangle = Mth.cos(f2 * 0.4F * this.wingspeed) * (float) Math.PI * 0.05F;
		if (state.sitting) {
		newangle = 0.0F;
		}
		this.Shell1.xRot = newangle / 4.0F;
		this.Shell1.yRot = -(newangle / 4.0F);
		this.Shell2.xRot = newangle - 0.49F;
		this.Shell2.yRot = -newangle + 0.33F;
		this.Shell3.xRot = newangle - 0.96F;
		this.Shell3.yRot = -newangle + 0.63F;
		this.Shell4.xRot = newangle - 0.28F;
		newangle = Mth.cos(f2 * 0.75F * this.wingspeed) * (float) Math.PI * 0.1F;
		newangle = Math.abs(newangle);
		this.BeakLower.xRot = newangle + 0.14F;
		this.BeakLower.zRot = newangle + 0.14F;
	}

	private void doLeftFLeg(ModelPart seg2, ModelPart seg3, float angle, float upangle) {
		seg2.xRot = angle - 0.17F;
		seg3.xRot = angle - 0.26F;
		seg3.z = (float) (seg2.z + Math.sin(seg2.xRot) * 7.0) - 0.5F;
		seg2.zRot = -upangle - 0.66F;
		seg3.zRot = -upangle;
		seg3.y = seg2.y + (float) (5.0 * Math.cos(seg2.xRot));
		seg3.x = (float) (seg2.x + Math.abs(Math.sin(seg2.zRot) * 7.0) + 1.0);
	}

	private void doLeftRLeg(ModelPart seg2, ModelPart seg3, float angle, float upangle) {
		seg2.xRot = angle + 0.17F;
		seg3.xRot = angle + 0.31F;
		seg3.z = (float) (seg2.z + Math.sin(seg2.xRot) * 7.0) - 0.5F;
		seg2.zRot = -upangle - 0.82F;
		seg3.zRot = -upangle;
		seg3.y = seg2.y + (float) (5.0 * Math.cos(seg2.xRot));
		seg3.x = (float) (seg2.x + Math.abs(Math.sin(seg2.zRot) * 7.0) + 1.5);
	}

	private void doRightFLeg(ModelPart seg2, ModelPart seg3, float angle, float upangle) {
		seg2.xRot = angle - 0.17F;
		seg3.xRot = angle - 0.26F;
		seg3.z = (float) (seg2.z + Math.sin(seg2.xRot) * 7.0) - 0.5F;
		seg2.zRot = -upangle + 0.34F;
		seg3.zRot = -upangle;
		seg3.y = seg2.y + (float) (5.0 * Math.cos(seg2.xRot));
		seg3.x = (float) (seg2.x - Math.abs(Math.sin(seg2.zRot) * 7.0) - 1.0);
	}

	private void doRightRLeg(ModelPart seg2, ModelPart seg3, float angle, float upangle) {
		seg2.xRot = angle + 0.17F;
		seg3.xRot = angle + 0.31F;
		seg3.z = (float) (seg2.z + Math.sin(seg2.xRot) * 7.0) - 0.5F;
		seg2.zRot = -upangle + 0.82F;
		seg3.zRot = -upangle;
		seg3.y = seg2.y + (float) (5.0 * Math.cos(seg2.xRot));
		seg3.x = (float) (seg2.x - Math.abs(Math.sin(seg2.zRot) * 7.0) - 1.5);
	}
}
