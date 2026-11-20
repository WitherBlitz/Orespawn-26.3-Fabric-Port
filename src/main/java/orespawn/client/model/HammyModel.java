package orespawn.client.model;

import orespawn.client.original.OriginalModels;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * OreSpawn's ModelHammy (its shape is read from the original OreSpawn 1.7.10 file, see OriginalModels): the Attitude Adjuster as held in the hand.
 * Its state is an animation clock in seconds (only the Chainsaw moves).
 */
@Environment(EnvType.CLIENT)
public class HammyModel extends Model<Float> {
	public HammyModel(ModelPart root) {
		super(root, RenderTypes::entityCutout);
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelHammy");
	}
}
