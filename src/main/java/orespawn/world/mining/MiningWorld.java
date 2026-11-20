package orespawn.world.mining;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.Fluids;
import orespawn.OreSpawn;
import orespawn.entity.tools.RockFeature;
import orespawn.registry.ModBlocks;
import orespawn.world.dungeons.OreSpawnDungeons;
import orespawn.original.OriginalDungeons;

import java.util.Optional;

/**
 * The Mining dimension's branch of OreSpawn's {@code OreSpawnWorld.generate} (DimensionID2), run on each chunk after the
 * chunk generator's own decoration, as Forge ran IWorldGenerators: generateRuby three times (ruby ore on the stone under
 * lava), 45 lapis veins of 7 and 25 of 4 below y 50, a dungeon (one chunk in 95, when no other structure was placed
 * lately: OreSpawn's Basilisk maze, Kyuubi dungeon, bee hive, shadow dungeon, alien WTF dungeon, Ender Knight dungeon
 * or Leon nest; otherwise its generic dungeon, one chunk in 16), addLavaAndWater, ant nests twice (one red, rainbow,
 * unstable or termite nest in two), mosquito plants twice, wild vegetables and loose rocks. Defaults of OreSpawn's
 * config (LessOre 0, LessLag 0, everything enabled).
 * <p>
 * The dungeons are the Dungeons group's ({@link OreSpawnDungeons}); it also counts OreSpawn's recently_placed down for
 * every new chunk. The ant nests are the Portals group's feature type ({@code orespawn:ant_nests}), placed here through
 * {@code orespawn:mining_ant_nests}. The loose rocks are the Tools group's {@link RockFeature} (OreSpawnWorld.addRocks).
 * Other groups' blocks are looked up by id and skipped while they're missing.
 */
final class MiningWorld {
	private static final ResourceKey<PlacedFeature> ANT_NESTS = ResourceKey.create(Registries.PLACED_FEATURE, OreSpawn.id("mining_ant_nests"));

	private MiningWorld() {
	}

	/** GameRegistry.generateWorld's random for one chunk: what Forge gave each IWorldGenerator. */
	static RandomSource random(long seed, int chunkX, int chunkZ) {
		RandomSource fml = new LegacyRandomSource(seed);
		// "nextLong() >> 2 + 1L" in Forge: a shift by 3.
		long xSeed = fml.nextLong() >> 3;
		long zSeed = fml.nextLong() >> 3;
		fml.setSeed(xSeed * chunkX + zSeed * chunkZ ^ seed);
		return fml;
	}

