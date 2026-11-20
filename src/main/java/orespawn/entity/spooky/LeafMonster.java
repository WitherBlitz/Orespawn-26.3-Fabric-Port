package orespawn.entity.spooky;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import orespawn.entity.Butterfly;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * OreSpawn's Leaf Monster: it sits still as a leafy bush, square to the block grid, until a player (or an ant, butterfly
 * or moth) comes within 4 blocks (6 up or down). Then it rises up out of the leaves, chases and swats it. 6 health, 2
 * damage, 1 armor. A fall hurts it for at most 2. Drops 0-2 of one thing (more with Looting): oak logs, oak leaves or
 * rotten flesh. Spawns at night at y 50 or higher, no more than 4 others within 20 blocks.
 * <p>
 * Not ported yet: its spawn rule in OreSpawn's Mining dimension (y 20 or lower), which waits for the dimension.
 */
public class LeafMonster extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(LeafMonster.class, EntityDataSerializers.BOOLEAN);
	/** Prey besides players and butterflies (OreSpawn ids, so it works before they're ported). */
	private static final Set<String> PREY = Set.of("ant");

	public LeafMonster(EntityType<? extends LeafMonster> type, Level level) {
		super(type, level);
		this.xpReward = 5;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 6.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.ARMOR, 1.0).add(Attributes.FOLLOW_RANGE, 32.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new PanicGoal(this, 1.35F));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
	}

	/** Up out of its bush and hunting. */
	public boolean attacking() {
		return entityData.get(ATTACKING);
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide() && !attacking()) {
			// Hiding as a bush: back to the middle of its block, facing straight along the grid.
			double x = (int) getX();
			double z = (int) getZ();
			if (x > 0.0) x += 0.5;
			if (z > 0.0) z += 0.5;
			if (x < 0.0) x -= 0.5;
			if (z < 0.0) z -= 0.5;
			setPos(x, (int) getY(), z);
			float yaw = (int) getYHeadRot() / 90 * 90;
			setXRot(0.0F);
			setYRot(yaw);
			setYHeadRot(yaw);
		}
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		float i = Mth.ceil(fallDistance - 3.0);
		if (i > 0.0F) {
			if (i > 2.0F) {
				playSound(SoundEvents.GENERIC_BIG_FALL, 1.0F, 1.0F);
				i = 2.0F;
			} else {
				playSound(SoundEvents.GENERIC_SMALL_FALL, 1.0F, 1.0F);
			}
			if (level() instanceof ServerLevel server) hurtServer(server, source, i);
			return true;
		}
		return false;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(100) == 1) setLastHurtByMob(null);
		if (random.nextInt(4) == 1) {
			LivingEntity prey = SpookyHelpers.closest(this, getBoundingBox().inflate(4.0, 6.0, 4.0), this::isPrey);
			if (prey != null) {
				lookAt(prey, 10.0F, 10.0F);
				entityData.set(ATTACKING, true);
				getNavigation().moveTo(prey, 1.25);
				if (distanceToSqr(prey) < 5.0 && (random.nextInt(8) == 0 || random.nextInt(10) == 1)) doHurtTarget(level, prey);
			} else {
				entityData.set(ATTACKING, false);
			}
		}
	}

	private boolean isPrey(LivingEntity e) {
		if (e == this || !e.isAlive() || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Butterfly || SpookyHelpers.isOreSpawn(e, PREY)) return true;
		return e instanceof Player && !SpookyHelpers.untouchable(e);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		Item item = switch (random.nextInt(3)) {
			case 0 -> Items.OAK_LOG;
			case 1 -> Items.OAK_LEAVES;
			default -> Items.ROTTEN_FLESH;
		};
		int n = SpookyHelpers.fewItems(random, SpookyHelpers.looting(level, source));
		for (int i = 0; i < n; i++) spawnAtLocation(level, item);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("leaves_hit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("leaves_death");
	}

	@Override
	protected float getSoundVolume() {
		return 0.65F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/** LeafMonster.getCanSpawnHere: dark enough, at night, at y 50 or higher, at most 4 others within 20 blocks (or a spawner). */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!Monster.isDarkEnoughToSpawn(level, pos, random) || level.getLevel().isBrightOutside()) return false;
		// OreSpawn: in the Islands dimension (ground at y 8) at y 20 or lower; elsewhere at y 50 or higher.
		if (level.getLevel().dimension() == orespawn.world.OreSpawnDimensions.ISLANDS ? pos.getY() > 20 : pos.getY() < 50) return false;
		AABB box = type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(20.0, 10.0, 20.0);
		return level.getEntitiesOfClass(LeafMonster.class, box, e -> true).size() <= 4;
	}
}
