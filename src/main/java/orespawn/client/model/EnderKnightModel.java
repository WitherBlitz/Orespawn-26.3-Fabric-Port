package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelEnderKnight (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class EnderKnightModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart rleg1;
	private final ModelPart rleg3;
	private final ModelPart pelvis;
	private final ModelPart spine1;
	private final ModelPart spine2;
	private final ModelPart spine3;
	private final ModelPart neck;
	private final ModelPart rleg2;
	private final ModelPart rhip;
	private final ModelPart rib4;
	private final ModelPart rib3;
	private final ModelPart rib2;
	private final ModelPart rib1;
	private final ModelPart rfoot1;
	private final ModelPart rfoot3;
	private final ModelPart rcollar;
	private final ModelPart lcollar;
	private final ModelPart lleg3;
	private final ModelPart lleg2;
	private final ModelPart lhip;
	private final ModelPart lleg1;
	private final ModelPart rfoot4;
	private final ModelPart rfoot2;
	private final ModelPart cape2;
	private final ModelPart cape1;
	private final ModelPart lfoot1;
	private final ModelPart lfoot3;
	private final ModelPart lfoot2;
	private final ModelPart lfoot4;
	private final ModelPart head;
	private final ModelPart lshoulder;
	private final ModelPart rshoulder;
	private final ModelPart rarm3;
	private final ModelPart rarm2;
	private final ModelPart rarm1;
	private final ModelPart larm3;
	private final ModelPart larm2;
	private final ModelPart larm1;
	private final ModelPart blade;
	private final ModelPart handle;
	private final float wingspeed;

	public EnderKnightModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.rleg1 = root.getChild("rleg1");
		this.rleg3 = root.getChild("rleg3");
		this.pelvis = root.getChild("pelvis");
		this.spine1 = root.getChild("spine1");
		this.spine2 = root.getChild("spine2");
		this.spine3 = root.getChild("spine3");
		this.neck = root.getChild("neck");
		this.rleg2 = root.getChild("rleg2");
		this.rhip = root.getChild("rhip");
		this.rib4 = root.getChild("rib4");
		this.rib3 = root.getChild("rib3");
		this.rib2 = root.getChild("rib2");
		this.rib1 = root.getChild("rib1");
		this.rfoot1 = root.getChild("rfoot1");
		this.rfoot3 = root.getChild("rfoot3");
		this.rcollar = root.getChild("rcollar");
		this.lcollar = root.getChild("lcollar");
		this.lleg3 = root.getChild("lleg3");
		this.lleg2 = root.getChild("lleg2");
		this.lhip = root.getChild("lhip");
		this.lleg1 = root.getChild("lleg1");
		this.rfoot4 = root.getChild("rfoot4");
		this.rfoot2 = root.getChild("rfoot2");
		this.cape2 = root.getChild("cape2");
		this.cape1 = root.getChild("cape1");
		this.lfoot1 = root.getChild("lfoot1");
		this.lfoot3 = root.getChild("lfoot3");
		this.lfoot2 = root.getChild("lfoot2");
		this.lfoot4 = root.getChild("lfoot4");
		this.head = root.getChild("head");
		this.lshoulder = root.getChild("lshoulder");
		this.rshoulder = root.getChild("rshoulder");
		this.rarm3 = root.getChild("rarm3");
		this.rarm2 = root.getChild("rarm2");
		this.rarm1 = root.getChild("rarm1");
		this.larm3 = root.getChild("larm3");
		this.larm2 = root.getChild("larm2");
		this.larm1 = root.getChild("larm1");
		this.blade = root.getChild("blade");
		this.handle = root.getChild("handle");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelEnderKnight");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		// state.attacking: the mob is screaming (EnderKnight.isScreaming).
		float newangle = 0.0F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.25F * f1;
		} else {
		newangle = 0.0F;
		}
		this.lfoot1.xRot = newangle;
		this.lfoot2.xRot = 0.6F + newangle;
		this.lfoot3.xRot = newangle;
		this.lfoot4.xRot = newangle;
		this.lleg1.xRot = newangle;
		this.lleg2.xRot = -0.1F + newangle;
		this.lleg3.xRot = -0.1F + newangle;
		this.rfoot1.xRot = -newangle;
		this.rfoot2.xRot = 0.6F - newangle;
		this.rfoot3.xRot = -newangle;
		this.rfoot4.xRot = -newangle;
		this.rleg1.xRot = -newangle;
		this.rleg2.xRot = -0.1F - newangle;
		this.rleg3.xRot = -0.1F - newangle;
		this.cape2.zRot = newangle / 4.0F;
		newangle = Mth.cos(f2 * 0.7F * this.wingspeed) * (float) Math.PI * 0.02F;
		this.cape2.xRot = newangle;
		this.head.yRot = (float)Math.toRadians(f3) * 0.45F;
		if (this.head.yRot > 0.45F) {
		this.head.yRot = 0.45F;
		}
		if (this.head.yRot < -0.45F) {
		this.head.yRot = -0.45F;
		}
		newangle = Mth.cos(f2 * 2.7F * this.wingspeed) * (float) Math.PI * 0.3F;
		if (state.attacking) {
		this.larm2.xRot = -1.2F + newangle;
		this.larm3.xRot = -1.2F + newangle;
		this.rarm2.xRot = -1.2F + newangle;
		this.rarm3.xRot = -1.2F + newangle;
		this.larm1.xRot = -1.8F + newangle;
		this.rarm1.xRot = -1.8F + newangle;
		this.blade.xRot = this.handle.xRot = 0.5F + newangle * 3.0F / 2.0F;
		} else {
		this.larm2.xRot = -0.5F;
		this.larm3.xRot = -0.5F;
		this.larm1.zRot = 0.0F;
		this.larm1.yRot = 1.0F;
		this.larm1.xRot = -1.0F;
		this.rarm2.xRot = -0.5F;
		this.rarm3.xRot = -0.5F;
		this.rarm1.zRot = 0.0F;
		this.rarm1.yRot = -1.0F;
		this.rarm1.xRot = -1.0F;
		this.blade.xRot = this.handle.xRot = 0.35F;
		}
		this.larm1.y = (float)(this.larm2.y + Math.cos(this.larm2.xRot) * 10.0);
		this.larm1.z = (float)(this.larm2.z + Math.sin(this.larm2.xRot) * 10.0);
		this.rarm1.y = (float)(this.rarm2.y + Math.cos(this.rarm2.xRot) * 10.0);
		this.rarm1.z = (float)(this.rarm2.z + Math.sin(this.rarm2.xRot) * 10.0);
		this.blade.y = this.handle.y = (float)(this.rarm1.y + Math.cos(this.rarm1.xRot) * 7.0) + 1.0F;
		this.blade.z = this.handle.z = (float)(this.rarm1.z + Math.sin(this.rarm1.xRot) * 7.0);
	}
}
