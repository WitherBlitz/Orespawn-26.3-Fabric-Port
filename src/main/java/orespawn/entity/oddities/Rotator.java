package orespawn.entity.oddities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import orespawn.entity.Ignored;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Rotator: a floating gyroscope of three spinning rings that comes out at night, sparking. It circles the
 * nearest creature within 12 blocks (not the sky and sea creatures OreSpawn spares, nor creative players), shooting
 * sparks at it and striking from up to 3 blocks (get_mobstats("Rotator", 35, 10, 8)). Arrows from dispensers pass
 * through it, it's fire-proof, and when not fighting it may vanish by day. Drops 0-2 (more with Looting) of one of: Pink Tourmaline
 * ingots, Tiger's Eye ingots, Crystal Energy or iron ingots.
 * <p>
 * OreSpawn's ambient sound for it ("vortexlive" without its "orespawn:") didn't exist, so it's silent here too. Its
 * spawner check ("one placed by a Rotator spawner never vanishes") counts any spawner spawn.
 */
public class Rotator extends Monster {
	private BlockPos flightTarget;
	private boolean busyFighting;
	private boolean spawnered;

	public Rotator(EntityType<? extends Rotator> type, Level level) {
		super(type, level);
		this.xpReward = 35;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 35.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, 8.0);
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
			@Nullable SpawnGroupData data) {
		if (EntitySpawnReason.isSpawner(reason)) spawnered = true;
		return super.finalizeSpawn(level, difficulty, reason, data);
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isPersistenceRequired() && !busyFighting && !spawnered;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("Spawnered", spawnered);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		spawnered = input.getBooleanOr("Spawnered", false);
	}

	@Override
	protected float getSoundVolume() {
		return 0.75F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("glasshit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("glassdead");
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6, 1.0));
		Level level = level();
		if (level.isClientSide() && random.nextInt(10) == 1) {
			level.addParticle(ParticleTypes.FIREWORK, getX(), getY() + 1.4, getZ(), (random.nextFloat() - random.nextFloat()) / 4.0F,
				(random.nextFloat() - random.nextFloat()) / 4.0F, (random.nextFloat() - random.nextFloat()) / 4.0F);
		}
		busyFighting = false;
		LivingEntity e = Oddity.nearest(this, 12.0, 10.0, this::isSuitableTarget);
		if (e != null) {
			if (level.isClientSide()) {
				double a = Math.atan2(e.getZ() - getZ(), e.getX() - getX());
				level.addParticle(ParticleTypes.FIREWORK, getX(), getY() + 1.4, getZ(), Math.cos(a), (e.getY() - getY()) / 10.0, Math.sin(a));
			}
			busyFighting = true;
		}
		if (!level.isClientSide() && !isPersistenceRequired() && !busyFighting && !spawnered) {
			long t = level.getOverworldClockTime() % 24000L;
			if (t < 12000L && random.nextInt(400) == 1) discard();
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (flightTarget == null) flightTarget = blockPosition();
		if (random.nextInt(300) != 0 && !(blockPosition().distSqr(flightTarget) < 2.1)) {
			if (random.nextInt(9) == 2) {
				LivingEntity e = Oddity.nearest(this, 12.0, 10.0, this::isSuitableTarget);
				if (e != null) {
					// Circle it: aim a quarter turn round from it, 2.5 blocks out.
					double a = Math.atan2(e.getZ() - getZ(), e.getX() - getX()) + Math.PI / 2;
					flightTarget = BlockPos.containing(e.getX() + 2.5 * Math.cos(a), e.getY(), e.getZ() + 2.5 * Math.sin(a));
					if (distanceToSqr(e) < 9.0) doHurtTarget(level, e);
				}
			}
		} else {
			for (int tries = 50; tries > 0; tries--) {
				int dz = random.nextInt(10) + 8, dx = random.nextInt(10) + 8;
				if (random.nextInt(2) == 0) dz = -dz;
				if (random.nextInt(2) == 0) dx = -dx;
				flightTarget = new BlockPos(getBlockX() + dx, getBlockY() + random.nextInt(6) - 3, getBlockZ() + dz);
				if (level.isEmptyBlock(flightTarget) && Oddity.canSee(this, flightTarget)) break;
			}
		}
		Oddity.steer(this, flightTarget.getX() + 0.5, flightTarget.getY() + 0.1, flightTarget.getZ() + 0.5, 0.4, 0.7, 0.2, 0.2, 4.0F);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		Entity e = source.getEntity();
		// OreSpawn: "if the source entity is an arrow" - true only for arrows with no shooter (dispensers).
		if (source.getDirectEntity() instanceof AbstractArrow && e == null) return false;
		boolean ret = super.hurtServer(level, source, damage);
		if (e != null && flightTarget != null) flightTarget = e.blockPosition();
		return ret;
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || Ignored.byHunters(e) || !hasLineOfSight(e) || Oddity.creative(e)) return false;
		if (e instanceof Vortex || e instanceof Rotator || e instanceof Mothra) return false;
		return !Oddity.is(e, "termite", "dungeon_beast", "peacock", "crystal_apple_cow", "irukandji", "skate", "whale", "flounder",
			"crystal_urchin", "terrible_terror", "lurking_terror", "cloud_shark", "bee", "mantis");
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		ItemStack drop = switch (random.nextInt(4)) {
			case 0 -> new ItemStack(ModItems.lazy("pink_tourmaline_ingot"));
			case 1 -> new ItemStack(ModItems.lazy("tigers_eye_ingot"));
			case 2 -> new ItemStack(ModItems.lazy("crystal_energy"));
			default -> new ItemStack(Items.IRON_INGOT);
		};
		for (int n = Oddity.fewItems(this, source); n > 0; n--) spawnAtLocation(level, drop.copy());
	}

	/**
	 * Rotator.getCanSpawnHere: dark enough for a monster, 3 x 2 x 3 blocks of air above, and night time. Spawners always
	 * may.
	 */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!Monster.isDarkEnoughToSpawn(level, pos, random)) return false;
		for (int k = -1; k <= 1; k++) {
			for (int j = -1; j <= 1; j++) {
				for (int i = 1; i < 3; i++) {
					if (!level.isEmptyBlock(pos.offset(j, i, k))) return false;
				}
			}
		}
		return level.getLevel().getOverworldClockTime() % 24000L >= 12000L;
	}
}
