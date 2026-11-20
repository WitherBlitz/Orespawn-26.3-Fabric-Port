package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * OreSpawn's firefly: drifts slowly about at night, blinking (full brightness for the first half of each 20-39 tick
 * cycle). During the day, a firefly nobody named has a 1 in 500 chance each tick of vanishing. Drops 0-2 Extreme
 * Torches (more with Looting).
 */
public class Firefly extends AmbientCreature {
	private final int blinkCycle;
	private int blinker;
	private BlockPos flightTarget;

	public Firefly(EntityType<? extends Firefly> type, Level level) {
		super(type, level);
		this.moveControl = Flight.keepOwnSteering(this);
		blinkCycle = 20 + random.nextInt(20);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.MOVEMENT_SPEED, 0.1).add(Attributes.ATTACK_DAMAGE, 0.0);
	}

	/** Firefly.getBlink: lit for the first half of the cycle. */
	public boolean lit() {
		return blinker < blinkCycle / 2;
	}

	@Override
	protected float getSoundVolume() {
		return 0.0F;
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	protected void pushEntities() {
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return level().isBrightOutside() && !isPersistenceRequired();
	}

	@Override
	public void tick() {
		super.tick();
		setDeltaMovement(getDeltaMovement().multiply(1.0, 0.600000023841, 1.0));
		if (++blinker > blinkCycle) blinker = 0;
		if (!level().isClientSide() && !isPersistenceRequired() && level().getOverworldClockTime() % 24000L <= 11000L && random.nextInt(500) == 1) {
			discard();
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (flightTarget == null) flightTarget = blockPosition();
		if (random.nextInt(40) == 0 || Flight.distSq(this, flightTarget) < 2.0) flightTarget = Flight.pickAir(this, 4, 25);
		Flight.steer(this, flightTarget, 0.2, 0.1, 0.1, 0.5, 0.2F, 0.25F);
	}

	/** Firefly.getCanSpawnHere: in air, at night, no more than 10 fireflies within 20 blocks, at y 50 or higher. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!level.isEmptyBlock(pos) || level.getLevel().isBrightOutside()) return false;
		if (pos.getY() < 50 && level.getLevel().dimension() != orespawn.world.OreSpawnDimensions.ISLANDS) return false;
		return level.getEntitiesOfClass(Firefly.class, new AABB(pos).inflate(20.0, 8.0, 20.0)).size() <= 10;
	}
}
