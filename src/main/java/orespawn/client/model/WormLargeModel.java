package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelWormLarge (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class WormLargeModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart head1;
	private final ModelPart head2;
	private final ModelPart head3;
	private final ModelPart head4;
	private final ModelPart head5;
	private final ModelPart neck1;
	private final ModelPart neck4;
	private final ModelPart neck5;
	private final ModelPart neck2;
	private final ModelPart neck3;
	private final ModelPart tail1;
	private final ModelPart tailtip;
	private final ModelPart tail2;
	private final ModelPart tail3;
	private final ModelPart tail4;
	private final ModelPart tooth1;
	private final ModelPart tooth2;
	private final ModelPart tooth3;
	private final ModelPart tooth4;
	private final ModelPart tooth5;
	private final ModelPart tooth6;
	private final ModelPart tooth7;
	private final ModelPart tooth8;

	public WormLargeModel(ModelPart root) {
		super(root);
		this.head1 = root.getChild("head1");
		this.head2 = root.getChild("head2");
		this.head3 = root.getChild("head3");
		this.head4 = root.getChild("head4");
		this.head5 = root.getChild("head5");
		this.neck1 = root.getChild("neck1");
		this.neck4 = root.getChild("neck4");
		this.neck5 = root.getChild("neck5");
		this.neck2 = root.getChild("neck2");
		this.neck3 = root.getChild("neck3");
		this.tail1 = root.getChild("tail1");
		this.tailtip = root.getChild("tailtip");
		this.tail2 = root.getChild("tail2");
		this.tail3 = root.getChild("tail3");
		this.tail4 = root.getChild("tail4");
		this.tooth1 = root.getChild("tooth1");
		this.tooth2 = root.getChild("tooth2");
		this.tooth3 = root.getChild("tooth3");
		this.tooth4 = root.getChild("tooth4");
		this.tooth5 = root.getChild("tooth5");
		this.tooth6 = root.getChild("tooth6");
		this.tooth7 = root.getChild("tooth7");
		this.tooth8 = root.getChild("tooth8");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelWormLarge");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		double dist = 32.0;
		float newangle = Mth.cos(f2 * 0.25F) * (float) Math.PI * 0.08F;
		newangle -= 0.698F;
		this.neck1.xRot = newangle;
		float newangle2 = Mth.cos(f2 * 0.15F) * (float) Math.PI * 0.07F;
		this.neck1.yRot = newangle2;
		this.neck2.xRot = this.neck3.xRot = this.neck4.xRot = this.neck5.xRot = this.neck1.xRot;
		this.neck2.yRot = this.neck3.yRot = this.neck4.yRot = this.neck5.yRot = this.neck1.yRot;
		double d1 = (float)(Math.cos(newangle) * dist);
		double d2 = (float)(Math.sin(newangle) * dist);
		this.head1.z = (float)(this.neck1.z - d1);
		double d3 = (float)(Math.sin(newangle2) * d1);
		double d4 = (float)(Math.cos(newangle2) * d1);
		this.head1.x = (float)(this.neck1.x - d3);
		this.head1.y = (float)(this.neck1.y + d2);
		this.head1.xRot = newangle = Mth.cos(f2 * 0.35F) * (float) Math.PI * 0.15F;
		this.head1.yRot = newangle2 = Mth.cos(f2 * 0.45F) * (float) Math.PI * 0.05F;
		this.head2.x = this.head3.x = this.head4.x = this.head5.x = this.head1.x;
		this.head2.y = this.head3.y = this.head4.y = this.head5.y = this.head1.y;
		this.head2.z = this.head3.z = this.head4.z = this.head5.z = this.head1.z;
		this.head2.xRot = this.head3.xRot = this.head4.xRot = this.head5.xRot = this.head1.xRot;
		this.head2.yRot = this.head3.yRot = this.head4.yRot = this.head5.yRot = this.head1.yRot;
		dist = 19.0;
		d1 = (float)(Math.cos(newangle) * dist);
		d2 = (float)(Math.sin(newangle) * dist);
		this.tooth1.z = (float)(this.head1.z - d1);
		d3 = (float)(Math.sin(newangle2) * d1);
		d4 = (float)(Math.cos(newangle2) * d1);
		this.tooth1.x = (float)(this.head1.x - d3);
		this.tooth1.y = (float)(this.head1.y + d2 - 9.0);
		this.tooth2.z = this.tooth1.z;
		this.tooth2.x = this.tooth1.x;
		this.tooth2.y = this.tooth1.y + 18.0F;
		this.tooth3.z = this.tooth1.z;
		this.tooth3.x = this.tooth1.x + 9.0F;
		this.tooth3.y = this.tooth1.y + 9.0F;
		this.tooth4.z = this.tooth1.z;
		this.tooth4.x = this.tooth1.x - 9.0F;
		this.tooth4.y = this.tooth1.y + 9.0F;
		this.tooth5.z = this.tooth1.z;
		this.tooth5.x = this.tooth1.x - 6.0F;
		this.tooth5.y = this.tooth1.y + 9.0F - 6.0F;
		this.tooth6.z = this.tooth1.z;
		this.tooth6.x = this.tooth1.x + 6.0F;
		this.tooth6.y = this.tooth1.y + 9.0F + 6.0F;
		this.tooth7.z = this.tooth1.z;
		this.tooth7.x = this.tooth1.x + 6.0F;
		this.tooth7.y = this.tooth1.y + 9.0F - 6.0F;
		this.tooth8.z = this.tooth1.z;
		this.tooth8.x = this.tooth1.x - 6.0F;
		this.tooth8.y = this.tooth1.y + 9.0F + 6.0F;
		this.tooth1.z = (float)(this.tooth1.z - Math.sin(this.head1.xRot) * 9.0);
		this.tooth2.z = (float)(this.tooth2.z + Math.sin(this.head1.xRot) * 9.0);
		this.tooth3.z = (float)(this.tooth3.z - Math.sin(this.head1.yRot) * 9.0);
		this.tooth4.z = (float)(this.tooth4.z + Math.sin(this.head1.yRot) * 9.0);
		this.tooth7.z = (float)(this.tooth7.z - Math.sin(this.head1.xRot) * 6.0);
		this.tooth7.z = (float)(this.tooth7.z - Math.sin(this.head1.yRot) * 6.0);
		this.tooth6.z = (float)(this.tooth6.z + Math.sin(this.head1.xRot) * 6.0);
		this.tooth6.z = (float)(this.tooth6.z - Math.sin(this.head1.yRot) * 6.0);
		this.tooth5.z = (float)(this.tooth5.z - Math.sin(this.head1.xRot) * 6.0);
		this.tooth5.z = (float)(this.tooth5.z + Math.sin(this.head1.yRot) * 6.0);
		this.tooth8.z = (float)(this.tooth8.z + Math.sin(this.head1.xRot) * 6.0);
		this.tooth8.z = (float)(this.tooth8.z + Math.sin(this.head1.yRot) * 6.0);
		newangle = Mth.cos(f2 * 0.57F) * (float) Math.PI * 0.35F;
		this.tooth1.xRot = this.head1.xRot + newangle;
		this.tooth2.xRot = this.head1.xRot - newangle;
		this.tooth3.yRot = this.head1.yRot + newangle;
		this.tooth4.yRot = this.head1.yRot - newangle;
		this.tooth5.xRot = this.head1.xRot + newangle;
		this.tooth7.xRot = this.head1.xRot + newangle;
		this.tooth6.xRot = this.head1.xRot - newangle;
		this.tooth8.xRot = this.head1.xRot - newangle;
		this.tooth6.yRot = this.head1.yRot + newangle;
		this.tooth7.yRot = this.head1.yRot + newangle;
		this.tooth5.yRot = this.head1.yRot - newangle;
		this.tooth8.yRot = this.head1.yRot - newangle;
		newangle = Mth.cos(f2 * 0.63F) * (float) Math.PI * 0.15F;
		this.tailtip.xRot = newangle + 0.35F;
		newangle = Mth.cos((float)(f2 * 0.63F + 1.57075)) * (float) Math.PI * 0.15F;
		this.tailtip.yRot = newangle;
	}
}
