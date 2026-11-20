package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's large worm (90 health, 18 attack, 14 armor, from WormLarge_stats; 2050 XP): the first time it ticks it
 * spawns a nest of 20 small and 20 medium worms around itself (not when it came from a spawner). It stays buried
 * while any medium worm is within 8 blocks, so it comes out last. With a player within 8 blocks it rises out of the
 * ground, faces them and walks like a normal mob (wandering, moving through villages, looking around), chasing a
 * non-creative player within 8 blocks (6 up or down). 1 in 10 ticks within 3 blocks it bites; then 1 in 4 it pulls
 * off their helmet (or, with none, their chestplate) and 1 in 4 the item in their main hand, wears each down by a
 * tenth of what's left and throws it up out of the ground nearby. With no player near it sinks back underground.
 * Fall damage only out of the ground. Drops a Worm Tooth, an item frame, 6 rotten flesh, 6 leather, 8 dirt, 16 gold nuggets,
 * 5 diamonds, 4 uranium nuggets and 4 titanium nuggets, thrown up around it. Spawns at y 50 or above, with no other
 * large worm within 32 blocks, on solid ground (no air 2 to 8 blocks under the 13 x 13 area) under open sky (only air
 * 2 to 8 blocks over it).
 * <p>
 * Not ported yet: the PlayNicely config (treated as off, OreSpawn's default).
 */
public class WormLarge extends Worm {
	private boolean wormsSpawned;

	public WormLarge(EntityType<? extends WormLarge> type, Level level) {
		super(type, level, 0.85);
		this.xpReward = 2050;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 90.0).add(Attributes.MOVEMENT_SPEED, 0.2)
			.add(Attributes.ATTACK_DAMAGE, 18.0).add(Attributes.ARMOR, 14.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
		goalSelector.addGoal(2, WanderGoal.wanderALot(this, 16, 1.0));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(4, new RandomLookAroundGoal(this));
	}

	@Override
	public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
			@Nullable SpawnGroupData data) {
		// OreSpawn: a large worm from its spawner comes without a nest.
		if (EntitySpawnReason.isSpawner(reason)) wormsSpawned = true;
		return super.finalizeSpawn(level, difficulty, reason, data);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("wormsSpawned", wormsSpawned ? 1 : 0);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		wormsSpawned = input.getIntOr("wormsSpawned", 0) != 0;
	}

	/** WormLarge.onLivingUpdate: burrowing, coming out, and the nest. */
	@Override
	public void aiStep() {
		super.aiStep();
		if (!(level() instanceof ServerLevel level)) return;
		WormMedium medium = nearest(WormMedium.class, getBoundingBox().inflate(8.0, 8.0, 8.0));
		Player target = medium == null ? nearestPlayer(8.0, 8.0, 8.0) : null;
		if (medium != null || target == null) {
			noPhysics = true;
			if (!dig(Mth.floor(getY() + 3.5), 0.1, 0.05)) return;
		} else {
			pointAt(target);
			if (!isOpen(probe(Mth.floor(getY())))) {
				setDeltaMovement(getDeltaMovement().add(0.0, 0.25, 0.0));
				setPos(getX(), getY() + 0.1, getZ());
			} else {
				noPhysics = false;
			}
		}
		if (noPhysics) holdStill();
		if (!wormsSpawned) {
			wormsSpawned = true;
			for (int i = 0; i < 20; i++) {
				spawnWorm(level, ModEntities.SMALL_WORM, getX() + random.nextInt(6) - random.nextInt(6),
					getZ() + random.nextInt(6) - random.nextInt(6));
				spawnWorm(level, ModEntities.MEDIUM_WORM, getX() + random.nextInt(5) - random.nextInt(5),
					getZ() + random.nextInt(5) - random.nextInt(5));
			}
		}
	}

	private void spawnWorm(ServerLevel level, EntityType<? extends Worm> type, double x, double z) {
		Worm worm = type.create(level, EntitySpawnReason.MOB_SUMMONED);
		if (worm == null) return;
		worm.snapTo(x, getY(), z, random.nextFloat() * 360.0F, 0.0F);
		level.addFreshEntity(worm);
	}

	/** WormLarge.updateAITasks: chasing, biting, and pulling off helmet, chestplate and held item. */
	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (nearest(WormMedium.class, getBoundingBox().inflate(8.0, 8.0, 8.0)) != null) return;
		Player target = nearestVictim(8.0, 6.0, 8.0);
		if (target == null) return;
		pointAt(target);
		getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.0);
		if (random.nextInt(10) == 1 && distanceTo(target) < 3.0F) {
			doHurtTarget(level, target);
			if (random.nextInt(4) == 1 && !strip(level, target, EquipmentSlot.HEAD, 10)) strip(level, target, EquipmentSlot.CHEST, 10);
			if (random.nextInt(4) == 1) strip(level, target, EquipmentSlot.MAINHAND, 10);
		}
	}

	/** OreSpawn ran the large worm's AI goals only out of the ground; buried, it doesn't walk or jump. */
	@Override
	public void travel(Vec3 input) {
		super.travel(noPhysics ? Vec3.ZERO : input);
	}

	@Override
	public void jumpFromGround() {
		if (!noPhysics) super.jumpFromGround();
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
		if (!noPhysics) super.checkFallDamage(ya, onGround, onState, pos);
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
		return !noPhysics && super.causeFallDamage(fallDistance, damageModifier, damageSource);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		dropAround(level, new ItemStack(ModItems.lazy("worm_tooth")), 4);
		dropAround(level, new ItemStack(Items.ITEM_FRAME), 4);
		for (int i = 0; i < 6; i++) dropAround(level, new ItemStack(Items.ROTTEN_FLESH), 4);
		for (int i = 0; i < 6; i++) dropAround(level, new ItemStack(Items.LEATHER), 4);
		for (int i = 0; i < 8; i++) dropAround(level, new ItemStack(Items.DIRT), 4);
		for (int i = 0; i < 16; i++) dropAround(level, new ItemStack(Items.GOLD_NUGGET), 4);
		for (int i = 0; i < 5; i++) dropAround(level, new ItemStack(Items.DIAMOND), 4);
		for (int i = 0; i < 4; i++) dropAround(level, new ItemStack(ModItems.URANIUM_NUGGET), 4);
		for (int i = 0; i < 4; i++) dropAround(level, new ItemStack(ModItems.TITANIUM_NUGGET), 4);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("big_splat");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("alo_death");
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/**
	 * WormLarge.getCanSpawnHere: at y 50 or above, no other large worm within 32 blocks (8 up or down), no air 2 to 8
	 * blocks under the 13 x 13 area around it, and only air 2 to 8 blocks over it. While a chunk generates, only the part of
	 * that area inside the chunk is checked: its neighbours may not have their terrain yet, and 26.3 flags those reads.
	 */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (pos.getY() < 50) return false;
		AABB box = type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(32.0, 8.0, 32.0);
		if (!level.getEntitiesOfClass(WormLarge.class, box, e -> true).isEmpty()) return false;
		var center = level instanceof net.minecraft.server.level.WorldGenRegion region ? region.getCenter() : null;
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int i = -6; i <= 6; i++) {
			for (int j = -6; j <= 6; j++) {
				if (center != null && ((pos.getX() + i) >> 4 != center.x() || (pos.getZ() + j) >> 4 != center.z())) continue;
				for (int k = -2; k >= -8; k--) {
					if (level.getBlockState(p.setWithOffset(pos, i, k, j)).isAir()) return false;
				}
			}
		}
		for (int i = -6; i <= 6; i++) {
			for (int j = -6; j <= 6; j++) {
				if (center != null && ((pos.getX() + i) >> 4 != center.x() || (pos.getZ() + j) >> 4 != center.z())) continue;
				for (int k = 2; k <= 8; k++) {
					if (!level.getBlockState(p.setWithOffset(pos, i, k, j)).isAir()) return false;
				}
			}
		}
		return true;
	}
}
