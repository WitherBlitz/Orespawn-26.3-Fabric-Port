package orespawn.entity;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * OreSpawn's dragonfly: a big darting flyer that picks spots 5-9 blocks away it can see, and flies at whatever hurt it.
 * Off peaceful, it has a 1 in 12 chance a tick of going after the nearest butterfly or moth, bird, mosquito, firefly or
 * horse (donkeys, mules and zombie and skeleton horses too, as 1.7.10's horse covered them; not llamas or camels)
 * within 10 blocks (6 up or down) that it can see, biting for 2 when within reach. OreSpawn's DragonflyHorseFriendly
 * and PlayNicely options both default to off, so it's ported that way. Drops a gold, uranium or titanium nugget (1 in
 * 6 each, 0-2 of it). Not ported yet: it hunted OreSpawn's ants too.
 */
public class Dragonfly extends Animal {
	private BlockPos flightTarget;

	public Dragonfly(EntityType<? extends Dragonfly> type, Level level) {
		super(type, level);
		this.moveControl = Flight.keepOwnSteering(this);
		this.xpReward = 5;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.33)
			.add(Attributes.ATTACK_DAMAGE, 2.0);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return ModSounds.get("dragonfly_living");
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("dragonfly_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("dragonfly_death");
	}

	@Override
	protected float getSoundVolume() {
		return 0.25F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	protected void doPush(Entity entity) {
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
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return false;
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}

	@Override
	public void tick() {
		super.tick();
		setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6, 1.0));
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		return target.hurtServer(level, damageSources().mobAttack(this), 2.0F);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		boolean hurt = super.hurtServer(level, source, damage);
		Entity attacker = source.getEntity();
		if (attacker != null && flightTarget != null) flightTarget = attacker.blockPosition();
		return hurt;
	}

	/** Dragonfly.canSeeTarget: nothing in the way from just above its feet to the corner of that block. */
	private boolean canSee(BlockPos p) {
		Vec3 from = new Vec3(getX(), getY() + 0.25, getZ());
		return level().clip(new ClipContext(from, Vec3.atLowerCornerOf(p), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType()
			== HitResult.Type.MISS;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (flightTarget == null) flightTarget = blockPosition();
		if (random.nextInt(300) != 0 && !(Flight.distSq(this, flightTarget) < 2.1)) {
			if (random.nextInt(12) == 0 && level.getDifficulty() != Difficulty.PEACEFUL) {
				LivingEntity prey = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(10.0, 6.0, 10.0), this::isPrey).stream()
					.min(Comparator.comparingDouble(this::targetWeight)).orElse(null);
				if (prey != null) {
					flightTarget = BlockPos.containing(prey.getX(), prey.getY() + 1.0, prey.getZ());
					if (distanceToSqr(prey) < 6.0) doHurtTarget(level, prey);
				}
			}
		} else {
			for (int tries = 50; tries > 0; tries--) {
				int dx = random.nextInt(5) + 5, dz = random.nextInt(5) + 5;
				if (random.nextInt(2) == 0) dz = -dz;
				if (random.nextInt(2) == 0) dx = -dx;
				BlockPos p = new BlockPos(getBlockX() + dx, getBlockY() + random.nextInt(5) - 2, getBlockZ() + dz);
				flightTarget = p;
				if (level.isEmptyBlock(p) && canSee(p)) break;
			}
		}
		Flight.steer(this, flightTarget, 0.5, 0.30000000149011613, 0.20000000149011612, 0.5, 1.0F, 0.25F);
	}

	/** Dragonfly.isSuitableTarget (OreSpawn's ants aren't ported yet). */
	private boolean isPrey(LivingEntity e) {
		if (e == this || !e.isAlive() || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Butterfly || e instanceof Bird || e instanceof Mosquito || e instanceof Firefly) return true;
		return e instanceof AbstractHorse && !(e instanceof Llama) && !(e instanceof Camel);
	}

	/** OreSpawn's GenericTargetSorter: distance squared, halved for creepers, divided by width x height when that's over 1. */
	private double targetWeight(LivingEntity e) {
		double d = distanceToSqr(e);
		if (e instanceof Creeper) d /= 2.0;
		double size = e.getBbHeight() * e.getBbWidth();
		return size > 1.0 ? d / size : d;
	}

	/** Dragonfly.getCanSpawnHere: by day, at y 50 or higher; 1.7.10's spawner kept mobs out of liquids, so: in air. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return level.getLevel().isBrightOutside() && pos.getY() >= 50 && level.isEmptyBlock(pos);
	}
}
