package orespawn.world.utopia;

import java.util.Random;

/**
 * Minecraft 1.7.10's terrain noise ({@code NoiseGeneratorImproved}, {@code NoiseGeneratorOctaves},
 * {@code NoiseGeneratorSimplex} and {@code NoiseGeneratorPerlin}), which OreSpawn's {@code ChunkProviderOreSpawn} builds
 * Utopia from. Kept as 1.7.10 had them (seeded from {@link Random} in the same order) so Utopia's hills come out the
 * same shape for a given seed. Every instance is immutable after construction, so it can be shared between worldgen
 * threads.
 */
final class LegacyNoise {
	private LegacyNoise() {
	}

	private static double lerp(double t, double a, double b) {
		return a + t * (b - a);
	}

	private static double fade(double t) {
		return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
	}

	/** NoiseGeneratorImproved: Ken Perlin's improved noise with 1.7.10's array fill. */
	static final class Improved {
		private static final double[] GX = {1, -1, 1, -1, 1, -1, 1, -1, 0, 0, 0, 0, 1, 0, -1, 0};
		private static final double[] GY = {1, 1, -1, -1, 0, 0, 0, 0, 1, -1, 1, -1, 1, -1, 1, -1};
		private static final double[] GZ = {0, 0, 0, 0, 1, 1, -1, -1, 1, 1, -1, -1, 0, 1, 0, -1};
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

		private static double grad(int hash, double x, double y, double z) {
			int h = hash & 15;
			return GX[h] * x + GY[h] * y + GZ[h] * z;
		}

		private static double grad2(int hash, double x, double z) {
			int h = hash & 15;
			return GX[h] * x + GZ[h] * z;
		}

		private static int floor(double d) {
			int i = (int) d;
			return d < i ? i - 1 : i;
		}

