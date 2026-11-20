package orespawn.world.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import orespawn.OreSpawn;

/**
 * OreSpawn's frog pond (OreSpawnWorld.addFrogPond, GenericDungeon.makeFrogPond): a 7 x 7 pond sunk into plains grass,
 * a little fountain in the middle ringed with lily pads, and a Frog spawner on top.
 */
public record FrogPondFeature() implements Feature {
	public static final MapCodec<FrogPondFeature> CODEC = MapCodec.unit(FrogPondFeature::new);

	@Override
	public MapCodec<FrogPondFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(final WorldGenLevel level, final ChunkGenerator generator, final RandomSource random, final BlockPos origin) {
		return Build.onSurface(level, random, origin, 4, 100, false, Build.GRASS, (l, r, x, y, z) -> build(l, r, x, y - 1, z));
	}

	/** {@code y} is the grass block. OreSpawn set all of this with block updates, so the water flows. */
	static void build(WorldGenLevel level, RandomSource random, int x, int y, int z) {
		Build.spawner(level, random, x, y + 2, z, OreSpawn.id("frog"));
		for (int i = -3; i <= 3; i++) {
			for (int j = -3; j <= 3; j++) Build.flowingWater(level, x + i, y, z + j);
		}
		Build.flowingWater(level, x, y + 1, z);
		Build.flowingWater(level, x - 1, y + 1, z);
		Build.flowingWater(level, x + 1, y + 1, z);
		Build.flowingWater(level, x, y + 1, z - 1);
		Build.flowingWater(level, x, y + 1, z + 1);
		Build.set(level, x - 1, y + 2, z, Blocks.LILY_PAD);
		Build.set(level, x + 1, y + 2, z, Blocks.LILY_PAD);
		Build.set(level, x, y + 2, z - 1, Blocks.LILY_PAD);
		Build.set(level, x, y + 2, z + 1, Blocks.LILY_PAD);
	}
}
