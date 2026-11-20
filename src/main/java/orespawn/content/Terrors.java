package orespawn.content;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import orespawn.OreSpawn;
import orespawn.entity.ModEntities;
import orespawn.entity.terrors.Alien;
import orespawn.entity.terrors.Basilisk;
import orespawn.entity.terrors.Cephadrome;
import orespawn.entity.terrors.CliffRacer;
import orespawn.entity.terrors.CreepingHorror;
import orespawn.entity.terrors.GammaMetroid;
import orespawn.entity.terrors.Kyuubi;
import orespawn.entity.terrors.LurkingTerror;
import orespawn.entity.terrors.PitchBlack;
import orespawn.entity.terrors.TerribleTerror;
import orespawn.registry.ModBlocks;
import orespawn.registry.ModItems;

/**
 * OreSpawn's terrors and other night things: the Creeping Horror, Terrible Terror, Lurking Terror and Nightmare of its
 * Islands and Chaos dimensions, the Nether's Kyuubi, the WTF? and Alien of its mining dimension, the Basilisk, the
 * rideable Cephadrome and the Cliff Racer, with OreSpawn's sizes, their egg ores, and the Nightmare and Basilisk scales
 * they drop. Categories are the spawn lists OreSpawn put them in (monster, or ambient for the Cephadrome and the Cliff
 * Racer); the Basilisk, which OreSpawn listed as ambient, is a monster here, as the Rat is.
 * <p>
 * Natural spawns are OreSpawn's EntityRegistry.addSpawn tables: the Kyuubi in the Nether, the Basilisk in jungles, birch
 * forest hills and dark forests, the Cephadrome in snowy plains and snowy taiga. The others spawned only in OreSpawn's
 * own dimensions, so they arrive with those.
 */
public final class Terrors {
	public static final EntityType<CreepingHorror> CREEPING_HORROR = ModEntities.register("creeping_horror", CreepingHorror::new,
		MobCategory.MONSTER, 0.75F, 0.5F, 8);
	public static final EntityType<TerribleTerror> TERRIBLE_TERROR = ModEntities.register("terrible_terror", TerribleTerror::new,
		MobCategory.MONSTER, 1.0F, 0.75F, 8);
	public static final EntityType<LurkingTerror> LURKING_TERROR = ModEntities.register("lurking_terror", LurkingTerror::new,
		MobCategory.MONSTER, 1.75F, 1.25F, 8);
	/** OreSpawn's PitchBlack; 2.5 x 3.5 at full size, scaled by each one's size. */
	public static final EntityType<PitchBlack> NIGHTMARE = ModEntities.register("nightmare", PitchBlack::new, MobCategory.MONSTER, 2.5F, 3.5F,
		10);
	public static final EntityType<Kyuubi> KYUUBI = ModEntities.register("kyuubi", Kyuubi::new, MobCategory.MONSTER, 0.5F, 1.25F, 8);
	/** OreSpawn's GammaMetroid, named "WTF?". */
	public static final EntityType<GammaMetroid> WTF = ModEntities.register("wtf", GammaMetroid::new, MobCategory.MONSTER, 1.5F, 1.5F, 8);
	public static final EntityType<Alien> ALIEN = ModEntities.register("alien", Alien::new, MobCategory.MONSTER, 1.1F, 3.25F, 8);
	public static final EntityType<Basilisk> BASILISK = ModEntities.register("basilisk", Basilisk::new, MobCategory.MONSTER, 1.6F, 3.5F, 10);
	public static final EntityType<Cephadrome> CEPHADROME = ModEntities.register("cephadrome", Cephadrome::new, MobCategory.AMBIENT, 2.5F, 2.25F,
		10);
	public static final EntityType<CliffRacer> CLIFF_RACER = ModEntities.register("cliff_racer", CliffRacer::new, MobCategory.AMBIENT, 0.75F,
		0.5F, 8);

	/** ItemSalt (a plain item): dropped by the Nightmare; the Nightmare Sword and Bertha's guard are made with it. */
	public static final Item NIGHTMARE_SCALE = ModItems.item("nightmare_scale", new Item.Properties());
	/** ItemSalt (a plain item): dropped by the Basilisk; Bertha's guard is made with it. */
	public static final Item BASILISK_SCALE = ModItems.item("basilisk_scale", new Item.Properties());

