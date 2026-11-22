package orespawn.entity.robots;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import orespawn.entity.bosses.BossUtil;
import orespawn.entity.Ignored;

/**
 * OreSpawn's SpiderRobot, the Robot Spider (Giant Spider Robot): an eight-legged machine with legs over 6 blocks long,
 * built from a Spider Robot Kit or driven by a Spider Driver. Ridden, it stands about 4 blocks up, reaches 0.45 a move
 * (about 9 blocks a second), and fights for its rider: now and then it stamps everything 12 to 18 blocks off (10
 * damage, a small shove), and it bites what's in front of it within 12 blocks (100 damage, thrown over a block). It
 * never attacks spiders, Spider Drivers or other spider robots. 1500 health, 16 armor. Drops 14-27 redstone parts.
 * <p>
 * See {@link LeggedRobot} for the riding, and {@link RobotLegs} for the legs.
 */
public class SpiderRobot extends LeggedRobot {
	/** OreSpawn's health: over 26.3's own cap of 1024, so it has 1024 unless the cap is raised (AttributeFix). */
	public static final float HEALTH = 1500.0F;

	public SpiderRobot(EntityType<? extends SpiderRobot> type, Level level) {
		super(type, level, RobotLegs.SPIDER);
		this.xpReward = 750;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, HEALTH).add(Attributes.MOVEMENT_SPEED, 0.35).add(Attributes.ATTACK_DAMAGE, 100.0)
			.add(Attributes.ARMOR, 16.0);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		BossUtil.saveMaxHealth(this, output);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		// Saved under a different cap (AttributeFix added or taken out): the same fraction of its health.
		BossUtil.keepHealthFraction(this, input, HEALTH);
	}

	@Override
	protected double hoverHeight() {
		return 1.55;
	}

	@Override
	protected double riddenHoverHeight() {
		return 4.25;
	}

	@Override
	protected double maxSpeed() {
		return 0.45;
	}

	@Override
	protected int scanReach() {
		return 3;
	}

	@Override
	protected double scanLift() {
		return 0.03;
	}

	@Override
	protected double mountedHeight(Entity rider) {
		return rider instanceof SpiderDriver ? 2.0 : 2.625 + Math.cos(rideTicker * 0.19F) * 0.02;
	}

	@Override
	protected float riderBehind() {
		return -3.0F;
	}

	@Override
	protected float rideSoundVolume() {
		return 0.45F;
	}

	@Override
	protected float mountSoundVolume() {
		return 0.65F;
	}

	private boolean friend(LivingEntity e) {
		return e == this || e instanceof SpiderRobot || e instanceof Spider || hasPassenger(e);
	}

	@Override
	protected void riddenAttacks(ServerLevel level) {
		float attack = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
		if (random.nextInt(40) == 0) {
			for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(20.0, 8.0, 20.0), this::feetTarget)) {
				strike(level, e, attack / 10.0F, 0.6, 0.1);
			}
		}
		if (random.nextInt(15) == 0) {
			LivingEntity e = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(20.0, 12.0, 20.0), this::biteTarget).stream()
				.findFirst().orElse(null);
			if (e != null) {
				float reach = 12.0F + e.getBbWidth() / 2.0F;
				if (distanceToSqr(e) < reach * reach) {
					setAttacking(true);
					strike(level, e, attack, 1.2, 0.15);
				}
			} else {
				setAttacking(false);
			}
		}
	}

	private boolean feetTarget(LivingEntity e) {
		if (!e.isAlive() || friend(e)) return false;
		double d = Math.sqrt(distanceToSqr(e));
		if (d > 18.0 || d < 12.0) return false;
		return !(e instanceof Player p) || !(p.isCreative() || p.isSpectator());
	}

	private boolean biteTarget(LivingEntity e) {
		if (!e.isAlive() || friend(e) || Ignored.byHunters(e) || !getSensing().hasLineOfSight(e)) return false;
		if (distanceToSqr(e) < 36.0) return true;
		if (RobotUtil.offAngle(this, e, getYRot()) > 0.75) return false;
		return !(e instanceof Player p) || !(p.isCreative() || p.isSpectator());
	}

	@Override
	protected void exhaust() {
		jet(8.0F, 90.0F, 90.0F, 2.0);
	}

	@Override
	protected int jetRate(int kind) {
		return kind == 0 ? 8 : kind == 1 ? 2 : 10;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		RobotUtil.parts(level, this, 14 + random.nextInt(14));
	}
}
