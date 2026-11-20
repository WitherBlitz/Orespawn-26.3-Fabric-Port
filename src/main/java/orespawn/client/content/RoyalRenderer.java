package orespawn.client.content;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Mob;
import orespawn.client.OreSpawnMobRenderer;
import orespawn.client.OreSpawnRenderState;

import java.util.function.BiConsumer;

/**
 * The royal children's renderer: OreSpawn's RenderThePrince and friends, plus what their models' render() did in GL:
 * the wings (and The Princess's power orbs) drawn last, blended at colour 0.75 and alpha 0.55, the orbs at full
 * brightness. The main pass is a copy of the model without them; layers draw a copy with only the blended parts and
 * one with only the bright ones (pass 0, 1 and 2 of the model's constructor).
 */
@Environment(EnvType.CLIENT)
public class RoyalRenderer<E extends Mob> extends OreSpawnMobRenderer<E> {
	private static final int BLEND = ARGB.colorFromFloat(0.55F, 0.75F, 0.75F, 0.75F);

	/** Makes a copy of the model for one pass (0 body, 1 blended, 2 blended and bright). */
	public interface Model {
		EntityModel<OreSpawnRenderState> make(ModelPart root, int pass);
	}

	public RoyalRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, Model model, float shadow, float scale,
			BiConsumer<E, OreSpawnRenderState> extractor, boolean brightPass) {
		super(context, model.make(context.bakeLayer(layer), 0), shadow, scale, extractor, null);
		EntityModel<OreSpawnRenderState> blended = model.make(context.bakeLayer(layer), 1);
		EntityModel<OreSpawnRenderState> bright = brightPass ? model.make(context.bakeLayer(layer), 2) : null;
		addLayer(new RenderLayer<>(this) {
			@Override
			public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, OreSpawnRenderState state, float yRot, float xRot) {
				collector.order(1).submitModel(blended, state, poseStack, RenderTypes.entityTranslucent(state.texture), light,
					LivingEntityRenderer.getOverlayCoords(state, 0.0F), BLEND, null, state.outlineColor);
				if (bright != null) {
					collector.order(1).submitModel(bright, state, poseStack, RenderTypes.entityTranslucent(state.texture), LightCoordsUtil.FULL_BRIGHT,
						OverlayTexture.NO_OVERLAY, BLEND, null, state.outlineColor);
				}
			}
		});
	}
}
