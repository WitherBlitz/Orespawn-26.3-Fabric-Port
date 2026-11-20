package orespawn.world.dungeons;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import orespawn.original.OriginalDungeons;

/**
 * OreSpawn's dungeons, for other code (the dimension groups' world generation, commands, items).
 * <p>
 * The plain methods build one dungeon at a position, exactly as OreSpawn's GenericDungeon (and BasiliskMaze,
 * RubyBirdDungeon, NightmareDungeon) did. The {@code add...} methods are OreSpawnWorld's placement: OreSpawn's odds,
 * its biome checks and its search for ground in the given chunk, then the build; they return whether they built.
 * OreSpawnWorld.generate decides, per dimension, which of them to try in each chunk (see the Dungeons content class
 * for which dimension calls which), and most of them check and set {@link OriginalDungeons#recentlyPlaced}.
 * <p>
 * {@code level} is a ServerLevel, or the WorldGenLevel a feature or chunk generator is placing into: parts of a big
 * dungeon outside the chunks world generation may write to are placed when those chunks load (see McWorld).
 * OreSpawn's own code does the building, run from the original OreSpawn 1.7.10 file by {@link OriginalDungeons}.
 */
public final class OreSpawnDungeons {
	private OreSpawnDungeons() {
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeAlienWTFDungeon. */
	public static void alienWTFDungeon(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeAlienWTFDungeon", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's BasiliskMaze.buildBasiliskMaze. */
	public static void basiliskMaze(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("BasiliskMaze", "buildBasiliskMaze", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeBeeHive. */
	public static void beeHive(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeBeeHive", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeBouncyCastle. */
	public static void bouncyCastle(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeBouncyCastle", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeCephadromeAltar. */
	public static void cephadromeAltar(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeCephadromeAltar", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeCloudSharkDungeon. */
	public static void cloudSharkDungeon(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeCloudSharkDungeon", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeCrystalBattleTower. */
	public static void crystalBattleTower(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeCrystalBattleTower", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeCrystalHauntedHouse. */
	public static void crystalHauntedHouse(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeCrystalHauntedHouse", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeDamselInDistress. */
	public static void damselInDistress(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeDamselInDistress", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeEnderCastle. */
	public static void enderCastle(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeEnderCastle", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeEnderDragonHospital. */
	public static void enderDragonHospital(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeEnderDragonHospital", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeEnderKnightDungeon. */
	public static void enderKnightDungeon(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeEnderKnightDungeon", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeEnderReaperGraveyard. */
	public static void enderReaperGraveyard(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeEnderReaperGraveyard", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeEnormousCastle. */
	public static void enormousCastle(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeEnormousCastle", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeEnormousCastleQ. */
	public static void enormousCastleQ(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeEnormousCastleQ", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeFrogPond. */
	public static void frogPond(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeFrogPond", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeDungeon. */
	public static void genericDungeon(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeDungeon", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeGirlfriendIsland. */
	public static void girlfriendIsland(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeGirlfriendIsland", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeGoldFishBowl. */
	public static void goldFishBowl(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeGoldFishBowl", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeGreenhouseDungeon. */
	public static void greenhouseDungeon(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeGreenhouseDungeon", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeHauntedHouse. */
	public static void hauntedHouse(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeHauntedHouse", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeIgloo. */
	public static void igloo(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeIgloo", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeIncaPyramid. */
	public static void incaPyramid(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeIncaPyramid", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeKingAltar. */
	public static void kingAltar(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeKingAltar", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeKyuubiDungeon. */
	public static void kyuubiDungeon(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeKyuubiDungeon", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeLeafMonsterDungeon. */
	public static void leafMonsterDungeon(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeLeafMonsterDungeon", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeLeonNest. */
	public static void leonNest(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeLeonNest", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeMantisHive. */
	public static void mantisHive(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeMantisHive", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeMiniDungeon. */
	public static void miniDungeon(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeMiniDungeon", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeMonsterIsland. */
	public static void monsterIsland(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeMonsterIsland", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's NightmareDungeon.makeDungeon. */
	public static void nightmareDungeon(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("NightmareDungeon", "makeDungeon", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeNightmareRookery. */
	public static void nightmareRookery(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeNightmareRookery", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makePlayPool. */
	public static void playPool(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makePlayPool", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makePumpkin. */
	public static void pumpkin(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makePumpkin", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeQueenAltar. */
	public static void queenAltar(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeQueenAltar", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeRainbow. */
	public static void rainbow(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeRainbow", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeRedAntHangout. */
	public static void redAntHangout(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeRedAntHangout", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeRobotLab. */
	public static void robotLab(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeRobotLab", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeRotatorStation. */
	public static void rotatorStation(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeRotatorStation", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeRoundRotator. */
	public static void roundRotator(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeRoundRotator", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeRubberDuckyPond. */
	public static void rubberDuckyPond(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeRubberDuckyPond", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's RubyBirdDungeon.makeDungeon. */
	public static void rubyBirdDungeon(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("RubyBirdDungeon", "makeDungeon", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeShadowDungeon. */
	public static void shadowDungeon(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeShadowDungeon", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeSmallBeeHive. */
	public static void smallBeeHive(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeSmallBeeHive", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeSpiderHangout. */
	public static void spiderHangout(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeSpiderHangout", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeSpitBugLair. */
	public static void spitBugLair(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeSpitBugLair", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeStinkyHouse. */
	public static void stinkyHouse(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeStinkyHouse", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeUrchinSpawner. */
	public static void urchinSpawner(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeUrchinSpawner", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeWaterDragonLair. */
	public static void waterDragonLair(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeWaterDragonLair", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** Builds it at pos, with OreSpawn's GenericDungeon.makeWhiteHouse. */
	public static void whiteHouse(WorldGenLevel level, BlockPos pos, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.build("GenericDungeon", "makeWhiteHouse", w, pos.getX(), pos.getY(), pos.getZ());
			return null;
		});
	}

	/** OreSpawnWorld.addEndKnights: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static void addEndKnights(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.place("addEndKnights", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ());
			return null;
		});
	}

	/** OreSpawnWorld.addEndReapers: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static void addEndReapers(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.place("addEndReapers", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ());
			return null;
		});
	}

	/** OreSpawnWorld.addHospital: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static void addHospital(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.place("addHospital", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ());
			return null;
		});
	}

	/** OreSpawnWorld.addEnderCastle: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static void addEnderCastle(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.place("addEnderCastle", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ());
			return null;
		});
	}

	/** OreSpawnWorld.addDamselInDistress: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addDamselInDistress(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addDamselInDistress", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addSpiderHangout: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addSpiderHangout(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addSpiderHangout", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addRedAntHangout: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addRedAntHangout(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addRedAntHangout", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addRotatorStation: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addRotatorStation(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addRotatorStation", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addRoundRotator: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addRoundRotator(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addRoundRotator", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addUrchinSpawner: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addUrchinSpawner(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addUrchinSpawner", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addCrystalHauntedHouse: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addCrystalHauntedHouse(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addCrystalHauntedHouse", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addCrystalBattleTower: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addCrystalBattleTower(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addCrystalBattleTower", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addRubyDungeon: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addRubyDungeon(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addRubyDungeon", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addGenericDungeon: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addGenericDungeon(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addGenericDungeon", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addBeeHive: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addBeeHive(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addBeeHive", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addAlienWTF: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addAlienWTF(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addAlienWTF", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addEnderKnight: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addEnderKnight(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addEnderKnight", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addLeonNest: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addLeonNest(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addLeonNest", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addShadowDungeon: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addShadowDungeon(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addShadowDungeon", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addD4RubyDungeon: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addD4RubyDungeon(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addD4RubyDungeon", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addD4CephadromeAltar: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addD4CephadromeAltar(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addD4CephadromeAltar", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addD4Castle: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addD4Castle(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addD4Castle", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addD4Greenhouse: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addD4Greenhouse(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addD4Greenhouse", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addD4NightmareRookery: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addD4NightmareRookery(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addD4NightmareRookery", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addD4StinkyHouse: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addD4StinkyHouse(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addD4StinkyHouse", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addD4WhiteHouse: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addD4WhiteHouse(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addD4WhiteHouse", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addD4EnderCastle: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addD4EnderCastle(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addD4EnderCastle", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addD4IncaPyramid: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addD4IncaPyramid(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addD4IncaPyramid", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addD4RobotLab: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addD4RobotLab(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addD4RobotLab", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addD4Mini: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addD4Mini(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addD4Mini", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addPumpkin: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addPumpkin(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addPumpkin", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addD4CloudShark: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addD4CloudShark(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addD4CloudShark", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addD4Rainbow: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addD4Rainbow(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addD4Rainbow", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addD4GenericDungeon: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addD4GenericDungeon(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addD4GenericDungeon", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addKingAltar: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static boolean addKingAltar(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.place("addKingAltar", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ()));
	}

	/** OreSpawnWorld.addBasiliskMaze: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static void addBasiliskMaze(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.place("addBasiliskMaze", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ());
			return null;
		});
	}

	/** OreSpawnWorld.addKyuubiDungeon: OreSpawn's odds and search for ground in this chunk, then the build. */
	public static void addKyuubiDungeon(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		McWorld.run(level, random, w -> {
			OriginalDungeons.place("addKyuubiDungeon", w, w.rand, chunk.getMinBlockX(), chunk.getMinBlockZ());
			return null;
		});
	}

	/**
	 * OreSpawnWorld.D4BigSpaceCheck: whether the 65 x 55 square at y + 4 around (x, z) is clear (air, logs and apple or
	 * scary leaves allowed). The Islands tried their structures only where it passed, at (chunk x, 7, chunk z).
	 */
	public static boolean d4BigSpaceCheck(WorldGenLevel level, int x, int y, int z, RandomSource random) {
		return McWorld.run(level, random, w -> OriginalDungeons.check("D4BigSpaceCheck", w, x, y, z));
	}
}
