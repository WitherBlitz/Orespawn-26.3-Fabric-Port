package orespawn.block.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * OreSpawn's BlockExperiencePlant (the Experience Tree Sapling): a reed-like plant on grass, dirt or farmland that, on
 * each random tick, has a 1 in 10 chance to become an Experience Tree ({@link ExperienceTree}). It sparkles with
 * happy-villager particles now and then, drops itself, and is tinted with the grass colour like 1.7.10's reeds.
 */
public class ExperienceSaplingBlock extends Block {
	private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 16.0);

	public ExperienceSaplingBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return Soil.ground(level.getBlockState(pos.below()));
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour,
			BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		if (!state.canSurvive(level, pos)) ticks.scheduleTick(pos, this, 1);
		return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.canSurvive(level, pos)) level.destroyBlock(pos, true);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (random.nextInt(10) == 1) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			ExperienceTree.grow(level, pos.below(), random);
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(20) == 1) {
			for (int i = 0; i < 20; i++) {
				level.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + random.nextFloat(), pos.getY() + random.nextFloat(),
					pos.getZ() + random.nextFloat(), 0.0, 0.0, 0.0);
			}
		}
	}
}
