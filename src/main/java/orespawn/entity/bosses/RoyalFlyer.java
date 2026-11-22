package orespawn.entity.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Bosses;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * What The Young Prince and The Young Adult Prince share (OreSpawn's ThePrinceTeen and ThePrinceAdult were the same
 * class with different numbers): three-headed winged dragons that walk (activity 0) or fly (activity 1).
 * <p>
 * On the ground they follow their owner, come for raw beef, wander and look about, and target monsters; now and then
 * they take off (1 in 15 of the 1-in-50 mood checks, or at once when their owner flies, gets too far away, or something
 * hits them). In the air they pick spots round their owner (or round themselves), fly at prey they find (monsters,
 * Mothras, Krakens, untamed Leonopteryxes, Water Dragons and Gamma Metroids; never royalty): bite it up close, flying
 * off again after; or, with their fire lit, shoot one of a fireball, a thunder bolt or an ice ball when facing it.
 * Tamed and below a quarter of their health, they fly away from it instead. They lift themselves over whatever is
 * below and ahead.
 * <p>
 * Their owner rides one by clicking it with an empty hand (it then flies): it turns after where the rider looks, forward
 * speeds it up, back slows it, the jump key climbs (OreSpawn's own fly-up key), left or right shoots in turn a
 * fireball, an ice ball and a thunder bolt where the rider looks; it bites what comes near and fires at what's further,
 * and shoves aside what it flies into. Tamed with a diamond block (which also fills them up for growing); fed raw beef
 * (full health) or any food (10 x its hunger); flint and steel lights their fire, ice puts it out; a name tag names
 * them; a diamond shrinks them back a stage; anything else makes them sit or stand. Fire, lava, cactus and suffocation
 * don't hurt them, nor their own kind; fireballs that hit them are put out; after a hit they take no more for 20
 * ticks; their owner can't hurt them. They breathe underwater and never take fall damage.
 * <p>
 * Not ported yet: EntityAIMoveIndoors (26.3 has no such goal); OreSpawn's pet health overlay. The thunder bolts and ice
 * balls are the weapons group's ({@code orespawn:thunder_bolt}, {@code orespawn:ice_ball}), looked up by id.
 */
public abstract class RoyalFlyer extends TamableAnimal {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(RoyalFlyer.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> ACTIVITY = SynchedEntityData.defineId(RoyalFlyer.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> FIRE = SynchedEntityData.defineId(RoyalFlyer.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> HEAD1 = SynchedEntityData.defineId(RoyalFlyer.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> HEAD2 = SynchedEntityData.defineId(RoyalFlyer.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> HEAD3 = SynchedEntityData.defineId(RoyalFlyer.class, EntityDataSerializers.INT);

	private @Nullable BlockPos flightTarget;
	private boolean targetInSight;
	private int ownerFlying;
	private int flyaway;
	private int hurtTimer;
	private int wingSound;
	private int whichAttack;
	private int fireballTicker;
	private final int[] headExt = new int[3];
	private final int[] headDir = {1, 1, 1};
	private float deltaSmooth;
	/** The body's turn, smoothed (OreSpawn's (prevRotationYaw - rotationYaw) * 10, eased in its RenderInfo.rf1); client side. */
	public float turn;

	protected RoyalFlyer(EntityType<? extends RoyalFlyer> type, Level level) {
		super(type, level);
	}

	// ---- The numbers that differ ----

	/** OreSpawn's health (under 26.3's own 1024 cap, hits and heals are scaled by cap / this: see {@link #scale}). */
	protected abstract float oreSpawnHealth();

	protected abstract float lookRange();

	/** Bite reach (plus half the prey's width), bite damage, its knockback and lift. */
	protected abstract float biteReach();

	protected abstract float biteDamage();

	protected abstract double knock();

	protected abstract double knockLift();

	/** fly_without_rider: 1 in this many ticks it looks for prey; shooting range (squared). */
	protected abstract int huntChance();

	protected abstract double shootRangeSq();

	/** fly_with_rider's shooting band (squared): over min, under max. */
	protected abstract double riddenShootMinSq();

	/** Flight spots: round a walking owner (base, spread), a flying owner (spread), or itself (base, spread). */
	protected abstract int[] wanderRanges();

	protected abstract double huntRange();

	protected abstract int wingInterval();

	protected abstract double ownerFarSq();

	protected abstract float idleHeal();

	/** Riding: ground check depth, top speed, forward acceleration, climb per tick and per unit of speed. */
	protected abstract double maxSpeed();

	protected abstract double accel();

	protected abstract double climb();

	protected abstract double climbPerSpeed();

	/** Rider shots: height, distance ahead, per-head-stretch height change, a big fireball, ticks between shots. */
	protected abstract double shotHeight();

	protected abstract double shotAhead();

	protected abstract double shotPerExt();

	protected abstract boolean bigRiderFireball();

	protected abstract int shotTicks();

	/** updateRiderPosition: how far ahead and how high the rider sits; how far round it shoves things. */
	protected abstract double riderAhead();

	protected abstract double riderHeight();

	protected abstract double shoveX();

	protected abstract double shoveY();

	protected abstract double jumpBoost();

	protected abstract double interactRangeSq();

	protected abstract double sitRangeSq();

	protected abstract SoundEvent roar();

	/** A diamond block: tamed / filled up for growing (each stage does its own). */
	protected abstract void diamondBlock(Player player);

	/** A diamond: shrinks back a stage (each stage does its own). */
	protected abstract void shrink(ServerLevel level, Player player);

	/** The ground AI's growing up (each stage does its own); runs while it walks. */
	protected abstract void growTick(ServerLevel level);

	// ---- Synced state ----

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
		builder.define(ACTIVITY, 0);
		builder.define(FIRE, 1);
		builder.define(HEAD1, 0);
		builder.define(HEAD2, 0);
		builder.define(HEAD3, 0);
	}

	public int getAttacking() {
		return entityData.get(ATTACKING) ? 1 : 0;
	}

	public void setAttacking(int a) {
		if (!level().isClientSide()) entityData.set(ATTACKING, a != 0);
	}

	/** 0 walking, 1 flying. */
	public int getActivity() {
		return entityData.get(ACTIVITY);
	}

	public void setActivity(int a) {
		if (!level().isClientSide()) entityData.set(ACTIVITY, a);
	}

	public int getFire() {
		return entityData.get(FIRE);
	}

	public void setFire(int f) {
		if (!level().isClientSide()) entityData.set(FIRE, f);
	}

	/** How far each neck is stretched (0-60), for the model. */
	public int headExt(int i) {
		return entityData.get(i == 0 ? HEAD1 : i == 1 ? HEAD2 : HEAD3);
	}

	/** What hits and heals are scaled by: 1 when max_health's cap allows the full OreSpawn health, else cap / it. */
	protected final float scale() {
		return BossUtil.healthScale(oreSpawnHealth());
	}

	// ---- Goals: only while walking (OreSpawn's flying skipped its vanilla AI) ----

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.1, 12.0F, 2.0F) {
			@Override
			public boolean canUse() {
				return getActivity() == 0 && super.canUse();
			}

			@Override
			public boolean canContinueToUse() {
				return getActivity() == 0 && super.canContinueToUse();
			}
		});
		goalSelector.addGoal(2, new TemptGoal(this, 1.25, s -> s.is(Items.BEEF), false) {
			@Override
			public boolean canUse() {
				return getActivity() == 0 && super.canUse();
			}
		});
		goalSelector.addGoal(3, new WanderGoal(this, 0.75, 90, 10) {
			@Override
			public boolean canUse() {
				return getActivity() == 0 && super.canUse();
			}
		});
		goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, lookRange()));
		goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Mob.class, 0, true, false, (e, l) -> e instanceof Enemy));
		targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}

	// ---- Sounds ----

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		if (isOrderedToSit()) return null;
		return getActivity() == 1 && !isVehicle() ? roar() : null;
	}

	// ---- What OreSpawn turned off ----

	@Override
	public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean isPushable() {
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

	/** canDespawn: only wild, unridden, unnamed ones. */
	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isVehicle() && !isTame();
	}

	@Override
	public void jumpFromGround() {
		super.jumpFromGround();
		setDeltaMovement(getDeltaMovement().add(0.0, jumpBoost(), 0.0));
	}

	// ---- Riding (the 26.3 way: the rider's game moves the mount, see travel) ----

	@Override
	public @Nullable LivingEntity getControllingPassenger() {
		return isTame() && getFirstPassenger() instanceof Player p ? p : super.getControllingPassenger();
	}

	/** updateRiderPosition: riderAhead in front, at the mounted height (26.3 seats the player 0.6 below the point). */
	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
		return new Vec3(0.0, riderHeight() + 0.1, riderAhead());
	}

	@Override
	protected Vec3 getRiddenInput(Player controller, Vec3 selfInput) {
		return Vec3.ZERO;
	}

	@Override
	public void travel(Vec3 input) {
		if (getControllingPassenger() instanceof Player rider) {
			flyRidden(rider);
		} else if (getActivity() != 0) {
			move(MoverType.SELF, getDeltaMovement());
		} else {
			super.travel(input);
		}
	}

	/** How much is solid below and ahead (OreSpawn's obstruction_factor), out to a distance growing with speed. */
	private double obstruction(double velocity, int base, double perSpeed) {
		double factor = 0.0;
		int dist = base + (int) (velocity * perSpeed);
		double c = Math.cos(Math.toRadians(getYRot() + 90.0F));
		double s = Math.sin(Math.toRadians(getYRot() + 90.0F));
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int k = 1; k < dist; k++) {
			for (int i = 1; i < dist * 2; i++) {
				if (!level().isEmptyBlock(p.set(Mth.floor(getX() + i * c), Mth.floor(getY()) - k, Mth.floor(getZ() + i * s)))) factor += 0.05;
			}
		}
		return factor;
	}

	/** The ridden half of OreSpawn's onLivingUpdate (runs on the rider's game). */
	private void flyRidden(Player rider) {
		Vec3 m = getDeltaMovement();
		double mx = Mth.clamp(m.x, -2.0, 2.0);
		double my = m.y;
		double mz = Mth.clamp(m.z, -2.0, 2.0);
		double velocity = Math.sqrt(mx * mx + mz * mz);
		if (!level().isEmptyBlock(BlockPos.containing(getX(), (float) getY() - 1.25F, getZ()))) {
			my += 0.03;
			setPos(getX(), getY() + 0.1, getZ());
		} else {
			my -= 0.018;
		}
		double obstruction = obstruction(velocity, 3, 7.0);
		my += obstruction * 0.07;
		setPos(getX(), getY() + obstruction * 0.07, getZ());
		if (my > 2.0) my = 2.0;
		// Swing round after the rider, more slowly the faster it goes.
		double riderYaw = ((rider.getYRot() % 360.0) + 360.0) % 360.0;
		double ownYaw = ((getYRot() % 360.0) + 360.0) % 360.0;
		double rel = ((riderYaw - ownYaw) % 180.0 + 180.0) % 180.0;
		if (rel > 90.0) rel -= 180.0;
		float yaw = velocity > 0.01 ? rider.getYRot() + (float) (rel * Mth.clamp(Math.abs(1.85 - velocity), 0.01, 0.9)) : rider.getYRot();
		setYRot(yaw);
		setXRot(2.0F * (float) velocity);
		yBodyRot = yaw;
		yHeadRot = yaw;
		double newVelocity = velocity;
		double rhm = Math.atan2(mz, mx);
		double rhdir = Math.toRadians((rider.getYRot() + 90.0F) % 360.0F);
		double rdv = Math.abs(rhm - rhdir) % (Math.PI * 2.0);
		if (rdv > Math.PI) rdv -= Math.PI * 2.0;
		rdv = Math.abs(rdv);
		if (Math.abs(newVelocity) < 0.01) rdv = 0.0;
		if (rdv > 1.5) newVelocity = -newVelocity;
		if (rider.isJumping()) my += climb() + velocity * climbPerSpeed();
		float im = rider.zza;
		double max = maxSpeed();
		if (Math.abs(im) > 0.001F) {
			double dv;
			if (im > 0.0F) {
				dv = accel();
				if (deltaSmooth < 0.0F) deltaSmooth = 0.0F;
				deltaSmooth = (float) Math.min(deltaSmooth + dv / 10.0, dv);
			} else {
				max = 0.35;
				dv = -0.02;
				if (deltaSmooth > 0.0F) deltaSmooth = 0.0F;
				deltaSmooth = (float) Math.max(deltaSmooth + dv / 10.0, dv);
			}
			newVelocity = Mth.clamp(newVelocity + deltaSmooth, -max, max);
		}
		double dir = Math.toRadians(getYRot() + (newVelocity >= 0.0 ? 90.0F : 270.0F));
		mx = Math.cos(dir) * Math.abs(newVelocity);
		mz = Math.sin(dir) * Math.abs(newVelocity);
		move(MoverType.SELF, new Vec3(mx, my, mz));
		setDeltaMovement(mx * 0.985, my * 0.94, mz * 0.985);
	}

	// ---- Ticking ----

	@Override
	public void tick() {
		super.tick();
		// OreSpawn's flying princes pass through blocks (noClip), lifting themselves over what's below and ahead.
		noPhysics = getActivity() != 0;
		if (hurtTimer > 0) hurtTimer--;
		if (getActivity() == 1 && ++wingSound > wingInterval()) {
			if (!level().isClientSide()) playSound(wingSound(), 0.5F, 1.0F);
			wingSound = 0;
		}
		if (isInWater()) setDeltaMovement(getDeltaMovement().add(0.0, 0.07, 0.0));
		if (level().isClientSide()) {
			float raw = Mth.wrapDegrees(yRotO - getYRot()) * 10.0F;
			turn = Mth.clamp(turn + (raw - turn) * 0.06F, -50.0F, 50.0F);
			return;
		}
		// The necks stretch and pull back at random (server side, synced).
		for (int i = 0; i < 3; i++) {
			if (random.nextInt(10) == 1) headDir[i] = new int[] {2, -2, 0}[random.nextInt(3)];
			headExt[i] = Mth.clamp(headExt[i] + headDir[i], 0, 60);
		}
		entityData.set(HEAD1, headExt[0]);
		entityData.set(HEAD2, headExt[1]);
		entityData.set(HEAD3, headExt[2]);
		if (getActivity() == 0 && isTame() && getOwner() != null && !isOrderedToSit() && distanceToSqr(getOwner()) > ownerFarSq()) setActivity(1);
	}

	/** OreSpawn's "MothraWings", registered lower-case (26.3 ids can't have capitals). */
	private static SoundEvent wingSound() {
		return ModSounds.get("mothrawings");
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (isRemoved() || isDeadOrDying()) return;
		if (getActivity() == 0) {
			if (!isOrderedToSit() && !isVehicle() && level.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(10) == 1) {
				if (findSomethingToAttack(level) != null) setActivity(1);
				else setAttacking(0);
			}
			growTick(level);
		} else {
			if (fireballTicker > 0) fireballTicker--;
			if (getFirstPassenger() instanceof ServerPlayer rider && isTame()) {
				riddenShots(level, rider);
				shove(level, rider);
				flyWithRider(level);
			} else {
				flyWithoutRider(level);
			}
		}
		alwaysDo();
	}

	private void alwaysDo() {
		if (random.nextInt(250) == 1 && getHealth() < getMaxHealth()) heal(idleHeal() * scale());
		if (random.nextInt(250) == 0) setTarget(null);
		if (isOrderedToSit()) return;
		ownerFlying = 0;
		if (isTame() && getOwner() instanceof Player p && !isVehicle() && p.getAbilities().flying) {
			ownerFlying = 1;
			setActivity(1);
		}
		if (random.nextInt(50) == 1 && !targetInSight && !isVehicle()) setActivity(random.nextInt(15) == 1 ? 1 : 0);
	}

	/** Left or right shoots in turn a fireball, an ice ball and a thunder bolt where the rider looks. */
	private void riddenShots(ServerLevel level, ServerPlayer rider) {
		var input = rider.getLastClientInput();
		if (fireballTicker != 0 || !(input.left() || input.right())) return;
		double yoff = shotHeight();
		double ahead = shotAhead();
		whichAttack = (whichAttack + 1) % 3;
		Vec3 m = getDeltaMovement();
		double lookX = Math.cos(Math.toRadians(rider.getYHeadRot() + 90.0F));
		double lookZ = Math.sin(Math.toRadians(rider.getYHeadRot() + 90.0F));
		double lookY = -Math.sin(Math.toRadians(rider.getXRot()));
		if (whichAttack == 0) {
			yoff += headExt(0) * shotPerExt();
			double yaw = Math.toRadians(getYRot() - 10.0F);
			BetterFireball bf = new BetterFireball(level, this, lookX, lookY, lookZ);
			bf.setNotMe();
			if (bigRiderFireball()) bf.setBig();
			bf.setPos(getX() - ahead * Math.sin(yaw) - m.x * 3.0, getY() + yoff - m.y * 3.0, getZ() + ahead * Math.cos(yaw) - m.z * 3.0);
			// OreSpawn kept the mount's speed and accelerated it along the look; 26.3's fireballs speed up along their own
			// heading, so it starts along the look at the mount's speed.
			bf.setDeltaMovement(new Vec3(lookX, lookY, lookZ).normalize().scale(m.length() + 0.1));
			playSound(SoundEvents.TNT_PRIMED, 1.0F, bowPitch());
			level.addFreshEntity(bf);
		} else if (whichAttack == 1) {
			yoff += headExt(2) * shotPerExt();
			double yaw = Math.toRadians(getYRot() + 10.0F);
			Projectile p = BossUtil.weapon(level, "ice_ball", rider, getX() - ahead * Math.sin(yaw), getY() + yoff, getZ() + ahead * Math.cos(yaw), true);
			if (p != null) {
				float lift = (float) Math.sqrt(lookX * lookX + lookZ * lookZ) * 0.2F;
				p.shoot(lookX, lookY + lift, lookZ, 1.4F, 5.0F);
				p.setPos(p.getX() - m.x * 3.0, p.getY() - m.y * 3.0, p.getZ() - m.z * 3.0);
				p.setDeltaMovement(p.getDeltaMovement().scale(2.0));
				playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 0.75F, bowPitch());
				level.addFreshEntity(p);
			}
		} else {
			yoff += headExt(1) * shotPerExt();
			double yaw = Math.toRadians(getYRot());
			Projectile p = BossUtil.weapon(level, "thunder_bolt", rider, getX() - ahead * Math.sin(yaw), getY() + yoff, getZ() + ahead * Math.cos(yaw),
				false);
			if (p != null) {
				p.shootFromRotation(rider, rider.getXRot(), rider.getYRot(), 0.0F, 1.5F, 1.0F);
				p.setDeltaMovement(p.getDeltaMovement().scale(3.0));
				playSound(SoundEvents.ARROW_SHOOT, 0.75F, bowPitch());
				level.addFreshEntity(p);
			}
		}
		fireballTicker = shotTicks();
	}

	/** Shoves aside what it flies into while ridden. */
	private void shove(ServerLevel level, Player rider) {
		List<Entity> list = level.getEntities(this, getBoundingBox().inflate(shoveX(), shoveY(), shoveX()));
		for (Entity e : list) {
			if (e != rider && e.isAlive() && e.isPushable()) e.push(this);
		}
	}

	private void flyWithRider(ServerLevel level) {
		if (isOrderedToSit() || random.nextInt(5) != 1 || level.getDifficulty() == Difficulty.PEACEFUL) return;
		LivingEntity e = findSomethingToAttack(level);
		if (e == null) {
			setAttacking(0);
			return;
		}
		setAttacking(1);
		float reach = biteReach() + e.getBbWidth() / 2.0F;
		double d = distanceToSqr(e);
		if (d < reach * reach) doHurtTarget(level, e);
		else if (d > riddenShootMinSq() && d < 625.0 && !isInWater() && getFire() != 0) shootSomething(level, e.getX(), e.getY(), e.getZ());
	}

	private void flyWithoutRider(ServerLevel level) {
		boolean doNew = false;
		boolean tooFar = false;
		if (flightTarget == null) {
			doNew = true;
			flightTarget = blockPosition();
		}
		LivingEntity owner = isTame() ? getOwner() : null;
		if (owner != null && distanceToSqr(owner) > 400.0) {
			tooFar = true;
			targetInSight = false;
			setAttacking(0);
			setOrderedToSit(false);
			flyaway = 0;
			doNew = true;
		}
		if (isOrderedToSit()) return;
		Vec3 m = getDeltaMovement();
		double mFactor = getY() < flightTarget.getY() + 2.0 ? 0.7 : getY() > flightTarget.getY() - 2.0 ? 0.5 : 0.61;
		setDeltaMovement(m.x, m.y * mFactor, m.z);
		if (random.nextInt(300) == 1) doNew = true;
		if (flyaway > 0) flyaway--;
		if (!tooFar && flyaway == 0 && level.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(huntChance()) == 1) {
			LivingEntity e = getTarget();
			if (e != null && !e.isAlive()) {
				setTarget(null);
				e = null;
			}
			if (e == null) e = findSomethingToAttack(level);
			if (e != null) {
				if (isTame() && getHealth() / getMaxHealth() < 0.25F) {
					setActivity(1);
					setAttacking(0);
					targetInSight = false;
					doNew = false;
					flightTarget = BlockPos.containing(getX() + (getX() - e.getX()), getY() + 1.0, getZ() + (getZ() - e.getZ()));
				} else {
					setActivity(1);
					setAttacking(1);
					targetInSight = true;
					flightTarget = BlockPos.containing(e.getX(), e.getY() + 1.0, e.getZ());
					doNew = false;
					float reach = biteReach() + e.getBbWidth() / 2.0F;
					if (distanceToSqr(e) < reach * reach) {
						doHurtTarget(level, e);
						flyaway = 5 + random.nextInt(15);
						doNew = true;
					} else if (distanceToSqr(e) < shootRangeSq() && !isInWater() && getFire() != 0 && random.nextInt(2) == 1) {
						shootSomething(level, e.getX(), e.getY(), e.getZ());
					}
				}
			} else {
				targetInSight = false;
				flyaway = 0;
				setAttacking(0);
			}
		}
		if (flightTarget.distSqr(blockPosition()) < 2.1F) doNew = true;
		if (doNew && !targetInSight || doNew && flyaway != 0) newFlightTarget(level, owner);
		double velocity = Math.sqrt(m.x * m.x + m.z * m.z);
		double obstruction = obstruction(velocity, 2, 4.0);
		m = getDeltaMovement();
		setDeltaMovement(m.x, m.y + obstruction * 0.05, m.z);
		if (obstruction > 0.0) setPos(getX(), getY() + obstruction * 0.05, getZ());
		double speed = 0.5;
		if (ownerFlying != 0) {
			speed = 1.75;
			if (owner != null && distanceToSqr(owner) > 64.0) speed = 3.5;
		}
		double var1 = flightTarget.getX() + 0.5 - getX();
		double var3 = flightTarget.getY() + 0.1 - getY();
		double var5 = flightTarget.getZ() + 0.5 - getZ();
		m = getDeltaMovement();
		double mx = m.x + (Math.signum(var1) - m.x) * 0.15 * speed;
		double my = m.y + (Math.signum(var3) - m.y) * 0.21 * speed;
		double mz = m.z + (Math.signum(var5) - m.z) * 0.15 * speed;
		setDeltaMovement(mx, my, mz);
		float var7 = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0F;
		zza = (float) (0.75 * speed);
		setYRot(getYRot() + Mth.wrapDegrees(var7 - getYRot()) / 4.0F);
		yBodyRot = getYRot();
	}

	private void newFlightTarget(ServerLevel level, @Nullable LivingEntity owner) {
		int[] r = wanderRanges();
		for (int tries = 10; tries > 0; tries--) {
			int gox = Mth.floor(getX());
			int goy = Mth.floor(getY());
			int goz = Mth.floor(getZ());
			int xdir;
			int zdir;
			if (owner != null) {
				gox = Mth.floor(owner.getX());
				goy = Mth.floor(owner.getY());
				goz = Mth.floor(owner.getZ());
				if (ownerFlying == 0) {
					zdir = random.nextInt(r[1]) + r[0];
					xdir = random.nextInt(r[1]) + r[0];
				} else {
					zdir = random.nextInt(r[2]);
					xdir = random.nextInt(r[2]);
				}
			} else {
				zdir = random.nextInt(r[4]) + r[3];
				xdir = random.nextInt(r[4]) + r[3];
			}
			if (random.nextInt(2) == 1) zdir = -zdir;
			if (random.nextInt(2) == 1) xdir = -xdir;
			flightTarget = new BlockPos(gox + xdir, goy + random.nextInt(9 + ownerFlying * 2) - 4, goz + zdir);
			if (level.isEmptyBlock(flightTarget) && canSeeSpot(flightTarget)) break;
		}
	}

	private boolean canSeeSpot(BlockPos p) {
		Vec3 from = new Vec3(getX(), getY() + 0.75, getZ());
		Vec3 to = new Vec3(p.getX(), p.getY(), p.getZ());
		return level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
	}

	// ---- Attacks ----

	private float bowPitch() {
		return 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F);
	}

	/** shoot_something: a fireball, a thunder bolt or an ice ball (one at random), if facing the target. */
	private void shootSomething(ServerLevel level, double x, double y, double z) {
		int which = random.nextInt(3);
		double rr = Math.atan2(z - getZ(), x - getX());
		double rhdir = Math.toRadians((getYRot() + 90.0F) % 360.0F);
		double rdd = Math.abs(rr - rhdir) % (Math.PI * 2.0);
		if (rdd > Math.PI) rdd -= Math.PI * 2.0;
		if (Math.abs(rdd) >= 0.5) return;
		double yaw = Math.toRadians(getYRot());
		double cx = getX() - 6.0 * Math.sin(yaw);
		double cz = getZ() + 6.0 * Math.cos(yaw);
		double cy = getY() + 3.5;
		playSound(SoundEvents.ARROW_SHOOT, 1.0F, bowPitch());
		float r1 = 5.0F * (random.nextFloat() - random.nextFloat());
		float r2 = 3.0F * (random.nextFloat() - random.nextFloat());
		float r3 = 5.0F * (random.nextFloat() - random.nextFloat());
		if (which == 0) {
			BetterFireball bf = new BetterFireball(level, this, x - cx + r1, y + 0.25 - cy + r2, z - cz + r3);
			bf.snapTo(cx, cy, cz, getYRot(), 0.0F);
			bf.setBig();
			level.addFreshEntity(bf);
		} else {
			BossUtil.lob(level, BossUtil.weapon(level, which == 1 ? "thunder_bolt" : "ice_ball", this, cx, cy, cz, which == 2), x, y, z);
		}
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (target instanceof LivingEntity) {
			float kraken = target instanceof Kraken ? 2.0F : 1.0F;
			target.hurtServer(level, damageSources().mobAttack(this), kraken * biteDamage());
			float f3 = (float) Math.atan2(target.getZ() - getZ(), target.getX() - getX());
			double inair = knockLift();
			if (!target.isAlive() || target instanceof Player) inair *= 2.0;
			target.push(Math.cos(f3) * knock(), inair, Math.sin(f3) * knock());
			if (target instanceof Player) target.syncVelocity = true;
			onBite(target);
		}
		return true;
	}

	/** After a bite (The Young Prince counts its kills). */
	protected void onBite(Entity target) {
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (BossUtil.unstoppable(source)) return super.hurtServer(level, source, damage);
		if (hurtTimer > 0) return false;
		if (source.is(DamageTypes.CACTUS) || source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypes.IN_WALL)) return false;
		setOrderedToSit(false);
		setActivity(1);
		Entity e = source.getEntity();
		Entity direct = source.getDirectEntity();
		if (direct instanceof BetterFireball || direct instanceof SmallFireball) {
			direct.discard();
			return false;
		}
		if (e != null && e.getClass() == getClass() || e != null && BossUtil.isType(e, "baby_dragon")) return false;
		boolean ret = super.hurtServer(level, source, damage * scale());
		hurtTimer = 20;
		if (e instanceof LivingEntity l) {
			if (isTame() && e instanceof Player) return false;
			setTarget(l);
			getNavigation().moveTo(l, 1.2);
			ret = true;
		}
		return ret;
	}

	// ---- Targets ----

	private boolean isSuitableTarget(LivingEntity e) {
		if (level().getDifficulty() == Difficulty.PEACEFUL || e == this || !e.isAlive() || !getSensing().hasLineOfSight(e)) return false;
		if (BossUtil.isRoyalty(e)) return false;
		if (e instanceof Monster || e instanceof Kraken || BossUtil.isType(e, "mothra")) return true;
		if (BossUtil.isType(e, "leonopteryx") || BossUtil.isType(e, "water_dragon") || BossUtil.isType(e, "wtf")) {
			return !(e instanceof TamableAnimal t && t.isTame());
		}
		return false;
	}

	protected @Nullable LivingEntity findSomethingToAttack(ServerLevel level) {
		List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(huntRange(), 20.0, huntRange()));
		list.sort(BossUtil.targetOrder(this));
		for (LivingEntity e : list) {
			if (isSuitableTarget(e)) return e;
		}
		return null;
	}

	// ---- Interaction ----

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		double d = player.distanceToSqr(this);
		if (stack.is(Items.DIAMOND_BLOCK) && d < interactRangeSq()) {
			if (!level().isClientSide()) {
				diamondBlock(player);
				heal(getMaxHealth() - getHealth());
			}
			stack.consume(1, player);
			return InteractionResult.SUCCESS;
		}
		if (!isTame() || !isOwnedBy(player)) return InteractionResult.PASS;
		if (stack.isEmpty() && d < interactRangeSq()) {
			if (!level().isClientSide()) {
				player.startRiding(this);
				setActivity(1);
				setOrderedToSit(false);
			}
			return InteractionResult.SUCCESS;
		}
		if (stack.is(Items.BEEF) && d < interactRangeSq()) {
			if (level().isClientSide()) level().broadcastEntityEvent(this, (byte) 7);
			heal(getMaxHealth() - getHealth());
			stack.consume(1, player);
			return InteractionResult.SUCCESS;
		}
		var food = stack.get(net.minecraft.core.component.DataComponents.FOOD);
		if (food != null && d < interactRangeSq()) {
			if (!level().isClientSide()) {
				if (getMaxHealth() > getHealth()) heal(food.nutrition() * 10 * scale());
				level().broadcastEntityEvent(this, (byte) 7);
			}
			stack.consume(1, player);
			return InteractionResult.SUCCESS;
		}
		if (stack.is(Items.ICE) && d < interactRangeSq()) {
			if (!level().isClientSide()) {
				level().broadcastEntityEvent(this, (byte) 6);
				setFire(0);
				player.sendSystemMessage(Component.literal("Fireballs extinguished."));
			}
			stack.consume(1, player);
			return InteractionResult.SUCCESS;
		}
		if (stack.is(Items.FLINT_AND_STEEL) && d < interactRangeSq()) {
			if (!level().isClientSide()) {
				level().broadcastEntityEvent(this, (byte) 6);
				setFire(1);
				player.sendSystemMessage(Component.literal("Fireballs lit!"));
			}
			stack.consume(1, player);
			return InteractionResult.SUCCESS;
		}
		if (stack.is(Items.DIAMOND) && d < interactRangeSq()) {
			if (level() instanceof ServerLevel level) shrink(level, player);
			stack.consume(1, player);
			return InteractionResult.SUCCESS;
		}
		if (stack.is(Items.NAME_TAG) && d < interactRangeSq()) {
			if (!level().isClientSide()) setCustomName(stack.getHoverName());
			stack.consume(1, player);
			return InteractionResult.SUCCESS;
		}
		if (!stack.isEmpty() && d < sitRangeSq()) {
			if (!level().isClientSide()) {
				setOrderedToSit(!isOrderedToSit());
				setActivity(0);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	/** Grows up (or shrinks back) into {@code type}: the new one keeps the owner, where it stands and its name. */
	protected <T extends TamableAnimal> @Nullable T becomeStage(ServerLevel level, EntityType<T> type, @Nullable LivingEntity owner) {
		T next = type.create(level, EntitySpawnReason.CONVERSION);
		if (next == null) return null;
		next.snapTo(getX(), getY(), getZ(), random.nextFloat() * 360.0F, 0.0F);
		if (isTame() && owner instanceof Player p) next.tame(p);
		if (hasCustomName()) next.setCustomName(getCustomName());
		level.addFreshEntity(next);
		next.playAmbientSound();
		ejectPassengers();
		discard();
		return next;
	}

	// ---- Saving ----

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("RoyalAttacking", getAttacking());
		output.putInt("RoyalActivity", getActivity());
		output.putInt("RoyalFire", getFire());
		BossUtil.saveMaxHealth(this, output);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		BossUtil.keepHealthFraction(this, input, oreSpawnHealth());
		setAttacking(input.getIntOr("RoyalAttacking", 0));
		setActivity(input.getIntOr("RoyalActivity", 0));
		setFire(input.getIntOr("RoyalFire", 1));
	}
}
