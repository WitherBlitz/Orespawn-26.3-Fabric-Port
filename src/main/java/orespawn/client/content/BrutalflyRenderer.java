package orespawn.client.content;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import orespawn.client.OreSpawnClient;
import orespawn.client.OreSpawnMobRenderer;
import orespawn.client.OreSpawnRenderState;
import orespawn.entity.bugs.Brutalfly;

/**
 * RenderBrutalfly: the shared OreSpawn renderer plus its second pass, the Brutalfly overlay texture drawn again over
 * the model, sliding slowly across it, additive and at half brightness (as the charged-creeper swirl is drawn).
 */
@Environment(EnvType.CLIENT)
public class BrutalflyRenderer extends OreSpawnMobRenderer<Brutalfly> {
	private static final Identifier OVERLAY = OreSpawnClient.tex("brutalfly_overlay.png");

	public BrutalflyRenderer(EntityRendererProvider.Context context, EntityModel<OreSpawnRenderState> model, float shadow, float scale) {
		super(context, model, shadow, scale, (e, s) -> s.texture = OreSpawnClient.tex("brutalfly.png"), null);
		addLayer(new RenderLayer<>(this) {
			@Override
			public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, OreSpawnRenderState state, float yRot, float xRot) {
				float t = state.ageInTicks * 0.01F;
				collector.order(1).submitModel(getParentModel(), state, poseStack, RenderTypes.energySwirl(OVERLAY, t % 1.0F, t % 1.0F), light,
					OverlayTexture.NO_OVERLAY, -8355712, null, state.outlineColor);
			}
		});
	}
}
