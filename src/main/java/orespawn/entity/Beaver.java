package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * OreSpawn's beaver: now and then (1 in 350 a tick, or 1 in 30 while hurt) it looks for the nearest wood within 10
 * blocks (logs, wooden fences and fence gates), walks to it and, when close and mob griefing is on, chews it down with
 * a chainsaw sound: the whole connected tree (up to 200 blocks deep) falls apart into items a few blocks up. Each meal
 * heals 1. Shy of players and monsters; never despawns. Drops raw porkchop.
 */
public class Beaver extends Animal {
	public Beaver(EntityType<? extends Beaver> type, Level level) {
		super(type, level);
		this.xpReward = 5;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 15.0).add(Attributes.MOVEMENT_SPEED, 0.2)
			.add(Attributes.ATTACK_DAMAGE, 1.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new BreedGoal(this, 1.0));
		goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 8.0F, 1.0, 1.5));
		goalSelector.addGoal(4, new PanicGoal(this, 1.5));
		goalSelector.addGoal(5, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.0, 1.5));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		goalSelector.addGoal(7, WanderGoal.wanderALot(this, 10, 1.0));
		goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	public static boolean isWood(BlockState state) {
		return state.is(BlockTags.LOGS) || state.is(BlockTags.WOODEN_FENCES) || state.is(BlockTags.FENCE_GATES);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		if (random.nextInt(30) == 0 && getHealth() < getMaxHealth() || random.nextInt(350) == 1) {
			BlockPos wood = nearestWood(level);
			if (wood != null) {
				getNavigation().moveTo(wood.getX(), wood.getY(), wood.getZ(), 1.0);
				if (wood.distSqr(blockPosition().above()) < 12) {
					if (level.getGameRules().get(GameRules.MOB_GRIEFING)) fell(level, wood);
					heal(1.0F);
					level.playSound(null, getX(), getY(), getZ(), ModSounds.CHAINSAW, SoundSource.NEUTRAL, 1.0F, random.nextFloat() * 0.2F + 0.9F);
				}
			}
		}
		if (random.nextInt(200) == 1) {
			level.getEntitiesOfClass(Beaver.class, getBoundingBox().inflate(16.0, 6.0, 16.0), b -> b != this).stream()
				.min(Comparator.comparingDouble(this::distanceToSqr))
				.ifPresent(b -> getNavigation().moveTo(b.getX(), b.getY(), b.getZ(), 0.5));
		}
	}

	/** The nearest wood block, scanning shells 1-10 blocks out (at most 2 up or down), as OreSpawn did. */
	private @Nullable BlockPos nearestWood(ServerLevel level) {
		BlockPos c = blockPosition().above();
		for (int i = 1; i < 11; i++) {
			int dy = Math.min(i, 2);
			BlockPos best = null;
			int bestD = Integer.MAX_VALUE;
			for (BlockPos p : BlockPos.betweenClosed(c.offset(-i, -dy, -i), c.offset(i, dy, i))) {
				int ax = Math.abs(p.getX() - c.getX()), ay = Math.abs(p.getY() - c.getY()), az = Math.abs(p.getZ() - c.getZ());
				if (ax != i && az != i && ay != dy) continue;
				if (!isWood(level.getBlockState(p))) continue;
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

	/** Beaver.breakRecursor: breaks this wood block and every wood block connected to it, dropping each as an item. */
	private void fell(ServerLevel level, BlockPos start) {
		java.util.ArrayDeque<BlockPos> todo = new java.util.ArrayDeque<>();
		java.util.HashSet<BlockPos> seen = new java.util.HashSet<>();
		todo.add(start);
		seen.add(start);
		int broken = 0;
		while (!todo.isEmpty() && broken <= 200) {
			BlockPos p = todo.poll();
			BlockState state = level.getBlockState(p);
			if (!isWood(state)) continue;
			level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			broken++;
			ItemEntity item = new ItemEntity(level, getX() + random.nextInt(4) - random.nextInt(4), getY() + 4.0 + random.nextInt(4),
				getZ() + random.nextInt(4) - random.nextInt(4), new ItemStack(state.getBlock()));
			level.addFreshEntity(item);
			for (BlockPos n : BlockPos.betweenClosed(p.offset(-1, -1, -1), p.offset(1, 1, 1))) {
				if (seen.add(n.immutable())) todo.add(n.immutable());
			}
		}
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
	public boolean removeWhenFarAway(double distSqr) {
		return false;
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return orespawn.entity.Breeding.crystalApple(stack);
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return ModEntities.BEAVER.create(level, EntitySpawnReason.BREEDING);
	}

	/** Beaver.getCanSpawnHere: y 50 to 100, on dirt, grass, short grass or leaves. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (pos.getY() < 50 || pos.getY() > 100) return false;
		BlockState below = level.getBlockState(pos.below());
		return below.is(Blocks.DIRT) || below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.SHORT_GRASS) || below.is(BlockTags.LEAVES);
	}
}
