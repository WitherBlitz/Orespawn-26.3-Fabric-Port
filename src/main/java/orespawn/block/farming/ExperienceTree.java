package orespawn.block.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.content.Farming;

/**
 * OreSpawn's Trees.ExperienceTree: a 2x2 oak trunk 19 blocks high with four big branches at 6 and four small ones at
 * 19, then 5-10 more trunk blocks, every branch and top log wrapped in Experience Leaves (7x7x3 of them, only into
 * air). Logs are set wherever they go, as OreSpawn did.
 */
public final class ExperienceTree {
	private ExperienceTree() {
	}

	/** Grows the tree on the ground block {@code ground} (grass, dirt or farmland; nothing happens otherwise). */
	public static void grow(ServerLevel level, BlockPos ground, RandomSource random) {
		if (!Soil.ground(level.getBlockState(ground))) return;
		int x = ground.getX(), y = ground.getY(), z = ground.getZ();
		for (int j = 1; j < 6; j++) {
			for (int i = 0; i < 2; i++) {
				for (int k = 0; k < 2; k++) log(level, x + i, y + j, z + k);
			}
		}
		branch(level, random, x, y + 6, z, 0, 1, 1, 1, false);
		branch(level, random, x + 1, y + 6, z, 1, 0, 1, -1, false);
		branch(level, random, x, y + 6, z + 1, -1, 0, -1, 1, false);
		branch(level, random, x + 1, y + 6, z + 1, 0, -1, -1, -1, false);
		for (int j = 7; j < 19; j++) {
			for (int i = 0; i < 2; i++) {
				for (int k = 0; k < 2; k++) log(level, x + i, y + j, z + k);
			}
		}
		branch(level, random, x, y + 19, z, 0, 1, -1, 1, true);
		branch(level, random, x + 1, y + 19, z, 1, 0, 1, 1, true);
		branch(level, random, x, y + 19, z + 1, -1, 0, -1, -1, true);
		branch(level, random, x + 1, y + 19, z + 1, 0, -1, 1, -1, true);
		int grow = 5 + random.nextInt(6);
		for (int j = 19; j < 19 + grow; j++) {
			for (int i = 0; i < 2; i++) {
				for (int k = 0; k < 2; k++) {
					log(level, x + i, y + j, z + k);
					leaves(level, x + i, y + j, z + k);
				}
			}
		}
	}

	/** Trees.grow_branch (big) and grow_small_branch (small): the same shape with shorter runs. */
	private static void branch(ServerLevel level, RandomSource random, int x, int y, int z, int xdir, int zdir, int xxdir, int zzdir, boolean small) {
		int i = x, j = y, k = z, i2 = 0, k2 = 0;
		int grow = small ? 4 + random.nextInt(2) : 5 + random.nextInt(4);
		for (int n = 0; n < grow; n++) {
			log(level, i, j, k);
			leaves(level, i, j, k);
			j++;
			i += xdir;
			k += zdir;
			i2 = i;
			k2 = k;
		}
		grow = small ? 4 + random.nextInt(3) : 6 + random.nextInt(5);
		for (int n = 0; n < grow; n++) {
			log(level, i, j, k);
			leaves(level, i, j, k);
			i += xdir;
			k += zdir;
		}
		grow = small ? 4 + random.nextInt(3) : 6 + random.nextInt(5);
		for (int n = 0; n < grow; n++) {
			log(level, i2, j, k2);
			leaves(level, i2, j, k2);
			i2 += xxdir;
			k2 += zzdir;
		}
		int j2 = --j;
		grow = small ? 3 + random.nextInt(3) : 4 + random.nextInt(4);
		for (int n = 0; n < grow; n++) {
			log(level, i, j, k);
			leaves(level, i, j, k);
			i += xdir;
			k += zdir;
			j--;
		}
		grow = small ? 3 + random.nextInt(3) : 4 + random.nextInt(4);
		for (int n = 0; n < grow; n++) {
			log(level, i2, j2, k2);
			leaves(level, i2, j2, k2);
			i2 += xxdir;
			k2 += zzdir;
			j2--;
		}
	}

	/** Trees.make_leaves: Experience Leaves into the air of a 7x7 square, from this level up two. */
	private static void leaves(ServerLevel level, int x, int y, int z) {
		BlockState leaves = Farming.EXPERIENCE_LEAVES.defaultBlockState();
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int dx = -3; dx <= 3; dx++) {
			for (int dz = -3; dz <= 3; dz++) {
				for (int dy = 0; dy <= 2; dy++) {
					p.set(x + dx, y + dy, z + dz);
					if (level.isInsideBuildHeight(p.getY()) && level.getBlockState(p).isAir()) level.setBlock(p, leaves, Block.UPDATE_CLIENTS);
				}
			}
		}
	}

	private static void log(ServerLevel level, int x, int y, int z) {
		BlockPos p = new BlockPos(x, y, z);
		if (level.isInsideBuildHeight(y)) level.setBlock(p, Blocks.OAK_LOG.defaultBlockState(), Block.UPDATE_CLIENTS);
	}
}
