package orespawn.world.chaos;

import java.util.Random;

/**
 * OreSpawn's ChunkProviderOreSpawn6 terrain: 1.7.10's Nether noise turned inside out. Where the Nether would be
 * netherrack this is air, and where it would be open cave this is stone, so the world is a thick, holed layer of
 * floating stone islands (roughly y 25 to 105) over the void, with nothing below and no bedrock. replaceBiomeBlocks then
 * covers every stone surface that has air above it with grass over a few layers of dirt (around y 60-65 the noise
 * leaves some of it bare stone), and clears the top and bottom few layers. There's no water or lava.
 * <p>
 * Blocks are codes in a {@code byte[16 * 16 * 128]} indexed {@code x << 11 | z << 7 | y}, as OreSpawn's own array was.
 * The noise generators are made from the world seed in OreSpawn's order and are shared; everything else is per call, so
 * chunks can be made on several threads at once.
 */
final class ChaosTerrain {
	static final byte AIR = 0;
	static final byte STONE = 1;
	static final byte GRASS = 2;
	static final byte DIRT = 3;
	/** OreSpawn's chunk array was 128 high (1.7.10 chunks were 256 high, the rest stayed air). */
	static final int HEIGHT = 128;

	private final long seed;
	private final ChaosNoise noise1;
	private final ChaosNoise noise2;
	private final ChaosNoise noise3;
	private final ChaosNoise slowsandGravelNoise;
	private final ChaosNoise exclusivityNoise;

	ChaosTerrain(long seed) {
		this.seed = seed;
		Random hellRNG = new Random(seed);
		this.noise1 = new ChaosNoise(hellRNG, 16);
		this.noise2 = new ChaosNoise(hellRNG, 16);
		this.noise3 = new ChaosNoise(hellRNG, 8);
		this.slowsandGravelNoise = new ChaosNoise(hellRNG, 4);
		this.exclusivityNoise = new ChaosNoise(hellRNG, 4);
		// OreSpawn also made netherNoiseGen6 (10 octaves) and netherNoiseGen7 (16) from hellRNG last; their output was
		// never used (as in vanilla's Nether), so they're left out. Nothing else draws from that random.
	}

	long seed() {
		return this.seed;
	}

	static int index(int x, int y, int z) {
		return x << 11 | z << 7 | y;
	}

	/** provideChunk's first two steps: func_147419_a (the shape) and replaceBiomeBlocks (grass and dirt). */
	byte[] chunk(int chunkX, int chunkZ) {
		byte[] blocks = new byte[16 * 16 * HEIGHT];
		this.shape(chunkX, chunkZ, blocks);
		Random hellRNG = new Random(chunkX * 341873128712L + chunkZ * 132897987541L);
		this.surface(chunkX, chunkZ, blocks, hellRNG);
		return blocks;
	}

