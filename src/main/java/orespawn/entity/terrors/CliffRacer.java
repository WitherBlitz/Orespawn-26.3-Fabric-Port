package orespawn.entity.terrors;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.entity.Flight;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Cliff Racer: a harmless flying critter of the high places (it spawns at y 50 or higher). It flies between
 * spots 5-14 blocks away that it can see, calling as it goes; it makes no sound when hurt or killed, other creatures can
 * push it but it never pushes them, and falls don't hurt it. 5 health. Drops 0-2 (more with Looting) of one of raw
 * chicken, uranium nuggets or titanium nuggets, 3 times in 8; nothing otherwise. Wheat puts it in love as OreSpawn's
 * did, but it has no young.
 * <p>
 * Not ported: OreSpawn's fire resistance setting (no 26.3 equivalent). OreSpawn spawned it only in its Islands and
 * Chaos dimensions.
 */
public class CliffRacer extends Animal {
	private BlockPos flightTarget;

	public CliffRacer(EntityType<? extends CliffRacer> type, Level level) {
		super(type, level);
		this.xpReward = 5;
		this.moveControl = TerrorAi.keepForward(this);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.MOVEMENT_SPEED, 0.33)
			.add(Attributes.ATTACK_DAMAGE, 1.0);
	}

	@Override
	public void tick() {
		super.tick();
		setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6, 1.0));
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (flightTarget == null) flightTarget = blockPosition();
		if (random.nextInt(300) == 0 || Flight.distSq(this, flightTarget) < 2.1F) {
			flightTarget = TerrorAi.newFlightTarget(this, 5, 10, 11, 5);
		}
		Flight.steer(this, flightTarget, 0.4, 0.3, 0.2, 0.4, 0.75F, 1.0F / 6.0F);
	}

	/** It never pushes anything (OreSpawn's collideWithEntity did nothing). */
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
	protected SoundEvent getAmbientSound() {
		return ModSounds.get("cliffracer");
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	protected @Nullable SoundEvent getDeathSound() {
		return null;
	}

	@Override
	protected float getSoundVolume() {
		return 0.45F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		// getDropItem picks one of eight (five of them nothing), then dropFewItems drops 0-2 of it.
		int i = random.nextInt(8);
		Item item = i == 0 ? Items.CHICKEN : i == 1 ? ModItems.URANIUM_NUGGET : i == 2 ? ModItems.TITANIUM_NUGGET : null;
		TerrorAi.dropFew(this, level, source, item);
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return true;
	}

	/** 1.7.10's EntityAnimal bred with wheat; OreSpawn's Cliff Racer kept that, but makes no young. */
	@Override
	public boolean isFood(ItemStack stack) {
		return stack.is(Items.WHEAT);
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}

	/** CliffRacer.getCanSpawnHere: at y 50 or higher. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return pos.getY() >= 50;
	}
}
