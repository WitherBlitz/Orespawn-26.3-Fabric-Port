package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelLeafMonster (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class LeafMonsterModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart larm;
	private final ModelPart rarm;
	private final ModelPart lleg;
	private final ModelPart rleg;

	public LeafMonsterModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.larm = root.getChild("larm");
		this.rarm = root.getChild("rarm");
		this.lleg = root.getChild("lleg");
		this.rleg = root.getChild("rleg");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelLeafMonster");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		// state.attacking: LeafMonster.getAttacking() != 0 (it stands up out of its bush).
		if (!state.attacking) {
		this.body.y = 16.0F;
		this.rarm.y = 8.0F;
		this.larm.y = 8.0F;
		this.rarm.yRot = 0.0F;
		this.larm.yRot = 0.0F;
		this.rarm.xRot = 0.0F;
		this.larm.xRot = 0.0F;
		this.lleg.xRot = 0.0F;
		this.rleg.xRot = 0.0F;
		} else {
		this.body.y = 0.0F;
		this.rarm.y = -8.0F;
		this.larm.y = -8.0F;
		float newangle;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 0.95F) * (float) Math.PI * 0.25F * f1;
		} else {
		newangle = 0.0F;
		}
		this.lleg.xRot = newangle;
		this.rleg.xRot = -newangle;
		newangle = Mth.cos(f2 * 0.7F) * (float) Math.PI * 0.55F;
		this.rarm.yRot = -Math.abs(newangle);
		this.larm.yRot = Math.abs(newangle);
		this.rarm.xRot = -Math.abs(newangle);
		this.larm.xRot = -Math.abs(newangle);
		}
	}
}
