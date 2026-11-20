package orespawn.item.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.block.farming.Soil;

/**
 * OreSpawn's ItemSeeds / ItemSeedFood (the seeds, corn, tomatoes, lettuce, radishes, rice and quinoa): plants its crop
 * on top of farmland (or Crystal Grass, which OreSpawn let anything grow on), and nowhere else, even where the crop
 * itself could stand (corn, tomatoes, lettuce and quinoa grow on grass and dirt too, but are planted on farmland).
 * The foods among them are eaten as usual when not planting.
 */
public class FarmlandSeedItem extends BlockItem {
	public FarmlandSeedItem(Block crop, Properties properties) {
		super(crop, properties);
	}

	@Override
	protected boolean canPlace(BlockPlaceContext context, BlockState state) {
		BlockPos below = context.getClickedPos().below();
		return Soil.farmland(context.getLevel().getBlockState(below)) && super.canPlace(context, state);
	}
}
