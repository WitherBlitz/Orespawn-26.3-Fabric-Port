package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * OreSpawn's crab: a big armored crab that comes in three sizes, chosen when it's made: a quarter size (most), half
 * size (1 in 4) or full size (1 in 8); crabs from a spawner are 0.35. Its size (saved as "Fscale") scales its body
 * (2.5 x 3.5 blocks at full size), health (250, OreSpawn reads the Nightmare's health stat for it), attack (24, from
 * Crab_stats, with a hard knock back), speed (0.55, 0.95 in water), armor (16 plus 2 per size), XP (400) and the
 * pitch of its voice. It hunts players, monsters and villagers within 16 blocks, clacking as it bites. On land it
 * heads for the nearest water within about 12 blocks; with none in reach it slowly loses health and vanishes at
 * none. In water it slowly heals with a splash. Cactus doesn't hurt it, it breathes under water, and a hit leaves it
 * unhurtable for 8 AI ticks (except /kill and the void). Drops 4 to 11 raw crab meat times its size (at least 1).
 * Spawns in water by day at y 50 or above.
 * <p>
 * Not ported yet: targeting OreSpawn's Lizard, Rubber Ducky, Girlfriend/Boyfriend and its other "attackable" mobs
 * (Mothra, Leon, the dragons, royalty, Gamma Metroid, Cephadrome, Water Dragon, Stinky), its Crystal-dimension spawn
 * limit, and the PlayNicely config (treated as off, OreSpawn's default). OreSpawn's heal splash used the sound name
 * "splash", which 1.7.10 doesn't have; the vanilla splash is used.
 */
public class Crab extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Crab.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(Crab.class, EntityDataSerializers.FLOAT);
	/** OreSpawnMain.PitchBlack_stats.health: OreSpawn's crab takes its health from the Nightmare's stats (250). */
	private static final int HEALTH = 250;
	/** OreSpawnMain.Crab_stats: attack 24, defense 16 (its own health stat, 180, is never used). */
	private static final float ATTACK = 24.0F;
	private static final int DEFENSE = 16;
	private int hurtTimer;
	private int closest = 99999;
	private int tx;
	private int ty;
	private int tz;

	public Crab(EntityType<? extends Crab> type, Level level) {
		super(type, level);
		setPathfindingMalus(PathType.WATER, 0.0F);
		if (!level.isClientSide()) {
			float t = 0.25F;
			if (random.nextInt(4) == 1) t = 0.5F;
			if (random.nextInt(8) == 2) t = 1.0F;
			setCrabScale(t);
			setHealth(getMaxHealth());
		}
		refreshDimensions();
	}

	/** Full-size values; each crab scales them by its size. */
	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, HEALTH).add(Attributes.MOVEMENT_SPEED, 0.55)
			.add(Attributes.ATTACK_DAMAGE, ATTACK).add(Attributes.ARMOR, DEFENSE + 2);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, WanderGoal.wanderALot(this, 16, 1.0));
		goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 10.0F));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Mob.class, 8.0F));
		goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
		builder.define(SIZE, 0.25F);
	}

	public boolean attacking() {
		return entityData.get(ATTACKING);
	}

	public float getCrabScale() {
		return entityData.get(SIZE);
	}

	/** Sets the crab's size, and on the server everything that follows from it. */
	public void setCrabScale(float scale) {
		entityData.set(SIZE, scale);
		if (level().isClientSide()) return;
		getAttribute(Attributes.MAX_HEALTH).setBaseValue((int) (HEALTH * scale));
		getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((isInWater() ? 0.95 : 0.55) * scale);
		getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(ATTACK * scale);
		getAttribute(Attributes.ARMOR).setBaseValue(DEFENSE + (int) (2.0F * scale));
		xpReward = (int) (400.0F * scale);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
		if (SIZE.equals(accessor)) refreshDimensions();
		super.onSyncedDataUpdated(accessor);
	}

	@Override
	protected EntityDimensions getDefaultDimensions(Pose pose) {
		return getType().getDimensions().scale(getCrabScale());
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putFloat("Fscale", getCrabScale());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		setCrabScale(input.getFloatOr("Fscale", getCrabScale()));
	}

	@Override
	public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
			@Nullable SpawnGroupData data) {
		// OreSpawn: crabs from a crab spawner are always 0.35 size.
		if (EntitySpawnReason.isSpawner(reason)) {
			setCrabScale(0.35F);
			setHealth(getMaxHealth());
		}
		return super.finalizeSpawn(level, difficulty, reason, data);
	}

	@Override
	public void tick() {
		if (!level().isClientSide()) {
			getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((isInWater() ? 0.95 : 0.55) * getCrabScale());
		}
		super.tick();
	}

	@Override
	protected int getFireImmuneTicks() {
		return (int) (10.0F * getCrabScale());
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	/** OreSpawn's getCanSpawnHere replaced the monster light check (crabs spawn by day); checkSpawn has the whole rule. */
	@Override
	public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
		return true;
	}

	/** Crabs spawn in water, so (like vanilla's water mobs) water in the way doesn't stop them. */
	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (hurtTimer > 0) hurtTimer--;
		if (!isInWater() && random.nextInt(25) == 0) {
			closest = 99999;
			tx = ty = tz = 0;
			int x = Mth.floor(getX()), y = Mth.floor(getY()) - 1, z = Mth.floor(getZ());
			for (int i = 1; i < 12; i++) {
				int j = Math.min(i, 10);
				if (scan(level, x, y, z, i, j, i)) break;
				if (i >= 5) i++;
			}
			if (closest < 99999) {
				getNavigation().moveTo(tx, ty - 1, tz, 1.33);
			} else {
				if (random.nextInt(100) == 1) heal(-1.0F * getCrabScale());
				if (getHealth() <= 0.0F) {
					discard();
					return;
				}
			}
		}
		if (random.nextInt(5) == 1) {
			if (random.nextInt(100) == 1) setTarget(null);
			LivingEntity e = getTarget();
			if (e != null && !e.isAlive()) {
				setTarget(null);
				e = null;
			}
			if (e == null) e = findSomethingToAttack(level);
			if (e != null) {
				lookAt(e, 10.0F, 10.0F);
				float reach = 6.0F + e.getBbWidth() / 2.0F;
				if (distanceToSqr(e) < reach * reach * getCrabScale()) {
					entityData.set(ATTACKING, true);
					if (random.nextInt(4) == 0 || random.nextInt(5) == 1) {
						doHurtTarget(level, e);
						String sound = random.nextInt(3) == 1 ? "scorpion_attack" : "scorpion_living";
						level.playSound(null, e.getX(), e.getY(), e.getZ(), ModSounds.get(sound), getSoundSource(), 0.75F, 1.5F);
					}
				} else {
					getNavigation().moveTo(e, 1.0);
				}
			} else {
				entityData.set(ATTACKING, false);
			}
		}
		if (random.nextInt(120) == 1 && isInWater() && getHealth() < getMaxHealth()) {
			playSound(SoundEvents.GENERIC_SPLASH, 1.5F, random.nextFloat() * 0.2F + 0.9F);
			heal(4.0F * getCrabScale());
		}
	}

	/** Crab.scan_it: looks for water on the faces of a box around (x, y, z), keeping the closest found so far. */
	private boolean scan(ServerLevel level, int x, int y, int z, int dx, int dy, int dz) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		int found = 0;
		for (int i = -dy; i <= dy; i++) {
			for (int j = -dz; j <= dz; j++) {
				found += water(level, p, x + dx, y + i, z + j, dx * dx + j * j + i * i);
				found += water(level, p, x - dx, y + i, z + j, dx * dx + j * j + i * i);
			}
		}
		for (int i = -dx; i <= dx; i++) {
			for (int j = -dz; j <= dz; j++) {
				found += water(level, p, x + i, y + dy, z + j, dy * dy + j * j + i * i);
				found += water(level, p, x + i, y - dy, z + j, dy * dy + j * j + i * i);
			}
		}
		for (int i = -dx; i <= dx; i++) {
			for (int j = -dy; j <= dy; j++) {
				found += water(level, p, x + i, y + j, z + dz, dz * dz + j * j + i * i);
				found += water(level, p, x + i, y + j, z - dz, dz * dz + j * j + i * i);
			}
		}
		return found != 0;
	}

	private int water(ServerLevel level, BlockPos.MutableBlockPos p, int x, int y, int z, int d) {
		if (d >= closest || !level.getBlockState(p.set(x, y, z)).is(Blocks.WATER)) return 0;
		closest = d;
		tx = x;
		ty = y;
		tz = z;
		return 1;
	}

	private @Nullable LivingEntity findSomethingToAttack(ServerLevel level) {
		LivingEntity current = getTarget();
		if (current != null && current.isAlive()) return current;
		setTarget(null);
		return level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(16.0, 6.0, 16.0), this::isSuitableTarget).stream()
			.min(Comparator.comparingDouble(this::targetWeight)).orElse(null);
	}

	/** OreSpawn's GenericTargetSorter: nearest first, creepers and big mobs counted as nearer. */
	private double targetWeight(Entity e) {
		double d = distanceToSqr(e);
		if (e instanceof Creeper) d /= 2.0;
		double size = e.getBbHeight() * e.getBbWidth();
		return size > 1.0 ? d / size : d;
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive()) return false;
		if (!getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Player p) return !p.isCreative() && !p.isSpectator();
		if (e instanceof Crab) return false;
		return e instanceof Monster || e instanceof Villager;
	}

	/** Crab.attackEntityAsMob: its own damage, and a hard knock back (twice as high for players). */
	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean hit = target.hurtServer(level, damageSources().mobAttack(this), ATTACK * getCrabScale());
		if (hit && target instanceof LivingEntity) {
			double ks = 1.15 * getCrabScale();
			double inAir = 0.48 * getCrabScale();
			float angle = (float) Math.atan2(target.getZ() - getZ(), target.getX() - getX());
			if (target.isRemoved() || target instanceof Player) inAir *= 2.0;
			target.push(Math.cos(angle) * ks, inAir, Math.sin(angle) * ks);
		}
		return hit;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypes.CACTUS)) return false;
		boolean hurt = false;
		if (hurtTimer <= 0 || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			hurt = super.hurtServer(level, source, damage);
			hurtTimer = 8;
		}
		if (source.getEntity() instanceof Mob attacker) {
			if (attacker instanceof Crab) return false;
			setTarget(attacker);
			getNavigation().moveTo(attacker, 1.2);
		}
		return hurt;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		int n = Math.max(1, (int) ((4 + random.nextInt(8)) * getCrabScale()));
		for (int i = 0; i < n; i++) {
			level.addFreshEntity(new ItemEntity(level, getX() + random.nextInt(2) - random.nextInt(2), getY() + 1.0,
				getZ() + random.nextInt(2) - random.nextInt(2), new ItemStack(ModItems.RAW_CRAB_MEAT)));
		}
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("leaves_hit");
	}

	@Override
	protected @Nullable SoundEvent getDeathSound() {
		return null;
	}

	@Override
	protected float getSoundVolume() {
		return 0.75F;
	}

	@Override
	public float getVoicePitch() {
		return 2.0F - 0.3F * (1.0F / getCrabScale());
	}

	/** Crab.getCanSpawnHere: by day, at y 50 or above (any crab from a spawner). */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return pos.getY() >= 50 && level.getLevel().isBrightOutside();
	}
}
