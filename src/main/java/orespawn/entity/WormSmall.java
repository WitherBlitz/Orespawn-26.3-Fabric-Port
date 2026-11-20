package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's small worm (10 health, 3 attack, no armor, from WormSmall_stats): a thin worm that stays buried until a
 * player comes within 8 blocks, then pops up for a while (25 to 75 ticks), turning to face them, and burrows again for
 * 100 to 250 ticks before the next look. While up it bites a non-creative player standing right over it (1.5 blocks
 * across, 4 up or down), 1 in 15 ticks, and 1 bite in 6 pulls their boots off, wears them down by a twentieth of
 * what's left and throws them up out of the ground nearby. No AI goals, no drops, no XP, no fall damage. Large worms
 * spawn 20 of them around themselves; they don't spawn naturally, and spawn only at night.
 * <p>
 * Not ported yet: the PlayNicely config (treated as off, OreSpawn's default).
 */
public class WormSmall extends Worm {
	/** Ticks left above ground, and ticks left below before looking again. */
	private int upcount = 50;
	private int downcount;

	public WormSmall(EntityType<? extends WormSmall> type, Level level) {
		super(type, level, 0.75);
		this.xpReward = 0;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.1)
			.add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.ARMOR, 0.0);
	}

	/** WormSmall.onLivingUpdate: burrowing and coming up. */
	@Override
	public void aiStep() {
		super.aiStep();
		if (level().isClientSide()) return;
		Player target = nearestPlayer(8.0, 8.0, 8.0);
		int y = Mth.floor(getY());
		if (target == null) {
			upcount = random.nextInt(50);
			downcount = 0;
			if (!dig(y + 2, 0.1, 0.05)) return;
		} else if (upcount > 0) {
			upcount--;
			if (upcount == 0) downcount = 100 + random.nextInt(150);
			pointAt(target);
			if (!dig(Mth.floor(getY() + 0.25), 0.15, 0.1)) return;
		} else {
			if (downcount > 0) {
				downcount--;
			} else {
				upcount = 25 + random.nextInt(50);
			}
			if (!dig(y + 2, 0.2, 0.05)) return;
		}
		holdStill();
	}

	/** WormSmall.updateAITasks: biting, and pulling boots off. */
	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		Player target = nearestVictim(1.5, 4.0, 1.5);
		if (target == null) return;
		pointAt(target);
		if (upcount > 0 && random.nextInt(15) == 1) {
			doHurtTarget(level, target);
			if (random.nextInt(6) == 1) strip(level, target, EquipmentSlot.FEET, 20);
		}
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
		return false;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("little_splat");
	}

	@Override
	protected @Nullable SoundEvent getDeathSound() {
		return null;
	}

	@Override
	public float getVoicePitch() {
		return 1.5F;
	}

	/** WormSmall.getCanSpawnHere: only at night. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return !level.getLevel().isBrightOutside();
	}
}
