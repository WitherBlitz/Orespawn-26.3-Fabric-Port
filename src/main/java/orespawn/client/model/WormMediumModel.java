package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelWormMedium (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels). */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class WormMediumModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart tooth1;
	private final ModelPart tooth2;
	private final ModelPart tooth3;
	private final ModelPart tooth4;
	private final ModelPart head2;

	public WormMediumModel(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
		this.body = root.getChild("body");
		this.tail = root.getChild("tail");
		this.tooth1 = root.getChild("tooth1");
		this.tooth2 = root.getChild("tooth2");
		this.tooth3 = root.getChild("tooth3");
		this.tooth4 = root.getChild("tooth4");
		this.head2 = root.getChild("head2");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelWormMedium");
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;
		float par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;
		// from setRotationAngles:
		// from render:
		float newangle = Mth.cos(f2 * 0.45F) * (float) Math.PI * 0.1F;
		this.tail.xRot = newangle;
		float d1 = (float)(Math.sin(newangle) * 12.0);
		float d2 = (float)(Math.cos(newangle) * 12.0);
		this.body.z = this.tail.z - d1;
		newangle = Mth.cos(f2 * 0.25F) * (float) Math.PI * 0.08F;
		this.tail.zRot = newangle;
		float d3 = (float)(Math.cos(newangle) * d2);
		float d4 = (float)(Math.sin(newangle) * d2);
		this.body.x = this.tail.x + d4;
		this.body.y = (float)(this.tail.y - 12.0 + (12.0 - d3));
		newangle = Mth.cos(f2 * 0.35F) * (float) Math.PI * 0.1F;
		this.body.xRot = newangle;
		d1 = (float)(Math.sin(newangle) * 12.0);
		d2 = (float)(Math.cos(newangle) * 12.0);
		this.head2.z = this.head.z = this.body.z - d1;
		newangle = Mth.cos(f2 * 0.15F) * (float) Math.PI * 0.07F;
		this.body.zRot = newangle;
		d3 = (float)(Math.cos(newangle) * d2);
		d4 = (float)(Math.sin(newangle) * d2);
		this.head2.x = this.head.x = this.body.x + d4;
		this.head2.y = this.head.y = (float)(this.body.y - 12.0 + (12.0 - d3));
		this.head2.xRot = this.head.xRot = 0.62F + Mth.cos(f2 * 0.55F) * (float) Math.PI * 0.15F;
		this.head2.zRot = this.head.zRot = Mth.cos(f2 * 0.25F) * (float) Math.PI * 0.05F;
		newangle = this.head.xRot;
		this.tooth1.xRot = this.tooth2.xRot = this.tooth3.xRot = this.tooth4.xRot = newangle;
		d1 = (float)(Math.sin(newangle) * 12.0);
		d2 = (float)(Math.cos(newangle) * 12.0);
		this.tooth1.z = this.tooth2.z = this.tooth3.z = this.tooth4.z = this.head.z - d1;
		newangle = this.head.zRot;
		this.tooth1.zRot = this.tooth2.zRot = this.tooth3.zRot = this.tooth4.zRot = newangle;
		d3 = (float)(Math.cos(newangle) * d2);
		d4 = (float)(Math.sin(newangle) * d2);
		this.tooth1.x = this.tooth2.x = this.tooth3.x = this.tooth4.x = this.head.x + d4;
		this.tooth1.y = this.tooth2.y = this.tooth3.y = this.tooth4.y = (float)(
		this.head.y - 12.0 + (12.0 - d3)
		);
		this.tooth1.z++;
		this.tooth2.z--;
		this.tooth1.xRot = this.tooth1.xRot - 0.4F - Mth.cos(f2 * 0.55F) * (float) Math.PI * 0.15F;
		this.tooth2.xRot = this.tooth2.xRot + 0.4F + Mth.cos(f2 * 0.55F) * (float) Math.PI * 0.15F;
		this.tooth3.x++;
		this.tooth4.x--;
		this.tooth3.zRot = this.tooth3.zRot + 0.4F + Mth.cos(f2 * 0.55F) * (float) Math.PI * 0.15F;
		this.tooth4.zRot = this.tooth4.zRot - 0.4F - Mth.cos(f2 * 0.55F) * (float) Math.PI * 0.15F;
	}
}
