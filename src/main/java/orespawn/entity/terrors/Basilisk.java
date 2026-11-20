package orespawn.entity.terrors;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import orespawn.content.Terrors;
import orespawn.entity.Ignored;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * OreSpawn's Basilisk: a huge serpent of the jungles and dark forests, out at night. Its gaze slows whatever it fixes
 * on (Slowness VI): every few ticks it picks the nearest living thing within 24 blocks (not other basilisks, nor the
 * small things hunters ignore, nor creative players), closes in and bites when it's within 6 blocks, knocking it back
 * and often poisoning it. After each hit it shrugs off damage for a second and a half. Fire can't hurt it and it heals
 * over time. 200 health, 24 damage, 15 armor.
 * <p>
 * Drops a Basilisk Scale, an item frame, 12-17 emeralds, 8-12 raw chicken and 3-7 rolls of OreSpawn's treasure table
 * (emeralds, emerald blocks, and emerald tools and armor that are often enchanted).
 * <p>
 * Not ported: OreSpawn's fire resistance setting (no 26.3 equivalent; it's fire immune anyway) and its spawner-name
 * check. The Basilisk Maze it guards is the structures group's.
 */
public class Basilisk extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Basilisk.class, EntityDataSerializers.BOOLEAN);
	private static final Set<String> FRIENDS = Set.of("leaf_monster");
	private int hurtTimer;

	public Basilisk(EntityType<? extends Basilisk> type, Level level) {
		super(type, level);
		this.xpReward = 150;
	}

	/** get_mobstats("Basilisk", 200, 24, 15); speed 0.4. */
	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.MOVEMENT_SPEED, 0.4)
			.add(Attributes.ATTACK_DAMAGE, 24.0).add(Attributes.ARMOR, 15.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
		goalSelector.addGoal(2, WanderGoal.wanderALot(this, 20, 1.0));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
	}

	public boolean attacking() {
		return entityData.get(ATTACKING);
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!level().isClientSide() && isAlive() && random.nextInt(200) == 0) heal(1.0F);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		return InteractionResult.PASS;
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (!super.doHurtTarget(level, target)) return false;
		if (target instanceof LivingEntity living) {
			int seconds = 8;
			if (level.getDifficulty() == Difficulty.EASY) seconds = 10;
			if (level.getDifficulty() == Difficulty.NORMAL) {
				seconds = 12;
			} else if (level.getDifficulty() == Difficulty.HARD) {
				seconds = 14;
			}
			if (random.nextInt(3) == 0) living.addEffect(new MobEffectInstance(MobEffects.POISON, seconds * 20, 0));
			TerrorAi.fling(this, target, 1.5, 0.15);
		}
		return true;
	}

	/** After each hit it ignores damage for 30 ticks. */
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (hurtTimer > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
		hurtTimer = 30;
		return super.hurtServer(level, source, damage);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (hurtTimer > 0) hurtTimer--;
		if (random.nextInt(5) == 0) {
			LivingEntity e = TerrorAi.findTarget(this, 24.0, 7.0, this::isSuitableTarget);
			if (e != null) {
				lookAt(e, 10.0F, 10.0F);
				double reach = 6.0F + e.getBbWidth() / 2.0F;
				if (distanceToSqr(e) < reach * reach) {
					entityData.set(ATTACKING, true);
					if (random.nextInt(3) == 0 || random.nextInt(4) == 1) doHurtTarget(level, e);
				} else {
					getNavigation().moveTo(e, 1.25);
				}
				e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 5));
			} else {
				entityData.set(ATTACKING, false);
			}
		}
		if (random.nextInt(75) == 1 && getHealth() < getMaxHealth()) heal(1.0F);
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (Ignored.byHunters(e) || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Basilisk || TerrorAi.isOreSpawn(e, FRIENDS)) return false;
		return !TerrorAi.protectedPlayer(e);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return random.nextInt(2) == 0 ? ModSounds.get("basilisk_living") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("alo_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("emperorscorpion_death");
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
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		// Basilisk.dropFewItems (randomly enchanted gear, so not a loot table).
		drop(level, Terrors.BASILISK_SCALE);
		drop(level, Items.ITEM_FRAME);
		int n = 12 + random.nextInt(6);
		for (int i = 0; i < n; i++) drop(level, Items.EMERALD);
		n = 8 + random.nextInt(5);
		for (int i = 0; i < n; i++) drop(level, Items.CHICKEN);
		n = 3 + random.nextInt(5);
		for (int i = 0; i < n; i++) {
			int roll = random.nextInt(15);
			if (roll == 1) drop(level, Items.EMERALD);
			else if (roll == 2) drop(level, Items.EMERALD_BLOCK);
			else TerrorAi.dropAround(this, level, TerrorAi.treasureGear(level, random, "emerald", roll), 4, 1.0F);
		}
	}

	private void drop(ServerLevel level, Item item) {
		TerrorAi.dropAround(this, level, new ItemStack(item), 4, 1.0F);
	}

	/** Basilisk.getCanSpawnHere: dark enough, at night, a 3 x 3 x 4 space of air above it, no other Basilisk within 20 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!Monster.isDarkEnoughToSpawn(level, pos, random) || level.getLevel().isBrightOutside()) return false;
		if (!TerrorAi.airAbove(level, pos, -1, 1, -1, 1, 1, 4)) return false;
		return level.getEntitiesOfClass(Basilisk.class, new AABB(pos).inflate(20.0, 6.0, 20.0)).isEmpty();
	}
}
