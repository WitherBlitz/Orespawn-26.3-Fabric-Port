package orespawn.client.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import orespawn.client.OreSpawnClient;
import orespawn.client.model.BirdModel;
import orespawn.client.model.ButterflyModel;
import orespawn.client.model.CoinModel;
import orespawn.client.model.CriminalModel;
import orespawn.client.model.DragonModel;
import orespawn.client.model.EasterBunnyModel;
import orespawn.client.model.LeonModel;
import orespawn.client.model.RotatorModel;
import orespawn.client.model.RubberDuckyModel;
import orespawn.client.model.SpitBugModel;
import orespawn.client.model.SpyroModel;
import orespawn.client.model.StinkyModel;
import orespawn.client.model.TrooperBugModel;
import orespawn.client.model.TshirtModel;
import orespawn.client.model.VortexModel;
import orespawn.content.Oddities;
import orespawn.entity.oddities.Dragon;
import orespawn.entity.oddities.FlyingMount;
import orespawn.entity.oddities.Leon;
import orespawn.entity.oddities.Mothra;
import orespawn.entity.oddities.RubberDucky;
import orespawn.entity.oddities.SpitBug;
import orespawn.entity.oddities.Spyro;
import orespawn.entity.oddities.Stinky;
import orespawn.entity.oddities.TrooperBug;

import static orespawn.client.OreSpawnClient.layer;
import static orespawn.client.OreSpawnClient.tex;

/**
 * The Oddities' renderers, as ClientProxyOreSpawn set them up: each model with its speed argument, OreSpawn's shadow
 * (its size times its scale) and scale, and the textures and states its renderers and models read.
 */
@Environment(EnvType.CLIENT)
public final class OdditiesClient {
	private static final Identifier[] STINKY = new Identifier[19];

	static {
		for (int i = 0; i < 19; i++) STINKY[i] = tex("stinky" + (i + 1) + ".png");
	}

	private OdditiesClient() {
	}

