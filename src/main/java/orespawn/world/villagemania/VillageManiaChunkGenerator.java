package orespawn.world.villagemania;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import orespawn.OreSpawn;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Village Mania's chunk generator (chunk generator type {@code orespawn:village_mania}): OreSpawn's
 * ChunkProviderOreSpawn3 for WorldProviderOreSpawn3 (DimensionID3, "Dimension-VillageMania"). The terrain, its surface,
 * caves, ravines and chunk ores are {@link VillageManiaTerrain}, 1.7.10's own algorithms on the world seed. Villages
 * are 26.3's plains villages ({@code orespawn:village_mania_plains}, a jigsaw structure on the Villages biome) on
 * MapGenMoreVillages' grid: one start every 9 chunks with 7 kept clear (an offset of 0 or 1 chunk), salt 10387312,
 * so the spots match OreSpawn's for a seed. Mineshafts are vanilla's ({@code orespawn:village_mania_mineshaft},
 * MapGenMineshaft's 0.004 odds), as the chunk provider meant them: in OreSpawn they only showed up in chunks generated
 * after a nearby chunk was loaded back from disk, because it never registered them for new chunks. Then
 * {@link VillageManiaPopulator} runs the populate step and OreSpawnWorld's Village Mania branch, and
 * {@link #spawnOriginalMobs} is populate's SpawnerAnimals.performWorldGenSpawning.
 * <p>
 * Not ported: strongholds. OreSpawn's chunk provider created them with the same quirk, and here they would bring a
 * second set of End portals and need their own eye-of-ender tag. Desert and jungle temples and witch huts can't
 * occur: none of them accept the Villages biome.
 */
@SuppressWarnings("deprecation") // RandomState.seed(): the world seed, which 1.7.10's noise is built from
public final class VillageManiaChunkGenerator extends ChunkGenerator {
	public static final MapCodec<VillageManiaChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource)
	).apply(i, i.stable(VillageManiaChunkGenerator::new)));
	/** OreSpawn's village structure, which keeps lakes out of its chunks. */
	private static final Identifier VILLAGE = OreSpawn.id("village_mania_plains");
	private volatile @Nullable VillageManiaTerrain terrain;

	public VillageManiaChunkGenerator(BiomeSource biomeSource) {
		super(biomeSource);
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	/** The terrain for a world seed (one per generator; a new seed replaces it). */
	private VillageManiaTerrain terrain(long seed) {
		VillageManiaTerrain t = this.terrain;
		if (t == null || t.seed() != seed) {
			synchronized (this) {
				t = this.terrain;
				if (t == null || t.seed() != seed) {
					t = new VillageManiaTerrain(seed);
					this.terrain = t;
				}
			}
		}
		return t;
	}

	@Override
	public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk, Blender blender, RandomState randomState, StructureManager structureManager,
			BiomeManager biomeManager, @Nullable WorldGenRegion carverBiomeRegion, Set<Holder<Biome>> possibleBiomes) {
		return CompletableFuture.supplyAsync(() -> {
			ChunkPos pos = chunk.getPos();
			Beardifier beardifier = Beardifier.forStructuresInChunk(structureManager, pos);
			this.write(chunk, this.terrain(randomState.seed()).chunk(pos.x(), pos.z(), beardifier));
			return chunk;
		}, Util.backgroundExecutor().forName("buildTerrain"));
	}

	/** The 1.7.10 block array into the chunk, with its worldgen heightmaps. */
	private void write(ChunkAccess chunk, BlockState[] blocks) {
		Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		int minY = Math.max(chunk.getMinY(), 0);
		int maxY = Math.min(chunk.getMaxY(), VillageManiaTerrain.HEIGHT - 1);
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
		// The structures (villages, mineshafts) and the biome's features (the ant nests), then OreSpawn's populate and
		// OreSpawnWorld, which 1.7.10 ran after the structures were built.
		super.applyBiomeDecoration(level, chunk, structureManager);
		ChunkPos pos = chunk.getPos();
		VillageManiaPopulator.populate(level, this, pos, villageAt(level, structureManager, pos));
		VillageManiaPopulator.oreSpawnWorld(level, pos);
	}

	/** populate's flag: a village stands in the area 1.7.10 populated (the chunk, offset 8 blocks into x and z). */
	private static boolean villageAt(WorldGenLevel level, StructureManager structureManager, ChunkPos pos) {
		Registry<Structure> structures = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
		int x = pos.getMinBlockX() + 8;
		int z = pos.getMinBlockZ() + 8;
		List<StructureStart> starts = structureManager.startsForStructure(pos.x(), pos.z(), s -> VILLAGE.equals(structures.getKey(s)));
		for (StructureStart start : starts) {
			if (start.getBoundingBox().intersects(x, z, x + 15, z + 15)) {
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
		return VillageManiaTerrain.HEIGHT;
	}

	@Override
	public int getSeaLevel() {
		return VillageManiaTerrain.SEA_LEVEL;
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
		BlockState[] column = this.terrain(randomState.seed()).baseColumn(x, z);
		for (int y = VillageManiaTerrain.HEIGHT - 1; y >= 0; y--) {
			if (type.isOpaque().test(column[y])) {
				return y + 1;
			}
		}
		return heightAccessor.getMinY();
	}

	@Override
	public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
		BlockState[] terrain = this.terrain(randomState.seed()).baseColumn(x, z);
		int minY = heightAccessor.getMinY();
		BlockState[] column = new BlockState[heightAccessor.getHeight()];
		for (int i = 0; i < column.length; i++) {
			int y = minY + i;
			column[i] = y >= 0 && y < VillageManiaTerrain.HEIGHT ? terrain[y] : VillageManiaTerrain.AIR;
		}
		return new NoiseColumn(minY, column);
	}

	@Override
	public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos, SamplerContext samplerContext) {
	}
}
