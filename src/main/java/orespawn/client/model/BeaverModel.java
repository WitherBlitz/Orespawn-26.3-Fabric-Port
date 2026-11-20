package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelBeaver (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class BeaverModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart head;
	private final ModelPart nose;
	private final ModelPart teeth;
	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart rff;
	private final ModelPart lff;
	private final ModelPart rrf;
	private final ModelPart lrf;
	private final float wingspeed;

	public BeaverModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.head = root.getChild("head");
		this.nose = root.getChild("nose");
		this.teeth = root.getChild("teeth");
		this.body = root.getChild("body");
		this.tail = root.getChild("tail");
		this.rff = root.getChild("rff");
		this.lff = root.getChild("lff");
		this.rrf = root.getChild("rrf");
		this.lrf = root.getChild("lrf");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelBeaver");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float newangle = 0.0F;
		newangle = Mth.cos(f2 * 3.7F * this.wingspeed) * (float) Math.PI * 0.45F * f1;
		this.rff.xRot = this.lrf.xRot = newangle;
		this.lff.xRot = this.rrf.xRot = -newangle;
		newangle = Mth.cos(f2 * 2.7F * this.wingspeed) * (float) Math.PI * 0.25F;
		this.teeth.xRot = newangle;
		newangle = Mth.cos(f2 * 0.5F * this.wingspeed) * (float) Math.PI * 0.05F;
		this.tail.xRot = newangle;
	}
}
