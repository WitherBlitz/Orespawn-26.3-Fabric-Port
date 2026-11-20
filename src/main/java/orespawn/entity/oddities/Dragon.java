package orespawn.entity.oddities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Oddities;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;
import orespawn.world.OreSpawnDimensions;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Dragon: a fire-proof red dragon (200 health, 14 armour) that walks, and flies off now and then (activity
 * 1), seeking out lava to bathe and heal in. It hunts monsters (and Mothra and the Kraken) within 20 blocks, diving at
 * them for 35 damage and a hard shove, and spits fireballs from up to 16 blocks once they're lit. Feed it raw beef
 * (within 5 blocks) and one time in five it's yours: it follows you (flying along when you fly or get far away),
 * flees when badly hurt, and you can ride it with an empty hand, steering where you look (forward, back, jump to
 * climb). Riding, the strafe keys fire: left small fireballs, right big ones. Flint and steel lights its fireballs,
 * gunpowder supercharges them (big fireballs), ice puts them out; a snowball turns it into a white dragon and coal back
 * to red; a diamond turns it back into a Baby Dragon; any other item makes it sit or stand; a dead bush sets it free.
 * White dragons shoot water balls (ice balls when supercharged) instead. Once tamed it doesn't turn on players who hit
 * it. Drops 1-6 raw beef.
 * <p>
 * Its BetterFireball, Water Ball and Ice Ball shots come from the groups that port them (found by id); until those are
 * in, the fireballs are vanilla ghast and blaze fireballs and a white dragon doesn't shoot. OreSpawn's own fly-up key is
 * the jump key here.
 */
