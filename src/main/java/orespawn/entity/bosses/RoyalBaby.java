package orespawn.entity.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.entity.Dragonfly;
import orespawn.entity.Mosquito;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * What The Prince and The Princess share (OreSpawn's ThePrince and ThePrincess were the same class with a few
 * changes): small three-headed dragon babies that walk (activity 1) or fly (activity 2, passing through blocks).
 * <p>
 * A wild one simply adopts the nearest player within 10 blocks. They follow their owner, come for raw beef, wander and
 * look about; now and then (1 in 20 of their mood changes), or when their owner flies, gets 16 blocks away or something
 * hits them, they take off and flutter round their owner (or round themselves). Every few ticks they look for prey
 * within 12 blocks they can see (monsters and Mothras, and for The Prince bugs and birds too; never royalty): they fly at
 * it and bite it up close, or, with their fire lit and 5-12 blocks off, shoot a fireball, a thunder bolt or an ice ball
 * when facing it. Tamed and below a quarter of their health, they flee instead. A diamond block tames and heals them;
 * any food heals 10 x its hunger; flint and steel lights their fire, ice puts it out; a name tag names them; anything
 * else (or an empty hand) makes them sit or stand. They breathe underwater, ignore suffocation, cactus and falls, and
 * never despawn; they drop 1-4 raw beef.
 * <p>
 * Not ported yet: EntityAIMoveIndoors (26.3 has no such goal); OreSpawn's pet health overlay; breeding (OreSpawn gave
 * them none). The thunder bolts and ice balls are the weapons group's ({@code orespawn:thunder_bolt},
 * {@code orespawn:ice_ball}), looked up by id.
 */
