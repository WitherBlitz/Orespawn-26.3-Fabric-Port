package orespawn.entity.sea;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import orespawn.entity.WanderGoal;
import org.jspecify.annotations.Nullable;

/**
 * What OreSpawn's Sea Monster and Sea Viper share (the two classes were copies of each other with different numbers):
 * big serpents that swim at the surface, faster in water than on land, and hunt players, monsters and OreSpawn's big
 * creatures, royalty, pets and villagers, biting with a knockback. They breathe underwater, heal in water now and then,
 * ignore cacti, and after taking a hit shrug off further hits for a few ticks. Out of water they look for water (1 in 25
 * a tick, out to about 11 blocks) and head for it; with none in reach they slowly lose health, vanishing at 0.
 * On death: their own trophy, an item frame, 9-14 raw cod, and 1 time in 20 each an iron ingot, an iron block or a piece
 * of (maybe enchanted) iron gear.
 */
public abstract class SeaSerpent extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(SeaSerpent.class, EntityDataSerializers.BOOLEAN);

	/** The numbers that differ between the two. */
	protected record Kind(double landSpeed, double waterSpeed, int hurtDelay, int lossChance, float reach, double chaseSpeed, int healChance,
			double huntRange, double knockback, double lift) {
	}

	private final Kind kind;
	private int hurtTimer;

	protected SeaSerpent(EntityType<? extends SeaSerpent> type, Level level, Kind kind) {
		super(type, level);
		this.kind = kind;
		setPathfindingMalus(PathType.WATER, 0.0F);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, WanderGoal.wanderALot(this, 16, 1.0));
		goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 10.0F));
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
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public void tick() {
		getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(isInWater() ? kind.waterSpeed() : kind.landSpeed());
		super.tick();
	}

	@Override
	protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
		super.travelInWater(SeaSwim.swimInput(input), baseGravity, isFalling, oldY);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (hurtTimer > 0) hurtTimer--;
		if (!isInWater() && random.nextInt(25) == 0 && !SeaSwim.seekWater(this, level, 12, 10, 1.33, kind.lossChance())) return;
		if (random.nextInt(5) == 1) {
			LivingEntity e = findSomethingToAttack();
			if (e != null) {
				lookAt(e, 10.0F, 10.0F);
				double reach = kind.reach() + e.getBbWidth() / 2.0F;
				if (distanceToSqr(e) < reach * reach) {
					entityData.set(ATTACKING, true);
					if (bites()) doHurtTarget(level, e);
				} else {
					getNavigation().moveTo(e, kind.chaseSpeed());
				}
			} else {
				entityData.set(ATTACKING, false);
			}
		}
		// OreSpawn also played "splash" here, which named no sound.
		if (random.nextInt(kind.healChance()) == 1 && isInWater() && getHealth() < getMaxHealth()) heal(1.0F);
	}

	/** Whether this tick's chance to bite comes up. */
	protected abstract boolean bites();

	private @Nullable LivingEntity findSomethingToAttack() {
		LivingEntity t = getTarget();
		if (t != null && t.isAlive()) return t;
		setTarget(null);
		return SeaTargets.nearest(this, kind.huntRange(), 4.0, this::isSuitableTarget);
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (!SeaTargets.basic(this, e)) return false;
		if (e instanceof Player p) return !p.isCreative() && !p.isSpectator();
		if (e.getClass() == getClass()) return false;
		return e instanceof Monster || SeaTargets.attackableNonMob(e);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (!super.doHurtTarget(level, target)) return false;
		if (target instanceof LivingEntity living) {
			double inair = kind.lift();
			float f3 = (float) Math.atan2(target.getZ() - getZ(), target.getX() - getX());
			if (!target.isAlive() || target instanceof Player) inair *= 2.0;
			target.push(Math.cos(f3) * kind.knockback(), inair, Math.sin(f3) * kind.knockback());
			afterBite(level, living);
		}
		return true;
	}

	/** Anything extra a successful bite does. */
	protected void afterBite(ServerLevel level, LivingEntity target) {
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypes.CACTUS)) return false;
		boolean hurt = false;
		if (hurtTimer <= 0) {
			hurt = super.hurtServer(level, source, damage);
			hurtTimer = kind.hurtDelay();
		}
		if (source.getEntity() instanceof Mob mob) {
			if (mob.getClass() == getClass()) return false;
			setTarget(mob);
			getNavigation().moveTo(mob, 1.2);
		}
		return hurt;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		SeaLoot.drop(this, level, trophy());
		SeaLoot.drop(this, level, Items.ITEM_FRAME);
		int n = 9 + random.nextInt(6);
		for (int i = 0; i < n; i++) dropFish(level);
		int roll = random.nextInt(20);
		if (roll == 1) SeaLoot.drop(this, level, Items.IRON_INGOT);
		else if (roll == 13) SeaLoot.drop(this, level, Items.IRON_BLOCK);
		else SeaLoot.gear(this, level, roll, SeaLoot.IRON);
	}

	/** Its own drop: the Sea Monster Scale or the Sea Viper Tongue. */
	protected abstract ItemLike trophy();

	/** One of the 9-14 fish drops. */
	protected void dropFish(ServerLevel level) {
		SeaLoot.drop(this, level, Items.COD);
	}

	@Override
	protected float getSoundVolume() {
		return 1.0F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/** OreSpawn's getCanSpawnHere replaced the monster light check; its rules are all in checkSpawn. */
	@Override
	public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
		return true;
	}

	/** They appear in water. */
	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
	}
}
