package orespawn.world.chaos;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.Random;

/**
 * ChunkProviderOreSpawn6.addScragglyTrees: Chaos's own twisted oak trees with OreSpawn's Apple Leaves. One chunk in four
 * gets 1-5 tries; each takes the highest grass block between y 50 and 119 in a random column (2-13 into the chunk) and
 * grows from it a trunk 1-3 high that then wanders up and sideways for up to 13 more logs, now and then putting out a
 * wandering branch, with leaves scattered around each log.
 * <p>
 * As in OreSpawn this runs while the chunk is made, and only inside it: a tree reaching past the chunk's edge is cut
 * off there (OreSpawn read outside blocks as air, so the tree keeps growing out of sight and may come back in).
 * <p>
 * Apple Leaves (Farming group) are looked up by id; without them oak leaves are used.
 */
final class ChaosTrees {
	private final ChunkAccess chunk;
	private final Random random;
	private final int chunkX;
	private final int chunkZ;
	private final BlockState log = Blocks.OAK_LOG.defaultBlockState();
	private final BlockState leaves;
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	private ChaosTrees(ChunkAccess chunk, Random random) {
		this.chunk = chunk;
		this.random = random;
		this.chunkX = chunk.getPos().x();
		this.chunkZ = chunk.getPos().z();
		Identifier apple = Identifier.fromNamespaceAndPath("orespawn", "apple_leaves");
		this.leaves = BuiltInRegistries.BLOCK.containsKey(apple) ? BuiltInRegistries.BLOCK.getValue(apple).defaultBlockState()
			: Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE, 1);
	}

	/**
	 * addScragglyTrees. {@code worldRand} stands for OreSpawn's world.rand (how many, and whether), {@code random} for the
	 * chunk provider's own random (everything else).
	 */
	static void add(ChunkAccess chunk, Random worldRand, Random random) {
		ChaosTrees trees = new ChaosTrees(chunk, random);
		int howmany = 1 + worldRand.nextInt(5);
		if (worldRand.nextInt(4) != 0) {
			return;
		}
		int baseX = trees.chunkX * 16;
		int baseZ = trees.chunkZ * 16;
		for (int i = 0; i < howmany; i++) {
			int posX = 2 + baseX + random.nextInt(12);
			int posZ = 2 + baseZ + random.nextInt(12);
			for (int posY = 120; posY > 50; posY--) {
				if (trees.get(posX, posY - 1, posZ).is(Blocks.GRASS_BLOCK)) {
					trees.tree(posX, posY, posZ);
					break;
				}
			}
		}
	}

	/** OreSpawnMain.getBlockIDInChunk: air outside this chunk. */
	private BlockState get(int x, int y, int z) {
		if (x >> 4 != this.chunkX || z >> 4 != this.chunkZ || y < 0 || y > 255) {
			return Blocks.AIR.defaultBlockState();
		}
		return this.chunk.getBlockState(this.pos.set(x, y, z));
	}

	/** OreSpawnMain.setBlockIDWithMetadataInChunk: nothing outside this chunk. */
	private void set(int x, int y, int z, BlockState state) {
		if (x >> 4 != this.chunkX || z >> 4 != this.chunkZ || y < 0 || y > 255) {
			return;
		}
		this.chunk.setBlockState(this.pos.set(x, y, z), state);
	}

	private boolean open(BlockState state) {
		return state.isAir() || state.is(this.log.getBlock()) || state.is(this.leaves.getBlock());
	}

	private void leavesAround(int x, int y, int z) {
		for (int m = -1; m < 2; m++) {
			for (int n = -1; n < 2; n++) {
				if (this.random.nextInt(2) == 1 && this.get(x + m, y, z + n).isAir()) {
					this.set(x + m, y, z + n, this.leaves);
				}
			}
		}
		if (this.random.nextInt(2) == 1 && this.get(x, y + 1, z).isAir()) {
			this.set(x, y + 1, z, this.leaves);
		}
	}

	/** ScragglyTreeWithBranches. */
	private void tree(int x, int y, int z) {
		int i = 1 + this.random.nextInt(3);
		int j = i + this.random.nextInt(12);
		for (int k = 0; k < i; k++) {
			if (k >= 1 && !this.open(this.get(x, y + k, z))) {
				return;
			}
			this.set(x, y + k, z, this.log);
		}
		y += i - 1;
		for (int step = i; step < j; step++) {
			int ix = this.random.nextInt(2) - this.random.nextInt(2);
			int iz = this.random.nextInt(2) - this.random.nextInt(2);
			int iy = this.random.nextInt(4) > 0 ? 1 : 0;
			x += ix;
			z += iz;
			y += iy;
			if (!this.open(this.get(x, y, z))) {
				break;
			}
			this.set(x, y, z, this.log);
			if (this.random.nextInt(4) == 1) {
				int len = this.random.nextInt(1 + j - step);
				int biasX = this.random.nextInt(2) - this.random.nextInt(2);
				int biasZ = this.random.nextInt(2) - this.random.nextInt(2);
				this.branch(x, y, z, len, biasX, biasZ);
			}
			this.leavesAround(x, y, z);
		}
	}

	/** makeScragglyBranch. */
	private void branch(int x, int y, int z, int len, int biasX, int biasZ) {
		for (int k = 0; k < len; k++) {
			int ix = Math.max(-1, Math.min(1, this.random.nextInt(2) - this.random.nextInt(2) + biasX));
			int iz = Math.max(-1, Math.min(1, this.random.nextInt(2) - this.random.nextInt(2) + biasZ));
			int iy = this.random.nextInt(3) > 0 ? 1 : 0;
			x += ix;
			z += iz;
			y += iy;
			if (!this.open(this.get(x, y, z))) {
				return;
			}
			this.set(x, y, z, this.log);
			this.leavesAround(x, y, z);
		}
	}
}
