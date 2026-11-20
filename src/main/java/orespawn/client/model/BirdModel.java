package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelCockateil (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class BirdModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Body;
	private final ModelPart Head;
	private final ModelPart Beak;
	private final ModelPart LowerBeak;
	private final ModelPart feather2;
	private final ModelPart feather1;
	private final ModelPart feather3;
	private final ModelPart tailfeather1;
	private final ModelPart rwing1;
	private final ModelPart lwing1;
	private final ModelPart leg;
	private final ModelPart otherleg;
	private final ModelPart lwing2;
	private final ModelPart rwing2;
	private final ModelPart tailfeather2;
	private final ModelPart tailfeather3;
	private final float wingspeed;

	public BirdModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Body = root.getChild("body");
		this.Head = root.getChild("head");
		this.Beak = root.getChild("beak");
		this.LowerBeak = root.getChild("lowerbeak");
		this.feather2 = root.getChild("feather2");
		this.feather1 = root.getChild("feather1");
		this.feather3 = root.getChild("feather3");
		this.tailfeather1 = root.getChild("tailfeather1");
		this.rwing1 = root.getChild("rwing1");
		this.lwing1 = root.getChild("lwing1");
		this.leg = root.getChild("leg");
		this.otherleg = root.getChild("otherleg");
		this.lwing2 = root.getChild("lwing2");
		this.rwing2 = root.getChild("rwing2");
		this.tailfeather2 = root.getChild("tailfeather2");
		this.tailfeather3 = root.getChild("tailfeather3");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelCockateil");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float newangle = 0.0F;
		newangle = Mth.cos(f2 * 1.5F * this.wingspeed) * (float) Math.PI * 0.35F;
		this.lwing1.zRot = -1.5F + newangle;
		this.lwing2.zRot = newangle;
		this.rwing1.zRot = 1.5F - newangle;
		this.rwing2.zRot = -newangle;
		newangle = Mth.cos(f2 * 0.3F * this.wingspeed) * (float) Math.PI * 0.1F;
		this.tailfeather1.xRot = newangle;
		this.tailfeather2.xRot = newangle;
		this.tailfeather3.xRot = newangle;
		newangle = Mth.cos(f2 * 1.1F * this.wingspeed) * (float) Math.PI * 0.08F;
		this.feather1.zRot = newangle;
		newangle = Mth.cos(f2 * 1.2F * this.wingspeed) * (float) Math.PI * 0.08F;
		this.feather2.zRot = newangle;
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.08F;
		this.feather3.zRot = newangle;
	}
}
