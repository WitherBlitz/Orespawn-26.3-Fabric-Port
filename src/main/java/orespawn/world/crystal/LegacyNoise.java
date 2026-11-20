package orespawn.world.crystal;

import java.util.Random;

/**
 * Minecraft 1.7.10's terrain noise, as OreSpawn's ChunkProviderOreSpawn5 used it: NoiseGeneratorImproved (Perlin noise
 * with Ken Perlin's improved fade), NoiseGeneratorOctaves (octaves of it), NoiseGeneratorSimplex and NoiseGeneratorPerlin
 * (octaves of simplex noise, for the surface depth). Kept as 1.7.10 computed them, quirks included, so the Crystal
 * dimension's ground has OreSpawn's shape for the same world seed. Each instance is read-only once built; callers pass
 * their own arrays, so one set can serve several worldgen threads.
 */
final class LegacyNoise {
	private LegacyNoise() {
	}

	/** MathHelper.floor_double_long. */
	static long floorLong(double d) {
		long i = (long) d;
		return d < (double) i ? i - 1L : i;
	}

	/** NoiseGeneratorImproved. */
	static final class Improved {
		private static final double[] GX = {1, -1, 1, -1, 1, -1, 1, -1, 0, 0, 0, 0, 1, 0, -1, 0};
		private static final double[] GY = {1, 1, -1, -1, 0, 0, 0, 0, 1, -1, 1, -1, 1, -1, 1, -1};
		private static final double[] GZ = {0, 0, 0, 0, 1, 1, -1, -1, 1, 1, -1, -1, 0, 1, 0, -1};
		private final int[] p = new int[512];
		private final double xCoord;
		private final double yCoord;
		private final double zCoord;