	/** func_147419_a: a 5 x 17 x 5 density field, interpolated; stone where it's 0 or less, air above 0. */
	private void shape(int chunkX, int chunkZ, byte[] blocks) {
		final int cells = 4;
		final int xSize = cells + 1;
		final int ySize = 17;
		final int zSize = cells + 1;
		double[] field = this.densityField(chunkX * cells, 0, chunkZ * cells, xSize, ySize, zSize);
		for (int i1 = 0; i1 < cells; i1++) {
			for (int j1 = 0; j1 < cells; j1++) {
				for (int k1 = 0; k1 < 16; k1++) {
					double d0 = 0.125;
					double d1 = field[((i1 + 0) * zSize + j1 + 0) * ySize + k1 + 0];
					double d2 = field[((i1 + 0) * zSize + j1 + 1) * ySize + k1 + 0];
					double d3 = field[((i1 + 1) * zSize + j1 + 0) * ySize + k1 + 0];
					double d4 = field[((i1 + 1) * zSize + j1 + 1) * ySize + k1 + 0];
					double d5 = (field[((i1 + 0) * zSize + j1 + 0) * ySize + k1 + 1] - d1) * d0;
					double d6 = (field[((i1 + 0) * zSize + j1 + 1) * ySize + k1 + 1] - d2) * d0;
					double d7 = (field[((i1 + 1) * zSize + j1 + 0) * ySize + k1 + 1] - d3) * d0;
					double d8 = (field[((i1 + 1) * zSize + j1 + 1) * ySize + k1 + 1] - d4) * d0;
					for (int l1 = 0; l1 < 8; l1++) {
						double d9 = 0.25;
						double d10 = d1;
						double d11 = d2;
						double d12 = (d3 - d1) * d9;
						double d13 = (d4 - d2) * d9;
						for (int i2 = 0; i2 < 4; i2++) {
							int j2 = (i2 + i1 * 4) << 11 | (j1 * 4) << 7 | (k1 * 8 + l1);
							double d14 = 0.25;
							double d15 = d10;
							double d16 = (d11 - d10) * d14;
							for (int k2 = 0; k2 < 4; k2++) {
								blocks[j2] = d15 > 0.0 ? AIR : STONE;
								j2 += 128;
								d15 += d16;
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
	}

	/** initializeNoiseField: vanilla 1.7.10's Nether density (its unused noiseData4/5 left out). */
	private double[] densityField(int x, int y, int z, int xSize, int ySize, int zSize) {
		double[] field = new double[xSize * ySize * zSize];
		double[] shape = new double[ySize];
		double d0 = 684.412;
		double d1 = 2053.236;
		double[] noise1 = this.noise3.generate(null, x, y, z, xSize, ySize, zSize, d0 / 80.0, d1 / 60.0, d0 / 80.0);
		double[] noise2 = this.noise1.generate(null, x, y, z, xSize, ySize, zSize, d0, d1, d0);
		double[] noise3 = this.noise2.generate(null, x, y, z, xSize, ySize, zSize, d0, d1, d0);
		for (int i2 = 0; i2 < ySize; i2++) {
			shape[i2] = Math.cos(i2 * Math.PI * 6.0 / ySize) * 2.0;
			double d2 = i2;
			if (i2 > ySize / 2) {
				d2 = ySize - 1 - i2;
			}
			if (d2 < 4.0) {
				d2 = 4.0 - d2;
				shape[i2] -= d2 * d2 * d2 * 10.0;
			}
		}
		int k1 = 0;
		for (int ix = 0; ix < xSize; ix++) {
			for (int iz = 0; iz < zSize; iz++) {
				for (int j2 = 0; j2 < ySize; j2++) {
					double d7 = shape[j2];
					double d8 = noise2[k1] / 512.0;
					double d9 = noise3[k1] / 512.0;
					double d10 = (noise1[k1] / 10.0 + 1.0) / 2.0;
					double d6;
					if (d10 < 0.0) {
						d6 = d8;
					} else if (d10 > 1.0) {
						d6 = d9;
					} else {
						d6 = d8 + (d9 - d8) * d10;
					}
					d6 -= d7;
					if (j2 > ySize - 4) {
						double d11 = (j2 - (ySize - 4)) / 3.0F;
						d6 = d6 * (1.0 - d11) + -10.0 * d11;
					}
					// OreSpawn's bottom slide (j2 < d4 with d4 = 0) could never apply.
					field[k1] = d6;
					k1++;
				}
			}
		}
		return field;
	}

	/**
	 * replaceBiomeBlocks: for each column top down, the first stone under air becomes grass (dirt for i1 layers under
	 * it); in the y 60-65 band the two noises decide between grass and bare stone, and where the third noise is low the
	 * surface is stripped instead. The top 0-4 and bottom 0-4 layers are cleared. (OreSpawn's "water below y 64" rule
	 * never ran: it compared with Blocks.air, but the cleared surface was null.)
	 */
	private void surface(int chunkX, int chunkZ, byte[] blocks, Random hellRNG) {
		final int seaLevel = 64;
		double d0 = 0.03125;
		double[] slowsand = this.slowsandGravelNoise.generate(null, chunkX * 16, chunkZ * 16, 0, 16, 16, 1, d0, d0, 1.0);
		double[] gravel = this.slowsandGravelNoise.generate(null, chunkX * 16, 109, chunkZ * 16, 16, 1, 16, d0, 1.0, d0);
		double[] exclusivity = this.exclusivityNoise.generate(null, chunkX * 16, chunkZ * 16, 0, 16, 16, 1, d0 * 2.0, d0 * 2.0, d0 * 2.0);
		for (int k = 0; k < 16; k++) {
			for (int l = 0; l < 16; l++) {
				boolean flag = slowsand[k + l * 16] + hellRNG.nextDouble() * 0.2 > 0.0;
				boolean flag1 = gravel[k + l * 16] + hellRNG.nextDouble() * 0.2 > 0.0;
				int i1 = (int) (exclusivity[k + l * 16] / 3.0 + 3.0 + hellRNG.nextDouble() * 0.25);
				int j1 = -1;
				byte top = GRASS;
				byte filler = DIRT;
				for (int k1 = HEIGHT - 1; k1 >= 0; k1--) {
					int l1 = (l * 16 + k) * HEIGHT + k1;
					if (k1 < 127 - hellRNG.nextInt(5) && k1 > hellRNG.nextInt(5)) {
						byte here = blocks[l1];
						if (here == AIR) {
							j1 = -1;
						} else if (here == STONE) {
							if (j1 == -1) {
								if (i1 <= 0) {
									top = AIR;
									filler = STONE;
								} else if (k1 >= seaLevel - 4 && k1 <= seaLevel + 1) {
									top = STONE;
									filler = STONE;
									if (flag1) {
										top = GRASS;
										filler = DIRT;
									}
									if (flag) {
										top = GRASS;
										filler = DIRT;
									}
								}
								j1 = i1;
								blocks[l1] = k1 >= seaLevel - 1 ? top : filler;
							} else if (j1 > 0) {
								j1--;
								blocks[l1] = filler;
							}
						}
					} else {
						blocks[l1] = AIR;
					}
				}
			}
		}
	}
}
