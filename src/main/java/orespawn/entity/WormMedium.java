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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.registry.ModSounds;

/**
 * OreSpawn's medium worm (30 health, 10 attack, 8 armor, from WormMedium_stats): stays buried while any small worm is
 * within 8 blocks, so it comes out only once the small ones around it are dead. Then, with a player within 8 blocks,
 * it pops up for 25 to 100 ticks facing them and burrows again for 100 to 250. While up it bites a non-creative
 * player within 2.25 blocks across (8 up or down), 1 in 15 ticks, and 1 bite in 6 pulls their boots off (or, with no
 * boots, their leggings), wears them down by a fifteenth of what's left and throws them up out of the ground. No AI
 * goals, no XP, no fall damage. Drops 2 rotten flesh and 2 leather, thrown up around it. Large worms spawn 20 of them
 * around themselves; they don't spawn naturally, and spawn only at night.
 * <p>
 * Not ported yet: the PlayNicely config (treated as off, OreSpawn's default).
 */
public class WormMedium extends Worm {
	/** Ticks left above ground, and ticks left below before looking again. */
	private int upcount;
	private int downcount;

	public WormMedium(EntityType<? extends WormMedium> type, Level level) {
		super(type, level, 0.65);
		this.xpReward = 0;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.1)
			.add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, 8.0);
	}

	/** WormMedium.onLivingUpdate: burrowing and coming up. */
	@Override
	public void aiStep() {
		super.aiStep();
		if (level().isClientSide()) return;
		WormSmall small = nearest(WormSmall.class, getBoundingBox().inflate(8.0, 8.0, 8.0));
		Player target = small == null ? nearestPlayer(8.0, 8.0, 8.0) : null;
		int y = Mth.floor(getY());
		if (small != null || target == null) {
			upcount = random.nextInt(50);
			downcount = 0;
			if (!dig(y + 3, 0.1, 0.05)) return;
		} else if (upcount > 0) {
			upcount--;
			if (upcount == 0) downcount = 100 + random.nextInt(150);
			pointAt(target);
			if (!dig(Mth.floor(getY() + 0.25), 0.2, 0.1)) return;
		} else {
			if (downcount > 0) {
				downcount--;
			} else {
				upcount = 25 + random.nextInt(75);
			}
			if (!dig(y + 3, 0.1, 0.05)) return;
		}
		holdStill();
	}

	/** WormMedium.updateAITasks: biting, and pulling boots or leggings off. */
	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (nearest(WormSmall.class, getBoundingBox().inflate(8.0, 8.0, 8.0)) != null) return;
		Player target = nearestVictim(2.25, 8.0, 2.25);
		if (target == null) return;
		pointAt(target);
		if (upcount > 0 && random.nextInt(15) == 1) {
			doHurtTarget(level, target);
			if (random.nextInt(6) == 1 && !strip(level, target, EquipmentSlot.FEET, 15)) strip(level, target, EquipmentSlot.LEGS, 15);
		}
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		for (int i = 0; i < 2; i++) dropAround(level, new ItemStack(Items.ROTTEN_FLESH), 3);
		for (int i = 0; i < 2; i++) dropAround(level, new ItemStack(Items.LEATHER), 3);
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
	protected SoundEvent getDeathSound() {
		return ModSounds.get("big_splat");
	}

	@Override
	public float getVoicePitch() {
		return 1.5F;
	}

	/** WormMedium.getCanSpawnHere: only at night. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return !level.getLevel().isBrightOutside();
	}
}
