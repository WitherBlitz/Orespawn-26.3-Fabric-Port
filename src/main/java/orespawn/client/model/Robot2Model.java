package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelRobot2 (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class Robot2Model extends EntityModel<OreSpawnRenderState> {
	private final ModelPart rleg1;
	private final ModelPart rleg2;
	private final ModelPart Shape3;
	private final ModelPart lleg2;
	private final ModelPart lleg1;
	private final ModelPart Shape6;
	private final ModelPart Shape7;
	private final ModelPart Shape8;
	private final ModelPart rarm3;
	private final ModelPart rarm2;
	private final ModelPart rarm1;
	private final ModelPart larm3;
	private final ModelPart larm2;
	private final ModelPart larm1;
	private final ModelPart head;
	private final float wingspeed;

	public Robot2Model(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.rleg1 = root.getChild("rleg1");
		this.rleg2 = root.getChild("rleg2");
		this.Shape3 = root.getChild("shape3");
		this.lleg2 = root.getChild("lleg2");
		this.lleg1 = root.getChild("lleg1");
		this.Shape6 = root.getChild("shape6");
		this.Shape7 = root.getChild("shape7");
		this.Shape8 = root.getChild("shape8");
		this.rarm3 = root.getChild("rarm3");
		this.rarm2 = root.getChild("rarm2");
		this.rarm1 = root.getChild("rarm1");
		this.larm3 = root.getChild("larm3");
		this.larm2 = root.getChild("larm2");
		this.larm1 = root.getChild("larm1");
		this.head = root.getChild("head");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelRobot2");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// From render(); state.value is which arms pound this cycle (OreSpawn's RenderInfo.ri1: 1 right, 2 left, 3 both).
		float newangle;
		if (f1 > 0.1) {
			newangle = Mth.cos(f2 * 0.3F * this.wingspeed) * (float) Math.PI * 0.12F * f1;
		} else {
			newangle = 0.0F;
		}
		this.lleg1.xRot = newangle;
		this.lleg2.xRot = newangle;
		this.rleg1.xRot = -newangle;
		this.rleg2.xRot = -newangle;
		this.head.yRot = (float) Math.toRadians(f3);
		int arms = (int) state.value;
		newangle = (float) Math.toRadians(f2 * 20.0F * this.wingspeed);
		if (arms != 1 && arms != 3) {
			this.rarm1.xRot = 0.0F;
			this.rarm2.xRot = 0.0F;
			this.rarm3.xRot = 0.0F;
		} else {
			this.rarm1.xRot = newangle;
			this.rarm2.xRot = newangle;
			this.rarm3.xRot = newangle;
		}
		if (arms != 2 && arms != 3) {
			this.larm1.xRot = 0.0F;
			this.larm2.xRot = 0.0F;
			this.larm3.xRot = 0.0F;
		} else {
			this.larm1.xRot = newangle;
			this.larm2.xRot = newangle;
			this.larm3.xRot = newangle;
		}
	}

	/**
	 * OreSpawn's choice of arms, made as each pounding cycle starts (when the swing passes zero): none unless the
	 * robot is attacking, otherwise right, left or both at random. Kept on the client entity between frames.
	 */
	public static int nextArms(int current, float ageInTicks, boolean attacking, float wingspeed) {
		float now = Mth.sin((float) Math.toRadians(ageInTicks * 20.0F * wingspeed));
		float next = Mth.sin((float) Math.toRadians(ageInTicks * 20.0F * wingspeed + 1.5F));
		if (next > 0.0F && now < 0.0F) return attacking ? 1 + java.util.concurrent.ThreadLocalRandom.current().nextInt(3) : 0;
		return current;
	}
}
