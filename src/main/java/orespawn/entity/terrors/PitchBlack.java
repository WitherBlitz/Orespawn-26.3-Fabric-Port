package orespawn.entity.terrors;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import orespawn.content.Terrors;
import orespawn.entity.Flight;
import orespawn.entity.Ignored;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * OreSpawn's Nightmare (PitchBlack): a black winged dragon of the night that comes in five sizes. Each one is half size
 * most of the time, sometimes full size, rarely twice, three or four times as big; health (250 a size), bite (30 a size),
 * armor, speed, experience and its body all grow with it. It walks about like a monster until something catches its eye
 * (anything alive within 16 + 6 a size blocks, except the small things hunters ignore, the other terrors and creative
 * players), or now and then on its own, and then takes off, hunting from the air, flinging what it bites. It lands
 * again when there's ground below and nothing about. After each hit it shrugs off further damage for a second. Falls
 * don't hurt it. Its drops grow with it: rotten flesh with some feathers, string, flint and beef, a Nightmare Scale, an
 * item frame and ZooKeeper Shards.
 * <p>
 * Not ported: OreSpawn's "NightmareSize" config option (sizes are always random), its fire resistance setting (no 26.3
 * equivalent) and its Chaos dimension check (that dimension isn't ported); the Mobzilla range bonus applies once that
 * creature exists. OreSpawn spawned it only in its Islands and Chaos dimensions.
 */
public class PitchBlack extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(PitchBlack.class, EntityDataSerializers.BOOLEAN);
	/** OreSpawn's "activity": false walking, true flying. */
	private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(PitchBlack.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(PitchBlack.class, EntityDataSerializers.FLOAT);
	/** Creatures from other groups OreSpawn's Nightmare leaves alone: EnderReaper, LeafMonster, Island, IslandToo, Triffid. */
	private static final Set<String> FRIENDS = Set.of("ender_reaper", "leaf_monster", "island", "island_too", "islandtoo", "triffid");
	/** Things it may bite from further off (10 blocks): Mobzilla and its head. */
	private static final Set<String> HUGE = Set.of("mobzilla", "mobzilla_head", "mobzillahead");
	private BlockPos flightTarget;
	private int damageTicker;
	private int wingSound;
	// OreSpawn's RenderInfo (client side): whether the idle jaw snaps this cycle.
	private float jawPhase;
	private int jawBit;

	public PitchBlack(EntityType<? extends PitchBlack> type, Level level) {
		super(type, level);
		this.moveControl = new MoveControl<PitchBlack>(this) {
			@Override
			public void tick() {
				// In the air it steers itself (OreSpawn set moveForward after the move helper had run).
				if (flying() && this.operation == MoveControl.Operation.WAIT) return;
				super.tick();
			}
		};
		if (!level.isClientSide()) {
			// OreSpawn's entityInit: half size, 1 in 4 full size, 1 in 8 double, 1 in 32 triple, 1 in 64 four times.
			float t = 0.5F;
			if (random.nextInt(4) == 1) t = 1.0F;
			if (random.nextInt(8) == 2) t = 2.0F;
			if (random.nextInt(32) == 3) t = 3.0F;
			if (random.nextInt(64) == 4) t = 4.0F;
			applyScale(t);
			setHealth(getMaxHealth());
		}
	}

	/** get_mobstats("Nightmare", 250, 30, 10), for a full-size one; each Nightmare scales these by its size. */
	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 250.0).add(Attributes.MOVEMENT_SPEED, 0.3)
			.add(Attributes.ATTACK_DAMAGE, 30.0).add(Attributes.ARMOR, 12.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
		goalSelector.addGoal(2, WanderGoal.wanderALot(this, 16, 1.0));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));
		goalSelector.addGoal(4, new RandomLookAroundGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
		builder.define(FLYING, false);
		builder.define(SCALE, 1.0F);
	}

	public boolean attacking() {
		return entityData.get(ATTACKING);
	}

	public boolean flying() {
		return entityData.get(FLYING);
	}

	public float getPitchBlackScale() {
		return entityData.get(SCALE);
	}

	/** Sets the size and everything that follows from it (OreSpawn's applyEntityAttributes and entityInit). */
	private void applyScale(float t) {
		entityData.set(SCALE, t);
		getAttribute(Attributes.MAX_HEALTH).setBaseValue((int) (250.0F * t));
		getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(30.0F * t);
		getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.2F + 0.1F * t);
		getAttribute(Attributes.ARMOR).setBaseValue(10 + (int) (2.0F * t));
		this.xpReward = (int) (100.0F * t);
		refreshDimensions();
	}

	private void setFlying(boolean flying) {
		entityData.set(FLYING, flying);
		updateControlFlags();
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
		super.onSyncedDataUpdated(accessor);
		if (SCALE.equals(accessor)) refreshDimensions();
	}

	@Override
	protected EntityDimensions getDefaultDimensions(Pose pose) {
		return super.getDefaultDimensions(pose).scale(getPitchBlackScale());
	}

	/** While it flies its walking goals stand still (OreSpawn skipped its AI tasks in the air). */
	@Override
	protected void updateControlFlags() {
		super.updateControlFlags();
		if (flying()) {
			goalSelector.setControlFlag(Goal.Flag.MOVE, false);
			goalSelector.setControlFlag(Goal.Flag.JUMP, false);
			goalSelector.setControlFlag(Goal.Flag.LOOK, false);
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putFloat("Fscale", getPitchBlackScale());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		applyScale(input.getFloatOr("Fscale", getPitchBlackScale()));
	}

	/** From a spawner it is never bigger than full size. */
	@Override
	public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
			@Nullable SpawnGroupData data) {
		if (EntitySpawnReason.isSpawner(reason) && getPitchBlackScale() > 1.0F) {
			applyScale(1.0F);
			setHealth(getMaxHealth());
		}
		return super.finalizeSpawn(level, difficulty, reason, data);
	}

	/**
	 * The model's idle jaw (OreSpawn's ModelPitchBlack kept it per creature): re-rolled, 1 in 20 to snap, each time its
	 * cycle (0.7 times {@code speed} a tick) starts over. Returns 1 while it snaps.
	 */
	public int jawBits(float ageInTicks, float speed) {
		if (attacking()) return jawBit;
		float a = Math.abs(ageInTicks * 0.7F * speed % (float) (Math.PI * 2));
		if (a < jawPhase) jawBit = random.nextInt(20) == 1 ? 1 : 0;
		jawPhase = a;
		return jawBit;
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide() && ++wingSound > 20) {
			TerrorAi.play(this, TerrorAi.optionalSound("mothrawings"), 1.0F, 1.0F);
			wingSound = 0;
		}
		setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6, 1.0));
		if (level() instanceof ServerLevel) {
			if (random.nextInt(250) == 1) {
				heal(1.0F + getPitchBlackScale());
				if (random.nextInt(5) == 0) {
					// Lands when there's ground within 10 blocks below (or it's that low anyway) and nothing to hunt.
					boolean ground = getY() <= 10.0;
					for (int i = 0; i < 10 && !ground; i++) {
						ground = !level().isEmptyBlock(BlockPos.containing(getX(), getY() - i, getZ()));
					}
					if (ground && findSomethingToAttack() == null) setFlying(false);
				} else {
					setFlying(true);
					getNavigation().stop();
				}
			}
			if (!flying() && random.nextInt(10) == 1 && findSomethingToAttack() != null) {
				setFlying(true);
				getNavigation().stop();
			}
		}
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		float damage = 30.0F * getPitchBlackScale();
		if (target instanceof EnderDragon dragon) {
			TerrorAi.hitDragon(level, dragon, 8, damage);
			return true;
		}
		boolean hit = target.hurtServer(level, damageSources().mobAttack(this), damage);
		if (hit && target instanceof LivingEntity) TerrorAi.fling(this, target, 1.15 * getPitchBlackScale(), 0.08 * getPitchBlackScale());
		return hit;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (damageTicker > 0) damageTicker--;
		if (!flying()) return;
		float scale = getPitchBlackScale();
		if (flightTarget == null) flightTarget = blockPosition();
		if (random.nextInt(150) != 0 && Flight.distSq(this, flightTarget) >= 2.1F) {
			if (random.nextInt(8) == 0) {
				LivingEntity e = findSomethingToAttack();
				if (e != null) {
					double reach = 5.0 + e.getBbWidth() / 2.0F + scale;
					reach *= reach;
					entityData.set(ATTACKING, true);
					if ((e instanceof EnderDragon || TerrorAi.isOreSpawn(e, HUGE)) && reach < 100.0) reach = 100.0;
					flightTarget = BlockPos.containing(e.getX(), e.getY() + 2.0, e.getZ());
					if (distanceToSqr(e) < reach) doHurtTarget(level, e);
				} else {
					entityData.set(ATTACKING, false);
				}
			}
		} else {
			flightTarget = TerrorAi.newFlightTarget(this, 5 * (int) scale, 20, 11, 5);
		}
		double speed = 0.5F + scale / 10.0F;
		Flight.steer(this, flightTarget, speed, 0.33, 0.20000000149011612, 0.4, 0.1F + (float) speed, 0.2F);
	}

	private @Nullable LivingEntity findSomethingToAttack() {
		float scale = getPitchBlackScale();
		return TerrorAi.findTarget(this, 16.0 + scale * 6.0F, 10.0 + scale * 4.0F, this::isSuitableTarget);
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (Ignored.byHunters(e) || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof PitchBlack || e instanceof TerribleTerror || e instanceof LurkingTerror || e instanceof CreepingHorror) return false;
		if (TerrorAi.isOreSpawn(e, FRIENDS)) return false;
		return !TerrorAi.protectedPlayer(e);
	}

	/** After each hit it ignores further damage for 20 ticks, takes to the air and goes after whoever hit it. */
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (damageTicker > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
		damageTicker = 20;
		boolean hurt = super.hurtServer(level, source, damage);
		Entity e = source.getEntity();
		if (e != null && flightTarget != null) flightTarget = BlockPos.containing(e.getX(), e.getY() + 2.0, e.getZ());
		setFlying(true);
		getNavigation().stop();
		return hurt;
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return random.nextInt(5) == 2 ? ModSounds.get("pitchblack_living") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("pitchblack_hit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("pitchblack_dead");
	}

	@Override
	protected float getSoundVolume() {
		return 0.75F;
	}

	/** OreSpawn's 1 - 0.7 * (4 / size), which the sound system held to 0.5-2 (so 0.5 for every size it has). */
	@Override
	public float getVoicePitch() {
		return Mth.clamp(1.0F - 0.7F * (4.0F / getPitchBlackScale()), 0.5F, 2.0F);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		// PitchBlack.dropFewItems: the amounts grow with its size, so this isn't a loot table.
		float scale = getPitchBlackScale();
		int n = 3 + random.nextInt(2 + (int) (5.0F * scale));
		for (int i = 0; i < n; i++) {
			drop(level, Items.ROTTEN_FLESH);
			int j = random.nextInt(10);
			if (j == 0) drop(level, Items.FEATHER);
			if (j == 1) drop(level, Items.STRING);
			if (j == 2) drop(level, Items.FLINT);
			if (j == 3) drop(level, Items.BEEF);
		}
		drop(level, Terrors.NIGHTMARE_SCALE);
		drop(level, Items.ITEM_FRAME);
		n = 2 + (int) scale + random.nextInt(2 + (int) (5.0F * scale));
		for (int i = 0; i < n; i++) drop(level, ModItems.lazy("zookeeper_shard"));
	}

	private void drop(ServerLevel level, Item item) {
		TerrorAi.dropAround(this, level, new ItemStack(item), 5, getPitchBlackScale());
	}

	/** canDespawn: by day, and not while flying (OreSpawn's despawn check ran with its walking AI). */
	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return level().isBrightOutside() && !flying();
	}

	/** PitchBlack.getCanSpawnHere, the part that doesn't depend on its size: dark enough, at night. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return Monster.isDarkEnoughToSpawn(level, pos, random) && !level.getLevel().isBrightOutside();
	}

	/** PitchBlack.getCanSpawnHere, the size part: a Nightmare bigger than full size needs open air above it. */
	@Override
	public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!super.checkSpawnRules(level, reason)) return false;
		float scale = getPitchBlackScale();
		if (scale < 1.1F) return true;
		int ix = scale > 3.1F ? 2 : 1;
		return TerrorAi.airAbove(level, blockPosition(), -ix, ix, -ix, ix, 1, ix * 3);
	}
}
