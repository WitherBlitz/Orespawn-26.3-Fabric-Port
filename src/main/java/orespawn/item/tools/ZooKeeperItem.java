package orespawn.item.tools;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * OreSpawn's ZooKeeper Shard ({@code ItemZooKeeper}): hitting a creature with it, instead of hurting it, makes it stay
 * forever (it no longer despawns), with a burst of smoke and a bang; the shard breaks doing it. Hitting anything else is
 * an ordinary blow (still with the burst). Called from Tools' attack hook.
 */
public class ZooKeeperItem extends Item {
	public ZooKeeperItem(Properties properties) {
		super(properties);
	}

	public InteractionResult leftClickEntity(Player player, Level level, InteractionHand hand, Entity entity) {
		burst(player, level, entity);
		if (!(entity instanceof Mob mob)) return InteractionResult.PASS;
		if (!level.isClientSide()) {
			mob.setPersistenceRequired();
			player.getItemInHand(hand).hurtAndBreak(2, player, hand);
		}
		return InteractionResult.SUCCESS;
	}

	/** Smoke, puffs and red dust within three blocks of the creature, and a quiet bang (also used by the Wrench). */
	static void burst(Player player, Level level, Entity entity) {
		if (level.isClientSide()) {
			RandomSource r = level.getRandom();
			for (int i = 0; i < 8; i++) {
				level.addParticle(ParticleTypes.SMOKE, entity.getX() + r.nextFloat() * 3.0F - r.nextFloat() * 3.0F,
					entity.getY() + 0.25F + r.nextFloat() * 2.0F, entity.getZ() + r.nextFloat() * 3.0F - r.nextFloat() * 3.0F, 0.0, 0.0, 0.0);
				level.addParticle(ParticleTypes.POOF, entity.getX() + r.nextFloat() * 3.0F - r.nextFloat() * 3.0F,
					entity.getY() + 0.25F + r.nextFloat() * 2.0F, entity.getZ() + r.nextFloat() * 3.0F - r.nextFloat() * 3.0F, 0.0, 0.0, 0.0);
				level.addParticle(DustParticleOptions.REDSTONE, entity.getX() + r.nextFloat() * 3.0F - r.nextFloat() * 3.0F,
					entity.getY() + 0.25F + r.nextFloat() * 2.0F, entity.getZ() + r.nextFloat() * 3.0F - r.nextFloat() * 3.0F, 0.0, 0.0, 0.0);
			}
		} else {
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5F, 1.5F);
		}
	}
}
