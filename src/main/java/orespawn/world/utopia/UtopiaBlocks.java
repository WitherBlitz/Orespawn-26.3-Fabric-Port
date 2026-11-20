package orespawn.world.utopia;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * OreSpawn blocks Utopia's trees place that other content groups own, looked up by id each time (they register in
 * their own groups). The leaves are the Farming group's; sky tree wood ("Sky Tree Wood", which another group's
 * IslandToo also looks up as {@code sky_tree_log}) isn't registered by anyone yet. Each falls back to the vanilla block
 * OreSpawn's trees otherwise use.
 */
public final class UtopiaBlocks {
	private UtopiaBlocks() {
	}

	/** MyAppleLeaves. */
	public static BlockState appleLeaves() {
		return state(Blocks.OAK_LEAVES, "apple_leaves");
	}

	/** MyCherryLeaves (vanilla's cherry leaves stand in). */
	public static BlockState cherryLeaves() {
		return state(Blocks.CHERRY_LEAVES, "cherry_leaves");
	}

	/** MyPeachLeaves. */
	public static BlockState peachLeaves() {
		return state(Blocks.OAK_LEAVES, "peach_leaves");
	}

	/** MySkyTreeLog. */
	public static BlockState skyTreeLog() {
		return state(Blocks.OAK_LOG, "sky_tree_log", "sky_tree_wood");
	}

	private static BlockState state(Block fallback, String... ids) {
		BlockState state = Lookup.state(fallback, ids);
		return state != null ? state : fallback.defaultBlockState();
	}
}
