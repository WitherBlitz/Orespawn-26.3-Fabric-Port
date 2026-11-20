package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/**
 * Ported from OreSpawn's ModelKraken (geometry converted by tools/convert_model.py; dangle_tentacle by hand): the
 * whole body turned to hang upright, fins waving, eight tentacles dangling in waves (the two long ones stiffer and
 * quicker while attacking), and the mouth and teeth now and then working (1 in 5 swings idle, 1 in 2 attacking).
 * {@code state.attacking}: Kraken.getAttacking(); {@code state.variant}: the entity's id, which picks those swings as
 * OreSpawn's per-entity RenderInfo did.
 */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class KrakenModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Lefteye;
	private final ModelPart Backbody;
	private final ModelPart Centerbody;
	private final ModelPart Head;
	private final ModelPart Sucktioncupleft;
	private final ModelPart Finright;
	private final ModelPart Tailbase1;
	private final ModelPart Tail2;
	private final ModelPart Tailtip;
	private final ModelPart Finleft;
	private final ModelPart Frontbody;
	private final ModelPart Mouth1;
	private final ModelPart Tent54;
	private final ModelPart Tent62;
	private final ModelPart Tent63;
	private final ModelPart Tent64;
	private final ModelPart Tent58;
	private final ModelPart Tent66;
	private final ModelPart Tent67;
	private final ModelPart Tent68;
	private final ModelPart Tent28;
	private final ModelPart Tent51;
	private final ModelPart Tent52;
	private final ModelPart Tent53;
	private final ModelPart Tent65;
	private final ModelPart Tent55;
	private final ModelPart Tent56;
	private final ModelPart Tent57;
	private final ModelPart Sucktioncupright;
	private final ModelPart Righteye;
	private final ModelPart Mouth2;
	private final ModelPart Mouth3;
	private final ModelPart Mouth4;
	private final ModelPart Mouth5;
	private final ModelPart Mouth6;
	private final ModelPart Mouth7;
	private final ModelPart Mouth8;
	private final ModelPart Tent61;
	private final ModelPart Tent38;
	private final ModelPart Tent22;
	private final ModelPart Tent23;
	private final ModelPart Tent24;
	private final ModelPart Tent25;
	private final ModelPart Tent26;
	private final ModelPart Tent27;
	private final ModelPart Tooth1;
	private final ModelPart Tent48;
	private final ModelPart Tent32;
	private final ModelPart Tent33;
	private final ModelPart Tent34;
	private final ModelPart Tent35;
	private final ModelPart Tent36;
	private final ModelPart Tent37;
	private final ModelPart Jet;
	private final ModelPart Tent41;
	private final ModelPart Tent42;
	private final ModelPart Tent43;
	private final ModelPart Tent44;
	private final ModelPart Tent45;
	private final ModelPart Tent46;
	private final ModelPart Tent47;
	private final ModelPart Tent21;
	private final ModelPart Tent11;
	private final ModelPart Tent12;
	private final ModelPart Tent13;
	private final ModelPart Tent14;
	private final ModelPart Tent15;
	private final ModelPart Tent16;
	private final ModelPart Tent31;
	private final ModelPart Tent18;
	private final ModelPart Tooth2;
	private final ModelPart Tooth3;
	private final ModelPart Tooth4;
	private final ModelPart Tooth5;
	private final ModelPart Tooth6;
	private final ModelPart Tooth7;
	private final ModelPart Tooth8;
	private final ModelPart Tooth9;
	private final ModelPart Tooth10;
	private final ModelPart Tooth11;
	private final ModelPart Tooth12;
	private final ModelPart Tooth13;
	private final ModelPart Tooth14;
	private final ModelPart Tooth15;
	private final ModelPart Tooth16;
	private final ModelPart Tooth17;
	private final ModelPart Tooth18;
	private final ModelPart Tooth19;
	private final ModelPart Tooth20;
	private final ModelPart Tooth21;
	private final ModelPart Tooth22;
	private final ModelPart Tooth23;
	private final ModelPart Tooth24;
	private final ModelPart Tooth25;
	private final ModelPart Tooth26;
	private final ModelPart Tooth27;
	private final ModelPart Tooth28;
	private final ModelPart Tooth29;
	private final ModelPart Tooth30;
	private final ModelPart Tooth31;
	private final ModelPart Tooth32;
	private final ModelPart Tooth33;
	private final ModelPart Tooth34;
	private final ModelPart Tooth35;
	private final ModelPart Tooth36;
	private final ModelPart Tooth37;
	private final ModelPart Tooth38;
	private final ModelPart Tooth39;
	private final ModelPart Tooth40;
	private final ModelPart Tooth41;
	private final ModelPart Tent17;
	private final float wingspeed;

	public KrakenModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Lefteye = root.getChild("lefteye");
		this.Backbody = root.getChild("backbody");
		this.Centerbody = root.getChild("centerbody");
		this.Head = root.getChild("head");
		this.Sucktioncupleft = root.getChild("sucktioncupleft");
		this.Finright = root.getChild("finright");
		this.Tailbase1 = root.getChild("tailbase1");
		this.Tail2 = root.getChild("tail2");
		this.Tailtip = root.getChild("tailtip");
		this.Finleft = root.getChild("finleft");
		this.Frontbody = root.getChild("frontbody");
		this.Mouth1 = root.getChild("mouth1");
		this.Tent54 = root.getChild("tent54");
		this.Tent62 = root.getChild("tent62");
		this.Tent63 = root.getChild("tent63");
		this.Tent64 = root.getChild("tent64");
		this.Tent58 = root.getChild("tent58");
		this.Tent66 = root.getChild("tent66");
		this.Tent67 = root.getChild("tent67");
		this.Tent68 = root.getChild("tent68");
		this.Tent28 = root.getChild("tent28");
		this.Tent51 = root.getChild("tent51");
		this.Tent52 = root.getChild("tent52");
		this.Tent53 = root.getChild("tent53");
		this.Tent65 = root.getChild("tent65");
		this.Tent55 = root.getChild("tent55");
		this.Tent56 = root.getChild("tent56");
		this.Tent57 = root.getChild("tent57");
		this.Sucktioncupright = root.getChild("sucktioncupright");
		this.Righteye = root.getChild("righteye");
		this.Mouth2 = root.getChild("mouth2");
		this.Mouth3 = root.getChild("mouth3");
		this.Mouth4 = root.getChild("mouth4");
		this.Mouth5 = root.getChild("mouth5");
		this.Mouth6 = root.getChild("mouth6");
		this.Mouth7 = root.getChild("mouth7");
		this.Mouth8 = root.getChild("mouth8");
		this.Tent61 = root.getChild("tent61");
		this.Tent38 = root.getChild("tent38");
		this.Tent22 = root.getChild("tent22");
		this.Tent23 = root.getChild("tent23");
		this.Tent24 = root.getChild("tent24");
		this.Tent25 = root.getChild("tent25");
		this.Tent26 = root.getChild("tent26");
		this.Tent27 = root.getChild("tent27");
		this.Tooth1 = root.getChild("tooth1");
		this.Tent48 = root.getChild("tent48");
		this.Tent32 = root.getChild("tent32");
		this.Tent33 = root.getChild("tent33");
		this.Tent34 = root.getChild("tent34");
		this.Tent35 = root.getChild("tent35");
		this.Tent36 = root.getChild("tent36");
		this.Tent37 = root.getChild("tent37");
		this.Jet = root.getChild("jet");
		this.Tent41 = root.getChild("tent41");
		this.Tent42 = root.getChild("tent42");
		this.Tent43 = root.getChild("tent43");
		this.Tent44 = root.getChild("tent44");
		this.Tent45 = root.getChild("tent45");
		this.Tent46 = root.getChild("tent46");
		this.Tent47 = root.getChild("tent47");
		this.Tent21 = root.getChild("tent21");
		this.Tent11 = root.getChild("tent11");
		this.Tent12 = root.getChild("tent12");
		this.Tent13 = root.getChild("tent13");
		this.Tent14 = root.getChild("tent14");
		this.Tent15 = root.getChild("tent15");
		this.Tent16 = root.getChild("tent16");
		this.Tent31 = root.getChild("tent31");
		this.Tent18 = root.getChild("tent18");
		this.Tooth2 = root.getChild("tooth2");
		this.Tooth3 = root.getChild("tooth3");
		this.Tooth4 = root.getChild("tooth4");
		this.Tooth5 = root.getChild("tooth5");
		this.Tooth6 = root.getChild("tooth6");
		this.Tooth7 = root.getChild("tooth7");
		this.Tooth8 = root.getChild("tooth8");
		this.Tooth9 = root.getChild("tooth9");
		this.Tooth10 = root.getChild("tooth10");
		this.Tooth11 = root.getChild("tooth11");
		this.Tooth12 = root.getChild("tooth12");
		this.Tooth13 = root.getChild("tooth13");
		this.Tooth14 = root.getChild("tooth14");
		this.Tooth15 = root.getChild("tooth15");
		this.Tooth16 = root.getChild("tooth16");
		this.Tooth17 = root.getChild("tooth17");
		this.Tooth18 = root.getChild("tooth18");
		this.Tooth19 = root.getChild("tooth19");
		this.Tooth20 = root.getChild("tooth20");
		this.Tooth21 = root.getChild("tooth21");
		this.Tooth22 = root.getChild("tooth22");
		this.Tooth23 = root.getChild("tooth23");
		this.Tooth24 = root.getChild("tooth24");
		this.Tooth25 = root.getChild("tooth25");
		this.Tooth26 = root.getChild("tooth26");
		this.Tooth27 = root.getChild("tooth27");
		this.Tooth28 = root.getChild("tooth28");
		this.Tooth29 = root.getChild("tooth29");
		this.Tooth30 = root.getChild("tooth30");
		this.Tooth31 = root.getChild("tooth31");
		this.Tooth32 = root.getChild("tooth32");
		this.Tooth33 = root.getChild("tooth33");
		this.Tooth34 = root.getChild("tooth34");
		this.Tooth35 = root.getChild("tooth35");
		this.Tooth36 = root.getChild("tooth36");
		this.Tooth37 = root.getChild("tooth37");
		this.Tooth38 = root.getChild("tooth38");
		this.Tooth39 = root.getChild("tooth39");
		this.Tooth40 = root.getChild("tooth40");
		this.Tooth41 = root.getChild("tooth41");
		this.Tent17 = root.getChild("tent17");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelKraken");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		int att = state.attacking ? 1 : 0;
		float newangle = 0.0F;
		float nextangle = 0.0F;
		this.Finright.zRot = Mth.cos(f2 * 0.43F * this.wingspeed) * (float) Math.PI * 0.15F;
		this.Finleft.zRot = Mth.cos(f2 * 0.32F * this.wingspeed) * (float) Math.PI * 0.14F;
		float pi4 = 0.39269876F;
		this.dangle_tentacle(f2, 5, att, this.Tent51, this.Tent52, this.Tent53, this.Tent54, this.Tent55, this.Tent56, this.Tent57, this.Tent58);
		this.dangle_tentacle(f2, 6, att, this.Tent61, this.Tent62, this.Tent63, this.Tent64, this.Tent65, this.Tent66, this.Tent67, this.Tent68);
		this.Sucktioncupleft.y = this.Tent58.y
		+ (float)Math.sin(this.Tent58.xRot) * 30.0F * (float)Math.cos(this.Tent58.yRot);
		this.Sucktioncupleft.z = this.Tent58.z
		- (float)Math.cos(this.Tent58.xRot) * 30.0F * (float)Math.cos(this.Tent58.yRot);
		this.Sucktioncupleft.x = this.Tent58.x
		- (float)Math.sin(this.Tent58.yRot) * 30.0F * (float)Math.cos(this.Tent58.xRot);
		this.Sucktioncupleft.xRot = this.Tent58.xRot;
		this.Sucktioncupleft.yRot = this.Tent58.yRot;
		this.Sucktioncupright.y = this.Tent68.y
		+ (float)Math.sin(this.Tent68.xRot) * 30.0F * (float)Math.cos(this.Tent68.yRot);
		this.Sucktioncupright.z = this.Tent68.z
		- (float)Math.cos(this.Tent68.xRot) * 30.0F * (float)Math.cos(this.Tent68.yRot);
		this.Sucktioncupright.x = this.Tent68.x
		- (float)Math.sin(this.Tent68.yRot) * 30.0F * (float)Math.cos(this.Tent68.xRot);
		this.Sucktioncupright.xRot = this.Tent68.xRot;
		this.Sucktioncupright.yRot = this.Tent68.yRot;
		// OreSpawn's RenderInfo re-rolled ri1 each time this swing came round (nextInt(10) idle, nextInt(4) attacking),
		// and the mouth worked while it was 1 or 3; here the roll comes from the swing count and the entity
		// (state.variant: the entity id).
		int swing = Mth.floor((f2 * 0.66F - (float) (Math.PI * 1.5)) / Mth.TWO_PI);
		int ri1 = roll(state.variant, swing, att != 0 ? 4 : 10);
		if (ri1 != 1 && ri1 != 3) {
		newangle = 0.0F;
		} else {
		newangle = Mth.cos(f2 * 0.5F * this.wingspeed) * (float) Math.PI * 0.015F;
		}
		this.Mouth1.xRot = -0.38F + newangle;
		this.Mouth1.yRot = -0.38F + newangle;
		this.Mouth2.xRot = -0.38F + newangle;
		this.Mouth2.yRot = 0.38F - newangle;
		this.Mouth3.xRot = 0.38F - newangle;
		this.Mouth3.yRot = 0.38F - newangle;
		this.Mouth5.xRot = 0.38F - newangle;
		this.Mouth5.yRot = -0.38F + newangle;
		this.Mouth4.xRot = 0.38F - newangle;
		this.Mouth6.yRot = -0.38F + newangle;
		this.Mouth7.yRot = 0.38F - newangle;
		this.Mouth8.xRot = -0.38F + newangle;
		newangle *= 7.0F;
		this.Tooth2.xRot = -0.35F - newangle;
		this.Tooth3.xRot = -0.34F - newangle;
		this.Tooth4.xRot = -0.33F - newangle;
		this.Tooth5.xRot = -0.36F - newangle;
		this.Tooth6.xRot = -0.32F - newangle;
		this.Tooth11.yRot = 0.35F + newangle;
		this.Tooth12.yRot = 0.37F + newangle;
		this.Tooth13.yRot = 0.33F + newangle;
		this.Tooth14.yRot = 0.34F + newangle;
		this.Tooth15.yRot = 0.36F + newangle;
		this.Tooth16.yRot = 0.35F + newangle;
		this.Tooth17.yRot = 0.32F + newangle;
		this.Tooth22.xRot = 0.31F + newangle;
		this.Tooth23.xRot = 0.37F + newangle;
		this.Tooth24.xRot = 0.33F + newangle;
		this.Tooth25.xRot = 0.34F + newangle;
		this.Tooth26.xRot = 0.36F + newangle;
		this.Tooth27.xRot = 0.35F + newangle;
		this.Tooth31.yRot = -0.35F - newangle;
		this.Tooth32.yRot = -0.37F - newangle;
		this.Tooth33.yRot = -0.33F - newangle;
		this.Tooth34.yRot = -0.34F - newangle;
		this.Tooth35.yRot = -0.36F - newangle;
		this.Tooth36.yRot = -0.35F - newangle;
		this.Tooth37.yRot = -0.32F - newangle;
		this.Tooth7.xRot = -0.35F - newangle;
		this.Tooth7.yRot = 0.33F + newangle;
		this.Tooth8.xRot = -0.31F - newangle;
		this.Tooth8.yRot = 0.37F + newangle;
		this.Tooth9.xRot = -0.32F - newangle;
		this.Tooth9.yRot = 0.3F + newangle;
		this.Tooth10.xRot = -0.33F - newangle;
		this.Tooth10.yRot = 0.33F + newangle;
		this.Tooth18.xRot = 0.35F + newangle;
		this.Tooth18.yRot = 0.33F + newangle;
		this.Tooth19.xRot = 0.31F + newangle;
		this.Tooth19.yRot = 0.37F + newangle;
		this.Tooth20.xRot = 0.37F + newangle;
		this.Tooth20.yRot = 0.37F + newangle;
		this.Tooth21.xRot = 0.3F + newangle;
		this.Tooth21.yRot = 0.3F + newangle;
		this.Tooth28.xRot = 0.37F + newangle;
		this.Tooth28.yRot = -0.3F - newangle;
		this.Tooth29.xRot = 0.33F + newangle;
		this.Tooth29.yRot = -0.32F - newangle;
		this.Tooth30.xRot = 0.3F + newangle;
		this.Tooth30.yRot = -0.37F - newangle;
		this.Tooth31.xRot = 0.37F + newangle;
		this.Tooth31.yRot = -0.3F - newangle;
		this.Tooth38.xRot = -0.34F - newangle;
		this.Tooth38.yRot = -0.33F - newangle;
		this.Tooth39.xRot = -0.35F - newangle;
		this.Tooth39.yRot = -0.37F - newangle;
		this.Tooth40.xRot = -0.39F - newangle;
		this.Tooth40.yRot = -0.33F - newangle;
		this.Tooth41.xRot = -0.34F - newangle;
		this.Tooth41.yRot = -0.36F - newangle;
		this.Tooth1.xRot = -0.35F - newangle;
		this.Tooth1.yRot = -0.32F - newangle;
		this.dangle_tentacle(f2, 1, 0, this.Tent11, this.Tent12, this.Tent13, this.Tent14, this.Tent15, this.Tent16, this.Tent17, this.Tent18);
		this.dangle_tentacle(f2, 2, 0, this.Tent21, this.Tent22, this.Tent23, this.Tent24, this.Tent25, this.Tent26, this.Tent27, this.Tent28);
		this.dangle_tentacle(f2, 3, 0, this.Tent31, this.Tent32, this.Tent33, this.Tent34, this.Tent35, this.Tent36, this.Tent37, this.Tent38);
		this.dangle_tentacle(f2, 4, 0, this.Tent41, this.Tent42, this.Tent43, this.Tent44, this.Tent45, this.Tent46, this.Tent47, this.Tent48);
		// render() drew the whole model turned 90 degrees about x (the body hangs head up, tentacles down).
		this.root().xRot = (float) (Math.PI / 2);
	}

	/** OreSpawn's nextInt(odds), for one entity and swing. */
	private static int roll(int id, int swing, int odds) {
		int h = id * 0x9E3779B1 + swing * 0x85EBCA6B;
		h ^= h >>> 15;
		h *= 0x2C1B3C6D;
		h ^= h >>> 12;
		return Math.floorMod(h, odds);
	}

	private void dangle_tentacle(
		float f2,
		int dir,
		int att,
		ModelPart p1,
		ModelPart p2,
		ModelPart p3,
		ModelPart p4,
		ModelPart p5,
		ModelPart p6,
		ModelPart p7,
		ModelPart p8
	) {
		float pi4 = 0.314159F;
		int dist = 30;
		float differ = 0.1F;
		float xoff = 0.0F;
		float ydiffer = 0.1F;
		float yoff = 0.0F;
		float s = -1.0F;
		float amp = 0.1F;
		if (dir == 1) {
			differ = 0.101F;
		}

		if (dir == 2) {
			differ = 0.097F;
		}

		if (dir == 3) {
			differ = 0.093F;
		}

		if (dir == 4) {
			differ = 0.087F;
		}

		if (dir == 1) {
			ydiffer = 0.102F;
		}

		if (dir == 2) {
			ydiffer = 0.098F;
		}

		if (dir == 3) {
			ydiffer = 0.092F;
		}

		if (dir == 4) {
			ydiffer = 0.088F;
		}

		if (dir == 2) {
			xoff = 0.26F;
		}

		if (dir == 3) {
			xoff = 0.26F;
		}

		if (dir == 1) {
			yoff = 0.44F;
		}

		if (dir == 4) {
			yoff = -0.44F;
		}

		if (dir == 5) {
			differ = 0.2F;
		}

		if (dir == 6) {
			differ = 0.2F;
		}

		if (dir == 5) {
			xoff = -0.25F;
		}

		if (dir == 6) {
			xoff = -0.25F;
		}

		if (dir == 6) {
			s = 1.0F;
		}

		if (att != 0) {
			if (dir == 5) {
				differ = 0.5F;
				amp = 0.03F;
				xoff = 0.0F;
			}

			if (dir == 6) {
				differ = 0.5F;
				amp = 0.03F;
				xoff = 0.0F;
			}
		}

		p1.xRot = xoff + s * Mth.cos(f2 * differ * this.wingspeed) * (float) Math.PI * amp;
		p1.yRot = yoff - Mth.cos(f2 * ydiffer * this.wingspeed) * (float) Math.PI * amp;
		p2.y = p1.y + (float)Math.sin(p1.xRot) * dist * (float)Math.cos(p1.yRot);
		p2.z = p1.z - (float)Math.cos(p1.xRot) * dist * (float)Math.cos(p1.yRot);
		p2.x = p1.x - (float)Math.sin(p1.yRot) * dist * (float)Math.cos(p1.xRot);
		p2.xRot = xoff / 2.0F + s * Mth.cos(f2 * differ * this.wingspeed - pi4) * (float) Math.PI * amp;
		p2.yRot = yoff / 2.0F - Mth.cos(f2 * ydiffer * this.wingspeed - pi4) * (float) Math.PI * amp;
		p3.y = p2.y + (float)Math.sin(p2.xRot) * dist * (float)Math.cos(p2.yRot);
		p3.z = p2.z - (float)Math.cos(p2.xRot) * dist * (float)Math.cos(p2.yRot);
		p3.x = p2.x - (float)Math.sin(p2.yRot) * dist * (float)Math.cos(p2.xRot);
		p3.xRot = s * Mth.cos(f2 * differ * this.wingspeed - 2.0F * pi4) * (float) Math.PI * amp;
		p3.yRot = -Mth.cos(f2 * ydiffer * this.wingspeed - 2.0F * pi4) * (float) Math.PI * amp;
		p4.y = p3.y + (float)Math.sin(p3.xRot) * dist * (float)Math.cos(p3.yRot);
		p4.z = p3.z - (float)Math.cos(p3.xRot) * dist * (float)Math.cos(p3.yRot);
		p4.x = p3.x - (float)Math.sin(p3.yRot) * dist * (float)Math.cos(p3.xRot);
		p4.xRot = s * Mth.cos(f2 * differ * this.wingspeed - 3.0F * pi4) * (float) Math.PI * amp;
		p4.yRot = -Mth.cos(f2 * ydiffer * this.wingspeed - 3.0F * pi4) * (float) Math.PI * amp;
		p5.y = p4.y + (float)Math.sin(p4.xRot) * dist * (float)Math.cos(p4.yRot);
		p5.z = p4.z - (float)Math.cos(p4.xRot) * dist * (float)Math.cos(p4.yRot);
		p5.x = p4.x - (float)Math.sin(p4.yRot) * dist * (float)Math.cos(p4.xRot);
		p5.xRot = s * Mth.cos(f2 * differ * this.wingspeed - 4.0F * pi4) * (float) Math.PI * amp;
		p5.yRot = -Mth.cos(f2 * ydiffer * this.wingspeed - 4.0F * pi4) * (float) Math.PI * amp;
		p6.y = p5.y + (float)Math.sin(p5.xRot) * dist * (float)Math.cos(p5.yRot);
		p6.z = p5.z - (float)Math.cos(p5.xRot) * dist * (float)Math.cos(p5.yRot);
		p6.x = p5.x - (float)Math.sin(p5.yRot) * dist * (float)Math.cos(p5.xRot);
		p6.xRot = s * Mth.cos(f2 * differ * this.wingspeed - 5.0F * pi4) * (float) Math.PI * amp;
		p6.yRot = -Mth.cos(f2 * ydiffer * this.wingspeed - 5.0F * pi4) * (float) Math.PI * amp;
		p7.y = p6.y + (float)Math.sin(p6.xRot) * dist * (float)Math.cos(p6.yRot);
		p7.z = p6.z - (float)Math.cos(p6.xRot) * dist * (float)Math.cos(p6.yRot);
		p7.x = p6.x - (float)Math.sin(p6.yRot) * dist * (float)Math.cos(p6.xRot);
		p7.xRot = s * Mth.cos(f2 * differ * this.wingspeed - 6.0F * pi4) * (float) Math.PI * amp;
		p7.yRot = -Mth.cos(f2 * ydiffer * this.wingspeed - 6.0F * pi4) * (float) Math.PI * amp;
		p8.y = p7.y + (float)Math.sin(p7.xRot) * dist * (float)Math.cos(p7.yRot);
		p8.z = p7.z - (float)Math.cos(p7.xRot) * dist * (float)Math.cos(p7.yRot);
		p8.x = p7.x - (float)Math.sin(p7.yRot) * dist * (float)Math.cos(p7.xRot);
		p8.xRot = s * Mth.cos(f2 * differ * this.wingspeed - 7.0F * pi4) * (float) Math.PI * amp;
		p8.yRot = -Mth.cos(f2 * ydiffer * this.wingspeed - 7.0F * pi4) * (float) Math.PI * amp;
	}
}
