package orespawn.entity;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's MyEntityAIWander (a 1 in 90 chance a tick, 10 blocks) and MyEntityAIWanderALot (1 in 30, a given range):
 * a random stroll that sitting pets skip.
 */
public class WanderGoal extends RandomStrollGoal {
	private final int range;

	public WanderGoal(PathfinderMob mob, double speed, int interval, int range) {
		super(mob, speed, interval, false);
		this.range = range;
	}

	/** MyEntityAIWander. */
	public static WanderGoal wander(PathfinderMob mob, double speed) {
		return new WanderGoal(mob, speed, 90, 10);
	}

	/** MyEntityAIWanderALot. */
	public static WanderGoal wanderALot(PathfinderMob mob, int range, double speed) {
		return new WanderGoal(mob, speed, 30, range);
	}

	@Override
	public boolean canUse() {
		if (mob instanceof TamableAnimal t && t.isOrderedToSit()) return false;
		return super.canUse();
	}

	@Override
	protected @Nullable Vec3 getPosition() {
		return DefaultRandomPos.getPos(this.mob, range, 7);
	}
}
