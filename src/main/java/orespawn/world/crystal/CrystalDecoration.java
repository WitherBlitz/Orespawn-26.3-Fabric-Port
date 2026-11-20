package orespawn.world.crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.Container;
import net.minecraft.world.level.chunk.ChunkGenerator;
import orespawn.content.Crystal;
import orespawn.entity.tools.RockFeature;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * OreSpawnWorld.generate's Crystal dimension branch, run as each chunk is decorated, with Forge's per-chunk random
 * (GameRegistry.generateWorld's seed). In order: a fairy tree or fairy castle tree (1 chunk in 5, on a clear patch of
 * Crystal Grass); otherwise termite nests (1 in 40) and, if no structure was placed in the last 50 chunks, one of the
 * rotator station (1 in 150), urchin spawner (1 in 180), crystal haunted house (1 in 230), round rotator (1 in 150) or
 * crystal battle tower (1 in 280), and an Irukandji spawner on the water (1 in 80); then a chest or spawner in the maze
 * at y 25, and in 1 chunk in 20 a few Rocks (the Tools group's RockFeature). OreSpawn's 50-chunk pause was shared
 * with its other dimensions; here it counts the Crystal dimension's chunks only.
 */
public final class CrystalDecoration {
	/** OreSpawnWorld.recently_placed. */
	private static final AtomicInteger RECENTLY_PLACED = new AtomicInteger(50);

	private CrystalDecoration() {
	}

	public static void decorate(WorldGenLevel level, ChunkGenerator generator, ChunkPos chunk) {
		long seed = level.getSeed();
		Random fml = new Random(seed);
		long xSeed = fml.nextLong() >> 2 + 1L;
		long zSeed = fml.nextLong() >> 2 + 1L;
		long chunkSeed = (xSeed * chunk.x() + zSeed * chunk.z()) ^ seed;
		Random random = new Random(chunkSeed);
		// world.rand, which OreSpawn's builders used: seeded from the chunk too, so a chunk always comes out the same.
		Random worldRand = new Random(chunkSeed ^ 0x2545F4914F6CDD1DL);
		RegionVoxels w = new RegionVoxels(level);
		int x = chunk.getMinBlockX();
		int z = chunk.getMinBlockZ();
		RECENTLY_PLACED.getAndUpdate(v -> v > 0 ? v - 1 : 0);

		if (!fairyTree(w, random, worldRand, x, z)) {
			termites(w, random, x, z);
			if (RECENTLY_PLACED.get() == 0) {
				if (!structure(w, random, worldRand, x, z, 150, CrystalStructures::rotatorStation)
					&& !structure(w, random, worldRand, x, z, 180, CrystalStructures::urchinSpawner)
					&& !structure(w, random, worldRand, x, z, 230, CrystalStructures::crystalHauntedHouse)
					&& !structure(w, random, worldRand, x, z, 150, CrystalStructures::roundRotator)) {
					structure(w, random, worldRand, x, z, 280, CrystalStructures::crystalBattleTower);
				}
				irukandji(w, random, x, z);
			}
		}
		chestsAndSpawners(w, random, worldRand, x, z);
		// addRocks, the Tools group's RockFeature (its own 1-in-5 roll and its search from y 110 down to 41).
		if (worldRand.nextInt(4) == 1) new RockFeature().place(level, generator, RandomSource.create(random.nextLong()), new BlockPos(x, 0, z));
	}

	/** Air with Crystal Grass under it, searching down from y 100 to 51; the y of the air, or -1. */
	private static int grassBelow(Voxels w, int x, int z) {
		for (int y = 100; y > 50; y--) {
			if (w.isAir(x, y, z) && w.get(x, y - 1, z) == Crystal.CRYSTAL_GRASS) return y;
		}
		return -1;
	}

	/** addFairyTree. Returns whether the chunk's 1-in-5 roll came up (even if no tree fitted), as OreSpawn's did. */
	private static boolean fairyTree(RegionVoxels w, Random random, Random worldRand, int chunkX, int chunkZ) {
		int x = chunkX + 8;
		int z = chunkZ + 8;
		if (random.nextInt(5) != 0) return false;
		for (int y = 128; y > 40; y--) {
			if (w.isAir(x, y, z) && w.get(x, y - 1, z) == Crystal.CRYSTAL_GRASS) {
				for (int i = -8; i <= 8; i++) {
					for (int j = -8; j <= 8; j++) {
						if (!w.isAir(x + i, y, z + j)) return false;
					}
				}
				for (int i = -2; i <= 2; i++) {
					for (int j = -2; j <= 2; j++) {
						if (w.get(x + i, y - 1, z + j) != Crystal.CRYSTAL_GRASS) return false;
					}
				}
				if (random.nextInt(5) != 1) {
					CrystalStructures.fairyTree(w, worldRand, x, y - 1, z);
				} else {
					CrystalStructures.fairyCastleTree(w, worldRand, x, y, z);
				}
				RECENTLY_PLACED.set(50);
				break;
			}
		}
		return true;
	}

	/** addCrystalTermites: in 1 chunk in 40, three tries at turning a patch of Crystal Grass into a termite nest. */
	private static void termites(Voxels w, Random random, int chunkX, int chunkZ) {
		if (random.nextInt(40) != 0) return;
		for (int i = 0; i < 3; i++) {
			int x = chunkX + random.nextInt(16);
			int z = chunkZ + random.nextInt(16);
			int y = grassBelow(w, x, z);
			if (y >= 0) w.set(x, y - 1, z, Crystal.CRYSTAL_TERMITE_NEST);
		}
	}

	interface Builder {
		void build(RegionVoxels w, Random random, int x, int y, int z);
	}

	/** The pattern of addRotatorStation and its siblings: a 1-in-n roll, then three tries for a spot on Crystal Grass. */
	private static boolean structure(RegionVoxels w, Random random, Random worldRand, int chunkX, int chunkZ, int rarity, Builder builder) {
		if (random.nextInt(rarity) != 0) return false;
		for (int i = 0; i < 3; i++) {
			int x = chunkX + random.nextInt(16);
			int z = chunkZ + random.nextInt(16);
			int y = grassBelow(w, x, z);
			if (y >= 0) {
				builder.build(w, worldRand, x, y, z);
				RECENTLY_PLACED.set(50);
				return true;
			}
		}
		return false;
	}

	/** addIrukandji: in 1 chunk in 80, an Irukandji spawner floating on the water. */
	private static void irukandji(RegionVoxels w, Random random, int chunkX, int chunkZ) {
		if (random.nextInt(80) != 0) return;
		for (int i = 0; i < 3; i++) {
			int x = chunkX + random.nextInt(16);
			int z = chunkZ + random.nextInt(16);
			for (int y = 100; y > 50; y--) {
				if (w.isAir(x, y, z) && w.get(x, y - 1, z) == Blocks.WATER) {
					w.spawner(x, y, z, "irukandji");
					return;
				}
			}
		}
	}

	/** addCrystalChestsAndSpawners: at the first open spot of three in the maze (y 25), a chest (1 in 3) or a spawner. */
	private static void chestsAndSpawners(RegionVoxels w, Random random, Random worldRand, int chunkX, int chunkZ) {
		for (int i = 0; i < 3; i++) {
			int x = 1 + chunkX + random.nextInt(14);
			int z = 1 + chunkZ + random.nextInt(14);
			int y = 25;
			if (!w.isAir(x, y, z)) continue;
			if (w.isAir(x + 1, y, z)) {
				crystalChest(w, worldRand, x, y, z, 5);
			} else if (w.isAir(x - 1, y, z)) {
				crystalChest(w, worldRand, x, y, z, 4);
			} else if (w.isAir(x, y, z + 1)) {
				crystalChest(w, worldRand, x, y, z, 2);
			} else if (w.isAir(x, y, z - 1)) {
				crystalChest(w, worldRand, x, y, z, 3);
			}
			break;
		}
	}

	/** addCrystalChest: a chest of crystal loot, or a Dungeon Beast or Rat spawner. */
	private static void crystalChest(RegionVoxels w, Random random, int x, int y, int z, int dir) {
		if (random.nextInt(3) == 0) {
			Container chest = w.chest(x, y, z, dir);
			if (chest != null) CrystalLoot.fill(chest, random, CrystalLoot.CRYSTAL, 1 + random.nextInt(3));
		} else {
			w.spawner(x, y, z, random.nextInt(2) == 0 ? "dungeon_beast" : "rat");
		}
	}
}
