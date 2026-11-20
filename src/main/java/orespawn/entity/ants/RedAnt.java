package orespawn.entity.ants;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import orespawn.entity.WanderGoal;
import orespawn.world.OreSpawnDimensions;

/**
 * OreSpawn's red ant (EntityRedAnt): a bit bigger and quicker than the black ant, 2 health. It goes for players and
 * bites, but a bite lands only one time in 15 (1 damage, never on Peaceful); once a second it also tries to bite the
 * nearest player within 1.5 blocks. Right-clicking one with an empty hand takes you to OreSpawn's Mining dimension, or
 * back to the overworld from there. Its goals are added on top of the black ant's, as OreSpawn's constructor did.
 */
public class RedAnt extends Ant {
	private int attackDelay = 20;

	public RedAnt(EntityType<? extends RedAnt> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return attributes(2.0, 0.2, 1.0);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(0, new PanicGoal(this, 1.4F));
		goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
		goalSelector.addGoal(2, WanderGoal.wanderALot(this, 10, 1.0));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 4, true, false, null));
	}

	@Override
	protected ResourceKey<Level> destination() {
		return OreSpawnDimensions.MINING;
	}

	/** One bite in 15 lands, for 1 damage. */
	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (random.nextInt(15) != 0) return false;
		return level.getDifficulty() != Difficulty.PEACEFUL && target.hurtServer(level, damageSources().mobAttack(this), 1.0F);
	}

	@Override
	public void tick() {
		super.tick();
		if (level() instanceof ServerLevel level && isAlive() && --attackDelay <= 0) {
			attackDelay = 20;
			if (level.getDifficulty() != Difficulty.PEACEFUL) {
				Player player = level.getNearestPlayer(getX(), getY(), getZ(), 1.5, true);
				if (player != null) doHurtTarget(level, player);
			}
		}
	}
}
