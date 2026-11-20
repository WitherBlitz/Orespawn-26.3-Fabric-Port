package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/**
 * Ported from OreSpawn's ModelSkate (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). It takes OreSpawn's speed argument
 * (ClientProxyOreSpawn passes 1.0) like the other models, though this model never read it.
 */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class SkateModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart tail1;
	private final ModelPart Shape1;
	private final float wingspeed;

	public SkateModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.body = root.getChild("body");
		this.tail1 = root.getChild("tail1");
		this.Shape1 = root.getChild("shape1");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelSkate");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float newangle = 0.0F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 1.2F) * (float) Math.PI * 0.15F * f1;
		} else {
		newangle = Mth.cos(f2 * 0.4F) * (float) Math.PI * 0.05F;
		}
		this.Shape1.xRot = 0.785F + newangle;
	}
}
