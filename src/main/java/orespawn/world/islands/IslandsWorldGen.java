package orespawn.world.islands;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.OreSpawn;
import orespawn.world.dungeons.McWorld;
import orespawn.original.OriginalDungeons;
import org.jspecify.annotations.Nullable;

import java.util.Random;

/**
 * OreSpawnWorld.generate's DimensionID4 branch: what OreSpawn added to each new chunk of its Islands dimension, run as
 * the chunk is decorated ({@link IslandsChunkGenerator#applyBiomeDecoration}). Per chunk, in OreSpawn's order and with
 * its odds (default config):
 * <ul>
 * <li>A big structure: once 50 chunks have been made since the last one ({@code recently_placed}), 1 in 100, if nothing
 * but air, logs or tree leaves stands at y 11 over the 65x55 blocks around (D4BigSpaceCheck). Then one of 19: a castle
 * (3 in 19, OreSpawn's two kinds alike), a mob dungeon (4), the Ender castle, Inca pyramid, robot lab, mini dungeon,
 * Ruby Bird dungeon, Cephadrome altar, greenhouse, Nightmare rookery, Stinky house, White House, pumpkin or rainbow
 * (1 each). Each looks for its own spot and room as OreSpawn's add* did and may still give up.</li>
 * <li>1 in 300: a Cloud Shark dungeon in the sky, at y 150-159.</li>
 * <li>1 in 30: up to three Unstable Ant Nests in place of the grass (OreSpawnWorld.addUnstableAnts).</li>
 * <li>1 in 10: an Island Block on the grass, which later raises the floating islands (addIslands).</li>
 * <li>1 in 7: 3 to 12 Rocks dropped on the grass (addD4Rocks).</li>
 * </ul>
 * The structures are the Dungeons group's ports of OreSpawn's add* methods (run from the original OreSpawn file by {@link OriginalDungeons}), run through
 * {@link McWorld}, which keeps what reaches past the chunks being decorated until those chunks load. OreSpawn's own
 * random for each chunk is rebuilt exactly (FML's GameRegistry.generateWorld seeding), so a seed gives the same
 * decisions. Blocks and creatures other groups own are looked up by id; while one isn't registered, that part is
 * skipped.
 * <p>
 * Differences: chunks are made several at a time now, so the {@code recently_placed} wait between structures is kept as
 * well as it can be (two chunks made at the same moment can both start one). D4BigSpaceCheck can only see the chunks
 * around the one being decorated; past them it sees the plain terrain (air at y 11), as McWorld's reads do.
 */
public final class IslandsWorldGen {
	/** OreSpawn's config, at its defaults. */
	private static final int LESS_LAG = 0;
	private static final boolean UNSTABLE_ANT_ENABLE = true;
	private static final boolean ROCK_ENABLE = true;

	private IslandsWorldGen() {
	}

	/** OreSpawnWorld.generate for one new chunk of the Islands (chunk coordinates). */
	static void generate(WorldGenLevel level, int chunkX, int chunkZ) {
		Random random = fmlRandom(level.getSeed(), chunkX, chunkZ);
		// OreSpawnWorld.recently_placed counts down once per chunk made, in every dimension (shared with the Dungeons group).
		int recent = OriginalDungeons.recentlyPlaced();
		if (recent > 0) {
			OriginalDungeons.setRecentlyPlaced(recent - 1);
		}

		int x = chunkX * 16;
		int z = chunkZ * 16;
		if (OriginalDungeons.recentlyPlaced() == 0 && random.nextInt(100) == 0 && bigSpaceCheck(level, x, IslandsChunkGenerator.GRASS_Y, z)) {
			int i = random.nextInt(19);
			McWorld.run(level, level.getRandom(), world -> switch (i) {
				case 0, 1, 2 -> OriginalDungeons.place("addD4Castle", world, random, x, z);
				case 3, 4, 5, 6 -> OriginalDungeons.place("addD4GenericDungeon", world, random, x, z);
				case 7 -> OriginalDungeons.place("addD4EnderCastle", world, random, x, z);
				case 8 -> OriginalDungeons.place("addD4IncaPyramid", world, random, x, z);
				case 9 -> OriginalDungeons.place("addD4RobotLab", world, random, x, z);
				case 10 -> OriginalDungeons.place("addD4Mini", world, random, x, z);
				case 11 -> OriginalDungeons.place("addD4RubyDungeon", world, random, x, z);
				case 12 -> OriginalDungeons.place("addD4CephadromeAltar", world, random, x, z);
				case 13 -> OriginalDungeons.place("addD4Greenhouse", world, random, x, z);
				case 14 -> OriginalDungeons.place("addD4NightmareRookery", world, random, x, z);
				case 15 -> OriginalDungeons.place("addD4StinkyHouse", world, random, x, z);
				case 16 -> OriginalDungeons.place("addD4WhiteHouse", world, random, x, z);
				case 17 -> OriginalDungeons.place("addPumpkin", world, random, x, z);
				default -> OriginalDungeons.place("addD4Rainbow", world, random, x, z);
			});
		}

		if (random.nextInt(300) == 0) {
			McWorld.run(level, level.getRandom(), world -> OriginalDungeons.place("addD4CloudShark", world, random, x, z));
		}

		addUnstableAnts(level, random, x, z);
		addIslands(level, random, x, z);
		addD4Rocks(level, random, x, z);
	}

	/**
	 * The random FML's GameRegistry.generateWorld handed each IWorldGenerator for a chunk. ({@code >> 2 + 1L} is a shift
	 * by 3, as FML wrote it.)
	 */
	static Random fmlRandom(long worldSeed, int chunkX, int chunkZ) {
		Random random = new Random(worldSeed);
		long xSeed = random.nextLong() >> 2 + 1L;
		long zSeed = random.nextLong() >> 2 + 1L;
		random.setSeed((xSeed * chunkX + zSeed * chunkZ) ^ worldSeed);
		return random;
	}

