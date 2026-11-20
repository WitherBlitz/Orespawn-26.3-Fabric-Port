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
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;
import orespawn.client.content.LegRenderState;

/**
 * Ported from OreSpawn's ModelSpiderRobot (its parts are read from the original OreSpawn 1.7.10 file, see OriginalModels). OreSpawn drew one leg
 * (three 99-pixel segments, knees, foot and spikes) eight times a frame; here each leg has its own copy (groups leg0 to
 * leg7), posed from the leg angles the robot works out each tick (orespawn.entity.robots.RobotLegs), and the jaws snap
 * while it attacks.
 * <p>
 * Drawn as OreSpawn's own renderer drew it: with no 1.5-block drop (the renderer undoes it) and no death tip-over.
 */
@Environment(EnvType.CLIENT)
public class SpiderRobotModel extends EntityModel<OreSpawnRenderState> {
	private static final String[] UPPER = new String[] {"leg1p1", "upperknee2", "hipjoint"};
	private static final String[] MIDDLE = new String[] {"leg1p2", "upperknee", "lowerknee2"};
	private static final String[] LOWER = new String[] {"leg1p3", "foot", "footspike1", "footspike2", "footspike3", "footspike4", "anklespike1", "anklespike2", "anklespike3", "anklespike4", "legbump1", "legbump2", "lowerknee"};
	private final ModelPart ljaw1, ljaw2, ljaw3, rjaw1, rjaw2, rjaw3;
	private final ModelPart[][] upper = new ModelPart[8][], middle = new ModelPart[8][], lower = new ModelPart[8][];

	public SpiderRobotModel(ModelPart root, float wingspeed) {
		super(root);
		this.ljaw1 = root.getChild("ljaw1");
		this.ljaw2 = root.getChild("ljaw2");
		this.ljaw3 = root.getChild("ljaw3");
		this.rjaw1 = root.getChild("rjaw1");
		this.rjaw2 = root.getChild("rjaw2");
		this.rjaw3 = root.getChild("rjaw3");
		for (int i = 0; i < 8; i++) {
			ModelPart leg = root.getChild("leg" + i);
			upper[i] = parts(leg, UPPER);
			middle[i] = parts(leg, MIDDLE);
			lower[i] = parts(leg, LOWER);
		}
	}

	private static ModelPart[] parts(ModelPart leg, String[] names) {
		ModelPart[] p = new ModelPart[names.length];
		for (int i = 0; i < names.length; i++) p[i] = leg.getChild(names[i]);
		return p;
	}

	public static LayerDefinition createBodyLayer() {
		OriginalModels.Geometry g = OriginalModels.geometry("ModelSpiderRobot");
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		for (String name : new String[] {"bodycenter", "abdomen", "head", "ljaw1", "rjaw1", "ljaw2", "rjaw2", "ljaw3", "rjaw3", "tail", "headspike1", "headspike2", "hip1", "hip2", "hip3", "hip4", "hip5", "hip6", "hip7", "hip8"}) {
			OriginalModels.copy(root, g.part(name), name);
		}
		// OreSpawn draws its one leg eight times (see setupAnim), so the port has eight.
		for (int i = 0; i < 8; i++) {
			PartDefinition leg = root.addOrReplaceChild("leg" + i, CubeListBuilder.create(), PartPose.ZERO);
			for (String name : new String[] {"leg1p1", "upperknee2", "hipjoint", "leg1p2", "upperknee", "lowerknee2", "leg1p3", "foot", "footspike1", "footspike2", "footspike3", "footspike4", "anklespike1", "anklespike2", "anklespike3", "anklespike4", "legbump1", "legbump2", "lowerknee"}) {
				OriginalModels.copy(leg, g.part(name), name);
			}
		}
		return g.layer(mesh);
	}

	/** One segment's parts: turned to the leg's heading and its own pitch, at the joint. */
	private static void pose(ModelPart[] parts, float yRot, float xRot, float x, float y, float z) {
		for (ModelPart p : parts) {
			p.yRot = yRot;
			p.xRot = xRot;
			p.x = x;
			p.y = y;
			p.z = z;
		}
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		if (!(state instanceof LegRenderState legs) || legs.ymid == null) return;
		int counter = legs.counter;
		for (int i = 0; i < 8; i++) {
			float yr = legs.ydisplay[i];
			float x1 = (float) (legs.p1x[i] + legs.uddisplay[i]);
			float x2 = (float) (legs.p2x[i] + legs.uddisplay[i]);
			float x3 = (float) (legs.p3x[i] + legs.uddisplay[i]);
			float px = -((float) Math.cos(legs.ymid[i])) * legs.legoff[i] * 16.0F;
			float pz = (float) Math.sin(legs.ymid[i]) * legs.legoff[i] * 16.0F;
			float py = legs.yoff[i] * -16.0F;
			pose(upper[i], yr, x1, px, py, pz);
			float qy = py - (float) Math.sin(x1) * 99.0F;
			float qz = pz + (float) Math.cos(x1) * (float) Math.cos(yr) * 99.0F;
			float qx = px + (float) Math.cos(x1) * (float) Math.sin(yr) * 99.0F;
			pose(middle[i], yr, x2, qx, qy, qz);
			float ry = qy - (float) Math.sin(x2) * 99.0F;
			float rz = qz + (float) Math.cos(x2) * (float) Math.cos(yr) * 99.0F;
			float rx = qx + (float) Math.cos(x2) * (float) Math.sin(yr) * 99.0F;
			pose(lower[i], yr, x3, rx, ry, rz);
		}
		if (!state.attacking) {
			this.ljaw1.yRot = 0.0F;
			this.ljaw2.yRot = 0.75F;
			this.ljaw3.yRot = 1.71F;
			this.rjaw1.yRot = 0.0F;
			this.rjaw2.yRot = 2.3F;
			this.rjaw3.yRot = 1.41F;
		} else {
			float newangle = Mth.cos(counter * 0.25F) * (float) Math.PI * 0.22F;
			this.ljaw1.yRot = newangle;
			this.ljaw2.yRot = newangle + 0.75F;
			this.ljaw3.yRot = newangle + 1.71F;
			this.rjaw1.yRot = -newangle;
			this.rjaw2.yRot = 2.3F - newangle;
			this.rjaw3.yRot = 1.41F - newangle;
		}
	}
}