		/** populateNoiseArray: adds this octave to {@code out}, indexed (x * zSize + z) * ySize + y. */
		void populate(double[] out, double xOff, double yOff, double zOff, int xSize, int ySize, int zSize, double xScale, double yScale,
				double zScale, double noiseScale) {
			double amp = 1.0 / noiseScale;
			int idx = 0;
			if (ySize == 1) {
				for (int ix = 0; ix < xSize; ix++) {
					double x = xOff + ix * xScale + this.xo;
					int xf = floor(x);
					int xi = xf & 255;
					x -= xf;
					double u = fade(x);
					for (int iz = 0; iz < zSize; iz++) {
						double z = zOff + iz * zScale + this.zo;
						int zf = floor(z);
						int zi = zf & 255;
						z -= zf;
						double w = fade(z);
						int a = this.p[xi];
						int aa = this.p[a] + zi;
						int b = this.p[xi + 1];
						int ba = this.p[b] + zi;
						double r1 = lerp(u, grad2(this.p[aa], x, z), grad(this.p[ba], x - 1.0, 0.0, z));
						double r2 = lerp(u, grad(this.p[aa + 1], x, 0.0, z - 1.0), grad(this.p[ba + 1], x - 1.0, 0.0, z - 1.0));
						out[idx++] += lerp(w, r1, r2) * amp;
					}
				}
				return;
			}
			int lastY = -1;
			double c1 = 0.0;
			double c2 = 0.0;
			double c3 = 0.0;
			double c4 = 0.0;
			for (int ix = 0; ix < xSize; ix++) {
				double x = xOff + ix * xScale + this.xo;
				int xf = floor(x);
				int xi = xf & 255;
				x -= xf;
				double u = fade(x);
				for (int iz = 0; iz < zSize; iz++) {
					double z = zOff + iz * zScale + this.zo;
					int zf = floor(z);
					int zi = zf & 255;
					z -= zf;
					double w = fade(z);
					for (int iy = 0; iy < ySize; iy++) {
						double y = yOff + iy * yScale + this.yo;
						int yf = floor(y);
						int yi = yf & 255;
						y -= yf;
						double v = fade(y);
						// 1.7.10 reuses the corner gradients while the y cell stays the same.
						if (iy == 0 || yi != lastY) {
							lastY = yi;
							int a = this.p[xi] + yi;
							int aa = this.p[a] + zi;
							int ab = this.p[a + 1] + zi;
							int b = this.p[xi + 1] + yi;
							int ba = this.p[b] + zi;
							int bb = this.p[b + 1] + zi;
							c1 = lerp(u, grad(this.p[aa], x, y, z), grad(this.p[ba], x - 1.0, y, z));
							c2 = lerp(u, grad(this.p[ab], x, y - 1.0, z), grad(this.p[bb], x - 1.0, y - 1.0, z));
							c3 = lerp(u, grad(this.p[aa + 1], x, y, z - 1.0), grad(this.p[ba + 1], x - 1.0, y, z - 1.0));
							c4 = lerp(u, grad(this.p[ab + 1], x, y - 1.0, z - 1.0), grad(this.p[bb + 1], x - 1.0, y - 1.0, z - 1.0));
						}
						double r1 = lerp(v, c1, c2);
						double r2 = lerp(v, c3, c4);
						out[idx++] += lerp(w, r1, r2) * amp;
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

		double[] generate(int x, int y, int z, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale) {
			double[] out = new double[xSize * ySize * zSize];
			double d = 1.0;
			for (Improved octave : this.octaves) {
				double dx = x * d * xScale;
				double dy = y * d * yScale;
				double dz = z * d * zScale;
				long lx = (long) Math.floor(dx);
				long lz = (long) Math.floor(dz);
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

		/** The 2D form (y fixed at 10, as 1.7.10 does). */
		double[] generate2D(int x, int z, int xSize, int zSize, double xScale, double zScale) {
			return this.generate(x, 10, z, xSize, 1, zSize, xScale, 1.0, zScale);
		}
	}

	/** NoiseGeneratorSimplex (2D only, as 1.7.10 uses it). */
	static final class Simplex {
		private static final int[][] GRAD3 = {{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1},
			{0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};
		private static final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
		private static final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;
		private final int[] p = new int[512];
		private final double xo;
		private final double yo;

		Simplex(Random random) {
			this.xo = random.nextDouble() * 256.0;
			this.yo = random.nextDouble() * 256.0;
			random.nextDouble();
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

		private static int fastFloor(double d) {
			return d > 0.0 ? (int) d : (int) d - 1;
		}

		private static double dot(int[] g, double x, double y) {
			return g[0] * x + g[1] * y;
		}

		/** func_151606_a: adds amplitude * noise to {@code out}, indexed z * xSize + x. */
		void add(double[] out, double x, double z, int xSize, int zSize, double xScale, double zScale, double amplitude) {
			int idx = 0;
			for (int iz = 0; iz < zSize; iz++) {
				double yin = (z + iz) * zScale + this.yo;
				for (int ix = 0; ix < xSize; ix++) {
					double xin = (x + ix) * xScale + this.xo;
					double s = (xin + yin) * F2;
					int i = fastFloor(xin + s);
					int j = fastFloor(yin + s);
					double t = (i + j) * G2;
					double x0 = xin - (i - t);
					double y0 = yin - (j - t);
					int i1;
					int j1;
					if (x0 > y0) {
						i1 = 1;
						j1 = 0;
					} else {
						i1 = 0;
						j1 = 1;
					}
					double x1 = x0 - i1 + G2;
					double y1 = y0 - j1 + G2;
					double x2 = x0 - 1.0 + 2.0 * G2;
					double y2 = y0 - 1.0 + 2.0 * G2;
					int ii = i & 255;
					int jj = j & 255;
					int gi0 = this.p[ii + this.p[jj]] % 12;
					int gi1 = this.p[ii + i1 + this.p[jj + j1]] % 12;
					int gi2 = this.p[ii + 1 + this.p[jj + 1]] % 12;
					double n0 = 0.0;
					double t0 = 0.5 - x0 * x0 - y0 * y0;
					if (t0 >= 0.0) {
						t0 *= t0;
						n0 = t0 * t0 * dot(GRAD3[gi0], x0, y0);
					}
					double n1 = 0.0;
					double t1 = 0.5 - x1 * x1 - y1 * y1;
					if (t1 >= 0.0) {
						t1 *= t1;
						n1 = t1 * t1 * dot(GRAD3[gi1], x1, y1);
					}
					double n2 = 0.0;
					double t2 = 0.5 - x2 * x2 - y2 * y2;
					if (t2 >= 0.0) {
						t2 *= t2;
						n2 = t2 * t2 * dot(GRAD3[gi2], x2, y2);
					}
					out[idx++] += 70.0 * (n0 + n1 + n2) * amplitude;
				}
			}
		}
	}

	/** NoiseGeneratorPerlin: layered simplex noise (Utopia's dirt-depth noise). */
	static final class Perlin {
		private final Simplex[] levels;

		Perlin(Random random, int count) {
			this.levels = new Simplex[count];
			for (int i = 0; i < count; i++) {
				this.levels[i] = new Simplex(random);
			}
		}

		/** func_151599_a with 1.7.10's (p, 0.5) falloff. */
		double[] generate(double x, double z, int xSize, int zSize, double xScale, double zScale, double p) {
			double[] out = new double[xSize * zSize];
			double d7 = 1.0;
			double d6 = 1.0;
			for (Simplex level : this.levels) {
				level.add(out, x, z, xSize, zSize, xScale * d6 * d7, zScale * d6 * d7, 0.55 / d7);
				d6 *= p;
				d7 *= 0.5;
			}
			return out;
		}
	}
}
