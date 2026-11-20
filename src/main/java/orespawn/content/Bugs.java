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
import orespawn.OreSpawn;
import orespawn.block.bugs.MoleDirtBlock;
import orespawn.entity.ModEntities;
import orespawn.entity.bugs.Bee;
import orespawn.entity.bugs.Brutalfly;
import orespawn.entity.bugs.BrutalflyFireball;
import orespawn.entity.bugs.CaterKiller;
import orespawn.entity.bugs.CaveFisher;
import orespawn.entity.bugs.EmperorScorpion;
import orespawn.entity.bugs.HerculesBeetle;
import orespawn.entity.bugs.Mantis;
import orespawn.entity.bugs.Molenoid;
import orespawn.registry.ModBlocks;
import orespawn.registry.ModItems;

/**
 * OreSpawn's giant bugs: the Mantis, Hercules Beetle, Bee, Brutalfly, CaterKiller, Emperor Scorpion, Cave Fisher and
 * Molenoid, with their egg ores, the parts they drop, the Brutalfly's fireball and the Molenoid's dirt.
 * <p>
 * OreSpawn listed all but the Cave Fisher as ambient creatures (EnumCreatureType.ambient) though they're monsters; so
 * they're AMBIENT here, with OreSpawn's weights and group sizes. The Cave Fisher only spawned in OreSpawn's own
 * dimensions (not ported yet), as a monster. Sizes are OreSpawn's (setSize); tracking ranges are in chunks.
 */
public final class Bugs {
	public static final EntityType<Mantis> MANTIS = ModEntities.register("mantis", Mantis::new, MobCategory.AMBIENT, 2.5F, 3.25F, 8);
	public static final EntityType<HerculesBeetle> HERCULES_BEETLE = ModEntities.register("hercules_beetle", HerculesBeetle::new,
		MobCategory.AMBIENT, 3.25F, 2.75F, 8);
	public static final EntityType<Bee> BEE = ModEntities.register("bee", Bee::new, MobCategory.AMBIENT, 1.5F, 2.5F, 8);
	public static final EntityType<Brutalfly> BRUTALFLY = ModEntities.register("brutalfly", Brutalfly::new, MobCategory.AMBIENT, 5.0F, 2.0F, 10);
	public static final EntityType<CaterKiller> CATERKILLER = ModEntities.register("caterkiller", CaterKiller::new, MobCategory.AMBIENT, 2.9F, 4.6F,
		8);
	public static final EntityType<EmperorScorpion> EMPEROR_SCORPION = ModEntities.register("emperor_scorpion", EmperorScorpion::new,
		MobCategory.AMBIENT, 3.5F, 3.0F, 8);
	public static final EntityType<CaveFisher> CAVE_FISHER = ModEntities.register("cave_fisher", CaveFisher::new, MobCategory.MONSTER, 1.35F, 0.75F,
		8);
	public static final EntityType<Molenoid> MOLENOID = ModEntities.register("molenoid", Molenoid::new, MobCategory.AMBIENT, 3.9F, 2.6F, 8);
	/** OreSpawn's BetterFireball as the Brutalfly shoots it (1x1, like vanilla's fireball otherwise). */
	public static final EntityType<BrutalflyFireball> BRUTALFLY_FIREBALL = ModEntities.misc("brutalfly_fireball", BrutalflyFireball::new, 1.0F,
		1.0F, 4, 10);

	// The parts they drop (OreSpawn's ItemSalt: plain items).
	public static final Item CATERKILLER_JAWS = ModItems.item("caterkiller_jaws", new Item.Properties());
	public static final Item EMPEROR_SCORPION_SCALE = ModItems.item("emperor_scorpion_scale", new Item.Properties());
	public static final Item MOLENOID_NOSE = ModItems.item("molenoid_nose", new Item.Properties());

	/** MoleDirtBlock: Material.ground, hardness 0.6, gravel sound, random ticks. */
	public static final Block MOLENOID_DIRT = ModBlocks.block("molenoid_dirt", MoleDirtBlock::new, BlockBehaviour.Properties.of()
		.mapColor(MapColor.DIRT).strength(0.6F).sound(SoundType.GRAVEL).randomTicks());

