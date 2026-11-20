package orespawn.world.villagemania;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import org.jspecify.annotations.Nullable;

import java.util.Random;

/**
 * ChunkProviderOreSpawn3's terrain for one world seed: 1.7.10's overworld noise (ChunkProviderGenerate) with every
 * column in OreSpawn's "Villages" biome (BiomeGenUtopianPlains: BiomeGenBase's default height 0.1 and variation 0.2,
 * grass over dirt), water up to y 62, the biome's surface pass (genBiomeTerrain: grass and three or so dirt, gravel deep
 * under water, bedrock at the bottom), MapGenCaves and MapGenRavine, and OreSpawn's chunk ores. y 0 to 255.
 * <p>
 * Blocks are held as 1.7.10's chunk array, {@code x << 12 | z << 8 | y}, with null for air. Where 26.3's village pieces
 * stand, their beardifier (terrain adaptation) is added to the density so the ground meets the houses, as 26.3's own
 * terrain does; elsewhere the terrain is exactly OreSpawn's. Not ported: the Amplified world type's taller terrain.
 */
final class VillageManiaTerrain {
	static final int HEIGHT = 256;
	static final int SEA_LEVEL = 63;
	static final BlockState STONE = Blocks.STONE.defaultBlockState();
	static final BlockState WATER = Blocks.WATER.defaultBlockState();
	static final BlockState LAVA = Blocks.LAVA.defaultBlockState();
	static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
	static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
	static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
	static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
	static final BlockState AIR = Blocks.AIR.defaultBlockState();
	/** BiomeGenBase's defaults, which BiomeGenUtopianPlains kept: rootHeight and heightVariation. */
	private static final float ROOT_HEIGHT = 0.1F;
	private static final float HEIGHT_VARIATION = 0.2F;
	/**
	 * 1.7.10's density rises about 2.7 a block above the ground; dividing by this and squeezing as 26.3 does puts it on
	 * 26.3's scale, where a structure's beardifier is weighed against it.
	 */
	private static final double BEARD_SCALE = 27.0;
	private static final int CACHE = 64;

	private final long seed;
	private final Legacy.Octaves minLimit;
	private final Legacy.Octaves maxLimit;
	private final Legacy.Octaves main;
	private final Legacy.Perlin stone;
	private final Legacy.Octaves depth;
	/** func_147423_a's biome-blended height and variation; one biome everywhere, so the same for every column. */
	private final double blendedHeight;
	private final double blendedVariation;
	private final ThreadLocal<Long2ObjectLinkedOpenHashMap<double[]>> fields = ThreadLocal.withInitial(Long2ObjectLinkedOpenHashMap::new);

	VillageManiaTerrain(long seed) {
		this.seed = seed;
		// The constructor's order: field_147431_j, field_147432_k, field_147429_l, field_147430_m, noiseGen5, noiseGen6
		// (then mobSpawnerNoise, never used here).
		Random rand = new Random(seed);
		this.minLimit = new Legacy.Octaves(rand, 16);
		this.maxLimit = new Legacy.Octaves(rand, 16);
		this.main = new Legacy.Octaves(rand, 8);
		this.stone = new Legacy.Perlin(rand, 4);
		new Legacy.Octaves(rand, 10); // noiseGen5: unused, but drawn so the depth noise after it matches
		this.depth = new Legacy.Octaves(rand, 16);
		float[] parabolic = new float[25];
		for (int j = -2; j <= 2; j++) {
			for (int k = -2; k <= 2; k++) {
				parabolic[j + 2 + (k + 2) * 5] = 10.0F / Legacy.sqrt(j * j + k * k + 0.2F);
			}
		}
		float f = 0.0F;
		float f1 = 0.0F;
		float f2 = 0.0F;
		for (int l1 = -2; l1 <= 2; l1++) {
			for (int i2 = -2; i2 <= 2; i2++) {
				float f5 = parabolic[l1 + 2 + (i2 + 2) * 5] / (ROOT_HEIGHT + 2.0F);
				f += HEIGHT_VARIATION * f5;
				f1 += ROOT_HEIGHT * f5;
				f2 += f5;
			}
		}
		f /= f2;
		f1 /= f2;
		f = f * 0.9F + 0.1F;
		f1 = (f1 * 4.0F - 1.0F) / 8.0F;
		this.blendedVariation = f;
		this.blendedHeight = f1;
	}

