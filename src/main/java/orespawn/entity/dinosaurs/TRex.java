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
 * OreSpawn's T. Rex: a fast, huge night hunter. It goes for whoever hurt it last (forgetting them 1 time in 200, or
 * while it can't see them), otherwise the nearest living thing within 20 blocks (not other T. Rexes, Cryolophosauruses
 * or Velocity Raptors, nor the small things hunters ignore, nor creative players), and bites for 22, flinging its
 * victim away. Cactus doesn't hurt it. 160 health, 14 armor, 150 xp. Drops a T. Rex tooth, an item frame, 7 raw beef
 * and 2-5 each of uranium and titanium nuggets.
 * Not ported: OreSpawn's fire resistance (it took 100 ticks in fire to catch light), which 26.3 has no setting for.
 */
public class TRex extends HuntingDinosaur {
	public TRex(EntityType<? extends TRex> type, Level level) {
		super(type, level, new Hunting(5, 200, 4.0F, 4, 20.0, 6.0, 1.2, true));
		this.xpReward = 150;
	}

	/** get_mobstats("TRex", 160, 22, 14). */
	public static AttributeSupplier.Builder createAttributes() {
		return createMonsterAttributes().add(Attributes.MAX_HEALTH, 160.0).add(Attributes.MOVEMENT_SPEED, 0.38)
			.add(Attributes.ATTACK_DAMAGE, 22.0).add(Attributes.ARMOR, 14.0);
	}

	@Override
	protected boolean hunts(LivingEntity e) {
		return !(e instanceof TRex || e instanceof Cryolophosaurus || e instanceof VelocityRaptor);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return random.nextInt(4) == 0 ? ModSounds.get("trex_living") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("alo_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("trex_death");
	}

	@Override
	protected float getSoundVolume() {
		return 1.5F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/** TRex.getCanSpawnHere: see {@link HuntingDinosaur#checkBigSpawn} (a 3x3 column of air); no other T. Rex within 24 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return checkBigSpawn(type, level, pos, random, 1, 24.0, 12.0);
	}
}
