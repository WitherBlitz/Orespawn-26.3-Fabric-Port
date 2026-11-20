package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelRobot5 (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class Robot5Model extends EntityModel<OreSpawnRenderState> {
	private final ModelPart lwheel1;
	private final ModelPart lwheel2;
	private final ModelPart rwheel1;
	private final ModelPart rwheel2;
	private final ModelPart axle;
	private final ModelPart drivebox;
	private final ModelPart stand;
	private final ModelPart swivel;
	private final ModelPart barrel1;
	private final ModelPart barrel2;
	private final ModelPart ammobox;
	private final float wingspeed;

	public Robot5Model(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.lwheel1 = root.getChild("lwheel1");
		this.lwheel2 = root.getChild("lwheel2");
		this.rwheel1 = root.getChild("rwheel1");
		this.rwheel2 = root.getChild("rwheel2");
		this.axle = root.getChild("axle");
		this.drivebox = root.getChild("drivebox");
		this.stand = root.getChild("stand");
		this.swivel = root.getChild("swivel");
		this.barrel1 = root.getChild("barrel1");
		this.barrel2 = root.getChild("barrel2");
		this.ammobox = root.getChild("ammobox");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelRobot5");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// From render().
		float newangle;
		if (f1 > 0.1) {
			newangle = f2 * 0.15F % (float) (Math.PI * 2);
			newangle = Math.abs(newangle);
		} else {
			newangle = 0.0F;
		}
		this.lwheel1.xRot = newangle;
		this.lwheel2.xRot = (float) (newangle + (Math.PI / 4));
		this.rwheel1.xRot = newangle;
		this.rwheel2.xRot = (float) (newangle + (Math.PI / 4));
		this.barrel1.yRot = this.barrel2.yRot = this.ammobox.yRot = (float) Math.toRadians(f3 / 2.0);
	}
}
