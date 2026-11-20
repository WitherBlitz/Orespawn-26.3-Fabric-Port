package orespawn.world.chaos;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Chunk generator {@code orespawn:chaos}: OreSpawn's ChunkProviderOreSpawn6, the Chaos dimension (DimensionID6,
 * "Dimension-Chaos"). Each new chunk gets, in OreSpawn's order:
 * <ol>
 * <li>the terrain and its grass ({@link ChaosTerrain}): an inside-out Nether of floating, holed stone between about y 25
 * and 105 over the void;</li>
 * <li>OreSpawn's ores ({@link ChaosOres}: its egg ores, uranium, titanium, amethyst, salt, the ant troll blocks, extra
 * diamond, emerald and gold ore and the gem blocks);</li>
 * <li>its scraggly apple trees ({@link ChaosTrees}).</li>
 * </ol>
 * The rest of ChunkProviderOreSpawn6.populate is the {@code orespawn:chaos} biome's features (its 1.7.10 biome
 * decorator, then OreSpawnWorld's Chaos branch) and {@link #spawnOriginalMobs} (SpawnerAnimals.performWorldGenSpawning).
 * Natural spawning uses {@link #getMobsAt}, OreSpawn's Chaos lists ({@link ChaosSpawns}). No caves, ravines, lakes,
 * dungeons or structures: OreSpawn's Chaos made none. The world is y 0-255; the sea level (63) is only nominal.
 * <p>
 * Change from 1.7.10: the ores and trees used OreSpawn's shared world random (and the provider's own random for the tree
 * shapes), so they depended on the order chunks were made in; here each chunk seeds its own from the world seed and its
 * position, so a chunk always comes out the same.
 */
