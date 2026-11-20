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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import orespawn.content.Oddities;
import orespawn.entity.Ignored;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Vortex: a whirlwind that comes out on dark nights above y 50. It chases the nearest creature within 16
 * blocks (not the sky and sea creatures OreSpawn spares, nor creative players), sucking it in from up to 9 blocks
 * (players twice as hard upwards) and striking it when close (get_mobstats("Vortex", 150, 26, 10)). A hit leaves it
 * winded for a second, so it stops pulling. It heals slowly, is fire-proof, and when not fighting may blow away by
 * day. Drops a Vortex Eye, an item frame and 5-11 odds and ends: sticks, Tiger's Eye and Pink Tourmaline ingots, iron
 * ingots, uranium and titanium nuggets, Dead Irukandji and Crystal Energy.
 * <p>
 * OreSpawn pulled with addVelocity, which reached a player's own game only when it was also hit; here the pull always
 * reaches players.
 */
public class Vortex extends Monster {
	private BlockPos flightTarget;
	private boolean busyFighting;
	private boolean spawnered;
	private int winded;

	public Vortex(EntityType<? extends Vortex> type, Level level) {
		super(type, level);
		this.xpReward = 200;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 150.0).add(Attributes.MOVEMENT_SPEED, 0.35)
			.add(Attributes.ATTACK_DAMAGE, 26.0).add(Attributes.ARMOR, 10.0);
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
	protected SoundEvent getAmbientSound() {
		return ModSounds.get("vortexlive");
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("vortexlive");
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
		busyFighting = false;
		LivingEntity e = Oddity.nearest(this, 16.0, 10.0, this::isSuitableTarget);
		if (e != null) {
			busyFighting = true;
			if (level.isClientSide()) {
				// A spinning funnel of smoke while it's after something.
				for (int i = 0; i < 20; i++) {
					double d = random.nextDouble() * 3.5;
					d *= d;
					double dir = random.nextDouble() * 2.0 * Math.PI - Math.PI;
					double dx = Math.cos(dir) * d / 2.0, dz = Math.sin(dir) * d / 2.0;
					dir += Math.PI / 2;
					level.addParticle(ParticleTypes.SMOKE, getX() + dx, getY() + 0.75 + d, getZ() + dz, Math.cos(dir) * random.nextFloat() / 4.0,
						random.nextFloat() / 2.0F, Math.sin(dir) * random.nextFloat() / 4.0);
				}
			}
		}
		if (!level.isClientSide()) {
			if (random.nextInt(200) == 1) heal(1.0F);
			if (!isPersistenceRequired() && !busyFighting && !spawnered) {
				long t = level.getOverworldClockTime() % 24000L;
				if (t < 12000L && random.nextInt(500) == 1) discard();
			}
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (flightTarget == null) flightTarget = blockPosition();
		if (winded > 0) winded--;
		if (random.nextInt(300) == 0 || blockPosition().distSqr(flightTarget) < 2.1) {
			for (int tries = 50; tries > 0; tries--) {
				int dz = random.nextInt(14) + 10, dx = random.nextInt(14) + 10;
				if (random.nextInt(2) == 0) dz = -dz;
				if (random.nextInt(2) == 0) dx = -dx;
				flightTarget = new BlockPos(getBlockX() + dx, getBlockY() + random.nextInt(6) - 3, getBlockZ() + dz);
				if (level.isEmptyBlock(flightTarget) && Oddity.canSee(this, flightTarget)) break;
			}
		}
		LivingEntity e = Oddity.nearest(this, 16.0, 10.0, this::isSuitableTarget);
		if (e != null) {
			flightTarget = e.blockPosition();
			double d = distanceToSqr(e);
			if (d < 81.0 && winded == 0) {
				double a = Math.atan2(getZ() - e.getZ(), getX() - e.getX());
				double pull = 10.0 - Math.sqrt(d);
				double pm = e instanceof Player ? 2.0 : 1.0;
				e.push(Math.cos(a) * pull * 0.1F, pull * 0.05F * pm, Math.sin(a) * pull * 0.1F);
				if (e instanceof Player) e.syncVelocity = true;
			}
			double reach = 4.0F + e.getBbWidth() / 2.0F;
			if (distanceToSqr(e) < reach * reach && random.nextInt(8) == 2) doHurtTarget(level, e);
		}
		Oddity.steer(this, flightTarget.getX() + 0.5, flightTarget.getY() + 0.1, flightTarget.getZ() + 0.5, 0.4, 0.7, 0.2, 0.2, 4.0F);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		boolean ret = super.hurtServer(level, source, damage);
		Entity e = source.getEntity();
		if (e != null && flightTarget != null) flightTarget = e.blockPosition();
		winded = 20;
		return ret;
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || Ignored.byHunters(e) || !hasLineOfSight(e) || Oddity.creative(e)) return false;
		if (e instanceof Vortex || e instanceof Rotator || e instanceof Mothra) return false;
		return !Oddity.is(e, "brutalfly", "peacock", "crystal_apple_cow", "irukandji", "skate", "whale", "flounder", "crystal_urchin");
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		drop(level, Oddities.VORTEX_EYE);
		drop(level, Items.ITEM_FRAME);
		for (int i = 5 + random.nextInt(7); i > 0; i--) {
			switch (random.nextInt(10)) {
				case 0 -> drop(level, Items.STICK);
				case 1 -> drop(level, ModItems.lazy("tigers_eye_ingot"));
				case 2 -> drop(level, ModItems.lazy("pink_tourmaline_ingot"));
				case 3 -> drop(level, Items.IRON_INGOT);
				case 4 -> drop(level, ModItems.URANIUM_NUGGET);
				case 6 -> drop(level, ModItems.TITANIUM_NUGGET);
				case 7 -> drop(level, ModItems.lazy("dead_irukandji"));
				case 8 -> drop(level, ModItems.lazy("crystal_energy"));
				default -> {
				}
			}
		}
	}

	/** Vortex.dropItemRand: within 5 blocks, 1-10 blocks up. */
	private void drop(ServerLevel level, net.minecraft.world.level.ItemLike item) {
		Oddity.dropRand(level, this, new net.minecraft.world.item.ItemStack(item), 6, 1.0 + random.nextInt(10));
	}

	/**
	 * Vortex.getCanSpawnHere: 5 x 3 x 5 blocks of air above, dark enough for a monster, at y 50 or higher, at night, half
	 * the time, and no other Vortex within 20 blocks. Spawners always may.
	 */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		for (int k = -2; k <= 2; k++) {
			for (int j = -2; j <= 2; j++) {
				for (int i = 1; i < 4; i++) {
					if (!level.isEmptyBlock(pos.offset(j, i, k))) return false;
				}
			}
		}
		if (!Monster.isDarkEnoughToSpawn(level, pos, random) || pos.getY() < 50) return false;
		if (level.getLevel().getOverworldClockTime() % 24000L < 12000L || random.nextInt(2) != 1) return false;
		return level.getEntitiesOfClass(Vortex.class, new AABB(pos).inflate(20.0, 16.0, 20.0)).isEmpty();
	}
}
