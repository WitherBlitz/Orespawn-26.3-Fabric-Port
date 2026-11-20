package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelGhost (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class GhostModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart HeadAndBody;
	private final ModelPart LArm;
	private final ModelPart RArm;

	public GhostModel(ModelPart root) {
		super(root);
		this.HeadAndBody = root.getChild("headandbody");
		this.LArm = root.getChild("larm");
		this.RArm = root.getChild("rarm");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelGhost");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		this.LArm.zRot = -0.33F + Mth.cos(f2 * 0.3F) * (float) Math.PI * 0.05F;
		this.RArm.zRot = 0.33F + Mth.cos(f2 * 0.32F) * (float) Math.PI * 0.05F;
		this.LArm.xRot = -0.33F + Mth.cos(f2 * 0.34F) * (float) Math.PI * 0.05F;
		this.RArm.xRot = 0.33F + Mth.cos(f2 * 0.36F) * (float) Math.PI * 0.05F;
		// OreSpawn drew the whole ghost blended at colour (0.75, 0.75, 0.75, 0.25): orespawn.client.content.GhostRenderer does that.
	}
}
