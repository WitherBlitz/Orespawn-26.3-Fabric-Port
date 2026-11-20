package orespawn.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.original.OriginalModels;

/** Ported from OreSpawn's ModelRockBase (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels): every rock type's shapes. */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class RockModel extends EntityModel<OreSpawnRenderState> {
	private final ModelPart RockShape1;
	private final ModelPart RockShape2;
	private final ModelPart RockShape3;
	private final ModelPart RockSmallShape2;
	private final ModelPart RockSmallShape1;
	private final ModelPart RockTNTShape1;
	private final ModelPart RockTNTShape2;
	private final ModelPart RockTNTShape3;
	private final ModelPart RockTNTShape4;
	private final ModelPart RockSpikeyShape1;
	private final ModelPart RockSpikeyShape2;
	private final ModelPart RockSpikeyShape3;
	private final ModelPart CrystalShape1;
	private final ModelPart CrystalShape2;
	private final ModelPart CrystalShape3a;
	private final ModelPart CrystalShape3b;
	private final ModelPart CrystalShape3c;
	private final ModelPart CrystalShape3d;
	private final ModelPart CrystalShape4a;
	private final ModelPart CrystalShape4b;
	private final ModelPart CrystalShape4c;
	private final ModelPart CrystalShape4d;
	private final float wingspeed;

	public RockModel(ModelPart root, float wingspeed) {
		super(root);
		this.wingspeed = wingspeed;
		this.RockShape1 = root.getChild("rockshape1");
		this.RockShape2 = root.getChild("rockshape2");
		this.RockShape3 = root.getChild("rockshape3");
		this.RockSmallShape2 = root.getChild("rocksmallshape2");
		this.RockSmallShape1 = root.getChild("rocksmallshape1");
		this.RockTNTShape1 = root.getChild("rocktntshape1");
		this.RockTNTShape2 = root.getChild("rocktntshape2");
		this.RockTNTShape3 = root.getChild("rocktntshape3");
		this.RockTNTShape4 = root.getChild("rocktntshape4");
		this.RockSpikeyShape1 = root.getChild("rockspikeyshape1");
		this.RockSpikeyShape2 = root.getChild("rockspikeyshape2");
		this.RockSpikeyShape3 = root.getChild("rockspikeyshape3");
		this.CrystalShape1 = root.getChild("crystalshape1");
		this.CrystalShape2 = root.getChild("crystalshape2");
		this.CrystalShape3a = root.getChild("crystalshape3a");
		this.CrystalShape3b = root.getChild("crystalshape3b");
		this.CrystalShape3c = root.getChild("crystalshape3c");
		this.CrystalShape3d = root.getChild("crystalshape3d");
		this.CrystalShape4a = root.getChild("crystalshape4a");
		this.CrystalShape4b = root.getChild("crystalshape4b");
		this.CrystalShape4c = root.getChild("crystalshape4c");
		this.CrystalShape4d = root.getChild("crystalshape4d");
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelRockBase");
	}

	/**
	 * ModelRockBase.render draws one shape set per rock type ({@code state.variant}, 1-12): the small rock, the spiky rock,
	 * the TNT rock, the crystal (types 9-12, drawn see-through and full bright by the renderer) or the plain rock.
	 */
	@Override
	public void setupAnim(OreSpawnRenderState state) {
		super.setupAnim(state);
		int rt = state.variant;
		boolean small = rt == 1, spikey = rt == 7, tnt = rt == 8, crystal = rt >= 9 && rt <= 12, plain = rt >= 2 && rt <= 6;
		for (ModelPart p : new ModelPart[] {RockShape1, RockShape2, RockShape3}) p.visible = plain;
		for (ModelPart p : new ModelPart[] {RockSmallShape1, RockSmallShape2}) p.visible = small;
		for (ModelPart p : new ModelPart[] {RockTNTShape1, RockTNTShape2, RockTNTShape3, RockTNTShape4}) p.visible = tnt;
		for (ModelPart p : new ModelPart[] {RockSpikeyShape1, RockSpikeyShape2, RockSpikeyShape3}) p.visible = spikey;
		for (ModelPart p : new ModelPart[] {CrystalShape1, CrystalShape2, CrystalShape3a, CrystalShape3b, CrystalShape3c, CrystalShape3d,
			CrystalShape4a, CrystalShape4b, CrystalShape4c, CrystalShape4d}) {
			p.visible = crystal;
		}
	}
}
