package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelAnt (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class AntModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart thorax;
	private final ModelPart thorax1;
	private final ModelPart thorax3;
	private final ModelPart abdomen;
	private final ModelPart abdomen1;
	private final ModelPart head;
	private final ModelPart jawsr;
	private final ModelPart jawsl;
	private final ModelPart llegtop1;
	private final ModelPart llegbot1;
	private final ModelPart llegtop2;
	private final ModelPart llegbot2;
	private final ModelPart llegtop3;
	private final ModelPart llegbot3;
	private final ModelPart rlegtop1;
	private final ModelPart rlegbot1;
	private final ModelPart rlegtop2;
	private final ModelPart rlegbot2;
	private final ModelPart rlegtop3;
	private final ModelPart rlegbot3;

	public AntModel(ModelPart root) {
		super(root);
		this.thorax = root.getChild("thorax");
		this.thorax1 = root.getChild("thorax1");
		this.thorax3 = root.getChild("thorax3");
		this.abdomen = root.getChild("abdomen");
		this.abdomen1 = root.getChild("abdomen1");
		this.head = root.getChild("head");
		this.jawsr = root.getChild("jawsr");
		this.jawsl = root.getChild("jawsl");
		this.llegtop1 = root.getChild("llegtop1");
		this.llegbot1 = root.getChild("llegbot1");
		this.llegtop2 = root.getChild("llegtop2");
		this.llegbot2 = root.getChild("llegbot2");
		this.llegtop3 = root.getChild("llegtop3");
		this.llegbot3 = root.getChild("llegbot3");
		this.rlegtop1 = root.getChild("rlegtop1");
		this.rlegbot1 = root.getChild("rlegbot1");
		this.rlegtop2 = root.getChild("rlegtop2");
		this.rlegbot2 = root.getChild("rlegbot2");
		this.rlegtop3 = root.getChild("rlegtop3");
		this.rlegbot3 = root.getChild("rlegbot3");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelAnt");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		this.llegtop1.xRot = Mth.cos(f2 * 2.7F) * (float) Math.PI * 0.45F * f1;
		this.llegbot1.xRot = this.llegtop1.xRot;
		this.rlegtop2.xRot = this.llegtop1.xRot;
		this.rlegbot2.xRot = this.llegtop1.xRot;
		this.rlegtop3.xRot = this.llegtop1.xRot;
		this.rlegbot3.xRot = this.llegtop1.xRot;
		this.rlegtop1.xRot = -this.llegtop1.xRot;
		this.rlegbot1.xRot = -this.llegtop1.xRot;
		this.llegtop2.xRot = -this.llegtop1.xRot;
		this.llegbot2.xRot = -this.llegtop1.xRot;
		this.llegtop3.xRot = -this.llegtop1.xRot;
		this.llegbot3.xRot = -this.llegtop1.xRot;
		this.jawsl.yRot = Mth.cos(f2 * 0.4F) * (float) Math.PI * 0.05F;
		this.jawsr.yRot = -this.jawsl.yRot;
	}
}
