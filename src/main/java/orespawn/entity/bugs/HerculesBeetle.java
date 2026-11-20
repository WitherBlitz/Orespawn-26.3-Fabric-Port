package orespawn.entity.bugs;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import orespawn.entity.Ignored;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Hercules Beetle: a fire-proof giant beetle of the hills that comes out at night. It wanders, and goes for
 * almost anything alive within 16 blocks (not creepers, other Hercules Beetles, or the small things hunters ignore),
 * butting it with its horns from 5 blocks: 30 damage and a big toss into the air. After any hit it ignores damage for a
 * second, and cactus can't hurt it. 250 health, 19 armor. Drops the Big Hammer, an item frame, beef and some diamond
 * gear.
 * <p>
 * Damage that bypasses invulnerability (the void, /kill) still gets through its one-second guard. Not ported yet: its
 * Utopia dimension spawns.
 */
public class HerculesBeetle extends BugMonster {
	private static final Item[] GEAR = {Items.DIAMOND_SWORD, Items.DIAMOND_SHOVEL, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_HOE,
		Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS};
	private int hurtTimer;

	public HerculesBeetle(EntityType<? extends HerculesBeetle> type, Level level) {
		super(type, level);
		this.xpReward = 200;
		setPathfindingMalus(PathType.WATER, -1.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 250.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 30.0).add(Attributes.ARMOR, 19.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 0.9, false, 4, () -> false));
		goalSelector.addGoal(2, WanderGoal.wanderALot(this, 14, 1.0));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("alo_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("hercules_death");
	}

	@Override
	protected float getSoundVolume() {
		return 1.5F;
	}

	@Override
	public void jumpFromGround() {
		super.jumpFromGround();
		setDeltaMovement(getDeltaMovement().add(0.0, 0.25, 0.0));
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (hurtTimer > 0) hurtTimer--;
		if (random.nextInt(4) == 0) {
			LivingEntity e = getTarget();
			if (e != null && !e.isAlive()) {
				setTarget(null);
				e = null;
			}
			if (e == null) e = findTarget(16.0, 6.0, 16.0, this::isSuitableTarget);
			if (e != null) {
				lookAt(e, 10.0F, 10.0F);
				double reach = 5.0F + e.getBbWidth() / 2.0F;
				if (distanceToSqr(e) < reach * reach) {
					setAttacking(true);
					if (random.nextInt(3) == 0 || random.nextInt(4) == 1) {
						doHurtTarget(level, e);
						if (random.nextInt(3) == 1) level.playSound(null, e, ModSounds.get("scorpion_attack"), getSoundSource(), 1.4F, 1.0F);
						else level.playSound(null, e, ModSounds.get("scorpion_living"), getSoundSource(), 1.0F, 1.0F);
					}
				} else {
					getNavigation().moveTo(e, 1.2);
				}
			} else {
				setAttacking(false);
			}
		}
		if (random.nextInt(150) == 1 && getHealth() < getMaxHealth()) heal(2.0F);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (!super.doHurtTarget(level, target)) return false;
		if (target instanceof LivingEntity) knock(target, 0.45, 1.25 * Math.abs(random.nextFloat()));
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (hurtTimer > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
		if (source.is(DamageTypes.CACTUS)) return false;
		boolean ret = super.hurtServer(level, source, amount);
		hurtTimer = 20;
		if (source.getEntity() instanceof Mob attacker) {
			setTarget(attacker);
			getNavigation().moveTo(attacker, 1.2);
		}
		return ret;
	}

	/** HerculesBeetle.isSuitableTarget. */
	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || Ignored.byHunters(e) || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Creeper || e instanceof HerculesBeetle) return false;
		return !isCreativeOrSpectator(e);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		dropRand(level, ModItems.lazy("big_hammer"), 5);
		dropRand(level, Items.ITEM_FRAME, 5);
		for (int i = 4 + random.nextInt(8); i > 0; i--) dropRand(level, Items.BEEF, 5);
		dropGear(level, null, Items.DIAMOND, GEAR, null);
	}

	/**
	 * HerculesBeetle.getCanSpawnHere: dark, at night, y 50 or higher, open air 4x3x4 from 2 blocks up, and no other
	 * Hercules Beetle within 16 blocks.
	 */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!isDarkEnoughToSpawn(level, pos, random) || level.getLevel().isBrightOutside() || pos.getY() < 50) return false;
		if (!allAir(level, pos, -2, 2, 2, 5, -2, 2)) return false;
		return noneNear(level, type, HerculesBeetle.class, pos, 16.0, 6.0, 16.0);
	}
}
