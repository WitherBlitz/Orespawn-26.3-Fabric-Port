package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelTrooperBug (the Jumpy Bug); state.attacking works its jaws. */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class TrooperBugModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart legintersection;
	private final ModelPart legintersectionb;
	private final ModelPart leg1start;
	private final ModelPart leg1shoulder;
	private final ModelPart leg1shoulderpart2;
	private final ModelPart leg1part1;
	private final ModelPart leg1part1b;
	private final ModelPart leg1elbow;
	private final ModelPart leg1part2;
	private final ModelPart leg1part2b;
	private final ModelPart leg1part2c;
	private final ModelPart leg1part3;
	private final ModelPart leg1part3b;
	private final ModelPart leg1part3c;
	private final ModelPart leg1part3d;
	private final ModelPart leg2start;
	private final ModelPart leg2shoulder;
	private final ModelPart leg2shoulderpart2;
	private final ModelPart leg2part1;
	private final ModelPart leg2part1b;
	private final ModelPart leg2elbow;
	private final ModelPart leg2part2;
	private final ModelPart leg2part2b;
	private final ModelPart leg2part2c;
	private final ModelPart leg2part3;
	private final ModelPart leg2part3b;
	private final ModelPart leg2part3c;
	private final ModelPart leg2part3d;
	private final ModelPart leg3start;
	private final ModelPart leg3shoulder;
	private final ModelPart leg3shoulderpart2;
	private final ModelPart leg3part1;
	private final ModelPart leg3part1b;
	private final ModelPart leg3elbow;
	private final ModelPart leg3part2;
	private final ModelPart leg3part2b;
	private final ModelPart leg3part2c;
	private final ModelPart leg3part3;
	private final ModelPart leg3part3b;
	private final ModelPart leg3part3c;
	private final ModelPart leg3part3d;
	private final ModelPart leg4start;
	private final ModelPart leg4shoulder;
	private final ModelPart leg4shoulderpart2;
	private final ModelPart leg4part1;
	private final ModelPart leg4part1b;
	private final ModelPart leg4elbow;
	private final ModelPart leg4part2;
	private final ModelPart leg4part2b;
	private final ModelPart leg4part2c;
	private final ModelPart leg4part3;
	private final ModelPart leg4part3b;
	private final ModelPart leg4part3c;
	private final ModelPart leg4part3d;
	private final ModelPart jawbase;
	private final ModelPart jawbase2;
	private final ModelPart jawbase3;
	private final ModelPart jawbase4;
	private final ModelPart jawbase5;
	private final ModelPart jawbase6;
	private final ModelPart jawbase7;
	private final ModelPart jawbase8;
	private final ModelPart jawbase9;
	private final ModelPart jawleft;
	private final ModelPart jawright;
	private final ModelPart jawend;
	private final ModelPart headstart;
	private final ModelPart headbase;
	private final ModelPart headbase2;
	private final ModelPart headbase3;
	private final ModelPart headbase4;
	private final ModelPart headbase5;
	private final ModelPart headbase6;
	private final ModelPart headbase7;
	private final ModelPart headbase8;
	private final ModelPart headbase9;
	private final ModelPart headbase10;
	private final ModelPart headleftridge;
	private final ModelPart headrightridge;
	private final ModelPart antenna1;
	private final ModelPart antenna1part2;
	private final ModelPart antenna2;
	private final ModelPart antenna2part2;
	private final ModelPart eyebase1;
	private final ModelPart eye1;
	private final ModelPart eyebase2;
	private final ModelPart eye2;
	private final ModelPart arm1start;
	private final ModelPart arm1part1;
	private final ModelPart arm1part2;
	private final ModelPart arm1part2b;
	private final ModelPart arm1part2c;
	private final ModelPart arm1part3;
	private final ModelPart arm1part3b;
	private final ModelPart arm2start;
	private final ModelPart arm2part1;
	private final ModelPart arm2part2;
	private final ModelPart arm2part2b;
	private final ModelPart arm2part2c;
	private final ModelPart arm2part3;
	private final ModelPart arm2part3b;
	private final ModelPart innermouth;
	private final ModelPart innermouth2;
	private final ModelPart tooth1;
	private final ModelPart tooth2;
	private final ModelPart tooth3;
	private final ModelPart tooth4;
	private final ModelPart tooth5;
	private final ModelPart tooth6;
	private final ModelPart mouthedge1;
	private final ModelPart mouthedge2;
	private final ModelPart mouthedge3;
	private final ModelPart mouthedge4;
	private final ModelPart arm3start;
	private final ModelPart arm3part1;
	private final ModelPart arm3part1b;
	private final ModelPart arm3part1c;
	private final ModelPart arm4start;
	private final ModelPart arm4part1;
	private final ModelPart arm4part1b;
	private final ModelPart arm4part1c;
	private final ModelPart tongue;
	private final ModelPart tonguepart2;
	private final ModelPart upperjawridgeleft;
	private final ModelPart upperjawridgeright;
	private final ModelPart upperjawbaseleft;
	private final ModelPart upperjawbaseright;
	private final ModelPart upperjawbase;
	private final ModelPart upperjawbase2;
	private final ModelPart upperjawbase3;
	private final ModelPart upperjawbase4;
	private final ModelPart upperjawend;
	private final ModelPart upperjawleftend;
	private final ModelPart upperjawrightend;
	private final float wingspeed;

	public TrooperBugModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.legintersection = root.getChild("legintersection");
		this.legintersectionb = root.getChild("legintersectionb");
		this.leg1start = root.getChild("leg1start");
		this.leg1shoulder = root.getChild("leg1shoulder");
		this.leg1shoulderpart2 = root.getChild("leg1shoulderpart2");
		this.leg1part1 = root.getChild("leg1part1");
		this.leg1part1b = root.getChild("leg1part1b");
		this.leg1elbow = root.getChild("leg1elbow");
		this.leg1part2 = root.getChild("leg1part2");
		this.leg1part2b = root.getChild("leg1part2b");
		this.leg1part2c = root.getChild("leg1part2c");
		this.leg1part3 = root.getChild("leg1part3");
		this.leg1part3b = root.getChild("leg1part3b");
		this.leg1part3c = root.getChild("leg1part3c");
		this.leg1part3d = root.getChild("leg1part3d");
		this.leg2start = root.getChild("leg2start");
		this.leg2shoulder = root.getChild("leg2shoulder");
		this.leg2shoulderpart2 = root.getChild("leg2shoulderpart2");
		this.leg2part1 = root.getChild("leg2part1");
		this.leg2part1b = root.getChild("leg2part1b");
		this.leg2elbow = root.getChild("leg2elbow");
		this.leg2part2 = root.getChild("leg2part2");
		this.leg2part2b = root.getChild("leg2part2b");
		this.leg2part2c = root.getChild("leg2part2c");
		this.leg2part3 = root.getChild("leg2part3");
		this.leg2part3b = root.getChild("leg2part3b");
		this.leg2part3c = root.getChild("leg2part3c");
		this.leg2part3d = root.getChild("leg2part3d");
		this.leg3start = root.getChild("leg3start");
		this.leg3shoulder = root.getChild("leg3shoulder");
		this.leg3shoulderpart2 = root.getChild("leg3shoulderpart2");
		this.leg3part1 = root.getChild("leg3part1");
		this.leg3part1b = root.getChild("leg3part1b");
		this.leg3elbow = root.getChild("leg3elbow");
		this.leg3part2 = root.getChild("leg3part2");
		this.leg3part2b = root.getChild("leg3part2b");
		this.leg3part2c = root.getChild("leg3part2c");
		this.leg3part3 = root.getChild("leg3part3");
		this.leg3part3b = root.getChild("leg3part3b");
		this.leg3part3c = root.getChild("leg3part3c");
		this.leg3part3d = root.getChild("leg3part3d");
		this.leg4start = root.getChild("leg4start");
		this.leg4shoulder = root.getChild("leg4shoulder");
		this.leg4shoulderpart2 = root.getChild("leg4shoulderpart2");
		this.leg4part1 = root.getChild("leg4part1");
		this.leg4part1b = root.getChild("leg4part1b");
		this.leg4elbow = root.getChild("leg4elbow");
		this.leg4part2 = root.getChild("leg4part2");
		this.leg4part2b = root.getChild("leg4part2b");
		this.leg4part2c = root.getChild("leg4part2c");
		this.leg4part3 = root.getChild("leg4part3");
		this.leg4part3b = root.getChild("leg4part3b");
		this.leg4part3c = root.getChild("leg4part3c");
		this.leg4part3d = root.getChild("leg4part3d");
		this.jawbase = root.getChild("jawbase");
		this.jawbase2 = root.getChild("jawbase2");
		this.jawbase3 = root.getChild("jawbase3");
		this.jawbase4 = root.getChild("jawbase4");
		this.jawbase5 = root.getChild("jawbase5");
		this.jawbase6 = root.getChild("jawbase6");
		this.jawbase7 = root.getChild("jawbase7");
		this.jawbase8 = root.getChild("jawbase8");
		this.jawbase9 = root.getChild("jawbase9");
		this.jawleft = root.getChild("jawleft");
		this.jawright = root.getChild("jawright");
		this.jawend = root.getChild("jawend");
		this.headstart = root.getChild("headstart");
		this.headbase = root.getChild("headbase");
		this.headbase2 = root.getChild("headbase2");
		this.headbase3 = root.getChild("headbase3");
		this.headbase4 = root.getChild("headbase4");
		this.headbase5 = root.getChild("headbase5");
		this.headbase6 = root.getChild("headbase6");
		this.headbase7 = root.getChild("headbase7");
		this.headbase8 = root.getChild("headbase8");
		this.headbase9 = root.getChild("headbase9");
		this.headbase10 = root.getChild("headbase10");
		this.headleftridge = root.getChild("headleftridge");
		this.headrightridge = root.getChild("headrightridge");
		this.antenna1 = root.getChild("antenna1");
		this.antenna1part2 = root.getChild("antenna1part2");
		this.antenna2 = root.getChild("antenna2");
		this.antenna2part2 = root.getChild("antenna2part2");
		this.eyebase1 = root.getChild("eyebase1");
		this.eye1 = root.getChild("eye1");
		this.eyebase2 = root.getChild("eyebase2");
		this.eye2 = root.getChild("eye2");
		this.arm1start = root.getChild("arm1start");
		this.arm1part1 = root.getChild("arm1part1");
		this.arm1part2 = root.getChild("arm1part2");
		this.arm1part2b = root.getChild("arm1part2b");
		this.arm1part2c = root.getChild("arm1part2c");
		this.arm1part3 = root.getChild("arm1part3");
		this.arm1part3b = root.getChild("arm1part3b");
		this.arm2start = root.getChild("arm2start");
		this.arm2part1 = root.getChild("arm2part1");
		this.arm2part2 = root.getChild("arm2part2");
		this.arm2part2b = root.getChild("arm2part2b");
		this.arm2part2c = root.getChild("arm2part2c");
		this.arm2part3 = root.getChild("arm2part3");
		this.arm2part3b = root.getChild("arm2part3b");
		this.innermouth = root.getChild("innermouth");
		this.innermouth2 = root.getChild("innermouth2");
		this.tooth1 = root.getChild("tooth1");
		this.tooth2 = root.getChild("tooth2");
		this.tooth3 = root.getChild("tooth3");
		this.tooth4 = root.getChild("tooth4");
		this.tooth5 = root.getChild("tooth5");
		this.tooth6 = root.getChild("tooth6");
		this.mouthedge1 = root.getChild("mouthedge1");
		this.mouthedge2 = root.getChild("mouthedge2");
		this.mouthedge3 = root.getChild("mouthedge3");
		this.mouthedge4 = root.getChild("mouthedge4");
		this.arm3start = root.getChild("arm3start");
		this.arm3part1 = root.getChild("arm3part1");
		this.arm3part1b = root.getChild("arm3part1b");
		this.arm3part1c = root.getChild("arm3part1c");
		this.arm4start = root.getChild("arm4start");
		this.arm4part1 = root.getChild("arm4part1");
		this.arm4part1b = root.getChild("arm4part1b");
		this.arm4part1c = root.getChild("arm4part1c");
		this.tongue = root.getChild("tongue");
		this.tonguepart2 = root.getChild("tonguepart2");
		this.upperjawridgeleft = root.getChild("upperjawridgeleft");
		this.upperjawridgeright = root.getChild("upperjawridgeright");
		this.upperjawbaseleft = root.getChild("upperjawbaseleft");
		this.upperjawbaseright = root.getChild("upperjawbaseright");
		this.upperjawbase = root.getChild("upperjawbase");
		this.upperjawbase2 = root.getChild("upperjawbase2");
		this.upperjawbase3 = root.getChild("upperjawbase3");
		this.upperjawbase4 = root.getChild("upperjawbase4");
		this.upperjawend = root.getChild("upperjawend");
		this.upperjawleftend = root.getChild("upperjawleftend");
		this.upperjawrightend = root.getChild("upperjawrightend");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelTrooperBug");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		float upangle = 0.0F;
		float nextangle = 0.0F;
		float pi4 = 1.570795F;
		if (!state.attacking) {
		newangle = Mth.cos(f2 * 0.4F * this.wingspeed) * (float) Math.PI * 0.05F;
		} else {
		newangle = Mth.cos(f2 * 1.4F * this.wingspeed) * (float) Math.PI * 0.1F;
		}
		this.antenna2part2.yRot = 0.78F + newangle;
		this.antenna1part2.yRot = -0.78F - newangle;
		if (!state.attacking) {
		newangle = Mth.cos(f2 * 0.5F * this.wingspeed) * (float) Math.PI * 0.05F;
		} else {
		newangle = Mth.cos(f2 * 2.5F * this.wingspeed) * (float) Math.PI * 0.15F;
		}
		this.arm4part1.yRot = newangle;
		this.arm4part1b.yRot = newangle;
		this.arm4part1c.yRot = newangle;
		this.arm3part1.yRot = -newangle;
		this.arm3part1b.yRot = -newangle;
		this.arm3part1c.yRot = -newangle;
		if (!state.attacking) {
		newangle = Mth.cos(f2 * 0.3F * this.wingspeed) * (float) Math.PI * 0.05F;
		} else {
		newangle = Mth.cos(f2 * 2.6F * this.wingspeed) * (float) Math.PI * 0.2F;
		}
		this.arm1part3.xRot = 1.56F + newangle;
		this.arm1part3b.xRot = 1.56F + newangle;
		this.arm2part3.xRot = 1.56F - newangle;
		this.arm2part3b.xRot = 1.56F - newangle;
		if (!state.attacking) {
		newangle = Mth.cos(f2 * 0.1F * this.wingspeed) * (float) Math.PI * 0.02F;
		} else {
		newangle = Mth.cos(f2 * 1.0F * this.wingspeed) * (float) Math.PI * 0.1F;
		}
		this.headleftridge.yRot = -0.25F + newangle;
		this.headrightridge.yRot = 0.25F - newangle;
		this.upperjawridgeleft.yRot = -0.372F + newangle;
		this.upperjawridgeright.yRot = 0.372F - newangle;
		if (!state.attacking) {
		newangle = Mth.cos(f2 * 0.3F * this.wingspeed) * (float) Math.PI * 0.015F;
		} else {
		newangle = Mth.cos(f2 * 2.6F * this.wingspeed) * (float) Math.PI * 0.1F;
		}
		this.jawbase.xRot = 0.22F + newangle;
		this.jawbase2.xRot = 0.22F + newangle;
		this.jawbase3.xRot = 0.22F + newangle;
		this.jawbase4.xRot = 0.22F + newangle;
		this.jawbase5.xRot = 0.22F + newangle;
		this.jawbase6.xRot = 0.22F + newangle;
		this.jawbase7.xRot = 0.22F + newangle;
		this.jawbase8.xRot = 0.2146F + newangle;
		this.jawbase8.yRot = 0.1487F + newangle;
		this.jawbase9.xRot = 0.1F + newangle;
		this.jawbase9.yRot = 0.07F + newangle;
		this.jawend.xRot = 0.22F + newangle;
		this.jawleft.xRot = 0.22F + newangle;
		this.jawright.xRot = 0.22F + newangle;
		newangle = Mth.sin(f2 * 2.0F * this.wingspeed) * (float) Math.PI * 0.12F * f1;
		nextangle = Mth.sin((f2 + 0.1F) * 2.0F * this.wingspeed) * (float) Math.PI * 0.12F * f1;
		upangle = 0.0F;
		if (nextangle > newangle) {
		upangle = Math.abs(Mth.cos(f2 * 2.0F * this.wingspeed) * (float) Math.PI * 0.12F * f1);
		}
		this.doLeftFrontLeg(newangle, upangle);
		this.doLeftRearLeg(-newangle, upangle);
		newangle = Mth.sin((float)(f2 * 2.0F * this.wingspeed + Math.PI)) * (float) Math.PI * 0.12F * f1;
		nextangle = Mth.sin((float)((f2 + 0.1F) * 2.0F * this.wingspeed + Math.PI)) * (float) Math.PI * 0.12F * f1;
		upangle = 0.0F;
		if (nextangle > newangle) {
		upangle = Math.abs(Mth.cos((float)(f2 * 2.0F * this.wingspeed + Math.PI)) * (float) Math.PI * 0.12F * f1);
		}
		this.doRightFrontLeg(-newangle, upangle);
		this.doRightRearLeg(newangle, upangle);
	}

	private void doRightFrontLeg(float angle, float upangle) {
		this.leg1part1.yRot = 1.2F + angle;
		this.leg1part1b.yRot = this.leg1part1.yRot;
		this.leg1elbow.yRot = this.leg1part1.yRot;
		this.leg1part2.yRot = this.leg1part1.yRot;
		this.leg1part2b.yRot = this.leg1part1.yRot;
		this.leg1part2c.yRot = this.leg1part1.yRot;
		this.leg1part3.yRot = this.leg1part1.yRot;
		this.leg1part3b.yRot = this.leg1part1.yRot;
		this.leg1part3c.yRot = this.leg1part1.yRot;
		this.leg1part3d.yRot = this.leg1part1.yRot;
		this.leg1part1.xRot = 1.115F + upangle;
		this.leg1part1b.xRot = 1.078F + upangle;
		this.leg1elbow.xRot = this.leg1part1.xRot;
		float dist = 26.0F;
		dist = (float)(dist * Math.cos(this.leg1part1.xRot));
		this.leg1part2.z = this.leg1part2b.z = this.leg1part2c.z = (float)(this.leg1part1.z - Math.cos(this.leg1part1.yRot) * dist);
		this.leg1part2.x = this.leg1part2b.x = this.leg1part2c.x = (float)(this.leg1part1.x - Math.sin(this.leg1part1.yRot) * dist);
		this.leg1part2.xRot = 1.871F - upangle;
		this.leg1part2b.xRot = 1.817F - upangle;
		this.leg1part2c.xRot = 1.762F - upangle;
		dist = 32.0F;
		dist = (float)Math.abs(dist * Math.cos(this.leg1part2.xRot));
		this.leg1part3.z = this.leg1part3b.z = this.leg1part3c.z = this.leg1part3d.z = (float)(this.leg1part2.z - Math.cos(this.leg1part2.yRot) * dist);
		this.leg1part3.x = this.leg1part3b.x = this.leg1part3c.x = this.leg1part3d.x = (float)(this.leg1part2.x - Math.sin(this.leg1part2.yRot) * dist);
		this.leg1part3.xRot = 1.08F + upangle;
		this.leg1part3b.xRot = 1.08F + upangle;
		this.leg1part3c.xRot = 1.08F + upangle;
		this.leg1part3d.xRot = 1.08F + upangle;
	}

	private void doLeftFrontLeg(float angle, float upangle) {
		this.leg2part1.yRot = -1.2F + angle;
		this.leg2part1b.yRot = this.leg2part1.yRot;
		this.leg2elbow.yRot = this.leg2part1.yRot;
		this.leg2part2.yRot = this.leg2part1.yRot;
		this.leg2part2b.yRot = this.leg2part1.yRot;
		this.leg2part2c.yRot = this.leg2part1.yRot;
		this.leg2part3.yRot = this.leg2part1.yRot;
		this.leg2part3b.yRot = this.leg2part1.yRot;
		this.leg2part3c.yRot = this.leg2part1.yRot;
		this.leg2part3d.yRot = this.leg2part1.yRot;
		this.leg2part1.xRot = 1.115F + upangle;
		this.leg2part1b.xRot = 1.078F + upangle;
		this.leg2elbow.xRot = this.leg2part1.xRot;
		float dist = 26.0F;
		dist = (float)(dist * Math.cos(this.leg2part1.xRot));
		this.leg2part2.z = this.leg2part2b.z = this.leg2part2c.z = (float)(this.leg2part1.z - Math.cos(this.leg2part1.yRot) * dist);
		this.leg2part2.x = this.leg2part2b.x = this.leg2part2c.x = (float)(this.leg2part1.x - Math.sin(this.leg2part1.yRot) * dist);
		this.leg2part2.xRot = 1.871F - upangle;
		this.leg2part2b.xRot = 1.817F - upangle;
		this.leg2part2c.xRot = 1.762F - upangle;
		dist = 32.0F;
		dist = (float)Math.abs(dist * Math.cos(this.leg2part2.xRot));
		this.leg2part3.z = this.leg2part3b.z = this.leg2part3c.z = this.leg2part3d.z = (float)(this.leg2part2.z - Math.cos(this.leg2part2.yRot) * dist);
		this.leg2part3.x = this.leg2part3b.x = this.leg2part3c.x = this.leg2part3d.x = (float)(this.leg2part2.x - Math.sin(this.leg2part2.yRot) * dist);
		this.leg2part3.xRot = 1.08F + upangle;
		this.leg2part3b.xRot = 1.08F + upangle;
		this.leg2part3c.xRot = 1.08F + upangle;
		this.leg2part3d.xRot = 1.08F + upangle;
	}

	private void doRightRearLeg(float angle, float upangle) {
		this.leg4part1.yRot = -1.2F + angle;
		this.leg4part1b.yRot = this.leg4part1.yRot;
		this.leg4elbow.yRot = this.leg4part1.yRot;
		this.leg4part2.yRot = this.leg4part1.yRot;
		this.leg4part2b.yRot = this.leg4part1.yRot;
		this.leg4part2c.yRot = this.leg4part1.yRot;
		this.leg4part3.yRot = this.leg4part1.yRot;
		this.leg4part3b.yRot = this.leg4part1.yRot;
		this.leg4part3c.yRot = this.leg4part1.yRot;
		this.leg4part3d.yRot = this.leg4part1.yRot;
		this.leg4part1.xRot = -1.115F + upangle;
		this.leg4part1b.xRot = -1.078F + upangle;
		this.leg4elbow.xRot = this.leg4part1.xRot;
		float dist = 26.0F;
		dist = (float)(dist * Math.cos(this.leg4part1.xRot));
		this.leg4part2.z = this.leg4part2b.z = this.leg4part2c.z = (float)(this.leg4part1.z + Math.cos(this.leg4part1.yRot) * dist);
		this.leg4part2.x = this.leg4part2b.x = this.leg4part2c.x = (float)(this.leg4part1.x + Math.sin(this.leg4part1.yRot) * dist);
		this.leg4part2.xRot = -1.871F - upangle;
		this.leg4part2b.xRot = -1.817F - upangle;
		this.leg4part2c.xRot = -1.762F - upangle;
		dist = 32.0F;
		dist = (float)Math.abs(dist * Math.cos(this.leg4part2.xRot));
		this.leg4part3.z = this.leg4part3b.z = this.leg4part3c.z = this.leg4part3d.z = (float)(this.leg4part2.z + Math.cos(this.leg4part2.yRot) * dist);
		this.leg4part3.x = this.leg4part3b.x = this.leg4part3c.x = this.leg4part3d.x = (float)(this.leg4part2.x + Math.sin(this.leg4part2.yRot) * dist);
		this.leg4part3.xRot = -1.08F + upangle;
		this.leg4part3b.xRot = -1.08F + upangle;
		this.leg4part3c.xRot = -1.08F + upangle;
		this.leg4part3d.xRot = -1.08F + upangle;
	}

	private void doLeftRearLeg(float angle, float upangle) {
		this.leg3part1.yRot = 1.2F + angle;
		this.leg3part1b.yRot = this.leg3part1.yRot;
		this.leg3elbow.yRot = this.leg3part1.yRot;
		this.leg3part2.yRot = this.leg3part1.yRot;
		this.leg3part2b.yRot = this.leg3part1.yRot;
		this.leg3part2c.yRot = this.leg3part1.yRot;
		this.leg3part3.yRot = this.leg3part1.yRot;
		this.leg3part3b.yRot = this.leg3part1.yRot;
		this.leg3part3c.yRot = this.leg3part1.yRot;
		this.leg3part3d.yRot = this.leg3part1.yRot;
		this.leg3part1.xRot = -1.115F + upangle;
		this.leg3part1b.xRot = -1.078F + upangle;
		this.leg3elbow.xRot = this.leg3part1.xRot;
		float dist = 26.0F;
		dist = (float)(dist * Math.cos(this.leg3part1.xRot));
		this.leg3part2.z = this.leg3part2b.z = this.leg3part2c.z = (float)(this.leg3part1.z + Math.cos(this.leg3part1.yRot) * dist);
		this.leg3part2.x = this.leg3part2b.x = this.leg3part2c.x = (float)(this.leg3part1.x + Math.sin(this.leg3part1.yRot) * dist);
		this.leg3part2.xRot = -1.871F - upangle;
		this.leg3part2b.xRot = -1.817F - upangle;
		this.leg3part2c.xRot = -1.762F - upangle;
		dist = 32.0F;
		dist = (float)Math.abs(dist * Math.cos(this.leg3part2.xRot));
		this.leg3part3.z = this.leg3part3b.z = this.leg3part3c.z = this.leg3part3d.z = (float)(this.leg3part2.z + Math.cos(this.leg3part2.yRot) * dist);
		this.leg3part3.x = this.leg3part3b.x = this.leg3part3c.x = this.leg3part3d.x = (float)(this.leg3part2.x + Math.sin(this.leg3part2.yRot) * dist);
		this.leg3part3.xRot = -1.08F + upangle;
		this.leg3part3b.xRot = -1.08F + upangle;
		this.leg3part3c.xRot = -1.08F + upangle;
		this.leg3part3d.xRot = -1.08F + upangle;
	}
}
