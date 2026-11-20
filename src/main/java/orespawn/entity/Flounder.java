package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's flounder, a flatfish. Like OreSpawn's other water creatures it is a land animal that swims at the surface:
 * it floats (OreSpawn's EntityAISwimming, here FloatGoal) and wanders on ordinary paths that may cross water, which it
 * doesn't avoid, and it breathes underwater. Shy: it backs off from players within 8 blocks and panics when hurt.
 * In water it heals 1 now and then (1 in 50 a tick). Out of water it looks for water now and then (1 in 20 a tick, out to
 * 9 blocks) and heads for it; with none in reach it loses 1 health 1 time in 25 and vanishes, without drops, at 0.
 * OreSpawn never spawned it naturally. Silent but for its death sound: its ambient "splash" and hurt "little_splat"
 * named no sound in 1.7.10 (the second lacks OreSpawn's "orespawn:" prefix). Drops 1-2 raw cod (a loot table). Babies
 * (a spawn egg used on a flounder) never despawn.
 * Breeds with OreSpawn's Crystal Apple.
 */
public class Flounder extends Animal {
	public Flounder(EntityType<? extends Flounder> type, Level level) {
		super(type, level);
		this.xpReward = 5;
		setPathfindingMalus(PathType.WATER, 0.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 0.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new BreedGoal(this, 1.0));
		goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.0, 1.4));
		goalSelector.addGoal(4, new PanicGoal(this, 1.5));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
		goalSelector.addGoal(6, WanderGoal.wander(this, 1.0));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
		super.travelInWater(swimInput(input), baseGravity, isFalling, oldY);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		if (!isInWater() && random.nextInt(20) == 0 && !seekWater(this, level, 11, 4, 1.0, 1.0F)) return;
		// OreSpawn also played "splash" here, which named no sound.
		if (isInWater() && random.nextInt(50) == 0) heal(1.0F);
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
		return ModSounds.get("ratdead");
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
		return ModEntities.FLOUNDER.create(level, EntitySpawnReason.BREEDING);
	}

	/** OreSpawn's getCanSpawnHere replaced the animal light check; its rules are all in checkSpawn. */
	@Override
	public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
		return true;
	}

	/** It may appear in water (as 26.3's water animals may). */
	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
	}

	/** Flounder.getCanSpawnHere: at y 50 or higher, by day, 1 time in 20, and no more than 10 flounders within 16 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return pos.getY() >= 50 && level.getLevel().isBrightOutside() && random.nextInt(20) == 1
			&& level.getEntitiesOfClass(Flounder.class, new AABB(pos).inflate(16.0, 8.0, 16.0)).size() <= 10;
	}

	/**
	 * 1.7.10 pushed an AI-driven mob through water at 0.04 a tick per unit of speed, 26.3 at 0.02: doubling the input keeps
	 * OreSpawn's swimming pace (26.3 still caps the input at 1, as 1.7.10 did). Shared by the flounder, whale and skate.
	 */
	static Vec3 swimInput(Vec3 input) {
		return input.scale(2.0);
	}

	/**
	 * OreSpawn's out-of-water routine, which the flounder, whale and skate each had a copy of: find the nearest water and
	 * walk to just under it; with none in reach, lose {@code loss} health 1 time in 25 and vanish (no death, no drops)
	 * at 0. Returns false if the mob vanished.
	 */
	static boolean seekWater(PathfinderMob mob, ServerLevel level, int shells, int maxDy, double speed, float loss) {
		BlockPos water = nearestWater(mob, level, shells, maxDy);
		if (water != null) {
			mob.getNavigation().moveTo(water.getX(), water.getY() - 1, water.getZ(), speed);
			return true;
		}
		if (mob.getRandom().nextInt(25) == 1) mob.heal(-loss);
		if (mob.getHealth() <= 0.0F) {
			mob.discard();
			return false;
		}
		return true;
	}

	/**
	 * OreSpawn's scan_it loop: water (still or flowing) in growing box shells around the block under the mob, i blocks
	 * across and min(i, maxDy) up and down, for i = 1-5 and then every other i below {@code shells}; the nearest in the
	 * first shell that has any.
	 */
	private static @Nullable BlockPos nearestWater(Mob mob, ServerLevel level, int shells, int maxDy) {
		BlockPos c = mob.blockPosition().below();
		for (int i = 1; i < shells; i++) {
			int dy = Math.min(i, maxDy);
			BlockPos best = null;
			int bestD = Integer.MAX_VALUE;
			for (BlockPos p : BlockPos.betweenClosed(c.offset(-i, -dy, -i), c.offset(i, dy, i))) {
				int ax = Math.abs(p.getX() - c.getX()), ay = Math.abs(p.getY() - c.getY()), az = Math.abs(p.getZ() - c.getZ());
				if (ax != i && az != i && ay != dy) continue;
				if (!level.getBlockState(p).is(Blocks.WATER)) continue;
				int d = ax * ax + ay * ay + az * az;
				if (d < bestD) {
					bestD = d;
					best = p.immutable();
				}
			}
			if (best != null) return best;
			if (i >= 5) i++;
		}
		return null;
	}
}
