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
 * Ported from OreSpawn's ModelFairy (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). OreSpawn drew the wings with the
 * world's light, then the body with its own lightmap (full sky light, and full block light while the fairy blinks).
 * The renderer draws the whole fairy normally, then the body again through a second copy of this model made with
 * {@code bodyOnly} at that light.
 */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class FairyModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart head;
	private final ModelPart chest;
	private final ModelPart waist;
	private final ModelPart hips;
	private final ModelPart lleg1;
	private final ModelPart lleg2;
	private final ModelPart rleg;
	private final ModelPart b1;
	private final ModelPart b2;
	private final ModelPart larm;
	private final ModelPart rarm;
	private final ModelPart lwing2;
	private final ModelPart lwing1;
	private final ModelPart rwing2;
	private final ModelPart rwing1;
	private final float wingspeed;

	public FairyModel(ModelPart root, float wingspeed) {
		this(root, wingspeed, false);
	}

	/** bodyOnly: leave the wings out (the body's second, glowing pass). */
	public FairyModel(ModelPart root, float wingspeed, boolean bodyOnly) {
		super(root);
		this.wingspeed = wingspeed;
		this.head = root.getChild("head");
		this.chest = root.getChild("chest");
		this.waist = root.getChild("waist");
		this.hips = root.getChild("hips");
		this.lleg1 = root.getChild("lleg1");
		this.lleg2 = root.getChild("lleg2");
		this.rleg = root.getChild("rleg");
		this.b1 = root.getChild("b1");
		this.b2 = root.getChild("b2");
		this.larm = root.getChild("larm");
		this.rarm = root.getChild("rarm");
		this.lwing2 = root.getChild("lwing2");
		this.lwing1 = root.getChild("lwing1");
		this.rwing2 = root.getChild("rwing2");
		this.rwing1 = root.getChild("rwing1");
		if (bodyOnly) {
			this.lwing1.visible = this.lwing2.visible = this.rwing1.visible = this.rwing2.visible = false;
		}
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelFairy");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		this.lwing1.yRot = -0.6F + Mth.cos(f2 * this.wingspeed) * (float) Math.PI * 0.35F;
		this.rwing1.yRot = -2.55F - Mth.cos(f2 * this.wingspeed) * (float) Math.PI * 0.35F;
		this.lwing2.yRot = -0.6F + Mth.cos(f2 * this.wingspeed * 0.85F) * (float) Math.PI * 0.25F;
		this.rwing2.yRot = -2.55F - Mth.cos(f2 * this.wingspeed * 0.85F) * (float) Math.PI * 0.25F;
		this.head.yRot = (float)Math.toRadians(f3) * 0.45F;
		if (this.head.yRot > 0.45F) {
		this.head.yRot = 0.45F;
		}
		if (this.head.yRot < -0.45F) {
		this.head.yRot = -0.45F;
		}
		this.head.xRot = (float)Math.toRadians(f4);
		this.larm.xRot = -0.2F + Mth.cos(f2 * this.wingspeed * 0.15F) * (float) Math.PI * 0.05F;
		this.rarm.xRot = -0.2F + Mth.cos(f2 * this.wingspeed * 0.12F) * (float) Math.PI * 0.05F;
		this.larm.zRot = -0.15F + Mth.cos(f2 * this.wingspeed * 0.1F) * (float) Math.PI * 0.03F;
		this.rarm.zRot = 0.15F + Mth.cos(f2 * this.wingspeed * 0.11F) * (float) Math.PI * 0.03F;
		// fly.getBlink() and the body's lightmap: see the class comment.
	}
}
