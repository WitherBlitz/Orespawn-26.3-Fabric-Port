package orespawn.client.content;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import orespawn.client.OreSpawnMobRenderer;
import orespawn.client.OreSpawnRenderState;
import orespawn.entity.robots.LeggedRobot;
import orespawn.entity.robots.RobotLegs;

/**
 * OreSpawn's RenderSpiderRobot / RenderAntRobot: they drew the model straight at the robot's feet, turned to its
 * facing (not its body or head), with no 1.5-block drop and no tip-over on death. Legs reach well past the hitbox, so
 * the culling box is widened by {@code reach} blocks.
 */
@Environment(EnvType.CLIENT)
public class LeggedRobotRenderer<E extends LeggedRobot> extends OreSpawnMobRenderer<E> {
	private final double reach;

	public LeggedRobotRenderer(EntityRendererProvider.Context context, EntityModel<OreSpawnRenderState> model, float shadow, Identifier texture,
			double reach) {
		super(context, model, shadow, 1.0F, (e, s) -> {
			s.texture = texture;
			s.attacking = e.attacking();
		}, null);
		this.reach = reach;
	}

	@Override
	public OreSpawnRenderState createRenderState() {
		return new LegRenderState();
	}

	@Override
	public void extractRenderState(E entity, OreSpawnRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.bodyRot = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
		if (state instanceof LegRenderState legs) {
			RobotLegs l = entity.legs;
			int n = l.c.legs();
			System.arraycopy(l.ydisplay, 0, legs.ydisplay, 0, n);
			System.arraycopy(l.uddisplay, 0, legs.uddisplay, 0, n);
			System.arraycopy(l.p1x, 0, legs.p1x, 0, n);
			System.arraycopy(l.p2x, 0, legs.p2x, 0, n);
			System.arraycopy(l.p3x, 0, legs.p3x, 0, n);
			legs.ymid = l.c.ymid();
			legs.legoff = l.c.legoff();
			legs.yoff = l.c.yoff();
			legs.counter = l.counter;
		}
	}

	@Override
	protected void setupRotations(OreSpawnRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
		poseStack.rotateDegrees(Axis.YP, 180.0F - bodyRot);
	}

	@Override
	protected void scale(OreSpawnRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.translate(0.0F, 1.501F, 0.0F);
	}

	@Override
	protected AABB getBoundingBoxForCulling(E entity, float partialTicks) {
		return super.getBoundingBoxForCulling(entity, partialTicks).inflate(reach);
	}
}
