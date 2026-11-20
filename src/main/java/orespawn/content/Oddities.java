package orespawn.content;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;
import orespawn.OreSpawn;
import orespawn.entity.ModEntities;
import orespawn.entity.oddities.Coin;
import orespawn.entity.oddities.Criminal;
import orespawn.entity.oddities.Dragon;
import orespawn.entity.oddities.EasterBunny;
import orespawn.entity.oddities.Leon;
import orespawn.entity.oddities.Mothra;
import orespawn.entity.oddities.Rotator;
import orespawn.entity.oddities.RubberDucky;
import orespawn.entity.oddities.RubyBird;
import orespawn.entity.oddities.SpitBug;
import orespawn.entity.oddities.Spyro;
import orespawn.entity.oddities.Stinky;
import orespawn.entity.oddities.TrooperBug;
import orespawn.entity.oddities.Tshirt;
import orespawn.entity.oddities.Vortex;
import orespawn.registry.ModBlocks;
import orespawn.registry.ModItems;

/**
 * OreSpawn's oddities: the Leonopteryx, Baby Dragon and Dragon, Stinky, Mothra, the Ruby Bird, Rotator and Vortex,
 * the Criminal, T-Shirt and Coin, the Easter Bunny, Rubber Ducky, and the Spit and Jumpy Bugs; their egg ores and the
 * three things only they drop (Moth Scale, Vortex Eye, Jumpy Bug Scale). Sizes, tracking ranges and spawn tables are
 * OreSpawn's (OreSpawnMain); tracking ranges are 8 chunks, 10 for the two OreSpawn tracked twice as far (Dragon,
 * Mothra).
 */
public final class Oddities {
	public static final EntityType<Leon> LEONOPTERYX = ModEntities.register("leonopteryx", Leon::new, MobCategory.CREATURE, 3.5F, 8.25F, 8);
	public static final EntityType<Spyro> BABY_DRAGON = ModEntities.register("baby_dragon", Spyro::new, MobCategory.CREATURE, 0.5F, 0.5F, 8);
	public static final EntityType<Dragon> DRAGON = ModEntities.register("dragon", Dragon::new, MobCategory.CREATURE, 1.5F, 1.25F, 10);
	public static final EntityType<Stinky> STINKY = ModEntities.register("stinky", Stinky::new, MobCategory.AMBIENT, 0.75F, 0.75F, 8);
	public static final EntityType<Mothra> MOTHRA = ModEntities.register("mothra", Mothra::new, MobCategory.AMBIENT, 5.0F, 2.0F, 10);
	public static final EntityType<RubyBird> RUBY_BIRD = ModEntities.register("ruby_bird", RubyBird::new, MobCategory.AMBIENT, 0.5F, 0.5F, 8);
	public static final EntityType<Rotator> ROTATOR = ModEntities.register("rotator", Rotator::new, MobCategory.MONSTER, 1.0F, 2.0F, 8);
	public static final EntityType<Vortex> VORTEX = ModEntities.register("vortex", Vortex::new, MobCategory.MONSTER, 2.0F, 4.0F, 8);
	public static final EntityType<Criminal> CRIMINAL = ModEntities.register("criminal", Criminal::new, MobCategory.MONSTER, 0.75F, 1.75F, 8);
	public static final EntityType<Tshirt> T_SHIRT = ModEntities.register("t_shirt", Tshirt::new, MobCategory.CREATURE, 4.0F, 4.0F, 8);
	public static final EntityType<Coin> COIN = ModEntities.register("coin", Coin::new, MobCategory.AMBIENT, 1.5F, 1.5F, 8);
	public static final EntityType<EasterBunny> EASTER_BUNNY = ModEntities.register("easter_bunny", EasterBunny::new, MobCategory.AMBIENT, 0.5F,
		0.75F, 8);
	public static final EntityType<RubberDucky> RUBBER_DUCKY = ModEntities.register("rubber_ducky", RubberDucky::new, MobCategory.WATER_CREATURE,
		0.33F, 0.5F, 8);
	public static final EntityType<SpitBug> SPIT_BUG = ModEntities.register("spit_bug", SpitBug::new, MobCategory.MONSTER, 2.0F, 2.0F, 8);
	public static final EntityType<TrooperBug> JUMPY_BUG = ModEntities.register("jumpy_bug", TrooperBug::new, MobCategory.MONSTER, 3.0F, 3.5F, 8);

	// Dropped only by these creatures (ItemSalt: plain items).
	public static final Item MOTH_SCALE = ModItems.item("moth_scale", new Item.Properties());
	public static final Item VORTEX_EYE = ModItems.item("vortex_eye", new Item.Properties());
	public static final Item JUMPY_BUG_SCALE = ModItems.item("jumpy_bug_scale", new Item.Properties());

