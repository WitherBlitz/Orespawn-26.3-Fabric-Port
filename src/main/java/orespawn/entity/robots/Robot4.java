package orespawn.entity.robots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;

/**
 * OreSpawn's Robot4, the Robo-Warrior: a 4-block robot with a shield arm and an arm cannon. It looks for prey within
 * 16 blocks (not monsters, ignorable bugs or creative players); up close it hits for 12 and knocks the target 2 blocks
 * away, further off it fires laser charges from its cannon (a special, exploding charge beyond 8 blocks). Once hit, it
 * shrugs off all damage for 65 ticks. 170 health, 18 armor, fire immune, ignores cactus; smokes from its back, and its
 * cannon glows while it fights. Drops 5-14 stacks of 4 Robot Laser Charges, a Ray Gun, an item frame and redstone
 * parts. Spawns at night at y 50 or higher, in the dark, with 6 blocks of open air above.
 * <p>
 * OreSpawn's model raised a "shielding" flag on the client only, so it never blocked anything; not ported.
 */
public class Robot4 extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Robot4.class, EntityDataSerializers.BOOLEAN);
	private int reload;
	private int wasAttacked;

	public Robot4(EntityType<? extends Robot4> type, Level level) {
		super(type, level);
		this.xpReward = 120;
		setPathfindingMalus(PathType.WATER, -1.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 170.0).add(Attributes.MOVEMENT_SPEED, 0.34)
			.add(Attributes.ATTACK_DAMAGE, 12.0).add(Attributes.ARMOR, 18.0);
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
	public void aiStep() {
		super.aiStep();
		if (!level().isClientSide()) return;
		if (random.nextInt(3) == 1) {
			double a = Math.toRadians(getYRot() + 180.0F);
			level().addParticle(ParticleTypes.SMOKE, getX() - 1.25 * Math.sin(a), getY() + 3.0 + random.nextFloat(), getZ() + 1.25 * Math.cos(a), 0.0,
				random.nextFloat() / 2.0, 0.0);
		}
		if (attacking()) {
			double a = Math.toRadians(getYRot() + 35.0F);
			float g = random.nextFloat();
			level().addParticle(new DustParticleOptions(0xFF0000 | (int) (g * 255.0F) << 8, 1.0F), getX() - 1.55 * Math.sin(a),
				getY() + 2.25 + random.nextFloat(), getZ() + 1.55 * Math.cos(a), 0.0, g, 0.0);
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (reload > 0) reload--;
		if (wasAttacked > 0) wasAttacked--;
		if (reload == 0 && random.nextInt(8) == 1) {
			if (random.nextInt(50) == 1) setTarget(null);
			LivingEntity e = getTarget();
			if (e != null && !e.isAlive()) {
				setTarget(null);
				e = null;
			}
			if (e == null) e = RobotUtil.find(this, 16.0, 4.0, x -> RobotUtil.isPrey(this, x));
			if (e != null) {
				lookAt(e, 10.0F, 10.0F);
				double d2 = distanceToSqr(e);
				if (d2 < 256.0) {
					float reach = 3.0F + e.getBbWidth() / 2.0F;
					if (d2 < reach * reach) {
						doHurtTarget(level, e);
					} else {
						if (RobotUtil.offAngle(this, e, yHeadRot) < 0.5) {
							double a = Math.toRadians(getYRot() + 45.0F);
							var ball = RobotUtil.laser(level, getX() - 1.75 * Math.sin(a), getY() + 2.0, getZ() + 1.75 * Math.cos(a), getYRot(), e,
								2.0F, 4.0F);
							if (d2 > 65.0) {
								if (ball != null) ball.setSpecial();
								reload = 30;
								playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 3.5F, 0.5F);
							} else {
								reload = 10;
								playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 2.5F, 1.0F);
							}
							if (ball != null) level.addFreshEntity(ball);
						}
						entityData.set(ATTACKING, true);
					}
					getNavigation().moveTo(e, 0.75);
				}
			}
		}
		if (reload <= 0 && wasAttacked <= 0) entityData.set(ATTACKING, false);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (target instanceof LivingEntity) RobotUtil.knockAway(this, target, 2.0, 0.12);
		return super.doHurtTarget(level, target);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return super.hurtServer(level, source, amount);
		if (source.is(DamageTypes.CACTUS) || wasAttacked != 0) return false;
		wasAttacked = 65;
		entityData.set(ATTACKING, true);
		boolean ret = super.hurtServer(level, source, amount);
		if (source.getEntity() instanceof Mob e && e != this) {
			setTarget(e);
			getNavigation().moveTo(e, 1.2);
			ret = true;
		}
		return ret;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		int n = 5 + random.nextInt(10);
		for (int i = 0; i < n; i++) RobotUtil.drop(level, this, ModItems.lazy("robot_laser_charge"), 4);
		RobotUtil.drop(level, this, ModItems.lazy("a_freakin_ray_gun"), 1);
		RobotUtil.drop(level, this, Items.ITEM_FRAME, 1);
		RobotUtil.parts(level, this, 10 + random.nextInt(15));
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

	/** Robot4.getCanSpawnHere: at y 50 or higher, at night, in the dark, with 6 blocks of air above. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return Robot2.tallRobotSpawn(level, reason, pos, random, 6);
	}
}
