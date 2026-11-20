package orespawn.entity.terrors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.core.registries.BuiltInRegistries;
import orespawn.OreSpawn;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Alien: a tall, fast, leaping hunter of the deep (it steers hard in mid-air and jumps higher than most).
 * It hunts players only (not in creative): within 12 blocks it closes in, swiping when it's within 4, each hit knocking
 * them back and sometimes poisoning them. Whoever hurts it becomes its target. It hates light: now and then it finds the
 * nearest torch within 14 blocks, walks to it and, close up, knocks it out (if mob griefing is on). Cactus can't hurt it,
 * it heals slowly, and acid drips from its jaws. 100 health, 12 damage, 8 armor. Drops 5-10 spider eyes, 5-10 flint, a
 * map, a clock and a compass (loot table).
 * <p>
 * Not ported: OreSpawn's fire resistance setting (no 26.3 equivalent), its spawner-name check and its Islands dimension
 * exception (that dimension isn't ported). OreSpawn spawned it only in its Extreme (mining) dimension.
 */
public class Alien extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Alien.class, EntityDataSerializers.BOOLEAN);
	// OreSpawn's RenderInfo (client side): which claws, tail and jaw move this cycle.
	private int ri1;
	private int ri2;
	private int ri3;

	public Alien(EntityType<? extends Alien> type, Level level) {
		super(type, level);
		this.xpReward = 100;
	}

	/** get_mobstats("Alien", 100, 12, 8); speed 0.65. */
	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 100.0).add(Attributes.MOVEMENT_SPEED, 0.65)
			.add(Attributes.ATTACK_DAMAGE, 12.0).add(Attributes.ARMOR, 8.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
		goalSelector.addGoal(2, WanderGoal.wanderALot(this, 10, 1.0));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
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

	/**
	 * The model's random claw, tail and jaw choices (OreSpawn's ModelAlien kept them per creature): re-rolled each time
	 * the jaw's swing (3.5 times {@code speed} a tick) turns back up. Returns ri1 + 16 * ri2 + 256 * ri3.
	 */
	public int animBits(float ageInTicks, float speed) {
		float now = Mth.cos(ageInTicks * 3.5F * speed);
		float next = Mth.cos((ageInTicks + 0.2F) * 3.5F * speed);
		if (next > 0.0F && now < 0.0F) {
			if (!attacking()) {
				ri1 = random.nextInt(15);
				ri2 = random.nextInt(15);
				ri3 = random.nextInt(15);
			} else {
				ri1 = random.nextInt(4);
				ri2 = random.nextInt(2);
				ri3 = 1;
			}
		}
		return ri1 | ri2 << 4 | ri3 << 8;
	}

	/** OreSpawn's jumpMovementFactor of 0.6: it steers hard in the air. */
	@Override
	protected float getFlyingSpeed() {
		return 0.6F;
	}

	@Override
	public void jumpFromGround() {
		super.jumpFromGround();
		setDeltaMovement(getDeltaMovement().add(0.0, 0.25, 0.0));
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (level().isClientSide() && random.nextInt(20) == 1) {
			float f = 1.7F + Math.abs(random.nextFloat() * 0.75F);
			double yaw = Math.toRadians(yHeadRot);
			level().addParticle(ParticleTypes.DRIPPING_LAVA, getX() - f * Math.sin(yaw), getY() + 1.6, getZ() + f * Math.cos(yaw), 0.0, 0.0, 0.0);
		}
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (!super.doHurtTarget(level, target)) return false;
		if (target instanceof LivingEntity living) {
			// OreSpawn meant 8/10/12 by difficulty, but its check only ever gave 8 on easy and 6 otherwise.
			int seconds = level.getDifficulty() == Difficulty.EASY ? 8 : 6;
			if (random.nextInt(5) == 1) living.addEffect(new MobEffectInstance(MobEffects.POISON, seconds * 5, 0));
			TerrorAi.fling(this, target, 1.1, 0.1);
		}
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypes.CACTUS)) return false;
		boolean hurt = super.hurtServer(level, source, damage);
		if (source.getEntity() instanceof Mob attacker) {
			setTarget(attacker);
			setLastHurtByMob(attacker);
			getNavigation().moveTo(attacker, 1.2);
			hurt = true;
		}
		return hurt;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(8) == 0) {
			LivingEntity e = findSomethingToAttack();
			if (e != null) {
				lookAt(e, 10.0F, 10.0F);
				if (distanceToSqr(e) < 16.0) {
					entityData.set(ATTACKING, true);
					if (random.nextInt(4) == 0 || random.nextInt(5) == 1) doHurtTarget(level, e);
				}
				getNavigation().moveTo(e, 1.2);
			} else {
				entityData.set(ATTACKING, false);
			}
		} else if (random.nextInt(30) == 0) {
			// It puts out the nearest torch.
			int[] best = {0, 0, 0, 99999};
			for (int i = 2; i < 15; i++) {
				if (TerrorAi.scanShell(level, getBlockX(), getBlockY(), getBlockZ(), i, i, i, Alien::isTorch, best)) break;
				if (i >= 10) i++;
			}
			if (best[3] < 99999) {
				getNavigation().moveTo(best[0], best[1], best[2], 1.0);
				if (best[3] < 27 && level.getGameRules().get(GameRules.MOB_GRIEFING)) {
					level.setBlock(new BlockPos(best[0], best[1], best[2]), Blocks.AIR.defaultBlockState(), 2);
				}
			}
		}
		if (random.nextInt(40) == 1 && getHealth() < getMaxHealth()) heal(1.0F);
	}

	/** Blocks.torch (standing or on a wall) and OreSpawn's Extreme Torch. */
	private static boolean isTorch(BlockState state) {
		if (state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH)) return true;
		Block extreme = BuiltInRegistries.BLOCK.getValue(OreSpawn.id("extreme_torch"));
		return extreme != Blocks.AIR && state.is(extreme);
	}

	/** Its target if it has a living one, else the nearest player within 12 blocks (not in creative). */
	private @Nullable LivingEntity findSomethingToAttack() {
		LivingEntity current = getTarget();
		if (current != null && current.isAlive()) return current;
		setTarget(null);
		return TerrorAi.findTarget(this, 12.0, 4.0, e -> e instanceof Player && !TerrorAi.protectedPlayer(e));
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return random.nextInt(4) == 0 ? ModSounds.get("alien_living") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("alien_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("alien_death");
	}

	@Override
	protected float getSoundVolume() {
		return 1.0F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/** Alien.getCanSpawnHere: dark enough, at y 50 or lower, with a 3 x 3 x 3 space of air above it. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!Monster.isDarkEnoughToSpawn(level, pos, random)) return false;
		if (pos.getY() > 50) return false;
		return TerrorAi.airAbove(level, pos, -1, 1, -1, 1, 1, 3);
	}
}
