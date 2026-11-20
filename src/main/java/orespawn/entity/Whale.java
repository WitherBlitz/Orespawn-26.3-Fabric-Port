package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's whale: a big, peaceful surface swimmer of deep oceans. Like OreSpawn's other water creatures it is a land
 * animal that floats (OreSpawn's EntityAISwimming, here FloatGoal) and wanders on ordinary paths over the water, which it
 * doesn't avoid; it breathes underwater. It follows players holding raw fish (cod, salmon, tropical fish or pufferfish:
 * 1.7.10's raw fish item covered all four), panics when hurt, and every 12-25 seconds spouts for a second or two: a
 * plume of splashes (and bubbles, which only last in water) from a block above its feet. It heals 1 now and then
 * (1 in 200 a tick, and 1 in 50 while in water). Out of water it looks for water now and then (1 in 20 a tick, out to
 * 9 blocks) and heads for it; with none in reach it loses 4 health 1 time in 25 and vanishes, without drops, at 0.
 * Its ambient and in-water "splash" sounds named no sound in OreSpawn, so it only makes its hurt and death splats.
 * On death it scatters 20-44 raw cod around it. Babies (a spawn egg used on a whale) never despawn.
 * Breeds with OreSpawn's Crystal Apple.
 */
public class Whale extends Animal {
	private int spray;
	private int sprayTimer;

	public Whale(EntityType<? extends Whale> type, Level level) {
		super(type, level);
		this.xpReward = 40;
		setPathfindingMalus(PathType.WATER, 0.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 100.0).add(Attributes.MOVEMENT_SPEED, 0.35)
			.add(Attributes.ATTACK_DAMAGE, 0.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new BreedGoal(this, 1.0));
		goalSelector.addGoal(2, new TemptGoal(this, 1.2, Whale::isRawFish, false));
		goalSelector.addGoal(4, new PanicGoal(this, 1.5));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
		goalSelector.addGoal(6, WanderGoal.wander(this, 1.0));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	private static boolean isRawFish(ItemStack stack) {
		return stack.is(Items.COD) || stack.is(Items.SALMON) || stack.is(Items.TROPICAL_FISH) || stack.is(Items.PUFFERFISH);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
		super.travelInWater(Flounder.swimInput(input), baseGravity, isFalling, oldY);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			spout();
		} else if (random.nextInt(200) == 1) {
			heal(1.0F);
		}
	}

	/** Whale.onUpdate's spray: OreSpawn ran its timer on each side on its own, and only the client drew it. */
	private void spout() {
		if (spray == 0) {
			if (sprayTimer > 0) sprayTimer--;
			if (sprayTimer == 0) {
				sprayTimer = 250 + random.nextInt(250);
				spray = 25 + random.nextInt(25);
			}
		}
		if (spray <= 0) return;
		for (int i = 0; i < 20; i++) {
			double d = random.nextDouble() * 0.75;
			d *= d;
			double dir = random.nextDouble() * 2.0 * Math.PI - Math.PI;
			double dx = Math.cos(dir) * d / 2.0;
			double dz = Math.sin(dir) * d / 2.0;
			dir += Math.PI / 2;
			level().addParticle(i < 10 ? ParticleTypes.BUBBLE : ParticleTypes.SPLASH, getX() + dx, getY() + 1.0 + d, getZ() + dz,
				Math.cos(dir) * random.nextFloat() / 4.0, random.nextFloat() * 2.0F, Math.sin(dir) * random.nextFloat() / 4.0);
		}
		spray--;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		if (!isInWater() && random.nextInt(20) == 0 && !Flounder.seekWater(this, level, 11, 4, 1.0, 4.0F)) return;
		// OreSpawn also played "splash" here, which named no sound.
		if (isInWater() && random.nextInt(50) == 0) heal(1.0F);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("little_splat");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("big_splat");
	}

	@Override
	protected float getSoundVolume() {
		return 0.9F;
	}

	@Override
	public float getVoicePitch() {
		return 0.5F;
	}

	/** Whale.dropFewItems: 20-44 raw cod, each dropped on its own up to 3 blocks away and a block up. */
	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		int count = 20 + random.nextInt(25);
		for (int i = 0; i < count; i++) {
			level.addFreshEntity(new ItemEntity(level, getX() + random.nextInt(4) - random.nextInt(4), getY() + 1.0,
				getZ() + random.nextInt(4) - random.nextInt(4), new ItemStack(Items.COD)));
		}
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
		return ModEntities.WHALE.create(level, EntitySpawnReason.BREEDING);
	}

	/** OreSpawn's getCanSpawnHere replaced the animal light check; its rules are all in checkSpawn. */
	@Override
	public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
		return true;
	}

	/** It spawns in water (as 26.3's water animals do). */
	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
	}

	/**
	 * Whale.getCanSpawnHere: at y 50 or higher, by day, 1 time in 50, and no other whale within 32 blocks. Water below
	 * too, as 1.7.10's water-creature spawning asked (the placement already needs water at the spot).
	 */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return pos.getY() >= 50 && level.getFluidState(pos.below()).is(FluidTags.WATER) && level.getLevel().isBrightOutside()
			&& random.nextInt(50) == 1 && level.getEntitiesOfClass(Whale.class, new AABB(pos).inflate(32.0, 8.0, 32.0)).isEmpty();
	}
}
