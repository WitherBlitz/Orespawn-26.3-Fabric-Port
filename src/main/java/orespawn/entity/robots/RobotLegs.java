package orespawn.entity.robots;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The walking legs of OreSpawn's Robot Spider and Robot Red Ant (RenderSpiderRobotInfo plus their updateLegs and
 * findNewFooting), run on the client each tick: every leg plants its foot on a solid block, keeps it there while the
 * body moves, and steps to new footing (lifting the foot over the way) when it's stretched too far, bunched too close,
 * turned too far, or its turn comes round. The model reads the joint angles.
 * <p>
 * Two OreSpawn quirks aren't copied: it trampled tall grass and turned grass to dirt under the feet on the client only
 * (so the world never really changed), and the ant's foot search, as decompiled, only tried one column of blocks.
 */
public final class RobotLegs {
	/** One robot's leg layout and step tuning (OreSpawn's numbers for each). */
	public record Config(int legs, float[] legoff, float[] ymid, float[] yrange, float[] yoff, int[] paired, float velMul, float velMin,
			float velMax, float segment, float reachMax, float reachMin, float turnDiv, float stepFront, float stepRear, float stepPair01,
			float rangeMul, float minStep, int searchTop, int searchBottom, boolean skipFar, float lift1, float lift2, float lift3, float lift2At,
			float lift3At) {
	}

	private static final float P = (float) Math.PI;

	/** SpiderRobot: eight legs, 99-pixel segments. */
	public static final Config SPIDER = new Config(8,
		new float[] {1.25F, 1.25F, 2.0F, 2.0F, 1.75F, 1.75F, 3.4F, 3.4F},
		new float[] {-0.32F, 3.4615927F, -1.0F, 4.1415925F, P / 5, P * 4 / 5, 1.05F, 2.0915928F},
		new float[] {P / 12, -P / 12, P / 12, -P / 12, P / 12, -P / 12, P / 12, -P / 12},
		new float[] {-0.3F, -0.3F, -0.1F, -0.1F, -0.3F, -0.3F, -0.1F, -0.1F},
		new int[] {1, 0, 3, 2, 5, 4, 7, 6},
		8.0F, 1.0F, 4.0F, 99.0F, 294.0F, 32.0F, 7.0F, 16.0F, 10.0F, Float.NaN, 0.875F, 3.5F, 11, -14, false, 1.0F, 1.5F, 1.5F, 48.0F, 100.0F);

	/** AntRobot: six legs, 49-pixel segments. */
	public static final Config ANT = new Config(6,
		new float[] {0.75F, 0.75F, 1.0F, 1.0F, 1.15F, 1.15F},
		new float[] {0.0F, P, -P / 4, P * 5 / 4, P / 4, P * 3 / 4},
		new float[] {P / 12, -P / 12, P / 12, -P / 12, P / 12, -P / 12},
		new float[] {-0.75F, -0.75F, -0.75F, -0.75F, -0.75F, -0.75F},
		new int[] {1, 0, 3, 2, 5, 4},
		18.0F, 2.0F, 8.0F, 49.0F, 144.0F, 22.0F, 6.0F, 9.0F, 4.0F, 6.0F, 0.8F, 2.5F, 8, -9, true, 0.3F, 0.6F, 0.6F, 24.0F, 50.0F);

	public final Config c;
	public final float[] ydisplay, uddisplay;
	public final double[] p1x, p2x, p3x;
	public int counter;
	private final float[] ywanted, ycurrent, yvel, udwanted, udcurrent, udvel, pxvel, footX, footY, footZ, realX, realY, realZ, uppoint;
	private final int[] footup, footing;
	private boolean started;

	public RobotLegs(Config c) {
		this.c = c;
		int n = c.legs();
		ydisplay = new float[n];
		uddisplay = new float[n];
		p1x = new double[n];
		p2x = new double[n];
		p3x = new double[n];
		ywanted = new float[n];
		ycurrent = new float[n];
		yvel = new float[n];
		udwanted = new float[n];
		udcurrent = new float[n];
		udvel = new float[n];
		pxvel = new float[n];
		footX = new float[n];
		footY = new float[n];
		footZ = new float[n];
		realX = new float[n];
		realY = new float[n];
		realZ = new float[n];
		uppoint = new float[n];
		footup = new int[n];
		footing = new int[n];
	}

	private void start(Entity e) {
		for (int i = 0; i < c.legs(); i++) {
			p1x[i] = Math.PI / 4;
			p2x[i] = 0.0;
			p3x[i] = -Math.PI / 4;
			footX[i] = (float) e.getX();
			footY[i] = (float) e.getY();
			footZ[i] = (float) e.getZ();
			footup[i] = 1;
		}
	}

