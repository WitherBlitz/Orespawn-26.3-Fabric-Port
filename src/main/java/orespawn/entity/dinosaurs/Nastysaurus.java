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
 * OreSpawn's Nastysaurus: the biggest of the night hunters. It goes for whoever hurt it last (forgetting them 1 time in
 * 250, or while it can't see them), otherwise the nearest living thing within 32 blocks (not other Nastysauruses,
 * Cryolophosauruses or Velocity Raptors, nor the small things hunters ignore, nor creative players), and bites for 32
 * from 4.5 blocks away, flinging its victim. Cactus doesn't hurt it. 200 health, 17 armor. Drops 10 each of iron
 * ingots, rotten flesh, leather and string. Its jaws snap now and then even when it isn't hunting.
 * Not ported: OreSpawn's fire resistance (it took 100 ticks in fire to catch light), which 26.3 has no setting for.
 */
public class Nastysaurus extends HuntingDinosaur {
	/** Client only: OreSpawn's RenderInfo for the idle jaw snap (the last jaw phase seen, and whether this cycle snaps). */
	private float jawPhase;
	private boolean jawSnap;

	public Nastysaurus(EntityType<? extends Nastysaurus> type, Level level) {
		super(type, level, new Hunting(5, 250, 4.5F, 4, 32.0, 8.0, 1.2, true));
		this.xpReward = 40;
	}

	/** get_mobstats("Nastysaurus", 200, 32, 17). */
	public static AttributeSupplier.Builder createAttributes() {
		return createMonsterAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.MOVEMENT_SPEED, 0.35)
			.add(Attributes.ATTACK_DAMAGE, 32.0).add(Attributes.ARMOR, 17.0);
	}

	@Override
	protected boolean hunts(LivingEntity e) {
		return !(e instanceof Nastysaurus || e instanceof Cryolophosaurus || e instanceof VelocityRaptor);
	}

	/**
	 * ModelNastysaurus's idle jaw (client side, called by its renderer each frame): each time the jaw cycle (age x 0.7 x the
	 * model's speed 0.65) wraps around, it picks 1 in 20 whether this cycle snaps.
	 */
	public boolean idleJawOpen(float ageInTicks) {
		float phase = Math.abs(ageInTicks * 0.7F * 0.65F % (float) (Math.PI * 2));
		if (phase < jawPhase) jawSnap = random.nextInt(20) == 1;
		jawPhase = phase;
		return jawSnap;
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

	/** Nastysaurus.getCanSpawnHere: see {@link HuntingDinosaur#checkBigSpawn}; no other Nastysaurus within 16 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return checkBigSpawn(type, level, pos, random, 0, 16.0, 8.0);
	}
}
