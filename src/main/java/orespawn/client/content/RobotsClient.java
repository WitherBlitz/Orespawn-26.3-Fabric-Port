package orespawn.client.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.entity.Mob;
import orespawn.client.OreSpawnClient;
import orespawn.client.model.AntRobotModel;
import orespawn.client.model.GiantRobotModel;
import orespawn.client.model.IslandModel;
import orespawn.client.model.Robot1Model;
import orespawn.client.model.Robot2Model;
import orespawn.client.model.Robot3Model;
import orespawn.client.model.Robot4Model;
import orespawn.client.model.Robot5Model;
import orespawn.client.model.SpiderRobotModel;
import orespawn.content.Robots;
import orespawn.entity.robots.AntRobot;
import orespawn.entity.robots.GiantRobot;
import orespawn.entity.robots.Robot2;
import orespawn.entity.robots.Robot3;
import orespawn.entity.robots.Robot4;
import orespawn.entity.robots.Robot5;
import orespawn.entity.robots.SpiderRobot;

/**
 * The robots' renderers, as ClientProxyOreSpawn set them up (model speed, shadow x scale, scale). state.attacking is
 * each robot's attacking flag; for the Robo-Pounder and Robo-Gunner state.value is which arms swing this cycle.
 */
@Environment(EnvType.CLIENT)
public final class RobotsClient {
	private RobotsClient() {
	}

	public static void init() {
		OreSpawnClient.<Mob>mob(Robots.BOMB_OMB, OreSpawnClient.layer("bomb_omb", Robot1Model::createBodyLayer), root -> new Robot1Model(root, 2.0F),
			0.3F, 1.0F, (e, s) -> s.texture = OreSpawnClient.tex("bomb_omb.png"), null);
		OreSpawnClient.<Robot2>mob(Robots.ROBO_POUNDER, OreSpawnClient.layer("robo_pounder", Robot2Model::createBodyLayer),
			root -> new Robot2Model(root, 1.0F), 1.0F, 1.0F, (e, s) -> {
				s.texture = OreSpawnClient.tex("robo_pounder.png");
				s.attacking = e.attacking();
				e.armChoice = Robot2Model.nextArms(e.armChoice, s.ageInTicks, e.attacking(), 1.0F);
				s.value = e.armChoice;
			}, null);
		OreSpawnClient.<Robot3>mob(Robots.ROBO_GUNNER, OreSpawnClient.layer("robo_gunner", Robot3Model::createBodyLayer),
			root -> new Robot3Model(root, 1.0F), 0.5F, 0.5F, (e, s) -> {
				s.texture = OreSpawnClient.tex("robo_gunner.png");
				s.attacking = e.attacking();
				e.armChoice = Robot3Model.nextArms(e.armChoice, s.ageInTicks, e.attacking(), 1.0F);
				s.value = e.armChoice;
			}, null);
		OreSpawnClient.<Robot4>mob(Robots.ROBO_WARRIOR, OreSpawnClient.layer("robo_warrior", Robot4Model::createBodyLayer),
			root -> new Robot4Model(root, 1.0F), 1.0F, 1.0F, (e, s) -> {
				s.texture = OreSpawnClient.tex("robo_warrior.png");
				s.attacking = e.attacking();
			}, null);
		OreSpawnClient.<Robot5>mob(Robots.ROBO_SNIPER, OreSpawnClient.layer("robo_sniper", Robot5Model::createBodyLayer),
			root -> new Robot5Model(root, 1.0F), 0.5F, 1.0F, (e, s) -> {
				s.texture = OreSpawnClient.tex("robo_sniper.png");
				s.attacking = e.attacking();
			}, null);
		OreSpawnClient.<GiantRobot>mob(Robots.JEFFERY, OreSpawnClient.layer("jeffery", GiantRobotModel::createBodyLayer),
			root -> new GiantRobotModel(root, 0.25F), 0.99F, 1.0F, (e, s) -> {
				s.texture = OreSpawnClient.tex("jeffery.png");
				s.attacking = e.attacking();
			}, null);
		ModelLayerLocation island = OreSpawnClient.layer("island", IslandModel::createBodyLayer);
		OreSpawnClient.<Mob>mob(Robots.ISLAND, island, IslandModel::new, 0.25F, 1.0F, (e, s) -> s.texture = OreSpawnClient.tex("island.png"), null);
		OreSpawnClient.<Mob>mob(Robots.ISLAND_TOO, island, IslandModel::new, 0.25F, 1.0F, (e, s) -> s.texture = OreSpawnClient.tex("island_too.png"),
			null);

		ModelLayerLocation spider = OreSpawnClient.layer("robot_spider", SpiderRobotModel::createBodyLayer);
		EntityRendererRegistry.register(Robots.ROBOT_SPIDER, c -> new LeggedRobotRenderer<SpiderRobot>(c, new SpiderRobotModel(c.bakeLayer(spider), 1.0F),
			0.99F, OreSpawnClient.tex("robot_spider.png"), 18.0));
		ModelLayerLocation ant = OreSpawnClient.layer("robot_red_ant", AntRobotModel::createBodyLayer);
		EntityRendererRegistry.register(Robots.ROBOT_RED_ANT, c -> new LeggedRobotRenderer<AntRobot>(c, new AntRobotModel(c.bakeLayer(ant), 1.0F),
			0.99F, OreSpawnClient.tex("robot_red_ant.png"), 9.0));
		EntityRendererRegistry.register(Robots.SPIDER_DRIVER, SpiderDriverRenderer::new);
	}
}
