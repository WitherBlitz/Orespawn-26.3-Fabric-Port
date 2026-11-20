package orespawn.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import orespawn.OreSpawn;
import orespawn.client.model.BeaverModel;
import orespawn.client.model.BirdModel;
import orespawn.client.model.ButterflyModel;
import orespawn.client.model.CassowaryModel;
import orespawn.client.model.ChipmunkModel;
import orespawn.client.model.CricketModel;
import orespawn.client.model.FireflyModel;
import orespawn.client.model.FrogModel;
import orespawn.client.model.LegacyCowModel;
import orespawn.client.model.RatModel;
import orespawn.entity.Bird;
import orespawn.entity.Butterfly;
import orespawn.entity.Chipmunk;
import orespawn.entity.Cricket;
import orespawn.entity.Firefly;
import orespawn.entity.Frog;
import orespawn.entity.ModEntities;
import orespawn.entity.Rat;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * Renderers for OreSpawn's creatures, set up as OreSpawn's ClientProxyOreSpawn did: each model with its speed
 * argument, shadow size and scale, and the textures it picked from the entity.
 */
public final class OreSpawnClient implements ClientModInitializer {
	public static Identifier tex(String file) {
		return OreSpawn.id("textures/entity/" + file);
	}

	private static final Identifier[] BUTTERFLIES = {tex("butterfly.png"), tex("butterfly2.png"), tex("butterfly3.png"), tex("butterfly4.png")};
	/** EntityButterfly: type 1 in the Islands dimension. */
	private static final Identifier ISLANDS_BUTTERFLY = tex("vbutterfly1.png");
	/** EntityButterfly.getTexture for moths: 0 luna, 1 eye, 2 dark, 3 fire. */
	private static final Identifier[] MOTHS = {tex("lunamoth.png"), tex("eyemoth.png"), tex("darkmoth.png"), tex("firemoth.png")};
	private static final Identifier[] BIRDS = {tex("bird1.png"), tex("bird2.png"), tex("bird3.png"), tex("bird4.png"), tex("bird5.png"),
		tex("bird6.png")};

