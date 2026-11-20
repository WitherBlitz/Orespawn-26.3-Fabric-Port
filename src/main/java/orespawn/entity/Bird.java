package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's bird (Cockateil): six kinds, flies about by day picking spots 5-12 blocks away that it can see, sings
 * in fair weather. Drops feathers; the sixth kind, killed by a player, has a 1 in 3 chance of dropping a ruby.
 */
public class Bird extends Animal {
	private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(Bird.class, EntityDataSerializers.INT);
	private BlockPos flightTarget;
	private int stuckCount;
	private int lastX;
	private int lastZ;

	public Bird(EntityType<? extends Bird> type, Level level) {
		super(type, level);
		this.moveControl = Flight.keepOwnSteering(this);
		this.xpReward = 2;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 2.0).add(Attributes.MOVEMENT_SPEED, 0.33)
			.add(Attributes.ATTACK_DAMAGE, 1.0);
	}

	public int variant() {
		return entityData.get(TYPE);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(TYPE, 0);
	}

	@Override
	public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
			@Nullable SpawnGroupData data) {
		entityData.set(TYPE, random.nextInt(6));
		return super.finalizeSpawn(level, difficulty, reason, data);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("BirdType", variant());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		entityData.set(TYPE, input.getIntOr("BirdType", 0));
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return level().isBrightOutside() && !level().isRaining() ? ModSounds.BIRDS : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.DUCK_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.DUCK_HURT;
	}

	@Override
	protected float getSoundVolume() {
		return 0.55F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
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
		if (flightTarget == null) flightTarget = blockPosition();
		else setDeltaMovement(getDeltaMovement().multiply(1.0, getY() < flightTarget.getY() ? 0.7 : 0.5, 1.0));
	}

	private boolean canSee(BlockPos p) {
		Vec3 from = new Vec3(getX(), getY() + 0.75, getZ());
		return level().clip(new ClipContext(from, Vec3.atLowerCornerOf(p), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType()
			== HitResult.Type.MISS;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (lastX == getBlockX() && lastZ == getBlockZ()) {
			stuckCount++;
		} else {
			stuckCount = 0;
			lastX = getBlockX();
			lastZ = getBlockZ();
		}
		if (flightTarget == null) flightTarget = blockPosition();
		if (stuckCount > 40 || random.nextInt(250) == 0 || Flight.distSq(this, flightTarget) < 4.1) {
			stuckCount = 0;
			for (int tries = 35; tries > 0; tries--) {
				int dx = random.nextInt(8) + 5, dz = random.nextInt(8) + 5;
				if (random.nextInt(2) == 0) dz = -dz;
				if (random.nextInt(2) == 0) dx = -dx;
				// Cockateil's "stayup": in the Islands dimension it picks spots a little higher.
				int stayUp = level.dimension() == orespawn.world.OreSpawnDimensions.ISLANDS ? 2 : 0;
				BlockPos p = new BlockPos(getBlockX() + dx, getBlockY() + random.nextInt(9 + stayUp) - 5, getBlockZ() + dz);
				flightTarget = p;
				if (level.isEmptyBlock(p) && canSee(p)) break;
			}
		}
		Flight.steer(this, flightTarget, 0.3, 0.25, 0.200000001, 0.3, 0.8F, 1.0F / 3.0F);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		if (variant() == 5 && killedByPlayer && random.nextInt(3) == 1) spawnAtLocation(level, ModItems.RUBY);
	}

	/** Cockateil.getCanSpawnHere: by day, at y 50 or higher (any height in the Islands dimension). */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return level.getLevel().isBrightOutside()
			&& (pos.getY() >= 50 || level.getLevel().dimension() == orespawn.world.OreSpawnDimensions.ISLANDS);
	}
}
