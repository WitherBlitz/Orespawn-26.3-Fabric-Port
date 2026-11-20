package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelCephadrome (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CephadromeModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart leftfoot;
	private final ModelPart butt;
	private final ModelPart rightfoot;
	private final ModelPart topfin1;
	private final ModelPart topfin2;
	private final ModelPart topfin3;
	private final ModelPart topfin4;
	private final ModelPart leftshoulder;
	private final ModelPart lefwingfin1;
	private final ModelPart tailfin1;
	private final ModelPart tailmembrane2;
	private final ModelPart tailfin2;
	private final ModelPart tailfin4;
	private final ModelPart tailfin3;
	private final ModelPart tailmembrane1;
	private final ModelPart topmem1;
	private final ModelPart topmem2;
	private final ModelPart topmem3;
	private final ModelPart topmem4;
	private final ModelPart neck1;
	private final ModelPart body;
	private final ModelPart chest1;
	private final ModelPart leftleg1;
	private final ModelPart mouth;
	private final ModelPart neck2;
	private final ModelPart head;
	private final ModelPart hammerhead;
	private final ModelPart chest;
	private final ModelPart neck3;
	private final ModelPart tail1;
	private final ModelPart rightleg1;
	private final ModelPart leftleg2;
	private final ModelPart rightleg2;
	private final ModelPart body2;
	private final ModelPart leftleg3;
	private final ModelPart rightleg3;
	private final ModelPart tail2;
	private final ModelPart tail3;
	private final ModelPart tailmembrane3;
	private final ModelPart leftwingfin2;
	private final ModelPart leftwingfin3;
	private final ModelPart leftwingfin4;
	private final ModelPart leftwingmembrane;
	private final ModelPart rightshoulder;
	private final ModelPart rightwingfin1;
	private final ModelPart rightwingfin2;
	private final ModelPart rightwingfin3;
	private final ModelPart rightwingfin4;
	private final ModelPart rightwingmembrane;
	private final ModelPart hammerhead2;
	private final float wingspeed;

	public CephadromeModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.leftfoot = root.getChild("leftfoot");
		this.butt = root.getChild("butt");
		this.rightfoot = root.getChild("rightfoot");
		this.topfin1 = root.getChild("topfin1");
		this.topfin2 = root.getChild("topfin2");
		this.topfin3 = root.getChild("topfin3");
		this.topfin4 = root.getChild("topfin4");
		this.leftshoulder = root.getChild("leftshoulder");
		this.lefwingfin1 = root.getChild("lefwingfin1");
		this.tailfin1 = root.getChild("tailfin1");
		this.tailmembrane2 = root.getChild("tailmembrane2");
		this.tailfin2 = root.getChild("tailfin2");
		this.tailfin4 = root.getChild("tailfin4");
		this.tailfin3 = root.getChild("tailfin3");
		this.tailmembrane1 = root.getChild("tailmembrane1");
		this.topmem1 = root.getChild("topmem1");
		this.topmem2 = root.getChild("topmem2");
		this.topmem3 = root.getChild("topmem3");
		this.topmem4 = root.getChild("topmem4");
		this.neck1 = root.getChild("neck1");
		this.body = root.getChild("body");
		this.chest1 = root.getChild("chest1");
		this.leftleg1 = root.getChild("leftleg1");
		this.mouth = root.getChild("mouth");
		this.neck2 = root.getChild("neck2");
		this.head = root.getChild("head");
		this.hammerhead = root.getChild("hammerhead");
		this.chest = root.getChild("chest");
		this.neck3 = root.getChild("neck3");
		this.tail1 = root.getChild("tail1");
		this.rightleg1 = root.getChild("rightleg1");
		this.leftleg2 = root.getChild("leftleg2");
		this.rightleg2 = root.getChild("rightleg2");
		this.body2 = root.getChild("body2");
		this.leftleg3 = root.getChild("leftleg3");
		this.rightleg3 = root.getChild("rightleg3");
		this.tail2 = root.getChild("tail2");
		this.tail3 = root.getChild("tail3");
		this.tailmembrane3 = root.getChild("tailmembrane3");
		this.leftwingfin2 = root.getChild("leftwingfin2");
		this.leftwingfin3 = root.getChild("leftwingfin3");
		this.leftwingfin4 = root.getChild("leftwingfin4");
		this.leftwingmembrane = root.getChild("leftwingmembrane");
		this.rightshoulder = root.getChild("rightshoulder");
		this.rightwingfin1 = root.getChild("rightwingfin1");
		this.rightwingfin2 = root.getChild("rightwingfin2");
		this.rightwingfin3 = root.getChild("rightwingfin3");
		this.rightwingfin4 = root.getChild("rightwingfin4");
		this.rightwingmembrane = root.getChild("rightwingmembrane");
		this.hammerhead2 = root.getChild("hammerhead2");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelCephadrome");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		// state.flag: flying with a rider (Cephadromstate.flag); state.value: how far it moved sideways last tick;
		// while flying, state.yRot is the smoothed turn its neck leans into (Cephadrome.neckTurn, OreSpawn's RenderInfo.rf1).
		float newangle = 0.0F;
		float lspeed = 0.0F;
		float pi4 = (float) (Math.PI / 4);
		float tailspeed = 0.76F;
		float tailamp = 0.1F;
		if (f1 > 0.001) {
		lspeed = state.value;
		newangle = Mth.cos(f2 * 0.75F * this.wingspeed) * (float) Math.PI * lspeed * 0.4F;
		if (newangle > 0.5) {
		newangle = 0.75F;
		}
		if (newangle < -0.5) {
		newangle = -0.75F;
		}
		} else {
		newangle = 0.0F;
		}
		if (state.flag) {
		newangle = 1.0F;
		this.rightleg1.xRot = -0.58F + newangle;
		this.rightleg2.xRot = 0.98F + newangle;
		this.rightleg3.xRot = -0.52F + newangle;
		this.rightfoot.xRot = newangle;
		this.leftleg1.xRot = -0.58F + newangle;
		this.leftleg2.xRot = 0.98F + newangle;
		this.leftleg3.xRot = -0.52F + newangle;
		this.leftfoot.xRot = newangle;
		} else {
		this.rightleg1.xRot = -0.58F + newangle;
		this.rightleg2.xRot = 0.98F + newangle;
		this.rightleg3.xRot = -0.52F + newangle;
		this.rightfoot.xRot = newangle;
		this.leftleg1.xRot = -0.58F - newangle;
		this.leftleg2.xRot = 0.98F - newangle;
		this.leftleg3.xRot = -0.52F - newangle;
		this.leftfoot.xRot = -newangle;
		}
		if (state.flag) {
		newangle = Mth.cos(f2 * 0.55F * this.wingspeed) * (float) Math.PI * 0.28F;
		} else if (!state.attacking) {
		newangle = -0.85F + Mth.cos(f2 * 0.2F * this.wingspeed) * (float) Math.PI * 0.028F;
		} else {
		newangle = -0.65F + Mth.cos(f2 * 0.9F * this.wingspeed) * (float) Math.PI * 0.068F;
		}
		this.lefwingfin1.zRot = newangle;
		this.leftwingfin2.zRot = newangle;
		this.leftwingfin3.zRot = newangle;
		this.leftwingfin4.zRot = newangle;
		this.leftwingmembrane.zRot = newangle;
		this.rightwingfin1.zRot = -newangle;
		this.rightwingfin2.zRot = -newangle;
		this.rightwingfin3.zRot = -newangle;
		this.rightwingfin4.zRot = -newangle;
		this.rightwingmembrane.zRot = -newangle;
		newangle = Mth.cos(f2 * 0.15F * this.wingspeed) * (float) Math.PI * 0.05F;
		this.topfin1.xRot = -1.85F - Math.abs(newangle);
		this.topmem1.xRot = -0.26F - Math.abs(newangle);
		this.topfin2.xRot = -2.07F - Math.abs(newangle / 2.0F);
		this.topmem2.xRot = -0.52F - Math.abs(newangle / 2.0F);
		this.topfin3.xRot = -2.42F - Math.abs(newangle / 4.0F);
		this.topmem3.xRot = -0.89F - Math.abs(newangle / 4.0F);
		this.topfin4.xRot = -2.63F - Math.abs(newangle / 8.0F);
		this.topmem4.xRot = -1.11F - Math.abs(newangle / 8.0F);
		if (!state.flag && !state.attacking) {
		tailspeed = 0.22F;
		tailamp = 0.03F;
		}
		this.tail1.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * (float) Math.PI * 0.04F;
		this.tail2.z = this.tail1.z + (float)Math.cos(this.tail1.yRot) * 13.0F;
		this.tail2.x = this.tail1.x + 1.5F + (float)Math.sin(this.tail1.yRot) * 13.0F;
		this.tail2.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - pi4) * (float) Math.PI * tailamp;
		this.tail3.z = this.tail2.z + (float)Math.cos(this.tail2.yRot) * 13.0F;
		this.tail3.x = this.tail2.x - 0.5F + (float)Math.sin(this.tail2.yRot) * 13.0F;
		this.tail3.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - 2.0F * pi4) * (float) Math.PI * tailamp;
		this.tailfin1.z = this.tail3.z + (float)Math.cos(this.tail3.yRot) * 10.0F;
		this.tailfin1.x = this.tail3.x - 1.0F + (float)Math.sin(this.tail3.yRot) * 10.0F;
		this.tailfin1.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - 3.0F * pi4) * (float) Math.PI * tailamp;
		this.tailfin2.z = this.tailfin1.z;
		this.tailfin2.x = this.tailfin1.x;
		this.tailfin2.yRot = this.tailfin1.yRot;
		this.tailfin3.z = this.tailfin1.z;
		this.tailfin3.x = this.tailfin1.x;
		this.tailfin3.yRot = this.tailfin1.yRot;
		this.tailfin4.z = this.tailfin1.z;
		this.tailfin4.x = this.tailfin1.x;
		this.tailfin4.yRot = this.tailfin1.yRot;
		this.tailmembrane1.z = this.tailfin1.z;
		this.tailmembrane1.x = this.tailfin1.x;
		this.tailmembrane1.yRot = this.tailfin1.yRot;
		this.tailmembrane2.z = this.tailfin1.z;
		this.tailmembrane2.x = this.tailfin1.x;
		this.tailmembrane2.yRot = this.tailfin1.yRot;
		this.tailmembrane3.z = this.tailfin1.z;
		this.tailmembrane3.x = this.tailfin1.x;
		this.tailmembrane3.yRot = this.tailfin1.yRot;
		if (state.flag) {
		f3 = state.yRot;
		} else {
		f3 /= 2.0F;
		}
		this.neck3.yRot = (float)Math.toRadians(f3) * 0.125F;
		this.neck2.z = this.neck3.z - (float)Math.cos(this.neck3.yRot) * 14.0F;
		this.neck2.x = this.neck3.x + 0.5F - (float)Math.sin(this.neck3.yRot) * 14.0F;
		this.neck2.yRot = (float)Math.toRadians(f3) * 0.25F;
		this.neck1.z = this.neck2.z - (float)Math.cos(this.neck2.yRot) * 14.0F;
		this.neck1.x = this.neck2.x + 0.5F - (float)Math.sin(this.neck2.yRot) * 14.0F;
		this.neck1.yRot = (float)Math.toRadians(f3) * 0.5F;
		this.head.z = this.neck1.z - (float)Math.cos(this.neck1.yRot) * 8.0F;
		this.head.x = this.neck1.x - (float)Math.sin(this.neck1.yRot) * 8.0F;
		this.head.yRot = (float)Math.toRadians(f3) * 0.75F;
		this.hammerhead.z = this.head.z;
		this.hammerhead.x = this.head.x;
		this.hammerhead.yRot = this.head.yRot;
		this.hammerhead2.z = this.head.z;
		this.hammerhead2.x = this.head.x;
		this.hammerhead2.yRot = this.head.yRot;
		this.mouth.z = this.head.z;
		this.mouth.x = this.head.x;
		this.mouth.yRot = this.head.yRot;
		newangle = Mth.cos(f2 * 0.5F * this.wingspeed) * (float) Math.PI * 0.14F;
		if (state.attacking) {
		this.mouth.xRot = -0.61F + newangle;
		} else {
		this.mouth.xRot = -0.87F;
		}
	}
}
