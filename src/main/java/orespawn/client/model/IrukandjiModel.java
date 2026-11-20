package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelIrukandji (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class IrukandjiModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart t11;
	private final ModelPart t12;
	private final ModelPart t21;
	private final ModelPart t22;
	private final ModelPart t31;
	private final ModelPart t32;
	private final ModelPart t41;
	private final ModelPart t42;
	private final float wingspeed;

	public IrukandjiModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.body = root.getChild("body");
		this.t11 = root.getChild("t11");
		this.t12 = root.getChild("t12");
		this.t21 = root.getChild("t21");
		this.t22 = root.getChild("t22");
		this.t31 = root.getChild("t31");
		this.t32 = root.getChild("t32");
		this.t41 = root.getChild("t41");
		this.t42 = root.getChild("t42");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelIrukandji");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		newangle = Mth.cos(f2 * 0.55F) * (float) Math.PI * 0.15F;
		this.t11.xRot = newangle;
		float d1 = (float)(Math.sin(newangle) * 7.0);
		float d2 = (float)(Math.cos(newangle) * 7.0);
		this.t12.z = this.t11.z + d1;
		newangle = Mth.cos(f2 * 0.35F) * (float) Math.PI * 0.1F;
		this.t11.zRot = newangle;
		float d3 = (float)(Math.cos(newangle) * d2);
		float d4 = (float)(Math.sin(newangle) * d2);
		this.t12.x = this.t11.x - d4;
		this.t12.y = this.t11.y + d3;
		newangle = Mth.cos(f2 * 0.45F) * (float) Math.PI * 0.15F;
		this.t12.xRot = newangle;
		newangle = Mth.cos(f2 * 0.25F) * (float) Math.PI * 0.1F;
		this.t12.zRot = newangle;
		newangle = Mth.cos(f2 * 0.65F) * (float) Math.PI * 0.15F;
		this.t21.xRot = newangle;
		d1 = (float)(Math.sin(newangle) * 7.0);
		d2 = (float)(Math.cos(newangle) * 7.0);
		this.t22.z = this.t21.z + d1;
		newangle = Mth.cos(f2 * 0.45F) * (float) Math.PI * 0.1F;
		this.t21.zRot = newangle;
		d3 = (float)(Math.cos(newangle) * d2);
		d4 = (float)(Math.sin(newangle) * d2);
		this.t22.x = this.t21.x - d4;
		this.t22.y = this.t21.y + d3;
		newangle = Mth.cos(f2 * 0.55F) * (float) Math.PI * 0.15F;
		this.t22.xRot = newangle;
		newangle = Mth.cos(f2 * 0.35F) * (float) Math.PI * 0.1F;
		this.t22.zRot = newangle;
		newangle = Mth.cos(f2 * 0.5F) * (float) Math.PI * 0.15F;
		this.t31.xRot = newangle;
		d1 = (float)(Math.sin(newangle) * 7.0);
		d2 = (float)(Math.cos(newangle) * 7.0);
		this.t32.z = this.t31.z + d1;
		newangle = Mth.cos(f2 * 0.3F) * (float) Math.PI * 0.1F;
		this.t31.zRot = newangle;
		d3 = (float)(Math.cos(newangle) * d2);
		d4 = (float)(Math.sin(newangle) * d2);
		this.t32.x = this.t31.x - d4;
		this.t32.y = this.t31.y + d3;
		newangle = Mth.cos(f2 * 0.4F) * (float) Math.PI * 0.15F;
		this.t32.xRot = newangle;
		newangle = Mth.cos(f2 * 0.2F) * (float) Math.PI * 0.1F;
		this.t32.zRot = newangle;
		newangle = Mth.cos(f2 * 0.57F) * (float) Math.PI * 0.15F;
		this.t41.xRot = newangle;
		d1 = (float)(Math.sin(newangle) * 7.0);
		d2 = (float)(Math.cos(newangle) * 7.0);
		this.t42.z = this.t41.z + d1;
		newangle = Mth.cos(f2 * 0.37F) * (float) Math.PI * 0.1F;
		this.t41.zRot = newangle;
		d3 = (float)(Math.cos(newangle) * d2);
		d4 = (float)(Math.sin(newangle) * d2);
		this.t42.x = this.t41.x - d4;
		this.t42.y = this.t41.y + d3;
		newangle = Mth.cos(f2 * 0.48F) * (float) Math.PI * 0.15F;
		this.t42.xRot = newangle;
		newangle = Mth.cos(f2 * 0.29F) * (float) Math.PI * 0.1F;
		this.t42.zRot = newangle;
	}
}
