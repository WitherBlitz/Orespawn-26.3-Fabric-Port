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
 * Ported from OreSpawn's ModelOstrich (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). OreSpawn's model read its entity
 * directly; here the renderer's extractor fills in: {@code state.value}, the horizontal distance it moved this tick (the
 * legs swing by it, {@code Ostrich.legSpeed}); {@code state.flag}, whether the wings flap this beat
 * ({@code Ostrich.wingsFlapping}, OreSpawn's RenderInfo.ri1); {@code state.yRot}, the head's yaw in degrees already worked
 * out by {@code Ostrich.headYaw} (half the look, or the smoothed turn while ridden, OreSpawn's RenderInfo.rf1); and
 * {@code state.sitting}, its sitting pose (head buried in the ground).
 */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class OstrichModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart Body1;
	private final ModelPart body2;
	private final ModelPart LLeg1;
	private final ModelPart Rleg1;
	private final ModelPart LLeg2;
	private final ModelPart Lfoot1;
	private final ModelPart RLeg2;
	private final ModelPart Lfoot2;
	private final ModelPart Lfoot3;
	private final ModelPart LClaw1;
	private final ModelPart LClaw2;
	private final ModelPart LClaw3;
	private final ModelPart Lfoot4;
	private final ModelPart LClaw4;
	private final ModelPart Rfoot1;
	private final ModelPart Rfoot2;
	private final ModelPart Rclaw1;
	private final ModelPart Rfoot3;
	private final ModelPart Rclaw3;
	private final ModelPart Rfoot4;
	private final ModelPart Rclaw2;
	private final ModelPart Rclaw4;
	private final ModelPart Body3;
	private final ModelPart Tail1;
	private final ModelPart Tail2;
	private final ModelPart Tail3;
	private final ModelPart Body4;
	private final ModelPart head;
	private final ModelPart leftleg;
	private final ModelPart Neck1;
	private final ModelPart Head1;
	private final ModelPart mouth1;
	private final ModelPart neck2;
	private final ModelPart rightleg;
	private final ModelPart Lwing;
	private final ModelPart Rwing;
	private final ModelPart Hat1;
	private final ModelPart Hat2;
	private final float wingspeed;

	public OstrichModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Body1 = root.getChild("body1");
		this.body2 = root.getChild("body2");
		this.LLeg1 = root.getChild("lleg1");
		this.Rleg1 = root.getChild("rleg1");
		this.LLeg2 = root.getChild("lleg2");
		this.Lfoot1 = root.getChild("lfoot1");
		this.RLeg2 = root.getChild("rleg2");
		this.Lfoot2 = root.getChild("lfoot2");
		this.Lfoot3 = root.getChild("lfoot3");
		this.LClaw1 = root.getChild("lclaw1");
		this.LClaw2 = root.getChild("lclaw2");
		this.LClaw3 = root.getChild("lclaw3");
		this.Lfoot4 = root.getChild("lfoot4");
		this.LClaw4 = root.getChild("lclaw4");
		this.Rfoot1 = root.getChild("rfoot1");
		this.Rfoot2 = root.getChild("rfoot2");
		this.Rclaw1 = root.getChild("rclaw1");
		this.Rfoot3 = root.getChild("rfoot3");
		this.Rclaw3 = root.getChild("rclaw3");
		this.Rfoot4 = root.getChild("rfoot4");
		this.Rclaw2 = root.getChild("rclaw2");
		this.Rclaw4 = root.getChild("rclaw4");
		this.Body3 = root.getChild("body3");
		this.Tail1 = root.getChild("tail1");
		this.Tail2 = root.getChild("tail2");
		this.Tail3 = root.getChild("tail3");
		this.Body4 = root.getChild("body4");
		this.head = root.getChild("head");
		this.leftleg = root.getChild("leftleg");
		this.Neck1 = root.getChild("neck1");
		this.Head1 = root.getChild("head1");
		this.mouth1 = root.getChild("mouth1");
		this.neck2 = root.getChild("neck2");
		this.rightleg = root.getChild("rightleg");
		this.Lwing = root.getChild("lwing");
		this.Rwing = root.getChild("rwing");
		this.Hat1 = root.getChild("hat1");
		this.Hat2 = root.getChild("hat2");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelOstrich");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f2 = state.ageInTicks, f3 = state.yRot;
		float lspeed = state.value;
		float newangle = Mth.cos(f2 * 1.25F * this.wingspeed) * (float) Math.PI * lspeed * 0.4F;
		if (newangle > 0.5) {
		newangle = 0.75F;
		}
		if (newangle < -0.5) {
		newangle = -0.75F;
		}
		this.leftleg.xRot = -0.297F + newangle;
		this.LLeg1.xRot = 0.483F + newangle;
		this.LLeg2.xRot = -0.437F + newangle;
		this.Lfoot1.xRot = newangle;
		this.Lfoot2.xRot = newangle;
		this.Lfoot3.xRot = newangle;
		this.Lfoot4.xRot = newangle;
		this.LClaw1.xRot = newangle;
		this.LClaw2.xRot = newangle;
		this.LClaw3.xRot = newangle;
		this.LClaw4.xRot = newangle;
		this.rightleg.xRot = -0.297F - newangle;
		this.Rleg1.xRot = 0.483F - newangle;
		this.RLeg2.xRot = -0.437F - newangle;
		this.Rfoot1.xRot = -newangle;
		this.Rfoot2.xRot = -newangle;
		this.Rfoot3.xRot = -newangle;
		this.Rfoot4.xRot = -newangle;
		this.Rclaw1.xRot = -newangle;
		this.Rclaw2.xRot = -newangle;
		this.Rclaw3.xRot = -newangle;
		this.Rclaw4.xRot = -newangle;
		this.Tail1.xRot = -0.594F + Mth.cos(f2 * 0.05F) * (float) Math.PI * 0.06F;
		this.Tail2.xRot = this.Tail1.xRot;
		this.Tail3.xRot = this.Tail1.xRot;
		this.Tail3.yRot = -0.334F + Mth.cos(f2 * 0.061F) * (float) Math.PI * 0.08F;
		this.Tail2.yRot = 0.334F - Mth.cos(f2 * 0.072F) * (float) Math.PI * 0.08F;
		// Sitting, it buries its head in the ground.
		if (state.sitting) {
		f3 = 0.0F;
		this.Head1.xRot = 3.1415F;
		} else {
		this.Head1.xRot = 0.0F;
		}
		this.head.xRot = this.Head1.xRot;
		this.mouth1.xRot = this.Head1.xRot;
		this.Neck1.xRot = this.Head1.xRot;
		this.Hat1.xRot = this.Head1.xRot;
		this.Hat2.xRot = this.Head1.xRot;
		this.Head1.yRot = (float)Math.toRadians(f3) * 0.65F;
		this.head.yRot = this.Head1.yRot;
		this.mouth1.yRot = this.Head1.yRot;
		this.Hat1.yRot = this.Head1.yRot;
		this.Hat2.yRot = this.Head1.yRot;
		newangle = Mth.cos(f2 * 1.0F * this.wingspeed) * (float) Math.PI * 0.15F;
		if (!state.flag) {
		newangle = 0.0F;
		}
		newangle = Math.abs(newangle);
		this.Lwing.zRot = -newangle;
		this.Lwing.yRot = newangle / 2.0F;
		this.Rwing.zRot = newangle;
		this.Rwing.yRot = -newangle / 2.0F;
		// The hats show only on activated "cannon fodder" ostriches (not ported yet).
		this.Hat1.visible = false;
		this.Hat2.visible = false;
	}
}
