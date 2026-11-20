package orespawn.content;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import orespawn.OreSpawn;
import orespawn.block.robots.IslandBlock;
import orespawn.entity.ModEntities;
import orespawn.entity.robots.AntRobot;
import orespawn.entity.robots.GiantRobot;
import orespawn.entity.robots.Island;
import orespawn.entity.robots.IslandToo;
import orespawn.entity.robots.Robot1;
import orespawn.entity.robots.Robot2;
import orespawn.entity.robots.Robot3;
import orespawn.entity.robots.Robot4;
import orespawn.entity.robots.Robot5;
import orespawn.entity.robots.SpiderDriver;
import orespawn.entity.robots.SpiderRobot;
import orespawn.item.robots.RobotKitItem;
import orespawn.registry.ModBlocks;
import orespawn.registry.ModItems;

/**
 * OreSpawn's robots: the Bomb-Omb, Robo-Pounder, Robo-Gunner, Robo-Warrior and Robo-Sniper, Jeffery the giant robot,
 * the rideable Robot Spider (and its Spider Driver) and Robot Red Ant with their kits, and the Light and Dark Floating
 * Islands with the Island Block that grows them. (OreSpawn's PurplePower is the Bosses group's.)
 * <p>
 * None of these spawn naturally in the Overworld: OreSpawn spawned the robots and Spider Drivers only in its own
 * dimensions' biomes (and from dungeon spawners), and placed Island Blocks only in its Islands dimension. Their spawn
 * rules are registered for when those come. The Robot Laser Charge the robots fire and drop is the Weapons group's
 * (orespawn:laser_ball / robot_laser_charge).
 */
public final class Robots {
	public static final EntityType<Robot1> BOMB_OMB = ModEntities.register("bomb_omb", Robot1::new, MobCategory.MONSTER, 0.5F, 0.5F, 8);
	public static final EntityType<Robot2> ROBO_POUNDER = ModEntities.register("robo_pounder", Robot2::new, MobCategory.MONSTER, 3.0F, 6.2F, 8);
	public static final EntityType<Robot3> ROBO_GUNNER = ModEntities.register("robo_gunner", Robot3::new, MobCategory.MONSTER, 2.5F, 5.0F, 8);
	public static final EntityType<Robot4> ROBO_WARRIOR = ModEntities.register("robo_warrior", Robot4::new, MobCategory.MONSTER, 2.5F, 4.0F, 8);
	public static final EntityType<Robot5> ROBO_SNIPER = ModEntities.register("robo_sniper", Robot5::new, MobCategory.MONSTER, 1.0F, 2.25F, 8);
	public static final EntityType<GiantRobot> JEFFERY = ModEntities.register("jeffery", GiantRobot::new, MobCategory.MONSTER, 3.0F, 9.75F, 10);
	public static final EntityType<SpiderRobot> ROBOT_SPIDER = ModEntities.register("robot_spider", SpiderRobot::new, MobCategory.MISC, 3.25F,
		2.25F, 10);
	public static final EntityType<SpiderDriver> SPIDER_DRIVER = ModEntities.register("spider_driver", SpiderDriver::new, MobCategory.MONSTER, 1.4F,
		0.9F, 8);
	public static final EntityType<AntRobot> ROBOT_RED_ANT = ModEntities.register("robot_red_ant", AntRobot::new, MobCategory.MISC, 2.75F, 1.25F,
		10);
	// OreSpawn gave these no spawn eggs.
	public static final EntityType<Island> ISLAND = ModEntities.misc("island", Island::new, 0.5F, 0.5F, 8, 3);
	public static final EntityType<IslandToo> ISLAND_TOO = ModEntities.misc("island_too", IslandToo::new, 0.5F, 0.5F, 8, 3);

	/** ItemSpiderRobotKit: durability is the robot's health (1500 and 300). */
	public static final Item SPIDER_ROBOT_KIT = ModItems.register("spider_robot_kit",
		new RobotKitItem(ModItems.props("spider_robot_kit").durability(1500), () -> ROBOT_SPIDER));
	public static final Item RED_ANT_ROBOT_KIT = ModItems.register("red_ant_robot_kit",
		new RobotKitItem(ModItems.props("red_ant_robot_kit").durability(300), () -> ROBOT_RED_ANT));

	/** IslandBlock (a BlockReed): no collision, breaks at once, light 0.9, ticks randomly. */
	public static final Block ISLAND_BLOCK = ModBlocks.block("island_block", IslandBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
		.noCollision().randomTicks().instabreak().lightLevel(s -> 13).sound(SoundType.STONE).pushReaction(PushReaction.POPPED));
	public static final Block SPIDER_DRIVER_EGG_ORE = ModBlocks.eggOre("spider_driver", "orespiderdriver", OreSpawn.id("spider_driver_spawn_egg"));

	private Robots() {
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(BOMB_OMB, Robot1.createAttributes());
		FabricDefaultAttributeRegistry.register(ROBO_POUNDER, Robot2.createAttributes());
		FabricDefaultAttributeRegistry.register(ROBO_GUNNER, Robot3.createAttributes());
		FabricDefaultAttributeRegistry.register(ROBO_WARRIOR, Robot4.createAttributes());
		FabricDefaultAttributeRegistry.register(ROBO_SNIPER, Robot5.createAttributes());
		FabricDefaultAttributeRegistry.register(JEFFERY, GiantRobot.createAttributes());
		FabricDefaultAttributeRegistry.register(ROBOT_SPIDER, SpiderRobot.createAttributes());
		FabricDefaultAttributeRegistry.register(SPIDER_DRIVER, SpiderDriver.createAttributes());
		FabricDefaultAttributeRegistry.register(ROBOT_RED_ANT, AntRobot.createAttributes());
		FabricDefaultAttributeRegistry.register(ISLAND, Island.createAttributes());
		FabricDefaultAttributeRegistry.register(ISLAND_TOO, IslandToo.createAttributes());

		var ground = SpawnPlacementTypes.ON_GROUND;
		var height = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
		SpawnPlacements.register(BOMB_OMB, ground, height, Robot1::checkSpawn);
		SpawnPlacements.register(ROBO_POUNDER, ground, height, Robot2::checkSpawn);
		SpawnPlacements.register(ROBO_GUNNER, ground, height, Robot3::checkSpawn);
		SpawnPlacements.register(ROBO_WARRIOR, ground, height, Robot4::checkSpawn);
		SpawnPlacements.register(ROBO_SNIPER, ground, height, Robot5::checkSpawn);
		SpawnPlacements.register(JEFFERY, ground, height, GiantRobot::checkSpawn);
		SpawnPlacements.register(SPIDER_DRIVER, ground, height, SpiderDriver::checkSpawn);
		// No Overworld spawn tables: see the class comment.
	}
}
