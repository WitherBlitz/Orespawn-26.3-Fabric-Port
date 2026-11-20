package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelGhostSkelly (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class GhostSkellyModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart shirt;
	private final ModelPart head;
	private final ModelPart stem;
	private final ModelPart rarm;
	private final ModelPart larm;
	private final ModelPart rsleeve;
	private final ModelPart lsleeve;
	private final ModelPart lchains;
	private final ModelPart rchains;

	public GhostSkellyModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.shirt = root.getChild("shirt");
		this.head = root.getChild("head");
		this.stem = root.getChild("stem");
		this.rarm = root.getChild("rarm");
		this.larm = root.getChild("larm");
		this.rsleeve = root.getChild("rsleeve");
		this.lsleeve = root.getChild("lsleeve");
		this.lchains = root.getChild("lchains");
		this.rchains = root.getChild("rchains");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelGhostSkelly");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		this.larm.zRot = this.lsleeve.zRot = this.lchains.zRot = Mth.cos(f2 * 0.2F) * (float) Math.PI * 0.05F;
		this.rarm.zRot = this.rsleeve.zRot = this.rchains.zRot = Mth.cos(f2 * 0.22F) * (float) Math.PI * 0.05F;
		this.larm.yRot = this.lsleeve.yRot = this.lchains.yRot = Mth.cos(f2 * 0.24F) * (float) Math.PI * 0.05F;
		this.rarm.yRot = this.rsleeve.yRot = this.rchains.yRot = Mth.cos(f2 * 0.26F) * (float) Math.PI * 0.05F;
		// The pumpkin head spins round and back over each cycle of cos(age * 0.05); OreSpawn rolled a 1 in 3 chance per
		// cycle that it spins at all. state.flag is that roll for the current cycle (made by the Spooky client renderer).
		newangle = Mth.cos(f2 * 0.05F) * (float) Math.PI * 2.0F;
		if (!state.flag) {
		newangle = 0.0F;
		}
		this.head.yRot = newangle;
		// OreSpawn drew the whole skelly blended at colour (0.75, 0.75, 0.75, 0.25): orespawn.client.content.GhostRenderer does that.
	}
}