	static void generate(WorldGenLevel level, ChunkGenerator generator, int cx, int cz) {
		RandomSource random = random(level.getSeed(), cx, cz);
		ChunkPos chunk = new ChunkPos(cx, cz);
		int chunkX = cx * 16;
		int chunkZ = cz * 16;
		for (int i = 0; i < 3; i++) {
			generateRuby(level, random, chunkX, chunkZ);
		}
		MiningOres.OreRandom r = MiningOres.OreRandom.of(random);
		BlockState lapis = Blocks.LAPIS_ORE.defaultBlockState();
		for (int i = 0; i < 45; i++) {
			int x = chunkX + random.nextInt(16);
			int y = random.nextInt(128);
			int z = chunkZ + random.nextInt(16);
			if (y < 50) {
				MiningOres.minable(level, r, x, y, z, lapis, 7, Blocks.STONE);
			}
		}
		for (int i = 0; i < 25; i++) {
			int x = chunkX + random.nextInt(16);
			int y = random.nextInt(128);
			int z = chunkZ + random.nextInt(16);
			if (y < 50) {
				MiningOres.minable(level, r, x, y, z, lapis, 4, Blocks.STONE);
			}
		}
		if (OriginalDungeons.recentlyPlaced() == 0 && random.nextInt(95) == 1) {
			switch (random.nextInt(7)) {
				case 0 -> OreSpawnDungeons.addBasiliskMaze(level, chunk, random);
				case 1 -> OreSpawnDungeons.addKyuubiDungeon(level, chunk, random);
				case 2 -> OreSpawnDungeons.addBeeHive(level, chunk, random);
				case 3 -> OreSpawnDungeons.addShadowDungeon(level, chunk, random);
				case 4 -> OreSpawnDungeons.addAlienWTF(level, chunk, random);
				case 5 -> OreSpawnDungeons.addEnderKnight(level, chunk, random);
				default -> OreSpawnDungeons.addLeonNest(level, chunk, random);
			}
		} else {
			OreSpawnDungeons.addGenericDungeon(level, chunk, random);
		}
		addLavaAndWater(level, random, chunkX, chunkZ);
		Optional<PlacedFeature> ants = level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE).getOptional(ANT_NESTS);
		BlockPos origin = new BlockPos(chunkX, 0, chunkZ);
		for (int i = 0; i < 2; i++) {
			ants.ifPresent(feature -> feature.place(level, generator, random, origin));
		}
		BlockState mosquito = MiningOres.orespawn("mosquito_plant");
		addMosquitos(level, random, chunkX, chunkZ, mosquito);
		addMosquitos(level, random, chunkX, chunkZ, mosquito);
		addVeggies(level, random, chunkX, chunkZ);
		// addRocks: the Tools group's feature, with its one-in-five roll.
		new RockFeature().place(level, generator, random, origin);
	}

	private static boolean isLava(BlockState state) {
		return state.is(Blocks.LAVA);
	}

	/** generateRuby: 10-16 tries; each looks down its column from y 0-50 for lava over stone, and the stone becomes ruby ore. */
	private static void generateRuby(WorldGenLevel level, RandomSource random, int chunkX, int chunkZ) {
		BlockState ruby = ModBlocks.RUBY_ORE.defaultBlockState();
		int patchy = 10 + random.nextInt(7);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int i = 0; i < patchy; i++) {
			int x = 3 + chunkX + random.nextInt(10);
			int y = random.nextInt(128);
			int z = 3 + chunkZ + random.nextInt(10);
			if (y <= 50 && y >= 0) {
				for (int m = y; m > 5; m--) {
					if (isLava(level.getBlockState(pos.set(x, m, z)))) {
						if (level.getBlockState(pos.set(x, m - 1, z)).is(Blocks.STONE)) {
							level.setBlock(pos, ruby, 2);
							break;
						}
					}
				}
			}
		}
	}

	private static boolean dirtOrStone(BlockState state) {
		return state.is(Blocks.DIRT) || state.is(Blocks.STONE);
	}

	/**
	 * addLavaAndWater: one chunk in five, up to six columns; the first grass block found from y 128 down to 76 (through
	 * air only), on dirt or stone, with air and ground beside it, becomes a little spring, water or lava, three deep.
	 */
	private static void addLavaAndWater(WorldGenLevel level, RandomSource random, int chunkX, int chunkZ) {
		if (random.nextInt(5) != 0) {
			return;
		}
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int i = 0; i < 6; i++) {
			int x = chunkX + random.nextInt(16);
			int z = chunkZ + random.nextInt(16);
			for (int y = 128; y > 75 && level.getBlockState(pos.set(x, y, z)).isAir(); y--) {
				if (level.getBlockState(pos.set(x, y - 1, z)).is(Blocks.GRASS_BLOCK)) {
					if (!dirtOrStone(level.getBlockState(pos.set(x, y - 2, z)))) {
						break;
					}
					int air = 0;
					int nonAir = 0;
					for (BlockPos side : new BlockPos[] {new BlockPos(x + 1, y - 1, z), new BlockPos(x - 1, y - 1, z), new BlockPos(x, y - 1, z + 1),
						new BlockPos(x, y - 1, z - 1)}) {
						BlockState state = level.getBlockState(side);
						if (state.isAir()) {
							air++;
						}
						if (dirtOrStone(state) || state.is(Blocks.GRASS_BLOCK)) {
							nonAir++;
						}
					}
					if (air != 0 && nonAir != 0) {
						// The top block was the flowing kind, so it spreads; the two below are still.
						boolean water = random.nextInt(2) == 0;
						BlockState fluid = water ? Blocks.WATER.defaultBlockState() : Blocks.LAVA.defaultBlockState();
						BlockPos top = new BlockPos(x, y, z);
						level.setBlock(top, fluid, 3);
						level.setBlock(top.below(), fluid, 3);
						level.setBlock(top.below(2), fluid, 3);
						level.scheduleTick(top, water ? Fluids.WATER : Fluids.LAVA, 0);
						return;
					}
					break;
				}
			}
		}
	}

	/** addMosquitos (the Mining dimension needs no biome check): one chunk in 25, two tries at grass from y 100 to 41. */
	private static void addMosquitos(WorldGenLevel level, RandomSource random, int chunkX, int chunkZ, BlockState plant) {
		if (random.nextInt(25) != 0) {
			return;
		}
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int i = 0; i < 2; i++) {
			int x = chunkX + random.nextInt(16);
			int z = chunkZ + random.nextInt(16);
			for (int y = 100; y > 40 && level.getBlockState(pos.set(x, y, z)).isAir(); y--) {
				if (level.getBlockState(pos.set(x, y - 1, z)).is(Blocks.GRASS_BLOCK)) {
					if (plant != null) {
						level.setBlock(pos.set(x, y, z), plant, 2);
					}
					break;
				}
			}
		}
	}

	private static BlockState firstBlock(String... ids) {
		for (String id : ids) {
			BlockState state = MiningOres.orespawn(id);
			if (state != null) {
				return state;
			}
		}
		return null;
	}

	/**
	 * addVeggies: one chunk in 15, eight tries at grass from y 100 to 41: carrots, potatoes, radishes or lettuce (newly
	 * planted), one time in ten a melon stem, or one time in fifty a duplicator tree log.
	 */
	private static void addVeggies(WorldGenLevel level, RandomSource random, int chunkX, int chunkZ) {
		if (random.nextInt(15) != 0) {
			return;
		}
		BlockState radish = MiningOres.orespawn("radish_plant");
		BlockState lettuce = MiningOres.orespawn("lettuce_plant");
		BlockState duplicator = firstBlock("duplicator_tree_wood", "duplicator_tree_log");
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int i = 0; i < 8; i++) {
			int x = chunkX + random.nextInt(16);
			int z = chunkZ + random.nextInt(16);
			for (int y = 100; y > 40 && level.getBlockState(pos.set(x, y, z)).isAir(); y--) {
				if (level.getBlockState(pos.set(x, y - 1, z)).is(Blocks.GRASS_BLOCK)) {
					pos.set(x, y, z);
					int what = random.nextInt(6);
					BlockState state = null;
					if (what == 0) {
						state = Blocks.CARROTS.defaultBlockState();
					} else if (what == 1) {
						state = Blocks.POTATOES.defaultBlockState();
					} else if (what == 2) {
						state = radish;
					} else if (what == 3) {
						state = lettuce;
					} else if (what == 4) {
						if (random.nextInt(10) == 0) {
							state = Blocks.MELON_STEM.defaultBlockState();
						}
					} else if (random.nextInt(50) == 1) {
						state = duplicator;
					}
					if (state != null) {
						level.setBlock(pos, state, 2);
					}
					break;
				}
			}
		}
	}
}
