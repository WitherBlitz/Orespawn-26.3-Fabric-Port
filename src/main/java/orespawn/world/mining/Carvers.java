package orespawn.world.mining;

import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

/**
 * 1.7.10's {@code MapGenCaves} and {@code MapGenRavine} (with Forge's digBlock / isOceanBlock / isTopBlock), as
 * OreSpawn's Mining dimension ran them on each new chunk's block array. Range 8 chunks; caves turn to lava below y 10;
 * a tunnel that would touch water isn't dug; grass is moved down onto dirt when a cave opens the surface.
 * {@code top} and {@code filler} are the biome's blocks as the chunk's surface pass left them.
 */
final class Carvers {
	private static final int RANGE = 8;

	private Carvers() {
	}

	/** MapGenBase.func_151539_a for caves. */
	static void caves(long seed, int chunkX, int chunkZ, BlockState[] blocks, BlockState top, BlockState filler) {
		Random rand = new Random(seed);
		long l = rand.nextLong();
		long i1 = rand.nextLong();
		Caves caves = new Caves(rand, blocks, top, filler);
		for (int j1 = chunkX - RANGE; j1 <= chunkX + RANGE; j1++) {
			for (int k1 = chunkZ - RANGE; k1 <= chunkZ + RANGE; k1++) {
				long l1 = (long) j1 * l;
				long i2 = (long) k1 * i1;
				rand.setSeed(l1 ^ i2 ^ seed);
				caves.recursiveGenerate(j1, k1, chunkX, chunkZ);
			}
		}
	}

	/** MapGenBase.func_151539_a for ravines. */
	static void ravines(long seed, int chunkX, int chunkZ, BlockState[] blocks, BlockState top, BlockState filler) {
		Random rand = new Random(seed);
		long l = rand.nextLong();
		long i1 = rand.nextLong();
		Ravines ravines = new Ravines(rand, blocks, top, filler);
		for (int j1 = chunkX - RANGE; j1 <= chunkX + RANGE; j1++) {
			for (int k1 = chunkZ - RANGE; k1 <= chunkZ + RANGE; k1++) {
				long l1 = (long) j1 * l;
				long i2 = (long) k1 * i1;
				rand.setSeed(l1 ^ i2 ^ seed);
				ravines.recursiveGenerate(j1, k1, chunkX, chunkZ);
			}
		}
	}

	private static boolean isOceanBlock(BlockState[] data, int index) {
		return data[index] == MiningTerrain.WATER;
	}

	/** Forge's digBlock (the same for caves and ravines, apart from the lava they leave below y 10). */
	private static void digBlock(BlockState[] data, int index, int y, boolean foundTop, BlockState top, BlockState filler) {
		BlockState block = data[index];
		if (block == MiningTerrain.STONE || block == filler || block == top) {
			if (y < 10) {
				data[index] = MiningTerrain.LAVA;
			} else {
				data[index] = null;
				if (foundTop && data[index - 1] == filler) {
					data[index - 1] = top;
				}
			}
		}
	}

	private static final class Caves {
		private final Random rand;
		private final BlockState[] blocks;
		private final BlockState top;
		private final BlockState filler;

		Caves(Random rand, BlockState[] blocks, BlockState top, BlockState filler) {
			this.rand = rand;
			this.blocks = blocks;
			this.top = top;
			this.filler = filler;
		}

		/** func_151542_a: a large cave room. */
		private void addRoom(long seed, int chunkX, int chunkZ, double x, double y, double z) {
			this.addTunnel(seed, chunkX, chunkZ, x, y, z, 1.0F + this.rand.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5);
		}

