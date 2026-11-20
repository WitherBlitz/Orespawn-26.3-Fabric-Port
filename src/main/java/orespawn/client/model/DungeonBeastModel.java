package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelDungeonBeast (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class DungeonBeastModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart tail7;
	private final ModelPart head3;
	private final ModelPart neck;
	private final ModelPart lhornbase;
	private final ModelPart leye;
	private final ModelPart ljaw3;
	private final ModelPart ljaw1;
	private final ModelPart ljaw2;
	private final ModelPart rjaw1;
	private final ModelPart rjaw2;
	private final ModelPart rjaw3;
	private final ModelPart t1s3;
	private final ModelPart rshoulder;
	private final ModelPart rheel;
	private final ModelPart lshoulder;
	private final ModelPart rleg1;
	private final ModelPart rleg2;
	private final ModelPart lleg1;
	private final ModelPart lleg2;
	private final ModelPart rfoot;
	private final ModelPart ltoe3;
	private final ModelPart ltoe2;
	private final ModelPart ltoe1;
	private final ModelPart head1;
	private final ModelPart horn2;
	private final ModelPart rhornbase;
	private final ModelPart rh1;
	private final ModelPart lh1;
	private final ModelPart lh2;
	private final ModelPart rh2;
	private final ModelPart rh3;
	private final ModelPart lh3;
	private final ModelPart lh4;
	private final ModelPart rh4;
	private final ModelPart horn1;
	private final ModelPart t2s3;
	private final ModelPart tail3;
	private final ModelPart t4s1;
	private final ModelPart t6s1;
	private final ModelPart tail6;
	private final ModelPart body;
	private final ModelPart bodys1;
	private final ModelPart bodys2;
	private final ModelPart tail1;
	private final ModelPart bodys3;
	private final ModelPart t1s1;
	private final ModelPart t1s2;
	private final ModelPart tail2;
	private final ModelPart t3s2;
	private final ModelPart t2s2;
	private final ModelPart t2s1;
	private final ModelPart t3s1;
	private final ModelPart tail4;
	private final ModelPart tail5;
	private final ModelPart t5s1;
	private final ModelPart head2;
	private final ModelPart reye;
	private final ModelPart lfoot;
	private final ModelPart rfoot2;
	private final ModelPart lfoot2;
	private final ModelPart lheel;
	private final ModelPart rtoe3;
	private final ModelPart rtoe2;
	private final ModelPart rtoe1;
	private final float wingspeed;

	public DungeonBeastModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.tail7 = root.getChild("tail7");
		this.head3 = root.getChild("head3");
		this.neck = root.getChild("neck");
		this.lhornbase = root.getChild("lhornbase");
		this.leye = root.getChild("leye");
		this.ljaw3 = root.getChild("ljaw3");
		this.ljaw1 = root.getChild("ljaw1");
		this.ljaw2 = root.getChild("ljaw2");
		this.rjaw1 = root.getChild("rjaw1");
		this.rjaw2 = root.getChild("rjaw2");
		this.rjaw3 = root.getChild("rjaw3");
		this.t1s3 = root.getChild("t1s3");
		this.rshoulder = root.getChild("rshoulder");
		this.rheel = root.getChild("rheel");
		this.lshoulder = root.getChild("lshoulder");
		this.rleg1 = root.getChild("rleg1");
		this.rleg2 = root.getChild("rleg2");
		this.lleg1 = root.getChild("lleg1");
		this.lleg2 = root.getChild("lleg2");
		this.rfoot = root.getChild("rfoot");
		this.ltoe3 = root.getChild("ltoe3");
		this.ltoe2 = root.getChild("ltoe2");
		this.ltoe1 = root.getChild("ltoe1");
		this.head1 = root.getChild("head1");
		this.horn2 = root.getChild("horn2");
		this.rhornbase = root.getChild("rhornbase");
		this.rh1 = root.getChild("rh1");
		this.lh1 = root.getChild("lh1");
		this.lh2 = root.getChild("lh2");
		this.rh2 = root.getChild("rh2");
		this.rh3 = root.getChild("rh3");
		this.lh3 = root.getChild("lh3");
		this.lh4 = root.getChild("lh4");
		this.rh4 = root.getChild("rh4");
		this.horn1 = root.getChild("horn1");
		this.t2s3 = root.getChild("t2s3");
		this.tail3 = root.getChild("tail3");
		this.t4s1 = root.getChild("t4s1");
		this.t6s1 = root.getChild("t6s1");
		this.tail6 = root.getChild("tail6");
		this.body = root.getChild("body");
		this.bodys1 = root.getChild("bodys1");
		this.bodys2 = root.getChild("bodys2");
		this.tail1 = root.getChild("tail1");
		this.bodys3 = root.getChild("bodys3");
		this.t1s1 = root.getChild("t1s1");
		this.t1s2 = root.getChild("t1s2");
		this.tail2 = root.getChild("tail2");
		this.t3s2 = root.getChild("t3s2");
		this.t2s2 = root.getChild("t2s2");
		this.t2s1 = root.getChild("t2s1");
		this.t3s1 = root.getChild("t3s1");
		this.tail4 = root.getChild("tail4");
		this.tail5 = root.getChild("tail5");
		this.t5s1 = root.getChild("t5s1");
		this.head2 = root.getChild("head2");
		this.reye = root.getChild("reye");
		this.lfoot = root.getChild("lfoot");
		this.rfoot2 = root.getChild("rfoot2");
		this.lfoot2 = root.getChild("lfoot2");
		this.lheel = root.getChild("lheel");
		this.rtoe3 = root.getChild("rtoe3");
		this.rtoe2 = root.getChild("rtoe2");
		this.rtoe1 = root.getChild("rtoe1");
		// OreSpawn never drew these four toes.
		this.ltoe1.visible = this.ltoe3.visible = this.rtoe1.visible = this.rtoe3.visible = false;
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelDungeonBeast");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		// state.attacking: DungeonBeast.getAttacking() != 0. state.flag: the jaws snap this cycle (see below).
		float newangle = 0.0F;
		float upangle = 0.0F;
		float tailamp = 0.0F;
		float pi4 = 0.39269876F;
		newangle = Mth.cos(f2 * 1.4F * this.wingspeed) * (float) Math.PI * 0.22F * f1;
		this.rleg1.zRot = this.rleg2.zRot = this.rfoot.zRot = this.rfoot2.zRot = this.rheel.zRot = newangle;
		this.rtoe2.zRot = -0.785F + newangle;
		this.lleg1.zRot = this.lleg2.zRot = this.lfoot.zRot = this.lfoot2.zRot = this.lheel.zRot = -newangle;
		this.ltoe2.zRot = -0.785F - newangle;
		this.bodys1.xRot = Mth.cos(f2 * 0.5F * this.wingspeed) * (float) Math.PI * 0.07F;
		this.bodys2.xRot = Mth.cos(f2 * 0.5F * this.wingspeed + pi4) * (float) Math.PI * 0.07F;
		this.bodys3.xRot = Mth.cos(f2 * 0.5F * this.wingspeed + 2.0F * pi4) * (float) Math.PI * 0.07F;
		this.t1s1.xRot = Mth.cos(f2 * 0.5F * this.wingspeed + 3.0F * pi4) * (float) Math.PI * 0.07F;
		this.t1s2.xRot = Mth.cos(f2 * 0.5F * this.wingspeed + 4.0F * pi4) * (float) Math.PI * 0.07F;
		this.t1s3.xRot = Mth.cos(f2 * 0.5F * this.wingspeed + 5.0F * pi4) * (float) Math.PI * 0.07F;
		this.t2s1.xRot = -Mth.cos(f2 * 0.5F * this.wingspeed + 6.0F * pi4) * (float) Math.PI * 0.07F;
		this.t2s2.xRot = -Mth.cos(f2 * 0.5F * this.wingspeed + 7.0F * pi4) * (float) Math.PI * 0.07F;
		this.t2s3.xRot = -Mth.cos(f2 * 0.5F * this.wingspeed + 8.0F * pi4) * (float) Math.PI * 0.07F;
		this.t3s1.xRot = -Mth.cos(f2 * 0.5F * this.wingspeed + 9.0F * pi4) * (float) Math.PI * 0.07F;
		this.t3s2.xRot = -Mth.cos(f2 * 0.5F * this.wingspeed + 10.0F * pi4) * (float) Math.PI * 0.07F;
		this.t4s1.xRot = -Mth.cos(f2 * 0.5F * this.wingspeed + 11.0F * pi4) * (float) Math.PI * 0.07F;
		this.t5s1.xRot = -Mth.cos(f2 * 0.5F * this.wingspeed + 12.0F * pi4) * (float) Math.PI * 0.07F;
		this.t6s1.xRot = -Mth.cos(f2 * 0.5F * this.wingspeed + 13.0F * pi4) * (float) Math.PI * 0.07F;
		if (!state.attacking) {
		tailamp = f1;
		} else {
		tailamp = 1.25F;
		}
		newangle = Mth.cos(f2 * 0.75F * this.wingspeed) * (float) Math.PI * 0.25F * tailamp;
		this.tail1.yRot = newangle * 0.25F;
		this.t1s1.yRot = this.t1s2.yRot = this.t1s3.yRot = this.tail1.yRot;
		this.tail2.yRot = newangle * 0.5F;
		this.tail2.x = this.tail1.x - (float)Math.cos(this.tail1.yRot) * 6.0F;
		this.tail2.z = this.tail1.z - (float)Math.sin(this.tail1.yRot) * 6.0F;
		this.t2s1.yRot = this.t2s2.yRot = this.t2s3.yRot = this.tail2.yRot;
		this.t2s1.z = this.t2s2.z = this.t2s3.z = this.tail2.z;
		this.t2s1.x = this.t2s2.x = this.t2s3.x = this.tail2.x;
		this.tail3.yRot = newangle * 0.75F;
		this.tail3.x = this.tail2.x - (float)Math.cos(this.tail2.yRot) * 5.0F;
		this.tail3.z = this.tail2.z - (float)Math.sin(this.tail2.yRot) * 5.0F;
		this.t3s1.yRot = this.t3s2.yRot = this.tail3.yRot;
		this.t3s1.z = this.t3s2.z = this.tail3.z;
		this.t3s1.x = this.t3s2.x = this.tail3.x;
		this.tail4.yRot = newangle;
		this.tail4.x = this.tail3.x - (float)Math.cos(this.tail3.yRot) * 4.5F;
		this.tail4.z = this.tail3.z - (float)Math.sin(this.tail3.yRot) * 4.5F;
		this.t4s1.yRot = this.tail4.yRot;
		this.t4s1.z = this.tail4.z;
		this.t4s1.x = this.tail4.x;
		this.tail5.yRot = newangle * 1.25F;
		this.tail5.x = this.tail4.x - (float)Math.cos(this.tail4.yRot) * 4.0F;
		this.tail5.z = this.tail4.z - (float)Math.sin(this.tail4.yRot) * 4.0F;
		this.t5s1.yRot = this.tail5.yRot;
		this.t5s1.z = this.tail5.z;
		this.t5s1.x = this.tail5.x;
		this.tail6.yRot = newangle * 1.5F;
		this.tail6.x = this.tail5.x - (float)Math.cos(this.tail5.yRot) * 3.0F;
		this.tail6.z = this.tail5.z - (float)Math.sin(this.tail5.yRot) * 3.0F;
		this.t6s1.yRot = this.tail6.yRot;
		this.t6s1.z = this.tail6.z;
		this.t6s1.x = this.tail6.x;
		this.tail7.yRot = newangle * 1.75F;
		this.tail7.x = this.tail6.x - (float)Math.cos(this.tail6.yRot) * 3.0F;
		this.tail7.z = this.tail6.z - (float)Math.sin(this.tail6.yRot) * 3.0F;
		// OreSpawn rerolled the jaws each time this wave turned upwards: always snapping while attacking, otherwise a 1 in 15
		// chance. The Spooky client renderer makes that roll per wave and passes it in state.flag.
		newangle = Mth.cos(f2 * 2.0F * this.wingspeed) * (float) Math.PI * 0.15F;
		if (state.flag) {
		this.ljaw1.yRot = -0.349F + newangle;
		this.ljaw2.yRot = 0.349F + newangle;
		this.ljaw3.yRot = 0.523F + newangle;
		this.rjaw1.yRot = 0.349F - newangle;
		this.rjaw2.yRot = -0.349F - newangle;
		this.rjaw3.yRot = -0.523F - newangle;
		} else {
		this.ljaw1.yRot = -0.349F;
		this.ljaw2.yRot = 0.349F;
		this.ljaw3.yRot = 0.523F;
		this.rjaw1.yRot = 0.349F;
		this.rjaw2.yRot = -0.349F;
		this.rjaw3.yRot = -0.523F;
		}
		// GL11.glRotatef(90, 0, 1, 0) before drawing: the whole model turns a quarter about Y.
		this.root.yRot = (float) (Math.PI / 2);
	}
}
