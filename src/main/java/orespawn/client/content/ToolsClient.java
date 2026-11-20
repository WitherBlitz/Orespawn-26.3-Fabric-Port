package orespawn.client.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import orespawn.client.OreSpawnClient;
import orespawn.client.model.HoverboardModel;
import orespawn.client.model.RockModel;
import orespawn.content.Tools;

/**
 * Renderers for the Tools group (OreSpawn's ClientProxyOreSpawn): thrown cages and shoes spin as their item pictures
 * (RenderCage, RenderShoe), thrown rocks too and faster (RenderThrownRock), rocks on the ground use ModelRockBase
 * (RenderRockBase) and the Hoverboard ModelElevator (RenderElevator).
 */
@Environment(EnvType.CLIENT)
public final class ToolsClient {
	private ToolsClient() {
	}

	public static void init() {
		EntityRendererRegistry.register(Tools.ENTITY_CAGE, c -> new SpinningItemRenderer<>(c, 20.0F));
		EntityRendererRegistry.register(Tools.SHOES, c -> new SpinningItemRenderer<>(c, 20.0F));
		EntityRendererRegistry.register(Tools.ENTITY_THROWN_ROCK, c -> new SpinningItemRenderer<>(c, 30.0F));
		ModelLayerLocation rock = OreSpawnClient.layer("rock", RockModel::createBodyLayer);
		EntityRendererRegistry.register(Tools.ROCK, c -> new RockRenderer(c, new RockModel(c.bakeLayer(rock), 1.0F)));
		ModelLayerLocation hoverboard = OreSpawnClient.layer("hoverboard", HoverboardModel::createBodyLayer);
		EntityRendererRegistry.register(Tools.HOVERBOARD_ENTITY, c -> new HoverboardRenderer(c, new HoverboardModel(c.bakeLayer(hoverboard))));
	}
}
