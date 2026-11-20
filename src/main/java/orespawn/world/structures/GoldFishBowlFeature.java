package orespawn.world.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import orespawn.OreSpawn;

/**
 * OreSpawn's gold fish bowl (OreSpawnWorld.addGoldFishBowl, GenericDungeon.makeGoldFishBowl): a glass bowl on the
 * ocean's surface with a sand floor, two layers of water lit by glowstone, and a Gold Fish spawner in the air above.
 */
public record GoldFishBowlFeature() implements Feature {
	public static final MapCodec<GoldFishBowlFeature> CODEC = MapCodec.unit(GoldFishBowlFeature::new);

	@Override
	public MapCodec<GoldFishBowlFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(final WorldGenLevel level, final ChunkGenerator generator, final RandomSource random, final BlockPos origin) {
		return Build.onSurface(level, random, origin, 4, 100, false, Build.WATER, (l, r, x, y, z) -> build(l, r, x, y - 1, z));
	}

	/** {@code y} is the ocean's top water block. */
	static void build(WorldGenLevel level, RandomSource random, int x, int y, int z) {
		for (int i = 0; i < 5; i++) {
			for (int k = 0; k < 5; k++) Build.set(level, x + i, y + 1, z + k, Blocks.GLASS);
		}
		ring(level, x, y + 2, z, Blocks.SAND);
		ring(level, x, y + 3, z, Blocks.WATER);
		Build.set(level, x, y + 3, z, Blocks.GLOWSTONE);
		Build.set(level, x + 4, y + 3, z + 4, Blocks.GLOWSTONE);
		Build.set(level, x, y + 3, z + 4, Blocks.GLOWSTONE);
		Build.set(level, x + 4, y + 3, z, Blocks.GLOWSTONE);
		ring(level, x, y + 4, z, Blocks.WATER);
		for (int j = 5; j < 8; j++) ring(level, x, y + j, z, Blocks.AIR);
		for (int i = 0; i < 5; i++) {
			for (int k = 0; k < 5; k++) Build.set(level, x + i, y + 8, z + k, Blocks.GLASS);
		}
		Build.spawner(level, random, x + 2, y + 6, z + 2, OreSpawn.id("gold_fish"));
	}

	/** A 7 x 7 layer: {@code inside} within a glass rim. */
	private static void ring(WorldGenLevel level, int x, int y, int z, Block inside) {
		for (int i = -1; i < 6; i++) {
			for (int k = -1; k < 6; k++) {
				Build.set(level, x + i, y, z + k, i == -1 || k == -1 || i == 5 || k == 5 ? Blocks.GLASS : inside);
			}
		}
	}
}
