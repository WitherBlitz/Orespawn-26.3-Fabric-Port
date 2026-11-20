package orespawn.entity.spooky;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import orespawn.entity.Flight;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * OreSpawn's Ghost: a harmless, see-through spirit that drifts through walls (it ignores collisions until it's made
 * persistent, and suffocation can't hurt it) and floats over to the nearest player within 16 blocks, otherwise wanders
 * a few blocks above the ground. 2 health, silent but for a moan now and then. Only out at night, and OreSpawn only
 * spawned ghosts naturally when the game was started on October 31 (orespawn.content.Spooky does the same).
 */
public class Ghost extends AmbientCreature {
	private @Nullable BlockPos flightTarget;

	public Ghost(EntityType<? extends Ghost> type, Level level) {
		super(type, level);
		this.xpReward = 5;
		this.noPhysics = true;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return attributes(2.0);
	}

	static AttributeSupplier.Builder attributes(double health) {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, health).add(Attributes.MOVEMENT_SPEED, 0.1).add(Attributes.ATTACK_DAMAGE, 0.0);
	}

	/** The sound it makes now and then (half the time it's silent). */
	protected SoundEvent moan() {
		return ModSounds.get("ghost_sound");
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return random.nextInt(2) == 0 ? moan() : null;
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
		return 0.3F;
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
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypes.IN_WALL)) return false;
		return super.hurtServer(level, source, damage);
	}

	@Override
	public void tick() {
		// A ghost someone has kept (a name tag) stops going through walls.
		if (isPersistenceRequired()) noPhysics = false;
		super.tick();
		setDeltaMovement(getDeltaMovement().multiply(1.0, 0.65, 1.0));
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		BlockPos here = blockPosition();
		if (flightTarget == null) flightTarget = here;
		if (random.nextInt(40) == 1 || Flight.distSq(this, flightTarget) < 2.0) {
			Player player = level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(16.0), p -> !p.isSpectator()).stream()
				.min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
			if (player != null) {
				flightTarget = new BlockPos((int) player.getX() + random.nextInt(3) - random.nextInt(3), (int) (player.getY() + 1.0),
					(int) player.getZ() + random.nextInt(3) - random.nextInt(3));
			} else {
				// Up to the first air within 3 blocks, down to the ground within 3, then 1-4 blocks above that.
				int i;
				for (i = 0; i < 3; i++) {
					if (level.isEmptyBlock(here.above(i))) break;
				}
				int j;
				for (j = -1; j >= -3; j--) {
					if (!level.isEmptyBlock(here.above(j))) break;
				}
				flightTarget = new BlockPos(here.getX() + random.nextInt(10) - random.nextInt(10), here.getY() + i + j + random.nextInt(4) + 1,
					here.getZ() + random.nextInt(10) - random.nextInt(10));
			}
		}
		Flight.steer(this, flightTarget, 0.1, 0.05, 0.1, 0.5, 0.05F, 1.0F / 6.0F);
	}

	/** Ghost.getCanSpawnHere: at night (or from a spawner). */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return !level.getLevel().isBrightOutside();
	}
}
