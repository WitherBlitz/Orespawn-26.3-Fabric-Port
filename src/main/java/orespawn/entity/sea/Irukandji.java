package orespawn.entity.sea;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Irukandji: a tiny, deadly jellyfish (1 health, 20 damage a sting) that drifts in water and stings players
 * within about 1.7 blocks, chasing any within 6. Touching it bare-handed deals you 200 damage: right-clicking it with an
 * empty hand, or hurting it while your main hand is empty (and then it takes no damage). Like OreSpawn's other water creatures it's a
 * land mob that floats (FloatGoal) and wanders paths across water and breathes underwater; out of water it looks for
 * water (1 in 10 a tick, out to about 11 blocks) and heads for it, and with none in reach it loses 1 health 1 time in
 * 25, vanishing at 0. 50 xp. Drops 0-2 Dead Irukandji (more with Looting; a loot table). Natural spawns: only in
 * OreSpawn's Crystal dimension (its water creatures, weight 4, 2-3), which isn't ported here. Silent but for its death
 * sound (its hurt sound "little_splt" named no sound in OreSpawn).
 */
public class Irukandji extends Monster {
	public Irukandji(EntityType<? extends Irukandji> type, Level level) {
		super(type, level);
		this.xpReward = 50;
		setPathfindingMalus(PathType.WATER, 0.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		// get_mobstats "Irukandji": 1 health, 20 attack, 0 defense.
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.MOVEMENT_SPEED, 0.15)
			.add(Attributes.ATTACK_DAMAGE, 20.0).add(Attributes.ARMOR, 0.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, WanderGoal.wander(this, 1.0));
		goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(3, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
		super.travelInWater(SeaSwim.swimInput(input), baseGravity, isFalling, oldY);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (!isInWater() && random.nextInt(10) == 0 && !SeaSwim.seekWater(this, level, 12, 5, 1.33, 25)) return;
		if (random.nextInt(8) == 1) {
			LivingEntity e = findSomethingToAttack();
			if (e != null) {
				if (distanceToSqr(e) < 3.0) {
					if (random.nextInt(4) == 0 || random.nextInt(5) == 1) doHurtTarget(level, e);
				} else {
					getNavigation().moveTo(e, 1.2);
				}
			}
		}
	}

	private @Nullable LivingEntity findSomethingToAttack() {
		LivingEntity t = getTarget();
		if (t != null && t.isAlive()) return t;
		setTarget(null);
		return SeaTargets.nearest(this, 6.0, 4.0, e -> SeaTargets.basic(this, e) && e instanceof Player p && !p.isCreative() && !p.isSpectator());
	}

	/** Irukandji.interact: an empty hand gets stung for 200. */
	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (hand == InteractionHand.MAIN_HAND && player.getMainHandItem().isEmpty() && level() instanceof ServerLevel level) {
			player.hurtServer(level, damageSources().mobAttack(this), 200.0F);
		}
		return InteractionResult.PASS;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		Entity e = source.getEntity();
		if (e instanceof Player p && p.getMainHandItem().isEmpty()) {
			p.hurtServer(level, damageSources().mobAttack(this), 200.0F);
			return false;
		}
		if (e instanceof Mob mob) {
			if (mob instanceof Irukandji) return false;
			setTarget(mob);
			getNavigation().moveTo(mob, 1.2);
		}
		return super.hurtServer(level, source, damage);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("ratdead");
	}

	@Override
	protected float getSoundVolume() {
		return 0.25F;
	}

	/** OreSpawn's getCanSpawnHere replaced the monster light check; its rules are all in checkSpawn. */
	@Override
	public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
		return true;
	}

	/** It appears in water. */
	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
	}

	/** Irukandji.getCanSpawnHere: at y 50 or higher, by day, 1 time in 60, and no more than 2 Irukandji within 16 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return pos.getY() >= 50 && level.getLevel().isBrightOutside() && random.nextInt(60) == 1
			&& level.getEntitiesOfClass(Irukandji.class, new AABB(pos).inflate(16.0, 8.0, 16.0)).size() <= 2;
	}
}
