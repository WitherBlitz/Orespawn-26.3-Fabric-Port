package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelPointysaurus (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class PointysaurusModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart lfleg;
	private final ModelPart rfleg;
	private final ModelPart lrleg;
	private final ModelPart rrleg;
	private final ModelPart body1;
	private final ModelPart head;
	private final ModelPart body2;
	private final ModelPart body3;
	private final ModelPart guard;
	private final ModelPart nose;
	private final ModelPart lhorn;
	private final ModelPart rhorn;
	private final ModelPart chorn;
	private final ModelPart tail;
	private final ModelPart bump1;
	private final ModelPart bump2;
	private final ModelPart bump3;
	private final ModelPart bump4;
	private final ModelPart bump5;
	private final ModelPart bump6;
	private final ModelPart bump7;
	private final ModelPart bump8;
	private final ModelPart bump9;
	private final ModelPart bump10;
	private final ModelPart bump11;
	private final ModelPart bump12;
	private final ModelPart bump13;
	private final ModelPart bump14;
	private final ModelPart bump15;
	private final ModelPart bump16;
	private final float wingspeed;

	public PointysaurusModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.lfleg = root.getChild("lfleg");
		this.rfleg = root.getChild("rfleg");
		this.lrleg = root.getChild("lrleg");
		this.rrleg = root.getChild("rrleg");
		this.body1 = root.getChild("body1");
		this.head = root.getChild("head");
		this.body2 = root.getChild("body2");
		this.body3 = root.getChild("body3");
		this.guard = root.getChild("guard");
		this.nose = root.getChild("nose");
		this.lhorn = root.getChild("lhorn");
		this.rhorn = root.getChild("rhorn");
		this.chorn = root.getChild("chorn");
		this.tail = root.getChild("tail");
		this.bump1 = root.getChild("bump1");
		this.bump2 = root.getChild("bump2");
		this.bump3 = root.getChild("bump3");
		this.bump4 = root.getChild("bump4");
		this.bump5 = root.getChild("bump5");
		this.bump6 = root.getChild("bump6");
		this.bump7 = root.getChild("bump7");
		this.bump8 = root.getChild("bump8");
		this.bump9 = root.getChild("bump9");
		this.bump10 = root.getChild("bump10");
		this.bump11 = root.getChild("bump11");
		this.bump12 = root.getChild("bump12");
		this.bump13 = root.getChild("bump13");
		this.bump14 = root.getChild("bump14");
		this.bump15 = root.getChild("bump15");
		this.bump16 = root.getChild("bump16");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelPointysaurus");
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
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.25F * f1;
		} else {
		newangle = 0.0F;
		}
		this.lfleg.xRot = newangle;
		this.rrleg.xRot = newangle;
		this.rfleg.xRot = -newangle;
		this.lrleg.xRot = -newangle;
		this.head.yRot = (float)Math.toRadians(f3) * 0.45F;
		this.nose.yRot = this.head.yRot;
		this.chorn.yRot = this.head.yRot;
		this.lhorn.yRot = this.head.yRot - 0.14F;
		this.rhorn.yRot = this.head.yRot + 0.14F;
		this.guard.yRot = this.head.yRot;
		this.bump1.yRot = this.head.yRot;
		this.bump2.yRot = this.head.yRot;
		this.bump3.yRot = this.head.yRot;
		this.bump4.yRot = this.head.yRot;
		this.bump5.yRot = this.head.yRot;
		this.bump6.yRot = this.head.yRot;
		this.bump7.yRot = this.head.yRot;
		this.bump8.yRot = this.head.yRot;
		this.bump9.yRot = this.head.yRot;
		this.bump10.yRot = this.head.yRot;
		this.bump11.yRot = this.head.yRot;
		this.bump12.yRot = this.head.yRot;
		this.bump13.yRot = this.head.yRot;
		this.bump14.yRot = this.head.yRot;
		this.bump15.yRot = this.head.yRot;
		this.bump16.yRot = this.head.yRot;
		this.head.xRot = (float)Math.toRadians(f4) * 0.45F;
		this.nose.xRot = this.head.xRot;
		this.chorn.xRot = this.head.xRot;
		this.lhorn.xRot = this.head.xRot - 0.16F;
		this.rhorn.xRot = this.head.xRot - 0.16F;
		this.guard.xRot = this.head.xRot - 0.262F;
		this.bump1.xRot = this.guard.xRot;
		this.bump2.xRot = this.guard.xRot;
		this.bump3.xRot = this.guard.xRot;
		this.bump4.xRot = this.guard.xRot;
		this.bump5.xRot = this.guard.xRot;
		this.bump6.xRot = this.guard.xRot;
		this.bump7.xRot = this.guard.xRot;
		this.bump8.xRot = this.guard.xRot;
		this.bump9.xRot = this.guard.xRot;
		this.bump10.xRot = this.guard.xRot;
		this.bump11.xRot = this.guard.xRot;
		this.bump12.xRot = this.guard.xRot;
		this.bump13.xRot = this.guard.xRot;
		this.bump14.xRot = this.guard.xRot;
		this.bump15.xRot = this.guard.xRot;
		this.bump16.xRot = this.guard.xRot;
		if (state.attacking) {
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.25F;
		} else {
		newangle = Mth.cos(f2 * 0.3F * this.wingspeed) * (float) Math.PI * 0.05F;
		}
		this.tail.yRot = newangle;
		newangle = Mth.cos(f2 * 0.02F * this.wingspeed) * (float) Math.PI * 0.15F;
		this.tail.xRot = newangle + 0.28F;
	}
}
