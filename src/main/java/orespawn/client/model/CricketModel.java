package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelCricket (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CricketModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart abdomen;
	private final ModelPart lfleg;
	private final ModelPart lrleg;
	private final ModelPart rfleg;
	private final ModelPart rrleg;
	private final ModelPart lleg1;
	private final ModelPart rleg1;
	private final ModelPart lleg2;
	private final ModelPart rleg2;
	private final float wingspeed;

	public CricketModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.abdomen = root.getChild("abdomen");
		this.lfleg = root.getChild("lfleg");
		this.lrleg = root.getChild("lrleg");
		this.rfleg = root.getChild("rfleg");
		this.rrleg = root.getChild("rrleg");
		this.lleg1 = root.getChild("lleg1");
		this.rleg1 = root.getChild("rleg1");
		this.lleg2 = root.getChild("lleg2");
		this.rleg2 = root.getChild("rleg2");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelCricket");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float newangle = 0.0F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * this.wingspeed) * (float) Math.PI * 0.25F * f1;
		} else {
		newangle = 0.0F;
		}
		this.lfleg.yRot = 0.47F + newangle;
		this.rfleg.yRot = -0.54F + newangle;
		this.lrleg.yRot = -0.296F - newangle;
		this.rrleg.yRot = 0.384F - newangle;
		if (state.singing) {
		newangle = Mth.cos(f2 * 3.0F * this.wingspeed) * (float) Math.PI * 0.25F;
		this.lleg1.yRot = -0.035F;
		this.lleg2.yRot = -0.105F;
		this.rleg1.yRot = 0.035F;
		this.rleg2.yRot = 0.105F;
		} else {
		newangle = 0.0F;
		this.lleg1.yRot = 0.436F;
		this.lleg2.yRot = 0.349F;
		this.rleg1.yRot = -0.436F;
		this.rleg2.yRot = -0.349F;
		}
		this.lleg1.xRot = newangle + 0.558F;
		this.lleg2.xRot = newangle - 0.366F;
		this.rleg1.xRot = -newangle + 0.558F;
		this.rleg2.xRot = -newangle - 0.366F;
	}
}
