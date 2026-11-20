package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelWhale (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class WhaleModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart belly;
	private final ModelPart body;
	private final ModelPart back;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart tailfin1;
	private final ModelPart tailfin2;
	private final ModelPart backfin;
	private final ModelPart head;
	private final ModelPart jaw;
	private final ModelPart lfin1;
	private final ModelPart lfin2;
	private final ModelPart rfin1;
	private final ModelPart rfin2;

	public WhaleModel(ModelPart root) {
		super(root);
		this.belly = root.getChild("belly");
		this.body = root.getChild("body");
		this.back = root.getChild("back");
		this.tail1 = root.getChild("tail1");
		this.tail2 = root.getChild("tail2");
		this.tailfin1 = root.getChild("tailfin1");
		this.tailfin2 = root.getChild("tailfin2");
		this.backfin = root.getChild("backfin");
		this.head = root.getChild("head");
		this.jaw = root.getChild("jaw");
		this.lfin1 = root.getChild("lfin1");
		this.lfin2 = root.getChild("lfin2");
		this.rfin1 = root.getChild("rfin1");
		this.rfin2 = root.getChild("rfin2");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelWhale");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float newangle = Mth.cos(f2 * 0.55F) * (float) Math.PI * 0.15F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 0.3F) * (float) Math.PI * 0.2F * f1;
		} else {
		newangle = Mth.cos(f2 * 0.08F) * (float) Math.PI * 0.05F;
		}
		this.lfin2.zRot = 0.436F + newangle;
		this.lfin1.zRot = this.lfin2.zRot / 2.0F;
		this.rfin2.zRot = -0.436F - newangle;
		this.rfin1.zRot = this.rfin2.zRot / 2.0F;
		newangle = Mth.cos(f2 * 0.03F) * (float) Math.PI * 0.02F;
		this.jaw.xRot = 0.087F + newangle;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 0.4F) * (float) Math.PI * 0.16F * f1;
		} else {
		newangle = Mth.cos(f2 * 0.05F) * (float) Math.PI * 0.03F;
		}
		this.tail1.xRot = newangle * 0.5F;
		this.tail2.xRot = newangle * 1.25F;
		this.tailfin1.xRot = this.tailfin2.xRot = newangle * 2.25F;
		this.tail2.z = this.tail1.z + (float)Math.cos(this.tail1.xRot) * 14.0F;
		this.tail2.y = this.tail1.y - (float)Math.sin(this.tail1.xRot) * 14.0F;
		this.tailfin1.z = this.tailfin2.z = this.tail2.z + (float)Math.cos(this.tail2.xRot) * 8.0F;
		this.tailfin1.y = this.tailfin2.y = this.tail2.y - (float)Math.sin(this.tail2.xRot) * 8.0F;
	}
}
