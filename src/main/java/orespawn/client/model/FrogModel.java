package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelFrog (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class FrogModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart jaw;
	private final ModelPart lfleg;
	private final ModelPart rfleg;
	private final ModelPart lleg1;
	private final ModelPart rleg1;
	private final ModelPart lleg2;
	private final ModelPart rleg2;
	private final ModelPart leye;
	private final ModelPart reye;
	private final float wingspeed;

	public FrogModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.body = root.getChild("body");
		this.jaw = root.getChild("jaw");
		this.lfleg = root.getChild("lfleg");
		this.rfleg = root.getChild("rfleg");
		this.lleg1 = root.getChild("lleg1");
		this.rleg1 = root.getChild("rleg1");
		this.lleg2 = root.getChild("lleg2");
		this.rleg2 = root.getChild("rleg2");
		this.leye = root.getChild("leye");
		this.reye = root.getChild("reye");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelFrog");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float newangle = 0.0F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * this.wingspeed * 1.4F) * (float) Math.PI * 0.55F * f1;
		} else {
		newangle = 0.0F;
		}
		this.lfleg.yRot = newangle;
		this.rfleg.yRot = -newangle;
		this.lleg2.yRot = -newangle / 2.0F;
		this.rleg2.yRot = newangle / 2.0F;
		if (state.singing) {
		newangle = Mth.cos(f2 * 0.85F * this.wingspeed) * (float) Math.PI * 0.15F;
		} else {
		newangle = 0.0F;
		}
		this.jaw.xRot = newangle + 1.22F;
		if (!(state.verticalSpeed > 0.1F) && !(state.verticalSpeed < -0.1F)) {
		this.lleg1.zRot = 0.227F;
		this.rleg1.zRot = -0.227F;
		} else {
		this.lleg1.zRot = 2.44F;
		this.rleg1.zRot = -2.44F;
		}
		this.lleg2.y = this.lleg1.y - (float)Math.cos(this.lleg1.zRot) * 9.0F;
		this.lleg2.x = this.lleg1.x + (float)Math.sin(this.lleg1.zRot) * 9.0F;
		this.rleg2.y = this.rleg1.y - (float)Math.cos(this.rleg1.zRot) * 9.0F;
		this.rleg2.x = this.rleg1.x + (float)Math.sin(this.rleg1.zRot) * 9.0F;
	}
}
