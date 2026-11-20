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
 * Ported from OreSpawn's ModelDragon. state.flag: flying; state.value: its speed over the ground (blocks a tick);
 * state.yRot: the neck's swing, already smoothed (OreSpawn's RenderInfo).
 */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class DragonModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart horn1;
	private final ModelPart horn2;
	private final ModelPart tail6;
	private final ModelPart wing15;
	private final ModelPart spike1;
	private final ModelPart spike2;
	private final ModelPart spike3;
	private final ModelPart spike4;
	private final ModelPart wing14;
	private final ModelPart spike5;
	private final ModelPart spike6;
	private final ModelPart spike7;
	private final ModelPart spike8;
	private final ModelPart spike9;
	private final ModelPart spike10;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart leg1;
	private final ModelPart leg2;
	private final ModelPart leg3;
	private final ModelPart leg4;
	private final ModelPart body2;
	private final ModelPart neck1;
	private final ModelPart body3;
	private final ModelPart neck2;
	private final ModelPart neck3;
	private final ModelPart leg5;
	private final ModelPart leg6;
	private final ModelPart leg7;
	private final ModelPart leg9;
	private final ModelPart foot1;
	private final ModelPart foot2;
	private final ModelPart leg10;
	private final ModelPart leg11;
	private final ModelPart foot3;
	private final ModelPart foot4;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart tail3;
	private final ModelPart mouth1;
	private final ModelPart mouth2;
	private final ModelPart tail5;
	private final ModelPart wing1;
	private final ModelPart wing2;
	private final ModelPart wing3;
	private final ModelPart wing4;
	private final ModelPart wing5;
	private final ModelPart wing6;
	private final ModelPart wing7;
	private final ModelPart wing8;
	private final ModelPart wing9;
	private final ModelPart wing10;
	private final ModelPart wing11;
	private final ModelPart wing12;
	private final ModelPart tail4;
	private final float wingspeed;

	public DragonModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.horn1 = root.getChild("horn1");
		this.horn2 = root.getChild("horn2");
		this.tail6 = root.getChild("tail6");
		this.wing15 = root.getChild("wing15");
		this.spike1 = root.getChild("spike1");
		this.spike2 = root.getChild("spike2");
		this.spike3 = root.getChild("spike3");
		this.spike4 = root.getChild("spike4");
		this.wing14 = root.getChild("wing14");
		this.spike5 = root.getChild("spike5");
		this.spike6 = root.getChild("spike6");
		this.spike7 = root.getChild("spike7");
		this.spike8 = root.getChild("spike8");
		this.spike9 = root.getChild("spike9");
		this.spike10 = root.getChild("spike10");
		this.head = root.getChild("head");
		this.body = root.getChild("body");
		this.leg1 = root.getChild("leg1");
		this.leg2 = root.getChild("leg2");
		this.leg3 = root.getChild("leg3");
		this.leg4 = root.getChild("leg4");
		this.body2 = root.getChild("body2");
		this.neck1 = root.getChild("neck1");
		this.body3 = root.getChild("body3");
		this.neck2 = root.getChild("neck2");
		this.neck3 = root.getChild("neck3");
		this.leg5 = root.getChild("leg5");
		this.leg6 = root.getChild("leg6");
		this.leg7 = root.getChild("leg7");
		this.leg9 = root.getChild("leg9");
		this.foot1 = root.getChild("foot1");
		this.foot2 = root.getChild("foot2");
		this.leg10 = root.getChild("leg10");
		this.leg11 = root.getChild("leg11");
		this.foot3 = root.getChild("foot3");
		this.foot4 = root.getChild("foot4");
		this.tail1 = root.getChild("tail1");
		this.tail2 = root.getChild("tail2");
		this.tail3 = root.getChild("tail3");
		this.mouth1 = root.getChild("mouth1");
		this.mouth2 = root.getChild("mouth2");
		this.tail5 = root.getChild("tail5");
		this.wing1 = root.getChild("wing1");
		this.wing2 = root.getChild("wing2");
		this.wing3 = root.getChild("wing3");
		this.wing4 = root.getChild("wing4");
		this.wing5 = root.getChild("wing5");
		this.wing6 = root.getChild("wing6");
		this.wing7 = root.getChild("wing7");
		this.wing8 = root.getChild("wing8");
		this.wing9 = root.getChild("wing9");
		this.wing10 = root.getChild("wing10");
		this.wing11 = root.getChild("wing11");
		this.wing12 = root.getChild("wing12");
		this.tail4 = root.getChild("tail4");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelDragon");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		float lspeed = 0.0F;
		float tailspeed = 0.76F;
		float tailamp = 0.45F;
		if (f1 > 0.001) {
		lspeed = state.value;
		newangle = Mth.cos(f2 * 1.25F * this.wingspeed) * (float) Math.PI * lspeed * 0.6F;
		} else {
		newangle = 0.0F;
		}
		if (state.flag) {
		newangle = 1.0F;
		this.leg4.xRot = 0.557F - newangle;
		this.leg5.xRot = -0.557F - newangle;
		this.foot2.xRot = -newangle;
		this.leg3.xRot = 0.557F - newangle;
		this.leg6.xRot = -0.557F - newangle;
		this.foot1.xRot = -newangle;
		this.leg2.xRot = -0.632F + newangle;
		this.leg7.xRot = 0.89F + newangle;
		this.leg10.xRot = -0.557F + newangle;
		this.foot3.xRot = newangle;
		this.leg1.xRot = -0.632F + newangle;
		this.leg9.xRot = 0.89F + newangle;
		this.leg11.xRot = -0.557F + newangle;
		this.foot4.xRot = newangle;
		} else {
		this.leg4.xRot = 0.557F + newangle;
		this.leg5.xRot = -0.557F + newangle;
		this.foot2.xRot = newangle;
		this.leg3.xRot = 0.557F - newangle;
		this.leg6.xRot = -0.557F - newangle;
		this.foot1.xRot = -newangle;
		this.leg2.xRot = -0.632F - newangle;
		this.leg7.xRot = 0.89F - newangle;
		this.leg10.xRot = -0.557F - newangle;
		this.foot3.xRot = -newangle;
		this.leg1.xRot = -0.632F + newangle;
		this.leg9.xRot = 0.89F + newangle;
		this.leg11.xRot = -0.557F + newangle;
		this.foot4.xRot = newangle;
		}
		if (state.attacking) {
		if (state.flag) {
		newangle = Mth.cos(f2 * 0.75F * this.wingspeed) * (float) Math.PI * 0.28F;
		} else {
		newangle = -0.45F + Mth.cos(f2 * 0.85F * this.wingspeed) * (float) Math.PI * 0.2F;
		}
		} else if (state.flag) {
		newangle = Mth.cos(f2 * 0.75F * this.wingspeed) * (float) Math.PI * 0.28F;
		} else {
		newangle = -0.85F + Mth.cos(f2 * 0.2F * this.wingspeed) * (float) Math.PI * 0.028F;
		}
		this.wing1.zRot = newangle;
		this.wing15.zRot = newangle;
		this.wing3.zRot = newangle * 4.0F / 3.0F;
		this.wing3.y = this.wing1.y + (float)Math.sin(this.wing1.zRot) * 7.0F;
		this.wing3.x = this.wing1.x + (float)Math.cos(this.wing1.zRot) * 7.0F;
		this.wing8.zRot = newangle * 4.0F / 3.0F;
		this.wing8.y = this.wing3.y;
		this.wing8.x = this.wing3.x;
		this.wing2.zRot = newangle * 3.0F / 2.0F;
		this.wing2.y = this.wing3.y + (float)Math.sin(this.wing3.zRot) * 6.0F;
		this.wing2.x = this.wing3.x + (float)Math.cos(this.wing3.zRot) * 6.0F;
		this.wing7.zRot = newangle * 3.0F / 2.0F;
		this.wing7.y = this.wing2.y;
		this.wing7.x = this.wing2.x;
		this.wing9.zRot = newangle * 3.0F / 2.0F;
		this.wing9.y = this.wing2.y;
		this.wing9.x = this.wing2.x;
		this.wing4.zRot = -newangle;
		this.wing14.zRot = -newangle;
		this.wing5.zRot = -newangle * 4.0F / 3.0F;
		this.wing5.y = this.wing4.y - (float)Math.sin(this.wing4.zRot) * 7.0F;
		this.wing5.x = this.wing4.x - (float)Math.cos(this.wing4.zRot) * 7.0F;
		this.wing10.zRot = -newangle * 4.0F / 3.0F;
		this.wing10.y = this.wing5.y;
		this.wing10.x = this.wing5.x;
		this.wing6.zRot = -newangle * 3.0F / 2.0F;
		this.wing6.y = this.wing5.y - (float)Math.sin(this.wing5.zRot) * 6.0F;
		this.wing6.x = this.wing5.x - (float)Math.cos(this.wing5.zRot) * 6.0F;
		this.wing11.zRot = -newangle * 3.0F / 2.0F;
		this.wing11.y = this.wing6.y;
		this.wing11.x = this.wing6.x;
		this.wing12.zRot = -newangle * 3.0F / 2.0F;
		this.wing12.y = this.wing6.y;
		this.wing12.x = this.wing6.x;
		if (state.attacking) {
		tailspeed = 0.96F;
		tailamp = 0.75F;
		}
		if (!state.flag && !state.attacking) {
		tailspeed = 0.22F;
		tailamp = 0.22F;
		}
		if (state.sitting) {
		tailspeed = 0.0F;
		tailamp = 0.0F;
		}
		this.tail1.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * (float) Math.PI * 0.04F;
		this.spike10.z = this.tail1.z;
		this.spike10.x = this.tail1.x;
		this.spike10.yRot = this.tail1.yRot;
		this.tail2.z = this.tail1.z + (float)Math.cos(this.tail1.yRot) * 6.0F;
		this.tail2.x = this.tail1.x + (float)Math.sin(this.tail1.yRot) * 6.0F;
		this.tail2.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * (float) Math.PI * tailamp * 0.125F;
		this.spike7.z = this.tail2.z;
		this.spike7.x = this.tail2.x;
		this.spike7.yRot = this.tail2.yRot;
		this.neck1.z = this.tail2.z + (float)Math.cos(this.tail2.yRot) * 6.0F;
		this.neck1.x = this.tail2.x + (float)Math.sin(this.tail2.yRot) * 6.0F;
		this.neck1.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * (float) Math.PI * tailamp * 0.25F;
		this.spike8.z = this.neck1.z;
		this.spike8.x = this.neck1.x;
		this.spike8.yRot = this.neck1.yRot;
		this.tail3.z = this.neck1.z + (float)Math.cos(this.neck1.yRot) * 6.0F;
		this.tail3.x = this.neck1.x + 1.0F + (float)Math.sin(this.neck1.yRot) * 6.0F;
		this.tail3.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * (float) Math.PI * tailamp * 0.375F;
		this.spike3.z = this.tail3.z;
		this.spike3.x = this.tail3.x - 1.0F;
		this.spike3.yRot = this.tail3.yRot;
		this.tail4.z = this.tail3.z + (float)Math.cos(this.tail3.yRot) * 6.0F;
		this.tail4.x = this.tail3.x + (float)Math.sin(this.tail3.yRot) * 6.0F;
		this.tail4.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * (float) Math.PI * tailamp * 0.5F;
		this.spike9.z = this.tail4.z;
		this.spike9.x = this.tail4.x - 1.0F;
		this.spike9.yRot = this.tail4.yRot;
		this.tail6.z = this.tail4.z + (float)Math.cos(this.tail4.yRot) * 6.0F;
		this.tail6.x = this.tail4.x - 1.0F + (float)Math.sin(this.tail4.yRot) * 6.0F;
		this.tail6.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * (float) Math.PI * tailamp * 0.625F;
		this.tail5.z = this.tail6.z + (float)Math.cos(this.tail6.yRot) * 6.0F;
		this.tail5.x = this.tail6.x - 0.5F + (float)Math.sin(this.tail6.yRot) * 6.0F;
		this.tail5.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * (float) Math.PI * tailamp * 0.75F;
		// f3 (state.yRot) is already the neck's swing: OreSpawn's RenderInfo smoothing is done in OdditiesClient.
		this.neck2.yRot = (float)Math.toRadians(f3) * 0.25F;
		this.spike2.yRot = this.neck2.yRot;
		this.neck3.z = this.neck2.z - (float)Math.cos(this.neck2.yRot) * 6.0F;
		this.neck3.x = this.neck2.x - (float)Math.sin(this.neck2.yRot) * 6.0F;
		this.neck3.yRot = (float)Math.toRadians(f3) * 0.5F;
		this.spike1.z = this.neck3.z;
		this.spike1.x = this.neck3.x;
		this.spike1.yRot = this.neck3.yRot;
		this.head.z = this.neck3.z - (float)Math.cos(this.neck3.yRot) * 6.0F;
		this.head.x = this.neck3.x - (float)Math.sin(this.neck3.yRot) * 6.0F;
		this.head.yRot = (float)Math.toRadians(f3) * 0.75F;
		this.mouth1.z = this.head.z;
		this.mouth1.x = this.head.x;
		this.mouth1.yRot = this.head.yRot;
		this.horn1.z = this.head.z;
		this.horn1.x = this.head.x;
		this.horn1.yRot = this.head.yRot + 0.26F;
		this.horn2.z = this.head.z;
		this.horn2.x = this.head.x;
		this.horn2.yRot = this.head.yRot - 0.26F;
		this.mouth2.z = this.head.z - (float)Math.cos(this.head.yRot) * 9.0F;
		this.mouth2.x = this.head.x - (float)Math.sin(this.head.yRot) * 9.0F;
		this.mouth2.yRot = this.head.yRot;
		newangle = Mth.cos(f2 * 1.5F * this.wingspeed) * (float) Math.PI * 0.14F;
		if (state.attacking) {
		this.mouth2.xRot = 0.4F + newangle;
		} else {
		this.mouth2.xRot = 0.07F;
		}
	}
}
