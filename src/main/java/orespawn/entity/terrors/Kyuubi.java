package orespawn.entity.terrors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import orespawn.entity.Ignored;
import orespawn.registry.ModSounds;

/**
 * OreSpawn's Kyuubi: a nine-tailed fire fox of the Nether, wreathed in flame. Fire can't hurt it, it keeps catching
 * light and trailing red dust and lava sparks, and water burns it. Every half second or so it looks for the nearest
 * living thing within 12 blocks that isn't a monster (nor the small things hunters ignore, nor creative players), runs
 * at it and throws small fireballs when it's within 8 blocks. It panics when hurt. 125 health, 10 damage, 10 armor.
 * Drops 10 coal, 3 blocks of redstone and 4 blocks of quartz (loot table).
 * <p>
 * Not ported: OreSpawn's fire resistance setting (no 26.3 equivalent; it's fire immune anyway).
 */
public class Kyuubi extends Monster {
	public Kyuubi(EntityType<? extends Kyuubi> type, Level level) {
		super(type, level);
		this.xpReward = 30;
	}

	/** get_mobstats("Kyuubi", 125, 10, 10); speed 0.25. */
	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 125.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, 10.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new PanicGoal(this, 1.35F));
		goalSelector.addGoal(2, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
		goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0));
		goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 10.0F));
		goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (random.nextInt(10) != 1) return;
		if (level().isClientSide()) {
			level().addParticle(new DustParticleOptions(0xFF0000, 1.0F), getX(), getY() + 2.0, getZ(), 0.0, 0.0, 0.0);
			level().addParticle(ParticleTypes.LAVA, getX(), getY() + 2.0, getZ(), 0.0, 0.0, 0.0);
			if (isInWater()) {
				level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 1.75, getZ(), 0.0, 0.0, 0.0);
				level().addParticle(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.75, getZ(), 0.0, 0.0, 0.0);
				level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 2.0, getZ(), 0.0, 0.0, 0.0);
				level().addParticle(ParticleTypes.LARGE_SMOKE, getX(), getY() + 2.0, getZ(), 0.0, 0.0, 0.0);
			}
		} else if (level() instanceof ServerLevel level) {
			igniteForSeconds(5.0F);
			// In water it hits itself (OreSpawn's attackEntityAsMob(this)).
			if (isInWater()) hurtServer(level, damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE));
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		super.customServerAiStep(level);
		if (random.nextInt(10) == 1) {
			LivingEntity e = TerrorAi.findTarget(this, 12.0, 4.0, this::isSuitableTarget);
			if (e != null) {
				lookAt(e, 10.0F, 10.0F);
				getNavigation().moveTo(e, 1.25);
				if (distanceToSqr(e) < 64.0 && (random.nextInt(6) == 0 || random.nextInt(8) == 1)) {
					// EntitySmallFireball(world, shooter, dx, dy, dz): OreSpawn's fireball spread its aim by gaussian * 0.4.
					Vec3 aim = new Vec3(e.getX() - getX() + random.nextGaussian() * 0.4, e.getY() + 0.75 - (getY() + 1.25) + random.nextGaussian() * 0.4,
						e.getZ() - getZ() + random.nextGaussian() * 0.4);
					SmallFireball fireball = new SmallFireball(level, this, aim.normalize());
					fireball.snapTo(getX(), getY() + 1.25, getZ(), getYRot(), getXRot());
					playSound(SoundEvents.ARROW_SHOOT, 0.75F, 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F));
					level.addFreshEntity(fireball);
				}
			}
		}
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (Ignored.byHunters(e) || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Monster) return false;
		return !TerrorAi.protectedPlayer(e);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		return InteractionResult.PASS;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.get("kyuubi_living");
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("alo_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("alo_death");
	}

	@Override
	protected float getSoundVolume() {
		return 0.75F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/** Kyuubi.getCanSpawnHere: anywhere its spawn list puts it (the Nether), with no light check. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return true;
	}
}
