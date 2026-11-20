package orespawn.client.model;

import orespawn.client.original.OriginalModels;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import orespawn.client.content.HoverboardRenderState;

/** Ported from OreSpawn's ModelElevator: the Hoverboard's deck with a short rail and a nub at each end. It has no animation. */
@Environment(EnvType.CLIENT)
public class HoverboardModel extends EntityModel<HoverboardRenderState> {
	public HoverboardModel(ModelPart root) {
		super(root);
	}

	public static LayerDefinition createBodyLayer() {
		return OriginalModels.layer("ModelElevator");
	}
}
