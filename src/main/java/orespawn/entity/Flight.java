package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

/**
 * OreSpawn's flying critters all steer the same way (as 1.7.10's bat did): keep a target block, nudge the velocity
 * towards it each tick by the sign of the gap, and turn to face the way they're going.
 */
public final class Flight {
	private Flight() {
	}

	/**
	 * A move control that does nothing, for the flyers' constructors. OreSpawn set moveForward after 1.7.10's move helper
	 * had run, so it stayed; 26.3's MoveControl runs after customServerAiStep and would zero the forward input that
	 * {@link #steer} sets. The flyers never use navigation.
	 */
	public static MoveControl keepOwnSteering(Mob mob) {
		return new MoveControl(mob) {
			@Override
			public void tick() {
			}
		};
	}

	/** Picks an air block near the mob: tries up to {@code tries} spots, dx/dz in ±spread, dy from -2 to +3. */
	public static BlockPos pickAir(Mob mob, int spread, int tries) {
		BlockPos target = mob.blockPosition();
		var r = mob.getRandom();
		for (int i = 0; i < tries; i++) {
			target = BlockPos.containing(mob.getX() + r.nextInt(spread) - r.nextInt(spread), mob.getY() + r.nextInt(6) - 2,
				mob.getZ() + r.nextInt(spread) - r.nextInt(spread));
			if (mob.level().isEmptyBlock(target)) break;
		}
		return target;
	}

	/**
	 * Steers towards {@code target}: speed is the horizontal top speed, accel how fast the velocity follows,
	 * offset the point aimed at inside the block, forward the walk input, turn how much of the turn is taken each tick.
	 */
	public static void steer(Mob mob, BlockPos target, double speed, double accel, double vAccel, double offset, float forward, float turn) {
		double dx = target.getX() + offset - mob.getX();
		double dy = target.getY() + 0.1 - mob.getY();
		double dz = target.getZ() + offset - mob.getZ();
		Vec3 m = mob.getDeltaMovement();
		Vec3 n = m.add((Math.signum(dx) * speed - m.x) * accel, (Math.signum(dy) * 0.7F - m.y) * vAccel, (Math.signum(dz) * speed - m.z) * accel);
		mob.setDeltaMovement(n);
		float yaw = (float) (Mth.atan2(n.z, n.x) * 180.0 / Math.PI) - 90.0F;
		mob.zza = forward;
		mob.setYRot(mob.getYRot() + Mth.wrapDegrees(yaw - mob.getYRot()) * turn);
	}

	/** 1.7.10's ChunkCoordinates.getDistanceSquared from the mob's block position. */
	public static double distSq(Mob mob, BlockPos target) {
		return mob.blockPosition().distSqr(target);
	}
}
