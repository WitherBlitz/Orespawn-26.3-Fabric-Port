package orespawn.world.mining;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.OreSpawn;
import orespawn.registry.ModBlocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * OreSpawn's {@code ChunkOreGenerator} (what its Mining dimension's chunk provider ran three times on every new chunk,
 * before the chunk existed), and 1.7.10's {@code WorldGenMinable} (the vein shape both use) for veins placed in the
 * world afterwards. Defaults of OreSpawn's config: LessOre 0.
 * <p>
 * Egg ores are found by OreSpawn's texture name in {@link ModBlocks#EGG_ORES}, so they work whatever id their group gave
 * them; ores from other groups (the troll blocks) are looked up by id. A block that isn't registered yet places
 * nothing, but its vein still draws the same random numbers, so the rest of the chunk stays as OreSpawn made it.
 */
public final class MiningOres {
	/** ChunkOreGenerator's seven rarer egg ores (7 in 104 of the egg-ore tries): OreSpawn texture names. */
	private static final String[] RARE_EGGS = {"orebrutalfly", "orenastysaurus", "orepointysaurus", "orecricket", "orefrog",
		"orespiderdriver", "orecrab"};
	/** ChunkOreGenerator's 98 other egg ores, in its switch order: OreSpawn texture names. */
	private static final String[] EGGS = {"orespider", "orebat", "orecow", "orepig", "oresquid", "orechicken", "orecreeper", "oreskeleton",
		"orezombie", "oreslime", "oreghast", "orezombiepigman", "oreenderman", "orecavespider", "oresilverfish", "oremagmacube", "orewitch",
		"oresheep", "orewolf", "oremooshroom", "oreocelot", "oreblaze", "orewitherskeleton", "oreenderdragon", "oresnowgolem",
		"oreirongolem", "orewitherboss", "oregirlfriend", "oreredcow", "oregoldcow", "oreenchantedcow", "oreMOTHRA", "orealosaurus",
		"orecryolophosaurus", "orecamarasaurus", "orevelocityraptor", "orehydrolisc", "orebasilisc", "oredragonfly", "oreemperorscorpion",
		"orescorpion", "orecavefisher", "orespyro", "orebaryonyx", "oregammametroid", "orecockateil", "orekyuubi", "orealien",
		"oreattacksquid", "orewaterdragon", "orekraken", "orelizard", "orecephadrome", "oredragon", "orebee", "orehorse", "oretrooper",
		"orespit", "orestink", "oreostrich", "oregazelle", "orechipmunk", "orecreepinghorror", "oreterribleterror", "orecliffracer",
		"oretriffid", "orenightmare", "orelurkingterror", "oregodzillapart", "oregodzilla", "oresmallworm", "oremediumworm",
		"orelargeworm", "orecassowary", "orecloudshark", "oregoldfish", "oreleafmonster", "oretshirt", "oreenderknight",
		"oreenderreaper", "orebeaver", "oretrex", "orehercules", "oremantis", "orestinky", "oreboyfriend", "orethekingpart",
		"oreeasterbunny", "orecaterkiller", "oremolenoid", "oreseamonster", "oreseaviper", "oreleon", "orehammerhead", "orerubberducky",
		"orevillager", "orecriminal", "orethequeenpart"};

	/** OreSpawn's ore settings (OreSpawnMain.get_orestats defaults): tries per pass, vein size, lowest and highest y. */
	private record Ore(int rate, int clump, int min, int max) {
	}

	private static final Ore SPAWN_ORES = new Ore(28, 4, 50, 128);
	private static final Ore URANIUM = new Ore(3, 4, 0, 30);
	private static final Ore TITANIUM = new Ore(3, 4, 0, 20);
	private static final Ore AMETHYST = new Ore(2, 6, 0, 25);
	private static final Ore SALT = new Ore(5, 12, 50, 128);
	private static final Ore DIAMOND = new Ore(4, 6, 0, 30);
	private static final Ore BLOCK_DIAMOND = new Ore(2, 4, 0, 20);
	private static final Ore EMERALD = new Ore(4, 6, 0, 40);
	private static final Ore BLOCK_EMERALD = new Ore(2, 4, 0, 20);
	private static final Ore GOLD = new Ore(4, 8, 0, 40);
	private static final Ore BLOCK_GOLD = new Ore(2, 4, 0, 25);
	private static final Ore BLOCK_RUBY = new Ore(1, 2, 0, 15);

