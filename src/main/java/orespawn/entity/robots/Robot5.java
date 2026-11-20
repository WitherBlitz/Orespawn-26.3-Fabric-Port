package orespawn.entity.robots;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.level.pathfinder.PathType;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;

/**
 * OreSpawn's Robot5, the Robo-Sniper: a small gun on wheels that, once a second, looks for prey within 30 blocks (not
 * monsters, ignorable bugs or creative players) and fires a laser charge when it's straight ahead, rolling closer
 * while it's more than 6 blocks off. 20 health, 5 attack, 6 armor, fire immune, ignores cactus; quiet (volume 0.5).
 * Drops 5-10 stacks of 4 Robot Laser Charges and a few redstone parts. Spawns at night at y 50 or higher, in the dark,
 * with 2 blocks of open air above.
 */
public class Robot5 extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Robot5.class, EntityDataSerializers.BOOLEAN);
	private int reload;

	public Robot5(EntityType<? extends Robot5> type, Level level) {
		super(type, level);
		this.xpReward = 20;
		setPathfindingMalus(PathType.WATER, -1.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.3)
			.add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.ARMOR, 6.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, WanderGoal.wanderALot(this, 14, 1.0));
		goalSelector.addGoal(2, new MoveThroughVillageGoal(this, 0.9, false, 4, () -> false));
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

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (reload > 0) {
			reload--;
			if (reload < 15) entityData.set(ATTACKING, false);
		}
		if (reload != 0) return;
		if (random.nextInt(50) == 1) setTarget(null);
		LivingEntity e = getTarget();
		if (e != null && !e.isAlive()) {
			setTarget(null);
			e = null;
		}
		if (e == null) e = RobotUtil.find(this, 30.0, 6.0, x -> RobotUtil.isPrey(this, x));
		reload = 20;
		if (e == null) {
			entityData.set(ATTACKING, false);
			return;
		}
		lookAt(e, 10.0F, 10.0F);
		double d2 = distanceToSqr(e);
		if (d2 < 900.0) {
			if (RobotUtil.offAngle(this, e, yHeadRot) < 0.5) {
				double off = 1.6;
				var ball = RobotUtil.laser(level, getX() - off * Math.sin(Math.toRadians(yHeadRot)), getY() + off,
					getZ() + off * Math.cos(Math.toRadians(yHeadRot)), yHeadRot, e, 1.4F, 5.0F);
				playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 3.0F, 1.0F);
				if (ball != null) level.addFreshEntity(ball);
				entityData.set(ATTACKING, true);
			}
			if (d2 > 36.0) getNavigation().moveTo(e, 0.5);
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (source.is(DamageTypes.CACTUS)) return false;
		return super.hurtServer(level, source, amount);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		int n = 5 + random.nextInt(6);
		for (int i = 0; i < n; i++) RobotUtil.drop(level, this, ModItems.lazy("robot_laser_charge"), 4);
		RobotUtil.parts(level, this, 2 + random.nextInt(5));
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return random.nextInt(4) == 0 ? ModSounds.get("robot_living") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("robot_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("robot_death");
	}

	@Override
	protected float getSoundVolume() {
		return 0.5F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/** Robot5.getCanSpawnHere: at y 50 or higher, at night, in the dark, with 2 blocks of air above. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return Robot2.tallRobotSpawn(level, reason, pos, random, 3);
	}
}
