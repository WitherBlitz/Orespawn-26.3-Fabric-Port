package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelBasilisk (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class BasiliskModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body3;
	private final ModelPart body2;
	private final ModelPart body1;
	private final ModelPart body4;
	private final ModelPart body5;
	private final ModelPart body6;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart tail3;
	private final ModelPart tail4;
	private final ModelPart neck2;
	private final ModelPart neck1;
	private final ModelPart head;
	private final ModelPart rog_1;
	private final ModelPart rog_2;
	private final ModelPart rog_3;
	private final ModelPart rog_4;
	private final ModelPart rog_5;
	private final ModelPart rog_6;
	private final ModelPart snout;
	private final ModelPart jaw;
	private final float wingspeed;

	public BasiliskModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.body3 = root.getChild("body3");
		this.body2 = root.getChild("body2");
		this.body1 = root.getChild("body1");
		this.body4 = root.getChild("body4");
		this.body5 = root.getChild("body5");
		this.body6 = root.getChild("body6");
		this.tail1 = root.getChild("tail1");
		this.tail2 = root.getChild("tail2");
		this.tail3 = root.getChild("tail3");
		this.tail4 = root.getChild("tail4");
		this.neck2 = root.getChild("neck2");
		this.neck1 = root.getChild("neck1");
		this.head = root.getChild("head");
		this.rog_1 = root.getChild("rog_1");
		this.rog_2 = root.getChild("rog_2");
		this.rog_3 = root.getChild("rog_3");
		this.rog_4 = root.getChild("rog_4");
		this.rog_5 = root.getChild("rog_5");
		this.rog_6 = root.getChild("rog_6");
		this.snout = root.getChild("snout");
		this.jaw = root.getChild("jaw");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelBasilisk");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.1F * f1;
		} else {
		newangle = 0.0F;
		}
		float pi4 = 0.7853975F;
		this.body1.yRot = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.1F * f1;
		this.body2.z = this.body1.z + (float)Math.cos(this.body1.yRot) * 12.0F;
		this.body2.x = this.body1.x + (float)Math.sin(this.body1.yRot) * 12.0F;
		this.body2.yRot = Mth.cos(f2 * 1.3F * this.wingspeed - pi4) * (float) Math.PI * 0.1F * f1;
		this.body3.z = this.body2.z + (float)Math.cos(this.body2.yRot) * 11.0F;
		this.body3.x = this.body2.x + (float)Math.sin(this.body2.yRot) * 11.0F;
		this.body3.yRot = Mth.cos(f2 * 1.3F * this.wingspeed - 2.0F * pi4) * (float) Math.PI * 0.1F * f1;
		this.body4.z = this.body3.z + (float)Math.cos(this.body3.yRot) * 12.0F;
		this.body4.x = this.body3.x + (float)Math.sin(this.body3.yRot) * 12.0F;
		this.body4.yRot = Mth.cos(f2 * 1.3F * this.wingspeed - 3.0F * pi4) * (float) Math.PI * 0.1F * f1;
		this.body5.z = this.body4.z + (float)Math.cos(this.body4.yRot) * 12.0F;
		this.body5.x = this.body4.x + (float)Math.sin(this.body4.yRot) * 12.0F;
		this.body5.yRot = Mth.cos(f2 * 1.3F * this.wingspeed - 4.0F * pi4) * (float) Math.PI * 0.1F * f1;
		this.body6.z = this.body5.z + (float)Math.cos(this.body5.yRot) * 12.0F;
		this.body6.x = this.body5.x + 0.5F + (float)Math.sin(this.body5.yRot) * 12.0F;
		this.body6.yRot = Mth.cos(f2 * 1.3F * this.wingspeed - 5.0F * pi4) * (float) Math.PI * 0.1F * f1;
		this.tail1.z = this.body6.z + (float)Math.cos(this.body6.yRot) * 12.0F;
		this.tail1.x = this.body6.x + 1.0F + (float)Math.sin(this.body6.yRot) * 12.0F;
		this.tail1.yRot = Mth.cos(f2 * 1.3F * this.wingspeed - 6.0F * pi4) * (float) Math.PI * 0.1F * f1;
		this.tail2.z = this.tail1.z + (float)Math.cos(this.tail1.yRot) * 10.0F;
		this.tail2.x = this.tail1.x + 1.5F + (float)Math.sin(this.tail1.yRot) * 10.0F;
		this.tail2.yRot = Mth.cos(f2 * 1.3F * this.wingspeed - 7.0F * pi4) * (float) Math.PI * 0.1F * f1;
		this.tail3.z = this.tail2.z + (float)Math.cos(this.tail2.yRot) * 10.0F;
		this.tail3.x = this.tail2.x + 1.0F + (float)Math.sin(this.tail2.yRot) * 10.0F;
		this.tail3.yRot = Mth.cos(f2 * 1.3F * this.wingspeed - 8.0F * pi4) * (float) Math.PI * 0.1F * f1;
		this.tail4.z = this.tail3.z + (float)Math.cos(this.tail3.yRot) * 10.0F;
		this.tail4.x = this.tail3.x + 1.0F + (float)Math.sin(this.tail3.yRot) * 10.0F;
		this.tail4.yRot = Mth.cos(f2 * 1.3F * this.wingspeed - 9.0F * pi4) * (float) Math.PI * 0.1F * f1;
		if (state.attacking) {
		this.jaw.xRot = -1.0F + Mth.cos(f2 * 0.45F) * (float) Math.PI * 0.18F;
		} else {
		this.jaw.xRot = -1.1F;
		}
	}
}
