package orespawn.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import orespawn.content.Portals;

/**
 * One try of OreSpawnWorld.addNetherAnts (feature type {@code orespawn:nether_ant_nests}): in the origin's column, the
 * highest netherrack between y 20 and 89 with air on top becomes a red ant nest. OreSpawn tried this 3 times in one Nether
 * chunk in 25.
 */
public record NetherAntNestsFeature() implements Feature {
	public static final MapCodec<NetherAntNestsFeature> CODEC = MapCodec.unit(NetherAntNestsFeature::new);

	@Override
	public MapCodec<NetherAntNestsFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int y = 90; y > 20; y--) {
			if (level.getBlockState(pos.set(origin.getX(), y, origin.getZ())).isAir()
				&& level.getBlockState(pos.set(origin.getX(), y - 1, origin.getZ())).is(Blocks.NETHERRACK)) {
				level.setBlock(pos, Portals.RED_ANT_NEST.defaultBlockState(), Block.UPDATE_CLIENTS);
				return true;
			}
		}
		return false;
	}
}
