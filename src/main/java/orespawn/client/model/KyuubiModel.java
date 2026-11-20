package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelKyuubi (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class KyuubiModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart rtHorn5;
	private final ModelPart lfHorn5;
	private final ModelPart tail9;
	private final ModelPart tail8;
	private final ModelPart tail7;
	private final ModelPart tail6;
	private final ModelPart tail5;
	private final ModelPart tail2;
	private final ModelPart tail1;
	private final ModelPart tail0;
	private final ModelPart lfLegLower;
	private final ModelPart rtLegLower;
	private final ModelPart head;
	private final ModelPart chest;
	private final ModelPart lfArmUpper;
	private final ModelPart rtArmLower;
	private final ModelPart lfLegUpper;
	private final ModelPart rtLegUpper;
	private final ModelPart body;
	private final ModelPart rtArmUpper;
	private final ModelPart lfArmLower;
	private final ModelPart tail3;
	private final ModelPart tail4;
	private final ModelPart lfHorn2;
	private final ModelPart rtHorn1;
	private final ModelPart rtHorn2;
	private final ModelPart lfHorn1;
	private final ModelPart lfHorn3;
	private final ModelPart rtHorn3;
	private final ModelPart lfHorn4;
	private final ModelPart rtHorn4;
	private final ModelPart headFire;
	private final ModelPart lfArmUpperFire;
	private final ModelPart chestFire;
	private final ModelPart bodyFire;
	private final ModelPart lfArmLowerFire;
	private final ModelPart rtArmUpperFire;
	private final ModelPart rtArmLowerFire;
	private final ModelPart lfLegUppperFire;
	private final ModelPart lfLegLowerFire;
	private final ModelPart rtLegUpperFire;
	private final ModelPart rtLegLowerFire;
	private final float wingspeed;

	public KyuubiModel(ModelPart root, float wingspeed) {
		// OreSpawn drew the whole Kyuubi with alpha blending (its fire is see-through).
		super(root, RenderTypes::entityTranslucent);
		this.wingspeed = wingspeed;
		this.rtHorn5 = root.getChild("rthorn5");
		this.lfHorn5 = root.getChild("lfhorn5");
		this.tail9 = root.getChild("tail9");
		this.tail8 = root.getChild("tail8");
		this.tail7 = root.getChild("tail7");
		this.tail6 = root.getChild("tail6");
		this.tail5 = root.getChild("tail5");
		this.tail2 = root.getChild("tail2");
		this.tail1 = root.getChild("tail1");
		this.tail0 = root.getChild("tail0");
		this.lfLegLower = root.getChild("lfleglower");
		this.rtLegLower = root.getChild("rtleglower");
		this.head = root.getChild("head");
		this.chest = root.getChild("chest");
		this.lfArmUpper = root.getChild("lfarmupper");
		this.rtArmLower = root.getChild("rtarmlower");
		this.lfLegUpper = root.getChild("lflegupper");
		this.rtLegUpper = root.getChild("rtlegupper");
		this.body = root.getChild("body");
		this.rtArmUpper = root.getChild("rtarmupper");
		this.lfArmLower = root.getChild("lfarmlower");
		this.tail3 = root.getChild("tail3");
		this.tail4 = root.getChild("tail4");
		this.lfHorn2 = root.getChild("lfhorn2");
		this.rtHorn1 = root.getChild("rthorn1");
		this.rtHorn2 = root.getChild("rthorn2");
		this.lfHorn1 = root.getChild("lfhorn1");
		this.lfHorn3 = root.getChild("lfhorn3");
		this.rtHorn3 = root.getChild("rthorn3");
		this.lfHorn4 = root.getChild("lfhorn4");
		this.rtHorn4 = root.getChild("rthorn4");
		this.headFire = root.getChild("headfire");
		this.lfArmUpperFire = root.getChild("lfarmupperfire");
		this.chestFire = root.getChild("chestfire");
		this.bodyFire = root.getChild("bodyfire");
		this.lfArmLowerFire = root.getChild("lfarmlowerfire");
		this.rtArmUpperFire = root.getChild("rtarmupperfire");
		this.rtArmLowerFire = root.getChild("rtarmlowerfire");
		this.lfLegUppperFire = root.getChild("lfleguppperfire");
		this.lfLegLowerFire = root.getChild("lfleglowerfire");
		this.rtLegUpperFire = root.getChild("rtlegupperfire");
		this.rtLegLowerFire = root.getChild("rtleglowerfire");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelKyuubi");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 1.1F * this.wingspeed) * (float) Math.PI * 0.2F * f1;
		} else {
		newangle = 0.0F;
		}
		this.rtLegUpper.xRot = 0.59F + newangle;
		this.rtLegUpperFire.xRot = 0.59F + newangle;
		this.rtLegLower.xRot = -0.15F + newangle;
		this.rtLegLowerFire.xRot = -0.15F + newangle;
		this.rtLegLower.z = (float)(Math.sin(this.rtLegUpperFire.xRot) * 8.0);
		this.rtLegLowerFire.z = (float)(Math.sin(this.rtLegUpperFire.xRot) * 8.0);
		this.lfLegUpper.xRot = 0.26F - newangle;
		this.lfLegUppperFire.xRot = 0.26F - newangle;
		this.lfLegLower.xRot = -0.44F - newangle;
		this.lfLegLowerFire.xRot = -0.44F - newangle;
		this.lfLegLower.z = (float)(Math.sin(this.lfLegUppperFire.xRot) * 8.0);
		this.lfLegLowerFire.z = (float)(Math.sin(this.lfLegUppperFire.xRot) * 8.0);
		newangle = Mth.cos(f2 * 1.1F * this.wingspeed) * (float) Math.PI * 0.08F * f1;
		newangle += Mth.cos(f2 * 0.5F * this.wingspeed) * (float) Math.PI * 0.01F;
		this.rtArmUpper.xRot = newangle;
		this.rtArmUpperFire.xRot = newangle;
		this.rtArmLower.xRot = 0.48F + newangle;
		this.rtArmLowerFire.xRot = 0.48F + newangle;
		this.rtArmLower.z = (float)(Math.sin(this.rtArmUpperFire.xRot) * 8.0);
		this.rtArmLowerFire.z = (float)(Math.sin(this.rtArmUpperFire.xRot) * 8.0);
		this.lfArmUpper.xRot = -newangle;
		this.lfArmUpperFire.xRot = -newangle;
		this.lfArmLower.xRot = 0.48F - newangle;
		this.lfArmLowerFire.xRot = 0.48F - newangle;
		this.lfArmLower.z = (float)(Math.sin(this.lfArmUpperFire.xRot) * 8.0);
		this.lfArmLowerFire.z = (float)(Math.sin(this.lfArmUpperFire.xRot) * 8.0);
		float pi4 = 0.7853975F;
		this.head.yRot = (float)Math.toRadians(f3) + pi4 * 4.0F;
		this.headFire.yRot = (float)Math.toRadians(f3);
		float fc = (float)Math.cos(this.headFire.yRot + pi4);
		float fs = (float)Math.sin(this.headFire.yRot + pi4);
		this.lfHorn1.z = this.headFire.z - fc * 3.6F;
		this.lfHorn1.x = this.headFire.x - fs * 3.6F;
		this.lfHorn1.yRot = this.headFire.yRot + 0.244F + Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.1F;
		this.lfHorn2.z = this.lfHorn1.z - (float)Math.cos(this.lfHorn1.yRot) * 2.0F;
		this.lfHorn2.x = this.lfHorn1.x - (float)Math.sin(this.lfHorn1.yRot) * 2.0F;
		this.lfHorn2.yRot = this.headFire.yRot + 0.244F + Mth.cos(f2 * 1.3F * this.wingspeed - pi4) * (float) Math.PI * 0.1F;
		this.lfHorn3.z = this.lfHorn2.z - (float)Math.cos(this.lfHorn2.yRot) * 4.0F;
		this.lfHorn3.x = this.lfHorn2.x - (float)Math.sin(this.lfHorn2.yRot) * 4.0F;
		this.lfHorn3.yRot = this.headFire.yRot
		+ 0.244F
		+ Mth.cos(f2 * 1.3F * this.wingspeed - 2.0F * pi4) * (float) Math.PI * 0.1F;
		this.lfHorn4.z = this.lfHorn3.z - (float)Math.cos(this.lfHorn3.yRot) * 3.0F;
		this.lfHorn4.x = this.lfHorn3.x - (float)Math.sin(this.lfHorn3.yRot) * 3.0F;
		this.lfHorn4.yRot = this.headFire.yRot
		+ 0.244F
		+ Mth.cos(f2 * 1.3F * this.wingspeed - 3.0F * pi4) * (float) Math.PI * 0.1F;
		this.lfHorn5.z = this.lfHorn4.z - (float)Math.cos(this.lfHorn4.yRot) * 2.0F;
		this.lfHorn5.x = this.lfHorn4.x - (float)Math.sin(this.lfHorn4.yRot) * 2.0F;
		this.lfHorn5.yRot = this.headFire.yRot
		+ 0.244F
		+ Mth.cos(f2 * 1.3F * this.wingspeed - 4.0F * pi4) * (float) Math.PI * 0.1F;
		fc = (float)Math.cos(this.headFire.yRot - pi4);
		fs = (float)Math.sin(this.headFire.yRot - pi4);
		this.rtHorn1.z = this.headFire.z - fc * 3.6F;
		this.rtHorn1.x = this.headFire.x - fs * 3.6F;
		this.rtHorn1.yRot = this.headFire.yRot + -0.244F - Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.1F;
		this.rtHorn2.z = this.rtHorn1.z - (float)Math.cos(this.rtHorn1.yRot) * 2.0F;
		this.rtHorn2.x = this.rtHorn1.x - (float)Math.sin(this.rtHorn1.yRot) * 2.0F;
		this.rtHorn2.yRot = this.headFire.yRot + -0.244F - Mth.cos(f2 * 1.3F * this.wingspeed - pi4) * (float) Math.PI * 0.1F;
		this.rtHorn3.z = this.rtHorn2.z - (float)Math.cos(this.rtHorn2.yRot) * 4.0F;
		this.rtHorn3.x = this.rtHorn2.x - (float)Math.sin(this.rtHorn2.yRot) * 4.0F;
		this.rtHorn3.yRot = this.headFire.yRot
		+ -0.244F
		- Mth.cos(f2 * 1.3F * this.wingspeed - 2.0F * pi4) * (float) Math.PI * 0.1F;
		this.rtHorn4.z = this.rtHorn3.z - (float)Math.cos(this.rtHorn3.yRot) * 3.0F;
		this.rtHorn4.x = this.rtHorn3.x - (float)Math.sin(this.rtHorn3.yRot) * 3.0F;
		this.rtHorn4.yRot = this.headFire.yRot
		+ -0.244F
		- Mth.cos(f2 * 1.3F * this.wingspeed - 3.0F * pi4) * (float) Math.PI * 0.1F;
		this.rtHorn5.z = this.rtHorn4.z - (float)Math.cos(this.rtHorn4.yRot) * 2.0F;
		this.rtHorn5.x = this.rtHorn4.x - (float)Math.sin(this.rtHorn4.yRot) * 2.0F;
		this.rtHorn5.yRot = this.headFire.yRot
		+ -0.244F
		- Mth.cos(f2 * 1.3F * this.wingspeed - 4.0F * pi4) * (float) Math.PI * 0.1F;
		this.tail1.yRot = Mth.cos(f2 * 0.9F * this.wingspeed) * (float) Math.PI * 0.2F;
		this.tail2.x = this.tail1.x - (float)Math.sin(this.tail1.yRot) * 3.0F;
		this.tail2.yRot = Mth.cos(f2 * 0.9F * this.wingspeed - pi4) * (float) Math.PI * 0.2F;
		this.tail3.x = this.tail2.x - (float)Math.sin(this.tail2.yRot) * 4.0F;
		this.tail3.yRot = Mth.cos(f2 * 0.9F * this.wingspeed - 2.0F * pi4) * (float) Math.PI * 0.2F;
		this.tail4.x = this.tail3.x - (float)Math.sin(this.tail3.yRot) * 3.5F;
		this.tail4.yRot = Mth.cos(f2 * 0.9F * this.wingspeed - 3.0F * pi4) * (float) Math.PI * 0.2F;
		this.tail5.x = this.tail4.x - (float)Math.sin(this.tail4.yRot) * 5.0F;
		this.tail5.yRot = Mth.cos(f2 * 0.9F * this.wingspeed - 4.0F * pi4) * (float) Math.PI * 0.2F;
		this.tail6.x = this.tail5.x - (float)Math.sin(this.tail5.yRot) * 4.0F;
		this.tail6.yRot = Mth.cos(f2 * 0.9F * this.wingspeed - 5.0F * pi4) * (float) Math.PI * 0.2F;
		this.tail7.x = this.tail6.x - (float)Math.sin(this.tail6.yRot) * 3.0F;
		this.tail7.yRot = Mth.cos(f2 * 0.9F * this.wingspeed - 6.0F * pi4) * (float) Math.PI * 0.2F;
		this.tail8.x = this.tail7.x - (float)Math.sin(this.tail7.yRot) * 2.0F;
		this.tail8.yRot = Mth.cos(f2 * 0.9F * this.wingspeed - 7.0F * pi4) * (float) Math.PI * 0.2F;
		this.tail9.x = this.tail8.x - (float)Math.sin(this.tail8.yRot) * 1.0F;
		this.tail9.yRot = Mth.cos(f2 * 0.9F * this.wingspeed - 8.0F * pi4) * (float) Math.PI * 0.2F;
		this.tail1.xRot = -0.26F + Mth.cos(f2 * 0.5F * this.wingspeed) * (float) Math.PI * 0.1F;
		this.tail2.y = this.tail1.y + (float)Math.sin(this.tail1.xRot) * 3.0F;
		this.tail2.z = this.tail1.z - (float)Math.cos(this.tail1.xRot) * 3.0F;
		this.tail2.xRot = -0.78F + Mth.cos(f2 * 0.5F * this.wingspeed - pi4) * (float) Math.PI * 0.1F;
		this.tail3.y = this.tail2.y + (float)Math.sin(this.tail2.xRot) * 4.0F;
		this.tail3.z = this.tail2.z - (float)Math.cos(this.tail2.xRot) * 4.0F;
		this.tail3.xRot = -1.11F + Mth.cos(f2 * 0.5F * this.wingspeed - 2.0F * pi4) * (float) Math.PI * 0.1F;
		this.tail4.y = this.tail3.y + (float)Math.sin(this.tail3.xRot) * 3.5F;
		this.tail4.z = this.tail3.z - (float)Math.cos(this.tail3.xRot) * 3.5F;
		this.tail4.xRot = -0.18F + Mth.cos(f2 * 0.5F * this.wingspeed - 3.0F * pi4) * (float) Math.PI * 0.1F;
		this.tail5.y = this.tail4.y + (float)Math.sin(this.tail4.xRot) * 5.0F;
		this.tail5.z = this.tail4.z - (float)Math.cos(this.tail4.xRot) * 5.0F;
		this.tail5.xRot = 0.22F + Mth.cos(f2 * 0.5F * this.wingspeed - 4.0F * pi4) * (float) Math.PI * 0.1F;
		this.tail6.y = this.tail5.y + (float)Math.sin(this.tail5.xRot) * 4.0F;
		this.tail6.z = this.tail5.z - (float)Math.cos(this.tail5.xRot) * 4.0F;
		this.tail6.xRot = 0.63F + Mth.cos(f2 * 0.5F * this.wingspeed - 5.0F * pi4) * (float) Math.PI * 0.1F;
		this.tail7.y = this.tail6.y + (float)Math.sin(this.tail6.xRot) * 3.0F;
		this.tail7.z = this.tail6.z - (float)Math.cos(this.tail6.xRot) * 3.0F;
		this.tail7.xRot = 0.89F + Mth.cos(f2 * 0.5F * this.wingspeed - 6.0F * pi4) * (float) Math.PI * 0.1F;
		this.tail8.y = this.tail7.y + (float)Math.sin(this.tail7.xRot) * 2.0F;
		this.tail8.z = this.tail7.z - (float)Math.cos(this.tail7.xRot) * 2.0F;
		this.tail8.xRot = 1.52F + Mth.cos(f2 * 0.5F * this.wingspeed - 7.0F * pi4) * (float) Math.PI * 0.1F;
		this.tail9.y = this.tail8.y + (float)Math.sin(this.tail8.xRot) * 2.0F;
		this.tail9.z = this.tail8.z - (float)Math.cos(this.tail8.xRot) * 2.0F;
		this.tail9.xRot = 2.0F + Mth.cos(f2 * 0.5F * this.wingspeed - 8.0F * pi4) * (float) Math.PI * 0.1F;
		// OreSpawn turned the whole model round (glRotatef(180, 0, 1, 0)) before drawing it.
		this.root().yRot = (float) Math.PI;
	}
}
