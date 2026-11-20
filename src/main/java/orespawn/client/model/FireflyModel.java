package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelFirefly (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class FireflyModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart wing_left;
	private final ModelPart wing_right;
	private final ModelPart head;
	private final ModelPart mouth;
	private final ModelPart eye_left;
	private final ModelPart eye_right;
	private final ModelPart front_leg_left_;
	private final ModelPart front_leg_right;
	private final ModelPart back_leg_left;
	private final ModelPart back_leg_right;
	private final ModelPart TailLight;
	private final float wingspeed;

	public FireflyModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.body = root.getChild("body");
		this.wing_left = root.getChild("wing_left");
		this.wing_right = root.getChild("wing_right");
		this.head = root.getChild("head");
		this.mouth = root.getChild("mouth");
		this.eye_left = root.getChild("eye_left");
		this.eye_right = root.getChild("eye_right");
		this.front_leg_left_ = root.getChild("front_leg_left_");
		this.front_leg_right = root.getChild("front_leg_right");
		this.back_leg_left = root.getChild("back_leg_left");
		this.back_leg_right = root.getChild("back_leg_right");
		this.TailLight = root.getChild("taillight");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelFirefly");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		this.wing_left.zRot = 1.11F + Mth.cos(f2 * this.wingspeed) * (float) Math.PI * 0.35F;
		this.wing_right.zRot = -1.11F - Mth.cos(f2 * this.wingspeed) * (float) Math.PI * 0.35F;
	}
}
