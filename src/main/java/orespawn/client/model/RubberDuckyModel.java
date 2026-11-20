package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelRubberDucky; state.flag: an evil duck (kill count 5 or more). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class RubberDuckyModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart bottom;
	private final ModelPart body;
	private final ModelPart back;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart beak;
	private final ModelPart Lwing;
	private final ModelPart Rwing;
	private final float wingspeed;

	public RubberDuckyModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.bottom = root.getChild("bottom");
		this.body = root.getChild("body");
		this.back = root.getChild("back");
		this.neck = root.getChild("neck");
		this.head = root.getChild("head");
		this.beak = root.getChild("beak");
		this.Lwing = root.getChild("lwing");
		this.Rwing = root.getChild("rwing");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelRubberDucky");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		this.head.yRot = (float)Math.toRadians(f3) * 0.45F;
		this.beak.yRot = this.head.yRot;
		this.head.xRot = (float)Math.toRadians(f4) * 0.65F;
		this.beak.xRot = this.head.xRot;
		// OreSpawn re-rolled, each time the flap wave came round, whether this beat flaps (1 in 3, evil ducks more often);
		// here the roll is fixed per beat so it needs no state kept between frames.
		newangle = Mth.cos(f2 * 1.0F * this.wingspeed) * (float) Math.PI * 0.15F;
		int beat = Mth.floor((f2 * this.wingspeed + (float) Math.PI / 2.0F) / ((float) Math.PI * 2.0F));
		int roll = Mth.murmurHash3Mixer(beat * 31 + 7) & 0x7FFFFFFF;
		boolean flap = roll % 3 == 1 || state.flag && (roll / 3) % 2 == 1;
		if (!flap || state.sitting) {
		newangle = 0.0F;
		}
		newangle = Math.abs(newangle);
		this.Lwing.zRot = -newangle;
		this.Lwing.yRot = newangle / 2.0F;
		this.Rwing.zRot = newangle;
		this.Rwing.yRot = -newangle / 2.0F;
	}
}
