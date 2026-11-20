package orespawn.client.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import orespawn.client.OreSpawnClient;
import orespawn.client.model.AlosaurusModel;
import orespawn.client.model.BaryonyxModel;
import orespawn.client.model.CamarasaurusModel;
import orespawn.client.model.CryolophosaurusModel;
import orespawn.client.model.NastysaurusModel;
import orespawn.client.model.PointysaurusModel;
import orespawn.client.model.TRexModel;
import orespawn.client.model.VelocityRaptorModel;
import orespawn.content.Dinosaurs;
import orespawn.entity.dinosaurs.Baryonyx;
import orespawn.entity.dinosaurs.Camarasaurus;
import orespawn.entity.dinosaurs.Cryolophosaurus;
import orespawn.entity.dinosaurs.HuntingDinosaur;
import orespawn.entity.dinosaurs.Nastysaurus;
import orespawn.entity.dinosaurs.VelocityRaptor;

import static orespawn.client.OreSpawnClient.layer;
import static orespawn.client.OreSpawnClient.tex;

/**
 * The dinosaurs' renderers, as ClientProxyOreSpawn set them up: each model with its speed argument, shadow (OreSpawn's
 * size x scale) and scale; babies (Baryonyx, Camarasaurus, Velocity Raptor) are drawn at half scale by the shared renderer.
 * Render state: attacking = jaws open (the hunters); sitting = ordered to sit (pets); value = whole health over max
 * health (pets' tail sway); flag = the Nastysaurus's idle jaw snap; variant = the Velocity Raptor's cannon-fodder hats
 * (always 0: cannon fodder isn't ported yet, and with it the raptor's velocityraptor2/3 hat textures).
 */
@Environment(EnvType.CLIENT)
public final class DinosaursClient {
	private DinosaursClient() {
	}

	public static void init() {
		OreSpawnClient.<HuntingDinosaur>mob(Dinosaurs.ALOSAURUS, layer("alosaurus", AlosaurusModel::createBodyLayer),
			root -> new AlosaurusModel(root, 0.22F), 1.0F, 1.0F, (e, s) -> {
				s.texture = tex("alosaurus.png");
				s.attacking = e.attacking();
			}, null);
		OreSpawnClient.<HuntingDinosaur>mob(Dinosaurs.T_REX, layer("t_rex", TRexModel::createBodyLayer), root -> new TRexModel(root, 0.2F), 1.2F,
			1.2F, (e, s) -> {
				s.texture = tex("t_rex.png");
				s.attacking = e.attacking();
			}, null);
		OreSpawnClient.<Nastysaurus>mob(Dinosaurs.NASTYSAURUS, layer("nastysaurus", NastysaurusModel::createBodyLayer),
			root -> new NastysaurusModel(root, 0.65F), 1.5F, 1.5F, (e, s) -> {
				s.texture = tex("nastysaurus.png");
				s.attacking = e.attacking();
				s.flag = !s.attacking && e.idleJawOpen(s.ageInTicks);
			}, null);
		OreSpawnClient.<HuntingDinosaur>mob(Dinosaurs.POINTYSAURUS, layer("pointysaurus", PointysaurusModel::createBodyLayer),
			root -> new PointysaurusModel(root, 1.0F), 1.0F, 1.0F, (e, s) -> {
				s.texture = tex("pointysaurus.png");
				s.attacking = e.attacking();
			}, null);
		OreSpawnClient.<Cryolophosaurus>mob(Dinosaurs.CRYOLOPHOSAURUS, layer("cryolophosaurus", CryolophosaurusModel::createBodyLayer),
			root -> new CryolophosaurusModel(root, 0.75F), 0.375F, 0.5F, (e, s) -> s.texture = tex("cryolophosaurus.png"), null);
		OreSpawnClient.<Baryonyx>mob(Dinosaurs.BARYONYX, layer("baryonyx", BaryonyxModel::createBodyLayer), root -> new BaryonyxModel(root, 0.25F),
			1.0F, 1.0F, (e, s) -> s.texture = tex("baryonyx.png"), null);
		OreSpawnClient.<Camarasaurus>mob(Dinosaurs.CAMARASAURUS, layer("camarasaurus", CamarasaurusModel::createBodyLayer),
			root -> new CamarasaurusModel(root, 0.65F), 0.4225F, 0.65F, (e, s) -> {
				s.texture = tex("camarasaurus.png");
				s.sitting = e.isOrderedToSit();
				s.value = (int) e.getHealth() / e.getMaxHealth();
			}, null);
		OreSpawnClient.<VelocityRaptor>mob(Dinosaurs.VELOCITY_RAPTOR, layer("velocity_raptor", VelocityRaptorModel::createBodyLayer),
			root -> new VelocityRaptorModel(root, 1.25F), 0.4125F, 0.75F, (e, s) -> {
				s.texture = tex("velocity_raptor.png");
				s.sitting = e.isOrderedToSit();
				s.value = (int) e.getHealth() / e.getMaxHealth();
				s.variant = 0;
			}, null);
	}
}
