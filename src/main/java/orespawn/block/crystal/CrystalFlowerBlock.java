package orespawn.block.crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * OreSpawn's Red, Green, Blue and Yellow Crystal Flowers (MyBlockFlower): small crossed plants that grow on grass, dirt,
 * farmland or Crystal Grass, can be walked through and break when their ground goes.
 */
public class CrystalFlowerBlock extends VegetationBlock {
	private static final VoxelShape SHAPE = Block.column(6.4, 0.0, 9.6);

	public CrystalFlowerBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return CrystalSaplingBlock.mayGrowOn(state);
	}
}
