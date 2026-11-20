package orespawn.world.crystal;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import orespawn.OreSpawn;
import orespawn.content.Crystal;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * OreSpawn's Crystal dimension terrain (ChunkProviderOreSpawn5), as a 26.3 chunk generator ({@code orespawn:crystal}).
 * <p>
 * The ground is 1.7.10's overworld density noise (same octaves, scales and 4 x 8 x 4 interpolation) for one flat-ish
 * biome (OreSpawn's "Crystal": height 0.1, variation 0.5): Kyanite where the density is positive, water below y 63, then
 * OreSpawn's surface pass (Crystal Grass on top from y 59 up, Kyanite under water, bedrock at y 0-4). Then, chunk by
 * chunk with the chunk's own seeded random as OreSpawn did: the bedrock maze at y 24-28, Pink Tourmaline and Tiger's
 * Eye veins, crystal trees, ore veins in the Kyanite (egg ores for the dimension's creatures, Crystal Energy, Crystalized
 * Rats and Fairies), crystal flowers, rice and quinoa. Like OreSpawn's, these only write inside their own chunk. The
 * world is 256 high (y 0-255), as OreSpawn's was.
 * <p>
 * Decoration ({@link CrystalDecoration}) is OreSpawnWorld's Crystal branch: fairy trees, termite nests, the dimension's
 * structures, Irukandji spawners, maze chests and spawners, and rocks.
 * <p>
 * Other groups' blocks are looked up by id: rice and quinoa ({@code orespawn:rice_plant}, {@code orespawn:quinoa_plant},
 * the Farming group) and the egg ores of the dimension's creatures; any not registered are skipped.
 */