public final class ChaosChunkGenerator extends ChunkGenerator {
	public static final MapCodec<ChaosChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource)
	).apply(i, i.stable(ChaosChunkGenerator::new)));
	/** Biome.getSpawningChance in 1.7.10: the chance of each further creature group when a chunk is made. */
	private static final float WORLD_GEN_SPAWN_CHANCE = 0.1F;
	private static final int SEA_LEVEL = 63;
	private static final int HEIGHT = 256;

	private volatile @Nullable ChaosTerrain terrain;

	public ChaosChunkGenerator(BiomeSource biomeSource) {
		super(biomeSource);
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	/** The terrain for the world seed (made once per generator; a different seed replaces it). */
	@SuppressWarnings("deprecation")
	private ChaosTerrain terrain(RandomState randomState) {
		long seed = randomState.seed();
		ChaosTerrain t = this.terrain;
		if (t == null || t.seed() != seed) {
			synchronized (this) {
				t = this.terrain;
				if (t == null || t.seed() != seed) {
					t = new ChaosTerrain(seed);
					this.terrain = t;
				}
			}
		}
		return t;
	}

	private static BlockState state(byte code) {
		return switch (code) {
			case ChaosTerrain.STONE -> Blocks.STONE.defaultBlockState();
			case ChaosTerrain.GRASS -> Blocks.GRASS_BLOCK.defaultBlockState();
			case ChaosTerrain.DIRT -> Blocks.DIRT.defaultBlockState();
			default -> Blocks.AIR.defaultBlockState();
		};
	}

	@Override
	public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk, Blender blender, RandomState randomState, StructureManager structureManager,
			BiomeManager biomeManager, @Nullable WorldGenRegion carverBiomeRegion, Set<Holder<Biome>> possibleBiomes) {
		ChaosTerrain terrain = this.terrain(randomState);
		return CompletableFuture.supplyAsync(() -> {
			this.fill(chunk, terrain);
			return chunk;
		}, Util.backgroundExecutor().forName("buildTerrain"));
	}

	private void fill(ChunkAccess chunk, ChaosTerrain terrain) {
		int chunkX = chunk.getPos().x();
		int chunkZ = chunk.getPos().z();
		byte[] codes = terrain.chunk(chunkX, chunkZ);
		BlockState[] states = new BlockState[codes.length];
		for (int i = 0; i < codes.length; i++) {
			states[i] = codes[i] == ChaosTerrain.AIR ? null : state(codes[i]);
		}
		long chunkSeed = terrain.seed() + chunkX * 0x4F9939F508L + chunkZ * 0x1EF1565BD5L;
		Random worldRand = new Random(chunkSeed);
		Random providerRand = new Random(chunkSeed ^ 0x5DEECE66DL);
		ChaosOres.generate(states, worldRand, chunkX, chunkZ);

		Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		int minY = Math.max(chunk.getMinY(), 0);
		int maxY = Math.min(chunk.getMaxY(), ChaosTerrain.HEIGHT - 1);
		for (int y = maxY; y >= minY; y--) {
			LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(y));
			section.acquire();
			try {
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						BlockState state = states[ChaosTerrain.index(x, y, z)];
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
		ChaosTrees.add(chunk, worldRand, providerRand);
	}

	@Override
	public WeightedList<MobSpawnSettings.SpawnerData> getMobsAt(Level level, StructureManager structureManager, MobCategory mobCategory, BlockPos pos) {
		return ChaosSpawns.get(mobCategory);
	}

	/**
	 * SpawnerAnimals.performWorldGenSpawning from OreSpawn's populate: while a 1 in 10 roll succeeds, a group from the
	 * creature list, placed as 26.3's own chunk-generation spawning places them.
	 */
	@Override
	@SuppressWarnings("deprecation")
	public void spawnOriginalMobs(WorldGenRegion region) {
		if (!region.getLevel().getGameRules().get(GameRules.SPAWN_MOBS)) {
			return;
		}
		WeightedList<MobSpawnSettings.SpawnerData> mobs = ChaosSpawns.get(MobCategory.CREATURE);
		if (mobs.isEmpty()) {
			return;
		}
		ChunkPos center = region.getCenter();
		WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
		random.setDecorationSeed(region.getSeed(), center.getMinBlockX(), center.getMinBlockZ());
		int xo = center.getMinBlockX();
		int zo = center.getMinBlockZ();
		while (random.nextFloat() < WORLD_GEN_SPAWN_CHANCE) {
			Optional<MobSpawnSettings.SpawnerData> next = mobs.getRandom(random);
			if (next.isEmpty()) {
				continue;
			}
			MobSpawnSettings.SpawnerData data = next.get();
			EntityType<?> type = data.type();
			int count = data.count().sample(random);
			SpawnGroupData groupData = null;
			int x = xo + random.nextInt(16);
			int z = zo + random.nextInt(16);
			int startX = x;
			int startZ = z;
			for (int i = 0; i < count; i++) {
				boolean success = false;
				for (int attempt = 0; !success && attempt < 4; attempt++) {
					int top = region.getHeight(SpawnPlacements.getHeightmapType(type), x, z);
					BlockPos pos = SpawnPlacements.getPlacementType(type).adjustSpawnPosition(region, new BlockPos(x, top, z));
					if (type.canSummon() && SpawnPlacements.isSpawnPositionOk(type, region, pos)) {
						float width = type.getWidth();
						double fx = Mth.clamp(x, (double) xo + width, xo + 16.0 - width);
						double fz = Mth.clamp(z, (double) zo + width, zo + 16.0 - width);
						if (region.noCollision(type.getSpawnAABB(fx, pos.getY(), fz)) && SpawnPlacements.checkSpawnRules(type, region,
							EntitySpawnReason.CHUNK_GENERATION, BlockPos.containing(fx, pos.getY(), fz), region.getRandom())) {
							Entity entity = type.create(region.getLevel(), EntitySpawnReason.NATURAL);
							if (entity != null) {
								entity.snapTo(fx, pos.getY(), fz, random.nextFloat() * 360.0F, 0.0F);
								if (entity instanceof Mob mob && mob.checkSpawnRules(region, EntitySpawnReason.CHUNK_GENERATION)
									&& mob.checkSpawnObstruction(region)) {
									groupData = mob.finalizeSpawn(region, region.getCurrentDifficultyAt(mob.blockPosition()),
										EntitySpawnReason.CHUNK_GENERATION, groupData);
									region.addFreshEntityWithPassengers(mob);
									success = true;
								}
							}
						}
					}
					x += random.nextInt(5) - random.nextInt(5);
					for (z += random.nextInt(5) - random.nextInt(5); x < xo || x >= xo + 16 || z < zo || z >= zo + 16;
						z = startZ + random.nextInt(5) - random.nextInt(5)) {
						x = startX + random.nextInt(5) - random.nextInt(5);
					}
				}
			}
		}
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

	@Override
	public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
		byte[] codes = this.terrain(randomState).chunk(x >> 4, z >> 4);
		for (int y = ChaosTerrain.HEIGHT - 1; y >= 0; y--) {
			if (type.isOpaque().test(state(codes[ChaosTerrain.index(x & 15, y, z & 15)]))) {
				return y + 1;
			}
		}
		return heightAccessor.getMinY();
	}

	@Override
	public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
		byte[] codes = this.terrain(randomState).chunk(x >> 4, z >> 4);
		int minY = heightAccessor.getMinY();
		BlockState[] column = new BlockState[heightAccessor.getHeight()];
		for (int i = 0; i < column.length; i++) {
			int y = minY + i;
			column[i] = y >= 0 && y < ChaosTerrain.HEIGHT ? state(codes[ChaosTerrain.index(x & 15, y, z & 15)]) : Blocks.AIR.defaultBlockState();
		}
		return new NoiseColumn(minY, column);
	}

	@Override
	public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos, SamplerContext samplerContext) {
	}
}
