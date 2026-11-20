package orespawn.client.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import orespawn.client.OreSpawnClient;
import orespawn.client.OreSpawnMobRenderer;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.model.PurplePowerModel;
import orespawn.entity.bosses.PurplePower;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's RenderPurplePower (ModelPurplePower(1.0), shadow 0.3 x 2.75, scale 2.75; 0.55 for the coloured bolts),
 * with what the model's render() did in GL: blended at colour 0.75 and alpha 0.55, at full brightness. Textures by kind:
 * PurplePowertexture.png (0), 2 (fire), 3 (poison), 4 (weakness), 10 (The King's exploding bolt).
 */
@Environment(EnvType.CLIENT)
public class PurplePowerRenderer extends OreSpawnMobRenderer<PurplePower> {
	private static final float SCALE = 2.75F;
	private static final int TINT = ARGB.colorFromFloat(0.55F, 0.75F, 0.75F, 0.75F);
	private static final Identifier BOLT = OreSpawnClient.tex("purple_power.png");
	private static final Identifier FIRE = OreSpawnClient.tex("purple_power2.png");
	private static final Identifier POISON = OreSpawnClient.tex("purple_power3.png");
	private static final Identifier WEAKNESS = OreSpawnClient.tex("purple_power4.png");
	private static final Identifier EXPLODING = OreSpawnClient.tex("purple_power10.png");

	public PurplePowerRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer) {
		super(context, new PurplePowerModel(context.bakeLayer(layer), 1.0F), 0.3F * SCALE, SCALE, (e, s) -> {
			int type = e.getPurpleType();
			s.texture = switch (type) {
				case 1 -> FIRE;
				case 2 -> POISON;
				case 3 -> WEAKNESS;
				case 10 -> EXPLODING;
				default -> BOLT;
			};
			s.scale = type != 0 ? 0.55F / SCALE : 1.0F;
		}, e -> 15);
	}

	@Override
	protected int getSkyLightLevel(PurplePower entity, BlockPos pos) {
		return 15;
	}

	@Override
	protected @Nullable RenderType getRenderType(OreSpawnRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
		return isBodyVisible || forceTransparent ? RenderTypes.entityTranslucent(state.texture) : super.getRenderType(state, isBodyVisible,
			forceTransparent, appearGlowing);
	}

	@Override
	protected int getModelTint(OreSpawnRenderState state) {
		return TINT;
	}
}
