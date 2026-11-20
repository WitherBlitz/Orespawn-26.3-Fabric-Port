package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelBrutalfly (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class BrutalflyModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart leftwing;
	private final ModelPart rightwing;
	private final ModelPart leftwing2;
	private final ModelPart rightwing2;
	private final ModelPart leftwing3;
	private final ModelPart rightwing3;
	private final ModelPart head;
	private final ModelPart leftwing4;
	private final ModelPart rightwing4;
	private final ModelPart leftwing5;
	private final ModelPart leftwing6;
	private final ModelPart rightwing5;
	private final ModelPart rightwing6;
	private final float wingspeed;

	public BrutalflyModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.body = root.getChild("body");
		this.leftwing = root.getChild("leftwing");
		this.rightwing = root.getChild("rightwing");
		this.leftwing2 = root.getChild("leftwing2");
		this.rightwing2 = root.getChild("rightwing2");
		this.leftwing3 = root.getChild("leftwing3");
		this.rightwing3 = root.getChild("rightwing3");
		this.head = root.getChild("head");
		this.leftwing4 = root.getChild("leftwing4");
		this.rightwing4 = root.getChild("rightwing4");
		this.leftwing5 = root.getChild("leftwing5");
		this.leftwing6 = root.getChild("leftwing6");
		this.rightwing5 = root.getChild("rightwing5");
		this.rightwing6 = root.getChild("rightwing6");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelBrutalfly");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		this.rightwing.zRot = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.25F;
		this.rightwing2.zRot = this.rightwing.zRot;
		this.rightwing3.zRot = this.rightwing.zRot;
		this.rightwing4.zRot = this.rightwing.zRot;
		this.rightwing5.zRot = this.rightwing.zRot;
		this.rightwing6.zRot = this.rightwing.zRot;
		this.leftwing.zRot = -this.rightwing.zRot;
		this.leftwing2.zRot = -this.rightwing.zRot;
		this.leftwing3.zRot = -this.rightwing.zRot;
		this.leftwing4.zRot = -this.rightwing.zRot;
		this.leftwing5.zRot = -this.rightwing.zRot;
		this.leftwing6.zRot = -this.rightwing.zRot;
	}
}
