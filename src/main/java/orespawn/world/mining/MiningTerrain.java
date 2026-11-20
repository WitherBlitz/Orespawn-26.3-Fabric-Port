package orespawn.world.mining;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

/**
 * OreSpawn's {@code ChunkProviderOreSpawn2.provideChunk} for one world seed, as 1.7.10 ran it: the noise terrain
 * ({@code func_147424_a} / {@code func_147423_a}) under one biome everywhere, Extreme Hills (OreSpawn's
 * {@code WorldChunkManagerHell}), its surface ({@code BiomeGenHills.genTerrainBlocks} and
 * {@code BiomeGenBase.genBiomeTerrain}, including 1.7.10's swapped x and z inside the chunk), {@code MapGenCaves},
 * {@code MapGenRavine}, then {@code ChunkOreGenerator.generateOresInChunk} three times ({@link MiningOres}). A chunk
 * comes out as 1.7.10's block array, indexed {@code x << 12 | z << 8 | y} for y 0-255, null for air.
 * <p>
 * Immutable once built, so worldgen threads share it.
 */
public final class MiningTerrain {
	public static final int HEIGHT = 256;
	public static final int SEA_LEVEL = 63;
	/** Extreme Hills' rootHeight and heightVariation (1.7.10's height_MidHills). */
	private static final float ROOT_HEIGHT = 1.0F;
	private static final float HEIGHT_VARIATION = 0.5F;

	static final BlockState STONE = Blocks.STONE.defaultBlockState();
	static final BlockState WATER = Blocks.WATER.defaultBlockState();
	static final BlockState LAVA = Blocks.LAVA.defaultBlockState();
	static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
	static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
	static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
	static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();

	private final long seed;
	private final Legacy1710.Octaves minLimit;
	private final Legacy1710.Octaves maxLimit;
	private final Legacy1710.Octaves mainNoise;
	private final Legacy1710.Perlin surfaceNoise;
	private final Legacy1710.Octaves depthNoise;
	private final float[] parabolicField = new float[25];
	/** The biome blend of func_147423_a, which is the same everywhere with one biome: {f, f1}. */
	private final float blendVariation;
	private final float blendRoot;

	public MiningTerrain(long seed) {
		this.seed = seed;
		// The constructor's order, so each generator gets the same numbers from the seed as in 1.7.10.
		Random rand = new Random(seed);
		this.minLimit = new Legacy1710.Octaves(rand, 16);
		this.maxLimit = new Legacy1710.Octaves(rand, 16);
		this.mainNoise = new Legacy1710.Octaves(rand, 8);
		this.surfaceNoise = new Legacy1710.Perlin(rand, 4);
		new Legacy1710.Octaves(rand, 10); // noiseGen5, unused
		this.depthNoise = new Legacy1710.Octaves(rand, 16);
		// mobSpawnerNoise (8 octaves) comes last and is unused.
		for (int j = -2; j <= 2; j++) {
			for (int k = -2; k <= 2; k++) {
				float f = 10.0F / (float) Math.sqrt(j * j + k * k + 0.2F);
				this.parabolicField[j + 2 + (k + 2) * 5] = f;
			}
		}
		float f = 0.0F;
		float f1 = 0.0F;
		float f2 = 0.0F;
		for (int l1 = -2; l1 <= 2; l1++) {
			for (int i2 = -2; i2 <= 2; i2++) {
				float f5 = this.parabolicField[l1 + 2 + (i2 + 2) * 5] / (ROOT_HEIGHT + 2.0F);
				f += HEIGHT_VARIATION * f5;
				f1 += ROOT_HEIGHT * f5;
				f2 += f5;
			}
		}
		f /= f2;
		f1 /= f2;
		this.blendVariation = f * 0.9F + 0.1F;
		this.blendRoot = (f1 * 4.0F - 1.0F) / 8.0F;
	}

	public long seed() {
		return this.seed;
	}

	/** provideChunk: the whole chunk, ores included. */
	public BlockState[] chunk(int chunkX, int chunkZ) {
		Random rand = new Random(chunkX * 341873128712L + chunkZ * 132897987541L);
		BlockState[] blocks = this.terrain(chunkX, chunkZ);
		BlockState[] topFiller = this.surface(chunkX, chunkZ, blocks, rand);
		Carvers.caves(this.seed, chunkX, chunkZ, blocks, topFiller[0], topFiller[1]);
		Carvers.ravines(this.seed, chunkX, chunkZ, blocks, topFiller[0], topFiller[1]);
		// LessOre is 0 by default: three passes.
		for (int i = 0; i < 3; i++) {
			MiningOres.generateOresInChunk(rand, chunkX, chunkZ, blocks);
		}
		return blocks;
	}

