package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelTshirt: a huge flat shirt that slowly turns. */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class TshirtModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Shape1;
	private final ModelPart Shape2;
	private final float wingspeed;

	public TshirtModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Shape1 = root.getChild("shape1");
		this.Shape2 = root.getChild("shape2");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelTshirt");
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
		this.Shape1.yRot = newangle;
		this.Shape2.yRot = newangle;
	}
}
