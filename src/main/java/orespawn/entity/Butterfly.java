package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import orespawn.world.OreSpawnDimensions;
import orespawn.world.OreSpawnTeleporter;

/**
 * OreSpawn's butterfly (EntityButterfly): four wing patterns, silent, flits from air block to air block by day.
 * Right-clicking one (or a moth) with an empty hand takes you to OreSpawn's Chaos dimension, or back from it. In the
 * Islands dimension, type-1 butterflies are drawn differently (vbutterfly1) and sting players and horses.
 */
public class Butterfly extends AmbientCreature {
	private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(Butterfly.class, EntityDataSerializers.INT);
	protected BlockPos flightTarget;

	/** EntityButterfly.interact: only with nothing in the main hand. */
	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) return super.mobInteract(player, hand);
		if (player instanceof ServerPlayer serverPlayer) OreSpawnTeleporter.travel(serverPlayer, OreSpawnDimensions.CHAOS);
		return InteractionResult.CONSUME;
	}

	public Butterfly(EntityType<? extends Butterfly> type, Level level) {
		super(type, level);
		this.moveControl = Flight.keepOwnSteering(this);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 2.0).add(Attributes.MOVEMENT_SPEED, 0.1).add(Attributes.ATTACK_DAMAGE, 0.0);
	}

	/** How many patterns this kind has (the moth overrides it). */
	protected int variants() {
		return 4;
	}

	public int variant() {
		return entityData.get(TYPE);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(TYPE, 0);
	}

	@Override
	public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
			@Nullable SpawnGroupData data) {
		entityData.set(TYPE, random.nextInt(variants()));
		return super.finalizeSpawn(level, difficulty, reason, data);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("ButterflyType", variant());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		entityData.set(TYPE, input.getIntOr("ButterflyType", 0));
	}

	@Override
	protected float getSoundVolume() {
		return 0.0F;
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	protected void pushEntities() {
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isPersistenceRequired();
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	/** How far a new flight target may be (7 for butterflies, 10 for moths). */
	protected int spread() {
		return 7;
	}

	/** A place to fly to other than a random one (the moth's torch); null for none. */
	protected @Nullable BlockPos attraction(ServerLevel level) {
		return null;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (flightTarget == null) flightTarget = blockPosition();
		if (random.nextInt(100) != 0 && !(Flight.distSq(this, flightTarget) < 4.0)) {
			BlockPos lure = attraction(level);
			if (lure != null) flightTarget = lure;
			if (stings(level) && random.nextInt(10) == 0) {
				LivingEntity target = findSomethingToSting(level);
				if (target != null) {
					flightTarget = BlockPos.containing(target.getX(), target.getY() + 1.0, target.getZ());
					if (distanceToSqr(target) < 6.0 && random.nextInt(2) == 0) target.hurtServer(level, damageSources().mobAttack(this), 1.0F);
				}
			}
		} else {
			flightTarget = Flight.pickAir(this, spread(), 25);
		}
		Flight.steer(this, flightTarget, 0.5, 0.1, 0.1, 0.5, 0.5F, 1.0F);
	}

	/** EntityButterfly: only type-1 butterflies (not moths) in the Islands dimension, and not on Peaceful. */
	private boolean stings(ServerLevel level) {
		return !(this instanceof Moth) && variant() == 1 && level.dimension() == OreSpawnDimensions.ISLANDS
			&& level.getDifficulty() != Difficulty.PEACEFUL;
	}

	/** findSomethingToAttack: the nearest visible non-creative player or horse within 8 x 5 x 8. */
	private @Nullable LivingEntity findSomethingToSting(ServerLevel level) {
		LivingEntity best = null;
		double bestDist = Double.MAX_VALUE;
		for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(8.0, 5.0, 8.0))) {
			boolean prey = e instanceof Player p ? !p.isCreative() && !p.isSpectator() : e instanceof AbstractHorse;
			if (!prey || e == this || !e.isAlive() || !getSensing().hasLineOfSight(e)) continue;
			double d = distanceToSqr(e);
			if (d < bestDist) {
				bestDist = d;
				best = e;
			}
		}
		return best;
	}

	@Override
	public void tick() {
		super.tick();
		setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6, 1.0));
	}

	/** EntityButterfly.getCanSpawnHere: in air, by day, at y 50 or higher (any height in the Islands dimension). */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return level.isEmptyBlock(pos) && level.getLevel().isBrightOutside()
			&& (pos.getY() >= 50 || level.getLevel().dimension() == OreSpawnDimensions.ISLANDS);
	}
}
