package orespawn.item.farming;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import orespawn.block.farming.FruitTree;
import orespawn.block.farming.Soil;

import java.util.function.Supplier;

/**
 * OreSpawn's ItemAppleSeed (Apple Tree Seed, Cherry Pit, Peach Pit): used on grass, dirt or farmland, it grows a whole
 * fruit tree there at once ({@link FruitTree}). Stacks to 16.
 */
public class FruitTreeSeedItem extends Item {
	private final Supplier<Block> leaves;

	public FruitTreeSeedItem(Supplier<Block> leaves, Properties properties) {
		super(properties);
		this.leaves = leaves;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (!Soil.ground(context.getLevel().getBlockState(context.getClickedPos()))) return InteractionResult.PASS;
		if (context.getLevel() instanceof ServerLevel level) {
			FruitTree.grow(level, context.getClickedPos(), leaves.get());
			Player player = context.getPlayer();
			if (player == null || !player.hasInfiniteMaterials()) context.getItemInHand().shrink(1);
		}
		return InteractionResult.SUCCESS;
	}
}
