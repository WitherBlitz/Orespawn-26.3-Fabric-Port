package orespawn.entity.sea;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.entity.Bird;
import orespawn.entity.Butterfly;
import orespawn.entity.Firefly;
import orespawn.entity.Flight;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Cloud Shark: a small shark that swims through the sky, keeping between y 120 and 140. It picks a visible
 * open spot 8-17 blocks away whenever it reaches its goal (or 1 time in 300 a tick) and steers there the way OreSpawn's
 * flyers do. 1 time in 9 a tick it looks within 12 blocks for prey: players, butterflies and moths, birds, fireflies,
 * mosquitoes, goldfish and cliff racers; it heads for the nearest and bites for 6 within 3 blocks. Whatever hurts it
 * becomes its new goal. It takes no fall damage, isn't pushed by other mobs and breathes underwater. 15 health, 5
 * armour, 5 xp; despawns only at night. Drops 0-2 of one of paper, string or bone (a loot table). Natural spawns: only
 * in OreSpawn's Islands and Chaos dimensions (their ambient creatures, weight 1 and 2), which aren't ported here. Its
 * ambient "splash" named no sound in OreSpawn, so it only makes its hurt and death splats.
 */
public class CloudShark extends Monster {
	private @Nullable BlockPos flightTarget;

	public CloudShark(EntityType<? extends CloudShark> type, Level level) {
		super(type, level);
		this.xpReward = 5;
	}

	public static AttributeSupplier.Builder createAttributes() {
		// get_mobstats "CloudShark": 15 health, 6 attack, 5 defense.
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 15.0).add(Attributes.MOVEMENT_SPEED, 0.3)
			.add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ARMOR, 5.0);
	}

	@Override
	public void tick() {
		super.tick();
		setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6, 1.0));
	}

	private boolean canSee(BlockPos p) {
		Vec3 from = new Vec3(getX(), getY() + 0.75, getZ());
		return level().clip(new ClipContext(from, Vec3.atLowerCornerOf(p), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType()
			== HitResult.Type.MISS;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (flightTarget == null) flightTarget = blockPosition();
		int updown = getBlockY() < 120 ? 2 : getBlockY() > 140 ? -2 : 0;
		if (random.nextInt(300) == 0 || Flight.distSq(this, flightTarget) < 2.1) {
			for (int tries = 50; tries > 0; tries--) {
				int zdir = random.nextInt(10) + 8;
				int xdir = random.nextInt(10) + 8;
				if (random.nextInt(2) == 0) zdir = -zdir;
				if (random.nextInt(2) == 0) xdir = -xdir;
				flightTarget = BlockPos.containing(getX() + xdir, getY() + random.nextInt(5) - 2 + updown, getZ() + zdir);
				if (level.isEmptyBlock(flightTarget) && canSee(flightTarget)) break;
			}
		}
		if (random.nextInt(9) == 2) {
			LivingEntity e = SeaTargets.nearest(this, 12.0, 10.0, this::isSuitableTarget);
			if (e != null) {
				flightTarget = e.blockPosition();
				if (distanceToSqr(e) < 9.0) doHurtTarget(level, e);
			}
		}
		Flight.steer(this, flightTarget, 0.5, 0.30000000149011613, 0.20000000149011612, 0.5, 1.0F, 0.25F);
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (!SeaTargets.basic(this, e) || SeaTargets.is(e, "rock", "ant", "red_ant", "rainbow_ant", "unstable_ant", "termite")) return false;
		if (e instanceof Butterfly || e instanceof Bird || e instanceof Firefly) return true;
		if (SeaTargets.is(e, "mothra", "mosquito", "gold_fish", "cliff_racer")) return true;
		return e instanceof Player p && !p.isCreative() && !p.isSpectator();
	}

	/** CloudShark.attackEntityAsMob: a plain bite for its attack damage. */
	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		return target.hurtServer(level, damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE));
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		boolean hurt = super.hurtServer(level, source, damage);
		Entity e = source.getEntity();
		if (e != null && flightTarget != null) flightTarget = e.blockPosition();
		return hurt;
	}

	@Override
	public boolean canBreatheUnderwater() {
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
	public boolean removeWhenFarAway(double distSqr) {
		return !level().isBrightOutside();
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("little_splat");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("big_splat");
	}

	@Override
	protected float getSoundVolume() {
		return 0.25F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	@Override
	public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
		return true;
	}

	/** CloudShark.getCanSpawnHere: anywhere its biome lists it. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return true;
	}
}
