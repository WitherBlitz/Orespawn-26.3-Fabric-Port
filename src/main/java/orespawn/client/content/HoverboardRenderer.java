package orespawn.client.content;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnClient;
import orespawn.client.model.HoverboardModel;
import orespawn.entity.tools.Elevator;

/**
 * OreSpawn's RenderElevator: the board turned to its facing, rocking after a hit as a boat does, drawn at its feet
 * (no living-model offset) in its colour's texture, with a 0.25 shadow.
 */
@Environment(EnvType.CLIENT)
public class HoverboardRenderer extends EntityRenderer<Elevator, HoverboardRenderState> {
	private static final Identifier[] TEXTURES = new Identifier[10];

	static {
		for (int i = 0; i < 10; i++) TEXTURES[i] = OreSpawnClient.tex("hoverboard" + (i + 1) + ".png");
	}

	private final HoverboardModel model;

	public HoverboardRenderer(EntityRendererProvider.Context context, HoverboardModel model) {
		super(context);
		this.model = model;
		this.shadowRadius = 0.25F;
	}

	@Override
	public void submit(HoverboardRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.rotateDegrees(Axis.YP, 180.0F - state.yRot);
		if (state.hurtTime > 0.0F) {
			poseStack.rotateDegrees(Axis.XP, Mth.sin(state.hurtTime) * state.hurtTime * state.damageTime / 10.0F * state.hurtDir);
		}
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		collector.submitModel(model, state, poseStack, state.texture, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();
		super.submit(state, poseStack, collector, camera);
	}

	@Override
	public HoverboardRenderState createRenderState() {
		return new HoverboardRenderState();
	}

	@Override
	public void extractRenderState(Elevator entity, HoverboardRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.yRot = entity.getYRot(partialTicks);
		state.hurtTime = entity.getHurtTime() - partialTicks;
		state.hurtDir = entity.getHurtDir();
		state.damageTime = Math.max(entity.getDamage() - partialTicks, 0.0F);
		state.texture = TEXTURES[Mth.clamp(entity.getColor(), 1, 10) - 1];
	}
}
