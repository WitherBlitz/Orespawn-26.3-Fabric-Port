package orespawn.item.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * The check OreSpawn's building items share (Instant Shelter, Instant Garden, Miner's Dream): the clicked block must
 * be straight north, south, east or west of the player's block (or under it, for none of them).
 */
final class Placement {
	private Placement() {
	}

	/** {dx, dz} from the player's block towards the clicked block, or null if it isn't in a straight line. */
	static int[] lineFromPlayer(Player player, BlockPos clicked) {
		int px = Mth.floor(player.getX()), pz = Mth.floor(player.getZ());
		if (clicked.getX() != px && clicked.getZ() != pz) return null;
		int dx = Integer.signum(clicked.getX() - px), dz = Integer.signum(clicked.getZ() - pz);
		if (dx == 0 && dz == 0) return null;
		return new int[] {dx, dz};
	}
}