	long seed() {
		return this.seed;
	}

	/** func_147423_a: chunk (cx, cz)'s 5 x 33 x 5 density lattice (x outermost, then z, then y), cached per thread. */
	private double[] field(int cx, int cz) {
		Long2ObjectLinkedOpenHashMap<double[]> cache = this.fields.get();
		long key = ChunkPos.pack(cx, cz);
		double[] field = cache.getAndMoveToFirst(key);
		if (field == null) {
			field = this.computeField(cx, cz);
			cache.putAndMoveToFirst(key, field);
			if (cache.size() > CACHE) {
				cache.removeLast();
			}
		}
		return field;
	}

	private double[] computeField(int cx, int cz) {
		int x = cx * 4;
		int z = cz * 4;
		double[] depthNoise = this.depth.generate2D(x, z, 5, 5, 200.0, 200.0);
		double[] mainNoise = this.main.generate(x, 0, z, 5, 33, 5, 8.555150000000001, 4.277575000000001, 8.555150000000001);
		double[] minNoise = this.minLimit.generate(x, 0, z, 5, 33, 5, 684.412, 684.412, 684.412);
		double[] maxNoise = this.maxLimit.generate(x, 0, z, 5, 33, 5, 684.412, 684.412, 684.412);
		double[] field = new double[825];
		int l = 0;
		int i1 = 0;
		for (int j1 = 0; j1 < 5; j1++) {
			for (int k1 = 0; k1 < 5; k1++) {
				double d13 = depthNoise[i1] / 8000.0;
				if (d13 < 0.0) {
					d13 = -d13 * 0.3;
				}
				d13 = d13 * 3.0 - 2.0;
				if (d13 < 0.0) {
					d13 /= 2.0;
					if (d13 < -1.0) {
						d13 = -1.0;
					}
					d13 /= 1.4;
					d13 /= 2.0;
				} else {
					if (d13 > 1.0) {
						d13 = 1.0;
					}
					d13 /= 8.0;
				}
				i1++;
				double d12 = this.blendedHeight;
				double d14 = this.blendedVariation;
				d12 += d13 * 0.2;
				d12 = d12 * 8.5 / 8.0;
				double d5 = 8.5 + d12 * 4.0;
				for (int j2 = 0; j2 < 33; j2++) {
					double d6 = (j2 - d5) * 12.0 * 128.0 / 256.0 / d14;
					if (d6 < 0.0) {
						d6 *= 4.0;
					}
					double d7 = minNoise[l] / 512.0;
					double d8 = maxNoise[l] / 512.0;
					double d9 = (mainNoise[l] / 10.0 + 1.0) / 2.0;
					double d10 = Legacy.denormalizeClamp(d7, d8, d9) - d6;
					if (j2 > 29) {
						double d11 = (j2 - 29) / 3.0F;
						d10 = d10 * (1.0 - d11) + -10.0 * d11;
					}
					field[l] = d10;
					l++;
				}
			}
		}
		return field;
	}

	/** func_147424_a's trilinear interpolation for one column (local lx, lz), in its own order of operations. */
	private static void density(double[] q, int lx, int lz, double[] out) {
		int k = lx >> 2;
		int i3 = lx & 3;
		int j1 = lz >> 2;
		int k3 = lz & 3;
		int k1 = (k * 5 + j1) * 33;
		int l1 = (k * 5 + j1 + 1) * 33;
		int i2 = ((k + 1) * 5 + j1) * 33;
		int j2 = ((k + 1) * 5 + j1 + 1) * 33;
		for (int k2 = 0; k2 < 32; k2++) {
			double d0 = 0.125;
			double d1 = q[k1 + k2];
			double d2 = q[l1 + k2];
			double d3 = q[i2 + k2];
			double d4 = q[j2 + k2];
			double d5 = (q[k1 + k2 + 1] - d1) * d0;
			double d6 = (q[l1 + k2 + 1] - d2) * d0;
			double d7 = (q[i2 + k2 + 1] - d3) * d0;
			double d8 = (q[j2 + k2 + 1] - d4) * d0;
			for (int l2 = 0; l2 < 8; l2++) {
				double d9 = 0.25;
				double d10 = d1;
				double d11 = d2;
				double d12 = (d3 - d1) * d9;
				double d13 = (d4 - d2) * d9;
				for (int a = 0; a < i3; a++) {
					d10 += d12;
					d11 += d13;
				}
				double d16 = (d11 - d10) * 0.25;
				double d15 = d10 - d16;
				for (int b = 0; b <= k3; b++) {
					d15 += d16;
				}
				out[k2 * 8 + l2] = d15;
				d1 += d5;
				d2 += d6;
				d3 += d7;
				d4 += d8;
			}
		}
	}

