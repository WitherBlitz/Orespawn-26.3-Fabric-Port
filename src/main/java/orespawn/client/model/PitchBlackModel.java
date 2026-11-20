package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelPitchBlack (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class PitchBlackModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart lclaw1;
	private final ModelPart body;
	private final ModelPart leftleg1;
	private final ModelPart tail1;
	private final ModelPart leftleg2;
	private final ModelPart body2;
	private final ModelPart leftleg3;
	private final ModelPart tail2;
	private final ModelPart tail3;
	private final ModelPart lclaw2;
	private final ModelPart lclaw3;
	private final ModelPart lclaw4;
	private final ModelPart lclaw5;
	private final ModelPart lclaw6;
	private final ModelPart lclaw7;
	private final ModelPart tail4;
	private final ModelPart tail5;
	private final ModelPart tail6;
	private final ModelPart tail7;
	private final ModelPart tail8;
	private final ModelPart tail9;
	private final ModelPart tailpoint1;
	private final ModelPart tailpoint2;
	private final ModelPart llegspike;
	private final ModelPart tailspike1;
	private final ModelPart tailspike2;
	private final ModelPart tailspike3;
	private final ModelPart tailspike4;
	private final ModelPart tailspike5;
	private final ModelPart tailspike6;
	private final ModelPart neck1;
	private final ModelPart neck2;
	private final ModelPart neck3;
	private final ModelPart head1;
	private final ModelPart leye;
	private final ModelPart reye;
	private final ModelPart head2;
	private final ModelPart head3;
	private final ModelPart head4;
	private final ModelPart head5;
	private final ModelPart head6;
	private final ModelPart jaw1;
	private final ModelPart jaw2;
	private final ModelPart jaw3;
	private final ModelPart jaw4;
	private final ModelPart tooth1;
	private final ModelPart tooth2;
	private final ModelPart tooth3;
	private final ModelPart tooth4;
	private final ModelPart tooth5;
	private final ModelPart jaw5;
	private final ModelPart head7;
	private final ModelPart tooth6;
	private final ModelPart tooth7;
	private final ModelPart tooth8;
	private final ModelPart tooth9;
	private final ModelPart tooth10;
	private final ModelPart tooth11;
	private final ModelPart tooth12;
	private final ModelPart tooth13;
	private final ModelPart rightleg1;
	private final ModelPart rightleg2;
	private final ModelPart tooth14;
	private final ModelPart tooth15;
	private final ModelPart tooth16;
	private final ModelPart tooth17;
	private final ModelPart tooth18;
	private final ModelPart tooth19;
	private final ModelPart tooth20;
	private final ModelPart tooth21;
	private final ModelPart tooth22;
	private final ModelPart tooth23;
	private final ModelPart rightleg3;
	private final ModelPart llegspike2;
	private final ModelPart rclaw2;
	private final ModelPart rclaw4;
	private final ModelPart rclaw1;
	private final ModelPart rclaw5;
	private final ModelPart rclaw7;
	private final ModelPart rclaw3;
	private final ModelPart rclaw6;
	private final ModelPart wing1;
	private final ModelPart wing2;
	private final ModelPart wing3;
	private final ModelPart mem1;
	private final ModelPart mem2;
	private final ModelPart mem3;
	private final ModelPart wingclaw1;
	private final ModelPart wingclaw2;
	private final ModelPart wingclaw3;
	private final ModelPart lshoulder;
	private final ModelPart rshoulder;
	private final ModelPart rwing1;
	private final ModelPart rmem1;
	private final ModelPart rwing2;
	private final ModelPart rmem2;
	private final ModelPart rwing3;
	private final ModelPart rmem3;
	private final ModelPart rwingclaw1;
	private final ModelPart rwingclaw2;
	private final ModelPart rwingclaw3;
	private final float wingspeed;

	public PitchBlackModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.lclaw1 = root.getChild("lclaw1");
		this.body = root.getChild("body");
		this.leftleg1 = root.getChild("leftleg1");
		this.tail1 = root.getChild("tail1");
		this.leftleg2 = root.getChild("leftleg2");
		this.body2 = root.getChild("body2");
		this.leftleg3 = root.getChild("leftleg3");
		this.tail2 = root.getChild("tail2");
		this.tail3 = root.getChild("tail3");
		this.lclaw2 = root.getChild("lclaw2");
		this.lclaw3 = root.getChild("lclaw3");
		this.lclaw4 = root.getChild("lclaw4");
		this.lclaw5 = root.getChild("lclaw5");
		this.lclaw6 = root.getChild("lclaw6");
		this.lclaw7 = root.getChild("lclaw7");
		this.tail4 = root.getChild("tail4");
		this.tail5 = root.getChild("tail5");
		this.tail6 = root.getChild("tail6");
		this.tail7 = root.getChild("tail7");
		this.tail8 = root.getChild("tail8");
		this.tail9 = root.getChild("tail9");
		this.tailpoint1 = root.getChild("tailpoint1");
		this.tailpoint2 = root.getChild("tailpoint2");
		this.llegspike = root.getChild("llegspike");
		this.tailspike1 = root.getChild("tailspike1");
		this.tailspike2 = root.getChild("tailspike2");
		this.tailspike3 = root.getChild("tailspike3");
		this.tailspike4 = root.getChild("tailspike4");
		this.tailspike5 = root.getChild("tailspike5");
		this.tailspike6 = root.getChild("tailspike6");
		this.neck1 = root.getChild("neck1");
		this.neck2 = root.getChild("neck2");
		this.neck3 = root.getChild("neck3");
		this.head1 = root.getChild("head1");
		this.leye = root.getChild("leye");
		this.reye = root.getChild("reye");
		this.head2 = root.getChild("head2");
		this.head3 = root.getChild("head3");
		this.head4 = root.getChild("head4");
		this.head5 = root.getChild("head5");
		this.head6 = root.getChild("head6");
		this.jaw1 = root.getChild("jaw1");
		this.jaw2 = root.getChild("jaw2");
		this.jaw3 = root.getChild("jaw3");
		this.jaw4 = root.getChild("jaw4");
		this.tooth1 = root.getChild("tooth1");
		this.tooth2 = root.getChild("tooth2");
		this.tooth3 = root.getChild("tooth3");
		this.tooth4 = root.getChild("tooth4");
		this.tooth5 = root.getChild("tooth5");
		this.jaw5 = root.getChild("jaw5");
		this.head7 = root.getChild("head7");
		this.tooth6 = root.getChild("tooth6");
		this.tooth7 = root.getChild("tooth7");
		this.tooth8 = root.getChild("tooth8");
		this.tooth9 = root.getChild("tooth9");
		this.tooth10 = root.getChild("tooth10");
		this.tooth11 = root.getChild("tooth11");
		this.tooth12 = root.getChild("tooth12");
		this.tooth13 = root.getChild("tooth13");
		this.rightleg1 = root.getChild("rightleg1");
		this.rightleg2 = root.getChild("rightleg2");
		this.tooth14 = root.getChild("tooth14");
		this.tooth15 = root.getChild("tooth15");
		this.tooth16 = root.getChild("tooth16");
		this.tooth17 = root.getChild("tooth17");
		this.tooth18 = root.getChild("tooth18");
		this.tooth19 = root.getChild("tooth19");
		this.tooth20 = root.getChild("tooth20");
		this.tooth21 = root.getChild("tooth21");
		this.tooth22 = root.getChild("tooth22");
		this.tooth23 = root.getChild("tooth23");
		this.rightleg3 = root.getChild("rightleg3");
		this.llegspike2 = root.getChild("llegspike2");
		this.rclaw2 = root.getChild("rclaw2");
		this.rclaw4 = root.getChild("rclaw4");
		this.rclaw1 = root.getChild("rclaw1");
		this.rclaw5 = root.getChild("rclaw5");
		this.rclaw7 = root.getChild("rclaw7");
		this.rclaw3 = root.getChild("rclaw3");
		this.rclaw6 = root.getChild("rclaw6");
		this.wing1 = root.getChild("wing1");
		this.wing2 = root.getChild("wing2");
		this.wing3 = root.getChild("wing3");
		this.mem1 = root.getChild("mem1");
		this.mem2 = root.getChild("mem2");
		this.mem3 = root.getChild("mem3");
		this.wingclaw1 = root.getChild("wingclaw1");
		this.wingclaw2 = root.getChild("wingclaw2");
		this.wingclaw3 = root.getChild("wingclaw3");
		this.lshoulder = root.getChild("lshoulder");
		this.rshoulder = root.getChild("rshoulder");
		this.rwing1 = root.getChild("rwing1");
		this.rmem1 = root.getChild("rmem1");
		this.rwing2 = root.getChild("rwing2");
		this.rmem2 = root.getChild("rmem2");
		this.rwing3 = root.getChild("rwing3");
		this.rmem3 = root.getChild("rmem3");
		this.rwingclaw1 = root.getChild("rwingclaw1");
		this.rwingclaw2 = root.getChild("rwingclaw2");
		this.rwingclaw3 = root.getChild("rwingclaw3");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelPitchBlack");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		// state.flag: flying (PitchBlack.getActivity() != 0); state.scale: its size (getPitchBlackScale());
		// state.value: 1 while the idle jaw is snapping (OreSpawn's RenderInfo roll, done by PitchBlack.jawBits).
		float newangle = 0.0F;
		float lspeed = 0.0F;
		float tailspeed = 0.76F;
		float tailamp = 0.25F;
		float pi4 = (float) (Math.PI / 4);
		float pscale = state.scale > 0.0F ? state.scale : 1.0F;
		if (state.flag) {
		newangle = Mth.cos(f2 * 0.45F * this.wingspeed / pscale) * (float) Math.PI * 0.24F;
		} else {
		newangle = -pi4 + Mth.cos(f2 * 0.05F * this.wingspeed / pscale) * (float) Math.PI * 0.02F;
		}
		this.wing1.zRot = newangle;
		this.mem1.zRot = newangle;
		this.wing2.zRot = newangle * 5.0F / 3.0F;
		this.wing2.y = this.wing1.y + (float)Math.sin(this.wing1.zRot) * 21.0F;
		this.wing2.x = this.wing1.x + (float)Math.cos(this.wing1.zRot) * 21.0F;
		this.mem2.zRot = newangle * 5.0F / 3.0F;
		this.mem2.y = this.wing2.y;
		this.mem2.x = this.wing2.x;
		this.wing3.zRot = newangle * 2.0F;
		this.wing3.y = this.wing2.y + (float)Math.sin(this.wing2.zRot) * 43.0F;
		this.wing3.x = this.wing2.x + (float)Math.cos(this.wing2.zRot) * 43.0F;
		this.mem3.zRot = newangle * 2.0F;
		this.mem3.y = this.wing3.y;
		this.mem3.x = this.wing3.x;
		this.wingclaw1.zRot = this.wingclaw2.zRot = this.wingclaw3.zRot = newangle * 3.0F / 2.0F;
		this.wingclaw1.y = this.wingclaw2.y = this.wingclaw3.y = this.wing3.y;
		this.wingclaw1.x = this.wingclaw2.x = this.wingclaw3.x = this.wing3.x;
		this.rwing1.zRot = -newangle;
		this.rmem1.zRot = -newangle;
		this.rwing2.zRot = -newangle * 5.0F / 3.0F;
		this.rwing2.y = this.rwing1.y - (float)Math.sin(this.rwing1.zRot) * 21.0F;
		this.rwing2.x = this.rwing1.x - (float)Math.cos(this.rwing1.zRot) * 21.0F;
		this.rmem2.zRot = -newangle * 5.0F / 3.0F;
		this.rmem2.y = this.rwing2.y;
		this.rmem2.x = this.rwing2.x;
		this.rwing3.zRot = -newangle * 2.0F;
		this.rwing3.y = this.rwing2.y - (float)Math.sin(this.rwing2.zRot) * 43.0F;
		this.rwing3.x = this.rwing2.x - (float)Math.cos(this.rwing2.zRot) * 43.0F;
		this.rmem3.zRot = -newangle * 2.0F;
		this.rmem3.y = this.rwing3.y;
		this.rmem3.x = this.rwing3.x;
		this.rwingclaw1.zRot = this.rwingclaw2.zRot = this.rwingclaw3.zRot = -newangle * 3.0F / 2.0F;
		this.rwingclaw1.y = this.rwingclaw2.y = this.rwingclaw3.y = this.rwing3.y;
		this.rwingclaw1.x = this.rwingclaw2.x = this.rwingclaw3.x = this.rwing3.x;
		f3 %= 360.0F;
		if (state.flag) {
		float var23;
		f3 = var23 = f3 * 0.2F;
		} else {
		float var25;
		f3 = var25 = f3 * 0.55F;
		}
		this.neck3.yRot = (float)Math.toRadians(f3) * 0.5F;
		this.head1.yRot = this.head2.yRot = this.head3.yRot = this.head4.yRot = (float)Math.toRadians(f3);
		this.head5.yRot = this.head6.yRot = this.head7.yRot = this.head1.yRot;
		this.jaw1.yRot = this.jaw2.yRot = this.jaw3.yRot = this.jaw4.yRot = this.jaw5.yRot = this.head1.yRot;
		this.tooth1.yRot = this.tooth2.yRot = this.tooth3.yRot = this.tooth4.yRot = this.tooth5.yRot = this.head1.yRot;
		this.tooth6.yRot = this.tooth7.yRot = this.tooth8.yRot = this.tooth9.yRot = this.tooth10.yRot = this.head1.yRot;
		this.tooth11.yRot = this.tooth12.yRot = this.tooth13.yRot = this.tooth14.yRot = this.tooth15.yRot = this.head1
		.yRot;
		this.tooth16.yRot = this.tooth17.yRot = this.tooth18.yRot = this.tooth19.yRot = this.tooth20.yRot = this.head1
		.yRot;
		this.tooth21.yRot = this.tooth22.yRot = this.tooth23.yRot = this.head1.yRot;
		this.reye.yRot = this.leye.yRot = this.head1.yRot;
		if (state.attacking) {
		newangle = Mth.cos(f2 * 0.85F * this.wingspeed) * (float) Math.PI * 0.16F;
		newangle += 0.5F;
		} else {
		if (state.value != 0.0F) {
		newangle = Mth.sin(f2 * 0.85F * this.wingspeed) * (float) Math.PI * 0.16F;
		newangle += 0.5F;
		} else {
		newangle = pi4 / 4.0F;
		}
		}
		this.jaw1.xRot = this.jaw2.xRot = this.jaw3.xRot = this.jaw4.xRot = this.jaw5.xRot = newangle;
		this.tooth14.xRot = this.tooth15.xRot = newangle;
		this.tooth16.xRot = this.tooth17.xRot = this.tooth18.xRot = this.tooth19.xRot = this.tooth20.xRot = newangle;
		this.tooth21.xRot = this.tooth22.xRot = this.tooth23.xRot = newangle;
		float clawZ = 7.0F;
		float clawY = 21.0F;
		float clawZamp = 12.0F * pscale;
		float clawYamp = 6.0F * pscale;
		if (!state.flag) {
		float t1 = 0.0F;
		float t2 = 0.0F;
		if (f1 > 0.001) {
		newangle = Mth.cos(f2 * 0.75F * this.wingspeed / pscale);
		t1 = Mth.sin(f2 * 0.75F * this.wingspeed / pscale);
		} else {
		newangle = 0.0F;
		t1 = 0.0F;
		t2 = 0.0F;
		}
		if (t1 > 0.0F) {
		t2 = t1 * clawYamp * f1;
		this.lclaw1.y = clawY - t2;
		} else {
		this.lclaw1.y = clawY;
		}
		this.lclaw1.z = clawZ + clawZamp * newangle * f1;
		this.lclaw2.z = this.lclaw3.z = this.lclaw4.z = this.lclaw5.z = this.lclaw6.z = this.lclaw7
		.z = this.lclaw1.z;
		this.lclaw2.y = this.lclaw3.y = this.lclaw4.y = this.lclaw5.y = this.lclaw6.y = this.lclaw7
		.y = this.lclaw1.y;
		this.llegspike.z = this.leftleg3.z = this.lclaw1.z;
		this.llegspike.y = this.leftleg3.y = this.lclaw1.y;
		this.leftleg3.xRot = -0.61F + newangle * (float) Math.PI * 0.18F * f1;
		this.llegspike.xRot = -0.785F + newangle * (float) Math.PI * 0.18F * f1;
		this.leftleg1.xRot = -0.576F + newangle * (float) Math.PI * 0.18F * f1;
		this.leftleg2.xRot = 0.977F + newangle * (float) Math.PI * 0.18F * f1;
		this.leftleg1.y = this.leftleg2.y = this.leftleg3.y
		- (float)Math.cos(this.leftleg3.xRot) * 17.0F
		+ t2 / 2.0F;
		this.leftleg1.z = this.leftleg2.z = this.leftleg3.z - (float)Math.sin(this.leftleg3.xRot) * 17.0F;
		t1 = 0.0F;
		t2 = 0.0F;
		if (f1 > 0.001) {
		newangle = Mth.cos(f2 * 0.75F * this.wingspeed / pscale + pi4 * 4.0F);
		t1 = Mth.sin(f2 * 0.75F * this.wingspeed / pscale + pi4 * 4.0F);
		} else {
		newangle = 0.0F;
		t1 = 0.0F;
		t2 = 0.0F;
		}
		if (t1 > 0.0F) {
		t2 = t1 * clawYamp * f1;
		this.rclaw1.y = clawY - t2;
		} else {
		this.rclaw1.y = clawY;
		}
		this.rclaw1.z = clawZ + clawZamp * newangle * f1;
		this.rclaw2.z = this.rclaw3.z = this.rclaw4.z = this.rclaw5.z = this.rclaw6.z = this.rclaw7
		.z = this.rclaw1.z;
		this.rclaw2.y = this.rclaw3.y = this.rclaw4.y = this.rclaw5.y = this.rclaw6.y = this.rclaw7
		.y = this.rclaw1.y;
		this.llegspike2.z = this.rightleg3.z = this.rclaw1.z;
		this.llegspike2.y = this.rightleg3.y = this.rclaw1.y;
		this.rightleg3.xRot = -0.61F + newangle * (float) Math.PI * 0.18F * f1;
		this.llegspike2.xRot = -0.785F + newangle * (float) Math.PI * 0.18F * f1;
		this.rightleg1.xRot = -0.576F + newangle * (float) Math.PI * 0.18F * f1;
		this.rightleg2.xRot = 0.977F + newangle * (float) Math.PI * 0.18F * f1;
		this.rightleg1.y = this.rightleg2.y = this.rightleg3.y
		- (float)Math.cos(this.rightleg3.xRot) * 17.0F
		+ t2 / 2.0F;
		this.rightleg1.z = this.rightleg2.z = this.rightleg3.z - (float)Math.sin(this.rightleg3.xRot) * 17.0F;
		this.lclaw2.xRot = this.lclaw3.xRot = this.lclaw4.xRot = this.lclaw5.xRot = this.lclaw6.xRot = this.lclaw7
		.xRot = this.lclaw1.xRot = 0.0F;
		this.rclaw2.xRot = this.rclaw3.xRot = this.rclaw4.xRot = this.rclaw5.xRot = this.rclaw6.xRot = this.rclaw7
		.xRot = this.rclaw1.xRot = 0.0F;
		} else {
		clawZ = 7.0F;
		clawY = 9.0F;
		if (state.attacking) {
		newangle = Mth.cos(f2 * 0.85F * this.wingspeed / pscale) * 0.2F;
		} else {
		newangle = 0.0F;
		}
		this.lclaw1.z = clawZ;
		this.lclaw1.y = clawY + newangle * 30.0F;
		this.lclaw1.xRot = -0.7F + newangle;
		this.lclaw2.z = this.lclaw3.z = this.lclaw4.z = this.lclaw5.z = this.lclaw6.z = this.lclaw7
		.z = this.lclaw1.z;
		this.lclaw2.y = this.lclaw3.y = this.lclaw4.y = this.lclaw5.y = this.lclaw6.y = this.lclaw7
		.y = this.lclaw1.y;
		this.lclaw2.xRot = this.lclaw3.xRot = this.lclaw4.xRot = this.lclaw5.xRot = this.lclaw6.xRot = this.lclaw7
		.xRot = this.lclaw1.xRot;
		this.llegspike.z = this.leftleg3.z = this.lclaw1.z;
		this.llegspike.y = this.leftleg3.y = this.lclaw1.y;
		this.leftleg3.xRot = -0.61F + this.lclaw1.xRot;
		this.llegspike.xRot = -0.785F + this.lclaw1.xRot;
		this.leftleg1.xRot = -0.576F - this.lclaw1.xRot / 4.0F;
		this.leftleg2.xRot = 0.977F - this.lclaw1.xRot / 4.0F;
		this.leftleg1.y = this.leftleg2.y = this.leftleg3.y - (float)Math.cos(this.leftleg3.xRot) * 17.0F;
		this.leftleg1.z = this.leftleg2.z = this.leftleg3.z - (float)Math.sin(this.leftleg3.xRot) * 17.0F;
		this.rclaw1.z = clawZ;
		this.rclaw1.y = clawY - newangle * 30.0F;
		this.rclaw1.xRot = -0.7F - newangle;
		this.rclaw2.z = this.rclaw3.z = this.rclaw4.z = this.rclaw5.z = this.rclaw6.z = this.rclaw7
		.z = this.rclaw1.z;
		this.rclaw2.y = this.rclaw3.y = this.rclaw4.y = this.rclaw5.y = this.rclaw6.y = this.rclaw7
		.y = this.rclaw1.y;
		this.rclaw2.xRot = this.rclaw3.xRot = this.rclaw4.xRot = this.rclaw5.xRot = this.rclaw6.xRot = this.rclaw7
		.xRot = this.rclaw1.xRot;
		this.llegspike2.z = this.rightleg3.z = this.rclaw1.z;
		this.llegspike2.y = this.rightleg3.y = this.rclaw1.y;
		this.rightleg3.xRot = -0.61F + this.rclaw1.xRot;
		this.llegspike2.xRot = -0.785F + this.rclaw1.xRot;
		this.rightleg1.xRot = -0.576F - this.rclaw1.xRot / 4.0F;
		this.rightleg2.xRot = 0.977F - this.rclaw1.xRot / 4.0F;
		this.rightleg1.y = this.rightleg2.y = this.rightleg3.y - (float)Math.cos(this.rightleg3.xRot) * 17.0F;
		this.rightleg1.z = this.rightleg2.z = this.rightleg3.z - (float)Math.sin(this.rightleg3.xRot) * 17.0F;
		}
		if (state.attacking) {
		tailspeed = 0.76F / pscale;
		tailamp = 0.25F;
		} else {
		tailspeed = 0.26F / pscale;
		tailamp = 0.08F;
		}
		this.tail1.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * (float) Math.PI * tailamp / 2.0F;
		this.tail2.z = this.tail1.z + (float)Math.cos(this.tail1.yRot) * 11.0F;
		this.tail2.x = this.tail1.x - 1.0F + (float)Math.sin(this.tail1.yRot) * 11.0F;
		this.tail2.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - pi4) * (float) Math.PI * tailamp;
		this.tail3.z = this.tail2.z + (float)Math.cos(this.tail2.yRot) * 9.0F;
		this.tail3.x = this.tail2.x + (float)Math.sin(this.tail2.yRot) * 9.0F;
		this.tail3.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - 2.0F * pi4) * (float) Math.PI * tailamp;
		this.tail4.z = this.tail3.z + (float)Math.cos(this.tail3.yRot) * 9.0F;
		this.tail4.x = this.tail3.x + (float)Math.sin(this.tail3.yRot) * 9.0F;
		this.tail4.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - 3.0F * pi4) * (float) Math.PI * tailamp;
		this.tail5.z = this.tail4.z + (float)Math.cos(this.tail4.yRot) * 9.0F;
		this.tail5.x = this.tail4.x + (float)Math.sin(this.tail4.yRot) * 9.0F;
		newangle = Mth.cos(f2 * tailspeed * this.wingspeed - 3.0F * pi4) * (float) Math.PI * tailamp;
		newangle /= 2.0F;
		this.tail5.yRot = this.tail4.yRot + newangle;
		this.tail6.z = this.tail5.z + (float)Math.cos(this.tail5.yRot) * 9.0F;
		this.tail6.x = this.tail5.x + (float)Math.sin(this.tail5.yRot) * 9.0F;
		this.tail6.yRot = 0.174F + this.tail5.yRot + newangle;
		this.tailspike2.z = this.tailspike3.z = this.tail6.z;
		this.tailspike2.x = this.tailspike3.x = this.tail6.x;
		this.tailspike2.yRot = this.tailspike3.yRot = this.tail6.yRot;
		this.tail9.z = this.tail6.z + (float)Math.cos(this.tail6.yRot) * 9.0F;
		this.tail9.x = this.tail6.x + (float)Math.sin(this.tail6.yRot) * 9.0F;
		this.tail9.yRot = this.tail6.yRot + newangle;
		this.tailspike1.z = this.tail9.z;
		this.tailspike1.x = this.tail9.x;
		this.tailspike1.yRot = this.tail9.yRot;
		this.tailpoint1.z = this.tail9.z + (float)Math.cos(this.tail9.yRot) * 9.0F;
		this.tailpoint1.x = this.tail9.x + (float)Math.sin(this.tail9.yRot) * 9.0F;
		this.tailpoint1.yRot = this.tail9.yRot + newangle;
		this.tail7.z = this.tail5.z + (float)Math.cos(this.tail5.yRot) * 9.0F;
		this.tail7.x = this.tail5.x + (float)Math.sin(this.tail5.yRot) * 9.0F;
		this.tail7.yRot = -0.174F + this.tail5.yRot + newangle;
		this.tailspike5.z = this.tailspike6.z = this.tail7.z;
		this.tailspike5.x = this.tailspike6.x = this.tail7.x;
		this.tailspike5.yRot = this.tailspike6.yRot = this.tail7.yRot;
		this.tail8.z = this.tail7.z + (float)Math.cos(this.tail7.yRot) * 9.0F;
		this.tail8.x = this.tail7.x + (float)Math.sin(this.tail7.yRot) * 9.0F;
		this.tail8.yRot = this.tail7.yRot + newangle;
		this.tailspike4.z = this.tail8.z;
		this.tailspike4.x = this.tail8.x;
		this.tailspike4.yRot = this.tail8.yRot;
		this.tailpoint2.z = this.tail8.z + (float)Math.cos(this.tail8.yRot) * 9.0F;
		this.tailpoint2.x = this.tail8.x + (float)Math.sin(this.tail8.yRot) * 9.0F;
		this.tailpoint2.yRot = this.tail8.yRot + newangle;
	}
}
