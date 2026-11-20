package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelIsland (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class IslandModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Shape1;
	private final ModelPart Shape2;
	private final ModelPart Shape3;
	private float wingspeed = 1.0F;

	public IslandModel(ModelPart root) {
		super(root);
		this.Shape1 = root.getChild("shape1");
		this.Shape2 = root.getChild("shape2");
		this.Shape3 = root.getChild("shape3");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelIsland");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		newangle = Mth.cos(f2 * 0.05F * this.wingspeed) * (float) Math.PI;
		this.Shape1.xRot = newangle;
		newangle = Mth.cos(f2 * 0.051F * this.wingspeed) * (float) Math.PI;
		this.Shape1.yRot = newangle;
		newangle = Mth.cos(f2 * 0.052F * this.wingspeed) * (float) Math.PI;
		this.Shape1.zRot = newangle;
		newangle = Mth.cos(f2 * 0.053F * this.wingspeed) * (float) Math.PI;
		this.Shape2.xRot = newangle;
		newangle = Mth.cos(f2 * 0.054F * this.wingspeed) * (float) Math.PI;
		this.Shape2.yRot = newangle;
		newangle = Mth.cos(f2 * 0.055F * this.wingspeed) * (float) Math.PI;
		this.Shape2.zRot = newangle;
		newangle = Mth.cos(f2 * 0.056F * this.wingspeed) * (float) Math.PI;
		this.Shape3.xRot = newangle;
		newangle = Mth.cos(f2 * 0.057F * this.wingspeed) * (float) Math.PI;
		this.Shape3.yRot = newangle;
		newangle = Mth.cos(f2 * 0.058F * this.wingspeed) * (float) Math.PI;
		this.Shape3.zRot = newangle;
	}
}
