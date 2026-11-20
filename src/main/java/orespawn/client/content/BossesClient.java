package orespawn.client.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.Identifier;
import orespawn.client.OreSpawnClient;
import orespawn.client.OreSpawnRenderState;
import orespawn.client.model.GodzillaModel;
import orespawn.client.model.KrakenModel;
import orespawn.client.model.PurplePowerModel;
import orespawn.client.model.TheKingModel;
import orespawn.client.model.ThePrinceAdultModel;
import orespawn.client.model.ThePrinceModel;
import orespawn.client.model.ThePrinceTeenModel;
import orespawn.client.model.ThePrincessModel;
import orespawn.client.model.TheQueenModel;
import orespawn.content.Bosses;
import orespawn.entity.bosses.Godzilla;
import orespawn.entity.bosses.Kraken;
import orespawn.entity.bosses.RoyalBaby;
import orespawn.entity.bosses.RoyalFlyer;
import orespawn.entity.bosses.ThePrincess;

/**
 * The Bosses group's renderers, as OreSpawn's ClientProxyOreSpawn set them up: each model with its speed argument, the
 * shadow (OreSpawn's size x scale) and scale, and the textures OreSpawn picked.
 */
@Environment(EnvType.CLIENT)
public final class BossesClient {
	private static final Identifier PRINCESS_ATTACKING = OreSpawnClient.tex("the_princess_attacking.png");

	private BossesClient() {
	}

	/** The three neck stretches (0-60), packed 8 bits each into the render state's variant for the royal models. */
	private static int necks(int a, int b, int c) {
		return a | b << 8 | c << 16;
	}

	private static void baby(RoyalBaby e, OreSpawnRenderState s) {
		s.flag = e.getActivity() == 2;
		s.sitting = e.isOrderedToSit();
		s.attacking = e.getAttacking() != 0;
		s.variant = necks(e.headExt(0), e.headExt(1), e.headExt(2));
	}

	private static void flyer(RoyalFlyer e, OreSpawnRenderState s) {
		s.flag = e.getActivity() != 0;
		s.sitting = e.isOrderedToSit();
		s.attacking = e.getAttacking() != 0;
		s.variant = necks(e.headExt(0), e.headExt(1), e.headExt(2));
		s.value = e.turn;
	}

	public static void init() {
		// RenderGodzilla(ModelGodzilla(0.2), 1.0, 2.0): state.attacking, state.variant = the entity id (its swings).
		OreSpawnClient.<Godzilla>mob(Bosses.MOBZILLA, OreSpawnClient.layer("mobzilla", GodzillaModel::createBodyLayer),
			root -> new GodzillaModel(root, 0.2F), 2.0F, 2.0F, (e, s) -> {
				s.texture = OreSpawnClient.tex("mobzilla.png");
				s.attacking = e.getAttacking() != 0;
				s.variant = e.getId();
			}, null);
		// RenderTheKing / RenderTheQueen: their own renderers (see-through wing membranes, the Queen's power cubes and eyes).
		ModelLayerLocation king = OreSpawnClient.layer("the_king", TheKingModel::createBodyLayer);
		EntityRendererRegistry.register(Bosses.THE_KING, c -> new TheKingRenderer(c, king));
		ModelLayerLocation queen = OreSpawnClient.layer("the_queen", TheQueenModel::createBodyLayer);
		EntityRendererRegistry.register(Bosses.THE_QUEEN, c -> new TheQueenRenderer(c, queen));
		// RenderThePrince (0.75, 0.75), RenderThePrincess (0.7, 0.7; ThePrincesstexture2 while attacking), RenderThePrinceTeen
		// (1.0, 1.25), RenderThePrinceAdult (1.2, 1.0, The King's texture); all with model speed 0.65.
		ModelLayerLocation prince = OreSpawnClient.layer("the_prince", ThePrinceModel::createBodyLayer);
		EntityRendererRegistry.register(Bosses.THE_PRINCE, c -> new RoyalRenderer<RoyalBaby>(c, prince,
			(root, pass) -> new ThePrinceModel(root, 0.65F, pass), 0.75F * 0.75F, 0.75F, (e, s) -> {
				s.texture = OreSpawnClient.tex("the_prince.png");
				baby(e, s);
			}, false));
		ModelLayerLocation princess = OreSpawnClient.layer("the_princess", ThePrincessModel::createBodyLayer);
		EntityRendererRegistry.register(Bosses.THE_PRINCESS, c -> new RoyalRenderer<ThePrincess>(c, princess,
			(root, pass) -> new ThePrincessModel(root, 0.65F, pass), 0.7F * 0.7F, 0.7F, (e, s) -> {
				baby(e, s);
				s.texture = s.attacking ? PRINCESS_ATTACKING : OreSpawnClient.tex("the_princess.png");
			}, true));
		ModelLayerLocation teen = OreSpawnClient.layer("the_young_prince", ThePrinceTeenModel::createBodyLayer);
		EntityRendererRegistry.register(Bosses.THE_YOUNG_PRINCE, c -> new RoyalRenderer<RoyalFlyer>(c, teen,
			(root, pass) -> new ThePrinceTeenModel(root, 0.65F, pass), 1.0F * 1.25F, 1.25F, (e, s) -> {
				s.texture = OreSpawnClient.tex("the_young_prince.png");
				flyer(e, s);
			}, false));
		ModelLayerLocation adult = OreSpawnClient.layer("the_young_adult_prince", ThePrinceAdultModel::createBodyLayer);
		EntityRendererRegistry.register(Bosses.THE_YOUNG_ADULT_PRINCE, c -> new RoyalRenderer<RoyalFlyer>(c, adult,
			(root, pass) -> new ThePrinceAdultModel(root, 0.65F, pass), 1.2F, 1.0F, (e, s) -> {
				s.texture = OreSpawnClient.tex("the_king.png");
				flyer(e, s);
			}, false));
		// RenderKraken(ModelKraken(1.0), 1.0, 1.0): state.attacking, state.variant = the entity id (its mouth's swings).
		OreSpawnClient.<Kraken>mob(Bosses.THE_KRAKEN, OreSpawnClient.layer("the_kraken", KrakenModel::createBodyLayer),
			root -> new KrakenModel(root, 1.0F), 1.0F, 1.0F, (e, s) -> {
				s.texture = OreSpawnClient.tex("the_kraken.png");
				s.attacking = e.getAttacking() != 0;
				s.variant = e.getId();
			}, null);
		// RenderPurplePower(ModelPurplePower(1.0), 0.3, 2.75).
		ModelLayerLocation purple = OreSpawnClient.layer("purple_power", PurplePowerModel::createBodyLayer);
		EntityRendererRegistry.register(Bosses.PURPLE_POWER, c -> new PurplePowerRenderer(c, purple));
		// The head hitboxes draw nothing (OreSpawn's RenderGodzillaHead etc. had no model).
		EntityRendererRegistry.register(Bosses.MOBZILLA_HEAD, NoopRenderer::new);
		EntityRendererRegistry.register(Bosses.KING_HEAD, NoopRenderer::new);
		EntityRendererRegistry.register(Bosses.QUEEN_HEAD, NoopRenderer::new);
		// 1.7.10 clients saw BetterFireball as a vanilla large fireball: drawn as the ghast's is.
		EntityRendererRegistry.register(Bosses.BETTER_FIREBALL, c -> new ThrownItemRenderer<>(c, 3.0F, true));
	}
}