	private static volatile Map<String, BlockState> eggOres;

	private MiningOres() {
	}

	/** The random numbers WorldGenMinable draws, from a java.util.Random or a RandomSource (the same sequence for a seed). */
	interface OreRandom {
		float nextFloat();

		int nextInt(int bound);

		double nextDouble();

		static OreRandom of(Random random) {
			return new OreRandom() {
				@Override
				public float nextFloat() {
					return random.nextFloat();
				}

				@Override
				public int nextInt(int bound) {
					return random.nextInt(bound);
				}

				@Override
				public double nextDouble() {
					return random.nextDouble();
				}
			};
		}

		static OreRandom of(RandomSource random) {
			return new OreRandom() {
				@Override
				public float nextFloat() {
					return random.nextFloat();
				}

				@Override
				public int nextInt(int bound) {
					return random.nextInt(bound);
				}

				@Override
				public double nextDouble() {
					return random.nextDouble();
				}
			};
		}
	}

	/** A registered orespawn block's default state, or null if no group has registered it yet. */
	static BlockState orespawn(String id) {
		return BuiltInRegistries.BLOCK.getOptional(OreSpawn.id(id)).map(Block::defaultBlockState).orElse(null);
	}

	private static BlockState egg(String texture) {
		Map<String, BlockState> map = eggOres;
		if (map == null) {
			map = new HashMap<>();
			for (ModBlocks.EggOre egg : ModBlocks.EGG_ORES) {
				map.put(egg.texture(), egg.block().defaultBlockState());
			}
			eggOres = map;
		}
		return map.get(texture);
	}

	/** ChunkOreGenerator.generateOresInChunk, on a chunk's block array (only blocks inside the chunk change). */
	static void generateOresInChunk(Random random, int cx, int cz, BlockState[] chunk) {
		int chunkX = cx * 16;
		int chunkZ = cz * 16;
		int patchy = SPAWN_ORES.rate + random.nextInt(30);
		if (random.nextInt(20) == 0) {
			patchy += 30;
		}
		for (int i = 0; i < patchy; i++) {
			int x = 3 + chunkX + random.nextInt(10);
			int y = random.nextInt(128);
			int z = 3 + chunkZ + random.nextInt(10);
			if (y <= SPAWN_ORES.max && y >= SPAWN_ORES.min) {
				BlockState b;
				if (random.nextInt(104) < 7) {
					b = egg(RARE_EGGS[random.nextInt(7)]);
				} else {
					b = egg(EGGS[random.nextInt(98)]);
				}
				generateBlockOre(random, x, y, z, cx, cz, chunk, b, SPAWN_ORES.clump);
			}
		}
		simple(random, chunkX, chunkZ, cx, cz, chunk, URANIUM, 9, ModBlocks.URANIUM_ORE.defaultBlockState());
		simple(random, chunkX, chunkZ, cx, cz, chunk, TITANIUM, 9, ModBlocks.TITANIUM_ORE.defaultBlockState());
		simple(random, chunkX, chunkZ, cx, cz, chunk, AMETHYST, 12, ModBlocks.AMETHYST_ORE.defaultBlockState());
		simple(random, chunkX, chunkZ, cx, cz, chunk, SALT, 9, ModBlocks.SALT_ORE.defaultBlockState());
		troll(random, chunkX, chunkZ, cx, cz, chunk, orespawn("red_ant_troll_block"));
		troll(random, chunkX, chunkZ, cx, cz, chunk, orespawn("termite_troll_block"));
		// LessOre 0: the gem ores and blocks.
		fixed(random, chunkX, chunkZ, cx, cz, chunk, DIAMOND, Blocks.DIAMOND_ORE.defaultBlockState());
		fixed(random, chunkX, chunkZ, cx, cz, chunk, BLOCK_DIAMOND, Blocks.DIAMOND_BLOCK.defaultBlockState());
		fixed(random, chunkX, chunkZ, cx, cz, chunk, EMERALD, Blocks.EMERALD_ORE.defaultBlockState());
		fixed(random, chunkX, chunkZ, cx, cz, chunk, BLOCK_EMERALD, Blocks.EMERALD_BLOCK.defaultBlockState());
		fixed(random, chunkX, chunkZ, cx, cz, chunk, GOLD, Blocks.GOLD_ORE.defaultBlockState());
		fixed(random, chunkX, chunkZ, cx, cz, chunk, BLOCK_GOLD, Blocks.GOLD_BLOCK.defaultBlockState());
		fixed(random, chunkX, chunkZ, cx, cz, chunk, BLOCK_RUBY, ModBlocks.RUBY_BLOCK.defaultBlockState());
	}

