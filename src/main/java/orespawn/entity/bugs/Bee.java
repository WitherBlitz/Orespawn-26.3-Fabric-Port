package orespawn.entity.bugs;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.entity.Flight;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's giant Bee: flies about by day over forests, jungles, taigas and savannas, and goes for players and
 * villagers within 10 blocks, stinging from 4 blocks: 12 damage, and a 1 in 3 chance of 2.5 s of poison. Whatever hurts
 * it becomes its target for good. Water stings it. 80 health, 5 armor. Drops gold nuggets, Butter Candy, dandelions
 * and sugar.
 * <p>
 * Not ported yet: OreSpawn's Girlfriend and Boyfriend as targets, its Utopia dimension spawns.
 */
public class Bee extends BugMonster {
	private @Nullable BlockPos flightTarget;
	private int stuckCount;
	private int lastX;
	private int lastZ;
	/** The last thing that hurt it (rt): it keeps going after it. */
	private @Nullable LivingEntity revengeTarget;

	public Bee(EntityType<? extends Bee> type, Level level) {
		super(type, level);
		this.xpReward = 25;
		keepOwnSteering();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 80.0).add(Attributes.MOVEMENT_SPEED, 0.32)
			.add(Attributes.ATTACK_DAMAGE, 12.0).add(Attributes.ARMOR, 5.0);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return optionalSound("beebuzz");
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("dragonfly_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("alo_death");
	}

	@Override
	protected float getSoundVolume() {
		return 0.25F;
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	/** collideWithEntity: it doesn't shove what it touches. */
	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	public void tick() {
		super.tick();
		setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6, 1.0));
		if (level() instanceof ServerLevel level && isInWater() && random.nextInt(4) == 1) doHurtTarget(level, this);
	}

	/** The sting: its attack damage as a plain mob hit, and a 1 in 3 chance of poison whether or not it hurt. */
	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean hit = target.hurtServer(level, damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE));
		if (random.nextInt(3) == 1 && target instanceof LivingEntity living) {
			living.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0), this);
		}
		return hit;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (lastX == getBlockX() && lastZ == getBlockZ()) {
			stuckCount++;
		} else {
			stuckCount = 0;
			lastX = getBlockX();
			lastZ = getBlockZ();
		}
		if (flightTarget == null) flightTarget = blockPosition();
		if (stuckCount <= 50 && random.nextInt(300) != 0 && !(Flight.distSq(this, flightTarget) < 2.1)) {
			if (random.nextInt(15) == 0) {
				LivingEntity e = revengeTarget;
				if (e != null && e.isRemoved()) e = null;
				if (e == null) e = findTarget(10.0, 6.0, 10.0, this::isSuitableTarget);
				if (e != null) {
					setAttacking(true);
					flightTarget = BlockPos.containing(e.getX(), e.getY() + 1.0, e.getZ());
					if (distanceToSqr(e) < 16.0) doHurtTarget(level, e);
				} else {
					setAttacking(false);
				}
			}
		} else {
			stuckCount = 0;
			for (int tries = 50; tries > 0; tries--) {
				int dz = random.nextInt(9) + 4, dx = random.nextInt(9) + 4;
				if (random.nextInt(2) == 0) dz = -dz;
				if (random.nextInt(2) == 0) dx = -dx;
				flightTarget = new BlockPos(getBlockX() + dx, getBlockY() + random.nextInt(6) - 3, getBlockZ() + dz);
				if (level.isEmptyBlock(flightTarget) && canSee(flightTarget)) break;
			}
		}
		Flight.steer(this, flightTarget, 0.5, 0.30000000149011613, 0.20000000149011612, 0.5, 1.0F, 0.25F);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean ret = super.hurtServer(level, source, amount);
		if (source.getEntity() instanceof LivingEntity e && flightTarget != null) {
			revengeTarget = e;
			flightTarget = e.blockPosition();
		}
		return ret;
	}

	/** Bee.isSuitableTarget: players (not in creative) and villagers, seen and out of water. */
	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || !getSensing().hasLineOfSight(e) || e.isInWater()) return false;
		if (e instanceof Player) return !isCreativeOrSpectator(e);
		return e instanceof Villager;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		for (int i = 2 + random.nextInt(10); i > 0; i--) dropRand(level, Items.GOLD_NUGGET, 4);
		for (int i = 2 + random.nextInt(10); i > 0; i--) dropRand(level, ModItems.lazy("butter_candy"), 4);
		for (int i = 2 + random.nextInt(10); i > 0; i--) dropRand(level, Blocks.DANDELION.asItem(), 4);
		for (int i = 2 + random.nextInt(10); i > 0; i--) dropRand(level, Items.SUGAR, 4);
	}

	/** Bee.getCanSpawnHere: open air 3x4x3 above, y 50 or higher, by day. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!allAir(level, pos, -1, 2, 1, 5, -1, 2)) return false;
		return pos.getY() >= 50 && level.getLevel().isBrightOutside();
	}
}
