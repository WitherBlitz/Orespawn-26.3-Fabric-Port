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
import orespawn.entity.dinosaurs.Alosaurus;
import orespawn.entity.dinosaurs.Baryonyx;
import orespawn.entity.dinosaurs.Camarasaurus;
import orespawn.entity.dinosaurs.Cryolophosaurus;
import orespawn.entity.dinosaurs.Nastysaurus;
import orespawn.entity.dinosaurs.Pointysaurus;
import orespawn.entity.dinosaurs.TRex;
import orespawn.entity.dinosaurs.VelocityRaptor;
import orespawn.registry.ModBlocks;
import orespawn.registry.ModItems;

/**
 * OreSpawn's dinosaurs: the night hunters (Alosaurus, T. Rex, Nastysaurus, Pointysaurus, Cryolophosaurus), the grazing
 * Baryonyx and the two pets (Camarasaurus, Velocity Raptor), with OreSpawn's sizes, their egg ores and the T. Rex tooth.
 * Categories are the spawn lists OreSpawn put them in (monster, or ambient for the three plant-eaters).
 * <p>
 * No natural spawns yet: OreSpawn spawned them only in its own dimensions (the Utopia biome's lists and the Extreme
 * dimension's chunk provider), not through EntityRegistry.addSpawn, so they arrive with those dimensions.
 */
public final class Dinosaurs {
	public static final EntityType<Alosaurus> ALOSAURUS = ModEntities.register("alosaurus", Alosaurus::new, MobCategory.MONSTER, 1.9F, 3.6F, 8);
	public static final EntityType<TRex> T_REX = ModEntities.register("t_rex", TRex::new, MobCategory.MONSTER, 2.0F, 4.2F, 8);
	public static final EntityType<Nastysaurus> NASTYSAURUS = ModEntities.register("nastysaurus", Nastysaurus::new, MobCategory.MONSTER, 2.2F,
		4.6F, 10);
	public static final EntityType<Pointysaurus> POINTYSAURUS = ModEntities.register("pointysaurus", Pointysaurus::new, MobCategory.MONSTER,
		2.9F, 2.9F, 8);
	public static final EntityType<Cryolophosaurus> CRYOLOPHOSAURUS = ModEntities.register("cryolophosaurus", Cryolophosaurus::new,
		MobCategory.MONSTER, 0.75F, 0.75F, 8);
	public static final EntityType<Baryonyx> BARYONYX = ModEntities.register("baryonyx", Baryonyx::new, MobCategory.AMBIENT, 1.5F, 2.8F, 8);
	public static final EntityType<Camarasaurus> CAMARASAURUS = ModEntities.register("camarasaurus", Camarasaurus::new, MobCategory.AMBIENT,
		0.5F, 1.2F, 8);
	public static final EntityType<VelocityRaptor> VELOCITY_RAPTOR = ModEntities.register("velocity_raptor", VelocityRaptor::new,
		MobCategory.AMBIENT, 0.5F, 0.6F, 8);

	/** TRexTooth (an ItemSalt: a plain item), dropped by the T. Rex. */
	public static final Item TREX_TOOTH = ModItems.item("trex_tooth", new Item.Properties());

	public static final Block ALOSAURUS_EGG_ORE = ModBlocks.eggOre("alosaurus", "orealosaurus", OreSpawn.id("alosaurus_spawn_egg"));
	public static final Block T_REX_EGG_ORE = ModBlocks.eggOre("t_rex", "oretrex", OreSpawn.id("t_rex_spawn_egg"));
	public static final Block NASTYSAURUS_EGG_ORE = ModBlocks.eggOre("nastysaurus", "orenastysaurus", OreSpawn.id("nastysaurus_spawn_egg"));
	public static final Block POINTYSAURUS_EGG_ORE = ModBlocks.eggOre("pointysaurus", "orepointysaurus", OreSpawn.id("pointysaurus_spawn_egg"));
	public static final Block CRYOLOPHOSAURUS_EGG_ORE = ModBlocks.eggOre("cryolophosaurus", "orecryolophosaurus",
		OreSpawn.id("cryolophosaurus_spawn_egg"));
	public static final Block BARYONYX_EGG_ORE = ModBlocks.eggOre("baryonyx", "orebaryonyx", OreSpawn.id("baryonyx_spawn_egg"));
	public static final Block CAMARASAURUS_EGG_ORE = ModBlocks.eggOre("camarasaurus", "orecamarasaurus", OreSpawn.id("camarasaurus_spawn_egg"));
	public static final Block VELOCITY_RAPTOR_EGG_ORE = ModBlocks.eggOre("velocity_raptor", "orevelocityraptor",
		OreSpawn.id("velocity_raptor_spawn_egg"));

	private Dinosaurs() {
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(ALOSAURUS, Alosaurus.createAttributes());
		FabricDefaultAttributeRegistry.register(T_REX, TRex.createAttributes());
		FabricDefaultAttributeRegistry.register(NASTYSAURUS, Nastysaurus.createAttributes());
		FabricDefaultAttributeRegistry.register(POINTYSAURUS, Pointysaurus.createAttributes());
		FabricDefaultAttributeRegistry.register(CRYOLOPHOSAURUS, Cryolophosaurus.createAttributes());
		FabricDefaultAttributeRegistry.register(BARYONYX, Baryonyx.createAttributes());
		FabricDefaultAttributeRegistry.register(CAMARASAURUS, Camarasaurus.createAttributes());
		FabricDefaultAttributeRegistry.register(VELOCITY_RAPTOR, VelocityRaptor.createAttributes());

		var ground = SpawnPlacementTypes.ON_GROUND;
		var height = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
		SpawnPlacements.register(ALOSAURUS, ground, height, Alosaurus::checkSpawn);
		SpawnPlacements.register(T_REX, ground, height, TRex::checkSpawn);
		SpawnPlacements.register(NASTYSAURUS, ground, height, Nastysaurus::checkSpawn);
		SpawnPlacements.register(POINTYSAURUS, ground, height, Pointysaurus::checkSpawn);
		SpawnPlacements.register(CRYOLOPHOSAURUS, ground, height, Cryolophosaurus::checkSpawn);
		SpawnPlacements.register(BARYONYX, ground, height, Baryonyx::checkSpawn);
		SpawnPlacements.register(CAMARASAURUS, ground, height, Camarasaurus::checkSpawn);
		SpawnPlacements.register(VELOCITY_RAPTOR, ground, height, VelocityRaptor::checkSpawn);
	}
}
