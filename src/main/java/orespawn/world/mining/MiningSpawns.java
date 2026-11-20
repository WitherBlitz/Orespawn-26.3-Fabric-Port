package orespawn.world.mining;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import orespawn.OreSpawn;

import java.util.List;
import java.util.Optional;

/**
 * {@code ChunkProviderOreSpawn2.getPossibleCreatures}: the Extreme Hills biome's spawn list for a category, and for
 * monsters and ambient creatures OreSpawn's own Mining list ahead of it (OreSpawn's config enables them all).
 * <p>
 * The biome's list is the {@code orespawn:mining} biome's (1.7.10 Extreme Hills' vanilla mobs) plus whatever OreSpawn's
 * creature groups added to Extreme Hills, which they add to 26.3's {@code windswept_hills}: its {@code orespawn:}
 * entries are copied over. Creatures are looked up by id; one that isn't registered yet is left out.
 */
final class MiningSpawns {
	/** One SpawnListEntry: an orespawn entity id (the first one registered, for creatures known by more than one), weight, group size. */
	private record Entry(String[] ids, int weight, int min, int max) {
		Entry(String id, int weight, int min, int max) {
			this(new String[] {id}, weight, min, max);
		}
	}

	/** MyMobList, in OreSpawn's order. */
	private static final List<Entry> MONSTERS = List.of(
		new Entry("alosaurus", 8, 1, 2),
		new Entry("t_rex", 6, 1, 2),
		new Entry("nastysaurus", 6, 1, 2),
		new Entry("pointysaurus", 10, 4, 8),
		new Entry("wtf", 35, 4, 7), // GammaMetroid
		new Entry("alien", 35, 2, 3),
		new Entry("cave_fisher", 35, 4, 8),
		new Entry("cryolophosaurus", 26, 4, 7),
		new Entry("baby_dragon", 5, 1, 2)); // Spyro
	/** MyAmbientList, in OreSpawn's order. */
	private static final List<Entry> AMBIENT = List.of(
		new Entry("velocity_raptor", 1, 2, 4),
		new Entry("dragonfly", 2, 1, 3),
		new Entry("camarasaurus", 1, 2, 4),
		new Entry("baryonyx", 2, 4, 8));

	private MiningSpawns() {
	}

	static WeightedList<MobSpawnSettings.SpawnerData> build(HolderLookup<Biome> biomes, MobCategory category,
			WeightedList<MobSpawnSettings.SpawnerData> biome) {
		WeightedList.Builder<MobSpawnSettings.SpawnerData> list = WeightedList.builder();
		List<Entry> own = category == MobCategory.MONSTER ? MONSTERS : category == MobCategory.AMBIENT ? AMBIENT : List.of();
		for (Entry e : own) {
			for (String id : e.ids) {
				Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id(id));
				if (type.isPresent()) {
					list.add(new MobSpawnSettings.SpawnerData(type.get(), UniformInt.of(e.min, e.max)), e.weight);
					break;
				}
			}
		}
		for (Weighted<MobSpawnSettings.SpawnerData> w : biome.unwrap()) {
			list.add(w.value(), w.weight());
		}
		biomes.get(Biomes.WINDSWEPT_HILLS).ifPresent(hills -> {
			MobSpawnSettings settings = hills.value().getAttributes().applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS, MobSpawnSettings.EMPTY);
			for (Weighted<MobSpawnSettings.SpawnerData> w : settings.getMobsToSpawn(category).unwrap()) {
				if (EntityType.getKey(w.value().type()).getNamespace().equals(OreSpawn.MOD_ID)) {
					list.add(w.value(), w.weight());
				}
			}
		});
		return list.build();
	}
}
