package orespawn.world.utopia;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Utopia's terrain, as OreSpawn's {@code ChunkProviderOreSpawn} made it: 1.7.10's overworld noise with one biome
 * everywhere (Utopian Plains: root height 0.1, variation 0.2), stone below the noise surface, water up to y 62, then
 * {@code BiomeGenBase.genBiomeTerrain}'s grass, 3-ish dirt, gravel under water and a bedrock floor. Caves, ravines and
 * everything else come later. A chunk is a {@code byte[65536]} indexed {@code x << 12 | z << 8 | y} (y 0-255).
 * <p>
 * Chunks are cached, so the chunk generator, the structure planner and big builds (which read terrain outside the
 * chunk they are placed in) all see the same blocks.
 */
public final class UtopiaTerrain {
	public static final byte AIR = 0;
	public static final byte STONE = 1;
	public static final byte WATER = 2;
	public static final byte GRASS = 3;
	public static final byte DIRT = 4;
	public static final byte GRAVEL = 5;
	public static final byte BEDROCK = 6;
	public static final int HEIGHT = 256;
	public static final int SEA_LEVEL = 63;
	private static final BlockState[] STATES = {Blocks.AIR.defaultBlockState(), Blocks.STONE.defaultBlockState(),
		Blocks.WATER.defaultBlockState(), Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.DIRT.defaultBlockState(),
		Blocks.GRAVEL.defaultBlockState(), Blocks.BEDROCK.defaultBlockState()};
	private static final float[] PARABOLIC = new float[25];

	static {
		for (int j = -2; j <= 2; j++) {
			for (int k = -2; k <= 2; k++) {
				PARABOLIC[j + 2 + (k + 2) * 5] = 10.0F / (float) Math.sqrt(j * j + k * k + 0.2F);
			}
		}
	}

