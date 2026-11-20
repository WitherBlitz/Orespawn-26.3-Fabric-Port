package orespawn.block.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.content.Farming;

/**
 * OreSpawn's BlockDuplicatorLog, "Duplicator Tree Wood": a log that grows on random ticks (OreSpawn's Trees.DuplicatorTree,
 * on by default). Standing on grass, dirt or farmland it stacks itself three high, tops itself with apple leaves and fills
 * the ring of leaves around its top. Once grown, it copies a block standing next to its base (within 2 blocks, at the
 * base's height) into an empty spot there, so whatever is placed beside it multiplies. Burns into 4 oak planks in the
 * crafting grid. As in OreSpawn, only the log standing directly on the ground grows the tree.
 */
public class DuplicatorLogBlock extends Block {
	public DuplicatorLogBlock(Properties properties) {
		super(properties);
	}

	private static boolean isGround(BlockState state) {
		return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.FARMLAND);
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!isGround(level.getBlockState(pos.below()))) return;
		int x = pos.getX(), z = pos.getZ(), groundY = pos.getY() - 1;
		Block log = this;
		Block leaves = Farming.APPLE_LEAVES;
		for (int dy = 1; dy <= 3; dy++) {
			BlockPos p = new BlockPos(x, groundY + dy, z);
			if (!level.getBlockState(p).is(log)) {
				level.setBlock(p, log.defaultBlockState(), Block.UPDATE_CLIENTS);
				return;
			}
		}
		BlockPos top = new BlockPos(x, groundY + 4, z);
		if (!level.getBlockState(top).is(leaves)) {
			level.setBlock(top, leaves.defaultBlockState(), Block.UPDATE_CLIENTS);
			return;
		}
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (i == 0 && j == 0) continue;
				BlockPos p = new BlockPos(x + i, groundY + 3, z + j);
				if (!level.getBlockState(p).is(leaves)) {
					level.setBlock(p, leaves.defaultBlockState(), Block.UPDATE_CLIENTS);
					return;
				}
			}
		}
		// Grown: copy a block found next to the base into an empty spot next to the base.
		BlockState found = Blocks.AIR.defaultBlockState();
		for (int tries = 0; tries < 20 && (found.isAir() || found.is(log)); tries++) {
			found = level.getBlockState(new BlockPos(x + random.nextInt(5) - 2, groundY + 1, z + random.nextInt(5) - 2));
			if (!found.isAir() && !found.is(log)) {
				for (int k = 0; k < 20; k++) {
					BlockPos to = new BlockPos(x + random.nextInt(5) - 2, groundY + 1, z + random.nextInt(5) - 2);
					if (level.getBlockState(to).isAir()) {
						level.setBlock(to, found, Block.UPDATE_CLIENTS);
						return;
					}
				}
			}
		}
	}
}
