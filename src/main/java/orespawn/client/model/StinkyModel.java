package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelStinky; state.value is the Stinky's activity (2 flying). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class StinkyModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart neck1;
	private final ModelPart neck;
	private final ModelPart neckbase;
	private final ModelPart head;
	private final ModelPart Rleg1;
	private final ModelPart Lleg1;
	private final ModelPart Lhorn1;
	private final ModelPart Rhorn1;
	private final ModelPart snout;
	private final ModelPart Lhorn2;
	private final ModelPart Rhorn2;
	private final ModelPart tail1;
	private final ModelPart Rleg2;
	private final ModelPart Lleg2;
	private final ModelPart tail2;
	private final ModelPart tail3;
	private final ModelPart tail4;
	private final ModelPart Lwing;
	private final ModelPart Rwing;
	private final float wingspeed;

	public StinkyModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.body = root.getChild("body");
		this.neck1 = root.getChild("neck1");
		this.neck = root.getChild("neck");
		this.neckbase = root.getChild("neckbase");
		this.head = root.getChild("head");
		this.Rleg1 = root.getChild("rleg1");
		this.Lleg1 = root.getChild("lleg1");
		this.Lhorn1 = root.getChild("lhorn1");
		this.Rhorn1 = root.getChild("rhorn1");
		this.snout = root.getChild("snout");
		this.Lhorn2 = root.getChild("lhorn2");
		this.Rhorn2 = root.getChild("rhorn2");
		this.tail1 = root.getChild("tail1");
		this.Rleg2 = root.getChild("rleg2");
		this.Lleg2 = root.getChild("lleg2");
		this.tail2 = root.getChild("tail2");
		this.tail3 = root.getChild("tail3");
		this.tail4 = root.getChild("tail4");
		this.Lwing = root.getChild("lwing");
		this.Rwing = root.getChild("rwing");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelStinky");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float hf = 0.0F;
		float newangle = 0.0F;
		int current_activity = (int) state.value;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 2.3F * this.wingspeed) * (float) Math.PI * 0.4F * f1;
		} else {
		newangle = 0.0F;
		}
		this.Rwing.zRot = newangle - 0.4F;
		this.Lwing.zRot = -newangle + 0.4F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 2.0F * this.wingspeed) * (float) Math.PI * 0.25F * f1;
		} else {
		newangle = 0.0F;
		}
		if (current_activity != 2) {
		this.Rleg1.xRot = newangle;
		this.Lleg1.xRot = -newangle;
		this.Rleg2.xRot = -newangle;
		this.Lleg2.xRot = newangle;
		} else {
		newangle = -1.0F;
		this.Rleg2.xRot = newangle;
		this.Lleg2.xRot = newangle;
		newangle = 1.0F;
		this.Rleg1.xRot = newangle;
		this.Lleg1.xRot = newangle;
		}
		newangle = Mth.cos(f2 * 1.0F * this.wingspeed) * (float) Math.PI * 0.2F;
		if (state.sitting) {
		newangle = 0.0F;
		}
		this.tail2.yRot = newangle;
		this.tail3.z = this.tail2.z + (float)Math.cos(this.tail2.yRot) * 4.0F;
		this.tail3.x = this.tail2.x + (float)Math.sin(this.tail2.yRot) * 4.0F - 0.5F;
		this.tail3.yRot = newangle * 1.6F;
		this.tail4.z = this.tail3.z + (float)Math.cos(this.tail3.yRot) * 3.0F;
		this.tail4.x = this.tail3.x + (float)Math.sin(this.tail3.yRot) * 3.0F - 0.5F;
		this.tail4.yRot = newangle * 2.6F;
		this.head.yRot = (float)Math.toRadians(f3);
		this.snout.yRot = (float)Math.toRadians(f3);
		this.neck.yRot = (float)Math.toRadians(f3) / 2.0F;
		this.Rhorn1.yRot = (float)Math.toRadians(f3);
		this.Rhorn2.yRot = (float)Math.toRadians(f3);
		this.Lhorn1.yRot = (float)Math.toRadians(f3);
		this.Lhorn2.yRot = (float)Math.toRadians(f3);
		this.head.xRot = (float)Math.toRadians(f4) / 3.0F;
		this.snout.xRot = (float)Math.toRadians(f4) / 3.0F;
		this.neck.xRot = (float)Math.toRadians(f4) / 3.0F;
		this.Rhorn1.xRot = (float)Math.toRadians(f4) / 3.0F;
		this.Rhorn2.xRot = (float)Math.toRadians(f4) / 3.0F;
		this.Lhorn1.xRot = (float)Math.toRadians(f4) / 3.0F;
		this.Lhorn2.xRot = (float)Math.toRadians(f4) / 3.0F;
	}
}
