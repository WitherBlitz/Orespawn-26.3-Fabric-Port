package orespawn.content;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import orespawn.OreSpawn;
import orespawn.world.chaos.ChaosChunkGenerator;
import orespawn.world.chaos.ChaosPlantsFeature;
import orespawn.world.chaos.SurfaceScanPlacement;

/**
 * The Chaos group: OreSpawn's Chaos dimension (DimensionID6, "Dimension-Chaos", WorldProviderOreSpawn6 and
 * ChunkProviderOreSpawn6), {@code orespawn:chaos} ({@link orespawn.world.OreSpawnDimensions#CHAOS}). OreSpawn's
 * butterfly takes players there and back (Portals group).
 * <p>
 * Code: the chunk generator {@code orespawn:chaos} (terrain, OreSpawn's in-chunk ores and scraggly apple trees, spawn
 * lists), the feature type {@code orespawn:chaos_plants} (OreSpawnWorld's butterfly plants and veggies) and the
 * placement type {@code orespawn:chaos_surface_scan}. Data (tools/content/chaos.json): the dimension and its type
 * (an overworld sky and day, sharing the overworld's clock, so sleeping there skips the night everywhere, as OreSpawn's
 * setWorldTime did; beds work, as its canRespawnHere was true; clouds at 1.7.10's y 128), and the {@code orespawn:chaos}
 * biome (temperature 0.8, rainfall 0.01, rain) with its 1.7.10 biome decorator and OreSpawnWorld's Chaos branch.
 * <p>
 * Not ported: respawning in Chaos itself after dying there (1.7.10 Forge kept a respawn point per dimension; 26.3 has
 * one, so a bed in Chaos is where you respawn, otherwise the overworld).
 */
public final class Chaos {
	private Chaos() {
	}

	public static void init() {
		Registry.register(BuiltInRegistries.CHUNK_GENERATOR, OreSpawn.id("chaos"), ChaosChunkGenerator.CODEC);
		Registry.register(BuiltInRegistries.FEATURE_TYPE, OreSpawn.id("chaos_plants"), ChaosPlantsFeature.CODEC);
		Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, OreSpawn.id("chaos_surface_scan"), SurfaceScanPlacement.CODEC);
	}
}
