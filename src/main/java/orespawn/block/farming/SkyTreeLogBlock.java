package orespawn.block.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * OreSpawn's BlockSkyTreeLog, "Sky Tree Wood", the wood of Utopia's sky trees. When a player breaks one, the whole tree
 * comes down: every sky tree wood block touching it (corners included), and every one touching those, breaks and drops
 * itself (OreSpawn's breakRecursor, which stopped 1000 steps deep; this stops after 8192 blocks).
 */
public class SkyTreeLogBlock extends Block {
	private static final int MAX_BLOCKS = 8192;

	public SkyTreeLogBlock(Properties properties) {
		super(properties);
	}

	/** onBlockDestroyedByPlayer. */
	@Override
	public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
		if (!(level instanceof ServerLevel server)) return;
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		Set<BlockPos> seen = new HashSet<>();
		queue.add(pos);
		seen.add(pos);
		int broken = 0;
		while (!queue.isEmpty() && broken < MAX_BLOCKS) {
			BlockPos at = queue.poll();
			for (BlockPos n : BlockPos.betweenClosed(at.offset(-1, -1, -1), at.offset(1, 1, 1))) {
				if (seen.contains(n)) continue;
				BlockPos p = n.immutable();
				seen.add(p);
				BlockState there = server.getBlockState(p);
				if (!there.is(this)) continue;
				server.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
				Block.dropResources(there, server, p);
				queue.add(p);
				broken++;
			}
		}
	}
}
