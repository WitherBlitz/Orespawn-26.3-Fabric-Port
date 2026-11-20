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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import orespawn.content.Robots;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;

/**
 * OreSpawn's GiantRobot, Jeffery: a 10-block walking robot. It looks for prey within 16 blocks (12 up and down; not
 * monsters, ignorable bugs or creative players) and, when it's ahead, fires laser charges from its head (an exploding
 * one beyond 10 blocks) and punches anything within 8 blocks for 40, throwing it over 2 blocks. 550 health, 18 armor,
 * fire immune, ignores cactus. Drops 15-29 stacks of 4 Robot Laser Charges and 10-19 picks from Spider and Red Ant Robot
 * Kits, Ray Guns and redstone parts. Spawns at night at y 50 or higher, in the dark, with 6 blocks of open air above.
 */
public class GiantRobot extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(GiantRobot.class, EntityDataSerializers.BOOLEAN);
	private int reload;

	public GiantRobot(EntityType<? extends GiantRobot> type, Level level) {
		super(type, level);
		this.xpReward = 275;
		setPathfindingMalus(PathType.WATER, -1.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 550.0).add(Attributes.MOVEMENT_SPEED, 0.55)
			.add(Attributes.ATTACK_DAMAGE, 40.0).add(Attributes.ARMOR, 18.0);
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
		if (reload > 0) reload--;
		if (random.nextInt(5) != 0) return;
		if (random.nextInt(100) == 1) setTarget(null);
		LivingEntity e = getTarget();
		if (e != null && !e.isAlive()) {
			setTarget(null);
			e = null;
		}
		if (e == null) e = RobotUtil.find(this, 16.0, 12.0, x -> RobotUtil.isPrey(this, x));
		if (e == null) {
			entityData.set(ATTACKING, false);
			return;
		}
		lookAt(e, 10.0F, 10.0F);
		double d2 = distanceToSqr(e);
		if (d2 >= 256.0) {
			entityData.set(ATTACKING, false);
			return;
		}
		if (RobotUtil.offAngle(this, e, yHeadRot) < 0.5) {
			if (reload == 0) {
				double xzoff = 3.75;
				var ball = RobotUtil.laser(level, getX() - xzoff * Math.sin(Math.toRadians(yHeadRot)), getY() + 10.0,
					getZ() + xzoff * Math.cos(Math.toRadians(yHeadRot)), yHeadRot, e, 2.0F, 4.0F);
				if (d2 > 100.0) {
					if (ball != null) ball.setSpecial();
					reload = 25;
					playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 3.5F, 0.5F);
				} else {
					reload = 10;
					playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 2.5F, 1.0F);
				}
				if (ball != null) level.addFreshEntity(ball);
			}
			float reach = 8.0F + e.getBbWidth() / 2.0F;
			if (d2 < reach * reach) {
				entityData.set(ATTACKING, true);
				doHurtTarget(level, e);
			} else {
				entityData.set(ATTACKING, false);
			}
		}
		getNavigation().moveTo(e, 0.5);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (!super.doHurtTarget(level, target)) return false;
		if (target instanceof LivingEntity) RobotUtil.knockAway(this, target, 2.2, 0.25);
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean ret = !source.is(DamageTypes.CACTUS) && super.hurtServer(level, source, amount);
		if (source.getEntity() instanceof Mob e && e != this) setTarget(e);
		return ret;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		int n = 15 + random.nextInt(15);
		for (int i = 0; i < n; i++) RobotUtil.drop(level, this, ModItems.lazy("robot_laser_charge"), 4);
		ItemLike[] picks = {Robots.SPIDER_ROBOT_KIT, Robots.RED_ANT_ROBOT_KIT, ModItems.lazy("a_freakin_ray_gun"), Items.REDSTONE_BLOCK,
			Items.DISPENSER, Items.STICKY_PISTON, Items.PISTON, Items.LEVER, Items.IRON_BLOCK, Items.DETECTOR_RAIL};
		n = 10 + random.nextInt(10);
		for (int i = 0; i < n; i++) {
			int k = random.nextInt(12);
			if (k < picks.length) RobotUtil.drop(level, this, picks[k], 1);
		}
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
		return 1.0F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/** GiantRobot.getCanSpawnHere: at y 50 or higher, at night, in the dark, with 6 blocks of air above. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return Robot2.tallRobotSpawn(level, reason, pos, random, 6);
	}
}
