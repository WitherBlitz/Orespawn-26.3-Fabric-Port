package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelWaterDragon (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class WaterDragonModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Head;
	private final ModelPart neck1;
	private final ModelPart body1;
	private final ModelPart Leg8;
	private final ModelPart Leg2;
	private final ModelPart Leg7;
	private final ModelPart Leg1;
	private final ModelPart neck2;
	private final ModelPart neck3;
	private final ModelPart neck4;
	private final ModelPart body2;
	private final ModelPart body3;
	private final ModelPart body4;
	private final ModelPart tail1;
	private final ModelPart tailmiddle;
	private final ModelPart tailtop;
	private final ModelPart tailbottom;
	private final ModelPart nose;
	private final ModelPart headfin;
	private final ModelPart rightear;
	private final ModelPart leftear;
	private final ModelPart neackfin;
	private final ModelPart Bodyfin;
	private final ModelPart jaw;
	private final float wingspeed;

	public WaterDragonModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Head = root.getChild("head");
		this.neck1 = root.getChild("neck1");
		this.body1 = root.getChild("body1");
		this.Leg8 = root.getChild("leg8");
		this.Leg2 = root.getChild("leg2");
		this.Leg7 = root.getChild("leg7");
		this.Leg1 = root.getChild("leg1");
		this.neck2 = root.getChild("neck2");
		this.neck3 = root.getChild("neck3");
		this.neck4 = root.getChild("neck4");
		this.body2 = root.getChild("body2");
		this.body3 = root.getChild("body3");
		this.body4 = root.getChild("body4");
		this.tail1 = root.getChild("tail1");
		this.tailmiddle = root.getChild("tailmiddle");
		this.tailtop = root.getChild("tailtop");
		this.tailbottom = root.getChild("tailbottom");
		this.nose = root.getChild("nose");
		this.headfin = root.getChild("headfin");
		this.rightear = root.getChild("rightear");
		this.leftear = root.getChild("leftear");
		this.neackfin = root.getChild("neackfin");
		this.Bodyfin = root.getChild("bodyfin");
		this.jaw = root.getChild("jaw");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelWaterDragon");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		float pi4 = (float) (Math.PI / 4);
		float root13 = (float)Math.sqrt(13.0);
		float root20 = (float)Math.sqrt(20.0);
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.2F * f1;
		} else {
		newangle = 0.0F;
		}
		this.body3.yRot = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.4F * f1;
		this.body4.z = this.body3.z + (float)Math.cos(this.body3.yRot) * 7.0F;
		this.body4.x = this.body3.x - 1.0F + (float)Math.sin(this.body3.yRot) * 7.0F;
		this.body4.yRot = Mth.cos(f2 * 1.3F * this.wingspeed - pi4) * (float) Math.PI * 0.4F * f1;
		this.tail1.z = this.body4.z + (float)Math.cos(this.body4.yRot) * 5.0F;
		this.tail1.x = this.body4.x + (float)Math.sin(this.body4.yRot) * 5.0F;
		this.tail1.yRot = Mth.cos(f2 * 1.3F * this.wingspeed - 2.0F * pi4) * (float) Math.PI * 0.4F * f1;
		this.tailmiddle.z = this.tail1.z + (float)Math.cos(this.tail1.yRot) * 3.0F;
		this.tailmiddle.x = this.tail1.x + (float)Math.sin(this.tail1.yRot) * 3.0F;
		this.tailmiddle.yRot = Mth.cos(f2 * 1.3F * this.wingspeed - 3.0F * pi4) * (float) Math.PI * 0.4F * f1;
		this.tailtop.yRot = this.tailmiddle.yRot;
		this.tailtop.z = this.tailmiddle.z;
		this.tailtop.x = this.tailmiddle.x;
		this.tailbottom.yRot = this.tailmiddle.yRot;
		this.tailbottom.z = this.tailmiddle.z;
		this.tailbottom.x = this.tailmiddle.x;
		this.Leg8.yRot = 0.58F + newangle;
		this.Leg2.yRot = -0.58F + newangle;
		this.Leg7.yRot = -0.58F - newangle;
		this.Leg1.yRot = 0.58F - newangle;
		newangle = Mth.cos(f2 * 0.8F * this.wingspeed) * (float) Math.PI * 0.1F;
		this.leftear.yRot = 0.62F + newangle;
		this.rightear.yRot = -0.62F - newangle;
		newangle = Mth.cos(f2 * 0.7F * this.wingspeed) * (float) Math.PI * 0.02F;
		if (state.sitting) {
		newangle = 0.0F;
		}
		this.Bodyfin.zRot = newangle;
		newangle = Mth.cos(f2 * 0.6F * this.wingspeed) * (float) Math.PI * 0.1F;
		if (state.sitting) {
		newangle = 0.0F;
		}
		this.neackfin.yRot = newangle;
		newangle = Mth.cos(f2 * 0.5F * this.wingspeed) * (float) Math.PI * 0.05F;
		if (state.sitting) {
		newangle = 0.0F;
		}
		this.headfin.yRot = newangle;
		if (state.attacking) {
		newangle = Mth.cos(f2 * 1.2F * this.wingspeed) * (float) Math.PI * 0.25F;
		this.jaw.xRot = newangle;
		} else if (state.flag) {
		// state.flag: spraying water (OreSpawn's attacking state 2), jaw held open.
		this.jaw.xRot = 0.45F;
		} else {
		this.jaw.xRot = -0.25F;
		}
		newangle = (float)Math.toRadians(f3) * 0.75F;
		this.Head.yRot = newangle;
		this.nose.yRot = newangle;
		this.nose.z = this.Head.z - (float)Math.cos(this.Head.yRot) * 8.0F;
		this.nose.x = this.Head.x - (float)Math.sin(this.Head.yRot) * 8.0F;
		this.jaw.yRot = newangle;
		this.jaw.z = this.Head.z - (float)Math.cos(this.Head.yRot) * 7.0F;
		this.jaw.x = this.Head.x - (float)Math.sin(this.Head.yRot) * 7.0F - 1.0F;
		this.headfin.yRot = newangle;
		this.headfin.z = this.Head.z - (float)Math.cos(this.Head.yRot) * 3.0F;
		this.headfin.x = this.Head.x - (float)Math.sin(this.Head.yRot) * 3.0F;
		this.leftear.yRot += newangle;
		this.leftear.z = this.Head.z - (float)Math.cos(this.Head.yRot - pi4) * root13;
		this.leftear.x = this.Head.x - (float)Math.sin(this.Head.yRot - pi4) * root13;
		this.rightear.yRot += newangle;
		this.rightear.z = this.Head.z - (float)Math.cos(this.Head.yRot + pi4) * root20;
		this.rightear.x = this.Head.x - (float)Math.sin(this.Head.yRot + pi4) * root20;
	}
}
