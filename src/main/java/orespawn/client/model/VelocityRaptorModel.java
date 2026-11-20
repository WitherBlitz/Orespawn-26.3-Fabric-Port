package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelVelocityRaptor (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class VelocityRaptorModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart hf3;
	private final ModelPart hf4;
	private final ModelPart hf2;
	private final ModelPart hf1;
	private final ModelPart lff2;
	private final ModelPart lff1;
	private final ModelPart lff3;
	private final ModelPart rff2;
	private final ModelPart rff3;
	private final ModelPart rff1;
	private final ModelPart tf4;
	private final ModelPart tf1;
	private final ModelPart Shape1;
	private final ModelPart neck;
	private final ModelPart head1;
	private final ModelPart lf1;
	private final ModelPart lf2;
	private final ModelPart head2;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart bl1;
	private final ModelPart br1;
	private final ModelPart bl2;
	private final ModelPart br2;
	private final ModelPart bl3;
	private final ModelPart br3;
	private final ModelPart rf1;
	private final ModelPart rf2;
	private final ModelPart tf2;
	private final ModelPart tf3;
	private final ModelPart bl4;
	private final ModelPart br4;
	private final ModelPart Hat1;
	private final ModelPart Hat2;
	private final float wingspeed;

	public VelocityRaptorModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.hf3 = root.getChild("hf3");
		this.hf4 = root.getChild("hf4");
		this.hf2 = root.getChild("hf2");
		this.hf1 = root.getChild("hf1");
		this.lff2 = root.getChild("lff2");
		this.lff1 = root.getChild("lff1");
		this.lff3 = root.getChild("lff3");
		this.rff2 = root.getChild("rff2");
		this.rff3 = root.getChild("rff3");
		this.rff1 = root.getChild("rff1");
		this.tf4 = root.getChild("tf4");
		this.tf1 = root.getChild("tf1");
		this.Shape1 = root.getChild("shape1");
		this.neck = root.getChild("neck");
		this.head1 = root.getChild("head1");
		this.lf1 = root.getChild("lf1");
		this.lf2 = root.getChild("lf2");
		this.head2 = root.getChild("head2");
		this.tail1 = root.getChild("tail1");
		this.tail2 = root.getChild("tail2");
		this.bl1 = root.getChild("bl1");
		this.br1 = root.getChild("br1");
		this.bl2 = root.getChild("bl2");
		this.br2 = root.getChild("br2");
		this.bl3 = root.getChild("bl3");
		this.br3 = root.getChild("br3");
		this.rf1 = root.getChild("rf1");
		this.rf2 = root.getChild("rf2");
		this.tf2 = root.getChild("tf2");
		this.tf3 = root.getChild("tf3");
		this.bl4 = root.getChild("bl4");
		this.br4 = root.getChild("br4");
		this.Hat1 = root.getChild("hat1");
		this.Hat2 = root.getChild("hat2");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelVelocityRaptor");
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
		this.bl1.xRot = newangle;
		this.bl2.xRot = newangle + 0.488F;
		this.bl3.xRot = newangle;
		this.bl4.xRot = newangle + 0.628F;
		this.br1.xRot = -newangle;
		this.br2.xRot = -newangle + 0.488F;
		this.br3.xRot = -newangle;
		this.br4.xRot = -newangle + 0.628F;
		// value: its (whole) health over its max health; hurt raptors flick their head feathers and tail less.
		hf = state.value;
		newangle = Mth.cos(f2 * 1.25F * this.wingspeed * hf) * (float) Math.PI * 0.1F * hf;
		this.hf1.yRot = newangle;
		this.hf2.yRot = -newangle;
		this.hf3.yRot = newangle;
		this.hf4.yRot = -newangle;
		newangle = Mth.cos(f2 * 0.3F) * (float) Math.PI * 0.05F;
		this.lf1.xRot = newangle + 0.279F;
		this.lf2.xRot = newangle - 0.436F;
		this.lff1.xRot = newangle - 0.279F;
		this.lff2.xRot = newangle - 0.453F;
		this.lff3.xRot = newangle - 1.047F;
		this.rf1.xRot = -newangle + 0.279F;
		this.rf2.xRot = -newangle - 0.436F;
		this.rff1.xRot = -newangle - 0.279F;
		this.rff2.xRot = -newangle - 0.453F;
		this.rff3.xRot = -newangle - 1.047F;
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.1F;
		this.lff1.yRot = newangle;
		this.lff2.yRot = -newangle;
		this.lff3.yRot = newangle;
		this.rff1.yRot = -newangle;
		this.rff2.yRot = newangle;
		this.rff3.yRot = -newangle;
		if (state.sitting) {
		newangle = 0.0F;
		} else {
		newangle = Mth.cos(f2 * 1.4F * this.wingspeed * hf) * (float) Math.PI * 0.25F * hf;
		}
		this.tf1.zRot = newangle;
		this.tf2.zRot = -newangle;
		this.tf3.zRot = newangle;
		this.tf4.zRot = -newangle;
		// variant: OreSpawn's cannon-fodder activation (0 none, 1 a hat, 2 two hats); hats show only when it's set.
		this.Hat1.visible = state.variant != 0;
		this.Hat2.visible = state.variant > 1;
	}
}
