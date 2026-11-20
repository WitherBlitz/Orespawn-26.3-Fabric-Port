package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelCloudShark (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CloudSharkModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart jaw;
	private final ModelPart topfin;
	private final ModelPart bbody;
	private final ModelPart fins;
	private final ModelPart leftfin;
	private final ModelPart rightfin;
	private final float wingspeed;

	public CloudSharkModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.jaw = root.getChild("jaw");
		this.topfin = root.getChild("topfin");
		this.bbody = root.getChild("bbody");
		this.fins = root.getChild("fins");
		this.leftfin = root.getChild("leftfin");
		this.rightfin = root.getChild("rightfin");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelCloudShark");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		newangle = Mth.cos(f2 * 0.7F * this.wingspeed) * (float) Math.PI * 0.15F;
		this.leftfin.yRot = 1.15F + newangle;
		newangle = Mth.cos(f2 * 1.5F * this.wingspeed) * (float) Math.PI * 0.15F;
		this.rightfin.yRot = -0.9F + newangle;
		newangle = Mth.cos(f2 * 1.5F * this.wingspeed) * (float) Math.PI * 0.25F;
		this.fins.yRot = newangle;
		newangle = Mth.cos(f2 * 0.5F * this.wingspeed) * (float) Math.PI * 0.1F;
		this.jaw.xRot = 0.5F + newangle;
	}
}
