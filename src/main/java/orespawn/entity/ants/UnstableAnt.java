package orespawn.entity.ants;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.level.Level;
import orespawn.entity.WanderGoal;
import orespawn.world.OreSpawnDimensions;

/**
 * OreSpawn's unstable ant (EntityUnstableAnt): a black ant with its own colours. Right-clicking one with an empty hand
 * takes you to the Islands, or back to the overworld from there. Like the rainbow ant, OreSpawn added its panic and
 * wander goals a second time; that's kept.
 */
public class UnstableAnt extends Ant {
	public UnstableAnt(EntityType<? extends UnstableAnt> type, Level level) {
		super(type, level);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(0, new PanicGoal(this, 1.4F));
		goalSelector.addGoal(1, WanderGoal.wanderALot(this, 9, 1.0));
	}

	@Override
	protected ResourceKey<Level> destination() {
		return OreSpawnDimensions.ISLANDS;
	}
}
