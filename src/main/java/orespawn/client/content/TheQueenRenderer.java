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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import orespawn.client.OreSpawnClient;
import orespawn.client.OreSpawnMobRenderer;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.model.TheQueenModel;
import orespawn.entity.bosses.TheQueen;

/**
 * OreSpawn's RenderTheQueen (model speed 0.65, shadow 1.9 x 2.0, scale 2.0; TheQueentexture2.png while she's happy,
 * TheQueentexture.png when angry), plus what its model's render() did with GL: the ten wing membranes blended at
 * colour 0.75 and alpha 0.55, then the power cubes the same way at full brightness, then the six eyes at full
 * brightness. The main pass is a copy of the model without those; each layer draws a copy with only its own parts.
 * {@code state.attacking}: TheQueen.getAttacking(); {@code state.value}: her power (the cubes she shows).
 */
@Environment(EnvType.CLIENT)
public class TheQueenRenderer extends OreSpawnMobRenderer<TheQueen> {
	private static final int BLEND = ARGB.colorFromFloat(0.55F, 0.75F, 0.75F, 0.75F);
	private static final Identifier ANGRY = OreSpawnClient.tex("the_queen.png");
	private static final Identifier HAPPY = OreSpawnClient.tex("the_queen_happy.png");

	public TheQueenRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer) {
		super(context, new TheQueenModel(context.bakeLayer(layer), 0.65F, TheQueenModel.BODY), 1.9F * 2.0F, 2.0F, (e, s) -> {
			s.texture = e.isHappy() ? HAPPY : ANGRY;
			s.attacking = e.getAttacking() != 0;
			s.value = e.power();
		}, null);
		TheQueenModel membranes = new TheQueenModel(context.bakeLayer(layer), 0.65F, TheQueenModel.MEMBRANES);
		TheQueenModel cubes = new TheQueenModel(context.bakeLayer(layer), 0.65F, TheQueenModel.CUBES);
		TheQueenModel eyes = new TheQueenModel(context.bakeLayer(layer), 0.65F, TheQueenModel.EYES);
		addLayer(new RenderLayer<>(this) {
			@Override
			public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, OreSpawnRenderState state, float yRot, float xRot) {
				int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
				collector.order(1).submitModel(membranes, state, poseStack, RenderTypes.entityTranslucent(state.texture), light, overlay, BLEND, null,
					state.outlineColor);
				collector.order(1).submitModel(cubes, state, poseStack, RenderTypes.entityTranslucent(state.texture), LightCoordsUtil.FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY, BLEND, null, state.outlineColor);
				collector.order(1).submitModel(eyes, state, poseStack, RenderTypes.entityCutout(state.texture), LightCoordsUtil.FULL_BRIGHT,
					overlay, -1, null, state.outlineColor);
			}
		});
	}
}
