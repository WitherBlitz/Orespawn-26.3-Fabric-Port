package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelSpitBug; state.attacking works its jaws. */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class SpitBugModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart legintersection;
	private final ModelPart legintersectionpart2;
	private final ModelPart legintersectionpart3;
	private final ModelPart leg1start;
	private final ModelPart leg1startpart2;
	private final ModelPart leg1startpart3;
	private final ModelPart leg1;
	private final ModelPart leg1part2;
	private final ModelPart leg1part2b;
	private final ModelPart leg1part2c;
	private final ModelPart leg1part2d;
	private final ModelPart leg1part3;
	private final ModelPart leg1part3b;
	private final ModelPart leg1part3c;
	private final ModelPart leg2start;
	private final ModelPart leg2startpart2;
	private final ModelPart leg2startpart3;
	private final ModelPart leg2;
	private final ModelPart leg2part2;
	private final ModelPart leg2part2b;
	private final ModelPart leg2part2c;
	private final ModelPart leg2part2d;
	private final ModelPart leg2part3;
	private final ModelPart leg2part3b;
	private final ModelPart leg2part3c;
	private final ModelPart leg3start;
	private final ModelPart leg3startpart2;
	private final ModelPart leg3startpart3;
	private final ModelPart leg3;
	private final ModelPart leg3part2;
	private final ModelPart leg3part2b;
	private final ModelPart leg3part2c;
	private final ModelPart leg3part2d;
	private final ModelPart leg3part3;
	private final ModelPart leg3part3b;
	private final ModelPart leg3part3c;
	private final ModelPart leg4start;
	private final ModelPart leg4startpart2;
	private final ModelPart leg4startpart3;
	private final ModelPart leg4;
	private final ModelPart leg4part2;
	private final ModelPart leg4part2b;
	private final ModelPart leg4part2c;
	private final ModelPart leg4part2d;
	private final ModelPart leg4part3;
	private final ModelPart leg4part3b;
	private final ModelPart leg4part3c;
	private final ModelPart bodybase;
	private final ModelPart bodybasepart2;
	private final ModelPart bodybasepart3;
	private final ModelPart bodybasepart4;
	private final ModelPart bodybasepart5;
	private final ModelPart bodybasepart6;
	private final ModelPart bodybasepart7;
	private final ModelPart bodybasepart8;
	private final ModelPart bodybasepart9;
	private final ModelPart bodybasepart10;
	private final ModelPart bodybasepart11;
	private final ModelPart bodybasepart12;
	private final ModelPart bodybasepart13;
	private final ModelPart bodybasepart14;
	private final ModelPart bodybasepart15;
	private final ModelPart upperjawbase;
	private final ModelPart upperjawbasepart1;
	private final ModelPart upperjawbasepart2;
	private final ModelPart upperjawbasepart3;
	private final ModelPart tooth1;
	private final ModelPart tooth2;
	private final ModelPart tooth3;
	private final ModelPart tooth4;
	private final ModelPart tooth5;
	private final ModelPart lowerjawbase;
	private final ModelPart lowerjawbasepart1;
	private final ModelPart lowerjawbasepart2;
	private final ModelPart lowerjawbasepart3;
	private final ModelPart lowerjawbasepart4;
	private final ModelPart lowerjawbasepart5;
	private final ModelPart lowerjawbasepart6;
	private final ModelPart lowerjawbasepart7;
	private final ModelPart lowerjawbasepart8;
	private final ModelPart lowerjawbasepart9;
	private final ModelPart lowerjawbasepart10;
	private final ModelPart lowerjawbasepart11;
	private final ModelPart arm1start;
	private final ModelPart arm1;
	private final ModelPart arm1part2;
	private final ModelPart arm1end;
	private final ModelPart arm2start;
	private final ModelPart arm2;
	private final ModelPart arm2part2;
	private final ModelPart arm2end;
	private final ModelPart eye1;
	private final ModelPart eye2;
	private final float wingspeed;

	public SpitBugModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.legintersection = root.getChild("legintersection");
		this.legintersectionpart2 = root.getChild("legintersectionpart2");
		this.legintersectionpart3 = root.getChild("legintersectionpart3");
		this.leg1start = root.getChild("leg1start");
		this.leg1startpart2 = root.getChild("leg1startpart2");
		this.leg1startpart3 = root.getChild("leg1startpart3");
		this.leg1 = root.getChild("leg1");
		this.leg1part2 = root.getChild("leg1part2");
		this.leg1part2b = root.getChild("leg1part2b");
		this.leg1part2c = root.getChild("leg1part2c");
		this.leg1part2d = root.getChild("leg1part2d");
		this.leg1part3 = root.getChild("leg1part3");
		this.leg1part3b = root.getChild("leg1part3b");
		this.leg1part3c = root.getChild("leg1part3c");
		this.leg2start = root.getChild("leg2start");
		this.leg2startpart2 = root.getChild("leg2startpart2");
		this.leg2startpart3 = root.getChild("leg2startpart3");
		this.leg2 = root.getChild("leg2");
		this.leg2part2 = root.getChild("leg2part2");
		this.leg2part2b = root.getChild("leg2part2b");
		this.leg2part2c = root.getChild("leg2part2c");
		this.leg2part2d = root.getChild("leg2part2d");
		this.leg2part3 = root.getChild("leg2part3");
		this.leg2part3b = root.getChild("leg2part3b");
		this.leg2part3c = root.getChild("leg2part3c");
		this.leg3start = root.getChild("leg3start");
		this.leg3startpart2 = root.getChild("leg3startpart2");
		this.leg3startpart3 = root.getChild("leg3startpart3");
		this.leg3 = root.getChild("leg3");
		this.leg3part2 = root.getChild("leg3part2");
		this.leg3part2b = root.getChild("leg3part2b");
		this.leg3part2c = root.getChild("leg3part2c");
		this.leg3part2d = root.getChild("leg3part2d");
		this.leg3part3 = root.getChild("leg3part3");
		this.leg3part3b = root.getChild("leg3part3b");
		this.leg3part3c = root.getChild("leg3part3c");
		this.leg4start = root.getChild("leg4start");
		this.leg4startpart2 = root.getChild("leg4startpart2");
		this.leg4startpart3 = root.getChild("leg4startpart3");
		this.leg4 = root.getChild("leg4");
		this.leg4part2 = root.getChild("leg4part2");
		this.leg4part2b = root.getChild("leg4part2b");
		this.leg4part2c = root.getChild("leg4part2c");
		this.leg4part2d = root.getChild("leg4part2d");
		this.leg4part3 = root.getChild("leg4part3");
		this.leg4part3b = root.getChild("leg4part3b");
		this.leg4part3c = root.getChild("leg4part3c");
		this.bodybase = root.getChild("bodybase");
		this.bodybasepart2 = root.getChild("bodybasepart2");
		this.bodybasepart3 = root.getChild("bodybasepart3");
		this.bodybasepart4 = root.getChild("bodybasepart4");
		this.bodybasepart5 = root.getChild("bodybasepart5");
		this.bodybasepart6 = root.getChild("bodybasepart6");
		this.bodybasepart7 = root.getChild("bodybasepart7");
		this.bodybasepart8 = root.getChild("bodybasepart8");
		this.bodybasepart9 = root.getChild("bodybasepart9");
		this.bodybasepart10 = root.getChild("bodybasepart10");
		this.bodybasepart11 = root.getChild("bodybasepart11");
		this.bodybasepart12 = root.getChild("bodybasepart12");
		this.bodybasepart13 = root.getChild("bodybasepart13");
		this.bodybasepart14 = root.getChild("bodybasepart14");
		this.bodybasepart15 = root.getChild("bodybasepart15");
		this.upperjawbase = root.getChild("upperjawbase");
		this.upperjawbasepart1 = root.getChild("upperjawbasepart1");
		this.upperjawbasepart2 = root.getChild("upperjawbasepart2");
		this.upperjawbasepart3 = root.getChild("upperjawbasepart3");
		this.tooth1 = root.getChild("tooth1");
		this.tooth2 = root.getChild("tooth2");
		this.tooth3 = root.getChild("tooth3");
		this.tooth4 = root.getChild("tooth4");
		this.tooth5 = root.getChild("tooth5");
		this.lowerjawbase = root.getChild("lowerjawbase");
		this.lowerjawbasepart1 = root.getChild("lowerjawbasepart1");
		this.lowerjawbasepart2 = root.getChild("lowerjawbasepart2");
		this.lowerjawbasepart3 = root.getChild("lowerjawbasepart3");
		this.lowerjawbasepart4 = root.getChild("lowerjawbasepart4");
		this.lowerjawbasepart5 = root.getChild("lowerjawbasepart5");
		this.lowerjawbasepart6 = root.getChild("lowerjawbasepart6");
		this.lowerjawbasepart7 = root.getChild("lowerjawbasepart7");
		this.lowerjawbasepart8 = root.getChild("lowerjawbasepart8");
		this.lowerjawbasepart9 = root.getChild("lowerjawbasepart9");
		this.lowerjawbasepart10 = root.getChild("lowerjawbasepart10");
		this.lowerjawbasepart11 = root.getChild("lowerjawbasepart11");
		this.arm1start = root.getChild("arm1start");
		this.arm1 = root.getChild("arm1");
		this.arm1part2 = root.getChild("arm1part2");
		this.arm1end = root.getChild("arm1end");
		this.arm2start = root.getChild("arm2start");
		this.arm2 = root.getChild("arm2");
		this.arm2part2 = root.getChild("arm2part2");
		this.arm2end = root.getChild("arm2end");
		this.eye1 = root.getChild("eye1");
		this.eye2 = root.getChild("eye2");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelSpitBug");
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
		if (!state.attacking) {
		newangle = Mth.cos(f2 * 0.3F * this.wingspeed) * (float) Math.PI * 0.015F;
		} else {
		newangle = Mth.cos(f2 * 2.6F * this.wingspeed) * (float) Math.PI * 0.1F;
		}
		newangle = Math.abs(newangle);
		this.upperjawbasepart1.xRot = newangle;
		this.upperjawbasepart2.xRot = newangle;
		this.upperjawbasepart3.xRot = newangle;
		this.tooth1.xRot = 0.26F + newangle;
		this.tooth2.xRot = 0.26F + newangle;
		this.tooth3.xRot = 0.26F + newangle;
	}

	private void doRightFrontLeg(float angle, float upangle) {
		this.leg1.yRot = -1.2F + angle;
		this.leg1part2.yRot = this.leg1.yRot;
		this.leg1part2b.yRot = this.leg1.yRot;
		this.leg1part2c.yRot = this.leg1.yRot;
		this.leg1part2d.yRot = this.leg1.yRot;
		this.leg1part3.yRot = this.leg1.yRot;
		this.leg1part3b.yRot = this.leg1.yRot;
		this.leg1part3c.yRot = this.leg1.yRot;
		float dist = 14.0F;
		dist = (float)(dist * Math.cos(this.leg1.xRot));
		this.leg1part2.z = this.leg1part2b.z = this.leg1part2c.z = this.leg1part2d.z = (float)(this.leg1.z - Math.cos(this.leg1.yRot) * dist);
		this.leg1part2.x = this.leg1part2b.x = this.leg1part2c.x = this.leg1part2d.x = (float)(this.leg1.x - Math.sin(this.leg1.yRot) * dist);
		this.leg1part2.xRot = -1.152F + upangle;
		this.leg1part2b.xRot = -0.743F + upangle;
		this.leg1part2c.xRot = -0.632F + upangle;
		this.leg1part2d.xRot = -1.041F + upangle;
		dist = 14.0F;
		dist = (float)(dist * Math.cos(this.leg1part2.xRot));
		this.leg1part3.z = (float)(this.leg1part2.z - Math.cos(this.leg1part2.yRot) * dist);
		this.leg1part3.x = (float)(this.leg1part2.x - Math.sin(this.leg1part2.yRot) * dist);
		this.leg1part3.xRot = 0.669F - upangle;
		dist = 8.0F;
		dist = (float)Math.abs(dist * Math.cos(this.leg1part3.xRot));
		this.leg1part3b.z = this.leg1part3c.z = (float)(this.leg1part3.z - Math.cos(this.leg1part3.yRot) * dist);
		this.leg1part3b.x = this.leg1part3c.x = (float)(this.leg1part3.x - Math.sin(this.leg1part3.yRot) * dist);
		this.leg1part3b.xRot = -0.48F - upangle;
		this.leg1part3c.xRot = -0.48F - upangle;
	}

	private void doLeftFrontLeg(float angle, float upangle) {
		this.leg2.yRot = 1.2F + angle;
		this.leg2part2.yRot = this.leg2.yRot;
		this.leg2part2b.yRot = this.leg2.yRot;
		this.leg2part2c.yRot = this.leg2.yRot;
		this.leg2part2d.yRot = this.leg2.yRot;
		this.leg2part3.yRot = this.leg2.yRot;
		this.leg2part3b.yRot = this.leg2.yRot;
		this.leg2part3c.yRot = this.leg2.yRot;
		float dist = 14.0F;
		dist = (float)(dist * Math.cos(this.leg2.xRot));
		this.leg2part2.z = this.leg2part2b.z = this.leg2part2c.z = this.leg2part2d.z = (float)(this.leg2.z - Math.cos(this.leg2.yRot) * dist);
		this.leg2part2.x = this.leg2part2b.x = this.leg2part2c.x = this.leg2part2d.x = (float)(this.leg2.x - Math.sin(this.leg2.yRot) * dist);
		this.leg2part2.xRot = -1.152F + upangle;
		this.leg2part2b.xRot = -0.743F + upangle;
		this.leg2part2c.xRot = -0.632F + upangle;
		this.leg2part2d.xRot = -1.041F + upangle;
		dist = 14.0F;
		dist = (float)(dist * Math.cos(this.leg2part2.xRot));
		this.leg2part3.z = (float)(this.leg2part2.z - Math.cos(this.leg2part2.yRot) * dist);
		this.leg2part3.x = (float)(this.leg2part2.x - Math.sin(this.leg2part2.yRot) * dist);
		this.leg2part3.xRot = 0.669F - upangle;
		dist = 8.0F;
		dist = (float)Math.abs(dist * Math.cos(this.leg2part3.xRot));
		this.leg2part3b.z = this.leg2part3c.z = (float)(this.leg2part3.z - Math.cos(this.leg2part3.yRot) * dist);
		this.leg2part3b.x = this.leg2part3c.x = (float)(this.leg2part3.x - Math.sin(this.leg2part3.yRot) * dist);
		this.leg2part3b.xRot = -0.48F - upangle;
		this.leg2part3c.xRot = -0.48F - upangle;
	}

	private void doRightRearLeg(float angle, float upangle) {
		this.leg4.yRot = 2.1F + angle;
		this.leg4part2.yRot = this.leg4.yRot;
		this.leg4part2b.yRot = this.leg4.yRot;
		this.leg4part2c.yRot = this.leg4.yRot;
		this.leg4part2d.yRot = this.leg4.yRot;
		this.leg4part3.yRot = this.leg4.yRot;
		this.leg4part3b.yRot = this.leg4.yRot;
		this.leg4part3c.yRot = this.leg4.yRot;
		float dist = 14.0F;
		dist = (float)(dist * Math.cos(this.leg4.xRot));
		this.leg4part2.z = this.leg4part2b.z = this.leg4part2c.z = this.leg4part2d.z = (float)(this.leg4.z - Math.cos(this.leg4.yRot) * dist);
		this.leg4part2.x = this.leg4part2b.x = this.leg4part2c.x = this.leg4part2d.x = (float)(this.leg4.x - Math.sin(this.leg4.yRot) * dist);
		this.leg4part2.xRot = -1.152F + upangle;
		this.leg4part2b.xRot = -0.743F + upangle;
		this.leg4part2c.xRot = -0.632F + upangle;
		this.leg4part2d.xRot = -1.041F + upangle;
		dist = 14.0F;
		dist = (float)(dist * Math.cos(this.leg4part2.xRot));
		this.leg4part3.z = (float)(this.leg4part2.z - Math.cos(this.leg4part2.yRot) * dist);
		this.leg4part3.x = (float)(this.leg4part2.x - Math.sin(this.leg4part2.yRot) * dist);
		this.leg4part3.xRot = 0.669F - upangle;
		dist = 8.0F;
		dist = (float)Math.abs(dist * Math.cos(this.leg4part3.xRot));
		this.leg4part3b.z = this.leg4part3c.z = (float)(this.leg4part3.z - Math.cos(this.leg4part3.yRot) * dist);
		this.leg4part3b.x = this.leg4part3c.x = (float)(this.leg4part3.x - Math.sin(this.leg4part3.yRot) * dist);
		this.leg4part3b.xRot = -0.48F - upangle;
		this.leg4part3c.xRot = -0.48F - upangle;
	}

	private void doLeftRearLeg(float angle, float upangle) {
		this.leg3.yRot = -2.1F + angle;
		this.leg3part2.yRot = this.leg3.yRot;
		this.leg3part2b.yRot = this.leg3.yRot;
		this.leg3part2c.yRot = this.leg3.yRot;
		this.leg3part2d.yRot = this.leg3.yRot;
		this.leg3part3.yRot = this.leg3.yRot;
		this.leg3part3b.yRot = this.leg3.yRot;
		this.leg3part3c.yRot = this.leg3.yRot;
		float dist = 14.0F;
		dist = (float)(dist * Math.cos(this.leg3.xRot));
		this.leg3part2.z = this.leg3part2b.z = this.leg3part2c.z = this.leg3part2d.z = (float)(this.leg3.z - Math.cos(this.leg3.yRot) * dist);
		this.leg3part2.x = this.leg3part2b.x = this.leg3part2c.x = this.leg3part2d.x = (float)(this.leg3.x - Math.sin(this.leg3.yRot) * dist);
		this.leg3part2.xRot = -1.152F + upangle;
		this.leg3part2b.xRot = -0.743F + upangle;
		this.leg3part2c.xRot = -0.632F + upangle;
		this.leg3part2d.xRot = -1.041F + upangle;
		dist = 14.0F;
		dist = (float)(dist * Math.cos(this.leg3part2.xRot));
		this.leg3part3.z = (float)(this.leg3part2.z - Math.cos(this.leg3part2.yRot) * dist);
		this.leg3part3.x = (float)(this.leg3part2.x - Math.sin(this.leg3part2.yRot) * dist);
		this.leg3part3.xRot = 0.669F - upangle;
		dist = 8.0F;
		dist = (float)Math.abs(dist * Math.cos(this.leg3part3.xRot));
		this.leg3part3b.z = this.leg3part3c.z = (float)(this.leg3part3.z - Math.cos(this.leg3part3.yRot) * dist);
		this.leg3part3b.x = this.leg3part3c.x = (float)(this.leg3part3.x - Math.sin(this.leg3part3.yRot) * dist);
		this.leg3part3b.xRot = -0.48F - upangle;
		this.leg3part3c.xRot = -0.48F - upangle;
	}
}
