package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelCryolophosaurus (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CryolophosaurusModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Shape1;
	private final ModelPart Shape2;
	private final ModelPart Shape3;
	private final ModelPart jaw;
	private final ModelPart Shape5;
	private final ModelPart Shape6;
	private final ModelPart Shape7;
	private final ModelPart Shape8;
	private final ModelPart Shape9;
	private final ModelPart rightleg;
	private final ModelPart Shape11;
	private final ModelPart rightleg2;
	private final ModelPart rightleg3;
	private final ModelPart rightleg4;
	private final ModelPart leftleg;
	private final ModelPart Shape16;
	private final ModelPart Shape17;
	private final ModelPart leftleg2;
	private final ModelPart leftleg3;
	private final ModelPart leftleg4;
	private final float wingspeed;

	public CryolophosaurusModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Shape1 = root.getChild("shape1");
		this.Shape2 = root.getChild("shape2");
		this.Shape3 = root.getChild("shape3");
		this.jaw = root.getChild("jaw");
		this.Shape5 = root.getChild("shape5");
		this.Shape6 = root.getChild("shape6");
		this.Shape7 = root.getChild("shape7");
		this.Shape8 = root.getChild("shape8");
		this.Shape9 = root.getChild("shape9");
		this.rightleg = root.getChild("rightleg");
		this.Shape11 = root.getChild("shape11");
		this.rightleg2 = root.getChild("rightleg2");
		this.rightleg3 = root.getChild("rightleg3");
		this.rightleg4 = root.getChild("rightleg4");
		this.leftleg = root.getChild("leftleg");
		this.Shape16 = root.getChild("shape16");
		this.Shape17 = root.getChild("shape17");
		this.leftleg2 = root.getChild("leftleg2");
		this.leftleg3 = root.getChild("leftleg3");
		this.leftleg4 = root.getChild("leftleg4");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelCryolophosaurus");
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
		this.rightleg.xRot = -0.2792527F + newangle;
		this.rightleg2.xRot = 0.384F + newangle;
		this.rightleg3.xRot = -0.68F + newangle;
		this.rightleg4.xRot = newangle;
		this.leftleg.xRot = -0.2792527F - newangle;
		this.leftleg2.xRot = 0.384F - newangle;
		this.leftleg3.xRot = -0.68F - newangle;
		this.leftleg4.xRot = -newangle;
		this.jaw.xRot = -1.15F + Mth.cos(f2 * 0.28F) * (float) Math.PI * 0.1F;
	}
}
