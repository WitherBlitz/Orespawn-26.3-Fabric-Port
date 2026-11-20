package orespawn.block.crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.content.Crystal;
import orespawn.world.OreSpawnDimensions;

import java.util.function.Supplier;

/**
 * OreSpawn's Crystal Tree Leaves (BlockCrystalLeaves), red, yellow or blue after the tree they grow on. On a random
 * tick a leaf looks for wood within 3 blocks (beside or below it); without any it falls apart, otherwise, with air
 * under it, it has a 1 in 20 chance (1 in 100 in the Islands) of letting fall what its drops are made of: a Crystal
 * Apple (1 in 100) and its own sapling (1 in 50). Breaking it gives the same chances (its loot table), or the leaves
 * themselves with shears or Silk Touch. Player-placed leaves decay too, as OreSpawn's did.
 */
public class CrystalLeavesBlock extends Block {
	private final Supplier<Block> sapling;

	public CrystalLeavesBlock(Supplier<Block> sapling, Properties properties) {
		super(properties);
		this.sapling = sapling;
	}

	/** canSustainLeaves: vanilla logs and OreSpawn's crystal wood. */
	private static boolean holdsLeaves(BlockState state) {
		return state.is(Crystal.CRYSTAL_TREE_WOOD) || state.is(BlockTags.LOGS);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int reach = 2;
		int chance = 20;
		if (level.dimension() == OreSpawnDimensions.ISLANDS) {
			chance = 100;
			reach = 1;
		}
		if (!level.hasChunksAt(pos.offset(-reach, -reach, -reach), pos.offset(reach, reach, reach))) return;
		for (int dx = -reach; dx <= reach; dx++) {
			for (int dy = -reach; dy <= 0; dy++) {
				for (int dz = -reach; dz <= reach; dz++) {
					if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 3) continue;
					if (holdsLeaves(level.getBlockState(pos.offset(dx, dy, dz)))) {
						if (level.isEmptyBlock(pos.below()) && random.nextInt(chance) == 3) this.dropFruit(level, pos.below(), random);
						return;
					}
				}
			}
		}
		Block.dropResources(state, level, pos);
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
	}

	/** dropBlockAsItemWithChance: a Crystal Apple 1 time in 100, this tree's sapling 1 time in 50. */
	private void dropFruit(ServerLevel level, BlockPos pos, RandomSource random) {
		if (random.nextInt(100) == 1) Block.popResource(level, pos, new ItemStack(Crystal.CRYSTAL_APPLE));
		if (random.nextInt(50) == 1) Block.popResource(level, pos, new ItemStack(this.sapling.get()));
	}
}
