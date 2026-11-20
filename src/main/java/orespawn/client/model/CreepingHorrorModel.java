package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelCreepingHorror (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CreepingHorrorModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart leg1;
	private final ModelPart leg1part2;
	private final ModelPart leg2;
	private final ModelPart leg2part2;
	private final ModelPart leg3;
	private final ModelPart leg3part2;
	private final ModelPart leg4;
	private final ModelPart leg4part2;
	private final ModelPart tailseg1;
	private final ModelPart tailseg2;
	private final ModelPart tailseg3;
	private final ModelPart pincer1;
	private final ModelPart pincer1part2;
	private final ModelPart pincer2;
	private final ModelPart pincer2part2;
	private final ModelPart spike1;
	private final ModelPart spike2;
	private final ModelPart spike3;
	private final ModelPart spike4;
	private final ModelPart spike5;
	private final ModelPart insides1;
	private final ModelPart insides2;
	private final ModelPart insides3;
	private final ModelPart insides4;
	private final ModelPart insides5;

	public CreepingHorrorModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.leg1 = root.getChild("leg1");
		this.leg1part2 = root.getChild("leg1part2");
		this.leg2 = root.getChild("leg2");
		this.leg2part2 = root.getChild("leg2part2");
		this.leg3 = root.getChild("leg3");
		this.leg3part2 = root.getChild("leg3part2");
		this.leg4 = root.getChild("leg4");
		this.leg4part2 = root.getChild("leg4part2");
		this.tailseg1 = root.getChild("tailseg1");
		this.tailseg2 = root.getChild("tailseg2");
		this.tailseg3 = root.getChild("tailseg3");
		this.pincer1 = root.getChild("pincer1");
		this.pincer1part2 = root.getChild("pincer1part2");
		this.pincer2 = root.getChild("pincer2");
		this.pincer2part2 = root.getChild("pincer2part2");
		this.spike1 = root.getChild("spike1");
		this.spike2 = root.getChild("spike2");
		this.spike3 = root.getChild("spike3");
		this.spike4 = root.getChild("spike4");
		this.spike5 = root.getChild("spike5");
		this.insides1 = root.getChild("insides1");
		this.insides2 = root.getChild("insides2");
		this.insides3 = root.getChild("insides3");
		this.insides4 = root.getChild("insides4");
		this.insides5 = root.getChild("insides5");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelCreepingHorror");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = Mth.cos(f2 * 1.25F) * (float) Math.PI * 0.35F * f1;
		this.leg1.yRot = 0.576F + newangle;
		this.leg1part2.yRot = this.leg1.yRot;
		this.leg2.yRot = -0.576F - newangle;
		this.leg2part2.yRot = this.leg2.yRot;
		this.leg3.yRot = -0.576F - newangle;
		this.leg3part2.yRot = this.leg3.yRot;
		this.leg4.yRot = 0.576F + newangle;
		this.leg4part2.yRot = this.leg4.yRot;
		newangle = Mth.cos(f2 * 0.48F) * (float) Math.PI * 0.15F;
		this.pincer1.yRot = newangle;
		this.pincer1part2.yRot = newangle;
		this.pincer2.yRot = -newangle;
		this.pincer2part2.yRot = -newangle;
		newangle = Mth.cos(f2 * 0.11F) * (float) Math.PI * 0.25F;
		newangle = Math.abs(newangle);
		this.tailseg1.xRot = -0.55F + newangle;
		this.tailseg3.xRot = -0.22F + newangle;
		this.tailseg2.xRot = newangle;
		newangle = Mth.cos(f2 * 0.81F) * (float) Math.PI * 0.08F;
		this.spike1.xRot = 0.7F + newangle;
		newangle = Mth.cos(f2 * 0.87F) * (float) Math.PI * 0.08F;
		this.spike2.xRot = 0.7F + newangle;
		newangle = Mth.cos(f2 * 0.99F) * (float) Math.PI * 0.08F;
		this.spike3.xRot = 0.7F + newangle;
		newangle = Mth.cos(f2 * 0.103F) * (float) Math.PI * 0.08F;
		this.spike4.xRot = 0.7F + newangle;
		newangle = Mth.cos(f2 * 0.107F) * (float) Math.PI * 0.08F;
		this.spike5.xRot = 0.7F + newangle;
		newangle = Mth.cos(f2 * 1.11F) * (float) Math.PI * 0.08F;
		this.spike1.yRot = newangle;
		newangle = Mth.cos(f2 * 1.17F) * (float) Math.PI * 0.08F;
		this.spike2.yRot = newangle;
		newangle = Mth.cos(f2 * 1.25F) * (float) Math.PI * 0.08F;
		this.spike3.yRot = newangle;
		newangle = Mth.cos(f2 * 1.28F) * (float) Math.PI * 0.08F;
		this.spike4.yRot = newangle;
		newangle = Mth.cos(f2 * 1.31F) * (float) Math.PI * 0.08F;
		this.spike5.yRot = newangle;
		newangle = Mth.cos(f2 * 1.41F) * (float) Math.PI * 0.08F;
		this.spike1.zRot = newangle;
		newangle = Mth.cos(f2 * 1.47F) * (float) Math.PI * 0.08F;
		this.spike2.zRot = newangle;
		newangle = Mth.cos(f2 * 1.55F) * (float) Math.PI * 0.08F;
		this.spike3.zRot = newangle;
		newangle = Mth.cos(f2 * 1.58F) * (float) Math.PI * 0.08F;
		this.spike4.zRot = newangle;
		newangle = Mth.cos(f2 * 1.61F) * (float) Math.PI * 0.08F;
		this.spike5.zRot = newangle;
	}
}
