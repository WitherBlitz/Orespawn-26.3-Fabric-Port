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

/**
 * Ported from OreSpawn's ModelPurplePower: three bars (4, 8 and 14 long), each drawn six times at 60 degree steps
 * round its long axis, so each makes a six-pointed burst; each burst is turned a random way every frame (about x, then
 * y, then z, the turns adding up as OreSpawn's GL rotations did), so the bolt flickers. 1.7.10 drew one bar six times;
 * here each copy is its own part. The see-through colour (0.75 grey, alpha 0.55) and full brightness are the
 * renderer's (orespawn.client.content.PurplePowerRenderer).
 */
@Environment(EnvType.CLIENT)
public class PurplePowerModel extends EntityModel<OreSpawnRenderState> {
	private static final int COPIES = 6;
	private final ModelPart burst1;
	private final ModelPart burst2;
	private final ModelPart burst3;

	public PurplePowerModel(ModelPart root, float wingspeed) {
		super(root);
		this.burst1 = root.getChild("burst1");
		this.burst2 = root.getChild("burst2");
		this.burst3 = root.getChild("burst3");
	}

	public static LayerDefinition createBodyLayer() {
		OriginalModels.Geometry g = OriginalModels.geometry("ModelPurplePower");
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		burst(root, "burst1", g.part("shape1"));
		burst(root, "burst2", g.part("shape2"));
		burst(root, "burst3", g.part("shape3"));
		return g.layer(mesh);
	}

	/** OreSpawn's bar drawn COPIES times around the centre, a sixth of a turn apart, mirrored. */

	/** OreSpawn's bar drawn COPIES times around the centre, a sixth of a turn apart, mirrored. */
	private static void burst(PartDefinition root, String name, OriginalModels.Part shape) {
		OriginalModels.Box b = shape.boxes.getFirst();
		PartDefinition group = root.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
		for (int i = 0; i < COPIES; i++) {
			group.addOrReplaceChild(name + "_" + i, CubeListBuilder.create().texOffs(b.u(), b.v()).mirror(true).addBox(b.x(), b.y(), b.z(), b.width(),
				b.height(), b.depth()), PartPose.rotation(0.0F, 0.0F, (float) (Math.PI / 3) * i));
		}
	}

	/** A different random-looking angle (0 to 2 pi) every frame, standing in for OreSpawn's world.rand.nextFloat() * 360. */
	private static float jitter(float t, float salt) {
		float v = Mth.sin(t * 12.9898F + salt * 78.233F) * 43758.547F;
		return (v - Mth.floor(v)) * Mth.TWO_PI;
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float a = jitter(t, 1.0F);
		float b = jitter(t, 2.0F);
		float c = jitter(t, 3.0F);
		burst1.xRot = a;
		burst2.xRot = 2.0F * a;
		burst2.yRot = b;
		burst3.xRot = 2.0F * a;
		burst3.yRot = 2.0F * b;
		burst3.zRot = c;
	}
}
