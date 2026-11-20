package orespawn.world.crystal;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.content.Crystal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * OreSpawn's CrystalMaze: under every chunk of the Crystal dimension, at y 25-27, a 4 x 4 maze of 4-block cells with
 * bedrock walls, a bedrock floor (y 24) and ceiling (y 28), open along the chunk's edges so the mazes of neighbouring
 * chunks join up. Four ceiling blocks and one floor block are Kyanite, a way in. OreSpawn picked the maze with
 * Math.random; here {@code random} is the chunk's own, so a chunk always gets the same maze.
 */
public final class CrystalMaze {
	private static final int WTOP = 1;
	private static final int WRGT = 2;
	private static final int WBOT = 4;
	private static final int WLFT = 8;

	private CrystalMaze() {
	}

	private record Point(int x, int y) {
	}

	/** buildCrystalMaze(world, chunkX * 16, 25, chunkZ * 16, chunk). */
	public static void build(Voxels w, Random random, int x, int y, int z) {
		BlockState air = Blocks.AIR.defaultBlockState();
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				for (int k = 0; k < 3; k++) w.set(x + j, y + k, z + i, air);
			}
		}
		makeMaze(w, random, x, y, z, 4, 4, 4);
		open(w, random, x, y, z, 4, 4);
	}

	/** openCrystalMaze. */
	private static void open(Voxels w, Random random, int xx, int yy, int zz, int zw, int csz) {
		BlockState air = Blocks.AIR.defaultBlockState();
		int size = zw * csz;
		for (int i = 0; i < size; i++) {
			for (int k = 0; k < 3; k++) {
				w.set(xx, yy + k, zz + i, air);
				w.set(xx + i, yy + k, zz, air);
				w.set(xx + size - 1, yy + k, zz + i, air);
				w.set(xx + i, yy + k, zz + size - 1, air);
			}
		}
		BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				w.set(xx + j, yy - 1, zz + i, bedrock);
				w.set(xx + j, yy + 3, zz + i, bedrock);
			}
		}
		for (int k = 0; k < 4; k++) {
			int i = random.nextInt(size);
			int j = random.nextInt(size);
			w.set(xx + j, yy + 3, zz + i, Crystal.KYANITE);
		}
		int i = random.nextInt(size);
		int j = random.nextInt(size);
		w.set(xx + j, yy - 1, zz + i, Crystal.KYANITE);
	}

	/** makeMaze: Prim's algorithm over the cell grid, then the remaining walls drawn in bedrock. */
	private static void makeMaze(Voxels w, Random random, int xx, int yy, int zz, int gridw, int gridh, int csz) {
		int cellsize = Math.max(3, csz);
		int[][] cells = new int[gridw][gridh];
		for (int x = 0; x < gridw; x++) {
			for (int y = 0; y < gridh; y++) cells[x][y] = 15;
		}
		for (int y = 0; y < gridh; y++) {
			cells[0][y] |= 128;
			cells[gridw - 1][y] |= 32;
		}
		for (int x = 0; x < gridw; x++) {
			cells[x][0] |= 16;
			cells[x][gridh - 1] |= 64;
		}
		List<Point> outlist = new ArrayList<>();
		List<Point> inlist = new ArrayList<>();
		List<Point> frontlist = new ArrayList<>();
		for (int x = 0; x < gridw; x++) {
			for (int y = 0; y < gridh; y++) outlist.add(new Point(x, y));
		}
		Point current = outlist.remove(random.nextInt(outlist.size()));
		inlist.add(current);
		moveNbrs(current, cells, outlist, frontlist);
		while (!frontlist.isEmpty()) {
			current = frontlist.remove(random.nextInt(frontlist.size()));
			inlist.add(current);
			moveNbrs(current, cells, outlist, frontlist);
			removeWall(current, findInNbr(random, current, cells, inlist), cells);
		}
		for (int x = 0; x < gridw; x++) {
			for (int y = 0; y < gridh; y++) {
				int val = cells[x][y];
				if ((val & WTOP) != 0) drawSide(w, x * cellsize, y * cellsize, (x + 1) * cellsize, y * cellsize, xx, yy, zz, cellsize, gridh, gridw);
				if ((val & WRGT) != 0) {
					drawSide(w, (x + 1) * cellsize - 1, y * cellsize, (x + 1) * cellsize - 1, (y + 1) * cellsize, xx, yy, zz, cellsize, gridh, gridw);
				}
				if ((val & WBOT) != 0) {
					drawSide(w, x * cellsize, (y + 1) * cellsize - 1, (x + 1) * cellsize, (y + 1) * cellsize - 1, xx, yy, zz, cellsize, gridh, gridw);
				}
				if ((val & WLFT) != 0) drawSide(w, x * cellsize, y * cellsize, x * cellsize, (y + 1) * cellsize, xx, yy, zz, cellsize, gridh, gridw);
			}
		}
	}

	private static void drawSide(Voxels w, int fromx, int fromz, int tox, int toz, int x, int y, int z, int cellsize, int gridh, int gridw) {
		BlockState wall = Blocks.BEDROCK.defaultBlockState();
		if (fromx > tox) {
			int t = fromx;
			fromx = tox;
			tox = t;
		}
		if (fromz > toz) {
			int t = fromz;
			fromz = toz;
			toz = t;
		}
		if (fromx == tox) {
			for (int j = fromz; j <= toz; j++) {
				if (j < cellsize * gridh) {
					for (int k = 0; k < 3; k++) w.set(fromx + x, y + k, j + z, wall);
				}
			}
		} else {
			for (int i = fromx; i <= tox; i++) {
				if (i < cellsize * gridw) {
					for (int k = 0; k < 3; k++) w.set(i + x, y + k, fromz + z, wall);
				}
			}
		}
	}

	private static int findInNbr(Random random, Point p, int[][] cells, List<Point> inlist) {
		int d = random.nextInt(4);
		for (int k = 0; k < 4; k++) {
			switch (d) {
				case 0 -> {
					if ((cells[p.x][p.y] & 16) == 0 && inlist.contains(new Point(p.x, p.y - 1))) return WTOP;
				}
				case 1 -> {
					if ((cells[p.x][p.y] & 32) == 0 && inlist.contains(new Point(p.x + 1, p.y))) return WRGT;
				}
				case 2 -> {
					if ((cells[p.x][p.y] & 64) == 0 && inlist.contains(new Point(p.x, p.y + 1))) return WBOT;
				}
				default -> {
					if ((cells[p.x][p.y] & 128) == 0 && inlist.contains(new Point(p.x - 1, p.y))) return WLFT;
				}
			}
			d = (d + 1) % 4;
		}
		return 0;
	}

	private static void moveNbrs(Point p, int[][] cells, List<Point> outlist, List<Point> frontlist) {
		if ((cells[p.x][p.y] & 16) == 0) movePoint(new Point(p.x, p.y - 1), outlist, frontlist);
		if ((cells[p.x][p.y] & 32) == 0) movePoint(new Point(p.x + 1, p.y), outlist, frontlist);
		if ((cells[p.x][p.y] & 64) == 0) movePoint(new Point(p.x, p.y + 1), outlist, frontlist);
		if ((cells[p.x][p.y] & 128) == 0) movePoint(new Point(p.x - 1, p.y), outlist, frontlist);
	}

	private static void movePoint(Point p, List<Point> from, List<Point> to) {
		int i = from.indexOf(p);
		if (i >= 0) {
			from.remove(i);
			to.add(p);
		}
	}

	private static void removeWall(Point p, int d, int[][] cells) {
		cells[p.x][p.y] ^= d;
		switch (d) {
			case WTOP -> cells[p.x][p.y - 1] ^= WBOT;
			case WRGT -> cells[p.x + 1][p.y] ^= WLFT;
			case WBOT -> cells[p.x][p.y + 1] ^= WTOP;
			case WLFT -> cells[p.x - 1][p.y] ^= WRGT;
			default -> {
			}
		}
	}
}