	/** getNewVelocity: a joint's turning speed toward its goal, faster as the robot moves faster. */
	private float velocity(float v, float diff, float cur) {
		float tv = Mth.clamp(v * c.velMul(), c.velMin(), c.velMax());
		if (diff > 0.0F) {
			if (diff < 0.008726646259971648 * tv) return 0.0F;
			cur = (float) (cur + 0.004363323129985824 * tv);
			if (diff < 0.06981317007977318 * tv) cur = (float) (Math.PI / 180.0 * tv);
			if (diff < 0.03490658503988659 * tv) cur = (float) (0.008726646259971648 * tv);
			if (cur > 0.06981317007977318 * tv) cur = (float) (0.06981317007977318 * tv);
			return cur;
		}
		if (diff > -0.008726646259971648 * tv) return 0.0F;
		cur = (float) (cur - 0.004363323129985824 * tv);
		if (diff > -0.06981317007977318 * tv) cur = -(float) (Math.PI / 180.0 * tv);
		if (diff > -0.03490658503988659 * tv) cur = -(float) (0.008726646259971648 * tv);
		if (cur < -0.06981317007977318 * tv) cur = -(float) (0.06981317007977318 * tv);
		return cur;
	}

	private static double wrapPi(double a) {
		while (a > Math.PI) a -= Math.PI * 2;
		while (a < -Math.PI) a += Math.PI * 2;
		return a;
	}

	/** updateLegs, once a client tick after the robot has moved. */
	public void update(Entity e) {
		float yaw = e.getYRot() % 360.0F;
		while (yaw < 0.0F) yaw += 360.0F;
		counter++;
		if (!started) {
			started = true;
			start(e);
		}
		float d1 = (float) (e.xo - e.getX());
		float d2 = (float) (e.yo - e.getY());
		float d3 = (float) (e.zo - e.getZ());
		float realv = (float) Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
		for (int i = 0; i < c.legs(); i++) {
			int fcount = 0;
			footing[i]++;
			double base = Math.toRadians(Mth.wrapDegrees(yaw + 90.0F)) + c.ymid()[i];
			realX[i] = (float) (e.getX() - c.legoff()[i] * Math.sin(base));
			realZ[i] = (float) (e.getZ() + c.legoff()[i] * Math.cos(base));
			realY[i] = (float) e.getY() + c.yoff()[i];
			int pair = c.paired()[i];
			if (footing[i] + footing[pair] > 50 && footing[i] > footing[pair]) footing[i] = 0;
			float dd = footDistance(i);
			float da = (float) (Math.abs(ycurrent[i] - (Math.toRadians(Mth.wrapDegrees(yaw)) + c.ymid()[i])) % (Math.PI * 2));
			if (da > Math.PI) da = (float) (da - Math.PI * 2);
			if (da < -Math.PI) da = (float) (da + Math.PI * 2);
			da = Math.abs(da);
			if (dd > c.reachMax() || dd < c.reachMin() || da > Math.abs(c.yrange()[i]) * 8.0F / c.turnDiv() || Math.abs(udcurrent[i]) > 1.25
				|| footing[i] == 0) {
				findNewFooting(e, yaw, i);
				dd = footDistance(i);
			}
			float seg = c.segment();
			float cc = (float) (seg * Math.cos(p2x[i] - p1x[i])) + seg + (float) (seg * Math.cos(p2x[i] - p3x[i]));
			float diff = cc - dd;
			pxvel[i] = velocity(realv, (float) (diff * Math.PI / 360.0), pxvel[i]);
			if (pxvel[i] == 0.0F || Math.abs(diff) < 8.0F) fcount++;
			p1x[i] += pxvel[i];
			p2x[i] = 0.0;
			p3x[i] = -p1x[i];
			float below = uppoint[i] != 0.0F ? realY[i] - uppoint[i] : realY[i] - footY[i];
			udwanted[i] = (float) wrapPi(Math.atan2(dd, below * 16.0) - Math.PI / 2);
			double rdv = wrapPi((udwanted[i] - udcurrent[i]) % (Math.PI * 2));
			udvel[i] = velocity(realv * 2.0F, (float) rdv, udvel[i]);
			if (udvel[i] == 0.0F || Math.abs(rdv) < 0.03490658503988659) {
				uppoint[i] = 0.0F;
				fcount++;
			}
			udcurrent[i] = (float) wrapPi(udcurrent[i] + udvel[i]);
			uddisplay[i] = udcurrent[i];
			ywanted[i] = (float) Math.atan2(realZ[i] - footZ[i], realX[i] - footX[i]);
			rdv = (ywanted[i] - ycurrent[i]) % (Math.PI * 2);
			if (rdv > Math.PI) rdv -= Math.PI * 2;
			if (rdv < -Math.PI) rdv += Math.PI * 2;
			yvel[i] = velocity(realv, (float) rdv, yvel[i]);
			if (yvel[i] == 0.0F || Math.abs(rdv) < 0.03490658503988659) fcount++;
			ycurrent[i] = (float) wrapPi(ycurrent[i] + yvel[i]);
			ydisplay[i] = (float) wrapPi(ycurrent[i] - Math.toRadians(Mth.wrapDegrees(yaw)) - Math.PI / 2);
			if (fcount == 3) footup[i] = 0;
		}
	}

