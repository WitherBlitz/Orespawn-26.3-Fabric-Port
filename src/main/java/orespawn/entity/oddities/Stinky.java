package orespawn.entity.oddities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Stinky: a little fire-proof winged beast (100 health, 6 armour) in one of 19 colours that changes now and
 * then. It walks, sniffs out coal ore within 8 blocks and eats it (healing, with a burp), burps up coal, and every so
 * often lets one go out the back: what comes out depends on its colour (blaze powder, rotten flesh, melon seeds,
 * uranium, wheat, brick, a torch, an emerald, gold, leaves, titanium, an apple tree seed, a diamond, sand,
 * cobblestone, a bone, string, a cherry or a peach pit). Now and then, and whenever it's hurt, it takes to the air
 * (activity 2) and goes after monsters (and Mothra) within 12 blocks. Feed it raw beef (within 4 blocks) and half the
 * time it's yours: it follows you (flying along when you fly or get far away), flees when badly hurt, and sits or
 * stands when you click it; a dead bush sets it free. Cactus doesn't hurt it. Tamed ones drop 1-4 beef.
 * <p>
 * Eating coal ore needs mob griefing on (OreSpawn didn't check). Not ported: 1.7.10's EntityAIMoveIndoors has no 26.3
 * equivalent.
 */
public class Stinky extends TamableAnimal {
	private static final EntityDataAccessor<Integer> ACTIVITY = SynchedEntityData.defineId(Stinky.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> SKIN = SynchedEntityData.defineId(Stinky.class, EntityDataSerializers.INT);
	private BlockPos flightTarget;
	private boolean ownerFlying;

	public Stinky(EntityType<? extends Stinky> type, Level level) {
		super(type, level);
		this.xpReward = 35;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return TamableAnimal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 100.0).add(Attributes.MOVEMENT_SPEED, 0.3)
			.add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, 6.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new Oddity.HoldGoals(this, () -> activity() == 2));
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
		builder.define(SKIN, -1);
	}

	/** 1 walking, 2 flying. */
	public int activity() {
		return entityData.get(ACTIVITY);
	}

	private void setActivity(int a) {
		entityData.set(ACTIVITY, a);
	}

	/** Its colour, 0-18 (Stinkytexture1-19). */
	public int skin() {
		return Math.max(0, entityData.get(SKIN));
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("SpyroActivity", activity());
		output.putInt("StinkySkin", skin());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		setActivity(input.getIntOr("SpyroActivity", 1));
		entityData.set(SKIN, input.getIntOr("StinkySkin", 0));
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
			if (server) {
				boolean sit = !isOrderedToSit();
				setOrderedToSit(sit);
				setInSittingPose(sit);
				if (sit) setActivity(1);
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
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.DUCK_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.CRYO_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 0.6F;
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
		return target.hurtServer(level, damageSources().mobAttack(this), 10.0F);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypes.CACTUS)) return false;
		boolean ret = super.hurtServer(level, source, damage);
		setOrderedToSit(false);
		setInSittingPose(false);
		setActivity(2);
		return ret;
	}

	/** dropItemFront / dropItemRear: out of its mouth or its back end. */
	private void dropFrom(ServerLevel level, ItemLike item, boolean front) {
		float f = front ? 0.75F + Math.abs(random.nextFloat() * 0.75F) : 0.55F + Math.abs(random.nextFloat() * 0.55F);
		double s = Math.sin(Math.toRadians(getYHeadRot())), c = Math.cos(Math.toRadians(getYHeadRot()));
		double x = front ? getX() - f * s : getX() + f * s;
		double z = front ? getZ() + f * c : getZ() - f * c;
		ItemEntity drop = new ItemEntity(level, x, getY() + (front ? 0.9 : 0.25), z, new ItemStack(item));
		drop.setDefaultPickUpDelay();
		level.addFreshEntity(drop);
	}

	private ItemLike fart() {
		return switch (skin()) {
			case 0 -> Items.BLAZE_POWDER;
			case 1 -> Items.ROTTEN_FLESH;
			case 2 -> Items.MELON_SEEDS;
			case 3 -> ModItems.URANIUM_NUGGET;
			case 4 -> Items.WHEAT;
			case 5 -> Items.BRICK;
			case 6 -> Items.TORCH;
			case 7 -> Items.EMERALD;
			case 8 -> Items.GOLD_INGOT;
			case 9 -> Items.OAK_LEAVES;
			case 10 -> ModItems.TITANIUM_NUGGET;
			case 11 -> ModItems.lazy("apple_tree_seed");
			case 12 -> Items.DIAMOND;
			case 13 -> Items.SAND;
			case 14 -> Items.COBBLESTONE;
			case 15 -> Items.BONE;
			case 16 -> Items.STRING;
			case 17 -> ModItems.lazy("cherry_pit");
			default -> ModItems.lazy("peach_pit");
		};
	}

	@Override
	public void tick() {
		super.tick();
		if (level() instanceof ServerLevel level) {
			if (entityData.get(SKIN) < 0) entityData.set(SKIN, random.nextInt(19));
			if (random.nextInt(1750) == 1) {
				playSound(SoundEvents.PLAYER_BURP, 1.0F, 1.0F);
				dropFrom(level, Items.COAL, true);
			}
			if (random.nextInt(2000) == 2) {
				playSound(ModSounds.get("fart"), 1.0F, 1.5F);
				ItemLike out = fart();
				if (out.asItem() != Items.AIR) dropFrom(level, out, false);
			}
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (isInWater()) setDeltaMovement(getDeltaMovement().add(0.0, 0.07, 0.0));
		if (!level().isClientSide() && random.nextInt(2000) == 1) entityData.set(SKIN, random.nextInt(19));
		if (flightTarget == null) flightTarget = blockPosition();
		if (activity() == 2) setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6, 1.0));
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		super.customServerAiStep(level);
		if (random.nextInt(100) == 1 && getHealth() < getMaxHealth()) heal(1.0F);
		if (isOrderedToSit()) return;
		if (activity() == 0) setActivity(1);
		if (random.nextInt(100) == 1) setActivity(random.nextInt(20) == 1 ? 2 : 1);
		ownerFlying = false;
		LivingEntity owner = isTame() ? getOwner() : null;
		if (owner instanceof Player p && p.getAbilities().flying) {
			ownerFlying = true;
			setActivity(2);
		}
		if (activity() == 1 && owner != null && distanceToSqr(owner) > 256.0) setActivity(2);
		doMovement(level, owner);
	}

	private void doMovement(ServerLevel level, @Nullable LivingEntity owner) {
		if (flightTarget == null) flightTarget = blockPosition();
		boolean doNew = activity() == 2 && random.nextInt(300) == 0;
		if (owner != null) {
			if (distanceToSqr(owner) > 100.0) doNew = true;
			if (ownerFlying && distanceToSqr(owner) > 36.0) doNew = true;
		}
		if (random.nextInt(7) == 1 && level.getDifficulty() != Difficulty.PEACEFUL) {
			LivingEntity e = Oddity.nearest(this, 12.0, 6.0, this::isSuitableTarget);
			if (e != null) {
				setActivity(2);
				doNew = false;
				if (isTame() && getHealth() / getMaxHealth() < 0.25F) {
					flightTarget = BlockPos.containing(getX() + (getX() - e.getX()), getY() + 1.0, getZ() + (getZ() - e.getZ()));
				} else {
					flightTarget = BlockPos.containing(e.getX(), e.getY() + 1.0, e.getZ());
					double reach = 3.0F + e.getBbWidth() / 2.0F;
					if (distanceToSqr(e) < reach * reach) doHurtTarget(level, e);
				}
			}
		}
		if (activity() == 1) {
			if (random.nextInt(50) == 0) {
				int[] coal = Oddity.search(level, getBlockX(), getBlockY() + 1, getBlockZ(), 9, 2, 4, s -> s.is(Blocks.COAL_ORE));
				if (coal[3] < 99999) {
					getNavigation().moveTo(coal[0], coal[1], coal[2], 1.25);
					if (coal[3] < 12 && level.getGameRules().get(GameRules.MOB_GRIEFING)) {
						level.setBlock(new BlockPos(coal[0], coal[1], coal[2]), Blocks.AIR.defaultBlockState(), 2);
						heal(1.0F);
						playSound(SoundEvents.PLAYER_BURP, 0.5F, random.nextFloat() * 0.2F + 1.5F);
					}
				}
			}
			return;
		}
		if (blockPosition().distSqr(flightTarget) < 2.1) doNew = true;
		if (doNew) {
			for (int tries = 50; tries > 0; tries--) {
				int gx = getBlockX(), gy = getBlockY(), gz = getBlockZ(), dx, dz;
				if (owner != null) {
					gx = owner.getBlockX();
					gy = owner.getBlockY();
					gz = owner.getBlockZ();
					dz = ownerFlying ? random.nextInt(8) : random.nextInt(4) + 6;
					dx = ownerFlying ? random.nextInt(8) : random.nextInt(4) + 6;
				} else {
					dz = random.nextInt(5) + 6;
					dx = random.nextInt(5) + 6;
				}
				if (random.nextInt(2) == 0) dz = -dz;
				if (random.nextInt(2) == 0) dx = -dx;
				flightTarget = new BlockPos(gx + dx, gy + random.nextInt(6 + (ownerFlying ? 2 : 0)) - 2, gz + dz);
				if (level.isEmptyBlock(flightTarget) && Oddity.canSee(this, flightTarget)) break;
			}
		}
		double sf = 1.0;
		if (ownerFlying) sf = owner != null && distanceToSqr(owner) > 49.0 ? 3.5 : 1.75;
		Oddity.steer(this, flightTarget.getX() + 0.5, flightTarget.getY() + 0.1, flightTarget.getZ() + 0.5, 0.5, 0.7, 0.15 * sf, 0.21 * sf, 3.0F);
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (level().getDifficulty() == Difficulty.PEACEFUL || e == this || !e.isAlive() || !getSensing().hasLineOfSight(e)) return false;
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

	/** Stinky.getCanSpawnHere: by day, with no more than 2 Stinkies within 20 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return level.getLevel().isBrightOutside() && level.getEntitiesOfClass(Stinky.class, new AABB(pos).inflate(20.0, 10.0, 20.0)).size() <= 2;
	}
}