public abstract class RoyalBaby extends TamableAnimal {
	private static final EntityDataAccessor<Integer> FIRE = SynchedEntityData.defineId(RoyalBaby.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> ACTIVITY = SynchedEntityData.defineId(RoyalBaby.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(RoyalBaby.class, EntityDataSerializers.BOOLEAN);

	private @Nullable BlockPos flightTarget;
	private int ownerFlying;
	private final int[] headExt = new int[3];
	private final int[] headDir = {1, 1, 1};
	protected int killCount;
	protected int fedCount;
	protected int dayCount;
	private int isDay;

	protected RoyalBaby(EntityType<? extends RoyalBaby> type, Level level) {
		super(type, level);
		this.xpReward = 50;
		// While flying it steers itself (OreSpawn skipped its vanilla AI then); on foot the usual move control.
		this.moveControl = new MoveControl(this) {
			@Override
			public void tick() {
				if (getActivity() != 2) super.tick();
			}
		};
	}

	/** Bite damage (OreSpawn's getAttackStrength). */
	protected abstract float biteDamage();

	/** "Prince" or "Princess", for the fire messages. */
	protected abstract String title();

	/** Whether it hunts butterflies and birds too (The Prince does). */
	protected abstract boolean huntsCritters();

	/** Its own extra AI each tick (The Prince grows, The Princess gathers power). */
	protected abstract void ownAi(ServerLevel level);

	/** A diamond: The Prince grows up with one once it may; returns whether it was used. */
	protected boolean diamond(ServerLevel level, Player player) {
		return false;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(FIRE, 1);
		builder.define(ACTIVITY, 1);
		builder.define(ATTACKING, false);
	}

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

	public int getAttacking() {
		return entityData.get(ATTACKING) ? 1 : 0;
	}

	public void setAttacking(int a) {
		if (!level().isClientSide()) entityData.set(ATTACKING, a != 0);
	}

	/** How far each neck is stretched (0-60), for the model (OreSpawn kept these on each side, unsynced). */
	public int headExt(int i) {
		return headExt[i];
	}

	// ---- Goals: only while walking ----

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(1, new FloatGoal(this));
		goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.15, 12.0F, 2.0F) {
			@Override
			public boolean canUse() {
				return getActivity() != 2 && super.canUse();
			}

			@Override
			public boolean canContinueToUse() {
				return getActivity() != 2 && super.canContinueToUse();
			}
		});
		goalSelector.addGoal(3, new TemptGoal(this, 1.25, s -> s.is(Items.BEEF), false) {
			@Override
			public boolean canUse() {
				return getActivity() != 2 && super.canUse();
			}
		});
		goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 6.0F));
		goalSelector.addGoal(5, new WanderGoal(this, 0.75, 90, 10) {
			@Override
			public boolean canUse() {
				return getActivity() != 2 && super.canUse();
			}
		});
		goalSelector.addGoal(6, new RandomLookAroundGoal(this));
	}

	// ---- Sounds ----

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		if (isOrderedToSit()) return null;
		return getAttacking() == 0 ? null : ModSounds.get("roar");
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("duck_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("cryo_death");
	}

	@Override
	protected float getSoundVolume() {
		return 0.6F;
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
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
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

	// ---- Ticking ----

	@Override
	public void tick() {
		super.tick();
		noPhysics = getActivity() == 2;
		for (int i = 0; i < 3; i++) {
			if (random.nextInt(10) == 1) headDir[i] = new int[] {2, -2, 0}[random.nextInt(3)];
			headExt[i] = Mth.clamp(headExt[i] + headDir[i], 0, 60);
		}
		Vec3 m = getDeltaMovement();
		if (isInWater()) m = m.add(0.0, 0.07, 0.0);
		if (getActivity() == 2) m = new Vec3(m.x, m.y * 0.6, m.z);
		setDeltaMovement(m);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (isRemoved() || isDeadOrDying()) return;
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		if (random.nextInt(200) == 1 && getHealth() < getMaxHealth()) heal(1.0F);
		if (!isTame()) {
			Player p = level.getNearestPlayer(this, 10.0);
			if (p != null && !p.isSpectator()) {
				tame(p);
				level.broadcastEntityEvent(this, (byte) 7);
				heal(getMaxHealth() - getHealth());
			}
		}
		LivingEntity owner = isTame() ? getOwner() : null;
		if (!isOrderedToSit()) {
			if (getActivity() == 0) setActivity(1);
			if (random.nextInt(100) == 1) setActivity(random.nextInt(20) == 1 ? 2 : 1);
			ownerFlying = 0;
			if (owner instanceof Player p && p.getAbilities().flying) {
				ownerFlying = 1;
				setActivity(2);
			}
			if (getActivity() == 1 && owner != null && distanceToSqr(owner) > 256.0) setActivity(2);
			doMovement(level, owner);
		} else if (owner != null && distanceToSqr(owner) > 256.0) {
			setOrderedToSit(false);
			setActivity(2);
		}
		ownAi(level);
		boolean day = level.isBrightOutside();
		if (isDay == -1 && day) dayCount++;
		isDay = day ? 1 : -1;
	}

	/** do_movement: hunting, then (when flying) steering round the owner or itself. */
	private void doMovement(ServerLevel level, @Nullable LivingEntity owner) {
		boolean doNew = false;
		if (flightTarget == null) {
			doNew = true;
			flightTarget = blockPosition();
		}
		if (getActivity() == 2 && random.nextInt(300) == 0) doNew = true;
		if (owner != null) {
			if (distanceToSqr(owner) > 100.0) doNew = true;
			if (ownerFlying != 0 && distanceToSqr(owner) > 36.0) doNew = true;
		}
		if (random.nextInt(7) == 1 && level.getDifficulty() != Difficulty.PEACEFUL) {
			LivingEntity e = findSomethingToAttack(level);
			if (e != null) {
				setActivity(2);
				if (isTame() && getHealth() / getMaxHealth() < 0.25F) {
					setAttacking(0);
					doNew = false;
					flightTarget = BlockPos.containing(getX() + (getX() - e.getX()), getY() + 1.0, getZ() + (getZ() - e.getZ()));
				} else {
					setAttacking(1);
					flightTarget = BlockPos.containing(e.getX(), e.getY() + 1.0, e.getZ());
					doNew = false;
					float reach = 3.0F + e.getBbWidth() / 2.0F;
					double d = distanceToSqr(e);
					if (d < reach * reach) {
						doHurtTarget(level, e);
					} else if (d > 25.0 && d < 144.0 && !isInWater() && getFire() != 0 && (random.nextInt(3) == 0 || random.nextInt(4) == 1)) {
						shoot(level, e, random.nextInt(3));
					}
				}
			} else {
				setAttacking(0);
			}
		}
		if (getActivity() == 1) return;
		if (flightTarget.distSqr(blockPosition()) < 2.1F) doNew = true;
		if (doNew) {
			for (int tries = 10; tries > 0; tries--) {
				int gox = Mth.floor(getX());
				int goy = Mth.floor(getY());
				int goz = Mth.floor(getZ());
				int xdir;
				int zdir;
				if (owner != null) {
					gox = Mth.floor(owner.getX());
					goy = Mth.floor(owner.getY() + 1.0);
					goz = Mth.floor(owner.getZ());
					if (ownerFlying == 0) {
						zdir = random.nextInt(4) + 6;
						xdir = random.nextInt(4) + 6;
					} else {
						zdir = random.nextInt(8);
						xdir = random.nextInt(8);
					}
				} else {
					zdir = random.nextInt(5) + 6;
					xdir = random.nextInt(5) + 6;
				}
				if (random.nextInt(2) == 0) zdir = -zdir;
				if (random.nextInt(2) == 0) xdir = -xdir;
				flightTarget = new BlockPos(gox + xdir, goy + (random.nextInt(6 + ownerFlying * 2) - 2), goz + zdir);
				if (level.isEmptyBlock(flightTarget)) break;
			}
		}
		double speed = 1.0;
		if (ownerFlying != 0) {
			speed = 1.75;
			if (owner != null && distanceToSqr(owner) > 49.0) speed = 3.5;
		}
		double var1 = flightTarget.getX() + 0.5 - getX();
		double var3 = flightTarget.getY() + 0.1 - getY();
		double var5 = flightTarget.getZ() + 0.5 - getZ();
		Vec3 m = getDeltaMovement();
		double mx = m.x + (Math.signum(var1) * 0.5 - m.x) * 0.15 * speed;
		double my = m.y + (Math.signum(var3) * 0.7 - m.y) * 0.21 * speed;
		double mz = m.z + (Math.signum(var5) * 0.5 - m.z) * 0.15 * speed;
		setDeltaMovement(mx, my, mz);
		float var7 = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0F;
		zza = (float) (0.75 * speed);
		setYRot(getYRot() + Mth.wrapDegrees(var7 - getYRot()) / 3.0F);
		yBodyRot = getYRot();
	}

	/** firecanon / firecanonl / firecanoni: from 3 blocks ahead, 1 up, if facing the target, with a random aim error. */
	private void shoot(ServerLevel level, LivingEntity e, int which) {
		double rr = Math.atan2(e.getZ() - getZ(), e.getX() - getX());
		double rhdir = Math.toRadians((getYRot() + 90.0F) % 360.0F);
		double rdd = Math.abs(rr - rhdir) % (Math.PI * 2.0);
		if (rdd > Math.PI) rdd -= Math.PI * 2.0;
		if (Math.abs(rdd) >= 0.5) return;
		double yaw = Math.toRadians(getYRot());
		double cx = getX() - 3.0 * Math.sin(yaw);
		double cz = getZ() + 3.0 * Math.cos(yaw);
		double cy = getY() + 1.0;
		playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F));
		float r1 = 5.0F * (random.nextFloat() - random.nextFloat());
		float r2 = 3.0F * (random.nextFloat() - random.nextFloat());
		float r3 = 5.0F * (random.nextFloat() - random.nextFloat());
		if (which == 0) {
			BetterFireball bf = new BetterFireball(level, this, e.getX() - cx + r1, e.getY() + e.getBbHeight() / 2.0F - cy + r2, e.getZ() - cz + r3);
			bf.snapTo(cx, cy, cz, getYRot(), 0.0F);
			bf.setBig();
			if (random.nextInt(2) == 1) bf.setSmall();
			level.addFreshEntity(bf);
		} else {
			BossUtil.lob(level, BossUtil.weapon(level, which == 1 ? "thunder_bolt" : "ice_ball", this, cx, cy, cz, which == 2), e.getX(), e.getY(),
				e.getZ());
		}
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean hit = target.hurtServer(level, damageSources().mobAttack(this), biteDamage());
		if (target instanceof LivingEntity e && e.getHealth() <= 0.0F) killCount++;
		return hit;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (BossUtil.unstoppable(source)) return super.hurtServer(level, source, damage);
		if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CACTUS)) return false;
		boolean ret = super.hurtServer(level, source, damage);
		setOrderedToSit(false);
		setActivity(2);
		return ret;
	}

	// ---- Targets ----

	private boolean canSeeSpot(double x, double y, double z) {
		Vec3 from = new Vec3(getX(), getY() + 0.75, getZ());
		return level().clip(new ClipContext(from, new Vec3(x, y, z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType()
			== HitResult.Type.MISS;
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (level().getDifficulty() == Difficulty.PEACEFUL || e == this || !e.isAlive() || !getSensing().hasLineOfSight(e)) return false;
		if (BossUtil.isRoyalty(e)) return false;
		if (e instanceof Monster || BossUtil.isType(e, "mothra")) return true;
		if (huntsCritters() && (e instanceof orespawn.entity.Butterfly || e instanceof orespawn.entity.Bird)) return true;
		return e instanceof Dragonfly || e instanceof Mosquito;
	}

	private @Nullable LivingEntity findSomethingToAttack(ServerLevel level) {
		List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(12.0, 6.0, 12.0));
		list.sort(BossUtil.targetOrder(this));
		for (LivingEntity e : list) {
			if (isSuitableTarget(e) && canSeeSpot(e.getX(), e.getY(), e.getZ())) return e;
		}
		return null;
	}

	// ---- Interaction (within 4 blocks) ----

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		boolean near = player.distanceToSqr(this) < 16.0;
		boolean mine = isTame() && isOwnedBy(player) && near;
		if (stack.is(Items.DIAMOND_BLOCK) && near) {
			if (level() instanceof ServerLevel) {
				tame(player);
				level().broadcastEntityEvent(this, (byte) 7);
				heal(getMaxHealth() - getHealth());
				onDiamondBlock();
			}
			stack.consume(1, player);
			return InteractionResult.SUCCESS;
		}
		var food = stack.get(net.minecraft.core.component.DataComponents.FOOD);
		if (mine && food != null) {
			if (!level().isClientSide()) {
				if (getMaxHealth() > getHealth()) heal(food.nutrition() * 10);
				level().broadcastEntityEvent(this, (byte) 7);
				onFed();
			}
			stack.consume(1, player);
			return InteractionResult.SUCCESS;
		}
		if (mine && (stack.is(Items.ICE) || stack.is(Items.FLINT_AND_STEEL))) {
			boolean light = stack.is(Items.FLINT_AND_STEEL);
			if (!level().isClientSide()) {
				level().broadcastEntityEvent(this, (byte) 6);
				setFire(light ? 1 : 0);
				player.sendSystemMessage(Component.literal(title() + (light ? " fireballs lit!" : " fireballs extinguished.")));
			}
			stack.consume(1, player);
			return InteractionResult.SUCCESS;
		}
		if (stack.is(Items.DIAMOND) && isTame() && isOwnedBy(player) && near && level() instanceof ServerLevel level && diamond(level, player)) {
			stack.consume(1, player);
			return InteractionResult.SUCCESS;
		}
		if (mine && stack.is(Items.NAME_TAG)) {
			if (!level().isClientSide()) setCustomName(stack.getHoverName());
			stack.consume(1, player);
			return InteractionResult.SUCCESS;
		}
		if (mine) {
			if (!level().isClientSide()) {
				if (!isOrderedToSit()) {
					setOrderedToSit(true);
					setActivity(1);
				} else {
					setOrderedToSit(false);
				}
			}
			return InteractionResult.SUCCESS;
		}
		return super.mobInteract(player, hand);
	}

	/** A diamond block (The Prince: ready to grow at once). */
	protected void onDiamondBlock() {
	}

	/** Fed (The Prince counts it towards growing). */
	protected void onFed() {
	}

	// ---- Saving ----

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("SpyroActivity", getActivity());
		output.putInt("SpyroFire", getFire());
		output.putInt("SpyroKill", killCount);
		output.putInt("SpyroFed", fedCount);
		output.putInt("SpyroDay", dayCount);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		setActivity(input.getIntOr("SpyroActivity", 1));
		setFire(input.getIntOr("SpyroFire", 1));
		killCount = input.getIntOr("SpyroKill", 0);
		fedCount = input.getIntOr("SpyroFed", 0);
		dayCount = input.getIntOr("SpyroDay", 0);
	}
}
