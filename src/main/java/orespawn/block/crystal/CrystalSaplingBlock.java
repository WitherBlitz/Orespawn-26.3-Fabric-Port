package orespawn.block.crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import orespawn.content.Crystal;
import orespawn.world.crystal.CrystalTrees;
import orespawn.world.crystal.Voxels;

import java.util.Random;

/**
 * OreSpawn's crystal tree saplings (BlockCrystalPlant): Red, Yellow and Blue. They stand on grass, dirt, farmland or
 * Crystal Grass, sparkle now and then, and on each random tick have a 1 in 5 chance to become their tree (the tall red
 * tree, the scraggly yellow one, the blue one), in any light. No bone meal, as in OreSpawn. OreSpawn's Yellow sapling
 * dropped a Blue one when broken (a slip in its getItemDropped), kept in its loot table.
 */
public class CrystalSaplingBlock extends VegetationBlock {
	public enum Tree { RED, YELLOW, BLUE }

	private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 16.0);
	private final Tree tree;

	public CrystalSaplingBlock(Tree tree, Properties properties) {
		super(properties);
		this.tree = tree;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return mayGrowOn(state);
	}

	/** BlockCrystalPlant.canPlaceBlockAt and MyBlockFlower: grass, dirt (any kind), farmland or Crystal Grass. */
	public static boolean mayGrowOn(BlockState state) {
		return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL)
			|| state.is(Blocks.FARMLAND) || state.is(Crystal.CRYSTAL_GRASS);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(30) != 1) return;
		for (int i = 0; i < 10; i++) {
			level.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + random.nextFloat(), pos.getY() + random.nextFloat(), pos.getZ() + random.nextFloat(),
				0.0, 0.0, 0.0);
		}
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (random.nextInt(5) != 1) return;
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
		Voxels world = new Voxels() {
			private final BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();

			@Override
			public BlockState state(int x, int y, int z) {
				return level.getBlockState(this.at.set(x, y, z));
			}

			@Override
			public void set(int x, int y, int z, BlockState s) {
				BlockPos p = new BlockPos(x, y, z);
				if (!level.isOutsideBuildHeight(p)) level.setBlock(p, s, 2);
			}
		};
		Random shapes = new Random(random.nextLong());
		switch (this.tree) {
			case RED -> CrystalTrees.tall(world, shapes, pos.getX(), pos.getY(), pos.getZ());
			case YELLOW -> CrystalTrees.scraggly(world, shapes, pos.getX(), pos.getY(), pos.getZ());
			case BLUE -> CrystalTrees.tallBlue(world, shapes, pos.getX(), pos.getY(), pos.getZ());
		}
	}
}