	public static final Block CREEPING_HORROR_EGG_ORE = ModBlocks.eggOre("creeping_horror", "orecreepinghorror",
		OreSpawn.id("creeping_horror_spawn_egg"));
	public static final Block TERRIBLE_TERROR_EGG_ORE = ModBlocks.eggOre("terrible_terror", "oreterribleterror",
		OreSpawn.id("terrible_terror_spawn_egg"));
	public static final Block LURKING_TERROR_EGG_ORE = ModBlocks.eggOre("lurking_terror", "orelurkingterror",
		OreSpawn.id("lurking_terror_spawn_egg"));
	public static final Block NIGHTMARE_EGG_ORE = ModBlocks.eggOre("nightmare", "orenightmare", OreSpawn.id("nightmare_spawn_egg"));
	public static final Block KYUUBI_EGG_ORE = ModBlocks.eggOre("kyuubi", "orekyuubi", OreSpawn.id("kyuubi_spawn_egg"));
	public static final Block WTF_EGG_ORE = ModBlocks.eggOre("wtf", "oregammametroid", OreSpawn.id("wtf_spawn_egg"));
	public static final Block ALIEN_EGG_ORE = ModBlocks.eggOre("alien", "orealien", OreSpawn.id("alien_spawn_egg"));
	public static final Block BASILISK_EGG_ORE = ModBlocks.eggOre("basilisk", "orebasilisc", OreSpawn.id("basilisk_spawn_egg"));
	public static final Block CEPHADROME_EGG_ORE = ModBlocks.eggOre("cephadrome", "orecephadrome", OreSpawn.id("cephadrome_spawn_egg"));
	public static final Block CLIFF_RACER_EGG_ORE = ModBlocks.eggOre("cliff_racer", "orecliffracer", OreSpawn.id("cliff_racer_spawn_egg"));

	private Terrors() {
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(CREEPING_HORROR, CreepingHorror.createAttributes());
		FabricDefaultAttributeRegistry.register(TERRIBLE_TERROR, TerribleTerror.createAttributes());
		FabricDefaultAttributeRegistry.register(LURKING_TERROR, LurkingTerror.createAttributes());
		FabricDefaultAttributeRegistry.register(NIGHTMARE, PitchBlack.createAttributes());
		FabricDefaultAttributeRegistry.register(KYUUBI, Kyuubi.createAttributes());
		FabricDefaultAttributeRegistry.register(WTF, GammaMetroid.createAttributes());
		FabricDefaultAttributeRegistry.register(ALIEN, Alien.createAttributes());
		FabricDefaultAttributeRegistry.register(BASILISK, Basilisk.createAttributes());
		FabricDefaultAttributeRegistry.register(CEPHADROME, Cephadrome.createAttributes());
		FabricDefaultAttributeRegistry.register(CLIFF_RACER, CliffRacer.createAttributes());

		var ground = SpawnPlacementTypes.ON_GROUND;
		var height = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
		SpawnPlacements.register(CREEPING_HORROR, ground, height, CreepingHorror::checkSpawn);
		SpawnPlacements.register(TERRIBLE_TERROR, ground, height, TerribleTerror::checkSpawn);
		SpawnPlacements.register(LURKING_TERROR, ground, height, LurkingTerror::checkSpawn);
		SpawnPlacements.register(NIGHTMARE, ground, height, PitchBlack::checkSpawn);
		SpawnPlacements.register(KYUUBI, ground, height, Kyuubi::checkSpawn);
		SpawnPlacements.register(WTF, ground, height, GammaMetroid::checkSpawn);
		SpawnPlacements.register(ALIEN, ground, height, Alien::checkSpawn);
		SpawnPlacements.register(BASILISK, ground, height, Basilisk::checkSpawn);
		SpawnPlacements.register(CEPHADROME, ground, height, Cephadrome::checkSpawn);
		SpawnPlacements.register(CLIFF_RACER, ground, height, CliffRacer::checkSpawn);

		// OreSpawn's EntityRegistry.addSpawn tables, "biome:weight:min:max".
		ModEntities.spawns(KYUUBI, "nether:10:1:1");
		ModEntities.spawns(BASILISK, "jungle:3:1:1 junglehills:2:1:1 birchhills:4:1:2 roofed:15:1:2");
		ModEntities.spawns(CEPHADROME, "iceplains:1:1:1 coldtaiga:1:1:1");
	}
}
