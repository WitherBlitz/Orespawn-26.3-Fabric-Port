package orespawn.entity.robots;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import orespawn.entity.Ignored;
import orespawn.item.tools.WrenchItem;

/**
 * OreSpawn's AntRobot, the Robot Red Ant: a six-legged machine. One built from a Red Ant Robot Kit is "owned" and can
 * be ridden (top speed 0.3 a move) and repaired with iron ingots; ridden, it stamps on things 6 to 9 blocks off and
 * bites what's in front of it within 6 blocks (30 damage). A wild one (from a spawn egg or a dungeon) can't be ridden
 * and hunts on its own: it scuttles toward anything alive within 24 blocks and bites it, stamping things nearby.
 * 300 health, 16 armor. Drops 7-13 picks of redstone parts and iron ingots. The Tools group's wrench packs robots back
 * into kits and reads and sets ownership through {@link WrenchItem.Owned}.
 * <p>
 * See {@link LeggedRobot} for the riding, and {@link RobotLegs} for the legs.
 */
public class AntRobot extends LeggedRobot implements WrenchItem.Owned {
	private boolean owned;

	public AntRobot(EntityType<? extends AntRobot> type, Level level) {
		super(type, level, RobotLegs.ANT);
		this.xpReward = 150;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 300.0).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, 30.0)
			.add(Attributes.ARMOR, 16.0);
	}

	/** Set by its kit, and by the Tools group's wrench when it packs a beaten wild one up. */
	@Override
	public void setOwned() {
		owned = true;
	}

	@Override
	public boolean isOwned() {
		return owned;
	}

	@Override
	protected boolean canBeRiddenBy(Player player) {
		return owned;
	}

	@Override
	protected double hoverHeight() {
		return 1.75;
	}

	@Override
	protected double riddenHoverHeight() {
		return 2.25;
	}

	@Override
	protected double maxSpeed() {
		return 0.3;
	}

	@Override
	protected int scanReach() {
		return 2;
	}

	@Override
	protected double scanLift() {
		return 0.02;
	}

	@Override
	protected double mountedHeight(Entity rider) {
		return 0.55 + Math.cos(rideTicker * 0.19F) * 0.02;
	}

	@Override
	protected float riderBehind() {
		return -1.25F;
	}

	@Override
	protected float rideSoundVolume() {
		return 0.35F;
	}

	@Override
	protected float mountSoundVolume() {
		return 0.45F;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (isVehicle() || owned || level.getDifficulty() == Difficulty.PEACEFUL) return;
		if (random.nextInt(20) == 0) feetAttack(level);
		if (random.nextInt(150) == 0) setTarget(null);
		LivingEntity e = getTarget();
		if (e != null && !e.isAlive()) {
			setTarget(null);
			e = null;
		}
		if (e == null) e = find(level, 2.0, false);
		if (e != null) {
			lookAt(e, 10.0F, 10.0F);
			if (distanceToSqr(e) > 16.0) {
				double dd = Math.atan2(e.getZ() - getZ(), e.getX() - getX());
				goThisWay(0.2 * Math.cos(dd), 0.2 * Math.sin(dd));
			}
		} else {
			setAttacking(false);
		}
		if (e != null && random.nextInt(15) == 0) {
			e = getTarget();
			if (e == null) e = find(level, 2.0, true);
			if (e != null) bite(level, e);
			else setAttacking(false);
		}
	}

	@Override
	protected void riddenAttacks(ServerLevel level) {
		if (random.nextInt(50) == 0) feetAttack(level);
		if (random.nextInt(9) == 0) {
			LivingEntity e = find(level, 1.0, true);
			if (e != null) {
				float reach = 6.0F + e.getBbWidth() / 2.0F;
				if (distanceToSqr(e) < reach * reach) {
					setAttacking(true);
					strike(level, e, (float) getAttributeValue(Attributes.ATTACK_DAMAGE), 0.7, 0.1);
				}
			} else {
				setAttacking(false);
			}
		}
	}

	private void bite(ServerLevel level, LivingEntity e) {
		float reach = 6.0F + e.getBbWidth() / 2.0F;
		if (distanceToSqr(e) < reach * reach) {
			setAttacking(true);
			strike(level, e, (float) getAttributeValue(Attributes.ATTACK_DAMAGE), 0.7, 0.1);
		} else {
			setAttacking(false);
		}
	}

	private boolean seen(LivingEntity e) {
		return e != this && e.isAlive() && !(e instanceof AntRobot) && !hasPassenger(e) && !Ignored.byHunters(e) && getSensing().hasLineOfSight(e);
	}

	private void feetAttack(ServerLevel level) {
		float attack = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
		for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(10.0, 8.0, 10.0), x -> {
			if (!seen(x)) return false;
			double d = Math.sqrt(distanceToSqr(x));
			if (d > 9.0 || d < 6.0) return false;
			return !(x instanceof Player p) || !(p.isCreative() || p.isSpectator());
		})) {
			strike(level, e, attack / 10.0F, 0.6, 0.1);
		}
	}

	private LivingEntity find(ServerLevel level, double distmul, boolean dircheck) {
		return level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(12.0 * distmul, 12.0, 12.0 * distmul), e -> {
			if (!seen(e)) return false;
			if (dircheck) {
				if (distanceToSqr(e) < 36.0) return true;
				if (RobotUtil.offAngle(this, e, getYRot()) > 0.75) return false;
			}
			return !(e instanceof Player p) || !(p.isCreative() || p.isSpectator());
		}).stream().findFirst().orElse(null);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean ret = super.hurtServer(level, source, amount);
		if (ret && source.getEntity() instanceof Mob e && e != this) {
			setTarget(e);
			lookAt(e, 20.0F, 20.0F);
		}
		return ret;
	}

	@Override
	protected void exhaust() {
		jet(4.0F, 80.0F, 90.0F, 0.5);
		jet(4.0F, 100.0F, 90.0F, 0.5);
	}

	@Override
	protected int jetRate(int kind) {
		return kind == 0 ? 18 : kind == 1 ? 7 : 16;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		ItemLike[] picks = {Items.REDSTONE, Items.REPEATER, Items.COMPARATOR, Items.REDSTONE_BLOCK, Items.DISPENSER, Items.STICKY_PISTON, Items.PISTON,
			Items.LEVER, Items.REDSTONE_BLOCK, Items.LIGHT_WEIGHTED_PRESSURE_PLATE, Items.IRON_INGOT};
		int n = 7 + random.nextInt(7);
		for (int i = 0; i < n; i++) {
			int k = random.nextInt(12);
			if (k < picks.length) RobotUtil.drop(level, this, picks[k], 1);
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("AntRobotOwned", owned);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		owned = input.getBooleanOr("AntRobotOwned", false);
	}
}
