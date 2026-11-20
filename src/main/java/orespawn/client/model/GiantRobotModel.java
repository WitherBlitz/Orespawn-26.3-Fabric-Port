package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/**
 * Ported from OreSpawn's ModelGiantRobot (its parts are read from the original OreSpawn 1.7.10 file, see OriginalModels). OreSpawn drew one leg (thigh,
 * shin, feet) and one arm (arm, fist) twice a frame, moved to each side in turn; here each side has its own copy, in
 * the groups leg_a/leg_b and arm_a/arm_b, posed the same way. The hips sway and bob as it walks, the legs stride and the
 * arms swing with them, and while attacking the arms windmill punches. state.attacking is GiantRobot.attacking().
 */
@Environment(EnvType.CLIENT)
public class GiantRobotModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart hip, back1, back2, back3, shoulders, neck, head;
	private final Leg legA, legB;
	private final Arm armA, armB;
	private final float wingspeed;
	private final float hipy;

	private record Leg(ModelPart thigh, ModelPart thigh2, ModelPart thigh3, ModelPart shin, ModelPart foot1, ModelPart foot2, ModelPart foot3) {
		static Leg of(ModelPart g) {
			return new Leg(g.getChild("thigh"), g.getChild("thigh2"), g.getChild("thigh3"), g.getChild("shin"), g.getChild("foot1"), g.getChild("foot2"),
				g.getChild("foot3"));
		}
	}

	private record Arm(ModelPart arm1, ModelPart arm2, ModelPart arm3, ModelPart knuckles) {
		static Arm of(ModelPart g) {
			return new Arm(g.getChild("arm1"), g.getChild("arm2"), g.getChild("arm3"), g.getChild("knuckles"));
		}
	}

	public GiantRobotModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.hip = root.getChild("hip");
		this.back1 = root.getChild("back1");
		this.back2 = root.getChild("back2");
		this.back3 = root.getChild("back3");
		this.shoulders = root.getChild("shoulders");
		this.neck = root.getChild("neck");
		this.head = root.getChild("head");
		this.legA = Leg.of(root.getChild("leg_a"));
		this.legB = Leg.of(root.getChild("leg_b"));
		this.armA = Arm.of(root.getChild("arm_a"));
		this.armB = Arm.of(root.getChild("arm_b"));
		this.hipy = hip.y;
	}

	public static LayerDefinition createBodyLayer() {
		OriginalModels.Geometry g = OriginalModels.geometry("ModelGiantRobot");
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		for (String name : new String[] {"hip", "back1", "back2", "back3", "shoulders", "neck", "head"}) OriginalModels.copy(root, g.part(name), name);
		// OreSpawn draws its one leg and one arm twice (see setupAnim), so the port has two of each.
		for (String side : new String[] {"leg_a", "leg_b"}) {
			PartDefinition leg = root.addOrReplaceChild(side, CubeListBuilder.create(), PartPose.ZERO);
			for (String name : new String[] {"thigh", "thigh2", "thigh3", "shin", "foot1", "foot2", "foot3"}) OriginalModels.copy(leg, g.part(name), name);
		}
		for (String side : new String[] {"arm_a", "arm_b"}) {
			PartDefinition arm = root.addOrReplaceChild(side, CubeListBuilder.create(), PartPose.ZERO);
			for (String name : new String[] {"arm1", "arm2", "arm3", "knuckles"}) OriginalModels.copy(arm, g.part(name), name);
		}
		return g.layer(mesh);
	}

	/** One leg hung from the hip, 13 pixels to one side ({@code side} 1 or -1). */
	private void poseLeg(Leg l, float thighAngle, float shinAngle, float side) {
		float x = hip.x + side * (float) Math.cos(hip.xRot) * (float) Math.sin(hip.yRot) * 13.0F;
		float y = hip.y - side * (float) Math.sin(hip.xRot) * 13.0F;
		float z = hip.z + side * (float) Math.cos(hip.xRot) * (float) Math.cos(hip.yRot) * 13.0F;
		for (ModelPart p : new ModelPart[] {l.thigh(), l.thigh2(), l.thigh3()}) {
			p.xRot = thighAngle;
			p.x = x;
			p.y = y;
			p.z = z;
		}
		l.shin().xRot = shinAngle;
		l.shin().y = y + (float) Math.cos(thighAngle) * 40.0F;
		l.shin().z = z + (float) Math.sin(thighAngle) * 40.0F;
		l.shin().x = x;
		for (ModelPart p : new ModelPart[] {l.foot1(), l.foot2(), l.foot3()}) {
			p.xRot = shinAngle;
			p.x = l.shin().x;
			p.y = l.shin().y;
			p.z = l.shin().z;
		}
	}

	/** One arm hung from the shoulders, 26 pixels to one side. */
	private void poseArm(Arm a, float upper, float lower, float side) {
		a.arm1().y = a.arm2().y = hip.y - 60.0F;
		a.arm1().x = a.arm2().x = hip.x + side * 26.0F;
		a.arm1().z = a.arm2().z = shoulders.z - side * (float) Math.sin(shoulders.yRot) * 26.0F;
		a.arm1().xRot = a.arm2().xRot = upper;
		a.arm3().xRot = a.knuckles().xRot = (float) (lower - Math.PI / 16);
		a.arm3().y = a.knuckles().y = a.arm1().y + (float) Math.cos(a.arm1().xRot) * 41.0F;
		a.arm3().z = a.knuckles().z = a.arm1().z + (float) Math.sin(a.arm1().xRot) * 41.0F;
		a.arm3().x = a.knuckles().x = a.arm1().x;
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		// From render() (OreSpawn kept these angles in RenderGiantRobotInfo, worked out afresh each frame).
		float movescale = Math.min(f1 * 0.65F, 1.0F);
		float hipx = (float) (Math.cos(-f2 * wingspeed) * Math.PI * 0.1F * movescale);
		float hipyaw = (float) (Math.sin(-f2 * wingspeed) * Math.PI * 0.1F * movescale);
		float thigh0 = (float) (Math.cos(-f2 * wingspeed + Math.PI / 2) * Math.PI * 0.15F * movescale) - (float) (Math.PI / 16 * movescale);
		float thigh1 = (float) (Math.cos(-f2 * wingspeed + Math.PI + Math.PI / 2) * Math.PI * 0.15F * movescale) - (float) (Math.PI / 16 * movescale);
		float shin0 = (float) ((float) (Math.cos(-f2 * wingspeed + Math.PI) * Math.PI * 0.2F * movescale) + 0.6283185400806344 * movescale);
		float shin1 = (float) ((float) (Math.cos(-f2 * wingspeed) * Math.PI * 0.2F * movescale) + 0.6283185400806344 * movescale);
		float bob = (float) (Math.cos(-f2 * wingspeed * 2.0F) * movescale);
		hip.y = hipy + bob * 4.0F;
		hip.xRot = hipx;
		hip.yRot = (float) (hipyaw + Math.PI / 2);
		poseLeg(legA, thigh0, shin0, 1.0F);
		poseLeg(legB, thigh1, shin1, -1.0F);
		float shoulderangle = -hipyaw;
		float a1 = thigh1, a2 = thigh1, b1 = thigh0, b2 = thigh0;
		if (state.attacking) {
			shoulderangle = (float) -(Math.sin(f2 * wingspeed * 2.0F) * Math.PI * 0.2F);
			a1 = (float) ((float) (Math.sin(f2 * wingspeed * 2.0F) * Math.PI / 5.0) - Math.PI / 4);
			a2 = (float) (-a1 + Math.PI);
			a1 = (float) (a1 + Math.PI / 5);
			a2 = (float) (a2 + Math.PI / 5);
			b1 = (float) ((float) -(Math.sin(f2 * wingspeed * 2.0F) * Math.PI / 5.0) - Math.PI / 4);
			b2 = (float) (-b1 + Math.PI);
			b1 = (float) (b1 + Math.PI / 5);
			b2 = (float) (b2 + Math.PI / 5);
		}
		back3.yRot = shoulderangle / 2.0F;
		shoulders.yRot = shoulderangle;
		poseArm(armA, a1, a2, 1.0F);
		poseArm(armB, b1, b2, -1.0F);
		back1.y = back2.y = back3.y = hip.y;
		shoulders.y = neck.y = head.y = hip.y;
		head.yRot = (float) Math.toRadians(f3);
		head.xRot = (float) Math.toRadians(f4) / 3.0F;
	}
}
