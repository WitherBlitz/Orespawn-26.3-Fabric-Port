package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelGoldFish (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class GoldFishModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Body;
	private final ModelPart Head;
	private final ModelPart Dorsalfin;
	private final ModelPart Mouth;
	private final ModelPart Jaw;
	private final ModelPart Pectoralfin1;
	private final ModelPart Pectoralfin2;
	private final ModelPart Pectoralfin3;
	private final ModelPart Pectoralfin4;
	private final ModelPart Bottomfin;
	private final ModelPart Tail1;
	private final ModelPart Tail2;
	private final ModelPart Caudalfin1;
	private final ModelPart Caudalfin2;
	private final ModelPart Bottomfin1;
	private final ModelPart Bottomfin2;
	private final float wingspeed;

	/** ClientProxyOreSpawn passes 0.7 as the fin speed. */
	public GoldFishModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Body = root.getChild("body");
		this.Head = root.getChild("head");
		this.Dorsalfin = root.getChild("dorsalfin");
		this.Mouth = root.getChild("mouth");
		this.Jaw = root.getChild("jaw");
		this.Pectoralfin1 = root.getChild("pectoralfin1");
		this.Pectoralfin2 = root.getChild("pectoralfin2");
		this.Pectoralfin3 = root.getChild("pectoralfin3");
		this.Pectoralfin4 = root.getChild("pectoralfin4");
		this.Bottomfin = root.getChild("bottomfin");
		this.Tail1 = root.getChild("tail1");
		this.Tail2 = root.getChild("tail2");
		this.Caudalfin1 = root.getChild("caudalfin1");
		this.Caudalfin2 = root.getChild("caudalfin2");
		this.Bottomfin1 = root.getChild("bottomfin1");
		this.Bottomfin2 = root.getChild("bottomfin2");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelGoldFish");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float newangle = 0.0F;
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.15F;
		this.Pectoralfin1.yRot = 0.4F + newangle;
		newangle = Mth.cos(f2 * 1.2F * this.wingspeed) * (float) Math.PI * 0.15F;
		this.Pectoralfin2.yRot = -0.4F + newangle;
		newangle = Mth.cos(f2 * 1.1F * this.wingspeed) * (float) Math.PI * 0.15F;
		this.Pectoralfin3.yRot = 0.4F + newangle;
		newangle = Mth.cos(f2 * 1.0F * this.wingspeed) * (float) Math.PI * 0.15F;
		this.Pectoralfin4.yRot = -0.4F + newangle;
		newangle = Mth.cos(f2 * 1.7F * this.wingspeed) * (float) Math.PI * 0.25F;
		this.Bottomfin1.yRot = newangle;
		this.Bottomfin2.yRot = -newangle;
		newangle = Mth.cos(f2 * 0.7F * this.wingspeed) * (float) Math.PI * 0.1F;
		this.Jaw.xRot = -0.25F + newangle;
	}
}
