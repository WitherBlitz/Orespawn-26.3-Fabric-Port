package orespawn.world.chaos;

import java.util.Random;

/**
 * Minecraft 1.7.10's {@code NoiseGeneratorOctaves} with its {@code NoiseGeneratorImproved} octaves, line for line, so
 * the Chaos terrain has the same shapes as in OreSpawn (26.3's own Perlin noise is seeded and sampled differently).
 * Immutable once made: {@link #generate} only reads the permutation tables, so chunk threads can share one.
 */
final class ChaosNoise {
	private final Octave[] octaves;

	ChaosNoise(Random random, int count) {
		this.octaves = new Octave[count];
		for (int i = 0; i < count; i++) {
			this.octaves[i] = new Octave(random);
		}
	}

	/** NoiseGeneratorOctaves.generateNoiseOctaves (func_76304_a): x outermost, then z, then y. */
	double[] generate(double[] out, int x, int y, int z, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale) {
		if (out == null) {
			out = new double[xSize * ySize * zSize];
		} else {
			java.util.Arrays.fill(out, 0.0);
		}
		double amplitude = 1.0;
		for (Octave octave : this.octaves) {
			double dx = x * amplitude * xScale;
			double dy = y * amplitude * yScale;
			double dz = z * amplitude * zScale;
			long lx = floorLong(dx);
			long lz = floorLong(dz);
			dx -= lx;
			dz -= lz;
			lx %= 16777216L;
			lz %= 16777216L;
			dx += lx;
			dz += lz;
			octave.populate(out, dx, dy, dz, xSize, ySize, zSize, xScale * amplitude, yScale * amplitude, zScale * amplitude, amplitude);
			amplitude /= 2.0;
		}
		return out;
	}

	private static long floorLong(double d) {
		long l = (long) d;
		return d < l ? l - 1L : l;
	}

	/** NoiseGeneratorImproved. */
	private static final class Octave {
		private static final double[] GX = {1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, -1.0, 0.0};
		private static final double[] GY = {1.0, 1.0, -1.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0};
		private static final double[] GZ = {0.0, 0.0, 0.0, 0.0, 1.0, 1.0, -1.0, -1.0, 1.0, 1.0, -1.0, -1.0, 0.0, 1.0, 0.0, -1.0};

		private final int[] perm = new int[512];
		private final double xCoord;
		private final double yCoord;
		private final double zCoord;

		Octave(Random random) {
			this.xCoord = random.nextDouble() * 256.0;
			this.yCoord = random.nextDouble() * 256.0;
			this.zCoord = random.nextDouble() * 256.0;
			for (int i = 0; i < 256; i++) {
				this.perm[i] = i;
			}
			for (int i = 0; i < 256; i++) {
				int j = random.nextInt(256 - i) + i;
				int k = this.perm[i];
				this.perm[i] = this.perm[j];
				this.perm[j] = k;
				this.perm[i + 256] = this.perm[i];
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

		private static double fade(double t) {
			return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
		}

		void populate(double[] out, double x0, double y0, double z0, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale,
				double amplitude) {
			int[] p = this.perm;
			double inv = 1.0 / amplitude;
			if (ySize == 1) {
				int index = 0;
				for (int ix = 0; ix < xSize; ix++) {
					double x = x0 + ix * xScale + this.xCoord;
					int xi = (int) x;
					if (x < xi) {
						xi--;
					}
					int xb = xi & 255;
					x -= xi;
					double u = fade(x);
					for (int iz = 0; iz < zSize; iz++) {
						double z = z0 + iz * zScale + this.zCoord;
						int zi = (int) z;
						if (z < zi) {
							zi--;
						}
						int zb = zi & 255;
						z -= zi;
						double w = fade(z);
						int a = p[xb];
						int aa = p[a] + zb;
						int b = p[xb + 1];
						int ba = p[b] + zb;
						double l1 = lerp(u, grad2(p[aa], x, z), grad(p[ba], x - 1.0, 0.0, z));
						double l2 = lerp(u, grad(p[aa + 1], x, 0.0, z - 1.0), grad(p[ba + 1], x - 1.0, 0.0, z - 1.0));
						out[index++] += lerp(w, l1, l2) * inv;
					}
				}
				return;
			}
			int index = 0;
			int lastY = -1;
			double d1 = 0.0;
			double d2 = 0.0;
			double d3 = 0.0;
			double d4 = 0.0;
			for (int ix = 0; ix < xSize; ix++) {
				double x = x0 + ix * xScale + this.xCoord;
				int xi = (int) x;
				if (x < xi) {
					xi--;
				}
				int xb = xi & 255;
				x -= xi;
				double u = fade(x);
				for (int iz = 0; iz < zSize; iz++) {
					double z = z0 + iz * zScale + this.zCoord;
					int zi = (int) z;
					if (z < zi) {
						zi--;
					}
					int zb = zi & 255;
					z -= zi;
					double w = fade(z);
					for (int iy = 0; iy < ySize; iy++) {
						double y = y0 + iy * yScale + this.yCoord;
						int yi = (int) y;
						if (y < yi) {
							yi--;
						}
						int yb = yi & 255;
						y -= yi;
						double v = fade(y);
						if (iy == 0 || yb != lastY) {
							lastY = yb;
							int a = p[xb] + yb;
							int aa = p[a] + zb;
							int ab = p[a + 1] + zb;
							int b = p[xb + 1] + yb;
							int ba = p[b] + zb;
							int bb = p[b + 1] + zb;
							d1 = lerp(u, grad(p[aa], x, y, z), grad(p[ba], x - 1.0, y, z));
							d2 = lerp(u, grad(p[ab], x, y - 1.0, z), grad(p[bb], x - 1.0, y - 1.0, z));
							d3 = lerp(u, grad(p[aa + 1], x, y, z - 1.0), grad(p[ba + 1], x - 1.0, y, z - 1.0));
							d4 = lerp(u, grad(p[ab + 1], x, y - 1.0, z - 1.0), grad(p[bb + 1], x - 1.0, y - 1.0, z - 1.0));
						}
						double e1 = lerp(v, d1, d2);
						double e2 = lerp(v, d3, d4);
						out[index++] += lerp(w, e1, e2) * inv;
					}
				}
			}
		}
	}
}
