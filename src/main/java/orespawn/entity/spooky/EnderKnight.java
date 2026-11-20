package orespawn.entity.spooky;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import orespawn.entity.WanderGoal;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Ender Knight: an armoured, sword-carrying enderman. Like an enderman it turns on a player who looks it in
 * the eye (unless they wear a carved pumpkin), screaming and swinging its sword; it teleports away when stared at from
 * close by, chases a far target by teleporting towards it, dodges arrows and other projectiles by teleporting, and is
 * hurt by water and rain. 60 health, 12 damage, 6 armor. Drops 0-1 (more with Looting) Eyes of Ender or Ender Pearls.
 * Spawns at night in the dark, at y 30 or higher.
 * <p>
 * OreSpawn ran it on 1.7.10's old mob AI, which walked a mob at a tenth of its speed attribute and, once it had a
 * target, at that AI's top speed (OreSpawn's +6.2 "attacking speed boost" did that). Here it keeps OreSpawn's speed
 * attribute and its goals move it at those same two speeds. Like that old AI it keeps a target until the target dies or
 * turns creative.
 */
public class EnderKnight extends Monster {
	private static final EntityDataAccessor<Boolean> SCREAMING = SynchedEntityData.defineId(EnderKnight.class, EntityDataSerializers.BOOLEAN);
	/** 1.7.10's old AI top speed, as 26.3's speed (its per-tick push was 0.1; 26.3's is speed squared). */
	private static final double OLD_AI_TOP_SPEED = Math.sqrt(0.1);
	private int teleportDelay;
	private int stareTimer;

	public EnderKnight(EntityType<? extends EnderKnight> type, Level level) {
		super(type, level);
		this.xpReward = 5;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return attributes(60.0, 0.32, 12.0, 6.0);
	}

