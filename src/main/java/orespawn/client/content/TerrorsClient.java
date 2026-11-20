package orespawn.client.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Mob;
import orespawn.client.OreSpawnClient;
import orespawn.client.model.AlienModel;
import orespawn.client.model.BasiliskModel;
import orespawn.client.model.CephadromeModel;
import orespawn.client.model.CliffRacerModel;
import orespawn.client.model.CreepingHorrorModel;
import orespawn.client.model.GammaMetroidModel;
import orespawn.client.model.KyuubiModel;
import orespawn.client.model.LurkingTerrorModel;
import orespawn.client.model.PitchBlackModel;
import orespawn.client.model.TerribleTerrorModel;
import orespawn.content.Terrors;
import orespawn.entity.terrors.Alien;
import orespawn.entity.terrors.Basilisk;
import orespawn.entity.terrors.Cephadrome;
import orespawn.entity.terrors.GammaMetroid;
import orespawn.entity.terrors.LurkingTerror;
import orespawn.entity.terrors.PitchBlack;

import static orespawn.client.OreSpawnClient.layer;
import static orespawn.client.OreSpawnClient.tex;

/**
 * The Terrors group's renderers, as ClientProxyOreSpawn set them up: each model with its speed argument, shadow
 * (OreSpawn's size x scale) and scale. Render state: attacking = hunting (jaws, claws, tail); flag = flying (the
 * Nightmare and the Cephadrome); scale = the Nightmare's size; sitting = the WTF? ordered to sit (its shell stops
 * swaying); value = OreSpawn's per-creature RenderInfo rolls (the Lurking Terror's legs and jaw, the Nightmare's idle
 * jaw, the Alien's claws, tail and jaw) or, for the Cephadrome, how far it moved sideways last tick. While the Cephadrome
 * flies, yRot is replaced by the smoothed turn its neck leans into.
 */
@Environment(EnvType.CLIENT)
public final class TerrorsClient {
	private TerrorsClient() {
	}

	public static void init() {
		OreSpawnClient.<Mob>mob(Terrors.CREEPING_HORROR, layer("creeping_horror", CreepingHorrorModel::createBodyLayer), CreepingHorrorModel::new,
			0.3375F, 0.75F, (e, s) -> s.texture = tex("creeping_horror.png"), null);
		OreSpawnClient.<Mob>mob(Terrors.TERRIBLE_TERROR, layer("terrible_terror", TerribleTerrorModel::createBodyLayer), TerribleTerrorModel::new,
			0.3375F, 0.75F, (e, s) -> s.texture = tex("terrible_terror.png"), null);
		OreSpawnClient.<LurkingTerror>mob(Terrors.LURKING_TERROR, layer("lurking_terror", LurkingTerrorModel::createBodyLayer),
			LurkingTerrorModel::new, 0.3825F, 0.85F, (e, s) -> {
				s.texture = tex("lurking_terror.png");
				s.attacking = e.attacking();
				s.value = e.animBits(s.ageInTicks);
			}, null);
		// The Nightmare's renderer scaled it by its own size only (OreSpawn's renderer scale 1.0, shadow 1.25).
		OreSpawnClient.<PitchBlack>mob(Terrors.NIGHTMARE, layer("nightmare", PitchBlackModel::createBodyLayer),
			root -> new PitchBlackModel(root, 0.65F), 1.25F, 1.0F, (e, s) -> {
				s.texture = tex("nightmare.png");
				s.attacking = e.attacking();
				s.flag = e.flying();
				s.scale = e.getPitchBlackScale();
				s.value = e.jawBits(s.ageInTicks, 0.65F);
			}, null);
		OreSpawnClient.<Mob>mob(Terrors.KYUUBI, layer("kyuubi", KyuubiModel::createBodyLayer), root -> new KyuubiModel(root, 0.5F), 0.1F, 1.0F,
			(e, s) -> s.texture = tex("kyuubi.png"), null);
		OreSpawnClient.<GammaMetroid>mob(Terrors.WTF, layer("wtf", GammaMetroidModel::createBodyLayer), root -> new GammaMetroidModel(root, 0.45F),
			0.675F, 0.9F, (e, s) -> {
				s.texture = tex("wtf.png");
				s.sitting = e.isOrderedToSit();
			}, null);
		OreSpawnClient.<Alien>mob(Terrors.ALIEN, layer("alien", AlienModel::createBodyLayer), root -> new AlienModel(root, 0.22F), 0.385F, 1.1F,
			(e, s) -> {
				s.texture = tex("alien.png");
				s.attacking = e.attacking();
				s.value = e.animBits(s.ageInTicks, 0.22F);
			}, null);
		OreSpawnClient.<Basilisk>mob(Terrors.BASILISK, layer("basilisk", BasiliskModel::createBodyLayer), root -> new BasiliskModel(root, 0.3F),
			0.625F, 1.25F, (e, s) -> {
				s.texture = tex("basilisk.png");
				s.attacking = e.attacking();
			}, null);
		OreSpawnClient.<Cephadrome>mob(Terrors.CEPHADROME, layer("cephadrome", CephadromeModel::createBodyLayer),
			root -> new CephadromeModel(root, 0.55F), 1.25F, 1.0F, (e, s) -> {
				s.texture = tex("cephadrome.png");
				s.attacking = e.attacking();
				s.flag = e.flying();
				double dx = e.xo - e.getX(), dz = e.zo - e.getZ();
				s.value = (float) Math.sqrt(dx * dx + dz * dz);
				if (s.flag) s.yRot = e.neckTurn();
			}, null);
		OreSpawnClient.<Mob>mob(Terrors.CLIFF_RACER, layer("cliff_racer", CliffRacerModel::createBodyLayer), root -> new CliffRacerModel(root, 1.0F),
			0.3F, 1.0F, (e, s) -> s.texture = tex("cliff_racer.png"), null);
	}
}
