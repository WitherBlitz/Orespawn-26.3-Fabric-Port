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
 * Ported from OreSpawn's ModelPeacock (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). The state's {@code blink} is
 * Peacock.showingOff(): the crest and the seven-feather tail fan stand open while it's true, and lie flat otherwise.
 */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class PeacockModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart lleg;
	private final ModelPart rleg;
	private final ModelPart body;
	private final ModelPart neck;
	private final ModelPart head1;
	private final ModelPart head2;
	private final ModelPart hf1;
	private final ModelPart hf2;
	private final ModelPart hf3;
	private final ModelPart tailf1;
	private final ModelPart tailf2;
	private final ModelPart tailf3;
	private final ModelPart tailf4;
	private final ModelPart tailf5;
	private final ModelPart tailf6;
	private final ModelPart tailf7;
	private final float wingspeed;

	public PeacockModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.lleg = root.getChild("lleg");
		this.rleg = root.getChild("rleg");
		this.body = root.getChild("body");
		this.neck = root.getChild("neck");
		this.head1 = root.getChild("head1");
		this.head2 = root.getChild("head2");
		this.hf1 = root.getChild("hf1");
		this.hf2 = root.getChild("hf2");
		this.hf3 = root.getChild("hf3");
		this.tailf1 = root.getChild("tailf1");
		this.tailf2 = root.getChild("tailf2");
		this.tailf3 = root.getChild("tailf3");
		this.tailf4 = root.getChild("tailf4");
		this.tailf5 = root.getChild("tailf5");
		this.tailf6 = root.getChild("tailf6");
		this.tailf7 = root.getChild("tailf7");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelPeacock");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float newangle = 0.0F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.15F * f1;
		} else {
		newangle = 0.0F;
		}
		this.lleg.xRot = newangle;
		this.rleg.xRot = -newangle;
		// Peacock.getBlink (showingOff here): the crest and tail fan open.
		if (state.blink) {
		this.hf1.xRot = 0.401F;
		this.hf2.xRot = -0.174F;
		this.hf3.xRot = -0.698F;
		this.tailf1.xRot = 1.047F;
		this.tailf2.xRot = 1.047F;
		this.tailf3.xRot = 1.047F;
		this.tailf4.xRot = 1.047F;
		this.tailf5.xRot = 1.047F;
		this.tailf6.xRot = 1.047F;
		this.tailf7.xRot = 1.047F;
		this.tailf1.zRot = -0.4F;
		this.tailf2.zRot = -0.8F;
		this.tailf3.zRot = -1.2F;
		this.tailf4.zRot = 0.4F;
		this.tailf5.zRot = 0.8F;
		this.tailf6.zRot = 1.2F;
		} else {
		this.hf1.xRot = -1.06F;
		this.hf2.xRot = -1.06F;
		this.hf3.xRot = -1.06F;
		this.tailf1.xRot = 0.0F;
		this.tailf2.xRot = 0.0F;
		this.tailf3.xRot = 0.0F;
		this.tailf4.xRot = 0.0F;
		this.tailf5.xRot = 0.0F;
		this.tailf6.xRot = 0.0F;
		this.tailf7.xRot = 0.0F;
		this.tailf1.zRot = 0.0F;
		this.tailf2.zRot = 0.0F;
		this.tailf3.zRot = 0.0F;
		this.tailf4.zRot = 0.0F;
		this.tailf5.zRot = 0.0F;
		this.tailf6.zRot = 0.0F;
		}
	}
}
