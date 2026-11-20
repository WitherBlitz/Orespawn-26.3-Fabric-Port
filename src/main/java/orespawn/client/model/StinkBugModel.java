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
 * Ported from OreSpawn's ModelStinkBug (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). OreSpawn's constructor ended by
 * moving every part 6 down (rotationPointY += 6); the converter missed that, so it was added to each pivot here.
 */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class StinkBugModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart f6;
	private final ModelPart b10;
	private final ModelPart l6;
	private final ModelPart l4;
	private final ModelPart f4;
	private final ModelPart l5;
	private final ModelPart f5;
	private final ModelPart l3;
	private final ModelPart l2;
	private final ModelPart l1;
	private final ModelPart f3;
	private final ModelPart f2;
	private final ModelPart f1;
	private final ModelPart jaw;
	private final ModelPart b9;
	private final ModelPart head;
	private final ModelPart b4;
	private final ModelPart h1;
	private final ModelPart h2;
	private final ModelPart body;
	private final ModelPart t21;
	private final ModelPart tail;
	private final ModelPart t22;
	private final ModelPart t20;
	private final ModelPart t19;
	private final ModelPart t6;
	private final ModelPart t11;
	private final ModelPart t9;
	private final ModelPart t4;
	private final ModelPart t2;
	private final ModelPart t7;
	private final ModelPart t12;
	private final ModelPart t10;
	private final ModelPart t8;
	private final ModelPart t5;
	private final ModelPart t3;
	private final ModelPart t1;
	private final ModelPart t18;
	private final ModelPart t16;
	private final ModelPart t14;
	private final ModelPart t13;
	private final ModelPart t15;
	private final ModelPart t17;
	private final ModelPart b1;
	private final ModelPart b2;
	private final ModelPart b3;
	private final ModelPart b8;
	private final ModelPart b7;
	private final ModelPart b6;
	private final ModelPart b5;
	private final float wingspeed;

	public StinkBugModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.f6 = root.getChild("f6");
		this.b10 = root.getChild("b10");
		this.l6 = root.getChild("l6");
		this.l4 = root.getChild("l4");
		this.f4 = root.getChild("f4");
		this.l5 = root.getChild("l5");
		this.f5 = root.getChild("f5");
		this.l3 = root.getChild("l3");
		this.l2 = root.getChild("l2");
		this.l1 = root.getChild("l1");
		this.f3 = root.getChild("f3");
		this.f2 = root.getChild("f2");
		this.f1 = root.getChild("f1");
		this.jaw = root.getChild("jaw");
		this.b9 = root.getChild("b9");
		this.head = root.getChild("head");
		this.b4 = root.getChild("b4");
		this.h1 = root.getChild("h1");
		this.h2 = root.getChild("h2");
		this.body = root.getChild("body");
		this.t21 = root.getChild("t21");
		this.tail = root.getChild("tail");
		this.t22 = root.getChild("t22");
		this.t20 = root.getChild("t20");
		this.t19 = root.getChild("t19");
		this.t6 = root.getChild("t6");
		this.t11 = root.getChild("t11");
		this.t9 = root.getChild("t9");
		this.t4 = root.getChild("t4");
		this.t2 = root.getChild("t2");
		this.t7 = root.getChild("t7");
		this.t12 = root.getChild("t12");
		this.t10 = root.getChild("t10");
		this.t8 = root.getChild("t8");
		this.t5 = root.getChild("t5");
		this.t3 = root.getChild("t3");
		this.t1 = root.getChild("t1");
		this.t18 = root.getChild("t18");
		this.t16 = root.getChild("t16");
		this.t14 = root.getChild("t14");
		this.t13 = root.getChild("t13");
		this.t15 = root.getChild("t15");
		this.t17 = root.getChild("t17");
		this.b1 = root.getChild("b1");
		this.b2 = root.getChild("b2");
		this.b3 = root.getChild("b3");
		this.b8 = root.getChild("b8");
		this.b7 = root.getChild("b7");
		this.b6 = root.getChild("b6");
		this.b5 = root.getChild("b5");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelStinkBug");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f1 = state.walkAnimationSpeed, f2 = state.ageInTicks;
		float newangle = Mth.sin(f2 * 3.1F * this.wingspeed) * (float) Math.PI * 0.3F * f1;
		// OreSpawn set f3 twice and never f5, so f5 (the middle foot on the right) doesn't step; kept as it was.
		this.f1.xRot = this.f3.xRot = newangle;
		this.f2.xRot = this.f4.xRot = this.f6.xRot = -newangle;
		newangle = Mth.sin(f2 * 0.4F * this.wingspeed) * (float) Math.PI * 0.2F;
		this.b9.zRot = newangle;
		this.b10.zRot = -newangle;
		newangle = Mth.sin(f2 * 0.2F * this.wingspeed) * (float) Math.PI * 0.04F;
		this.jaw.xRot = 0.18F + newangle;
		this.h1.xRot = 0.52F + Mth.sin(f2 * 0.4F * this.wingspeed) * (float) Math.PI * 0.15F;
		this.h1.yRot = -0.3F + Mth.sin(f2 * 0.43F * this.wingspeed) * (float) Math.PI * 0.15F;
		this.h2.xRot = 0.52F + Mth.sin(f2 * 0.46F * this.wingspeed) * (float) Math.PI * 0.15F;
		this.h2.yRot = 0.3F + Mth.sin(f2 * 0.49F * this.wingspeed) * (float) Math.PI * 0.15F;
		this.tail.xRot = -0.2F + Mth.sin(f2 * 0.1F * this.wingspeed) * (float) Math.PI * 0.1F;
		for (ModelPart t : new ModelPart[] {t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21, t22}) {
			t.xRot = this.tail.xRot;
		}
	}
}