		Improved(Random random) {
			this.xCoord = random.nextDouble() * 256.0;
			this.yCoord = random.nextDouble() * 256.0;
			this.zCoord = random.nextDouble() * 256.0;
			for (int i = 0; i < 256; i++) this.p[i] = i;
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

		/** func_76309_a: the 2D gradient (x and z). */
		private static double grad2(int hash, double x, double z) {
			int j = hash & 15;
			return GX[j] * x + GZ[j] * z;
		}

		private static double grad(int hash, double x, double y, double z) {
			int j = hash & 15;
			return GX[j] * x + GY[j] * y + GZ[j] * z;
		}

		/** populateNoiseArray: adds this octave into {@code out} (x outermost, then z, then y). */
		void populate(double[] out, double x0, double y0, double z0, int xSize, int ySize, int zSize, double xScale, double yScale,
				double zScale, double amplitude) {
			int[] p = this.p;
			if (ySize == 1) {
				int n = 0;
				double inv = 1.0 / amplitude;
				for (int ix = 0; ix < xSize; ix++) {
					double dx = x0 + ix * xScale + this.xCoord;
					int fx = (int) dx;
					if (dx < fx) fx--;
					int bx = fx & 255;
					dx -= fx;
					double ux = dx * dx * dx * (dx * (dx * 6.0 - 15.0) + 10.0);
					for (int iz = 0; iz < zSize; iz++) {
						double dz = z0 + iz * zScale + this.zCoord;
						int fz = (int) dz;
						if (dz < fz) fz--;
						int bz = fz & 255;
						dz -= fz;
						double uz = dz * dz * dz * (dz * (dz * 6.0 - 15.0) + 10.0);
						int a = p[bx];
						int aa = p[a] + bz;
						int b = p[bx + 1];
						int ba = p[b] + bz;
						double l1 = lerp(ux, grad2(p[aa], dx, dz), grad(p[ba], dx - 1.0, 0.0, dz));
						double l2 = lerp(ux, grad(p[aa + 1], dx, 0.0, dz - 1.0), grad(p[ba + 1], dx - 1.0, 0.0, dz - 1.0));
						out[n++] += lerp(uz, l1, l2) * inv;
					}
				}
			} else {
				int n = 0;
				double inv = 1.0 / amplitude;
				int lastY = -1;
				double c1 = 0.0, c2 = 0.0, c3 = 0.0, c4 = 0.0;
				for (int ix = 0; ix < xSize; ix++) {
					double dx = x0 + ix * xScale + this.xCoord;
					int fx = (int) dx;
					if (dx < fx) fx--;
					int bx = fx & 255;
					dx -= fx;
					double ux = dx * dx * dx * (dx * (dx * 6.0 - 15.0) + 10.0);
					for (int iz = 0; iz < zSize; iz++) {
						double dz = z0 + iz * zScale + this.zCoord;
						int fz = (int) dz;
						if (dz < fz) fz--;
						int bz = fz & 255;
						dz -= fz;
						double uz = dz * dz * dz * (dz * (dz * 6.0 - 15.0) + 10.0);
						for (int iy = 0; iy < ySize; iy++) {
							double dy = y0 + iy * yScale + this.yCoord;
							int fy = (int) dy;
							if (dy < fy) fy--;
							int by = fy & 255;
							dy -= fy;
							double uy = dy * dy * dy * (dy * (dy * 6.0 - 15.0) + 10.0);
							// 1.7.10 only recomputes the corners when the y lattice cell changes (the cache spans columns).
							if (iy == 0 || by != lastY) {
								lastY = by;
								int a = p[bx] + by;
								int aa = p[a] + bz;
								int ab = p[a + 1] + bz;
								int b = p[bx + 1] + by;
								int ba = p[b] + bz;
								int bb = p[b + 1] + bz;
								c1 = lerp(ux, grad(p[aa], dx, dy, dz), grad(p[ba], dx - 1.0, dy, dz));
								c2 = lerp(ux, grad(p[ab], dx, dy - 1.0, dz), grad(p[bb], dx - 1.0, dy - 1.0, dz));
								c3 = lerp(ux, grad(p[aa + 1], dx, dy, dz - 1.0), grad(p[ba + 1], dx - 1.0, dy, dz - 1.0));
								c4 = lerp(ux, grad(p[ab + 1], dx, dy - 1.0, dz - 1.0), grad(p[bb + 1], dx - 1.0, dy - 1.0, dz - 1.0));
							}
							double e1 = lerp(uy, c1, c2);
							double e2 = lerp(uy, c3, c4);
							out[n++] += lerp(uz, e1, e2) * inv;
						}
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
			for (int i = 0; i < count; i++) this.octaves[i] = new Improved(random);
		}

		/** generateNoiseOctaves (3D): a new array of xSize * ySize * zSize, x outermost, then z, then y. */
		double[] generate(int x, int y, int z, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale) {
			double[] out = new double[xSize * ySize * zSize];
			double d = 1.0;
			for (Improved octave : this.octaves) {
				double dx = x * d * xScale;
				double dy = y * d * yScale;
				double dz = z * d * zScale;
				long lx = floorLong(dx);
				long lz = floorLong(dz);
				dx -= lx;
				dz -= lz;
				lx %= 16777216L;
				lz %= 16777216L;
				dx += lx;
				dz += lz;
				octave.populate(out, dx, dy, dz, xSize, ySize, zSize, xScale * d, yScale * d, zScale * d, d);
				d /= 2.0;
			}
			return out;
		}

		/** generateNoiseOctaves (2D): y 10, one layer, y scale 1. */
		double[] generate2D(int x, int z, int xSize, int zSize, double xScale, double zScale) {
			return this.generate(x, 10, z, xSize, 1, zSize, xScale, 1.0, zScale);
		}
	}

	/** NoiseGeneratorSimplex (2D only, as NoiseGeneratorPerlin uses it). */
	static final class Simplex {
		private static final int[][] GRAD = {{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1},
			{0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};
		private static final double SQRT3 = Math.sqrt(3.0);
		private static final double F2 = 0.5 * (SQRT3 - 1.0);
		private static final double G2 = (3.0 - SQRT3) / 6.0;
		private final int[] p = new int[512];
		private final double xo;
		private final double yo;

		Simplex(Random random) {
			this.xo = random.nextDouble() * 256.0;
			this.yo = random.nextDouble() * 256.0;
			random.nextDouble(); // the z offset, unused in 2D
			for (int i = 0; i < 256; i++) this.p[i] = i;
			for (int i = 0; i < 256; i++) {
				int j = random.nextInt(256 - i) + i;
				int k = this.p[i];
				this.p[i] = this.p[j];
				this.p[j] = k;
				this.p[i + 256] = this.p[i];
			}
		}

		/** func_151607_a: 1.7.10's floor, which gives -1 for exactly 0. */
		private static int fastFloor(double d) {
			return d > 0.0 ? (int) d : (int) d - 1;
		}

		private static double dot(int[] g, double x, double y) {
			return g[0] * x + g[1] * y;
		}

		/** func_151606_a: adds this octave into {@code out} (z outermost, then x). */
		void add(double[] out, double x0, double z0, int xSize, int zSize, double xScale, double zScale, double amplitude) {
			int n = 0;
			for (int iz = 0; iz < zSize; iz++) {
				double y = (z0 + iz) * zScale + this.yo;
				for (int ix = 0; ix < xSize; ix++) {
					double x = (x0 + ix) * xScale + this.xo;
					double s = (x + y) * F2;
					int i = fastFloor(x + s);
					int j = fastFloor(y + s);
					double t = (i + j) * G2;
					double x1 = x - (i - t);
					double y1 = y - (j - t);
					int i1, j1;
					if (x1 > y1) {
						i1 = 1;
						j1 = 0;
					} else {
						i1 = 0;
						j1 = 1;
					}
					double x2 = x1 - i1 + G2;
					double y2 = y1 - j1 + G2;
					double x3 = x1 - 1.0 + 2.0 * G2;
					double y3 = y1 - 1.0 + 2.0 * G2;
					int ii = i & 255;
					int jj = j & 255;
					int g0 = this.p[ii + this.p[jj]] % 12;
					int g1 = this.p[ii + i1 + this.p[jj + j1]] % 12;
					int g2 = this.p[ii + 1 + this.p[jj + 1]] % 12;
					double t0 = 0.5 - x1 * x1 - y1 * y1;
					double n0 = 0.0;
					if (t0 >= 0.0) {
						t0 *= t0;
						n0 = t0 * t0 * dot(GRAD[g0], x1, y1);
					}
					double t1 = 0.5 - x2 * x2 - y2 * y2;
					double n1 = 0.0;
					if (t1 >= 0.0) {
						t1 *= t1;
						n1 = t1 * t1 * dot(GRAD[g1], x2, y2);
					}
					double t2 = 0.5 - x3 * x3 - y3 * y3;
					double n2 = 0.0;
					if (t2 >= 0.0) {
						t2 *= t2;
						n2 = t2 * t2 * dot(GRAD[g2], x3, y3);
					}
					out[n++] += 70.0 * (n0 + n1 + n2) * amplitude;
				}
			}
		}
	}

	/** NoiseGeneratorPerlin: octaves of simplex noise. */
	static final class Perlin {
		private final Simplex[] octaves;

		Perlin(Random random, int count) {
			this.octaves = new Simplex[count];
			for (int i = 0; i < count; i++) this.octaves[i] = new Simplex(random);
		}

		/** func_151599_a / func_151600_a with the default amplitude step of 0.5: index z * xSize + x. */
		double[] generate(double x, double z, int xSize, int zSize, double xScale, double zScale, double frequencyStep) {
			double[] out = new double[xSize * zSize];
			double d7 = 1.0;
			double d6 = 1.0;
			for (Simplex octave : this.octaves) {
				octave.add(out, x, z, xSize, zSize, xScale * d6 * d7, zScale * d6 * d7, 0.55 / d7);
				d6 *= frequencyStep;
				d7 *= 0.5;
			}
			return out;
		}
	}
}
