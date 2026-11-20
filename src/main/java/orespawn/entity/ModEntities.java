package orespawn.entity;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import orespawn.OreSpawn;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OreSpawn's creatures: entity types (OreSpawn's sizes), attributes, spawn rules, spawn eggs and natural spawns.
 * Natural spawns use OreSpawn's own weights and group sizes (EntityRegistry.addSpawn in OreSpawnMain), with its 1.7.10
 * biomes mapped to their 26.3 equivalents; where several old biomes became one, the highest weight is kept.
 */
public final class ModEntities {
	/** id -> (entity type, its spawn egg), in creative-tab order. */
	public static final Map<String, EntityType<? extends Mob>> TYPES = new LinkedHashMap<>();
	public static final Map<String, Item> SPAWN_EGGS = new LinkedHashMap<>();

	public static final EntityType<AppleCow> APPLE_COW = register("apple_cow", AppleCow::new, MobCategory.CREATURE, 0.9F, 1.3F, 10);
	public static final EntityType<AppleCow> GOLDEN_APPLE_COW = register("golden_apple_cow", AppleCow::new, MobCategory.CREATURE, 0.9F, 1.3F, 10);
	public static final EntityType<AppleCow> ENCHANTED_GOLDEN_APPLE_COW = register("enchanted_golden_apple_cow", AppleCow::new,
		MobCategory.CREATURE, 0.9F, 1.3F, 10);
	public static final EntityType<Butterfly> BUTTERFLY = register("butterfly", Butterfly::new, MobCategory.AMBIENT, 0.4F, 0.4F, 8);
	public static final EntityType<Moth> MOTH = register("moth", Moth::new, MobCategory.AMBIENT, 0.5F, 0.5F, 8);
	public static final EntityType<Firefly> FIREFLY = register("firefly", Firefly::new, MobCategory.AMBIENT, 0.4F, 0.8F, 8);
	public static final EntityType<Bird> BIRD = register("bird", Bird::new, MobCategory.AMBIENT, 0.5F, 0.5F, 8);
	public static final EntityType<Chipmunk> CHIPMUNK = register("chipmunk", Chipmunk::new, MobCategory.AMBIENT, 0.35F, 0.35F, 8);
	public static final EntityType<Cricket> CRICKET = register("cricket", Cricket::new, MobCategory.AMBIENT, 0.1F, 0.1F, 6);
	public static final EntityType<Frog> FROG = register("frog", Frog::new, MobCategory.AMBIENT, 0.75F, 0.75F, 8);
	public static final EntityType<Beaver> BEAVER = register("beaver", Beaver::new, MobCategory.CREATURE, 0.6F, 0.8F, 8);
	public static final EntityType<Cassowary> CASSOWARY = register("cassowary", Cassowary::new, MobCategory.AMBIENT, 0.5F, 1.2F, 8);
	public static final EntityType<Rat> RAT = register("rat", Rat::new, MobCategory.MONSTER, 0.25F, 0.5F, 8);
	// batch 2
	public static final EntityType<Peacock> PEACOCK = register("peacock", Peacock::new, MobCategory.AMBIENT, 0.65F, 1.2F, 8);
	public static final EntityType<StinkBug> STINK_BUG = register("stink_bug", StinkBug::new, MobCategory.AMBIENT, 0.55F, 0.55F, 8);
	public static final EntityType<Mosquito> MOSQUITO = register("mosquito", Mosquito::new, MobCategory.AMBIENT, 0.2F, 0.2F, 4);
	public static final EntityType<Dragonfly> DRAGONFLY = register("dragonfly", Dragonfly::new, MobCategory.AMBIENT, 1.5F, 0.5F, 8);
	public static final EntityType<GoldFish> GOLD_FISH = register("gold_fish", GoldFish::new, MobCategory.CREATURE, 0.75F, 0.5F, 8);
	public static final EntityType<Flounder> FLOUNDER = register("flounder", Flounder::new, MobCategory.WATER_CREATURE, 0.55F, 0.25F, 8);
	public static final EntityType<Skate> SKATE = register("skate", Skate::new, MobCategory.MONSTER, 0.75F, 0.25F, 8);
	public static final EntityType<Whale> WHALE = register("whale", Whale::new, MobCategory.WATER_CREATURE, 1.5F, 2.5F, 10);
	public static final EntityType<Gazelle> GAZELLE = register("gazelle", Gazelle::new, MobCategory.CREATURE, 0.6F, 1.8F, 8);
	public static final EntityType<Ostrich> OSTRICH = register("ostrich", Ostrich::new, MobCategory.AMBIENT, 0.85F, 2.1F, 8);
	public static final EntityType<Lizard> LIZARD = register("lizard", Lizard::new, MobCategory.WATER_CREATURE, 1.5F, 1.25F, 8);
	public static final EntityType<Crab> CRAB = register("crab", Crab::new, MobCategory.WATER_CREATURE, 2.5F, 3.5F, 8);
	public static final EntityType<Scorpion> SCORPION = register("scorpion", Scorpion::new, MobCategory.AMBIENT, 0.85F, 0.55F, 8);
	public static final EntityType<WormSmall> SMALL_WORM = register("small_worm", WormSmall::new, MobCategory.MONSTER, 0.25F, 1.0F, 8);
	public static final EntityType<WormMedium> MEDIUM_WORM = register("medium_worm", WormMedium::new, MobCategory.MONSTER, 0.5F, 2.0F, 8);
	public static final EntityType<WormLarge> LARGE_WORM = register("large_worm", WormLarge::new, MobCategory.CREATURE, 1.55F, 2.5F, 8);

