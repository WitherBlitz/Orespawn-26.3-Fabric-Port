package orespawn.entity.terrors;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import orespawn.entity.Firefly;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;

import java.util.Set;

/**
 * OreSpawn's Creeping Horror: a small crawling night thing of the deep. Now and then it looks for the nearest living
 * thing within 16 blocks (not its own kind, the other terrors, fireflies or creative players), crawls after it and
 * bites. 10 health, 3 damage, 2 armor. It panics when hurt, and by day it may simply vanish (unless it's persistent).
 * Drops 0-2 (more with Looting) of one of rotten flesh, bones or string.
 * <p>
 * Not ported: OreSpawn's fire resistance setting (no 26.3 equivalent), its spawner-name check and its Chaos dimension
 * exception (that dimension isn't ported). OreSpawn spawned it only in its Islands and Chaos dimensions.
 */
public class CreepingHorror extends Monster {
	/** Creatures from other groups that OreSpawn's Creeping Horror left alone (RockBase, EnderReaper, LeafMonster, Dragon, Island, IslandToo). */
	private static final Set<String> FRIENDS = Set.of("rock", "ender_reaper", "leaf_monster", "dragon", "island", "island_too", "islandtoo");

	public CreepingHorror(EntityType<? extends CreepingHorror> type, Level level) {
		super(type, level);
		this.xpReward = 5;
	}

	/** get_mobstats("CreepingHorror", 10, 3, 2). */
	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.ARMOR, 2.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new PanicGoal(this, 1.35F));
		goalSelector.addGoal(2, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
		goalSelector.addGoal(3, WanderGoal.wanderALot(this, 10, 1.0));
		goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	public void tick() {
		super.tick();
		// By day (time of day up to 11000) a Creeping Horror that isn't persistent vanishes, 1 tick in 500.
		if (!level().isClientSide() && !isPersistenceRequired() && level().getOverworldClockTime() % 24000L <= 11000L
			&& random.nextInt(500) == 1) {
			discard();
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		if (random.nextInt(5) == 1) {
			LivingEntity e = TerrorAi.findTarget(this, 16.0, 4.0, this::isSuitableTarget);
			if (e != null) {
				getNavigation().moveTo(e, 1.25);
				if (distanceToSqr(e) < 5.0 && (random.nextInt(12) == 0 || random.nextInt(14) == 1)) doHurtTarget(level, e);
			}
		}
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (!getSensing().hasLineOfSight(e)) return false;
		if (e instanceof CreepingHorror || e instanceof TerribleTerror || e instanceof LurkingTerror || e instanceof PitchBlack
			|| e instanceof Firefly) {
			return false;
		}
		if (TerrorAi.isOreSpawn(e, FRIENDS)) return false;
		return !TerrorAi.protectedPlayer(e);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.get("creepinghorror_living");
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("creepinghorror_hit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("creepinghorror_dead");
	}

	@Override
	protected float getSoundVolume() {
		return 0.65F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		// getDropItem picks one of three, then dropFewItems drops 0-2 of it (no loot table: one roll picks the item).
		int i = random.nextInt(3);
		TerrorAi.dropFew(this, level, source, i == 0 ? Items.ROTTEN_FLESH : i == 1 ? Items.BONE : Items.STRING);
	}

	/** canDespawn: only by day. */
	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return level().isBrightOutside();
	}

	/** CreepingHorror.getCanSpawnHere: dark enough, at night, at y 15 or lower. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!Monster.isDarkEnoughToSpawn(level, pos, random)) return false;
		return !level.getLevel().isBrightOutside() && pos.getY() <= 15;
	}
}
