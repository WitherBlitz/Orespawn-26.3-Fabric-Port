package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelRat (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class RatModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart lfleg;
	private final ModelPart rfleg;
	private final ModelPart lrleg;
	private final ModelPart rrleg;
	private final ModelPart body2;
	private final ModelPart head;
	private final ModelPart nose;
	private final ModelPart lear;
	private final ModelPart rear;
	private final float wingspeed;

	public RatModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.body = root.getChild("body");
		this.tail1 = root.getChild("tail1");
		this.tail2 = root.getChild("tail2");
		this.lfleg = root.getChild("lfleg");
		this.rfleg = root.getChild("rfleg");
		this.lrleg = root.getChild("lrleg");
		this.rrleg = root.getChild("rrleg");
		this.body2 = root.getChild("body2");
		this.head = root.getChild("head");
		this.nose = root.getChild("nose");
		this.lear = root.getChild("lear");
		this.rear = root.getChild("rear");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelRat");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float newangle = 0.0F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 1.7F * this.wingspeed) * (float) Math.PI * 0.25F * f1;
		} else {
		newangle = 0.0F;
		}
		this.rfleg.xRot = newangle;
		this.lfleg.xRot = -newangle;
		this.rrleg.xRot = -newangle;
		this.lrleg.xRot = newangle;
		if (state.attacking) {
		newangle = Mth.cos(f2 * 1.5F * this.wingspeed) * (float) Math.PI * 0.25F;
		} else {
		newangle = Mth.cos(f2 * 0.4F * this.wingspeed) * (float) Math.PI * 0.05F;
		}
		this.tail1.yRot = newangle * 0.5F;
		this.tail2.yRot = newangle * 1.25F;
		this.tail2.z = this.tail1.z + (float)Math.cos(this.tail1.yRot) * 9.0F;
		this.tail2.x = this.tail1.x + (float)Math.sin(this.tail1.yRot) * 9.0F;
	}
}
