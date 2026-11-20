package orespawn.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Comparator;

/**
 * OreSpawn's rat: small and quick, and it goes for almost anything alive within 9 blocks (not other rats, nor the
 * bugs and birds OreSpawn's hunters ignore, nor creative players), squeaking and whipping its tail while it hunts.
 * 5 health, 3 damage, 1 armor. Drops rotten flesh. OreSpawn's Rat Sword made pet rats that follow their owner; that
 * comes with the Rat Sword.
 */
public class Rat extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Rat.class, EntityDataSerializers.BOOLEAN);

	public Rat(EntityType<? extends Rat> type, Level level) {
		super(type, level);
		this.xpReward = 5;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.ARMOR, 1.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new PanicGoal(this, 1.35F));
		goalSelector.addGoal(2, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
		goalSelector.addGoal(3, WanderGoal.wanderALot(this, 10, 1.0));
		goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	public boolean attacking() {
		return entityData.get(ATTACKING);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
	}

	@Override
	public void jumpFromGround() {
		super.jumpFromGround();
		setDeltaMovement(getDeltaMovement().add(0.0, 0.25, 0.0));
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		if (random.nextInt(5) == 1) {
			LivingEntity prey = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(9.0, 2.0, 9.0), this::isPrey).stream()
				.min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
			if (prey != null) {
				entityData.set(ATTACKING, true);
				getNavigation().moveTo(prey, 1.25);
				if (distanceToSqr(prey) < 4.0 && (random.nextInt(8) == 0 || random.nextInt(7) == 1)) doHurtTarget(level, prey);
			} else {
				entityData.set(ATTACKING, false);
			}
		}
		if (random.nextInt(250) == 1) heal(1.0F);
	}

	private boolean isPrey(LivingEntity e) {
		if (e == this || !e.isAlive() || Ignored.byHunters(e) || e instanceof Rat) return false;
		if (!getSensing().hasLineOfSight(e)) return false;
		return !(e instanceof Player p) || !(p.isCreative() || p.isSpectator());
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return orespawn.registry.ModSounds.RAT_LIVE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return orespawn.registry.ModSounds.RAT_HIT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return orespawn.registry.ModSounds.RAT_DEAD;
	}

	@Override
	protected float getSoundVolume() {
		return 0.45F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}
}
