package orespawn.world.crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import orespawn.content.Crystal;
import org.jspecify.annotations.Nullable;

import java.util.Random;

/**
 * The Crystal dimension's builds, block for block as OreSpawn made them: the fairy tree and fairy castle tree
 * (Trees.FairyTree / FairyCastleTree) and GenericDungeon's rotator station, urchin spawner, crystal haunted house,
 * crystal battle tower and round rotator. {@code random} stands for the world random OreSpawn used. Creatures, spawn
 * eggs and items other groups own are looked up by id (an empty spawner, or no item, until they exist).
 */
public final class CrystalStructures {
	private CrystalStructures() {
	}

	/**
	 * Trees.FairyTree at OreSpawn's coordinates: {@code pos} is the ground block, the 2 x 2 trunk starts above it (the
	 * Fairy spawner and chest at its foot). Works in any level, generating or live; always builds.
	 */
	public static boolean fairyTree(WorldGenLevel level, BlockPos pos, RandomSource random) {
		fairyTree(new RegionVoxels(level), new Random(random.nextLong()), pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	/** Trees.FairyCastleTree at OreSpawn's coordinates: {@code pos} is the air block above the ground. Always builds. */
	public static boolean fairyCastleTree(WorldGenLevel level, BlockPos pos, RandomSource random) {
		fairyCastleTree(new RegionVoxels(level), new Random(random.nextLong()), pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	private static void put(@Nullable Container chest, int slot, String item, int count) {
		if (chest == null) return;
		var it = CrystalLoot.item(item.contains(":") ? item : "orespawn:" + item);
		if (it != net.minecraft.world.item.Items.AIR) chest.setItem(slot, new ItemStack(it, count));
	}

	// ---- Trees.FairyTree / FairyCastleTree ----

	/** FairyTree: a 2 x 2 trunk with eight long branches, blue leaves, a Fairy spawner and a chest at its foot. */
	static void fairyTree(RegionVoxels w, Random random, int x, int y, int z) {
		Block log = Crystal.CRYSTAL_TREE_WOOD;
		for (int j = 1; j < 6; j++) {
			for (int i = 0; i < 2; i++) {
				for (int k = 0; k < 2; k++) w.set(x + i, y + j, z + k, log);
			}
		}
		branch(w, random, x, y + 5, z, 0, 1, 1, 1, -1);
		branch(w, random, x + 1, y + 5, z, 1, 0, 1, -1, -1);
		branch(w, random, x, y + 5, z + 1, -1, 0, -1, 1, -1);
		branch(w, random, x + 1, y + 5, z + 1, 0, -1, -1, -1, -1);
		branch(w, random, x, y + 6, z, 0, 1, -1, 1, -1);
		branch(w, random, x + 1, y + 6, z, 1, 0, 1, 1, -1);
		branch(w, random, x, y + 6, z + 1, -1, 0, -1, -1, -1);
		branch(w, random, x + 1, y + 6, z + 1, 0, -1, 1, -1, -1);
		int grow = 5 + random.nextInt(5);
		for (int j = 6; j < 6 + grow; j++) {
			for (int i = 0; i < 2; i++) {
				for (int k = 0; k < 2; k++) {
					w.set(x + i, y + j, z + k, log);
					leaves(w, x + i, y + j, z + k);
				}
			}
		}
		w.spawner(x - 1, y + 1, z, "fairy");
		Container chest = w.chest(x + 2, y + 1, z, 0);
		if (chest != null) CrystalLoot.fill(chest, random, CrystalLoot.CRYSTAL, 1 + random.nextInt(5));
	}

	/** make_crystal_leaves: blue leaves in the air around a fairy tree's wood. */
	private static void leaves(Voxels w, int x, int y, int z) {
		for (int l1 = -2; l1 <= 2; l1++) {
			for (int l2 = -2; l2 <= 2; l2++) {
				for (int l3 = 0; l3 <= 1; l3++) {
					if (w.isAir(x + l1, y + l3, z + l2)) w.set(x + l1, y + l3, z + l2, Crystal.BLUE_CRYSTAL_TREE_LEAVES);
				}
			}
		}
	}

	/** make_crystal_castle_leaves: yellow leaves level with the platform's rim, blue ones above. */
	private static void castleLeaves(Voxels w, int x, int y, int z) {
		for (int l1 = -1; l1 <= 1; l1++) {
			for (int l2 = -1; l2 <= 1; l2++) {
				for (int l3 = 0; l3 <= 1; l3++) {
					if (w.isAir(x + l1, y + l3, z + l2)) {
						w.set(x + l1, y + l3, z + l2, l3 != 0 ? Crystal.BLUE_CRYSTAL_TREE_LEAVES : Crystal.YELLOW_CRYSTAL_TREE_LEAVES);
					}
				}
			}
		}
	}

	/** grow_crystal_branch. */
	private static void branch(Voxels w, Random random, int x, int y, int z, int xdir, int zdir, int xxdir, int zzdir, int ydir) {
		Block log = Crystal.CRYSTAL_TREE_WOOD;
		int i2 = 0;
		int k2 = 0;
		int i = x;
		int j = y;
		int k = z;
		int grow = 4 + random.nextInt(4);
		for (int n = 0; n < grow; n++) {
			w.set(i, j, k, log);
			leaves(w, i, j, k);
			j++;
			i += xdir;
			k += zdir;
			i2 = i;
			k2 = k;
		}
		grow = 5 + random.nextInt(5);
		for (int n = 0; n < grow; n++) {
			w.set(i, j, k, log);
			leaves(w, i, j, k);
			i += xdir;
			k += zdir;
		}
		grow = 5 + random.nextInt(5);
		for (int n = 0; n < grow; n++) {
			w.set(i2, j, k2, log);
			leaves(w, i2, j, k2);
			i2 += xxdir;
			k2 += zzdir;
		}
		int j2 = --j;
		grow = 4 + random.nextInt(4);
		for (int n = 0; n < grow; n++) {
			w.set(i, j, k, log);
			leaves(w, i, j, k);
			i += xdir;
			k += zdir;
			j += ydir;
		}
		grow = 4 + random.nextInt(4);
		for (int n = 0; n < grow; n++) {
			w.set(i2, j2, k2, log);
			leaves(w, i2, j2, k2);
			i2 += xxdir;
			k2 += zzdir;
			j2 += ydir;
		}
	}

	/** addSomething: a Fairy spawner (1 in 3) or a chest (1 in 3) on top of a castle platform's centre. */
	private static void something(RegionVoxels w, Random random, int x, int y, int z) {
		int i = random.nextInt(3);
		if (i == 1) w.spawner(x, y + 1, z, "fairy");
		if (i == 2) {
			Container chest = w.chest(x, y + 1, z, 0);
			if (chest != null) CrystalLoot.fill(chest, random, CrystalLoot.CRYSTAL, 1 + random.nextInt(5));
		}
	}

	/** One square platform of a fairy castle tree: wood, a leafy rim, torches on the corners, maybe a prize. */
	private static void platform(RegionVoxels w, Random random, int cx, int y, int cz, int width, boolean prize) {
		BlockState torch = Crystal.CRYSTAL_TORCH.defaultBlockState();
		for (int i = -width; i <= width; i++) {
			for (int k = -width; k <= width; k++) {
				w.set(cx + i, y, cz + k, Crystal.CRYSTAL_TREE_WOOD);
				if (i == -width || i == width || k == -width || k == width) castleLeaves(w, cx + i, y, cz + k);
				if (prize && i == 0 && k == 0) something(w, random, cx + i, y, cz + k);
				if ((i == -width || i == width) && (k == -width || k == width)) w.set(cx + i, y + 1, cz + k, torch);
			}
		}
	}

	/** FairyCastleTree: six storeys of platforms spreading out in all eight directions, with fairies and chests. */
	static void fairyCastleTree(RegionVoxels w, Random random, int x, int y, int z) {
		int nc = 6;
		int j = 3 + random.nextInt(3);
		int spread = 0;
		for (int iter = 0; iter < nc; iter++) {
			int grow = 4 + random.nextInt(3);
			int width = 1 + random.nextInt(3);
			int randy = random.nextInt(3) - 1;
			platform(w, random, x + spread, y + j + randy, z, width, iter != 0);
			if (iter != 0) {
				width = 1 + random.nextInt(3 + iter);
				randy = random.nextInt(3) - 1;
				platform(w, random, x - spread, y + j + randy, z, width, true);
				width = 1 + random.nextInt(3 + iter);
				randy = random.nextInt(3) - 1;
				platform(w, random, x, y + j + randy, z + spread, width, true);
				width = 1 + random.nextInt(3 + iter);
				randy = random.nextInt(3) - 1;
				platform(w, random, x, y + j + randy, z - spread, width, true);
			}
			if (iter >= 2) {
				width = 1 + random.nextInt(3 + iter);
				randy = random.nextInt(3) - 1;
				platform(w, random, x + spread, y + j + randy, z + spread, width, true);
				width = 1 + random.nextInt(3 + iter);
				randy = random.nextInt(3) - 1;
				platform(w, random, x - spread, y + j + randy, z - spread, width, true);
				width = 1 + random.nextInt(3 + iter);
				randy = random.nextInt(3) - 1;
				platform(w, random, x - spread, y + j + randy, z + spread, width, true);
				width = 1 + random.nextInt(3 + iter);
				randy = random.nextInt(3) - 1;
				platform(w, random, x + spread, y + j + randy, z - spread, width, true);
			}
			j += grow;
			if (iter == 0) spread = 3;
			spread += grow;
		}
	}

	// ---- GenericDungeon ----

	/** makeRotatorStation: two Rotator spawners on a Kyanite post, a chest of Rotator eggs and Crystal Energy on top. */
	static void rotatorStation(RegionVoxels w, Random random, int x, int y, int z) {
		w.set(x, y + 4, z, Crystal.KYANITE);
		w.spawner(x, y + 5, z, "rotator");
		w.spawner(x, y + 6, z, "rotator");
		w.set(x, y + 7, z, Crystal.KYANITE);
		Container chest = w.chest(x, y + 8, z, 2);
		put(chest, 1, "rotator_spawn_egg", 1 + random.nextInt(5));
		put(chest, 2, "crystal_energy", 4 + random.nextInt(16));
		put(chest, 3, "crystal_energy", 4 + random.nextInt(16));
	}

	/** makeUrchinSpawner: rods of Kyanite, Pink Tourmaline and Tiger's Eye, three Crystal Urchin spawners, a chest below. */
	static void urchinSpawner(RegionVoxels w, Random random, int x, int y, int z) {
		for (int i = 0; i < 3; i++) {
			Block block = i == 1 ? Crystal.PINK_TOURMALINE : i == 2 ? Crystal.TIGERS_EYE : Crystal.KYANITE;
			float dx = random.nextFloat() - random.nextFloat();
			float dz = random.nextFloat() - random.nextFloat();
			float dy = 0.5F + random.nextFloat() / 2.0F;
			int width = random.nextInt(2);
			int length = 10 + width * 3 + random.nextInt(5);
			if (i != 0) length /= 2;
			float rx = x;
			float ry = y;
			float rz = z;
			for (int iy = 0; iy <= length; iy++) {
				for (int ix = 0; ix <= width; ix++) {
					for (int iz = 0; iz <= width; iz++) w.set((int) (rx + ix), (int) ry, (int) (rz + iz), block);
				}
				ry += dy;
				rx += dx;
				rz += dz;
			}
		}
		w.spawner(x, y + 1, z, "crystal_urchin");
		w.spawner(x, y + 2, z, "crystal_urchin");
		w.spawner(x, y + 3, z, "crystal_urchin");
		w.set(x, y, z, Blocks.AIR);
		Container chest = w.chest(x, y - 1, z, 2);
		put(chest, 1, "crystal_urchin_spawn_egg", 1 + random.nextInt(5));
		put(chest, 2, "crystal_energy", 4 + random.nextInt(16));
		put(chest, 3, "crystal_energy", 4 + random.nextInt(16));
	}

	/** makeCrystalHauntedHouse: a 7 x 7 Crystal Planks hut with glass windows, a furnace, workbench and chest, and spawners. */
	static void crystalHauntedHouse(RegionVoxels w, Random random, int x, int y, int z) {
		int width = 3;
		int length = 3;
		int height = 3;
		for (int i = -width; i <= width; i++) {
			for (int j = -length; j <= length; j++) {
				for (int k = 0; k <= height + 1; k++) {
					Block block;
					if (k == height + 1) {
						block = Crystal.CRYSTAL_PLANKS;
					} else if (k == 0) {
						block = Crystal.KYANITE;
					} else if (i != width && j != length && i != -width && j != -length) {
						block = Blocks.AIR;
					} else if (k == height) {
						block = Blocks.GLASS;
					} else if ((k == 1 || k == 2) && i == width && j == 0) {
						block = Blocks.AIR;
					} else {
						block = Crystal.CRYSTAL_PLANKS;
					}
					w.set(x + i, y + k, z + j, block);
				}
			}
		}
		w.set(x + 2, y + 1, z + 2, Crystal.CRYSTAL_FURNACE.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
		w.set(x + 1, y + 1, z + 2, Crystal.CRYSTAL_WORKBENCH);
		Container chest = w.chest(x, y + 1, z + 2, 2);
		if (random.nextInt(2) == 0) put(chest, 0, "minecraft:compass", 1);
		if (random.nextInt(3) != 0) put(chest, 2, "cooked_peacock", 8);
		if (random.nextInt(3) != 0) put(chest, 3, "crystal_torch", 32);
		if (random.nextInt(2) == 0) put(chest, 4, "crystal_energy", 16);
		if (random.nextInt(2) == 0) put(chest, 5, "minecraft:red_bed", 1);
		if (random.nextInt(2) == 0) put(chest, 6, "minecraft:red_bed", 1);
		if (random.nextInt(2) == 0) put(chest, 7, "minecraft:oak_door", 1);
		if (random.nextInt(2) == 0) put(chest, 8, "pink_tourmaline_pickaxe", 1);
		if (random.nextInt(2) == 0) put(chest, 9, "pink_tourmaline_sword", 1);
		if (random.nextInt(2) == 0) put(chest, 10, "pink_tourmaline_axe", 1);
		put(chest, 11, "kraken_repellent", 1);
		if (random.nextInt(2) == 0) put(chest, 13, "minecraft:chest", 1);
		w.spawner(x, y + 1, z, "rat");
		w.spawner(x, y + 2, z, "ghost");
		w.spawner(x, y + 3, z, "ghost_pumpkin_skelly");
	}

	/** makeCrystalBattleTower: a 21-high round Kyanite tower of five floors, each with two spawners over a loot chest. */
	static void crystalBattleTower(RegionVoxels w, Random random, int x, int y, int z) {
		float radius = 10.0F;
		for (int j = 0; j <= 20; j++) {
			if (j % 5 == 0) {
				for (float currad = 0.0F; currad < radius; currad += 0.33F) {
					for (float curdeg = 0.0F; curdeg < 360.0F; curdeg += 5.0F) {
						float curx = (float) (currad * Math.cos(Math.toRadians(curdeg)));
						float curz = (float) (currad * Math.sin(Math.toRadians(curdeg)));
						w.set((int) (x + curx + 0.5F), y + j, (int) (z + curz + 0.5F), Crystal.KYANITE);
					}
				}
			} else {
				float currad = 10.0F;
				for (float curdeg = 0.0F; curdeg < 360.0F; curdeg += 5.0F) {
					float curx = (float) (currad * Math.cos(Math.toRadians(curdeg)));
					float curz = (float) (currad * Math.sin(Math.toRadians(curdeg)));
					Block block = j % 5 >= 1 && j % 5 <= 3 && (curdeg < 10.0F || curdeg > 350.0F) ? Blocks.AIR : Crystal.KYANITE;
					w.set((int) (x + curx + 0.5F), y + j, (int) (z + curz + 0.5F), block);
				}
			}
		}
		for (int j = 21; j <= 22; j++) {
			float currad = 10.0F;
			for (float curdeg = 0.0F; curdeg < 360.0F; curdeg += 5.0F) {
				float curx = (float) (currad * Math.cos(Math.toRadians(curdeg)));
				float curz = (float) (currad * Math.sin(Math.toRadians(curdeg)));
				w.set((int) (x + curx + 0.5F), y + j, (int) (z + curz + 0.5F), Crystal.PINK_TOURMALINE);
			}
		}
		floor(w, random, x, y + 1, z, "rat", CrystalLoot.TOWER_RAT, 5);
		floor(w, random, x, y + 6, z, "dungeon_beast", CrystalLoot.TOWER_DUNGEON_BEAST, 5);
		floor(w, random, x, y + 11, z, "crystal_urchin", CrystalLoot.TOWER_URCHIN, 5);
		floor(w, random, x, y + 16, z, "rotator", CrystalLoot.TOWER_ROTATOR, 5);
		floor(w, random, x, y + 21, z, "vortex", CrystalLoot.TOWER_VORTEX, 6);
	}

	private static void floor(RegionVoxels w, Random random, int x, int y, int z, String creature, CrystalLoot.Entry[] loot, int rolls) {
		w.spawner(x, y + 1, z, creature);
		w.spawner(x, y + 2, z, creature);
		Container chest = w.chest(x, y, z, 0);
		if (chest != null) CrystalLoot.fill(chest, random, loot, rolls + random.nextInt(rolls));
	}

	/** makeRoundRotator: an upright ring of bedrock round a Pink Tourmaline Block ring, with spawners and a chest inside. */
	static void roundRotator(RegionVoxels w, Random random, int x, int y, int z) {
		float radius = 6.0F;
		for (float curdeg = 0.0F; curdeg < 360.0F; curdeg += 5.0F) {
			float curx = (float) (radius * Math.cos(Math.toRadians(curdeg)));
			float cury = (float) (radius * Math.sin(Math.toRadians(curdeg)));
			w.set((int) (x + curx + 0.5F), (int) (y + 6 + cury + 0.5F), z, Blocks.BEDROCK);
		}
		radius = 2.0F;
		for (float curdeg = 0.0F; curdeg < 360.0F; curdeg += 5.0F) {
			float curx = (float) (radius * Math.cos(Math.toRadians(curdeg)));
			float cury = (float) (radius * Math.sin(Math.toRadians(curdeg)));
			w.set((int) (x + curx + 0.5F), (int) (y + 6 + cury + 0.5F), z, Crystal.PINK_TOURMALINE_BLOCK);
		}
		w.spawner(x + 1, y + 7, z, "rotator");
		w.spawner(x - 1, y + 5, z, "rotator");
		w.spawner(x + 1, y + 5, z, "rotator");
		w.spawner(x - 1, y + 7, z, "rotator");
		w.spawner(x + 5, y + 6, z, "dungeon_beast");
		w.spawner(x - 5, y + 6, z, "dungeon_beast");
		w.spawner(x, y + 1, z, "dungeon_beast");
		w.spawner(x, y + 11, z, "dungeon_beast");
		w.set(x + 1, y + 6, z, Crystal.CRYSTAL_ENERGY);
		w.set(x - 1, y + 6, z, Crystal.CRYSTAL_ENERGY);
		w.set(x, y + 7, z, Crystal.CRYSTAL_ENERGY);
		w.set(x, y + 5, z, Crystal.CRYSTAL_ENERGY);
		Container chest = w.chest(x, y + 6, z, 2);
		if (chest != null) CrystalLoot.fill(chest, random, CrystalLoot.TOWER_VORTEX, 6 + random.nextInt(6));
	}
}
