package orespawn.block.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.content.Farming;

/**
 * OreSpawn's ItemAppleSeed.makeTree: an apple, cherry or peach tree grown at once on grass, dirt or farmland. An oak
 * trunk, two crosses of oak branches, and a square canopy of the fruit leaves filled into the air (6 wide at the bottom,
 * narrowing upward; one narrower for cherry and peach). Apple trees are the biggest, cherry trees the smallest.
 */
public final class FruitTree {
	private FruitTree() {
	}

	/** Grows the tree with the given leaves on the ground block {@code ground}; false if that isn't grass/dirt/farmland. */
	public static boolean grow(ServerLevel level, BlockPos ground, Block leaves) {
		if (!Soil.ground(level.getBlockState(ground))) return false;
		int h1 = 12, h2 = 6, h3 = 9, h4 = 6, h5 = 14, w1 = 5, w2 = 3;
		if (leaves == Farming.PEACH_LEAVES) {
			h1 = 10; h2 = 5; h3 = 7; h4 = 5; h5 = 12; w1 = 4; w2 = 2;
		}
		if (leaves == Farming.CHERRY_LEAVES) {
			h1 = 8; h2 = 3; h3 = 5; h4 = 3; h5 = 10; w1 = 3; w2 = 1;
		}
		int x = ground.getX(), y = ground.getY(), z = ground.getZ();
		BlockState log = Blocks.OAK_LOG.defaultBlockState();
		for (int j = 1; j < h1; j++) set(level, x, y + j, z, log);
		for (int i = 1; i < w1; i++) {
			set(level, x + i, y + h2, z, log);
			set(level, x - i, y + h2, z, log);
			set(level, x, y + h2, z + i, log);
			set(level, x, y + h2, z - i, log);
		}
		for (int i = 1; i < w2; i++) {
			set(level, x + i, y + h3, z, log);
			set(level, x - i, y + h3, z, log);
			set(level, x, y + h3, z + i, log);
			set(level, x, y + h3, z - i, log);
		}
		BlockState leaf = leaves.defaultBlockState();
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int i = h4; i < h5; i++) {
			int width = i > 10 ? 4 : i > 8 ? 5 : 6;
			if (leaves != Farming.APPLE_LEAVES) width--;
			for (int dz = -width; dz <= width; dz++) {
				for (int dx = -width; dx <= width; dx++) {
					p.set(x + dx, y + i, z + dz);
					if (level.isInsideBuildHeight(p.getY()) && level.getBlockState(p).isAir()) level.setBlock(p, leaf, Block.UPDATE_CLIENTS);
				}
			}
		}
		return true;
	}

	private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
		if (level.isInsideBuildHeight(y)) level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
	}
}