	static AttributeSupplier.Builder attributes(double health, double speed, double attack, double armor) {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, health).add(Attributes.MOVEMENT_SPEED, speed)
			.add(Attributes.ATTACK_DAMAGE, attack).add(Attributes.ARMOR, armor).add(Attributes.STEP_HEIGHT, 1.0);
	}

	/** How far away a staring player is noticed (64 for the knight, 81 for the reaper). */
	protected double stareRange() {
		return 64.0;
	}

	@Override
	protected void registerGoals() {
		double speed = getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MeleeAttackGoal(this, OLD_AI_TOP_SPEED / speed, true));
		// The old AI's wandering: a push of a tenth of the speed attribute, so a 26.3 speed of sqrt(speed / 10).
		goalSelector.addGoal(2, new WanderGoal(this, Math.sqrt(0.1 * speed) / speed, 72, 6));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(4, new RandomLookAroundGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(SCREAMING, false);
	}

	public boolean isScreaming() {
		return entityData.get(SCREAMING);
	}

	public void setScreaming(boolean screaming) {
		entityData.set(SCREAMING, screaming);
	}

	/** EnderKnight.shouldAttackPlayer: the player looks straight at it, without a pumpkin on, and can see it. */
	private boolean isStaredAtBy(Player player) {
		if (!LivingEntity.PLAYER_NOT_WEARING_DISGUISE_ITEM.test(player)) return false;
		return isLookingAtMe(player, 0.025, true, false, getY(0.5));
	}

	@Override
	public boolean isSensitiveToWater() {
		return true;
	}

	@Override
	public void aiStep() {
		Level level = level();
		if (level.isClientSide()) {
			for (int i = 0; i < 2; i++) {
				level.addParticle(ParticleTypes.PORTAL, getRandomX(0.5), getRandomY() - 0.25, getRandomZ(0.5), (random.nextDouble() - 0.5) * 2.0,
					-random.nextDouble(), (random.nextDouble() - 0.5) * 2.0);
			}
		} else {
			ServerLevel server = (ServerLevel) level;
			if (server.isBrightOutside()) {
				float light = getLightLevelDependentMagicValue();
				if (light > 0.5F && server.canSeeSky(blockPosition()) && random.nextFloat() * 30.0F < (light - 0.4F) * 2.0F) {
					setTarget(null);
					setScreaming(false);
					teleportRandomly();
				}
			}
			if (isInWaterOrRain() || isOnFire()) {
				setScreaming(false);
				teleportRandomly();
			}
			LivingEntity target = getTarget();
			if (target != null) lookAt(target, 100.0F, 100.0F);
			if (isAlive()) {
				if (target != null) {
					if (target instanceof Player player && isStaredAtBy(player)) {
						if (target.distanceToSqr(this) < 16.0) teleportRandomly();
						teleportDelay = 0;
					} else if (target.distanceToSqr(this) > 256.0 && teleportDelay++ >= 30 && teleportTowards(target)) {
						teleportDelay = 0;
					}
				} else {
					setScreaming(false);
					teleportDelay = 0;
				}
			}
		}
		this.jumping = false;
		super.aiStep();
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		LivingEntity target = getTarget();
		if (target != null && (!target.isAlive() || SpookyHelpers.untouchable(target))) {
			setTarget(null);
			target = null;
		}
		if (target == null) {
			// EnderKnight.findPlayerToAttack: the nearest player who could be attacked, if they're staring at it.
			Player player = level.getNearestPlayer(TargetingConditions.forCombat().range(stareRange()).ignoreLineOfSight(), this);
			if (player != null) {
				if (isStaredAtBy(player)) {
					if (stareTimer == 0) {
						level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_STARE, getSoundSource(), 1.0F, 1.0F);
					}
					if (stareTimer++ == 5) stareTimer = 0;
					setScreaming(true);
					setTarget(player);
				} else {
					stareTimer = 0;
					setScreaming(false);
				}
			}
		}
	}

	protected boolean teleportRandomly() {
		double x = getX() + (random.nextDouble() - 0.5) * 64.0;
		double y = getY() + (random.nextInt(64) - 32);
		double z = getZ() + (random.nextDouble() - 0.5) * 64.0;
		return teleportToSpot(x, y, z);
	}

	protected boolean teleportTowards(Entity e) {
		// OreSpawn's (and 1.7.10's) aim, eye height added rather than taken away.
		Vec3 dir = new Vec3(getX() - e.getX(), getY(0.5) - e.getY() + e.getEyeHeight(), getZ() - e.getZ()).normalize();
		double x = getX() + (random.nextDouble() - 0.5) * 8.0 - dir.x * 16.0;
		double y = getY() + (random.nextInt(16) - 8) - dir.y * 16.0;
		double z = getZ() + (random.nextDouble() - 0.5) * 8.0 - dir.z * 16.0;
		return teleportToSpot(x, y, z);
	}

	/** Drops to the ground below the spot, if there's room and no liquid there; portal particles and sounds at both ends. */
	protected boolean teleportToSpot(double x, double y, double z) {
		if (isPassenger()) return false;
		double ox = getX();
		double oy = getY();
		double oz = getZ();
		if (!randomTeleport(x, y, z, true, state -> false)) return false;
		level().playSound(null, ox, oy, oz, SoundEvents.ENDERMAN_TELEPORT, getSoundSource(), 1.0F, 1.0F);
		playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (isInvulnerableTo(level, source)) return false;
		setScreaming(true);
		if (!source.isDirect()) {
			// Arrows, fireballs, potions and the like: it tries to dodge by teleporting.
			for (int i = 0; i < 16; i++) {
				if (teleportRandomly()) return true;
			}
		}
		boolean hurt = super.hurtServer(level, source, damage);
		// EntityMob: whatever hurt it becomes its target.
		if (hurt && source.getEntity() instanceof LivingEntity attacker && attacker != this && !hasPassenger(attacker) && getVehicle() != attacker) {
			setTarget(attacker);
		}
		return hurt;
	}

	/** What it drops, 0-1 of (more with Looting): an Eye of Ender or an Ender Pearl, even odds. */
	protected Item dropItem() {
		return random.nextInt(2) == 1 ? Items.ENDER_EYE : Items.ENDER_PEARL;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		Item item = dropItem();
		int n = random.nextInt(2 + SpookyHelpers.looting(level, source));
		for (int i = 0; i < n; i++) spawnAtLocation(level, item);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return isScreaming() ? SoundEvents.ENDERMAN_SCREAM : SoundEvents.ENDERMAN_AMBIENT;
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENDERMAN_HURT;
	}

	@Override
	protected @Nullable SoundEvent getDeathSound() {
		return SoundEvents.ENDERMAN_DEATH;
	}

	/** EnderKnight.getCanSpawnHere: dark enough, at night, at y 30 or higher (or from a spawner). */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return Monster.isDarkEnoughToSpawn(level, pos, random) && !level.getLevel().isBrightOutside() && pos.getY() >= 30;
	}
}