	public static void init() {
		OreSpawnClient.<Leon>mob(Oddities.LEONOPTERYX, layer("leonopteryx", LeonModel::createBodyLayer), root -> new LeonModel(root, 0.22F), 1.75F,
			1.75F, (e, s) -> {
				s.texture = tex("leonopteryx.png");
				s.flag = e.activity() != 0;
				s.attacking = e.attacking();
				s.sitting = e.isInSittingPose();
				if (s.ridden) s.yRot = neck(e);
			}, null);
		OreSpawnClient.<Spyro>mob(Oddities.BABY_DRAGON, layer("baby_dragon", SpyroModel::createBodyLayer), root -> new SpyroModel(root, 0.65F),
			0.65F * 0.75F, 0.75F, (e, s) -> {
				s.texture = tex("baby_dragon.png");
				s.value = e.activity();
				s.sitting = e.isInSittingPose();
			}, null);
		OreSpawnClient.<Dragon>mob(Oddities.DRAGON, layer("dragon", DragonModel::createBodyLayer), root -> new DragonModel(root, 0.65F), 1.25F, 1.0F,
			(e, s) -> {
				s.texture = tex(e.dragonType() != 0 ? "white_dragon.png" : "dragon.png");
				s.flag = e.activity() != 0;
				s.attacking = e.attacking();
				s.sitting = e.isInSittingPose();
				double dx = e.xo - e.getX(), dz = e.zo - e.getZ();
				s.value = s.walkAnimationSpeed > 0.001F ? (float) Math.sqrt(dx * dx + dz * dz) : 0.0F;
				s.yRot = s.flag ? neck(e) : s.yRot / 2.0F;
			}, null);
		OreSpawnClient.<Stinky>mob(Oddities.STINKY, layer("stinky", StinkyModel::createBodyLayer), root -> new StinkyModel(root, 0.65F), 0.75F, 1.0F,
			(e, s) -> {
				s.texture = STINKY[Mth.clamp(e.skin(), 0, 18)];
				s.value = e.activity();
				s.sitting = e.isInSittingPose();
			}, null);
		OreSpawnClient.<Mothra>mob(Oddities.MOTHRA, layer("mothra", ButterflyModel::createBodyLayer), root -> new ButterflyModel(root, 0.2F),
			0.75F * 10.0F, 10.0F, (e, s) -> {
				s.texture = tex("eyemoth.png");
				s.swirl = true;
			}, null);
		OreSpawnClient.<Mob>mob(Oddities.RUBY_BIRD, layer("ruby_bird", BirdModel::createBodyLayer), root -> new BirdModel(root, 1.0F), 0.3F * 0.75F,
			0.75F, (e, s) -> s.texture = tex("bird6.png"), null);
		OreSpawnClient.<Mob>mob(Oddities.ROTATOR, layer("rotator", RotatorModel::createBodyLayer), root -> new RotatorModel(root, 0.25F), 0.1F, 1.0F,
			(e, s) -> s.texture = tex("rotator.png"), null);
		OreSpawnClient.<Mob>mob(Oddities.VORTEX, layer("vortex", VortexModel::createBodyLayer), root -> new VortexModel(root, 0.25F), 0.1F, 1.0F,
			(e, s) -> s.texture = tex("vortex.png"), null);
		OreSpawnClient.<Mob>mob(Oddities.CRIMINAL, layer("criminal", CriminalModel::createBodyLayer), root -> new CriminalModel(root, 0.4F), 1.0F,
			1.0F, (e, s) -> s.texture = tex("criminal.png"), null);
		OreSpawnClient.<Mob>mob(Oddities.T_SHIRT, layer("t_shirt", TshirtModel::createBodyLayer), root -> new TshirtModel(root, 0.22F), 0.33F, 0.33F,
			(e, s) -> s.texture = tex("t_shirt.png"), null);
		OreSpawnClient.<Mob>mob(Oddities.COIN, layer("coin", CoinModel::createBodyLayer), root -> new CoinModel(root, 0.22F), 0.75F * 0.125F, 0.125F,
			(e, s) -> s.texture = tex("coin.png"), null);
		OreSpawnClient.<Mob>mob(Oddities.EASTER_BUNNY, layer("easter_bunny", EasterBunnyModel::createBodyLayer), root -> new EasterBunnyModel(root,
			0.55F), 0.5F, 1.0F, (e, s) -> s.texture = tex("easter_bunny.png"), null);
		OreSpawnClient.<RubberDucky>mob(Oddities.RUBBER_DUCKY, layer("rubber_ducky", RubberDuckyModel::createBodyLayer),
			root -> new RubberDuckyModel(root, 1.0F), 0.15F * 0.75F, 0.75F, (e, s) -> {
				s.flag = e.killCount() >= 5;
				s.texture = tex(s.flag ? "evil_rubber_ducky.png" : "rubber_ducky.png");
				s.sitting = e.isInSittingPose();
			}, null);
		OreSpawnClient.<SpitBug>mob(Oddities.SPIT_BUG, layer("spit_bug", SpitBugModel::createBodyLayer), root -> new SpitBugModel(root, 0.55F),
			0.55F * 0.75F, 0.75F, (e, s) -> {
				s.texture = tex("spit_bug.png");
				s.attacking = e.attacking();
			}, null);
		OreSpawnClient.<TrooperBug>mob(Oddities.JUMPY_BUG, layer("jumpy_bug", TrooperBugModel::createBodyLayer), root -> new TrooperBugModel(root,
			0.22F), 0.95F * 1.1F, 1.1F, (e, s) -> {
				s.texture = tex("jumpy_bug.png");
				s.attacking = e.attacking();
			}, null);
	}

	/**
	 * The neck/head swing OreSpawn's Dragon and Leonopteryx models worked out each frame while flying or ridden (kept in
	 * RenderInfo.rf1): it eases towards eight times the last turn, within 50 degrees.
	 */
	private static float neck(FlyingMount e) {
		float turn = -(e.yRotO - e.getYRot()) * 8.0F;
		e.renderNeck = Mth.clamp(e.renderNeck + (turn - e.renderNeck) / 60.0F, -50.0F, 50.0F);
		return e.renderNeck;
	}
}
