package orespawn.entity.bugs;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.entity.Bird;
import orespawn.entity.Butterfly;
import orespawn.entity.Flight;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Mantis: a huge flying mantis of the daytime forests and jungles. It flies about like OreSpawn's other
 * flyers, and every few ticks picks a target within 16 blocks (monsters, butterflies, birds, villagers and players, but
 * nothing in water and not other mantises or bees) and flies at it, striking with its claws from 5 blocks. Whatever
 * hurts it becomes its target for good. Water hurts it. 120 health, 16 damage, 10 armor. Drops two Mantis Claws, an item
 * frame, gold nuggets, uranium and titanium nuggets and diamonds.
 * <p>
 * Not ported yet: OreSpawn's Utopia dimension spawns.
 */
public class Mantis extends BugMonster {
	private @Nullable BlockPos flightTarget;
	private int stuckCount;
	private int lastX;
	private int lastZ;
	/** The last thing that hurt it (rt): it keeps going after it. */
	private @Nullable LivingEntity revengeTarget;

	public Mantis(EntityType<? extends Mantis> type, Level level) {
		super(type, level);
		this.xpReward = 100;
		keepOwnSteering();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 120.0).add(Attributes.MOVEMENT_SPEED, 0.32)
			.add(Attributes.ATTACK_DAMAGE, 16.0).add(Attributes.ARMOR, 10.0);
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
		return 0.35F;
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
		if (level() instanceof ServerLevel level && isInWater() && random.nextInt(20) == 1) doHurtTarget(level, this);
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
			if (random.nextInt(8) == 0) {
				LivingEntity e = revengeTarget;
				if (e != null && e.isRemoved()) e = null;
				if (e == null) e = findTarget(16.0, 8.0, 16.0, this::isSuitableTarget);
				if (e != null) {
					setAttacking(true);
					flightTarget = BlockPos.containing(e.getX(), e.getY() + 1.0, e.getZ());
					double reach = 5.0F + e.getBbWidth() / 2.0F;
					if (distanceToSqr(e) < reach * reach) doHurtTarget(level, e);
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
		if (random.nextInt(100) == 1) heal(1.0F);
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

	/** Mantis.isSuitableTarget (OreSpawn's sea monsters, Fairies and Mothra aren't ported yet). */
	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || !getSensing().hasLineOfSight(e) || e.isInWater()) return false;
		if (e instanceof Player) return !isCreativeOrSpectator(e);
		if (e instanceof Mantis || e instanceof Bee || e instanceof Squid) return false;
		return e instanceof Monster || e instanceof Butterfly || e instanceof Bird || e instanceof Villager;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		dropRand(level, ModItems.lazy("mantis_claw"), 5);
		dropRand(level, ModItems.lazy("mantis_claw"), 5);
		dropRand(level, Items.ITEM_FRAME, 5);
		for (int i = 2 + random.nextInt(10); i > 0; i--) dropRand(level, Items.GOLD_NUGGET, 5);
		for (int i = 1 + random.nextInt(3); i > 0; i--) dropRand(level, ModItems.URANIUM_NUGGET, 5);
		for (int i = 1 + random.nextInt(3); i > 0; i--) dropRand(level, ModItems.TITANIUM_NUGGET, 5);
		for (int i = 2 + random.nextInt(3); i > 0; i--) dropRand(level, Items.DIAMOND, 5);
	}

	/** Mantis.getCanSpawnHere: open air 4x5x4 above, y 50 or higher, by day, and no other mantis within 32 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!allAir(level, pos, -2, 2, 1, 6, -2, 2)) return false;
		if (pos.getY() < 50 || !level.getLevel().isBrightOutside()) return false;
		return noneNear(level, type, Mantis.class, pos, 32.0, 16.0, 32.0);
	}
}
