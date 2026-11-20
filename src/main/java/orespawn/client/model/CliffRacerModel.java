package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelCliffRacer (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CliffRacerModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Body;
	private final ModelPart Fins;
	private final ModelPart LWing;
	private final ModelPart RWing;
	private final ModelPart Tail;
	private final ModelPart TailEnd;
	private final ModelPart Head;
	private final ModelPart Beak;
	private final float wingspeed;

	public CliffRacerModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Body = root.getChild("body");
		this.Fins = root.getChild("fins");
		this.LWing = root.getChild("lwing");
		this.RWing = root.getChild("rwing");
		this.Tail = root.getChild("tail");
		this.TailEnd = root.getChild("tailend");
		this.Head = root.getChild("head");
		this.Beak = root.getChild("beak");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelCliffRacer");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.25F;
		this.LWing.zRot = newangle;
		this.RWing.zRot = -newangle;
	}
}