	/** OreSpawn's 1.7.10 biome names, as its spawn tables use them, and the 26.3 biomes they became. */
	private static final Map<String, List<String>> BIOMES = Map.ofEntries(
		Map.entry("plains", List.of("plains", "sunflower_plains", "meadow")), Map.entry("forest", List.of("forest", "flower_forest")),
		Map.entry("foresthills", List.of("forest")), Map.entry("taiga", List.of("taiga")), Map.entry("taigahills", List.of("taiga")),
		Map.entry("megataiga", List.of("old_growth_pine_taiga", "old_growth_spruce_taiga")),
		Map.entry("megataigahills", List.of("old_growth_pine_taiga", "old_growth_spruce_taiga")), Map.entry("river", List.of("river")),
		Map.entry("swamp", List.of("swamp", "mangrove_swamp")), Map.entry("jungle", List.of("jungle", "bamboo_jungle")),
		Map.entry("junglehills", List.of("jungle")), Map.entry("birch", List.of("birch_forest", "old_growth_birch_forest")),
		Map.entry("birchhills", List.of("birch_forest", "old_growth_birch_forest")), Map.entry("roofed", List.of("dark_forest")),
		Map.entry("savanna", List.of("savanna", "windswept_savanna")), Map.entry("savannaplat", List.of("savanna_plateau")),
		Map.entry("mesa", List.of("badlands", "eroded_badlands")), Map.entry("mesaplat", List.of("badlands")),
		Map.entry("mesaF", List.of("wooded_badlands")),
		Map.entry("xhills", List.of("windswept_hills", "windswept_gravelly_hills", "windswept_forest")),
		Map.entry("xhillsedge", List.of("windswept_hills")), Map.entry("xhillsplus", List.of("windswept_forest")),
		Map.entry("beach", List.of("beach")), Map.entry("stonebeach", List.of("stony_shore")), Map.entry("desert", List.of("desert")),
		Map.entry("iceplains", List.of("snowy_plains")), Map.entry("coldtaiga", List.of("snowy_taiga")),
		Map.entry("mushroom", List.of("mushroom_fields")), Map.entry("mushroomshore", List.of("mushroom_fields")),
		Map.entry("ocean", List.of("ocean", "lukewarm_ocean", "warm_ocean", "cold_ocean")),
		Map.entry("deepocean", List.of("deep_ocean", "deep_lukewarm_ocean", "deep_cold_ocean")),
		Map.entry("frozenocean", List.of("frozen_ocean", "deep_frozen_ocean")), Map.entry("frozenriver", List.of("frozen_river")),
		Map.entry("icemtn", List.of("ice_spikes")), Map.entry("deserthills", List.of("desert")), Map.entry("coldbeach", List.of("snowy_beach")),
		Map.entry("coldtaigahills", List.of("snowy_taiga")), Map.entry("jungleedge", List.of("sparse_jungle")),
		Map.entry("nether", List.of("nether_wastes", "crimson_forest", "warped_forest", "soul_sand_valley", "basalt_deltas")),
		Map.entry("end", List.of("the_end", "end_highlands", "end_midlands", "small_end_islands", "end_barrens")));

	private ModEntities() {
	}

