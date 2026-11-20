package orespawn.world.crystal;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import orespawn.OreSpawn;
import orespawn.content.Crystal;

import java.util.Random;

/**
 * The features ChunkProviderOreSpawn5 put into each new chunk of the Crystal dimension (generateCrystals,
 * addCrystalTrees, generateCrystalOres, addCrystalFlowers, addRice, addQuinoa), with OreSpawn's rates. Blocks other
 * groups own (egg ores, rice, quinoa) are looked up by id and skipped while they don't exist; the random is drawn the
 * same either way.
 */
final class CrystalFeatures {
	/** generateCrystalOres' egg ores, in OreSpawn's order (urchin, flounder, skate, rotator, peacock, fairy, ...). */
	private static final String[] EGG_ORES = {"crystal_urchin", "flounder", "skate", "rotator", "peacock", "fairy", "dungeon_beast", "vortex",
		"rat", "whale", "irukandji"};

	private CrystalFeatures() {
	}

	private static Block lazy(String id) {
		return BuiltInRegistries.BLOCK.getValue(OreSpawn.id(id));
	}

	/** Blocks other groups own, looked up once worldgen starts (registries are frozen by then). */
	private static final class Lazy {
		static final Block[] EGG_ORES = new Block[CrystalFeatures.EGG_ORES.length];
		static final Block RICE = lazy("rice_plant");
		static final Block QUINOA = lazy("quinoa_plant");

		static {
			for (int i = 0; i < EGG_ORES.length; i++) EGG_ORES[i] = lazy(CrystalFeatures.EGG_ORES[i] + "_egg_ore");
		}
	}

	static Block rice() {
		return Lazy.RICE;
	}

	static Block quinoa() {
		return Lazy.QUINOA;
	}

	// ---- MathHelper's table sine, as 1.7.10 computed it ----
	private static final float[] SIN_TABLE = new float[65536];

	static {
		for (int i = 0; i < 65536; i++) SIN_TABLE[i] = (float) Math.sin(i * Math.PI * 2.0 / 65536.0);
	}

	private static float sin(float f) {
		return SIN_TABLE[(int) (f * 10430.378F) & 65535];
	}

	private static float cos(float f) {
		return SIN_TABLE[(int) (f * 10430.378F + 16384.0F) & 65535];
	}

	private static int floor(double d) {
		int i = (int) d;
		return d < i ? i - 1 : i;
	}

	/** generateCrystals: a Pink Tourmaline cluster at y 30-34 and a Tiger's Eye one at y 5-9, each in 1 chunk in 30. */
	static void crystals(Voxels w, Random random, int chunkX, int chunkZ) {
		vein(w, random, chunkX, chunkZ, 30, 10, true, Crystal.PINK_TOURMALINE);
		vein(w, random, chunkX, chunkZ, 5, 5, false, Crystal.TIGERS_EYE);
	}

	/** addPinkTourmaline / addTigersEye: a few straight rods of crystal leaning out of one point. */
	private static void vein(Voxels w, Random random, int chunkX, int chunkZ, int baseY, int maxRods, boolean pink, Block block) {
		if (random.nextInt(30) != 1) return;
		int x0 = 3 + chunkX + random.nextInt(10);
		int y0 = baseY + random.nextInt(5);
		int z0 = 3 + chunkZ + random.nextInt(10);
		int rods = 1 + random.nextInt(maxRods);
		for (int i = 0; i < rods; i++) {
			float dx = random.nextFloat() - random.nextFloat();
			float dz = random.nextFloat() - random.nextFloat();
			float dy = 0.5F + random.nextFloat() / 2.0F;
			int width = pink ? random.nextInt(2) : 0;
			int length = pink ? 1 + width * 3 + random.nextInt(15) : width * 3 + random.nextInt(6);
			float rx = x0;
			float ry = y0;
			float rz = z0;
			for (int iy = 0; iy <= length; iy++) {
				for (int ix = 0; ix <= width; ix++) {
					for (int iz = 0; iz <= width; iz++) w.set((int) (rx + ix), (int) ry, (int) (rz + iz), block);
				}
				ry += dy;
				rx += dx;
				rz += dz;
			}
		}
	}

	/** Scans down from y 128 to 41 for air on Crystal Grass; returns the y of the air, or -1. */
	private static int grassTop(Voxels w, int x, int z) {
		for (int y = 128; y > 40; y--) {
			if (w.isAir(x, y, z) && w.get(x, y - 1, z) == Crystal.CRYSTAL_GRASS) return y;
		}
		return -1;
	}

	/** addCrystalTrees: in 1 chunk in 5, up to 7 tall trees (1 in 5) or up to 14 scraggly ones. */
	static void trees(Voxels w, Random random, Random shapes, int chunkX, int chunkZ) {
		if (random.nextInt(5) != 0) return;
		int what = random.nextInt(5);
		int howmany = random.nextInt(8);
		if (what != 0) howmany *= 2;
		for (int i = 0; i < howmany; i++) {
			int x = 4 + chunkX + random.nextInt(8);
			int z = 4 + chunkZ + random.nextInt(8);
			int y = grassTop(w, x, z);
			if (y < 0) continue;
			if (what == 0) {
				CrystalTrees.tall(w, shapes, x, y, z);
			} else {
				CrystalTrees.scraggly(w, shapes, x, y, z);
			}
		}
	}

