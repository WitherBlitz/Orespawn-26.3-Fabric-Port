package orespawn.content;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import orespawn.OreSpawn;
import orespawn.entity.ModEntities;
import orespawn.entity.sea.AttackSquid;
import orespawn.entity.sea.CloudShark;
import orespawn.entity.sea.DeadIrukandji;
import orespawn.entity.sea.Hammerhead;
import orespawn.entity.sea.Hydrolisc;
import orespawn.entity.sea.InkSack;
import orespawn.entity.sea.Irukandji;
import orespawn.entity.sea.SeaMonster;
import orespawn.entity.sea.SeaViper;
import orespawn.entity.sea.SunspotUrchin;
import orespawn.entity.sea.Urchin;
import orespawn.entity.sea.WaterBall;
import orespawn.entity.sea.WaterDragon;
import orespawn.item.sea.SeaThrowableItem;
import orespawn.registry.ModBlocks;
import orespawn.registry.ModItems;

/**
 * OreSpawn's sea creatures and their kin: the Attack Squid (and its ink sacs), the Hammerhead, the Irukandji, the Sea
 * Monster, the Sea Viper, the Cloud Shark, the Hydrolisc, the Water Dragon and the Crystal Urchin, with the water balls
 * the squid and the dragon spit, the throwable Dead Irukandji, Sunspot Urchin and WaterDragon Charge, the trophies they
 * drop, and their egg ores.
 * <p>
 * Sizes are OreSpawn's setSize. Tracking ranges: 10 chunks for the big ones, 8 for the small, 4 for projectiles (as
 * vanilla's snowball). Natural spawns are OreSpawn's own; the Hammerhead, Irukandji, Cloud Shark and Crystal Urchin
 * spawned only in OreSpawn's Islands, Crystal and Chaos dimensions, whose groups add them to their biomes.
 */
public final class Sea {
	// ---- Creatures (each also gets <id>_spawn_egg) ----
	public static final EntityType<AttackSquid> ATTACK_SQUID = ModEntities.register("attack_squid", AttackSquid::new, MobCategory.WATER_CREATURE,
		1.0F, 1.25F, 8);
	public static final EntityType<Hammerhead> HAMMERHEAD = ModEntities.register("hammerhead", Hammerhead::new, MobCategory.MONSTER, 3.0F, 5.0F, 10);
	public static final EntityType<Irukandji> IRUKANDJI = ModEntities.register("irukandji", Irukandji::new, MobCategory.WATER_CREATURE, 0.25F, 0.25F,
		8);
	public static final EntityType<SeaMonster> SEA_MONSTER = ModEntities.register("sea_monster", SeaMonster::new, MobCategory.WATER_CREATURE, 1.25F,
		2.5F, 10);
	public static final EntityType<SeaViper> SEA_VIPER = ModEntities.register("sea_viper", SeaViper::new, MobCategory.WATER_CREATURE, 1.5F, 2.5F, 10);
	public static final EntityType<CloudShark> CLOUD_SHARK = ModEntities.register("cloud_shark", CloudShark::new, MobCategory.AMBIENT, 1.0F, 0.75F, 8);
	public static final EntityType<Hydrolisc> HYDROLISC = ModEntities.register("hydrolisc", Hydrolisc::new, MobCategory.CREATURE, 0.5F, 0.5F, 8);
	public static final EntityType<WaterDragon> WATER_DRAGON = ModEntities.register("water_dragon", WaterDragon::new, MobCategory.WATER_CREATURE,
		1.25F, 1.9F, 10);
	public static final EntityType<Urchin> CRYSTAL_URCHIN = ModEntities.register("crystal_urchin", Urchin::new, MobCategory.MONSTER, 1.35F, 2.1F, 10);

	// ---- Projectiles (EntityThrowable's 0.25 x 0.25) ----
	public static final EntityType<InkSack> INK_SACK = ModEntities.misc("ink_sack", InkSack::new, 0.25F, 0.25F, 4, 10);
	public static final EntityType<WaterBall> WATER_BALL = ModEntities.misc("water_ball", WaterBall::new, 0.25F, 0.25F, 4, 10);
	public static final EntityType<SunspotUrchin> SUNSPOT_URCHIN_ENTITY = ModEntities.misc("sunspot_urchin", SunspotUrchin::new, 0.25F, 0.25F, 4,
		10);
	public static final EntityType<DeadIrukandji> DEAD_IRUKANDJI_ENTITY = ModEntities.misc("dead_irukandji", DeadIrukandji::new, 0.25F, 0.25F, 4,
		10);

