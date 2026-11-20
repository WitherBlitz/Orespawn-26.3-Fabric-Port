package orespawn.block.portals;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * OreSpawn's Red Ant Troll Block and Termite Troll Block (OreBasicStone): stone-like blocks hidden in the overworld's
 * stone that let out 15 to 20 red ants or termites when a player breaks one. They need a pickaxe to drop themselves.
 * OreBasicStone made itself see-through outside the Crystal world (a side effect of its Crystal-world blocks); these are
 * ordinary opaque blocks here.
 */
public class TrollBlock extends Block {
	private final Supplier<? extends EntityType<? extends Mob>> ant;

	public TrollBlock(Properties properties, Supplier<? extends EntityType<? extends Mob>> ant) {
		super(properties);
		this.ant = ant;
	}

	/** OreBasicStone.onBlockDestroyedByPlayer. */
	@Override
	public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
		if (level instanceof ServerLevel server) {
			RandomSource random = server.getRandom();
			int num = 15 + random.nextInt(6);
			for (int i = 0; i < num; i++) {
				AntNestBlock.release(server, ant.get(), pos.getX() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.2, pos.getY() + 0.01,
					pos.getZ() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.2);
			}
		}
		super.destroy(level, pos, state);
	}
}
