package orespawn.client.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import orespawn.client.OreSpawnClient;
import orespawn.client.model.AntModel;
import orespawn.content.Portals;

import java.util.List;

/**
 * The Portals group's client side, as ClientProxyOreSpawn set up RenderAnt: one ModelAnt for every ant, drawn at a
 * quarter size (black, rainbow and unstable ants) or 0.35 (red ant, termite), with RenderAnt's shadow of its two
 * arguments multiplied. The ant nests take the biome's grass colour, as AntBlock's colorMultiplier did.
 */
@Environment(EnvType.CLIENT)
public final class PortalsClient {
	private PortalsClient() {
	}

	public static void init() {
		ModelLayerLocation layer = OreSpawnClient.layer("ant", AntModel::createBodyLayer);
		ant(Portals.ANT, layer, 0.1F, 0.25F, "ant.png");
		ant(Portals.RED_ANT, layer, 0.15F, 0.35F, "red_ant.png");
		ant(Portals.RAINBOW_ANT, layer, 0.1F, 0.25F, "rainbow_ant.png");
		ant(Portals.UNSTABLE_ANT, layer, 0.1F, 0.25F, "unstable_ant.png");
		ant(Portals.TERMITE, layer, 0.15F, 0.35F, "termite.png");
		BlockColorRegistry.register(List.of(BlockTintSources.grass()), Portals.ANT_NEST, Portals.RED_ANT_NEST, Portals.RAINBOW_ANT_NEST,
			Portals.UNSTABLE_ANT_NEST, Portals.TERMITE_NEST);
	}

	/** new RenderAnt(new ModelAnt(), shadow, scale): its shadow is shadow * scale. */
	private static void ant(EntityType<? extends Mob> type, ModelLayerLocation layer, float shadow, float scale, String texture) {
		Identifier tex = OreSpawnClient.tex(texture);
		OreSpawnClient.<Mob>mob(type, layer, AntModel::new, shadow * scale, scale, (e, s) -> s.texture = tex, null);
	}
}
