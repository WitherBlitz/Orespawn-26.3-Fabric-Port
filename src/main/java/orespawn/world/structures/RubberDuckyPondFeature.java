package orespawn.world.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import orespawn.OreSpawn;
import orespawn.world.structures.Build.Loot;

import java.util.List;

/**
 * OreSpawn's Rubber Ducky pond (OreSpawnWorld.addRubberDuckyPond, GenericDungeon.makeRubberDuckyPond): a sand-rimmed
 * pond on the plains with a little tower in it: water spilling over a glass block, a chest and two Rubber Ducky spawners.
 */
public record RubberDuckyPondFeature() implements Feature {
	public static final MapCodec<RubberDuckyPondFeature> CODEC = MapCodec.unit(RubberDuckyPondFeature::new);

	/** GenericDungeon.RubberDuckyContentsList. */
	static final List<Loot> LOOT = List.of(Loot.of("dead_stink_bug", 4, 10, 35), Loot.of("fire_fish", 4, 10, 35),
		Loot.of("sun_fish", 4, 10, 35), Loot.of("spark_fish", 4, 10, 35), Loot.of("green_fish", 4, 10, 35),
		Loot.of("blue_fish", 4, 10, 35), Loot.of("pink_fish", 4, 10, 35), Loot.of("rock_fish", 4, 10, 35),
		Loot.of("wood_fish", 4, 10, 35), Loot.of("grey_fish", 4, 10, 35), Loot.of("rubber_ducky_spawn_egg", 4, 10, 35),
		Loot.of("peacock_feather", 4, 10, 35), Loot.of(Items.FEATHER, 6, 16, 35));

	@Override
	public MapCodec<RubberDuckyPondFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(final WorldGenLevel level, final ChunkGenerator generator, final RandomSource random, final BlockPos origin) {
		return Build.onSurface(level, random, origin, 4, 100, false, Build.GRASS, RubberDuckyPondFeature::build);
	}

	/** {@code y} is the air block above the grass. */
	static void build(WorldGenLevel level, RandomSource random, int x, int y, int z) {
		for (int i = 0; i < 2; i++) Build.spawner(level, random, x + i, y + 6, z, OreSpawn.id("rubber_ducky"));
		Container chest = Build.doubleChest(level, x, y + 5, z, false);
		Build.fill(chest, random, LOOT, 8 + random.nextInt(5));
		Build.set(level, x, y + 4, z, Blocks.GLASS);
		Build.set(level, x + 1, y + 4, z, Blocks.GLASS);
		for (int i = 0; i < 2; i++) Build.flowingWater(level, x + i, y + 3, z);
		Build.flowingWater(level, x - 1, y + 3, z);
		Build.flowingWater(level, x + 2, y + 3, z);
		for (int i = 0; i < 12; i++) {
			for (int k = 0; k < 11; k++) {
				boolean rim = i == 0 || k == 0 || i == 11 || k == 10;
				Build.set(level, x + i - 5, y, z + k - 5, rim ? Blocks.SAND : Blocks.WATER);
				Build.set(level, x + i - 5, y + 1, z + k - 5, Blocks.AIR);
				Build.set(level, x + i - 5, y + 2, z + k - 5, Blocks.AIR);
			}
		}
	}
}
