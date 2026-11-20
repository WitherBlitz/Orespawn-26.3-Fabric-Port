package orespawn.world.crystal;

import net.minecraft.world.level.block.Block;
import orespawn.content.Crystal;

import java.util.Random;

/**
 * OreSpawn's crystal trees, as ChunkProviderOreSpawn5 grew them in new chunks and BlockCrystalPlant grew them from
 * saplings: the tall red-leaved tree, the scraggly yellow-leaved tree with branches, and (saplings only) the tall
 * blue-leaved tree. The shapes are OreSpawn's own; {@code random} stands for the world random OreSpawn used. Its
 * LessLag setting is taken at its default (0).
 */
public final class CrystalTrees {
	private CrystalTrees() {
	}

	private static boolean blocks(Voxels w, int x, int y, int z, Block leaves) {
		Block b = w.get(x, y, z);
		return !w.isAir(x, y, z) && b != Crystal.CRYSTAL_TREE_WOOD && b != leaves;
	}

	/** TallCrystalTree: a 10-21 block trunk with leaf tufts every 4 blocks and a flat crown (red leaves). */
	public static void tall(Voxels w, Random random, int x, int y, int z) {
		Block log = Crystal.CRYSTAL_TREE_WOOD;
		Block leaves = Crystal.RED_CRYSTAL_TREE_LEAVES;
		int i = 10 + random.nextInt(12);
		int j = i + random.nextInt(18);
		for (int k = 0; k < i; k++) {
			if (k >= 1 && blocks(w, x, y + k, z, leaves)) return;
			w.set(x, y + k, z, log);
		}
		y += i - 1;
		for (int k = i; k < j; k++) {
			y++;
			if (blocks(w, x, y, z, leaves)) break;
			w.set(x, y, z, log);
			if (k % 4 == 0) {
				for (int m = -1; m < 2; m++) {
					for (int n = -1; n < 2; n++) {
						if (random.nextInt(2) == 1 && w.isAir(x + m, y, z + n)) w.set(x + m, y, z + n, leaves);
					}
				}
			}
		}
		crown(w, random, x, y, z, log, leaves);
	}

	/** TallCrystalTreeBlue (BlockCrystalPlant only): a 5-10 block trunk, tufts every 3 blocks, blue leaves. */
	public static void tallBlue(Voxels w, Random random, int x, int y, int z) {
		Block log = Crystal.CRYSTAL_TREE_WOOD;
		Block leaves = Crystal.BLUE_CRYSTAL_TREE_LEAVES;
		int i = 5 + random.nextInt(6);
		int j = 2 + i + random.nextInt(12);
		for (int k = 0; k < i; k++) {
			if (k >= 1 && blocks(w, x, y + k, z, leaves)) return;
			w.set(x, y + k, z, log);
		}
		y += i - 1;
		for (int k = i; k < j; k++) {
			y++;
			if (blocks(w, x, y, z, leaves)) break;
			w.set(x, y, z, log);
			if (k % 3 == 0) {
				for (int m = -1; m < 2; m++) {
					for (int n = -1; n < 2; n++) {
						if (random.nextInt(2) == 1 && w.isAir(x + m, y, z + n)) w.set(x + m, y, z + n, leaves);
					}
				}
			}
		}
		crown(w, random, x, y, z, log, leaves);
	}

	/** The tall trees' top: a ragged ring of wood, a 7x7 layer of leaves, and a 3x3 layer above it. */
	private static void crown(Voxels w, Random random, int x, int y, int z, Block log, Block leaves) {
		y++;
		for (int m = -1; m < 2; m++) {
			for (int n = -1; n < 2; n++) {
				if (random.nextInt(2) == 1 && w.isAir(x + m, y, z + n)) w.set(x + m, y, z + n, log);
			}
		}
		for (int m = -3; m < 4; m++) {
			for (int n = -3; n < 4; n++) {
				if (w.isAir(x + m, y, z + n)) w.set(x + m, y, z + n, leaves);
			}
		}
		y++;
		for (int m = -1; m < 2; m++) {
			for (int n = -1; n < 2; n++) {
				if (w.isAir(x + m, y, z + n)) w.set(x + m, y, z + n, leaves);
			}
		}
	}

	/** ScragglyCrystalTreeWithBranches: a short trunk wandering upwards, with side branches (yellow leaves). */
	public static void scraggly(Voxels w, Random random, int x, int y, int z) {
		Block log = Crystal.CRYSTAL_TREE_WOOD;
		Block leaves = Crystal.YELLOW_CRYSTAL_TREE_LEAVES;
		int i = 1 + random.nextInt(2);
		int j = i + random.nextInt(8);
		for (int k = 0; k < i; k++) {
			if (k >= 1 && blocks(w, x, y + k, z, leaves)) return;
			w.set(x, y + k, z, log);
		}
		y += i - 1;
		for (int k = i; k < j; k++) {
			int ix = random.nextInt(2) - random.nextInt(2);
			int iz = random.nextInt(2) - random.nextInt(2);
			int iy = random.nextInt(4) > 0 ? 1 : 0;
			x += ix;
			z += iz;
			y += iy;
			if (blocks(w, x, y, z, leaves)) break;
			w.set(x, y, z, log);
			if (random.nextInt(4) == 1) {
				branch(w, random, x, y, z, random.nextInt(1 + j - k), random.nextInt(2) - random.nextInt(2),
					random.nextInt(2) - random.nextInt(2), log, leaves);
			}
			tuft(w, random, x, y, z, leaves);
		}
	}

	/** makeScragglyCrystalBranch. */
	private static void branch(Voxels w, Random random, int x, int y, int z, int len, int biasx, int biasz, Block log, Block leaves) {
		for (int k = 0; k < len; k++) {
			int ix = Math.max(-1, Math.min(1, random.nextInt(2) - random.nextInt(2) + biasx));
			int iz = Math.max(-1, Math.min(1, random.nextInt(2) - random.nextInt(2) + biasz));
			int iy = random.nextInt(3) > 0 ? 1 : 0;
			x += ix;
			z += iz;
			y += iy;
			if (blocks(w, x, y, z, leaves)) return;
			w.set(x, y, z, log);
			tuft(w, random, x, y, z, leaves);
		}
	}

	/** Leaves around (each 1 in 2) and maybe on top of a scraggly tree's wood. */
	private static void tuft(Voxels w, Random random, int x, int y, int z, Block leaves) {
		for (int m = -1; m < 2; m++) {
			for (int n = -1; n < 2; n++) {
				if (random.nextInt(2) == 1 && w.isAir(x + m, y, z + n)) w.set(x + m, y, z + n, leaves);
			}
		}
		if (random.nextInt(2) == 1 && w.isAir(x, y + 1, z)) w.set(x, y + 1, z, leaves);
	}
}
