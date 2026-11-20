package orespawn.item.gadgets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import orespawn.OreSpawn;

/**
 * OreSpawn's instant builders: the Insta-Bridge (StepAccross), Stairs going Up (StepUp) and Stairs going Down
 * (StepDown). Used on a block, with a bang and a puff of smoke, it lays a line of cobblestone from the block over the
 * clicked one in the direction you face (any of the 8 compass directions): flat for the bridge, one block up or down per
 * step for the stairs. It goes up to 32 blocks and stops at the first block that isn't air. Every 8 blocks, starting
 * with the first, an Extreme Torch goes on top where there's room. One item is used up (not in creative).
 * <p>
 * Change from OreSpawn: it read the facing from the unwrapped head yaw, so a negative yaw picked south or did nothing;
 * here the yaw is wrapped first, so every direction works.
 */
public class StepItem extends Item {
	/** OreSpawn's directions for yaw sectors 0-7 (S, SW, W, NW, N, NE, E, SE), as {dx, dz}. */
	private static final int[][] DIRECTIONS = {{0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}};
	private static final int LENGTH = 33;

	/** Blocks up (1), down (-1) or level (0) per step. */
	private final int rise;

	public StepItem(Properties properties, int rise) {
		super(properties);
		this.rise = rise;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null) return InteractionResult.PASS;
		int[] d = DIRECTIONS[(int) (Mth.positiveModulo(player.getYHeadRot() + 22.5F, 360.0F) / 45.0F) & 7];
		int dx = d[0], dz = d[1];
		BlockPos clicked = context.getClickedPos();
		int x = clicked.getX(), y = clicked.getY() + 1, z = clicked.getZ();
		Level level = context.getLevel();
		if (level.isClientSide()) {
			// OreSpawn's largesmoke, largeexplode and reddust, raised a block for the stairs going up.
			RandomSource r = level.getRandom();
			double up = rise > 0 ? 1.0 : 0.0;
			for (int i = 0; i < 6; i++) {
				level.addParticle(ParticleTypes.LARGE_SMOKE, x + r.nextFloat() - r.nextFloat(), y + r.nextFloat() + up,
					z + r.nextFloat() - r.nextFloat(), 0.0, 0.0, 0.0);
				level.addParticle(ParticleTypes.EXPLOSION, x + r.nextFloat() - r.nextFloat(), y + r.nextFloat() + up,
					z + r.nextFloat() - r.nextFloat(), 0.0, 0.0, 0.0);
				level.addParticle(new DustParticleOptions(0xFF0000, 1.0F), x + r.nextFloat() - r.nextFloat(), y + r.nextFloat() + up,
					z + r.nextFloat() - r.nextFloat(), 0.0, 0.0, 0.0);
			}
			return InteractionResult.SUCCESS;
		}
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.5F);
		Block torch = BuiltInRegistries.BLOCK.getValue(OreSpawn.id("extreme_torch"));
		for (int k = 1; k < LENGTH; k++) {
			BlockPos step = new BlockPos(x + k * dx, y + rise * k - 1, z + k * dz);
			if (!level.getBlockState(step).isAir()) break;
			level.setBlock(step, Blocks.COBBLESTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
			if ((k - 1) % 8 == 0 && torch != Blocks.AIR && level.getBlockState(step.above()).isAir()) {
				level.setBlock(step.above(), torch.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}
		context.getItemInHand().consume(1, player);
		return InteractionResult.SUCCESS;
	}
}
