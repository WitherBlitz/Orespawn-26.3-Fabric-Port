package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelCamarasaurus (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CamarasaurusModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Body1;
	private final ModelPart Body2;
	private final ModelPart Body3;
	private final ModelPart Body4;
	private final ModelPart Tail0;
	private final ModelPart Neck1;
	private final ModelPart Neck2;
	private final ModelPart Neck3;
	private final ModelPart Head1;
	private final ModelPart Head2;
	private final ModelPart Tail1;
	private final ModelPart Tail2;
	private final ModelPart Tail3;
	private final ModelPart BLegupleft;
	private final ModelPart FLegupleft;
	private final ModelPart BLegupright;
	private final ModelPart FLegupright;
	private final ModelPart BLegdownright;
	private final ModelPart FLegdownleft;
	private final ModelPart FLegdownright;
	private final ModelPart BLegdownleft;
	private final float wingspeed;

	public CamarasaurusModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Body1 = root.getChild("body1");
		this.Body2 = root.getChild("body2");
		this.Body3 = root.getChild("body3");
		this.Body4 = root.getChild("body4");
		this.Tail0 = root.getChild("tail0");
		this.Neck1 = root.getChild("neck1");
		this.Neck2 = root.getChild("neck2");
		this.Neck3 = root.getChild("neck3");
		this.Head1 = root.getChild("head1");
		this.Head2 = root.getChild("head2");
		this.Tail1 = root.getChild("tail1");
		this.Tail2 = root.getChild("tail2");
		this.Tail3 = root.getChild("tail3");
		this.BLegupleft = root.getChild("blegupleft");
		this.FLegupleft = root.getChild("flegupleft");
		this.BLegupright = root.getChild("blegupright");
		this.FLegupright = root.getChild("flegupright");
		this.BLegdownright = root.getChild("blegdownright");
		this.FLegdownleft = root.getChild("flegdownleft");
		this.FLegdownright = root.getChild("flegdownright");
		this.BLegdownleft = root.getChild("blegdownleft");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelCamarasaurus");
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
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.25F * f1;
		} else {
		newangle = 0.0F;
		}
		this.FLegupleft.xRot = newangle;
		this.FLegdownleft.xRot = newangle;
		this.FLegupright.xRot = -newangle;
		this.FLegdownright.xRot = -newangle;
		this.BLegupleft.xRot = -0.15F - newangle;
		this.BLegdownleft.xRot = -newangle;
		this.BLegupright.xRot = -0.15F + newangle;
		this.BLegdownright.xRot = newangle;
		// value: its (whole) health over its max health; the tail sways slower and less as it's hurt.
		hf = state.value;
		newangle = Mth.cos(f2 * 1.5F * this.wingspeed * hf) * (float) Math.PI * 0.25F * hf;
		if (state.sitting) {
		newangle = 0.0F;
		}
		this.Tail0.yRot = newangle * 0.25F;
		this.Tail1.z = this.Tail0.z + (float)Math.cos(this.Tail0.yRot) * 5.0F;
		this.Tail1.x = this.Tail0.x + (float)Math.sin(this.Tail0.yRot) * 5.0F;
		this.Tail1.yRot = newangle * 0.5F;
		this.Tail2.z = this.Tail1.z + (float)Math.cos(this.Tail1.yRot) * 8.0F;
		this.Tail2.x = this.Tail1.x + (float)Math.sin(this.Tail1.yRot) * 8.0F;
		this.Tail2.yRot = newangle * 0.75F;
		this.Tail3.z = this.Tail2.z + (float)Math.cos(this.Tail2.yRot) * 7.0F;
		this.Tail3.x = this.Tail2.x + (float)Math.sin(this.Tail2.yRot) * 7.0F;
		this.Tail3.yRot = newangle * 1.0F;
		this.Neck1.yRot = (float)Math.toRadians(f3) * 0.125F;
		this.Neck2.z = this.Neck1.z;
		this.Neck2.x = this.Neck1.x;
		this.Neck2.yRot = (float)Math.toRadians(f3) * 0.25F;
		this.Neck3.z = this.Neck2.z - (float)Math.cos(this.Neck2.yRot) * 6.0F;
		this.Neck3.x = this.Neck2.x - (float)Math.sin(this.Neck2.yRot) * 6.0F;
		this.Neck3.yRot = (float)Math.toRadians(f3) * 0.38F;
		this.Head1.z = this.Neck3.z - (float)Math.cos(this.Neck3.yRot) * 7.0F;
		this.Head1.x = this.Neck3.x - (float)Math.sin(this.Neck3.yRot) * 7.0F;
		this.Head1.yRot = (float)Math.toRadians(f3);
		this.Head2.z = this.Head1.z - (float)Math.cos(this.Head1.yRot) * 5.0F;
		this.Head2.x = this.Head1.x - (float)Math.sin(this.Head1.yRot) * 5.0F;
		this.Head2.yRot = (float)Math.toRadians(f3);
	}
}
