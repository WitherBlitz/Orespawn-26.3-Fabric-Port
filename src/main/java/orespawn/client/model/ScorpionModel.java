package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelScorpion (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels); ClientProxyOreSpawn passed it speed 0.62. */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class ScorpionModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart tail3;
	private final ModelPart tail4;
	private final ModelPart tail5;
	private final ModelPart tail6;
	private final ModelPart lleg1;
	private final ModelPart rleg1;
	private final ModelPart rleg2;
	private final ModelPart lleg3;
	private final ModelPart rleg4;
	private final ModelPart rleg3;
	private final ModelPart lleg4;
	private final ModelPart lleg2;
	private final ModelPart head;
	private final ModelPart larm2;
	private final ModelPart rarm2;
	private final ModelPart larm1;
	private final ModelPart rarm1;
	private final ModelPart lclaw;
	private final ModelPart rclaw;
	private final float wingspeed;

	public ScorpionModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.body = root.getChild("body");
		this.tail1 = root.getChild("tail1");
		this.tail2 = root.getChild("tail2");
		this.tail3 = root.getChild("tail3");
		this.tail4 = root.getChild("tail4");
		this.tail5 = root.getChild("tail5");
		this.tail6 = root.getChild("tail6");
		this.lleg1 = root.getChild("lleg1");
		this.rleg1 = root.getChild("rleg1");
		this.rleg2 = root.getChild("rleg2");
		this.lleg3 = root.getChild("lleg3");
		this.rleg4 = root.getChild("rleg4");
		this.rleg3 = root.getChild("rleg3");
		this.lleg4 = root.getChild("lleg4");
		this.lleg2 = root.getChild("lleg2");
		this.head = root.getChild("head");
		this.larm2 = root.getChild("larm2");
		this.rarm2 = root.getChild("rarm2");
		this.larm1 = root.getChild("larm1");
		this.rarm1 = root.getChild("rarm1");
		this.lclaw = root.getChild("lclaw");
		this.rclaw = root.getChild("rclaw");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelScorpion");
	}

	/**
	 * ModelScorpion.render. The claw and tail strikes OreSpawn picked at random into the scorpion's RenderInfo come
	 * from the entity ({@code Scorpion.clawAnimation}) in {@code state.variant}: bit 0 left claw, bit 1 right claw,
	 * bit 2 tail.
	 */
	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f1 = state.walkAnimationSpeed, f2 = state.ageInTicks;
		float pi4 = 1.570795F;
		float newangle = Mth.cos(f2 * 2.0F * this.wingspeed) * (float) Math.PI * 0.12F * f1;
		this.lleg1.yRot = newangle + 0.49F;
		this.rleg1.yRot = -newangle + 2.65F;
		newangle = Mth.cos(f2 * 2.0F * this.wingspeed - 1.0F * pi4) * (float) Math.PI * 0.12F * f1;
		this.lleg2.yRot = newangle + 0.24F;
		this.rleg2.yRot = -newangle + 2.9F;
		newangle = Mth.cos(f2 * 2.0F * this.wingspeed - 2.0F * pi4) * (float) Math.PI * 0.12F * f1;
		this.lleg3.yRot = newangle - 0.24F;
		this.rleg3.yRot = -newangle - 2.9F;
		newangle = Mth.cos(f2 * 2.0F * this.wingspeed - 3.0F * pi4) * (float) Math.PI * 0.12F * f1;
		this.lleg4.yRot = newangle - 0.49F;
		this.rleg4.yRot = -newangle - 2.65F;
		newangle = Mth.cos(f2 * 3.0F * this.wingspeed) * (float) Math.PI * 0.15F;
		this.doLeftClaw((state.variant & 1) != 0 ? newangle : 0.0F);
		this.doRightClaw((state.variant & 2) != 0 ? newangle : 0.0F);
		this.doTail((state.variant & 4) != 0 ? newangle : 0.0F);
	}

	private void doLeftClaw(float angle) {
		this.larm2.yRot = 0.52F + angle;
		this.larm1.z = (float) (this.larm2.z - Math.sin(this.larm2.yRot) * 4.5);
		this.lclaw.z = this.larm1.z - 3.0F;
		this.lclaw.yRot = 0.381F - angle;
	}

	private void doRightClaw(float angle) {
		this.rarm2.yRot = 2.61F - angle;
		this.rarm1.z = (float) (this.rarm2.z - Math.sin(this.rarm2.yRot) * 4.5);
		this.rclaw.z = this.rarm1.z - 3.0F;
		this.rclaw.yRot = -0.381F + angle;
	}

	private void doTail(float angle) {
		this.tail1.xRot = 0.26F + angle;
		this.tail2.xRot = this.tail1.xRot + 0.76900005F + angle;
		this.tail2.y = (float) (this.tail1.y - Math.sin(this.tail1.xRot) * 4.0);
		this.tail2.z = (float) (this.tail1.z + Math.cos(this.tail1.xRot) * 4.0);
		this.tail3.xRot = this.tail2.xRot + 0.701F + angle;
		this.tail3.y = (float) (this.tail2.y - Math.sin(this.tail2.xRot) * 4.0);
		this.tail3.z = (float) (this.tail2.z + Math.cos(this.tail2.xRot) * 4.0);
		this.tail4.xRot = this.tail3.xRot + -5.501F - angle * 3.0F / 2.0F - 0.4F;
		this.tail4.y = (float) (this.tail3.y - Math.sin(this.tail3.xRot) * 3.0);
		this.tail4.z = (float) (this.tail3.z + Math.cos(this.tail3.xRot) * 3.0);
		this.tail5.y = (float) (this.tail4.y - Math.sin(this.tail4.xRot) * 4.0);
		this.tail5.z = (float) (this.tail4.z + Math.cos(this.tail4.xRot) * 4.0);
		this.tail6.y = (float) (this.tail5.y - Math.sin(this.tail5.xRot) * 4.0);
		this.tail6.z = (float) (this.tail5.z + Math.cos(this.tail5.xRot) * 4.0);
	}
}
