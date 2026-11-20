package orespawn.world.villagemania;

import java.util.Random;

/**
 * Minecraft 1.7.10's math and noise as ChunkProviderOreSpawn3 used them (MathHelper's sine table, NoiseGeneratorImproved,
 * NoiseGeneratorOctaves, NoiseGeneratorSimplex and NoiseGeneratorPerlin), so Village Mania's terrain comes out of a
 * seed the way OreSpawn made it.
 */
final class Legacy {
	private static final float[] SIN = new float[65536];

	static {
		for (int i = 0; i < 65536; i++) {
			SIN[i] = (float) Math.sin(i * Math.PI * 2.0 / 65536.0);
		}
	}

	private Legacy() {
	}

	/** MathHelper.sin: the 65536-entry table. */
	static float sin(float f) {
		return SIN[(int) (f * 10430.378F) & 65535];
	}

	/** MathHelper.cos. */
	static float cos(float f) {
		return SIN[(int) (f * 10430.378F + 16384.0F) & 65535];
	}

	/** MathHelper.sqrt_float. */
	static float sqrt(float f) {
		return (float) Math.sqrt(f);
	}

	/** MathHelper.floor_double. */
	static int floor(double d) {
		int i = (int) d;
		return d < i ? i - 1 : i;
	}

	/** MathHelper.floor_double_long. */
	static long lfloor(double d) {
		long i = (long) d;
		return d < i ? i - 1L : i;
	}

	/** MathHelper.denormalizeClamp (func_151238_b). */
	static double denormalizeClamp(double a, double b, double t) {
		return t < 0.0 ? a : t > 1.0 ? b : a + (b - a) * t;
	}

	/** NoiseGeneratorImproved: one octave of Perlin's improved noise. */
	static final class Improved {
		private static final double[] GX = {1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, -1.0, 0.0};
		private static final double[] GY = {1.0, 1.0, -1.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0};
		private static final double[] GZ = {0.0, 0.0, 0.0, 0.0, 1.0, 1.0, -1.0, -1.0, 1.0, 1.0, -1.0, -1.0, 0.0, 1.0, 0.0, -1.0};
		private final int[] p = new int[512];
		private final double xo;
		private final double yo;
		private final double zo;

		Improved(Random random) {
			this.xo = random.nextDouble() * 256.0;
			this.yo = random.nextDouble() * 256.0;
			this.zo = random.nextDouble() * 256.0;
			for (int i = 0; i < 256; i++) {
				this.p[i] = i;
			}
			for (int i = 0; i < 256; i++) {
				int j = random.nextInt(256 - i) + i;
				int k = this.p[i];
				this.p[i] = this.p[j];
				this.p[j] = k;
				this.p[i + 256] = this.p[i];
			}
		}

		private static double lerp(double t, double a, double b) {
			return a + t * (b - a);
		}

		/** func_76309_a: the 2D gradient (x and z only). */
		private static double grad2(int hash, double x, double z) {
			int h = hash & 15;
			return GX[h] * x + GZ[h] * z;
		}

		private static double grad(int hash, double x, double y, double z) {
			int h = hash & 15;
			return GX[h] * x + GY[h] * y + GZ[h] * z;
		}

		private static double fade(double t) {
			return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
		}

