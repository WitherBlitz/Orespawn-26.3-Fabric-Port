package orespawn.entity.weapons;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import orespawn.content.Weapons;

import java.util.Set;

/**
 * OreSpawn's LaserBall, the Robot Laser Charge (also the Ray Gun's shot, the robots' shots, and the base of the Ice
 * Ball and Acid). It flies as a thrown item for up to 200 ticks with a trail of firework sparks and red dust, and on a
 * hit does 16 thrown damage and sets the target on fire for 1 s, then bursts in smoke and sparks with an explosion sound.
 * The Ray Gun's shots ("special") have a bigger trail and also explode (power 3; blocks break only with mobGriefing).
 * Robots (OreSpawn's Robo-Pounder, -Gunner, -Warrior, -Sniper and Jeffery), a ridden Dragon and players riding
 * something aren't hurt.
 * <p>
 * Not ported yet: OreSpawn's Dragon type check (ice balls didn't hurt non-standard Dragons); add it when the Dragon is
 * ported.
 */
public class LaserBall extends ThrowableItemProjectile {
	private static final EntityDataAccessor<Boolean> SPECIAL = SynchedEntityData.defineId(LaserBall.class, EntityDataSerializers.BOOLEAN);
	private static final Set<String> ROBOTS = Set.of("robo_pounder", "robo_gunner", "robo_warrior", "robo_sniper", "jeffery");
	private int ticksAlive;
	/** OreSpawn just removed the ball, with no burst, when it touched something it spares. */
	private boolean spared;

	public LaserBall(EntityType<? extends LaserBall> type, Level level) {
		super(type, level);
	}

	protected boolean iceBall() {
		return false;
	}

	protected boolean acid() {
		return false;
	}

	/** The Ray Gun's shot. */
	public void setSpecial() {
		entityData.set(SPECIAL, true);
	}

	public boolean special() {
		return entityData.get(SPECIAL);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(SPECIAL, false);
	}

	@Override
	protected Item getDefaultItem() {
		return Weapons.ROBOT_LASER_CHARGE;
	}

	@Override
	public void tick() {
		if (++ticksAlive > 200) {
			if (!level().isClientSide()) discard();
			return;
		}
		super.tick();
		if (level().isClientSide() && !acid()) {
			int n = special() ? 10 : iceBall() ? 2 : 4;
			for (int i = 0; i < n; i++) {
				level().addParticle(ParticleTypes.FIREWORK, getX(), getY(), getZ(), random.nextGaussian() / 2.0, random.nextGaussian() / 2.0,
					random.nextGaussian() / 2.0);
				if (!iceBall()) {
					level().addParticle(new DustParticleOptions(0xFF0000, 1.0F), getX(), getY(), getZ(), random.nextGaussian() / 10.0,
						random.nextGaussian() / 10.0, random.nextGaussian() / 10.0);
				}
			}
		}
	}

	/** Whether this ball does nothing to {@code e} (it's just gone, without bursting). */
	protected boolean spares(Entity e) {
		if (acid() && (WeaponHooks.is(e, "jumpy_bug") || WeaponHooks.is(e, "spit_bug"))) return true;
		if (!iceBall() && !acid()) {
			for (String robot : ROBOTS) if (WeaponHooks.is(e, robot)) return true;
		}
		if (!acid() && WeaponHooks.is(e, "dragon") && e.isVehicle()) return true;
		return !acid() && e instanceof Player p && p.isPassenger();
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		if (!(level() instanceof ServerLevel level)) return;
		Entity e = result.getEntity();
		if (spares(e)) {
			spared = true;
			return;
		}
		e.hurtServer(level, damageSources().thrown(this, getOwner()), 16.0F);
		if (!iceBall()) e.igniteForSeconds(1.0F);
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		if (!(level() instanceof ServerLevel level)) return;
		if (spared) {
			discard();
			return;
		}
		if (!acid()) {
			level.broadcastEntityEvent(this, (byte) 3);
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 0.5F,
				1.0F + (random.nextFloat() - random.nextFloat()) * 0.5F);
			if (special() || iceBall()) level.explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.MOB);
		}
		afterImpact(level, result);
		discard();
	}

	/** IceBall's ice maker. */
	protected void afterImpact(ServerLevel level, HitResult result) {
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id != 3) {
			super.handleEntityEvent(id);
			return;
		}
		int n = special() ? 20 : 10;
		for (int i = 0; i < n; i++) {
			level().addParticle(ParticleTypes.SMOKE, getX() + random.nextFloat() - random.nextFloat(), getY() + random.nextFloat() - random.nextFloat(),
				getZ() + random.nextFloat(), 0.0, 0.0, 0.0);
			level().addParticle(ParticleTypes.LARGE_SMOKE, getX() + random.nextFloat() - random.nextFloat(),
				getY() + random.nextFloat() - random.nextFloat(), getZ() + random.nextFloat() - random.nextFloat(), 0.0, 0.0, 0.0);
			level().addParticle(ParticleTypes.FIREWORK, getX(), getY(), getZ(), random.nextGaussian(), random.nextGaussian(), random.nextGaussian());
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("Special", special());
		output.putInt("TicksAlive", ticksAlive);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		entityData.set(SPECIAL, input.getBooleanOr("Special", false));
		ticksAlive = input.getIntOr("TicksAlive", 0);
	}
}
