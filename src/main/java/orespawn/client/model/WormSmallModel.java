package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelWormSmall (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class WormSmallModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart tail;

	public WormSmallModel(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
		this.body = root.getChild("body");
		this.tail = root.getChild("tail");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelWormSmall");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = Mth.cos(f2 * 0.55F) * (float) Math.PI * 0.15F;
		this.tail.xRot = newangle;
		float d1 = (float)(Math.sin(newangle) * 5.0);
		float d2 = (float)(Math.cos(newangle) * 5.0);
		this.body.z = this.tail.z - d1;
		newangle = Mth.cos(f2 * 0.35F) * (float) Math.PI * 0.1F;
		this.tail.zRot = newangle;
		float d3 = (float)(Math.cos(newangle) * d2);
		float d4 = (float)(Math.sin(newangle) * d2);
		this.body.x = this.tail.x + d4;
		this.body.y = (float)(this.tail.y - 5.0 + (5.0 - d3));
		newangle = Mth.cos(f2 * 0.45F) * (float) Math.PI * 0.15F;
		this.body.xRot = newangle;
		d1 = (float)(Math.sin(newangle) * 5.0);
		d2 = (float)(Math.cos(newangle) * 5.0);
		this.head.z = this.body.z - d1;
		newangle = Mth.cos(f2 * 0.25F) * (float) Math.PI * 0.1F;
		this.body.zRot = newangle;
		d3 = (float)(Math.cos(newangle) * d2);
		d4 = (float)(Math.sin(newangle) * d2);
		this.head.x = this.body.x + d4;
		this.head.y = (float)(this.body.y - 5.0 + (5.0 - d3));
		this.head.xRot = 0.62F + Mth.cos(f2 * 0.65F) * (float) Math.PI * 0.15F;
		this.head.zRot = Mth.cos(f2 * 0.3F) * (float) Math.PI * 0.05F;
	}
}
