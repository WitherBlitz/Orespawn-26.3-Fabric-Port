package orespawn.client.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import orespawn.client.OreSpawnClient;
import orespawn.entity.robots.SpiderDriver;

/** OreSpawn's RenderSpiderDriver: the vanilla spider (with its glowing eyes) in OreSpawn's Spider Driver skin. */
@Environment(EnvType.CLIENT)
public class SpiderDriverRenderer extends SpiderRenderer<SpiderDriver> {
	private static final Identifier TEXTURE = OreSpawnClient.tex("spider_driver.png");

	public SpiderDriverRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public Identifier getTextureLocation(LivingEntityRenderState state) {
		return TEXTURE;
	}
}
