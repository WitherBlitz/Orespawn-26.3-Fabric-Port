package orespawn.content;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import orespawn.OreSpawn;
import orespawn.block.dungeons.DungeonSpawnerBlock;
import orespawn.item.dungeons.RandomDungeonItem;
import orespawn.registry.ModBlocks;
import orespawn.registry.ModItems;
import orespawn.world.dungeons.Deferred;
import orespawn.world.dungeons.EndDungeonsFeature;
import orespawn.original.OriginalDungeons;

/**
 * OreSpawn's dungeons (GenericDungeon, BasiliskMaze, RubyBirdDungeon, NightmareDungeon and OreSpawnWorld's dungeon
 * placement), ported in orespawn.world.dungeons, and the Random Dungeon item and its spawner block.
 * <p>
 * Where they're built:
 * <ul>
 * <li>The End: the {@code orespawn:end_dungeons} feature (OreSpawnWorld.generateEnd).</li>
 * <li>The overworld's (OreSpawnWorld.generateSurface) are the Structures group's (orespawn.world.structures); the
 * builders here are still used by the Random Dungeon.</li>
 * <li>OreSpawn's dimensions: their groups call {@link orespawn.world.dungeons.OreSpawnDungeons}. OreSpawnWorld.generate
 * tried, per chunk: Utopia, addKingAltar (only when no tree was placed, and recently_placed is 0) then addRubyDungeon,
 * else addGenericDungeon; the Mining dimension, when recently_placed is 0 and 1 in 95, one of addBasiliskMaze,
 * addKyuubiDungeon, addBeeHive, addShadowDungeon, addAlienWTF, addEnderKnight, addLeonNest (1 in 7 each), else
 * addGenericDungeon; Village Mania, addGenericDungeon, then addDamselInDistress, addSpiderHangout and
 * addRedAntHangout each while recently_placed is 0; the Islands, when recently_placed is 0, 1 in 100 and
 * D4BigSpaceCheck(chunk, 7) passes, i = 1 in 19: 0-2 addD4Castle, 3-6 addD4GenericDungeon, 7 addD4EnderCastle, 8
 * addD4IncaPyramid, 9 addD4RobotLab, 10 addD4Mini, 11 addD4RubyDungeon, 12 addD4CephadromeAltar, 13 addD4Greenhouse,
 * 14 addD4NightmareRookery, 15 addD4StinkyHouse, 16 addD4WhiteHouse, 17 addPumpkin, 18 addD4Rainbow, and separately 1
 * in 300 addD4CloudShark; the Crystal world, while recently_placed is 0 (and no fairy tree was placed),
 * addRotatorStation, else addUrchinSpawner, else addCrystalHauntedHouse, else addRoundRotator, else
 * addCrystalBattleTower.</li>
 * <li>Anywhere: the Random Dungeon item.</li>
 * </ul>
 * OreSpawn's recently_placed (chunks to wait after a structure) counts down once per chunk generated in any dimension,
 * as OreSpawnWorld.generate did.
 */
public final class Dungeons {
	/** DungeonSpawnerBlock: BlockReed-like (no collision, breaks instantly, grass sounds), light 0.9. */
	public static final Block RANDOM_DUNGEON_SPAWNER = ModBlocks.block("random_dungeon_spawner", DungeonSpawnerBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(s -> 13)
			.randomTicks().pushReaction(PushReaction.POPPED));
	public static final Item RANDOM_DUNGEON = ModItems.register("random_dungeon",
		new RandomDungeonItem(ModItems.props("random_dungeon").stacksTo(1)));

	private Dungeons() {
	}

	public static void init() {
		Registry.register(BuiltInRegistries.FEATURE_TYPE, OreSpawn.id("end_dungeons"), EndDungeonsFeature.CODEC);
		BiomeModifications.addFeature(BiomeSelectors.foundInTheEnd(), GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
			ResourceKey.create(Registries.PLACED_FEATURE, OreSpawn.id("end_dungeons")));
		Deferred.init();
		ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
			if (generated && OriginalDungeons.recentlyPlaced() > 0) {
				OriginalDungeons.setRecentlyPlaced(OriginalDungeons.recentlyPlaced() - 1);
			}
		});
	}
}
