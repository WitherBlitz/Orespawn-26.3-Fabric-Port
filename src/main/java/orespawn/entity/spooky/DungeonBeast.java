package orespawn.entity.spooky;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * OreSpawn's Dungeon Beast: a low, long-tailed lizard beast of dark forests and dungeons. It goes for nearly anything alive
 * within 16 blocks (3 up or down) that it can see, snapping its jaws and lashing its tail as it bites. 65 health, 12
 * damage, 6 armor; immune to cactus and suffocation. Drops 0-2 of one thing (more with Looting): a Pink Tourmaline
 * Ingot, a Crystal Apple or an oak log, or nothing.
 * <p>
 * Not ported yet: its spawn rule in OreSpawn's Crystal dimension (only at y 25-28 under open air), which waits for the
 * dimension.
 */
public class DungeonBeast extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(DungeonBeast.class, EntityDataSerializers.BOOLEAN);
	/** DungeonBeast.isSuitableTarget leaves these alone (OreSpawn ids, so it works before they're all ported). */
	private static final Set<String> FRIENDS = Set.of("rat", "dungeon_beast", "rotator", "peacock", "irukandji", "skate", "whale", "flounder");

	public DungeonBeast(EntityType<? extends DungeonBeast> type, Level level) {
		super(type, level);
		this.xpReward = 60;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 65.0).add(Attributes.MOVEMENT_SPEED, 0.29)
			.add(Attributes.ATTACK_DAMAGE, 12.0).add(Attributes.ARMOR, 6.0).add(Attributes.FOLLOW_RANGE, 32.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, WanderGoal.wanderALot(this, 14, 1.0));
		goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(3, new RandomLookAroundGoal(this));
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
	protected int getFireImmuneTicks() {
		return 10;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(8) == 0) {
			LivingEntity prey = SpookyHelpers.closest(this, getBoundingBox().inflate(16.0, 3.0, 16.0), this::isPrey);
			if (prey != null) {
				if (distanceToSqr(prey) < 8.0) {
					entityData.set(ATTACKING, true);
					if (random.nextInt(7) == 0 || random.nextInt(8) == 1) doHurtTarget(level, prey);
				} else {
					getNavigation().moveTo(prey, 1.2);
				}
			} else {
				entityData.set(ATTACKING, false);
			}
		}
	}

	private boolean isPrey(LivingEntity e) {
		if (e == this || !e.isAlive() || SpookyHelpers.ignorable(e) || SpookyHelpers.untouchable(e)) return false;
		if (!getSensing().hasLineOfSight(e)) return false;
		return !SpookyHelpers.isOreSpawn(e, FRIENDS);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CACTUS)) return false;
		return super.hurtServer(level, source, damage);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		Item item = switch (random.nextInt(4)) {
			case 1 -> ModItems.lazy("pink_tourmaline_ingot");
			case 2 -> ModItems.lazy("crystal_apple");
			case 3 -> Items.OAK_LOG;
			default -> null;
		};
		if (item == null) return;
		int n = SpookyHelpers.fewItems(random, SpookyHelpers.looting(level, source));
		for (int i = 0; i < n; i++) spawnAtLocation(level, item);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("dbhit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("dbdead");
	}

	@Override
	protected float getSoundVolume() {
		return 0.8F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/** DungeonBeast.getCanSpawnHere: dark enough, as any monster (or from a spawner). */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return Monster.isDarkEnoughToSpawn(level, pos, random);
	}
}
