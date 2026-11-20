package orespawn.entity.oddities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import orespawn.content.Oddities;
import orespawn.entity.ModEntities;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Rubber Ducky: a little floating duck (5 health) that waddles back to the nearest water, quacks, and pecks
 * squid. Feed it a raw fish (within 4 blocks) and half the time it's yours: it follows you, heals with more fish, sits
 * or stands when you click it, and a dead bush sets it free. Kill one and the ducks remember: each kill raises the
 * kill count, and a new duck appears nearby carrying it (up to 10). At 5 or more they turn evil (red eyes, won't sit)
 * and peck players too, twice as hard. The count wears off slowly. Drops 0-2 feathers or, half the rest of the time,
 * 0-2 Rubber Ducky spawn eggs.
 * <p>
 * Breeds with OreSpawn's Crystal Apple.
 */
public class RubberDucky extends TamableAnimal {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(RubberDucky.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> KILLS = SynchedEntityData.defineId(RubberDucky.class, EntityDataSerializers.INT);
	private boolean died;
	private @Nullable LivingEntity buddy;

	public RubberDucky(EntityType<? extends RubberDucky> type, Level level) {
		super(type, level);
		this.xpReward = 15;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return TamableAnimal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.MOVEMENT_SPEED, 0.22)
			.add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ARMOR, 1.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
		goalSelector.addGoal(1, new BreedGoal(this, 1.0));
		goalSelector.addGoal(1, new FollowOwnerGoal(this, 2.0, 10.0F, 2.0F));
		goalSelector.addGoal(3, new TemptGoal(this, 1.25, s -> s.is(Items.COD), false));
		goalSelector.addGoal(4, WanderGoal.wanderALot(this, 16, 1.0));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, LivingEntity.class, 6.0F));
		goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
		builder.define(KILLS, 0);
	}

	public boolean attacking() {
		return entityData.get(ATTACKING);
	}

	public int killCount() {
		return entityData.get(KILLS);
	}

	public void setKillCount(int n) {
		entityData.set(KILLS, n);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("Killcount", killCount());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		setKillCount(input.getIntOr("Killcount", 0));
	}

	@Override
	public void tick() {
		super.tick();
		if (isInWater()) {
			double my = getDeltaMovement().y + 0.1F;
			setDeltaMovement(getDeltaMovement().x, Math.max(my, -0.05F), getDeltaMovement().z);
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		boolean ret = super.hurtServer(level, source, damage);
		setOrderedToSit(false);
		setInSittingPose(false);
		if (source.getEntity() instanceof Player && (isRemoved() || getHealth() <= 0.0F) && !died) {
			died = true;
			int kills = killCount() + 1;
			setKillCount(kills);
			if (kills < 10) {
				// Revenge: a new duck nearby, on the first spot with ground under air.
				for (int m = 0; m < 20; m++) {
					int i = random.nextInt(3), k = random.nextInt(3);
					if (random.nextInt(2) == 1) i = -i;
					if (random.nextInt(2) == 1) k = -k;
					for (int j = 3; j > -3; j--) {
						BlockPos above = BlockPos.containing(getX() + i, getY() + j + 1, getZ() + k);
						if (level.isEmptyBlock(above) && !level.isEmptyBlock(above.below())) {
							RubberDucky d = Oddity.spawn(level, Oddities.RUBBER_DUCKY, getBlockX() + i + 1, getBlockY() + j + 1, getBlockZ() + k);
							if (d != null) d.setKillCount(kills);
							return ret;
						}
					}
				}
			}
		}
		return ret;
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
		return random.nextInt(10) == 1 ? ModSounds.DUCK_HURT : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.DUCK_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.DUCK_HURT;
	}

	@Override
	protected float getSoundVolume() {
		return 0.8F;
	}

	@Override
	public float getVoicePitch() {
		return 1.2F;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		ItemStack drop = random.nextInt(2) == 1 ? new ItemStack(Items.FEATHER)
			: random.nextInt(2) == 1 ? new ItemStack(ModEntities.SPAWN_EGGS.get("rubber_ducky")) : ItemStack.EMPTY;
		if (drop.isEmpty()) return;
		for (int n = Oddity.fewItems(this, source); n > 0; n--) spawnAtLocation(level, drop.copy());
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		InteractionResult sup = super.mobInteract(player, hand);
		if (sup.consumesAction()) return sup;
		boolean near = player.distanceToSqr(this) < 16.0;
		if (stack.is(Items.COD) && near) {
			if (!level().isClientSide()) {
				if (!isTame()) {
					if (random.nextInt(2) == 0) {
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
		if (isTame() && stack.is(Items.DEAD_BUSH) && near && isOwnedBy(player)) {
			if (!level().isClientSide()) {
				setTame(false, true);
				setOwner(null);
				level().broadcastEntityEvent(this, (byte) 6);
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		if (isTame() && isOwnedBy(player) && near) {
			if (!level().isClientSide()) {
				boolean sit = !isOrderedToSit() && killCount() < 5;
				setOrderedToSit(sit);
				setInSittingPose(sit);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (!isInWater() && random.nextInt(50) == 0) {
			int[] w = Oddity.search(level, getBlockX(), getBlockY() - 1, getBlockZ(), 14, 5, 5, s -> s.getFluidState().is(FluidTags.WATER));
			if (w[3] < 99999) getNavigation().moveTo(w[0], w[1] - 1, w[2], 1.33);
		}
		if (killCount() > 0 && random.nextInt(200) == 1) setKillCount(killCount() - 1);
		if (getHealth() < getMaxHealth() && random.nextInt(300) == 1) heal(1.0F);
		if (level.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(5) == 1) {
			LivingEntity e = findSomethingToAttack();
			if (e != null) {
				if (distanceToSqr(e) < 12.0) {
					entityData.set(ATTACKING, true);
					if (random.nextInt(4) == 0 || random.nextInt(5) == 1) doHurtTarget(level, e);
				} else {
					getNavigation().moveTo(e, 1.2);
				}
			} else {
				if (buddy != null && buddy.isAlive() && random.nextInt(15) == 1) getNavigation().moveTo(buddy, 1.0);
				entityData.set(ATTACKING, false);
			}
		}
		if (buddy != null && buddy.isAlive() && random.nextInt(20) == 1) getNavigation().moveTo(buddy, 1.0);
	}

	/** RubberDucky.attackEntityAsMob: a peck of 1, or 2 when evil. */
	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		return target.hurtServer(level, damageSources().mobAttack(this), killCount() >= 5 ? 2.0F : 1.0F);
	}

	private @Nullable LivingEntity findSomethingToAttack() {
		LivingEntity current = getTarget();
		if (current != null && current.isAlive()) return current;
		setTarget(null);
		buddy = null;
		return Oddity.nearest(this, 8.0, 4.0, this::isSuitableTarget);
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (level().getDifficulty() == Difficulty.PEACEFUL || e == this || !e.isAlive() || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Squid || Oddity.is(e, "attack_squid")) return true;
		if (e instanceof RubberDucky && random.nextInt(10) == 1) buddy = e;
		return killCount() >= 5 && e instanceof Player p && !Oddity.creative(p);
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		if (isBaby()) {
			setPersistenceRequired();
			return false;
		}
		return !isPersistenceRequired() && !isTame();
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return orespawn.entity.Breeding.crystalApple(stack);
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return Oddities.RUBBER_DUCKY.create(level, EntitySpawnReason.BREEDING);
	}

	/** RubberDucky.getCanSpawnHere: at y 50 or higher, by day. Spawners always may. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return pos.getY() >= 50 && level.getLevel().isBrightOutside();
	}
}
