package orespawn.world.villagemania;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.material.Fluids;
import orespawn.OreSpawn;
import orespawn.world.dungeons.OreSpawnDungeons;
import orespawn.original.OriginalDungeons;

import java.util.Random;

/**
 * What decorates a Village Mania chunk once its terrain and structures are in, in 1.7.10's order:
 * <ul>
 * <li>{@link #populate}: ChunkProviderOreSpawn3.populate. A water lake one chunk in four and a lava lake one in eight
 * (neither where a village stands), eight dungeon tries (vanilla's monster room, as WorldGenDungeons), then the Villages
 * biome's BiomeDecorator: dirt, gravel and the vanilla ores at 1.7.10's heights, sand, clay and gravel patches under
 * water, four flower patches (dandelions two times in three, else poppies), six of grass, mushrooms now and then, sugar
 * cane by water, a pumpkin patch one chunk in 32, and 50 water and 20 lava springs. No trees: OreSpawn set the
 * biome's trees to -999. The mobs it spawned with the chunk are the generator's spawnOriginalMobs.</li>
 * <li>{@link #oreSpawnWorld}: OreSpawnWorld.generate's Village Mania branch (after the ant nests, which are the biome's
 * {@code orespawn:ant_nests} feature): mosquito plants, apple, cherry and peach trees, OreSpawn's generic dungeon,
 * and while no OreSpawn structure was placed lately, the damsel in distress, the spider hangout and the red ant
 * hangout (the Dungeons group's builders).</li>
 * </ul>
 * The 1.7.10 generators are ported as they were, writing with flag 2 (no block updates), except that springs are
 * ticked so they flow, as 1.7.10 did at once. Other groups' blocks (mosquito plant, fruit leaves) are looked up by id
 * and skipped while not registered. One change from OreSpawn: its Village Mania branch passed the chunk's coordinates
 * to addMosquitos instead of its block coordinates (a bug that put every mosquito plant near the world's origin, which
 * 26.3's world generation can't reach from other chunks); here they go in the chunk itself.
 */
final class VillageManiaPopulator {
	private static final MonsterRoomFeature MONSTER_ROOM = new MonsterRoomFeature();
	private static final BlockState WATER = Blocks.WATER.defaultBlockState();
	private static final BlockState LAVA = Blocks.LAVA.defaultBlockState();
	private static final int FLAGS = Block.UPDATE_CLIENTS;

	private VillageManiaPopulator() {
	}

	// ---- ChunkProviderOreSpawn3.populate ----

	static void populate(WorldGenLevel level, ChunkGenerator generator, ChunkPos chunk, boolean village) {
		long seed = level.getSeed();
		int k = chunk.getMinBlockX();
		int l = chunk.getMinBlockZ();
		Random rand = new Random(seed);
		long i1 = rand.nextLong() / 2L * 2L + 1L;
		long j1 = rand.nextLong() / 2L * 2L + 1L;
		rand.setSeed(chunk.x() * i1 + chunk.z() * j1 ^ seed);
		// (1.7.10 then built the chunk's mineshafts, villages and strongholds, drawing from this random too.)
		if (!village && rand.nextInt(4) == 0) {
			int x = k + rand.nextInt(16) + 8;
			int y = rand.nextInt(256);
			int z = l + rand.nextInt(16) + 8;
			lake(level, rand, x, y, z, WATER);
		}
		if (!village && rand.nextInt(8) == 0) {
			int x = k + rand.nextInt(16) + 8;
			int y = rand.nextInt(rand.nextInt(248) + 8);
			int z = l + rand.nextInt(16) + 8;
			if (y < 63 || rand.nextInt(10) == 0) {
				lake(level, rand, x, y, z, LAVA);
			}
		}
		for (int n = 0; n < 8; n++) {
			int x = k + rand.nextInt(16) + 8;
			int y = rand.nextInt(256);
			int z = l + rand.nextInt(16) + 8;
			MONSTER_ROOM.place(level, generator, RandomSource.create(rand.nextLong()), new BlockPos(x, y, z));
		}
		decorate(level, rand, k, l);
	}

