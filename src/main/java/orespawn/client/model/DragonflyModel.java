package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelDragonfly (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class DragonflyModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Shape1;
	private final ModelPart lfwing;
	private final ModelPart Shape3;
	private final ModelPart Shape4;
	private final ModelPart Shape5;
	private final ModelPart rjaw;
	private final ModelPart ljaw;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart Shape10;
	private final ModelPart Shape11;
	private final ModelPart Shape12;
	private final ModelPart Shape13;
	private final ModelPart Shape14;
	private final ModelPart Shape15;
	private final ModelPart Shape16;
	private final ModelPart Shape17;
	private final ModelPart Shape18;
	private final ModelPart Shape19;
	private final ModelPart Shape20;
	private final ModelPart Shape21;
	private final ModelPart Shape22;
	private final ModelPart Shape23;
	private final ModelPart lrwing;
	private final ModelPart rfwing;
	private final ModelPart rrwing;
	private final float wingspeed;

	public DragonflyModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Shape1 = root.getChild("shape1");
		this.lfwing = root.getChild("lfwing");
		this.Shape3 = root.getChild("shape3");
		this.Shape4 = root.getChild("shape4");
		this.Shape5 = root.getChild("shape5");
		this.rjaw = root.getChild("rjaw");
		this.ljaw = root.getChild("ljaw");
		this.tail1 = root.getChild("tail1");
		this.tail2 = root.getChild("tail2");
		this.Shape10 = root.getChild("shape10");
		this.Shape11 = root.getChild("shape11");
		this.Shape12 = root.getChild("shape12");
		this.Shape13 = root.getChild("shape13");
		this.Shape14 = root.getChild("shape14");
		this.Shape15 = root.getChild("shape15");
		this.Shape16 = root.getChild("shape16");
		this.Shape17 = root.getChild("shape17");
		this.Shape18 = root.getChild("shape18");
		this.Shape19 = root.getChild("shape19");
		this.Shape20 = root.getChild("shape20");
		this.Shape21 = root.getChild("shape21");
		this.Shape22 = root.getChild("shape22");
		this.Shape23 = root.getChild("shape23");
		this.lrwing = root.getChild("lrwing");
		this.rfwing = root.getChild("rfwing");
		this.rrwing = root.getChild("rrwing");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelDragonfly");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float newangle = 0.0F;
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.25F;
		this.lfwing.zRot = newangle;
		this.rfwing.zRot = -newangle;
		this.lrwing.zRot = newangle + 3.14F;
		this.rrwing.zRot = -newangle + 3.14F;
		newangle = Mth.cos(f2 * 0.3F * this.wingspeed) * (float) Math.PI * 0.1F;
		this.ljaw.xRot = newangle;
		this.rjaw.xRot = -newangle;
	}
}
