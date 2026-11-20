package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's chipmunk: a tameable pet. Feed it an apple (within 4 blocks): half the time a wild one is tamed and healed;
 * a tamed one is healed. Click your own with anything else to make it sit or stand; give it a dead bush to set it free.
 * Wild ones dig holes now and then (dirt or farmland under them, when mob griefing is on). Falls hurt it at most 2.
 * Wild ones drop wheat, tamed ones 2-6 poppies. It breeds with OreSpawn's Crystal Apple. OreSpawn's "cannon fodder"
 * hats come later.
 */
public class Chipmunk extends TamableAnimal {
	public Chipmunk(EntityType<? extends Chipmunk> type, Level level) {
		super(type, level);
		this.xpReward = 5;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return TamableAnimal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.MOVEMENT_SPEED, 0.38)
			.add(Attributes.ATTACK_DAMAGE, 1.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
		goalSelector.addGoal(1, new BreedGoal(this, 1.0));
		goalSelector.addGoal(2, new FollowOwnerGoal(this, 2.0, 10.0F, 2.0F));
		goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Monster.class, 8.0F, 1.0, 1.6F));
		goalSelector.addGoal(4, new TemptGoal(this, 1.2F, s -> s.is(Items.APPLE), false));
		goalSelector.addGoal(5, new PanicGoal(this, 1.5));
		goalSelector.addGoal(6, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.0, 1.4F, e -> !isTame()));
		goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
		goalSelector.addGoal(8, new LookAtPlayerGoal(this, LivingEntity.class, 5.0F));
		goalSelector.addGoal(9, WanderGoal.wanderALot(this, 10, 1.0));
		goalSelector.addGoal(10, new RandomLookAroundGoal(this));
	}

	@Override
	protected int calculateFallDamage(double fallDistance, float multiplier) {
		int d = Mth.ceil(fallDistance - 3.0F);
		return Math.min(d, 2);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		if (random.nextInt(250) == 0) heal(1.0F);
		if (random.nextInt(600) == 1) {
			BlockPos below = blockPosition().below();
			BlockState state = level.getBlockState(below);
			if ((state.is(Blocks.DIRT) || state.is(Blocks.FARMLAND)) && level.getGameRules().get(GameRules.MOB_GRIEFING)) {
				level.setBlock(below, Blocks.AIR.defaultBlockState(), 2);
			}
		}
		super.customServerAiStep(level);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player.distanceToSqr(this) >= 16.0) return super.mobInteract(player, hand);
		if (stack.is(Items.APPLE)) {
			if (!level().isClientSide()) {
				if (!isTame()) {
					if (random.nextInt(2) == 0) {
						tame(player);
						heal(getMaxHealth() - getHealth());
						level().broadcastEntityEvent(this, (byte) 7);
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
		if (isTame() && isOwnedBy(player) && stack.is(Items.DEAD_BUSH)) {
			if (!level().isClientSide()) {
				setTame(false, true);
				setOwner(null);
				setOrderedToSit(false);
				level().broadcastEntityEvent(this, (byte) 6);
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		InteractionResult result = super.mobInteract(player, hand);
		if (result.consumesAction()) return result;
		if (isTame() && isOwnedBy(player) && !stack.is(Items.NAME_TAG)) {
			if (!level().isClientSide()) setOrderedToSit(!isOrderedToSit());
			return InteractionResult.SUCCESS;
		}
		return result;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.SCORPION_HIT;
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
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		// Chipmunk.dropFewItems: a tamed one drops 2-6 poppies, a wild one 0-2 wheat (no loot table, since it depends on taming).
		int n = isTame() ? 2 + random.nextInt(5) : random.nextInt(3);
		for (int i = 0; i < n; i++) spawnAtLocation(level, isTame() ? Items.POPPY : Items.WHEAT);
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
		return ModEntities.CHIPMUNK.create(level, EntitySpawnReason.BREEDING);
	}

	/** Chipmunk.getCanSpawnHere: at y 50 or higher, no more than 2 chipmunks within 20 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return pos.getY() >= 50 && level.getEntitiesOfClass(Chipmunk.class, new AABB(pos).inflate(20.0, 10.0, 20.0)).size() <= 2;
	}
}