	/** The block func_147424_a puts at a density: stone, water up to y 62, else air (null). */
	private static @Nullable BlockState fill(double density, int y) {
		return density > 0.0 ? STONE : y < SEA_LEVEL ? WATER : null;
	}

	/** 26.3's squeeze of 1.7.10's density, plus a structure's beardifier. */
	private static double bearded(double density, float beard) {
		double x = Math.clamp(density / BEARD_SCALE, -1.0, 1.0);
		return x / 2.0 - x * x * x / 24.0 + beard;
	}

	/** The column at world (x, z) before the surface pass, caves and ores: stone, water or air, y 0 to 255. */
	BlockState[] baseColumn(int x, int z) {
		double[] density = new double[HEIGHT];
		density(this.field(x >> 4, z >> 4), x & 15, z & 15, density);
		BlockState[] column = new BlockState[HEIGHT];
		for (int y = 0; y < HEIGHT; y++) {
			BlockState state = fill(density[y], y);
			column[y] = state == null ? AIR : state;
		}
		return column;
	}

	/** ChunkProviderOreSpawn3.provideChunk: the whole chunk, as 1.7.10's block array. */
	BlockState[] chunk(int cx, int cz, Beardifier beardifier) {
		BlockState[] blocks = new BlockState[16 * 16 * HEIGHT];
		double[] q = this.field(cx, cz);
		double[] density = new double[HEIGHT];
		boolean beard = beardifier != Beardifier.EMPTY;
		for (int lx = 0; lx < 16; lx++) {
			for (int lz = 0; lz < 16; lz++) {
				density(q, lx, lz, density);
				int base = lx << 12 | lz << 8;
				for (int y = 0; y < HEIGHT; y++) {
					double d = density[y];
					if (beard) {
						d = bearded(d, beardifier.sampleValue(SamplerContext.EMPTY_UNCACHED, cx * 16 + lx, y, cz * 16 + lz));
					}
					blocks[base | y] = fill(d, y);
				}
			}
		}
		Random rand = new Random(cx * 341873128712L + cz * 132897987541L);
		double[] stoneNoise = this.stone.generate(cx * 16, cz * 16, 16, 16, 0.0625, 0.0625, 1.0);
		// replaceBlocksForBiome: k (outer) is z, l (inner) is x once 1.7.10's two swaps cancel out.
		for (int k = 0; k < 16; k++) {
			for (int l = 0; l < 16; l++) {
				surface(blocks, rand, l << 12 | k << 8, stoneNoise[l + k * 16]);
			}
		}
		LegacyCarvers.caves(this.seed, cx, cz, blocks);
		LegacyCarvers.ravines(this.seed, cx, cz, blocks);
		ChunkOres.generate(blocks, rand, cx, cz);
		return blocks;
	}

	/** BiomeGenBase.genBiomeTerrain for one column (grass over dirt; never cold enough for ice). */
	private static void surface(BlockState[] blocks, Random rand, int base, double noise) {
		BlockState top = GRASS;
		BlockState filler = DIRT;
		int k = -1;
		int depth = (int) (noise / 3.0 + 3.0 + rand.nextDouble() * 0.25);
		for (int y = 255; y >= 0; y--) {
			int i = base | y;
			if (y <= rand.nextInt(5)) {
				blocks[i] = BEDROCK;
				continue;
			}
			BlockState block = blocks[i];
			if (block == null) {
				k = -1;
			} else if (block == STONE) {
				if (k == -1) {
					if (depth <= 0) {
						top = null;
						filler = STONE;
					} else if (y >= 59 && y <= 64) {
						top = GRASS;
						filler = DIRT;
					}
					if (y < SEA_LEVEL && top == null) {
						top = WATER;
					}
					k = depth;
					if (y >= 62) {
						blocks[i] = top;
					} else if (y < 56 - depth) {
						top = null;
						filler = STONE;
						blocks[i] = GRAVEL;
					} else {
						blocks[i] = filler;
					}
				} else if (k > 0) {
					k--;
					blocks[i] = filler;
				}
			}
		}
	}
}