	/** Uranium, titanium, amethyst and salt: rate + rand(extra) tries. */
	private static void simple(Random random, int chunkX, int chunkZ, int cx, int cz, BlockState[] chunk, Ore ore, int extra, BlockState block) {
		int patchy = ore.rate + random.nextInt(extra);
		for (int i = 0; i < patchy; i++) {
			int x = 3 + chunkX + random.nextInt(10);
			int y = random.nextInt(128);
			int z = 3 + chunkZ + random.nextInt(10);
			if (y <= ore.max && y >= ore.min) {
				generateBlockOre(random, x, y, z, cx, cz, chunk, block, ore.clump);
			}
		}
	}

	/** Red ant and termite troll blocks: 4-7 tries, veins of 4 at y 5-50. */
	private static void troll(Random random, int chunkX, int chunkZ, int cx, int cz, BlockState[] chunk, BlockState block) {
		int patchy = 4 + random.nextInt(4);
		for (int i = 0; i < patchy; i++) {
			int x = 3 + chunkX + random.nextInt(10);
			int y = random.nextInt(128);
			int z = 3 + chunkZ + random.nextInt(10);
			if (y <= 50 && y >= 5) {
				generateBlockOre(random, x, y, z, cx, cz, chunk, block, 4);
			}
		}
	}

	/** Diamond, emerald, gold and their blocks, and ruby blocks: exactly rate tries. */
	private static void fixed(Random random, int chunkX, int chunkZ, int cx, int cz, BlockState[] chunk, Ore ore, BlockState block) {
		for (int i = 0; i < ore.rate; i++) {
			int x = 3 + chunkX + random.nextInt(10);
			int y = random.nextInt(128);
			int z = 3 + chunkZ + random.nextInt(10);
			if (y <= ore.max && y >= ore.min) {
				generateBlockOre(random, x, y, z, cx, cz, chunk, block, ore.clump);
			}
		}
	}

