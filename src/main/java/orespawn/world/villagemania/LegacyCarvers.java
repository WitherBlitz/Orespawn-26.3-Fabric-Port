package orespawn.world.villagemania;

import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

/**
 * 1.7.10's {@code MapGenCaves} and {@code MapGenRavine}, with Forge's digBlock / isOceanBlock / isTopBlock, as
 * ChunkProviderOreSpawn3 ran them on each new chunk's block array (x << 12 | z << 8 | y, null is air). Range 8 chunks;
 * they leave lava below y 10, never dig a stretch that would touch water, and put the grass back on the dirt they
 * uncover under it. The Villages biome's top and filler blocks are grass and dirt.
 */
final class LegacyCarvers {
	private static final int RANGE = 8;

	private LegacyCarvers() {
	}

	/** MapGenBase.func_151539_a with MapGenCaves. */
	static void caves(long seed, int chunkX, int chunkZ, BlockState[] blocks) {
		Random rand = new Random(seed);
		long l = rand.nextLong();
		long i1 = rand.nextLong();
		for (int j1 = chunkX - RANGE; j1 <= chunkX + RANGE; j1++) {
			for (int k1 = chunkZ - RANGE; k1 <= chunkZ + RANGE; k1++) {
				rand.setSeed((long) j1 * l ^ (long) k1 * i1 ^ seed);
				caveStarts(rand, j1, k1, chunkX, chunkZ, blocks);
			}
		}
	}

	/** MapGenBase.func_151539_a with MapGenRavine. */
	static void ravines(long seed, int chunkX, int chunkZ, BlockState[] blocks) {
		Random rand = new Random(seed);
		long l = rand.nextLong();
		long i1 = rand.nextLong();
		float[] widths = new float[1024];
		for (int j1 = chunkX - RANGE; j1 <= chunkX + RANGE; j1++) {
			for (int k1 = chunkZ - RANGE; k1 <= chunkZ + RANGE; k1++) {
				rand.setSeed((long) j1 * l ^ (long) k1 * i1 ^ seed);
				if (rand.nextInt(50) == 0) {
					double x = j1 * 16 + rand.nextInt(16);
					double y = rand.nextInt(rand.nextInt(40) + 8) + 20;
					double z = k1 * 16 + rand.nextInt(16);
					float yaw = rand.nextFloat() * (float) Math.PI * 2.0F;
					float pitch = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
					float width = (rand.nextFloat() * 2.0F + rand.nextFloat()) * 2.0F;
					ravine(rand.nextLong(), chunkX, chunkZ, blocks, widths, x, y, z, width, yaw, pitch, 0, 0, 3.0);
				}
			}
		}
	}

	/** isOceanBlock: water. */
	private static boolean isOceanBlock(BlockState[] data, int index) {
		return data[index] == VillageManiaTerrain.WATER;
	}

	/** Forge's digBlock: stone, dirt and grass become air, or lava below y 10; grass found above moves down onto dirt. */
	private static void digBlock(BlockState[] data, int index, int y, boolean foundTop) {
		BlockState block = data[index];
		if (block == VillageManiaTerrain.STONE || block == VillageManiaTerrain.DIRT || block == VillageManiaTerrain.GRASS) {
			if (y < 10) {
				data[index] = VillageManiaTerrain.LAVA;
			} else {
				data[index] = null;
				if (foundTop && data[index - 1] == VillageManiaTerrain.DIRT) {
					data[index - 1] = VillageManiaTerrain.GRASS;
				}
			}
		}
	}

	/** The box of one step; false if it would touch water (isOceanBlock), as 1.7.10 checked before digging. */
	private static boolean dry(BlockState[] data, int i4, int l1, int j4, int i2, int k4, int j2) {
		for (int k2 = i4; k2 < l1; k2++) {
			for (int l3 = k4; l3 < j2; l3++) {
				for (int i3 = i2 + 1; i3 >= j4 - 1; i3--) {
					if (i3 >= 0 && i3 < 256) {
						if (isOceanBlock(data, (k2 * 16 + l3) * 256 + i3)) {
							return false;
						}
						if (i3 != j4 - 1 && k2 != i4 && k2 != l1 - 1 && l3 != k4 && l3 != j2 - 1) {
							i3 = j4;
						}
					}
				}
			}
		}
		return true;
	}

