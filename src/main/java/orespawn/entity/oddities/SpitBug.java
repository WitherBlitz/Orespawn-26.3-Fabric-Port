package orespawn.entity.oddities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import orespawn.entity.Ignored;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Spit Bug (its "Blister Bug" texture): a big swamp beetle that comes out at night (and now and then by
 * day). It goes for the nearest creature within 12 blocks (not endermen, creepers or its own kind, nor creative
 * players), leaping at it, biting with a shove when close (get_mobstats("SpitBug", 100, 10, 12)), and otherwise
 * creeping up while spitting streams of acid. Falls and cactus don't hurt it, and a hit makes it immune for 15 ticks.
 * Heals slowly. Drops 1-3 amethysts.
 * <p>
 * The acid is OreSpawn's Acid from the group that ports it (found by id); until that's in, it doesn't spit.
 */
public class SpitBug extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(SpitBug.class, EntityDataSerializers.BOOLEAN);
	private int hurtTimer;
	private int streamCount;

	public SpitBug(EntityType<? extends SpitBug> type, Level level) {
		super(type, level);
		this.xpReward = 50;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 100.0).add(Attributes.MOVEMENT_SPEED, 0.33)
			.add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, 12.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 0.9, false, 4, () -> false));
		goalSelector.addGoal(2, WanderGoal.wanderALot(this, 14, 1.0));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));
		goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
	}

	public boolean attacking() {
		return entityData.get(ATTACKING);
	}

	@Override
	public void tick() {
		super.tick();
		if (!onGround()) getNavigation().stop();
	}

	/** SpitBug.jump: a big hop forward. */
	@Override
	public void jumpFromGround() {
		float f = 0.2F + Math.abs(random.nextFloat() * 0.45F);
		setDeltaMovement(getDeltaMovement().add(-f * Math.sin(Math.toRadians(getYHeadRot())), 0.75, f * Math.cos(Math.toRadians(getYHeadRot()))));
		setPos(getX(), getY() + 0.75, getZ());
		needsSync = true;
	}

	private void jumpAt(LivingEntity e) {
		float f = 0.2F + Math.abs(random.nextFloat() * 0.25F);
		double d = Math.atan2(e.getX() - getX(), e.getZ() - getZ());
		setDeltaMovement(getDeltaMovement().add(f * Math.sin(d), 0.75, f * Math.cos(d)));
		setPos(getX(), getY() + 0.75, getZ());
		needsSync = true;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return random.nextInt(4) == 0 ? ModSounds.get("clatter") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("crunch");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("emperorscorpion_death");
	}

	@Override
	protected float getSoundVolume() {
		return 0.75F;
	}

	@Override
	public float getVoicePitch() {
		return 1.5F;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isPersistenceRequired();
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (!super.doHurtTarget(level, target)) return false;
		if (target instanceof LivingEntity) {
			double inair = target.isRemoved() || target instanceof Player ? 0.2 : 0.1;
			float a = (float) Math.atan2(target.getZ() - getZ(), target.getX() - getX());
			target.push(Math.cos(a) * 0.5, inair, Math.sin(a) * 0.5);
		}
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (hurtTimer > 0) return false;
		if (source.is(DamageTypes.CACTUS) || source.is(DamageTypes.FALL)) return false;
		boolean ret = super.hurtServer(level, source, damage);
		hurtTimer = 15;
		if (source.getEntity() instanceof Mob e) {
			setTarget(e);
			getNavigation().moveTo(e, 1.2);
			ret = true;
		}
		return ret;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (hurtTimer > 0) hurtTimer--;
		if (random.nextInt(5) == 0) {
			LivingEntity e = getTarget();
			if (e != null && !e.isAlive()) {
				setTarget(null);
				e = null;
			}
			if (e == null) e = Oddity.nearest(this, 12.0, 7.0, this::isSuitableTarget);
			if (e != null) {
				lookAt(e, 10.0F, 10.0F);
				if (random.nextInt(15) == 1 && onGround()) {
					jumpAt(e);
				} else if (distanceToSqr(e) < 9.0) {
					entityData.set(ATTACKING, true);
					if (random.nextInt(6) == 0 || random.nextInt(7) == 1) {
						doHurtTarget(level, e);
						if (random.nextInt(3) != 1) e.playSound(ModSounds.get("clatter"), 1.0F, 1.0F);
					}
				} else if (onGround()) {
					getNavigation().moveTo(e, 0.5);
					spit(e);
				}
			} else {
				entityData.set(ATTACKING, false);
			}
		}
		if (random.nextInt(150) == 1 && getHealth() < getMaxHealth()) heal(1.0F);
	}

	/** watercanon: streams of 8 acid shots (OreSpawn's Acid, from the group that ports it), a new stream one time in 7. */
	private void spit(LivingEntity e) {
		if (streamCount > 0) {
			entityData.set(ATTACKING, true);
			double x = getX() - 1.5 * Math.sin(Math.toRadians(getYHeadRot())), y = getY() + 1.5, z = getZ() + 1.5 * Math.cos(Math.toRadians(getYRot()));
			Projectile acid = Oddity.orespawnProjectile("acid", this, x, y, z);
			if (acid != null) {
				double dx = e.getX() - x, dy = e.getY() + 0.25 - y, dz = e.getZ() - z;
				Oddity.throwAt(acid, dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.2F, dz, 1.1F, 6.0F);
				Oddity.bowSound(this);
			}
			streamCount--;
		} else {
			entityData.set(ATTACKING, false);
		}
		if (streamCount <= 0 && random.nextInt(7) == 1) streamCount = 8;
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || Ignored.byHunters(e) || !getSensing().hasLineOfSight(e) || Oddity.creative(e)) return false;
		if (e instanceof Enderman || e instanceof Creeper || e instanceof SpitBug || e instanceof TrooperBug) return false;
		return !Oddity.is(e, "ender_reaper", "ender_knight", "hydrolisc");
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		for (int i = 1 + random.nextInt(3); i > 0; i--) Oddity.dropRand(level, this, ModItems.AMETHYST, 3, 1.0);
	}

	/**
	 * SpitBug.getCanSpawnHere: at night (or one time in ten by day), dark enough for a monster, with 4 x 3 x 4 blocks of
	 * air above. Spawners always may.
	 */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (level.getLevel().isBrightOutside() && random.nextInt(20) > 1) return false;
		if (!Monster.isDarkEnoughToSpawn(level, pos, random)) return false;
		for (int k = -2; k < 2; k++) {
			for (int j = -2; j < 2; j++) {
				for (int i = 1; i < 4; i++) {
					if (!level.isEmptyBlock(pos.offset(j, i, k))) return false;
				}
			}
		}
		return true;
	}
}