		/** func_151541_a. */
		private void addTunnel(long seed, int chunkX, int chunkZ, double x, double y, double z, float width, float yaw, float pitch, int step,
				int steps, double heightScale) {
			BlockState[] data = this.blocks;
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
				double d6 = 1.5 + Legacy1710.sin(step * (float) Math.PI / steps) * width * 1.0F;
				double d7 = d6 * heightScale;
				float f5 = Legacy1710.cos(pitch);
				float f6 = Legacy1710.sin(pitch);
				x += Legacy1710.cos(yaw) * f5;
				y += f6;
				z += Legacy1710.sin(yaw) * f5;
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
					this.addTunnel(random.nextLong(), chunkX, chunkZ, x, y, z, random.nextFloat() * 0.5F + 0.5F, yaw - (float) Math.PI / 2.0F,
						pitch / 3.0F, step, steps, 1.0);
					this.addTunnel(random.nextLong(), chunkX, chunkZ, x, y, z, random.nextFloat() * 0.5F + 0.5F, yaw + (float) Math.PI / 2.0F,
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
						int i4 = Legacy1710.floor(x - d6) - chunkX * 16 - 1;
						int l1 = Legacy1710.floor(x + d6) - chunkX * 16 + 1;
						int j4 = Legacy1710.floor(y - d7) - 1;
						int i2 = Legacy1710.floor(y + d7) + 1;
						int k4 = Legacy1710.floor(z - d6) - chunkZ * 16 - 1;
						int j2 = Legacy1710.floor(z + d6) - chunkZ * 16 + 1;
						if (i4 < 0) {
							i4 = 0;
						}
						if (l1 > 16) {
							l1 = 16;
						}
						if (j4 < 1) {
							j4 = 1;
						}
						if (i2 > 248) {
							i2 = 248;
						}
						if (k4 < 0) {
							k4 = 0;
						}
						if (j2 > 16) {
							j2 = 16;
						}
						boolean water = false;
						for (int k2 = i4; !water && k2 < l1; k2++) {
							for (int l2 = k4; !water && l2 < j2; l2++) {
								for (int i3 = i2 + 1; !water && i3 >= j4 - 1; i3--) {
									int j3 = (k2 * 16 + l2) * 256 + i3;
									if (i3 >= 0 && i3 < 256) {
										if (isOceanBlock(data, j3)) {
											water = true;
										}
										if (i3 != j4 - 1 && k2 != i4 && k2 != l1 - 1 && l2 != k4 && l2 != j2 - 1) {
											i3 = j4;
										}
									}
								}
							}
						}
						if (!water) {
							for (int k2 = i4; k2 < l1; k2++) {
								double d13 = (k2 + chunkX * 16 + 0.5 - x) / d6;
								for (int j3 = k4; j3 < j2; j3++) {
									double d14 = (j3 + chunkZ * 16 + 0.5 - z) / d6;
									int k3 = (k2 * 16 + j3) * 256 + i2;
									boolean foundTop = false;
									if (d13 * d13 + d14 * d14 < 1.0) {
										for (int l3 = i2 - 1; l3 >= j4; l3--) {
											double d12 = (l3 + 0.5 - y) / d7;
											if (d12 > -0.7 && d13 * d13 + d12 * d12 + d14 * d14 < 1.0) {
												if (data[k3] == this.top) {
													foundTop = true;
												}
												digBlock(data, k3, l3, foundTop, this.top, this.filler);
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

		/** func_151538_a: the caves that start in chunk (sx, sz), carved into chunk (chunkX, chunkZ). */
		void recursiveGenerate(int sx, int sz, int chunkX, int chunkZ) {
			Random rand = this.rand;
			int i1 = rand.nextInt(rand.nextInt(rand.nextInt(15) + 1) + 1);
			if (rand.nextInt(7) != 0) {
				i1 = 0;
			}
			for (int j1 = 0; j1 < i1; j1++) {
				double d0 = sx * 16 + rand.nextInt(16);
				double d1 = rand.nextInt(rand.nextInt(120) + 8);
				double d2 = sz * 16 + rand.nextInt(16);
				int k1 = 1;
				if (rand.nextInt(4) == 0) {
					this.addRoom(rand.nextLong(), chunkX, chunkZ, d0, d1, d2);
					k1 += rand.nextInt(4);
				}
				for (int l1 = 0; l1 < k1; l1++) {
					float f = rand.nextFloat() * (float) Math.PI * 2.0F;
					float f1 = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
					float f2 = rand.nextFloat() * 2.0F + rand.nextFloat();
					if (rand.nextInt(10) == 0) {
						f2 *= rand.nextFloat() * rand.nextFloat() * 3.0F + 1.0F;
					}
					this.addTunnel(rand.nextLong(), chunkX, chunkZ, d0, d1, d2, f2, f, f1, 0, 0, 1.0);
				}
			}
		}
	}

	private static final class Ravines {
		private final Random rand;
		private final BlockState[] blocks;
		private final BlockState top;
		private final BlockState filler;
		private final float[] widths = new float[1024];

		Ravines(Random rand, BlockState[] blocks, BlockState top, BlockState filler) {
			this.rand = rand;
			this.blocks = blocks;
			this.top = top;
			this.filler = filler;
		}

		/** func_151540_a. */
		private void addTunnel(long seed, int chunkX, int chunkZ, double x, double y, double z, float width, float yaw, float pitch, int step,
				int steps, double heightScale) {
			BlockState[] data = this.blocks;
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
				this.widths[k1] = f5 * f5;
			}
			for (; step < steps; step++) {
				double d12 = 1.5 + Legacy1710.sin(step * (float) Math.PI / steps) * width * 1.0F;
				double d6 = d12 * heightScale;
				d12 *= random.nextFloat() * 0.25 + 0.75;
				d6 *= random.nextFloat() * 0.25 + 0.75;
				float f6 = Legacy1710.cos(pitch);
				float f7 = Legacy1710.sin(pitch);
				x += Legacy1710.cos(yaw) * f6;
				y += f7;
				z += Legacy1710.sin(yaw) * f6;
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
						int i4 = Legacy1710.floor(x - d12) - chunkX * 16 - 1;
						int l1 = Legacy1710.floor(x + d12) - chunkX * 16 + 1;
						int j4 = Legacy1710.floor(y - d6) - 1;
						int i2 = Legacy1710.floor(y + d6) + 1;
						int k4 = Legacy1710.floor(z - d12) - chunkZ * 16 - 1;
						int j2 = Legacy1710.floor(z + d12) - chunkZ * 16 + 1;
						if (i4 < 0) {
							i4 = 0;
						}
						if (l1 > 16) {
							l1 = 16;
						}
						if (j4 < 1) {
							j4 = 1;
						}
						if (i2 > 248) {
							i2 = 248;
						}
						if (k4 < 0) {
							k4 = 0;
						}
						if (j2 > 16) {
							j2 = 16;
						}
						boolean water = false;
						for (int k2 = i4; !water && k2 < l1; k2++) {
							for (int l2 = k4; !water && l2 < j2; l2++) {
								for (int i3 = i2 + 1; !water && i3 >= j4 - 1; i3--) {
									int j3 = (k2 * 16 + l2) * 256 + i3;
									if (i3 >= 0 && i3 < 256) {
										if (isOceanBlock(data, j3)) {
											water = true;
										}
										if (i3 != j4 - 1 && k2 != i4 && k2 != l1 - 1 && l2 != k4 && l2 != j2 - 1) {
											i3 = j4;
										}
									}
								}
							}
						}
						if (!water) {
							for (int k2 = i4; k2 < l1; k2++) {
								double d13 = (k2 + chunkX * 16 + 0.5 - x) / d12;
								for (int j3 = k4; j3 < j2; j3++) {
									double d14 = (j3 + chunkZ * 16 + 0.5 - z) / d12;
									int k3 = (k2 * 16 + j3) * 256 + i2;
									boolean foundTop = false;
									if (d13 * d13 + d14 * d14 < 1.0) {
										for (int l3 = i2 - 1; l3 >= j4; l3--) {
											double d11 = (l3 + 0.5 - y) / d6;
											if ((d13 * d13 + d14 * d14) * this.widths[l3] + d11 * d11 / 6.0 < 1.0) {
												if (data[k3] == this.top) {
													foundTop = true;
												}
												digBlock(data, k3, l3, foundTop, this.top, this.filler);
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

		/** func_151538_a: one ravine in fifty chunks. */
		void recursiveGenerate(int sx, int sz, int chunkX, int chunkZ) {
			Random rand = this.rand;
			if (rand.nextInt(50) == 0) {
				double d0 = sx * 16 + rand.nextInt(16);
				double d1 = rand.nextInt(rand.nextInt(40) + 8) + 20;
				double d2 = sz * 16 + rand.nextInt(16);
				int b0 = 1;
				for (int i1 = 0; i1 < b0; i1++) {
					float f = rand.nextFloat() * (float) Math.PI * 2.0F;
					float f1 = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
					float f2 = (rand.nextFloat() * 2.0F + rand.nextFloat()) * 2.0F;
					this.addTunnel(rand.nextLong(), chunkX, chunkZ, d0, d1, d2, f2, f, f1, 0, 0, 3.0);
				}
			}
		}
	}
}
