package orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.entity.ants.Ant;

/** OreSpawn's OreSalt: ants (every kind, termites too) that walk on it take 5 damage, as from a cactus. */
public class SaltOreBlock extends Block {
	public SaltOreBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		if (entity instanceof Ant && level instanceof ServerLevel server) {
			entity.hurtServer(server, level.damageSources().cactus(), 5.0F);
		}
		super.stepOn(level, pos, state, entity);
	}
}
