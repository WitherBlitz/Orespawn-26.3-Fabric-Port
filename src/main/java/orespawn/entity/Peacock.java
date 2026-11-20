package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's peacock: keeps away from monsters and players, and now and then fans out its crest and tail for a while
 * (a cycle each side runs on its own, as OreSpawn's did, so it isn't synced). Off peaceful, it has a 1 in 5000 chance
 * each tick of laying 1-3 peacock spawn eggs. Drops 1-2 raw peacock and, half the time, a peacock feather (none for
 * babies). Breeds with OreSpawn's Crystal Apple. Not ported yet: it hunted OreSpawn's termites (6 damage a
 * peck).
 */
public class Peacock extends Animal {
	private int showOffTime;
	private int showOffCount;
	private boolean showingOff;

	public Peacock(EntityType<? extends Peacock> type, Level level) {
		super(type, level);
		this.xpReward = 8;
		this.showOffTime = 20 + random.nextInt(50);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 15.0).add(Attributes.MOVEMENT_SPEED, 0.38)
			.add(Attributes.ATTACK_DAMAGE, 4.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new BreedGoal(this, 1.0));
		goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 8.0F, 1.0, 1.4F));
		goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 12.0F, 1.2F, 1.6F));
		goalSelector.addGoal(4, new PanicGoal(this, 1.5));
		goalSelector.addGoal(5, WanderGoal.wander(this, 1.0));
		goalSelector.addGoal(6, new RandomLookAroundGoal(this));
	}

	/** Peacock.getBlink: the crest and tail fan are open. */
	public boolean showingOff() {
		return showingOff;
	}

	@Override
	public void tick() {
		super.tick();
		if (++showOffCount > showOffTime) {
			showOffCount = 0;
			showingOff = !showingOff;
			showOffTime = showingOff ? 25 + random.nextInt(100) : 50 + random.nextInt(300);
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		super.customServerAiStep(level);
		if (level.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(5000) == 1) {
			ItemStack eggs = new ItemStack(ModEntities.SPAWN_EGGS.get("peacock"), 1 + random.nextInt(3));
			level.addFreshEntity(new ItemEntity(level, getX() + random.nextInt(2) - random.nextInt(2), getY() + 1.0,
				getZ() + random.nextInt(2) - random.nextInt(2), eggs));
		}
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return random.nextInt(8) != 1 ? null : ModSounds.get("peacocklive");
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("peacockhit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("peacockdead");
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		// Peacock.dropFewItems: one raw peacock, a 1 in 3 chance of a second, a 1 in 2 chance of a feather; Looting doesn't add.
		spawnAtLocation(level, ModItems.RAW_PEACOCK);
		if (random.nextInt(3) == 1) spawnAtLocation(level, ModItems.RAW_PEACOCK);
		if (random.nextInt(2) == 1) spawnAtLocation(level, ModItems.PEACOCK_FEATHER);
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
		return ModEntities.PEACOCK.create(level, EntitySpawnReason.BREEDING);
	}

	/**
	 * Peacock.getCanSpawnHere: a 2x2, two-high space starting one block above its feet is open air, it's daytime (clock
	 * time 0-12000), y is 50-100, and no more than 2 peacocks are within 16 blocks.
	 */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		for (int k = -1; k < 1; k++) {
			for (int j = -1; j < 1; j++) {
				for (int i = 1; i < 3; i++) {
					if (!level.getBlockState(pos.offset(j, i, k)).isAir()) return false;
				}
			}
		}
		if (level.getLevel().getOverworldClockTime() % 24000L > 12000L) return false;
		if (pos.getY() < 50 || pos.getY() > 100) return false;
		return level.getEntitiesOfClass(Peacock.class, new AABB(pos).inflate(16.0, 10.0, 16.0)).size() <= 2;
	}
}
