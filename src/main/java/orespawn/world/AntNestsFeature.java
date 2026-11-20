package orespawn.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import orespawn.content.Portals;

/**
 * One try of OreSpawnWorld.addAnts (feature type {@code orespawn:ant_nests}): on the surface (the placement puts the
 * origin on the first air above the ground), if the top block is a grass block between y 40 and 99 it becomes an ant
 * nest: one time in {@code red_freq} a red, rainbow, unstable or termite nest (one of the four at random), otherwise a
 * black ant nest. OreSpawn tried this 4 times in one chunk in 30, scanning down from y 100 through air only, so a grass
 * block under tall grass, snow or leaves, or above y 99, doesn't count. Its overworld used a red_freq of 4; its own
 * dimensions used 4 (Utopia, Village Mania) or 2 (Mining, twice; Chaos).
 */
public record AntNestsFeature(int redFreq) implements Feature {
	public static final MapCodec<AntNestsFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Codec.INT.fieldOf("red_freq").forGetter(AntNestsFeature::redFreq)).apply(i, AntNestsFeature::new));

	@Override
	public MapCodec<AntNestsFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		BlockPos ground = origin.below();
		if (origin.getY() <= 40 || origin.getY() > 100 || !level.getBlockState(ground).is(Blocks.GRASS_BLOCK)) return false;
		Block nest;
		if (random.nextInt(Math.max(2, redFreq)) == 0) {
			nest = switch (random.nextInt(4)) {
				case 0 -> Portals.RED_ANT_NEST;
				case 1 -> Portals.RAINBOW_ANT_NEST;
				case 2 -> Portals.UNSTABLE_ANT_NEST;
				default -> Portals.TERMITE_NEST;
			};
		} else {
			nest = Portals.ANT_NEST;
		}
		level.setBlock(ground, nest.defaultBlockState(), Block.UPDATE_CLIENTS);
		return true;
	}
}
