package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelSpyro (the Baby Dragon); state.value is its activity (2 flying). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class SpyroModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart RightFrontPaw;
	private final ModelPart WingLeft;
	private final ModelPart LegRightFrontTop;
	private final ModelPart LegRightFrontBottom;
	private final ModelPart LegRightBackTop;
	private final ModelPart LegRightBackBottom;
	private final ModelPart RightBackPaw;
	private final ModelPart LegLeftFrontTop;
	private final ModelPart SnoutRight;
	private final ModelPart LeftFrontPaw;
	private final ModelPart LegLeftBackTop;
	private final ModelPart LegLeftBackBottom;
	private final ModelPart LeftBackPaw;
	private final ModelPart LegLeftFrontBottom;
	private final ModelPart TailPieceSmall;
	private final ModelPart JawPiece;
	private final ModelPart HeadPieceBottom;
	private final ModelPart HeadPieceTop;
	private final ModelPart HornRightBottom;
	private final ModelPart HornLeftBottom;
	private final ModelPart HornRightTop;
	private final ModelPart HornLeftTop;
	private final ModelPart Torso;
	private final ModelPart SnoutLeft;
	private final ModelPart WingPieceLeft;
	private final ModelPart WingRight;
	private final ModelPart WingPieceRight;
	private final ModelPart Neck;
	private final ModelPart TailBack;
	private final ModelPart TailFront;
	private final ModelPart ScaleBackHead;
	private final ModelPart TailPieceLarge;
	private final ModelPart ScaleTailPiece;
	private final ModelPart ScaleHead;
	private final ModelPart ScaleTop1;
	private final ModelPart ScaleBackPiece1;
	private final ModelPart ScaleBackPiece2;
	private final float wingspeed;

	public SpyroModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.RightFrontPaw = root.getChild("rightfrontpaw");
		this.WingLeft = root.getChild("wingleft");
		this.LegRightFrontTop = root.getChild("legrightfronttop");
		this.LegRightFrontBottom = root.getChild("legrightfrontbottom");
		this.LegRightBackTop = root.getChild("legrightbacktop");
		this.LegRightBackBottom = root.getChild("legrightbackbottom");
		this.RightBackPaw = root.getChild("rightbackpaw");
		this.LegLeftFrontTop = root.getChild("legleftfronttop");
		this.SnoutRight = root.getChild("snoutright");
		this.LeftFrontPaw = root.getChild("leftfrontpaw");
		this.LegLeftBackTop = root.getChild("legleftbacktop");
		this.LegLeftBackBottom = root.getChild("legleftbackbottom");
		this.LeftBackPaw = root.getChild("leftbackpaw");
		this.LegLeftFrontBottom = root.getChild("legleftfrontbottom");
		this.TailPieceSmall = root.getChild("tailpiecesmall");
		this.JawPiece = root.getChild("jawpiece");
		this.HeadPieceBottom = root.getChild("headpiecebottom");
		this.HeadPieceTop = root.getChild("headpiecetop");
		this.HornRightBottom = root.getChild("hornrightbottom");
		this.HornLeftBottom = root.getChild("hornleftbottom");
		this.HornRightTop = root.getChild("hornrighttop");
		this.HornLeftTop = root.getChild("hornlefttop");
		this.Torso = root.getChild("torso");
		this.SnoutLeft = root.getChild("snoutleft");
		this.WingPieceLeft = root.getChild("wingpieceleft");
		this.WingRight = root.getChild("wingright");
		this.WingPieceRight = root.getChild("wingpieceright");
		this.Neck = root.getChild("neck");
		this.TailBack = root.getChild("tailback");
		this.TailFront = root.getChild("tailfront");
		this.ScaleBackHead = root.getChild("scalebackhead");
		this.TailPieceLarge = root.getChild("tailpiecelarge");
		this.ScaleTailPiece = root.getChild("scaletailpiece");
		this.ScaleHead = root.getChild("scalehead");
		this.ScaleTop1 = root.getChild("scaletop1");
		this.ScaleBackPiece1 = root.getChild("scalebackpiece1");
		this.ScaleBackPiece2 = root.getChild("scalebackpiece2");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelSpyro");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float hf = 0.0F;
		float newangle = 0.0F;
		int current_activity = (int) state.value;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 2.3F * this.wingspeed) * (float) Math.PI * 0.4F * f1;
		} else {
		newangle = 0.0F;
		}
		if (current_activity == 3) {
		newangle *= 0.5F;
		}
		this.WingLeft.zRot = newangle;
		this.WingRight.zRot = -newangle;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 2.0F * this.wingspeed) * (float) Math.PI * 0.25F * f1;
		} else {
		newangle = 0.0F;
		}
		if (current_activity == 3) {
		newangle = 0.0F;
		}
		if (current_activity != 2) {
		this.LegRightFrontTop.xRot = newangle - 0.087F;
		this.LegRightFrontBottom.xRot = newangle - 0.17F;
		this.RightFrontPaw.xRot = newangle;
		this.LegLeftFrontTop.xRot = -newangle - 0.087F;
		this.LegLeftFrontBottom.xRot = -newangle - 0.17F;
		this.LeftFrontPaw.xRot = -newangle;
		this.LegRightBackBottom.xRot = -newangle + 0.139F;
		this.LegRightBackTop.xRot = -newangle - 0.174F;
		this.RightBackPaw.xRot = -newangle;
		this.LegLeftBackBottom.xRot = newangle + 0.139F;
		this.LegLeftBackTop.xRot = newangle - 0.174F;
		this.LeftBackPaw.xRot = newangle;
		} else {
		newangle = -1.0F;
		this.LegRightFrontTop.xRot = newangle - 0.087F;
		this.LegRightFrontBottom.xRot = newangle - 0.17F;
		this.RightFrontPaw.xRot = newangle;
		this.LegLeftFrontTop.xRot = newangle - 0.087F;
		this.LegLeftFrontBottom.xRot = newangle - 0.17F;
		this.LeftFrontPaw.xRot = newangle;
		newangle = 1.0F;
		this.LegRightBackBottom.xRot = newangle + 0.139F;
		this.LegRightBackTop.xRot = newangle - 0.174F;
		this.RightBackPaw.xRot = newangle;
		this.LegLeftBackBottom.xRot = newangle + 0.139F;
		this.LegLeftBackTop.xRot = newangle - 0.174F;
		this.LeftBackPaw.xRot = newangle;
		}
		newangle = Mth.cos(f2 * 1.2F * this.wingspeed) * (float) Math.PI * 0.25F;
		if (state.sitting || current_activity == 3) {
		newangle = 0.0F;
		}
		this.TailBack.yRot = newangle;
		this.ScaleTailPiece.yRot = newangle;
		this.TailFront.z = this.TailBack.z + (float)Math.cos(this.TailBack.yRot) * 3.0F;
		this.TailFront.x = this.TailBack.x + (float)Math.sin(this.TailBack.yRot) * 3.0F - 0.5F;
		this.TailFront.yRot = newangle * 1.6F;
		this.TailPieceLarge.z = this.TailFront.z;
		this.TailPieceLarge.x = this.TailFront.x;
		this.TailPieceLarge.yRot = this.TailFront.yRot;
		this.TailPieceSmall.z = this.TailFront.z;
		this.TailPieceSmall.x = this.TailFront.x;
		this.TailPieceSmall.yRot = this.TailFront.yRot;
		this.HeadPieceTop.yRot = (float)Math.toRadians(f3);
		this.HeadPieceBottom.yRot = (float)Math.toRadians(f3);
		this.JawPiece.yRot = (float)Math.toRadians(f3);
		this.SnoutRight.yRot = (float)Math.toRadians(f3);
		this.SnoutLeft.yRot = (float)Math.toRadians(f3);
		this.ScaleTop1.yRot = (float)Math.toRadians(f3);
		this.ScaleHead.yRot = (float)Math.toRadians(f3);
		this.ScaleBackHead.yRot = (float)Math.toRadians(f3);
		this.HornRightBottom.yRot = (float)Math.toRadians(f3) + 0.785F;
		this.HornRightTop.yRot = (float)Math.toRadians(f3) + 0.785F;
		this.HornLeftBottom.yRot = (float)Math.toRadians(f3) - 0.785F;
		this.HornLeftTop.yRot = (float)Math.toRadians(f3) - 0.785F;
		this.HeadPieceTop.xRot = (float)Math.toRadians(f4);
		this.HeadPieceBottom.xRot = (float)Math.toRadians(f4);
		this.JawPiece.xRot = (float)Math.toRadians(f4);
		this.SnoutRight.xRot = (float)Math.toRadians(f4);
		this.SnoutLeft.xRot = (float)Math.toRadians(f4);
		this.ScaleTop1.xRot = (float)Math.toRadians(f4);
		this.ScaleHead.xRot = (float)Math.toRadians(f4);
		this.ScaleBackHead.xRot = (float)Math.toRadians(f4);
		this.HornRightBottom.xRot = (float)Math.toRadians(f4) - 0.785F;
		this.HornRightTop.xRot = (float)Math.toRadians(f4) - 0.785F;
		this.HornLeftBottom.xRot = (float)Math.toRadians(f4) - 0.785F;
		this.HornLeftTop.xRot = (float)Math.toRadians(f4) - 0.785F;
	}
}
