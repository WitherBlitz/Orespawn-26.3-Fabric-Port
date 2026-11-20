package orespawn.entity.weapons;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * OreSpawn's ThunderBolt, the Thunder Staff's shot: an unseen bolt that leaves a trail of firework sparks. On a hit it
 * does 20 thrown damage and 20 more as the shooter's attack and sets the target on fire for 1 s (royalty is never
 * hurt); wherever it lands it bursts in smoke and sparks, explodes (power 3; blocks break only with mobGriefing) and
 * calls down a lightning bolt.
 * <p>
 * OreSpawn never registered this entity, so clients only saw the bolt their own staff spawned; here it is a normal
 * entity drawn as nothing but its sparks.
 */
public class ThunderBolt extends ThrowableProjectile {
	public ThunderBolt(EntityType<? extends ThunderBolt> type, Level level) {
		super(type, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			for (int i = 0; i < 4; i++) {
				level().addParticle(ParticleTypes.FIREWORK, getX(), getY(), getZ(), random.nextGaussian() / 10.0, random.nextGaussian() / 10.0,
					random.nextGaussian() / 10.0);
			}
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		if (!(level() instanceof ServerLevel level)) return;
		Entity e = result.getEntity();
		if (WeaponHooks.isRoyalty(e)) {
			discard();
			return;
		}
		e.hurtServer(level, damageSources().thrown(this, getOwner()), 20.0F);
		if (getOwner() instanceof LivingEntity owner) e.hurtServer(level, damageSources().mobAttack(owner), 20.0F);
		e.igniteForSeconds(1.0F);
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		if (!(level() instanceof ServerLevel level) || isRemoved()) return;
		level.broadcastEntityEvent(this, (byte) 3);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 0.5F,
			1.0F + (random.nextFloat() - random.nextFloat()) * 0.5F);
		level.explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.MOB);
		LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
		if (bolt != null) {
			bolt.snapTo(getX(), getY() + 1.0, getZ());
			level.addFreshEntity(bolt);
		}
		discard();
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id != 3) {
			super.handleEntityEvent(id);
			return;
		}
		for (int i = 0; i < 20; i++) {
			level().addParticle(ParticleTypes.SMOKE, getX() + random.nextFloat() - random.nextFloat(), getY() + random.nextFloat() - random.nextFloat(),
				getZ() + random.nextFloat(), 0.0, 0.0, 0.0);
			level().addParticle(ParticleTypes.LARGE_SMOKE, getX() + random.nextFloat() - random.nextFloat(),
				getY() + random.nextFloat() - random.nextFloat(), getZ() + random.nextFloat() - random.nextFloat(), 0.0, 0.0, 0.0);
			level().addParticle(ParticleTypes.FIREWORK, getX(), getY(), getZ(), random.nextGaussian(), random.nextGaussian(), random.nextGaussian());
		}
	}
}
