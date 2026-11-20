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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Pointysaurus: a horned, armored night wanderer that only hunts players (survival ones within 12 blocks),
 * plus whoever hurt it last (forgetting them 1 time in 250, or while it can't see them). It rams for 10, tossing its
 * victim. Cactus doesn't hurt it. 80 health, 16 armor. Drops 10 leather and 6 each of raw beef, rotten flesh and string.
 * Not ported: OreSpawn's fire resistance (it took 100 ticks in fire to catch light), which 26.3 has no setting for.
 */
public class Pointysaurus extends HuntingDinosaur {
	public Pointysaurus(EntityType<? extends Pointysaurus> type, Level level) {
		super(type, level, new Hunting(6, 250, 4.0F, 5, 12.0, 5.0, 0.8, true));
		this.xpReward = 40;
	}

	/** get_mobstats("Pointysaurus", 80, 10, 16). */
	public static AttributeSupplier.Builder createAttributes() {
		return createMonsterAttributes().add(Attributes.MAX_HEALTH, 80.0).add(Attributes.MOVEMENT_SPEED, 0.35)
			.add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, 16.0);
	}

	@Override
	protected boolean hunts(LivingEntity e) {
		return e instanceof Player;
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
		return 0.9F;
	}

	@Override
	public float getVoicePitch() {
		return 1.5F;
	}

	/** Pointysaurus.getCanSpawnHere: see {@link HuntingDinosaur#checkBigSpawn}; no limit on others nearby. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return checkBigSpawn(type, level, pos, random, 0, 0.0, 0.0);
	}
}