	// "Ancient Dried ... Spawn Egg" ores.
	public static final Block MANTIS_EGG_ORE = ModBlocks.eggOre("mantis", "oremantis", OreSpawn.id("mantis_spawn_egg"));
	public static final Block HERCULES_BEETLE_EGG_ORE = ModBlocks.eggOre("hercules_beetle", "orehercules", OreSpawn.id("hercules_beetle_spawn_egg"));
	public static final Block BEE_EGG_ORE = ModBlocks.eggOre("bee", "orebee", OreSpawn.id("bee_spawn_egg"));
	public static final Block BRUTALFLY_EGG_ORE = ModBlocks.eggOre("brutalfly", "orebrutalfly", OreSpawn.id("brutalfly_spawn_egg"));
	public static final Block CATERKILLER_EGG_ORE = ModBlocks.eggOre("caterkiller", "orecaterkiller", OreSpawn.id("caterkiller_spawn_egg"));
	public static final Block EMPEROR_SCORPION_EGG_ORE = ModBlocks.eggOre("emperor_scorpion", "oreemperorscorpion",
		OreSpawn.id("emperor_scorpion_spawn_egg"));
	public static final Block CAVE_FISHER_EGG_ORE = ModBlocks.eggOre("cave_fisher", "orecavefisher", OreSpawn.id("cave_fisher_spawn_egg"));
	public static final Block MOLENOID_EGG_ORE = ModBlocks.eggOre("molenoid", "oremolenoid", OreSpawn.id("molenoid_spawn_egg"));

	private Bugs() {
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(MANTIS, Mantis.createAttributes());
		FabricDefaultAttributeRegistry.register(HERCULES_BEETLE, HerculesBeetle.createAttributes());
		FabricDefaultAttributeRegistry.register(BEE, Bee.createAttributes());
		FabricDefaultAttributeRegistry.register(BRUTALFLY, Brutalfly.createAttributes());
		FabricDefaultAttributeRegistry.register(CATERKILLER, CaterKiller.createAttributes());
		FabricDefaultAttributeRegistry.register(EMPEROR_SCORPION, EmperorScorpion.createAttributes());
		FabricDefaultAttributeRegistry.register(CAVE_FISHER, CaveFisher.createAttributes());
		FabricDefaultAttributeRegistry.register(MOLENOID, Molenoid.createAttributes());

		// 1.7.10's spawner wanted solid ground under every creature type but water ones, flyers included.
		var ground = SpawnPlacementTypes.ON_GROUND;
		var height = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
		SpawnPlacements.register(MANTIS, ground, height, Mantis::checkSpawn);
		SpawnPlacements.register(HERCULES_BEETLE, ground, height, HerculesBeetle::checkSpawn);
		SpawnPlacements.register(BEE, ground, height, Bee::checkSpawn);
		SpawnPlacements.register(BRUTALFLY, ground, height, Brutalfly::checkSpawn);
		SpawnPlacements.register(CATERKILLER, ground, height, CaterKiller::checkSpawn);
		SpawnPlacements.register(EMPEROR_SCORPION, ground, height, EmperorScorpion::checkSpawn);
		SpawnPlacements.register(CAVE_FISHER, ground, height, CaveFisher::checkSpawn);
		SpawnPlacements.register(MOLENOID, ground, height, Molenoid::checkSpawn);

		// OreSpawnMain's EntityRegistry.addSpawn tables, "biome:weight:min:max".
		ModEntities.spawns(BEE, "forest:2:1:2 foresthills:2:1:2 jungle:5:3:5 junglehills:5:2:5 birch:3:2:4 birchhills:3:2:4 megataiga:5:1:2"
			+ " taiga:5:1:2 savanna:3:1:1 savannaplat:2:1:1");
		ModEntities.spawns(MANTIS, "forest:1:1:1 foresthills:1:1:1 jungle:5:1:2 plains:1:1:2 swamp:1:1:2 birch:1:2:4 megataiga:1:1:2"
			+ " savanna:1:1:1 savannaplat:1:1:1");
		ModEntities.spawns(HERCULES_BEETLE, "foresthills:2:1:2 junglehills:2:1:2 xhillsedge:5:1:2 taigahills:2:1:2 birchhills:5:1:1"
			+ " coldtaigahills:2:1:1 megataigahills:2:1:2");
		ModEntities.spawns(MOLENOID, "plains:2:1:2 savanna:2:1:1 savannaplat:2:1:1");
		ModEntities.spawns(CATERKILLER, "forest:2:1:1 jungle:2:1:2 foresthills:4:1:2 junglehills:4:1:2 birch:6:1:2 birchhills:2:1:2"
			+ " megataiga:2:1:2 taiga:2:1:2 roofed:10:1:2");
		ModEntities.spawns(BRUTALFLY, "megataigahills:2:1:1 xhillsplus:2:1:1 mesaplat:2:1:1");
		ModEntities.spawns(EMPEROR_SCORPION, "desert:1:1:1 savanna:2:1:1");
	}
}