	@Override
	public void onInitializeClient() {
		ModelLayerLocation cow = layer("legacy_cow", LegacyCowModel::createBodyLayer);
		cow(ModEntities.APPLE_COW, cow, tex("red_cow.png"), false);
		cow(ModEntities.GOLDEN_APPLE_COW, cow, tex("gold_cow.png"), false);
		cow(ModEntities.ENCHANTED_GOLDEN_APPLE_COW, cow, tex("gold_cow.png"), true);
		ModelLayerLocation butterfly = layer("butterfly", ButterflyModel::createBodyLayer);
		OreSpawnClient.<Butterfly>mob(ModEntities.BUTTERFLY, butterfly, root -> new ButterflyModel(root, 1.0F), 0.3F, 1.0F,
			(e, s) -> s.texture = e.variant() == 1 && e.level().dimension() == orespawn.world.OreSpawnDimensions.ISLANDS ? ISLANDS_BUTTERFLY
				: BUTTERFLIES[Math.floorMod(e.variant(), 4)], null);
		OreSpawnClient.<Butterfly>mob(ModEntities.MOTH, butterfly, root -> new ButterflyModel(root, 0.75F), 0.4F, 1.5F, (e, s) -> {
			s.texture = MOTHS[Math.floorMod(e.variant(), 4)];
			s.swirl = e.variant() == 0;
		}, null);
		OreSpawnClient.<Firefly>mob(ModEntities.FIREFLY, layer("firefly", FireflyModel::createBodyLayer), root -> new FireflyModel(root, 2.5F), 0.2F, 0.75F,
			(e, s) -> {
				s.texture = tex("firefly.png");
				s.blink = e.lit();
			}, e -> e.lit() ? 15 : 0);
		OreSpawnClient.<Bird>mob(ModEntities.BIRD, layer("bird", BirdModel::createBodyLayer), root -> new BirdModel(root, 1.0F), 0.3F, 0.75F,
			(e, s) -> s.texture = BIRDS[Math.floorMod(e.variant(), 6)], null);
		OreSpawnClient.<Chipmunk>mob(ModEntities.CHIPMUNK, layer("chipmunk", ChipmunkModel::createBodyLayer), root -> new ChipmunkModel(root, 1.0F), 0.15F,
			0.9F, (e, s) -> {
				s.texture = tex("chipmunk.png");
				s.sitting = e.isInSittingPose();
			}, null);
		OreSpawnClient.<Cricket>mob(ModEntities.CRICKET, layer("cricket", CricketModel::createBodyLayer), root -> new CricketModel(root, 2.5F), 0.15F, 0.5F,
			(e, s) -> {
				s.texture = tex("cricket.png");
				s.singing = e.singing();
			}, null);
		OreSpawnClient.<Frog>mob(ModEntities.FROG, layer("frog", FrogModel::createBodyLayer), root -> new FrogModel(root, 1.0F), 0.35F, 1.0F, (e, s) -> {
			s.texture = tex("frog.png");
			s.singing = e.singing();
		}, null);
		OreSpawnClient.<Mob>mob(ModEntities.BEAVER, layer("beaver", BeaverModel::createBodyLayer), root -> new BeaverModel(root, 0.5F), 0.15F, 0.75F,
			(e, s) -> s.texture = tex("beaver.png"), null);
		OreSpawnClient.<Mob>mob(ModEntities.CASSOWARY, layer("cassowary", CassowaryModel::createBodyLayer), root -> new CassowaryModel(root, 0.55F), 0.5F,
			1.0F, (e, s) -> s.texture = tex("cassowary.png"), null);
		OreSpawnClient.<Rat>mob(ModEntities.RAT, layer("rat", RatModel::createBodyLayer), root -> new RatModel(root, 1.0F), 0.1F, 0.75F, (e, s) -> {
			s.texture = tex("rat.png");
			s.attacking = e.attacking();
		}, null);
		// batch 2
		OreSpawnClient.<orespawn.entity.Peacock>mob(ModEntities.PEACOCK, layer("peacock", orespawn.client.model.PeacockModel::createBodyLayer),
			root -> new orespawn.client.model.PeacockModel(root, 0.75F), 0.25F, 1.0F, (e, s) -> {
				s.texture = tex("peacock.png");
				s.blink = e.showingOff();
			}, null);
		OreSpawnClient.<Mob>mob(ModEntities.STINK_BUG, layer("stink_bug", orespawn.client.model.StinkBugModel::createBodyLayer),
			root -> new orespawn.client.model.StinkBugModel(root, 0.75F), 0.35F, 0.85F, (e, s) -> s.texture = tex("stink_bug.png"), null);
		OreSpawnClient.<Mob>mob(ModEntities.MOSQUITO, layer("mosquito", orespawn.client.model.MosquitoModel::createBodyLayer),
			orespawn.client.model.MosquitoModel::new, 0.3F, 0.5F, (e, s) -> s.texture = tex("mosquito.png"), null);
		OreSpawnClient.<Mob>mob(ModEntities.DRAGONFLY, layer("dragonfly", orespawn.client.model.DragonflyModel::createBodyLayer),
			root -> new orespawn.client.model.DragonflyModel(root, 2.0F), 0.3F, 1.5F, (e, s) -> s.texture = tex("dragonfly.png"), null);
		OreSpawnClient.<Mob>mob(ModEntities.GOLD_FISH, layer("gold_fish", orespawn.client.model.GoldFishModel::createBodyLayer),
			root -> new orespawn.client.model.GoldFishModel(root, 0.7F), 0.2F, 1.0F, (e, s) -> s.texture = tex("gold_fish.png"), null);
		OreSpawnClient.<Mob>mob(ModEntities.FLOUNDER, layer("flounder", orespawn.client.model.FlounderModel::createBodyLayer),
			orespawn.client.model.FlounderModel::new, 0.1F, 1.0F, (e, s) -> s.texture = tex("flounder.png"), null);
		OreSpawnClient.<Mob>mob(ModEntities.SKATE, layer("skate", orespawn.client.model.SkateModel::createBodyLayer),
			root -> new orespawn.client.model.SkateModel(root, 1.0F), 0.1F, 0.75F, (e, s) -> s.texture = tex("skate.png"), null);
		OreSpawnClient.<Mob>mob(ModEntities.WHALE, layer("whale", orespawn.client.model.WhaleModel::createBodyLayer),
			orespawn.client.model.WhaleModel::new, 0.1F, 1.0F, (e, s) -> s.texture = tex("whale.png"), null);
		OreSpawnClient.<orespawn.entity.Gazelle>mob(ModEntities.GAZELLE, layer("gazelle", orespawn.client.model.GazelleModel::createBodyLayer),
			root -> new orespawn.client.model.GazelleModel(root, 0.65F), 0.45F, 1.0F, (e, s) -> {
				s.texture = tex("gazelle.png");
				s.sitting = e.isInSittingPose();
			}, null);
		OreSpawnClient.<orespawn.entity.Ostrich>mob(ModEntities.OSTRICH, layer("ostrich", orespawn.client.model.OstrichModel::createBodyLayer),
			root -> new orespawn.client.model.OstrichModel(root, 0.65F), 0.55F, 1.0F, (e, s) -> {
				s.texture = tex("ostrich.png");
				s.sitting = e.isInSittingPose();
				s.value = e.legSpeed();
				s.flag = e.wingsFlapping();
				s.yRot = e.headYaw(s.yRot);
			}, null);
		OreSpawnClient.<orespawn.entity.Lizard>mob(ModEntities.LIZARD, layer("lizard", orespawn.client.model.LizardModel::createBodyLayer),
			root -> new orespawn.client.model.LizardModel(root, 0.65F), 0.75F, 1.0F, (e, s) -> {
				s.texture = tex("lizard.png");
				s.attacking = e.attacking();
			}, null);
		OreSpawnClient.<orespawn.entity.Crab>mob(ModEntities.CRAB, layer("crab", orespawn.client.model.CrabModel::createBodyLayer),
			orespawn.client.model.CrabModel::new, 0.99F, 1.0F, (e, s) -> {
				s.texture = tex("crab.png");
				s.scale = e.getCrabScale();
				s.attacking = e.attacking();
			}, null);
		OreSpawnClient.<orespawn.entity.Scorpion>mob(ModEntities.SCORPION, layer("scorpion", orespawn.client.model.ScorpionModel::createBodyLayer),
			root -> new orespawn.client.model.ScorpionModel(root, 0.62F), 0.2625F, 0.75F, (e, s) -> {
				s.texture = tex("scorpion.png");
				s.variant = e.clawAnimation(s.ageInTicks);
			}, null);
		OreSpawnClient.<Mob>mob(ModEntities.SMALL_WORM, layer("small_worm", orespawn.client.model.WormSmallModel::createBodyLayer),
			orespawn.client.model.WormSmallModel::new, 0.1F, 1.0F, (e, s) -> s.texture = tex("small_worm.png"), null);
		OreSpawnClient.<Mob>mob(ModEntities.MEDIUM_WORM, layer("medium_worm", orespawn.client.model.WormMediumModel::createBodyLayer),
			orespawn.client.model.WormMediumModel::new, 0.25F, 1.0F, (e, s) -> s.texture = tex("medium_worm.png"), null);
		OreSpawnClient.<Mob>mob(ModEntities.LARGE_WORM, layer("large_worm", orespawn.client.model.WormLargeModel::createBodyLayer),
			orespawn.client.model.WormLargeModel::new, 0.9F, 1.0F, (e, s) -> s.texture = tex("large_worm.png"), null);
		ClientContent.init();
	}

