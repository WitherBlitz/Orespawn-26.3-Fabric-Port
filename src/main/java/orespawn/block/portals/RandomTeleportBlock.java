package orespawn.block.portals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * OreSpawn's Random Teleport Block (RTPBlock), found in its dungeons: a player who walks onto it (not sneaking) is
 * thrown about 16 blocks away in a random diagonal direction (8 either way), to a spot within 4 blocks up or down with
 * two blocks of air over something solid, with a puff of smoke and red dust and an explosion sound. If none of 1000 tries
 * finds a spot, nothing happens. It breaks instantly but drops only to a pickaxe. In 1.7.10 the client ran its own
 * teleport too; here only the server does.
 */
public class RandomTeleportBlock extends Block {
	public RandomTeleportBlock(Properties properties) {
		super(properties);
	}

	/** RTPBlock.onEntityWalking. */
	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		if (level instanceof ServerLevel server && entity instanceof ServerPlayer player && !player.isSteppingCarefully()) teleport(server, pos, player);
		super.stepOn(level, pos, state, entity);
	}

	private static void teleport(ServerLevel level, BlockPos pos, ServerPlayer player) {
		RandomSource random = level.getRandom();
		BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		boolean found = false;
		for (int tries = 0; tries < 1000 && !found; tries++) {
			x = pos.getX() + (random.nextInt(2) == 0 ? 16 : -16) + random.nextInt(8) - random.nextInt(8);
			z = pos.getZ() + (random.nextInt(2) == 0 ? 16 : -16) + random.nextInt(8) - random.nextInt(8);
			for (y = pos.getY() - 4; y <= pos.getY() + 4; y++) {
				if (level.getBlockState(at.set(x, y - 1, z)).isSolid() && level.getBlockState(at.set(x, y, z)).isAir()
					&& level.getBlockState(at.set(x, y + 1, z)).isAir()) {
					found = true;
					break;
				}
			}
		}
		if (!found) return;
		player.connection.teleport(x + 0.5, y, z + 0.5, player.getYRot(), 0.0F);
		double px = x + 0.5;
		double py = y + 2.25;
		double pz = z + 0.5;
		level.sendParticles(ParticleTypes.SMOKE, px, py, pz, 6, 0.0, 0.0, 0.0, 0.0);
		level.sendParticles(ParticleTypes.POOF, px, py, pz, 6, 0.0, 0.0, 0.0, 0.0);
		level.sendParticles(DustParticleOptions.REDSTONE, px, py, pz, 6, 0.0, 0.0, 0.0, 0.0);
		level.playSound(null, x + 0.5, y, z + 0.5, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 1.5F);
	}
}
