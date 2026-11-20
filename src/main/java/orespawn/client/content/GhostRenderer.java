package orespawn.client.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Mob;
import orespawn.client.OreSpawnMobRenderer;
import orespawn.client.OreSpawnRenderState;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * OreSpawn's ghosts (ModelGhost, ModelGhostSkelly) drew themselves with blending on (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
 * and the colour (0.75, 0.75, 0.75, 0.25): a dim, three-quarters see-through grey. This draws them translucent with
 * that tint, lit as usual.
 */
@Environment(EnvType.CLIENT)
public class GhostRenderer<E extends Mob> extends OreSpawnMobRenderer<E> {
	private static final int TINT = ARGB.colorFromFloat(0.25F, 0.75F, 0.75F, 0.75F);

	public GhostRenderer(EntityRendererProvider.Context context, EntityModel<OreSpawnRenderState> model, float shadow, float scale,
			BiConsumer<E, OreSpawnRenderState> extractor) {
		super(context, model, shadow, scale, extractor, null);
	}

	@Override
	protected @Nullable RenderType getRenderType(OreSpawnRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
		if (!isBodyVisible && !forceTransparent) return super.getRenderType(state, isBodyVisible, forceTransparent, appearGlowing);
		return RenderTypes.entityTranslucent(getTextureLocation(state));
	}

	@Override
	protected int getModelTint(OreSpawnRenderState state) {
		return TINT;
	}
}
