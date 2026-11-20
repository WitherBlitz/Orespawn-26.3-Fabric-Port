package orespawn.content;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import orespawn.OreSpawn;
import orespawn.world.islands.IslandsChunkGenerator;

/**
 * The Islands group: OreSpawn's Islands dimension (DimensionID4, "Dimension-Islands"; WorldProviderOreSpawn4,
 * ChunkProviderOreSpawn4 and OreSpawnWorld's DimensionID4 branch), {@code orespawn:islands}.
 * <p>
 * The level itself is data ({@code data/orespawn/dimension/islands.json}, {@code dimension_type/islands.json} and
 * {@code worldgen/biome/islands.json}, written by gen_assets.py from tools/content/islands.json); this registers its
 * chunk generator, {@code orespawn:islands} ({@link IslandsChunkGenerator}), which also decorates it and gives its
 * spawn lists. As OreSpawn had it:
 * <ul>
 * <li>An overworld sky: sun, moon, stars, clouds (at y 128, 1.7.10's cloud height), the overworld's day and night and
 * weather. It keeps the overworld's time, and sleeping there skips the night everywhere (WorldProviderOreSpawn4's
 * setWorldTime). Beds work and can set your spawn; respawn anchors don't.</li>
 * <li>The world is y 0 to 255, light as in the overworld, monsters spawning in the same darkness.</li>
 * <li>One biome, "Islands" (BiomeGenUtopianPlains as the provider set it up: temperature 0.8, rainfall 0.01, so the
 * grass and leaves take a dry, yellow-green colour). It rains there.</li>
 * </ul>
 * Getting there (the Unstable Ant and OreSpawnTeleporter) is the Portals group's.
 */
public final class Islands {
	private Islands() {
	}

	public static void init() {
		Registry.register(BuiltInRegistries.CHUNK_GENERATOR, OreSpawn.id("islands"), IslandsChunkGenerator.CODEC);
	}
}