	private Oddities() {
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(LEONOPTERYX, Leon.createAttributes());
		FabricDefaultAttributeRegistry.register(BABY_DRAGON, Spyro.createAttributes());
		FabricDefaultAttributeRegistry.register(DRAGON, Dragon.createAttributes());
		FabricDefaultAttributeRegistry.register(STINKY, Stinky.createAttributes());
		FabricDefaultAttributeRegistry.register(MOTHRA, Mothra.createAttributes());
		FabricDefaultAttributeRegistry.register(RUBY_BIRD, RubyBird.createAttributes());
		FabricDefaultAttributeRegistry.register(ROTATOR, Rotator.createAttributes());
		FabricDefaultAttributeRegistry.register(VORTEX, Vortex.createAttributes());
		FabricDefaultAttributeRegistry.register(CRIMINAL, Criminal.createAttributes());
		FabricDefaultAttributeRegistry.register(T_SHIRT, Tshirt.createAttributes());
		FabricDefaultAttributeRegistry.register(COIN, Coin.createAttributes());
		FabricDefaultAttributeRegistry.register(EASTER_BUNNY, EasterBunny.createAttributes());
		FabricDefaultAttributeRegistry.register(RUBBER_DUCKY, RubberDucky.createAttributes());
		FabricDefaultAttributeRegistry.register(SPIT_BUG, SpitBug.createAttributes());
		FabricDefaultAttributeRegistry.register(JUMPY_BUG, TrooperBug.createAttributes());

		var ground = SpawnPlacementTypes.ON_GROUND;
		var air = SpawnPlacementTypes.NO_RESTRICTIONS;
		var height = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
		SpawnPlacements.register(LEONOPTERYX, ground, height, Leon::checkSpawn);
		SpawnPlacements.register(BABY_DRAGON, ground, height, Spyro::checkSpawn);
		SpawnPlacements.register(DRAGON, ground, height, Dragon::checkSpawn);
		SpawnPlacements.register(STINKY, ground, height, Stinky::checkSpawn);
		SpawnPlacements.register(MOTHRA, air, height, Mothra::checkMothraSpawn);
		SpawnPlacements.register(ROTATOR, ground, height, Rotator::checkSpawn);
		SpawnPlacements.register(VORTEX, ground, height, Vortex::checkSpawn);
		SpawnPlacements.register(CRIMINAL, ground, height, Criminal::checkSpawn);
		SpawnPlacements.register(T_SHIRT, ground, height, Tshirt::checkSpawn);
		SpawnPlacements.register(COIN, ground, height, Coin::checkSpawn);
		SpawnPlacements.register(EASTER_BUNNY, ground, height, EasterBunny::checkSpawn);
		SpawnPlacements.register(RUBBER_DUCKY, SpawnPlacementTypes.IN_WATER, height, RubberDucky::checkSpawn);
		SpawnPlacements.register(SPIT_BUG, ground, height, SpitBug::checkSpawn);
		SpawnPlacements.register(JUMPY_BUG, ground, height, TrooperBug::checkSpawn);

		// OreSpawn's overworld spawn tables ("biome:weight:min:max"). The Leonopteryx, Baby Dragon, Dragon, Rotator, Vortex
		// and T-Shirt (and more of these) spawn only in OreSpawn's own dimensions; the Ruby Bird only from its dungeons.
		ModEntities.spawns(STINKY, "nether:2:1:1 mesa:1:1:1 mesaplat:1:1:1 mesaF:1:1:1");
		ModEntities.spawns(MOTHRA, "xhills:2:1:1 xhillsplus:2:1:1");
		ModEntities.spawns(CRIMINAL, "plains:20:1:2 desert:20:1:2 savanna:20:1:2");
		ModEntities.spawns(COIN, "taiga:2:1:1 forest:2:1:1 jungle:2:1:1 birch:2:1:1 coldtaiga:2:1:1 megataiga:2:1:1");
		ModEntities.spawns(EASTER_BUNNY, "plains:10:1:2 forest:10:1:2 foresthills:10:1:2 birch:5:1:2 birchhills:5:1:2 megataiga:5:1:2 taiga:8:1:2");
		ModEntities.spawns(RUBBER_DUCKY, "river:10:10:20 stonebeach:4:4:6");
		ModEntities.spawns(SPIT_BUG, "swamp:6:1:2");
		ModEntities.spawns(JUMPY_BUG, "swamp:3:1:2 mesa:1:1:1");

		// Ancient Dried eggs (OreGenericEgg): OreSpawn has none for the Ruby Bird or the Coin.
		eggOre("leonopteryx", "oreleon");
		eggOre("baby_dragon", "orespyro");
		eggOre("dragon", "oredragon");
		eggOre("stinky", "orestinky");
		eggOre("mothra", "oreMOTHRA");
		eggOre("rotator", "orerotator");
		eggOre("vortex", "orevortex");
		eggOre("criminal", "orecriminal");
		eggOre("t_shirt", "oretshirt");
		eggOre("easter_bunny", "oreeasterbunny");
		eggOre("rubber_ducky", "orerubberducky");
		eggOre("spit_bug", "orespit");
		eggOre("jumpy_bug", "oretrooper");
	}

	private static void eggOre(String mob, String texture) {
		ModBlocks.eggOre(mob, texture, OreSpawn.id(mob + "_spawn_egg"));
	}
}
