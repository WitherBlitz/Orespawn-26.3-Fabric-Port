package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelChipmunk (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class ChipmunkModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Cheek2;
	private final ModelPart Leg1;
	private final ModelPart Leg2;
	private final ModelPart Leg3;
	private final ModelPart Leg4;
	private final ModelPart Tail2;
	private final ModelPart Neck;
	private final ModelPart Head;
	private final ModelPart MouthUnder;
	private final ModelPart Cheek1;
	private final ModelPart Ear2;
	private final ModelPart Nose;
	private final ModelPart Ear1;
	private final ModelPart Body;
	private final ModelPart BodyTail;
	private final ModelPart Tail1;
	private final ModelPart Hat1;
	private final ModelPart Hat2;
	private final float wingspeed;

	public ChipmunkModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Cheek2 = root.getChild("cheek2");
		this.Leg1 = root.getChild("leg1");
		this.Leg2 = root.getChild("leg2");
		this.Leg3 = root.getChild("leg3");
		this.Leg4 = root.getChild("leg4");
		this.Tail2 = root.getChild("tail2");
		this.Neck = root.getChild("neck");
		this.Head = root.getChild("head");
		this.MouthUnder = root.getChild("mouthunder");
		this.Cheek1 = root.getChild("cheek1");
		this.Ear2 = root.getChild("ear2");
		this.Nose = root.getChild("nose");
		this.Ear1 = root.getChild("ear1");
		this.Body = root.getChild("body");
		this.BodyTail = root.getChild("bodytail");
		this.Tail1 = root.getChild("tail1");
		this.Hat1 = root.getChild("hat1");
		this.Hat2 = root.getChild("hat2");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelChipmunk");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float hf = 0.0F;
		float newangle = 0.0F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 2.3F * this.wingspeed) * (float) Math.PI * 0.25F * f1;
		} else {
		newangle = 0.0F;
		}
		this.Leg1.xRot = newangle;
		this.Leg3.xRot = newangle;
		this.Leg2.xRot = -newangle;
		this.Leg4.xRot = -newangle;
		this.Head.yRot = (float)Math.toRadians(f3) * 0.45F;
		this.Nose.yRot = this.Head.yRot;
		this.Ear1.yRot = this.Head.yRot;
		this.Ear2.yRot = this.Head.yRot;
		this.MouthUnder.yRot = this.Head.yRot;
		this.Cheek1.yRot = this.Head.yRot;
		this.Cheek2.yRot = this.Head.yRot;
		this.Hat1.yRot = this.Head.yRot;
		this.Hat2.yRot = this.Head.yRot;
		if (!state.sitting) {
		this.Tail1.xRot = 0.306F + Mth.cos(f2 * 0.25F) * (float) Math.PI * 0.06F;
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.25F * f1;
		this.Tail1.xRot += newangle;
		this.Tail2.xRot = 0.306F + this.Tail1.xRot;
		}
		// The hats show only on activated "cannon fodder" chipmunks (not ported yet).
		this.Hat1.visible = false;
		this.Hat2.visible = false;
	}
}