	/** func_147424_a: stone below the noise surface, still water up to y 62, air above. */
	BlockState[] terrain(int chunkX, int chunkZ) {
		BlockState[] blocks = new BlockState[65536];
		double[] field = this.noiseField(chunkX * 4, 0, chunkZ * 4);
		for (int k = 0; k < 4; k++) {
			int l = k * 5;
			int i1 = (k + 1) * 5;
			for (int j1 = 0; j1 < 4; j1++) {
				int k1 = (l + j1) * 33;
				int l1 = (l + j1 + 1) * 33;
				int i2 = (i1 + j1) * 33;
				int j2 = (i1 + j1 + 1) * 33;
				for (int k2 = 0; k2 < 32; k2++) {
					double d0 = 0.125;
					double d1 = field[k1 + k2];
					double d2 = field[l1 + k2];
					double d3 = field[i2 + k2];
					double d4 = field[j2 + k2];
					double d5 = (field[k1 + k2 + 1] - d1) * d0;
					double d6 = (field[l1 + k2 + 1] - d2) * d0;
					double d7 = (field[i2 + k2 + 1] - d3) * d0;
					double d8 = (field[j2 + k2 + 1] - d4) * d0;
					for (int l2 = 0; l2 < 8; l2++) {
						double d9 = 0.25;
						double d10 = d1;
						double d11 = d2;
						double d12 = (d3 - d1) * d9;
						double d13 = (d4 - d2) * d9;
						for (int i3 = 0; i3 < 4; i3++) {
							int j3 = i3 + k * 4 << 12 | 0 + j1 * 4 << 8 | k2 * 8 + l2;
							short short1 = 256;
							j3 -= short1;
							double d14 = 0.25;
							double d16 = (d11 - d10) * d14;
							double d15 = d10 - d16;
							for (int k3 = 0; k3 < 4; k3++) {
								if ((d15 += d16) > 0.0) {
									blocks[j3 += short1] = STONE;
								} else if (k2 * 8 + l2 < SEA_LEVEL) {
									blocks[j3 += short1] = WATER;
								} else {
									blocks[j3 += short1] = null;
								}
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
		return blocks;
	}

	/** func_147423_a: the 5 x 33 x 5 density field for a chunk at (x, y, z) in quarter-chunk units. */
	private double[] noiseField(int x, int y, int z) {
		double[] depth = this.depthNoise.generate2D(x, z, 5, 5, 200.0, 200.0);
		double[] main = this.mainNoise.generate(x, y, z, 5, 33, 5, 8.555150000000001, 4.277575000000001, 8.555150000000001);
		double[] min = this.minLimit.generate(x, y, z, 5, 33, 5, 684.412, 684.412, 684.412);
		double[] max = this.maxLimit.generate(x, y, z, 5, 33, 5, 684.412, 684.412, 684.412);
		double[] field = new double[825];
		int l = 0;
		int i1 = 0;
		for (int j1 = 0; j1 < 5; j1++) {
			for (int k1 = 0; k1 < 5; k1++) {
				float f = this.blendVariation;
				float f1 = this.blendRoot;
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
					double d10 = Legacy1710.denormalizeClamp(d7, d8, d9) - d6;
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

	/**
	 * replaceBlocksForBiome with Extreme Hills' genTerrainBlocks: grass over dirt, or bare stone where the surface noise
	 * is over 1. Returns the biome's top and filler blocks as the last column left them, which is what 1.7.10's caves
	 * and ravines then read from the (shared) biome.
	 */
	private BlockState[] surface(int chunkX, int chunkZ, BlockState[] blocks, Random rand) {
		double d0 = 0.03125;
		double[] stoneNoise = this.surfaceNoise.generate(chunkX * 16, chunkZ * 16, 16, 16, d0 * 2.0, d0 * 2.0, 1.0);
		BlockState top = GRASS;
		BlockState filler = DIRT;
		for (int k = 0; k < 16; k++) {
			for (int l = 0; l < 16; l++) {
				double noise = stoneNoise[l + k * 16];
				top = GRASS;
				filler = DIRT;
				// field_150638_aH is field_150635_aE for plain Extreme Hills: no gravel, stone where noise > 1.
				if (noise > 1.0) {
					top = STONE;
					filler = STONE;
				}
				genBiomeTerrain(blocks, rand, chunkX * 16 + k, chunkZ * 16 + l, noise, top, filler);
			}
		}
		return new BlockState[] {top, filler};
	}

	/**
	 * BiomeGenBase.genBiomeTerrain. Its index swaps x and z inside the chunk ({@code (z & 15) * 16 + (x & 15)} as the
	 * column), as 1.7.10's did. Below sea level an empty top becomes water: Extreme Hills here is 0.8 warm (OreSpawn set
	 * it so), never under 0.15, so never ice.
	 */
	private static void genBiomeTerrain(BlockState[] blocks, Random rand, int x, int z, double noise, BlockState topBlock, BlockState fillerBlock) {
		BlockState block = topBlock;
		BlockState block1 = fillerBlock;
		int k = -1;
		int l = (int) (noise / 3.0 + 3.0 + rand.nextDouble() * 0.25);
		int i1 = x & 15;
		int j1 = z & 15;
		int k1 = blocks.length / 256;
		for (int l1 = 255; l1 >= 0; l1--) {
			int i2 = (j1 * 16 + i1) * k1 + l1;
			if (l1 <= 0 + rand.nextInt(5)) {
				blocks[i2] = BEDROCK;
			} else {
				BlockState block2 = blocks[i2];
				if (block2 != null) {
					if (block2 == STONE) {
						if (k == -1) {
							if (l <= 0) {
								block = null;
								block1 = STONE;
							} else if (l1 >= 59 && l1 <= 64) {
								block = topBlock;
								block1 = fillerBlock;
							}
							if (l1 < 63 && block == null) {
								block = WATER;
							}
							k = l;
							if (l1 >= 62) {
								blocks[i2] = block;
							} else if (l1 < 56 - l) {
								block = null;
								block1 = STONE;
								blocks[i2] = GRAVEL;
							} else {
								blocks[i2] = block1;
							}
						} else if (k > 0) {
							k--;
							blocks[i2] = block1;
						}
					}
				} else {
					k = -1;
				}
			}
		}
	}

	/** The terrain (stone and water only, before the surface, caves and ores) at one column, for base heights. */
	BlockState[] column(int x, int z) {
		BlockState[] chunk = this.terrain(x >> 4, z >> 4);
		BlockState[] column = new BlockState[HEIGHT];
		int base = (x & 15) << 12 | (z & 15) << 8;
		System.arraycopy(chunk, base, column, 0, HEIGHT);
		return column;
	}
}
