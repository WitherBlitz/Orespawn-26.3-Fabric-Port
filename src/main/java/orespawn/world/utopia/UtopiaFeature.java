package orespawn.world.utopia;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import orespawn.world.dungeons.OreSpawnDungeons;
import orespawn.original.OriginalDungeons;
import org.jspecify.annotations.Nullable;

import java.util.Random;

/**
 * {@code orespawn:utopia}: OreSpawnWorld.generate's DimensionID branch, once per new Utopia chunk, after the biome's
 * plants as in 1.7.10 (where it ran after the chunk's populate):
 * <ol>
 * <li>generateSurface: strawberries (1 in 20), corn (1 in 35), tomatoes (1 in 70), vegetables (1 in 15: carrots,
 * potatoes, radishes, lettuce, now and then a melon stem or a Duplicator Tree Wood log), butterfly, moth and firefly
 * plants (1 in 10) and mosquito
 * plants (1 in 75), each on grass under open air between y 41 and 100. Its ant nests are the Portals group's
 * {@code orespawn:ant_nests} feature (red_freq 4), placed right after this one.</li>
 * <li>Unless the chunk grows a huge tree ({@link UtopiaStructure}): a grove of apple, cherry or peach trees; if no
 * grove and no wind or sky trees, The King's or The Queen's altar (1 in 2000, once {@code recently_placed} has run
 * out); and another round of vegetables.</li>
 * <li>A Ruby Bird dungeon by lava (1 in 15), else a mob dungeon (1 in 16).</li>
 * </ol>
 * The altar and dungeons are the Dungeons group's ({@link OreSpawnDungeons}: OreSpawn's own code, run from the original
 * OreSpawn 1.7.10 file). The crops, critter plants and duplicator log are the Farming group's blocks,
 * looked up by id; while one isn't registered, that spot stays empty. The plant and tree decisions use FML's per-chunk
 * random, as OreSpawn's did.
 */
public record UtopiaFeature() implements Feature {
	public static final MapCodec<UtopiaFeature> CODEC = MapCodec.unit(UtopiaFeature::new);
	/** MyStrawberryPlant (huge trees grow through it). */
	static final String STRAWBERRY_PLANT = "strawberry_plant";

