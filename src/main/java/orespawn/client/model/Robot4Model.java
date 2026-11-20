package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelRobot4 (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class Robot4Model extends EntityModel<OreSpawnRenderState> {
	private final ModelPart leftfootfront;
	private final ModelPart leftfootbase;
	private final ModelPart leftfootback;
	private final ModelPart leftfoottip;
	private final ModelPart leftshin;
	private final ModelPart leftcalf;
	private final ModelPart leftkneegaurd;
	private final ModelPart leftthigh;
	private final ModelPart rightfootfront;
	private final ModelPart rightfoottip;
	private final ModelPart rightfootbase;
	private final ModelPart rightfootback;
	private final ModelPart rightshin;
	private final ModelPart rightcalf;
	private final ModelPart rightkneegaurd;
	private final ModelPart rightthigh;
	private final ModelPart hips;
	private final ModelPart stomach;
	private final ModelPart chest;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart righttopspinebase;
	private final ModelPart lefttopspinebase;
	private final ModelPart righttopspinetip;
	private final ModelPart lefttopspinetip;
	private final ModelPart middlerightspinebase;
	private final ModelPart middleleftspinebase;
	private final ModelPart middleleftspinetip;
	private final ModelPart middlerightspinetip;
	private final ModelPart torso;
	private final ModelPart rightsholder;
	private final ModelPart leftsholder;
	private final ModelPart rightsholdergaurd;
	private final ModelPart sheildbase;
	private final ModelPart sheildtip;
	private final ModelPart rightupperarm;
	private final ModelPart rightlowerarm;
	private final ModelPart sheildend;
	private final ModelPart leftupperarm;
	private final ModelPart sholdergaurdtip;
	private final ModelPart cannonbase;
	private final ModelPart cannonend;
	private final ModelPart leftcannonpiece;
	private final ModelPart topcannonpiece;
	private final ModelPart rightcannonpiece;
	private final ModelPart bottomcannonpiece;
	private final ModelPart glowycannonbit1;
	private final ModelPart glowycannonbit2;
	private final ModelPart glowycannonbit3;
	private final ModelPart glowycannonbit4;
	private final ModelPart glowycannonbit5;
	private final ModelPart cannonammo;
	private final ModelPart lowerrightspinebase;
	private final ModelPart lowerleftspinebase;
	private final ModelPart lowerrightspinetip;
	private final ModelPart lowerleftspinetip;
	private final float wingspeed;

	public Robot4Model(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.leftfootfront = root.getChild("leftfootfront");
		this.leftfootbase = root.getChild("leftfootbase");
		this.leftfootback = root.getChild("leftfootback");
		this.leftfoottip = root.getChild("leftfoottip");
		this.leftshin = root.getChild("leftshin");
		this.leftcalf = root.getChild("leftcalf");
		this.leftkneegaurd = root.getChild("leftkneegaurd");
		this.leftthigh = root.getChild("leftthigh");
		this.rightfootfront = root.getChild("rightfootfront");
		this.rightfoottip = root.getChild("rightfoottip");
		this.rightfootbase = root.getChild("rightfootbase");
		this.rightfootback = root.getChild("rightfootback");
		this.rightshin = root.getChild("rightshin");
		this.rightcalf = root.getChild("rightcalf");
		this.rightkneegaurd = root.getChild("rightkneegaurd");
		this.rightthigh = root.getChild("rightthigh");
		this.hips = root.getChild("hips");
		this.stomach = root.getChild("stomach");
		this.chest = root.getChild("chest");
		this.neck = root.getChild("neck");
		this.head = root.getChild("head");
		this.righttopspinebase = root.getChild("righttopspinebase");
		this.lefttopspinebase = root.getChild("lefttopspinebase");
		this.righttopspinetip = root.getChild("righttopspinetip");
		this.lefttopspinetip = root.getChild("lefttopspinetip");
		this.middlerightspinebase = root.getChild("middlerightspinebase");
		this.middleleftspinebase = root.getChild("middleleftspinebase");
		this.middleleftspinetip = root.getChild("middleleftspinetip");
		this.middlerightspinetip = root.getChild("middlerightspinetip");
		this.torso = root.getChild("torso");
		this.rightsholder = root.getChild("rightsholder");
		this.leftsholder = root.getChild("leftsholder");
		this.rightsholdergaurd = root.getChild("rightsholdergaurd");
		this.sheildbase = root.getChild("sheildbase");
		this.sheildtip = root.getChild("sheildtip");
		this.rightupperarm = root.getChild("rightupperarm");
		this.rightlowerarm = root.getChild("rightlowerarm");
		this.sheildend = root.getChild("sheildend");
		this.leftupperarm = root.getChild("leftupperarm");
		this.sholdergaurdtip = root.getChild("sholdergaurdtip");
		this.cannonbase = root.getChild("cannonbase");
		this.cannonend = root.getChild("cannonend");
		this.leftcannonpiece = root.getChild("leftcannonpiece");
		this.topcannonpiece = root.getChild("topcannonpiece");
		this.rightcannonpiece = root.getChild("rightcannonpiece");
		this.bottomcannonpiece = root.getChild("bottomcannonpiece");
		this.glowycannonbit1 = root.getChild("glowycannonbit1");
		this.glowycannonbit2 = root.getChild("glowycannonbit2");
		this.glowycannonbit3 = root.getChild("glowycannonbit3");
		this.glowycannonbit4 = root.getChild("glowycannonbit4");
		this.glowycannonbit5 = root.getChild("glowycannonbit5");
		this.cannonammo = root.getChild("cannonammo");
		this.lowerrightspinebase = root.getChild("lowerrightspinebase");
		this.lowerleftspinebase = root.getChild("lowerleftspinebase");
		this.lowerrightspinetip = root.getChild("lowerrightspinetip");
		this.lowerleftspinetip = root.getChild("lowerleftspinetip");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelRobot4");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		float nextangle = 0.0F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 0.5F * this.wingspeed) * (float) Math.PI * 0.15F * f1;
		} else {
		newangle = 0.0F;
		}
		this.leftfootfront.xRot = newangle;
		this.leftfootbase.xRot = newangle;
		this.leftfootback.xRot = newangle;
		this.leftfoottip.xRot = newangle;
		this.leftshin.xRot = newangle;
		this.leftcalf.xRot = newangle + 0.175F;
		this.leftkneegaurd.xRot = newangle + 0.63F;
		this.leftthigh.xRot = newangle - 0.175F;
		this.rightfootfront.xRot = -newangle;
		this.rightfoottip.xRot = -newangle;
		this.rightfootbase.xRot = -newangle;
		this.rightfootback.xRot = -newangle;
		this.rightshin.xRot = -newangle;
		this.rightcalf.xRot = -newangle + 0.175F;
		this.rightkneegaurd.xRot = -newangle + 0.63F;
		this.rightthigh.xRot = -newangle - 0.175F;
		this.head.yRot = (float)Math.toRadians(f3 / 1.5);
		float amp = (float) (Math.PI / 4);
		if (state.attacking) {
		newangle = Mth.cos((float)Math.toRadians(f2 % 360.0F) * this.wingspeed * 6.0F) * amp;
		newangle = Math.abs(newangle);
		newangle += 0.75F;
		} else {
		newangle = 0.0F;
		}
		// OreSpawn set the robot's "shielding" flag here, on the client only; it never reached the server.
		this.rightsholder.xRot = -newangle;
		this.rightsholdergaurd.xRot = -newangle - 0.21F;
		this.sheildbase.xRot = -newangle + 1.047F;
		this.sheildtip.xRot = -newangle + 1.047F;
		this.rightupperarm.xRot = -newangle - 0.21F;
		this.rightlowerarm.xRot = -newangle + 1.047F;
		this.sheildend.xRot = -newangle + 1.04F;
		this.sholdergaurdtip.xRot = -newangle - 0.21F;
		if (state.attacking) {
		newangle = 0.85F;
		} else {
		newangle = 0.0F;
		}
		this.leftsholder.xRot = -newangle;
		this.leftupperarm.xRot = -newangle - 0.21F;
		this.cannonbase.xRot = -newangle - 0.7F;
		this.cannonend.xRot = -newangle - 0.7F;
		this.leftcannonpiece.xRot = -newangle - 0.7F;
		this.topcannonpiece.xRot = -newangle - 0.7F;
		this.rightcannonpiece.xRot = -newangle - 0.7F;
		this.bottomcannonpiece.xRot = -newangle - 0.7F;
		this.glowycannonbit1.xRot = -newangle + 0.17F;
		this.glowycannonbit2.xRot = -newangle + 0.17F;
		this.glowycannonbit3.xRot = -newangle + 0.08F;
		this.glowycannonbit4.xRot = -newangle + 0.08F;
		this.glowycannonbit5.xRot = -newangle;
		this.cannonammo.xRot = -newangle - 0.7F;
		double newposy = (float)(this.leftsholder.y + Math.cos(this.leftupperarm.xRot) * 14.0);
		double newposz = (float)(this.leftsholder.z + Math.sin(this.leftupperarm.xRot) * 14.0);
		this.cannonbase.y = (float)newposy;
		this.cannonbase.z = (float)newposz;
		this.cannonend.y = (float)newposy;
		this.cannonend.z = (float)newposz;
		this.leftcannonpiece.y = (float)newposy;
		this.leftcannonpiece.z = (float)newposz;
		this.topcannonpiece.y = (float)newposy;
		this.topcannonpiece.z = (float)newposz;
		this.rightcannonpiece.y = (float)newposy;
		this.rightcannonpiece.z = (float)newposz;
		this.bottomcannonpiece.y = (float)newposy;
		this.bottomcannonpiece.z = (float)newposz;
		this.glowycannonbit1.y = (float)newposy;
		this.glowycannonbit1.z = (float)newposz;
		this.glowycannonbit2.y = (float)newposy;
		this.glowycannonbit2.z = (float)newposz;
		this.glowycannonbit3.y = (float)newposy;
		this.glowycannonbit3.z = (float)newposz;
		this.glowycannonbit4.y = (float)newposy;
		this.glowycannonbit4.z = (float)newposz;
		this.glowycannonbit5.y = (float)newposy;
		this.glowycannonbit5.z = (float)newposz;
		this.cannonammo.y = (float)newposy;
		this.cannonammo.z = (float)newposz;
	}
}
