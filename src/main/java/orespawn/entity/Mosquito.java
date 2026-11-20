package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * OreSpawn's mosquito (EntityMosquito): a tiny whining flyer. Each time it picks a new spot (a 1 in 20 chance a tick,
 * or on getting within reach of the old one), one time in four it looks for the nearest player within 10 blocks (6 up
 * or down) and flies to 2 blocks above their feet, so it hangs about just over their head; otherwise it flits to an air
 * block nearby. It never bites: nothing in OreSpawn hurts anyone for it. It's silent when hit or killed, and drops
 * nothing. Not ported yet: OreSpawn's Mosquito Plant crop, which grew wild in jungles and swamps and hatched 2-6
 * mosquitoes at a time.
 */
public class Mosquito extends AmbientCreature {
	private BlockPos flightTarget;

	public Mosquito(EntityType<? extends Mosquito> type, Level level) {
		super(type, level);
		this.moveControl = Flight.keepOwnSteering(this);
		this.xpReward = 5;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 2.0).add(Attributes.MOVEMENT_SPEED, 0.1).add(Attributes.ATTACK_DAMAGE, 0.0);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.get("mosquito");
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	protected @Nullable SoundEvent getDeathSound() {
		return null;
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	public float getVoicePitch() {
		return 1.5F;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	protected void pushEntities() {
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isPersistenceRequired();
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
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
	public void tick() {
		super.tick();
		setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6F, 1.0));
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (flightTarget == null) flightTarget = blockPosition();
		if (random.nextInt(20) == 0 || Flight.distSq(this, flightTarget) < 3.0) {
			Player player = random.nextInt(4) != 0 ? null
				: level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(10.0, 6.0, 10.0), p -> !p.isSpectator()).stream()
					.min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
			flightTarget = player != null ? player.blockPosition().above(2) : Flight.pickAir(this, 6, 50);
		}
		Flight.steer(this, flightTarget, 0.5, 0.1, 0.1, 0.5, 0.3F, 1.0F);
	}

	/** EntityMosquito.getCanSpawnHere was always yes; 1.7.10's spawner still kept mobs out of liquids, so: in air. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return EntitySpawnReason.isSpawner(reason) || level.isEmptyBlock(pos);
	}
}
