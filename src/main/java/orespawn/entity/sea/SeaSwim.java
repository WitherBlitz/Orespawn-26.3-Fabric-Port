package orespawn.entity.sea;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * How OreSpawn's water creatures swim, the same way as the Flounder, Whale and Skate ports ({@code Flounder.swimInput} /
 * {@code seekWater}, which aren't visible from this package): they're land mobs that float at the surface (FloatGoal,
 * OreSpawn's EntityAISwimming) on ordinary paths that may cross water, and when out of water now and then they head
 * for the nearest water.
 */
final class SeaSwim {
	private SeaSwim() {
	}

	/**
	 * 1.7.10 pushed an AI-driven mob through water at 0.04 a tick per unit of speed, 26.3 at 0.02: doubling the input
	 * keeps OreSpawn's swimming pace (26.3 still caps the input at 1, as 1.7.10 did).
	 */
	static Vec3 swimInput(Vec3 input) {
		return input.scale(2.0);
	}

	/**
	 * OreSpawn's out-of-water routine: find the nearest water and walk to just under it; with none in reach, lose 1 health
	 * 1 time in {@code lossChance} and vanish (no death, no drops) at 0. Returns false if the mob vanished.
	 */
	static boolean seekWater(PathfinderMob mob, ServerLevel level, int shells, int maxDy, double speed, int lossChance) {
		BlockPos water = nearestWater(mob, level, shells, maxDy);
		if (water != null) {
			mob.getNavigation().moveTo(water.getX(), water.getY() - 1, water.getZ(), speed);
			return true;
		}
		if (mob.getRandom().nextInt(lossChance) == 1) mob.heal(-1.0F);
		if (mob.getHealth() <= 0.0F) {
			mob.discard();
			return false;
		}
		return true;
	}

	/**
	 * OreSpawn's scan_it loop: water (still or flowing) in growing box shells around the block under the mob, i blocks
	 * across and min(i, maxDy) up and down, for i = 1-5 and then every other i below {@code shells}; the nearest in the
	 * first shell that has any.
	 */
	static @Nullable BlockPos nearestWater(Mob mob, ServerLevel level, int shells, int maxDy) {
		BlockPos c = mob.blockPosition().below();
		for (int i = 1; i < shells; i++) {
			int dy = Math.min(i, maxDy);
			BlockPos best = null;
			int bestD = Integer.MAX_VALUE;
			for (BlockPos p : BlockPos.betweenClosed(c.offset(-i, -dy, -i), c.offset(i, dy, i))) {
				int ax = Math.abs(p.getX() - c.getX()), ay = Math.abs(p.getY() - c.getY()), az = Math.abs(p.getZ() - c.getZ());
				if (ax != i && az != i && ay != dy) continue;
				if (!level.getBlockState(p).is(Blocks.WATER)) continue;
				int d = ax * ax + ay * ay + az * az;
				if (d < bestD) {
					bestD = d;
					best = p.immutable();
				}
			}
			if (best != null) return best;
			if (i >= 5) i++;
		}
		return null;
	}
}
