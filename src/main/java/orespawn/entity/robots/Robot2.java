package orespawn.entity.robots;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathType;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;

/**
 * OreSpawn's Robot2, the Robo-Pounder: a 6-block robot that stomps after anything alive within 14 blocks (not
 * monsters, ignorable bugs or creative players). Once its prey is within 5 blocks in front of it, it pounds the
 * ground with both arms: some swings hit (22 damage) and dig out the blocks under the target, and every swing smashes
 * blocks around it (with mob griefing; never obsidian, bedrock, quartz, spawners, redstone or iron blocks, chests, or
 * anything unbreakable). Now and then it pounds the ground just for fun. 200 health, 18 armor, fire immune.
 * Drops 2-9 iron blocks, 5-10 iron ingots and a handful of redstone parts. Spawns at night at y 50 or higher, in the
 * dark, with 6 blocks of open air above.
 */
public class Robot2 extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Robot2.class, EntityDataSerializers.BOOLEAN);
	private int justForFun;
	/** Client side: which arms swing this cycle (RenderInfo.ri1 in OreSpawn's model: 1 right, 2 left, 3 both). */
	public int armChoice;

	public Robot2(EntityType<? extends Robot2> type, Level level) {
		super(type, level);
		this.xpReward = 100;
		setPathfindingMalus(PathType.WATER, -1.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.MOVEMENT_SPEED, 0.3)
			.add(Attributes.ATTACK_DAMAGE, 22.0).add(Attributes.ARMOR, 18.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, WanderGoal.wanderALot(this, 14, 1.0));
		goalSelector.addGoal(2, new MoveThroughVillageGoal(this, 0.9, false, 4, () -> false));
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

	private void setAttacking(boolean b) {
		entityData.set(ATTACKING, b);
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(6) == 1) {
			if (random.nextInt(50) == 1) setTarget(null);
			LivingEntity e = getTarget();
			if (e != null && !e.isAlive()) {
				setTarget(null);
				e = null;
			}
			if (e == null) e = RobotUtil.find(this, 14.0, 3.0, x -> RobotUtil.isPrey(this, x));
			if (e != null) {
				double rdd = RobotUtil.offAngle(this, e, getYRot());
				lookAt(e, 10.0F, 10.0F);
				if (rdd < 1.25) {
					float reach = 5.0F + e.getBbWidth() / 2.0F;
					if (distanceToSqr(e) < reach * reach) {
						setAttacking(true);
						if (random.nextInt(5) == 0 || random.nextInt(6) == 1) {
							doHurtTarget(level, e);
							for (int i = 0; i < 6; i++) destroyBlockUnder(level, e);
						}
						destroyNearbyBlocks(level);
					}
				} else {
					setAttacking(false);
				}
				getNavigation().moveTo(e, 1.0);
			} else {
				setAttacking(false);
			}
		}
		if (!attacking()) {
			if (random.nextInt(450) == 1) justForFun = 50;
			if (justForFun > 0) justForFun--;
			if (justForFun > 0) {
				setAttacking(true);
				if (random.nextInt(3) == 1) destroyNearbyBlocks(level);
			} else {
				setAttacking(false);
			}
		}
	}

	private void destroyBlockUnder(ServerLevel level, LivingEntity e) {
		BlockPos pos = RobotUtil.floor(e.getX() + random.nextFloat() - random.nextFloat(), e.getY() - 1.0,
			e.getZ() + random.nextFloat() - random.nextFloat());
		smash(level, pos);
	}

	private void destroyNearbyBlocks(ServerLevel level) {
		for (int i = 0; i < 50; i++) {
			smash(level, RobotUtil.floor(getX() + random.nextFloat() * 6.5 - random.nextFloat() * 6.5, getY() + 0.1 + random.nextFloat() * 8.5,
				getZ() + random.nextFloat() * 6.5 - random.nextFloat() * 6.5));
		}
	}

	private void smash(ServerLevel level, BlockPos pos) {
		BlockState s = level.getBlockState(pos);
		if (RobotUtil.smashable(level, pos, s) && level.getGameRules().get(GameRules.MOB_GRIEFING)) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (source.is(DamageTypes.CACTUS)) return false;
		boolean ret = super.hurtServer(level, source, amount);
		if (source.getEntity() instanceof Mob e && e != this) {
			setTarget(e);
			getNavigation().moveTo(e, 1.2);
		}
		return ret;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		int n = 2 + random.nextInt(8);
		for (int i = 0; i < n; i++) RobotUtil.drop(level, this, Items.IRON_BLOCK, 1);
		n = 5 + random.nextInt(6);
		for (int i = 0; i < n; i++) RobotUtil.drop(level, this, Items.IRON_INGOT, 1);
		RobotUtil.parts(level, this, 5 + random.nextInt(10));
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

	/** Robot2.getCanSpawnHere: at y 50 or higher, at night, in the dark, with 6 blocks of air above. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return tallRobotSpawn(level, reason, pos, random, 6);
	}

	static boolean tallRobotSpawn(ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random, int height) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (pos.getY() < 50 || level.getLevel().isBrightOutside()) return false;
		return RobotUtil.clearAbove(level, pos, height) && Monster.isDarkEnoughToSpawn(level, pos, random);
	}
}
