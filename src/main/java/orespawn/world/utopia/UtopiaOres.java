package orespawn.world.utopia;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import orespawn.registry.ModBlocks;
import org.jspecify.annotations.Nullable;

import java.util.Random;

/**
 * OreSpawn's {@code ChunkOreGenerator.generateOresInChunk}, which ChunkProviderOreSpawn ran on every new Utopia chunk
 * after its caves, with the chunk's own random (default config, LessOre 0): egg ores (7 in 104 of the tries one of seven
 * rarer ones), uranium, titanium, amethyst, salt, the red ant and termite troll blocks, diamond ore and blocks, emerald
 * ore and blocks, gold ore and blocks, and ruby blocks. Veins replace stone and stop at the chunk's edge, as 1.7.10's
 * in-chunk version did.
 * <p>
 * Egg ores are found by OreSpawn's texture name in {@link ModBlocks#EGG_ORES}, whichever group adds them; the troll
 * blocks are looked up by id. One that isn't registered places nothing but still draws its random numbers.
 */
final class UtopiaOres {
	private static final String[] RARE_EGGS = {"orebrutalfly", "orenastysaurus", "orepointysaurus", "orecricket", "orefrog",
		"orespiderdriver", "orecrab"};
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

	/** OreSpawnMain.get_orestats defaults: tries, vein size, lowest and highest y. */
	private record Ore(int rate, int clump, int min, int max) {}

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

	private UtopiaOres() {
	}

	private static @Nullable BlockState egg(String texture) {
		for (ModBlocks.EggOre egg : ModBlocks.EGG_ORES) {
			if (egg.texture().equals(texture)) {
				return egg.block().defaultBlockState();
			}
		}
		return null;
	}

	static void generate(Random random, ChunkAccess chunk) {
		int cx = chunk.getPos().x();
		int cz = chunk.getPos().z();
		int chunkX = cx * 16;
		int chunkZ = cz * 16;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int patchy = SPAWN_ORES.rate + random.nextInt(30);
		if (random.nextInt(20) == 0) {
			patchy += 30;
		}
		for (int i = 0; i < patchy; i++) {
			int x = 3 + chunkX + random.nextInt(10);
			int y = random.nextInt(128);
			int z = 3 + chunkZ + random.nextInt(10);
			if (y <= SPAWN_ORES.max && y >= SPAWN_ORES.min) {
				BlockState b = random.nextInt(104) < 7 ? egg(RARE_EGGS[random.nextInt(7)]) : egg(EGGS[random.nextInt(98)]);
				vein(random, x, y, z, chunk, pos, b, SPAWN_ORES.clump);
			}
		}
		simple(random, chunk, pos, URANIUM, 9, ModBlocks.URANIUM_ORE.defaultBlockState());
		simple(random, chunk, pos, TITANIUM, 9, ModBlocks.TITANIUM_ORE.defaultBlockState());
		simple(random, chunk, pos, AMETHYST, 12, ModBlocks.AMETHYST_ORE.defaultBlockState());
		simple(random, chunk, pos, SALT, 9, ModBlocks.SALT_ORE.defaultBlockState());
		troll(random, chunk, pos, Lookup.state(null, "red_ant_troll_block"));
		troll(random, chunk, pos, Lookup.state(null, "termite_troll_block"));
		fixed(random, chunk, pos, DIAMOND, Blocks.DIAMOND_ORE.defaultBlockState());
		fixed(random, chunk, pos, BLOCK_DIAMOND, Blocks.DIAMOND_BLOCK.defaultBlockState());
		fixed(random, chunk, pos, EMERALD, Blocks.EMERALD_ORE.defaultBlockState());
		fixed(random, chunk, pos, BLOCK_EMERALD, Blocks.EMERALD_BLOCK.defaultBlockState());
		fixed(random, chunk, pos, GOLD, Blocks.GOLD_ORE.defaultBlockState());
		fixed(random, chunk, pos, BLOCK_GOLD, Blocks.GOLD_BLOCK.defaultBlockState());
		fixed(random, chunk, pos, BLOCK_RUBY, ModBlocks.RUBY_BLOCK.defaultBlockState());
	}

