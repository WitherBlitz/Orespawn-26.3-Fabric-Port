package orespawn.entity.ants;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.level.Level;
import orespawn.entity.WanderGoal;
import orespawn.world.OreSpawnDimensions;

/**
 * OreSpawn's rainbow ant (EntityRainbowAnt): a black ant in rainbow colours. Right-clicking one with an empty hand
 * takes you to Village Mania, or back to the overworld from there. OreSpawn's constructor added its panic and wander
 * goals a second time on top of the black ant's, so this one wanders a little more often; that's kept.
 */
public class RainbowAnt extends Ant {
	public RainbowAnt(EntityType<? extends RainbowAnt> type, Level level) {
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
		return OreSpawnDimensions.VILLAGE_MANIA;
	}
}