	/** generateCrystalOres: egg ores above y 45, Crystal Energy anywhere, Crystalized Rats and Fairies below y 25. */
	static void ores(Voxels w, Random random, int chunkX, int chunkZ) {
		int patchy = 25 + random.nextInt(30);
		if (random.nextInt(20) == 0) patchy += 30;
		for (int i = 0; i < patchy; i++) {
			int x = 2 + chunkX + random.nextInt(12);
			int y = random.nextInt(128);
			int z = 2 + chunkZ + random.nextInt(12);
			if (y > 45) {
				Block egg = Lazy.EGG_ORES[random.nextInt(11)];
				ore(w, random, x, y, z, egg == Blocks.AIR ? null : egg, 4);
			}
		}
		patchy = 3 + random.nextInt(8);
		for (int i = 0; i < patchy; i++) {
			int x = 2 + chunkX + random.nextInt(12);
			int y = random.nextInt(128);
			int z = 2 + chunkZ + random.nextInt(12);
			ore(w, random, x, y, z, Crystal.CRYSTAL_ENERGY, 6);
		}
		patchy = 15 + random.nextInt(20);
		for (int i = 0; i < patchy; i++) {
			int x = 2 + chunkX + random.nextInt(12);
			int y = random.nextInt(128);
			int z = 2 + chunkZ + random.nextInt(12);
			if (y < 25) ore(w, random, x, y, z, Crystal.CRYSTALIZED_RATS, 6);
		}
		patchy = 12 + random.nextInt(20);
		for (int i = 0; i < patchy; i++) {
			int x = 2 + chunkX + random.nextInt(12);
			int y = random.nextInt(128);
			int z = 2 + chunkZ + random.nextInt(12);
			if (y < 25) ore(w, random, x, y, z, Crystal.CRYSTALIZED_FAIRIES, 6);
		}
	}

	/** generateOre: WorldGenMinable's ellipsoid string of blocks, replacing only Kyanite; null places nothing. */
	private static void ore(Voxels w, Random random, int x, int y, int z, Block block, int n) {
		float f = random.nextFloat() * (float) Math.PI;
		double d0 = x + 8 + sin(f) * n / 8.0F;
		double d1 = x + 8 - sin(f) * n / 8.0F;
		double d2 = z + 8 + cos(f) * n / 8.0F;
		double d3 = z + 8 - cos(f) * n / 8.0F;
		double d4 = y + random.nextInt(3) - 2;
		double d5 = y + random.nextInt(3) - 2;
		for (int l = 0; l <= n; l++) {
			double d6 = d0 + (d1 - d0) * l / n;
			double d7 = d4 + (d5 - d4) * l / n;
			double d8 = d2 + (d3 - d2) * l / n;
			double d9 = random.nextDouble() * n / 16.0;
			double d10 = (sin(l * (float) Math.PI / n) + 1.0F) * d9 + 1.0;
			double d11 = (sin(l * (float) Math.PI / n) + 1.0F) * d9 + 1.0;
			int i1 = floor(d6 - d10 / 2.0);
			int j1 = floor(d7 - d11 / 2.0);
			int k1 = floor(d8 - d10 / 2.0);
			int l1 = floor(d6 + d10 / 2.0);
			int i2 = floor(d7 + d11 / 2.0);
			int j2 = floor(d8 + d10 / 2.0);
			for (int k2 = i1; k2 <= l1; k2++) {
				double d12 = (k2 + 0.5 - d6) / (d10 / 2.0);
				if (d12 * d12 >= 1.0) continue;
				for (int l2 = j1; l2 <= i2; l2++) {
					double d13 = (l2 + 0.5 - d7) / (d11 / 2.0);
					if (d12 * d12 + d13 * d13 >= 1.0) continue;
					for (int i3 = k1; i3 <= j2; i3++) {
						double d14 = (i3 + 0.5 - d8) / (d10 / 2.0);
						if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0 && block != null && w.get(k2, l2, i3) == Crystal.KYANITE) {
							w.set(k2, l2, i3, block);
						}
					}
				}
			}
		}
	}

	/** addCrystalFlowers: in 1 chunk in 3, 1-13 flowers of one colour on Crystal Grass. */
	static void flowers(Voxels w, Random random, int chunkX, int chunkZ) {
		if (random.nextInt(3) != 0) return;
		int howmany = 1 + random.nextInt(13);
		int what = random.nextInt(4);
		Block flower = switch (what) {
			case 0 -> Crystal.RED_CRYSTAL_FLOWER;
			case 1 -> Crystal.GREEN_CRYSTAL_FLOWER;
			case 2 -> Crystal.BLUE_CRYSTAL_FLOWER;
			default -> Crystal.YELLOW_CRYSTAL_FLOWER;
		};
		for (int i = 0; i < howmany; i++) {
			int x = chunkX + random.nextInt(16);
			int z = chunkZ + random.nextInt(16);
			int y = grassTop(w, x, z);
			if (y >= 0) w.set(x, y, z, flower);
		}
	}

	/** addRice (1 chunk in 10) and addQuinoa (1 in 20): five tries each on Crystal Grass. */
	static void crop(Voxels w, Random random, int chunkX, int chunkZ, int rarity, Block crop) {
		if (random.nextInt(rarity) != 0) return;
		for (int i = 0; i < 5; i++) {
			int x = chunkX + random.nextInt(16);
			int z = chunkZ + random.nextInt(16);
			int y = grassTop(w, x, z);
			if (y >= 0 && crop != Blocks.AIR) w.set(x, y, z, crop);
		}
	}
}
