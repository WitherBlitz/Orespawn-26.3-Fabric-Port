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
 * Ported from OreSpawn's ModelGodzilla (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels): Mobzilla's walk (legs and toes
 * stepping), swaying tail (faster while attacking), head following the look, and the jaw and arms that now and then
 * join the swing (1 in 20 swings idle, 1 in 2 attacking). {@code state.attacking}: Godzilla.getAttacking();
 * {@code state.variant}: the entity's id, which picks those swings as OreSpawn's per-entity RenderInfo did.
 */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class GodzillaModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart LToe1;
	private final ModelPart LToe3;
	private final ModelPart LToe2;
	private final ModelPart LToe9;
	private final ModelPart LToe8;
	private final ModelPart LToe7;
	private final ModelPart LToe6;
	private final ModelPart LToe5;
	private final ModelPart LToe4;
	private final ModelPart RToe9;
	private final ModelPart RToe6;
	private final ModelPart RToe5;
	private final ModelPart RToe2;
	private final ModelPart RToe1;
	private final ModelPart RToe4;
	private final ModelPart RToe7;
	private final ModelPart RToe8;
	private final ModelPart RToe3;
	private final ModelPart LThigh;
	private final ModelPart LLowerLeg;
	private final ModelPart LUpperLeg;
	private final ModelPart TailTip;
	private final ModelPart RLegLower;
	private final ModelPart RLegUpper;
	private final ModelPart RThigh;
	private final ModelPart LowerJaw;
	private final ModelPart TailBase;
	private final ModelPart Tail2;
	private final ModelPart Tail3;
	private final ModelPart Tail4;
	private final ModelPart Tail5;
	private final ModelPart Tail6;
	private final ModelPart Tail7;
	private final ModelPart BodyBottom;
	private final ModelPart RLowerArm;
	private final ModelPart BodyCenter;
	private final ModelPart Neck;
	private final ModelPart TopJaw;
	private final ModelPart Head;
	private final ModelPart BodyTop;
	private final ModelPart RShoulder;
	private final ModelPart RThumbTip;
	private final ModelPart RUpperArm;
	private final ModelPart RHand;
	private final ModelPart RThumbBase;
	private final ModelPart R3rdFingerTip;
	private final ModelPart R3rdFingerBase;
	private final ModelPart RIndexTip;
	private final ModelPart RIndexBase;
	private final ModelPart LShoulder;
	private final ModelPart LUpperArm;
	private final ModelPart LLowerArm;
	private final ModelPart LIndexBase;
	private final ModelPart LIndexTip;
	private final ModelPart LHand;
	private final ModelPart LThumbBase;
	private final ModelPart LThumbTip;
	private final ModelPart L3rdFingerTip;
	private final ModelPart L3rdFingerBase;
	private final ModelPart Lspikes1;
	private final ModelPart Rspikes1;
	private final ModelPart Lspike2;
	private final ModelPart Rspike2;
	private final ModelPart Lspike3;
	private final ModelPart Rspike3;
	private final ModelPart Lspike4;
	private final ModelPart Rspike4;
	private final ModelPart Lspike5;
	private final ModelPart Rspike5;
	private final ModelPart Spike6;
	private final ModelPart Spikes7;
	private final float wingspeed;

	public GodzillaModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.LToe1 = root.getChild("ltoe1");
		this.LToe3 = root.getChild("ltoe3");
		this.LToe2 = root.getChild("ltoe2");
		this.LToe9 = root.getChild("ltoe9");
		this.LToe8 = root.getChild("ltoe8");
		this.LToe7 = root.getChild("ltoe7");
		this.LToe6 = root.getChild("ltoe6");
		this.LToe5 = root.getChild("ltoe5");
		this.LToe4 = root.getChild("ltoe4");
		this.RToe9 = root.getChild("rtoe9");
		this.RToe6 = root.getChild("rtoe6");
		this.RToe5 = root.getChild("rtoe5");
		this.RToe2 = root.getChild("rtoe2");
		this.RToe1 = root.getChild("rtoe1");
		this.RToe4 = root.getChild("rtoe4");
		this.RToe7 = root.getChild("rtoe7");
		this.RToe8 = root.getChild("rtoe8");
		this.RToe3 = root.getChild("rtoe3");
		this.LThigh = root.getChild("lthigh");
		this.LLowerLeg = root.getChild("llowerleg");
		this.LUpperLeg = root.getChild("lupperleg");
		this.TailTip = root.getChild("tailtip");
		this.RLegLower = root.getChild("rleglower");
		this.RLegUpper = root.getChild("rlegupper");
		this.RThigh = root.getChild("rthigh");
		this.LowerJaw = root.getChild("lowerjaw");
		this.TailBase = root.getChild("tailbase");
		this.Tail2 = root.getChild("tail2");
		this.Tail3 = root.getChild("tail3");
		this.Tail4 = root.getChild("tail4");
		this.Tail5 = root.getChild("tail5");
		this.Tail6 = root.getChild("tail6");
		this.Tail7 = root.getChild("tail7");
		this.BodyBottom = root.getChild("bodybottom");
		this.RLowerArm = root.getChild("rlowerarm");
		this.BodyCenter = root.getChild("bodycenter");
		this.Neck = root.getChild("neck");
		this.TopJaw = root.getChild("topjaw");
		this.Head = root.getChild("head");
		this.BodyTop = root.getChild("bodytop");
		this.RShoulder = root.getChild("rshoulder");
		this.RThumbTip = root.getChild("rthumbtip");
		this.RUpperArm = root.getChild("rupperarm");
		this.RHand = root.getChild("rhand");
		this.RThumbBase = root.getChild("rthumbbase");
		this.R3rdFingerTip = root.getChild("r3rdfingertip");
		this.R3rdFingerBase = root.getChild("r3rdfingerbase");
		this.RIndexTip = root.getChild("rindextip");
		this.RIndexBase = root.getChild("rindexbase");
		this.LShoulder = root.getChild("lshoulder");
		this.LUpperArm = root.getChild("lupperarm");
		this.LLowerArm = root.getChild("llowerarm");
		this.LIndexBase = root.getChild("lindexbase");
		this.LIndexTip = root.getChild("lindextip");
		this.LHand = root.getChild("lhand");
		this.LThumbBase = root.getChild("lthumbbase");
		this.LThumbTip = root.getChild("lthumbtip");
		this.L3rdFingerTip = root.getChild("l3rdfingertip");
		this.L3rdFingerBase = root.getChild("l3rdfingerbase");
		this.Lspikes1 = root.getChild("lspikes1");
		this.Rspikes1 = root.getChild("rspikes1");
		this.Lspike2 = root.getChild("lspike2");
		this.Rspike2 = root.getChild("rspike2");
		this.Lspike3 = root.getChild("lspike3");
		this.Rspike3 = root.getChild("rspike3");
		this.Lspike4 = root.getChild("lspike4");
		this.Rspike4 = root.getChild("rspike4");
		this.Lspike5 = root.getChild("lspike5");
		this.Rspike5 = root.getChild("rspike5");
		this.Spike6 = root.getChild("spike6");
		this.Spikes7 = root.getChild("spikes7");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelGodzilla");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		boolean attacking = state.attacking;
		float newangle = 0.0F;
		float newangle2 = 0.0F;
		float pscale = 1.0F;
		float pi4 = (float) (Math.PI / 4);
		float clawZ = 6.0F;
		float clawY = 16.0F;
		float clawZamp = 35.0F * pscale;
		float clawYamp = 18.0F * pscale;
		float spikeamp = 1.0F;
		float spikefreq = 1.0F;
		float t1 = 0.0F;
		float t2 = 0.0F;
		if (f1 > 0.001) {
		newangle = Mth.cos(f2 * 0.75F * this.wingspeed / pscale);
		newangle2 = Mth.cos(f2 * 0.75F * this.wingspeed / pscale + pi4);
		t1 = Mth.sin(f2 * 0.75F * this.wingspeed / pscale);
		} else {
		newangle2 = 0.0F;
		newangle = 0.0F;
		t1 = 0.0F;
		t2 = 0.0F;
		}
		if (t1 > 0.0F) {
		t2 = t1 * clawYamp * f1;
		this.LToe1.y = clawY - t2;
		} else {
		this.LToe1.y = clawY;
		}
		this.LToe1.z = clawZ + clawZamp * newangle * f1;
		this.LToe2.z = this.LToe3.z = this.LToe4.z = this.LToe5.z = this.LToe6.z = this.LToe7.z = this.LToe8
		.z = this.LToe9.z = this.LToe1.z;
		this.LToe2.y = this.LToe3.y = this.LToe4.y = this.LToe5.y = this.LToe6.y = this.LToe7.y = this.LToe8
		.y = this.LToe9.y = this.LToe1.y;
		this.LLowerLeg.z = this.LToe1.z;
		this.LLowerLeg.y = this.LToe1.y;
		this.LLowerLeg.xRot = 0.22F + newangle * (float) Math.PI * 0.09F * f1;
		this.LUpperLeg.xRot = -0.17F + newangle2 * (float) Math.PI * 0.15F * f1;
		this.LUpperLeg.y = this.LLowerLeg.y - (float)Math.cos(this.LLowerLeg.xRot) * 55.0F;
		this.LUpperLeg.z = this.LLowerLeg.z - (float)Math.sin(this.LLowerLeg.xRot) * 55.0F;
		this.LThigh.xRot = -0.558F + newangle2 * (float) Math.PI * 0.1F * f1;
		this.LThigh.z = 2.0F + clawZamp * newangle * f1 / 4.0F;
		t1 = 0.0F;
		t2 = 0.0F;
		if (f1 > 0.001) {
		newangle = Mth.cos(f2 * 0.75F * this.wingspeed / pscale + pi4 * 4.0F);
		newangle2 = Mth.cos(f2 * 0.75F * this.wingspeed / pscale + pi4 * 5.0F);
		t1 = Mth.sin(f2 * 0.75F * this.wingspeed / pscale + pi4 * 4.0F);
		} else {
		newangle = 0.0F;
		t1 = 0.0F;
		t2 = 0.0F;
		}
		if (t1 > 0.0F) {
		t2 = t1 * clawYamp * f1;
		this.RToe1.y = clawY - t2;
		} else {
		this.RToe1.y = clawY;
		}
		this.RToe1.z = clawZ + clawZamp * newangle * f1;
		this.RToe2.z = this.RToe3.z = this.RToe4.z = this.RToe5.z = this.RToe6.z = this.RToe7.z = this.RToe8
		.z = this.RToe9.z = this.RToe1.z;
		this.RToe2.y = this.RToe3.y = this.RToe4.y = this.RToe5.y = this.RToe6.y = this.RToe7.y = this.RToe8
		.y = this.RToe9.y = this.RToe1.y;
		this.RLegLower.z = this.RToe1.z;
		this.RLegLower.y = this.RToe1.y;
		this.RLegLower.xRot = 0.22F + newangle * (float) Math.PI * 0.09F * f1;
		this.RLegUpper.xRot = -0.17F + newangle2 * (float) Math.PI * 0.15F * f1;
		this.RLegUpper.y = this.RLegLower.y - (float)Math.cos(this.RLegLower.xRot) * 55.0F;
		this.RLegUpper.z = this.RLegLower.z - (float)Math.sin(this.RLegLower.xRot) * 55.0F;
		this.RThigh.xRot = -0.558F + newangle2 * (float) Math.PI * 0.1F * f1;
		this.RThigh.z = 2.0F + clawZamp * newangle * f1 / 4.0F;
		this.LToe2.xRot = this.LToe3.xRot = this.LToe4.xRot = this.LToe5.xRot = this.LToe6.xRot = this.LToe7.xRot = this.LToe8
		.xRot = this.LToe9.xRot = this.LToe1.xRot = 0.0F;
		this.RToe2.xRot = this.RToe3.xRot = this.RToe4.xRot = this.RToe5.xRot = this.RToe6.xRot = this.RToe7.xRot = this.RToe8
		.xRot = this.RToe9.xRot = this.RToe1.xRot = 0.0F;
		if (attacking) {
		newangle = Mth.cos(f2 * this.wingspeed * 1.75F) * (float) Math.PI * 0.2F;
		} else {
		newangle = Mth.cos(f2 * this.wingspeed * 0.75F) * (float) Math.PI * 0.05F;
		}
		this.doTail(newangle);
		newangle = (float)Math.toRadians(f3) * 0.55F;
		this.Head.yRot = newangle;
		this.TopJaw.yRot = newangle;
		this.LowerJaw.yRot = newangle;
		this.LowerJaw.z = this.Head.z - (float)Math.cos(this.Head.yRot) * 11.0F;
		this.LowerJaw.x = this.Head.x - (float)Math.sin(this.Head.yRot) * 11.0F;
		this.TopJaw.xRot = this.Head.xRot = (float)Math.toRadians(f4);
		newangle = Mth.cos(f2 * this.wingspeed * 1.5F) * (float) Math.PI * 0.12F;
		// OreSpawn's RenderInfo re-rolled "jaw moving" each time the jaw swing came round (1 in 20 idle, 1 in 2
		// attacking); here the roll comes from the swing count and the entity (state.variant: the entity id).
		int odds = attacking ? 2 : 20;
		if (!roll(state.variant, swing(f2 * 1.5F * this.wingspeed), 0, odds)) {
		newangle = 0.0F;
		}
		this.LowerJaw.xRot = 0.52F + newangle + this.TopJaw.xRot;
		newangle = newangle2 = Mth.sin(f2 * this.wingspeed * 1.75F) * (float) Math.PI * 0.16F;
		// The arms likewise, each re-rolled every arm swing.
		int armSwing = swing(f2 * 1.75F * this.wingspeed);
		if (!roll(state.variant, armSwing, 1, odds)) {
		newangle = 0.0F;
		}
		if (!roll(state.variant, armSwing, 2, odds)) {
		newangle2 = 0.0F;
		}
		this.LUpperArm.yRot = 0.65F + newangle;
		this.LLowerArm.yRot = 0.78F + newangle * 3.0F / 2.0F;
		this.LLowerArm.z = this.LUpperArm.z - (float)Math.sin(this.LUpperArm.yRot) * 50.0F;
		this.LLowerArm.x = this.LUpperArm.x + (float)Math.cos(this.LUpperArm.yRot) * 50.0F;
		this.LLowerArm.y = this.LUpperArm.y - (float)Math.sin(this.LUpperArm.yRot) * 10.0F + 18.0F;
		this.LHand.z = this.LLowerArm.z - (float)Math.sin(this.LLowerArm.yRot) * 45.0F;
		this.LHand.x = this.LLowerArm.x + (float)Math.cos(this.LLowerArm.yRot) * 45.0F;
		this.LHand.y = this.LLowerArm.y - (float)Math.sin(this.LLowerArm.yRot) * 10.0F + 15.0F;
		this.LIndexBase.z = this.LThumbBase.z = this.L3rdFingerBase.z = this.LHand.z;
		this.LIndexTip.z = this.LThumbTip.z = this.L3rdFingerTip.z = this.LHand.z;
		this.LIndexBase.y = this.LThumbBase.y = this.L3rdFingerBase.y = this.LHand.y;
		this.LIndexTip.y = this.LThumbTip.y = this.L3rdFingerTip.y = this.LHand.y;
		this.LIndexBase.x = this.LThumbBase.x = this.L3rdFingerBase.x = this.LHand.x;
		this.LIndexTip.x = this.LThumbTip.x = this.L3rdFingerTip.x = this.LHand.x;
		this.LHand.yRot = 1.308F + newangle * 2.0F;
		this.LIndexBase.yRot = -0.139F + newangle * 2.0F;
		this.LIndexTip.yRot = -0.034F + newangle * 2.0F;
		this.LThumbBase.yRot = 0.261F + newangle;
		this.LThumbTip.yRot = 0.139F + newangle;
		this.L3rdFingerBase.yRot = -0.471F + newangle * 3.0F;
		this.L3rdFingerTip.yRot = -0.331F + newangle * 3.0F;
		this.RUpperArm.yRot = -0.65F - newangle2;
		this.RLowerArm.yRot = -0.78F - newangle2 * 3.0F / 2.0F;
		this.RLowerArm.z = this.RUpperArm.z + (float)Math.sin(this.RUpperArm.yRot) * 50.0F;
		this.RLowerArm.x = this.RUpperArm.x - (float)Math.cos(this.RUpperArm.yRot) * 50.0F;
		this.RLowerArm.y = this.RUpperArm.y + (float)Math.sin(this.RUpperArm.yRot) * 10.0F + 18.0F;
		this.RHand.z = this.RLowerArm.z + (float)Math.sin(this.RLowerArm.yRot) * 45.0F;
		this.RHand.x = this.RLowerArm.x - (float)Math.cos(this.RLowerArm.yRot) * 45.0F;
		this.RHand.y = this.RLowerArm.y + (float)Math.sin(this.RLowerArm.yRot) * 10.0F + 15.0F;
		this.RIndexBase.z = this.RThumbBase.z = this.R3rdFingerBase.z = this.RHand.z;
		this.RIndexTip.z = this.RThumbTip.z = this.R3rdFingerTip.z = this.RHand.z;
		this.RIndexBase.y = this.RThumbBase.y = this.R3rdFingerBase.y = this.RHand.y;
		this.RIndexTip.y = this.RThumbTip.y = this.R3rdFingerTip.y = this.RHand.y;
		this.RIndexBase.x = this.RThumbBase.x = this.R3rdFingerBase.x = this.RHand.x;
		this.RIndexTip.x = this.RThumbTip.x = this.R3rdFingerTip.x = this.RHand.x;
		this.RHand.yRot = -2.0F - newangle2 * 2.0F;
		this.RIndexBase.yRot = 0.157F - newangle2 * 2.0F;
		this.RIndexTip.yRot = 0.174F - newangle2 * 2.0F;
		this.RThumbBase.yRot = -0.104F - newangle2;
		this.RThumbTip.yRot = 0.001F - newangle2;
		this.R3rdFingerTip.yRot = 0.68F - newangle2 * 3.0F;
		this.R3rdFingerBase.yRot = 0.645F - newangle2 * 3.0F;
	}

	/** How many full turns (2 pi) a swing at this phase has made. */
	private static int swing(float phase) {
		return Mth.floor(Math.abs(phase) / Mth.TWO_PI);
	}

	/** OreSpawn's nextInt(odds) == 1, for one entity and swing. */
	private static boolean roll(int id, int swing, int salt, int odds) {
		int h = id * 0x9E3779B1 + swing * 0x85EBCA6B + salt * 0xC2B2AE35;
		h ^= h >>> 15;
		h *= 0x2C1B3C6D;
		h ^= h >>> 12;
		return Math.floorMod(h, odds) == 1;
	}

	private void doTail(float angle) {
		this.TailBase.yRot = angle * 0.25F;
		this.Lspike5.yRot = this.Rspike5.yRot = this.TailBase.yRot;
		this.Tail2.yRot = angle * 0.5F;
		this.Tail2.z = this.TailBase.z + (float) Math.cos(this.TailBase.yRot) * 25.0F;
		this.Tail2.x = this.TailBase.x + (float) Math.sin(this.TailBase.yRot) * 25.0F;
		this.Spike6.yRot = this.Tail2.yRot;
		this.Spike6.z = this.Tail2.z;
		this.Spike6.x = this.Tail2.x;
		this.Tail3.yRot = angle * 0.75F;
		this.Tail3.z = this.Tail2.z + (float) Math.cos(this.Tail2.yRot) * 20.0F;
		this.Tail3.x = this.Tail2.x + (float) Math.sin(this.Tail2.yRot) * 20.0F;
		this.Spikes7.yRot = this.Tail3.yRot;
		this.Spikes7.z = this.Tail3.z;
		this.Spikes7.x = this.Tail3.x;
		this.Tail4.yRot = angle * 1.25F;
		this.Tail4.z = this.Tail3.z + (float) Math.cos(this.Tail3.yRot) * 20.0F;
		this.Tail4.x = this.Tail3.x + (float) Math.sin(this.Tail3.yRot) * 20.0F;
		this.Tail5.yRot = angle * 1.5F;
		this.Tail5.z = this.Tail4.z + (float) Math.cos(this.Tail4.yRot) * 25.0F;
		this.Tail5.x = this.Tail4.x + (float) Math.sin(this.Tail4.yRot) * 25.0F;
		this.Tail6.yRot = angle * 1.75F;
		this.Tail6.z = this.Tail5.z + (float) Math.cos(this.Tail5.yRot) * 27.0F;
		this.Tail6.x = this.Tail5.x + (float) Math.sin(this.Tail5.yRot) * 27.0F;
		this.Tail7.yRot = angle * 2.0F;
		this.Tail7.z = this.Tail6.z + (float) Math.cos(this.Tail6.yRot) * 28.0F;
		this.Tail7.x = this.Tail6.x + (float) Math.sin(this.Tail6.yRot) * 28.0F;
		this.TailTip.yRot = angle * 2.25F;
		this.TailTip.z = this.Tail7.z + (float) Math.cos(this.Tail7.yRot) * 18.0F;
		this.TailTip.x = this.Tail7.x + (float) Math.sin(this.Tail7.yRot) * 18.0F;
	}
}
