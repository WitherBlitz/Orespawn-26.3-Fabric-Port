package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelFlounder (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class FlounderModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart rfin;
	private final ModelPart lfin;

	public FlounderModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.tail1 = root.getChild("tail1");
		this.tail2 = root.getChild("tail2");
		this.rfin = root.getChild("rfin");
		this.lfin = root.getChild("lfin");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelFlounder");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float newangle;
		float newangle2;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 1.3F) * (float) Math.PI * 0.25F * f1;
		newangle2 = Mth.cos(f2 * 1.7F) * (float) Math.PI * 0.25F * f1;
		} else {
		newangle = 0.0F;
		newangle2 = 0.0F;
		}
		this.lfin.zRot = newangle;
		this.rfin.zRot = newangle2;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 1.2F) * (float) Math.PI * 0.25F * f1;
		} else {
		newangle = Mth.cos(f2 * 0.7F) * (float) Math.PI * 0.05F;
		}
		this.tail1.xRot = this.tail2.xRot = newangle;
	}
}
