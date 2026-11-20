package orespawn.block.crystal;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A block of OreSpawn's Crystal dimension (Kyanite, Crystal Grass, Crystal Planks, the crystal ores and blocks...).
 * OreSpawn drew them as see-through lattices and declared them not opaque, so 1.7.10 gave them no light opacity: light,
 * sky light included, passes straight through them, and nothing suffocates inside them. Their textures are cut out
 * (OreSpawn's own, mostly transparent). Kyanite, Crystal Grass and the Crystalized blocks hide their neighbours' faces as
 * OreSpawn made them do inside the Crystal dimension; the others show every face.
 */
public class CrystalBlock extends Block {
	public CrystalBlock(Properties properties) {
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
