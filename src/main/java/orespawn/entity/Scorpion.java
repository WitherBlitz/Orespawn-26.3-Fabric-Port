package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * OreSpawn's scorpion: a small desert hunter (15 health, 4 attack, 10 armor, from Scorpion_stats). Every few ticks it
 * looks for prey within 8 blocks (3 up and down), creepers and big mobs counted as nearer: anything alive that isn't a
 * monster, plus spiders and creepers, but never other scorpions, the bugs and birds OreSpawn's hunters ignore, or
 * creative players. It runs at prey and stings within 3 blocks. Cactus doesn't hurt it. Spawns in the dark, and by
 * day only below y 51. Drops (loot table) one gold, uranium or titanium nugget kind, each a 1 in 10 chance.
 * <p>
 * Not ported yet: OreSpawn's VelocityRaptor (hunted), Emperor Scorpion, Ghost and Ghost Skelly (never hunted), and
 * the PlayNicely config (treated as off, OreSpawn's default).
 */
public class Scorpion extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Scorpion.class, EntityDataSerializers.BOOLEAN);
	/** ClientProxyOreSpawn's speed for ModelScorpion; the model swings claws and tail at 3x this. */
	private static final float MODEL_SPEED = 0.62F;
	/** Client side: OreSpawn's RenderInfo ri1 (which claws strike) and ri2 (1 = the tail strikes). */
	private int clawPick;
	private int tailPick;

	public Scorpion(EntityType<? extends Scorpion> type, Level level) {
		super(type, level);
		this.xpReward = 10;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 15.0).add(Attributes.MOVEMENT_SPEED, 0.2)
			.add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.ARMOR, 10.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
		goalSelector.addGoal(2, WanderGoal.wanderALot(this, 14, 1.0));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(4, new RandomLookAroundGoal(this));
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

	/**
	 * Client side, once per frame: ModelScorpion.render's random claw and tail strikes. Each time the strike swing passes
	 * zero it picks again (more often while attacking). Returns bit 0 left claw, bit 1 right claw, bit 2 tail.
	 */
	public int clawAnimation(float ageInTicks) {
		float speed = 3.0F * MODEL_SPEED;
		float now = Mth.cos(ageInTicks * speed);
		float next = Mth.cos((ageInTicks + 0.1F) * speed);
		if (next > 0.0F && now < 0.0F) {
			if (!attacking()) {
				clawPick = random.nextInt(20);
				tailPick = random.nextInt(25);
			} else {
				clawPick = random.nextInt(4);
				tailPick = random.nextInt(3);
			}
		}
		int bits = 0;
		if (clawPick == 1 || clawPick == 3) bits |= 1;
		if (clawPick == 2 || clawPick == 3) bits |= 2;
		if (tailPick == 1) bits |= 4;
		return bits;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(6) != 0) return;
		LivingEntity e = findSomethingToAttack(level);
		if (e != null) {
			if (distanceToSqr(e) < 9.0) {
				entityData.set(ATTACKING, true);
				if (random.nextInt(5) == 0 || random.nextInt(6) == 1) {
					doHurtTarget(level, e);
					if (random.nextInt(3) == 1) {
						level.playSound(null, e.getX(), e.getY(), e.getZ(), ModSounds.get("scorpion_attack"), getSoundSource(), 0.75F, 1.5F);
					}
				}
			} else {
				getNavigation().moveTo(e, 1.2);
			}
		} else {
			entityData.set(ATTACKING, false);
		}
	}

	private @Nullable LivingEntity findSomethingToAttack(ServerLevel level) {
		return level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(8.0, 3.0, 8.0), this::isSuitableTarget).stream()
			.min(Comparator.comparingDouble(this::targetWeight)).orElse(null);
	}

	/** OreSpawn's GenericTargetSorter: nearest first, creepers and big mobs counted as nearer. */
	private double targetWeight(Entity e) {
		double d = distanceToSqr(e);
		if (e instanceof Creeper) d /= 2.0;
		double size = e.getBbHeight() * e.getBbWidth();
		return size > 1.0 ? d / size : d;
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || Ignored.byHunters(e)) return false;
		if (!getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Spider) return true;
		if (e instanceof Scorpion) return false;
		if (e instanceof Creeper) return true;
		if (e instanceof Monster) return false;
		return !(e instanceof Player p) || !(p.isCreative() || p.isSpectator());
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypes.CACTUS)) return false;
		return super.hurtServer(level, source, damage);
	}

	/** OreSpawn's getCanSpawnHere replaced the monster spawn checks; checkSpawn has the whole rule. */
	@Override
	public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
		return true;
	}

	@Override
	protected int getFireImmuneTicks() {
		return 100;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("scorpion_hit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("cryo_death");
	}

	@Override
	protected float getSoundVolume() {
		return 1.5F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/** Scorpion.getCanSpawnHere: dark enough for a monster, and by day only below y 51. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return Monster.isDarkEnoughToSpawn(level, pos, random) && (!level.getLevel().isBrightOutside() || pos.getY() <= 50);
	}
}
