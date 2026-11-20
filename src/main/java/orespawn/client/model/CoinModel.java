package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelCoin: a huge flat coin that slowly turns. */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CoinModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Shape1;
	private final float wingspeed;

	public CoinModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Shape1 = root.getChild("shape1");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelCoin");
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
	}
}
