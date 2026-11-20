package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelBee (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class BeeModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Sting;
	private final ModelPart Abdomnem1;
	private final ModelPart Abdomnem2;
	private final ModelPart Abdomnem3;
	private final ModelPart Abdomnem4;
	private final ModelPart Abdomnem5;
	private final ModelPart MainBody;
	private final ModelPart Neck;
	private final ModelPart Head;
	private final ModelPart WingRight;
	private final ModelPart WingLeft;
	private final ModelPart RA1;
	private final ModelPart LA1;
	private final ModelPart LA2;
	private final ModelPart RA2;
	private final ModelPart RA3;
	private final ModelPart LA3;
	private final ModelPart LeftPom;
	private final ModelPart RightPom;
	private final ModelPart LeftPincerExtra;
	private final ModelPart LeftPincerMain;
	private final ModelPart RightPincerMain;
	private final ModelPart RightPincerExtra;
	private final float wingspeed;

	public BeeModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Sting = root.getChild("sting");
		this.Abdomnem1 = root.getChild("abdomnem1");
		this.Abdomnem2 = root.getChild("abdomnem2");
		this.Abdomnem3 = root.getChild("abdomnem3");
		this.Abdomnem4 = root.getChild("abdomnem4");
		this.Abdomnem5 = root.getChild("abdomnem5");
		this.MainBody = root.getChild("mainbody");
		this.Neck = root.getChild("neck");
		this.Head = root.getChild("head");
		this.WingRight = root.getChild("wingright");
		this.WingLeft = root.getChild("wingleft");
		this.RA1 = root.getChild("ra1");
		this.LA1 = root.getChild("la1");
		this.LA2 = root.getChild("la2");
		this.RA2 = root.getChild("ra2");
		this.RA3 = root.getChild("ra3");
		this.LA3 = root.getChild("la3");
		this.LeftPom = root.getChild("leftpom");
		this.RightPom = root.getChild("rightpom");
		this.LeftPincerExtra = root.getChild("leftpincerextra");
		this.LeftPincerMain = root.getChild("leftpincermain");
		this.RightPincerMain = root.getChild("rightpincermain");
		this.RightPincerExtra = root.getChild("rightpincerextra");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelBee");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = 0.0F;
		newangle = Mth.cos(f2 * 1.1F * this.wingspeed) * (float) Math.PI * 0.3F;
		this.WingLeft.zRot = -1.745F - newangle;
		this.WingRight.zRot = 1.754F + newangle;
		newangle = Mth.cos(f2 * 0.3F * this.wingspeed) * (float) Math.PI * 0.1F;
		this.LeftPincerMain.yRot = -0.274F + newangle;
		this.LeftPincerExtra.yRot = -0.274F + newangle;
		this.RightPincerMain.yRot = 0.274F - newangle;
		this.RightPincerExtra.yRot = 0.274F - newangle;
		newangle = Mth.cos(f2 * 0.21F * this.wingspeed) * (float) Math.PI * 0.06F;
		this.LA1.xRot = 0.261F + newangle;
		this.LA2.xRot = 0.436F + newangle;
		this.LA3.xRot = 0.611F + newangle;
		this.LeftPom.xRot = newangle;
		newangle = Mth.cos(f2 * 0.27F * this.wingspeed) * (float) Math.PI * 0.06F;
		this.RA1.xRot = 0.261F + newangle;
		this.RA2.xRot = 0.436F + newangle;
		this.RA3.xRot = 0.611F + newangle;
		this.RightPom.xRot = newangle;
		newangle = Mth.cos(f2 * 0.31F * this.wingspeed) * (float) Math.PI * 0.06F;
		this.LA1.zRot = newangle;
		this.LA2.zRot = newangle;
		this.LA3.zRot = newangle;
		this.LeftPom.zRot = newangle;
		newangle = Mth.cos(f2 * 0.37F * this.wingspeed) * (float) Math.PI * 0.06F;
		this.RA1.zRot = newangle;
		this.RA2.zRot = newangle;
		this.RA3.zRot = newangle;
		this.RightPom.zRot = newangle;
		if (!state.attacking) {
		newangle = Mth.cos(f2 * 0.021F * this.wingspeed) * (float) Math.PI * 0.023F;
		} else {
		newangle = Mth.cos(f2 * 0.11F * this.wingspeed) * (float) Math.PI * 0.055F;
		}
		this.Abdomnem5.xRot = 1.099F + newangle;
		this.Abdomnem4.xRot = this.Abdomnem5.xRot + newangle - 0.35F;
		this.Abdomnem4.y = (float)(this.Abdomnem5.y + Math.cos(this.Abdomnem5.xRot) * 10.0);
		this.Abdomnem4.z = (float)(this.Abdomnem5.z + Math.sin(this.Abdomnem5.xRot) * 10.0);
		this.Abdomnem3.xRot = this.Abdomnem4.xRot + newangle - 0.35F;
		this.Abdomnem3.y = (float)(this.Abdomnem4.y + Math.cos(this.Abdomnem4.xRot) * 10.0);
		this.Abdomnem3.z = (float)(this.Abdomnem4.z + Math.sin(this.Abdomnem4.xRot) * 10.0);
		this.Abdomnem2.xRot = this.Abdomnem3.xRot + newangle - 0.35F;
		this.Abdomnem2.y = (float)(this.Abdomnem3.y + Math.cos(this.Abdomnem3.xRot) * 6.0);
		this.Abdomnem2.z = (float)(this.Abdomnem3.z + Math.sin(this.Abdomnem3.xRot) * 6.0);
		this.Abdomnem1.xRot = this.Abdomnem2.xRot + newangle - 0.35F;
		this.Abdomnem1.y = (float)(this.Abdomnem2.y + Math.cos(this.Abdomnem2.xRot) * 5.0);
		this.Abdomnem1.z = (float)(this.Abdomnem2.z + Math.sin(this.Abdomnem2.xRot) * 5.0);
		this.Sting.xRot = this.Abdomnem1.xRot + newangle - 0.35F;
		this.Sting.y = (float)(this.Abdomnem1.y + Math.cos(this.Abdomnem1.xRot) * 7.0);
		this.Sting.z = 1.0F + (float)(this.Abdomnem1.z + Math.sin(this.Abdomnem1.xRot) * 7.0);
	}
}
