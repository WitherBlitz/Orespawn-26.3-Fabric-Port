package orespawn.world.crystal;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Where OreSpawn's Crystal generators read and write blocks: one chunk while it's built (OreSpawn's
 * getBlockIDInChunk / setBlockIDWithMetadataInChunk, which see air and drop writes outside that chunk), the
 * decoration region around a chunk, or the live world (a growing sapling). Coordinates are world block coordinates.
 */
public interface Voxels {
	BlockState state(int x, int y, int z);

	void set(int x, int y, int z, BlockState state);

	default Block get(int x, int y, int z) {
		return this.state(x, y, z).getBlock();
	}

	default void set(int x, int y, int z, Block block) {
		this.set(x, y, z, block.defaultBlockState());
	}

	default boolean isAir(int x, int y, int z) {
		return this.state(x, y, z).isAir();
	}
}
