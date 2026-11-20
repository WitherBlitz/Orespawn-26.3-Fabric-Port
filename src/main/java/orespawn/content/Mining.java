package orespawn.content;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import orespawn.OreSpawn;
import orespawn.world.mining.MiningChunkGenerator;

/**
 * OreSpawn's Mining dimension ("Dimension-Extreme", DimensionID2: WorldProviderOreSpawn2 and ChunkProviderOreSpawn2),
 * {@link orespawn.world.OreSpawnDimensions#MINING}. The chunk generator ({@code orespawn:mining}) is registered here;
 * the dimension, its type and its biome are data ({@code tools/content/mining.json}):
 * <ul>
 * <li>{@code dimension_type/mining}: WorldProviderOreSpawn2 kept the overworld's sky, light, day and night (the same
 * clock as the overworld, so sleeping through the night there brings morning everywhere, as its setWorldTime did) and
 * let you respawn there; y 0-255 and clouds at 128 as in 1.7.10.</li>
 * <li>{@code worldgen/biome/mining}: Extreme Hills everywhere (WorldChunkManagerHell), made 0.8 warm and 0.01 wet as
 * OreSpawn did, so no snow and a dry-green colour; 1.7.10 Extreme Hills' vanilla mobs.</li>
 * <li>{@code dimension/mining}: this generator on that biome.</li>
 * </ul>
 * OreSpawn's red ant takes players there (the Portals group's teleporter).
 */
public final class Mining {
	private Mining() {
	}

	public static void init() {
		Registry.register(BuiltInRegistries.CHUNK_GENERATOR, OreSpawn.id("mining"), MiningChunkGenerator.CODEC);
	}
}
