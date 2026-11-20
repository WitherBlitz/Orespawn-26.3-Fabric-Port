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
 * Ported from OreSpawn's ModelAntRobot (its parts are read from the original OreSpawn 1.7.10 file, see OriginalModels). OreSpawn drew one leg (three
 * 49-pixel segments and the foot) six times a frame; here each leg has its own copy (groups leg0 to leg5), posed from
 * the leg angles the robot works out each tick (orespawn.entity.robots.RobotLegs), and the jaws and antennae work
 * faster while it attacks.
 * <p>
 * Drawn as OreSpawn's own renderer drew it: with no 1.5-block drop (the renderer undoes it) and no death tip-over.
 */
@Environment(EnvType.CLIENT)
public class AntRobotModel extends EntityModel<OreSpawnRenderState> {
	private static final String[] UPPER = new String[] {"leg1"};
	private static final String[] MIDDLE = new String[] {"leg2"};
	private static final String[] LOWER = new String[] {"leg3", "foot1", "foot2", "foot3", "foot4", "foot5", "foot6", "foot7"};
	private final ModelPart ljaw1, ljaw2, rjaw1, rjaw2, lantenna, rantenna;
	private final ModelPart[][] upper = new ModelPart[6][], middle = new ModelPart[6][], lower = new ModelPart[6][];

	public AntRobotModel(ModelPart root, float wingspeed) {
		super(root);
		this.ljaw1 = root.getChild("ljaw1");
		this.ljaw2 = root.getChild("ljaw2");
		this.rjaw1 = root.getChild("rjaw1");
		this.rjaw2 = root.getChild("rjaw2");
		this.lantenna = root.getChild("lantenna");
		this.rantenna = root.getChild("rantenna");
		for (int i = 0; i < 6; i++) {
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
		OriginalModels.Geometry g = OriginalModels.geometry("ModelAntRobot");
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		for (String name : new String[] {"body", "abdomen", "head", "jet1", "jet2", "hip1", "hip2", "ljaw1", "rjaw1", "ljaw2", "rjaw2", "lantenna", "rantenna", "hip3", "hip4", "hip5", "hip6"}) {
			OriginalModels.copy(root, g.part(name), name);
		}
		// OreSpawn draws its one leg six times (see setupAnim), so the port has six.
		for (int i = 0; i < 6; i++) {
			PartDefinition leg = root.addOrReplaceChild("leg" + i, CubeListBuilder.create(), PartPose.ZERO);
			for (String name : new String[] {"leg1", "leg2", "leg3", "foot1", "foot2", "foot3", "foot4", "foot5", "foot6", "foot7"}) {
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
		for (int i = 0; i < 6; i++) {
			float yr = legs.ydisplay[i];
			float x1 = (float) (legs.p1x[i] + legs.uddisplay[i]);
			float x2 = (float) (legs.p2x[i] + legs.uddisplay[i]);
			float x3 = (float) (legs.p3x[i] + legs.uddisplay[i]);
			float px = -((float) Math.cos(legs.ymid[i])) * legs.legoff[i] * 16.0F;
			float pz = (float) Math.sin(legs.ymid[i]) * legs.legoff[i] * 16.0F;
			float py = legs.yoff[i] * -16.0F;
			pose(upper[i], yr, x1, px, py, pz);
			float qy = py - (float) Math.sin(x1) * 49.0F;
			float qz = pz + (float) Math.cos(x1) * (float) Math.cos(yr) * 49.0F;
			float qx = px + (float) Math.cos(x1) * (float) Math.sin(yr) * 49.0F;
			pose(middle[i], yr, x2, qx, qy, qz);
			float ry = qy - (float) Math.sin(x2) * 49.0F;
			float rz = qz + (float) Math.cos(x2) * (float) Math.cos(yr) * 49.0F;
			float rx = qx + (float) Math.cos(x2) * (float) Math.sin(yr) * 49.0F;
			pose(lower[i], yr, x3, rx, ry, rz);
		}
		if (!state.attacking) {
			this.ljaw1.yRot = 0.89F;
			this.ljaw2.yRot = 1.378F;
			this.rjaw1.yRot = 2.216F;
			this.rjaw2.yRot = 1.745F;
			this.lantenna.xRot = Mth.cos(counter * 0.35F) * (float) Math.PI * 0.05F;
			this.lantenna.zRot = 0.54F + Mth.cos(counter * 0.25F) * (float) Math.PI * 0.05F;
			this.rantenna.xRot = Mth.cos(counter * 0.3F) * (float) Math.PI * 0.05F;
			this.rantenna.zRot = -0.54F + Mth.cos(counter * 0.45F) * (float) Math.PI * 0.05F;
		} else {
			float newangle = Mth.cos(counter * 0.25F) * (float) Math.PI * 0.22F;
			this.ljaw1.yRot = newangle + 0.89F;
			this.ljaw2.yRot = newangle + 1.378F;
			this.rjaw1.yRot = -newangle + 2.216F;
			this.rjaw2.yRot = 1.745F - newangle;
			this.lantenna.xRot = Mth.cos(counter * 0.45F) * (float) Math.PI * 0.1F;
			this.lantenna.zRot = 0.54F + Mth.cos(counter * 0.35F) * (float) Math.PI * 0.1F;
			this.rantenna.xRot = Mth.cos(counter * 0.4F) * (float) Math.PI * 0.1F;
			this.rantenna.zRot = -0.54F + Mth.cos(counter * 0.55F) * (float) Math.PI * 0.1F;
		}
	}
}
