package orespawn.world.mining;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/**
 * The parts of OreSpawn's {@code ChunkProviderOreSpawn2.populate} that aren't the biome's data features: its water lakes
 * (one chunk in four, at any height), lava lakes (one in eight, mostly below sea level), eight tries at a vanilla
 * monster room, and from Extreme Hills' decoration ({@code BiomeDecorator.generateOres}, the springs, and
 * {@code BiomeGenHills.decorate}) 1.7.10's ore veins, water and lava springs, single emerald ores and silverfish stone.
 * The decorator's other parts (trees, flowers, grass, mushrooms, sugar cane, pumpkins, sand/clay/gravel disks) and the
 * freezing pass are 26.3's own features, listed in the {@code orespawn:mining} biome; the mineshafts and strongholds are
 * 26.3's structures. Positions keep 1.7.10's +8 offset, so everything stays within the chunks next to this one.
 */
final class MiningPopulator {
	private static final LakeFeature WATER_LAKE = lake(Blocks.WATER, Blocks.AIR);
	private static final LakeFeature LAVA_LAKE = lake(Blocks.LAVA, Blocks.STONE);
	private static final MonsterRoomFeature MONSTER_ROOM = new MonsterRoomFeature();

	private MiningPopulator() {
	}

	/** WorldGenLakes: 1.7.10 put stone walls round lava lakes only. */
	private static LakeFeature lake(Block fluid, Block barrier) {
		return new LakeFeature(BlockStateProvider.holderOf(fluid), BlockStateProvider.holderOf(barrier), BlockPredicate.alwaysTrue(),
			BlockPredicate.not(BlockPredicate.matchesTag(BlockTags.FEATURES_CANNOT_REPLACE)),
			BlockPredicate.not(BlockPredicate.matchesTag(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE)));
	}

	static boolean inWorld(LevelHeightAccessor level, BlockPos pos) {
		return !level.isOutsideBuildHeight(pos);
	}

	/** populate's random: seeded from the world seed and the chunk, as 1.7.10 did. */
	static RandomSource random(long seed, int chunkX, int chunkZ) {
		RandomSource rand = new LegacyRandomSource(seed);
		long i1 = rand.nextLong() / 2L * 2L + 1L;
		long j1 = rand.nextLong() / 2L * 2L + 1L;
		rand.setSeed(chunkX * i1 + chunkZ * j1 ^ seed);
		return rand;
	}

	/** populate, up to and including the decorator's ores (run before the biome's own features). */
	static void beforeFeatures(WorldGenLevel level, ChunkGenerator generator, RandomSource rand, int chunkX, int chunkZ) {
		int k = chunkX * 16;
		int l = chunkZ * 16;
		// Extreme Hills is neither desert biome, so its lakes are allowed.
		if (rand.nextInt(4) == 0) {
			int x = k + rand.nextInt(16) + 8;
			int y = rand.nextInt(256);
			int z = l + rand.nextInt(16) + 8;
			lake(level, generator, rand, WATER_LAKE, x, y, z);
		}
		if (rand.nextInt(8) == 0) {
			int x = k + rand.nextInt(16) + 8;
			int y = rand.nextInt(rand.nextInt(248) + 8);
			int z = l + rand.nextInt(16) + 8;
			if (y < 63 || rand.nextInt(10) == 0) {
				lake(level, generator, rand, LAVA_LAKE, x, y, z);
			}
		}
		for (int i = 0; i < 8; i++) {
			int x = k + rand.nextInt(16) + 8;
			int y = rand.nextInt(256);
			int z = l + rand.nextInt(16) + 8;
			BlockPos pos = new BlockPos(x, y, z);
			if (inWorld(level, pos)) {
				MONSTER_ROOM.place(level, generator, rand, pos);
			}
		}
		decoratorOres(level, rand, k, l);
	}

	/**
	 * WorldGenLakes.generate: from (x - 8, y, z - 8) it sinks through air, then 26.3's lake (the same shape) is placed
	 * there; it gives up at y 4 and below.
	 */
	private static void lake(WorldGenLevel level, ChunkGenerator generator, RandomSource rand, LakeFeature lake, int x, int y, int z) {
		BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos(x - 8, y, z - 8);
		while (y > 5 && level.getBlockState(probe.setY(y)).isAir()) {
			y--;
		}
		if (y <= 4) {
			return;
		}
		lake.place(level, generator, rand, new BlockPos(x - 8, y, z - 8));
	}

