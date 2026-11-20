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
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/**
 * Ported from OreSpawn's ModelCaterKiller (its parts are read from the original OreSpawn 1.7.10 file, see OriginalModels). OreSpawn drew the front
 * segment's parts 3 times and the middle segment's 6 times, moving them between draws; here each copy is its own part
 * ({@code seg1..._0} to {@code _2}, {@code seg2..._0} to {@code _5}), posed the way each draw was. Reads
 * {@code attacking}.
 */
@Environment(EnvType.CLIENT)
public class CaterKillerModel extends EntityModel<OreSpawnRenderState> {
	private static final int SEG1_COPIES = 3;
	private static final int SEG2_COPIES = 6;
	private final ModelPart Head;
	private final ModelPart falsehead;
	private final ModelPart ltusk1;
	private final ModelPart ltusk2;
	private final ModelPart rtusk1;
	private final ModelPart rtusk2;
	private final ModelPart ljaw;
	private final ModelPart rjaw;
	private final ModelPart[] seg1 = new ModelPart[SEG1_COPIES];
	private final ModelPart[] seg1lspike = new ModelPart[SEG1_COPIES];
	private final ModelPart[] seg1rspike = new ModelPart[SEG1_COPIES];
	private final ModelPart[] seg1ltopspike = new ModelPart[SEG1_COPIES];
	private final ModelPart[] seg1rtopspike = new ModelPart[SEG1_COPIES];
	private final ModelPart[] seg1lleg = new ModelPart[SEG1_COPIES];
	private final ModelPart[] seg1rleg = new ModelPart[SEG1_COPIES];
	private final ModelPart[] seg2 = new ModelPart[SEG2_COPIES];
	private final ModelPart[] seg2lfoot = new ModelPart[SEG2_COPIES];
	private final ModelPart[] seg2rfoot = new ModelPart[SEG2_COPIES];
	private final ModelPart[] seg2ltopspike = new ModelPart[SEG2_COPIES];
	private final ModelPart[] seg2rtopspike = new ModelPart[SEG2_COPIES];
	private final ModelPart[] seg2lspike = new ModelPart[SEG2_COPIES];
	private final ModelPart[] seg2rspike = new ModelPart[SEG2_COPIES];
	private final ModelPart seg3;
	private final ModelPart seg3lfoot;
	private final ModelPart seg3rfoot;
	private final ModelPart seg3lspike;
	private final ModelPart seg3rspike;
	private final ModelPart seg3ltopspike;
	private final ModelPart seg3rtopspike;
	private final ModelPart seg3lbackspike;
	private final ModelPart seg3rbackspike;
	private final float wingspeed;