	private float footDistance(int i) {
		float d1 = realX[i] - footX[i];
		float d2 = realY[i] - footY[i];
		float d3 = realZ[i] - footZ[i];
		return (float) Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3) * 16.0F;
	}

	private static boolean solid(Level level, BlockPos pos) {
		BlockState s = level.getBlockState(pos);
		return !s.isAir() && s.isSolid();
	}

	/** findNewFooting: the nearest solid block ahead of where the leg wants to step (backwards when reversing). */
	private void findNewFooting(Entity e, float yaw, int i) {
		Level level = e.level();
		double rhdir = Math.toRadians((yaw + 90.0F) % 360.0F);
		footing[i] = 0;
		float d1 = (float) (e.getX() - e.xo);
		float d3 = (float) (e.getZ() - e.zo);
		double rdv = Math.abs(Math.atan2(d3, d1) - rhdir) % (Math.PI * 2.0);
		if (rdv > Math.PI) rdv -= Math.PI * 2.0;
		rdv = Math.abs(rdv);
		if (Math.sqrt(d1 * d1 + d3 * d3) < 0.01) rdv = 0.0;
		float range = c.yrange()[i] * c.rangeMul();
		if (Math.abs((e.yRotO - e.getYRot()) % 360.0F) > 0.75F) range = 0.0F;
		float f = i >= 4 ? c.stepRear() : c.stepFront();
		if (rdv > 1.5) {
			range = -range;
			f = i >= 4 ? c.stepFront() : c.stepRear();
		}
		if (!Float.isNaN(c.stepPair01()) && (i == 0 || i == 1)) f = c.stepPair01();
		double base = Math.toRadians(Mth.wrapDegrees(yaw + 90.0F)) + c.ymid()[i];
		float fx = (float) (realX[i] - f / 2.0F * Math.sin(base));
		float fz = (float) (realZ[i] + f / 2.0F * Math.cos(base));
		float fy = realY[i] - 1.0F;
		float deffx = fx, deffy = fy, deffz = fz;
		float oldf = f;
		int span = 1;
		boolean found = false;
		while (!found && f > c.minStep()) {
			fx = (float) (realX[i] - f * Math.sin(base - range));
			fz = (float) (realZ[i] + f * Math.cos(base - range));
			fy = realY[i];
			search:
			for (int j = c.searchTop(); j > c.searchBottom(); j--) {
				for (int m = -span; m <= span; m++) {
					for (int n = -span; n <= span; n++) {
						if (!solid(level, new BlockPos(Mth.floor(fx) + m, Mth.floor(fy) + j, Mth.floor(fz) + n))) continue;
						if (c.skipFar()) {
							float a = realX[i] - (fx + m), b = realY[i] - (fy + j + 1.0F), d = realZ[i] - (fz + n);
							if ((float) Math.sqrt(a * a + b * b + d * d) * 16.0F > c.reachMax()) continue;
						}
						fy += j + 1;
						fx += m;
						fz += n;
						found = true;
						break search;
					}
				}
			}
			if (found && !c.skipFar()) {
				float a = realX[i] - fx, b = realY[i] - fy, d = realZ[i] - fz;
				if ((float) Math.sqrt(a * a + b * b + d * d) * 16.0F > c.reachMax()) found = false;
			}
			if (--f < c.minStep() && range != 0.0F) {
				range = 0.0F;
				span = 3;
				f = oldf;
			}
		}
		if (!found) {
			fx = deffx;
			fy = deffy;
			fz = deffz;
		}
		float sfx = footX[i], sfy = footY[i], sfz = footZ[i];
		footX[i] = fx;
		footY[i] = fy;
		footZ[i] = fz;
		if (footup[i] == 0) {
			footup[i] = 1;
			float a = sfx - fx, b = sfy - fy, d = sfz - fz;
			float dd = (float) Math.sqrt(a * a + b * b + d * d) * 16.0F;
			float lift = (sfy + fy) / 2.0F;
			if (dd > 3.0F) lift += c.lift1();
			if (dd > c.lift2At()) lift += c.lift2();
			if (dd > c.lift3At()) lift += c.lift3();
			uppoint[i] = lift;
		}
	}
}
