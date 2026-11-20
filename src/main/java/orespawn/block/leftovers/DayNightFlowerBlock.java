package orespawn.block.leftovers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import orespawn.OreSpawn;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * OreSpawn's Pink, Blue, Black and Dead Flowers ({@code MyBlockFlower}): small crossed plants (0.4 wide, 0.6 high) that
 * stand on grass, dirt (any kind), farmland or Crystal Grass (Forge's Plains plants, and Crystal Grass sustains any
 * plant), can be walked through and break, dropping themselves, when their ground goes.
 * <p>
 * They change with the time of day, on each random tick and each neighbour update: after dusk (overworld clock past
 * 12000 of 24000) a Pink Flower turns into a Black Flower and a Blue Flower into a Dead Flower; by day the Black and Dead
 * Flowers turn back.
 */
public class DayNightFlowerBlock extends VegetationBlock {
	private static final VoxelShape SHAPE = Block.column(6.4, 0.0, 9.6);
	private static final Identifier CRYSTAL_GRASS = OreSpawn.id("crystal_grass");

	private final boolean nightForm;
	private final Supplier<Block> other;

	/**
	 * @param nightForm true for the Black and Dead Flowers (what the Pink and Blue ones become at night)
	 * @param other the flower this one turns into
	 */
	public DayNightFlowerBlock(Properties properties, boolean nightForm, Supplier<Block> other) {
		super(properties);
		this.nightForm = nightForm;
		this.other = other;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL)
			|| state.is(Blocks.FARMLAND) || BuiltInRegistries.BLOCK.getOptional(CRYSTAL_GRASS).map(b -> state.is(b)).orElse(false);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		this.checkFlowerChange(state, level, pos);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
		super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
		if (!level.isClientSide()) this.checkFlowerChange(state, level, pos);
	}

	/** MyBlockFlower.checkFlowerChange. */
	private void checkFlowerChange(BlockState state, Level level, BlockPos pos) {
		if (!state.canSurvive(level, pos)) {
			level.destroyBlock(pos, true);
			return;
		}
		boolean night = level.getOverworldClockTime() % 24000L > 12000L;
		if (night != this.nightForm) level.setBlockAndUpdate(pos, this.other.get().defaultBlockState());
	}
}
