package orespawn.entity.sea;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import orespawn.OreSpawn;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;
import orespawn.world.OreSpawnDimensions;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Attack Squid: a squid that floats in rivers, swamps and oceans by day and goes for players, zombies,
 * villagers, spiders, lizards and OreSpawn's Girlfriends and Boyfriends within 10 blocks. Up close (3 blocks) it bites
 * (8 damage); further off it swims after its prey and now and then spits a water ball, or 1 time in 3 an ink sac that
 * can blind. With nothing to hunt it tags along with another Attack Squid. Like OreSpawn's other water creatures it's a
 * land mob that floats (FloatGoal) and wanders paths across water; out of water it looks for water (1 in 10 a tick, out
 * to about 11 blocks) and heads for it, and with none in reach it loses 1 health 1 time in 25, vanishing at 0.
 * Squids, Water Dragons and their water balls can't hurt it. 10 health, 15 xp. Killing one in the overworld (not the
 * Crystal dimension) as a player has a 1 in 15 chance to bring 1-3 Krakens down from the sky (when the Kraken is
 * ported). Drops 1-3 raw cod and 1 time in 50 each a gold nugget, gold ingot, golden carrot, golden apple, enchanted
 * golden apple or gold block, a piece of (maybe enchanted) gold gear 9 times in 50, or an ink sac 3 times in 50.
 * A squid fired from the SquidZooka (weapons group: {@link #setWasShot()}) takes no fall damage, attacks anything, and
 * vanishes after 250 ticks.
 */
public class AttackSquid extends Monster {
	private @Nullable LivingEntity buddy;
	private int wasShot;

	public AttackSquid(EntityType<? extends AttackSquid> type, Level level) {
		super(type, level);
		this.xpReward = 15;
		setPathfindingMalus(PathType.WATER, 0.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		// get_mobstats "AttackSquid": 10 health, 8 attack, 0 defense.
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 8.0).add(Attributes.ARMOR, 0.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, WanderGoal.wanderALot(this, 16, 1.0));
		goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(3, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	/** The SquidZooka's shot squid: 250 ticks to live. */
	public void setWasShot() {
		wasShot = 250;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("WasShot", wasShot);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		wasShot = input.getIntOr("WasShot", 0);
	}

	@Override
	protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
		super.travelInWater(SeaSwim.swimInput(input), baseGravity, isFalling, oldY);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (wasShot > 0 && --wasShot == 0) {
			discard();
			return;
		}
		if (!isInWater() && random.nextInt(10) == 0 && !SeaSwim.seekWater(this, level, 12, 5, 1.33, 25)) return;
		if (random.nextInt(10) == 1) {
			LivingEntity e = findSomethingToAttack();
			if (e != null) {
				if (distanceToSqr(e) < 9.0) {
					if (random.nextInt(4) == 0 || random.nextInt(5) == 1) doHurtTarget(level, e);
				} else {
					getNavigation().moveTo(e, 1.2);
					waterCannon(level, e);
				}
			} else if (buddy != null) {
				getNavigation().moveTo(buddy, 1.0);
			}
		}
	}

	private @Nullable LivingEntity findSomethingToAttack() {
		LivingEntity t = getTarget();
		if (t != null && t.isAlive()) return t;
		setTarget(null);
		return SeaTargets.nearest(this, 10.0, 4.0, this::isSuitableTarget);
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (!SeaTargets.basic(this, e)) return false;
		if (e instanceof Player p) return !p.isCreative() && !p.isSpectator();
		if (e instanceof Zombie || e instanceof Villager || e instanceof Spider) return true;
		if (SeaTargets.is(e, "girlfriend", "boyfriend", "lizard")) return true;
		if (SeaTargets.is(e, "ghost", "ghost_pumpkin_skelly")) return false;
		if (e instanceof AttackSquid) {
			if (random.nextInt(5) == 1) buddy = e;
			return false;
		}
		return wasShot != 0;
	}

	/** OreSpawn's watercanon: 1 time in 5, an ink sac (1 in 3) or a water ball at the target, from just in front. */
	private void waterCannon(ServerLevel level, LivingEntity e) {
		if (random.nextInt(5) != 1) return;
		double yoff = 1.0, xzoff = 1.2;
		// OreSpawn placed it by the head's yaw sideways and the body's yaw forwards; kept as it was.
		double x = getX() - xzoff * Math.sin(Math.toRadians(getYHeadRot()));
		double z = getZ() + xzoff * Math.cos(Math.toRadians(getYRot()));
		Projectile shot = random.nextInt(3) == 1 ? new InkSack(level, x, getY() + yoff, z) : new WaterBall(level, x, getY() + yoff, z);
		double dx = e.getX() - getX(), dy = e.getY() + 0.25 - shot.getY(), dz = e.getZ() - getZ();
		float lift = Mth.sqrt((float) (dx * dx + dz * dz)) * 0.2F;
		shot.shoot(dx, dy + lift, dz, 1.4F, 5.0F);
		playSound(SoundEvents.ARROW_SHOOT, 0.75F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
		level.addFreshEntity(shot);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		Entity e = source.getEntity();
		if (e instanceof AttackSquid || e instanceof WaterDragon || e instanceof WaterBall || source.getDirectEntity() instanceof WaterBall) return false;
		if (e instanceof Mob mob) {
			setTarget(mob);
			getNavigation().moveTo(mob, 1.2);
		}
		boolean hurt = super.hurtServer(level, source, damage);
		if (isDeadOrDying() && e instanceof Player && level.dimension() != OreSpawnDimensions.CRYSTAL && random.nextInt(15) == 1 && wasShot == 0) {
			summonKrakens(level);
		}
		return hurt;
	}

	/** 1-3 of OreSpawn's Krakens, dropped from y 170 near the squid (nothing until the Kraken is ported). */
	private void summonKrakens(ServerLevel level) {
		EntityType<?> kraken = BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id("the_kraken")).orElse(null);
		if (kraken == null) return;
		int n = 1 + random.nextInt(3);
		for (int i = 0; i < n; i++) {
			Entity k = kraken.create(level, EntitySpawnReason.MOB_SUMMONED);
			if (k == null) continue;
			k.snapTo(getX() + random.nextInt(4) - random.nextInt(4), 170.0, getZ() + random.nextInt(4) - random.nextInt(4), random.nextFloat() * 360.0F,
				0.0F);
			level.addFreshEntity(k);
			if (k instanceof Mob m) m.playAmbientSound();
		}
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return wasShot == 0 && super.causeFallDamage(fallDistance, multiplier, source);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		int roll = random.nextInt(50);
		switch (roll) {
			case 0 -> SeaLoot.drop(this, level, Items.GOLD_NUGGET);
			case 1 -> SeaLoot.drop(this, level, Items.GOLD_INGOT);
			case 2 -> SeaLoot.drop(this, level, Items.GOLDEN_CARROT);
			case 12 -> SeaLoot.drop(this, level, Items.GOLDEN_APPLE);
			case 13 -> SeaLoot.drop(this, level, Items.GOLD_BLOCK);
			case 14 -> SeaLoot.drop(this, level, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), 3, 1.0);
			case 15, 16, 17 -> SeaLoot.drop(this, level, Items.INK_SAC);
			default -> SeaLoot.gear(this, level, roll, SeaLoot.GOLD);
		}
		int n = 1 + random.nextInt(3);
		for (int i = 0; i < n; i++) SeaLoot.drop(this, level, Items.COD);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("squid_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("squid_death");
	}

	@Override
	protected float getSoundVolume() {
		return 1.0F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/** OreSpawn's getCanSpawnHere ignored the monster light check; its rules are all in checkSpawn. */
	@Override
	public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
		return true;
	}

	/** It appears in water. */
	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
	}

	/** AttackSquid.getCanSpawnHere: at y 50 or higher, by day. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return pos.getY() >= 50 && level.getLevel().isBrightOutside();
	}
}
