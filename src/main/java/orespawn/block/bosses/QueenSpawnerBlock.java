package orespawn.block.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import orespawn.content.Bosses;
import orespawn.entity.bosses.TheQueen;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's QueenSpawnerBlock ("The Queen Spawner Block"): a sugar-cane-like plant (no collision, breaks instantly,
 * light 13) that must stand on something solid and gives off firework sparks. Five seconds after it's placed, on any
 * random tick, or as soon as a neighbouring block changes, it summons The Queen 8 blocks above it in guard mode (it
 * stays near that spot) and removes itself and whatever is above it. Breaking it first gets it back.
 */
public class QueenSpawnerBlock extends Block {
	/** BlockReed's bounds: 2-14 across, full height. */
	private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

	public QueenSpawnerBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(20) == 1) {
			for (int i = 0; i < 20; i++) {
				level.addParticle(ParticleTypes.FIREWORK, pos.getX() + random.nextFloat(), pos.getY() + random.nextFloat(), pos.getZ() + random.nextFloat(),
					0.0, 0.0, 0.0);
			}
		}
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		if (!level.isClientSide()) level.scheduleTick(pos, this, 100);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
		if (level instanceof ServerLevel server) summon(server, pos);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		summon(level, pos);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		summon(level, pos);
	}

	private void summon(ServerLevel level, BlockPos pos) {
		if (!level.getBlockState(pos).is(this)) return;
		spawnTheQueen(level, pos.getX(), pos.getY() + 8, pos.getZ());
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
	}

	/** OreSpawn's spawnTheQueen: at the block corner, facing a random way, roaring, in guard mode. */
	public static @Nullable TheQueen spawnTheQueen(ServerLevel level, double x, double y, double z) {
		TheQueen queen = Bosses.THE_QUEEN.create(level, EntitySpawnReason.TRIGGERED);
		if (queen == null) return null;
		queen.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
		level.addFreshEntity(queen);
		queen.playAmbientSound();
		queen.setGuardMode(1);
		return queen;
	}
}
