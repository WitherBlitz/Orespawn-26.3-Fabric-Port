package orespawn.block.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * OreSpawn's BlockCrops plants (BlockRadish, BlockRice, BlockStrawberry and the creature plants): vanilla crop growth
 * (ages 0-7 on farmland, light 9, bone meal 2-5), drawn with four textures (ages 0-1, 2-3, 4-6, 7).
 * <p>
 * 1.7.10's crops ticked at every age and, like every BlockBush, first checked they could still stand there and popped
 * off (dropping their items) when not. That's how OreSpawn's wild plants worked: the world generator set them on grass,
 * and the first random tick popped them into seeds. Both are kept. Their outline is 1.7.10's fixed 4 pixels high.
 * Drops are the block's loot table.
 */
public class OreSpawnCropBlock extends CropBlock {
	private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 4.0);
	private final Supplier<? extends ItemLike> seed;

	public OreSpawnCropBlock(Supplier<? extends ItemLike> seed, BlockBehaviour.Properties properties) {
		super(properties);
		this.seed = seed;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return Soil.farmland(state);
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.canSurvive(level, pos)) {
			level.destroyBlock(pos, true);
			return;
		}
		super.randomTick(state, level, pos, random);
	}

	@Override
	protected ItemLike getBaseSeedId() {
		return seed.get();
	}
}
