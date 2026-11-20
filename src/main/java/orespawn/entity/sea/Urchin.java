package orespawn.entity.sea;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Crystal Urchin: a fiery crystal urchin of the Crystal and Chaos dimensions that rolls about at night,
 * trailing flames. It goes for nearly anything alive within 16 blocks (not the small things OreSpawn's hunters ignore,
 * other urchins, Vortexes, Rotators, Peacocks, Crystal Apple Cows, Irukandji, Skates, Whales, Flounders, or creative
 * players), stinging within about 2.8 blocks for 10 and setting the target alight for 5 seconds. It's fire-proof and
 * cactus-proof, and avoids water, which hurts it (1 in 15 a tick, with a puff of smoke). Unless it came from a spawner
 * it vanishes now and then by day (1 in 400 a tick) and may despawn. 25 health, 4 armour, 20 xp. Drops 0-2 of either
 * OreSpawn's Pink Tourmaline Ingots or Crystal Apples, or nothing (those come with the groups that port them).
 * Natural spawns: only in OreSpawn's Crystal dimension (its monsters, weight 15, 2-4) and Chaos dimension (weight 2,
 * 1-5), which aren't ported here.
 */
public class Urchin extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Urchin.class, EntityDataSerializers.BOOLEAN);
	/** Came from a spawner (OreSpawn didn't save this either). */
	private boolean spawnered;

	public Urchin(EntityType<? extends Urchin> type, Level level) {
		super(type, level);
		this.xpReward = 20;
	}

	public static AttributeSupplier.Builder createAttributes() {
		// get_mobstats "Urchin": 25 health, 10 attack, 4 defense.
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 25.0).add(Attributes.MOVEMENT_SPEED, 0.3)
			.add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, 4.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, WanderGoal.wanderALot(this, 14, 1.0));
		goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(3, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
	}

	public boolean attacking() {
		return entityData.get(ATTACKING);
	}

	@Override
	public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
			@Nullable SpawnGroupData data) {
		if (EntitySpawnReason.isSpawner(reason)) spawnered = true;
		return super.finalizeSpawn(level, difficulty, reason, data);
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide() && !isPersistenceRequired() && !spawnered && level().getDefaultClockTime() % 24000L < 12000L
				&& random.nextInt(400) == 1) {
			discard();
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (random.nextInt(3) != 1) return;
		Level level = level();
		if (level.isClientSide()) {
			level.addParticle(ParticleTypes.FLAME, getX(), getY() + 0.75, getZ(), 0.0, random.nextFloat() / 10.0F, 0.0);
		}
		if (isInWater() && random.nextInt(5) == 1) {
			if (level instanceof ServerLevel server) {
				doHurtTarget(server, this);
			} else {
				level.addParticle(ParticleTypes.SMOKE, getX(), getY() + 1.75, getZ(), 0.0, random.nextFloat() / 10.0F, 0.0);
				level.addParticle(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.75, getZ(), 0.0, random.nextFloat() / 10.0F, 0.0);
				level.addParticle(ParticleTypes.SMOKE, getX(), getY() + 2.0, getZ(), 0.0, random.nextFloat() / 10.0F, 0.0);
				level.addParticle(ParticleTypes.LARGE_SMOKE, getX(), getY() + 2.0, getZ(), 0.0, random.nextFloat() / 10.0F, 0.0);
			}
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(8) != 0) return;
		LivingEntity e = SeaTargets.nearest(this, 16.0, 3.0, this::isSuitableTarget);
		if (e != null) {
			if (distanceToSqr(e) < 8.0) {
				entityData.set(ATTACKING, true);
				if (random.nextInt(7) == 0 || random.nextInt(8) == 1) doHurtTarget(level, e);
			} else {
				getNavigation().moveTo(e, 1.2);
			}
		} else {
			entityData.set(ATTACKING, false);
		}
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || SeaTargets.ignoreable(e) || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Urchin || e instanceof Irukandji) return false;
		if (SeaTargets.is(e, "vortex", "rotator", "peacock", "crystal_apple_cow", "skate", "whale", "flounder")) return false;
		return !(e instanceof Player p && (p.isCreative() || p.isSpectator()));
	}

	/** Urchin.attackEntityAsMob: sets the target alight for 5 seconds, then the usual bite. */
	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		target.igniteForSeconds(5.0F);
		return super.doHurtTarget(level, target);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		return !source.is(DamageTypes.CACTUS) && super.hurtServer(level, source, damage);
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !spawnered;
	}

	/** getDropItem: nothing, a Pink Tourmaline Ingot or a Crystal Apple, 1 in 3 each; then 0-2 of it (more with Looting). */
	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		int i = random.nextInt(3);
		Item item = i == 1 ? ModItems.lazy("pink_tourmaline_ingot") : i == 2 ? ModItems.lazy("crystal_apple") : Items.AIR;
		if (item == Items.AIR) return;
		int n = random.nextInt(3);
		if (source.getEntity() instanceof Player p) {
			int looting = EnchantmentHelper.getEnchantmentLevel(
				level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), p);
			if (looting > 0) n += random.nextInt(looting + 1);
		}
		for (int k = 0; k < n; k++) spawnAtLocation(level, new ItemStack(item));
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.get("kyuubi_living");
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("glasshit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("glassdead");
	}

	@Override
	protected float getSoundVolume() {
		return 1.1F;
	}

	@Override
	public float getVoicePitch() {
		return 1.25F;
	}

	/**
	 * Urchin.getCanSpawnHere: at least 6 of the 9 blocks around the one above it are air, it's dark enough for a monster,
	 * and it's night (time of day 13000 or later).
	 */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		int air = 0;
		for (int k = -1; k <= 1; k++) {
			for (int j = -1; j <= 1; j++) {
				if (level.getBlockState(pos.offset(j, 1, k)).isAir()) air++;
			}
		}
		return air >= 6 && Monster.isDarkEnoughToSpawn(level, pos, random) && level.getLevel().getDefaultClockTime() % 24000L >= 13000L;
	}
}
