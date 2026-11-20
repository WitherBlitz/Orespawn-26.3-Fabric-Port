package orespawn.entity.sea;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Sea;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Water Dragon: a fire-proof, tameable water dragon of rivers, swamps, oceans and stony shores, out by day.
 * Raw fish (within 5 blocks) tames a wild one 1 time in 3 and heals a tame one fully; the owner frees it with a dead
 * bush and makes it stay or move with anything else (see {@link SeaPets}). It follows its owner and players holding
 * fish. Except in Peaceful it hunts monsters, and (wild ones only) players, OreSpawn's big creatures, royalty, pets and
 * villagers within 14 blocks: up close (4 blocks plus half the target's width) it bites for 20 with a knockback;
 * further off it chases and sprays streams of 8 water balls, now and then with a small fireball. 150 health, 8 armour;
 * 0.25 speed on land, 0.55 in water; breathes underwater; heals 1 in water 1 time in 100. After a hit it ignores hits
 * for 10 ticks; Attack Squids, other Water Dragons, water balls and cacti can't hurt it. Out of water (unless told to
 * stay) it looks for water (1 in 25 a tick, out to about 11 blocks) and heads for it; with none in reach it loses 1
 * health 1 time in 50, vanishing at 0. 100 xp. Babies never despawn, nor do tame ones. Drops a Water Dragon Scale, an
 * item frame, 9-14 raw cod, and 1 time in 20 each an iron ingot, an iron block, a piece of (maybe enchanted) iron gear,
 * or The Ultimate Axe, Pickaxe or Shovel. Breeds with OreSpawn's Crystal Apple (once that's ported); a tame parent's
 * baby is born tame to the same owner (OreSpawn's baby was tame but ownerless).
 */
public class WaterDragon extends TamableAnimal {
	/** 0 idle, 1 biting, 2 spraying water (the model opens its jaw). */
	private static final EntityDataAccessor<Integer> ATTACKING = SynchedEntityData.defineId(WaterDragon.class, EntityDataSerializers.INT);
	private int streamCount;
	private int hurtTimer;

	public WaterDragon(EntityType<? extends WaterDragon> type, Level level) {
		super(type, level);
		this.xpReward = 100;
		setPathfindingMalus(PathType.WATER, 0.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		// get_mobstats "WaterDragon": 150 health, 20 attack, 8 defense.
		return TamableAnimal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 150.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 20.0).add(Attributes.ARMOR, 8.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new BreedGoal(this, 1.0));
		goalSelector.addGoal(2, new FollowOwnerGoal(this, 2.0, 10.0F, 2.0F));
		goalSelector.addGoal(3, new TemptGoal(this, 1.2, SeaPets::rawFish, false));
		goalSelector.addGoal(4, WanderGoal.wanderALot(this, 16, 1.0));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, 0);
	}

	public int attacking() {
		return entityData.get(ATTACKING);
	}

	private void setAttacking(int a) {
		entityData.set(ATTACKING, a);
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public void tick() {
		getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(isInWater() ? 0.55 : 0.25);
		super.tick();
	}

	@Override
	protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
		super.travelInWater(SeaSwim.swimInput(input), baseGravity, isFalling, oldY);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		InteractionResult result = super.mobInteract(player, hand);
		if (result.consumesAction()) return result;
		return SeaPets.interact(this, player, hand, 25.0, 3);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (hurtTimer > 0) hurtTimer--;
		if (!isInWater() && random.nextInt(25) == 0 && !isOrderedToSit() && !SeaSwim.seekWater(this, level, 12, 10, 1.33, 50)) return;
		if (random.nextInt(200) == 0) setTarget(null);
		if (level.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(5) == 1) {
			LivingEntity e = findSomethingToAttack();
			if (e != null) {
				lookAt(e, 10.0F, 10.0F);
				double reach = 4.0F + e.getBbWidth() / 2.0F;
				if (distanceToSqr(e) < reach * reach) {
					setAttacking(1);
					if (random.nextInt(4) == 0 || random.nextInt(5) == 1) doHurtTarget(level, e);
				} else {
					getNavigation().moveTo(e, 1.0);
					waterCannon(level, e);
				}
			} else {
				setAttacking(0);
			}
		}
		// OreSpawn also played "splash" here, which named no sound.
		if (random.nextInt(100) == 1 && isInWater() && getHealth() < getMaxHealth()) heal(1.0F);
	}

	private @Nullable LivingEntity findSomethingToAttack() {
		if (isBaby()) return null;
		LivingEntity t = getTarget();
		if (t != null && t.isAlive()) return t;
		setTarget(null);
		return SeaTargets.nearest(this, 14.0, 4.0, this::isSuitableTarget);
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (level().getDifficulty() == Difficulty.PEACEFUL || !SeaTargets.basic(this, e) || e instanceof WaterDragon) return false;
		if (e instanceof Monster) return true;
		if (isTame()) return false;
		if (e instanceof Player p) return !p.isCreative() && !p.isSpectator();
		return SeaTargets.attackableNonMob(e);
	}

	/**
	 * OreSpawn's watercanon: while a stream lasts (8 shots), a water ball at the target every time its chance comes up,
	 * and 1 time in 15 a small fireball too; a spent stream refills 1 time in 4.
	 */
	private void waterCannon(ServerLevel level, LivingEntity e) {
		double yoff = 1.75, xzoff = 1.5;
		if (streamCount > 0) {
			setAttacking(2);
			double mx = getX() - xzoff * Math.sin(Math.toRadians(getYHeadRot()));
			double my = getY() + yoff;
			if (random.nextInt(15) == 1) {
				double mz = getZ() + xzoff * Math.cos(Math.toRadians(getYHeadRot()));
				Vec3 dir = new Vec3(e.getX() - getX() + random.nextGaussian() * 0.4, e.getY() + 0.75 - my + random.nextGaussian() * 0.4,
					e.getZ() - getZ() + random.nextGaussian() * 0.4);
				SmallFireball fireball = new SmallFireball(level, this, dir.normalize());
				fireball.snapTo(mx, my, mz, getYRot(), getXRot());
				playSound(SoundEvents.ARROW_SHOOT, 0.75F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
				level.addFreshEntity(fireball);
			}
			// OreSpawn placed the water ball by the head's yaw sideways and the body's yaw forwards; kept as it was.
			WaterBall ball = new WaterBall(level, mx, my, getZ() + xzoff * Math.cos(Math.toRadians(getYRot())));
			double dx = e.getX() - ball.getX(), dy = e.getY() + 0.25 - ball.getY(), dz = e.getZ() - ball.getZ();
			float lift = Mth.sqrt((float) (dx * dx + dz * dz)) * 0.2F;
			ball.shoot(dx, dy + lift, dz, 1.4F, 5.0F);
			playSound(SoundEvents.ARROW_SHOOT, 0.75F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
			level.addFreshEntity(ball);
			streamCount--;
		} else {
			setAttacking(0);
		}
		if (streamCount <= 0 && random.nextInt(4) == 1) streamCount = 8;
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (!super.doHurtTarget(level, target)) return false;
		if (target instanceof LivingEntity) {
			double ks = 1.1, inair = 0.14;
			float f3 = (float) Math.atan2(target.getZ() - getZ(), target.getX() - getX());
			if (!target.isAlive() || target instanceof Player) inair *= 2.0;
			target.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
		}
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypes.CACTUS)) return false;
		Entity e = source.getEntity();
		if (e instanceof WaterDragon || e instanceof AttackSquid || e instanceof WaterBall || source.getDirectEntity() instanceof WaterBall) return false;
		boolean hurt = false;
		if (hurtTimer <= 0) {
			hurt = super.hurtServer(level, source, damage);
			hurtTimer = 10;
		}
		if (e instanceof Mob mob) {
			setTarget(mob);
			getNavigation().moveTo(mob, 1.2);
		}
		return hurt;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		if (isBaby()) {
			setPersistenceRequired();
			return false;
		}
		return !isTame();
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		SeaLoot.drop(this, level, Sea.WATER_DRAGON_SCALE);
		SeaLoot.drop(this, level, Items.ITEM_FRAME);
		int n = 9 + random.nextInt(6);
		for (int i = 0; i < n; i++) SeaLoot.drop(this, level, Items.COD);
		int roll = random.nextInt(20);
		switch (roll) {
			case 0 -> SeaLoot.drop(this, level, ModItems.byName("ultimate_axe"));
			case 1 -> SeaLoot.drop(this, level, Items.IRON_INGOT);
			case 2 -> SeaLoot.drop(this, level, ModItems.byName("ultimate_pickaxe"));
			case 12 -> SeaLoot.drop(this, level, ModItems.byName("ultimate_shovel"));
			case 13 -> SeaLoot.drop(this, level, Items.IRON_BLOCK);
			default -> SeaLoot.gear(this, level, roll, SeaLoot.IRON);
		}
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("waterdragon_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("waterdragon_death");
	}

	@Override
	protected float getSoundVolume() {
		return 1.0F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return SeaPets.crystalApple(stack);
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		WaterDragon baby = Sea.WATER_DRAGON.create(level, EntitySpawnReason.BREEDING);
		if (baby != null && isTame() && getOwner() != null) {
			baby.setTame(true, true);
			baby.setOwner(getOwner());
		}
		return baby;
	}

	/** OreSpawn's getCanSpawnHere replaced the animal checks; its rules are all in checkSpawn. */
	@Override
	public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
		return true;
	}

	/** It appears in water. */
	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
	}

	/** WaterDragon.getCanSpawnHere: at y 50 or higher, by day, and no other Water Dragon within 16 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return pos.getY() >= 50 && level.getLevel().isBrightOutside()
			&& level.getEntitiesOfClass(WaterDragon.class, new AABB(pos).inflate(16.0, 5.0, 16.0)).isEmpty();
	}
}
