package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelMantis (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class MantisModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart lfleg1;
	private final ModelPart lfleg2;
	private final ModelPart lfleg3;
	private final ModelPart lfleg4;
	private final ModelPart lrleg1;
	private final ModelPart lrleg2;
	private final ModelPart lrleg3;
	private final ModelPart lrleg4;
	private final ModelPart abdomen;
	private final ModelPart thorax;
	private final ModelPart neck1;
	private final ModelPart neck2;
	private final ModelPart head1;
	private final ModelPart head2;
	private final ModelPart leye;
	private final ModelPart reye;
	private final ModelPart lantenna;
	private final ModelPart rantenna;
	private final ModelPart larm1;
	private final ModelPart larm2;
	private final ModelPart larm3;
	private final ModelPart lfwing;
	private final ModelPart rfwing;
	private final ModelPart lrwing;
	private final ModelPart rrwing;
	private final ModelPart rarm1;
	private final ModelPart rarm2;
	private final ModelPart rarm3;
	private final ModelPart rlfleg3;
	private final ModelPart rfleg4;
	private final ModelPart rfleg2;
	private final ModelPart rfleg1;
	private final ModelPart rrleg3;
	private final ModelPart rrleg4;
	private final ModelPart rrleg2;
	private final ModelPart rrleg1;
	private final float wingspeed;

	public MantisModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.lfleg1 = root.getChild("lfleg1");
		this.lfleg2 = root.getChild("lfleg2");
		this.lfleg3 = root.getChild("lfleg3");
		this.lfleg4 = root.getChild("lfleg4");
		this.lrleg1 = root.getChild("lrleg1");
		this.lrleg2 = root.getChild("lrleg2");
		this.lrleg3 = root.getChild("lrleg3");
		this.lrleg4 = root.getChild("lrleg4");
		this.abdomen = root.getChild("abdomen");
		this.thorax = root.getChild("thorax");
		this.neck1 = root.getChild("neck1");
		this.neck2 = root.getChild("neck2");
		this.head1 = root.getChild("head1");
		this.head2 = root.getChild("head2");
		this.leye = root.getChild("leye");
		this.reye = root.getChild("reye");
		this.lantenna = root.getChild("lantenna");
		this.rantenna = root.getChild("rantenna");
		this.larm1 = root.getChild("larm1");
		this.larm2 = root.getChild("larm2");
		this.larm3 = root.getChild("larm3");
		this.lfwing = root.getChild("lfwing");
		this.rfwing = root.getChild("rfwing");
		this.lrwing = root.getChild("lrwing");
		this.rrwing = root.getChild("rrwing");
		this.rarm1 = root.getChild("rarm1");
		this.rarm2 = root.getChild("rarm2");
		this.rarm3 = root.getChild("rarm3");
		this.rlfleg3 = root.getChild("rlfleg3");
		this.rfleg4 = root.getChild("rfleg4");
		this.rfleg2 = root.getChild("rfleg2");
		this.rfleg1 = root.getChild("rfleg1");
		this.rrleg3 = root.getChild("rrleg3");
		this.rrleg4 = root.getChild("rrleg4");
		this.rrleg2 = root.getChild("rrleg2");
		this.rrleg1 = root.getChild("rrleg1");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelMantis");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		newangle = Mth.cos(f2 * 0.9F * this.wingspeed) * (float) Math.PI * 0.25F;
		this.lfwing.zRot = -0.698F - newangle;
		this.rfwing.zRot = 0.698F + newangle;
		newangle = Mth.cos(f2 * 0.9F * this.wingspeed) * (float) Math.PI * 0.35F;
		this.lrwing.zRot = -0.349F + newangle;
		this.rrwing.zRot = 0.349F - newangle;
		float a1;
		if (!state.attacking) {
		newangle = Mth.cos(f2 * 0.051F * this.wingspeed) * (float) Math.PI * 0.013F;
		a1 = -0.2F;
		} else {
		newangle = Mth.cos(f2 * 0.51F * this.wingspeed) * (float) Math.PI * 0.25F;
		a1 = -0.698F;
		}
		this.larm1.xRot = a1 + newangle;
		this.larm2.z = (float)(this.larm1.z + 1.0F + Math.sin(this.larm1.xRot) * 22.0);
		this.larm2.y = (float)(this.larm1.y + Math.cos(this.larm1.xRot) * 22.0);
		this.larm2.xRot = -a1 - newangle;
		this.larm3.z = (float)(this.larm2.z + 1.0F - Math.sin(this.larm2.xRot) * 17.0);
		this.larm3.y = (float)(this.larm2.y - Math.cos(this.larm2.xRot) * 17.0);
		this.larm3.xRot = a1 + newangle;
		this.rarm1.xRot = a1 - newangle;
		this.rarm2.z = (float)(this.rarm1.z + 1.0F + Math.sin(this.rarm1.xRot) * 22.0);
		this.rarm2.y = (float)(this.rarm1.y + Math.cos(this.rarm1.xRot) * 22.0);
		this.rarm2.xRot = -a1 + newangle;
		this.rarm3.z = (float)(this.rarm2.z + 1.0F - Math.sin(this.rarm2.xRot) * 17.0);
		this.rarm3.y = (float)(this.rarm2.y - Math.cos(this.rarm2.xRot) * 17.0);
		this.rarm3.xRot = a1 - newangle;
	}
}