	/** WorldGenLakes (1.7.10's Material.isSolid / isLiquid are BlockState.isSolid / liquid, as vanilla's LakeFeature uses). */
	@SuppressWarnings("deprecation")
	private static void lake(WorldGenLevel level, Random rand, int x, int y, int z, BlockState fluid) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		x -= 8;
		z -= 8;
		while (y > 5 && isAir(level, p.set(x, y, z))) {
			y--;
		}
		if (y <= 4) {
			return;
		}
		y -= 4;
		boolean[] grid = new boolean[2048];
		int spots = rand.nextInt(4) + 4;
		for (int i = 0; i < spots; i++) {
			double d0 = rand.nextDouble() * 6.0 + 3.0;
			double d1 = rand.nextDouble() * 4.0 + 2.0;
			double d2 = rand.nextDouble() * 6.0 + 3.0;
			double d3 = rand.nextDouble() * (16.0 - d0 - 2.0) + 1.0 + d0 / 2.0;
			double d4 = rand.nextDouble() * (8.0 - d1 - 4.0) + 2.0 + d1 / 2.0;
			double d5 = rand.nextDouble() * (16.0 - d2 - 2.0) + 1.0 + d2 / 2.0;
			for (int a = 1; a < 15; a++) {
				for (int b = 1; b < 15; b++) {
					for (int c = 1; c < 7; c++) {
						double d6 = (a - d3) / (d0 / 2.0);
						double d7 = (c - d4) / (d1 / 2.0);
						double d8 = (b - d5) / (d2 / 2.0);
						if (d6 * d6 + d7 * d7 + d8 * d8 < 1.0) {
							grid[(a * 16 + b) * 8 + c] = true;
						}
					}
				}
			}
		}
		for (int a = 0; a < 16; a++) {
			for (int b = 0; b < 16; b++) {
				for (int c = 0; c < 8; c++) {
					if (edge(grid, a, b, c)) {
						BlockState state = level.getBlockState(p.set(x + a, y + c, z + b));
						if (c >= 4 && state.liquid()) {
							return;
						}
						if (c < 4 && !state.isSolid() && state != fluid) {
							return;
						}
					}
				}
			}
		}
		// The sky light check of the grass pass below, as it was before the lake: open air above the ground.
		int[] surface = new int[256];
		for (int a = 0; a < 16; a++) {
			for (int b = 0; b < 16; b++) {
				surface[a * 16 + b] = level.getHeight(Heightmap.Types.WORLD_SURFACE, x + a, z + b);
			}
		}
		for (int a = 0; a < 16; a++) {
			for (int b = 0; b < 16; b++) {
				for (int c = 0; c < 8; c++) {
					if (grid[(a * 16 + b) * 8 + c]) {
						level.setBlock(p.set(x + a, y + c, z + b), c >= 4 ? Blocks.AIR.defaultBlockState() : fluid, FLAGS);
					}
				}
			}
		}
		for (int a = 0; a < 16; a++) {
			for (int b = 0; b < 16; b++) {
				for (int c = 4; c < 8; c++) {
					if (grid[(a * 16 + b) * 8 + c] && y + c >= surface[a * 16 + b] && level.getBlockState(p.set(x + a, y + c - 1, z + b)).is(Blocks.DIRT)) {
						level.setBlock(p, Blocks.GRASS_BLOCK.defaultBlockState(), FLAGS);
					}
				}
			}
		}
		if (fluid == LAVA) {
			for (int a = 0; a < 16; a++) {
				for (int b = 0; b < 16; b++) {
					for (int c = 0; c < 8; c++) {
						if (edge(grid, a, b, c) && (c < 4 || rand.nextInt(2) != 0) && level.getBlockState(p.set(x + a, y + c, z + b)).isSolid()) {
							level.setBlock(p, Blocks.STONE.defaultBlockState(), FLAGS);
						}
					}
				}
			}
		} else {
			for (int a = 0; a < 16; a++) {
				for (int b = 0; b < 16; b++) {
					p.set(x + a, y + 4, z + b);
					if (level.getBiome(p).value().shouldFreeze(level, p, false)) {
						level.setBlock(p, Blocks.ICE.defaultBlockState(), FLAGS);
					}
				}
			}
		}
	}

	/** A lake cell's outside neighbour: not in the lake, next to a cell that is. */
	private static boolean edge(boolean[] grid, int a, int b, int c) {
		return !grid[(a * 16 + b) * 8 + c] && (a < 15 && grid[((a + 1) * 16 + b) * 8 + c] || a > 0 && grid[((a - 1) * 16 + b) * 8 + c]
			|| b < 15 && grid[(a * 16 + b + 1) * 8 + c] || b > 0 && grid[(a * 16 + (b - 1)) * 8 + c] || c < 7 && grid[(a * 16 + b) * 8 + c + 1]
			|| c > 0 && grid[(a * 16 + b) * 8 + (c - 1)]);
	}

	// ---- BiomeDecorator.genDecorations (the Villages biome: no trees, 4 flowers, 6 grass) ----

	private static void decorate(WorldGenLevel level, Random rand, int cx, int cz) {
		ore(level, rand, cx, cz, 20, Blocks.DIRT, 32, 0, 256);
		ore(level, rand, cx, cz, 10, Blocks.GRAVEL, 32, 0, 256);
		ore(level, rand, cx, cz, 20, Blocks.COAL_ORE, 16, 0, 128);
		ore(level, rand, cx, cz, 20, Blocks.IRON_ORE, 8, 0, 64);
		ore(level, rand, cx, cz, 2, Blocks.GOLD_ORE, 8, 0, 32);
		ore(level, rand, cx, cz, 8, Blocks.REDSTONE_ORE, 7, 0, 16);
		ore(level, rand, cx, cz, 1, Blocks.DIAMOND_ORE, 7, 0, 16);
		// genStandardOre2(1, lapis, 16, 16)
		{
			int x = cx + rand.nextInt(16);
			int y = rand.nextInt(16) + rand.nextInt(16);
			int z = cz + rand.nextInt(16);
			minable(level, rand, x, y, z, Blocks.LAPIS_ORE.defaultBlockState(), 6);
		}
		for (int i = 0; i < 3; i++) {
			int x = cx + rand.nextInt(16) + 8;
			int z = cz + rand.nextInt(16) + 8;
			disk(level, rand, x, topSolidOrLiquid(level, x, z), z, Blocks.SAND.defaultBlockState(), 7, 2, false);
		}
		{
			int x = cx + rand.nextInt(16) + 8;
			int z = cz + rand.nextInt(16) + 8;
			disk(level, rand, x, topSolidOrLiquid(level, x, z), z, Blocks.CLAY.defaultBlockState(), 4, 1, true);
		}
		{
			int x = cx + rand.nextInt(16) + 8;
			int z = cz + rand.nextInt(16) + 8;
			disk(level, rand, x, topSolidOrLiquid(level, x, z), z, Blocks.GRAVEL.defaultBlockState(), 6, 2, false);
		}
		rand.nextInt(10); // treesPerChunk -999, plus one 1 time in 10: still none
		for (int i = 0; i < 4; i++) {
			int x = cx + rand.nextInt(16) + 8;
			int z = cz + rand.nextInt(16) + 8;
			int y = rand.nextInt(height(level, x, z) + 32);
			BlockState flower = rand.nextInt(3) > 0 ? Blocks.DANDELION.defaultBlockState() : Blocks.POPPY.defaultBlockState();
			flowers(level, rand, x, y, z, flower);
		}
		for (int i = 0; i < 6; i++) {
			int x = cx + rand.nextInt(16) + 8;
			int z = cz + rand.nextInt(16) + 8;
			int y = rand.nextInt(Math.max(1, height(level, x, z) * 2));
			tallGrass(level, rand, x, y, z);
		}
		if (rand.nextInt(4) == 0) {
			int x = cx + rand.nextInt(16) + 8;
			int z = cz + rand.nextInt(16) + 8;
			int y = rand.nextInt(Math.max(1, height(level, x, z) * 2));
			flowers(level, rand, x, y, z, Blocks.BROWN_MUSHROOM.defaultBlockState());
		}
		if (rand.nextInt(8) == 0) {
			int x = cx + rand.nextInt(16) + 8;
			int z = cz + rand.nextInt(16) + 8;
			int y = rand.nextInt(Math.max(1, height(level, x, z) * 2));
			flowers(level, rand, x, y, z, Blocks.RED_MUSHROOM.defaultBlockState());
		}
		for (int i = 0; i < 10; i++) {
			int x = cx + rand.nextInt(16) + 8;
			int z = cz + rand.nextInt(16) + 8;
			int y = rand.nextInt(Math.max(1, height(level, x, z) * 2));
			reeds(level, rand, x, y, z);
		}
		if (rand.nextInt(32) == 0) {
			int x = cx + rand.nextInt(16) + 8;
			int z = cz + rand.nextInt(16) + 8;
			int y = rand.nextInt(Math.max(1, height(level, x, z) * 2));
			pumpkins(level, rand, x, y, z);
		}
		for (int i = 0; i < 50; i++) {
			int x = cx + rand.nextInt(16) + 8;
			int y = rand.nextInt(rand.nextInt(248) + 8);
			int z = cz + rand.nextInt(16) + 8;
			spring(level, x, y, z, WATER);
		}
		for (int i = 0; i < 20; i++) {
			int x = cx + rand.nextInt(16) + 8;
			int y = rand.nextInt(rand.nextInt(rand.nextInt(240) + 8) + 8);
			int z = cz + rand.nextInt(16) + 8;
			spring(level, x, y, z, LAVA);
		}
	}

	/** World.getHeightValue: above the highest block that dims light (water and leaves count, plants don't). */
	private static int height(WorldGenLevel level, int x, int z) {
		return level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
	}

	/** World.getTopSolidOrLiquidBlock: above the highest block that blocks movement. */
	private static int topSolidOrLiquid(WorldGenLevel level, int x, int z) {
		return level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
	}

	private static boolean isAir(WorldGenLevel level, BlockPos pos) {
		return level.getBlockState(pos).isAir();
	}

	/** genStandardOre1. */
	private static void ore(WorldGenLevel level, Random rand, int cx, int cz, int count, Block block, int size, int minY, int maxY) {
		BlockState state = block.defaultBlockState();
		for (int i = 0; i < count; i++) {
			int x = cx + rand.nextInt(16);
			int y = rand.nextInt(maxY - minY) + minY;
			int z = cz + rand.nextInt(16);
			minable(level, rand, x, y, z, state, size);
		}
	}

	/** WorldGenMinable: a vein of {@code size} in stone, centred 8 blocks into x and z. */
	private static void minable(WorldGenLevel level, Random rand, int x, int y, int z, BlockState ore, int size) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		float f = rand.nextFloat() * (float) Math.PI;
		double d0 = x + 8 + Legacy.sin(f) * size / 8.0F;
		double d1 = x + 8 - Legacy.sin(f) * size / 8.0F;
		double d2 = z + 8 + Legacy.cos(f) * size / 8.0F;
		double d3 = z + 8 - Legacy.cos(f) * size / 8.0F;
		double d4 = y + rand.nextInt(3) - 2;
		double d5 = y + rand.nextInt(3) - 2;
		for (int l = 0; l <= size; l++) {
			double d6 = d0 + (d1 - d0) * l / size;
			double d7 = d4 + (d5 - d4) * l / size;
			double d8 = d2 + (d3 - d2) * l / size;
			double d9 = rand.nextDouble() * size / 16.0;
			double d10 = (Legacy.sin(l * (float) Math.PI / size) + 1.0F) * d9 + 1.0;
			double d11 = (Legacy.sin(l * (float) Math.PI / size) + 1.0F) * d9 + 1.0;
			int i1 = Legacy.floor(d6 - d10 / 2.0);
			int j1 = Legacy.floor(d7 - d11 / 2.0);
			int k1 = Legacy.floor(d8 - d10 / 2.0);
			int l1 = Legacy.floor(d6 + d10 / 2.0);
			int i2 = Legacy.floor(d7 + d11 / 2.0);
			int j2 = Legacy.floor(d8 + d10 / 2.0);
			for (int k2 = i1; k2 <= l1; k2++) {
				double d12 = (k2 + 0.5 - d6) / (d10 / 2.0);
				if (d12 * d12 < 1.0) {
					for (int l2 = j1; l2 <= i2; l2++) {
						double d13 = (l2 + 0.5 - d7) / (d11 / 2.0);
						if (d12 * d12 + d13 * d13 < 1.0) {
							for (int i3 = k1; i3 <= j2; i3++) {
								double d14 = (i3 + 0.5 - d8) / (d10 / 2.0);
								if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0 && level.getBlockState(p.set(k2, l2, i3)).is(Blocks.STONE)) {
									level.setBlock(p, ore, FLAGS);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * WorldGenSand (sand, radius 7; gravel, radius 6) and WorldGenClay (radius 4): a disk of {@code block} in the
	 * dirt and grass (clay: dirt and clay) around a water block.
	 */
	private static void disk(WorldGenLevel level, Random rand, int x, int y, int z, BlockState block, int radius, int half, boolean clay) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		if (!level.getBlockState(p.set(x, y, z)).is(Blocks.WATER)) {
			return;
		}
		int r = rand.nextInt(radius - 2) + 2;
		for (int i = x - r; i <= x + r; i++) {
			for (int j = z - r; j <= z + r; j++) {
				int dx = i - x;
				int dz = j - z;
				if (dx * dx + dz * dz <= r * r) {
					for (int k = y - half; k <= y + half; k++) {
						BlockState state = level.getBlockState(p.set(i, k, j));
						if (state.is(Blocks.DIRT) || (clay ? state.is(Blocks.CLAY) : state.is(Blocks.GRASS_BLOCK))) {
							level.setBlock(p, block, FLAGS);
						}
					}
				}
			}
		}
	}

	/** WorldGenFlowers (also the mushrooms): 64 tries around the spot, where the plant can stay. */
	private static void flowers(WorldGenLevel level, Random rand, int x, int y, int z, BlockState plant) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int l = 0; l < 64; l++) {
			int i = x + rand.nextInt(8) - rand.nextInt(8);
			int j = y + rand.nextInt(4) - rand.nextInt(4);
			int k = z + rand.nextInt(8) - rand.nextInt(8);
			p.set(i, j, k);
			if (isAir(level, p) && plant.canSurvive(level, p)) {
				level.setBlock(p, plant, FLAGS);
			}
		}
	}

	/** WorldGenTallGrass (short grass): down through air and leaves to the ground, then 128 tries around it. */
	private static void tallGrass(WorldGenLevel level, Random rand, int x, int y, int z) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		BlockState grass = Blocks.SHORT_GRASS.defaultBlockState();
		do {
			BlockState state = level.getBlockState(p.set(x, y, z));
			if (!state.isAir() && !state.is(BlockTags.LEAVES)) {
				break;
			}
			y--;
		} while (y > 0);
		for (int l = 0; l < 128; l++) {
			int i = x + rand.nextInt(8) - rand.nextInt(8);
			int j = y + rand.nextInt(4) - rand.nextInt(4);
			int k = z + rand.nextInt(8) - rand.nextInt(8);
			p.set(i, j, k);
			if (isAir(level, p) && grass.canSurvive(level, p)) {
				level.setBlock(p, grass, FLAGS);
			}
		}
	}

	/** WorldGenReeds: 20 tries next to water, two to four canes tall. */
	private static void reeds(WorldGenLevel level, Random rand, int x, int y, int z) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		BlockState cane = Blocks.SUGAR_CANE.defaultBlockState();
		for (int l = 0; l < 20; l++) {
			int i = x + rand.nextInt(4) - rand.nextInt(4);
			int k = z + rand.nextInt(4) - rand.nextInt(4);
			if (isAir(level, p.set(i, y, k)) && (water(level, p, i - 1, y - 1, k) || water(level, p, i + 1, y - 1, k)
				|| water(level, p, i, y - 1, k - 1) || water(level, p, i, y - 1, k + 1))) {
				int tall = 2 + rand.nextInt(rand.nextInt(3) + 1);
				for (int n = 0; n < tall; n++) {
					p.set(i, y + n, k);
					if (cane.canSurvive(level, p)) {
						level.setBlock(p, cane, FLAGS);
					}
				}
			}
		}
	}

	private static boolean water(WorldGenLevel level, BlockPos.MutableBlockPos p, int x, int y, int z) {
		return level.getBlockState(p.set(x, y, z)).is(Blocks.WATER);
	}

	/** WorldGenPumpkin: 64 tries on grass. */
	private static void pumpkins(WorldGenLevel level, Random rand, int x, int y, int z) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos below = new BlockPos.MutableBlockPos();
		for (int l = 0; l < 64; l++) {
			int i = x + rand.nextInt(8) - rand.nextInt(8);
			int j = y + rand.nextInt(4) - rand.nextInt(4);
			int k = z + rand.nextInt(8) - rand.nextInt(8);
			if (isAir(level, p.set(i, j, k)) && level.getBlockState(below.set(i, j - 1, k)).is(Blocks.GRASS_BLOCK)) {
				rand.nextInt(4); // its facing; 26.3's plain pumpkin has none
				level.setBlock(p, Blocks.PUMPKIN.defaultBlockState(), FLAGS);
			}
		}
	}

	/** WorldGenLiquids: a spring where stone surrounds it on three sides with air on the fourth. */
	private static void spring(WorldGenLevel level, int x, int y, int z, BlockState fluid) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		if (!stone(level, p, x, y + 1, z) || !stone(level, p, x, y - 1, z)) {
			return;
		}
		BlockState here = level.getBlockState(p.set(x, y, z));
		if (!here.isAir() && !here.is(Blocks.STONE)) {
			return;
		}
		int stone = 0;
		if (stone(level, p, x - 1, y, z)) stone++;
		if (stone(level, p, x + 1, y, z)) stone++;
		if (stone(level, p, x, y, z - 1)) stone++;
		if (stone(level, p, x, y, z + 1)) stone++;
		int air = 0;
		if (isAir(level, p.set(x - 1, y, z))) air++;
		if (isAir(level, p.set(x + 1, y, z))) air++;
		if (isAir(level, p.set(x, y, z - 1))) air++;
		if (isAir(level, p.set(x, y, z + 1))) air++;
		if (stone == 3 && air == 1) {
			BlockPos pos = new BlockPos(x, y, z);
			level.setBlock(pos, fluid, FLAGS);
			level.scheduleTick(pos, fluid.getFluidState().is(FluidTags.LAVA) ? Fluids.LAVA : Fluids.WATER, 0);
		}
	}

	private static boolean stone(WorldGenLevel level, BlockPos.MutableBlockPos p, int x, int y, int z) {
		return level.getBlockState(p.set(x, y, z)).is(Blocks.STONE);
	}

	// ---- OreSpawnWorld.generate, the DimensionID3 branch ----

	static void oreSpawnWorld(WorldGenLevel level, ChunkPos chunk) {
		// GameRegistry.generateWorld's random for the chunk, as Forge 1.7.10 made it (">> 2 + 1L" shifts by 3).
		long seed = level.getSeed();
		Random random = new Random(seed);
		long xSeed = random.nextLong() >> 2 + 1L;
		long zSeed = random.nextLong() >> 2 + 1L;
		random.setSeed(xSeed * chunk.x() + zSeed * chunk.z() ^ seed);
		int x = chunk.getMinBlockX();
		int z = chunk.getMinBlockZ();
		mosquitos(level, random, x, z);
		// addAnts(..., 4): the biome's orespawn:ant_nests feature (red_freq 4).
		appleTrees(level, random, x, z);
		OreSpawnDungeons.addGenericDungeon(level, chunk, RandomSource.create(random.nextLong()));
		if (OriginalDungeons.recentlyPlaced() == 0) {
			OreSpawnDungeons.addDamselInDistress(level, chunk, RandomSource.create(random.nextLong()));
		}
		if (OriginalDungeons.recentlyPlaced() == 0) {
			OreSpawnDungeons.addSpiderHangout(level, chunk, RandomSource.create(random.nextLong()));
		}
		if (OriginalDungeons.recentlyPlaced() == 0) {
			OreSpawnDungeons.addRedAntHangout(level, chunk, RandomSource.create(random.nextLong()));
		}
	}

	/** An OreSpawn block another group owns, by id; air while it isn't registered. */
	private static Block block(String id) {
		return BuiltInRegistries.BLOCK.getValue(OreSpawn.id(id));
	}

	/** addMosquitos: one chunk in 25, then (in Village Mania) one in 3, two tries on grass, scanning down from y 100 through air. */
	private static void mosquitos(WorldGenLevel level, Random random, int x, int z) {
		if (random.nextInt(25) != 0 || random.nextInt(3) != 0) {
			return;
		}
		Block plant = block("mosquito_plant");
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int i = 0; i < 2; i++) {
			int posX = x + random.nextInt(16);
			int posZ = z + random.nextInt(16);
			for (int posY = 100; posY > 40 && isAir(level, p.set(posX, posY, posZ)); posY--) {
				if (level.getBlockState(p.set(posX, posY - 1, posZ)).is(Blocks.GRASS_BLOCK)) {
					if (plant != Blocks.AIR) {
						level.setBlock(p.set(posX, posY, posZ), plant.defaultBlockState(), FLAGS);
					}
					break;
				}
			}
		}
	}

	/**
	 * addAppleTrees: one chunk in 15 + (|chunk x| + |chunk z|) % 15, two or more trees on grass, eight in ten apple
	 * trees, else cherry or peach (ItemAppleSeed.makeTree).
	 */
	private static void appleTrees(WorldGenLevel level, Random random, int x, int z) {
		int freq = Math.abs(x / 16) + Math.abs(z / 16);
		int howmany = 2;
		freq %= 15;
		howmany += random.nextInt(2 + (15 - freq) / 2);
		int which = random.nextInt(10);
		if (random.nextInt(15 + freq) != 0) {
			return;
		}
		String leavesId = which < 8 ? "apple_leaves" : which == 8 ? "cherry_leaves" : "peach_leaves";
		Block leaves = block(leavesId);
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int i = 0; i < howmany; i++) {
			int posX = 2 + x + random.nextInt(12);
			int posZ = 2 + z + random.nextInt(12);
			for (int posY = 100; posY > 50 && isAir(level, p.set(posX, posY, posZ)); posY--) {
				if (level.getBlockState(p.set(posX, posY - 1, posZ)).is(Blocks.GRASS_BLOCK)) {
					if (leaves != Blocks.AIR) {
						makeTree(level, posX, posY - 1, posZ, leaves, leavesId);
					}
					break;
				}
			}
		}
	}

	/** ItemAppleSeed.makeTree on grass, dirt or farmland: an oak trunk, two crosses of branches, a square canopy. */
	private static void makeTree(WorldGenLevel level, int x, int y, int z, Block leaves, String leavesId) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		BlockState ground = level.getBlockState(p.set(x, y, z));
		if (!ground.is(Blocks.GRASS_BLOCK) && !ground.is(Blocks.DIRT) && !ground.is(Blocks.FARMLAND)) {
			return;
		}
		int h1 = 12;
		int h2 = 6;
		int h3 = 9;
		int h4 = 6;
		int h5 = 14;
		int w1 = 5;
		int w2 = 3;
		if (leavesId.equals("peach_leaves")) {
			h1 = 10;
			h2 = 5;
			h3 = 7;
			h4 = 5;
			h5 = 12;
			w1 = 4;
			w2 = 2;
		}
		if (leavesId.equals("cherry_leaves")) {
			h1 = 8;
			h2 = 3;
			h3 = 5;
			h4 = 3;
			h5 = 10;
			w1 = 3;
			w2 = 1;
		}
		BlockState log = Blocks.OAK_LOG.defaultBlockState();
		for (int j = 1; j < h1; j++) {
			set(level, p, x, y + j, z, log);
		}
		for (int i = 1; i < w1; i++) {
			set(level, p, x + i, y + h2, z, log);
		}
		for (int i = 1; i < w1; i++) {
			set(level, p, x - i, y + h2, z, log);
		}
		for (int i = 1; i < w1; i++) {
			set(level, p, x, y + h2, z + i, log);
		}
		for (int i = 1; i < w1; i++) {
			set(level, p, x, y + h2, z - i, log);
		}
		for (int i = 1; i < w2; i++) {
			set(level, p, x + i, y + h3, z, log);
		}
		for (int i = 1; i < w2; i++) {
			set(level, p, x - i, y + h3, z, log);
		}
		for (int i = 1; i < w2; i++) {
			set(level, p, x, y + h3, z + i, log);
		}
		for (int i = 1; i < w2; i++) {
			set(level, p, x, y + h3, z - i, log);
		}
		BlockState leaf = leaves.defaultBlockState();
		for (int i = h4; i < h5; i++) {
			int width = 6;
			if (i > 8) {
				width = 5;
			}
			if (i > 10) {
				width = 4;
			}
			if (!leavesId.equals("apple_leaves")) {
				width--;
			}
			for (int dz = -width; dz <= width; dz++) {
				for (int dx = -width; dx <= width; dx++) {
					if (isAir(level, p.set(x + dx, y + i, z + dz))) {
						set(level, p, x + dx, y + i, z + dz, leaf);
					}
				}
			}
		}
	}

	/** setBlock with flag 2 below y 256 (OreSpawn's setBlockSuperFast refused y 256 and above). */
	private static void set(WorldGenLevel level, BlockPos.MutableBlockPos p, int x, int y, int z, BlockState state) {
		if (y >= 0 && y < 256) {
			level.setBlock(p.set(x, y, z), state, FLAGS);
		}
	}
}