	/** MapGenCaves.func_151538_a: the caves that start in chunk (sx, sz), cut into chunk (chunkX, chunkZ). */
	private static void caveStarts(Random rand, int sx, int sz, int chunkX, int chunkZ, BlockState[] blocks) {
		int count = rand.nextInt(rand.nextInt(rand.nextInt(15) + 1) + 1);
		if (rand.nextInt(7) != 0) {
			count = 0;
		}
		for (int j1 = 0; j1 < count; j1++) {
			double x = sx * 16 + rand.nextInt(16);
			double y = rand.nextInt(rand.nextInt(120) + 8);
			double z = sz * 16 + rand.nextInt(16);
			int tunnels = 1;
			if (rand.nextInt(4) == 0) {
				// func_151542_a: a room.
				tunnel(rand.nextLong(), chunkX, chunkZ, blocks, x, y, z, 1.0F + rand.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5);
				tunnels += rand.nextInt(4);
			}
			for (int l1 = 0; l1 < tunnels; l1++) {
				float yaw = rand.nextFloat() * (float) Math.PI * 2.0F;
				float pitch = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
				float width = rand.nextFloat() * 2.0F + rand.nextFloat();
				if (rand.nextInt(10) == 0) {
					width *= rand.nextFloat() * rand.nextFloat() * 3.0F + 1.0F;
				}
				tunnel(rand.nextLong(), chunkX, chunkZ, blocks, x, y, z, width, yaw, pitch, 0, 0, 1.0);
			}
		}
	}

	/** MapGenCaves.func_151541_a. */
	private static void tunnel(long seed, int chunkX, int chunkZ, BlockState[] data, double x, double y, double z, float width, float yaw,
			float pitch, int step, int steps, double heightScale) {
		double d4 = chunkX * 16 + 8;
		double d5 = chunkZ * 16 + 8;
		float f3 = 0.0F;
		float f4 = 0.0F;
		Random random = new Random(seed);
		if (steps <= 0) {
			int j1 = RANGE * 16 - 16;
			steps = j1 - random.nextInt(j1 / 4);
		}
		boolean room = false;
		if (step == -1) {
			step = steps / 2;
			room = true;
		}
		int branchAt = random.nextInt(steps / 2) + steps / 4;
		for (boolean steep = random.nextInt(6) == 0; step < steps; step++) {
			double d6 = 1.5 + Legacy.sin(step * (float) Math.PI / steps) * width * 1.0F;
			double d7 = d6 * heightScale;
			float f5 = Legacy.cos(pitch);
			float f6 = Legacy.sin(pitch);
			x += Legacy.cos(yaw) * f5;
			y += f6;
			z += Legacy.sin(yaw) * f5;
			if (steep) {
				pitch *= 0.92F;
			} else {
				pitch *= 0.7F;
			}
			pitch += f4 * 0.1F;
			yaw += f3 * 0.1F;
			f4 *= 0.9F;
			f3 *= 0.75F;
			f4 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			f3 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
			if (!room && step == branchAt && width > 1.0F && steps > 0) {
				tunnel(random.nextLong(), chunkX, chunkZ, data, x, y, z, random.nextFloat() * 0.5F + 0.5F, yaw - (float) Math.PI / 2.0F,
					pitch / 3.0F, step, steps, 1.0);
				tunnel(random.nextLong(), chunkX, chunkZ, data, x, y, z, random.nextFloat() * 0.5F + 0.5F, yaw + (float) Math.PI / 2.0F,
					pitch / 3.0F, step, steps, 1.0);
				return;
			}
			if (room || random.nextInt(4) != 0) {
				double d8 = x - d4;
				double d9 = z - d5;
				double d10 = steps - step;
				double d11 = width + 2.0F + 16.0F;
				if (d8 * d8 + d9 * d9 - d10 * d10 > d11 * d11) {
					return;
				}
				if (x >= d4 - 16.0 - d6 * 2.0 && z >= d5 - 16.0 - d6 * 2.0 && x <= d4 + 16.0 + d6 * 2.0 && z <= d5 + 16.0 + d6 * 2.0) {
					int i4 = Math.max(Legacy.floor(x - d6) - chunkX * 16 - 1, 0);
					int l1 = Math.min(Legacy.floor(x + d6) - chunkX * 16 + 1, 16);
					int j4 = Math.max(Legacy.floor(y - d7) - 1, 1);
					int i2 = Math.min(Legacy.floor(y + d7) + 1, 248);
					int k4 = Math.max(Legacy.floor(z - d6) - chunkZ * 16 - 1, 0);
					int j2 = Math.min(Legacy.floor(z + d6) - chunkZ * 16 + 1, 16);
					if (dry(data, i4, l1, j4, i2, k4, j2)) {
						for (int k2 = i4; k2 < l1; k2++) {
							double d13 = (k2 + chunkX * 16 + 0.5 - x) / d6;
							for (int j3 = k4; j3 < j2; j3++) {
								double d14 = (j3 + chunkZ * 16 + 0.5 - z) / d6;
								int k3 = (k2 * 16 + j3) * 256 + i2;
								boolean foundTop = false;
								if (d13 * d13 + d14 * d14 < 1.0) {
									for (int l2 = i2 - 1; l2 >= j4; l2--) {
										double d12 = (l2 + 0.5 - y) / d7;
										if (d12 > -0.7 && d13 * d13 + d12 * d12 + d14 * d14 < 1.0) {
											if (data[k3] == VillageManiaTerrain.GRASS) {
												foundTop = true;
											}
											digBlock(data, k3, l2, foundTop);
										}
										k3--;
									}
								}
							}
						}
						if (room) {
							break;
						}
					}
				}
			}
		}
	}

