package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import orespawn.client.original.OriginalModels;

/**
 * OreSpawn's ModelChainsaw (its parts are read from the original OreSpawn 1.7.10 file, see OriginalModels): the Chainsaw as held in the hand.
 * Its state is an animation clock in seconds (only the Chainsaw moves). The chain's six teeth run up the
 * top of the blade and back along the bottom, and the wheel at its tip spins; OreSpawn moved them a step each frame,
 * here they move by time (30 steps and a turn a second).
 */
@Environment(EnvType.CLIENT)
public class ChainsawModel extends Model<Float> {
	private static final float[] START = {0.0F, 7.0F, 14.0F, 22.0F, 29.0F, 36.0F};
	private final ModelPart[] teeth = new ModelPart[6];
	private final ModelPart blade2;

	public ChainsawModel(ModelPart root) {
		super(root, RenderTypes::entityCutout);
		for (int i = 0; i < 6; i++) teeth[i] = root.getChild("tooth" + i);
		blade2 = root.getChild("blade2");
	}

	public static LayerDefinition createBodyLayer() {
		OriginalModels.Geometry g = OriginalModels.geometry("ModelChainsaw");
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		for (OriginalModels.Part p : g.parts().values()) {
			if (!p.name.equalsIgnoreCase("tooth")) OriginalModels.add(root, p);
		}
		// OreSpawn draws its one tooth at six points along the blade (see setupAnim).
		OriginalModels.Part tooth = g.part("tooth");
		for (int i = 0; i < 6; i++) OriginalModels.copy(root, tooth, "tooth" + i);
		return g.layer(mesh);
	}

	/** {@code seconds}: the animation clock. */
	@Override
	public void setupAnim(Float seconds) {
		super.setupAnim(seconds);
		float steps = seconds * 60.0F * 0.5F;
		for (int i = 0; i < 6; i++) {
			// OreSpawn's toothpos/toothdir pairs start 0, 7, 14 forward and 20, 13, 6 back: a 42-step loop.
			float s = (steps + START[i]) % 42.0F;
			ModelPart tooth = teeth[i];
			tooth.y = s < 21.0F ? -2.0F : 3.0F;
			tooth.z = -5.0F - (s < 21.0F ? s : 42.0F - s);
		}
		blade2.xRot = (seconds * Mth.TWO_PI) % Mth.TWO_PI;
	}
}
