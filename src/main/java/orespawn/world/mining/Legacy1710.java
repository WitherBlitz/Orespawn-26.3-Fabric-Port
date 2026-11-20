package orespawn.world.mining;

import java.util.Random;

/**
 * The parts of Minecraft 1.7.10 that OreSpawn's {@code ChunkProviderOreSpawn2} (the Mining dimension) builds its terrain
 * with, kept exactly as they were so a seed gives the same hills as it did in 1.7.10: {@code MathHelper}'s sine table
 * and floors, and the noise generators {@code NoiseGeneratorImproved}, {@code NoiseGeneratorOctaves},
 * {@code NoiseGeneratorSimplex} and {@code NoiseGeneratorPerlin}. Every noise object is immutable once built (each call
 * fills an array the caller owns), so one can be shared by all worldgen threads.
 */
final class Legacy1710 {
	private static final float[] SIN_TABLE = new float[65536];

	static {
		for (int i = 0; i < 65536; i++) {
			SIN_TABLE[i] = (float) Math.sin(i * Math.PI * 2.0 / 65536.0);
		}
	}

	private Legacy1710() {
	}

	/** MathHelper.sin. */
	static float sin(float f) {
		return SIN_TABLE[(int) (f * 10430.378F) & 65535];
	}

	/** MathHelper.cos. */
	static float cos(float f) {
		return SIN_TABLE[(int) (f * 10430.378F + 16384.0F) & 65535];
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

	/** MathHelper.denormalizeClamp. */
	static double denormalizeClamp(double a, double b, double t) {
		return t < 0.0 ? a : (t > 1.0 ? b : a + (b - a) * t);
	}

	/** NoiseGeneratorImproved: Perlin's improved noise with 1.7.10's array fill (and its 2D special case). */
	static final class Improved {
		private static final double[] GX = {1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, -1.0, 0.0};
		private static final double[] GY = {1.0, 1.0, -1.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0};
		private static final double[] GZ = {0.0, 0.0, 0.0, 0.0, 1.0, 1.0, -1.0, -1.0, 1.0, 1.0, -1.0, -1.0, 0.0, 1.0, 0.0, -1.0};
		private final int[] p = new int[512];
		private final double xCoord;
		private final double yCoord;
		private final double zCoord;

		Improved(Random random) {
			this.xCoord = random.nextDouble() * 256.0;
			this.yCoord = random.nextDouble() * 256.0;
			this.zCoord = random.nextDouble() * 256.0;
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

		private static double grad2(int hash, double x, double z) {
			int h = hash & 15;
			return GX[h] * x + GZ[h] * z;
		}

		private static double grad(int hash, double x, double y, double z) {
			int h = hash & 15;
			return GX[h] * x + GY[h] * y + GZ[h] * z;
		}

		/** populateNoiseArray: adds this octave into {@code out}, x outermost, then z, then y. */
		void populate(double[] out, double xOff, double yOff, double zOff, int xSize, int ySize, int zSize, double xScale, double yScale,
				double zScale, double noiseScale) {
			int[] p = this.p;
			if (ySize == 1) {
				int index = 0;
				double amp = 1.0 / noiseScale;
				for (int ix = 0; ix < xSize; ix++) {
					double dx = xOff + ix * xScale + this.xCoord;
					int fx = (int) dx;
					if (dx < fx) {
						fx--;
					}
					int x = fx & 255;
					dx -= fx;
					double u = dx * dx * dx * (dx * (dx * 6.0 - 15.0) + 10.0);
					for (int iz = 0; iz < zSize; iz++) {
						double dz = zOff + iz * zScale + this.zCoord;
						int fz = (int) dz;
						if (dz < fz) {
							fz--;
						}
						int z = fz & 255;
						dz -= fz;
						double w = dz * dz * dz * (dz * (dz * 6.0 - 15.0) + 10.0);
						int a = p[x] + 0;
						int aa = p[a] + z;
						int b = p[x + 1] + 0;
						int ba = p[b] + z;
						double l1 = lerp(u, grad2(p[aa], dx, dz), grad(p[ba], dx - 1.0, 0.0, dz));
						double l2 = lerp(u, grad(p[aa + 1], dx, 0.0, dz - 1.0), grad(p[ba + 1], dx - 1.0, 0.0, dz - 1.0));
						out[index++] += lerp(w, l1, l2) * amp;
					}
				}
				return;
			}
			int index = 0;
			double amp = 1.0 / noiseScale;
			int lastY = -1;
			double l1 = 0.0;
			double l2 = 0.0;
			double l3 = 0.0;
			double l4 = 0.0;
			for (int ix = 0; ix < xSize; ix++) {
				double dx = xOff + ix * xScale + this.xCoord;
				int fx = (int) dx;
				if (dx < fx) {
					fx--;
				}
				int x = fx & 255;
				dx -= fx;
				double u = dx * dx * dx * (dx * (dx * 6.0 - 15.0) + 10.0);
				for (int iz = 0; iz < zSize; iz++) {
					double dz = zOff + iz * zScale + this.zCoord;
					int fz = (int) dz;
					if (dz < fz) {
						fz--;
					}
					int z = fz & 255;
					dz -= fz;
					double w = dz * dz * dz * (dz * (dz * 6.0 - 15.0) + 10.0);
					for (int iy = 0; iy < ySize; iy++) {
						double dy = yOff + iy * yScale + this.yCoord;
						int fy = (int) dy;
						if (dy < fy) {
							fy--;
						}
						int y = fy & 255;
						dy -= fy;
						double v = dy * dy * dy * (dy * (dy * 6.0 - 15.0) + 10.0);
						// 1.7.10 only recomputed the corner gradients when y crossed a lattice line (or at the column top).
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
						double m1 = lerp(v, l1, l2);
						double m2 = lerp(v, l3, l4);
						out[index++] += lerp(w, m1, m2) * amp;
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

		/** generateNoiseOctaves (3D, func_76304_a); always fills a new array. */
		double[] generate(int x, int y, int z, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale) {
			double[] out = new double[xSize * ySize * zSize];
			double d6 = 1.0;
			for (Improved octave : this.octaves) {
				double d3 = x * d6 * xScale;
				double d4 = y * d6 * yScale;
				double d5 = z * d6 * zScale;
				long i2 = lfloor(d3);
				long j2 = lfloor(d5);
				d3 -= i2;
				d5 -= j2;
				i2 %= 16777216L;
				j2 %= 16777216L;
				d3 += i2;
				d5 += j2;
				octave.populate(out, d3, d4, d5, xSize, ySize, zSize, xScale * d6, yScale * d6, zScale * d6, d6);
				d6 /= 2.0;
			}
			return out;
		}

		/** generateNoiseOctaves (2D, func_76305_a): y is 10 and its scale 1, as 1.7.10 passed them. */
		double[] generate2D(int x, int z, int xSize, int zSize, double xScale, double zScale) {
			return this.generate(x, 10, z, xSize, 1, zSize, xScale, 1.0, zScale);
		}
	}

	/** NoiseGeneratorSimplex (2D only; the array fill that NoiseGeneratorPerlin uses). */
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
			random.nextDouble(); // zo, unused in 2D
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

		/** 1.7.10's fast floor: one lower even at exact whole numbers above zero... and at zero. */
		private static int fastFloor(double d) {
			return d > 0.0 ? (int) d : (int) d - 1;
		}

		private static double dot(int[] g, double x, double y) {
			return g[0] * x + g[1] * y;
		}

		/** func_151606_a: adds into {@code out}, z (rows) outermost and x inner. */
		void add(double[] out, double xOff, double zOff, int xSize, int zSize, double xScale, double zScale, double amplitude) {
			int[] p = this.p;
			int index = 0;
			for (int row = 0; row < zSize; row++) {
				double d6 = (zOff + row) * zScale + this.yo;
				for (int col = 0; col < xSize; col++) {
					double d7 = (xOff + col) * xScale + this.xo;
					double d11 = (d7 + d6) * F2;
					int j1 = fastFloor(d7 + d11);
					int k1 = fastFloor(d6 + d11);
					double d12 = (j1 + k1) * G2;
					double d13 = j1 - d12;
					double d14 = k1 - d12;
					double d15 = d7 - d13;
					double d16 = d6 - d14;
					int b0;
					int b1;
					if (d15 > d16) {
						b0 = 1;
						b1 = 0;
					} else {
						b0 = 0;
						b1 = 1;
					}
					double d17 = d15 - b0 + G2;
					double d18 = d16 - b1 + G2;
					double d19 = d15 - 1.0 + 2.0 * G2;
					double d20 = d16 - 1.0 + 2.0 * G2;
					int l1 = j1 & 255;
					int i2 = k1 & 255;
					int j2 = p[l1 + p[i2]] % 12;
					int k2 = p[l1 + b0 + p[i2 + b1]] % 12;
					int l2 = p[l1 + 1 + p[i2 + 1]] % 12;
					double d21 = 0.5 - d15 * d15 - d16 * d16;
					double d8;
					if (d21 < 0.0) {
						d8 = 0.0;
					} else {
						d21 *= d21;
						d8 = d21 * d21 * dot(GRAD[j2], d15, d16);
					}
					double d22 = 0.5 - d17 * d17 - d18 * d18;
					double d9;
					if (d22 < 0.0) {
						d9 = 0.0;
					} else {
						d22 *= d22;
						d9 = d22 * d22 * dot(GRAD[k2], d17, d18);
					}
					double d23 = 0.5 - d19 * d19 - d20 * d20;
					double d10;
					if (d23 < 0.0) {
						d10 = 0.0;
					} else {
						d23 *= d23;
						d10 = d23 * d23 * dot(GRAD[l2], d19, d20);
					}
					out[index++] += 70.0 * (d8 + d9 + d10) * amplitude;
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
