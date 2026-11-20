package orespawn.world.chaos;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The Chaos biome's spawn lists: BiomeGenUtopianPlains.setChaosCreatures (OreSpawn's default config, every creature
 * enabled), as weight, smallest and largest group. Each list keeps OreSpawn's category: its "cave creature" list
 * (EnumCreatureType.ambient) is {@link MobCategory#AMBIENT}, as in 1.7.10, even for the bigger animals OreSpawn put
 * there. The water list is empty (setChaosCreatures cleared it).
 * <p>
 * Creatures are looked up by id the first time the lists are needed (after every group has registered); any that
 * isn't registered is left out.
 */
final class ChaosSpawns {
	private record Entry(String id, int weight, int min, int max) {
	}

	private static final List<Entry> AMBIENT = List.of(
		new Entry("butterfly", 20, 3, 6), new Entry("moth", 10, 1, 5), new Entry("bird", 10, 2, 4), new Entry("firefly", 15, 3, 6),
		new Entry("cliff_racer", 30, 3, 6), new Entry("cloud_shark", 2, 1, 1), new Entry("gold_fish", 10, 2, 4),
		new Entry("fairy", 5, 2, 4), new Entry("baryonyx", 2, 2, 4), new Entry("bee", 2, 2, 4), new Entry("cassowary", 2, 2, 4),
		new Entry("dragonfly", 2, 2, 4), new Entry("peacock", 2, 2, 4), new Entry("stink_bug", 3, 2, 4), new Entry("ostrich", 1, 1, 2),
		new Entry("chipmunk", 1, 1, 2));
	private static final List<Entry> CREATURE = List.of(
		new Entry("beaver", 1, 1, 2), new Entry("apple_cow", 3, 2, 4), new Entry("golden_apple_cow", 2, 2, 4),
		new Entry("enchanted_golden_apple_cow", 1, 2, 4));
	private static final List<Entry> MONSTER = List.of(
		new Entry("vortex", 1, 1, 2), new Entry("nightmare", 1, 1, 2), new Entry("terrible_terror", 4, 2, 6), new Entry("alosaurus", 1, 1, 1),
		new Entry("basilisk", 1, 1, 1), new Entry("bomb_omb", 5, 2, 8), new Entry("robo_pounder", 2, 1, 4), new Entry("robo_gunner", 2, 1, 4),
		new Entry("robo_warrior", 1, 1, 2), new Entry("robo_sniper", 2, 3, 5), new Entry("caterkiller", 1, 1, 1),
		new Entry("cave_fisher", 5, 1, 5), new Entry("creeping_horror", 5, 1, 5), new Entry("cryolophosaurus", 5, 1, 5),
		new Entry("crystal_urchin", 2, 1, 5), new Entry("dungeon_beast", 2, 1, 5), new Entry("emperor_scorpion", 1, 1, 1),
		new Entry("ender_knight", 2, 1, 2), new Entry("ender_reaper", 1, 1, 1), new Entry("hammerhead", 1, 1, 1),
		new Entry("hercules_beetle", 1, 1, 1), new Entry("jumpy_bug", 1, 1, 1), new Entry("molenoid", 1, 1, 1), new Entry("mothra", 1, 1, 1),
		new Entry("brutalfly", 1, 1, 1), new Entry("rat", 10, 1, 10), new Entry("rotator", 1, 1, 3), new Entry("scorpion", 2, 1, 3),
		new Entry("spit_bug", 2, 1, 3), new Entry("nastysaurus", 1, 1, 1), new Entry("t_rex", 1, 1, 1), new Entry("leaf_monster", 2, 1, 4),
		new Entry("pointysaurus", 2, 1, 4), new Entry("leonopteryx", 1, 1, 1), new Entry("mantis", 1, 1, 1),
		new Entry("lurking_terror", 1, 1, 1), new Entry("wtf", 1, 1, 1));

	private static final WeightedList<MobSpawnSettings.SpawnerData> NONE = WeightedList.of();
	private static volatile @Nullable Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> lists;

	private ChaosSpawns() {
	}

	/** The list for a category (the same instance every time, as NaturalSpawner's contains() check needs). */
	static WeightedList<MobSpawnSettings.SpawnerData> get(MobCategory category) {
		Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> map = lists;
		if (map == null) {
			map = new EnumMap<>(MobCategory.class);
			map.put(MobCategory.AMBIENT, build(AMBIENT));
			map.put(MobCategory.CREATURE, build(CREATURE));
			map.put(MobCategory.MONSTER, build(MONSTER));
			lists = map;
		}
		return map.getOrDefault(category, NONE);
	}

	private static WeightedList<MobSpawnSettings.SpawnerData> build(List<Entry> entries) {
		WeightedList.Builder<MobSpawnSettings.SpawnerData> builder = WeightedList.builder();
		for (Entry e : entries) {
			Identifier id = Identifier.fromNamespaceAndPath("orespawn", e.id);
			if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
				continue;
			}
			EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(id);
			if (type.getCategory() == MobCategory.MISC) {
				continue;
			}
			builder.add(new MobSpawnSettings.SpawnerData(type, UniformInt.of(e.min, e.max)), e.weight);
		}
		return builder.build();
	}
}
