package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's goldfish ("Gold Fish", shown as Goldfish). It swims through the air, not water: it keeps a target air block
 * it can see, 5-9 blocks away on each axis and up to 5 up or down (nudged 2 up below y 120 and 2 down above y 140), picks
 * a new one when it gets there or 1 time in 300 a tick, and steers to it the way OreSpawn's fliers do. In water it
 * breathes fine but still only aims for air, so it swims back out; it never lands and takes no fall damage. OreSpawn only
 * spawned it from mob spawners in its goldfish bowls and on Girlfriend Island. Silent apart from its death splat (its
 * "splash" sounds named no sound that existed). Despawns at night when far from players.
 * Its drops (0-2 of one of: gold block, uranium nugget, titanium nugget) are a loot table.
 * Feeding it wheat did nothing in OreSpawn but show hearts (it had no mating AI and no babies), so it isn't food here.
 */
public class GoldFish extends Animal {
	private @Nullable BlockPos flightTarget;

	public GoldFish(EntityType<? extends GoldFish> type, Level level) {
		super(type, level);
		this.moveControl = Flight.keepOwnSteering(this);
		this.xpReward = 5;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 6.0).add(Attributes.MOVEMENT_SPEED, 0.22)
			.add(Attributes.ATTACK_DAMAGE, 1.0);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("little_splat");
	}

	@Override
	protected float getSoundVolume() {
		return 0.45F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/** OreSpawn emptied collideWithEntity: it doesn't shove what it bumps into. */
	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isPersistenceRequired() && !level().isBrightOutside();
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return false;
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}

	@Override
	public void tick() {
		super.tick();
		setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6, 1.0));
	}

	private boolean canSee(BlockPos p) {
		Vec3 from = new Vec3(getX(), getY() + 0.75, getZ());
		return level().clip(new ClipContext(from, Vec3.atLowerCornerOf(p), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType()
			== HitResult.Type.MISS;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (flightTarget == null) flightTarget = blockPosition();
		int updown = getBlockY() < 120 ? 2 : getBlockY() > 140 ? -2 : 0;
		if (random.nextInt(300) == 0 || Flight.distSq(this, flightTarget) < 2.1) {
			for (int tries = 50; tries > 0; tries--) {
				int dz = random.nextInt(5) + 5, dx = random.nextInt(5) + 5;
				if (random.nextInt(2) == 0) dz = -dz;
				if (random.nextInt(2) == 0) dx = -dx;
				flightTarget = new BlockPos(getBlockX() + dx, getBlockY() + random.nextInt(11) - 5 + updown, getBlockZ() + dz);
				if (level.isEmptyBlock(flightTarget) && canSee(flightTarget)) break;
			}
		}
		Flight.steer(this, flightTarget, 0.4, 0.3, 0.2, 0.4, 0.75F, 1.0F / 6.0F);
	}

	/** OreSpawn's getCanSpawnHere let a goldfish spawn anywhere, without the animal light check. */
	@Override
	public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
		return true;
	}

	/** GoldFish.getCanSpawnHere: always true. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return true;
	}
}
