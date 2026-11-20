package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelHammerhead (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class HammerheadModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart chest;
	private final ModelPart abdomen;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart snout;
	private final ModelPart neck_armour;
	private final ModelPart horn_base;
	private final ModelPart horn_1;
	private final ModelPart horn_2;
	private final ModelPart horn_R;
	private final ModelPart horn_L;
	private final ModelPart back_armour1;
	private final ModelPart back_armour_2;
	private final ModelPart back_armour_3;
	private final ModelPart back_armour_3R;
	private final ModelPart back_armour_4;
	private final ModelPart back_armour_4R;
	private final ModelPart tail;
	private final ModelPart leg_1R;
	private final ModelPart leg_1;
	private final ModelPart leg_2;
	private final ModelPart leg_2R;
	private final ModelPart leg_3R;
	private final ModelPart leg_3;
	private final ModelPart leg_1Rb;
	private final ModelPart leg_1b;
	private final ModelPart leg_2b;
	private final ModelPart leg_2Rb;
	private final ModelPart leg_3Rb;
	private final ModelPart leg_3b;
	private final ModelPart fan1;
	private final ModelPart Lfan2;
	private final ModelPart Rfan2;
	private final ModelPart Lfan3;
	private final ModelPart Rfan3;
	private final ModelPart Lear;
	private final ModelPart Rear;
	private final float wingspeed;

	public HammerheadModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.chest = root.getChild("chest");
		this.abdomen = root.getChild("abdomen");
		this.neck = root.getChild("neck");
		this.head = root.getChild("head");
		this.snout = root.getChild("snout");
		this.neck_armour = root.getChild("neck_armour");
		this.horn_base = root.getChild("horn_base");
		this.horn_1 = root.getChild("horn_1");
		this.horn_2 = root.getChild("horn_2");
		this.horn_R = root.getChild("horn_r");
		this.horn_L = root.getChild("horn_l");
		this.back_armour1 = root.getChild("back_armour1");
		this.back_armour_2 = root.getChild("back_armour_2");
		this.back_armour_3 = root.getChild("back_armour_3");
		this.back_armour_3R = root.getChild("back_armour_3r");
		this.back_armour_4 = root.getChild("back_armour_4");
		this.back_armour_4R = root.getChild("back_armour_4r");
		this.tail = root.getChild("tail");
		this.leg_1R = root.getChild("leg_1r");
		this.leg_1 = root.getChild("leg_1");
		this.leg_2 = root.getChild("leg_2");
		this.leg_2R = root.getChild("leg_2r");
		this.leg_3R = root.getChild("leg_3r");
		this.leg_3 = root.getChild("leg_3");
		this.leg_1Rb = root.getChild("leg_1rb");
		this.leg_1b = root.getChild("leg_1b");
		this.leg_2b = root.getChild("leg_2b");
		this.leg_2Rb = root.getChild("leg_2rb");
		this.leg_3Rb = root.getChild("leg_3rb");
		this.leg_3b = root.getChild("leg_3b");
		this.fan1 = root.getChild("fan1");
		this.Lfan2 = root.getChild("lfan2");
		this.Rfan2 = root.getChild("rfan2");
		this.Lfan3 = root.getChild("lfan3");
		this.Rfan3 = root.getChild("rfan3");
		this.Lear = root.getChild("lear");
		this.Rear = root.getChild("rear");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelHammerhead");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		float newangle2 = 0.0F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.1F * f1;
		newangle2 = Mth.cos((float)(f2 * 1.3F * this.wingspeed + (Math.PI / 4))) * (float) Math.PI * 0.1F * f1;
		} else {
		newangle = 0.0F;
		}
		this.leg_1.xRot = -0.087F + newangle;
		this.leg_1b.xRot = newangle;
		this.leg_1R.xRot = -0.087F - newangle;
		this.leg_1Rb.xRot = -newangle;
		this.leg_2.xRot = -0.052F + newangle2;
		this.leg_2b.xRot = newangle2;
		this.leg_2R.xRot = -0.052F - newangle2;
		this.leg_2Rb.xRot = -newangle2;
		this.leg_3.xRot = -0.349F - newangle;
		this.leg_3b.xRot = -newangle;
		this.leg_3R.xRot = -0.349F + newangle;
		this.leg_3Rb.xRot = newangle;
		this.neck.yRot = (float)Math.toRadians(f3) * 0.25F;
		this.neck_armour.yRot = this.neck.yRot;
		this.horn_base.yRot = this.neck.yRot;
		this.horn_1.yRot = this.neck.yRot;
		this.horn_2.yRot = this.neck.yRot;
		this.horn_L.yRot = this.neck.yRot;
		this.horn_R.yRot = this.neck.yRot;
		this.head.yRot = this.neck.yRot;
		this.snout.yRot = this.neck.yRot;
		this.fan1.yRot = this.neck.yRot;
		this.Lfan2.yRot = this.neck.yRot - 0.122F;
		this.Lfan3.yRot = this.neck.yRot - 0.226F;
		this.Rfan2.yRot = this.neck.yRot + 0.122F;
		this.Rfan3.yRot = this.neck.yRot + 0.226F;
		this.Lear.yRot = this.neck.yRot + 0.227F;
		this.Rear.yRot = this.neck.yRot - 0.227F;
		newangle = Mth.cos(f2 * 0.3F * this.wingspeed) * (float) Math.PI * 0.03F;
		this.back_armour_4.yRot = 0.349F + newangle;
		this.back_armour_4R.yRot = -0.349F - newangle;
		if (state.attacking) {
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.13F;
		} else {
		newangle = 0.0F;
		}
		this.neck.xRot = newangle + 0.157F;
		this.neck_armour.xRot = newangle + 0.157F;
		this.horn_base.xRot = newangle + 0.087F;
		this.horn_1.xRot = newangle + 0.192F;
		this.horn_2.xRot = newangle + 0.192F;
		this.horn_L.xRot = newangle + 0.192F;
		this.horn_R.xRot = newangle + 0.192F;
		this.head.xRot = newangle + 0.209F;
		this.snout.xRot = newangle + 0.611F;
		this.fan1.xRot = newangle - 0.139F;
		this.Lfan2.xRot = newangle - 0.209F;
		this.Lfan3.xRot = newangle - 0.331F;
		this.Rfan2.xRot = newangle - 0.209F;
		this.Rfan3.xRot = newangle - 0.331F;
		this.Lear.xRot = newangle + 0.366F;
		this.Rear.xRot = newangle + 0.366F;
	}
}
