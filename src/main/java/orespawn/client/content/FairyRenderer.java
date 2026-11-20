package orespawn.client.content;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.LightCoordsUtil;
import orespawn.client.OreSpawnMobRenderer;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.model.FairyModel;
import orespawn.entity.spooky.Fairy;

import java.util.function.BiConsumer;

/**
 * The Fairy, as RenderFairy and ModelFairy drew it: the whole fairy at the world's light, then its body again at its own
 * lightmap, full sky light always and full block light while it blinks (state.flag), so the body glows and flashes and
 * the wings don't.
 */
@Environment(EnvType.CLIENT)
public class FairyRenderer extends OreSpawnMobRenderer<Fairy> {
	/** ModelFairy's speed argument, as ClientProxyOreSpawn passed it. */
	private static final float WING_SPEED = 1.5F;

	public FairyRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, float shadow, float scale,
			BiConsumer<Fairy, OreSpawnRenderState> extractor) {
		super(context, new FairyModel(context.bakeLayer(layer), WING_SPEED), shadow, scale, extractor, null);
		FairyModel body = new FairyModel(context.bakeLayer(layer), WING_SPEED, true);
		addLayer(new RenderLayer<>(this) {
			@Override
			public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, OreSpawnRenderState state, float yRot, float xRot) {
				if (state.isInvisible) return;
				int glow = LightCoordsUtil.pack(state.flag ? 15 : 0, 15);
				collector.order(1).submitModel(body, state, poseStack, body.renderType(state.texture), glow,
					LivingEntityRenderer.getOverlayCoords(state, 0.0F), state.outlineColor);
			}
		});
	}
}
