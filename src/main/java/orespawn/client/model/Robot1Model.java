package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelRobot1 (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class Robot1Model extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Shape1;
	private final ModelPart Shape2;
	private final ModelPart Shape2a;
	private final ModelPart Shape3;
	private final ModelPart Shape4;
	private final ModelPart Shape5;
	private final ModelPart Shape6;
	private final ModelPart Shape7;
	private final ModelPart Shape8;
	private final ModelPart Shape9;
	private final ModelPart Shape10;
	private final ModelPart Shape11;
	private final ModelPart Shape12;
	private final ModelPart Shape13;
	private final ModelPart Shape14;
	private final ModelPart Shape15;
	private final ModelPart Shape15a;
	private final ModelPart Shape16;
	private final ModelPart Shape17;
	private final ModelPart Shape18;
	private final ModelPart rfoot;
	private final ModelPart lfoot;
	private final ModelPart key2;
	private final ModelPart key1;
	private final ModelPart key3;
	private final ModelPart key4;
	private final ModelPart key5;
	private final float wingspeed;

	public Robot1Model(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Shape1 = root.getChild("shape1");
		this.Shape2 = root.getChild("shape2");
		this.Shape2a = root.getChild("shape2a");
		this.Shape3 = root.getChild("shape3");
		this.Shape4 = root.getChild("shape4");
		this.Shape5 = root.getChild("shape5");
		this.Shape6 = root.getChild("shape6");
		this.Shape7 = root.getChild("shape7");
		this.Shape8 = root.getChild("shape8");
		this.Shape9 = root.getChild("shape9");
		this.Shape10 = root.getChild("shape10");
		this.Shape11 = root.getChild("shape11");
		this.Shape12 = root.getChild("shape12");
		this.Shape13 = root.getChild("shape13");
		this.Shape14 = root.getChild("shape14");
		this.Shape15 = root.getChild("shape15");
		this.Shape15a = root.getChild("shape15a");
		this.Shape16 = root.getChild("shape16");
		this.Shape17 = root.getChild("shape17");
		this.Shape18 = root.getChild("shape18");
		this.rfoot = root.getChild("rfoot");
		this.lfoot = root.getChild("lfoot");
		this.key2 = root.getChild("key2");
		this.key1 = root.getChild("key1");
		this.key3 = root.getChild("key3");
		this.key4 = root.getChild("key4");
		this.key5 = root.getChild("key5");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelRobot1");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 1.5F * this.wingspeed) * (float) Math.PI * 0.75F * f1;
		} else {
		newangle = 0.0F;
		}
		this.lfoot.xRot = newangle;
		this.rfoot.xRot = -newangle;
		newangle = (float)Math.toRadians(f2 * 0.75F * this.wingspeed);
		this.key1.zRot = newangle;
		this.key2.zRot = newangle;
		this.key3.zRot = newangle;
		this.key4.zRot = newangle;
		this.key5.zRot = newangle;
	}
}
