package orespawn.block.crystal;

import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * OreSpawn's Crystal Tree Wood (BlockCrystalTreeLog): a log, see-through and letting light pass like the other crystal
 * blocks, that crystal leaves count as their tree.
 */
public class CrystalLogBlock extends RotatedPillarBlock {
	public CrystalLogBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected int getLightDampening(BlockState state) {
		return 0;
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}
}
