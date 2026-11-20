package orespawn.world.utopia;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Which trees a Utopia chunk gets, as OreSpawnWorld's DimensionID branch decided it: a huge tree (1 in 50); if not, a
 * grove of apple, cherry or peach trees (1 in 15 + its distance-based freq); if neither, a few wind trees or sky trees
 * (1 in 30); and only if none of those, The King's or Queen's altar (the feature tries that).
 * <p>
 * The huge, wind and sky trees reach far past one chunk, so they are a structure ({@link UtopiaStructure}), while the
 * fruit trees are part of the chunk's feature ({@link UtopiaFeature}). Both work the plan out the same way: from the
 * world seed and the chunk only, looking for grass on the plain terrain (OreSpawn looked in the half-decorated world,
 * so a tall grass tuft could block a spot; here the fruit-tree feature still rescans the real world before planting).
 */
public record UtopiaPlan(@Nullable Build huge, List<int[]> fruit, int fruitKind, List<Build> others) {
	/** What a build is: one of ItemMagicApple's huge trees, or Trees.WindTree / SkyTree. */
	public enum Kind { SQUARE, EMERALD, RUBY, CIRCULAR, ROUND, WIND, SKY }

	/** A big build at (x, y, z) (the grass block it stands on), with the random seed its blocks come from. */
	public record Build(Kind kind, int x, int y, int z, int treeType, int radius, boolean noCritters, boolean appleLeaves, long seed) {}

	public List<Build> builds() {
		List<Build> list = new ArrayList<>();
		if (this.huge != null) {
			list.add(this.huge);
		}
		list.addAll(this.others);
		return list;
	}

	private static boolean grassUnderAir(UtopiaTerrain t, int x, int y, int z) {
		return t.typeAt(x, y, z) == UtopiaTerrain.AIR && t.typeAt(x, y - 1, z) == UtopiaTerrain.GRASS;
	}

	/** The first y (scanning down from {@code top} through air, as OreSpawn did) with grass below, or -1. */
	private static int scanDown(UtopiaTerrain t, int x, int top, int bottom, int z) {
		for (int y = top; y > bottom && t.typeAt(x, y, z) == UtopiaTerrain.AIR; y--) {
			if (t.typeAt(x, y - 1, z) == UtopiaTerrain.GRASS) {
				return y;
			}
		}
		return -1;
	}

	public static UtopiaPlan of(UtopiaTerrain t, long seed, int cx, int cz) {
		Random r = new Random(seed);
		long a = r.nextLong() | 1L;
		long b = r.nextLong() | 1L;
		r.setSeed(cx * a + cz * b ^ seed ^ 0x55746F706961L);
		int chunkX = cx * 16;
		int chunkZ = cz * 16;
		// addHugeTree
		Build huge = null;
		if (r.nextInt(50) == 0) {
			for (int i = 0; i < 3 && huge == null; i++) {
				int posX = 4 + chunkX + r.nextInt(8);
				int posZ = 4 + chunkZ + r.nextInt(8);
				for (int posY = 127; posY > 50; posY--) {
					if (!grassUnderAir(t, posX, posY, posZ)) {
						continue;
					}
					int treeType = r.nextInt(4);
					int radius = 6 - r.nextInt(2);
					boolean noCritters = r.nextInt(100) > 25;
					int randType = r.nextInt(100);
					Kind kind;
					boolean apple = false;
					if (randType > 75) {
						apple = treeType != 3 && r.nextInt(20) == 0;
						kind = Kind.SQUARE;
					} else if (randType == 0) {
						radius = 6;
						noCritters = true;
						kind = r.nextInt(2) == 0 ? Kind.EMERALD : Kind.RUBY;
					} else if (randType > 15) {
						radius = 6 - r.nextInt(3);
						kind = Kind.CIRCULAR;
					} else {
						radius = 6 - r.nextInt(3);
						kind = Kind.ROUND;
					}
					huge = new Build(kind, posX, posY - 1, posZ, treeType, radius, noCritters, apple, r.nextLong());
					break;
				}
			}
		}
		List<int[]> fruit = new ArrayList<>();
		int which = 0;
		List<Build> others = new ArrayList<>();
		if (huge == null) {
			// addAppleTrees
			int freq = (Math.abs(cx) + Math.abs(cz)) % 15;
			int howmany = 2 + r.nextInt(2 + (15 - freq) / 2);
			which = r.nextInt(10);
			if (r.nextInt(15 + freq) == 0) {
				for (int i = 0; i < howmany; i++) {
					int posX = 2 + chunkX + r.nextInt(12);
					int posZ = 2 + chunkZ + r.nextInt(12);
					if (scanDown(t, posX, 100, 50, posZ) >= 0) {
						fruit.add(new int[] {posX, posZ});
					}
				}
			}
			// addOtherTrees (only in Utopia), if no fruit tree went in
			if (fruit.isEmpty() && r.nextInt(30) == 0) {
				boolean wind = r.nextInt(2) == 0;
				int count = 0;
				for (int i = 0; i < 5; i++) {
					int posX = 3 + chunkX + r.nextInt(10);
					int posZ = 3 + chunkZ + r.nextInt(10);
					int posY = scanDown(t, posX, 100, 50, posZ);
					if (posY < 0) {
						continue;
					}
					count++;
					others.add(new Build(wind ? Kind.WIND : Kind.SKY, posX, posY - 1, posZ, 0, 0, false, false, r.nextLong()));
					if (count >= (wind ? 4 : 3)) {
						break;
					}
				}
			}
		}
		return new UtopiaPlan(huge, fruit, which, others);
	}
}
