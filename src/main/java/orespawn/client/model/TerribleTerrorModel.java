package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelTerribleTerror (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class TerribleTerrorModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Horn1;
	private final ModelPart Horn2;
	private final ModelPart Snout;
	private final ModelPart Head;
	private final ModelPart Jaw;
	private final ModelPart Neck;
	private final ModelPart Body;
	private final ModelPart Wing1;
	private final ModelPart Wing2;
	private final ModelPart Tail1;
	private final ModelPart Tail2;
	private final ModelPart Tail3;
	private final ModelPart Tail4;
	private final ModelPart FL11;
	private final ModelPart FL12;
	private final ModelPart FL21;
	private final ModelPart FL22;
	private final ModelPart BL21;
	private final ModelPart BL22;
	private final ModelPart BL11;
	private final ModelPart BL12;
	private float wingspeed = 1.0F;

	public TerribleTerrorModel(ModelPart root) {
		super(root);
		this.Horn1 = root.getChild("horn1");
		this.Horn2 = root.getChild("horn2");
		this.Snout = root.getChild("snout");
		this.Head = root.getChild("head");
		this.Jaw = root.getChild("jaw");
		this.Neck = root.getChild("neck");
		this.Body = root.getChild("body");
		this.Wing1 = root.getChild("wing1");
		this.Wing2 = root.getChild("wing2");
		this.Tail1 = root.getChild("tail1");
		this.Tail2 = root.getChild("tail2");
		this.Tail3 = root.getChild("tail3");
		this.Tail4 = root.getChild("tail4");
		this.FL11 = root.getChild("fl11");
		this.FL12 = root.getChild("fl12");
		this.FL21 = root.getChild("fl21");
		this.FL22 = root.getChild("fl22");
		this.BL21 = root.getChild("bl21");
		this.BL22 = root.getChild("bl22");
		this.BL11 = root.getChild("bl11");
		this.BL12 = root.getChild("bl12");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelTerribleTerror");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.25F;
		this.Wing1.zRot = -2.0F + newangle;
		this.Wing2.zRot = 2.0F - newangle;
		newangle = Mth.cos(f2 * 0.3F * this.wingspeed) * (float) Math.PI * 0.1F;
		this.Jaw.xRot = Math.abs(newangle);
		newangle = Mth.cos(f2 * 1.25F) * (float) Math.PI * 0.35F;
		this.FL21.xRot = 0.349F + newangle;
		this.FL22.xRot = -0.296F + newangle;
		this.BL21.xRot = -0.349F - newangle;
		this.BL22.xRot = 0.174F - newangle;
		this.FL11.xRot = 0.349F - newangle;
		this.FL12.xRot = -0.296F - newangle;
		this.BL11.xRot = -0.349F + newangle;
		this.BL12.xRot = 0.174F + newangle;
		newangle = Mth.cos(f2 * 0.71F * this.wingspeed) * (float) Math.PI * 0.1F;
		this.Tail1.xRot = newangle;
		newangle = Mth.cos(f2 * 0.77F * this.wingspeed) * (float) Math.PI * 0.1F;
		this.Tail1.yRot = newangle;
		float dist = 6.0F;
		dist = (float)(dist * Math.cos(this.Tail1.xRot));
		this.Tail2.y = (float)(this.Tail1.y - Math.sin(this.Tail1.xRot) * dist);
		this.Tail2.x = (float)(this.Tail1.x + Math.sin(this.Tail1.yRot) * dist);
		newangle = Mth.cos(f2 * 0.81F * this.wingspeed) * (float) Math.PI * 0.15F;
		this.Tail2.xRot = newangle;
		newangle = Mth.cos(f2 * 0.87F * this.wingspeed) * (float) Math.PI * 0.15F;
		this.Tail2.yRot = newangle;
		dist = 6.0F;
		dist = (float)(dist * Math.cos(this.Tail2.xRot));
		this.Tail3.y = this.Tail4.y = (float)(this.Tail2.y - Math.sin(this.Tail2.xRot) * dist);
		this.Tail3.x = this.Tail4.x = (float)(this.Tail2.x + Math.sin(this.Tail2.yRot) * dist);
		newangle = Mth.cos(f2 * 0.91F * this.wingspeed) * (float) Math.PI * 0.2F;
		this.Tail3.xRot = this.Tail4.xRot = newangle;
		newangle = Mth.cos(f2 * 0.97F * this.wingspeed) * (float) Math.PI * 0.2F;
		this.Tail3.yRot = this.Tail4.yRot = newangle;
	}
}
