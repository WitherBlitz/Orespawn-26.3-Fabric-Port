package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelMosquito (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class MosquitoModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart leftwing1;
	private final ModelPart rightwing1;
	private final ModelPart leftwing2;
	private final ModelPart rightwing2;

	public MosquitoModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.leftwing1 = root.getChild("leftwing1");
		this.rightwing1 = root.getChild("rightwing1");
		this.leftwing2 = root.getChild("leftwing2");
		this.rightwing2 = root.getChild("rightwing2");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelMosquito");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		this.rightwing1.zRot = Mth.cos(f2 * 3.0F) * (float) Math.PI * 0.25F;
		this.rightwing2.zRot = this.rightwing1.zRot;
		this.leftwing1.zRot = -this.rightwing1.zRot;
		this.leftwing2.zRot = -this.rightwing1.zRot;
	}
}
