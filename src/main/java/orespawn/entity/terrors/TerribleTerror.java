package orespawn.entity.terrors;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.entity.Butterfly;
import orespawn.entity.Firefly;
import orespawn.entity.Flight;
import orespawn.registry.ModSounds;

import java.util.Set;

/**
 * OreSpawn's Terrible Terror: a little flying dragon of the night. It flits between spots 5-9 blocks away that it can
 * see, and every few ticks dives at the nearest living thing within 12 blocks (not the other terrors, nor butterflies,
 * fireflies or creative players), biting it for 5 when it's close. Falls don't hurt it. 10 health, 3 armor.
 * Drops 0-2 (more with Looting) of one of rotten flesh, emeralds or feathers.
 * <p>
 * Not ported: OreSpawn's fire resistance setting (no 26.3 equivalent), its spawner-name check and its Chaos dimension
 * exception (that dimension isn't ported). OreSpawn spawned it only in its Islands and Chaos dimensions.
 */
public class TerribleTerror extends Monster {
	/**
	 * Creatures from other groups OreSpawn's terrors leave alone: RockBase, EnderReaper, Mothra, CloudShark, Rotator, Bee,
	 * Mantis, LeafMonster, Triffid, Dragon, Island, IslandToo.
	 */
	static final Set<String> FRIENDS = Set.of("rock", "ender_reaper", "mothra", "cloud_shark", "rotator", "bee", "mantis", "leaf_monster",
		"triffid", "dragon", "island", "island_too", "islandtoo");
	private BlockPos flightTarget;

	public TerribleTerror(EntityType<? extends TerribleTerror> type, Level level) {
		super(type, level);
		this.xpReward = 10;
		this.moveControl = TerrorAi.keepForward(this);
	}

	/** get_mobstats("TerribleTerror", 10, 5, 3); speed 0.1. */
	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.1)
			.add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.ARMOR, 3.0);
	}

	@Override
	public void tick() {
		super.tick();
		setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6, 1.0));
	}

	/** OreSpawn's terrors bite for a flat 5. */
	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		return target.hurtServer(level, damageSources().mobAttack(this), 5.0F);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (flightTarget == null) flightTarget = blockPosition();
		if (random.nextInt(100) != 0 && Flight.distSq(this, flightTarget) >= 2.1F) {
			if (random.nextInt(9) == 0) {
				LivingEntity e = TerrorAi.findTarget(this, 12.0, 8.0, this::isSuitableTarget);
				if (e != null) {
					flightTarget = BlockPos.containing(e.getX(), e.getY() + 1.0, e.getZ());
					if (distanceToSqr(e) < 6.0) doHurtTarget(level, e);
				}
			}
		} else {
			flightTarget = TerrorAi.newFlightTarget(this, 5, 5, 5, 2);
		}
		Flight.steer(this, flightTarget, 0.4, 0.30000000149011613, 0.20000000149011612, 0.4, 0.75F, 0.25F);
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (!getSensing().hasLineOfSight(e)) return false;
		if (e instanceof TerribleTerror || e instanceof LurkingTerror || e instanceof CreepingHorror || e instanceof PitchBlack
			|| e instanceof Butterfly || e instanceof Firefly) {
			return false;
		}
		if (TerrorAi.isOreSpawn(e, FRIENDS)) return false;
		return !TerrorAi.protectedPlayer(e);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		boolean hurt = super.hurtServer(level, source, damage);
		Entity e = source.getEntity();
		if (e != null && flightTarget != null) flightTarget = e.blockPosition();
		return hurt;
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.get("terribleterror_living");
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("terribleterror_hit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("terribleterror_dead");
	}

	@Override
	protected float getSoundVolume() {
		return 0.45F;
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
		TerrorAi.dropFew(this, level, source, i == 0 ? Items.ROTTEN_FLESH : i == 1 ? Items.EMERALD : Items.FEATHER);
	}

	/** canDespawn: only by day. */
	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return level().isBrightOutside();
	}

	/** TerribleTerror.getCanSpawnHere: dark enough, at night, at y 40 or lower. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!Monster.isDarkEnoughToSpawn(level, pos, random)) return false;
		return !level.getLevel().isBrightOutside() && pos.getY() <= 40;
	}
}
