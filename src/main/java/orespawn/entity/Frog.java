package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * OreSpawn's frog: big leaps forward now and then (and when hurt), croaks with its throat, breathes underwater, and
 * hunts bugs (butterflies, crickets, fireflies; ants, mosquitoes and small worms once they're ported), biting for 3 and
 * healing 1 when it eats one. Drops four slimeballs. OreSpawn's kiss (sneak-click a frog) is left out: it turned the
 * frog into the Girlfriend or Boyfriend mobs, which this pack skips.
 */
public class Frog extends Animal {
	private static final EntityDataAccessor<Boolean> SINGING = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.BOOLEAN);
	private int singing;
	private int jumpCount;

	public Frog(EntityType<? extends Frog> type, Level level) {
		super(type, level);
		this.xpReward = 5;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.1)
			.add(Attributes.ATTACK_DAMAGE, 0.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new PanicGoal(this, 1.4));
		goalSelector.addGoal(2, WanderGoal.wander(this, 1.0));
	}

	public boolean singing() {
		return entityData.get(SINGING);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(SINGING, false);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	private void jumpAround() {
		float f = 0.7F + Math.abs(random.nextFloat() * 0.75F);
		double d = Math.toRadians(getYRot());
		setDeltaMovement(getDeltaMovement().add(-f * Math.sin(d), 0.75F + Math.abs(random.nextFloat() * 0.55F), f * Math.cos(d)));
		setPos(getX(), getY() + 0.35F, getZ());
		needsSync = true;
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) return;
		if (singing != 0 && --singing <= 0) entityData.set(SINGING, false);
		if (jumpCount > 0) jumpCount--;
		if (jumpCount == 0 && random.nextInt(70) == 1) {
			jumpAround();
			jumpCount = 50;
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean hurt = super.hurtServer(level, source, amount);
		if (jumpCount <= 0) {
			jumpAround();
			jumpCount = 25;
		}
		return hurt;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(12) == 0 && level.getDifficulty() != Difficulty.PEACEFUL) {
			LivingEntity prey = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(8.0, 3.0, 8.0), this::isPrey).stream()
				.min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
			if (prey != null) {
				getNavigation().moveTo(prey, 1.25);
				if (distanceToSqr(prey) < 6.0) doHurtTarget(level, prey);
			}
		}
	}

	private boolean isPrey(LivingEntity e) {
		return e != this && e.isAlive() && getSensing().hasLineOfSight(e) && (e instanceof orespawn.entity.ants.Ant || e instanceof Butterfly || e instanceof Cricket || e instanceof Firefly || e instanceof Mosquito);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean hit = target.hurtServer(level, damageSources().mobAttack(this), 3.0F);
		if (!target.isAlive()) heal(1.0F);
		return hit;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		if (!level().isClientSide()) {
			if (random.nextInt(2) == 0) return null;
			singing = 35;
			entityData.set(SINGING, true);
		}
		// OreSpawn asked for "orespawn:frog", but never shipped that sound: its frogs croaked silently.
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.SCORPION_HIT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.BIG_SPLAT;
	}

	@Override
	protected float getSoundVolume() {
		return 0.7F;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isPersistenceRequired();
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		for (int i = 0; i < 4; i++) spawnAtLocation(level, new ItemStack(Items.SLIME_BALL), 1.0F);
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return false;
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}

	/** Frog.getCanSpawnHere: at y 50 or higher, by day, no more than 5 frogs within 20 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return pos.getY() >= 50 && level.getLevel().isBrightOutside()
			&& level.getEntitiesOfClass(Frog.class, new AABB(pos).inflate(20.0, 8.0, 20.0)).size() <= 5;
	}
}
