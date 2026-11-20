package orespawn.entity.terrors;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.phys.AABB;
import orespawn.entity.Butterfly;
import orespawn.entity.Firefly;
import orespawn.entity.Flight;
import orespawn.registry.ModSounds;

/**
 * OreSpawn's Lurking Terror: a big many-legged flyer that hangs about in dark places by day. It drifts between spots
 * 2-11 blocks away that it can see, and every few ticks goes for the nearest living thing within 12 blocks (not the
 * other terrors, nor butterflies, fireflies or creative players), biting it for 5 when it's close. While it's hunting
 * it won't despawn and its jaws snap. Falls don't hurt it. 30 health, 5 armor. Drops 0-2 (more with Looting) of one
 * of raw beef, flint or feathers.
 * <p>
 * Not ported: OreSpawn's fire resistance setting (no 26.3 equivalent), its spawner-name check and its Chaos dimension
 * odds (that dimension isn't ported). OreSpawn spawned it only in its Islands and Chaos dimensions.
 */
public class LurkingTerror extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(LurkingTerror.class, EntityDataSerializers.BOOLEAN);
	private BlockPos flightTarget;
	// OreSpawn's RenderInfo (client side): which legs move this cycle and whether the jaw snaps.
	private float legPhase;
	private float mouthPhase;
	private int legBits;
	private int jawBit;

	public LurkingTerror(EntityType<? extends LurkingTerror> type, Level level) {
		super(type, level);
		this.xpReward = 20;
		this.moveControl = TerrorAi.keepForward(this);
	}

	/** get_mobstats("LurkingTerror", 30, 6, 5); speed 0.25. Its bite is a flat 5, as in OreSpawn. */
	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ARMOR, 5.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
	}

	public boolean attacking() {
		return entityData.get(ATTACKING);
	}

	/**
	 * The model's random leg and jaw choices (OreSpawn's ModelLurkingTerror kept them per creature): re-rolled each time
	 * the leg (0.7 a tick) and jaw (0.9 a tick) cycles start over. Returns the six leg bits plus the jaw at bit 6.
	 */
	public int animBits(float ageInTicks) {
		float a = Math.abs(ageInTicks * 0.7F % (float) (Math.PI * 2));
		if (a < legPhase) {
			legBits = 0;
			if (random.nextInt(3) == 1) legBits |= 1;
			if (random.nextInt(3) == 1) legBits |= 2;
			if (random.nextInt(4) == 1) legBits |= 4;
			if (random.nextInt(4) == 1) legBits |= 8;
			if (random.nextInt(6) == 1) legBits |= 16;
			if (random.nextInt(6) == 1) legBits |= 32;
		}
		legPhase = a;
		a = Math.abs(ageInTicks * 0.9F % (float) (Math.PI * 2));
		if (a < mouthPhase) {
			jawBit = random.nextInt(20) == 1 ? 1 : 0;
			if (attacking()) jawBit = 1;
		}
		mouthPhase = a;
		return legBits | jawBit << 6;
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
		if (random.nextInt(120) != 0 && Flight.distSq(this, flightTarget) >= 2.1F) {
			if (random.nextInt(9) == 0) {
				LivingEntity e = TerrorAi.findTarget(this, 12.0, 8.0, this::isSuitableTarget);
				if (e != null) {
					entityData.set(ATTACKING, true);
					flightTarget = BlockPos.containing(e.getX(), e.getY() + 1.0, e.getZ());
					if (distanceToSqr(e) < 6.0) doHurtTarget(level, e);
				} else {
					entityData.set(ATTACKING, false);
				}
			}
		} else {
			flightTarget = TerrorAi.newFlightTarget(this, 2, 10, 5, 2);
		}
		Flight.steer(this, flightTarget, 0.4, 0.30000000149011613, 0.20000000149011612, 0.4, 0.75F, 0.25F);
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (!getSensing().hasLineOfSight(e)) return false;
		if (e instanceof LurkingTerror || e instanceof TerribleTerror || e instanceof CreepingHorror || e instanceof PitchBlack
			|| e instanceof Butterfly || e instanceof Firefly) {
			return false;
		}
		if (TerrorAi.isOreSpawn(e, TerribleTerror.FRIENDS)) return false;
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
		return ModSounds.get("lurkinghorror_living");
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("lurkinghorror_hit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("lurkinghorror_dead");
	}

	@Override
	protected float getSoundVolume() {
		return 0.55F;
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
		TerrorAi.dropFew(this, level, source, i == 0 ? Items.BEEF : i == 1 ? Items.FLINT : Items.FEATHER);
	}

	/** canDespawn: not while it's hunting. */
	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !attacking();
	}

	/**
	 * LurkingTerror.getCanSpawnHere: somewhere dark, but by day, half the time, at y 10 or higher, and with no other
	 * Lurking Terror within 32 blocks.
	 */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!Monster.isDarkEnoughToSpawn(level, pos, random)) return false;
		if (!level.getLevel().isBrightOutside() || random.nextInt(2) != 1) return false;
		if (!level.getEntitiesOfClass(LurkingTerror.class, new AABB(pos).inflate(32.0, 16.0, 32.0)).isEmpty()) return false;
		return pos.getY() >= 10;
	}
}
