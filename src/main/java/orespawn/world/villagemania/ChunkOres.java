package orespawn.world.villagemania;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.OreSpawn;
import orespawn.registry.ModBlocks;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * OreSpawn's {@code ChunkOreGenerator} as ChunkProviderOreSpawn3 ran it on each new chunk's block array, before the chunk
 * existed (so a vein only fills the part of itself inside the chunk): egg ores, uranium, titanium, amethyst, salt, the
 * red ant and termite troll blocks, diamond, emerald and gold ore, and diamond, emerald, gold and ruby blocks, all in
 * stone. Defaults of OreSpawn's config (LessOre 0).
 * <p>
 * Egg ores are found by OreSpawn's texture name in {@link ModBlocks#EGG_ORES}, so they work whatever id their group gave
 * them; the troll blocks are looked up by id. A block that isn't registered yet places nothing, but its vein still
 * draws the same random numbers, so the rest of the chunk stays as OreSpawn made it.
 */
final class ChunkOres {
	/** The seven rarer egg ores (7 in 104 of the egg-ore tries), in ChunkOreGenerator's switch order. */
	private static final String[] RARE_EGGS = {"orebrutalfly", "orenastysaurus", "orepointysaurus", "orecricket", "orefrog",
		"orespiderdriver", "orecrab"};
	/** The 98 other egg ores, in its switch order. */
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

	/** OreSpawnMain.get_orestats defaults: tries per chunk, vein size, lowest and highest y. */
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

	private static volatile @Nullable Map<String, BlockState> eggOres;

	private ChunkOres() {
	}

	/** The egg ore with OreSpawn's texture name, or null while its creature's group isn't ported. */
	private static @Nullable BlockState egg(String texture) {
		Map<String, BlockState> map = eggOres;
		if (map == null) {
			map = new HashMap<>();
			for (ModBlocks.EggOre ore : ModBlocks.EGG_ORES) {
				map.put(ore.texture(), ore.block().defaultBlockState());
			}
			eggOres = map;
		}
		return map.get(texture);
	}

	/** An OreSpawn block another group owns, by id, or null while it isn't registered. */
	private static @Nullable BlockState byId(String id) {
		Block block = BuiltInRegistries.BLOCK.getValue(OreSpawn.id(id));
		return block == Blocks.AIR ? null : block.defaultBlockState();
	}

