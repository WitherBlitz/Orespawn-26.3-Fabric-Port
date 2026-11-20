package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
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
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * OreSpawn's lizard: a big river lizard (30 health, 5 armor) that hunts spiders, cave spiders and chickens within 12
 * blocks (off peaceful), biting for 6, and fights back against anything that hurts it, snapping its jaw and lashing its
 * tail while it fights. Out of the water it heads back to the nearest water now and then. Give it a dye of any colour
 * (OreSpawn checked only for its dye item, never the colour, so ink sac, cocoa beans, lapis and bone meal count too, as
 * do today's white, blue, brown and black dyes) and it follows you for 2.5-4 minutes; clicking it with anything else, or
 * hurting it, stops that. It sometimes tags along with another lizard instead. Immune to cactus. While it's following
 * someone it doesn't despawn. No drops.
 *
 * <p>Not ported yet: hunting OreSpawn's attack squids, its "cannon fodder" hats (carrot, potato or quinoa activation,
 * corn cob cloning, the guard behavior and its two other textures; it couldn't be tamed any other way, so the follow-owner
 * goal kept from OreSpawn never runs)). It breeds with OreSpawn's Crystal Apple.
 */
public class Lizard extends TamableAnimal {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Lizard.class, EntityDataSerializers.BOOLEAN);
	/** Who it's following: a player who gave it dye, or another lizard. Not saved, as in OreSpawn. */
	private @Nullable LivingEntity buddy;
	private int followTime;
	private boolean shouldDespawn = true;

	public Lizard(EntityType<? extends Lizard> type, Level level) {
		super(type, level);
		this.xpReward = 15;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return TamableAnimal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.3)
			.add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ARMOR, 5.0);
	}

	/** OreSpawn's Items.dye, any colour: today's dyes plus the four items that were dyes in 1.7.10. */
	public static boolean isDye(ItemStack stack) {
		return stack.is(ItemTags.DYES) || stack.is(Items.INK_SAC) || stack.is(Items.COCOA_BEANS) || stack.is(Items.LAPIS_LAZULI)
			|| stack.is(Items.BONE_MEAL);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new FollowOwnerGoal(this, 2.0, 10.0F, 2.0F));
		goalSelector.addGoal(2, new BreedGoal(this, 1.0));
		goalSelector.addGoal(3, new TemptGoal(this, 1.25, Lizard::isDye, false));
		goalSelector.addGoal(4, WanderGoal.wanderALot(this, 16, 1.0));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	public boolean attacking() {
		return entityData.get(ATTACKING);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		if (followTime > 0) {
			followTime--;
			shouldDespawn = false;
		} else {
			shouldDespawn = true;
		}
		if (!isInWater() && random.nextInt(100) == 0) {
			BlockPos water = nearestWater(level, blockPosition().below());
			if (water != null) getNavigation().moveTo(water.getX(), water.getY() - 1, water.getZ(), 1.33);
		}
		if (getHealth() < getMaxHealth() && random.nextInt(300) == 1) heal(1.0F);
		if (level.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(10) == 1) {
			LivingEntity prey = findSomethingToAttack(level);
			if (prey != null) {
				followTime = 0;
				if (distanceToSqr(prey) < 12.0) {
					entityData.set(ATTACKING, true);
					if (random.nextInt(4) == 0 || random.nextInt(5) == 1) doHurtTarget(level, prey);
				} else {
					getNavigation().moveTo(prey, 1.2);
				}
			} else {
				if (buddyAround() && random.nextInt(15) == 1) getNavigation().moveTo(buddy, 1.0);
				entityData.set(ATTACKING, false);
			}
		}
		if (buddyAround() && followTime > 0 && random.nextInt(20) == 1) getNavigation().moveTo(buddy, 1.0);
	}

	private boolean buddyAround() {
		return buddy != null && buddy.isAlive() && buddy.level() == level();
	}

	/** Lizard.scan_it: the nearest water, scanning shells 1-13 blocks out (at most 5 up or down, every other shell past 5). */
	private static @Nullable BlockPos nearestWater(ServerLevel level, BlockPos c) {
		for (int i = 1; i < 14; i++) {
			int dy = Math.min(i, 5);
			BlockPos best = null;
			int bestD = Integer.MAX_VALUE;
			for (BlockPos p : BlockPos.betweenClosed(c.offset(-i, -dy, -i), c.offset(i, dy, i))) {
				int ax = Math.abs(p.getX() - c.getX()), ay = Math.abs(p.getY() - c.getY()), az = Math.abs(p.getZ() - c.getZ());
				if (ax != i && az != i && ay != dy) continue;
				if (!level.getBlockState(p).is(Blocks.WATER)) continue;
				int d = ax * ax + ay * ay + az * az;
				if (d < bestD) {
					bestD = d;
					best = p.immutable();
				}
			}
			if (best != null) return best;
			if (i >= 5) i++;
		}
		return null;
	}

	/** Lizard.findSomethingToAttack: whatever it's already after, else the nearest spider or chicken it can see. */
	private @Nullable LivingEntity findSomethingToAttack(ServerLevel level) {
		if (random.nextInt(100) == 0) setTarget(null);
		LivingEntity target = getTarget();
		if (target != null && target.isAlive()) return target;
		setTarget(null);
		List<LivingEntity> near = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(12.0, 4.0, 12.0)));
		near.sort(Comparator.comparingDouble(this::sortDistance));
		for (LivingEntity e : near) {
			if (isSuitableTarget(level, e)) return e;
		}
		return null;
	}

	/** OreSpawn's GenericTargetSorter: distance squared, halved for creepers, divided by the size of anything big. */
	private double sortDistance(Entity e) {
		double d = distanceToSqr(e);
		if (e instanceof Creeper) d /= 2.0;
		double size = e.getBbHeight() * e.getBbWidth();
		if (size > 1.0) d /= size;
		return d;
	}

	private boolean isSuitableTarget(ServerLevel level, LivingEntity e) {
		if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
		if (e == this || !e.isAlive() || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Spider || e instanceof Chicken) return true;
		if (e instanceof Lizard && random.nextInt(10) == 1 && followTime <= 0) buddy = e;
		return false;
	}

	/** Lizard.attackEntityAsMob: a plain 6-damage bite. */
	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		return target.hurtServer(level, damageSources().mobAttack(this), 6.0F);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		boolean hurt = false;
		if (!source.is(DamageTypes.CACTUS)) {
			hurt = super.hurtServer(level, source, damage);
			if (source.getEntity() instanceof LivingEntity attacker) setTarget(attacker);
		}
		followTime = 0;
		return hurt;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		InteractionResult result = super.mobInteract(player, hand);
		if (result.consumesAction()) return result;
		ItemStack stack = player.getItemInHand(hand);
		if (isDye(stack) && player.distanceToSqr(this) < 16.0) {
			if (!level().isClientSide()) {
				buddy = player;
				followTime = 3000 + random.nextInt(2000);
				level().broadcastEntityEvent(this, (byte) 7);
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		if (!level().isClientSide()) {
			buddy = null;
			followTime = 0;
			level().broadcastEntityEvent(this, (byte) 6);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected int getFireImmuneTicks() {
		return 3;
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
		return 1.0F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		if (isBaby()) {
			setPersistenceRequired();
			return false;
		}
		return !isPersistenceRequired() && !isTame() && shouldDespawn;
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return orespawn.entity.Breeding.crystalApple(stack);
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return ModEntities.LIZARD.create(level, EntitySpawnReason.BREEDING);
	}

	/** Lizard.getCanSpawnHere: at y 50 or higher (it spawns in water, as OreSpawn's water creature did). */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return EntitySpawnReason.isSpawner(reason) || pos.getY() >= 50;
	}
}
