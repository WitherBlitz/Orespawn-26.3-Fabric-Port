package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/**
 * Ported from OreSpawn's ModelEmperorScorpion (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). Reads {@code attacking},
 * and OreSpawn's RenderInfo as the renderer picks it: {@code variant} is which claws snap (1 left, 2 right, 3 both),
 * {@code flag} whether the tail strikes.
 */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class EmperorScorpionModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Head;
	private final ModelPart Seg1;
	private final ModelPart Seg2;
	private final ModelPart Seg3;
	private final ModelPart Seg4;
	private final ModelPart Seg5;
	private final ModelPart Seg6;
	private final ModelPart Seg7;
	private final ModelPart Seg8;
	private final ModelPart Tailseg1;
	private final ModelPart Tailseg2;
	private final ModelPart Tailseg3;
	private final ModelPart Tailseg4;
	private final ModelPart Tailseg5;
	private final ModelPart Tailseg6;
	private final ModelPart Tailseg7;
	private final ModelPart Tailseg8;
	private final ModelPart Stinger1;
	private final ModelPart Stinger2;
	private final ModelPart Stinger3;
	private final ModelPart LeftShoulder;
	private final ModelPart LeftArmSeg1;
	private final ModelPart LeftArmSeg2;
	private final ModelPart LeftArmSeg3;
	private final ModelPart LeftArmSeg4;
	private final ModelPart RightShoulder;
	private final ModelPart RightArmSeg1;
	private final ModelPart RightArmSeg2;
	private final ModelPart RightArmSeg3;
	private final ModelPart RightArmSeg4;
	private final ModelPart RightPincer;
	private final ModelPart LeftPincer;
	private final ModelPart Lefteye;
	private final ModelPart Righteye;
	private final ModelPart RightMandible;
	private final ModelPart LeftMandible;
	private final ModelPart RightManPart2;
	private final ModelPart LeftManPart2;
	private final ModelPart Leg1Seg1;
	private final ModelPart Leg1Seg2;
	private final ModelPart Leg1Seg3;
	private final ModelPart Leg1Seg4;
	private final ModelPart Leg1Seg5;
	private final ModelPart Leg2Seg1;
	private final ModelPart Leg2Seg2;
	private final ModelPart Leg2Seg3;
	private final ModelPart Leg2Seg4;
	private final ModelPart Leg2Seg5;
	private final ModelPart Leg3Seg1;
	private final ModelPart Leg3Seg2;
	private final ModelPart Leg3Seg3;
	private final ModelPart Leg3Seg4;
	private final ModelPart Leg3Seg5;
	private final ModelPart Leg4Seg1;
	private final ModelPart Leg4Seg2;
	private final ModelPart Leg4Seg3;
	private final ModelPart Leg4Seg4;
	private final ModelPart Leg4Seg5;
	private final ModelPart Leg5Seg1;
	private final ModelPart Leg5Seg2;
	private final ModelPart Leg5Seg3;
	private final ModelPart Leg5Seg4;
	private final ModelPart Leg5Seg5;
	private final ModelPart Leg6Seg1;
	private final ModelPart Leg6Seg2;
	private final ModelPart Leg6Seg3;
	private final ModelPart Leg6Seg4;
	private final ModelPart Leg6Seg5;
	private final ModelPart Leg7Seg1;
	private final ModelPart Leg7Seg2;
	private final ModelPart Leg7Seg3;
	private final ModelPart Leg7Seg4;
	private final ModelPart Leg7Seg5;
	private final ModelPart Leg8Seg1;
	private final ModelPart Leg8Seg2;
	private final ModelPart Leg8Seg3;
	private final ModelPart Leg8Seg4;
	private final ModelPart Leg8Seg5;
	private final float wingspeed;
	/** Each leg's five segments: legs 1-4 on the left, 5-8 on the right, front to back. */
	private final ModelPart[][] leftLegs;
	private final ModelPart[][] rightLegs;

	public EmperorScorpionModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Head = root.getChild("head");
		this.Seg1 = root.getChild("seg1");
		this.Seg2 = root.getChild("seg2");
		this.Seg3 = root.getChild("seg3");
		this.Seg4 = root.getChild("seg4");
		this.Seg5 = root.getChild("seg5");
		this.Seg6 = root.getChild("seg6");
		this.Seg7 = root.getChild("seg7");
		this.Seg8 = root.getChild("seg8");
		this.Tailseg1 = root.getChild("tailseg1");
		this.Tailseg2 = root.getChild("tailseg2");
		this.Tailseg3 = root.getChild("tailseg3");
		this.Tailseg4 = root.getChild("tailseg4");
		this.Tailseg5 = root.getChild("tailseg5");
		this.Tailseg6 = root.getChild("tailseg6");
		this.Tailseg7 = root.getChild("tailseg7");
		this.Tailseg8 = root.getChild("tailseg8");
		this.Stinger1 = root.getChild("stinger1");
		this.Stinger2 = root.getChild("stinger2");
		this.Stinger3 = root.getChild("stinger3");
		this.LeftShoulder = root.getChild("leftshoulder");
		this.LeftArmSeg1 = root.getChild("leftarmseg1");
		this.LeftArmSeg2 = root.getChild("leftarmseg2");
		this.LeftArmSeg3 = root.getChild("leftarmseg3");
		this.LeftArmSeg4 = root.getChild("leftarmseg4");
		this.RightShoulder = root.getChild("rightshoulder");
		this.RightArmSeg1 = root.getChild("rightarmseg1");
		this.RightArmSeg2 = root.getChild("rightarmseg2");
		this.RightArmSeg3 = root.getChild("rightarmseg3");
		this.RightArmSeg4 = root.getChild("rightarmseg4");
		this.RightPincer = root.getChild("rightpincer");
		this.LeftPincer = root.getChild("leftpincer");
		this.Lefteye = root.getChild("lefteye");
		this.Righteye = root.getChild("righteye");
		this.RightMandible = root.getChild("rightmandible");
		this.LeftMandible = root.getChild("leftmandible");
		this.RightManPart2 = root.getChild("rightmanpart2");
		this.LeftManPart2 = root.getChild("leftmanpart2");
		this.Leg1Seg1 = root.getChild("leg1seg1");
		this.Leg1Seg2 = root.getChild("leg1seg2");
		this.Leg1Seg3 = root.getChild("leg1seg3");
		this.Leg1Seg4 = root.getChild("leg1seg4");
		this.Leg1Seg5 = root.getChild("leg1seg5");
		this.Leg2Seg1 = root.getChild("leg2seg1");
		this.Leg2Seg2 = root.getChild("leg2seg2");
		this.Leg2Seg3 = root.getChild("leg2seg3");
		this.Leg2Seg4 = root.getChild("leg2seg4");
		this.Leg2Seg5 = root.getChild("leg2seg5");
		this.Leg3Seg1 = root.getChild("leg3seg1");
		this.Leg3Seg2 = root.getChild("leg3seg2");
		this.Leg3Seg3 = root.getChild("leg3seg3");
		this.Leg3Seg4 = root.getChild("leg3seg4");
		this.Leg3Seg5 = root.getChild("leg3seg5");
		this.Leg4Seg1 = root.getChild("leg4seg1");
		this.Leg4Seg2 = root.getChild("leg4seg2");
		this.Leg4Seg3 = root.getChild("leg4seg3");
		this.Leg4Seg4 = root.getChild("leg4seg4");
		this.Leg4Seg5 = root.getChild("leg4seg5");
		this.Leg5Seg1 = root.getChild("leg5seg1");
		this.Leg5Seg2 = root.getChild("leg5seg2");
		this.Leg5Seg3 = root.getChild("leg5seg3");
		this.Leg5Seg4 = root.getChild("leg5seg4");
		this.Leg5Seg5 = root.getChild("leg5seg5");
		this.Leg6Seg1 = root.getChild("leg6seg1");
		this.Leg6Seg2 = root.getChild("leg6seg2");
		this.Leg6Seg3 = root.getChild("leg6seg3");
		this.Leg6Seg4 = root.getChild("leg6seg4");
		this.Leg6Seg5 = root.getChild("leg6seg5");
		this.Leg7Seg1 = root.getChild("leg7seg1");
		this.Leg7Seg2 = root.getChild("leg7seg2");
		this.Leg7Seg3 = root.getChild("leg7seg3");
		this.Leg7Seg4 = root.getChild("leg7seg4");
		this.Leg7Seg5 = root.getChild("leg7seg5");
		this.Leg8Seg1 = root.getChild("leg8seg1");
		this.Leg8Seg2 = root.getChild("leg8seg2");
		this.Leg8Seg3 = root.getChild("leg8seg3");
		this.Leg8Seg4 = root.getChild("leg8seg4");
		this.Leg8Seg5 = root.getChild("leg8seg5");
		this.leftLegs = new ModelPart[][] {{Leg1Seg1, Leg1Seg2, Leg1Seg3, Leg1Seg4, Leg1Seg5}, {Leg2Seg1, Leg2Seg2, Leg2Seg3, Leg2Seg4, Leg2Seg5},
			{Leg3Seg1, Leg3Seg2, Leg3Seg3, Leg3Seg4, Leg3Seg5}, {Leg4Seg1, Leg4Seg2, Leg4Seg3, Leg4Seg4, Leg4Seg5}};
		this.rightLegs = new ModelPart[][] {{Leg5Seg1, Leg5Seg2, Leg5Seg3, Leg5Seg4, Leg5Seg5}, {Leg6Seg1, Leg6Seg2, Leg6Seg3, Leg6Seg4, Leg6Seg5},
			{Leg7Seg1, Leg7Seg2, Leg7Seg3, Leg7Seg4, Leg7Seg5}, {Leg8Seg1, Leg8Seg2, Leg8Seg3, Leg8Seg4, Leg8Seg5}};
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelEmperorScorpion");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f1 = state.walkAnimationSpeed, f2 = state.ageInTicks;
		float pi4 = 1.570795F;
		for (int n = 0; n < 4; n++) {
			float newangle = Mth.cos(f2 * 2.0F * this.wingspeed - n * pi4) * (float) Math.PI * 0.12F * f1;
			float nextangle = Mth.cos((f2 + 0.1F) * 2.0F * this.wingspeed - n * pi4) * (float) Math.PI * 0.12F * f1;
			float upangle = nextangle > newangle ? 0.47F * f1 - Math.abs(newangle) : 0.0F;
			ModelPart[] left = leftLegs[n], right = rightLegs[n];
			doLeftLeg(left[1], left[2], left[3], left[4], newangle, upangle);
			doRightLeg(right[1], right[2], right[3], right[4], -newangle, upangle);
		}
		float newangle;
		if (!state.attacking) {
			newangle = Mth.cos(f2 * 0.5F * this.wingspeed) * (float) Math.PI * 0.05F;
		} else {
			newangle = Mth.cos(f2 * 2.5F * this.wingspeed) * (float) Math.PI * 0.15F;
		}
		this.LeftManPart2.zRot = newangle;
		this.RightManPart2.zRot = -newangle;
		// OreSpawn's RenderInfo, picked by the renderer: variant = ri1 (which claws snap), flag = ri2 == 1 (the tail strikes).
		newangle = Mth.cos(f2 * 3.0F * this.wingspeed) * (float) Math.PI * 0.15F;
		int ri1 = state.variant;
		this.doLeftClaw(ri1 != 1 && ri1 != 3 ? 0.0F : newangle);
		this.doRightClaw(ri1 != 2 && ri1 != 3 ? 0.0F : newangle);
		this.doTail(state.flag ? newangle : 0.0F);
	}

	private void doLeftLeg(ModelPart seg2, ModelPart seg3, ModelPart seg4, ModelPart seg5, float angle, float upangle) {
		seg2.yRot = angle;
		seg3.yRot = angle;
		seg4.yRot = angle;
		seg5.yRot = angle;
		seg3.z = (float) (seg2.z - Math.sin(angle) * 6.0);
		seg3.x = (float) (seg2.x - Math.abs(Math.sin(angle) * 6.0) + 6.0);
		seg4.z = (float) (seg3.z - Math.sin(angle) * 9.0);
		seg4.x = (float) (seg3.x - Math.abs(Math.sin(angle) * 9.0) + 9.0);
		seg5.z = (float) (seg4.z - Math.sin(angle) * 1.0);
		seg5.x = (float) (seg4.x - Math.abs(Math.sin(angle) * 1.0) + 1.0);
		seg2.zRot = -upangle - 0.929F;
		seg3.zRot = -upangle + 0.632F;
		seg3.y = seg2.y + (float) (11.5 * Math.sin(seg2.zRot));
		seg4.y = seg3.y + (float) (11.5 * Math.sin(seg3.zRot));
		seg5.y = seg4.y + 6.5F;
	}

	private void doRightLeg(ModelPart seg2, ModelPart seg3, ModelPart seg4, ModelPart seg5, float angle, float upangle) {
		seg2.yRot = angle;
		seg3.yRot = angle;
		seg4.yRot = angle;
		seg5.yRot = -angle;
		seg3.z = (float) (seg2.z + Math.sin(angle) * 6.0);
		seg3.x = (float) (seg2.x + Math.abs(Math.sin(angle) * 6.0) - 6.0);
		seg4.z = (float) (seg3.z + Math.sin(angle) * 9.0);
		seg4.x = (float) (seg3.x + Math.abs(Math.sin(angle) * 9.0) - 9.0);
		seg5.z = (float) (seg4.z + Math.sin(angle) * 1.0);
		seg5.x = (float) (seg4.x + Math.abs(Math.sin(angle) * 1.0) - 1.0);
		seg2.zRot = upangle + 0.929F;
		seg3.zRot = upangle - 0.632F;
		seg3.y = seg2.y - (float) (11.5 * Math.sin(seg2.zRot));
		seg4.y = seg3.y - (float) (11.5 * Math.sin(seg3.zRot));
		seg5.y = seg4.y + 6.5F;
	}

	private void doLeftClaw(float angle) {
		this.LeftArmSeg1.yRot = -1.57F + angle;
		this.LeftArmSeg2.z = (float) (-22.0 - Math.cos(this.LeftArmSeg1.yRot) * 12.0);
		this.LeftArmSeg3.z = this.LeftArmSeg2.z - 11.0F;
		this.LeftArmSeg4.z = this.LeftArmSeg2.z - 11.0F;
		this.LeftPincer.z = this.LeftArmSeg2.z - 11.0F;
		this.LeftArmSeg3.yRot = 0.074F + angle;
		this.LeftPincer.yRot = 0.371F - angle;
	}

	private void doRightClaw(float angle) {
		this.RightArmSeg1.yRot = 1.57F - angle;
		this.RightArmSeg2.z = (float) (-22.0 - Math.cos(this.RightArmSeg1.yRot) * 12.0);
		this.RightArmSeg3.z = this.RightArmSeg2.z - 11.0F;
		this.RightArmSeg4.z = this.RightArmSeg2.z - 11.0F;
		this.RightPincer.z = this.RightArmSeg2.z - 11.0F;
		this.RightArmSeg3.yRot = -0.074F - angle;
		this.RightPincer.yRot = -0.371F + angle;
	}

	private void doTail(float angle) {
		this.Tailseg1.xRot = 0.594F + angle;
		this.Tailseg2.xRot = this.Tailseg1.xRot + 0.48399997F + angle;
		this.Tailseg2.y = (float) (this.Tailseg1.y - Math.sin(this.Tailseg1.xRot) * 9.0);
		this.Tailseg2.z = (float) (this.Tailseg1.z + Math.cos(this.Tailseg1.xRot) * 9.0);
		this.Tailseg3.xRot = this.Tailseg2.xRot + 0.6320001F + angle;
		this.Tailseg3.y = (float) (this.Tailseg2.y - Math.sin(this.Tailseg2.xRot) * 10.0);
		this.Tailseg3.z = (float) (this.Tailseg2.z + Math.cos(this.Tailseg2.xRot) * 10.0);
		this.Tailseg4.xRot = this.Tailseg3.xRot + 0.5569999F - angle;
		this.Tailseg4.y = (float) (this.Tailseg3.y - Math.sin(this.Tailseg3.xRot) * 10.0);
		this.Tailseg4.z = (float) (this.Tailseg3.z + Math.cos(this.Tailseg3.xRot) * 10.0);
		this.Tailseg5.xRot = this.Tailseg4.xRot + 0.63199997F - angle;
		this.Tailseg5.y = (float) (this.Tailseg4.y - Math.sin(this.Tailseg4.xRot) * 10.0);
		this.Tailseg5.z = (float) (this.Tailseg4.z + Math.cos(this.Tailseg4.xRot) * 10.0);
		this.Tailseg6.xRot = this.Tailseg5.xRot + -5.501F - angle * 3.0F / 2.0F - 0.4F;
		this.Tailseg6.y = (float) (this.Tailseg5.y - Math.sin(this.Tailseg5.xRot) * 10.0);
		this.Tailseg6.z = (float) (this.Tailseg5.z + Math.cos(this.Tailseg5.xRot) * 10.0);
		this.Tailseg7.xRot = this.Tailseg6.xRot + -2.822F - angle * 2.5F - 2.2F;
		this.Tailseg7.y = (float) (this.Tailseg6.y - Math.sin(this.Tailseg6.xRot) * 10.0);
		this.Tailseg7.z = (float) (this.Tailseg6.z + Math.cos(this.Tailseg6.xRot) * 10.0);
		this.Tailseg8.xRot = this.Tailseg7.xRot;
		this.Tailseg8.y = this.Tailseg7.y;
		this.Tailseg8.z = this.Tailseg7.z;
		this.Stinger1.xRot = this.Tailseg7.xRot + 0.0F + angle * 0.66F;
		this.Stinger1.y = (float) (this.Tailseg7.y - Math.sin(this.Tailseg7.xRot) * 10.0);
		this.Stinger1.z = (float) (this.Tailseg7.z + Math.cos(this.Tailseg7.xRot) * 10.0);
		this.Stinger2.xRot = this.Stinger1.xRot + -0.48F + angle;
		this.Stinger2.y = (float) (this.Stinger1.y - Math.sin(this.Stinger1.xRot) * 3.0);
		this.Stinger2.z = (float) (this.Stinger1.z + Math.cos(this.Stinger1.xRot) * 3.0);
		this.Stinger3.xRot = this.Stinger2.xRot + -1.01F + angle * 1.7F;
		this.Stinger3.y = (float) (this.Stinger2.y - Math.sin(this.Stinger2.xRot) * 3.0);
		this.Stinger3.z = (float) (this.Stinger2.z + Math.cos(this.Stinger2.xRot) * 3.0);
	}
}
