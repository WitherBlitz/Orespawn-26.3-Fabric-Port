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
 * Ported from OreSpawn's ModelGazelle (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). The tail swishes unless it's
 * sitting ({@code state.sitting}).
 */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class GazelleModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Chest;
	private final ModelPart lfleg1;
	private final ModelPart lrleg2;
	private final ModelPart lrleg1;
	private final ModelPart rfleg3;
	private final ModelPart rrleg2;
	private final ModelPart rrleg3;
	private final ModelPart rfleg2;
	private final ModelPart lrleg4;
	private final ModelPart tail;
	private final ModelPart lear;
	private final ModelPart rrleg1;
	private final ModelPart rfleg1;
	private final ModelPart lrleg3;
	private final ModelPart lfleg2;
	private final ModelPart rrleg5;
	private final ModelPart rrleg4;
	private final ModelPart lfleg3;
	private final ModelPart rfleg4;
	private final ModelPart lfleg4;
	private final ModelPart lrleg5;
	private final ModelPart Body;
	private final ModelPart neck;
	private final ModelPart la3;
	private final ModelPart throatfluff;
	private final ModelPart rear;
	private final ModelPart head;
	private final ModelPart ra1;
	private final ModelPart la1;
	private final ModelPart la2;
	private final ModelPart ra2;
	private final ModelPart ra3;
	private final ModelPart nose;
	private final ModelPart mouth;
	private final float wingspeed;

	public GazelleModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Chest = root.getChild("chest");
		this.lfleg1 = root.getChild("lfleg1");
		this.lrleg2 = root.getChild("lrleg2");
		this.lrleg1 = root.getChild("lrleg1");
		this.rfleg3 = root.getChild("rfleg3");
		this.rrleg2 = root.getChild("rrleg2");
		this.rrleg3 = root.getChild("rrleg3");
		this.rfleg2 = root.getChild("rfleg2");
		this.lrleg4 = root.getChild("lrleg4");
		this.tail = root.getChild("tail");
		this.lear = root.getChild("lear");
		this.rrleg1 = root.getChild("rrleg1");
		this.rfleg1 = root.getChild("rfleg1");
		this.lrleg3 = root.getChild("lrleg3");
		this.lfleg2 = root.getChild("lfleg2");
		this.rrleg5 = root.getChild("rrleg5");
		this.rrleg4 = root.getChild("rrleg4");
		this.lfleg3 = root.getChild("lfleg3");
		this.rfleg4 = root.getChild("rfleg4");
		this.lfleg4 = root.getChild("lfleg4");
		this.lrleg5 = root.getChild("lrleg5");
		this.Body = root.getChild("body");
		this.neck = root.getChild("neck");
		this.la3 = root.getChild("la3");
		this.throatfluff = root.getChild("throatfluff");
		this.rear = root.getChild("rear");
		this.head = root.getChild("head");
		this.ra1 = root.getChild("ra1");
		this.la1 = root.getChild("la1");
		this.la2 = root.getChild("la2");
		this.ra2 = root.getChild("ra2");
		this.ra3 = root.getChild("ra3");
		this.nose = root.getChild("nose");
		this.mouth = root.getChild("mouth");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelGazelle");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float hf = 0.0F;
		float newangle = 0.0F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 1.1F * this.wingspeed) * (float) Math.PI * 0.12F * f1;
		} else {
		newangle = 0.0F;
		}
		this.lfleg1.xRot = 0.297F + newangle;
		this.lfleg2.xRot = -0.074F + newangle;
		this.lfleg3.xRot = -0.409F + newangle;
		this.lfleg4.xRot = newangle;
		this.rfleg1.xRot = 0.297F - newangle;
		this.rfleg2.xRot = -0.074F - newangle;
		this.rfleg3.xRot = -0.409F - newangle;
		this.rfleg4.xRot = -newangle;
		this.lrleg1.xRot = -newangle;
		this.lrleg2.xRot = 0.185F - newangle;
		this.lrleg3.xRot = -0.074F - newangle;
		this.lrleg4.xRot = -0.409F - newangle;
		this.lrleg5.xRot = -newangle;
		this.rrleg1.xRot = newangle;
		this.rrleg2.xRot = 0.185F + newangle;
		this.rrleg3.xRot = -0.074F + newangle;
		this.rrleg4.xRot = -0.409F + newangle;
		this.rrleg5.xRot = newangle;
		newangle = Mth.cos(f2 * 0.5F) * (float) Math.PI * 0.02F;
		this.head.yRot = (float)Math.toRadians(f3) * 0.45F;
		this.nose.yRot = this.head.yRot;
		this.mouth.yRot = this.head.yRot;
		this.lear.yRot = 1.57F + this.head.yRot + newangle;
		this.rear.yRot = 1.57F + this.head.yRot + newangle;
		this.la1.yRot = this.head.yRot;
		this.la2.yRot = this.head.yRot;
		this.la3.yRot = this.head.yRot;
		this.ra1.yRot = this.head.yRot;
		this.ra2.yRot = this.head.yRot;
		this.ra3.yRot = this.head.yRot;
		if (!state.sitting) {
		this.tail.xRot = 1.0F + Mth.cos(f2 * 0.1F) * (float) Math.PI * 0.06F;
		}
	}
}
