package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * OreSpawn's skate, a small stinging ray. Like OreSpawn's other water creatures it swims at the surface: it floats
 * (OreSpawn's EntityAISwimming, here FloatGoal), wanders on ordinary paths over the water, which it doesn't avoid, and
 * breathes underwater. Now and then (1 in 8 a tick) it goes for the nearest player it can see within 10 blocks (not
 * creative or spectating ones), or whatever it's already after, and stings when within 2 blocks (8 damage, 4 armor, 8
 * health from OreSpawn's Skate stats). A mob that hurts it becomes its target; skates can't hurt each other. Out of
 * water it looks for water now and then (1 in 10 a tick, out to 11 blocks) and heads for it; with none in reach it loses
 * 1 health 1 time in 25 and vanishes, without drops, at 0. OreSpawn never spawned it naturally. Silent but for its death
 * sound. Drops 0-2 string (more with Looting; a loot table), the makings of OreSpawn's Skate String Bow.
 */
public class Skate extends Monster {
	public Skate(EntityType<? extends Skate> type, Level level) {
		super(type, level);
		this.xpReward = 10;
		setPathfindingMalus(PathType.WATER, 0.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 8.0).add(Attributes.ARMOR, 4.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, WanderGoal.wander(this, 1.0));
		goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(3, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
		super.travelInWater(Flounder.swimInput(input), baseGravity, isFalling, oldY);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		Entity attacker = source.getEntity();
		if (attacker instanceof Skate) return false;
		if (attacker instanceof Mob mob) {
			setTarget(mob);
			getNavigation().moveTo(mob, 1.2);
		}
		return super.hurtServer(level, source, amount);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (!isInWater() && random.nextInt(10) == 0 && !Flounder.seekWater(this, level, 12, 5, 1.33, 1.0F)) return;
		if (random.nextInt(8) == 1) {
			LivingEntity prey = findSomethingToAttack(level);
			if (prey != null) {
				if (distanceToSqr(prey) < 4.0) {
					if (random.nextInt(4) == 0 || random.nextInt(5) == 1) doHurtTarget(level, prey);
				} else {
					getNavigation().moveTo(prey, 1.2);
				}
			}
		}
	}

	/** Its current target while that lives, or else the nearest player it can see within 10 blocks (4 up or down). */
	private @Nullable LivingEntity findSomethingToAttack(ServerLevel level) {
		LivingEntity target = getTarget();
		if (target != null && target.isAlive()) return target;
		setTarget(null);
		return level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(10.0, 4.0, 10.0), this::isPrey).stream()
			.min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
	}

	private boolean isPrey(Player p) {
		return p.isAlive() && !p.isCreative() && !p.isSpectator() && getSensing().hasLineOfSight(p);
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
		return ModSounds.get("ratdead");
	}

	@Override
	protected float getSoundVolume() {
		return 0.33F;
	}

	@Override
	public float getVoicePitch() {
		return 1.75F;
	}

	/** OreSpawn's getCanSpawnHere replaced the monster darkness check; its rules are all in checkSpawn. */
	@Override
	public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
		return true;
	}

	/** It may appear in water (as 26.3's water animals may). */
	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
	}

	/** Skate.getCanSpawnHere: at y 50 or higher, by day, 1 time in 30, and no more than 6 skates within 16 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return pos.getY() >= 50 && level.getLevel().isBrightOutside() && random.nextInt(30) == 1
			&& level.getEntitiesOfClass(Skate.class, new AABB(pos).inflate(16.0, 8.0, 16.0)).size() <= 6;
	}
}