public final class CrystalChunkGenerator extends ChunkGenerator {
	public static final MapCodec<CrystalChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
		i -> i.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource))
			.apply(i, i.stable(CrystalChunkGenerator::new)));

	static final int HEIGHT = 256;
	static final int SEA_LEVEL = 63;
	/** OreSpawn's "Crystal" biome: setHeight(new Height(0.1, 0.5)). */
	private static final float ROOT_HEIGHT = 0.1F;
	private static final float HEIGHT_VARIATION = 0.5F;
	private static final float[] PARABOLIC = new float[25];

	static {
		for (int j = -2; j <= 2; j++) {
			for (int k = -2; k <= 2; k++) PARABOLIC[j + 2 + (k + 2) * 5] = 10.0F / (float) Math.sqrt(j * j + k * k + 0.2F);
		}
	}

	private volatile @Nullable Noises noises;

	public CrystalChunkGenerator(BiomeSource biomeSource) {
		super(biomeSource);
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	/** ChunkProviderOreSpawn5's noise generators, made from the world seed in OreSpawn's order. */
	private static final class Noises {
		final long seed;
		final LegacyNoise.Octaves minLimit;
		final LegacyNoise.Octaves maxLimit;
		final LegacyNoise.Octaves main;
		final LegacyNoise.Perlin surface;
		final LegacyNoise.Octaves depth;

		Noises(long seed) {
			this.seed = seed;
			Random random = new Random(seed);
			this.minLimit = new LegacyNoise.Octaves(random, 16);
			this.maxLimit = new LegacyNoise.Octaves(random, 16);
			this.main = new LegacyNoise.Octaves(random, 8);
			this.surface = new LegacyNoise.Perlin(random, 4);
			new LegacyNoise.Octaves(random, 10); // noiseGen5, made but unused
			this.depth = new LegacyNoise.Octaves(random, 16);
			// mobSpawnerNoise (8 octaves) comes last and is unused too.
		}
	}

	private Noises noises(long seed) {
		Noises n = this.noises;
		if (n == null || n.seed != seed) {
			n = new Noises(seed);
			this.noises = n;
		}
		return n;
	}

	/** Index into a chunk's column-major block array: x, then z, then y (1.7.10's Block[65536] layout). */
	static int index(int x, int y, int z) {
		return x << 12 | z << 8 | y;
	}

	/** The random OreSpawn seeded for each chunk (independent of the world seed). */
	static Random chunkRandom(int chunkX, int chunkZ) {
		return new Random(chunkX * 341873128712L + chunkZ * 132897987541L);
	}

	// -----------------------------------------------------------------------------------------------------------------
	// Terrain (func_147424_a, func_147423_a, replaceBlocksForBiome)
	// -----------------------------------------------------------------------------------------------------------------

	/** func_147423_a: the 5 x 33 x 5 density field for a chunk (x outermost, then z, then y). */
	private static double[] density(Noises n, int x, int z) {
		double[] depth = n.depth.generate2D(x, z, 5, 5, 200.0, 200.0);
		double[] main = n.main.generate(x, 0, z, 5, 33, 5, 8.555150000000001, 4.277575000000001, 8.555150000000001);
		double[] min = n.minLimit.generate(x, 0, z, 5, 33, 5, 684.412, 684.412, 684.412);
		double[] max = n.maxLimit.generate(x, 0, z, 5, 33, 5, 684.412, 684.412, 684.412);
		double[] out = new double[825];
		int l = 0;
		int i1 = 0;
		for (int j1 = 0; j1 < 5; j1++) {
			for (int k1 = 0; k1 < 5; k1++) {
				// Every column has the same biome, so the smoothing over its neighbours gives the biome's own values.
				float f = 0.0F;
				float f1 = 0.0F;
				float f2 = 0.0F;
				for (int l1 = -2; l1 <= 2; l1++) {
					for (int i2 = -2; i2 <= 2; i2++) {
						float f5 = PARABOLIC[l1 + 2 + (i2 + 2) * 5] / (ROOT_HEIGHT + 2.0F);
						f += HEIGHT_VARIATION * f5;
						f1 += ROOT_HEIGHT * f5;
						f2 += f5;
					}
				}
				f /= f2;
				f1 /= f2;
				f = f * 0.9F + 0.1F;
				f1 = (f1 * 4.0F - 1.0F) / 8.0F;
				double d13 = depth[i1] / 8000.0;
				if (d13 < 0.0) d13 = -d13 * 0.3;
				d13 = d13 * 3.0 - 2.0;
				if (d13 < 0.0) {
					d13 /= 2.0;
					if (d13 < -1.0) d13 = -1.0;
					d13 /= 1.4;
					d13 /= 2.0;
				} else {
					if (d13 > 1.0) d13 = 1.0;
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
					if (d6 < 0.0) d6 *= 4.0;
					double d7 = min[l] / 512.0;
					double d8 = max[l] / 512.0;
					double d9 = (main[l] / 10.0 + 1.0) / 2.0;
					double d10 = (d9 < 0.0 ? d7 : d9 > 1.0 ? d8 : d7 + (d8 - d7) * d9) - d6;
					if (j2 > 29) {
						double d11 = (float) (j2 - 29) / 3.0F;
						d10 = d10 * (1.0 - d11) + -10.0 * d11;
					}
					out[l++] = d10;
				}
			}
		}
		return out;
	}

	/** The bare ground of a chunk: Kyanite, water below 63, air; then OreSpawn's surface pass with the chunk random. */
	static BlockState[] terrain(Noises n, int chunkX, int chunkZ, Random random) {
		BlockState air = Blocks.AIR.defaultBlockState();
		BlockState stone = Crystal.KYANITE.defaultBlockState();
		BlockState water = Blocks.WATER.defaultBlockState();
		BlockState[] blocks = new BlockState[65536];
		double[] q = density(n, chunkX * 4, chunkZ * 4);
		for (int k = 0; k < 4; k++) {
			for (int j1 = 0; j1 < 4; j1++) {
				int k1 = (k * 5 + j1) * 33;
				int l1 = (k * 5 + j1 + 1) * 33;
				int i2 = ((k + 1) * 5 + j1) * 33;
				int j2 = ((k + 1) * 5 + j1 + 1) * 33;
				for (int k2 = 0; k2 < 32; k2++) {
					double d1 = q[k1 + k2];
					double d2 = q[l1 + k2];
					double d3 = q[i2 + k2];
					double d4 = q[j2 + k2];
					double d5 = (q[k1 + k2 + 1] - d1) * 0.125;
					double d6 = (q[l1 + k2 + 1] - d2) * 0.125;
					double d7 = (q[i2 + k2 + 1] - d3) * 0.125;
					double d8 = (q[j2 + k2 + 1] - d4) * 0.125;
					for (int l2 = 0; l2 < 8; l2++) {
						double d10 = d1;
						double d11 = d2;
						double d12 = (d3 - d1) * 0.25;
						double d13 = (d4 - d2) * 0.25;
						for (int i3 = 0; i3 < 4; i3++) {
							double d16 = (d11 - d10) * 0.25;
							double d15 = d10 - d16;
							for (int k3 = 0; k3 < 4; k3++) {
								d15 += d16;
								int y = k2 * 8 + l2;
								BlockState s = d15 > 0.0 ? stone : y < SEA_LEVEL ? water : air;
								blocks[index(k * 4 + i3, y, j1 * 4 + k3)] = s;
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
		// replaceBlocksForBiome: the surface depth noise, then MygenBiomeTerrain for each column.
		double scale = 0.03125;
		double[] stoneNoise = n.surface.generate(chunkX * 16, chunkZ * 16, 16, 16, scale * 2.0, scale * 2.0, 1.0);
		for (int z = 0; z < 16; z++) {
			for (int x = 0; x < 16; x++) surface(blocks, random, x, z, stoneNoise[z * 16 + x]);
		}
		return blocks;
	}

	/** MygenBiomeTerrain for one column (bedrock at the bottom, Crystal Grass on top, Kyanite under water). */
	private static void surface(BlockState[] blocks, Random random, int x, int z, double noise) {
		Block grass = Crystal.CRYSTAL_GRASS;
		Block stone = Crystal.KYANITE;
		BlockState top = grass.defaultBlockState();
		BlockState filler = stone.defaultBlockState();
		int k = -1;
		int l = (int) (noise / 3.0 + 3.0 + random.nextDouble() * 0.25);
		for (int y = 255; y >= 0; y--) {
			int i = index(x, y, z);
			if (y <= random.nextInt(5)) {
				blocks[i] = Blocks.BEDROCK.defaultBlockState();
			} else {
				BlockState here = blocks[i];
				if (here.isAir()) {
					k = -1;
				} else if (here.is(stone)) {
					if (k == -1) {
						if (l <= 0) {
							top = null;
							filler = stone.defaultBlockState();
						} else if (y >= 59 && y <= 64) {
							top = grass.defaultBlockState();
							filler = stone.defaultBlockState();
						}
						if (y < SEA_LEVEL && (top == null || top.isAir())) top = Blocks.WATER.defaultBlockState();
						k = l;
						if (y >= 62) {
							blocks[i] = top == null ? Blocks.AIR.defaultBlockState() : top;
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
	}

	// -----------------------------------------------------------------------------------------------------------------
	// The chunk (func_73154_d)
	// -----------------------------------------------------------------------------------------------------------------

	/** Writes that stay inside one chunk's array, as OreSpawn's chunk helpers did. */
	private static final class ChunkVoxels implements Voxels {
		private final BlockState[] blocks;
		private final int minX;
		private final int minZ;

		ChunkVoxels(BlockState[] blocks, int chunkX, int chunkZ) {
			this.blocks = blocks;
			this.minX = chunkX << 4;
			this.minZ = chunkZ << 4;
		}

		private boolean inside(int x, int y, int z) {
			return x >> 4 == this.minX >> 4 && z >> 4 == this.minZ >> 4 && y >= 0 && y < HEIGHT;
		}

		@Override
		public BlockState state(int x, int y, int z) {
			return this.inside(x, y, z) ? this.blocks[index(x & 15, y, z & 15)] : Blocks.AIR.defaultBlockState();
		}

		@Override
		public void set(int x, int y, int z, BlockState state) {
			if (this.inside(x, y, z)) this.blocks[index(x & 15, y, z & 15)] = state;
		}
	}

	/** The whole chunk: terrain, then OreSpawn's in-chunk features in its order. */
	static BlockState[] chunk(Noises n, int chunkX, int chunkZ) {
		Random random = chunkRandom(chunkX, chunkZ);
		BlockState[] blocks = terrain(n, chunkX, chunkZ, random);
		ChunkVoxels w = new ChunkVoxels(blocks, chunkX, chunkZ);
		// OreSpawn shaped the maze and the trees with Math.random / world.rand; a second random seeded from the chunk keeps
		// them the same each time without changing the chunk random's sequence for everything else.
		Random shapes = new Random((chunkX * 341873128712L + chunkZ * 132897987541L) ^ 0x5DEECE66DL);
		int x = chunkX * 16;
		int z = chunkZ * 16;
		CrystalMaze.build(w, shapes, x, 25, z);
		CrystalFeatures.crystals(w, random, x, z);
		CrystalFeatures.trees(w, random, shapes, x, z);
		CrystalFeatures.ores(w, random, x, z);
		CrystalFeatures.flowers(w, random, x, z);
		CrystalFeatures.crop(w, random, x, z, 10, CrystalFeatures.rice());
		CrystalFeatures.crop(w, random, x, z, 20, CrystalFeatures.quinoa());
		return blocks;
	}

	@Override
	public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk, Blender blender, RandomState randomState, StructureManager structureManager,
			BiomeManager biomeManager, @Nullable WorldGenRegion carverBiomeRegion, Set<Holder<Biome>> possibleBiomes) {
		ChunkPos pos = chunk.getPos();
		BlockState[] blocks = chunk(this.noises(randomState.seed()), pos.x(), pos.z());
		int minY = chunk.getMinY();
		int maxY = Math.min(chunk.getMaxY(), HEIGHT - 1);
		for (int sectionY = minY >> 4; sectionY <= maxY >> 4; sectionY++) {
			LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(sectionY));
			section.acquire();
			try {
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						for (int dy = 0; dy < 16; dy++) {
							int y = (sectionY << 4) + dy;
							if (y < 0 || y >= HEIGHT) continue;
							BlockState state = blocks[index(x, y, z)];
							if (!state.isAir()) section.setBlockState(x, dy, z, state, false);
						}
					}
				}
			} finally {
				section.release();
			}
		}
		Heightmap.primeHeightmaps(chunk, EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG));
		return CompletableFuture.completedFuture(chunk);
	}

	@Override
	public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
		super.applyBiomeDecoration(level, chunk, structureManager);
		CrystalDecoration.decorate(level, this, chunk.getPos());
	}

	/** SpawnerAnimals.performWorldGenSpawning, as the chunk provider's populate did it. */
	@Override
	public void spawnOriginalMobs(WorldGenRegion region) {
		ChunkPos center = region.getCenter();
		BlockPos sourcePos = center.getWorldPosition().atY(region.getMaxY());
		WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
		random.setDecorationSeed(region.getSeed(), center.getMinBlockX(), center.getMinBlockZ());
		NaturalSpawner.spawnMobsForChunkGeneration(region, sourcePos, center, random);
	}

	// -----------------------------------------------------------------------------------------------------------------
	// Heights for spawn points and the like: the bare terrain of the column's chunk.
	// -----------------------------------------------------------------------------------------------------------------

	private BlockState[] column(int x, int z, RandomState randomState) {
		int chunkX = Mth.floor(x / 16.0);
		int chunkZ = Mth.floor(z / 16.0);
		BlockState[] blocks = terrain(this.noises(randomState.seed()), chunkX, chunkZ, chunkRandom(chunkX, chunkZ));
		BlockState[] column = new BlockState[HEIGHT];
		for (int y = 0; y < HEIGHT; y++) column[y] = blocks[index(x & 15, y, z & 15)];
		return column;
	}

	@Override
	public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
		BlockState[] column = this.column(x, z, randomState);
		for (int y = Math.min(HEIGHT, heightAccessor.getMaxY() + 1) - 1; y >= Math.max(0, heightAccessor.getMinY()); y--) {
			if (type.isOpaque().test(column[y])) return y + 1;
		}
		return heightAccessor.getMinY();
	}

	@Override
	public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
		BlockState[] column = this.column(x, z, randomState);
		BlockState[] states = new BlockState[heightAccessor.getHeight()];
		for (int i = 0; i < states.length; i++) {
			int y = heightAccessor.getMinY() + i;
			states[i] = y >= 0 && y < HEIGHT ? column[y] : Blocks.AIR.defaultBlockState();
		}
		return new NoiseColumn(heightAccessor.getMinY(), states);
	}

	@Override
	public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos, SamplerContext samplerContext) {
	}

	@Override
	public int getGenDepth() {
		return HEIGHT;
	}

	@Override
	public int getSeaLevel() {
		return SEA_LEVEL;
	}

	@Override
	public int getMinY() {
		return 0;
	}

	/** Registers the generator type ({@code orespawn:crystal}). */
	public static void register() {
		net.minecraft.core.Registry.register(BuiltInRegistries.CHUNK_GENERATOR, OreSpawn.id("crystal"), CODEC);
	}
}
