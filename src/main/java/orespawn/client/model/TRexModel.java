package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelTRex (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class TRexModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Shape18;
	private final ModelPart Shape19;
	private final ModelPart Shape20;
	private final ModelPart Shape21;
	private final ModelPart Shape1;
	private final ModelPart Shape2;
	private final ModelPart Shape3;
	private final ModelPart Shape4;
	private final ModelPart Shape5;
	private final ModelPart Shape6;
	private final ModelPart jaw;
	private final ModelPart leftleg;
	private final ModelPart leftleg2;
	private final ModelPart leftleg3;
	private final ModelPart Shape11;
	private final ModelPart rightleg;
	private final ModelPart rightleg2;
	private final ModelPart rightleg3;
	private final ModelPart leftleg4;
	private final ModelPart rightleg4;
	private final ModelPart Shape17;
	private final ModelPart TailExtension;
	private final ModelPart Spine1;
	private final ModelPart Spine2;
	private final ModelPart Spine3;
	private final ModelPart Spine4;
	private final ModelPart Spine5;
	private final float wingspeed;

	public TRexModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Shape18 = root.getChild("shape18");
		this.Shape19 = root.getChild("shape19");
		this.Shape20 = root.getChild("shape20");
		this.Shape21 = root.getChild("shape21");
		this.Shape1 = root.getChild("shape1");
		this.Shape2 = root.getChild("shape2");
		this.Shape3 = root.getChild("shape3");
		this.Shape4 = root.getChild("shape4");
		this.Shape5 = root.getChild("shape5");
		this.Shape6 = root.getChild("shape6");
		this.jaw = root.getChild("jaw");
		this.leftleg = root.getChild("leftleg");
		this.leftleg2 = root.getChild("leftleg2");
		this.leftleg3 = root.getChild("leftleg3");
		this.Shape11 = root.getChild("shape11");
		this.rightleg = root.getChild("rightleg");
		this.rightleg2 = root.getChild("rightleg2");
		this.rightleg3 = root.getChild("rightleg3");
		this.leftleg4 = root.getChild("leftleg4");
		this.rightleg4 = root.getChild("rightleg4");
		this.Shape17 = root.getChild("shape17");
		this.TailExtension = root.getChild("tailextension");
		this.Spine1 = root.getChild("spine1");
		this.Spine2 = root.getChild("spine2");
		this.Spine3 = root.getChild("spine3");
		this.Spine4 = root.getChild("spine4");
		this.Spine5 = root.getChild("spine5");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelTRex");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.25F * f1;
		} else {
		newangle = 0.0F;
		}
		this.rightleg.xRot = -0.174F + newangle;
		this.rightleg2.xRot = 0.506F + newangle;
		this.rightleg3.xRot = -0.401F + newangle;
		this.rightleg4.xRot = newangle;
		this.leftleg.xRot = -0.174F - newangle;
		this.leftleg2.xRot = 0.506F - newangle;
		this.leftleg3.xRot = -0.401F - newangle;
		this.leftleg4.xRot = -newangle;
		if (state.attacking) {
		this.jaw.xRot = 0.52F + Mth.cos(f2 * 0.45F) * (float) Math.PI * 0.18F;
		} else {
		this.jaw.xRot = 0.1F;
		}
		this.Shape17.xRot = -0.523F + Mth.cos(f2 * 0.1F) * (float) Math.PI * 0.05F;
		this.Shape11.xRot = -0.523F + Mth.cos(f2 * 0.1F) * (float) Math.PI * 0.05F;
	}
}
