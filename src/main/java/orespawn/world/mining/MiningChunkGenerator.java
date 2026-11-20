package orespawn.world.mining;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
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
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OreSpawn's Mining dimension ("Dimension-Extreme", DimensionID2): {@code ChunkProviderOreSpawn2} as a 26.3 chunk
 * generator ({@code orespawn:mining}). Terrain, surface, caves, ravines and OreSpawn's chunk ores are
 * {@link MiningTerrain}, 1.7.10's code on 1.7.10's block array, so a seed gives the hills it gave in 1.7.10: Extreme
 * Hills everywhere, y 0-255, sea level 63. Decoration is populate's lakes, monster rooms and 1.7.10 ore veins
 * ({@link MiningPopulator}), then the {@code orespawn:mining} biome's features and 26.3's mineshafts and strongholds,
 * then the decorator's springs and Extreme Hills' emeralds and silverfish stone, then OreSpawnWorld's Mining branch
 * ({@link MiningWorld}).
 * <p>
 * Natural spawns are getPossibleCreatures: the biome's own (1.7.10 Extreme Hills' vanilla mobs, in the biome file), what
 * OreSpawn's creature groups add to Extreme Hills (read from 26.3's windswept_hills, the biome they add it to), and for
 * monsters and ambient creatures the dimension's own list ({@link MiningSpawns}).
 */
public final class MiningChunkGenerator extends ChunkGenerator {
	public static final MapCodec<MiningChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource)
	).apply(i, i.stable(MiningChunkGenerator::new)));

	private volatile @Nullable MiningTerrain terrain;
	private final Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawns = new ConcurrentHashMap<>();

	public MiningChunkGenerator(BiomeSource biomeSource) {
		super(biomeSource);
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	/** The terrain for a world seed (one per generator; a new seed replaces it). */
	public MiningTerrain terrain(long seed) {
		MiningTerrain t = this.terrain;
		if (t == null || t.seed() != seed) {
			synchronized (this) {
				t = this.terrain;
				if (t == null || t.seed() != seed) {
					t = new MiningTerrain(seed);
					this.terrain = t;
				}
			}
		}
		return t;
	}

	@SuppressWarnings("deprecation")
	private MiningTerrain terrain(RandomState randomState) {
		return this.terrain(randomState.seed());
	}

	@Override
	public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk, Blender blender, RandomState randomState, StructureManager structureManager,
			BiomeManager biomeManager, @Nullable WorldGenRegion carverBiomeRegion, Set<Holder<Biome>> possibleBiomes) {
		return CompletableFuture.supplyAsync(() -> {
			this.fill(chunk, this.terrain(randomState));
			return chunk;
		}, Util.backgroundExecutor().forName("buildTerrain"));
	}

	private void fill(ChunkAccess chunk, MiningTerrain terrain) {
		ChunkPos pos = chunk.getPos();
		BlockState[] blocks = terrain.chunk(pos.x(), pos.z());
		Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		int minY = Math.max(chunk.getMinY(), 0);
		int maxY = Math.min(chunk.getMaxY(), MiningTerrain.HEIGHT - 1);
		for (int y = maxY; y >= minY; y--) {
			LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(y));
			section.acquire();
			try {
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						BlockState state = blocks[x << 12 | z << 8 | y];
						if (state != null) {
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

	@Override
	public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
		ChunkPos pos = chunk.getPos();
		RandomSource rand = MiningPopulator.random(level.getSeed(), pos.x(), pos.z());
		MiningPopulator.beforeFeatures(level, this, rand, pos.x(), pos.z());
		super.applyBiomeDecoration(level, chunk, structureManager);
		MiningPopulator.afterFeatures(level, rand, pos.x(), pos.z());
		MiningWorld.generate(level, this, pos.x(), pos.z());
	}

	@Override
	public WeightedList<MobSpawnSettings.SpawnerData> getMobsAt(Level level, StructureManager structureManager, MobCategory category, BlockPos pos) {
		WeightedList<MobSpawnSettings.SpawnerData> base = super.getMobsAt(level, structureManager, category, pos);
		WeightedList<MobSpawnSettings.SpawnerData> biome = level.environmentAttributes().getValue(EnvironmentAttributes.NATURAL_MOB_SPAWNS, pos)
			.getMobsToSpawn(category);
		if (!base.equals(biome)) {
			// A structure's own spawns (none of 26.3's mineshafts or strongholds has them, but other mods' might).
			return base;
		}
		return this.spawns.computeIfAbsent(category, c -> MiningSpawns.build(level.registryAccess().lookupOrThrow(Registries.BIOME), c, biome));
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
		return MiningTerrain.HEIGHT;
	}

	@Override
	public int getSeaLevel() {
		return MiningTerrain.SEA_LEVEL;
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
		BlockState[] column = this.terrain(randomState).column(x, z);
		for (int y = Math.min(MiningTerrain.HEIGHT, heightAccessor.getMaxY() + 1) - 1; y >= Math.max(0, heightAccessor.getMinY()); y--) {
			BlockState state = column[y];
			if (state != null && type.isOpaque().test(state)) {
				return y + 1;
			}
		}
		return heightAccessor.getMinY();
	}

	@Override
	public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
		BlockState[] terrain = this.terrain(randomState).column(x, z);
		int minY = heightAccessor.getMinY();
		BlockState air = Blocks.AIR.defaultBlockState();
		BlockState[] column = new BlockState[heightAccessor.getHeight()];
		for (int i = 0; i < column.length; i++) {
			int y = minY + i;
			BlockState state = y >= 0 && y < MiningTerrain.HEIGHT ? terrain[y] : null;
			column[i] = state != null ? state : air;
		}
		return new NoiseColumn(minY, column);
	}

	@Override
	public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos, SamplerContext samplerContext) {
	}
}
