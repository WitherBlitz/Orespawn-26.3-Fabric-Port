package orespawn.entity.terrors;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import orespawn.content.Terrors;
import orespawn.entity.Ignored;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's "WTF?" (GammaMetroid): a floating shelled thing from the deep that eats stone. Now and then (more often
 * when hurt) it finds the nearest stone within 5 blocks, goes to it and, when it's close, eats it (if mob griefing is on)
 * and heals. Wild ones go for the nearest living thing within 10 blocks that isn't a monster (nor the small things
 * hunters ignore, nor creative players), except in peaceful, biting for 10. 100 health, 12 armor.
 * <p>
 * Tameable: give it an iron ingot (within 5 blocks): a wild one is tamed and healed 1 time in 3; your own is healed. A
 * tamed one never attacks and follows its owner; click your own with anything else to make it sit or stand; give it a
 * dead bush to set it free. Iron ingots tempt it. Drops 5-14 gold nuggets and 6-15 iron ingots (loot table).
 * <p>
 * Breeds with OreSpawn's Crystal Apple. Not ported: its fire resistance setting (no 26.3 equivalent), its spawner-name
 * check and its Islands dimension exception.
 * OreSpawn spawned it only in its Extreme (mining) and Chaos dimensions.
 */
public class GammaMetroid extends TamableAnimal {
	public GammaMetroid(EntityType<? extends GammaMetroid> type, Level level) {
		super(type, level);
		this.xpReward = 20;
	}

	/** get_mobstats("GammaMetroid", 100, 10, 12); speed 0.15. */
	public static AttributeSupplier.Builder createAttributes() {
		return TamableAnimal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 100.0).add(Attributes.MOVEMENT_SPEED, 0.15)
			.add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, 12.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new BreedGoal(this, 1.0));
		goalSelector.addGoal(2, new FollowOwnerGoal(this, 2.0, 10.0F, 2.0F));
		goalSelector.addGoal(3, new TemptGoal(this, 1.2F, s -> s.is(Items.IRON_INGOT), false));
		goalSelector.addGoal(4, WanderGoal.wanderALot(this, 16, 1.0));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		return target.hurtServer(level, damageSources().mobAttack(this), 10.0F);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		InteractionResult result = super.mobInteract(player, hand);
		if (result.consumesAction()) return result;
		if (stack.is(Items.IRON_INGOT) && player.distanceToSqr(this) < 25.0) {
			if (!level().isClientSide()) {
				if (!isTame()) {
					if (random.nextInt(3) == 0) {
						tame(player);
						heal(getMaxHealth() - getHealth());
						level().broadcastEntityEvent(this, (byte) 7);
					} else {
						level().broadcastEntityEvent(this, (byte) 6);
					}
				} else if (isOwnedBy(player) && getHealth() < getMaxHealth()) {
					heal(getMaxHealth() - getHealth());
				}
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		if (isTame() && stack.is(Items.DEAD_BUSH) && player.distanceToSqr(this) < 25.0 && isOwnedBy(player)) {
			if (!level().isClientSide()) {
				setTame(false, true);
				setOwner(null);
				setOrderedToSit(false);
				level().broadcastEntityEvent(this, (byte) 6);
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		if (isTame() && isOwnedBy(player) && player.distanceToSqr(this) < 25.0 && !stack.is(Items.NAME_TAG)) {
			if (!level().isClientSide()) setOrderedToSit(!isOrderedToSit());
			return InteractionResult.SUCCESS;
		}
		return result;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		eatStone(level);
		if (level.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(5) == 0) {
			LivingEntity e = findSomethingToAttack();
			if (e != null) {
				lookAt(e, 10.0F, 10.0F);
				if (distanceToSqr(e) <= 9.0) {
					if (random.nextInt(4) == 0 || random.nextInt(5) == 1) doHurtTarget(level, e);
				} else {
					getNavigation().moveTo(e, 1.25);
				}
			}
		}
	}

	/** updateAITick: looks for stone in shells of 1 to 5 blocks around it, goes there, and eats it when it's close. */
	private void eatStone(ServerLevel level) {
		if (!((random.nextInt(20) == 0 && getHealth() < getMaxHealth()) || random.nextInt(100) == 0) || isOrderedToSit()) return;
		int[] best = {0, 0, 0, 99999};
		int x = getBlockX(), y = getBlockY() + 1, z = getBlockZ();
		for (int i = 1; i < 6; i++) {
			int j = Math.min(i, 2);
			if (TerrorAi.scanShell(level, x, y, z, i, j, i, s -> s.is(Blocks.STONE), best)) break;
			if (i >= 4) i++;
		}
		if (best[3] < 99999) {
			getNavigation().moveTo(best[0], best[1], best[2], 1.0);
			if (best[3] < 12) {
				if (level.getGameRules().get(GameRules.MOB_GRIEFING)) {
					level.setBlock(new BlockPos(best[0], best[1], best[2]), Blocks.AIR.defaultBlockState(), 2);
				}
				heal(1.0F);
				playSound(SoundEvents.PLAYER_BURP, 0.5F, random.nextFloat() * 0.2F + 1.5F);
			}
		}
	}

	private @Nullable LivingEntity findSomethingToAttack() {
		if (isBaby() || isTame()) return null;
		return TerrorAi.findTarget(this, 10.0, 3.0, e -> {
			if (Ignored.byHunters(e) || !getSensing().hasLineOfSight(e)) return false;
			if (e instanceof GammaMetroid || e instanceof Monster) return false;
			return !TerrorAi.protectedPlayer(e);
		});
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return random.nextInt(5) == 1 ? ModSounds.get("wtf_living") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("duck_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("alo_death");
	}

	@Override
	protected float getSoundVolume() {
		return 1.5F;
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
		return !isTame();
	}

	/** OreSpawn bred them with its Crystal Apple, which isn't ported yet. */
	@Override
	public boolean isFood(ItemStack stack) {
		return orespawn.entity.Breeding.crystalApple(stack);
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		GammaMetroid baby = Terrors.WTF.create(level, EntitySpawnReason.BREEDING);
		if (baby != null && isTame() && getOwner() instanceof Player owner) baby.tame(owner);
		return baby;
	}

	/** GammaMetroid.getCanSpawnHere: dark enough, at y 50 or lower, with a 2 x 2 x 3 space of air above it. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!Monster.isDarkEnoughToSpawn(level, pos, random)) return false;
		if (pos.getY() > 50) return false;
		return TerrorAi.airAbove(level, pos, -1, 0, -1, 0, 1, 3);
	}
}