	private final long seed;
	private final LegacyNoise.Octaves minLimit;
	private final LegacyNoise.Octaves maxLimit;
	private final LegacyNoise.Octaves mainNoise;
	private final LegacyNoise.Perlin surfaceNoise;
	private final LegacyNoise.Octaves depthNoise;
	private final Map<Long, byte[]> cache = new LinkedHashMap<>(64, 0.75F, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Long, byte[]> eldest) {
			return this.size() > 512;
		}
	};

	public UtopiaTerrain(long seed) {
		this.seed = seed;
		Random random = new Random(seed);
		this.minLimit = new LegacyNoise.Octaves(random, 16);
		this.maxLimit = new LegacyNoise.Octaves(random, 16);
		this.mainNoise = new LegacyNoise.Octaves(random, 8);
		this.surfaceNoise = new LegacyNoise.Perlin(random, 4);
		new LegacyNoise.Octaves(random, 10); // noiseGen5: unused, but it draws from the same random
		this.depthNoise = new LegacyNoise.Octaves(random, 16);
	}

	public long seed() {
		return this.seed;
	}

	public static BlockState state(byte type) {
		return STATES[type];
	}

	public static int index(int x, int y, int z) {
		return (x & 15) << 12 | (z & 15) << 8 | y;
	}

	/** The terrain block at a world position (air above 255 and below 0). */
	public byte typeAt(int x, int y, int z) {
		if (y < 0 || y >= HEIGHT) {
			return AIR;
		}
		return this.chunk(x >> 4, z >> 4)[index(x, y, z)];
	}

	public BlockState stateAt(int x, int y, int z) {
		return STATES[this.typeAt(x, y, z)];
	}

	/** The y of the highest non-air terrain block in a column, or -1. */
	public int topSolid(int x, int z) {
		byte[] c = this.chunk(x >> 4, z >> 4);
		int base = (x & 15) << 12 | (z & 15) << 8;
		for (int y = HEIGHT - 1; y >= 0; y--) {
			if (c[base | y] != AIR) {
				return y;
			}
		}
		return -1;
	}

	public byte[] chunk(int cx, int cz) {
		long key = (long) cx << 32 | (cz & 0xFFFFFFFFL);
		synchronized (this.cache) {
			byte[] cached = this.cache.get(key);
			if (cached != null) {
				return cached;
			}
		}
		byte[] blocks = this.generate(cx, cz);
		synchronized (this.cache) {
			this.cache.put(key, blocks);
		}
		return blocks;
	}

	/** ChunkProviderOreSpawn.provideChunk without caves: func_147424_a then replaceBlocksForBiome. */
	private byte[] generate(int cx, int cz) {
		Random rand = new Random(cx * 341873128712L + cz * 132897987541L);
		byte[] blocks = new byte[65536];
		double[] density = this.density(cx * 4, cz * 4);
		for (int k = 0; k < 4; k++) {
			int l = k * 5;
			int i1 = (k + 1) * 5;
			for (int j1 = 0; j1 < 4; j1++) {
				int k1 = (l + j1) * 33;
				int l1 = (l + j1 + 1) * 33;
				int i2 = (i1 + j1) * 33;
				int j2 = (i1 + j1 + 1) * 33;
				for (int k2 = 0; k2 < 32; k2++) {
					double d1 = density[k1 + k2];
					double d2 = density[l1 + k2];
					double d3 = density[i2 + k2];
					double d4 = density[j2 + k2];
					double d5 = (density[k1 + k2 + 1] - d1) * 0.125;
					double d6 = (density[l1 + k2 + 1] - d2) * 0.125;
					double d7 = (density[i2 + k2 + 1] - d3) * 0.125;
					double d8 = (density[j2 + k2 + 1] - d4) * 0.125;
					for (int l2 = 0; l2 < 8; l2++) {
						double d10 = d1;
						double d11 = d2;
						double d12 = (d3 - d1) * 0.25;
						double d13 = (d4 - d2) * 0.25;
						int y = k2 * 8 + l2;
						for (int i3 = 0; i3 < 4; i3++) {
							int x = i3 + k * 4;
							double d16 = (d11 - d10) * 0.25;
							double d15 = d10 - d16;
							for (int k3 = 0; k3 < 4; k3++) {
								int z = j1 * 4 + k3;
								byte type;
								if ((d15 += d16) > 0.0) {
									type = STONE;
								} else if (y < SEA_LEVEL) {
									type = WATER;
								} else {
									type = AIR;
								}
								blocks[x << 12 | z << 8 | y] = type;
							}
							d10 += d12;
							d11 += d13;
						}
						d1 += d5;
						d2 += d6;
						d3 += d7;
						d4 += d8;
					}
				}
			}
		}
		// replaceBlocksForBiome: 1.7.10 walks k then l and builds column (x = l, z = k).
		double[] stoneNoise = this.surfaceNoise.generate(cx * 16, cz * 16, 16, 16, 0.0625, 0.0625, 1.0);
		for (int k = 0; k < 16; k++) {
			for (int l = 0; l < 16; l++) {
				this.genBiomeTerrain(blocks, rand, l, k, stoneNoise[l + k * 16]);
			}
		}
		return blocks;
	}

	/**
	 * The chunk's random as it was after replaceBlocksForBiome, which ChunkProviderOreSpawn went on to hand to
	 * ChunkOreGenerator. genBiomeTerrain always draws the same numbers per column (one double, then one int per y), so
	 * replaying them gives it back.
	 */
	public static Random oreRandom(int cx, int cz) {
		Random rand = new Random(cx * 341873128712L + cz * 132897987541L);
		for (int column = 0; column < 256; column++) {
			rand.nextDouble();
			for (int y = 0; y < HEIGHT; y++) {
				rand.nextInt(5);
			}
		}
		return rand;
	}

	/** BiomeGenBase.genBiomeTerrain for Utopia's biome (grass on dirt; it is never cold enough for ice). */
	private void genBiomeTerrain(byte[] blocks, Random rand, int x, int z, double noise) {
		byte top = GRASS;
		byte filler = DIRT;
		int k = -1;
		int depth = (int) (noise / 3.0 + 3.0 + rand.nextDouble() * 0.25);
		int base = x << 12 | z << 8;
		for (int y = 255; y >= 0; y--) {
			int i = base | y;
			if (y <= rand.nextInt(5)) {
				blocks[i] = BEDROCK;
				continue;
			}
			byte b = blocks[i];
			if (b == AIR) {
				k = -1;
			} else if (b == STONE) {
				if (k == -1) {
					if (depth <= 0) {
						top = AIR;
						filler = STONE;
					} else if (y >= 59 && y <= 64) {
						top = GRASS;
						filler = DIRT;
					}
					if (y < SEA_LEVEL && top == AIR) {
						top = WATER;
					}
					k = depth;
					if (y >= 62) {
						blocks[i] = top;
					} else if (y < 56 - depth) {
						top = AIR;
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

	/** ChunkProviderOreSpawn.func_147423_a: the 5x33x5 density grid. */
	private double[] density(int x, int z) {
		double[] depth = this.depthNoise.generate2D(x, z, 5, 5, 200.0, 200.0);
		double[] main = this.mainNoise.generate(x, 0, z, 5, 33, 5, 8.555150000000001, 4.277575000000001, 8.555150000000001);
		double[] min = this.minLimit.generate(x, 0, z, 5, 33, 5, 684.412, 684.412, 684.412);
		double[] max = this.maxLimit.generate(x, 0, z, 5, 33, 5, 684.412, 684.412, 684.412);
		double[] out = new double[825];
		// One biome everywhere: Utopian Plains keeps BiomeGenBase's default height (root 0.1, variation 0.2).
		float rootHeight = 0.1F;
		float variation = 0.2F;
		int l = 0;
		int i1 = 0;
		for (int j1 = 0; j1 < 5; j1++) {
			for (int k1 = 0; k1 < 5; k1++) {
				float f = 0.0F;
				float f1 = 0.0F;
				float f2 = 0.0F;
				for (int l1 = -2; l1 <= 2; l1++) {
					for (int i2 = -2; i2 <= 2; i2++) {
						float f5 = PARABOLIC[l1 + 2 + (i2 + 2) * 5] / (rootHeight + 2.0F);
						f += variation * f5;
						f1 += rootHeight * f5;
						f2 += f5;
					}
				}
				f /= f2;
				f1 /= f2;
				f = f * 0.9F + 0.1F;
				f1 = (f1 * 4.0F - 1.0F) / 8.0F;
				double d13 = depth[i1] / 8000.0;
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
				double d12 = f1;
				double d14 = f;
				d12 += d13 * 0.2;
				d12 = d12 * 8.5 / 8.0;
				double d5 = 8.5 + d12 * 4.0;
				for (int j2 = 0; j2 < 33; j2++) {
					double d6 = (j2 - d5) * 12.0 * 128.0 / 256.0 / d14;
					if (d6 < 0.0) {
						d6 *= 4.0;
					}
					double d7 = min[l] / 512.0;
					double d8 = max[l] / 512.0;
					double d9 = (main[l] / 10.0 + 1.0) / 2.0;
					double d10 = (d9 < 0.0 ? d7 : d9 > 1.0 ? d8 : d7 + (d8 - d7) * d9) - d6;
					if (j2 > 29) {
						double d11 = (j2 - 29) / 3.0F;
						d10 = d10 * (1.0 - d11) + -10.0 * d11;
					}
					out[l] = d10;
					l++;
				}
			}
		}
		return out;
	}
}