	/** ChunkOreGenerator.generateOresInChunk. */
	static void generate(BlockState[] blocks, Random random, int cx, int cz) {
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
				BlockState egg = random.nextInt(104) < 7 ? egg(RARE_EGGS[random.nextInt(7)]) : egg(EGGS[random.nextInt(98)]);
				vein(blocks, random, cx, cz, x, y, z, egg, SPAWN_ORES.clump);
			}
		}
		tries(blocks, random, cx, cz, URANIUM, 9, ModBlocks.URANIUM_ORE.defaultBlockState());
		tries(blocks, random, cx, cz, TITANIUM, 9, ModBlocks.TITANIUM_ORE.defaultBlockState());
		tries(blocks, random, cx, cz, AMETHYST, 12, ModBlocks.AMETHYST_ORE.defaultBlockState());
		tries(blocks, random, cx, cz, SALT, 9, ModBlocks.SALT_ORE.defaultBlockState());
		troll(blocks, random, cx, cz, byId("red_ant_troll_block"));
		troll(blocks, random, cx, cz, byId("termite_troll_block"));
		fixed(blocks, random, cx, cz, DIAMOND, Blocks.DIAMOND_ORE.defaultBlockState());
		fixed(blocks, random, cx, cz, BLOCK_DIAMOND, Blocks.DIAMOND_BLOCK.defaultBlockState());
		fixed(blocks, random, cx, cz, EMERALD, Blocks.EMERALD_ORE.defaultBlockState());
		fixed(blocks, random, cx, cz, BLOCK_EMERALD, Blocks.EMERALD_BLOCK.defaultBlockState());
		fixed(blocks, random, cx, cz, GOLD, Blocks.GOLD_ORE.defaultBlockState());
		fixed(blocks, random, cx, cz, BLOCK_GOLD, Blocks.GOLD_BLOCK.defaultBlockState());
		fixed(blocks, random, cx, cz, BLOCK_RUBY, ModBlocks.RUBY_BLOCK.defaultBlockState());
	}

	/** rate + nextInt(extra) tries. */
	private static void tries(BlockState[] blocks, Random random, int cx, int cz, Ore ore, int extra, BlockState state) {
		int patchy = ore.rate + random.nextInt(extra);
		for (int i = 0; i < patchy; i++) {
			int x = 3 + cx * 16 + random.nextInt(10);
			int y = random.nextInt(128);
			int z = 3 + cz * 16 + random.nextInt(10);
			if (y <= ore.max && y >= ore.min) {
				vein(blocks, random, cx, cz, x, y, z, state, ore.clump);
			}
		}
	}

	/** The troll blocks: 4 to 7 tries, y 5 to 50, veins of 4. */
	private static void troll(BlockState[] blocks, Random random, int cx, int cz, @Nullable BlockState state) {
		int patchy = 4 + random.nextInt(4);
		for (int i = 0; i < patchy; i++) {
			int x = 3 + cx * 16 + random.nextInt(10);
			int y = random.nextInt(128);
			int z = 3 + cz * 16 + random.nextInt(10);
			if (y <= 50 && y >= 5) {
				vein(blocks, random, cx, cz, x, y, z, state, 4);
			}
		}
	}

	/** Exactly rate tries (the LessOre-0 ores). */
	private static void fixed(BlockState[] blocks, Random random, int cx, int cz, Ore ore, BlockState state) {
		for (int i = 0; i < ore.rate; i++) {
			int x = 3 + cx * 16 + random.nextInt(10);
			int y = random.nextInt(128);
			int z = 3 + cz * 16 + random.nextInt(10);
			if (y <= ore.max && y >= ore.min) {
				vein(blocks, random, cx, cz, x, y, z, state, ore.clump);
			}
		}
	}

	/** ChunkOreGenerator.generateBlockOre (1.7.10's WorldGenMinable shape), clipped to the chunk, replacing stone. */
	private static void vein(BlockState[] blocks, Random random, int cx, int cz, int x, int y, int z, @Nullable BlockState ore, int size) {
		float f = random.nextFloat() * (float) Math.PI;
		double d0 = x + 8 + Legacy.sin(f) * size / 8.0F;
		double d1 = x + 8 - Legacy.sin(f) * size / 8.0F;
		double d2 = z + 8 + Legacy.cos(f) * size / 8.0F;
		double d3 = z + 8 - Legacy.cos(f) * size / 8.0F;
		double d4 = y + random.nextInt(3) - 2;
		double d5 = y + random.nextInt(3) - 2;
		for (int l = 0; l <= size; l++) {
			double d6 = d0 + (d1 - d0) * l / size;
			double d7 = d4 + (d5 - d4) * l / size;
			double d8 = d2 + (d3 - d2) * l / size;
			double d9 = random.nextDouble() * size / 16.0;
			double d10 = (Legacy.sin(l * (float) Math.PI / size) + 1.0F) * d9 + 1.0;
			double d11 = (Legacy.sin(l * (float) Math.PI / size) + 1.0F) * d9 + 1.0;
			if (ore == null) {
				continue;
			}
			int i1 = Legacy.floor(d6 - d10 / 2.0);
			int j1 = Legacy.floor(d7 - d11 / 2.0);
			int k1 = Legacy.floor(d8 - d10 / 2.0);
			int l1 = Legacy.floor(d6 + d10 / 2.0);
			int i2 = Legacy.floor(d7 + d11 / 2.0);
			int j2 = Legacy.floor(d8 + d10 / 2.0);
			for (int k2 = i1; k2 <= l1; k2++) {
				double d12 = (k2 + 0.5 - d6) / (d10 / 2.0);
				if (d12 * d12 < 1.0 && k2 >> 4 == cx) {
					for (int l2 = j1; l2 <= i2; l2++) {
						double d13 = (l2 + 0.5 - d7) / (d11 / 2.0);
						if (d12 * d12 + d13 * d13 < 1.0 && l2 >= 0 && l2 <= 255) {
							for (int i3 = k1; i3 <= j2; i3++) {
								double d14 = (i3 + 0.5 - d8) / (d10 / 2.0);
								if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0 && i3 >> 4 == cz) {
									int index = (k2 & 15) << 12 | (i3 & 15) << 8 | l2;
									if (blocks[index] == VillageManiaTerrain.STONE) {
										blocks[index] = ore;
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
