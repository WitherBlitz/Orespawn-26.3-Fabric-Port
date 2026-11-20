package orespawn.entity.oddities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import orespawn.content.Oddities;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Baby Dragon (its class is Spyro): a small fire-proof dragon, 200 health, 5 armour. It walks, and now and
 * then takes to the air (activity 2), roaring; it seeks out lava nearby and heals bathing in it. Feed it raw beef
 * (within 4 blocks) and half the time it's yours: it follows you (flying along when you fly or get more than 16 blocks
 * away), fights monsters (and Mothra) within 12 blocks with bites and fireballs, flees when badly hurt, and sits or
 * stands when you click it. Light its fireballs with flint and steel (more often) or put them out with ice; a diamond
 * grows a tamed one into a full Dragon at once (wild ones do so very rarely on their own); a dead bush sets it free.
 * Cactus doesn't hurt it. Tamed ones drop 1-4 beef.
 */
public class Spyro extends TamableAnimal {
	private static final EntityDataAccessor<Integer> ACTIVITY = SynchedEntityData.defineId(Spyro.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> FIRE = SynchedEntityData.defineId(Spyro.class, EntityDataSerializers.INT);
	private BlockPos flightTarget;
	private boolean ownerFlying;
	private boolean targetInSight;

	public Spyro(EntityType<? extends Spyro> type, Level level) {
		super(type, level);
		this.xpReward = 35;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return TamableAnimal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.MOVEMENT_SPEED, 0.3)
			.add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.ARMOR, 5.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(1, new FloatGoal(this));
		goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
		goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 8.0F, 0.3, 0.4));
		goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.15, 12.0F, 2.0F));
		goalSelector.addGoal(4, new TemptGoal(this, 1.25, s -> s.is(Items.BEEF), false));
		goalSelector.addGoal(5, new PanicGoal(this, 1.5));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		goalSelector.addGoal(7, WanderGoal.wander(this, 0.75));
		goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ACTIVITY, 1);
		builder.define(FIRE, 1);
	}

	/** 1 walking, 2 flying. */
	public int activity() {
		return entityData.get(ACTIVITY);
	}

	private void setActivity(int a) {
		entityData.set(ACTIVITY, a);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("SpyroActivity", activity());
		output.putInt("SpyroFire", entityData.get(FIRE));
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		setActivity(input.getIntOr("SpyroActivity", 1));
		entityData.set(FIRE, input.getIntOr("SpyroFire", 1));
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		boolean near = player.distanceToSqr(this) < 16.0;
		boolean server = !level().isClientSide();
		if (stack.is(Items.BEEF) && near) {
			if (server) {
				if (!isTame()) {
					if (random.nextInt(2) == 1) {
						tame(player);
						level().broadcastEntityEvent(this, (byte) 7);
						heal(getMaxHealth() - getHealth());
					} else {
						level().broadcastEntityEvent(this, (byte) 6);
					}
				} else if (isOwnedBy(player)) {
					heal(getMaxHealth() - getHealth());
				}
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		if (isTame() && near && isOwnedBy(player)) {
			if (stack.is(Items.DEAD_BUSH)) {
				if (server) {
					setTame(false, true);
					setHealth(getMaxHealth());
					setOwner(null);
					level().broadcastEntityEvent(this, (byte) 6);
					stack.consume(1, player);
				}
				return InteractionResult.SUCCESS;
			}
			if (stack.is(Items.ICE)) {
				if (server) {
					level().broadcastEntityEvent(this, (byte) 6);
					entityData.set(FIRE, 0);
					player.sendSystemMessage(Component.literal("Baby Dragon fireballs extinguished."));
					stack.consume(1, player);
				}
				return InteractionResult.SUCCESS;
			}
			if (stack.is(Items.DIAMOND)) {
				if (server) {
					Dragon d = Oddity.spawn((ServerLevel) level(), Oddities.DRAGON, getX(), getY(), getZ());
					if (d != null) {
						d.tame(player);
						discard();
					}
					stack.consume(1, player);
				}
				return InteractionResult.SUCCESS;
			}
			if (stack.is(Items.FLINT_AND_STEEL)) {
				if (server) {
					level().broadcastEntityEvent(this, (byte) 6);
					entityData.set(FIRE, 1);
					player.sendSystemMessage(Component.literal("Baby Dragon fireballs lit!"));
					stack.consume(1, player);
				}
				return InteractionResult.SUCCESS;
			}
			if (stack.is(Items.NAME_TAG)) return InteractionResult.PASS;
			if (server) {
				boolean sit = !isOrderedToSit();
				setOrderedToSit(sit);
				setInSittingPose(sit);
			}
			return InteractionResult.SUCCESS;
		}
		return super.mobInteract(player, hand);
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isPersistenceRequired() && !isTame();
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return isOrderedToSit() || activity() != 2 ? null : ModSounds.get("roar");
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.DUCK_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.CRYO_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	public float getVoicePitch() {
		return (random.nextFloat() - random.nextFloat()) * 0.1F + (isBaby() ? 1.5F : 1.0F);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		if (isTame()) {
			for (int i = 1 + random.nextInt(4); i > 0; i--) spawnAtLocation(level, Items.BEEF);
		}
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		return target.hurtServer(level, damageSources().mobAttack(this), 4.0F);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypes.CACTUS)) return false;
		return super.hurtServer(level, source, damage);
	}

	@Override
	public void tick() {
		super.tick();
		if (isInWater()) setDeltaMovement(getDeltaMovement().add(0.0, 0.07, 0.0));
		if (!(level() instanceof ServerLevel level)) return;
		if (flightTarget == null) flightTarget = blockPosition();
		if (random.nextInt(100000) == 1 && !isPersistenceRequired()) {
			// Grows up on its own, very rarely.
			Dragon d = Oddity.spawn(level, Oddities.DRAGON, getX(), getY(), getZ());
			if (d != null) {
				if (isTame()) {
					d.setTame(true, true);
					d.setOwnerReference(getOwnerReference());
				}
				discard();
				return;
			}
		}
		if (activity() == 2) {
			double f = getY() < flightTarget.getY() + 2.0 ? 0.7 : getY() > flightTarget.getY() - 2.0 ? 0.5 : 0.61;
			setDeltaMovement(getDeltaMovement().multiply(1.0, f, 1.0));
		}
		if (activity() == 1 && isTame() && getOwner() != null && distanceToSqr(getOwner()) > 256.0) setActivity(2);
		doMovement(level);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		if (!isOrderedToSit()) {
			if (activity() == 0) setActivity(1);
			if (random.nextInt(20) == 0) {
				int[] lava = Oddity.search(level, getBlockX(), getBlockY() - 1, getBlockZ(), 11, 4, 6, s -> s.getFluidState().is(FluidTags.LAVA));
				if (lava[3] < 99999) {
					setActivity(1);
					getNavigation().moveTo(lava[0], lava[1] - 1, lava[2], 1.0);
					if (isInLava()) {
						heal(1.0F);
						playSound(SoundEvents.GENERIC_SPLASH, 1.0F, random.nextFloat() * 0.2F + 0.9F);
					}
				}
			}
			if (random.nextInt(100) == 1 && !targetInSight) setActivity(random.nextInt(8) == 1 ? 2 : 1);
			ownerFlying = false;
			if (isTame() && getOwner() instanceof Player p && p.getAbilities().flying) {
				ownerFlying = true;
				setActivity(2);
			}
		}
		if (random.nextInt(100) == 1 && getHealth() < getMaxHealth()) heal(1.0F);
	}

	private void doMovement(ServerLevel level) {
		if (isOrderedToSit() || activity() == 1) return;
		boolean doNew = random.nextInt(300) == 0;
		LivingEntity owner = isTame() ? getOwner() : null;
		if (owner != null) {
			if (distanceToSqr(owner) > 100.0) doNew = true;
			if (ownerFlying && distanceToSqr(owner) > 36.0) doNew = true;
		}
		if (random.nextInt(6) == 1 && level.getDifficulty() != Difficulty.PEACEFUL) {
			LivingEntity e = Oddity.nearest(this, 12.0, 6.0, this::isSuitableTarget);
			if (e != null) {
				setActivity(2);
				doNew = false;
				if (isTame() && getHealth() / getMaxHealth() < 0.25F) {
					targetInSight = false;
					flightTarget = BlockPos.containing(getX() + (getX() - e.getX()), getY() + 1.0, getZ() + (getZ() - e.getZ()));
				} else {
					targetInSight = true;
					flightTarget = BlockPos.containing(e.getX(), e.getY() + 1.0, e.getZ());
					getNavigation().moveTo(e, 1.25);
					double reach = 3.0F + e.getBbWidth() / 2.0F;
					if (distanceToSqr(e) < reach * reach) {
						doHurtTarget(level, e);
					} else if (distanceToSqr(e) < 64.0 && !isInWater()
						&& (entityData.get(FIRE) == 1 && random.nextInt(10) == 0 || random.nextInt(15) == 1)) {
						Oddity.smallFireball(this, getX(), getY() + 1.25, getZ(), e.getX(), e.getY() + 0.25, e.getZ());
						Oddity.bowSound(this);
					}
				}
			} else {
				targetInSight = false;
			}
		}
		if (blockPosition().distSqr(flightTarget) < 2.1 && activity() != 3) doNew = true;
		if (doNew && !targetInSight) {
			for (int tries = 50; tries > 0; tries--) {
				int gx = getBlockX(), gy = getBlockY(), gz = getBlockZ(), dx, dz;
				if (owner != null) {
					gx = owner.getBlockX();
					gy = owner.getBlockY();
					gz = owner.getBlockZ();
					dz = ownerFlying ? random.nextInt(6) : random.nextInt(4) + 6;
					dx = ownerFlying ? random.nextInt(6) : random.nextInt(4) + 6;
				} else {
					dz = random.nextInt(5) + 6;
					dx = random.nextInt(5) + 6;
				}
				if (random.nextInt(2) == 0) dz = -dz;
				if (random.nextInt(2) == 0) dx = -dx;
				flightTarget = new BlockPos(gx + dx, gy + random.nextInt(9 + (ownerFlying ? 2 : 0)) - 4, gz + dz);
				if (level.isEmptyBlock(flightTarget) && Oddity.canSee(this, flightTarget)) break;
			}
		}
		double sf = 0.5;
		if (ownerFlying) sf = owner != null && distanceToSqr(owner) > 49.0 ? 3.5 : 1.75;
		Oddity.steer(this, flightTarget.getX() + 0.5, flightTarget.getY() + 0.1, flightTarget.getZ() + 0.5, 0.5, 0.7, 0.15 * sf, 0.21 * sf, 3.0F);
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (level().getDifficulty() == Difficulty.PEACEFUL || e == this || !e.isAlive() || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Spyro) return false;
		if (!(e instanceof Mothra || e instanceof Monster)) return false;
		return Oddity.canSee(this, e.getX(), e.getY(), e.getZ());
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return false;
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}

	/** Spyro.getCanSpawnHere: by day, at y 50 or higher. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return level.getLevel().isBrightOutside() && pos.getY() >= 50;
	}
}
