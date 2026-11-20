package orespawn.block.robots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
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
import orespawn.content.Robots;

/**
 * OreSpawn's IslandBlock: a glowing sprout (light 13) that stands on solid ground and sparkles green now and then. On
 * a random tick it grows 1-3 floating islands 12-66 blocks above it, each where a 21x21 patch at that height is all
 * air (a Light Floating Island 1 time in 25, otherwise a Dark one), and then it's gone. It breaks at once, drops
 * itself, and pops off if the ground under it goes. OreSpawn placed it in its Islands dimension; that comes with the
 * dimension.
 */
public class IslandBlock extends Block {
	private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 16.0);

	public IslandBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return level.getBlockState(pos.below()).isSolid();
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		if (!state.canSurvive(level, pos)) ticks.scheduleTick(pos, this, 1);
		return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.canSurvive(level, pos)) level.destroyBlock(pos, true);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(20) != 1) return;
		for (int i = 0; i < 20; i++) {
			level.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + random.nextFloat(), pos.getY() + random.nextFloat(),
				pos.getZ() + random.nextFloat(), 0.0, 0.0, 0.0);
		}
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int n = 1 + random.nextInt(3);
		int m = 55; // OreSpawn's IslandSizeFactor 2
		for (int i = 0; i < n; i++) {
			int height = 12 + random.nextInt(m);
			if (!clear(level, pos.above(height))) continue;
			EntityType<? extends Mob> type = random.nextInt(25) == 1 ? Robots.ISLAND : Robots.ISLAND_TOO;
			Mob island = type.create(level, EntitySpawnReason.TRIGGERED);
			if (island == null) continue;
			island.snapTo(pos.getX(), pos.getY() + height, pos.getZ(), random.nextFloat() * 360.0F, 0.0F);
			island.finalizeSpawn(level, level.getCurrentDifficultyAt(island.blockPosition()), EntitySpawnReason.TRIGGERED, null);
			level.addFreshEntity(island);
		}
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
	}

	private static boolean clear(ServerLevel level, BlockPos at) {
		for (int k = -10; k <= 10; k++) {
			for (int j = -10; j <= 10; j++) {
				if (!level.getBlockState(at.offset(j, 0, k)).isAir()) return false;
			}
		}
		return true;
	}
}