	public CaterKillerModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.Head = root.getChild("head");
		this.falsehead = root.getChild("falsehead");
		this.ltusk1 = root.getChild("ltusk1");
		this.ltusk2 = root.getChild("ltusk2");
		this.rtusk1 = root.getChild("rtusk1");
		this.rtusk2 = root.getChild("rtusk2");
		this.ljaw = root.getChild("ljaw");
		this.rjaw = root.getChild("rjaw");
		for (int i = 0; i < SEG1_COPIES; i++) {
			seg1[i] = root.getChild("seg1_" + i);
			seg1lspike[i] = root.getChild("seg1lspike_" + i);
			seg1rspike[i] = root.getChild("seg1rspike_" + i);
			seg1ltopspike[i] = root.getChild("seg1ltopspike_" + i);
			seg1rtopspike[i] = root.getChild("seg1rtopspike_" + i);
			seg1lleg[i] = root.getChild("seg1lleg_" + i);
			seg1rleg[i] = root.getChild("seg1rleg_" + i);
		}
		for (int i = 0; i < SEG2_COPIES; i++) {
			seg2[i] = root.getChild("seg2_" + i);
			seg2lfoot[i] = root.getChild("seg2lfoot_" + i);
			seg2rfoot[i] = root.getChild("seg2rfoot_" + i);
			seg2ltopspike[i] = root.getChild("seg2ltopspike_" + i);
			seg2rtopspike[i] = root.getChild("seg2rtopspike_" + i);
			seg2lspike[i] = root.getChild("seg2lspike_" + i);
			seg2rspike[i] = root.getChild("seg2rspike_" + i);
		}
		this.seg3 = root.getChild("seg3");
		this.seg3lfoot = root.getChild("seg3lfoot");
		this.seg3rfoot = root.getChild("seg3rfoot");
		this.seg3lspike = root.getChild("seg3lspike");
		this.seg3rspike = root.getChild("seg3rspike");
		this.seg3ltopspike = root.getChild("seg3ltopspike");
		this.seg3rtopspike = root.getChild("seg3rtopspike");
		this.seg3lbackspike = root.getChild("seg3lbackspike");
		this.seg3rbackspike = root.getChild("seg3rbackspike");
	}

	public static LayerDefinition createBodyLayer() {
		OriginalModels.Geometry g = OriginalModels.geometry("ModelCaterKiller");
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		for (String name : new String[] {"head", "falsehead", "ltusk1", "ltusk2", "rtusk1", "rtusk2", "ljaw", "rjaw"}) OriginalModels.copy(root, g.part(name), name);
		// OreSpawn draws its first two body segments several times (see setupAnim), so the port has that many copies.
		for (int i = 0; i < SEG1_COPIES; i++) {
			for (String name : new String[] {"seg1", "seg1lspike", "seg1rspike", "seg1ltopspike", "seg1rtopspike", "seg1lleg", "seg1rleg"}) {
				OriginalModels.copy(root, g.part(name), name + "_" + i);
			}
		}
		for (int i = 0; i < SEG2_COPIES; i++) {
			for (String name : new String[] {"seg2", "seg2lfoot", "seg2rfoot", "seg2ltopspike", "seg2rtopspike", "seg2lspike", "seg2rspike"}) {
				OriginalModels.copy(root, g.part(name), name + "_" + i);
			}
		}
		for (String name : new String[] {"seg3", "seg3lfoot", "seg3rfoot", "seg3lspike", "seg3rspike", "seg3ltopspike", "seg3rtopspike", "seg3lbackspike", "seg3rbackspike"}) {
			OriginalModels.copy(root, g.part(name), name);
		}
		return g.layer(mesh);
	}

	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		float f1 = state.walkAnimationSpeed, f2 = state.ageInTicks;
		float newangle;
		float headoff;
		float zpi = 0.0F;
		if (state.attacking) {
			newangle = Mth.cos(f2 * 1.7F * this.wingspeed) * (float) Math.PI * 0.07F;
		} else {
			newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.025F;
		}
		this.ljaw.zRot = 0.139F + newangle;
		this.rjaw.zRot = -0.139F - newangle;
		if (state.attacking) {
			headoff = Mth.cos(f2 * 1.7F * this.wingspeed) * 8.0F;
		} else {
			headoff = Mth.cos(f2 * 0.3F * this.wingspeed) * 2.0F;
		}
		this.Head.y = -8.0F + headoff;
		this.falsehead.y = -8.0F + headoff;
		this.ltusk1.y = -25.0F + headoff;
		this.ltusk2.y = -25.0F + headoff;
		this.rtusk1.y = -25.0F + headoff;
		this.rtusk2.y = -25.0F + headoff;
		this.ljaw.y = -1.0F + headoff;
		this.rjaw.y = -1.0F + headoff;
		newangle = Mth.cos(f2 * 2.11F * this.wingspeed) * (float) Math.PI * 0.08F;
		this.ltusk2.yRot = 0.802F + newangle;
		newangle = Mth.cos(f2 * 2.3F * this.wingspeed) * (float) Math.PI * 0.08F;
		this.rtusk2.yRot = -0.802F + newangle;
		for (int i = 0; i < SEG1_COPIES; i++) {
			this.seg1[i].y = -8.0F + headoff / (i + 1) + 8 * i;
			this.seg1lspike[i].y = -32.0F + headoff / (i + 1) + 8 * i;
			this.seg1rspike[i].y = -32.0F + headoff / (i + 1) + 8 * i;
			this.seg1ltopspike[i].y = -39.0F + headoff / (i + 1) + 8 * i;
			this.seg1rtopspike[i].y = -39.0F + headoff / (i + 1) + 8 * i;
			this.seg1lleg[i].y = -8.0F + headoff / (i + 1) + 8 * i;
			this.seg1rleg[i].y = -8.0F + headoff / (i + 1) + 8 * i;
			this.seg1[i].z = -12 + 14 * i;
			this.seg1lspike[i].z = -6 + 14 * i;
			this.seg1rspike[i].z = -6 + 14 * i;
			this.seg1ltopspike[i].z = -6 + 14 * i;
			this.seg1rtopspike[i].z = -6 + 14 * i;
			this.seg1lleg[i].z = -5 + 14 * i;
			this.seg1rleg[i].z = -5 + 14 * i;
			newangle = Mth.cos((float) (f2 * 0.91F * this.wingspeed + (Math.PI / 8) * i)) * (float) Math.PI * 0.08F;
			this.seg1lspike[i].zRot = newangle;
			this.seg1rspike[i].zRot = -newangle;
			if (state.attacking) {
				newangle = Mth.cos((float) (f2 * 2.91F * this.wingspeed + (Math.PI / 8) * i)) * (float) Math.PI * 0.15F;
			} else {
				newangle = Mth.cos((float) (f2 * 0.35F * this.wingspeed + (Math.PI / 8) * i)) * (float) Math.PI * 0.04F;
			}
			this.seg1lleg[i].xRot = newangle;
			this.seg1rleg[i].xRot = -newangle;
		}
		for (int i = 0; i < SEG2_COPIES; i++) {
			float zdist = Mth.cos(f2 * 1.7F * this.wingspeed + zpi) * 1.5F * f1;
			float z = 39.0F + (16.0F + zdist) * i;
			this.seg2[i].z = z;
			this.seg2lfoot[i].z = z;
			this.seg2rfoot[i].z = z;
			this.seg2ltopspike[i].z = z;
			this.seg2rtopspike[i].z = z;
			this.seg2lspike[i].z = z;
			this.seg2rspike[i].z = z;
			newangle = Mth.cos((float) (f2 * 0.4F * this.wingspeed - (Math.PI / 8) * i)) * (float) Math.PI * 0.07F;
			this.seg2lspike[i].zRot = newangle;
			this.seg2rspike[i].zRot = -newangle;
			zpi += (float) (Math.PI / 4);
		}
		this.seg3.z = this.seg2rspike[SEG2_COPIES - 1].z + 16.0F;
		this.seg3lfoot.z = this.seg3.z;
		this.seg3rfoot.z = this.seg3.z;
		this.seg3lspike.z = this.seg3.z;
		this.seg3rspike.z = this.seg3.z;
		this.seg3ltopspike.z = this.seg3.z;
		this.seg3rtopspike.z = this.seg3.z;
		this.seg3lbackspike.z = this.seg3.z + 6.0F;
		this.seg3rbackspike.z = this.seg3.z + 6.0F;
		int i = SEG2_COPIES;
		newangle = Mth.cos((float) (f2 * 0.4F * this.wingspeed - (Math.PI / 8) * i)) * (float) Math.PI * 0.07F;
		this.seg3lspike.zRot = newangle;
		this.seg3rspike.zRot = -newangle;
		newangle = Mth.cos(f2 * 0.81F * this.wingspeed) * (float) Math.PI * 0.04F;
		this.seg3lbackspike.xRot = -0.977F + newangle;
		newangle = Mth.cos(f2 * 0.87F * this.wingspeed) * (float) Math.PI * 0.04F;
		this.seg3rbackspike.xRot = -0.977F + newangle;
		newangle = Mth.cos(f2 * 1.11F * this.wingspeed) * (float) Math.PI * 0.04F;
		this.seg3lbackspike.yRot = 0.28F + newangle;
		newangle = Mth.cos(f2 * 1.3F * this.wingspeed) * (float) Math.PI * 0.04F;
		this.seg3rbackspike.yRot = -0.28F + newangle;
	}
}
