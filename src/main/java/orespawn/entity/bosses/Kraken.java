package orespawn.entity.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Bosses;
import orespawn.entity.Chipmunk;
import orespawn.entity.Flight;
import orespawn.entity.Ignored;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * OreSpawn's The Kraken: a 4 x 15 sky squid that brings a storm with it (rain and thunder kept going while it's
 * around, and a lightning bolt under it now and then). It drops straight down from where it appears until it finds the
 * ground within 30 blocks, then drifts about 20 above it, picking spots 12-17 blocks away. Every few ticks it looks for
 * the nearest player within 25 blocks (40 up and down) it can see, or else (half the time) for something on the ground
 * or in water within 20 (not creative or flying players, squids, Attack Squids, other Krakens, Baby Dragons, chickens,
 * chipmunks, stink bugs, Mothras, nor ridden Dragons, Cephadromes, Leonopteryxes or Young Princes), and flies to 15
 * above it. Once within reach (5.5 blocks of the spot 15 below it) it grabs it, holds it 15 blocks below and climbs to
 * 200, biting now and then (40), and lets go at 190 (or at random, or half the time it's hit), so its prey falls. It
 * climbs over anything in its way. When badly hurt (or after three minutes) it flies off upward and may despawn;
 * if a player has hit it and it's down to an eighth of its health above y 130 it calls ten more Krakens (once). A hit
 * makes it stop taking damage for 30 ticks. 1000 health, 40 damage, 10 armor, 500 experience; it ignores fire and falls.
 * <p>
 * Spawning: OreSpawn's rule (y 50 or higher, air or tall grass in the 3 x 2 x 5 space above) is kept, but OreSpawn
 * spawned it only from its Attack Squids (another group's: 1 in 15 Attack Squids killed by a player call 1-3 Krakens at
 * y 170) and eggs; it has no spawn table.
 * <p>
 * Death drops: a Kraken Tooth, an item frame, 120-279 ink sacs and 5-14 rolls of OreSpawn's gear table (its first 53
 * entries; see {@link BossUtil#randomGear}).
 * <p>
 * Holding prey: OreSpawn set the prey's position every tick (a player was pulled along by the server's corrections);
 * here mobs are moved the same way and players are pulled by their motion, and moved back if they get 4 blocks off.
 * <p>
 * Not ported yet: OreSpawn's PlayNicely option. The Kraken Repellent block (it pushes Krakens away) and the Caged
 * Kraken belong to other groups.
 */
public class Kraken extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Kraken.class, EntityDataSerializers.BOOLEAN);
	/** OreSpawn's get_mobstats("Kraken", 1000, 40, 10). */
	public static final int HEALTH = 1000;
	public static final int ATTACK = 40;
	public static final int DEFENSE = 10;

	private final BossBar bar = new BossBar(this, BossEvent.BossBarColor.BLUE);
	private @Nullable BlockPos flightTarget;
	private @Nullable LivingEntity caught;
	private boolean newTarget;
	private boolean release;
	private int weatherSet = 10;
	private int longEnough = 3600;
	private boolean callReinforcements;
	private boolean hitByPlayer;
	private boolean straightDown = true;
	private int hurtTimer;

	public Kraken(EntityType<? extends Kraken> type, Level level) {
		super(type, level);
		this.xpReward = 500;
		this.moveControl = Flight.keepOwnSteering(this);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, HEALTH).add(Attributes.MOVEMENT_SPEED, 0.37)
			.add(Attributes.ATTACK_DAMAGE, ATTACK).add(Attributes.ARMOR, DEFENSE);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(1, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
	}

	public final int getAttacking() {
		return entityData.get(ATTACKING) ? 1 : 0;
	}

	public final void setAttacking(int attacking) {
		entityData.set(ATTACKING, attacking != 0);
	}

	// ---- Sounds ----

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return random.nextInt(5) == 0 ? ModSounds.get("kraken_living") : null;
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("alo_death");
	}

	@Override
	protected float getSoundVolume() {
		return 2.0F;
	}

	// ---- What OreSpawn turned off ----

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public int getArmorValue() {
		return DEFENSE;
	}

	/** canDespawn: once its time is up, or when it's up high and badly hurt. */
	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return longEnough <= 0 || getY() > 150.0 && getHealth() < getMaxHealth() / 2;
	}

	// ---- Boss bar ----

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		bar.add(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		bar.remove(player);
	}

	@Override
	public void setCustomName(@Nullable Component name) {
		super.setCustomName(name);
		bar.rename();
	}

	// ---- Ticking ----

	@Override
	public void tick() {
		super.tick();
		if (isRemoved() || isDeadOrDying()) return;
		Vec3 m = getDeltaMovement();
		if (flightTarget == null) {
			flightTarget = BlockPos.containing(getX(), getY() - 10.0, getZ());
		} else {
			setDeltaMovement(m.x, m.y * (getY() < flightTarget.getY() ? 0.72 : 0.5), m.z);
		}
		if (weatherSet > 0 && --weatherSet == 0 && level() instanceof ServerLevel level) {
			// Rain and thunder for 15 seconds, renewed every 5.
			level.getServer().setWeatherParameters(0, 300, true, !level.isRaining() || level.isThundering());
			weatherSet = 100;
		}
	}

	private boolean canSeeSpot(BlockPos p) {
		Vec3 from = new Vec3(getX(), getY() + 0.75, getZ());
		Vec3 to = new Vec3(p.getX(), p.getY(), p.getZ());
		return level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (isRemoved() || isDeadOrDying()) return;
		bar.update();
		if (hurtTimer > 0) hurtTimer--;
		if (longEnough > 0) longEnough--;
		if (random.nextInt(400) == 1) {
			LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
			if (bolt != null) {
				bolt.snapTo(getX(), getY() - 16.0, getZ());
				level.addFreshEntity(bolt);
			}
		}
		if (flightTarget == null) flightTarget = blockPosition();
		if (newTarget || random.nextInt(250) == 1 || Flight.distSq(this, flightTarget) < 9.1F) {
			pickSpot(level);
		} else if (caught == null && random.nextInt(8) == 1) {
			hunt(level);
		}
		if (caught != null) carry(level);

		// Steer: 0.45 blocks a tick across, 0.71 up or down.
		double var1 = flightTarget.getX() + 0.3 - getX();
		double var3 = flightTarget.getY() + 0.1 - getY();
		double var5 = flightTarget.getZ() + 0.3 - getZ();
		Vec3 m = getDeltaMovement();
		double mx = m.x + (Math.signum(var1) * 0.45 - m.x) * 0.15;
		double my = m.y + (Math.signum(var3) * 0.70999 - m.y) * 0.202;
		double mz = m.z + (Math.signum(var5) * 0.45 - m.z) * 0.15;
		float var7 = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0F;
		float var8 = Mth.wrapDegrees(var7 - getYRot());
		zza = 0.4F;
		if (Math.abs(mx) + Math.abs(mz) < 0.15) var8 = 0.0F;
		setYRot(getYRot() + var8 / 5.0F);
		// Climb over whatever is ahead: each solid block in a 10 x 38 strip in front lifts it a little.
		double obstruction = 0.0;
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		double dir = Math.toRadians(getYRot() + 90.0F);
		for (int k = -20; k < 18; k += 2) {
			for (int i = 1; i < 10; i += 2) {
				double dx = i * Math.cos(dir);
				double dz = i * Math.sin(dir);
				if (!level.getBlockState(p.set((int) (getX() + dx), (int) getY() + k, (int) (getZ() + dz))).isAir()) obstruction += 0.1;
			}
		}
		setDeltaMovement(mx, my + obstruction * 0.08, mz);
		if (obstruction > 0.0) setPos(getX(), getY() + obstruction * 0.08, getZ());
		if (getY() > 256.0 && !isPersistenceRequired()) discard();
	}

	/** A new spot: straight down until there's ground within 30, then 12-17 blocks off and about 20 above the ground. */
	private void pickSpot(ServerLevel level) {
		newTarget = false;
		int groundDist;
		for (groundDist = 0; groundDist < 31; groundDist++) {
			if (!level.getBlockState(BlockPos.containing(getX(), getY() - groundDist, getZ())).isAir()) {
				straightDown = false;
				break;
			}
		}
		groundDist = 20 - groundDist;
		for (int tries = 50; tries > 0; tries--) {
			int zdir = random.nextInt(6) + 12;
			int xdir = random.nextInt(6) + 12;
			if (random.nextInt(2) == 0) zdir = -zdir;
			if (random.nextInt(2) == 0) xdir = -xdir;
			if (straightDown) {
				xdir = 0;
				zdir = 0;
			}
			flightTarget = BlockPos.containing(getX() + xdir, (int) getY() + groundDist + random.nextInt(9) - 6, getZ() + zdir);
			if (level.getBlockState(flightTarget).isAir() && canSeeSpot(flightTarget)) break;
		}
		if (longEnough <= 0 || getY() < 200.0 && getHealth() < getMaxHealth() / 4) {
			flightTarget = flightTarget.above(30);
			if (hitByPlayer && !callReinforcements && getHealth() < getMaxHealth() / 8 && getY() > 130.0) {
				callReinforcements = true;
				for (int i = 0; i < 10; i++) {
					Kraken k = Bosses.THE_KRAKEN.create(level, EntitySpawnReason.MOB_SUMMONED);
					if (k == null) continue;
					k.snapTo(getX() + random.nextInt(10) - random.nextInt(10), 170.0, getZ() + random.nextInt(10) - random.nextInt(10),
						random.nextFloat() * 360.0F, 0.0F);
					level.addFreshEntity(k);
					k.playAmbientSound();
				}
			}
		}
	}

	private void hunt(ServerLevel level) {
		Player target = level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(25.0, 40.0, 25.0), Player::isAlive).stream()
			.min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
		if (target != null) {
			if (!target.isCreative() && !target.isSpectator()) {
				if (getSensing().hasLineOfSight(target)) {
					flightTarget = BlockPos.containing(target.getX(), (int) target.getY() + 15, target.getZ());
					tryToCatch(target);
				}
			} else {
				target = null;
			}
		}
		if (target == null && random.nextInt(2) == 0) {
			LivingEntity e = findSomethingToAttack(level);
			if (e != null) {
				flightTarget = BlockPos.containing(e.getX(), (int) e.getY() + 15, e.getZ());
				tryToCatch(e);
			}
		}
	}

	/** attackWithSomething: grabs it when it's within 5.5 blocks of the spot 15 below. */
	private void tryToCatch(LivingEntity e) {
		if (caught != null) return;
		double dx = getX() - e.getX();
		double dz = getZ() - e.getZ();
		double dy = getY() - e.getY() - 15.0;
		if (dx * dx + dz * dz + dy * dy < 30.0) {
			caught = e;
			release = false;
			setAttacking(1);
		}
	}

	/** Holds the prey 15 below and climbs to 200, biting now and then; lets go at 190, at random, or when hit. */
	private void carry(ServerLevel level) {
		LivingEntity e = caught;
		if (e == null) return;
		if (!e.isAlive() || e.isRemoved()) {
			letGo();
			return;
		}
		flightTarget = BlockPos.containing(getX(), 200, getZ());
		if (getY() > 190.0) release = true;
		Vec3 m = getDeltaMovement();
		double my = m.y;
		if (getY() - e.getY() > 16.0) my += 0.25;
		if (e instanceof ServerPlayer player) {
			// Players are moved by their client: pull them by their motion, and put them back if they get away.
			Vec3 want = new Vec3(getX(), getY() - 15.0, getZ());
			Vec3 gap = want.subtract(e.position());
			if (gap.lengthSqr() > 16.0) {
				player.teleportTo(level, want.x, want.y, want.z, java.util.Set.of(), getYRot(), player.getXRot(), false);
			}
			player.setDeltaMovement(m.x + gap.x * 0.5, my + gap.y * 0.5, m.z + gap.z * 0.5);
			player.syncVelocity = true;
			player.resetFallDistance();
		} else {
			e.setDeltaMovement(m.x, my, m.z);
			e.setPos(getX(), getY() - 15.0, getZ());
			e.setYRot(getYRot());
		}
		if (random.nextInt(50) == 1) doHurtTarget(level, e);
		if (release || random.nextInt(250) == 1) letGo();
	}

	private void letGo() {
		caught = null;
		newTarget = true;
		release = false;
		setAttacking(0);
	}

	// ---- Being hurt ----

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (BossUtil.unstoppable(source)) return super.hurtServer(level, source, damage);
		Entity e = source.getEntity();
		if (flightTarget != null && e instanceof Player && getHealth() > getMaxHealth() / 4) {
			hitByPlayer = true;
			flightTarget = BlockPos.containing(e.getX(), (int) e.getY() + 15, e.getZ());
		}
		if (hurtTimer > 0) return false;
		hurtTimer = 30;
		boolean ret = super.hurtServer(level, source, damage);
		if (random.nextInt(2) == 1) release = true;
		return ret;
	}

	// ---- Targets ----

	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || Ignored.byHunters(e) || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Player p) return !p.isCreative() && !p.isSpectator() && !p.getAbilities().flying;
		if (!e.onGround() && !e.isInWater()) return false;
		if (e instanceof Squid || e instanceof Kraken || e instanceof Chicken || e instanceof Chipmunk) return false;
		if (BossUtil.isType(e, "attack_squid") || BossUtil.isType(e, "baby_dragon") || BossUtil.isType(e, "stink_bug")
			|| BossUtil.isType(e, "mothra")) {
			return false;
		}
		if (BossUtil.isType(e, "dragon") || BossUtil.isType(e, "cephadrome") || BossUtil.isType(e, "leonopteryx")
			|| e instanceof ThePrinceTeen || e instanceof ThePrinceAdult) {
			return !e.isVehicle();
		}
		return true;
	}

	private @Nullable LivingEntity findSomethingToAttack(ServerLevel level) {
		List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(20.0, 40.0, 20.0));
		list.sort(BossUtil.targetOrder(this));
		for (LivingEntity e : list) {
			if (isSuitableTarget(e)) return e;
		}
		return null;
	}

	// ---- Death ----

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		BossUtil.dropAround(this, Bosses.KRAKEN_TOOTH, 8, 1.0, 0);
		BossUtil.dropAround(this, Items.ITEM_FRAME, 8, 1.0, 0);
		int n = 120 + random.nextInt(160);
		for (int i = 0; i < n; i++) BossUtil.dropAround(this, Items.INK_SAC, 8, 1.0, 0);
		n = 5 + random.nextInt(10);
		for (int i = 0; i < n; i++) {
			BossUtil.randomGear(this, random.nextInt(53), s -> BossUtil.dropAround(this, s, 8, 1.0, 0), s -> BossUtil.dropAround(this, s, 3, 1.0, 0));
		}
	}

	// ---- Saving ----

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("LongEnough", longEnough);
		output.putBoolean("HitByPlayer", hitByPlayer);
		output.putBoolean("CalledReinforcements", callReinforcements);
		output.putBoolean("StraightDown", straightDown);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		longEnough = input.getIntOr("LongEnough", 3600);
		hitByPlayer = input.getBooleanOr("HitByPlayer", false);
		callReinforcements = input.getBooleanOr("CalledReinforcements", false);
		straightDown = input.getBooleanOr("StraightDown", true);
		bar.rename();
	}

	// ---- Spawning ----

	/** getCanSpawnHere: y 50 or higher, and air or tall grass in the 3 x 2 x 5 space just above. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (pos.getY() < 50) return false;
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int k = -1; k < 2; k++) {
			for (int j = -1; j < 1; j++) {
				for (int i = 1; i < 6; i++) {
					BlockState s = level.getBlockState(p.set(pos.getX() + j, pos.getY() + i, pos.getZ() + k));
					if (!s.isAir() && !s.is(Blocks.SHORT_GRASS) && !s.is(Blocks.TALL_GRASS)) return false;
				}
			}
		}
		return true;
	}
}