	/** BiomeDecorator.generateOres: the overworld's 1.7.10 veins, by its counts and heights. */
	private static void decoratorOres(WorldGenLevel level, RandomSource rand, int chunkX, int chunkZ) {
		MiningOres.OreRandom r = MiningOres.OreRandom.of(rand);
		standard(level, rand, r, chunkX, chunkZ, 20, Blocks.DIRT, 32, 0, 256);
		standard(level, rand, r, chunkX, chunkZ, 10, Blocks.GRAVEL, 32, 0, 256);
		standard(level, rand, r, chunkX, chunkZ, 20, Blocks.COAL_ORE, 16, 0, 128);
		standard(level, rand, r, chunkX, chunkZ, 20, Blocks.IRON_ORE, 8, 0, 64);
		standard(level, rand, r, chunkX, chunkZ, 2, Blocks.GOLD_ORE, 8, 0, 32);
		standard(level, rand, r, chunkX, chunkZ, 8, Blocks.REDSTONE_ORE, 7, 0, 16);
		standard(level, rand, r, chunkX, chunkZ, 1, Blocks.DIAMOND_ORE, 7, 0, 16);
		// genStandardOre2(1, lapis, 16, 16): a triangle around y 16.
		int x = chunkX + rand.nextInt(16);
		int y = rand.nextInt(16) + rand.nextInt(16) + (16 - 16);
		int z = chunkZ + rand.nextInt(16);
		MiningOres.minable(level, r, x, y, z, Blocks.LAPIS_ORE.defaultBlockState(), 6, Blocks.STONE);
	}

	/** BiomeDecorator.genStandardOre1. */
	private static void standard(WorldGenLevel level, RandomSource rand, MiningOres.OreRandom r, int chunkX, int chunkZ, int count, Block ore,
			int size, int minY, int maxY) {
		for (int i = 0; i < count; i++) {
			int x = chunkX + rand.nextInt(16);
			int y = rand.nextInt(maxY - minY) + minY;
			int z = chunkZ + rand.nextInt(16);
			MiningOres.minable(level, r, x, y, z, ore.defaultBlockState(), size, Blocks.STONE);
		}
	}

	/** The end of the decorator (its 50 water and 20 lava springs), then BiomeGenHills' emerald ore and silverfish stone. */
	static void afterFeatures(WorldGenLevel level, RandomSource rand, int chunkX, int chunkZ) {
		int k = chunkX * 16;
		int l = chunkZ * 16;
		BlockState water = Blocks.WATER.defaultBlockState();
		BlockState lava = Blocks.LAVA.defaultBlockState();
		for (int i = 0; i < 50; i++) {
			int x = k + rand.nextInt(16) + 8;
			int y = rand.nextInt(rand.nextInt(248) + 8);
			int z = l + rand.nextInt(16) + 8;
			spring(level, x, y, z, water);
		}
		for (int i = 0; i < 20; i++) {
			int x = k + rand.nextInt(16) + 8;
			int y = rand.nextInt(rand.nextInt(rand.nextInt(240) + 8) + 8);
			int z = l + rand.nextInt(16) + 8;
			spring(level, x, y, z, lava);
		}
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int emeralds = 3 + rand.nextInt(6);
		for (int i = 0; i < emeralds; i++) {
			int x = k + rand.nextInt(16);
			int y = rand.nextInt(28) + 4;
			int z = l + rand.nextInt(16);
			if (level.getBlockState(pos.set(x, y, z)).is(Blocks.STONE)) {
				level.setBlock(pos, Blocks.EMERALD_ORE.defaultBlockState(), 2);
			}
		}
		MiningOres.OreRandom r = MiningOres.OreRandom.of(rand);
		for (int i = 0; i < 7; i++) {
			int x = k + rand.nextInt(16);
			int y = rand.nextInt(64);
			int z = l + rand.nextInt(16);
			MiningOres.minable(level, r, x, y, z, Blocks.INFESTED_STONE.defaultBlockState(), 8, Blocks.STONE);
		}
	}

	/** WorldGenLiquids: a spring in a stone wall with stone above and below, three stone sides and one open. */
	private static void spring(WorldGenLevel level, int x, int y, int z, BlockState fluid) {
		BlockPos pos = new BlockPos(x, y, z);
		if (!inWorld(level, pos) || !level.getBlockState(pos.above()).is(Blocks.STONE) || !level.getBlockState(pos.below()).is(Blocks.STONE)) {
			return;
		}
		BlockState here = level.getBlockState(pos);
		if (!here.isAir() && !here.is(Blocks.STONE)) {
			return;
		}
		int stone = 0;
		int air = 0;
		for (BlockPos side : new BlockPos[] {pos.west(), pos.east(), pos.north(), pos.south()}) {
			BlockState state = level.getBlockState(side);
			if (state.is(Blocks.STONE)) {
				stone++;
			}
			if (state.isAir()) {
				air++;
			}
		}
		if (stone == 3 && air == 1) {
			level.setBlock(pos, fluid, 2);
			level.scheduleTick(pos, fluid.getFluidState().getType(), 0);
		}
	}
}