	/** MapGenRavine.func_151540_a. */
	private static void ravine(long seed, int chunkX, int chunkZ, BlockState[] data, float[] widths, double x, double y, double z, float width,
			float yaw, float pitch, int step, int steps, double heightScale) {
		Random random = new Random(seed);
		double d4 = chunkX * 16 + 8;
		double d5 = chunkZ * 16 + 8;
		float f3 = 0.0F;
		float f4 = 0.0F;
		if (steps <= 0) {
			int j1 = RANGE * 16 - 16;
			steps = j1 - random.nextInt(j1 / 4);
		}
		boolean single = false;
		if (step == -1) {
			step = steps / 2;
			single = true;
		}
		float f5 = 1.0F;
		for (int k1 = 0; k1 < 256; k1++) {
			if (k1 == 0 || random.nextInt(3) == 0) {
				f5 = 1.0F + random.nextFloat() * random.nextFloat() * 1.0F;
			}
			widths[k1] = f5 * f5;
		}
		for (; step < steps; step++) {
			double d12 = 1.5 + Legacy.sin(step * (float) Math.PI / steps) * width * 1.0F;
			double d6 = d12 * heightScale;
			d12 *= random.nextFloat() * 0.25 + 0.75;
			d6 *= random.nextFloat() * 0.25 + 0.75;
			float f6 = Legacy.cos(pitch);
			float f7 = Legacy.sin(pitch);
			x += Legacy.cos(yaw) * f6;
			y += f7;
			z += Legacy.sin(yaw) * f6;
			pitch *= 0.7F;
			pitch += f4 * 0.05F;
			yaw += f3 * 0.05F;
			f4 *= 0.8F;
			f3 *= 0.5F;
			f4 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			f3 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
			if (single || random.nextInt(4) != 0) {
				double d7 = x - d4;
				double d8 = z - d5;
				double d9 = steps - step;
				double d10 = width + 2.0F + 16.0F;
				if (d7 * d7 + d8 * d8 - d9 * d9 > d10 * d10) {
					return;
				}
				if (x >= d4 - 16.0 - d12 * 2.0 && z >= d5 - 16.0 - d12 * 2.0 && x <= d4 + 16.0 + d12 * 2.0 && z <= d5 + 16.0 + d12 * 2.0) {
					int i4 = Math.max(Legacy.floor(x - d12) - chunkX * 16 - 1, 0);
					int l1 = Math.min(Legacy.floor(x + d12) - chunkX * 16 + 1, 16);
					int j4 = Math.max(Legacy.floor(y - d6) - 1, 1);
					int i2 = Math.min(Legacy.floor(y + d6) + 1, 248);
					int k4 = Math.max(Legacy.floor(z - d12) - chunkZ * 16 - 1, 0);
					int j2 = Math.min(Legacy.floor(z + d12) - chunkZ * 16 + 1, 16);
					if (dry(data, i4, l1, j4, i2, k4, j2)) {
						for (int k2 = i4; k2 < l1; k2++) {
							double d13 = (k2 + chunkX * 16 + 0.5 - x) / d12;
							for (int j3 = k4; j3 < j2; j3++) {
								double d14 = (j3 + chunkZ * 16 + 0.5 - z) / d12;
								int k3 = (k2 * 16 + j3) * 256 + i2;
								boolean foundTop = false;
								if (d13 * d13 + d14 * d14 < 1.0) {
									for (int l2 = i2 - 1; l2 >= j4; l2--) {
										double d11 = (l2 + 0.5 - y) / d6;
										if ((d13 * d13 + d14 * d14) * widths[l2] + d11 * d11 / 6.0 < 1.0) {
											if (data[k3] == VillageManiaTerrain.GRASS) {
												foundTop = true;
											}
											digBlock(data, k3, l2, foundTop);
										}
										k3--;
									}
								}
							}
						}
						if (single) {
							break;
						}
					}
				}
			}
		}
	}
}