	/** ChunkOreGenerator.generateBlockOre: WorldGenMinable's vein, replacing stone inside this chunk only. */
	private static void generateBlockOre(Random rand, int par3, int par4, int par5, int cx, int cz, BlockState[] chunk, BlockState newbid,
			int numberOfBlocks) {
		float f = rand.nextFloat() * (float) Math.PI;
		double d0 = par3 + 8 + Legacy1710.sin(f) * numberOfBlocks / 8.0F;
		double d1 = par3 + 8 - Legacy1710.sin(f) * numberOfBlocks / 8.0F;
		double d2 = par5 + 8 + Legacy1710.cos(f) * numberOfBlocks / 8.0F;
		double d3 = par5 + 8 - Legacy1710.cos(f) * numberOfBlocks / 8.0F;
		double d4 = par4 + rand.nextInt(3) - 2;
		double d5 = par4 + rand.nextInt(3) - 2;
		for (int l = 0; l <= numberOfBlocks; l++) {
			double d6 = d0 + (d1 - d0) * l / numberOfBlocks;
			double d7 = d4 + (d5 - d4) * l / numberOfBlocks;
			double d8 = d2 + (d3 - d2) * l / numberOfBlocks;
			double d9 = rand.nextDouble() * numberOfBlocks / 16.0;
			double d10 = (Legacy1710.sin(l * (float) Math.PI / numberOfBlocks) + 1.0F) * d9 + 1.0;
			double d11 = (Legacy1710.sin(l * (float) Math.PI / numberOfBlocks) + 1.0F) * d9 + 1.0;
			int i1 = Legacy1710.floor(d6 - d10 / 2.0);
			int j1 = Legacy1710.floor(d7 - d11 / 2.0);
			int k1 = Legacy1710.floor(d8 - d10 / 2.0);
			int l1 = Legacy1710.floor(d6 + d10 / 2.0);
			int i2 = Legacy1710.floor(d7 + d11 / 2.0);
			int j2 = Legacy1710.floor(d8 + d10 / 2.0);
			for (int k2 = i1; k2 <= l1; k2++) {
				double d12 = (k2 + 0.5 - d6) / (d10 / 2.0);
				if (d12 * d12 < 1.0) {
					for (int l2 = j1; l2 <= i2; l2++) {
						double d13 = (l2 + 0.5 - d7) / (d11 / 2.0);
						if (d12 * d12 + d13 * d13 < 1.0) {
							for (int i3 = k1; i3 <= j2; i3++) {
								double d14 = (i3 + 0.5 - d8) / (d10 / 2.0);
								// getBlockIDInChunk: air outside this chunk or y 0-255.
								if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0 && k2 >> 4 == cx && i3 >> 4 == cz && l2 >= 0 && l2 <= 255) {
									int index = (k2 & 15) << 12 | (i3 & 15) << 8 | l2;
									if (chunk[index] == MiningTerrain.STONE && newbid != null) {
										chunk[index] = newbid;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * WorldGenMinable.generate: a vein of {@code numberOfBlocks} around (x + 8, y, z + 8), replacing {@code target}
	 * (isReplaceableOreGen is "is that block" for everything here).
	 */
	static void minable(WorldGenLevel level, OreRandom rand, int x, int y, int z, BlockState ore, int numberOfBlocks, Block target) {
		float f = rand.nextFloat() * (float) Math.PI;
		double d0 = (float) (x + 8) + Legacy1710.sin(f) * numberOfBlocks / 8.0F;
		double d1 = (float) (x + 8) - Legacy1710.sin(f) * numberOfBlocks / 8.0F;
		double d2 = (float) (z + 8) + Legacy1710.cos(f) * numberOfBlocks / 8.0F;
		double d3 = (float) (z + 8) - Legacy1710.cos(f) * numberOfBlocks / 8.0F;
		double d4 = y + rand.nextInt(3) - 2;
		double d5 = y + rand.nextInt(3) - 2;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int l = 0; l <= numberOfBlocks; l++) {
			double d6 = d0 + (d1 - d0) * l / numberOfBlocks;
			double d7 = d4 + (d5 - d4) * l / numberOfBlocks;
			double d8 = d2 + (d3 - d2) * l / numberOfBlocks;
			double d9 = rand.nextDouble() * numberOfBlocks / 16.0;
			double d10 = (Legacy1710.sin(l * (float) Math.PI / numberOfBlocks) + 1.0F) * d9 + 1.0;
			double d11 = (Legacy1710.sin(l * (float) Math.PI / numberOfBlocks) + 1.0F) * d9 + 1.0;
			int i1 = Legacy1710.floor(d6 - d10 / 2.0);
			int j1 = Legacy1710.floor(d7 - d11 / 2.0);
			int k1 = Legacy1710.floor(d8 - d10 / 2.0);
			int l1 = Legacy1710.floor(d6 + d10 / 2.0);
			int i2 = Legacy1710.floor(d7 + d11 / 2.0);
			int j2 = Legacy1710.floor(d8 + d10 / 2.0);
			for (int k2 = i1; k2 <= l1; k2++) {
				double d12 = (k2 + 0.5 - d6) / (d10 / 2.0);
				if (d12 * d12 < 1.0) {
					for (int l2 = j1; l2 <= i2; l2++) {
						double d13 = (l2 + 0.5 - d7) / (d11 / 2.0);
						if (d12 * d12 + d13 * d13 < 1.0) {
							for (int i3 = k1; i3 <= j2; i3++) {
								double d14 = (i3 + 0.5 - d8) / (d10 / 2.0);
								if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0 && ore != null) {
									pos.set(k2, l2, i3);
									if (MiningPopulator.inWorld(level, pos) && level.getBlockState(pos).is(target)) {
										level.setBlock(pos, ore, 2);
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
