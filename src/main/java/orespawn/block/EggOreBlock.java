package orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An "Ancient Dried Spawn Egg" ore (OreSpawn's {@code OreGenericEgg}): drops itself, and half the time 5-9 experience
 * (5 + two rolls of 0-2). Crafting it with a water bucket gives the mob's spawn egg (a data recipe).
 */
public class EggOreBlock extends Block {
	public EggOreBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
		super.spawnAfterBreak(state, level, pos, tool, dropExperience);
		if (dropExperience && level.getRandom().nextInt(2) == 1) {
			this.popExperience(level, pos, 5 + level.getRandom().nextInt(3) + level.getRandom().nextInt(3));
		}
	}
}
