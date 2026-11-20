package orespawn.client.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnClient;
import orespawn.client.OreSpawnMobRenderer;
import orespawn.client.OreSpawnRenderState;
import orespawn.entity.tools.RockBase;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's RenderRockBase: no shadow, scale 1, each rock type's texture ({@code state.variant} is the type). The
 * crystals (types 9-12, {@code state.flag}) are drawn as ModelRockBase drew them: blended, tinted 0.75 grey at 55%
 * opacity, and full bright.
 */
@Environment(EnvType.CLIENT)
public class RockRenderer extends OreSpawnMobRenderer<RockBase> {
	/** RenderRockBase's textures for types 1-12. */
	private static final Identifier[] TEXTURES = {OreSpawnClient.tex("rock.png"), OreSpawnClient.tex("rock.png"), OreSpawnClient.tex("rock_red.png"),
		OreSpawnClient.tex("rock_green.png"), OreSpawnClient.tex("rock_blue.png"), OreSpawnClient.tex("rock_purple.png"), OreSpawnClient.tex("rock.png"),
		OreSpawnClient.tex("rock_tnt.png"), OreSpawnClient.tex("rock_crystal.png"), OreSpawnClient.tex("rock_crystal_green.png"),
		OreSpawnClient.tex("rock_crystal_blue.png"), OreSpawnClient.tex("rock_crystal_tnt.png")};
	private static final int CRYSTAL_TINT = ARGB.colorFromFloat(0.55F, 0.75F, 0.75F, 0.75F);

	public RockRenderer(EntityRendererProvider.Context context, EntityModel<OreSpawnRenderState> model) {
		super(context, model, 0.0F, 1.0F, (e, s) -> {
			int type = e.rockType();
			s.variant = type;
			s.flag = type >= 9 && type <= 12;
			s.texture = TEXTURES[Mth.clamp(type, 1, 12) - 1];
		}, e -> e.rockType() >= 9 ? 15 : 0);
	}

	@Override
	protected int getModelTint(OreSpawnRenderState state) {
		return state.flag ? CRYSTAL_TINT : -1;
	}

	@Override
	protected @Nullable RenderType getRenderType(OreSpawnRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
		if (state.flag && isBodyVisible && !forceTransparent) return RenderTypes.entityTranslucent(state.texture);
		return super.getRenderType(state, isBodyVisible, forceTransparent, appearGlowing);
	}
}
