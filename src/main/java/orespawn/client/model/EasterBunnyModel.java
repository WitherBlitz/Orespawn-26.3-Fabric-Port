package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelEasterBunny. */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class EasterBunnyModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart lfoot;
	private final ModelPart lleg;
	private final ModelPart upperbody;
	private final ModelPart head;
	private final ModelPart nose;
	private final ModelPart lear;
	private final ModelPart lpaw;
	private final ModelPart rleg;
	private final ModelPart rfoot;
	private final ModelPart rear;
	private final ModelPart rpaw;
	private final float wingspeed;

	public EasterBunnyModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.body = root.getChild("body");
		this.tail = root.getChild("tail");
		this.lfoot = root.getChild("lfoot");
		this.lleg = root.getChild("lleg");
		this.upperbody = root.getChild("upperbody");
		this.head = root.getChild("head");
		this.nose = root.getChild("nose");
		this.lear = root.getChild("lear");
		this.lpaw = root.getChild("lpaw");
		this.rleg = root.getChild("rleg");
		this.rfoot = root.getChild("rfoot");
		this.rear = root.getChild("rear");
		this.rpaw = root.getChild("rpaw");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelEasterBunny");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		float newangle2 = 0.0F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 2.6F * this.wingspeed) * (float) Math.PI * 0.15F * f1;
		newangle2 = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.1F * f1;
		} else {
		newangle = 0.0F;
		newangle2 = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.01F;
		}
		this.lleg.xRot = this.lfoot.xRot = newangle;
		this.rleg.xRot = this.rfoot.xRot = -newangle;
		this.lear.xRot = -0.226F + newangle2;
		this.rear.xRot = -0.418F - newangle2;
	}
}
