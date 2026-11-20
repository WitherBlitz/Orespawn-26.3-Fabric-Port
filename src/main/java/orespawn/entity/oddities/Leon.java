package orespawn.entity.oddities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.entity.Ignored;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Leonopteryx: a towering winged predator (250 health, 16 armour, 8 blocks tall) that walks and takes to
 * the sky, hunting within 20 blocks: monsters, and while wild, players in survival and most other big creatures too.
 * Its dives strike for 55 (four times that on the Kraken, and it bites the Ender Dragon too) with a shove. A diamond
 * block (within 7 blocks) tames it at once; raw beef does one time in three. Tamed, it follows you (flying along when
 * you fly or get far away), flees when badly hurt, and you can ride it with an empty hand, steering where you look
 * (forward, back, jump to climb) while it bites whatever comes near; raw beef heals it, any other item makes it sit or
 * stand, a dead bush sets it free. Drops 4-9 raw chicken, 16-21 feathers, 2-7 Kraken Repellent and, one time in five,
 * a Battle Axe.
 * <p>
 * Not ported yet: OreSpawn's own fly-up key, replaced by jump. Its spawns are in OreSpawn's dimensions.
 */
public class Leon extends FlyingMount {
	private static final EntityDataAccessor<Integer> ATTACKING = SynchedEntityData.defineId(Leon.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> ACTIVITY = SynchedEntityData.defineId(Leon.class, EntityDataSerializers.INT);
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

	public Leon(EntityType<? extends Leon> type, Level level) {
		super(type, level);
		this.xpReward = 300;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return TamableAnimal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 250.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 55.0).add(Attributes.ARMOR, 16.0).add(Attributes.FOLLOW_RANGE, 20.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new Oddity.HoldGoals(this, () -> activity() != 0));
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
		goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.1, 16.0F, 2.0F));
		goalSelector.addGoal(2, new TemptGoal(this, 1.25, s -> s.is(Items.BEEF), false));
		goalSelector.addGoal(3, WanderGoal.wander(this, 0.75));
		goalSelector.addGoal(4, new LookAtPlayerGoal(this, LivingEntity.class, 9.0F));
		goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Mob.class, 0, true, false, (e, level) -> e instanceof Enemy));
		targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, 0);
		builder.define(ACTIVITY, 0);
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
		return entityData.get(ATTACKING) != 0;
	}

	private void setAttacking(boolean a) {
		entityData.set(ATTACKING, a ? 1 : 0);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("LeonAttacking", entityData.get(ATTACKING));
		output.putInt("LeonActivity", activity());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		entityData.set(ATTACKING, input.getIntOr("LeonAttacking", 0));
		setActivity(input.getIntOr("LeonActivity", 0));
	}

	@Override
	protected double riderHeight() {
		return 3.75;
	}

	@Override
	protected double groundCheck() {
		return 1.55;
	}

	@Override
	protected double maxSpeed() {
		return 1.15;
	}

	/** 0.028, plus OreSpawn's 0.06 for a top speed over 1. */
	@Override
	protected double accel() {
		return 0.088;
	}

	@Override
	protected double climb() {
		return 0.035;
	}

	@Override
	protected double climbPerSpeed() {
		return 0.038;
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
		return activity() == 1 && !isVehicle() ? ModSounds.get("leon_living") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("leon_hit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("leon_death");
	}

	@Override
	protected float getSoundVolume() {
		return 1.75F;
	}

	@Override
	public float getVoicePitch() {
		return 0.85F;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isPersistenceRequired() && !isVehicle() && !isTame();
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		for (int i = 4 + random.nextInt(6); i > 0; i--) Oddity.dropRand(level, this, Items.CHICKEN, 4, 2.0);
		for (int i = 16 + random.nextInt(6); i > 0; i--) Oddity.dropRand(level, this, Items.FEATHER, 4, 2.0);
		for (int i = 2 + random.nextInt(6); i > 0; i--) Oddity.dropRand(level, this, ModItems.lazy("kraken_repellent"), 4, 2.0);
		if (random.nextInt(5) == 1) Oddity.dropRand(level, this, ModItems.lazy("battle_axe"), 4, 2.0);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (target instanceof EnderDragon dragon) {
			EnderDragonPart part = dragon.head;
			if (random.nextInt(6) != 1) {
				for (EnderDragonPart p : dragon.getSubEntities()) {
					if (p.name.equals("body")) part = p;
				}
			}
			dragon.hurt(level, part, damageSources().explosion(null, null), 55.0F);
		} else if (target instanceof LivingEntity) {
			float kraken = Oddity.is(target, "the_kraken") ? 4.0F : 1.0F;
			target.hurtServer(level, damageSources().mobAttack(this), kraken * 55.0F);
			float a = (float) Math.atan2(target.getZ() - getZ(), target.getX() - getX());
			double inair = !target.isAlive() || target instanceof Player ? 0.3 : 0.15;
			target.push(Math.cos(a) * 1.25, inair, Math.sin(a) * 1.25);
		}
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (hurtTimer > 0 || source.is(DamageTypes.IN_WALL)) return false;
		setOrderedToSit(false);
		setInSittingPose(false);
		setActivity(1);
		Entity e = source.getEntity();
		if (e instanceof Leon) return false;
		boolean ret = super.hurtServer(level, source, damage);
		hurtTimer = 15;
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
		if (random.nextInt(200) == 1) setTarget(null);
	}

	@Override
	public void aiStep() {
		if (level() instanceof ServerLevel level && isAlive()) {
			alwaysDo(level);
			if (isVehicle() && activity() == 0) setActivity(1);
			if (activity() != 0) {
				if (getControllingPassenger() instanceof Player) flyWithRider(level);
				else flyWithoutRider(level);
			}
		}
		super.aiStep();
	}

	/** always_do: noticing prey, healing, keeping up with a flying or far-off owner, taking off and landing. */
	private void alwaysDo(ServerLevel level) {
		if (!isOrderedToSit() && activity() == 0 && !isVehicle() && level.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(10) == 1) {
			if (Oddity.nearest(this, 20.0, 20.0, this::isSuitableTarget) != null) setActivity(1);
		}
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
	}

	/** fly_with_rider: while ridden it still bites whatever it's after within reach. */
	private void flyWithRider(ServerLevel level) {
		if (isOrderedToSit() || random.nextInt(7) != 1 || level.getDifficulty() == Difficulty.PEACEFUL) return;
		LivingEntity e = getTarget();
		if (e != null && !e.isAlive()) {
			setTarget(null);
			e = null;
		}
		if (e == null) e = Oddity.nearest(this, 20.0, 20.0, this::isSuitableTarget);
		if (e != null) {
			setAttacking(true);
			double reach = 9.0F + e.getBbWidth() / 2.0F;
			if (distanceToSqr(e) < reach * reach) doHurtTarget(level, e);
			return;
		}
		setAttacking(false);
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
				setActivity(1);
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
		if (!tooFar && unstickTimer == 0 && flyaway == 0 && level.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(8) == 1) {
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
					double reach = 7.0F + e.getBbWidth() / 2.0F;
					if (distanceToSqr(e) < reach * reach) doHurtTarget(level, e);
				}
			} else {
				targetInSight = false;
				flyaway = 0;
				setAttacking(false);
			}
		}
		if (blockPosition().distSqr(flightTarget) < 4.1) doNew = true;
		if (doNew && (!targetInSight || flyaway != 0)) {
			for (int tries = 50; tries > 0; tries--) {
				int gx = getBlockX(), gy = getBlockY(), gz = getBlockZ(), dx, dz;
				if (owner != null && unstickTimer == 0) {
					gx = owner.getBlockX();
					gy = owner.getBlockY();
					gz = owner.getBlockZ();
					dz = ownerFlying ? random.nextInt(8) : random.nextInt(12) + 6;
					dx = ownerFlying ? random.nextInt(8) : random.nextInt(12) + 6;
				} else {
					dz = random.nextInt(20) + 6;
					dx = random.nextInt(20) + 6;
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
		Oddity.steer(this, flightTarget.getX() + 0.5, flightTarget.getY() + 0.1, flightTarget.getZ() + 0.5, 1.0, 1.0, 0.15 * sf, 0.21 * sf, 5.0F);
	}

	/**
	 * Leon.isSuitableTarget: monsters; while wild also players in survival and MyUtils.isAttackableNonMob's list
	 * (Mothra, dragons, villagers, Stinkies and other big creatures).
	 */
	private boolean isSuitableTarget(LivingEntity e) {
		if (level().getDifficulty() == Difficulty.PEACEFUL || e == this || !e.isAlive() || Ignored.byHunters(e)) return false;
		if (!getSensing().hasLineOfSight(e) || e instanceof Leon) return false;
		if (e instanceof Monster) return true;
		if (e instanceof Player p) return !Oddity.creative(p) && !isTame();
		if (isTame()) return false;
		return e instanceof Mothra || e instanceof Dragon || e instanceof Spyro || e instanceof Stinky || e instanceof Villager
			|| Oddity.is(e, "the_prince", "the_young_prince", "the_young_adult_prince", "the_princess", "the_king", "the_queen", "wtf",
				"cephadrome", "water_dragon");
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		boolean server = !level().isClientSide();
		boolean near = player.distanceToSqr(this) < 49.0;
		if (stack.is(Items.DIAMOND_BLOCK) && near) {
			if (server) {
				tame(player);
				level().broadcastEntityEvent(this, (byte) 7);
				heal(getMaxHealth() - getHealth());
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		if (!isTame()) {
			if (stack.is(Items.BEEF) && near) {
				if (server) {
					if (random.nextInt(3) == 1) {
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
		if (!isOwnedBy(player) || !near) return InteractionResult.PASS;
		if (stack.isEmpty()) {
			if (server) {
				player.startRiding(this);
				setActivity(1);
				setOrderedToSit(false);
				setInSittingPose(false);
			}
			return InteractionResult.SUCCESS;
		}
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
		if (stack.is(Items.NAME_TAG)) return InteractionResult.PASS;
		if (!isVehicle()) {
			if (server) {
				boolean sit = !isOrderedToSit();
				setOrderedToSit(sit);
				setInSittingPose(sit);
				setActivity(0);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return false;
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}

	/** Leon.getCanSpawnHere: one time in 16, by day, at y 50 or higher, no other Leonopteryx within 48 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (random.nextInt(16) != 0 || !level.getLevel().isBrightOutside()) return false;
		return pos.getY() >= 50 && level.getEntitiesOfClass(Leon.class, new AABB(pos).inflate(48.0, 16.0, 48.0)).isEmpty();
	}
}
