package orespawn.entity.robots;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import orespawn.entity.Ignored;
import orespawn.entity.WanderGoal;

import java.util.Comparator;

/**
 * OreSpawn's SpiderDriver, the Spider Driver: a spider that pilots Robot Spiders. It wanders and looks about, walks
 * to the nearest unridden Robot Spider within 25 blocks and climbs on, then steers it toward anything alive within
 * 35 blocks (not spiders, robot spiders or other drivers, nor anything within 6 blocks), letting the robot do the
 * fighting. It panics when hurt and never attacks by itself (OreSpawn's own AI tasks replaced the spider's attack).
 * 16 health, armor 20 on foot and 8 while driving. Drops string (loot table) and, killed by a player, sometimes a
 * spider eye. Spawns as a spider does, or anywhere near a Robot Spider; it doesn't despawn while driving.
 */
public class SpiderDriver extends Spider {
	public SpiderDriver(EntityType<? extends SpiderDriver> type, Level level) {
		super(type, level);
	}

	/** EntitySpider's 1.7.10 attributes: 16 health, speed 0.8 (with OreSpawn's AI tasks, which scale it). */
	public static AttributeSupplier.Builder createAttributes() {
		return Spider.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.8);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(1, new FloatGoal(this));
		goalSelector.addGoal(2, new PanicGoal(this, 1.5));
		goalSelector.addGoal(3, WanderGoal.wander(this, 0.65F));
		goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isPassenger() && super.removeWhenFarAway(distSqr);
	}

	@Override
	public int getArmorValue() {
		return isPassenger() ? 8 : 20;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (level.getDifficulty() == Difficulty.PEACEFUL) return;
		if (random.nextInt(5) == 0 && !isPassenger()) {
			SpiderRobot robot = level.getEntitiesOfClass(SpiderRobot.class, getBoundingBox().inflate(25.0, 15.0, 25.0), r -> !r.isVehicle()).stream()
				.min(Comparator.comparingDouble(r -> RobotUtil.sortKey(this, r))).orElse(null);
			if (robot != null) {
				lookAt(robot, 10.0F, 10.0F);
				float reach = 4.0F + robot.getBbWidth() / 2.0F;
				if (distanceToSqr(robot) < reach * reach) startRiding(robot);
				else getNavigation().moveTo(robot, 0.55);
			}
		}
		if (random.nextInt(4) == 0 && getVehicle() instanceof SpiderRobot robot) {
			LivingEntity e = RobotUtil.find(this, 35.0, 15.0, this::isPrey);
			if (e != null) {
				lookAt(e, 10.0F, 10.0F);
				float reach = 11.0F + e.getBbWidth() / 2.0F;
				if (distanceToSqr(e) >= reach * reach) {
					double dd = Math.atan2(e.getZ() - getZ(), e.getX() - getX());
					robot.goThisWay(0.35 * Math.cos(dd), 0.35 * Math.sin(dd));
				}
			}
		}
	}

	private boolean isPrey(LivingEntity e) {
		if (e == this || !e.isAlive() || Ignored.byHunters(e)) return false;
		if (e instanceof SpiderRobot || e instanceof Spider) return false;
		if (!getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Player p) return !(p.isCreative() || p.isSpectator());
		return distanceToSqr(e) >= 36.0;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		// EntitySpider.dropFewItems: a spider eye if a player killed it, 1 in 3 (Looting doesn't change this here).
		if (killedByPlayer && random.nextInt(3) == 0) spawnAtLocation(level, Items.SPIDER_EYE);
	}

	/** SpiderDriver.getCanSpawnHere: always next to a Robot Spider (within 24 blocks), otherwise as a spider. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!level.getEntitiesOfClass(SpiderRobot.class, new AABB(pos).inflate(24.0, 12.0, 24.0)).isEmpty()) return true;
		return Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
	}
}
