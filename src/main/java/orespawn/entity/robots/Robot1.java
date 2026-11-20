package orespawn.entity.robots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;

/**
 * OreSpawn's Robot1, the Bomb-Omb: a little wind-up bomb that walks after anything alive within 8 blocks (not
 * monsters, ignorable bugs or creative players), smoking and spitting lava sparks, and blows up (power 2.5, breaking
 * blocks only with mob griefing) when it gets within about 2 blocks. 5 health, 2 armor, fire immune, ignores cactus.
 * Drops gunpowder (loot table). Spawns at night at y 50 or higher, in the dark.
 */
public class Robot1 extends Monster {
	public Robot1(EntityType<? extends Robot1> type, Level level) {
		super(type, level);
		this.xpReward = 5;
		setPathfindingMalus(PathType.WATER, -1.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.MOVEMENT_SPEED, 0.2)
			.add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.ARMOR, 2.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, WanderGoal.wanderALot(this, 10, 1.0));
		goalSelector.addGoal(2, new MoveThroughVillageGoal(this, 0.9, false, 4, () -> false));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!(level() instanceof ServerLevel level) || !isAlive() || random.nextInt(8) != 0) return;
		LivingEntity e = RobotUtil.find(this, 8.0, 3.0, x -> RobotUtil.isPrey(this, x));
		if (e == null) return;
		if (distanceToSqr(e) < 5.0 && random.nextInt(18) == 1) {
			level.explode(this, getX(), getY(), getZ(), 2.5F, Level.ExplosionInteraction.MOB);
			discard();
			return;
		}
		for (int i = 0; i < 2; i++) {
			level.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.0, getZ(), 1, 0.0, 0.0, 0.0, 0.0);
			level.sendParticles(ParticleTypes.LAVA, getX(), getY() + 1.0, getZ(), 1, 0.0, 0.0, 0.0, 0.0);
		}
		getNavigation().moveTo(e, 1.2);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (source.is(DamageTypes.CACTUS)) return false;
		return super.hurtServer(level, source, amount);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.get("kyuubi_living");
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.SCORPION_HIT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("robot1_death");
	}

	@Override
	protected float getSoundVolume() {
		return 1.0F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/** Robot1.getCanSpawnHere: at y 50 or higher, at night, in the dark. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return pos.getY() >= 50 && !level.getLevel().isBrightOutside() && Monster.isDarkEnoughToSpawn(level, pos, random);
	}
}
