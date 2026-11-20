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
 * Ported from OreSpawn's ModelThePrince (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels): fluttering see-through wings
 * (drawn blended by the renderer's second pass), walking legs tucked up in flight, a swaying tail, three heads that
 * follow the look and stretch their necks in and out, snapping while attacking. Render state: {@code flag} flying
 * (activity 2), {@code sitting}, {@code attacking}, {@code variant} the three neck stretches (8 bits each, 0-60).
 */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class ThePrinceModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart neck1;
	private final ModelPart neck;
	private final ModelPart neckbase;
	private final ModelPart head;
	private final ModelPart Rleg1;
	private final ModelPart Lleg1;
	private final ModelPart snout;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart tail3;
	private final ModelPart tail4;
	private final ModelPart Lwing;
	private final ModelPart Rwing;
	private final ModelPart Tail5;
	private final ModelPart Tail6;
	private final ModelPart Lneck1;
	private final ModelPart Lneck;
	private final ModelPart Lhead;
	private final ModelPart Lsnout;
	private final ModelPart Rneck1;
	private final ModelPart Rneck;
	private final ModelPart Rhead;
	private final ModelPart Rsnout;
	private final ModelPart headfin;
	private final ModelPart Lheadfin;
	private final ModelPart Rheadfin;
	private final ModelPart Backfin;
	private final ModelPart Rwing2;
	private final ModelPart Rwing3;
	private final ModelPart Lwing2;
	private final ModelPart Lwing3;
	private final ModelPart Ljaw;
	private final ModelPart jaw;
	private final ModelPart Rjaw;
	private final float wingspeed;
	public static final int BODY = 0;
	public static final int BLEND = 1;
	public static final int BRIGHT = 2;
	private final int pass;

	public ThePrinceModel(ModelPart root, float wingspeed) {
		this(root, wingspeed, BODY);
	}

	/** @param pass {@link #BODY}, {@link #BLEND} (the see-through wings) or {@link #BRIGHT} (full-bright see-through parts). */
	public ThePrinceModel(ModelPart root, float wingspeed, int pass) {
		super(root);
		this.wingspeed = wingspeed;
		this.pass = pass;
		this.body = root.getChild("body");
		this.neck1 = root.getChild("neck1");
		this.neck = root.getChild("neck");
		this.neckbase = root.getChild("neckbase");
		this.head = root.getChild("head");
		this.Rleg1 = root.getChild("rleg1");
		this.Lleg1 = root.getChild("lleg1");
		this.snout = root.getChild("snout");
		this.tail1 = root.getChild("tail1");
		this.tail2 = root.getChild("tail2");
		this.tail3 = root.getChild("tail3");
		this.tail4 = root.getChild("tail4");
		this.Lwing = root.getChild("lwing");
		this.Rwing = root.getChild("rwing");
		this.Tail5 = root.getChild("tail5");
		this.Tail6 = root.getChild("tail6");
		this.Lneck1 = root.getChild("lneck1");
		this.Lneck = root.getChild("lneck");
		this.Lhead = root.getChild("lhead");
		this.Lsnout = root.getChild("lsnout");
		this.Rneck1 = root.getChild("rneck1");
		this.Rneck = root.getChild("rneck");
		this.Rhead = root.getChild("rhead");
		this.Rsnout = root.getChild("rsnout");
		this.headfin = root.getChild("headfin");
		this.Lheadfin = root.getChild("lheadfin");
		this.Rheadfin = root.getChild("rheadfin");
		this.Backfin = root.getChild("backfin");
		this.Rwing2 = root.getChild("rwing2");
		this.Rwing3 = root.getChild("rwing3");
		this.Lwing2 = root.getChild("lwing2");
		this.Lwing3 = root.getChild("lwing3");
		this.Ljaw = root.getChild("ljaw");
		this.jaw = root.getChild("jaw");
		this.Rjaw = root.getChild("rjaw");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelThePrince");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float hf = 0.0F;
		float newangle = 0.0F;
		int current_activity = state.flag ? 2 : 1;
		if (!(f1 > 0.1) && !state.attacking) {
		newangle = Mth.cos(f2 * 0.3F * this.wingspeed) * (float) Math.PI * 0.04F;
		} else {
		newangle = Mth.cos(f2 * 2.3F * this.wingspeed) * (float) Math.PI * 0.4F * f1;
		}
		this.Rwing.zRot = newangle - 0.4F;
		this.Rwing2.zRot = newangle - 0.6F;
		this.Rwing3.zRot = newangle - 0.2F;
		this.Lwing.zRot = -newangle + 0.4F;
		this.Lwing2.zRot = -newangle + 0.6F;
		this.Lwing3.zRot = -newangle + 0.2F;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 2.0F * this.wingspeed) * (float) Math.PI * 0.25F * f1;
		} else {
		newangle = 0.0F;
		}
		if (current_activity == 2 && !state.attacking) {
		newangle = -1.0F;
		this.Rleg1.xRot = newangle;
		this.Lleg1.xRot = newangle;
		} else {
		this.Rleg1.xRot = newangle;
		this.Lleg1.xRot = -newangle;
		}
		newangle = Mth.cos(f2 * 0.9F * this.wingspeed) * (float) Math.PI * 0.06F;
		if (state.sitting) {
		newangle = 0.0F;
		}
		if (state.attacking) {
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.12F;
		}
		this.tail2.yRot = newangle;
		this.tail3.z = this.tail2.z + (float)Math.cos(this.tail2.yRot) * 6.0F;
		this.tail3.x = this.tail2.x + (float)Math.sin(this.tail2.yRot) * 6.0F;
		this.tail3.yRot = newangle * 1.6F;
		this.tail4.z = this.tail3.z + (float)Math.cos(this.tail3.yRot) * 5.0F;
		this.tail4.x = this.tail3.x + (float)Math.sin(this.tail3.yRot) * 5.0F;
		this.tail4.yRot = newangle * 2.6F;
		this.Tail5.z = this.tail4.z + (float)Math.cos(this.tail4.yRot) * 4.0F;
		this.Tail5.x = this.tail4.x + (float)Math.sin(this.tail4.yRot) * 4.0F;
		this.Tail5.yRot = newangle * 3.6F;
		this.Tail6.z = this.Tail5.z + (float)Math.cos(this.Tail5.yRot) * 4.0F;
		this.Tail6.x = this.Tail5.x + (float)Math.sin(this.Tail5.yRot) * 4.0F;
		this.Tail6.yRot = newangle * 4.6F;
		float h3;
		float h1 = h3 = f3 * 2.0F / 3.0F;
		float d3;
		float d1 = d3 = f4 * 2.0F / 3.0F;
		float h2;
		float d2;
		if (h1 < 0.0F) {
		h2 = h3 = h1 / 2.0F;
		d2 = d3 = d1 / 2.0F;
		} else {
		h2 = h1 = h3 / 2.0F;
		d2 = d1 = d3 / 2.0F;
		}
		this.head.yRot = (float)Math.toRadians(h2);
		this.snout.yRot = (float)Math.toRadians(h2);
		this.headfin.yRot = (float)Math.toRadians(h2);
		this.jaw.yRot = (float)Math.toRadians(h2);
		this.jaw.z = this.snout.z - (float)Math.cos(this.snout.yRot);
		this.jaw.x = this.snout.x - (float)Math.sin(this.snout.yRot);
		this.neck.yRot = (float)Math.toRadians(h2) / 2.0F;
		this.Lhead.yRot = (float)Math.toRadians(h1);
		this.Lsnout.yRot = (float)Math.toRadians(h1);
		this.Lheadfin.yRot = (float)Math.toRadians(h1);
		this.Ljaw.yRot = (float)Math.toRadians(h1);
		this.Ljaw.z = this.Lsnout.z - (float)Math.cos(this.Lsnout.yRot);
		this.Ljaw.x = this.Lsnout.x - (float)Math.sin(this.Lsnout.yRot);
		this.Lneck.yRot = (float)Math.toRadians(h1) / 2.0F;
		this.Rhead.yRot = (float)Math.toRadians(h3);
		this.Rsnout.yRot = (float)Math.toRadians(h3);
		this.Rheadfin.yRot = (float)Math.toRadians(h3);
		this.Rjaw.yRot = (float)Math.toRadians(h3);
		this.Rjaw.z = this.Rsnout.z - (float)Math.cos(this.Rsnout.yRot);
		this.Rjaw.x = this.Rsnout.x - (float)Math.sin(this.Rsnout.yRot);
		this.Rneck.yRot = (float)Math.toRadians(h3) / 2.0F;
		float Rjx = 0.0F;
		float jx = 0.0F;
		float Ljx = 0.0F;
		if (state.attacking) {
		newangle = Mth.cos(f2 * 1.9F * this.wingspeed) * (float) Math.PI * 0.2F;
		Ljx = 0.2F + newangle;
		newangle = Mth.cos(f2 * 2.1F * this.wingspeed) * (float) Math.PI * 0.2F;
		Rjx = 0.2F + newangle;
		newangle = Mth.cos(f2 * 2.3F * this.wingspeed) * (float) Math.PI * 0.2F;
		jx = 0.2F + newangle;
		}
		this.head.xRot = (float)Math.toRadians(d2);
		this.snout.xRot = (float)Math.toRadians(d2);
		this.headfin.xRot = (float)Math.toRadians(d2);
		this.jaw.xRot = (float)Math.toRadians(d2) + jx;
		this.Lhead.xRot = (float)Math.toRadians(d1);
		this.Lsnout.xRot = (float)Math.toRadians(d1);
		this.Lheadfin.xRot = (float)Math.toRadians(d1);
		this.Ljaw.xRot = (float)Math.toRadians(d1) + Ljx;
		this.Rhead.xRot = (float)Math.toRadians(d3);
		this.Rsnout.xRot = (float)Math.toRadians(d3);
		this.Rheadfin.xRot = (float)Math.toRadians(d3);
		this.Rjaw.xRot = (float)Math.toRadians(d3) + Rjx;
		d1 = (state.variant & 0xFF);
		d2 = (state.variant >> 8 & 0xFF);
		d3 = (state.variant >> 16 & 0xFF);
		this.Lneck.xRot = (float)Math.toRadians(d1);
		this.neck.xRot = (float)Math.toRadians(d2);
		this.Rneck.xRot = (float)Math.toRadians(d3);
		this.Lhead.y = this.Lneck.y - (float)Math.cos(this.Lneck.xRot) * 7.0F;
		this.Lheadfin.y = this.Lsnout.y = this.Ljaw.y = this.Lhead.y;
		this.Lhead.z = this.Lneck.z - (float)Math.sin(this.Lneck.xRot) * 7.0F;
		this.Lheadfin.z = this.Lsnout.z = this.Ljaw.z = this.Lhead.z;
		this.Lhead.x = this.Lneck.x - (float)Math.sin(this.Lneck.yRot) * 7.0F * (float)Math.sin(this.Lneck.xRot);
		this.Lheadfin.x = this.Lsnout.x = this.Ljaw.x = this.Lhead.x;
		this.Rhead.y = this.Rneck.y - (float)Math.cos(this.Rneck.xRot) * 7.0F;
		this.Rheadfin.y = this.Rsnout.y = this.Rjaw.y = this.Rhead.y;
		this.Rhead.z = this.Rneck.z - (float)Math.sin(this.Rneck.xRot) * 7.0F;
		this.Rheadfin.z = this.Rsnout.z = this.Rjaw.z = this.Rhead.z;
		this.Rhead.x = this.Rneck.x - (float)Math.sin(this.Rneck.yRot) * 7.0F * (float)Math.sin(this.Rneck.xRot);
		this.Rheadfin.x = this.Rsnout.x = this.Rjaw.x = this.Rhead.x;
		this.head.y = this.neck.y - (float)Math.cos(this.neck.xRot) * 7.0F;
		this.headfin.y = this.snout.y = this.jaw.y = this.head.y;
		this.head.z = this.neck.z - (float)Math.sin(this.neck.xRot) * 7.0F;
		this.headfin.z = this.snout.z = this.jaw.z = this.head.z;
		this.head.x = this.neck.x - (float)Math.sin(this.neck.yRot) * 7.0F * (float)Math.sin(this.neck.xRot);
		this.headfin.x = this.snout.x = this.jaw.x = this.head.x;
		// render(): the wings blended (colour 0.75, alpha 0.55); each copy of the model shows only its own pass.
		this.allParts().forEach(part -> part.visible = pass == BODY);
		this.root().visible = true;
		for (ModelPart part : new ModelPart[] {Rwing, Rwing2, Rwing3, Lwing, Lwing2, Lwing3}) part.visible = pass == BLEND;
	}
}