		/** populateNoiseArray: adds this octave into {@code out}, x outermost, then z, then y. */
		void populate(double[] out, double xOff, double yOff, double zOff, int xSize, int ySize, int zSize, double xScale, double yScale,
				double zScale, double noiseScale) {
			int[] p = this.p;
			double amp = 1.0 / noiseScale;
			int index = 0;
			if (ySize == 1) {
				for (int ix = 0; ix < xSize; ix++) {
					double dx = xOff + ix * xScale + this.xo;
					int fx = (int) dx;
					if (dx < fx) {
						fx--;
					}
					int x = fx & 255;
					dx -= fx;
					double u = fade(dx);
					for (int iz = 0; iz < zSize; iz++) {
						double dz = zOff + iz * zScale + this.zo;
						int fz = (int) dz;
						if (dz < fz) {
							fz--;
						}
						int z = fz & 255;
						dz -= fz;
						double w = fade(dz);
						int a = p[x];
						int aa = p[a] + z;
						int b = p[x + 1];
						int ba = p[b] + z;
						double l1 = lerp(u, grad2(p[aa], dx, dz), grad(p[ba], dx - 1.0, 0.0, dz));
						double l2 = lerp(u, grad(p[aa + 1], dx, 0.0, dz - 1.0), grad(p[ba + 1], dx - 1.0, 0.0, dz - 1.0));
						out[index++] += lerp(w, l1, l2) * amp;
					}
				}
				return;
			}
			int lastY = -1;
			double l1 = 0.0;
			double l2 = 0.0;
			double l3 = 0.0;
			double l4 = 0.0;
			for (int ix = 0; ix < xSize; ix++) {
				double dx = xOff + ix * xScale + this.xo;
				int fx = (int) dx;
				if (dx < fx) {
					fx--;
				}
				int x = fx & 255;
				dx -= fx;
				double u = fade(dx);
				for (int iz = 0; iz < zSize; iz++) {
					double dz = zOff + iz * zScale + this.zo;
					int fz = (int) dz;
					if (dz < fz) {
						fz--;
					}
					int z = fz & 255;
					dz -= fz;
					double w = fade(dz);
					for (int iy = 0; iy < ySize; iy++) {
						double dy = yOff + iy * yScale + this.yo;
						int fy = (int) dy;
						if (dy < fy) {
							fy--;
						}
						int y = fy & 255;
						dy -= fy;
						double v = fade(dy);
						// 1.7.10 recomputed the corner gradients only at a column's top or when y crossed a lattice line.
						if (iy == 0 || y != lastY) {
							lastY = y;
							int a = p[x] + y;
							int aa = p[a] + z;
							int ab = p[a + 1] + z;
							int b = p[x + 1] + y;
							int ba = p[b] + z;
							int bb = p[b + 1] + z;
							l1 = lerp(u, grad(p[aa], dx, dy, dz), grad(p[ba], dx - 1.0, dy, dz));
							l2 = lerp(u, grad(p[ab], dx, dy - 1.0, dz), grad(p[bb], dx - 1.0, dy - 1.0, dz));
							l3 = lerp(u, grad(p[aa + 1], dx, dy, dz - 1.0), grad(p[ba + 1], dx - 1.0, dy, dz - 1.0));
							l4 = lerp(u, grad(p[ab + 1], dx, dy - 1.0, dz - 1.0), grad(p[bb + 1], dx - 1.0, dy - 1.0, dz - 1.0));
						}
						out[index++] += lerp(w, lerp(v, l1, l2), lerp(v, l3, l4)) * amp;
					}
				}
			}
		}
	}

	/** NoiseGeneratorOctaves. */
	static final class Octaves {
		private final Improved[] octaves;

		Octaves(Random random, int count) {
			this.octaves = new Improved[count];
			for (int i = 0; i < count; i++) {
				this.octaves[i] = new Improved(random);
			}
		}

		/** generateNoiseOctaves (3D, func_76304_a), into a new array. */
		double[] generate(int x, int y, int z, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale) {
			double[] out = new double[xSize * ySize * zSize];
			double d3 = 1.0;
			for (Improved octave : this.octaves) {
				double d0 = x * d3 * xScale;
				double d1 = y * d3 * yScale;
				double d2 = z * d3 * zScale;
				long i2 = lfloor(d0);
				long j2 = lfloor(d2);
				d0 -= i2;
				d2 -= j2;
				i2 %= 16777216L;
				j2 %= 16777216L;
				d0 += i2;
				d2 += j2;
				octave.populate(out, d0, d1, d2, xSize, ySize, zSize, xScale * d3, yScale * d3, zScale * d3, d3);
				d3 /= 2.0;
			}
			return out;
		}

		/** generateNoiseOctaves (2D, func_76305_a): 1.7.10 passed y 10 with a y scale of 1 and dropped its last argument. */
		double[] generate2D(int x, int z, int xSize, int zSize, double xScale, double zScale) {
			return this.generate(x, 10, z, xSize, 1, zSize, xScale, 1.0, zScale);
		}
	}

