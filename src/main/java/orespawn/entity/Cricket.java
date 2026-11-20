package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's cricket: tiny, hops about at random (a 1 in 50 chance a tick, at most every 50 ticks), and chirps half
 * the times it would make a sound, rubbing its back legs while it does.
 */
public class Cricket extends Animal {
	private static final EntityDataAccessor<Boolean> SINGING = SynchedEntityData.defineId(Cricket.class, EntityDataSerializers.BOOLEAN);
	private int singing;
	private int jumpCount;

	public Cricket(EntityType<? extends Cricket> type, Level level) {
		super(type, level);
		this.xpReward = 1;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 3.0).add(Attributes.MOVEMENT_SPEED, 0.15)
			.add(Attributes.ATTACK_DAMAGE, 0.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new PanicGoal(this, 1.4));
		goalSelector.addGoal(1, WanderGoal.wanderALot(this, 8, 1.0));
	}

	public boolean singing() {
		return entityData.get(SINGING);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(SINGING, false);
	}

	private void jumpAround() {
		float f = 0.3F + Math.abs(random.nextFloat() * 0.25F);
		float d = (float) (random.nextFloat() * Math.PI * 2.0);
		setDeltaMovement(getDeltaMovement().add(f * Math.sin(d), 0.55F + Math.abs(random.nextFloat() * 0.35F), f * Math.cos(d)));
		setPos(getX(), getY() + 0.25, getZ());
		needsSync = true;
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) return;
		if (singing != 0 && --singing <= 0) entityData.set(SINGING, false);
		if (jumpCount > 0) jumpCount--;
		if (jumpCount == 0 && random.nextInt(50) == 1) {
			jumpAround();
			jumpCount = 50;
		}
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		if (!level().isClientSide()) {
			if (random.nextInt(2) == 0) return null;
			singing = 40;
			entityData.set(SINGING, true);
		}
		return ModSounds.CRICKET;
	}

	@Override
	protected float getSoundVolume() {
		return 0.7F;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
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
	public boolean isFood(ItemStack stack) {
		return false;
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}

	/** Cricket.getCanSpawnHere: at y 30 or higher, no more than 5 crickets within 20 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return pos.getY() >= 30 && level.getEntitiesOfClass(Cricket.class, new AABB(pos).inflate(20.0, 10.0, 20.0)).size() <= 5;
	}
}