public class Dragon extends FlyingMount {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Dragon.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> ACTIVITY = SynchedEntityData.defineId(Dragon.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(Dragon.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> FIRE = SynchedEntityData.defineId(Dragon.class, EntityDataSerializers.INT);
	private BlockPos flightTarget;
	private boolean targetInSight;
	private boolean ownerFlying;
	private int hurtTimer;
	private int wingSound;
	private int flyaway;
	private int stuckCount;
	private int lastX;
	private int lastZ;
	private int unstickTimer;
	private int fireballTicker;

	public Dragon(EntityType<? extends Dragon> type, Level level) {
		super(type, level);
		this.xpReward = 100;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return TamableAnimal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.MOVEMENT_SPEED, 0.32)
			.add(Attributes.ATTACK_DAMAGE, 35.0).add(Attributes.ARMOR, 14.0).add(Attributes.FOLLOW_RANGE, 20.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new Oddity.HoldGoals(this, () -> activity() != 0));
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
		goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.1, 12.0F, 2.0F));
		goalSelector.addGoal(2, new TemptGoal(this, 1.25, s -> s.is(Items.BEEF), false));
		goalSelector.addGoal(3, WanderGoal.wander(this, 0.75));
		goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 9.0F));
		goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Mob.class, 0, true, false, (e, level) -> e instanceof Enemy));
		targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
		builder.define(ACTIVITY, 0);
		builder.define(TYPE, 0);
		builder.define(FIRE, 1);
	}

	@Override
	public int activity() {
		return entityData.get(ACTIVITY);
	}

	@Override
	protected void setActivity(int a) {
		entityData.set(ACTIVITY, a);
	}

	public boolean attacking() {
		return entityData.get(ATTACKING);
	}

	private void setAttacking(boolean a) {
		entityData.set(ATTACKING, a);
	}

	/** 0 red, otherwise white. */
	public int dragonType() {
		return entityData.get(TYPE);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("DragonAttacking", attacking() ? 1 : 0);
		output.putInt("DragonActivity", activity());
		output.putInt("DragonFire", entityData.get(FIRE));
		output.putInt("DragonType", dragonType());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		setAttacking(input.getIntOr("DragonAttacking", 0) != 0);
		setActivity(input.getIntOr("DragonActivity", 0));
		entityData.set(FIRE, input.getIntOr("DragonFire", 1));
		entityData.set(TYPE, input.getIntOr("DragonType", 0));
	}

	@Override
	protected double riderHeight() {
		return 1.3;
	}

	@Override
	protected double groundCheck() {
		return 1.25;
	}

	@Override
	protected double maxSpeed() {
		return 0.95;
	}

	@Override
	protected double accel() {
		return 0.025;
	}

	@Override
	protected double climb() {
		return 0.03;
	}

	@Override
	protected double climbPerSpeed() {
		return 0.036;
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public void jumpFromGround() {
		super.jumpFromGround();
		setDeltaMovement(getDeltaMovement().add(0.0, 0.25, 0.0));
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		if (isOrderedToSit()) return null;
		return attacking() && !isVehicle() ? ModSounds.get("roar") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("alo_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("alo_death");
	}

	@Override
	protected float getSoundVolume() {
		return 0.6F;
	}

	@Override
	public float getVoicePitch() {
		return 0.75F;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isPersistenceRequired() && !isVehicle() && !isTame();
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		for (int i = 1 + random.nextInt(6); i > 0; i--) Oddity.dropRand(level, this, Items.BEEF, 5, 1.0);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (target instanceof LivingEntity) {
			float kraken = Oddity.is(target, "the_kraken") ? 2.0F : 1.0F;
			target.hurtServer(level, damageSources().mobAttack(this), kraken * 35.0F);
			float a = (float) Math.atan2(target.getZ() - getZ(), target.getX() - getX());
			double inair = target.isRemoved() || !target.isAlive() || target instanceof Player ? 0.2 : 0.1;
			target.push(Math.cos(a) * 1.75, inair, Math.sin(a) * 1.75);
		}
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (hurtTimer > 0) return false;
		if (source.is(DamageTypes.CACTUS) || source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.LAVA)
			|| source.is(DamageTypes.IN_WALL)) {
			return false;
		}
		setOrderedToSit(false);
		setInSittingPose(false);
		setActivity(1);
		Entity e = source.getEntity();
		if (e instanceof Fireball && dragonType() == 0) {
			e.discard();
			return false;
		}
		if (e instanceof Dragon || e instanceof Spyro) return false;
		boolean ret = super.hurtServer(level, source, damage);
		hurtTimer = 20;
		if (e instanceof LivingEntity living) {
			if (isTame() && e instanceof Player) return false;
			setTarget(living);
			getNavigation().moveTo(living, 1.2);
			ret = true;
		}
		return ret;
	}

	@Override
	public void tick() {
		super.tick();
		if (hurtTimer > 0) hurtTimer--;
		if (activity() == 1 && ++wingSound > 20) {
			if (!level().isClientSide()) Oddity.play(this, Oddity.optionalSound("mothrawings"), 0.5F, 1.0F);
			wingSound = 0;
		}
		if (isInWater()) setDeltaMovement(getDeltaMovement().add(0.0, 0.07, 0.0));
		if (!level().isClientSide() && activity() == 0 && isTame() && !isOrderedToSit()) {
			LivingEntity owner = getOwner();
			if (owner != null && distanceToSqr(owner) > 144.0) setActivity(1);
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (!isOrderedToSit() && activity() == 0 && !isVehicle() && level.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(10) == 1) {
			if (Oddity.nearest(this, 20.0, 20.0, this::isSuitableTarget) != null) setActivity(1);
		}
	}

	@Override
	public void aiStep() {
		if (level() instanceof ServerLevel level && isAlive()) {
			if (isVehicle() && activity() == 0) setActivity(1);
			if (activity() != 0) {
				if (fireballTicker > 0) fireballTicker--;
				if (getControllingPassenger() instanceof Player rider) {
					riderFire(level, rider);
					flyWithRider(level);
				} else {
					flyWithoutRider(level);
				}
			}
		}
		super.aiStep();
		if (level() instanceof ServerLevel level && isAlive()) alwaysDo(level);
	}

	/** always_do: healing, keeping up with a flying or far-off owner, taking off and landing, and lava baths. */
	private void alwaysDo(ServerLevel level) {
		if (random.nextInt(250) == 1 && getHealth() < getMaxHealth()) heal(2.0F);
		if (isOrderedToSit()) return;
		ownerFlying = false;
		LivingEntity owner = isTame() ? getOwner() : null;
		if (owner instanceof Player p && !isVehicle() && p.getAbilities().flying) {
			ownerFlying = true;
			setActivity(1);
		}
		if (owner != null && distanceToSqr(owner) > 400.0) setActivity(1);
		if (random.nextInt(50) == 1 && !targetInSight && !isVehicle()) setActivity(random.nextInt(15) == 1 ? 1 : 0);
		if (random.nextInt(25) == 0 && !targetInSight && !isVehicle()) {
			int[] lava = Oddity.search(level, getBlockX(), getBlockY() - 1, getBlockZ(), 11, 4, 6, s -> s.getFluidState().is(FluidTags.LAVA));
			if (lava[3] < 99999) {
				setActivity(0);
				getNavigation().moveTo(lava[0], lava[1] - 1, lava[2], 1.0);
				if (isInLava()) {
					heal(1.0F);
					playSound(SoundEvents.GENERIC_SPLASH, 1.0F, random.nextFloat() * 0.2F + 0.9F);
				}
			}
		}
	}

	/** fly_with_rider: while ridden it still bites whatever it's after within reach. */
	private void flyWithRider(ServerLevel level) {
		if (isOrderedToSit() || random.nextInt(7) != 1 || level.getDifficulty() == Difficulty.PEACEFUL) return;
		if (random.nextInt(250) == 0) setTarget(null);
		LivingEntity e = getTarget();
		if (e != null && !e.isAlive()) {
			setTarget(null);
			e = null;
		}
		if (e == null) e = Oddity.nearest(this, 20.0, 20.0, this::isSuitableTarget);
		if (e != null) {
			setAttacking(true);
			double reach = 7.0F + e.getBbWidth() / 2.0F;
			if (distanceToSqr(e) < reach * reach) doHurtTarget(level, e);
			return;
		}
		setAttacking(false);
	}

	/** fly_without_rider's shot: fireballs from a red dragon, water balls (or, supercharged, ice balls) from a white one. */
	private void shootAt(LivingEntity e) {
		double cx = getX() - 2.25 * Math.sin(Math.toRadians(getYRot()));
		double cz = getZ() + 2.25 * Math.cos(Math.toRadians(getYRot()));
		double y = getY() + 1.25, ty = e.getY() + e.getBbHeight() / 2.0F;
		boolean supercharged = entityData.get(FIRE) != 1;
		if (dragonType() == 0) {
			if (!supercharged) {
				Oddity.smallFireball(this, cx, y, cz, e.getX(), ty, e.getZ());
				Oddity.bowSound(this);
			} else {
				Oddity.bigFireball(this, cx, y, cz, e.getX(), ty, e.getZ(), false);
			}
			return;
		}
		Projectile ball = Oddity.orespawnProjectile(supercharged ? "ice_ball" : "water_ball", this, cx, y, cz);
		if (ball == null) return;
		if (supercharged) Oddity.call(ball, "setSpecial");
		double dx = e.getX() - ball.getX(), dy = e.getY() + 0.25 - ball.getY(), dz = e.getZ() - ball.getZ();
		Oddity.throwAt(ball, dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.2F, dz, 1.4F, 5.0F);
		if (supercharged) playSound(SoundEvents.TNT_PRIMED, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
		else Oddity.bowSound(this);
	}

	/**
	 * The rider's strafe keys, where the rider looks: on a red dragon left a small fireball and right a big one; on a
	 * white one left a water ball and right an ice ball.
	 */
	private void riderFire(ServerLevel level, Player rider) {
		if (fireballTicker != 0 || !(rider instanceof ServerPlayer sp)) return;
		boolean left = sp.getLastClientInput().left(), right = sp.getLastClientInput().right();
		if (left == right) return;
		Vec3 motion = new Vec3(getX() - xo, getY() - yo, getZ() - zo);
		double x = getX() - 4.0 * Math.sin(Math.toRadians(getYRot()));
		double y = getY() - 0.25;
		double z = getZ() + 4.0 * Math.cos(Math.toRadians(getYRot()));
		if (dragonType() == 0) {
			double cx = Math.cos(Math.toRadians(rider.getYHeadRot() + 90.0F));
			double cz = Math.sin(Math.toRadians(rider.getYHeadRot() + 90.0F));
			double cy = -Math.sin(Math.toRadians(rider.getXRot()));
			Vec3 dir = new Vec3(cx, cy, cz);
			Projectile f = Oddity.betterFireball(this, x - motion.x * 9.0, y - motion.y * 9.0, z - motion.z * 9.0, dir, true, left, left ? 0.15 : 0.1);
			f.setDeltaMovement(f.getDeltaMovement().add(motion));
			level.addFreshEntity(f);
			if (left) Oddity.bowSound(this);
			else playSound(SoundEvents.TNT_PRIMED, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
			fireballTicker = left ? 10 : 20;
			return;
		}
		Projectile ball = Oddity.orespawnProjectile(left ? "water_ball" : "ice_ball", this, x - motion.x * 7.0, y - motion.y * 7.0, z - motion.z * 7.0);
		if (ball == null) return;
		float yaw = left ? rider.getYHeadRot() : rider.getYRot();
		double cx = Math.cos(Math.toRadians(yaw + 90.0F)), cz = Math.sin(Math.toRadians(yaw + 90.0F));
		double cy = -Math.sin(Math.toRadians(rider.getXRot()));
		if (left) {
			Oddity.throwAt(ball, cx, cy + 0.2F, cz, 1.4F, 5.0F);
			Oddity.bowSound(this);
			fireballTicker = 5;
		} else {
			Oddity.call(ball, "setSpecial");
			ball.shoot(cx, cy + 0.2F, cz, 1.4F, 5.0F);
			ball.setDeltaMovement(ball.getDeltaMovement().scale(2.0));
			level.addFreshEntity(ball);
			playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 0.75F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
			fireballTicker = 15;
		}
	}

	/** fly_without_rider: wandering flight, hunting, following its owner, and getting unstuck. */
	private void flyWithoutRider(ServerLevel level) {
		if (isOrderedToSit()) return;
		boolean doNew = false;
		if (flightTarget == null) {
			doNew = true;
			flightTarget = blockPosition();
		}
		if (unstickTimer > 0) unstickTimer--;
		if (lastX == getBlockX() && lastZ == getBlockZ()) {
			if (++stuckCount > 50) {
				stuckCount = 0;
				unstickTimer = 100;
				targetInSight = false;
				setAttacking(false);
				doNew = true;
			}
		} else {
			stuckCount = 0;
			lastX = getBlockX();
			lastZ = getBlockZ();
		}
		double f = getY() < flightTarget.getY() + 2.0 ? 0.7 : getY() > flightTarget.getY() - 2.0 ? 0.5 : 0.61;
		setDeltaMovement(getDeltaMovement().multiply(1.0, f, 1.0));
		if (random.nextInt(300) == 1) doNew = true;
		LivingEntity owner = isTame() ? getOwner() : null;
		boolean tooFar = false;
		if (owner != null && distanceToSqr(owner) > 144.0) {
			tooFar = true;
			targetInSight = false;
			setAttacking(false);
			flyaway = 0;
			doNew = true;
		}
		if (flyaway > 0) flyaway--;
		if (!tooFar && unstickTimer == 0 && flyaway == 0 && level.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(9) == 1) {
			LivingEntity e = Oddity.nearest(this, 20.0, 20.0, this::isSuitableTarget);
			if (e != null) {
				setActivity(1);
				doNew = false;
				if (isTame() && getHealth() / getMaxHealth() < 0.25F) {
					setAttacking(false);
					targetInSight = false;
					flightTarget = BlockPos.containing(getX() + (getX() - e.getX()), getY() + 1.0, getZ() + (getZ() - e.getZ()));
				} else {
					setAttacking(true);
					targetInSight = true;
					flightTarget = BlockPos.containing(e.getX(), e.getY() + 1.0, e.getZ());
					double reach = 5.0F + e.getBbWidth() / 2.0F;
					if (distanceToSqr(e) < reach * reach) {
						doHurtTarget(level, e);
						flyaway = 5 + random.nextInt(10);
						doNew = true;
					} else if (distanceToSqr(e) < 256.0 && !isInWater() && entityData.get(FIRE) >= 1) {
						shootAt(e);
					}
				}
			} else {
				targetInSight = false;
				flyaway = 0;
				setAttacking(false);
			}
		}
		if (blockPosition().distSqr(flightTarget) < 2.1) doNew = true;
		if (doNew && (!targetInSight || flyaway != 0)) {
			for (int tries = 50; tries > 0; tries--) {
				int gx = getBlockX(), gy = getBlockY(), gz = getBlockZ(), dx, dz;
				if (owner != null && unstickTimer == 0) {
					gx = owner.getBlockX();
					gy = owner.getBlockY();
					gz = owner.getBlockZ();
					dz = ownerFlying ? random.nextInt(6) : random.nextInt(10) + 4;
					dx = ownerFlying ? random.nextInt(6) : random.nextInt(10) + 4;
				} else {
					dz = random.nextInt(10) + 16;
					dx = random.nextInt(10) + 16;
				}
				if (random.nextInt(2) == 1) dz = -dz;
				if (random.nextInt(2) == 1) dx = -dx;
				flightTarget = new BlockPos(gx + dx, gy + random.nextInt(9 + (ownerFlying ? 2 : 0)) - 4, gz + dz);
				if (level.isEmptyBlock(flightTarget) && Oddity.canSee(this, flightTarget)) break;
			}
		}
		Vec3 m = getDeltaMovement();
		double obstruction = obstruction(Math.sqrt(m.x * m.x + m.z * m.z), 2, 4.0);
		setDeltaMovement(m.add(0.0, obstruction * 0.05, 0.0));
		setPos(getX(), getY() + obstruction * 0.05, getZ());
		double sf = 0.5;
		if (ownerFlying) sf = owner != null && distanceToSqr(owner) > 49.0 ? 3.5 : 1.75;
		Oddity.steer(this, flightTarget.getX() + 0.5, flightTarget.getY() + 0.1, flightTarget.getZ() + 0.5, 1.0, 1.0, 0.15 * sf, 0.21 * sf, 4.0F);
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (level().getDifficulty() == Difficulty.PEACEFUL || e == this || !e.isAlive() || !getSensing().hasLineOfSight(e)) return false;
		if (Oddity.is(e, "lurking_terror", "ender_reaper", "terrible_terror", "leaf_monster", "creeping_horror", "triffid")) return false;
		return e instanceof Monster || e instanceof Mothra || Oddity.is(e, "the_kraken");
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		boolean server = !level().isClientSide();
		boolean near = player.distanceToSqr(this) < 25.0;
		if (!isTame()) {
			if (stack.is(Items.BEEF) && near) {
				if (server) {
					if (random.nextInt(5) == 1) {
						tame(player);
						level().broadcastEntityEvent(this, (byte) 7);
						heal(getMaxHealth() - getHealth());
					} else {
						level().broadcastEntityEvent(this, (byte) 6);
					}
					stack.consume(1, player);
				}
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}
		if (!isOwnedBy(player)) return InteractionResult.PASS;
		if (stack.isEmpty() && player.distanceToSqr(this) < 16.0) {
			if (server) {
				player.startRiding(this);
				setActivity(1);
				setOrderedToSit(false);
				setInSittingPose(false);
			}
			return InteractionResult.SUCCESS;
		}
		if (!near) return InteractionResult.PASS;
		if (stack.is(Items.BEEF)) {
			if (server) {
				heal(getMaxHealth() - getHealth());
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		if (stack.is(Items.DEAD_BUSH)) {
			if (server) {
				setTame(false, true);
				setOwner(null);
				level().broadcastEntityEvent(this, (byte) 6);
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		if (stack.is(Items.ICE) || stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.GUNPOWDER) && entityData.get(FIRE) > 0) {
			if (server) {
				level().broadcastEntityEvent(this, (byte) 6);
				int fire = stack.is(Items.ICE) ? 0 : stack.is(Items.FLINT_AND_STEEL) ? 1 : 2;
				entityData.set(FIRE, fire);
				player.sendSystemMessage(Component.literal(fire == 0 ? "Dragon fireballs extinguished." : fire == 1 ? "Dragon fireballs lit!"
					: "Dragon fireballs supercharged!"));
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		if (stack.is(Items.SNOWBALL) || stack.is(Items.COAL)) {
			if (server) {
				entityData.set(TYPE, stack.is(Items.SNOWBALL) ? 1 : 0);
				level().broadcastEntityEvent(this, (byte) 7);
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		if (stack.is(Items.DIAMOND)) {
			if (server) {
				Spyro baby = Oddity.spawn((ServerLevel) level(), Oddities.BABY_DRAGON, getX(), getY(), getZ());
				if (baby != null) {
					baby.tame(player);
					discard();
				}
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		if (stack.is(Items.NAME_TAG)) return InteractionResult.PASS;
		if (server) {
			boolean sit = !isOrderedToSit();
			setOrderedToSit(sit);
			setInSittingPose(sit);
			setActivity(0);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return false;
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}

	/**
	 * Dragon.getCanSpawnHere: by day, no other Dragon within 16 blocks, and at y 50 or higher except in OreSpawn's
	 * Islands dimension.
	 */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!level.getLevel().isBrightOutside()) return false;
		if (!level.getEntitiesOfClass(Dragon.class, new AABB(pos).inflate(16.0, 6.0, 16.0)).isEmpty()) return false;
		return level.getLevel().dimension() == OreSpawnDimensions.ISLANDS || pos.getY() >= 50;
	}
}