	/** Uranium, titanium, amethyst and salt: rate + rand(extra) tries. */
	private static void simple(Random random, ChunkAccess chunk, BlockPos.MutableBlockPos pos, Ore ore, int extra, BlockState block) {
		int patchy = ore.rate + random.nextInt(extra);
		tries(random, chunk, pos, patchy, ore.min, ore.max, block, ore.clump);
	}

	/** The troll blocks: 4-7 tries, veins of 4 at y 5-50. */
	private static void troll(Random random, ChunkAccess chunk, BlockPos.MutableBlockPos pos, @Nullable BlockState block) {
		tries(random, chunk, pos, 4 + random.nextInt(4), 5, 50, block, 4);
	}

	/** The gem ores and blocks: exactly rate tries. */
	private static void fixed(Random random, ChunkAccess chunk, BlockPos.MutableBlockPos pos, Ore ore, BlockState block) {
		tries(random, chunk, pos, ore.rate, ore.min, ore.max, block, ore.clump);
	}

	private static void tries(Random random, ChunkAccess chunk, BlockPos.MutableBlockPos pos, int count, int min, int max, @Nullable BlockState block,
			int clump) {
		int chunkX = chunk.getPos().getMinBlockX();
		int chunkZ = chunk.getPos().getMinBlockZ();
		for (int i = 0; i < count; i++) {
			int x = 3 + chunkX + random.nextInt(10);
			int y = random.nextInt(128);
			int z = 3 + chunkZ + random.nextInt(10);
			if (y <= max && y >= min) {
				vein(random, x, y, z, chunk, pos, block, clump);
			}
		}
	}

	/** generateBlockOre: WorldGenMinable's vein around (x + 8, y, z + 8), replacing stone inside this chunk only. */
	private static void vein(Random rand, int par3, int par4, int par5, ChunkAccess chunk, BlockPos.MutableBlockPos pos, @Nullable BlockState newbid,
			int numberOfBlocks) {
		int cx = chunk.getPos().x();
		int cz = chunk.getPos().z();
		float f = rand.nextFloat() * (float) Math.PI;
		double d0 = par3 + 8 + Mth.sin(f) * numberOfBlocks / 8.0F;
		double d1 = par3 + 8 - Mth.sin(f) * numberOfBlocks / 8.0F;
		double d2 = par5 + 8 + Mth.cos(f) * numberOfBlocks / 8.0F;
		double d3 = par5 + 8 - Mth.cos(f) * numberOfBlocks / 8.0F;
		double d4 = par4 + rand.nextInt(3) - 2;
		double d5 = par4 + rand.nextInt(3) - 2;
		for (int l = 0; l <= numberOfBlocks; l++) {
			double d6 = d0 + (d1 - d0) * l / numberOfBlocks;
			double d7 = d4 + (d5 - d4) * l / numberOfBlocks;
			double d8 = d2 + (d3 - d2) * l / numberOfBlocks;
			double d9 = rand.nextDouble() * numberOfBlocks / 16.0;
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
				if (d12 * d12 >= 1.0) {
					continue;
				}
				for (int l2 = j1; l2 <= i2; l2++) {
					double d13 = (l2 + 0.5 - d7) / (d11 / 2.0);
					if (d12 * d12 + d13 * d13 >= 1.0) {
						continue;
					}
					for (int i3 = k1; i3 <= j2; i3++) {
						double d14 = (i3 + 0.5 - d8) / (d10 / 2.0);
						if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0 && newbid != null && k2 >> 4 == cx && i3 >> 4 == cz && l2 >= 0
							&& l2 < UtopiaTerrain.HEIGHT && chunk.getBlockState(pos.set(k2, l2, i3)).is(Blocks.STONE)) {
							chunk.setBlockState(pos, newbid);
						}
					}
				}
			}
		}
	}
}