	/**
	 * OreSpawnWorld.D4BigSpaceCheck: at y {@code posY + 4}, over x -25..39 and z -25..29 around the chunk's corner, only
	 * air, logs, Apple Tree Leaves or Scary Tree Leaves. Decoration may only look at the chunk being decorated and the
	 * eight around it; columns further out count as the plain terrain there (air).
	 */
	private static boolean bigSpaceCheck(WorldGenLevel level, int posX, int posY, int posZ) {
		int centerX = posX >> 4;
		int centerZ = posZ >> 4;
		Block apple = IslandsChunkGenerator.appleLeaves().getBlock();
		@Nullable Block scary = IslandsChunkGenerator.scaryLeaves();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int i = -25; i < 40; i++) {
			for (int k = -25; k < 30; k++) {
				int x = posX + i;
				int z = posZ + k;
				if (Math.abs((x >> 4) - centerX) > 1 || Math.abs((z >> 4) - centerZ) > 1) continue;
				BlockState s = level.getBlockState(pos.set(x, posY + 4, z));
				if (!s.isAir() && !IslandsChunkGenerator.isLog(s) && !s.is(apple) && (scary == null || !s.is(scary))) {
					return false;
				}
			}
		}
		return true;
	}

	/** OreSpawnWorld.addUnstableAnts: 1 in 30, three tries at turning the grass under open air into an Unstable Ant Nest. */
	private static void addUnstableAnts(WorldGenLevel level, Random random, int chunkX, int chunkZ) {
		if (!UNSTABLE_ANT_ENABLE || random.nextInt(30) != 0) return;
		@Nullable Block nest = block("unstable_ant_nest");
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int i = 0; i < 3; i++) {
			int posX = chunkX + random.nextInt(16);
			int posZ = chunkZ + random.nextInt(16);
			for (int posY = 20; posY > 2 && level.isEmptyBlock(pos.set(posX, posY, posZ)); posY--) {
				if (level.getBlockState(pos.set(posX, posY - 1, posZ)).is(Blocks.GRASS_BLOCK)) {
					if (nest != null) setBlockFast(level, pos, nest.defaultBlockState());
					break;
				}
			}
		}
	}

	/** OreSpawnWorld.addIslands: 1 in 10, an Island Block on the grass under open air, somewhere in the chunk's middle 12x12. */
	private static void addIslands(WorldGenLevel level, Random random, int chunkX, int chunkZ) {
		int posX = 2 + chunkX + random.nextInt(12);
		int posZ = 2 + chunkZ + random.nextInt(12);
		if (random.nextInt(10 + LESS_LAG * 2) != 1) return;
		@Nullable Block island = block("island_block");
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int posY = 20; posY > 2 && level.isEmptyBlock(pos.set(posX, posY, posZ)); posY--) {
			if (level.getBlockState(pos.set(posX, posY - 1, posZ)).is(Blocks.GRASS_BLOCK)) {
				if (island != null) setBlockFast(level, pos.set(posX, posY, posZ), island.defaultBlockState());
				break;
			}
		}
	}

	/** OreSpawnWorld.addD4Rocks: 1 in 7, 3 to 12 Rocks, each on the grass under open air (y 20 down to 6). */
	private static void addD4Rocks(WorldGenLevel level, Random random, int chunkX, int chunkZ) {
		if (random.nextInt(7) != 0 || !ROCK_ENABLE) return;
		@Nullable EntityType<?> rock = BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id("rock")).orElse(null);
		int howmany = 3 + random.nextInt(10);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int i = 0; i < howmany; i++) {
			int posX = chunkX + random.nextInt(16);
			int posZ = chunkZ + random.nextInt(16);
			for (int posY = 20; posY > 5 && level.getBlockState(pos.set(posX, posY, posZ)).is(Blocks.AIR); posY--) {
				if (level.getBlockState(pos.set(posX, posY - 1, posZ)).is(Blocks.GRASS_BLOCK)) {
					if (rock != null) spawnCreature(level, rock, posX, posY, posZ);
					break;
				}
			}
		}
	}

	/**
	 * OreSpawnWorld.spawnCreature: the creature half a block out from the block's corner, away from 0 (so at negative
	 * coordinates it lands half a block into the next block over, as in OreSpawn), 0.01 above the ground, facing a random
	 * way. No onSpawnWithEgg: it starts as a plain new creature. OreSpawn also played its living sound; that's left out,
	 * since decoration runs off the server thread, where sounds can't be sent.
	 */
	private static void spawnCreature(WorldGenLevel level, EntityType<?> type, double x, double y, double z) {
		Entity entity = type.create(level.getLevel(), EntitySpawnReason.CHUNK_GENERATION);
		if (entity == null) return;
		if (x > 0.0) x += 0.5;
		if (x < 0.0) x -= 0.5;
		if (z > 0.0) z += 0.5;
		if (z < 0.0) z -= 0.5;
		entity.snapTo(x, y + 0.01, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
		level.addFreshEntity(entity);
	}

	/** OreSpawnMain.setBlockFast with flags 2: no neighbour updates. */
	private static void setBlockFast(WorldGenLevel level, BlockPos pos, BlockState state) {
		level.setBlock(pos, state, Block.UPDATE_CLIENTS);
	}

	/** A block another group registers, or null until it does. */
	private static @Nullable Block block(String id) {
		return BuiltInRegistries.BLOCK.getOptional(OreSpawn.id(id)).orElse(null);
	}
}