	@Override
	public MapCodec<UtopiaFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int cx = origin.getX() >> 4;
		int cz = origin.getZ() >> 4;
		ChunkPos chunk = new ChunkPos(cx, cz);
		int x = cx * 16;
		int z = cz * 16;
		Random r = fmlRandom(level.getSeed(), cx, cz);
		// OreSpawnWorld.recently_placed counts down once per chunk made, in every dimension.
		int recent = OriginalDungeons.recentlyPlaced();
		if (recent > 0) {
			OriginalDungeons.setRecentlyPlaced(recent - 1);
		}
		LevelWorld w = new LevelWorld(level, random);
		generateSurface(w, r, x, z);
		UtopiaPlan plan = chunkGenerator instanceof UtopiaChunkGenerator utopia
			? UtopiaPlan.of(utopia.terrain(level.getSeed()), level.getSeed(), cx, cz) : null;
		if (plan == null || plan.huge() == null) {
			if (plan != null) {
				HugeTrees.Fruit fruit = plan.fruitKind() < 8 ? HugeTrees.Fruit.APPLE : plan.fruitKind() == 8 ? HugeTrees.Fruit.CHERRY
					: HugeTrees.Fruit.PEACH;
				for (int[] spot : plan.fruit()) {
					int y = surface(w, spot[0], 100, 50, spot[1]);
					if (y >= 0) {
						HugeTrees.fruitTree(w, spot[0], y - 1, spot[1], fruit);
					}
				}
				if (plan.fruit().isEmpty() && plan.others().isEmpty() && OriginalDungeons.recentlyPlaced() == 0) {
					OreSpawnDungeons.addKingAltar(level, chunk, random);
				}
			}
			addVeggies(w, r, x, z);
		}
		if (!OreSpawnDungeons.addRubyDungeon(level, chunk, random)) {
			OreSpawnDungeons.addGenericDungeon(level, chunk, random);
		}
		return true;
	}

	/** The random FML's GameRegistry.generateWorld gave OreSpawnWorld for a chunk ({@code >> 2 + 1L} shifts by 3, as FML wrote it). */
	static Random fmlRandom(long worldSeed, int chunkX, int chunkZ) {
		Random random = new Random(worldSeed);
		long xSeed = random.nextLong() >> 2 + 1L;
		long zSeed = random.nextLong() >> 2 + 1L;
		random.setSeed((xSeed * chunkX + zSeed * chunkZ) ^ worldSeed);
		return random;
	}

	/** OreSpawn's search: down from {@code top} through air only; the first y with grass below it, or -1. */
	private static int surface(GenWorld w, int x, int top, int bottom, int z) {
		for (int y = top; y > bottom && w.isAir(x, y, z); y--) {
			if (w.block(x, y - 1, z) == Blocks.GRASS_BLOCK) {
				return y;
			}
		}
		return -1;
	}

	private static @Nullable BlockState farm(String id) {
		return Lookup.state(null, id);
	}

	private static void generateSurface(GenWorld w, Random r, int x, int z) {
		addStrawberries(w, r, x, z);
		addCorn(w, r, x, z);
		addTomatoes(w, r, x, z);
		addVeggies(w, r, x, z);
		addButterfliesAndMoths(w, r, x, z);
		addMosquitos(w, r, x, z);
	}

	private static void addStrawberries(GenWorld w, Random r, int x, int z) {
		if (r.nextInt(20) != 0) {
			return;
		}
		for (int i = 0; i < 5; i++) {
			int posX = x + r.nextInt(16);
			int posZ = z + r.nextInt(16);
			int y = surface(w, posX, 100, 40, posZ);
			if (y >= 0) {
				w.set(posX, y, posZ, farm(STRAWBERRY_PLANT));
			}
		}
	}

	private static boolean clearAbove(GenWorld w, int x, int y, int z) {
		for (int i = 1; i < 10; i++) {
			if (!w.isAir(x, y + i, z)) {
				return false;
			}
		}
		return true;
	}

	/** Corn and tomatoes: OreSpawn's Plant1 is the top, Plant2 a young base, Plant3 a ripe base, Plant4 the stalk between. */
	private static void stalk(GenWorld w, int x, int y, int z, String id, int height, int ripeBase) {
		if (height == 1) {
			w.set(x, y, z, Lookup.stage(id, 1));
			return;
		}
		if (height == 2) {
			w.set(x, y, z, Lookup.stage(id, 2));
			w.set(x, y + 1, z, Lookup.stage(id, 1));
			return;
		}
		w.set(x, y, z, Lookup.stage(id, ripeBase));
		for (int i = 1; i < height; i++) {
			w.set(x, y + i, z, Lookup.stage(id, 4));
		}
		w.set(x, y + height, z, Lookup.stage(id, 1));
	}

	private static void addCorn(GenWorld w, Random r, int x, int z) {
		if (r.nextInt(35) != 1) {
			return;
		}
		for (int j = 0; j < 6; j++) {
			int posX = x + r.nextInt(16);
			int posZ = z + r.nextInt(16);
			int y = surface(w, posX, 100, 40, posZ);
			if (y >= 0 && clearAbove(w, posX, y, posZ)) {
				// OreSpawn put Plant2 (not Plant3) under a tall corn stalk.
				stalk(w, posX, y, posZ, "corn_plant", r.nextInt(5) + 1, 2);
			}
		}
	}

	private static void addTomatoes(GenWorld w, Random r, int x, int z) {
		if (r.nextInt(70) != 1) {
			return;
		}
		for (int j = 0; j < 5; j++) {
			int posX = x + r.nextInt(16);
			int posZ = z + r.nextInt(16);
			int y = surface(w, posX, 100, 40, posZ);
			if (y >= 0 && clearAbove(w, posX, y, posZ)) {
				stalk(w, posX, y, posZ, "tomato_plant", r.nextInt(3) + 1, 3);
			}
		}
	}

	private static void addVeggies(GenWorld w, Random r, int x, int z) {
		if (r.nextInt(15) != 0) {
			return;
		}
		for (int i = 0; i < 8; i++) {
			int posX = x + r.nextInt(16);
			int posZ = z + r.nextInt(16);
			int y = surface(w, posX, 100, 40, posZ);
			if (y < 0) {
				continue;
			}
			switch (r.nextInt(6)) {
				case 0 -> w.set(posX, y, posZ, Blocks.CARROTS.defaultBlockState());
				case 1 -> w.set(posX, y, posZ, Blocks.POTATOES.defaultBlockState());
				case 2 -> w.set(posX, y, posZ, farm("radish_plant"));
				case 3 -> w.set(posX, y, posZ, Lookup.stage("lettuce_plant", 1));
				case 4 -> {
					if (r.nextInt(10) == 0) {
						w.set(posX, y, posZ, Blocks.MELON_STEM.defaultBlockState());
					}
				}
				default -> {
					// OreSpawn's enableduplicatortree is on by default.
					if (r.nextInt(50) == 1) {
						w.set(posX, y, posZ, farm("duplicator_tree_wood"));
					}
				}
			}
		}
	}

	private static void addButterfliesAndMoths(GenWorld w, Random r, int x, int z) {
		if (r.nextInt(10) != 0) {
			return;
		}
		for (int i = 0; i < 4; i++) {
			int posX = x + r.nextInt(16);
			int posZ = z + r.nextInt(16);
			int y = surface(w, posX, 100, 40, posZ);
			if (y >= 0) {
				String plant = switch (r.nextInt(3)) {
					case 0 -> "butterfly_plant";
					case 1 -> "moth_plant";
					default -> "firefly_plant";
				};
				w.set(posX, y, posZ, farm(plant));
			}
		}
	}

	private static void addMosquitos(GenWorld w, Random r, int x, int z) {
		// 1 in 25, and in Utopia only one time in three of those.
		if (r.nextInt(25) != 0 || r.nextInt(3) != 0) {
			return;
		}
		for (int i = 0; i < 2; i++) {
			int posX = x + r.nextInt(16);
			int posZ = z + r.nextInt(16);
			int y = surface(w, posX, 100, 40, posZ);
			if (y >= 0) {
				w.set(posX, y, posZ, farm("mosquito_plant"));
			}
		}
	}
}
