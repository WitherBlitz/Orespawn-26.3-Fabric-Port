package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelEnderReaper (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class EnderReaperModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart rwing1;
	private final ModelPart lwing1;
	private final ModelPart Shape3;
	private final ModelPart Shape4;
	private final ModelPart Shape5;
	private final ModelPart Shape6;
	private final ModelPart Shape7;
	private final ModelPart Shape8;
	private final ModelPart Shape9;
	private final ModelPart Shape10;
	private final ModelPart Shape11;
	private final ModelPart Shape12;
	private final ModelPart Shape13;
	private final ModelPart Shape14;
	private final ModelPart Shape15;
	private final ModelPart Shape16;
	private final ModelPart Shape17;
	private final ModelPart Shape18;
	private final ModelPart Shape19;
	private final ModelPart Shape20;
	private final ModelPart Shape21;
	private final ModelPart Shape22;
	private final ModelPart Shape23;
	private final ModelPart Shape24;
	private final ModelPart Shape25;
	private final ModelPart Shape26;
	private final ModelPart Shape27;
	private final ModelPart Shape28;
	private final ModelPart Shape29;
	private final ModelPart Shape30;
	private final ModelPart Shape31;
	private final ModelPart Shape32;
	private final ModelPart Shape33;
	private final ModelPart Shape34;
	private final ModelPart Shape35;
	private final ModelPart Shape36;
	private final ModelPart Shape37;
	private final ModelPart Shape38;
	private final ModelPart Shape39;
	private final ModelPart Shape40;
	private final ModelPart Shape41;
	private final ModelPart Shape42;
	private final ModelPart Shape43;
	private final ModelPart Shape44;
	private final ModelPart Shape45;
	private final ModelPart Shape46;
	private final ModelPart Shape47;
	private final ModelPart Shape48;
	private final ModelPart Shape49;
	private final ModelPart rarm2;
	private final ModelPart rarm3;
	private final ModelPart relbow;
	private final ModelPart rarm1;
	private final ModelPart Shape54;
	private final ModelPart larm3;
	private final ModelPart larm2;
	private final ModelPart lelbow;
	private final ModelPart larm1;
	private final ModelPart scythe1;
	private final ModelPart scythe2;
	private final ModelPart scythe3;
	private final ModelPart head;
	private final ModelPart lwing3;
	private final ModelPart lwing2;
	private final ModelPart rwing3;
	private final ModelPart rwing2;
	private final float wingspeed;

	public EnderReaperModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.rwing1 = root.getChild("rwing1");
		this.lwing1 = root.getChild("lwing1");
		this.Shape3 = root.getChild("shape3");
		this.Shape4 = root.getChild("shape4");
		this.Shape5 = root.getChild("shape5");
		this.Shape6 = root.getChild("shape6");
		this.Shape7 = root.getChild("shape7");
		this.Shape8 = root.getChild("shape8");
		this.Shape9 = root.getChild("shape9");
		this.Shape10 = root.getChild("shape10");
		this.Shape11 = root.getChild("shape11");
		this.Shape12 = root.getChild("shape12");
		this.Shape13 = root.getChild("shape13");
		this.Shape14 = root.getChild("shape14");
		this.Shape15 = root.getChild("shape15");
		this.Shape16 = root.getChild("shape16");
		this.Shape17 = root.getChild("shape17");
		this.Shape18 = root.getChild("shape18");
		this.Shape19 = root.getChild("shape19");
		this.Shape20 = root.getChild("shape20");
		this.Shape21 = root.getChild("shape21");
		this.Shape22 = root.getChild("shape22");
		this.Shape23 = root.getChild("shape23");
		this.Shape24 = root.getChild("shape24");
		this.Shape25 = root.getChild("shape25");
		this.Shape26 = root.getChild("shape26");
		this.Shape27 = root.getChild("shape27");
		this.Shape28 = root.getChild("shape28");
		this.Shape29 = root.getChild("shape29");
		this.Shape30 = root.getChild("shape30");
		this.Shape31 = root.getChild("shape31");
		this.Shape32 = root.getChild("shape32");
		this.Shape33 = root.getChild("shape33");
		this.Shape34 = root.getChild("shape34");
		this.Shape35 = root.getChild("shape35");
		this.Shape36 = root.getChild("shape36");
		this.Shape37 = root.getChild("shape37");
		this.Shape38 = root.getChild("shape38");
		this.Shape39 = root.getChild("shape39");
		this.Shape40 = root.getChild("shape40");
		this.Shape41 = root.getChild("shape41");
		this.Shape42 = root.getChild("shape42");
		this.Shape43 = root.getChild("shape43");
		this.Shape44 = root.getChild("shape44");
		this.Shape45 = root.getChild("shape45");
		this.Shape46 = root.getChild("shape46");
		this.Shape47 = root.getChild("shape47");
		this.Shape48 = root.getChild("shape48");
		this.Shape49 = root.getChild("shape49");
		this.rarm2 = root.getChild("rarm2");
		this.rarm3 = root.getChild("rarm3");
		this.relbow = root.getChild("relbow");
		this.rarm1 = root.getChild("rarm1");
		this.Shape54 = root.getChild("shape54");
		this.larm3 = root.getChild("larm3");
		this.larm2 = root.getChild("larm2");
		this.lelbow = root.getChild("lelbow");
		this.larm1 = root.getChild("larm1");
		this.scythe1 = root.getChild("scythe1");
		this.scythe2 = root.getChild("scythe2");
		this.scythe3 = root.getChild("scythe3");
		this.head = root.getChild("head");
		this.lwing3 = root.getChild("lwing3");
		this.lwing2 = root.getChild("lwing2");
		this.rwing3 = root.getChild("rwing3");
		this.rwing2 = root.getChild("rwing2");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelEnderReaper");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		// state.attacking: the mob is screaming (EnderKnight.isScreaming).
		float newangle = 0.0F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.25F * f1;
		} else {
		newangle = 0.0F;
		}
		this.scythe3.zRot = this.scythe2.zRot = this.scythe1.zRot = 1.0F - Math.abs(newangle);
		if (state.attacking) {
		newangle = Mth.cos(f2 * 1.9F * this.wingspeed) * (float) Math.PI * 0.25F;
		this.scythe3.zRot = this.scythe2.zRot = this.scythe1.zRot = 1.0F + newangle;
		this.larm1.xRot = -0.436F;
		this.larm1.yRot = -0.488F;
		newangle = Mth.cos(f2 * 2.7F * this.wingspeed) * (float) Math.PI * 0.3F;
		} else {
		this.larm1.xRot = -2.436F;
		this.larm1.yRot = 1.0F;
		newangle = Mth.cos(f2 * 0.7F * this.wingspeed) * (float) Math.PI * 0.06F;
		}
		this.lwing1.yRot = this.lwing2.yRot = this.lwing3.yRot = 0.785F + newangle;
		this.rwing1.yRot = this.rwing2.yRot = this.rwing3.yRot = -0.785F - newangle;
		this.head.yRot = (float)Math.toRadians(f3) * 0.45F;
		if (this.head.yRot > 0.45F) {
		this.head.yRot = 0.45F;
		}
		if (this.head.yRot < -0.45F) {
		this.head.yRot = -0.45F;
		}
	}
}
