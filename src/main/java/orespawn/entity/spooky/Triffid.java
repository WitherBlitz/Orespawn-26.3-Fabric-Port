package orespawn.entity.spooky;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Spooky;
import orespawn.registry.ModSounds;

import java.util.Set;

/**
 * OreSpawn's Triffid: a huge carnivorous plant that barely moves. It turns to face anything alive within 10 blocks (8 up
 * or down) that it can see, opens its leafy mouth and whips its tongue at it, hitting for 20 from up to 5 blocks away.
 * It can only be hurt while its mouth is open: any hit, landed or not, closes it for 15 seconds, and it won't move
 * meanwhile. 100 health, 12 armor, heals slowly. Drops 4-9 Green Goo and an item frame, scattered around it. Spawns
 * anywhere (it has no natural spawns of its own; its egg ore and spawners place it).
 * <p>
 * Changed: damage that ignores invulnerability (the void, /kill) always gets through, so a closed Triffid can still be
 * killed by commands; in OreSpawn it couldn't.
 */
public class Triffid extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Triffid.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> OPEN = SynchedEntityData.defineId(Triffid.class, EntityDataSerializers.BOOLEAN);
	/** Triffid.isSuitableTarget leaves these alone (OreSpawn ids, so it works before they're ported). */
	private static final Set<String> FRIENDS = Set.of("ender_reaper", "triffid", "terrible_terror", "lurking_terror", "nightmare", "dragon");
	private int hurtTimer;

	public Triffid(EntityType<? extends Triffid> type, Level level) {
		super(type, level);
		this.xpReward = 50;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 100.0).add(Attributes.MOVEMENT_SPEED, 0.13)
			.add(Attributes.ATTACK_DAMAGE, 20.0).add(Attributes.ARMOR, 12.0).add(Attributes.FOLLOW_RANGE, 32.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));
		goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
		builder.define(OPEN, false);
	}

	/** Whipping its tongue at something. */
	public boolean attacking() {
		return entityData.get(ATTACKING);
	}

	/** Its mouth is open (the only time it can be hurt). */
	public boolean open() {
		return entityData.get(OPEN);
	}

	@Override
	protected int getFireImmuneTicks() {
		return 75;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (!(level() instanceof ServerLevel level)) return;
		if (random.nextInt(100) == 1) {
			// Shuffles a little towards whichever side has more ground under it, 5 blocks either way.
			BlockPos below = blockPosition().below();
			int ix = below.getX();
			int iz = below.getZ();
			for (int k = -5; k <= 5; k++) {
				if (!level.isEmptyBlock(below.offset(0, 0, k))) iz += Integer.signum(k);
			}
			for (int k = -5; k <= 5; k++) {
				if (!level.isEmptyBlock(below.offset(k, 0, 0))) ix += Integer.signum(k);
			}
			getNavigation().moveTo(ix, getY(), iz, 1.0);
		}
		if (hurtTimer <= 0) {
			LivingEntity prey = SpookyHelpers.closest(this, getBoundingBox().inflate(10.0, 8.0, 10.0), this::isPrey);
			if (prey != null) face(prey);
		}
	}

	private void face(LivingEntity e) {
		float yaw = (float) Math.toDegrees(Math.atan2(e.getZ() - getZ(), e.getX() - getX())) - 90.0F;
		while (yaw < 0.0F) yaw += 360.0F;
		setYRot(yaw);
		setYHeadRot(yaw);
		setYBodyRot(yaw);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!level().isClientSide() && hurtTimer > 0) setDeltaMovement(0.0, getDeltaMovement().y, 0.0);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return super.hurtServer(level, source, damage);
		boolean wasOpen = hurtTimer <= 0 && open();
		hurtTimer = 300;
		entityData.set(OPEN, false);
		entityData.set(ATTACKING, false);
		return wasOpen && super.hurtServer(level, source, damage);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (hurtTimer > 0) {
			hurtTimer--;
			entityData.set(OPEN, false);
		}
		if (random.nextInt(250) == 1 && getHealth() < getMaxHealth()) heal(1.0F);
		if (random.nextInt(80) == 2 && hurtTimer <= 0) entityData.set(OPEN, random.nextInt(8) == 1);
		if (random.nextInt(10) == 1 && hurtTimer <= 0) {
			LivingEntity prey = SpookyHelpers.closest(this, getBoundingBox().inflate(10.0, 8.0, 10.0), this::isPrey);
			if (prey != null) {
				entityData.set(OPEN, true);
				if (distanceToSqr(prey) < 25.0) {
					face(prey);
					entityData.set(ATTACKING, true);
					doHurtTarget(level, prey);
				} else {
					entityData.set(ATTACKING, false);
				}
			} else {
				entityData.set(ATTACKING, false);
			}
		}
	}

	private boolean isPrey(LivingEntity e) {
		if (e == this || !e.isAlive() || SpookyHelpers.ignorable(e) || SpookyHelpers.untouchable(e)) return false;
		if (!getSensing().hasLineOfSight(e)) return false;
		return !(e instanceof Creeper) && !SpookyHelpers.isOreSpawn(e, FRIENDS);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		int n = 4 + random.nextInt(6);
		for (int i = 0; i < n; i++) dropScattered(level, Spooky.GREEN_GOO);
		dropScattered(level, Items.ITEM_FRAME);
	}

	/** Triffid.dropItemRand: up to 2 blocks off to each side, a block up. */
	private void dropScattered(ServerLevel level, Item item) {
		Vec3 at = new Vec3(getX() + random.nextInt(3) - random.nextInt(3), getY() + 1.0, getZ() + random.nextInt(3) - random.nextInt(3));
		ItemEntity drop = new ItemEntity(level, at.x, at.y, at.z, new ItemStack(item));
		drop.setDefaultPickUpDelay();
		level.addFreshEntity(drop);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.get("triffid_living");
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("triffid_hit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("triffid_dead");
	}

	@Override
	protected float getSoundVolume() {
		return 0.75F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/** Triffid.getCanSpawnHere: anywhere. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return true;
	}
}
