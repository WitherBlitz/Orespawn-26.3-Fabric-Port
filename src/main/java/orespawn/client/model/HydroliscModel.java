package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelHydrolisc (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class HydroliscModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart tail2;
	private final ModelPart tail3;
	private final ModelPart body2;
	private final ModelPart lb2;
	private final ModelPart lb1;
	private final ModelPart spine3;
	private final ModelPart spine4;
	private final ModelPart rb1;
	private final ModelPart rb2;
	private final ModelPart spine1;
	private final ModelPart spine2;
	private final ModelPart lb3;
	private final ModelPart rb3;
	private final ModelPart body1;
	private final ModelPart body0;
	private final ModelPart lf1;
	private final ModelPart rf1;
	private final ModelPart rb6;
	private final ModelPart rb4;
	private final ModelPart rb5;
	private final ModelPart lb6;
	private final ModelPart lb5;
	private final ModelPart lb4;
	private final ModelPart head3;
	private final ModelPart feather3;
	private final ModelPart feather1;
	private final ModelPart feather2;
	private final ModelPart head1;
	private final ModelPart rf2;
	private final ModelPart rf3;
	private final ModelPart rf4;
	private final ModelPart rf5;
	private final ModelPart rf6;
	private final ModelPart lf2;
	private final ModelPart lf3;
	private final ModelPart lf4;
	private final ModelPart lf5;
	private final ModelPart lf6;
	private final ModelPart head2;
	private final ModelPart tail1;
	private final float wingspeed;

	public HydroliscModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.tail2 = root.getChild("tail2");
		this.tail3 = root.getChild("tail3");
		this.body2 = root.getChild("body2");
		this.lb2 = root.getChild("lb2");
		this.lb1 = root.getChild("lb1");
		this.spine3 = root.getChild("spine3");
		this.spine4 = root.getChild("spine4");
		this.rb1 = root.getChild("rb1");
		this.rb2 = root.getChild("rb2");
		this.spine1 = root.getChild("spine1");
		this.spine2 = root.getChild("spine2");
		this.lb3 = root.getChild("lb3");
		this.rb3 = root.getChild("rb3");
		this.body1 = root.getChild("body1");
		this.body0 = root.getChild("body0");
		this.lf1 = root.getChild("lf1");
		this.rf1 = root.getChild("rf1");
		this.rb6 = root.getChild("rb6");
		this.rb4 = root.getChild("rb4");
		this.rb5 = root.getChild("rb5");
		this.lb6 = root.getChild("lb6");
		this.lb5 = root.getChild("lb5");
		this.lb4 = root.getChild("lb4");
		this.head3 = root.getChild("head3");
		this.feather3 = root.getChild("feather3");
		this.feather1 = root.getChild("feather1");
		this.feather2 = root.getChild("feather2");
		this.head1 = root.getChild("head1");
		this.rf2 = root.getChild("rf2");
		this.rf3 = root.getChild("rf3");
		this.rf4 = root.getChild("rf4");
		this.rf5 = root.getChild("rf5");
		this.rf6 = root.getChild("rf6");
		this.lf2 = root.getChild("lf2");
		this.lf3 = root.getChild("lf3");
		this.lf4 = root.getChild("lf4");
		this.lf5 = root.getChild("lf5");
		this.lf6 = root.getChild("lf6");
		this.head2 = root.getChild("head2");
		this.tail1 = root.getChild("tail1");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelHydrolisc");
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
		this.lf1.xRot = newangle;
		this.lf2.xRot = newangle - 0.488F;
		this.lf3.xRot = newangle - 2.347F;
		this.lf4.xRot = newangle - 0.628F;
		this.lf5.xRot = newangle - 0.628F;
		this.lf6.xRot = newangle + 0.174F;
		this.rf1.xRot = -newangle;
		this.rf2.xRot = -newangle - 0.488F;
		this.rf3.xRot = -newangle - 2.347F;
		this.rf4.xRot = -newangle - 0.628F;
		this.rf5.xRot = -newangle - 0.628F;
		this.rf6.xRot = -newangle + 0.174F;
		this.lb1.xRot = -newangle;
		this.lb2.xRot = -newangle - 0.488F;
		this.lb3.xRot = -newangle - 2.347F;
		this.lb4.xRot = -newangle - 0.628F;
		this.lb5.xRot = -newangle - 0.628F;
		this.lb6.xRot = -newangle + 0.174F;
		this.rb1.xRot = newangle;
		this.rb2.xRot = newangle - 0.488F;
		this.rb3.xRot = newangle - 2.347F;
		this.rb4.xRot = newangle - 0.628F;
		this.rb5.xRot = newangle - 0.628F;
		this.rb6.xRot = newangle + 0.174F;
		newangle = Mth.cos(f2 * 1.0F * this.wingspeed) * (float) Math.PI * 0.15F;
		if (state.sitting) {
		newangle = 0.0F;
		}
		this.tail1.yRot = newangle * 0.25F;
		this.tail2.z = this.tail1.z + (float)Math.cos(this.tail1.yRot) * 5.0F;
		this.tail2.x = this.tail1.x + (float)Math.sin(this.tail1.yRot) * 5.0F;
		this.tail2.yRot = newangle * 0.5F;
		this.tail3.z = this.tail2.z + (float)Math.cos(this.tail2.yRot) * 8.0F;
		this.tail3.x = this.tail2.x + (float)Math.sin(this.tail2.yRot) * 8.0F;
		this.tail3.yRot = newangle * 0.75F;
		// state.value: its health over its max health (the feathers slow down and droop as it's hurt).
		hf = state.value;
		newangle = Mth.cos(f2 * 1.25F * this.wingspeed * hf) * (float) Math.PI * 0.2F * hf;
		this.feather2.yRot = newangle;
		newangle = Mth.cos(f2 * 0.75F * this.wingspeed * hf) * (float) Math.PI * 0.2F * hf;
		this.feather1.yRot = newangle - 0.9F;
		this.feather3.yRot = -newangle + 0.9F;
	}
}
