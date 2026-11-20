package orespawn.world.utopia;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The trees from OreSpawn's {@code Trees} that Utopia grows (OreSpawnWorld.addOtherTrees): WindTree and SkyTree, on a
 * {@link GenWorld}, ported line for line. The class's other trees belong to their own groups, which ported them: the
 * experience tree (Farming, ExperienceTree), the Islands' small tree (IslandToo) and the Crystal dimension's fairy and
 * fairy-castle trees (CrystalTrees); DuplicatorTree is the growth of Farming's duplicator_tree_wood log, and
 * ScragglyTreeWithBranches was never called by OreSpawn.
 */
public final class Trees {
	private Trees() {
	}

	private static BlockState log() {
		return Blocks.OAK_LOG.defaultBlockState();
	}

	private static BlockState leaves() {
		return Blocks.OAK_LEAVES.defaultBlockState();
	}

	private static boolean isGrassOrDirt(Block b) {
		return b == Blocks.GRASS_BLOCK || b == Blocks.DIRT;
	}

	private static void windTreeBranch(GenWorld w, int x, int y, int z, int length, int dirx, int dirz) {
		for (int i = 1; i <= length; i++) {
			w.set(x + i * dirx, y, z + i * dirz, log());
			if (w.isAir(x + i * dirx, y + 1, z + i * dirz)) {
				w.set(x + i * dirx, y + 1, z + i * dirz, leaves());
			}
			if (i < length / 3 && w.isAir(x + i * dirx, y + 2, z + i * dirz)) {
				w.set(x + i * dirx, y + 2, z + i * dirz, leaves());
			}
			if (i > length / 3) {
				if (w.isAir(x + i * dirx + dirz, y, z + i * dirz + dirx)) {
					w.set(x + i * dirx + dirz, y, z + i * dirz + dirx, leaves());
				}
				if (w.isAir(x + i * dirx - dirz, y, z + i * dirz - dirx)) {
					w.set(x + i * dirx - dirz, y, z + i * dirz - dirx, leaves());
				}
			}
		}
		if (w.isAir(x + (length + 1) * dirx, y, z + (length + 1) * dirz)) {
			w.set(x + (length + 1) * dirx, y, z + (length + 1) * dirz, leaves());
		}
		if (w.isAir(x + (length + 2) * dirx, y, z + (length + 2) * dirz)) {
			w.set(x + (length + 2) * dirx, y, z + (length + 2) * dirz, leaves());
		}
	}

	/** WindTree: a 40-47 tall trunk with every branch blown one way (dir 0-3: +x, -x, +z, -z). */
	public static void windTree(GenWorld w, int x, int y, int z, int dir) {
		if (dir < 0 || dir > 3) {
			return;
		}
		int dirx = 1;
		int dirz = 0;
		if (dir == 1) {
			dirx = -1;
		}
		if (dir == 2) {
			dirx = 0;
			dirz = 1;
		}
		if (dir == 3) {
			dirx = 0;
			dirz = -1;
		}
		if (!isGrassOrDirt(w.block(x, y, z))) {
			return;
		}
		int height = w.random().nextInt(8) + 40;
		w.random().nextInt(4); // width, unused
		for (int j = 0; j < height; j++) {
			w.set(x, j + y, z, log());
			if (j > height / 5) {
				w.set(x + dirx, j + y, z + dirz, leaves());
				if (j > height / 4 && j % 4 == 0) {
					windTreeBranch(w, x, j + y, z, height - j, dirx, dirz);
				}
			}
		}
		w.set(x, y + height, z, leaves());
	}

	private static void skyTreeBranch(GenWorld w, int x, int y, int z, int length, int dirx, int dirz) {
		BlockState wood = UtopiaBlocks.skyTreeLog();
		for (int i = 1; i < length; i++) {
			w.set(x + i * dirx, y, z + i * dirz, wood);
			if (w.isAir(x + i * dirx, y + 1, z + i * dirz)) {
				w.set(x + i * dirx, y + 1, z + i * dirz, leaves());
			}
			if (w.isAir(x + i * dirx + dirz, y, z + i * dirz + dirx)) {
				w.set(x + i * dirx + dirz, y, z + i * dirz + dirx, leaves());
			}
			if (w.isAir(x + i * dirx - dirz, y, z + i * dirz - dirx)) {
				w.set(x + i * dirx - dirz, y, z + i * dirz - dirx, leaves());
			}
		}
		if (w.isAir(x + length * dirx, y, z + length * dirz)) {
			w.set(x + length * dirx, y, z + length * dirz, leaves());
		}
	}

	/** SkyTree: a sky tree wood trunk to y 190-204 with two crosses of branches at the top. */
	public static void skyTree(GenWorld w, int x, int y, int z) {
		if (!isGrassOrDirt(w.block(x, y, z))) {
			return;
		}
		int height = w.random().nextInt(15) + 190;
		if (height - y < 20) {
			return;
		}
		int width = w.random().nextInt(10) + 25;
		BlockState wood = UtopiaBlocks.skyTreeLog();
		for (int j = y; j <= height; j++) {
			w.set(x, j, z, wood);
		}
		w.set(x, height + 1, z, leaves());
		skyTreeBranch(w, x, height, z, width, 1, 0);
		skyTreeBranch(w, x, height, z, width, -1, 0);
		skyTreeBranch(w, x, height, z, width, 0, 1);
		skyTreeBranch(w, x, height, z, width, 0, -1);
		height -= 5;
		height -= w.random().nextInt(4);
		width /= 3;
		skyTreeBranch(w, x, height, z, width, 1, 0);
		skyTreeBranch(w, x, height, z, width, -1, 0);
		skyTreeBranch(w, x, height, z, width, 0, 1);
		skyTreeBranch(w, x, height, z, width, 0, -1);
	}
}
