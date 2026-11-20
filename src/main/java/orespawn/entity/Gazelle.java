package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
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
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * OreSpawn's gazelle: a shy grazer that keeps away from players (unless tamed) and monsters. Feed it an apple (within 4
 * blocks): half the time a wild one is tamed and healed; a tamed one is healed. Click your own with anything else to make
 * it sit or stand; give it a dead bush to set it free. While it isn't sitting it grazes now and then (1 in 750 a tick, or
 * 1 in 30 while hurt): it finds the nearest potatoes, carrots, short grass, fern or tall plant (tall grass, large fern,
 * sunflower, lilac, rose bush, peony) within 10 blocks, walks to it and, when close, eats it (only removes it when mob
 * griefing is on), burps and heals 1. Now and then it wanders over to the nearest other gazelle. A tamed one never takes
 * more than 10 damage from one hit; falls hurt it at most 2. Never despawns. Wild ones drop 0-2 raw beef (loot table),
 * tamed ones 2-6 poppies.
 *
 * <p>OreSpawn's findBuddy picked the nearest gazelle including itself, so it walked to its own spot; like the beaver's, this
 * one picks the nearest other gazelle. Breeds with OreSpawn's Crystal Apple. Not ported yet: grazing OreSpawn's strawberry
 * plants, and EntityAIMoveIndoors (villager doors, which 26.3 doesn't have).
 */
public class Gazelle extends TamableAnimal {
	public Gazelle(EntityType<? extends Gazelle> type, Level level) {
		super(type, level);
		this.xpReward = 5;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return TamableAnimal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 15.0).add(Attributes.MOVEMENT_SPEED, 0.3)
			.add(Attributes.ATTACK_DAMAGE, 0.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
		goalSelector.addGoal(1, new BreedGoal(this, 1.0));
		goalSelector.addGoal(2, new FollowOwnerGoal(this, 2.0, 10.0F, 2.0F));
		goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Monster.class, 8.0F, 1.0, 1.7F));
		goalSelector.addGoal(4, new TemptGoal(this, 1.2F, s -> s.is(Items.APPLE), false));
		goalSelector.addGoal(5, new PanicGoal(this, 1.5));
		goalSelector.addGoal(6, new AvoidEntityGoal<>(this, Player.class, 12.0F, 1.0, 2.0, e -> !isTame()));
		goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
		goalSelector.addGoal(8, WanderGoal.wander(this, 1.0));
		goalSelector.addGoal(9, new RandomLookAroundGoal(this));
	}

	/** What a gazelle grazes: OreSpawn's potatoes, carrots, tallgrass (short grass, fern) and double_plant blocks. */
	public static boolean isFodder(BlockState state) {
		return state.is(Blocks.POTATOES) || state.is(Blocks.CARROTS) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN)
			|| state.is(Blocks.TALL_GRASS) || state.is(Blocks.LARGE_FERN) || state.is(Blocks.SUNFLOWER) || state.is(Blocks.LILAC)
			|| state.is(Blocks.ROSE_BUSH) || state.is(Blocks.PEONY);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		if (!isOrderedToSit()) {
			if (random.nextInt(30) == 0 && (int) getHealth() < 15 || random.nextInt(750) == 1) {
				BlockPos center = blockPosition().above();
				BlockPos food = nearestFodder(level, center);
				if (food != null) {
					getNavigation().moveTo(food.getX(), food.getY(), food.getZ(), 1.0);
					if (food.distSqr(center) < 12) {
						if (level.getGameRules().get(GameRules.MOB_GRIEFING)) {
							level.setBlock(food, Blocks.AIR.defaultBlockState(), 2);
						}
						heal(1.0F);
						playSound(SoundEvents.PLAYER_BURP, 1.0F, random.nextFloat() * 0.2F + 0.9F);
					}
				}
			}
			if (random.nextInt(250) == 1) {
				level.getEntitiesOfClass(Gazelle.class, getBoundingBox().inflate(16.0, 6.0, 16.0), g -> g != this).stream()
					.min(Comparator.comparingDouble(this::distanceToSqr))
					.ifPresent(g -> getNavigation().moveTo(g.getX(), g.getY(), g.getZ(), 0.5));
			}
		}
		if (random.nextInt(250) == 0) heal(1.0F);
	}

	/** Gazelle.scan_it: the nearest fodder, scanning shells 1-10 blocks out (at most 2 up or down, every other shell past 6). */
	private @Nullable BlockPos nearestFodder(ServerLevel level, BlockPos c) {
		for (int i = 1; i < 11; i++) {
			int dy = Math.min(i, 2);
			BlockPos best = null;
			int bestD = Integer.MAX_VALUE;
			for (BlockPos p : BlockPos.betweenClosed(c.offset(-i, -dy, -i), c.offset(i, dy, i))) {
				int ax = Math.abs(p.getX() - c.getX()), ay = Math.abs(p.getY() - c.getY()), az = Math.abs(p.getZ() - c.getZ());
				if (ax != i && az != i && ay != dy) continue;
				if (!isFodder(level.getBlockState(p))) continue;
				int d = ax * ax + ay * ay + az * az;
				if (d < bestD) {
					bestD = d;
					best = p.immutable();
				}
			}
			if (best != null) return best;
			if (i >= 6) i++;
		}
		return null;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (isTame() && damage > 10.0F) damage = 10.0F;
		return super.hurtServer(level, source, damage);
	}

	@Override
	protected int calculateFallDamage(double fallDistance, float multiplier) {
		int d = Mth.ceil(fallDistance - 3.0F);
		return Math.min(d, 2);
	}

	@Override
	protected int getFireImmuneTicks() {
		return 100;
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
		return ModSounds.get("scorpion_hit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("cryo_death");
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	public float getVoicePitch() {
		return (random.nextFloat() - random.nextFloat()) * 0.1F + (isBaby() ? 1.5F : 1.0F);
	}

	/** Gazelle.dropFewItems: a wild one drops its loot table (raw beef), a tamed one 2-6 poppies instead. */
	@Override
	protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKilled) {
		if (!isTame()) super.dropFromLootTable(level, source, playerKilled);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		if (isTame()) {
			int n = 2 + random.nextInt(5);
			for (int i = 0; i < n; i++) spawnAtLocation(level, Items.POPPY);
		}
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return false;
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return orespawn.entity.Breeding.crystalApple(stack);
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return ModEntities.GAZELLE.create(level, EntitySpawnReason.BREEDING);
	}

	/** Gazelle.getCanSpawnHere: y 50 to 100, on dirt (any kind), a grass block, or in short grass or fern. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (pos.getY() < 50 || pos.getY() > 100) return false;
		BlockState below = level.getBlockState(pos.below());
		return below.is(Blocks.DIRT) || below.is(Blocks.COARSE_DIRT) || below.is(Blocks.PODZOL) || below.is(Blocks.GRASS_BLOCK)
			|| below.is(Blocks.SHORT_GRASS) || below.is(Blocks.FERN);
	}
}
