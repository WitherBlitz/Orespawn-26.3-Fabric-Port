package orespawn.client.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import orespawn.client.OreSpawnClient;
import orespawn.client.model.BeeModel;
import orespawn.client.model.BrutalflyModel;
import orespawn.client.model.CaterKillerModel;
import orespawn.client.model.CaveFisherModel;
import orespawn.client.model.EmperorScorpionModel;
import orespawn.client.model.HerculesBeetleModel;
import orespawn.client.model.MantisModel;
import orespawn.client.model.MolenoidModel;
import orespawn.content.Bugs;
import orespawn.entity.bugs.Bee;
import orespawn.entity.bugs.CaterKiller;
import orespawn.entity.bugs.CaveFisher;
import orespawn.entity.bugs.EmperorScorpion;
import orespawn.entity.bugs.HerculesBeetle;
import orespawn.entity.bugs.Mantis;
import orespawn.entity.bugs.Molenoid;

import static orespawn.client.OreSpawnClient.layer;
import static orespawn.client.OreSpawnClient.tex;

/**
 * The bugs' renderers, as ClientProxyOreSpawn set them up: each model with its speed argument, OreSpawn's shadow (its
 * renderers' size x scale) and scale. Models read {@code attacking}; the Emperor Scorpion's and Cave Fisher's also read
 * OreSpawn's RenderInfo, rolled here each frame as their models did ({@code variant} = ri1, {@code flag} = ri2 == 1).
 */
@Environment(EnvType.CLIENT)
public final class BugsClient {
	private static final float EMPEROR_SCORPION_SPEED = 0.22F;
	private static final float CAVE_FISHER_SPEED = 0.62F;

	private BugsClient() {
	}

	public static void init() {
		OreSpawnClient.<Mantis>mob(Bugs.MANTIS, layer("mantis", MantisModel::createBodyLayer), root -> new MantisModel(root, 2.0F), 0.99F, 1.1F,
			(e, s) -> {
				s.texture = tex("mantis.png");
				s.attacking = e.attacking();
			}, null);
		OreSpawnClient.<HerculesBeetle>mob(Bugs.HERCULES_BEETLE, layer("hercules_beetle", HerculesBeetleModel::createBodyLayer),
			root -> new HerculesBeetleModel(root, 1.0F), 1.089F, 1.1F, (e, s) -> {
				s.texture = tex("hercules_beetle.png");
				s.attacking = e.attacking();
			}, null);
		OreSpawnClient.<Bee>mob(Bugs.BEE, layer("bee", BeeModel::createBodyLayer), root -> new BeeModel(root, 2.0F), 0.99F, 1.1F, (e, s) -> {
			s.texture = tex("bee.png");
			s.attacking = e.attacking();
		}, null);
		ModelLayerLocation brutalfly = layer("brutalfly", BrutalflyModel::createBodyLayer);
		EntityRendererRegistry.register(Bugs.BRUTALFLY, c -> new BrutalflyRenderer(c, new BrutalflyModel(c.bakeLayer(brutalfly), 0.2F), 6.75F, 9.0F));
		EntityRendererRegistry.register(Bugs.BRUTALFLY_FIREBALL, c -> new ThrownItemRenderer<>(c, 3.0F, true));
		OreSpawnClient.<CaterKiller>mob(Bugs.CATERKILLER, layer("caterkiller", CaterKillerModel::createBodyLayer),
			root -> new CaterKillerModel(root, 0.22F), 1.25F, 1.25F, (e, s) -> {
				s.texture = tex("caterkiller.png");
				s.attacking = e.attacking();
			}, null);
		OreSpawnClient.<EmperorScorpion>mob(Bugs.EMPEROR_SCORPION, layer("emperor_scorpion", EmperorScorpionModel::createBodyLayer),
			root -> new EmperorScorpionModel(root, EMPEROR_SCORPION_SPEED), 1.425F, 1.5F, (e, s) -> {
				s.texture = tex("emperor_scorpion.png");
				s.attacking = e.attacking();
				if (snapStarts(s.ageInTicks, EMPEROR_SCORPION_SPEED)) {
					RandomSource r = e.level().getRandom();
					e.renderClaws = s.attacking ? r.nextInt(4) : r.nextInt(20);
					e.renderTail = s.attacking ? r.nextInt(3) : r.nextInt(25);
				}
				s.variant = e.renderClaws;
				s.flag = e.renderTail == 1;
			}, null);
		OreSpawnClient.<CaveFisher>mob(Bugs.CAVE_FISHER, layer("cave_fisher", CaveFisherModel::createBodyLayer),
			root -> new CaveFisherModel(root, CAVE_FISHER_SPEED), 0.2625F, 0.75F, (e, s) -> {
				s.texture = tex("cave_fisher.png");
				s.attacking = e.attacking();
				if (snapStarts(s.ageInTicks, CAVE_FISHER_SPEED)) {
					RandomSource r = e.level().getRandom();
					e.renderClaws = s.attacking ? r.nextInt(4) : r.nextInt(20);
					e.renderTail = s.attacking ? r.nextInt(3) : r.nextInt(25);
				}
				s.variant = e.renderClaws;
			}, null);
		OreSpawnClient.<Molenoid>mob(Bugs.MOLENOID, layer("molenoid", MolenoidModel::createBodyLayer), root -> new MolenoidModel(root, 0.5F), 1.0F,
			1.0F, (e, s) -> {
				s.texture = tex("molenoid.png");
				s.attacking = e.attacking();
			}, null);
	}

	/** The models' claw wave is about to cross zero going up: where OreSpawn rolled the next claw and tail moves. */
	private static boolean snapStarts(float ageInTicks, float speed) {
		float now = Mth.cos(ageInTicks * 3.0F * speed) * (float) Math.PI * 0.15F;
		float next = Mth.cos((ageInTicks + 0.1F) * 3.0F * speed) * (float) Math.PI * 0.15F;
		return next > 0.0F && now < 0.0F;
	}
}
