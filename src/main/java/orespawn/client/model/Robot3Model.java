package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelRobot3 (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class Robot3Model extends EntityModel<OreSpawnRenderState> {
	private final ModelPart rleg1;
	private final ModelPart lleg1;
	private final ModelPart rleg2;
	private final ModelPart lleg2;
	private final ModelPart hips;
	private final ModelPart waist1;
	private final ModelPart waist2;
	private final ModelPart body3;
	private final ModelPart lazer;
	private final ModelPart body2;
	private final ModelPart body1;
	private final ModelPart body4;
	private final ModelPart waist3;
	private final ModelPart larm3;
	private final ModelPart rarm3;
	private final ModelPart larm2;
	private final ModelPart rarm2;
	private final ModelPart larm1;
	private final ModelPart rarm1;
	private final float wingspeed;

	public Robot3Model(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.rleg1 = root.getChild("rleg1");
		this.lleg1 = root.getChild("lleg1");
		this.rleg2 = root.getChild("rleg2");
		this.lleg2 = root.getChild("lleg2");
		this.hips = root.getChild("hips");
		this.waist1 = root.getChild("waist1");
		this.waist2 = root.getChild("waist2");
		this.body3 = root.getChild("body3");
		this.lazer = root.getChild("lazer");
		this.body2 = root.getChild("body2");
		this.body1 = root.getChild("body1");
		this.body4 = root.getChild("body4");
		this.waist3 = root.getChild("waist3");
		this.larm3 = root.getChild("larm3");
		this.rarm3 = root.getChild("rarm3");
		this.larm2 = root.getChild("larm2");
		this.rarm2 = root.getChild("rarm2");
		this.larm1 = root.getChild("larm1");
		this.rarm1 = root.getChild("rarm1");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelRobot3");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// From render(); state.value is whether the arms pump this cycle (OreSpawn's RenderInfo.ri1).
		float newangle;
		if (f1 > 0.1) {
			newangle = Mth.cos(f2 * 0.55F * this.wingspeed) * (float) Math.PI * 0.12F * f1;
		} else {
			newangle = 0.0F;
		}
		this.lleg1.xRot = newangle;
		this.lleg2.xRot = newangle;
		this.rleg1.xRot = -newangle;
		this.rleg2.xRot = -newangle;
		this.lazer.yRot = (float) Math.toRadians(f3 / 2.0);
		newangle = Mth.cos(f2 * 1.0F * this.wingspeed) * (float) Math.PI * 0.15F;
		if (state.value == 0.0F) newangle = 0.0F;
		this.rarm1.xRot = newangle - 1.0F;
		this.rarm2.xRot = newangle + 1.0F;
		this.rarm3.xRot = newangle + 1.0F;
		this.larm1.xRot = newangle - 1.0F;
		this.larm2.xRot = newangle + 1.0F;
		this.larm3.xRot = newangle + 1.0F;
	}

	/** OreSpawn's choice, made as each arm cycle starts: pump while attacking. Kept on the client entity. */
	public static int nextArms(int current, float ageInTicks, boolean attacking, float wingspeed) {
		float now = Mth.cos(ageInTicks * 1.0F * wingspeed) * (float) Math.PI * 0.15F;
		float next = Mth.cos((ageInTicks + 0.3F) * 1.0F * wingspeed) * (float) Math.PI * 0.15F;
		if (next > 0.0F && now < 0.0F) return attacking ? 1 : 0;
		return current;
	}
}
