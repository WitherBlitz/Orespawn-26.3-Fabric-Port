package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelMolenoid (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class MolenoidModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart shoulders;
	private final ModelPart head1;
	private final ModelPart head2;
	private final ModelPart head3;
	private final ModelPart nosestar1;
	private final ModelPart nosestar2;
	private final ModelPart nosestar3;
	private final ModelPart larm;
	private final ModelPart lhand;
	private final ModelPart lclaw1;
	private final ModelPart lclaw2;
	private final ModelPart lclaw3;
	private final ModelPart lclaw4;
	private final ModelPart nosestar4;
	private final ModelPart nosestar5;
	private final ModelPart nosestar6;
	private final ModelPart butt;
	private final ModelPart tail;
	private final ModelPart lleg;
	private final ModelPart lfoot;
	private final ModelPart ltoe1;
	private final ModelPart ltoe2;
	private final ModelPart ltoe3;
	private final ModelPart ltoe4;
	private final ModelPart rarm;
	private final ModelPart rhand;
	private final ModelPart rclaw1;
	private final ModelPart rclaw2;
	private final ModelPart rclaw3;
	private final ModelPart rclaw4;
	private final ModelPart rleg;
	private final ModelPart rfoot;
	private final ModelPart rtoe1;
	private final ModelPart rtoe2;
	private final ModelPart rtoe3;
	private final ModelPart rtoe4;
	private final float wingspeed;

	public MolenoidModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.body = root.getChild("body");
		this.shoulders = root.getChild("shoulders");
		this.head1 = root.getChild("head1");
		this.head2 = root.getChild("head2");
		this.head3 = root.getChild("head3");
		this.nosestar1 = root.getChild("nosestar1");
		this.nosestar2 = root.getChild("nosestar2");
		this.nosestar3 = root.getChild("nosestar3");
		this.larm = root.getChild("larm");
		this.lhand = root.getChild("lhand");
		this.lclaw1 = root.getChild("lclaw1");
		this.lclaw2 = root.getChild("lclaw2");
		this.lclaw3 = root.getChild("lclaw3");
		this.lclaw4 = root.getChild("lclaw4");
		this.nosestar4 = root.getChild("nosestar4");
		this.nosestar5 = root.getChild("nosestar5");
		this.nosestar6 = root.getChild("nosestar6");
		this.butt = root.getChild("butt");
		this.tail = root.getChild("tail");
		this.lleg = root.getChild("lleg");
		this.lfoot = root.getChild("lfoot");
		this.ltoe1 = root.getChild("ltoe1");
		this.ltoe2 = root.getChild("ltoe2");
		this.ltoe3 = root.getChild("ltoe3");
		this.ltoe4 = root.getChild("ltoe4");
		this.rarm = root.getChild("rarm");
		this.rhand = root.getChild("rhand");
		this.rclaw1 = root.getChild("rclaw1");
		this.rclaw2 = root.getChild("rclaw2");
		this.rclaw3 = root.getChild("rclaw3");
		this.rclaw4 = root.getChild("rclaw4");
		this.rleg = root.getChild("rleg");
		this.rfoot = root.getChild("rfoot");
		this.rtoe1 = root.getChild("rtoe1");
		this.rtoe2 = root.getChild("rtoe2");
		this.rtoe3 = root.getChild("rtoe3");
		this.rtoe4 = root.getChild("rtoe4");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelMolenoid");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		if (state.attacking) {
		newangle = Mth.cos(f2 * 1.7F * this.wingspeed) * (float) Math.PI * 0.25F;
		} else if (f1 > 0.1F) {
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.25F * f1;
		} else {
		newangle = 0.0F;
		}
		this.larm.yRot = newangle + 0.628F;
		this.lhand.z = this.larm.z - (float)Math.sin(this.larm.yRot) * 15.0F;
		this.lhand.x = this.larm.x + (float)Math.cos(this.larm.yRot) * 15.0F;
		this.lhand.yRot = newangle * 1.25F;
		this.lclaw1.z = this.lhand.z - (float)Math.sin(this.lhand.yRot) * 10.0F;
		this.lclaw1.x = this.lhand.x + (float)Math.cos(this.lhand.yRot) * 10.0F;
		this.lclaw1.yRot = newangle * 1.5F - 0.174F;
		this.lclaw2.z = this.lclaw1.z;
		this.lclaw2.x = this.lclaw1.x;
		this.lclaw2.yRot = this.lclaw1.yRot;
		this.lclaw3.z = this.lclaw1.z;
		this.lclaw3.x = this.lclaw1.x;
		this.lclaw3.yRot = this.lclaw1.yRot;
		this.lclaw4.z = this.lclaw1.z;
		this.lclaw4.x = this.lclaw1.x;
		this.lclaw4.yRot = this.lclaw1.yRot;
		this.rarm.yRot = newangle - 0.628F;
		this.rhand.z = this.rarm.z + (float)Math.sin(this.rarm.yRot) * 15.0F;
		this.rhand.x = this.rarm.x - (float)Math.cos(this.rarm.yRot) * 15.0F;
		this.rhand.yRot = newangle * 1.25F;
		this.rclaw1.z = this.rhand.z + (float)Math.sin(this.rhand.yRot) * 10.0F;
		this.rclaw1.x = this.rhand.x - (float)Math.cos(this.rhand.yRot) * 10.0F;
		this.rclaw1.yRot = newangle * 1.5F + 0.174F;
		this.rclaw2.z = this.rclaw1.z;
		this.rclaw2.x = this.rclaw1.x;
		this.rclaw2.yRot = this.rclaw1.yRot;
		this.rclaw3.z = this.rclaw1.z;
		this.rclaw3.x = this.rclaw1.x;
		this.rclaw3.yRot = this.rclaw1.yRot;
		this.rclaw4.z = this.rclaw1.z;
		this.rclaw4.x = this.rclaw1.x;
		this.rclaw4.yRot = this.rclaw1.yRot;
		if (f1 > 0.1F) {
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.25F * f1;
		} else {
		newangle = 0.0F;
		}
		this.lleg.yRot = -newangle + 0.628F;
		this.lfoot.z = this.lleg.z - (float)Math.sin(this.lleg.yRot) * 15.0F;
		this.lfoot.x = this.lleg.x + (float)Math.cos(this.lleg.yRot) * 15.0F;
		this.lfoot.yRot = -newangle * 1.25F;
		this.ltoe1.z = this.lfoot.z - (float)Math.sin(this.lfoot.yRot) * 10.0F;
		this.ltoe1.x = this.lfoot.x + (float)Math.cos(this.lfoot.yRot) * 10.0F;
		this.ltoe1.yRot = -newangle * 1.5F - 0.261F;
		this.ltoe2.z = this.ltoe1.z;
		this.ltoe2.x = this.ltoe1.x;
		this.ltoe2.yRot = this.ltoe1.yRot;
		this.ltoe3.z = this.ltoe1.z;
		this.ltoe3.x = this.ltoe1.x;
		this.ltoe3.yRot = this.ltoe1.yRot;
		this.ltoe4.z = this.ltoe1.z;
		this.ltoe4.x = this.ltoe1.x;
		this.ltoe4.yRot = this.ltoe1.yRot;
		this.rleg.yRot = -newangle - 0.628F;
		this.rfoot.z = this.rleg.z + (float)Math.sin(this.rleg.yRot) * 15.0F;
		this.rfoot.x = this.rleg.x - (float)Math.cos(this.rleg.yRot) * 15.0F;
		this.rfoot.yRot = -newangle * 1.25F;
		this.rtoe1.z = this.rfoot.z + (float)Math.sin(this.rfoot.yRot) * 10.0F;
		this.rtoe1.x = this.rfoot.x - (float)Math.cos(this.rfoot.yRot) * 10.0F;
		this.rtoe1.yRot = -newangle * 1.5F + 0.261F;
		this.rtoe2.z = this.rtoe1.z;
		this.rtoe2.x = this.rtoe1.x;
		this.rtoe2.yRot = this.rtoe1.yRot;
		this.rtoe3.z = this.rtoe1.z;
		this.rtoe3.x = this.rtoe1.x;
		this.rtoe3.yRot = this.rtoe1.yRot;
		this.rtoe4.z = this.rtoe1.z;
		this.rtoe4.x = this.rtoe1.x;
		this.rtoe4.yRot = this.rtoe1.yRot;
		newangle = Mth.cos(f2 * 0.1F * this.wingspeed) * (float) Math.PI;
		this.nosestar1.zRot = newangle;
		this.nosestar2.zRot = newangle + 0.523F;
		this.nosestar3.zRot = newangle + 1.047F;
		this.nosestar4.zRot = newangle + 1.57F;
		this.nosestar5.zRot = newangle - 1.047F;
		this.nosestar6.zRot = newangle - 0.523F;
	}
}
