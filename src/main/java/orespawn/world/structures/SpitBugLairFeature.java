package orespawn.world.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import orespawn.OreSpawn;
import orespawn.world.structures.Build.Loot;

import java.util.List;

/**
 * OreSpawn's Spit Bug lair (OreSpawnWorld.addSpitBug, GenericDungeon.makeSpitBugLair): a swamp shrine of green and lime
 * terracotta (1.7.10's stained clay 13 and 5) with chiseled stone brick trim, a mossy arch topped with emerald ore over
 * three Spit Bug spawners, and a chest in the middle.
 */
public record SpitBugLairFeature() implements Feature {
	public static final MapCodec<SpitBugLairFeature> CODEC = MapCodec.unit(SpitBugLairFeature::new);

	/** GenericDungeon.SpitBugContentsList. */
	static final List<Loot> LOOT = List.of(Loot.of(Items.ROTTEN_FLESH, 6, 16, 35), Loot.of(Items.COD, 6, 16, 25),
		Loot.of(Items.BONE, 6, 16, 25), Loot.of(Items.STRING, 6, 16, 25), Loot.of("amethyst_pickaxe", 1, 1, 15),
		Loot.of("amethyst_shovel", 1, 1, 15), Loot.of("amethyst_hoe", 1, 1, 15), Loot.of("amethyst_axe", 1, 1, 15),
		Loot.of("amethyst_sword", 1, 1, 15), Loot.of("amethyst_chestplate", 1, 1, 15), Loot.of("amethyst_leggings", 1, 1, 15),
		Loot.of("amethyst_helmet", 1, 1, 15), Loot.of("amethyst_boots", 1, 1, 15), Loot.of("instant_survival_garden", 2, 4, 25),
		Loot.of("instant_survival_shelter", 2, 4, 25));

	@Override
	public MapCodec<SpitBugLairFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(final WorldGenLevel level, final ChunkGenerator generator, final RandomSource random, final BlockPos origin) {
		return Build.onSurface(level, random, origin, 4, 100, false, Build.GRASS, SpitBugLairFeature::build);
	}

	/** {@code y} is the air block above the grass. */
	static void build(WorldGenLevel level, RandomSource random, int x, int y, int z) {
		int width = 9;
		for (int i = 0; i < width; i++) {
			for (int side : new int[] {i, -i}) {
				Build.set(level, x + side, y + width - i + 2, z, Blocks.DYED_TERRACOTTA.green());
				Build.set(level, x + side, y + width - i + 1, z, Blocks.DYED_TERRACOTTA.green());
				Build.set(level, x + side, y + width - i, z, Blocks.MOSSY_COBBLESTONE);
			}
		}
		Build.set(level, x, y + width + 3, z, Blocks.EMERALD_ORE);
		Build.set(level, x, y + width + 2, z, Blocks.EMERALD_ORE);
		Build.set(level, x, y + width + 1, z, Blocks.EMERALD_ORE);
		Build.spawner(level, random, x, y + width, z, OreSpawn.id("spit_bug"));
		Build.spawner(level, random, x, y + width - 1, z, OreSpawn.id("spit_bug"));
		Build.spawner(level, random, x, y + width - 2, z, OreSpawn.id("spit_bug"));
		for (int i = 0; i < width; i++) {
			for (int j = -i; j <= i; j++) {
				int west = x - width + i + 1;
				int east = x + width - i - 1;
				Build.set(level, west, y, z + j, Blocks.DYED_TERRACOTTA.lime());
				Build.set(level, east, y, z + j, Blocks.DYED_TERRACOTTA.lime());
				if (j != -i && j != i) {
					Build.set(level, west, y + 1, z + j, Blocks.AIR);
					Build.set(level, east, y + 1, z + j, Blocks.AIR);
					Build.set(level, west, y + 2, z + j, Blocks.AIR);
					Build.set(level, east, y + 2, z + j, Blocks.AIR);
				} else {
					Build.set(level, west, y + 1, z + j, Blocks.DYED_TERRACOTTA.green());
					Build.set(level, east, y + 1, z + j, Blocks.DYED_TERRACOTTA.green());
					Build.set(level, west, y + 2, z + j, Blocks.CHISELED_STONE_BRICKS);
					Build.set(level, east, y + 2, z + j, Blocks.CHISELED_STONE_BRICKS);
				}
			}
		}
		Build.fill(Build.chest(level, x, y + 1, z), random, LOOT, 4 + random.nextInt(4));
	}
}
