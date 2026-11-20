package orespawn.world.utopia;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Utopia's chunk generator: OreSpawn's {@code ChunkProviderOreSpawn} (DimensionID, "Dimension-Utopia"). The terrain and
 * its grass/dirt surface come from {@link UtopiaTerrain}; then the biome's carvers (OreSpawn's MapGenCaves and
 * MapGenRavine, as 26.3 carvers tuned to 1.7.10's numbers in {@code worldgen/carver}) cut caves and ravines, which fill
 * with lava below y 10 and air above, never break into water, and turn the dirt they uncover under grass back into
 * grass, as 1.7.10's carvers did. Then OreSpawn's ChunkOreGenerator ({@link UtopiaOres}) with the chunk's own random.
 * {@code spawnOriginalMobs} is ChunkProviderOreSpawn.populate's SpawnerAnimals.performWorldGenSpawning. The world is
 * y 0 to 255 with the sea at 63, as in 1.7.10.
 * <p>
 * No lakes, vanilla dungeons, villages, mineshafts or strongholds: ChunkProviderOreSpawn never made any (its MapGen
 * structures are created but never called). The biome decorator and OreSpawnWorld's Utopia branch are the biome's
 * features and the {@code orespawn:utopia_big} structure.
 */
public final class UtopiaChunkGenerator extends ChunkGenerator {
	public static final MapCodec<UtopiaChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource)
	).apply(i, i.stable(UtopiaChunkGenerator::new)));
	private static final int LAVA_LEVEL = 10;
	private volatile @Nullable UtopiaTerrain terrain;

	public UtopiaChunkGenerator(BiomeSource biomeSource) {
		super(biomeSource);
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	/** The terrain for a world seed (one per generator; a new seed replaces it). */
	public UtopiaTerrain terrain(long seed) {
		UtopiaTerrain t = this.terrain;
		if (t == null || t.seed() != seed) {
			synchronized (this) {
				t = this.terrain;
				if (t == null || t.seed() != seed) {
					t = new UtopiaTerrain(seed);
					this.terrain = t;
				}
			}
		}
		return t;
	}

	@SuppressWarnings("deprecation")
	public UtopiaTerrain terrain(RandomState randomState) {
		return this.terrain(randomState.seed());
	}

	@Override
	public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk, Blender blender, RandomState randomState, StructureManager structureManager,
			BiomeManager biomeManager, @Nullable WorldGenRegion carverBiomeRegion, Set<Holder<Biome>> possibleBiomes) {
		return CompletableFuture.supplyAsync(() -> {
			this.fill(chunk, this.terrain(randomState));
			this.carve(chunk, randomState);
			UtopiaOres.generate(UtopiaTerrain.oreRandom(chunk.getPos().x(), chunk.getPos().z()), chunk);
			return chunk;
		}, Util.backgroundExecutor().forName("buildTerrain"));
	}

	private void fill(ChunkAccess chunk, UtopiaTerrain terrain) {
		ChunkPos pos = chunk.getPos();
		byte[] blocks = terrain.chunk(pos.x(), pos.z());
		Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		int minY = Math.max(chunk.getMinY(), 0);
		int maxY = Math.min(chunk.getMaxY(), UtopiaTerrain.HEIGHT - 1);
		for (int y = maxY; y >= minY; y--) {
			LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(y));
			section.acquire();
			try {
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						byte type = blocks[x << 12 | z << 8 | y];
						if (type != UtopiaTerrain.AIR) {
							BlockState state = UtopiaTerrain.state(type);
							section.setBlockState(x, SectionPos.sectionRelative(y), z, state, false);
							oceanFloor.update(x, y, z, state);
							worldSurface.update(x, y, z, state);
						}
					}
				}
			} finally {
				section.release();
			}
		}
	}

	/** MapGenCaves / MapGenRavine: the biome's carvers mark what to dig; 1.7.10's rules decide what goes there. */
	@SuppressWarnings("deprecation")
	private void carve(ChunkAccess chunk, RandomState randomState) {
		ChunkPos pos = chunk.getPos();
		WorldGenerationContext context = new WorldGenerationContext(this, chunk.getHeightAccessorForGeneration());
		CarvingMask mask = new CarvingMask(context.getMinGenY() + 1, context.getMinGenY() + context.getGenDepth() - 1);
		WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
		Holder<Biome> biome = this.biomeSource.possibleBiomes().iterator().next();
		Iterable<Holder<WorldCarver>> carvers = this.getBiomeGenerationSettings(biome).getCarvers();
		for (int dx = -8; dx <= 8; dx++) {
			for (int dz = -8; dz <= 8; dz++) {
				ChunkPos source = new ChunkPos(pos.x() + dx, pos.z() + dz);
				int index = 0;
				for (Holder<WorldCarver> holder : carvers) {
					WorldCarver carver = holder.value();
					random.setLargeFeatureSeed(randomState.seed() + index, source.x(), source.z());
					if (carver.isStartChunk(random)) {
						carver.carve(context, random, pos, source, mask);
					}
					index++;
				}
			}
		}
		if (mask.isEmpty()) {
			return;
		}
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos q = new BlockPos.MutableBlockPos();
		BlockState air = Blocks.CAVE_AIR.defaultBlockState();
		BlockState lava = Blocks.LAVA.defaultBlockState();
		mask.visit((x, z, bottomY, topY) -> {
			int wx = pos.getBlockX(x);
			int wz = pos.getBlockZ(z);
			boolean grass = false;
			for (int y = topY; y >= bottomY; y--) {
				p.set(wx, y, wz);
				BlockState state = chunk.getBlockState(p);
				// digBlock only cuts stone and the biome's top/filler blocks, and isOceanBlock keeps water in.
				if (!state.is(Blocks.STONE) && !state.is(Blocks.DIRT) && !state.is(Blocks.GRASS_BLOCK) || this.touchesWater(chunk, p, q)) {
					continue;
				}
				if (state.is(Blocks.GRASS_BLOCK)) {
					grass = true;
				}
				if (y < LAVA_LEVEL) {
					chunk.setBlockState(p, lava);
					chunk.markPosForPostProcessing(p);
				} else {
					chunk.setBlockState(p, air);
					q.set(wx, y - 1, wz);
					if (grass && chunk.getBlockState(q).is(Blocks.DIRT)) {
						chunk.setBlockState(q, Blocks.GRASS_BLOCK.defaultBlockState());
					}
				}
			}
		});
	}

	private boolean touchesWater(ChunkAccess chunk, BlockPos pos, BlockPos.MutableBlockPos q) {
		for (Direction d : Direction.values()) {
			q.setWithOffset(pos, d);
			if ((q.getX() >> 4) != (pos.getX() >> 4) || (q.getZ() >> 4) != (pos.getZ() >> 4)) {
				continue;
			}
			if (chunk.getFluidState(q).is(net.minecraft.tags.FluidTags.WATER)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void spawnOriginalMobs(WorldGenRegion region) {
		ChunkPos center = region.getCenter();
		BlockPos source = center.getWorldPosition().atY(region.getMaxY());
		WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
		random.setDecorationSeed(region.getSeed(), center.getMinBlockX(), center.getMinBlockZ());
		NaturalSpawner.spawnMobsForChunkGeneration(region, source, center, random);
	}

	@Override
	public int getGenDepth() {
		return UtopiaTerrain.HEIGHT;
	}

	@Override
	public int getSeaLevel() {
		return UtopiaTerrain.SEA_LEVEL;
	}

	@Override
	public int getMinY() {
		return 0;
	}

	@Override
	public int getSpawnHeight(LevelHeightAccessor heightAccessor) {
		return 64;
	}

	@Override
	public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
		UtopiaTerrain terrain = this.terrain(randomState);
		for (int y = UtopiaTerrain.HEIGHT - 1; y >= 0; y--) {
			if (type.isOpaque().test(UtopiaTerrain.state(terrain.typeAt(x, y, z)))) {
				return y + 1;
			}
		}
		return heightAccessor.getMinY();
	}

	@Override
	public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
		UtopiaTerrain terrain = this.terrain(randomState);
		int minY = heightAccessor.getMinY();
		BlockState[] column = new BlockState[heightAccessor.getHeight()];
		for (int i = 0; i < column.length; i++) {
			column[i] = UtopiaTerrain.state(terrain.typeAt(x, minY + i, z));
		}
		return new NoiseColumn(minY, column);
	}

	@Override
	public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos, SamplerContext samplerContext) {
	}
}