	public static ModelLayerLocation layer(String name, Supplier<net.minecraft.client.model.geom.builders.LayerDefinition> def) {
		ModelLayerLocation layer = new ModelLayerLocation(OreSpawn.id(name), "main");
		ModelLayerRegistry.registerModelLayer(layer, def::get);
		return layer;
	}

	private static void cow(EntityType<? extends Mob> type, ModelLayerLocation layer, Identifier texture, boolean glint) {
		EntityRendererRegistry.register(type, (EntityRendererProvider.Context c) -> new OreSpawnMobRenderer<Mob>(c,
			new LegacyCowModel(c.bakeLayer(layer)), 0.7F, 1.0F, (e, s) -> {
				s.texture = texture;
				s.glint = glint;
			}, null));
	}

	@SuppressWarnings("unchecked")
	public static <E extends Mob> void mob(EntityType<? extends Mob> type, ModelLayerLocation layer, Function<ModelPart, EntityModel<OreSpawnRenderState>> model,
			float shadow, float scale, BiConsumer<E, OreSpawnRenderState> extractor, ToIntFunction<E> light) {
		EntityRendererRegistry.register((EntityType<E>) type, c -> new OreSpawnMobRenderer<>(c, model.apply(c.bakeLayer(layer)), shadow, scale,
			extractor, light));
	}
}
