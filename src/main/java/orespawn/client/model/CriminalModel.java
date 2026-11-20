package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelBandP (the Criminal). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CriminalModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart belly;
	private final ModelPart chest;
	private final ModelPart head;
	private final ModelPart lleg;
	private final ModelPart rleg;
	private final ModelPart larm;
	private final ModelPart rarm;
	private final float wingspeed;

	public CriminalModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.belly = root.getChild("belly");
		this.chest = root.getChild("chest");
		this.head = root.getChild("head");
		this.lleg = root.getChild("lleg");
		this.rleg = root.getChild("rleg");
		this.larm = root.getChild("larm");
		this.rarm = root.getChild("rarm");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelBandP");
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
		float newangle3 = 0.0F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.25F * f1;
		newangle2 = Mth.cos(f2 * 2.6F * this.wingspeed) * (float) Math.PI * 0.025F * f1;
		newangle3 = newangle;
		} else {
		newangle = 0.0F;
		newangle2 = Mth.cos(f2 * 0.6F * this.wingspeed) * (float) Math.PI * 0.005F;
		newangle3 = Mth.cos(f2 * 0.3F * this.wingspeed) * (float) Math.PI * 0.02F;
		}
		this.lleg.xRot = newangle;
		this.rleg.xRot = -newangle;
		this.belly.xRot = 0.07F + newangle2;
		this.larm.xRot = -newangle3;
		this.rarm.xRot = newangle3;
		this.belly.yRot = -newangle / 2.0F;
		this.head.yRot = (float)Math.toRadians(f3);
		this.head.xRot = (float)Math.toRadians(f4);
	}
}
