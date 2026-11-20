package orespawn.client.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.world.entity.Mob;
import orespawn.client.OreSpawnClient;
import orespawn.client.model.AttackSquidModel;
import orespawn.client.model.CloudSharkModel;
import orespawn.client.model.HammerheadModel;
import orespawn.client.model.HydroliscModel;
import orespawn.client.model.IrukandjiModel;
import orespawn.client.model.SeaMonsterModel;
import orespawn.client.model.SeaViperModel;
import orespawn.client.model.UrchinModel;
import orespawn.client.model.WaterDragonModel;
import orespawn.content.Sea;
import orespawn.entity.sea.Hammerhead;
import orespawn.entity.sea.Hydrolisc;
import orespawn.entity.sea.SeaSerpent;
import orespawn.entity.sea.Urchin;
import orespawn.entity.sea.WaterDragon;

import static orespawn.client.OreSpawnClient.layer;
import static orespawn.client.OreSpawnClient.tex;

/**
 * The Sea group's renderers, as OreSpawn's ClientProxyOreSpawn set them up: each model with its speed argument, the
 * shadow its RenderX gave (size times scale) and the scale. Babies (Hydrolisc, Water Dragon) are drawn at half size, as
 * their renderers did. The projectiles are spinning item pictures (OreSpawn's RenderItemUrchin).
 */
@Environment(EnvType.CLIENT)
public final class SeaClient {
	private SeaClient() {
	}

	public static void init() {
		OreSpawnClient.<Mob>mob(Sea.ATTACK_SQUID, layer("attack_squid", AttackSquidModel::createBodyLayer), root -> new AttackSquidModel(root, 1.0F),
			0.25F * 0.9F, 0.9F, (e, s) -> s.texture = tex("attack_squid.png"), null);
		OreSpawnClient.<Hammerhead>mob(Sea.HAMMERHEAD, layer("hammerhead", HammerheadModel::createBodyLayer), root -> new HammerheadModel(root, 0.33F),
			1.0F * 2.5F, 2.5F, (e, s) -> {
				s.texture = tex("hammerhead.png");
				s.attacking = e.attacking();
			}, null);
		OreSpawnClient.<Mob>mob(Sea.IRUKANDJI, layer("irukandji", IrukandjiModel::createBodyLayer), root -> new IrukandjiModel(root, 1.0F),
			0.1F * 0.25F, 0.25F, (e, s) -> s.texture = tex("irukandji.png"), null);
		OreSpawnClient.<SeaSerpent>mob(Sea.SEA_MONSTER, layer("sea_monster", SeaMonsterModel::createBodyLayer), root -> new SeaMonsterModel(root, 0.5F),
			1.0F, 1.0F, (e, s) -> {
				s.texture = tex("sea_monster.png");
				s.attacking = e.attacking();
			}, null);
		OreSpawnClient.<SeaSerpent>mob(Sea.SEA_VIPER, layer("sea_viper", SeaViperModel::createBodyLayer), root -> new SeaViperModel(root, 0.5F),
			1.0F, 1.0F, (e, s) -> {
				s.texture = tex("sea_viper.png");
				s.attacking = e.attacking();
			}, null);
		OreSpawnClient.<Mob>mob(Sea.CLOUD_SHARK, layer("cloud_shark", CloudSharkModel::createBodyLayer), root -> new CloudSharkModel(root, 1.0F),
			0.5F, 1.0F, (e, s) -> s.texture = tex("cloud_shark.png"), null);
		// value: health over max health (OreSpawn's (int) health / max health), which sways its feathers.
		OreSpawnClient.<Hydrolisc>mob(Sea.HYDROLISC, layer("hydrolisc", HydroliscModel::createBodyLayer), root -> new HydroliscModel(root, 0.65F),
			0.65F * 0.65F, 0.65F, (e, s) -> {
				s.texture = tex("hydrolisc.png");
				s.sitting = e.isInSittingPose();
				s.value = (int) e.getHealth() / e.getMaxHealth();
			}, null);
		// attacking: biting (OreSpawn's state 1); flag: spraying water (state 2).
		OreSpawnClient.<WaterDragon>mob(Sea.WATER_DRAGON, layer("water_dragon", WaterDragonModel::createBodyLayer),
			root -> new WaterDragonModel(root, 0.5F), 0.85F * 1.1F, 1.1F, (e, s) -> {
				s.texture = tex("water_dragon.png");
				s.sitting = e.isInSittingPose();
				s.attacking = e.attacking() == 1;
				s.flag = e.attacking() == 2;
			}, null);
		OreSpawnClient.<Urchin>mob(Sea.CRYSTAL_URCHIN, layer("crystal_urchin", UrchinModel::createBodyLayer), root -> new UrchinModel(root, 1.0F),
			0.35F * 1.25F, 1.25F, (e, s) -> {
				s.texture = tex("crystal_urchin.png");
				s.attacking = e.attacking();
			}, null);

		EntityRendererRegistry.register(Sea.INK_SACK, c -> new SeaSpinnerRenderer<>(c, 30.0F));
		EntityRendererRegistry.register(Sea.WATER_BALL, c -> new SeaSpinnerRenderer<>(c, 30.0F));
		EntityRendererRegistry.register(Sea.SUNSPOT_URCHIN_ENTITY, c -> new SeaSpinnerRenderer<>(c, 30.0F));
		EntityRendererRegistry.register(Sea.DEAD_IRUKANDJI_ENTITY, c -> new SeaSpinnerRenderer<>(c, 50.0F));
	}
}
