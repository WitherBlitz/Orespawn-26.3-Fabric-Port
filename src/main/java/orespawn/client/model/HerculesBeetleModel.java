package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelHerculesBeetle (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class HerculesBeetleModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body1;
	private final ModelPart body2;
	private final ModelPart head1;
	private final ModelPart head2;
	private final ModelPart head3;
	private final ModelPart head4;
	private final ModelPart head5;
	private final ModelPart head6;
	private final ModelPart head8;
	private final ModelPart jaw1;
	private final ModelPart jaw2;
	private final ModelPart jaw3;
	private final ModelPart jaw4;
	private final ModelPart head7;
	private final ModelPart lfleg1;
	private final ModelPart lfleg2;
	private final ModelPart lfleg3;
	private final ModelPart lmleg1;
	private final ModelPart lmleg2;
	private final ModelPart lmleg3;
	private final ModelPart lrleg1;
	private final ModelPart lrleg2;
	private final ModelPart lrleg3;
	private final ModelPart jaw5;
	private final ModelPart jaw6;
	private final ModelPart jaw7;
	private final ModelPart jaw8;
	private final ModelPart rfleg1;
	private final ModelPart rfleg2;
	private final ModelPart rfleg3;
	private final ModelPart rmleg1;
	private final ModelPart rmleg2;
	private final ModelPart rmleg3;
	private final ModelPart rrleg1;
	private final ModelPart rrleg2;
	private final ModelPart rrleg3;
	private final ModelPart jaw9;
	private final float wingspeed;

	public HerculesBeetleModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.body1 = root.getChild("body1");
		this.body2 = root.getChild("body2");
		this.head1 = root.getChild("head1");
		this.head2 = root.getChild("head2");
		this.head3 = root.getChild("head3");
		this.head4 = root.getChild("head4");
		this.head5 = root.getChild("head5");
		this.head6 = root.getChild("head6");
		this.head8 = root.getChild("head8");
		this.jaw1 = root.getChild("jaw1");
		this.jaw2 = root.getChild("jaw2");
		this.jaw3 = root.getChild("jaw3");
		this.jaw4 = root.getChild("jaw4");
		this.head7 = root.getChild("head7");
		this.lfleg1 = root.getChild("lfleg1");
		this.lfleg2 = root.getChild("lfleg2");
		this.lfleg3 = root.getChild("lfleg3");
		this.lmleg1 = root.getChild("lmleg1");
		this.lmleg2 = root.getChild("lmleg2");
		this.lmleg3 = root.getChild("lmleg3");
		this.lrleg1 = root.getChild("lrleg1");
		this.lrleg2 = root.getChild("lrleg2");
		this.lrleg3 = root.getChild("lrleg3");
		this.jaw5 = root.getChild("jaw5");
		this.jaw6 = root.getChild("jaw6");
		this.jaw7 = root.getChild("jaw7");
		this.jaw8 = root.getChild("jaw8");
		this.rfleg1 = root.getChild("rfleg1");
		this.rfleg2 = root.getChild("rfleg2");
		this.rfleg3 = root.getChild("rfleg3");
		this.rmleg1 = root.getChild("rmleg1");
		this.rmleg2 = root.getChild("rmleg2");
		this.rmleg3 = root.getChild("rmleg3");
		this.rrleg1 = root.getChild("rrleg1");
		this.rrleg2 = root.getChild("rrleg2");
		this.rrleg3 = root.getChild("rrleg3");
		this.jaw9 = root.getChild("jaw9");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelHerculesBeetle");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		newangle = Mth.cos(f2 * this.wingspeed * 0.45F) * (float) Math.PI * 0.12F * f1;
		this.lfleg1.yRot = 0.349F + newangle;
		this.lfleg3.yRot = this.lfleg2.yRot = this.lfleg1.yRot;
		this.lmleg1.yRot = -newangle;
		this.lmleg3.yRot = this.lmleg2.yRot = this.lmleg1.yRot;
		this.lrleg1.yRot = -0.349F + newangle;
		this.lrleg3.yRot = this.lrleg2.yRot = this.lrleg1.yRot;
		this.rfleg1.yRot = -0.349F + newangle;
		this.rfleg3.yRot = this.rfleg2.yRot = this.rfleg1.yRot;
		this.rmleg1.yRot = -newangle;
		this.rmleg3.yRot = this.rmleg2.yRot = this.rmleg1.yRot;
		this.rrleg1.yRot = 0.349F + newangle;
		this.rrleg3.yRot = this.rrleg2.yRot = this.rrleg1.yRot;
		if (!state.attacking) {
		newangle = Mth.cos(f2 * 0.051F * this.wingspeed) * (float) Math.PI * 0.01F;
		} else {
		newangle = Mth.cos(f2 * 0.51F * this.wingspeed) * (float) Math.PI * 0.07F;
		}
		this.jaw1.xRot = 0.122F + newangle;
		this.jaw2.xRot = 0.122F + newangle;
		this.jaw3.xRot = 0.0F + newangle;
		this.jaw4.xRot = 0.0F + newangle;
		this.jaw5.xRot = 0.122F + newangle;
		this.jaw6.xRot = 0.122F + newangle;
		this.jaw7.xRot = 0.0F + newangle;
		this.jaw8.xRot = 0.0F + newangle;
		this.jaw9.xRot = 0.314F + newangle;
	}
}
