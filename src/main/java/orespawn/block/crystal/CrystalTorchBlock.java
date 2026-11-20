package orespawn.block.crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * OreSpawn's Crystal Torch (BlockCrystalTorch): a torch (light 14) that, one tick in four, gives off a firework spark
 * and a drifting flame instead of smoke. {@link Wall} is the same torch on a wall. Being full blocks, the crystal blocks
 * hold it as OreSpawn made them do.
 */
public class CrystalTorchBlock extends TorchBlock {
	public CrystalTorchBlock(Properties properties) {
		super(ParticleTypes.FLAME, properties);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		sparks(level, random, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5);
	}

	static void sparks(Level level, RandomSource random, double x, double y, double z) {
		if (random.nextInt(4) != 1) return;
		level.addParticle(ParticleTypes.FIREWORK, x, y, z, (random.nextFloat() - random.nextFloat()) / 8.0F, random.nextFloat() / 8.0F,
			(random.nextFloat() - random.nextFloat()) / 8.0F);
		level.addParticle(ParticleTypes.FLAME, x, y, z, (random.nextFloat() - random.nextFloat()) / 60.0F, random.nextFloat() / 10.0F,
			(random.nextFloat() - random.nextFloat()) / 60.0F);
	}

	/** The Crystal Torch on a wall. */
	public static class Wall extends WallTorchBlock {
		public Wall(Properties properties) {
			super(ParticleTypes.FLAME, properties);
		}

		@Override
		public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
			Direction opposite = state.getValue(FACING).getOpposite();
			sparks(level, random, pos.getX() + 0.5 + 0.271 * opposite.getStepX(), pos.getY() + 0.7 + 0.213, pos.getZ() + 0.5 + 0.271 * opposite.getStepZ());
		}
	}
}
