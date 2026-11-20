package orespawn.client.content;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import orespawn.client.OreSpawnClient;
import orespawn.client.OreSpawnMobRenderer;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.model.TheKingModel;
import orespawn.entity.bosses.TheKing;

/**
 * OreSpawn's RenderTheKing (model speed 0.65, shadow 1.9 x 2.1, scale 2.1, TheKingtexture.png), plus what its model's
 * render() did with GL: the ten wing membranes drawn last, blended at colour 0.75 and alpha 0.55. The main pass uses a
 * copy of the model without the membranes; a layer draws a second copy with only them, translucent.
 * {@code state.attacking} is TheKing.getAttacking() (faster wing beats, swinging heads, legs and tail).
 */
@Environment(EnvType.CLIENT)
public class TheKingRenderer extends OreSpawnMobRenderer<TheKing> {
	private static final int MEMBRANE_COLOR = ARGB.colorFromFloat(0.55F, 0.75F, 0.75F, 0.75F);

	public TheKingRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer) {
		super(context, new TheKingModel(context.bakeLayer(layer), 0.65F, false), 1.9F * 2.1F, 2.1F, (e, s) -> {
			s.texture = OreSpawnClient.tex("the_king.png");
			s.attacking = e.getAttacking() != 0;
		}, null);
		TheKingModel membranes = new TheKingModel(context.bakeLayer(layer), 0.65F, true);
		addLayer(new RenderLayer<>(this) {
			@Override
			public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, OreSpawnRenderState state, float yRot, float xRot) {
				collector.order(1).submitModel(membranes, state, poseStack, RenderTypes.entityTranslucent(state.texture), light,
					LivingEntityRenderer.getOverlayCoords(state, 0.0F), MEMBRANE_COLOR, null, state.outlineColor);
			}
		});
	}
}
