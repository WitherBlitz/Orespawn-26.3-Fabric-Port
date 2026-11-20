package orespawn.world.chaos;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.registry.ModBlocks;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * OreSpawn's {@code ChunkOreGenerator.generateOresInChunk}, which ChunkProviderOreSpawn6 ran once on every new Chaos
 * chunk, before the chunk existed (OreSpawn's config defaults, LessOre 0). Veins only replace stone, and only inside the
 * chunk being made: OreSpawn tried them at 3-12 into the chunk but centred them 8 further on, so they land in the
 * chunk's last few blocks along x and z or are cut off at its edge. That's kept, as is everything else.
 * <p>
 * Egg ores are found by OreSpawn's texture name in {@link ModBlocks#EGG_ORES}, whatever id their group gave them; the
 * troll blocks (Portals group) by id. A block that isn't registered yet places nothing, but its vein still draws the
 * same random numbers.
 */
final class ChaosOres {
	/** The seven egg ores ChunkOreGenerator picks 7 times in 104: OreSpawn texture names. */
	private static final String[] RARE_EGGS = {"orebrutalfly", "orenastysaurus", "orepointysaurus", "orecricket", "orefrog",
		"orespiderdriver", "orecrab"};
	/** Its other 98, in the order of its switch. */
	private static final String[] EGGS = {"orespider", "orebat", "orecow", "orepig", "oresquid", "orechicken", "orecreeper",
		"oreskeleton", "orezombie", "oreslime", "oreghast", "orezombiepigman", "oreenderman", "orecavespider", "oresilverfish",
		"oremagmacube", "orewitch", "oresheep", "orewolf", "oremooshroom", "oreocelot", "oreblaze", "orewitherskeleton",
		"oreenderdragon", "oresnowgolem", "oreirongolem", "orewitherboss", "oregirlfriend", "oreredcow", "oregoldcow",
		"oreenchantedcow", "oreMOTHRA", "orealosaurus", "orecryolophosaurus", "orecamarasaurus", "orevelocityraptor",
		"orehydrolisc", "orebasilisc", "oredragonfly", "oreemperorscorpion", "orescorpion", "orecavefisher", "orespyro",
		"orebaryonyx", "oregammametroid", "orecockateil", "orekyuubi", "orealien", "oreattacksquid", "orewaterdragon",
		"orekraken", "orelizard", "orecephadrome", "oredragon", "orebee", "orehorse", "oretrooper", "orespit", "orestink",
		"oreostrich", "oregazelle", "orechipmunk", "orecreepinghorror", "oreterribleterror", "orecliffracer", "oretriffid",
		"orenightmare", "orelurkingterror", "oregodzillapart", "oregodzilla", "oresmallworm", "oremediumworm", "orelargeworm",
		"orecassowary", "orecloudshark", "oregoldfish", "oreleafmonster", "oretshirt", "oreenderknight", "oreenderreaper",
		"orebeaver", "oretrex", "orehercules", "oremantis", "orestinky", "oreboyfriend", "orethekingpart", "oreeasterbunny",
		"orecaterkiller", "oremolenoid", "oreseamonster", "oreseaviper", "oreleon", "orehammerhead", "orerubberducky",
		"orevillager", "orecriminal", "orethequeenpart"};

	/** OreSpawnMain.get_orestats defaults: tries a chunk, vein size, lowest and highest y. */
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

	private static volatile @Nullable Map<String, Block> eggsByTexture;

	private ChaosOres() {
	}

	private static @Nullable Block egg(String texture) {
		Map<String, Block> map = eggsByTexture;
		if (map == null) {
			map = new HashMap<>();
			for (ModBlocks.EggOre egg : ModBlocks.EGG_ORES) {
				map.put(egg.texture(), egg.block());
			}
			eggsByTexture = map;
		}
		return map.get(texture);
	}

	private static @Nullable Block block(String id) {
		Identifier key = Identifier.fromNamespaceAndPath("orespawn", id);
		return BuiltInRegistries.BLOCK.containsKey(key) ? BuiltInRegistries.BLOCK.getValue(key) : null;
	}

	/**
	 * generateOresInChunk on a chunk's blocks ({@code states}, indexed as {@link ChaosTerrain#index}, null for air).
	 * {@code random} stands for OreSpawn's world.rand.
	 */
	static void generate(BlockState[] states, Random random, int chunkX, int chunkZ) {
		int baseX = chunkX * 16;
		int baseZ = chunkZ * 16;
		int patchy = SPAWN_ORES.rate + random.nextInt(30);
		if (random.nextInt(20) == 0) {
			patchy += 30;
		}
		for (int i = 0; i < patchy; i++) {
			int x = 3 + baseX + random.nextInt(10);
			int y = random.nextInt(128);
			int z = 3 + baseZ + random.nextInt(10);
			if (y <= SPAWN_ORES.max && y >= SPAWN_ORES.min) {
				Block b = random.nextInt(104) < 7 ? egg(RARE_EGGS[random.nextInt(7)]) : egg(EGGS[random.nextInt(98)]);
				vein(states, random, x, y, z, chunkX, chunkZ, b, SPAWN_ORES.clump);
			}
		}
		tries(states, random, chunkX, chunkZ, URANIUM, 9, ModBlocks.URANIUM_ORE);
		tries(states, random, chunkX, chunkZ, TITANIUM, 9, ModBlocks.TITANIUM_ORE);
		tries(states, random, chunkX, chunkZ, AMETHYST, 12, ModBlocks.AMETHYST_ORE);
		tries(states, random, chunkX, chunkZ, SALT, 9, ModBlocks.SALT_ORE);
		trolls(states, random, chunkX, chunkZ, block("red_ant_troll_block"));
		trolls(states, random, chunkX, chunkZ, block("termite_troll_block"));
		fixed(states, random, chunkX, chunkZ, DIAMOND, Blocks.DIAMOND_ORE);
		fixed(states, random, chunkX, chunkZ, BLOCK_DIAMOND, Blocks.DIAMOND_BLOCK);
		fixed(states, random, chunkX, chunkZ, EMERALD, Blocks.EMERALD_ORE);
		fixed(states, random, chunkX, chunkZ, BLOCK_EMERALD, Blocks.EMERALD_BLOCK);
		fixed(states, random, chunkX, chunkZ, GOLD, Blocks.GOLD_ORE);
		fixed(states, random, chunkX, chunkZ, BLOCK_GOLD, Blocks.GOLD_BLOCK);
		fixed(states, random, chunkX, chunkZ, BLOCK_RUBY, ModBlocks.RUBY_BLOCK);
	}

	/** Uranium, titanium, amethyst and salt: rate + rand(extra) tries. */
	private static void tries(BlockState[] states, Random random, int chunkX, int chunkZ, Ore ore, int extra, Block block) {
		int patchy = ore.rate + random.nextInt(extra);
		for (int i = 0; i < patchy; i++) {
			place(states, random, chunkX, chunkZ, ore.min, ore.max, block, ore.clump);
		}
	}

	/** RedAntTroll and TermiteTroll: 4-7 tries of veins of 4 at y 5-50. */
	private static void trolls(BlockState[] states, Random random, int chunkX, int chunkZ, @Nullable Block block) {
		int patchy = 4 + random.nextInt(4);
		for (int i = 0; i < patchy; i++) {
			place(states, random, chunkX, chunkZ, 5, 50, block, 4);
		}
	}

	/** The vanilla ores and the gem blocks: exactly rate tries. */
	private static void fixed(BlockState[] states, Random random, int chunkX, int chunkZ, Ore ore, Block block) {
		for (int i = 0; i < ore.rate; i++) {
			place(states, random, chunkX, chunkZ, ore.min, ore.max, block, ore.clump);
		}
	}

	private static void place(BlockState[] states, Random random, int chunkX, int chunkZ, int min, int max, @Nullable Block block, int clump) {
		int x = 3 + chunkX * 16 + random.nextInt(10);
		int y = random.nextInt(128);
		int z = 3 + chunkZ * 16 + random.nextInt(10);
		if (y <= max && y >= min) {
			vein(states, random, x, y, z, chunkX, chunkZ, block, clump);
		}
	}

	/** ChunkOreGenerator.generateBlockOre: 1.7.10's WorldGenMinable vein, written into this chunk only. */
	private static void vein(BlockState[] states, Random random, int par3, int par4, int par5, int chunkX, int chunkZ, @Nullable Block block,
			int numberOfBlocks) {
		BlockState ore = block == null ? null : block.defaultBlockState();
		float f = random.nextFloat() * (float) Math.PI;
		double d0 = par3 + 8 + Mth.sin(f) * numberOfBlocks / 8.0F;
		double d1 = par3 + 8 - Mth.sin(f) * numberOfBlocks / 8.0F;
		double d2 = par5 + 8 + Mth.cos(f) * numberOfBlocks / 8.0F;
		double d3 = par5 + 8 - Mth.cos(f) * numberOfBlocks / 8.0F;
		double d4 = par4 + random.nextInt(3) - 2;
		double d5 = par4 + random.nextInt(3) - 2;
		for (int l = 0; l <= numberOfBlocks; l++) {
			double d6 = d0 + (d1 - d0) * l / numberOfBlocks;
			double d7 = d4 + (d5 - d4) * l / numberOfBlocks;
			double d8 = d2 + (d3 - d2) * l / numberOfBlocks;
			double d9 = random.nextDouble() * numberOfBlocks / 16.0;
			double d10 = (Mth.sin(l * (float) Math.PI / numberOfBlocks) + 1.0F) * d9 + 1.0;
			double d11 = (Mth.sin(l * (float) Math.PI / numberOfBlocks) + 1.0F) * d9 + 1.0;
			int i1 = Mth.floor(d6 - d10 / 2.0);
			int j1 = Mth.floor(d7 - d11 / 2.0);
			int k1 = Mth.floor(d8 - d10 / 2.0);
			int l1 = Mth.floor(d6 + d10 / 2.0);
			int i2 = Mth.floor(d7 + d11 / 2.0);
			int j2 = Mth.floor(d8 + d10 / 2.0);
			for (int k2 = i1; k2 <= l1; k2++) {
				double d12 = (k2 + 0.5 - d6) / (d10 / 2.0);
				if (d12 * d12 < 1.0) {
					for (int l2 = j1; l2 <= i2; l2++) {
						double d13 = (l2 + 0.5 - d7) / (d11 / 2.0);
						if (d12 * d12 + d13 * d13 < 1.0) {
							for (int i3 = k1; i3 <= j2; i3++) {
								double d14 = (i3 + 0.5 - d8) / (d10 / 2.0);
								if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0 && ore != null && (k2 >> 4) == chunkX && (i3 >> 4) == chunkZ
										&& l2 >= 0 && l2 < ChaosTerrain.HEIGHT) {
									int index = ChaosTerrain.index(k2 & 15, l2, i3 & 15);
									BlockState here = states[index];
									if (here != null && here.is(Blocks.STONE)) {
										states[index] = ore;
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
