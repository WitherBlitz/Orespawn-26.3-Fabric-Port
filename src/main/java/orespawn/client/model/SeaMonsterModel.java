package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelSeaMonster (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class SeaMonsterModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart TailTip;
	private final ModelPart TailBase;
	private final ModelPart Tail2;
	private final ModelPart Tail3;
	private final ModelPart BodyBack;
	private final ModelPart Neck6;
	private final ModelPart BodyFront;
	private final ModelPart NeckBase;
	private final ModelPart Neck2;
	private final ModelPart Neck3;
	private final ModelPart Neck4;
	private final ModelPart Neck5;
	private final ModelPart BottomJaw;
	private final ModelPart FinBackRight;
	private final ModelPart FinBackLeft;
	private final ModelPart FinFrontLeft;
	private final ModelPart FinFrontRight;
	private final ModelPart Tail4;
	private final ModelPart Tail5;
	private final ModelPart Tail6;
	private final ModelPart TopJaw;
	private final ModelPart RightEye;
	private final ModelPart LeftEye;
	private final float wingspeed;

	public SeaMonsterModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.TailTip = root.getChild("tailtip");
		this.TailBase = root.getChild("tailbase");
		this.Tail2 = root.getChild("tail2");
		this.Tail3 = root.getChild("tail3");
		this.BodyBack = root.getChild("bodyback");
		this.Neck6 = root.getChild("neck6");
		this.BodyFront = root.getChild("bodyfront");
		this.NeckBase = root.getChild("neckbase");
		this.Neck2 = root.getChild("neck2");
		this.Neck3 = root.getChild("neck3");
		this.Neck4 = root.getChild("neck4");
		this.Neck5 = root.getChild("neck5");
		this.BottomJaw = root.getChild("bottomjaw");
		this.FinBackRight = root.getChild("finbackright");
		this.FinBackLeft = root.getChild("finbackleft");
		this.FinFrontLeft = root.getChild("finfrontleft");
		this.FinFrontRight = root.getChild("finfrontright");
		this.Tail4 = root.getChild("tail4");
		this.Tail5 = root.getChild("tail5");
		this.Tail6 = root.getChild("tail6");
		this.TopJaw = root.getChild("topjaw");
		this.RightEye = root.getChild("righteye");
		this.LeftEye = root.getChild("lefteye");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelSeaMonster");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		float pi4 = (float) (Math.PI / 4);
		if (!(f1 > 0.1) && !state.attacking) {
		newangle = 0.0F;
		} else {
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.2F * f1;
		}
		this.TailBase.yRot = newangle / 7.0F;
		this.Tail2.z = this.TailBase.z + (float)Math.cos(this.TailBase.yRot) * 10.0F;
		this.Tail2.x = this.TailBase.x + (float)Math.sin(this.TailBase.yRot) * 10.0F;
		this.Tail2.yRot = newangle / 6.0F;
		this.Tail3.z = this.Tail2.z + (float)Math.cos(this.Tail2.yRot) * 7.0F;
		this.Tail3.x = this.Tail2.x + (float)Math.sin(this.Tail2.yRot) * 7.0F;
		this.Tail3.yRot = newangle / 5.0F;
		this.Tail4.z = this.Tail3.z + (float)Math.cos(this.Tail3.yRot) * 5.0F;
		this.Tail4.x = this.Tail3.x + (float)Math.sin(this.Tail3.yRot) * 5.0F;
		this.Tail4.yRot = newangle / 4.0F;
		this.Tail5.z = this.Tail4.z + (float)Math.cos(this.Tail4.yRot) * 5.0F;
		this.Tail5.x = this.Tail4.x + (float)Math.sin(this.Tail4.yRot) * 5.0F;
		this.Tail5.yRot = newangle / 3.0F;
		this.Tail6.z = this.Tail5.z + (float)Math.cos(this.Tail5.yRot) * 5.0F;
		this.Tail6.x = this.Tail5.x + (float)Math.sin(this.Tail5.yRot) * 5.0F;
		this.Tail6.yRot = newangle / 2.0F;
		this.TailTip.z = this.Tail6.z + (float)Math.cos(this.Tail6.yRot) * 5.0F;
		this.TailTip.x = this.Tail6.x + (float)Math.sin(this.Tail6.yRot) * 5.0F;
		this.TailTip.yRot = newangle;
		if (!(f1 > 0.1) && !state.attacking) {
		newangle = Mth.cos(f2 * 1.2F * this.wingspeed) * (float) Math.PI * 0.02F;
		} else {
		newangle = Mth.cos(f2 * 1.2F * this.wingspeed) * (float) Math.PI * 0.2F * f1;
		}
		this.FinFrontLeft.xRot = newangle - 0.523F;
		this.FinFrontLeft.yRot = newangle + 0.698F;
		this.FinBackLeft.xRot = -newangle - 0.523F;
		this.FinBackLeft.yRot = -newangle + 0.698F;
		this.FinFrontRight.xRot = newangle - 0.523F;
		this.FinFrontRight.yRot = newangle - 0.698F;
		this.FinBackRight.xRot = -newangle - 0.523F;
		this.FinBackRight.yRot = -newangle - 0.698F;
		if (!(f1 > 0.1) && !state.attacking) {
		newangle = Mth.cos(f2 * 0.3F * this.wingspeed) * (float) Math.PI * 0.02F;
		} else {
		newangle = 0.455F * f1 + Mth.cos(f2 * 0.9F * this.wingspeed) * (float) Math.PI * 0.25F * f1;
		}
		this.NeckBase.xRot = 0.455F + newangle / 5.0F;
		this.Neck2.z = this.NeckBase.z - (float)Math.sin(this.NeckBase.xRot) * 9.0F;
		this.Neck2.y = this.NeckBase.y - (float)Math.cos(this.NeckBase.xRot) * 9.0F;
		this.Neck2.xRot = this.NeckBase.xRot + newangle / 4.0F;
		this.Neck3.z = this.Neck2.z - (float)Math.sin(this.Neck2.xRot) * 9.0F;
		this.Neck3.y = this.Neck2.y - (float)Math.cos(this.Neck2.xRot) * 9.0F;
		this.Neck3.xRot = this.Neck2.xRot + newangle / 3.0F;
		this.Neck4.z = this.Neck3.z - (float)Math.sin(this.Neck3.xRot) * 9.0F;
		this.Neck4.y = this.Neck3.y - (float)Math.cos(this.Neck3.xRot) * 9.0F;
		this.Neck4.xRot = this.Neck3.xRot + newangle / 2.0F;
		this.Neck5.z = this.Neck4.z - (float)Math.sin(this.Neck4.xRot) * 9.0F;
		this.Neck5.y = this.Neck4.y - (float)Math.cos(this.Neck4.xRot) * 9.0F;
		this.Neck5.xRot = this.Neck4.xRot - newangle / 2.0F;
		this.Neck6.z = this.Neck5.z - (float)Math.sin(this.Neck5.xRot) * 5.0F;
		this.Neck6.y = this.Neck5.y - (float)Math.cos(this.Neck5.xRot) * 5.0F;
		this.Neck6.xRot = this.Neck5.xRot - newangle / 3.0F;
		this.BottomJaw.z = this.LeftEye.z = this.RightEye.z = this.TopJaw.z = this.Neck6.z
		- (float)Math.sin(this.Neck6.xRot) * 5.0F;
		this.BottomJaw.y = this.LeftEye.y = this.RightEye.y = this.TopJaw.y = this.Neck6.y
		- (float)Math.cos(this.Neck6.xRot) * 5.0F;
		newangle = (float)Math.toRadians(f3) * 0.5F;
		this.BottomJaw.yRot = this.LeftEye.yRot = this.RightEye.yRot = this.TopJaw.yRot = newangle;
		if (state.attacking) {
		newangle = Mth.cos(f2 * 1.7F * this.wingspeed) * (float) Math.PI * 0.17F;
		this.BottomJaw.xRot = 0.45F + newangle;
		} else {
		newangle = Mth.cos(f2 * 0.2F * this.wingspeed) * (float) Math.PI * 0.05F;
		this.BottomJaw.xRot = 0.17F + newangle;
		}
	}
}
