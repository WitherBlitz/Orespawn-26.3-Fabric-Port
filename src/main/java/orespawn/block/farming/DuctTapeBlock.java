package orespawn.block.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * OreSpawn's BlockDuctTape: a roll with six uses, shaped like the pizza. Right or left clicking it while holding a
 * single damaged tool, weapon or armor piece repairs a sixth of its durability (at least 1) and uses up one sixth of
 * the roll. It stands on any solid block and drops nothing when broken. Like the pizza, it has the cake's 0.5
 * hardness rather than OreSpawn's 0.
 */
public class DuctTapeBlock extends PizzaBlock {
	public DuctTapeBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected boolean use(BlockState state, Level level, BlockPos pos, Player player) {
		ItemStack held = player.getMainHandItem();
		if (held.isEmpty() || held.getCount() != 1) return false;
		int max = held.getMaxDamage();
		if (max <= 0) return false;
		int repair = Math.max(1, max / 6);
		int damage = held.getDamageValue();
		if (damage <= 0) return false;
		if (level.isClientSide()) return true;
		held.setDamageValue(damage > repair ? damage - repair : 0);
		takeSlice(state, level, pos);
		return true;
	}

	/** BlockDuctTape.canBlockStay: the block below is solid. */
	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return level.getBlockState(pos.below()).isSolid();
	}
}
