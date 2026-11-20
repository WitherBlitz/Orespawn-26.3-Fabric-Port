package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelLurkingTerror (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class LurkingTerrorModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart leg1;
	private final ModelPart leg1part2;
	private final ModelPart leg1part3;
	private final ModelPart leg2;
	private final ModelPart leg2part2;
	private final ModelPart leg2part3;
	private final ModelPart leg3;
	private final ModelPart leg3part2;
	private final ModelPart leg3part3;
	private final ModelPart leg4;
	private final ModelPart leg4part2;
	private final ModelPart leg4part3;
	private final ModelPart leg5;
	private final ModelPart leg5part2;
	private final ModelPart leg6;
	private final ModelPart leg6part2;
	private final ModelPart thorax;
	private final ModelPart abdomen;
	private final ModelPart head;
	private final ModelPart jaw1;
	private final ModelPart jaw1part2;
	private final ModelPart jaw1tooth1;
	private final ModelPart jaw1tooth2;
	private final ModelPart jaw1tooth3;
	private final ModelPart jaw1tooth4;
	private final ModelPart jaw1tooth5;
	private final ModelPart jaw1tooth6;
	private final ModelPart jaw2;
	private final ModelPart jaw2part2;
	private final ModelPart jaw2tooth1;
	private final ModelPart jaw2tooth2;
	private final ModelPart jaw2tooth3;
	private final ModelPart jaw2tooth4;
	private final ModelPart jaw2tooth5;
	private final ModelPart jaw2tooth6;
	private final ModelPart jaw3;
	private final ModelPart jaw3part2;
	private final ModelPart jaw3tooth1;
	private final ModelPart jaw3tooth2;
	private final ModelPart jaw3tooth3;
	private final ModelPart jaw3tooth4;
	private final ModelPart jaw3tooth5;
	private final ModelPart jaw3tooth6;
	private final ModelPart jaw4;
	private final ModelPart jaw4part2;
	private final ModelPart jaw4tooth1;
	private final ModelPart jaw4tooth2;
	private final ModelPart jaw4tooth3;
	private final ModelPart jaw4tooth4;
	private final ModelPart jaw4tooth5;
	private final ModelPart jaw4tooth6;
	private final ModelPart tonguepart1;
	private final ModelPart tonguepart2;
	private final ModelPart tonguepart3;
	private final ModelPart wing_1;
	private final ModelPart wing_2;
	private final ModelPart wing_3;
	private final ModelPart wing_4;
	private float wingspeed = 1.0F;

	public LurkingTerrorModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.leg1 = root.getChild("leg1");
		this.leg1part2 = root.getChild("leg1part2");
		this.leg1part3 = root.getChild("leg1part3");
		this.leg2 = root.getChild("leg2");
		this.leg2part2 = root.getChild("leg2part2");
		this.leg2part3 = root.getChild("leg2part3");
		this.leg3 = root.getChild("leg3");
		this.leg3part2 = root.getChild("leg3part2");
		this.leg3part3 = root.getChild("leg3part3");
		this.leg4 = root.getChild("leg4");
		this.leg4part2 = root.getChild("leg4part2");
		this.leg4part3 = root.getChild("leg4part3");
		this.leg5 = root.getChild("leg5");
		this.leg5part2 = root.getChild("leg5part2");
		this.leg6 = root.getChild("leg6");
		this.leg6part2 = root.getChild("leg6part2");
		this.thorax = root.getChild("thorax");
		this.abdomen = root.getChild("abdomen");
		this.head = root.getChild("head");
		this.jaw1 = root.getChild("jaw1");
		this.jaw1part2 = root.getChild("jaw1part2");
		this.jaw1tooth1 = root.getChild("jaw1tooth1");
		this.jaw1tooth2 = root.getChild("jaw1tooth2");
		this.jaw1tooth3 = root.getChild("jaw1tooth3");
		this.jaw1tooth4 = root.getChild("jaw1tooth4");
		this.jaw1tooth5 = root.getChild("jaw1tooth5");
		this.jaw1tooth6 = root.getChild("jaw1tooth6");
		this.jaw2 = root.getChild("jaw2");
		this.jaw2part2 = root.getChild("jaw2part2");
		this.jaw2tooth1 = root.getChild("jaw2tooth1");
		this.jaw2tooth2 = root.getChild("jaw2tooth2");
		this.jaw2tooth3 = root.getChild("jaw2tooth3");
		this.jaw2tooth4 = root.getChild("jaw2tooth4");
		this.jaw2tooth5 = root.getChild("jaw2tooth5");
		this.jaw2tooth6 = root.getChild("jaw2tooth6");
		this.jaw3 = root.getChild("jaw3");
		this.jaw3part2 = root.getChild("jaw3part2");
		this.jaw3tooth1 = root.getChild("jaw3tooth1");
		this.jaw3tooth2 = root.getChild("jaw3tooth2");
		this.jaw3tooth3 = root.getChild("jaw3tooth3");
		this.jaw3tooth4 = root.getChild("jaw3tooth4");
		this.jaw3tooth5 = root.getChild("jaw3tooth5");
		this.jaw3tooth6 = root.getChild("jaw3tooth6");
		this.jaw4 = root.getChild("jaw4");
		this.jaw4part2 = root.getChild("jaw4part2");
		this.jaw4tooth1 = root.getChild("jaw4tooth1");
		this.jaw4tooth2 = root.getChild("jaw4tooth2");
		this.jaw4tooth3 = root.getChild("jaw4tooth3");
		this.jaw4tooth4 = root.getChild("jaw4tooth4");
		this.jaw4tooth5 = root.getChild("jaw4tooth5");
		this.jaw4tooth6 = root.getChild("jaw4tooth6");
		this.tonguepart1 = root.getChild("tonguepart1");
		this.tonguepart2 = root.getChild("tonguepart2");
		this.tonguepart3 = root.getChild("tonguepart3");
		this.wing_1 = root.getChild("wing_1");
		this.wing_2 = root.getChild("wing_2");
		this.wing_3 = root.getChild("wing_3");
		this.wing_4 = root.getChild("wing_4");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelLurkingTerror");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		// OreSpawn kept which legs move (and whether the jaw snaps) per creature, re-rolled each cycle (RenderInfo);
		// LurkingTerror.animBits rolls them, and the renderer passes them in state.value.
		float newangle = 0.0F;
		float legspeed = 0.7F;
		float mouthspeed = 0.9F;
		int bits = (int) state.value;
		int ri1 = bits & 63;
		int ri2 = (bits >> 6) & 1;
		newangle = 0.0F;
		if ((ri1 & 1) != 0) {
		newangle = Mth.sin(f2 * legspeed * this.wingspeed) * (float) Math.PI * 0.25F;
		}
		this.leg2.zRot = this.leg2part2.zRot = 0.191F + newangle;
		this.leg2part3.zRot = 0.675F + newangle;
		newangle = 0.0F;
		if ((ri1 & 2) != 0) {
		newangle = Mth.sin(f2 * legspeed * this.wingspeed) * (float) Math.PI * 0.25F;
		}
		this.leg1.zRot = this.leg1part2.zRot = -0.191F + newangle;
		this.leg1part3.zRot = -0.675F + newangle;
		newangle = 0.0F;
		if ((ri1 & 4) != 0) {
		newangle = Mth.sin(f2 * legspeed * this.wingspeed) * (float) Math.PI * 0.15F;
		}
		this.leg4.zRot = this.leg4part2.zRot = 0.191F + newangle;
		this.leg4part3.zRot = 0.675F + newangle;
		newangle = 0.0F;
		if ((ri1 & 8) != 0) {
		newangle = Mth.sin(f2 * legspeed * this.wingspeed) * (float) Math.PI * 0.15F;
		}
		this.leg3.zRot = this.leg3part2.zRot = -0.191F + newangle;
		this.leg3part3.zRot = -0.675F + newangle;
		newangle = 0.0F;
		if ((ri1 & 16) != 0) {
		newangle = Mth.sin(f2 * legspeed * this.wingspeed) * (float) Math.PI * 0.1F;
		}
		this.leg6.zRot = this.leg6part2.zRot = -0.34F + newangle;
		newangle = 0.0F;
		if ((ri1 & 32) != 0) {
		newangle = Mth.sin(f2 * legspeed * this.wingspeed) * (float) Math.PI * 0.1F;
		}
		this.leg5.zRot = this.leg5part2.zRot = 0.34F + newangle;
		newangle = 0.0F;
		if ((ri2 & 1) != 0) {
		newangle = Mth.sin(f2 * mouthspeed * this.wingspeed) * (float) Math.PI * 0.35F;
		newangle = Math.abs(newangle);
		}
		this.jaw1.yRot = newangle;
		this.jaw1part2.yRot = newangle;
		this.jaw1tooth1.yRot = this.jaw1tooth3.yRot = this.jaw1tooth5.yRot = newangle;
		this.jaw1tooth2.yRot = this.jaw1tooth4.yRot = this.jaw1tooth6.yRot = newangle;
		this.jaw2.yRot = -newangle;
		this.jaw2part2.yRot = -newangle;
		this.jaw2tooth1.yRot = this.jaw2tooth3.yRot = this.jaw2tooth5.yRot = -newangle;
		this.jaw2tooth2.yRot = this.jaw2tooth4.yRot = this.jaw2tooth6.yRot = -newangle;
		this.jaw3.xRot = -newangle;
		this.jaw3part2.xRot = -newangle;
		this.jaw3tooth1.xRot = this.jaw3tooth3.xRot = this.jaw3tooth5.xRot = -newangle;
		this.jaw3tooth2.xRot = this.jaw3tooth4.xRot = this.jaw3tooth6.xRot = -newangle;
		this.jaw4.xRot = newangle;
		this.jaw4part2.xRot = newangle;
		this.jaw4tooth1.xRot = this.jaw4tooth3.xRot = this.jaw4tooth5.xRot = newangle;
		this.jaw4tooth2.xRot = this.jaw4tooth4.xRot = this.jaw4tooth6.xRot = newangle;
		this.tonguepart1.xRot = this.tonguepart2.xRot = this.tonguepart3.xRot = 0.0F;
		this.tonguepart1.yRot = this.tonguepart2.yRot = this.tonguepart3.yRot = 0.0F;
		this.tonguepart1.zRot = this.tonguepart2.zRot = this.tonguepart3.zRot = 0.0F;
		this.tonguepart1.x = this.tonguepart3.x = this.tonguepart2.x;
		this.tonguepart1.y = this.tonguepart3.y = this.tonguepart2.y;
		this.tonguepart1.z = this.tonguepart2.z - newangle * 5.0F;
		this.tonguepart3.z = this.tonguepart2.z - newangle * 10.0F;
		newangle = Mth.sin(f2 * 0.1F * this.wingspeed) * (float) Math.PI * 0.06F;
		this.thorax.xRot = newangle;
		this.abdomen.y = (float)(this.thorax.y - Math.sin(newangle) * 14.0);
		newangle = Mth.cos(f2 * 1.4F * this.wingspeed) * (float) Math.PI * 0.2F;
		this.wing_1.xRot = 0.455F + newangle;
		this.wing_2.xRot = 0.455F + newangle;
		this.wing_3.xRot = 0.455F - newangle;
		this.wing_4.xRot = 0.455F - newangle;
	}
}
