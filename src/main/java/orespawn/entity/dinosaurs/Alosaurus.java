package orespawn.entity.dinosaurs;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Alosaurus: a big night hunter. Every 5 ticks or so it looks for the nearest living thing within 12 blocks
 * (not other Alosauruses, Cryolophosauruses or Velocity Raptors, nor the small things hunters ignore, nor creative
 * players) and bites it for 18, flinging it away. 110 health, 8 armor. Drops 10 gold nuggets and 6 raw beef.
 * Not ported: OreSpawn's fire resistance (it took 100 ticks in fire to catch light), which 26.3 has no setting for.
 */
public class Alosaurus extends HuntingDinosaur {
	public Alosaurus(EntityType<? extends Alosaurus> type, Level level) {
		super(type, level, new Hunting(5, 0, 4.0F, 4, 12.0, 5.0, 1.2, false));
		this.xpReward = 40;
	}

	/** get_mobstats("Alosaurus", 110, 18, 8). */
	public static AttributeSupplier.Builder createAttributes() {
		return createMonsterAttributes().add(Attributes.MAX_HEALTH, 110.0).add(Attributes.MOVEMENT_SPEED, 0.35)
			.add(Attributes.ATTACK_DAMAGE, 18.0).add(Attributes.ARMOR, 8.0);
	}

	@Override
	protected boolean hunts(LivingEntity e) {
		return !(e instanceof Alosaurus || e instanceof Cryolophosaurus || e instanceof VelocityRaptor);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return random.nextInt(4) == 0 ? ModSounds.get("alo_living") : null;
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
		return 1.5F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/** Alosaurus.getCanSpawnHere: see {@link HuntingDinosaur#checkBigSpawn}; no other Alosaurus within 16 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return checkBigSpawn(type, level, pos, random, 0, 16.0, 8.0);
	}
}
