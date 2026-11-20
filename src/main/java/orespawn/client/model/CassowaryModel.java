package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelCassowary (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CassowaryModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart tail;
	private final ModelPart body;
	private final ModelPart neck1;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart beak;
	private final ModelPart leg1;
	private final ModelPart leg2;
	private final ModelPart crest;
	private final ModelPart foot1;
	private final ModelPart foot2;
	private final ModelPart gobbler;
	private final float wingspeed;

	public CassowaryModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.tail = root.getChild("tail");
		this.body = root.getChild("body");
		this.neck1 = root.getChild("neck1");
		this.neck = root.getChild("neck");
		this.head = root.getChild("head");
		this.beak = root.getChild("beak");
		this.leg1 = root.getChild("leg1");
		this.leg2 = root.getChild("leg2");
		this.crest = root.getChild("crest");
		this.foot1 = root.getChild("foot1");
		this.foot2 = root.getChild("foot2");
		this.gobbler = root.getChild("gobbler");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelCassowary");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float newangle = 0.0F;
		float newangle2 = 0.0F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.15F * f1;
		newangle2 = Mth.cos(f2 * 2.6F * this.wingspeed) * (float) Math.PI * 0.1F * f1;
		} else {
		newangle2 = 0.0F;
		newangle = 0.0F;
		}
		this.leg1.xRot = this.foot2.xRot = newangle;
		this.leg2.xRot = this.foot1.xRot = -newangle;
		this.neck.xRot = -2.827F + newangle2;
		this.gobbler.xRot = newangle2;
		this.head.z = this.crest.z = this.beak.z = this.neck.z
		+ Mth.sin(this.neck.xRot) * 7.0F;
		this.head.y = this.crest.y = this.beak.y = this.neck.y
		+ Mth.cos(this.neck.xRot) * 7.0F;
	}
}
