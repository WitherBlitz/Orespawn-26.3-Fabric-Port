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
 * Ported from OreSpawn's ModelRotator: three rings, each one bar drawn eight times round a circle, spinning about
 * the x, y and z axes. OreSpawn turned them 2 degrees each frame it drew; here it's 6 degrees a tick (the same at 60
 * frames a second).
 */
@Environment(EnvType.CLIENT)
public class RotatorModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart ring1;
	private final ModelPart ring2;
	private final ModelPart ring3;

	public RotatorModel(ModelPart root, float wingspeed) {
		super(root);
		this.ring1 = root.getChild("ring1");
		this.ring2 = root.getChild("ring2");
		this.ring3 = root.getChild("ring3");
	}

	public static LayerDefinition createBodyLayer() {
		OriginalModels.Geometry g = OriginalModels.geometry("ModelRotator");
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		ring(root, "ring1", g.part("shape1"));
		ring(root, "ring2", g.part("shape2"));
		ring(root, "ring3", g.part("shape3"));
		return g.layer(mesh);
	}

	/** OreSpawn's bar drawn eight times around the centre, an eighth of a turn apart, at its own depth. */

	/** One of OreSpawn's Shape1-3, copied eight times at 45 degree steps round z, in a group that spins. */
	/** OreSpawn's bar drawn eight times around the centre, an eighth of a turn apart, at its own depth. */
	private static void ring(PartDefinition root, String name, OriginalModels.Part shape) {
		PartDefinition group = root.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
		for (int i = 0; i < 8; i++) {
			group.addOrReplaceChild("bar" + i, shape.cubes(), PartPose.offsetAndRotation(0.0F, 0.0F, shape.z, 0.0F, 0.0F, i * (float) (Math.PI / 4)));
		}
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float spin = (float) Math.toRadians((state.ageInTicks * 6.0F) % 360.0F);
		this.ring1.xRot = spin;
		this.ring2.yRot = spin;
		this.ring3.zRot = spin;
	}
}
