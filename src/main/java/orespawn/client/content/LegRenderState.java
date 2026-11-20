package orespawn.client.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import orespawn.client.OreSpawnRenderState;

/**
 * The Robot Spider's and Robot Red Ant's render state: OreSpawn's render state plus each leg's angles
 * (RenderSpiderRobotInfo's display angles), copied from the robot's client-side legs each frame.
 */
@Environment(EnvType.CLIENT)
public class LegRenderState extends OreSpawnRenderState {
	public final float[] ydisplay = new float[8];
	public final float[] uddisplay = new float[8];
	public final double[] p1x = new double[8];
	public final double[] p2x = new double[8];
	public final double[] p3x = new double[8];
	/** Each leg's fixed layout (RobotLegs.Config): where it joins the body. */
	public float[] ymid, legoff, yoff;
	/** RenderSpiderRobotInfo.gpcounter: client ticks, for the jaws. */
	public int counter;
}
