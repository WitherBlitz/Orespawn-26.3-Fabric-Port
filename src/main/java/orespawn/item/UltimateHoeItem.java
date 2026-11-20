package orespawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * The Ultimate Hoe (OreSpawn's {@code UltimateHoe}): clicking grass or dirt (not from below, with air above) tills
 * every grass and dirt block with air above in the 3x3x3 around it, straight to fully watered farmland.
 */
public class UltimateHoeItem extends AutoEnchantItem {
	public UltimateHoeItem(Properties properties, List<Ench> enchantments) {
		super(properties, enchantments);
	}

	private static boolean tillable(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)) && level.isEmptyBlock(pos.above());
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		ItemStack stack = context.getItemInHand();
		if (player != null && !player.mayUseItemAt(pos, context.getClickedFace(), stack)) return InteractionResult.FAIL;
		if (context.getClickedFace() == Direction.DOWN || !tillable(level, pos)) return InteractionResult.PASS;
		level.playSound(player, pos, SoundEvents.HOE_TILL.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		BlockState farmland = Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, FarmlandBlock.MAX_MOISTURE);
		for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
			if (tillable(level, p)) level.setBlock(p, farmland, Block.UPDATE_CLIENTS);
		}
		if (player != null) stack.hurtAndBreak(1, player, context.getHand());
		return InteractionResult.SUCCESS;
	}
}
