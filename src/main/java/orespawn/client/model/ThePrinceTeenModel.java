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
 * Ported from OreSpawn's ModelThePrinceTeen (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels): beating wings with
 * see-through membranes (drawn blended by the renderer's second pass), legs and claws, a swaying tail, three stretching
 * necks, leaning into turns while flying. Render state: {@code flag} flying (activity 1), {@code sitting},
 * {@code attacking}, {@code variant} the three neck stretches (8 bits each, 0-60), {@code value} the smoothed turn
 * (OreSpawn's RenderInfo.rf1, kept by the entity).
 */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class ThePrinceTeenModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart body;
	private final ModelPart leftleg1;
	private final ModelPart tail1;
	private final ModelPart leftleg2;
	private final ModelPart body2;
	private final ModelPart leftleg3;
	private final ModelPart tail2;
	private final ModelPart tail3;
	private final ModelPart lclaw2;
	private final ModelPart lclaw4;
	private final ModelPart lclaw5;
	private final ModelPart lclaw6;
	private final ModelPart lclaw7;
	private final ModelPart tail4;
	private final ModelPart tail5;
	private final ModelPart neck1;
	private final ModelPart neck3;
	private final ModelPart head3;
	private final ModelPart jaw1;
	private final ModelPart jaw5;
	private final ModelPart head7;
	private final ModelPart rightleg1;
	private final ModelPart rightleg2;
	private final ModelPart rightleg3;
	private final ModelPart rclaw2;
	private final ModelPart rclaw4;
	private final ModelPart rclaw5;
	private final ModelPart rclaw7;
	private final ModelPart rclaw6;
	private final ModelPart wing1;
	private final ModelPart wing2;
	private final ModelPart mem1;
	private final ModelPart mem2;
	private final ModelPart lshoulder;
	private final ModelPart rshoulder;
	private final ModelPart rwing1;
	private final ModelPart rmem1;
	private final ModelPart rwing2;
	private final ModelPart rmem2;
	private final ModelPart neck4;
	private final ModelPart neck5;
	private final ModelPart wing3;
	private final ModelPart mem3;
	private final ModelPart rwing3;
	private final ModelPart rmem3;
	private final ModelPart wing4;
	private final ModelPart mem4;
	private final ModelPart rwing4;
	private final ModelPart rmem4;
	private final ModelPart Tailspike1;
	private final ModelPart Tailspike2;
	private final ModelPart Tailspike3;
	private final ModelPart headfin;
	private final ModelPart backfin1;
	private final ModelPart backfin2;
	private final ModelPart neck3L;
	private final ModelPart neck4L;
	private final ModelPart neck3R;
	private final ModelPart neck4R;
	private final ModelPart neck5L;
	private final ModelPart neck5R;
	private final ModelPart jaw5L;
	private final ModelPart jaw5R;
	private final ModelPart head7L;
	private final ModelPart headfinL;
	private final ModelPart headfinR;
	private final ModelPart head7R;
	private final ModelPart jaw1L;
	private final ModelPart jaw1R;
	private final ModelPart head3L;
	private final ModelPart head3R;
	private final float wingspeed;
	public static final int BODY = 0;
	public static final int BLEND = 1;
	public static final int BRIGHT = 2;
	private final int pass;

	public ThePrinceTeenModel(ModelPart root, float wingspeed) {
		this(root, wingspeed, BODY);
	}

	/** @param pass {@link #BODY}, {@link #BLEND} (the see-through wings) or {@link #BRIGHT} (full-bright see-through parts). */
	public ThePrinceTeenModel(ModelPart root, float wingspeed, int pass) {
		super(root);
		this.wingspeed = wingspeed;
		this.pass = pass;
		this.body = root.getChild("body");
		this.leftleg1 = root.getChild("leftleg1");
		this.tail1 = root.getChild("tail1");
		this.leftleg2 = root.getChild("leftleg2");
		this.body2 = root.getChild("body2");
		this.leftleg3 = root.getChild("leftleg3");
		this.tail2 = root.getChild("tail2");
		this.tail3 = root.getChild("tail3");
		this.lclaw2 = root.getChild("lclaw2");
		this.lclaw4 = root.getChild("lclaw4");
		this.lclaw5 = root.getChild("lclaw5");
		this.lclaw6 = root.getChild("lclaw6");
		this.lclaw7 = root.getChild("lclaw7");
		this.tail4 = root.getChild("tail4");
		this.tail5 = root.getChild("tail5");
		this.neck1 = root.getChild("neck1");
		this.neck3 = root.getChild("neck3");
		this.head3 = root.getChild("head3");
		this.jaw1 = root.getChild("jaw1");
		this.jaw5 = root.getChild("jaw5");
		this.head7 = root.getChild("head7");
		this.rightleg1 = root.getChild("rightleg1");
		this.rightleg2 = root.getChild("rightleg2");
		this.rightleg3 = root.getChild("rightleg3");
		this.rclaw2 = root.getChild("rclaw2");
		this.rclaw4 = root.getChild("rclaw4");
		this.rclaw5 = root.getChild("rclaw5");
		this.rclaw7 = root.getChild("rclaw7");
		this.rclaw6 = root.getChild("rclaw6");
		this.wing1 = root.getChild("wing1");
		this.wing2 = root.getChild("wing2");
		this.mem1 = root.getChild("mem1");
		this.mem2 = root.getChild("mem2");
		this.lshoulder = root.getChild("lshoulder");
		this.rshoulder = root.getChild("rshoulder");
		this.rwing1 = root.getChild("rwing1");
		this.rmem1 = root.getChild("rmem1");
		this.rwing2 = root.getChild("rwing2");
		this.rmem2 = root.getChild("rmem2");
		this.neck4 = root.getChild("neck4");
		this.neck5 = root.getChild("neck5");
		this.wing3 = root.getChild("wing3");
		this.mem3 = root.getChild("mem3");
		this.rwing3 = root.getChild("rwing3");
		this.rmem3 = root.getChild("rmem3");
		this.wing4 = root.getChild("wing4");
		this.mem4 = root.getChild("mem4");
		this.rwing4 = root.getChild("rwing4");
		this.rmem4 = root.getChild("rmem4");
		this.Tailspike1 = root.getChild("tailspike1");
		this.Tailspike2 = root.getChild("tailspike2");
		this.Tailspike3 = root.getChild("tailspike3");
		this.headfin = root.getChild("headfin");
		this.backfin1 = root.getChild("backfin1");
		this.backfin2 = root.getChild("backfin2");
		this.neck3L = root.getChild("neck3l");
		this.neck4L = root.getChild("neck4l");
		this.neck3R = root.getChild("neck3r");
		this.neck4R = root.getChild("neck4r");
		this.neck5L = root.getChild("neck5l");
		this.neck5R = root.getChild("neck5r");
		this.jaw5L = root.getChild("jaw5l");
		this.jaw5R = root.getChild("jaw5r");
		this.head7L = root.getChild("head7l");
		this.headfinL = root.getChild("headfinl");
		this.headfinR = root.getChild("headfinr");
		this.head7R = root.getChild("head7r");
		this.jaw1L = root.getChild("jaw1l");
		this.jaw1R = root.getChild("jaw1r");
		this.head3L = root.getChild("head3l");
		this.head3R = root.getChild("head3r");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelThePrinceTeen");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float hf = 0.0F;
		float newangle = 0.0F;
		float newangle2 = 0.0F;
		float rnewangle = 0.0F;
		float rnewangle2 = 0.0F;
		float clawangle = 0.0F;
		float tailspeed = 0.26F;
		float tailamp = 0.08F;
		float pi4 = (float) (Math.PI / 4);
		int current_activity = state.flag ? 1 : 0;
		if (f1 > 0.1 && current_activity == 0) {
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.2F * f1;
		} else {
		newangle = Mth.cos(f2 * 0.3F * this.wingspeed) * (float) Math.PI * 0.04F;
		}
		if (current_activity == 1) {
		newangle = Mth.cos(f2 * 1.4F * this.wingspeed) * (float) Math.PI * 0.4F;
		}
		if (state.attacking) {
		newangle = Mth.cos(f2 * 1.7F * this.wingspeed) * (float) Math.PI * 0.4F;
		}
		this.wing1.zRot = newangle - 0.4F;
		this.wing2.zRot = newangle * 1.25F - 0.4F;
		this.wing3.zRot = newangle - 0.6F;
		this.wing4.zRot = newangle - 0.2F;
		this.wing2.y = this.wing1.y + (float)Math.sin(this.wing1.zRot) * 22.0F;
		this.wing2.x = this.wing1.x + (float)Math.cos(this.wing1.zRot) * 22.0F;
		this.mem1.zRot = this.wing1.zRot;
		this.mem2.zRot = this.wing2.zRot;
		this.mem3.zRot = this.wing3.zRot;
		this.mem4.zRot = this.wing4.zRot;
		this.mem2.y = this.wing2.y;
		this.mem2.x = this.wing2.x;
		this.rwing1.zRot = -newangle + 0.4F;
		this.rwing2.zRot = -newangle * 1.25F + 0.4F;
		this.rwing3.zRot = -newangle + 0.6F;
		this.rwing4.zRot = -newangle + 0.2F;
		this.rwing2.y = this.rwing1.y - (float)Math.sin(this.rwing1.zRot) * 22.0F;
		this.rwing2.x = this.rwing1.x - (float)Math.cos(this.rwing1.zRot) * 22.0F;
		this.rmem1.zRot = this.rwing1.zRot;
		this.rmem2.zRot = this.rwing2.zRot;
		this.rmem3.zRot = this.rwing3.zRot;
		this.rmem4.zRot = this.rwing4.zRot;
		this.rmem2.y = this.rwing2.y;
		this.rmem2.x = this.rwing2.x;
		if (f1 > 0.1) {
		newangle = Mth.cos(f2 * 0.55F * this.wingspeed) * (float) Math.PI * 0.25F * f1;
		newangle2 = Mth.cos((float)(f2 * 0.55F * this.wingspeed + (Math.PI / 2))) * (float) Math.PI * 0.25F * f1;
		rnewangle = newangle;
		rnewangle2 = newangle2;
		clawangle = 0.0F;
		} else {
		newangle = 0.0F;
		newangle2 = 0.0F;
		rnewangle = 0.0F;
		rnewangle2 = 0.0F;
		clawangle = 0.0F;
		}
		if (state.attacking) {
		newangle = Mth.cos(f2 * this.wingspeed) * (float) Math.PI * 0.25F;
		newangle2 = Mth.cos((float)(f2 * this.wingspeed + (Math.PI / 2))) * (float) Math.PI * 0.25F;
		rnewangle = newangle;
		rnewangle2 = newangle2;
		clawangle = 0.0F;
		}
		if (current_activity == 1 && !state.attacking) {
		newangle = -0.5F;
		newangle2 = -1.25F;
		rnewangle = 0.5F;
		rnewangle2 = 1.25F;
		}
		if (current_activity == 1) {
		clawangle = -0.685F;
		}
		this.leftleg1.xRot = newangle - 0.575F;
		this.leftleg2.xRot = newangle + 0.977F;
		this.leftleg3.xRot = newangle2 - 0.523F;
		this.leftleg3.y = this.leftleg2.y + (float)Math.cos(this.leftleg2.xRot) * 14.0F + 6.0F;
		this.leftleg3.z = this.leftleg2.z + (float)Math.sin(this.leftleg2.xRot) * 14.0F;
		this.lclaw2.y = this.leftleg3.y + (float)Math.cos(this.leftleg3.xRot) * 17.0F;
		this.lclaw2.z = this.leftleg3.z + (float)Math.sin(this.leftleg3.xRot) * 17.0F - 1.0F;
		this.lclaw4.y = this.lclaw2.y;
		this.lclaw4.z = this.lclaw2.z;
		this.lclaw5.y = this.lclaw2.y;
		this.lclaw5.z = this.lclaw2.z;
		this.lclaw6.y = this.lclaw2.y;
		this.lclaw6.z = this.lclaw2.z;
		this.lclaw7.y = this.lclaw2.y;
		this.lclaw7.z = this.lclaw2.z;
		this.lclaw2.xRot = clawangle;
		this.lclaw4.xRot = clawangle;
		this.lclaw5.xRot = clawangle;
		this.lclaw6.xRot = clawangle;
		this.lclaw7.xRot = clawangle;
		this.rightleg1.xRot = -rnewangle - 0.575F;
		this.rightleg2.xRot = -rnewangle + 0.977F;
		this.rightleg3.xRot = -rnewangle2 - 0.523F;
		this.rightleg3.y = this.rightleg2.y + (float)Math.cos(this.rightleg2.xRot) * 14.0F + 5.0F;
		this.rightleg3.z = this.rightleg2.z + (float)Math.sin(this.rightleg2.xRot) * 14.0F;
		this.rclaw2.y = this.rightleg3.y + (float)Math.cos(this.rightleg3.xRot) * 17.0F;
		this.rclaw2.z = this.rightleg3.z + (float)Math.sin(this.rightleg3.xRot) * 17.0F - 1.0F;
		this.rclaw4.y = this.rclaw2.y;
		this.rclaw4.z = this.rclaw2.z;
		this.rclaw5.y = this.rclaw2.y;
		this.rclaw5.z = this.rclaw2.z;
		this.rclaw6.y = this.rclaw2.y;
		this.rclaw6.z = this.rclaw2.z;
		this.rclaw7.y = this.rclaw2.y;
		this.rclaw7.z = this.rclaw2.z;
		this.rclaw2.xRot = clawangle;
		this.rclaw4.xRot = clawangle;
		this.rclaw5.xRot = clawangle;
		this.rclaw6.xRot = clawangle;
		this.rclaw7.xRot = clawangle;
		if (state.attacking) {
		tailspeed = 0.56F;
		tailamp = 0.19F;
		}
		if (state.sitting) {
		tailamp = 0.0F;
		}
		this.tail1.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * (float) Math.PI * tailamp / 4.0F;
		this.tail2.z = this.tail1.z + (float)Math.cos(this.tail1.yRot) * 11.0F;
		this.tail2.x = this.tail1.x + (float)Math.sin(this.tail1.yRot) * 11.0F;
		this.tail2.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - pi4) * (float) Math.PI * tailamp;
		this.tail3.z = this.tail2.z + (float)Math.cos(this.tail2.yRot) * 9.0F;
		this.tail3.x = this.tail2.x + (float)Math.sin(this.tail2.yRot) * 9.0F;
		this.tail3.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - 2.0F * pi4) * (float) Math.PI * tailamp;
		this.tail4.z = this.tail3.z + (float)Math.cos(this.tail3.yRot) * 9.0F;
		this.tail4.x = this.tail3.x + (float)Math.sin(this.tail3.yRot) * 9.0F;
		this.tail4.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - 3.0F * pi4) * (float) Math.PI * tailamp;
		newangle = Mth.cos(f2 * tailspeed * this.wingspeed - 3.0F * pi4) * (float) Math.PI * tailamp;
		newangle /= 2.0F;
		this.tail5.z = this.tail4.z + (float)Math.cos(this.tail4.yRot) * 9.0F;
		this.tail5.x = this.tail4.x + (float)Math.sin(this.tail4.yRot) * 9.0F;
		this.tail5.yRot = this.tail4.yRot + newangle;
		this.Tailspike1.z = this.tail5.z + (float)Math.cos(this.tail5.yRot) * 9.0F;
		this.Tailspike1.x = this.tail5.x + (float)Math.sin(this.tail5.yRot) * 9.0F;
		this.Tailspike2.z = this.tail5.z + (float)Math.cos(this.tail5.yRot) * 15.0F;
		this.Tailspike2.x = this.tail5.x + (float)Math.sin(this.tail5.yRot) * 15.0F;
		this.Tailspike1.yRot = this.Tailspike2.yRot = this.tail5.yRot + newangle * 2.0F / 3.0F;
		this.Tailspike3.z = this.Tailspike1.z + (float)Math.cos(this.Tailspike1.yRot) * 11.0F;
		this.Tailspike3.x = this.Tailspike1.x + (float)Math.sin(this.Tailspike1.yRot) * 11.0F;
		this.Tailspike3.yRot = this.Tailspike1.yRot + newangle * 3.0F / 2.0F;
		if (state.flag) {
		f3 = state.value;
		f3 = -f3;
		}
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
		this.head7.yRot = (float)Math.toRadians(h2);
		this.head3.yRot = (float)Math.toRadians(h2);
		this.headfin.yRot = (float)Math.toRadians(h2);
		this.jaw5.yRot = (float)Math.toRadians(h2);
		this.jaw1.yRot = (float)Math.toRadians(h2);
		this.neck3.yRot = (float)Math.toRadians(h2) / 8.0F;
		this.neck4.yRot = (float)Math.toRadians(h2) / 4.0F;
		this.neck5.yRot = (float)Math.toRadians(h2) / 2.0F;
		this.head7L.yRot = (float)Math.toRadians(h1);
		this.head3L.yRot = (float)Math.toRadians(h1);
		this.headfinL.yRot = (float)Math.toRadians(h1);
		this.jaw5L.yRot = (float)Math.toRadians(h1);
		this.jaw1L.yRot = (float)Math.toRadians(h1);
		this.neck3L.yRot = (float)Math.toRadians(h1) / 8.0F;
		this.neck4L.yRot = (float)Math.toRadians(h1) / 4.0F;
		this.neck5L.yRot = (float)Math.toRadians(h1) / 2.0F;
		this.head7R.yRot = (float)Math.toRadians(h3);
		this.head3R.yRot = (float)Math.toRadians(h3);
		this.headfinR.yRot = (float)Math.toRadians(h3);
		this.jaw5R.yRot = (float)Math.toRadians(h3);
		this.jaw1R.yRot = (float)Math.toRadians(h3);
		this.neck3R.yRot = (float)Math.toRadians(h3) / 8.0F;
		this.neck4R.yRot = (float)Math.toRadians(h3) / 4.0F;
		this.neck5R.yRot = (float)Math.toRadians(h3) / 2.0F;
		float Rjx = 0.0F;
		float jx = 0.0F;
		float Ljx = 0.0F;
		if (state.attacking) {
		newangle = Mth.cos(f2 * 0.9F * this.wingspeed) * (float) Math.PI * 0.1F;
		Ljx = 0.25F + newangle;
		newangle = Mth.cos(f2 * 1.1F * this.wingspeed) * (float) Math.PI * 0.1F;
		Rjx = 0.25F + newangle;
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.1F;
		jx = 0.25F + newangle;
		} else {
		newangle = Mth.cos(f2 * 0.25F * this.wingspeed) * (float) Math.PI * 0.02F;
		Ljx = 0.1F + newangle;
		newangle = Mth.cos(f2 * 0.3F * this.wingspeed) * (float) Math.PI * 0.02F;
		Rjx = 0.1F + newangle;
		newangle = Mth.cos(f2 * 0.35F * this.wingspeed) * (float) Math.PI * 0.02F;
		jx = 0.1F + newangle;
		}
		this.head7.xRot = (float)Math.toRadians(d2);
		this.head3.xRot = (float)Math.toRadians(d2);
		this.headfin.xRot = (float)Math.toRadians(d2) + 0.5F;
		this.jaw5.xRot = (float)Math.toRadians(d2) + jx;
		this.jaw1.xRot = (float)Math.toRadians(d2) + jx;
		this.head7L.xRot = (float)Math.toRadians(d1);
		this.head3L.xRot = (float)Math.toRadians(d1);
		this.headfinL.xRot = (float)Math.toRadians(d1) + 0.5F;
		this.jaw5L.xRot = (float)Math.toRadians(d1) + Ljx;
		this.jaw1L.xRot = (float)Math.toRadians(d1) + Ljx;
		this.head7R.xRot = (float)Math.toRadians(d3);
		this.head3R.xRot = (float)Math.toRadians(d3);
		this.headfinR.xRot = (float)Math.toRadians(d3) + 0.5F;
		this.jaw5R.xRot = (float)Math.toRadians(d3) + Rjx;
		this.jaw1R.xRot = (float)Math.toRadians(d3) + Rjx;
		d1 = (state.variant & 0xFF);
		d2 = (state.variant >> 8 & 0xFF);
		d3 = (state.variant >> 16 & 0xFF);
		this.neck3L.xRot = -((float)Math.toRadians(d1 / 3.0));
		this.neck4L.xRot = -((float)Math.toRadians(d1 * 2.0 / 3.0));
		this.neck5L.xRot = -((float)Math.toRadians(d1));
		this.neck3.xRot = -((float)Math.toRadians(d2 / 3.0));
		this.neck4.xRot = -((float)Math.toRadians(d2 * 2.0 / 3.0));
		this.neck5.xRot = -((float)Math.toRadians(d2));
		this.neck3R.xRot = -((float)Math.toRadians(d3 / 3.0));
		this.neck4R.xRot = -((float)Math.toRadians(d3 * 2.0 / 3.0));
		this.neck5R.xRot = -((float)Math.toRadians(d3));
		this.neck4.y = this.neck3.y + (float)Math.sin(this.neck3.xRot) * 9.0F;
		this.neck4.z = this.neck3.z - (float)Math.cos(this.neck3.xRot) * 9.0F;
		this.neck4.x = this.neck3.x - (float)Math.sin(this.neck3.yRot) * 9.0F * (float)Math.cos(this.neck3.xRot);
		this.neck5.y = this.neck4.y + (float)Math.sin(this.neck4.xRot) * 9.0F;
		this.neck5.z = this.neck4.z - (float)Math.cos(this.neck4.xRot) * 9.0F;
		this.neck5.x = this.neck4.x - (float)Math.sin(this.neck4.yRot) * 9.0F * (float)Math.cos(this.neck4.xRot);
		this.head7.y = this.neck5.y + (float)Math.sin(this.neck5.xRot) * 9.0F;
		this.headfin.y = this.jaw5.y = this.jaw1.y = this.head3.y = this.head7.y;
		this.head7.z = this.neck5.z - (float)Math.cos(this.neck5.xRot) * 9.0F;
		this.headfin.z = this.jaw5.z = this.jaw1.z = this.head3.z = this.head7.z;
		this.head7.x = this.neck5.x - (float)Math.sin(this.neck5.yRot) * 9.0F * (float)Math.cos(this.neck5.xRot);
		this.headfin.x = this.jaw5.x = this.jaw1.x = this.head3.x = this.head7.x;
		this.neck4L.y = this.neck3L.y + (float)Math.sin(this.neck3L.xRot) * 9.0F;
		this.neck4L.z = this.neck3L.z - (float)Math.cos(this.neck3L.xRot) * 9.0F;
		this.neck4L.x = this.neck3L.x - (float)Math.sin(this.neck3L.yRot) * 9.0F * (float)Math.cos(this.neck3L.xRot);
		this.neck5L.y = this.neck4L.y + (float)Math.sin(this.neck4L.xRot) * 9.0F;
		this.neck5L.z = this.neck4L.z - (float)Math.cos(this.neck4L.xRot) * 9.0F;
		this.neck5L.x = this.neck4L.x - (float)Math.sin(this.neck4L.yRot) * 9.0F * (float)Math.cos(this.neck4L.xRot);
		this.head7L.y = this.neck5L.y + (float)Math.sin(this.neck5L.xRot) * 9.0F;
		this.headfinL.y = this.jaw5L.y = this.jaw1L.y = this.head3L.y = this.head7L.y;
		this.head7L.z = this.neck5L.z - (float)Math.cos(this.neck5L.xRot) * 9.0F;
		this.headfinL.z = this.jaw5L.z = this.jaw1L.z = this.head3L.z = this.head7L.z;
		this.head7L.x = this.neck5L.x - (float)Math.sin(this.neck5L.yRot) * 9.0F * (float)Math.cos(this.neck5L.xRot);
		this.headfinL.x = this.jaw5L.x = this.jaw1L.x = this.head3L.x = this.head7L.x;
		this.neck4R.y = this.neck3R.y + (float)Math.sin(this.neck3R.xRot) * 9.0F;
		this.neck4R.z = this.neck3R.z - (float)Math.cos(this.neck3R.xRot) * 9.0F;
		this.neck4R.x = this.neck3R.x - (float)Math.sin(this.neck3R.yRot) * 9.0F * (float)Math.cos(this.neck3R.xRot);
		this.neck5R.y = this.neck4R.y + (float)Math.sin(this.neck4R.xRot) * 9.0F;
		this.neck5R.z = this.neck4R.z - (float)Math.cos(this.neck4R.xRot) * 9.0F;
		this.neck5R.x = this.neck4R.x - (float)Math.sin(this.neck4R.yRot) * 9.0F * (float)Math.cos(this.neck4R.xRot);
		this.head7R.y = this.neck5R.y + (float)Math.sin(this.neck5R.xRot) * 9.0F;
		this.headfinR.y = this.jaw5R.y = this.jaw1R.y = this.head3R.y = this.head7R.y;
		this.head7R.z = this.neck5R.z - (float)Math.cos(this.neck5R.xRot) * 9.0F;
		this.headfinR.z = this.jaw5R.z = this.jaw1R.z = this.head3R.z = this.head7R.z;
		this.head7R.x = this.neck5R.x - (float)Math.sin(this.neck5R.yRot) * 9.0F * (float)Math.cos(this.neck5R.xRot);
		this.headfinR.x = this.jaw5R.x = this.jaw1R.x = this.head3R.x = this.head7R.x;
		// render(): the wings blended (colour 0.75, alpha 0.55); each copy of the model shows only its own pass.
		this.allParts().forEach(part -> part.visible = pass == BODY);
		this.root().visible = true;
		for (ModelPart part : new ModelPart[] {mem1, mem2, rmem1, rmem2, mem3, rmem3, mem4, rmem4}) part.visible = pass == BLEND;
	}
}
