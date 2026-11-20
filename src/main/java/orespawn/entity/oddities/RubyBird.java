package orespawn.entity.oddities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import orespawn.world.OreSpawnDimensions;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Ruby Bird: the ruby-coloured sixth kind of OreSpawn's bird (Cockateil), the one its Ruby Bird dungeons'
 * spawners make. It flies like the bird but keeps climbing (its new spots are 2 higher and closer in), and sings its
 * own song in fair weather by day (and keeps higher still in OreSpawn's Islands dimension). Drops 0-2 feathers, or 0-2 rubies one time in three when a player kills it.
 */
public class RubyBird extends Animal {
	private static final int FLY_UP = 2;
	private BlockPos flightTarget;
	private int stuckCount;
	private int lastX;
	private int lastZ;

	public RubyBird(EntityType<? extends RubyBird> type, Level level) {
		super(type, level);
		this.xpReward = 2;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 2.0).add(Attributes.MOVEMENT_SPEED, 0.33)
			.add(Attributes.ATTACK_DAMAGE, 1.0);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return level().isBrightOutside() && !level().isRaining() ? ModSounds.get("rubybird") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.DUCK_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.DUCK_HURT;
	}

	@Override
	protected float getSoundVolume() {
		return 0.55F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isPersistenceRequired();
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return false;
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}

	@Override
	public void tick() {
		super.tick();
		if (flightTarget == null) flightTarget = blockPosition();
		else setDeltaMovement(getDeltaMovement().multiply(1.0, getY() < flightTarget.getY() ? 0.7 : 0.5, 1.0));
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (lastX == getBlockX() && lastZ == getBlockZ()) {
			stuckCount++;
		} else {
			stuckCount = 0;
			lastX = getBlockX();
			lastZ = getBlockZ();
		}
		if (flightTarget == null) flightTarget = blockPosition();
		// Cockateil: in OreSpawn's Islands dimension birds keep higher.
		int stayUp = level.dimension() == OreSpawnDimensions.ISLANDS ? 2 : 0;
		if (stuckCount > 40 || random.nextInt(250) == 0 || blockPosition().distSqr(flightTarget) < 4.1) {
			stuckCount = 0;
			for (int tries = 35; tries > 0; tries--) {
				int dz = random.nextInt(8) + 5 - FLY_UP * 2, dx = random.nextInt(8) + 5 - FLY_UP * 2;
				if (random.nextInt(2) == 0) dz = -dz;
				if (random.nextInt(2) == 0) dx = -dx;
				BlockPos p = new BlockPos(getBlockX() + dx, getBlockY() + random.nextInt(9 + stayUp) - 5 + FLY_UP, getBlockZ() + dz);
				flightTarget = p;
				if (level.isEmptyBlock(p) && Oddity.canSee(this, p)) break;
			}
		}
		Oddity.steer(this, flightTarget.getX() + 0.3, flightTarget.getY() + 0.1, flightTarget.getZ() + 0.3, 0.3, 0.699999, 0.25, 0.200000001,
			3.0F);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		// Cockateil.getDropItem for the sixth kind: rubies instead of feathers one time in three when a player killed it.
		ItemStack drop = new ItemStack(killedByPlayer && random.nextInt(3) == 1 ? ModItems.RUBY : Items.FEATHER);
		for (int n = Oddity.fewItems(this, source); n > 0; n--) spawnAtLocation(level, drop.copy());
	}
}
