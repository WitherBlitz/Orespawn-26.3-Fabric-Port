package orespawn.entity.sea;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Hammerhead: a huge, armoured land beast (240 health, 20 armour) with a hammer-shaped head. It hunts players,
 * monsters and OreSpawn's other big creatures, royalty, pets and villagers within 18 blocks, remembers whoever last hurt
 * it, and rams anything within 7 blocks (plus half the target's width) for 75 damage, flinging it away (players and
 * the dead twice as high); its head bobs while it fights. Cacti can't hurt it. It avoids water and wanders through
 * villages. 350 xp. On death it scatters 8 bottles o' enchanting, 6 raw beef, and OreSpawn's 10 Experience Orb
 * Catchers, 16 Creeper Launchers, 4 Creeper Repellents, 2 Experience Tree Seeds and 1 time in 3 the Attitude Adjuster
 * (those come with the groups that port them). Natural spawns: only in OreSpawn's Chaos dimension (its monsters,
 * weight 1), which isn't ported here.
 */
public class Hammerhead extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Hammerhead.class, EntityDataSerializers.BOOLEAN);
	/** The last thing that hurt it. */
	private @Nullable LivingEntity revenge;

	public Hammerhead(EntityType<? extends Hammerhead> type, Level level) {
		super(type, level);
		this.xpReward = 350;
	}

	public static AttributeSupplier.Builder createAttributes() {
		// get_mobstats "Hammerhead": 240 health, 75 attack, 20 defense.
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 240.0).add(Attributes.MOVEMENT_SPEED, 0.35)
			.add(Attributes.ATTACK_DAMAGE, 75.0).add(Attributes.ARMOR, 20.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
		goalSelector.addGoal(2, WanderGoal.wanderALot(this, 16, 1.0));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Mob.class, 8.0F));
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
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(3) != 1) return;
		LivingEntity e = revenge;
		if (e != null) {
			if (!e.isAlive() || random.nextInt(250) == 1) {
				e = null;
				revenge = null;
			}
			if (e != null && !getSensing().hasLineOfSight(e)) e = null;
		}
		if (e == null) e = SeaTargets.nearest(this, 18.0, 9.0, this::isSuitableTarget);
		if (e != null) {
			lookAt(e, 10.0F, 10.0F);
			double reach = 7.0F + e.getBbWidth() / 2.0F;
			if (distanceToSqr(e) < reach * reach) {
				entityData.set(ATTACKING, true);
				if (random.nextInt(3) == 1 || random.nextInt(4) == 1) doHurtTarget(level, e);
			} else {
				getNavigation().moveTo(e, 1.25);
			}
		} else {
			entityData.set(ATTACKING, false);
		}
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (!SeaTargets.basic(this, e) || e instanceof Hammerhead) return false;
		if (e instanceof Player p) return !p.isCreative() && !p.isSpectator();
		return e instanceof Monster || SeaTargets.attackableNonMob(e);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (!super.doHurtTarget(level, target)) return false;
		if (target instanceof LivingEntity) {
			double ks = 1.1, inair = 0.85;
			float f3 = (float) Math.atan2(target.getZ() - getZ(), target.getX() - getX());
			if (!target.isAlive() || target instanceof Player) inair *= 2.0;
			target.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
		}
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypes.CACTUS)) return false;
		boolean hurt = super.hurtServer(level, source, damage);
		if (source.getEntity() instanceof LivingEntity e) revenge = e;
		return hurt;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		drop(level, Items.EXPERIENCE_BOTTLE, 8);
		drop(level, ModItems.lazy("experience_orb_catcher"), 10);
		drop(level, ModItems.lazy("creeper_launcher"), 16);
		drop(level, ModItems.lazy("creeper_repellent"), 4);
		drop(level, Items.BEEF, 6);
		drop(level, ModItems.lazy("experience_tree_seed"), 2);
		if (random.nextInt(3) == 1) drop(level, ModItems.lazy("attitude_adjuster"), 1);
	}

	/** Hammerhead.dropItemRand, n times: up to 4 blocks off in x and z, 2 blocks up. Nothing for an item not ported yet. */
	private void drop(ServerLevel level, ItemLike item, int n) {
		if (item.asItem() == Items.AIR) return;
		for (int i = 0; i < n; i++) SeaLoot.drop(this, level, new ItemStack(item), 5, 2.0);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return random.nextInt(3) == 0 ? ModSounds.get("hammerhead_living") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("alo_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("hammerhead_death");
	}

	@Override
	protected float getSoundVolume() {
		return 1.2F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/**
	 * Hammerhead.getCanSpawnHere: at night, dark enough for a monster, at y 50 or higher, with 5 blocks of air above a
	 * 2x2 patch, and no other Hammerhead within 16 blocks.
	 */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!Monster.isDarkEnoughToSpawn(level, pos, random) || pos.getY() < 50 || level.getLevel().isBrightOutside()) return false;
		for (int k = -1; k < 1; k++) {
			for (int j = -1; j < 1; j++) {
				for (int i = 1; i < 6; i++) {
					if (!level.getBlockState(pos.offset(j, i, k)).isAir()) return false;
				}
			}
		}
		return level.getEntitiesOfClass(Hammerhead.class, new AABB(pos).inflate(16.0, 8.0, 16.0)).isEmpty();
	}
}