	// ---- Items ----
	/** ItemIrukandji: thrown with a loud bow sound; 100 damage on a hit. */
	public static final Item DEAD_IRUKANDJI = ModItems.register("dead_irukandji", new SeaThrowableItem(ModItems.props("dead_irukandji"),
		DeadIrukandji::new, DeadIrukandji::new, 3.0F, 1.0F));
	/** ItemSunspotUrchin: a burning throwable (OreSpawn fished it up with its Ultimate Fishing Rod). */
	public static final Item SUNSPOT_URCHIN = ModItems.register("sunspot_urchin", new SeaThrowableItem(ModItems.props("sunspot_urchin"),
		SunspotUrchin::new, SunspotUrchin::new, 0.5F, 0.0F));
	/** ItemWaterBall, "WaterDragon Charge": throws a water ball. */
	public static final Item WATERDRAGON_CHARGE = ModItems.register("waterdragon_charge", new SeaThrowableItem(ModItems.props("waterdragon_charge"),
		WaterBall::new, WaterBall::new, 0.5F, 0.0F));
	public static final Item SEA_MONSTER_SCALE = ModItems.item("sea_monster_scale", new Item.Properties());
	public static final Item SEA_VIPER_TONGUE = ModItems.item("sea_viper_tongue", new Item.Properties());
	public static final Item WATER_DRAGON_SCALE = ModItems.item("water_dragon_scale", new Item.Properties());

	// ---- Egg ores ("Ancient Dried ... Spawn Egg") ----
	public static final Block ATTACK_SQUID_EGG_ORE = eggOre("attack_squid", "oreattacksquid");
	public static final Block HAMMERHEAD_EGG_ORE = eggOre("hammerhead", "orehammerhead");
	public static final Block IRUKANDJI_EGG_ORE = eggOre("irukandji", "oreirukandji");
	public static final Block SEA_MONSTER_EGG_ORE = eggOre("sea_monster", "oreseamonster");
	public static final Block SEA_VIPER_EGG_ORE = eggOre("sea_viper", "oreseaviper");
	public static final Block CLOUD_SHARK_EGG_ORE = eggOre("cloud_shark", "orecloudshark");
	public static final Block HYDROLISC_EGG_ORE = eggOre("hydrolisc", "orehydrolisc");
	public static final Block WATER_DRAGON_EGG_ORE = eggOre("water_dragon", "orewaterdragon");
	public static final Block CRYSTAL_URCHIN_EGG_ORE = eggOre("crystal_urchin", "oreurchin");

	private Sea() {
	}

	private static Block eggOre(String id, String texture) {
		return ModBlocks.eggOre(id, texture, OreSpawn.id(id + "_spawn_egg"));
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(ATTACK_SQUID, AttackSquid.createAttributes());
		FabricDefaultAttributeRegistry.register(HAMMERHEAD, Hammerhead.createAttributes());
		FabricDefaultAttributeRegistry.register(IRUKANDJI, Irukandji.createAttributes());
		FabricDefaultAttributeRegistry.register(SEA_MONSTER, SeaMonster.createAttributes());
		FabricDefaultAttributeRegistry.register(SEA_VIPER, SeaViper.createAttributes());
		FabricDefaultAttributeRegistry.register(CLOUD_SHARK, CloudShark.createAttributes());
		FabricDefaultAttributeRegistry.register(HYDROLISC, Hydrolisc.createAttributes());
		FabricDefaultAttributeRegistry.register(WATER_DRAGON, WaterDragon.createAttributes());
		FabricDefaultAttributeRegistry.register(CRYSTAL_URCHIN, Urchin.createAttributes());

		var water = SpawnPlacementTypes.IN_WATER;
		var ground = SpawnPlacementTypes.ON_GROUND;
		var height = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
		SpawnPlacements.register(ATTACK_SQUID, water, height, AttackSquid::checkSpawn);
		SpawnPlacements.register(HAMMERHEAD, ground, height, Hammerhead::checkSpawn);
		SpawnPlacements.register(IRUKANDJI, water, height, Irukandji::checkSpawn);
		SpawnPlacements.register(SEA_MONSTER, water, height, SeaMonster::checkSpawn);
		SpawnPlacements.register(SEA_VIPER, water, height, SeaViper::checkSpawn);
		SpawnPlacements.register(CLOUD_SHARK, SpawnPlacementTypes.NO_RESTRICTIONS, height, CloudShark::checkSpawn);
		SpawnPlacements.register(HYDROLISC, ground, height, Animal::checkAnimalSpawnRules);
		SpawnPlacements.register(WATER_DRAGON, water, height, WaterDragon::checkSpawn);
		SpawnPlacements.register(CRYSTAL_URCHIN, ground, height, Urchin::checkSpawn);

		// OreSpawn's spawn tables (OreSpawnMain), "biome:weight:min:max".
		ModEntities.spawns(ATTACK_SQUID, "river:12:6:10 swamp:10:5:9 ocean:7:4:8");
		ModEntities.spawns(SEA_MONSTER, "ocean:4:1:1 swamp:2:1:1");
		ModEntities.spawns(SEA_VIPER, "ocean:3:1:1 stonebeach:2:1:1");
		ModEntities.spawns(HYDROLISC, "swamp:25:3:6 jungle:15:2:5 junglehills:10:1:3 stonebeach:5:3:6");
		ModEntities.spawns(WATER_DRAGON, "river:5:1:1 swamp:3:1:1 ocean:2:1:1 stonebeach:2:1:1");

		// OreSpawn's MyDispenserBehavior...: dispensers fire them.
		DispenserBlock.registerProjectileBehavior(DEAD_IRUKANDJI);
		DispenserBlock.registerProjectileBehavior(SUNSPOT_URCHIN);
		DispenserBlock.registerProjectileBehavior(WATERDRAGON_CHARGE);
	}
}
