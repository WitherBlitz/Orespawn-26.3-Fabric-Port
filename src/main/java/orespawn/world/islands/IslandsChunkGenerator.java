package orespawn.world.islands;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import orespawn.OreSpawn;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * OreSpawn's Islands dimension terrain (ChunkProviderOreSpawn4): a flat world of bedrock at y 0, dirt at y 1-6 and grass
 * at y 7, scattered with OreSpawn's "scraggly" apple trees (1 to 10 a chunk, oak logs and Apple Tree Leaves wandering
 * up to about y 25, clipped to their own chunk as OreSpawn's were). Above that it's open sky: the floating islands come
 * later, from the Island Blocks {@link IslandsWorldGen} scatters on the grass.
 * <p>
 * Decoration is OreSpawnWorld's DimensionID4 branch ({@link IslandsWorldGen}): OreSpawn's own chunk provider decorated
 * nothing (no grass, flowers, ores, lakes or caves), so the biome has no features. Natural spawns come from this
 * generator ({@link #getMobsAt}), OreSpawn's island list (BiomeGenUtopianPlains.setIslandCreatures); no creatures are
 * placed when a chunk is made, as OreSpawn placed none.
 * <p>
 * OreSpawn's tree random was one sequence for the whole world in the order chunks happened to be made; here each chunk
 * has its own, from the world seed, so the same seed gives the same trees. The Apple Tree Leaves are the Farming
 * group's {@code orespawn:apple_leaves}; until that block exists, persistent oak leaves stand in (OreSpawn's
 * generated leaves never decayed on their own either).
 */
public class IslandsChunkGenerator extends ChunkGenerator {
	public static final MapCodec<IslandsChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource)).apply(i, i.stable(IslandsChunkGenerator::new)));

	/** The grass layer: bedrock at 0, dirt at 1-6, grass at 7 (OreSpawn's cachedBlockIDs). */
	static final int GRASS_Y = 7;
	/** OreSpawn's world was 256 blocks high (y 0-255); its block helpers ignored anything outside that. */
	private static final int MAX_Y = 255;

	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
	private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
	private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
	/** 1.7.10's Blocks.log, metadata 0: an upright oak log. */
	private static final BlockState LOG = Blocks.OAK_LOG.defaultBlockState();

	/** One row of OreSpawn's island spawn list: creature id, weight, group size. */
	private record Spawn(String id, int weight, int min, int max) {
	}

	/**
	 * BiomeGenUtopianPlains.setIslandCreatures, all of OreSpawn's Enable options on (their defaults). OreSpawn put the
	 * first nine in its ambient list and the rest in its monster list; here each spawns in its own category (as the other
	 * groups' spawn tables do), so it counts against the right mob cap. Its creature and water lists were empty.
	 */
	private static final List<Spawn> SPAWNS = List.of(
		new Spawn("butterfly", 5, 2, 6), new Spawn("bird", 4, 1, 2), new Spawn("moth", 5, 2, 4), new Spawn("firefly", 10, 4, 8),
		new Spawn("dragon", 1, 1, 2), new Spawn("stinky", 2, 1, 2), new Spawn("cliff_racer", 20, 3, 6), new Spawn("cloud_shark", 1, 1, 1),
		new Spawn("gold_fish", 5, 2, 4),
		new Spawn("creeping_horror", 60, 4, 8), new Spawn("terrible_terror", 25, 3, 6), new Spawn("lurking_terror", 1, 1, 1),
		new Spawn("nightmare", 15, 3, 6), new Spawn("leaf_monster", 35, 2, 4), new Spawn("ender_reaper", 25, 2, 4),
		new Spawn("hercules_beetle", 5, 1, 2));

	/** The spawn lists by category, built the first time they're asked for (the entity registry is complete by then). */
	private static volatile @Nullable Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawnLists;

	public IslandsChunkGenerator(BiomeSource biomeSource) {
		super(biomeSource);
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	// ---- Terrain (ChunkProviderOreSpawn4.provideChunk) ----

	/** The flat layers at a height. */
	private static BlockState layer(int y) {
		if (y == 0) return BEDROCK;
		if (y > 0 && y < GRASS_Y) return DIRT;
		return y == GRASS_Y ? GRASS : AIR;
	}

	@Override
	public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk, Blender blender, RandomState randomState, StructureManager structureManager,
			BiomeManager biomeManager, @Nullable WorldGenRegion carverBiomeRegion, Set<Holder<Biome>> possibleBiomes) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int y = 0; y <= GRASS_Y; y++) {
			BlockState state = layer(y);
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					chunk.setBlockState(pos.set(x, y, z), state);
				}
			}
		}
		ChunkPos cp = chunk.getPos();
		RandomSource random = randomState.getOrCreateRandomFactory(OreSpawn.id("islands_trees")).at(cp.getMinBlockX(), 0, cp.getMinBlockZ());
		new Trees(chunk, random).addScragglyTrees(cp.getMinBlockX(), cp.getMinBlockZ());
		return CompletableFuture.completedFuture(chunk);
	}

	@Override
	public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
		for (int y = Math.min(GRASS_Y, heightAccessor.getMaxY()); y >= heightAccessor.getMinY(); y--) {
			if (type.isOpaque().test(layer(y))) return y + 1;
		}
		return heightAccessor.getMinY();
	}

	@Override
	public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
		int minY = heightAccessor.getMinY();
		BlockState[] states = new BlockState[Math.max(0, Math.min(GRASS_Y, heightAccessor.getMaxY()) + 1 - minY)];
		for (int i = 0; i < states.length; i++) states[i] = layer(minY + i);
		return new NoiseColumn(minY, states);
	}

	@Override
	public int getSpawnHeight(LevelHeightAccessor heightAccessor) {
		return GRASS_Y + 1;
	}

	@Override
	public int getSeaLevel() {
		// 1.7.10's fixed sea level (there's no water here; bats and the like still measure from it).
		return 63;
	}

	@Override
	public int getMinY() {
		return 0;
	}

	@Override
	public int getGenDepth() {
		return MAX_Y + 1;
	}

	@Override
	public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos, SamplerContext samplerContext) {
	}

	// ---- Decoration and creatures ----

	/** After the (empty) biome decoration, OreSpawnWorld.generate's DimensionID4 branch. */
	@Override
	public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
		super.applyBiomeDecoration(level, chunk, structureManager);
		ChunkPos pos = chunk.getPos();
		IslandsWorldGen.generate(level, pos.x(), pos.z());
	}

	/** ChunkProviderOreSpawn4.populate didn't run SpawnerAnimals.performWorldGenSpawning: no creatures with new chunks. */
	@Override
	public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
	}

	/** ChunkProviderOreSpawn4.getPossibleCreatures: the island list, whatever the biome's own lists say. */
	@Override
	public WeightedList<MobSpawnSettings.SpawnerData> getMobsAt(Level level, StructureManager structureManager, MobCategory mobCategory, BlockPos pos) {
		return spawnLists().getOrDefault(mobCategory, WeightedList.of());
	}

	/** The island spawn lists, each creature looked up by id; ones no group has registered yet are left out. */
	private static Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawnLists() {
		Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> lists = spawnLists;
		if (lists == null) {
			Map<MobCategory, WeightedList.Builder<MobSpawnSettings.SpawnerData>> builders = new EnumMap<>(MobCategory.class);
			for (Spawn s : SPAWNS) {
				EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id(s.id())).orElse(null);
				if (type == null || type.getCategory() == MobCategory.MISC) continue;
				builders.computeIfAbsent(type.getCategory(), c -> WeightedList.builder())
					.add(new MobSpawnSettings.SpawnerData(type, UniformInt.of(s.min(), s.max())), s.weight());
			}
			lists = new EnumMap<>(MobCategory.class);
			for (var e : builders.entrySet()) lists.put(e.getKey(), e.getValue().build());
			spawnLists = lists;
		}
		return lists;
	}

	// ---- Blocks shared with IslandsWorldGen ----

	/** OreSpawnMain.MyAppleLeaves: the Farming group's block, or persistent oak leaves until it exists. */
	static BlockState appleLeaves() {
		Block block = BuiltInRegistries.BLOCK.getOptional(OreSpawn.id("apple_leaves")).orElse(null);
		if (block == null) return Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
		BlockState state = block.defaultBlockState();
		return state.hasProperty(LeavesBlock.PERSISTENT) ? state.setValue(LeavesBlock.PERSISTENT, true) : state;
	}

	/** OreSpawnMain.MyScaryLeaves, or null until the Farming group's block exists. */
	static @Nullable Block scaryLeaves() {
		return BuiltInRegistries.BLOCK.getOptional(OreSpawn.id("scary_leaves")).orElse(null);
	}

	/** 1.7.10's Blocks.log: its oak, spruce, birch and jungle logs (any metadata). */
	static boolean isLog(BlockState state) {
		return state.is(Blocks.OAK_LOG) || state.is(Blocks.SPRUCE_LOG) || state.is(Blocks.BIRCH_LOG) || state.is(Blocks.JUNGLE_LOG);
	}

	// ---- ChunkProviderOreSpawn4's scraggly trees ----

	/**
	 * addScragglyTrees, ScragglyTreeWithBranches and makeScragglyBranch, unchanged. They work on the chunk being made
	 * only (OreSpawnMain.getBlockIDInChunk / setBlockIDWithMetadataInChunk): outside it, reads see air and writes are
	 * dropped, so a tree reaching over the edge is cut off there.
	 */
	private static final class Trees {
		private final ChunkAccess chunk;
		private final int chunkX;
		private final int chunkZ;
		private final RandomSource random;
		private final BlockState leaves = appleLeaves();
		private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		Trees(ChunkAccess chunk, RandomSource random) {
			this.chunk = chunk;
			this.chunkX = chunk.getPos().x();
			this.chunkZ = chunk.getPos().z();
			this.random = random;
		}

		private boolean inChunk(int x, int y, int z) {
			return x >> 4 == this.chunkX && z >> 4 == this.chunkZ && y >= 0 && y <= MAX_Y;
		}

		private BlockState get(int x, int y, int z) {
			return this.inChunk(x, y, z) ? this.chunk.getBlockState(this.pos.set(x, y, z)) : AIR;
		}

		private void set(int x, int y, int z, BlockState state) {
			if (this.inChunk(x, y, z)) this.chunk.setBlockState(this.pos.set(x, y, z), state);
		}

		/** Air, a log or Apple Tree Leaves: what a trunk or branch may grow through. */
		private boolean open(BlockState state) {
			return state.isAir() || isLog(state) || state.is(this.leaves.getBlock());
		}

		/** Each of the 3x3 around (x, y, z) has a 1 in 2 chance of leaves where it's air, then the block above. */
		private void leavesAround(int x, int y, int z) {
			for (int m = -1; m < 2; m++) {
				for (int n = -1; n < 2; n++) {
					if (this.random.nextInt(2) == 1 && this.get(x + m, y, z + n).isAir()) {
						this.set(x + m, y, z + n, this.leaves);
					}
				}
			}
			if (this.random.nextInt(2) == 1 && this.get(x, y + 1, z).isAir()) {
				this.set(x, y + 1, z, this.leaves);
			}
		}

		void addScragglyTrees(int chunkX, int chunkZ) {
			int howmany = 1 + this.random.nextInt(10); // OreSpawn's LessLag 0 (its default) kept them all
			for (int i = 0; i < howmany; i++) {
				int posX = 2 + chunkX + this.random.nextInt(12);
				int posZ = 2 + chunkZ + this.random.nextInt(12);
				for (int posY = 20; posY > 2; posY--) {
					if (this.get(posX, posY - 1, posZ).is(Blocks.GRASS_BLOCK)) {
						this.scragglyTreeWithBranches(posX, posY, posZ);
						break;
					}
				}
			}
		}

		private void makeScragglyBranch(int x, int y, int z, int len, int biasx, int biasz) {
			for (int k = 0; k < len; k++) {
				int ix = Math.clamp(this.random.nextInt(2) - this.random.nextInt(2) + biasx, -1, 1);
				int iz = Math.clamp(this.random.nextInt(2) - this.random.nextInt(2) + biasz, -1, 1);
				int iy = this.random.nextInt(3) > 0 ? 1 : 0;
				x += ix;
				z += iz;
				y += iy;
				if (!this.open(this.get(x, y, z))) return;
				this.set(x, y, z, LOG);
				this.leavesAround(x, y, z);
			}
		}

		private void scragglyTreeWithBranches(int x, int y, int z) {
			int i = 1 + this.random.nextInt(3);
			int j = i + this.random.nextInt(12);
			for (int k = 0; k < i; k++) {
				if (k >= 1 && !this.open(this.get(x, y + k, z))) return;
				this.set(x, y + k, z, LOG);
			}
			y += i - 1;
			for (int step = i; step < j; step++) {
				int ix = this.random.nextInt(2) - this.random.nextInt(2);
				int iz = this.random.nextInt(2) - this.random.nextInt(2);
				int iy = this.random.nextInt(4) > 0 ? 1 : 0;
				x += ix;
				z += iz;
				y += iy;
				if (!this.open(this.get(x, y, z))) break;
				this.set(x, y, z, LOG);
				if (this.random.nextInt(4) == 1) {
					int len = this.random.nextInt(1 + j - step);
					int biasx = this.random.nextInt(2) - this.random.nextInt(2);
					int biasz = this.random.nextInt(2) - this.random.nextInt(2);
					this.makeScragglyBranch(x, y, z, len, biasx, biasz);
				}
				this.leavesAround(x, y, z);
			}
		}
	}
}
