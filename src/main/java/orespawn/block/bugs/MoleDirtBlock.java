package orespawn.block.bugs;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Molenoid Dirt (OreSpawn's MoleDirtBlock), the loose soil a Molenoid flings about: you sink an eighth of a block into it
 * and it slows you to 30% while you're in it, and it crumbles away to nothing on a random tick. Drops itself.
 */
public class MoleDirtBlock extends Block {
	private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 14.0);

	public MoleDirtBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
		return Shapes.block();
	}

	@Override
	protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.block();
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.3, 1.0, 0.3));
	}
}
