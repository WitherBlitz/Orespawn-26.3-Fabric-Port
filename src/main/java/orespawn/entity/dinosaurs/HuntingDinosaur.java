package orespawn.entity.dinosaurs;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import orespawn.entity.Ignored;
import orespawn.entity.WanderGoal;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * What OreSpawn's big hunting dinosaurs (Alosaurus, T. Rex, Nastysaurus, Pointysaurus) share. They wander a lot (16
 * blocks), keep out of water, and every few ticks look for the nearest thing they hunt in a box around them; in reach
 * they open their jaws ("attacking", which the models animate) and bite now and then, flinging what they bit away;
 * otherwise they run at it (1.25x). Some also remember who last hurt them and go after that one first, and shrug off
 * cactus. Each subclass passes OreSpawn's own numbers.
 */
public abstract class HuntingDinosaur extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(HuntingDinosaur.class, EntityDataSerializers.BOOLEAN);

	/** OreSpawn's numbers for one kind. */
	protected record Hunting(int aiOdds, int forgetOdds, float reach, int biteOdds, double rangeXZ, double rangeY, double knockback,
			boolean cactusProof) {}

	private final Hunting hunting;
	/** OreSpawn's "rt": who last hurt it (null for the kinds that don't remember). */
	private @Nullable LivingEntity revenge;

	protected HuntingDinosaur(EntityType<? extends HuntingDinosaur> type, Level level, Hunting hunting) {
		super(type, level);
		this.hunting = hunting;
		setPathfindingMalus(PathType.WATER, -1.0F);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
		goalSelector.addGoal(2, WanderGoal.wanderALot(this, 16, 1.0));
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

	/** Whether it hunts this one (OreSpawn's isSuitableTarget, after the shared checks). */
	protected abstract boolean hunts(LivingEntity e);

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(hunting.aiOdds) != 0) return;
		LivingEntity e = null;
		if (hunting.forgetOdds > 0) {
			e = revenge;
			if (e != null) {
				if (!e.isAlive() || random.nextInt(hunting.forgetOdds) == 1) {
					e = null;
					revenge = null;
				}
				if (e != null && !getSensing().hasLineOfSight(e)) e = null;
			}
		}
		if (e == null) e = findPrey(level);
		if (e == null) {
			entityData.set(ATTACKING, false);
			return;
		}
		lookAt(e, 10.0F, 10.0F);
		float reach = hunting.reach + e.getBbWidth() / 2.0F;
		if (distanceToSqr(e) < reach * reach) {
			entityData.set(ATTACKING, true);
			if (random.nextInt(hunting.biteOdds) == 0 || random.nextInt(hunting.biteOdds + 1) == 1) doHurtTarget(level, e);
		} else {
			getNavigation().moveTo(e, 1.25);
		}
	}

	private @Nullable LivingEntity findPrey(ServerLevel level) {
		return level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(hunting.rangeXZ, hunting.rangeY, hunting.rangeXZ), this::isPrey)
			.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
	}

	private boolean isPrey(LivingEntity e) {
		if (e == this || !e.isAlive() || Ignored.byHunters(e) || !hunts(e)) return false;
		if (!getSensing().hasLineOfSight(e)) return false;
		return !(e instanceof Player p) || !(p.isCreative() || p.isSpectator());
	}

	/** OreSpawn's attackEntityAsMob: a normal bite, then the victim is flung away (higher if it's a player or it died). */
	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (!super.doHurtTarget(level, target)) return false;
		if (target instanceof LivingEntity) {
			double up = !target.isAlive() || target instanceof Player ? 0.2 : 0.1;
			double angle = Math.atan2(target.getZ() - getZ(), target.getX() - getX());
			target.push(Math.cos(angle) * hunting.knockback, up, Math.sin(angle) * hunting.knockback);
			target.syncVelocity = true;
		}
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (!hunting.cactusProof) return super.hurtServer(level, source, damage);
		if (source.is(DamageTypes.CACTUS)) return false;
		boolean hurt = super.hurtServer(level, source, damage);
		if (source.getEntity() instanceof LivingEntity attacker) revenge = attacker;
		return hurt;
	}

	/**
	 * OreSpawn's getCanSpawnHere for these: dark enough, at y 50 or higher, at night, with open air above (5 blocks up from
	 * one above its feet, over {@code -1..edge} around it), and none of its own kind within the given box (none if 0).
	 */
	protected static boolean checkBigSpawn(EntityType<?> type, ServerLevelAccessor level, BlockPos pos,
			RandomSource random, int edge, double spaceXZ, double spaceY) {
		if (level.getDifficulty() == Difficulty.PEACEFUL || !isDarkEnoughToSpawn(level, pos, random)) return false;
		if (pos.getY() < 50 || level.getLevel().isBrightOutside()) return false;
		for (int dz = -1; dz <= edge; dz++) {
			for (int dx = -1; dx <= edge; dx++) {
				for (int dy = 1; dy < 6; dy++) {
					if (!level.getBlockState(pos.offset(dx, dy, dz)).isAir()) return false;
				}
			}
		}
		if (spaceXZ <= 0.0) return true;
		return level.getEntities(type, type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(spaceXZ, spaceY, spaceXZ),
			e -> true).isEmpty();
	}
}
