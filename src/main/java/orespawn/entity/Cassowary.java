package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's cassowary: a big flightless bird that keeps away from players and monsters, bobbing its neck as it runs.
 * Drops 2-4 raw chicken. It breeds with OreSpawn's Crystal Apple.
 */
public class Cassowary extends Animal {
	public Cassowary(EntityType<? extends Cassowary> type, Level level) {
		super(type, level);
		this.xpReward = 5;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 8.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new BreedGoal(this, 1.0));
		goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 8.0F, 1.0, 1.4F));
		goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.0, 1.4F));
		goalSelector.addGoal(4, new PanicGoal(this, 1.5));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, LivingEntity.class, 12.0F));
		goalSelector.addGoal(6, WanderGoal.wander(this, 1.0));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		super.customServerAiStep(level);
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
		return 0.4F;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		if (isBaby()) {
			setPersistenceRequired();
			return false;
		}
		return !isPersistenceRequired();
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return orespawn.entity.Breeding.crystalApple(stack);
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return ModEntities.CASSOWARY.create(level, EntitySpawnReason.BREEDING);
	}

	/** Cassowary.getCanSpawnHere: by day. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return EntitySpawnReason.isSpawner(reason) || level.getLevel().isBrightOutside();
	}
}