	/** NoiseGeneratorSimplex, the 2D array fill NoiseGeneratorPerlin uses. */
	static final class Simplex {
		private static final int[][] GRAD = {{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1},
			{0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};
		private static final double SQRT_3 = Math.sqrt(3.0);
		private static final double F2 = 0.5 * (SQRT_3 - 1.0);
		private static final double G2 = (3.0 - SQRT_3) / 6.0;
		private final int[] p = new int[512];
		private final double xo;
		private final double yo;

		Simplex(Random random) {
			this.xo = random.nextDouble() * 256.0;
			this.yo = random.nextDouble() * 256.0;
			random.nextDouble(); // its z offset, which the 2D noise never uses
			for (int i = 0; i < 256; i++) {
				this.p[i] = i;
			}
			for (int i = 0; i < 256; i++) {
				int j = random.nextInt(256 - i) + i;
				int k = this.p[i];
				this.p[i] = this.p[j];
				this.p[j] = k;
				this.p[i + 256] = this.p[i];
			}
		}

		/** 1.7.10's floor: one lower at zero and at whole numbers below zero. */
		private static int fastFloor(double d) {
			return d > 0.0 ? (int) d : (int) d - 1;
		}

		private static double dot(int[] g, double x, double y) {
			return g[0] * x + g[1] * y;
		}

		/** func_151606_a: adds into {@code out}, rows (z) outermost, x inner. */
		void add(double[] out, double xOff, double zOff, int xSize, int zSize, double xScale, double zScale, double amplitude) {
			int[] p = this.p;
			int index = 0;
			for (int row = 0; row < zSize; row++) {
				double d5 = (zOff + row) * zScale + this.yo;
				for (int col = 0; col < xSize; col++) {
					double d6 = (xOff + col) * xScale + this.xo;
					double d10 = (d6 + d5) * F2;
					int j1 = fastFloor(d6 + d10);
					int k1 = fastFloor(d5 + d10);
					double d11 = (j1 + k1) * G2;
					double d14 = d6 - (j1 - d11);
					double d15 = d5 - (k1 - d11);
					int b0;
					int b1;
					if (d14 > d15) {
						b0 = 1;
						b1 = 0;
					} else {
						b0 = 0;
						b1 = 1;
					}
					double d16 = d14 - b0 + G2;
					double d17 = d15 - b1 + G2;
					double d18 = d14 - 1.0 + 2.0 * G2;
					double d19 = d15 - 1.0 + 2.0 * G2;
					int l1 = j1 & 255;
					int i2 = k1 & 255;
					int j2 = p[l1 + p[i2]] % 12;
					int k2 = p[l1 + b0 + p[i2 + b1]] % 12;
					int l2 = p[l1 + 1 + p[i2 + 1]] % 12;
					double d7 = 0.0;
					double d20 = 0.5 - d14 * d14 - d15 * d15;
					if (d20 >= 0.0) {
						d20 *= d20;
						d7 = d20 * d20 * dot(GRAD[j2], d14, d15);
					}
					double d8 = 0.0;
					double d21 = 0.5 - d16 * d16 - d17 * d17;
					if (d21 >= 0.0) {
						d21 *= d21;
						d8 = d21 * d21 * dot(GRAD[k2], d16, d17);
					}
					double d9 = 0.0;
					double d22 = 0.5 - d18 * d18 - d19 * d19;
					if (d22 >= 0.0) {
						d22 *= d22;
						d9 = d22 * d22 * dot(GRAD[l2], d18, d19);
					}
					out[index++] += 70.0 * (d7 + d8 + d9) * amplitude;
				}
			}
		}
	}

	/** NoiseGeneratorPerlin: octaves of simplex noise. */
	static final class Perlin {
		private final Simplex[] levels;

		Perlin(Random random, int count) {
			this.levels = new Simplex[count];
			for (int i = 0; i < count; i++) {
				this.levels[i] = new Simplex(random);
			}
		}

		/** func_151599_a (persistence 0.5): a new xSize * zSize array, row (z) major. */
		double[] generate(double x, double z, int xSize, int zSize, double xScale, double zScale, double lacunarity) {
			double[] out = new double[xSize * zSize];
			double d7 = 1.0;
			double d6 = 1.0;
			for (Simplex level : this.levels) {
				level.add(out, x, z, xSize, zSize, xScale * d6 * d7, zScale * d6 * d7, 0.55 / d7);
				d6 *= lacunarity;
				d7 *= 0.5;
			}
			return out;
		}
	}
}
