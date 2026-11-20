package orespawn.entity.bugs;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Bugs;
import orespawn.entity.Flight;
import orespawn.entity.Ignored;
import orespawn.entity.ModEntities;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * OreSpawn's Brutalfly: a huge fire-proof butterfly of the night skies over mountains and mesas (and what a hurt
 * CaterKiller turns into). It soars about, never landing, and every few ticks hunts the nearest player within 30 blocks
 * (or a monster within 25), hovering above it and shooting fireballs: small ones on Easy, Brutalfly fireballs on Hard, half
 * and half on Normal; close up it bites. Each shot heals it a little, and it heals 1 every 5 s. 110 health, 10 damage,
 * 6 armor. It dies with a bang and leaves 53 gold nuggets and a cloud of 20 butterflies.
 * <p>
 * OreSpawn's dropFewItems also spawned 20 explosion particles on the server, where 1.7.10 never showed them; left out.
 * Not ported yet: Mothra and the Vortex (which it leaves alone), its Utopia dimension spawns.
 */
public class Brutalfly extends BugMonster {
	private @Nullable BlockPos flightTarget;
	private int lastX;
	private int lastY;
	private int lastZ;
	private int stuckCount;
	private int wingSound;
	private int healthTicker = 100;

	public Brutalfly(EntityType<? extends Brutalfly> type, Level level) {
		super(type, level);
		this.xpReward = 100;
		keepOwnSteering();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 110.0).add(Attributes.MOVEMENT_SPEED, 0.35)
			.add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, 6.0);
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.GENERIC_EXPLODE.value();
	}

	@Override
	protected float getSoundVolume() {
		return 1.5F;
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	/** collideWithEntity and collideWithNearbyEntities: it neither shoves nor is shoved. */
	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	protected void pushEntities() {
	}

	/** canTriggerWalking false. */
	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	/** doesEntityNotTriggerPressurePlate. */
	@Override
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6, 1.0));
		if (level().isClientSide()) return;
		if (++wingSound > 30) {
			SoundEvent wings = optionalSound("mothrawings");
			if (wings != null) playSound(wings, 1.0F, 1.0F);
			wingSound = 0;
		}
		if (--healthTicker <= 0) {
			if (getHealth() < getMaxHealth()) heal(1.0F);
			healthTicker = 100;
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (lastX == getBlockX() && lastY == getBlockY() && lastZ == getBlockZ()) {
			stuckCount++;
		} else {
			stuckCount = 0;
			lastX = getBlockX();
			lastY = getBlockY();
			lastZ = getBlockZ();
		}
		int shoot = level.getDifficulty() == Difficulty.HARD ? 2 : 3;
		if (flightTarget == null) flightTarget = blockPosition();
		if (stuckCount > 30 || random.nextInt(200) == 0 || Flight.distSq(this, flightTarget) < 9.0) {
			// Look down at the ground below it (a 3x3 grid, 5 blocks apart) and drop towards it if it's over 10 below.
			int dist = 20;
			for (int i = -5; i <= 5; i += 5) {
				for (int j = -5; j <= 5; j += 5) {
					for (int k = 1; k < 20; k++) {
						if (!level.getBlockState(new BlockPos(getBlockX() + j, getBlockY() - k, getBlockZ() + i)).isAir()) {
							if (k < dist) dist = k;
							break;
						}
					}
				}
			}
			int down = dist > 10 ? dist - 10 + 1 : 0;
			for (int tries = 30; tries > 0; tries--) {
				int sx = random.nextInt(2) == 0 ? -1 : 1, sz = random.nextInt(2) == 0 ? -1 : 1;
				int newz = (random.nextInt(20) + 8) * sz, newx = (random.nextInt(20) + 8) * sx;
				flightTarget = new BlockPos(getBlockX() + newx, getBlockY() + random.nextInt(7) - 1 - down, getBlockZ() + newz);
				if (level.isEmptyBlock(flightTarget) && canSee(flightTarget)) break;
			}
			stuckCount = 0;
		}
		if (random.nextInt(6) == 0) {
			Player target = level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(30.0, 20.0, 30.0), p -> true).stream()
				.min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
			if (target != null) {
				if (!isCreativeOrSpectator(target)) {
					if (getSensing().hasLineOfSight(target)) {
						flightTarget = BlockPos.containing(target.getX(), target.getY() + 4.0, target.getZ());
						if (random.nextInt(shoot) == 0) attackWithSomething(level, target);
					}
				} else {
					target = null;
				}
			}
			if (target == null && random.nextInt(3) == 0) {
				LivingEntity e = findTarget(25.0, 20.0, 25.0, this::isSuitableTarget);
				if (e != null) {
					flightTarget = BlockPos.containing(e.getX(), e.getY() + 5.0, e.getZ());
					if (distanceToSqr(e) > 25.0) {
						if (random.nextInt(shoot) == 0) attackWithSomething(level, e);
					} else {
						doHurtTarget(level, e);
					}
				}
			}
		}
		Flight.steer(this, flightTarget, 0.5, 0.30001, 0.20001, 0.5, 1.0F, 0.125F);
	}

	/** Fires from 2.25 blocks ahead of it at the target's middle, then heals 1. */
	private void attackWithSomething(ServerLevel level, LivingEntity target) {
		double cx = getX() - 2.25 * Math.sin(Math.toRadians(getYRot()));
		double cz = getZ() + 2.25 * Math.cos(Math.toRadians(getYRot()));
		Vec3 dir = new Vec3(target.getX() - cx, target.getY() + 0.55 - getY(), target.getZ() - cz);
		Difficulty difficulty = level.getDifficulty();
		if (difficulty == Difficulty.EASY || difficulty == Difficulty.NORMAL && random.nextInt(2) == 0) {
			// 1.7.10's EntitySmallFireball spread its aim by a gaussian 0.4 on each axis.
			Vec3 aim = dir.add(random.nextGaussian() * 0.4, random.nextGaussian() * 0.4, random.nextGaussian() * 0.4);
			SmallFireball sf = new SmallFireball(level, this, aim);
			sf.snapTo(cx, getY(), cz, getYRot(), 0.0F);
			playSound(SoundEvents.ARROW_SHOOT, 0.75F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
			level.addFreshEntity(sf);
		} else {
			BrutalflyFireball bf = new BrutalflyFireball(Bugs.BRUTALFLY_FIREBALL, level);
			bf.setOwner(this);
			bf.snapTo(cx, getY(), cz, getYRot(), 0.0F);
			bf.setDeltaMovement(dir.normalize().scale(bf.accelerationPower));
			playSound(SoundEvents.TNT_PRIMED, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
			level.addFreshEntity(bf);
		}
		if (getHealth() < getMaxHealth()) heal(1.0F);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		Entity e = source.getEntity();
		if (e instanceof Brutalfly) return false;
		boolean ret = super.hurtServer(level, source, amount);
		if (e != null && flightTarget != null) flightTarget = BlockPos.containing(e.getX(), e.getY() + 2.0, e.getZ());
		return ret;
	}

	/** Brutalfly.isSuitableTarget: monsters and players (not in creative) it can see. */
	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || e instanceof Brutalfly || Ignored.byHunters(e) || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Monster) return true;
		return e instanceof Player && !isCreativeOrSpectator(e);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		for (int i = 0; i < 53; i++) dropRand(level, Items.GOLD_NUGGET, 8);
		for (int i = 0; i < 20; i++) summon(level, ModEntities.BUTTERFLY, getX() + 0.5, getY() + 1.0, getZ() + 0.5);
	}

	/**
	 * Brutalfly.getCanSpawnHere: y 70 or higher, dark, at night, open air 6x9x8 above, and no other Brutalfly within 64
	 * blocks.
	 */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (pos.getY() < 70 || !isDarkEnoughToSpawn(level, pos, random) || level.getLevel().isBrightOutside()) return false;
		if (!allAir(level, pos, -3, 3, 1, 10, -4, 4)) return false;
		return noneNear(level, type, Brutalfly.class, pos, 64.0, 32.0, 64.0);
	}
}
