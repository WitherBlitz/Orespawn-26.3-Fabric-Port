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
 * Ported from OreSpawn's ModelCaveFisher (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). Reads OreSpawn's RenderInfo as
 * the renderer picks it: {@code variant} is ri1 (the claws snap when it's 1 or 3).
 */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CaveFisherModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Nose;
	private final ModelPart EyeLeft;
	private final ModelPart HeadMid;
	private final ModelPart HeadEnd;
	private final ModelPart TailTuft;
	private final ModelPart EyeRight;
	private final ModelPart BodyTopLeft4;
	private final ModelPart BodyTopRight4;
	private final ModelPart BodyTopLeft1;
	private final ModelPart BodyTopRight1;
	private final ModelPart BodyTopRight2;
	private final ModelPart BodyTopLeft2;
	private final ModelPart BodyTopRight3;
	private final ModelPart BodyTopLeft3;
	private final ModelPart HeadBase;
	private final ModelPart TailBase;
	private final ModelPart BodyLow2;
	private final ModelPart BodyLow1;
	private final ModelPart Spine5;
	private final ModelPart Spine1;
	private final ModelPart Spine2;
	private final ModelPart Spine3;
	private final ModelPart Spine4;
	private final ModelPart RightArmSeg4;
	private final ModelPart LeftArmSeg1;
	private final ModelPart LeftArmSeg3;
	private final ModelPart RightArmSeg2;
	private final ModelPart RightArmSeg1;
	private final ModelPart LeftArmSeg5;
	private final ModelPart LeftArmSeg2;
	private final ModelPart LeftClawTop;
	private final ModelPart RightArmSeg3;
	private final ModelPart RightArmSeg5;
	private final ModelPart LeftArmSeg4;
	private final ModelPart LeftClawBase;
	private final ModelPart RightClawBase;
	private final ModelPart LeftClawLow;
	private final ModelPart RightClawTop;
	private final ModelPart RightClawLow;
	private final ModelPart LBLeg1;
	private final ModelPart LBLeg3;
	private final ModelPart RBLeg1;
	private final ModelPart RBLeg3;
	private final ModelPart LBLeg2;
	private final ModelPart RBLeg2;
	private final ModelPart LBLeg4;
	private final ModelPart RBLeg4;
	private final ModelPart RBLeg5;
	private final ModelPart LBLeg6;
	private final ModelPart RBLeg6;
	private final ModelPart LBLeg5;
	private final ModelPart RFLeg1;
	private final ModelPart RFLeg2;
	private final ModelPart RFLeg3;
	private final ModelPart RFLeg4;
	private final ModelPart RFLeg5;
	private final ModelPart RFLeg6;
	private final ModelPart RMLeg1;
	private final ModelPart RMLeg2;
	private final ModelPart RMLeg3;
	private final ModelPart RMLeg4;
	private final ModelPart RMLeg5;
	private final ModelPart RMLeg6;
	private final ModelPart LFLeg1;
	private final ModelPart LFLeg2;
	private final ModelPart LFLeg3;
	private final ModelPart LFLeg4;
	private final ModelPart LFLeg5;
	private final ModelPart LFLeg6;
	private final ModelPart LMLeg1;
	private final ModelPart LMLeg2;
	private final ModelPart LMLeg4;
	private final ModelPart LMLeg3;
	private final ModelPart LMLeg5;
	private final ModelPart LMLeg6;
	private final float wingspeed;

	public CaveFisherModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Nose = root.getChild("nose");
		this.EyeLeft = root.getChild("eyeleft");
		this.HeadMid = root.getChild("headmid");
		this.HeadEnd = root.getChild("headend");
		this.TailTuft = root.getChild("tailtuft");
		this.EyeRight = root.getChild("eyeright");
		this.BodyTopLeft4 = root.getChild("bodytopleft4");
		this.BodyTopRight4 = root.getChild("bodytopright4");
		this.BodyTopLeft1 = root.getChild("bodytopleft1");
		this.BodyTopRight1 = root.getChild("bodytopright1");
		this.BodyTopRight2 = root.getChild("bodytopright2");
		this.BodyTopLeft2 = root.getChild("bodytopleft2");
		this.BodyTopRight3 = root.getChild("bodytopright3");
		this.BodyTopLeft3 = root.getChild("bodytopleft3");
		this.HeadBase = root.getChild("headbase");
		this.TailBase = root.getChild("tailbase");
		this.BodyLow2 = root.getChild("bodylow2");
		this.BodyLow1 = root.getChild("bodylow1");
		this.Spine5 = root.getChild("spine5");
		this.Spine1 = root.getChild("spine1");
		this.Spine2 = root.getChild("spine2");
		this.Spine3 = root.getChild("spine3");
		this.Spine4 = root.getChild("spine4");
		this.RightArmSeg4 = root.getChild("rightarmseg4");
		this.LeftArmSeg1 = root.getChild("leftarmseg1");
		this.LeftArmSeg3 = root.getChild("leftarmseg3");
		this.RightArmSeg2 = root.getChild("rightarmseg2");
		this.RightArmSeg1 = root.getChild("rightarmseg1");
		this.LeftArmSeg5 = root.getChild("leftarmseg5");
		this.LeftArmSeg2 = root.getChild("leftarmseg2");
		this.LeftClawTop = root.getChild("leftclawtop");
		this.RightArmSeg3 = root.getChild("rightarmseg3");
		this.RightArmSeg5 = root.getChild("rightarmseg5");
		this.LeftArmSeg4 = root.getChild("leftarmseg4");
		this.LeftClawBase = root.getChild("leftclawbase");
		this.RightClawBase = root.getChild("rightclawbase");
		this.LeftClawLow = root.getChild("leftclawlow");
		this.RightClawTop = root.getChild("rightclawtop");
		this.RightClawLow = root.getChild("rightclawlow");
		this.LBLeg1 = root.getChild("lbleg1");
		this.LBLeg3 = root.getChild("lbleg3");
		this.RBLeg1 = root.getChild("rbleg1");
		this.RBLeg3 = root.getChild("rbleg3");
		this.LBLeg2 = root.getChild("lbleg2");
		this.RBLeg2 = root.getChild("rbleg2");
		this.LBLeg4 = root.getChild("lbleg4");
		this.RBLeg4 = root.getChild("rbleg4");
		this.RBLeg5 = root.getChild("rbleg5");
		this.LBLeg6 = root.getChild("lbleg6");
		this.RBLeg6 = root.getChild("rbleg6");
		this.LBLeg5 = root.getChild("lbleg5");
		this.RFLeg1 = root.getChild("rfleg1");
		this.RFLeg2 = root.getChild("rfleg2");
		this.RFLeg3 = root.getChild("rfleg3");
		this.RFLeg4 = root.getChild("rfleg4");
		this.RFLeg5 = root.getChild("rfleg5");
		this.RFLeg6 = root.getChild("rfleg6");
		this.RMLeg1 = root.getChild("rmleg1");
		this.RMLeg2 = root.getChild("rmleg2");
		this.RMLeg3 = root.getChild("rmleg3");
		this.RMLeg4 = root.getChild("rmleg4");
		this.RMLeg5 = root.getChild("rmleg5");
		this.RMLeg6 = root.getChild("rmleg6");
		this.LFLeg1 = root.getChild("lfleg1");
		this.LFLeg2 = root.getChild("lfleg2");
		this.LFLeg3 = root.getChild("lfleg3");
		this.LFLeg4 = root.getChild("lfleg4");
		this.LFLeg5 = root.getChild("lfleg5");
		this.LFLeg6 = root.getChild("lfleg6");
		this.LMLeg1 = root.getChild("lmleg1");
		this.LMLeg2 = root.getChild("lmleg2");
		this.LMLeg4 = root.getChild("lmleg4");
		this.LMLeg3 = root.getChild("lmleg3");
		this.LMLeg5 = root.getChild("lmleg5");
		this.LMLeg6 = root.getChild("lmleg6");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelCaveFisher");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f1 = state.walkAnimationSpeed, f2 = state.ageInTicks;
		float pi4 = 1.570795F;
		float newangle = Mth.cos(f2 * 2.0F * this.wingspeed) * (float) Math.PI * 0.12F * f1;
		this.LFLeg1.yRot = newangle;
		this.LFLeg2.yRot = newangle;
		this.LFLeg3.yRot = newangle;
		this.LFLeg4.yRot = newangle;
		this.LFLeg5.yRot = newangle;
		this.LFLeg6.yRot = newangle;
		this.RFLeg1.yRot = -newangle;
		this.RFLeg2.yRot = -newangle;
		this.RFLeg3.yRot = -newangle;
		this.RFLeg4.yRot = -newangle;
		this.RFLeg5.yRot = -newangle;
		this.RFLeg6.yRot = -newangle;
		newangle = Mth.cos(f2 * 2.0F * this.wingspeed - 1.0F * pi4) * (float) Math.PI * 0.12F * f1;
		this.LMLeg1.yRot = newangle;
		this.LMLeg2.yRot = newangle;
		this.LMLeg3.yRot = newangle;
		this.LMLeg4.yRot = newangle;
		this.LMLeg5.yRot = newangle;
		this.LMLeg6.yRot = newangle;
		this.RMLeg1.yRot = -newangle;
		this.RMLeg2.yRot = -newangle;
		this.RMLeg3.yRot = -newangle;
		this.RMLeg4.yRot = -newangle;
		this.RMLeg5.yRot = -newangle;
		this.RMLeg6.yRot = -newangle;
		newangle = Mth.cos(f2 * 2.0F * this.wingspeed - 2.0F * pi4) * (float) Math.PI * 0.12F * f1;
		this.LBLeg1.yRot = newangle;
		this.LBLeg2.yRot = newangle;
		this.LBLeg3.yRot = newangle;
		this.LBLeg4.yRot = newangle;
		this.LBLeg5.yRot = newangle;
		this.LBLeg6.yRot = newangle;
		this.RBLeg1.yRot = -newangle;
		this.RBLeg2.yRot = -newangle;
		this.RBLeg3.yRot = -newangle;
		this.RBLeg4.yRot = -newangle;
		this.RBLeg5.yRot = -newangle;
		this.RBLeg6.yRot = -newangle;
		// OreSpawn's RenderInfo ri1, picked by the renderer (variant): the claws snap when it's 1 or 3.
		newangle = Mth.cos(f2 * 3.0F * this.wingspeed) * (float) Math.PI * 0.15F;
		float claw = state.variant != 1 && state.variant != 3 ? 0.0F : newangle;
		this.doLeftClaw(claw);
		this.doRightClaw(claw);
	}

	private void doLeftClaw(float angle) {
		this.LeftArmSeg1.xRot = Math.abs(angle);
		this.LeftArmSeg2.xRot = Math.abs(angle);
		this.LeftArmSeg3.xRot = Math.abs(angle);
		this.LeftArmSeg4.xRot = Math.abs(angle);
		this.LeftArmSeg5.xRot = Math.abs(angle);
		this.LeftClawBase.xRot = Math.abs(angle);
		this.LeftClawTop.xRot = Math.abs(angle) - 0.54F;
		this.LeftClawLow.xRot = Math.abs(angle) + 0.35F;
	}

	private void doRightClaw(float angle) {
		this.RightArmSeg1.xRot = Math.abs(angle);
		this.RightArmSeg2.xRot = Math.abs(angle);
		this.RightArmSeg3.xRot = Math.abs(angle);
		this.RightArmSeg4.xRot = Math.abs(angle);
		this.RightArmSeg5.xRot = Math.abs(angle);
		this.RightClawBase.xRot = Math.abs(angle);
		this.RightClawTop.xRot = Math.abs(angle) - 0.54F;
		this.RightClawLow.xRot = Math.abs(angle) + 0.35F;
	}
}
