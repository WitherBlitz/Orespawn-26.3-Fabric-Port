package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelAlien (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class AlienModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart torso;
	private final ModelPart stomach;
	private final ModelPart rThigh;
	private final ModelPart lThigh;
	private final ModelPart lShin;
	private final ModelPart rShin;
	private final ModelPart lShin1;
	private final ModelPart rShin1;
	private final ModelPart lFoot;
	private final ModelPart rFoot;
	private final ModelPart neck;
	private final ModelPart fan;
	private final ModelPart tail2;
	private final ModelPart tail3;
	private final ModelPart tail4;
	private final ModelPart tail5;
	private final ModelPart tail1;
	private final ModelPart fanl1;
	private final ModelPart fanr1;
	private final ModelPart fanl2;
	private final ModelPart fanr2;
	private final ModelPart fanl3;
	private final ModelPart fanr3;
	private final ModelPart fanl4;
	private final ModelPart fanr4;
	private final ModelPart fanl5;
	private final ModelPart fanr5;
	private final ModelPart fanl6;
	private final ModelPart fanr6;
	private final ModelPart spike4;
	private final ModelPart spike5;
	private final ModelPart spike3;
	private final ModelPart fanl7;
	private final ModelPart fanr7;
	private final ModelPart head;
	private final ModelPart head1;
	private final ModelPart jaw1;
	private final ModelPart head2;
	private final ModelPart jaw2;
	private final ModelPart fang1;
	private final ModelPart fang2;
	private final ModelPart fang3;
	private final ModelPart fang4;
	private final ModelPart spike2;
	private final ModelPart spike1;
	private final ModelPart arml1;
	private final ModelPart armr1;
	private final ModelPart arml2;
	private final ModelPart armr2;
	private final ModelPart clawr1;
	private final ModelPart clawr2;
	private final ModelPart clawr3;
	private final ModelPart clawl2;
	private final ModelPart clawl3;
	private final ModelPart clawl1;
	private final float wingspeed;

	public AlienModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.torso = root.getChild("torso");
		this.stomach = root.getChild("stomach");
		this.rThigh = root.getChild("rthigh");
		this.lThigh = root.getChild("lthigh");
		this.lShin = root.getChild("lshin");
		this.rShin = root.getChild("rshin");
		this.lShin1 = root.getChild("lshin1");
		this.rShin1 = root.getChild("rshin1");
		this.lFoot = root.getChild("lfoot");
		this.rFoot = root.getChild("rfoot");
		this.neck = root.getChild("neck");
		this.fan = root.getChild("fan");
		this.tail2 = root.getChild("tail2");
		this.tail3 = root.getChild("tail3");
		this.tail4 = root.getChild("tail4");
		this.tail5 = root.getChild("tail5");
		this.tail1 = root.getChild("tail1");
		this.fanl1 = root.getChild("fanl1");
		this.fanr1 = root.getChild("fanr1");
		this.fanl2 = root.getChild("fanl2");
		this.fanr2 = root.getChild("fanr2");
		this.fanl3 = root.getChild("fanl3");
		this.fanr3 = root.getChild("fanr3");
		this.fanl4 = root.getChild("fanl4");
		this.fanr4 = root.getChild("fanr4");
		this.fanl5 = root.getChild("fanl5");
		this.fanr5 = root.getChild("fanr5");
		this.fanl6 = root.getChild("fanl6");
		this.fanr6 = root.getChild("fanr6");
		this.spike4 = root.getChild("spike4");
		this.spike5 = root.getChild("spike5");
		this.spike3 = root.getChild("spike3");
		this.fanl7 = root.getChild("fanl7");
		this.fanr7 = root.getChild("fanr7");
		this.head = root.getChild("head");
		this.head1 = root.getChild("head1");
		this.jaw1 = root.getChild("jaw1");
		this.head2 = root.getChild("head2");
		this.jaw2 = root.getChild("jaw2");
		this.fang1 = root.getChild("fang1");
		this.fang2 = root.getChild("fang2");
		this.fang3 = root.getChild("fang3");
		this.fang4 = root.getChild("fang4");
		this.spike2 = root.getChild("spike2");
		this.spike1 = root.getChild("spike1");
		this.arml1 = root.getChild("arml1");
		this.armr1 = root.getChild("armr1");
		this.arml2 = root.getChild("arml2");
		this.armr2 = root.getChild("armr2");
		this.clawr1 = root.getChild("clawr1");
		this.clawr2 = root.getChild("clawr2");
		this.clawr3 = root.getChild("clawr3");
		this.clawl2 = root.getChild("clawl2");
		this.clawl3 = root.getChild("clawl3");
		this.clawl1 = root.getChild("clawl1");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelAlien");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		// state.value: OreSpawn's per-creature RenderInfo rolls (ri1 + 16 * ri2 + 256 * ri3), re-rolled by Alien.animBits
		// each time the jaw swings back.
		int bits = (int) state.value;
		int ri1 = bits & 15, ri2 = (bits >> 4) & 15, ri3 = (bits >> 8) & 15;
		float newangle = 0.0F;
		float nextangle = 0.0F;
		newangle = Mth.cos(f2 * 4.0F * this.wingspeed) * (float) Math.PI * 0.5F * f1;
		this.doLeftLeg(newangle);
		this.doRightLeg(-newangle);
		if (!state.attacking) {
		this.fan.zRot = 0.0F;
		this.fanl1.zRot = 0.0F;
		this.fanl2.zRot = 0.0F;
		this.fanl3.zRot = 0.0F;
		this.fanl4.zRot = 0.0F;
		this.fanl5.zRot = 0.0F;
		this.fanl6.zRot = 0.0F;
		this.fanl7.zRot = 0.0F;
		this.fanr1.zRot = 0.0F;
		this.fanr2.zRot = 0.0F;
		this.fanr3.zRot = 0.0F;
		this.fanr4.zRot = 0.0F;
		this.fanr5.zRot = 0.0F;
		this.fanr6.zRot = 0.0F;
		this.fanr7.zRot = 0.0F;
		this.fan.xRot = -1.85F;
		this.fanl1.xRot = -1.85F;
		this.fanl2.xRot = -1.85F;
		this.fanl3.xRot = -1.85F;
		this.fanl4.xRot = -1.85F;
		this.fanl5.xRot = -1.85F;
		this.fanl6.xRot = -1.85F;
		this.fanl7.xRot = -1.85F;
		this.fanr1.xRot = -1.85F;
		this.fanr2.xRot = -1.85F;
		this.fanr3.xRot = -1.85F;
		this.fanr4.xRot = -1.85F;
		this.fanr5.xRot = -1.85F;
		this.fanr6.xRot = -1.85F;
		this.fanr7.xRot = -1.85F;
		} else {
		float pi6 = (float) (Math.PI / 6);
		float fanspeed = 1.22F;
		float fanamp = 0.1F;
		this.fan.xRot = Mth.cos(f2 * fanspeed * this.wingspeed) * (float) Math.PI * fanamp;
		this.fanl1.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 1.0F * pi6) * (float) Math.PI * fanamp;
		this.fanl2.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 2.0F * pi6) * (float) Math.PI * fanamp;
		this.fanl3.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 3.0F * pi6) * (float) Math.PI * fanamp;
		this.fanl4.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 4.0F * pi6) * (float) Math.PI * fanamp;
		this.fanl5.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 5.0F * pi6) * (float) Math.PI * fanamp;
		this.fanl6.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 6.0F * pi6) * (float) Math.PI * fanamp;
		this.fanl7.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 7.0F * pi6) * (float) Math.PI * fanamp;
		this.fanr1.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 1.0F * pi6) * (float) Math.PI * fanamp;
		this.fanr2.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 2.0F * pi6) * (float) Math.PI * fanamp;
		this.fanr3.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 3.0F * pi6) * (float) Math.PI * fanamp;
		this.fanr4.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 4.0F * pi6) * (float) Math.PI * fanamp;
		this.fanr5.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 5.0F * pi6) * (float) Math.PI * fanamp;
		this.fanr6.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 6.0F * pi6) * (float) Math.PI * fanamp;
		this.fanr7.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 7.0F * pi6) * (float) Math.PI * fanamp;
		this.fan.zRot = 0.0F;
		this.fanl1.zRot = 0.261F;
		this.fanl2.zRot = 0.523F;
		this.fanl3.zRot = 0.785F;
		this.fanl4.zRot = 1.047F;
		this.fanl5.zRot = 1.309F;
		this.fanl6.zRot = 1.571F;
		this.fanl7.zRot = 1.832F;
		this.fanr1.zRot = -0.261F;
		this.fanr2.zRot = -0.523F;
		this.fanr3.zRot = -0.785F;
		this.fanr4.zRot = -1.047F;
		this.fanr5.zRot = -1.309F;
		this.fanr6.zRot = -1.571F;
		this.fanr7.zRot = -1.832F;
		}
		this.neck.yRot = (float)Math.toRadians(f3) * 0.35F;
		this.head.yRot = (float)Math.toRadians(f3) * 0.75F;
		this.head.z = this.neck.z - (float)Math.cos(this.neck.yRot) * 3.0F;
		this.head.x = this.neck.x + (float)Math.sin(this.neck.yRot) * 3.0F;
		this.head1.yRot = this.head.yRot;
		this.head1.z = this.head.z;
		this.head1.x = this.head.x;
		this.head2.yRot = this.head.yRot;
		this.head2.z = this.head.z;
		this.head2.x = this.head.x;
		this.fang1.yRot = this.head.yRot;
		this.fang1.z = this.head.z;
		this.fang1.x = this.head.x;
		this.fang2.yRot = this.head.yRot;
		this.fang2.z = this.head.z;
		this.fang2.x = this.head.x;
		this.fang3.yRot = this.head.yRot;
		this.fang3.z = this.head.z;
		this.fang3.x = this.head.x;
		this.fang4.yRot = this.head.yRot;
		this.fang4.z = this.head.z;
		this.fang4.x = this.head.x;
		this.jaw1.yRot = this.head.yRot;
		this.jaw1.z = this.head.z - (float)Math.cos(this.head.yRot) * 8.0F;
		this.jaw1.x = this.head.x - (float)Math.sin(this.head.yRot) * 8.0F;
		this.jaw2.yRot = this.jaw1.yRot;
		this.jaw2.z = this.jaw1.z;
		this.jaw2.x = this.jaw1.x;
		newangle = Mth.cos(f2 * 3.5F * this.wingspeed) * (float) Math.PI * 0.5F;
		if (ri2 == 1) {
		this.doTail(newangle);
		} else {
		newangle = Mth.cos(f2 * this.wingspeed) * (float) Math.PI * 0.05F;
		this.doTail(newangle);
		}
		if (ri3 == 1) {
		newangle = Mth.cos(f2 * 3.5F * this.wingspeed) * (float) Math.PI * 0.35F;
		this.doJaw(newangle);
		} else {
		newangle = Mth.cos(f2 * this.wingspeed) * (float) Math.PI * 0.02F;
		this.doJaw(newangle);
		}
		newangle = Mth.cos(f2 * this.wingspeed * 3.5F) * (float) Math.PI * 0.2F;
		if (ri1 != 1 && ri1 != 3) {
		newangle = Mth.cos(f2 * this.wingspeed) * (float) Math.PI * 0.03F;
		this.doLeftClaw(newangle);
		} else {
		this.doLeftClaw(newangle);
		}
		if (ri1 != 2 && ri1 != 3) {
		newangle = Mth.cos(f2 * this.wingspeed) * (float) Math.PI * 0.03F;
		this.doRightClaw(-newangle);
		} else {
		this.doRightClaw(-newangle);
		}
	}

	private void doLeftLeg(float angle) {
		this.lFoot.xRot = angle;
		this.lShin.xRot = angle - 0.4F;
		this.lShin1.xRot = angle - 0.8F;
		this.lThigh.xRot = angle - 0.8F;
	}

	private void doRightLeg(float angle) {
		this.rFoot.xRot = angle;
		this.rShin.xRot = angle - 0.4F;
		this.rShin1.xRot = angle - 0.8F;
		this.rThigh.xRot = angle - 0.8F;
	}

	private void doJaw(float angle) {
		this.jaw1.xRot = Math.abs(angle);
		this.jaw2.xRot = this.jaw1.xRot;
	}

	private void doTail(float angle) {
		this.tail1.yRot = angle * 0.25F;
		this.spike1.yRot = this.tail1.yRot;
		this.tail2.yRot = angle * 0.5F;
		this.tail2.z = this.tail1.z + (float) Math.cos(this.tail1.yRot) * 10.0F;
		this.tail2.x = this.tail1.x + (float) Math.sin(this.tail1.yRot) * 10.0F;
		this.spike2.yRot = this.tail2.yRot;
		this.spike2.z = this.tail2.z;
		this.spike2.x = this.tail2.x;
		this.tail3.yRot = angle * 0.8F;
		this.tail3.z = this.tail2.z + (float) Math.cos(this.tail2.yRot) * 10.0F;
		this.tail3.x = this.tail2.x + (float) Math.sin(this.tail2.yRot) * 10.0F;
		this.spike3.yRot = this.tail3.yRot;
		this.spike3.z = this.tail3.z;
		this.spike3.x = this.tail3.x;
		this.tail4.yRot = angle * 1.25F;
		this.tail4.z = this.tail3.z + (float) Math.cos(this.tail3.yRot) * 10.0F;
		this.tail4.x = this.tail3.x + (float) Math.sin(this.tail3.yRot) * 10.0F;
		this.spike4.yRot = this.tail4.yRot + 0.52F;
		this.spike4.z = this.tail4.z;
		this.spike4.x = this.tail4.x;
		this.spike5.yRot = this.tail4.yRot - 0.52F;
		this.spike5.z = this.tail4.z;
		this.spike5.x = this.tail4.x;
		this.tail5.yRot = angle * 1.5F;
		this.tail5.z = this.tail4.z + (float) Math.cos(this.tail4.yRot) * 10.0F;
		this.tail5.x = this.tail4.x + (float) Math.sin(this.tail4.yRot) * 10.0F;
	}

	private void doLeftClaw(float angle) {
		this.arml1.yRot = -0.52F + Math.abs(angle * 2.0F);
		this.arml2.z = this.arml1.z - (float) Math.sin(this.arml1.yRot) * 9.0F;
		this.arml2.x = this.arml1.x + (float) Math.cos(this.arml1.yRot) * 9.0F;
		this.arml2.yRot = 0.855F + Math.abs(angle);
		this.clawl1.z = this.arml2.z - (float) Math.sin(this.arml2.yRot) * 14.0F;
		this.clawl1.x = this.arml2.x + (float) Math.cos(this.arml2.yRot) * 14.0F;
		this.clawl1.yRot = 2.7F + Math.abs(angle * 4.0F);
		this.clawl2.z = this.clawl1.z;
		this.clawl2.x = this.clawl1.x;
		this.clawl2.yRot = 2.27F + Math.abs(angle * 4.0F);
		this.clawl3.z = this.clawl1.z;
		this.clawl3.x = this.clawl1.x;
		this.clawl3.yRot = 2.7F + Math.abs(angle * 4.0F);
	}

	private void doRightClaw(float angle) {
		this.armr1.yRot = -2.61F - Math.abs(angle * 2.0F);
		this.armr2.z = this.armr1.z - (float) Math.sin(this.armr1.yRot) * 9.0F;
		this.armr2.x = this.armr1.x + (float) Math.cos(this.armr1.yRot) * 9.0F;
		this.armr2.yRot = 2.27F - Math.abs(angle);
		this.clawr1.z = this.armr2.z - (float) Math.sin(this.armr2.yRot) * 14.0F;
		this.clawr1.x = this.armr2.x + (float) Math.cos(this.armr2.yRot) * 14.0F;
		this.clawr1.yRot = 0.436F - Math.abs(angle * 4.0F);
		this.clawr2.z = this.clawr1.z;
		this.clawr2.x = this.clawr1.x;
		this.clawr2.yRot = 0.87F - Math.abs(angle * 4.0F);
		this.clawr3.z = this.clawr1.z;
		this.clawr3.x = this.clawr1.x;
		this.clawr3.yRot = 0.436F - Math.abs(angle * 4.0F);
	}
}
