package orespawn.entity.bugs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import orespawn.OreSpawn;
import orespawn.content.Bugs;
import orespawn.entity.Ignored;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Emperor Scorpion: a fire-proof giant scorpion of deserts and savannas that comes out at night. It wanders,
 * and goes for almost anything alive within 24 blocks (not creepers, endermen or other scorpions), pinching and stinging
 * from 6 blocks: 35 damage, a 1 in 3 chance of poison (6 s on Easy, 4.5 s otherwise), and a hard shove. While it fights it now and then calls up
 * an ordinary Scorpion between itself and its prey. After any hit it ignores damage for 1.5 s, and cactus can't hurt it.
 * 350 health, 20 armor. Drops an Emperor Scorpion Scale, an item frame, obsidian, beef and some diamond gear, maybe The
 * Ultimate Sword or Bow.
 * <p>
 * OreSpawn's longer poison for Normal and Hard sat inside its Easy test and never applied, so it doesn't here either. Its
 * claw and tail animation is picked on the client ({@link #renderClaws}, {@link #renderTail}), as OreSpawn's RenderInfo
 * was. Damage that bypasses invulnerability (the void, /kill) still gets through its guard. Not ported yet: the
 * EnderKnight and EnderReaper (which it leaves alone), its Utopia dimension spawns.
 */
public class EmperorScorpion extends BugMonster {
	private static final Item[] GEAR = {Items.DIAMOND_SWORD, Items.DIAMOND_SHOVEL, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_HOE,
		Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS};
	private int hurtTimer;
	/** Client only (OreSpawn's RenderInfo ri1, ri2): which claws snap (1 left, 2 right, 3 both) and whether the tail strikes (1). */
	public int renderClaws;
	public int renderTail;

	public EmperorScorpion(EntityType<? extends EmperorScorpion> type, Level level) {
		super(type, level);
		this.xpReward = 200;
		setPathfindingMalus(PathType.WATER, -1.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 350.0).add(Attributes.MOVEMENT_SPEED, 0.35)
			.add(Attributes.ATTACK_DAMAGE, 35.0).add(Attributes.ARMOR, 20.0);
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
		return ModSounds.get("emperorscorpion_death");
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
			if (random.nextInt(100) == 0) setTarget(null);
			if (e == null) e = findTarget(24.0, 6.0, 24.0, this::isSuitableTarget);
			if (e != null) {
				lookAt(e, 10.0F, 10.0F);
				double reach = 6.0F + e.getBbWidth() / 2.0F;
				if (distanceToSqr(e) < reach * reach) {
					setAttacking(true);
					if (random.nextInt(4) == 0 || random.nextInt(6) == 1) {
						doHurtTarget(level, e);
						if (random.nextInt(3) == 1) level.playSound(null, e, ModSounds.get("scorpion_attack"), getSoundSource(), 1.4F, 1.0F);
						else level.playSound(null, e, ModSounds.get("scorpion_living"), getSoundSource(), 1.0F, 1.0F);
					}
				} else {
					getNavigation().moveTo(e, 1.2);
				}
				if (random.nextInt(20) == 1) {
					summon(level, BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id("scorpion")).orElse(null),
						(getX() + e.getX()) / 2.0 + random.nextInt(5) - random.nextInt(5), (getY() + e.getY()) / 2.0 + 1.01,
						(getZ() + e.getZ()) / 2.0 + random.nextInt(5) - random.nextInt(5));
				}
			} else {
				setAttacking(false);
			}
		}
		if (random.nextInt(100) == 1 && getHealth() < getMaxHealth()) heal(2.0F);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (!super.doHurtTarget(level, target)) return false;
		if (target instanceof LivingEntity living) {
			// OreSpawn: 6, or 8 on Easy (its 10 and 12 for Normal and Hard sat inside the Easy test, so never applied).
			int t = level.getDifficulty() == Difficulty.EASY ? 8 : 6;
			if (random.nextInt(3) == 1) living.addEffect(new MobEffectInstance(MobEffects.POISON, t * 15, 0), this);
			knock(target, 3.0, 0.2);
		}
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (hurtTimer > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
		if (source.is(DamageTypes.CACTUS)) return false;
		boolean ret = super.hurtServer(level, source, amount);
		hurtTimer = 30;
		if (source.getEntity() instanceof Mob attacker) {
			setTarget(attacker);
			getNavigation().moveTo(attacker, 1.2);
		}
		return ret;
	}

	/** EmperorScorpion.isSuitableTarget. */
	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || !getSensing().hasLineOfSight(e) || Ignored.byHunters(e)) return false;
		if (e instanceof Enderman || e instanceof Creeper || e instanceof EmperorScorpion) return false;
		if (BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).equals(OreSpawn.id("scorpion"))) return false;
		return !isCreativeOrSpectator(e);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		dropRand(level, Bugs.EMPEROR_SCORPION_SCALE, 5);
		dropRand(level, Items.ITEM_FRAME, 5);
		for (int i = 4 + random.nextInt(5); i > 0; i--) dropRand(level, Items.OBSIDIAN, 5);
		for (int i = 4 + random.nextInt(8); i > 0; i--) dropRand(level, Items.BEEF, 5);
		dropGear(level, ModItems.byName("ultimate_sword"), Items.DIAMOND, GEAR, ModItems.lazy("the_ultimate_bow"));
	}

	/**
	 * EmperorScorpion.getCanSpawnHere: open air 4x3x4 from 2 blocks up, dark, at night, y 50 or higher, and no other
	 * Emperor Scorpion within 20 blocks.
	 */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!allAir(level, pos, -2, 2, 2, 5, -2, 2)) return false;
		if (!isDarkEnoughToSpawn(level, pos, random) || level.getLevel().isBrightOutside() || pos.getY() < 50) return false;
		return noneNear(level, type, EmperorScorpion.class, pos, 20.0, 6.0, 20.0);
	}
}
