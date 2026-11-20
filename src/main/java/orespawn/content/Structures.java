package orespawn.content;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import orespawn.OreSpawn;
import orespawn.world.structures.BouncyCastleFeature;
import orespawn.world.structures.FrogPondFeature;
import orespawn.world.structures.GoldFishBowlFeature;
import orespawn.world.structures.HauntedHouseFeature;
import orespawn.world.structures.IglooFeature;
import orespawn.world.structures.LeafMonsterFeature;
import orespawn.world.structures.MonsterIslandFeature;
import orespawn.world.structures.NestFeature;
import orespawn.world.structures.PlayPoolFeature;
import orespawn.world.structures.RubberDuckyPondFeature;
import orespawn.world.structures.SpitBugLairFeature;
import orespawn.world.structures.WaterDragonLairFeature;

import java.util.Arrays;

/**
 * OreSpawn's overworld structures (OreSpawnWorld.generateSurface in the overworld) as 26.3 features: the ocean's play
 * pool, Water Dragon lair, gold fish bowl and Monster Island; the plains' frog pond, Leaf Monster tree and Rubber Ducky
 * pond; the forests' Bee and Mantis nests; the haunted house, Spit Bug lair, igloo and bouncy castle. Their configured
 * and placed features are data files ({@code data/orespawn/worldgen}, written by gen_assets.py from
 * tools/content/structures.json). OreSpawnWorld.addRocks belongs to the Rock's group (orespawn.entity.tools.RockFeature).
 * <p>
 * Odds per chunk are OreSpawn's (default config): each chunk picked one of six water structures (rolled 1 in 6), which
 * then rolled 1 in 350 (1 in 300 for the islands), so the play pool, lair, bowl and frog pond are 1 in 2100 and Monster
 * Island 1 in 1800; the nest, haunted house, Leaf Monster, Spit Bug, igloo, bouncy castle and Rubber Ducky rolled 1 in
 * 230, 285, 275, 190, 220, 230 and 275. Each looks at the biome at the chunk's corner, as OreSpawn did. OreSpawn
 * compared exact 1.7.10 biome names, so variants with other names (Sunflower Plains, Flower Forest...) never matched;
 * each name maps to the 26.3 biomes the original became. They're placed after everything else in the chunk (top layer
 * modification), since OreSpawn's world generator ran after vanilla's decoration.
 * <p>
 * Not ported: OreSpawn's "recently_placed" counter, which held off any structure for the next 50 chunks generated after
 * one was built. 26.3 generates chunks in parallel, so a counter in generation order can't be kept; at these odds it
 * changed how often structures appear by a few percent. For the same reason the haunted house, Leaf Monster, Spit Bug
 * and Rubber Ducky no longer rule each other out in one chunk, nor do the six water structures (now two can share about
 * one plains chunk in 25,000). The Girlfriend Island is left out at the user's request.
 */
public final class Structures {
	private static final String[] OCEAN = {"ocean", "lukewarm_ocean", "warm_ocean", "cold_ocean"};
	private static final String[] PLAINS = {"plains"};

	private Structures() {
	}

	public static void init() {
		feature("play_pool", PlayPoolFeature.CODEC, OCEAN);
		feature("water_dragon_lair", WaterDragonLairFeature.CODEC, OCEAN);
		feature("gold_fish_bowl", GoldFishBowlFeature.CODEC, OCEAN);
		feature("monster_island", MonsterIslandFeature.CODEC, OCEAN);
		feature("frog_pond", FrogPondFeature.CODEC, PLAINS);
		// Forest, ForestHills, Jungle, JungleHills, Birch Forest, Birch Forest Hills.
		feature("nest", NestFeature.CODEC, "forest", "jungle", "bamboo_jungle", "birch_forest");
		// Plains, Taiga, Swampland.
		feature("haunted_house", HauntedHouseFeature.CODEC, "plains", "taiga", "swamp");
		feature("leaf_monster_tree", LeafMonsterFeature.CODEC, PLAINS);
		feature("spit_bug_lair", SpitBugLairFeature.CODEC, "swamp");
		// Ice Plains.
		feature("igloo", IglooFeature.CODEC, "snowy_plains");
		feature("bouncy_castle", BouncyCastleFeature.CODEC, "desert");
		feature("rubber_ducky_pond", RubberDuckyPondFeature.CODEC, PLAINS);
	}

	/** Registers the feature type {@code orespawn:<id>} and adds placed feature {@code orespawn:<id>} to the biomes. */
	private static void feature(String id, MapCodec<? extends Feature> codec, String... biomes) {
		Registry.register(BuiltInRegistries.FEATURE_TYPE, OreSpawn.id(id), codec);
		ResourceKey<PlacedFeature> placed = ResourceKey.create(Registries.PLACED_FEATURE, OreSpawn.id(id));
		BiomeModifications.addFeature(BiomeSelectors.includeByKey(Arrays.stream(biomes)
				.map(b -> ResourceKey.create(Registries.BIOME, Identifier.withDefaultNamespace(b))).toList()),
			GenerationStep.Decoration.TOP_LAYER_MODIFICATION, placed);
	}
}
