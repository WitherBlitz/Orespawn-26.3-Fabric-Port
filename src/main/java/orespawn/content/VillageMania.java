package orespawn.content;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import orespawn.OreSpawn;
import orespawn.world.OreSpawnDimensions;
import orespawn.world.villagemania.VillageManiaChunkGenerator;

import java.util.List;
import java.util.Optional;

/**
 * OreSpawn's Village Mania dimension ({@link OreSpawnDimensions#VILLAGE_MANIA}; OreSpawn's DimensionID3,
 * "Dimension-VillageMania", WorldProviderOreSpawn3): villages everywhere on gentle 1.7.10 plains.
 * <ul>
 * <li>{@code data/orespawn/dimension_type/village_mania.json}: WorldProviderOreSpawn3 was a plain surface world, so
 * the overworld's sky, light, weather and day: its time is the overworld's clock (sleeping here moves every
 * dimension to morning, as OreSpawn's setWorldTime did), beds set your spawn, y 0 to 255 with 1.7.10's clouds at 128.
 * The sky is the colour 1.7.10 gave the biome's temperature of 0.7.</li>
 * <li>{@code worldgen/biome/villages.json}: OreSpawn's "Villages" biome (a BiomeGenUtopianPlains, temperature 0.7,
 * rainfall 0.5) with 1.7.10's BiomeGenBase animals and monsters; OreSpawn's own creatures are added here.</li>
 * <li>The chunk generator {@code orespawn:village_mania} ({@link VillageManiaChunkGenerator}), the
 * {@code orespawn:village_mania_plains} village and {@code orespawn:village_mania_mineshaft} structures, and their
 * structure sets (MapGenMoreVillages' dense grid).</li>
 * </ul>
 * Travel there (the rainbow ant) is the Portals group's.
 */
public final class VillageMania {
	public static final ResourceKey<Biome> VILLAGES = ResourceKey.create(Registries.BIOME, OreSpawn.id("villages"));

	/** One of BiomeGenUtopianPlains' SpawnListEntries: an orespawn: entity id, weight, group size. */
	private record Spawn(String id, int weight, int min, int max) {
	}

	private static Spawn s(String id, int weight, int min, int max) {
		return new Spawn(id, weight, min, max);
	}

	/**
	 * BiomeGenUtopianPlains' constructor, then setVillageCreatures (which added to its lists), in OreSpawn's order, with
	 * every creature OreSpawn's default config enabled. A creature listed twice spawns from both entries, as it did.
	 */
	private static final List<Spawn> SPAWNS = List.of(
		// The constructor.
		s("gazelle", 10, 2, 4), s("firefly", 15, 3, 6), s("girlfriend", 5, 2, 3), s("boyfriend", 5, 2, 3), s("apple_cow", 10, 4, 8),
		s("golden_apple_cow", 8, 2, 6), s("enchanted_golden_apple_cow", 5, 2, 4), s("butterfly", 20, 3, 6), s("moth", 10, 1, 5),
		s("chipmunk", 3, 1, 2), s("bird", 10, 2, 4), s("gold_fish", 1, 1, 1), s("whale", 1, 1, 1), s("flounder", 2, 2, 4), s("coin", 2, 1, 1),
		s("cricket", 5, 4, 6), s("frog", 5, 4, 6),
		// setVillageCreatures.
		s("bomb_omb", 25, 4, 8), s("robo_pounder", 16, 2, 8), s("robo_gunner", 12, 2, 4), s("robo_warrior", 8, 1, 2), s("robo_sniper", 20, 4, 8),
		s("jeffery", 8, 1, 2), s("spider_driver", 20, 3, 5), s("mobzilla", 2, 1, 1), s("firefly", 10, 3, 6), s("girlfriend", 1, 2, 3),
		s("boyfriend", 1, 2, 3), s("apple_cow", 8, 4, 8), s("golden_apple_cow", 6, 2, 6), s("enchanted_golden_apple_cow", 4, 2, 4),
		s("butterfly", 25, 3, 6), s("moth", 20, 1, 5), s("chipmunk", 5, 1, 2), s("bird", 15, 2, 4), s("t_shirt", 2, 1, 1), s("coin", 2, 1, 1),
		s("criminal", 15, 1, 2));

	private VillageMania() {
	}

	public static void init() {
		Registry.register(BuiltInRegistries.CHUNK_GENERATOR, OreSpawn.id("village_mania"), VillageManiaChunkGenerator.CODEC);
		// Looked up when the server loads its biomes, so creatures whose groups aren't ported yet are left out. Each
		// spawns in its own entity type's category, as the rest of the port does (OreSpawn kept its flying critters in
		// the "cave creature" list and its frog in the water list).
		BiomeModifications.create(OreSpawn.id("village_mania_creatures")).add(ModificationPhase.ADDITIONS, BiomeSelectors.includeByKey(VILLAGES),
			context -> {
				for (Spawn spawn : SPAWNS) {
					Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id(spawn.id()));
					type.ifPresent(t -> context.getMobSpawnSettings().addSpawn(t, spawn.weight(), spawn.min(), spawn.max()));
				}
			});
	}
}
