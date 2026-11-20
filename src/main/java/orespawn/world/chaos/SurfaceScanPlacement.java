package orespawn.world.chaos;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.function.Consumer;

/**
 * Placement type {@code orespawn:chaos_surface_scan}: OreSpawnWorld's way of finding the ground for its butterfly
 * plants, veggies and ant nests. From {@code from} (100) down to just above {@code to} (40) it scans the column through
 * air only, and places at the first air block with a grass block under it. Any other block ends the scan, so tall grass,
 * a flower or solid ground at y 100 (common in Chaos, whose top is around y 105) means nothing goes in that column.
 */
public record SurfaceScanPlacement(int from, int to) implements PlacementModifier {
	public static final MapCodec<SurfaceScanPlacement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Codec.INT.optionalFieldOf("from", 100).forGetter(SurfaceScanPlacement::from),
		Codec.INT.optionalFieldOf("to", 40).forGetter(SurfaceScanPlacement::to)
	).apply(i, SurfaceScanPlacement::new));

	@Override
	public void modify(PlacementContext context, RandomSource random, BlockPos origin, Consumer<BlockPos> output) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(origin.getX(), this.from, origin.getZ());
		BlockPos.MutableBlockPos below = new BlockPos.MutableBlockPos();
		for (int y = this.from; y > this.to; y--) {
			pos.setY(y);
			if (!context.getBlockState(pos).isAir()) {
				return;
			}
			below.set(pos.getX(), y - 1, pos.getZ());
			if (context.getBlockState(below).is(Blocks.GRASS_BLOCK)) {
				output.accept(pos.immutable());
				return;
			}
		}
	}

	@Override
	public MapCodec<SurfaceScanPlacement> codec() {
		return CODEC;
	}
}
