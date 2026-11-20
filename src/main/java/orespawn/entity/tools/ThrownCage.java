package orespawn.entity.tools;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import orespawn.content.Tools;
import orespawn.item.tools.CritterCages;

/**
 * A thrown Empty Critter Cage (OreSpawn's {@code EntityCage}). Two times in ten it misses whatever it hits and drops
 * empty. Otherwise, hitting a creature OreSpawn could cage puts it in the cage (with a puff of smoke and a bang at the
 * thrower), unless that creature breaks free first (its escape chance is in {@link CritterCages}; then the cage drops
 * empty). Hitting a player drops the cage empty. Hitting anything else loses the cage, as in OreSpawn. The Ender
 * Dragon is caught through any part of its body.
 */
public class ThrownCage extends ThrowableItemProjectile {
	public ThrownCage(EntityType<? extends ThrownCage> type, Level level) {
		super(type, level);
	}

	public ThrownCage(Level level, LivingEntity owner, ItemStack stack) {
		super(Tools.ENTITY_CAGE, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return Tools.EMPTY_CRITTER_CAGE;
	}

	@Override
	protected void onHit(HitResult hit) {
		super.onHit(hit);
		if (!(level() instanceof ServerLevel level) || isRemoved()) return;
		if (hit instanceof EntityHitResult entityHit && random.nextInt(10) >= 2) {
			Entity target = entityHit.getEntity();
			for (int i = 0; i < 4; i++) {
				level.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY() + 0.25, target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
				level.sendParticles(ParticleTypes.POOF, target.getX(), target.getY() + 0.25, target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
				level.sendParticles(DustParticleOptions.REDSTONE, target.getX(), target.getY() + 0.25, target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
			}
			Entity owner = getOwner();
			if (owner != null) {
				level.playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.5F);
			}
			if (target instanceof Player) {
				spawnAtLocation(level, new ItemStack(Tools.EMPTY_CRITTER_CAGE));
				discard();
				return;
			}
			if (target instanceof EnderDragonPart part) target = part.parentMob;
			CritterCages.Cage cage = CritterCages.forEntity(target);
			Item caged = cage == null ? null : Tools.CAGED.get(cage.item());
			if (caged != null && cage.accepts(target)) {
				if (random.nextInt(100) < cage.failPercent()) {
					spawnAtLocation(level, new ItemStack(Tools.EMPTY_CRITTER_CAGE));
				} else {
					target.discard();
					spawnAtLocation(level, new ItemStack(caged, cage.count()));
				}
			}
		} else {
			spawnAtLocation(level, new ItemStack(Tools.EMPTY_CRITTER_CAGE));
		}
		discard();
	}
}
