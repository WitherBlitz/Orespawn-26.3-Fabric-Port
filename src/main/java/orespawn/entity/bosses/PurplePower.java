package orespawn.entity.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Bosses;
import orespawn.entity.Flight;
import orespawn.entity.Ignored;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * OreSpawn's PurplePower: the glowing bolts The King, The Queen and The Princess loose. A small (0.75) flying thing
 * that passes through blocks, steers like OreSpawn's other flyers towards a spot, and every few ticks looks for prey
 * within 32 blocks (24 up and down) it can see: never royalty or creative players, never tamed pets for the coloured
 * bolts, and players only for the plain and exploding bolts. Within reach (4 plus half the prey's width) it strikes and
 * is gone. 1000 health (hits capped at 10; being hit turns it on the attacker), 25 armor; it vanishes on Peaceful, or
 * at random (1 in 2500 a tick), when the exploding kind blows up as it goes. It never despawns and drops nothing.
 * <p>
 * Kinds ({@link #setPurpleType}): 0, The King's and The Queen's: the prey drops to a quarter of its health less 1, then
 * takes an eighth of its maximum health; 10, The King's end-game bolt: the same, then an explosion of power 9.1 on the
 * prey (fire, and blocks broken when mob griefing is on); 1-3, The Princess's: the prey drops to 15/16 of its health and
 * takes 5, then 1 burns for 10 s, 2 is poisoned and 3 weakened for 2.5 s.
 * <p>
 * One change: OreSpawn could set the prey's health to 0 or less outright (a prey at 4 health or less), which leaves a
 * 26.3 player stuck at 0 health; such prey is left at half a heart, and the strike that follows finishes it.
 */
public class PurplePower extends Mob {
	private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(PurplePower.class, EntityDataSerializers.INT);
	private @Nullable BlockPos flightTarget;

	public PurplePower(EntityType<? extends PurplePower> type, Level level) {
		super(type, level);
		this.xpReward = 35;
		this.noPhysics = true;
		this.moveControl = Flight.keepOwnSteering(this);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 1000.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 500.0).add(Attributes.ARMOR, 25.0);
	}

	/** OreSpawn's spawnCreature(world, "PurplePower", x, y, z): a bolt at (x, y, z) facing a random way, added to the world. */
	public static @Nullable PurplePower spawn(ServerLevel level, double x, double y, double z) {
		PurplePower p = Bosses.PURPLE_POWER.create(level, EntitySpawnReason.MOB_SUMMONED);
		if (p == null) return null;
		p.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
		level.addFreshEntity(p);
		return p;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(TYPE, 0);
	}

	/** 0: The King's and Queen's homing bolt; 1-3: the Princess's fire, poison and weakness bolts; 10: The King's exploding bolt. */
	public void setPurpleType(int type) {
		if (!level().isClientSide()) entityData.set(TYPE, type);
	}

	public int getPurpleType() {
		return entityData.get(TYPE);
	}

	// ---- What OreSpawn turned off ----

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return false;
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	@Override
	protected float getSoundVolume() {
		return 0.75F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	// ---- Ticking ----

	@Override
	public void tick() {
		super.tick();
		noPhysics = true;
		Vec3 m = getDeltaMovement();
		setDeltaMovement(m.x, m.y * 0.6, m.z);
		RandomSource r = level().getRandom();
		if (level().isClientSide()) {
			if (getPurpleType() == 0) {
				if (r.nextInt(4) == 1) {
					level().addParticle(ParticleTypes.FIREWORK, getX(), getY() + 1.25, getZ(), (r.nextFloat() - r.nextFloat()) / 2.0F,
						(r.nextFloat() - r.nextFloat()) / 2.0F, (r.nextFloat() - r.nextFloat()) / 2.0F);
				}
			} else if (r.nextInt(6) == 1) {
				level().addParticle(ParticleTypes.FIREWORK, getX(), getY() + 0.65F, getZ(), (r.nextFloat() - r.nextFloat()) / 5.0F,
					(r.nextFloat() - r.nextFloat()) / 5.0F, (r.nextFloat() - r.nextFloat()) / 5.0F);
			}
		} else if (r.nextInt(2500) == 1) {
			if (getPurpleType() == 10) level().explode(null, getX(), getY() + 0.25, getZ(), 9.1F, true, Level.ExplosionInteraction.MOB);
			discard();
		}
	}

	private boolean canSeeSpot(BlockPos p) {
		Vec3 from = new Vec3(getX(), getY() + 0.55, getZ());
		Vec3 to = new Vec3(p.getX(), p.getY(), p.getZ());
		return level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (isRemoved()) return;
		if (flightTarget == null) flightTarget = blockPosition();
		if (random.nextInt(300) != 0 && Flight.distSq(this, flightTarget) >= 2.1F) {
			if (random.nextInt(7) == 2 && level.getDifficulty() != Difficulty.PEACEFUL) {
				LivingEntity e = findSomethingToAttack(level);
				if (e != null) {
					flightTarget = BlockPos.containing(e.getX(), e.getY() + e.getBbHeight() / 2.0F, e.getZ());
					float reach = 4.0F + e.getBbWidth() / 2.0F;
					if (distanceToSqr(e) < reach * reach) {
						doHurtTarget(level, e);
						discard();
						return;
					}
				}
			}
		} else {
			// A fresh spot 8-17 blocks away (10 up or down), in the air and in sight; up to 50 tries.
			for (int tries = 50; tries > 0; tries--) {
				int zdir = random.nextInt(10) + 8;
				int xdir = random.nextInt(10) + 8;
				if (random.nextInt(2) == 0) zdir = -zdir;
				if (random.nextInt(2) == 0) xdir = -xdir;
				flightTarget = BlockPos.containing(getX() + xdir, getY() + random.nextInt(20) - 10, getZ() + zdir);
				if (level.isEmptyBlock(flightTarget) && canSeeSpot(flightTarget)) break;
			}
		}
		if (level.getDifficulty() == Difficulty.PEACEFUL) {
			discard();
			return;
		}
		Flight.steer(this, flightTarget, 0.4, 0.2, 0.2, 0.5, 0.75F, 0.25F);
	}

	// ---- Targets and strikes ----

	private boolean isSuitableTarget(LivingEntity e) {
		if (level().getDifficulty() == Difficulty.PEACEFUL || e == this || !e.isAlive() || Ignored.byHunters(e)) return false;
		if (!getSensing().hasLineOfSight(e)) return false;
		int type = getPurpleType();
		if (e instanceof Player p) {
			if (p.isCreative() || p.isSpectator()) return false;
			return type <= 0 || type == 10;
		}
		if (type != 0 && type != 10 && e instanceof TamableAnimal t && t.isTame()) return false;
		return !BossUtil.isRoyalty(e);
	}

	private @Nullable LivingEntity findSomethingToAttack(ServerLevel level) {
		return level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(32.0, 24.0, 32.0)).stream()
			.sorted(BossUtil.targetOrder(this)).filter(this::isSuitableTarget).findFirst().orElse(null);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (!(target instanceof LivingEntity e)) return false;
		int type = getPurpleType();
		boolean hit;
		if (type != 0 && type != 10) {
			e.setHealth(Math.max(e.getHealth() * 15.0F / 16.0F, 0.5F));
			hit = e.hurtServer(level, damageSources().mobAttack(this), 5.0F);
			if (type == 1) e.igniteForSeconds(10.0F);
			if (type == 2) e.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0), this);
			if (type == 3) e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 0), this);
		} else {
			e.setHealth(Math.max(e.getHealth() / 4.0F - 1.0F, 0.5F));
			hit = e.hurtServer(level, damageSources().mobAttack(this), e.getMaxHealth() / 8.0F);
			if (type == 10) level.explode(null, e.getX(), e.getY() - 0.25, e.getZ(), 9.1F, true, Level.ExplosionInteraction.MOB);
		}
		return hit;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (BossUtil.unstoppable(source)) return super.hurtServer(level, source, damage);
		Entity e = source.getEntity();
		// OreSpawn ignored hits whose source entity was an arrow: only arrows with no shooter (a dispenser's).
		if (e instanceof AbstractArrow) return false;
		boolean ret = super.hurtServer(level, source, Math.min(damage, 10.0F));
		if (e != null && flightTarget != null) flightTarget = BlockPos.containing(e.getX(), e.getY() + e.getBbHeight() / 2.0F, e.getZ());
		return ret;
	}

	// ---- Saving ----

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("PurpleType", getPurpleType());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		entityData.set(TYPE, input.getIntOr("PurpleType", 0));
	}
}