	@SuppressWarnings("unchecked")
	/** Registers a creature and its spawn egg (used by every content group). */
	public static <T extends Mob> EntityType<T> register(String name, EntityType.EntityFactory<T> factory, MobCategory category, float w, float h,
			int range) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, OreSpawn.id(name));
		EntityType<T> type = Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
			EntityType.Builder.of(factory, category).sized(w, h).clientTrackingRange(range).build(key));
		TYPES.put(name, type);
		ResourceKey<Item> eggKey = ResourceKey.create(Registries.ITEM, OreSpawn.id(name + "_spawn_egg"));
		SPAWN_EGGS.put(name, Registry.register(BuiltInRegistries.ITEM, eggKey,
			new SpawnEggItem(new Item.Properties().setId(eggKey).spawnEgg(type))));
		return type;
	}

	public static void init() {
		attributes(AbstractCow.createAttributes(), APPLE_COW, GOLDEN_APPLE_COW, ENCHANTED_GOLDEN_APPLE_COW);
		FabricDefaultAttributeRegistry.register(BUTTERFLY, Butterfly.createAttributes());
		FabricDefaultAttributeRegistry.register(MOTH, Moth.createAttributes());
		FabricDefaultAttributeRegistry.register(FIREFLY, Firefly.createAttributes());
		FabricDefaultAttributeRegistry.register(BIRD, Bird.createAttributes());
		FabricDefaultAttributeRegistry.register(CHIPMUNK, Chipmunk.createAttributes());
		FabricDefaultAttributeRegistry.register(CRICKET, Cricket.createAttributes());
		FabricDefaultAttributeRegistry.register(FROG, Frog.createAttributes());
		FabricDefaultAttributeRegistry.register(BEAVER, Beaver.createAttributes());
		FabricDefaultAttributeRegistry.register(CASSOWARY, Cassowary.createAttributes());
		FabricDefaultAttributeRegistry.register(RAT, Rat.createAttributes());
		FabricDefaultAttributeRegistry.register(PEACOCK, Peacock.createAttributes());
		FabricDefaultAttributeRegistry.register(STINK_BUG, StinkBug.createAttributes());
		FabricDefaultAttributeRegistry.register(MOSQUITO, Mosquito.createAttributes());
		FabricDefaultAttributeRegistry.register(DRAGONFLY, Dragonfly.createAttributes());
		FabricDefaultAttributeRegistry.register(GOLD_FISH, GoldFish.createAttributes());
		FabricDefaultAttributeRegistry.register(FLOUNDER, Flounder.createAttributes());
		FabricDefaultAttributeRegistry.register(SKATE, Skate.createAttributes());
		FabricDefaultAttributeRegistry.register(WHALE, Whale.createAttributes());
		FabricDefaultAttributeRegistry.register(GAZELLE, Gazelle.createAttributes());
		FabricDefaultAttributeRegistry.register(OSTRICH, Ostrich.createAttributes());
		FabricDefaultAttributeRegistry.register(LIZARD, Lizard.createAttributes());
		FabricDefaultAttributeRegistry.register(CRAB, Crab.createAttributes());
		FabricDefaultAttributeRegistry.register(SCORPION, Scorpion.createAttributes());
		FabricDefaultAttributeRegistry.register(SMALL_WORM, WormSmall.createAttributes());
		FabricDefaultAttributeRegistry.register(MEDIUM_WORM, WormMedium.createAttributes());
		FabricDefaultAttributeRegistry.register(LARGE_WORM, WormLarge.createAttributes());

		var ground = SpawnPlacementTypes.ON_GROUND;
		var air = SpawnPlacementTypes.NO_RESTRICTIONS;
		var height = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
		for (EntityType<AppleCow> cow : List.of(APPLE_COW, GOLDEN_APPLE_COW, ENCHANTED_GOLDEN_APPLE_COW)) {
			SpawnPlacements.register(cow, ground, height, Animal::checkAnimalSpawnRules);
		}
		SpawnPlacements.register(BUTTERFLY, air, height, Butterfly::checkSpawn);
		SpawnPlacements.register(MOTH, air, height, Moth::checkMothSpawn);
		SpawnPlacements.register(FIREFLY, air, height, Firefly::checkSpawn);
		SpawnPlacements.register(BIRD, air, height, Bird::checkSpawn);
		SpawnPlacements.register(CHIPMUNK, ground, height, Chipmunk::checkSpawn);
		SpawnPlacements.register(CRICKET, ground, height, Cricket::checkSpawn);
		SpawnPlacements.register(FROG, ground, height, Frog::checkSpawn);
		SpawnPlacements.register(BEAVER, ground, height, Beaver::checkSpawn);
		SpawnPlacements.register(CASSOWARY, ground, height, Cassowary::checkSpawn);
		SpawnPlacements.register(RAT, ground, height, Monster::checkMonsterSpawnRules);
		SpawnPlacements.register(PEACOCK, ground, height, Peacock::checkSpawn);
		SpawnPlacements.register(STINK_BUG, ground, height, StinkBug::checkSpawn);
		SpawnPlacements.register(MOSQUITO, air, height, Mosquito::checkSpawn);
		SpawnPlacements.register(DRAGONFLY, air, height, Dragonfly::checkSpawn);
		SpawnPlacements.register(GOLD_FISH, air, height, GoldFish::checkSpawn);
		SpawnPlacements.register(FLOUNDER, SpawnPlacementTypes.IN_WATER, height, Flounder::checkSpawn);
		SpawnPlacements.register(SKATE, SpawnPlacementTypes.IN_WATER, height, Skate::checkSpawn);
		SpawnPlacements.register(WHALE, SpawnPlacementTypes.IN_WATER, height, Whale::checkSpawn);
		SpawnPlacements.register(GAZELLE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Gazelle::checkSpawn);
		SpawnPlacements.register(OSTRICH, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Ostrich::checkSpawn);
		SpawnPlacements.register(LIZARD, SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, Lizard::checkSpawn);
		SpawnPlacements.register(CRAB, SpawnPlacementTypes.IN_WATER, height, Crab::checkSpawn);
		SpawnPlacements.register(SCORPION, SpawnPlacementTypes.ON_GROUND, height, Scorpion::checkSpawn);
		SpawnPlacements.register(SMALL_WORM, SpawnPlacementTypes.ON_GROUND, height, WormSmall::checkSpawn);
		SpawnPlacements.register(MEDIUM_WORM, SpawnPlacementTypes.ON_GROUND, height, WormMedium::checkSpawn);
		SpawnPlacements.register(LARGE_WORM, SpawnPlacementTypes.ON_GROUND, height, WormLarge::checkSpawn);

		// OreSpawn's spawn tables, "biome:weight:min:max".
		spawns(APPLE_COW, "plains:8:4:8 forest:8:4:8 megataiga:5:2:5 taiga:5:2:5 savanna:8:1:3 savannaplat:2:1:3");
		spawns(GOLDEN_APPLE_COW, "plains:5:2:6 forest:5:2:6 megataiga:5:2:5 taiga:5:2:5");
		spawns(ENCHANTED_GOLDEN_APPLE_COW, "forest:3:2:4 plains:3:2:4 megataiga:5:2:5 mushroom:15:3:6");
		spawns(BUTTERFLY, "beach:8:5:15 xhills:5:1:2 xhillsedge:5:1:2 forest:30:3:6 foresthills:20:2:5 jungle:20:2:5 junglehills:10:2:5 plains:20:3:6 river:20:2:5 swamp:20:4:10 birch:15:2:4 birchhills:15:2:5 megataiga:15:2:5 taiga:15:2:5 savanna:10:1:5 savannaplat:10:1:5");
		spawns(MOTH, "xhills:8:1:2 xhillsedge:8:1:2 foresthills:10:2:5 forest:20:3:6 jungle:20:3:6 junglehills:10:2:5 swamp:20:2:5 plains:10:1:5 birch:15:2:4 birchhills:15:2:5 roofed:15:2:5 megataiga:15:2:5 taiga:15:2:5 savanna:10:1:5 savannaplat:10:1:5");
		spawns(FIREFLY, "forest:15:5:10 foresthills:15:5:10 swamp:10:4:8 jungle:15:5:10 junglehills:15:5:10 stonebeach:10:3:6 birch:15:3:10 birchhills:15:3:10 megataiga:15:2:10 taiga:15:2:10 megataigahills:15:2:10 savanna:10:2:8 savannaplat:10:2:8");
		spawns(BIRD, "beach:10:2:5 xhills:10:1:2 xhillsedge:10:2:4 forest:25:5:10 foresthills:20:3:6 jungle:35:5:10 junglehills:25:5:10 plains:10:2:4 river:15:3:6 stonebeach:5:3:6 birch:15:3:6 birchhills:5:3:6 megataiga:15:2:5 taiga:15:2:5 savanna:11:1:5 savannaplat:11:1:5");
		spawns(CHIPMUNK, "forest:8:3:6 foresthills:5:3:6 jungle:4:3:6 plains:2:1:2 birch:5:3:6 birchhills:4:3:6 roofed:10:2:5 megataiga:2:2:5 taiga:6:2:5");
		spawns(CRICKET, "forest:3:2:4 foresthills:2:2:4 jungle:3:2:4 junglehills:2:3:5 plains:3:4:8 birch:2:2:6 birchhills:2:2:6 roofed:3:1:4 megataiga:2:1:6 taiga:2:1:6 savannaplat:1:1:4");
		spawns(FROG, "river:20:3:6 river:3:3:6 jungle:3:3:6 swamp:20:2:6 swamp:2:2:6");
		spawns(BEAVER, "river:10:2:4 forest:3:2:4 birch:2:2:4 birchhills:2:2:5 megataiga:5:2:5 taiga:5:2:5");
		spawns(CASSOWARY, "xhills:5:1:2 xhillsedge:5:1:2 xhillsplus:5:1:2 birch:5:2:4 birchhills:5:2:5 megataiga:15:2:5 megataigahills:15:2:5 savanna:3:1:5 savannaplat:10:1:5");
		spawns(RAT, "roofed:35:10:20 taiga:25:2:8");
		spawns(PEACOCK, "mesa:1:1:3 mesaplat:1:1:3");
		spawns(STINK_BUG, "forest:10:2:4 jungle:8:2:4 foresthills:6:2:4 junglehills:4:2:4 savanna:8:2:5");
		spawns(MOSQUITO, "swamp:30:5:10 jungle:20:5:10 junglehills:20:5:10 roofed:15:2:5");
		spawns(DRAGONFLY, "swamp:5:3:5 river:4:1:2");
		spawns(WHALE, "deepocean:1:1:2");
		spawns(OSTRICH, "desert:1:1:1 stonebeach:1:1:1 savanna:1:1:1 savannaplat:1:1:1");
		spawns(LIZARD, "river:5:2:4 swamp:4:2:4 ocean:2:2:4");
		spawns(CRAB, "ocean:2:3:6 swamp:1:3:6 stonebeach:1:2:4");
		spawns(SCORPION, "desert:15:3:6 roofed:28:2:4 savanna:15:3:5 savannaplat:15:2:4 mesa:6:1:3 mesaplat:4:1:3 mesaF:5:3:6");
		spawns(LARGE_WORM, "plains:25:1:1 savanna:15:1:1 savannaplat:10:1:1");
	}

	@SafeVarargs
	private static void attributes(AttributeSupplier.Builder builder, EntityType<? extends net.minecraft.world.entity.LivingEntity>... types) {
		for (var t : types) FabricDefaultAttributeRegistry.register(t, builder);
	}

	private record Entry(int weight, int min, int max) {}

	/** Registers a non-mob entity (a projectile, a thrown thing, a part): no spawn egg. */
	public static <T extends net.minecraft.world.entity.Entity> EntityType<T> misc(String name, EntityType.EntityFactory<T> factory, float w, float h,
			int range, int updateInterval) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, OreSpawn.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, EntityType.Builder.of(factory, MobCategory.MISC).sized(w, h)
			.clientTrackingRange(range).updateInterval(updateInterval).build(key));
	}

	/** Adds OreSpawn's spawn table ("biome:weight:min:max ...", its 1.7.10 biome names) to the matching 26.3 biomes. */
	public static void spawns(EntityType<? extends Mob> type, String table) {
		Map<String, Entry> byBiome = new LinkedHashMap<>();
		for (String row : table.trim().split("\\s+")) {
			String[] f = row.split(":");
			Entry e = new Entry(Integer.parseInt(f[1]), Integer.parseInt(f[2]), Integer.parseInt(f[3]));
			for (String biome : BIOMES.get(f[0])) byBiome.merge(biome, e, (a, b) -> a.weight >= b.weight ? a : b);
		}
		for (var e : byBiome.entrySet()) {
			ResourceKey<Biome> biome = ResourceKey.create(Registries.BIOME, Identifier.withDefaultNamespace(e.getKey()));
			BiomeModifications.addSpawn(BiomeSelectors.includeByKey(biome), type.getCategory(), type, e.getValue().weight, e.getValue().min,
				e.getValue().max);
		}
	}
}
