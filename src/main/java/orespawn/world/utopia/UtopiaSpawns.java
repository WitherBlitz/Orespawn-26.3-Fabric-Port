package orespawn.world.utopia;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import orespawn.OreSpawn;

import java.util.List;

/**
 * Utopia's creatures: OreSpawn's {@code BiomeGenUtopianPlains} constructor lists and BiomeGenBase's 1.7.10 defaults
 * (weight, min, max as written there; each creature looked up by id when the server loads its biomes, so creatures no
 * group has ported yet are simply left out). Utopia's ChunkProviderOreSpawn spawned no monsters at all and no horses,
 * so BiomeGenBase's default monsters are left out. (BiomeGenUtopianPlains's setVillageCreatures, setIslandCreatures,
 * setCrystalCreatures and setChaosCreatures are other dimensions' lists, which their groups apply.)
 * <p>
 * Each creature spawns in the category its entity type is registered with (as the rest of the port does), not the
 * list OreSpawn kept it in (OreSpawn's flying critters were in the "cave creature" list, its frogs in the water list).
 */
public final class UtopiaSpawns {
	/** One SpawnListEntry: an entity id (orespawn: unless given), weight, group size. */
	public record Spawn(String id, int weight, int min, int max) {}

	private static Spawn s(String id, int weight, int min, int max) {
		return new Spawn(id, weight, min, max);
	}

	/**
	 * BiomeGenBase's default creatures, bats and squid, then the constructor's additions (OreSpawn's default config
	 * enables them all). No monsters: ChunkProviderOreSpawn spawned none.
	 */
	public static final List<Spawn> UTOPIA = List.of(s("minecraft:sheep", 12, 4, 4), s("minecraft:pig", 10, 4, 4),
		s("minecraft:chicken", 10, 4, 4), s("minecraft:cow", 8, 4, 4), s("minecraft:bat", 10, 8, 8), s("minecraft:squid", 10, 4, 4),
		s("gazelle", 10, 2, 4), s("firefly", 15, 3, 6), s("girlfriend", 5, 2, 3), s("boyfriend", 5, 2, 3), s("apple_cow", 10, 4, 8),
		s("golden_apple_cow", 8, 2, 6), s("enchanted_golden_apple_cow", 5, 2, 4), s("butterfly", 20, 3, 6), s("moth", 10, 1, 5),
		s("chipmunk", 3, 1, 2), s("bird", 10, 2, 4), s("gold_fish", 1, 1, 1), s("whale", 1, 1, 1), s("flounder", 2, 2, 4),
		s("coin", 2, 1, 1), s("cricket", 5, 4, 6), s("frog", 5, 4, 6));

	private UtopiaSpawns() {
	}

	/** Adds {@link #UTOPIA} to the biome when the server loads its biomes; creatures not registered then are skipped. */
	public static void register(ResourceKey<Biome> biome) {
		BiomeModifications.create(OreSpawn.id("utopia_spawns")).add(ModificationPhase.ADDITIONS, BiomeSelectors.includeByKey(biome), context -> {
			for (Spawn spawn : UTOPIA) {
				EntityType<?> type = Lookup.entity(spawn.id());
				if (type != null) {
					context.getMobSpawnSettings().addSpawn(type, spawn.weight(), spawn.min(), spawn.max());
				}
			}
		});
	}
}
