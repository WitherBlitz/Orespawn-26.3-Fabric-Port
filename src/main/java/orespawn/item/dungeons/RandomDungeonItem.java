package orespawn.item.dungeons;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.content.Dungeons;
import orespawn.item.AutoEnchantItem;

import java.util.List;

/**
 * OreSpawn's ItemRandomDungeon ("Random Dungeon"): used on stone, cobblestone, grass or dirt at y 40 or above, it
 * plants a Random Dungeon Spawner on top, which builds a random dungeon twenty seconds later. Stacks to 1 and carries
 * Fortune II, as OreSpawn's did (just for the shine).
 */
public class RandomDungeonItem extends AutoEnchantItem {
	public RandomDungeonItem(Properties properties) {
		super(properties, List.of(new Ench(Enchantments.FORTUNE, 2)));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState clicked = level.getBlockState(pos);
		if (!clicked.is(Blocks.STONE) && !clicked.is(Blocks.COBBLESTONE) && !clicked.is(Blocks.GRASS_BLOCK) && !clicked.is(Blocks.DIRT)) {
			return InteractionResult.PASS;
		}
		if (pos.getY() < 40) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			level.setBlock(pos.above(), Dungeons.RANDOM_DUNGEON_SPAWNER.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
		Player player = context.getPlayer();
		if (player == null || !player.getAbilities().instabuild) {
			context.getItemInHand().shrink(1);
		}
		return InteractionResult.SUCCESS;
	}
}
