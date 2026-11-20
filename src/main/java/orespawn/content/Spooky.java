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
import orespawn.entity.spooky.DungeonBeast;
import orespawn.entity.spooky.EnderKnight;
import orespawn.entity.spooky.EnderReaper;
import orespawn.entity.spooky.Fairy;
import orespawn.entity.spooky.Ghost;
import orespawn.entity.spooky.GhostSkelly;
import orespawn.entity.spooky.LeafMonster;
import orespawn.entity.spooky.Triffid;
import orespawn.registry.ModBlocks;
import orespawn.registry.ModItems;

import java.time.LocalDate;
import java.time.Month;

/**
 * The Spooky group: OreSpawn's Ghost and Ghost Pumpkin Skelly, Ender Knight and Ender Reaper, Fairy, Dungeon Beast,
 * Leaf Monster and Triffid, with their spawn eggs, egg ores (all but the ghosts had one), spawn rules and natural spawns
 * (OreSpawnMain's EntityRegistry.addSpawn), and Green Goo, which only the Triffid drops.
 * <p>
 * Sizes, tracking and categories: OreSpawn's setSize for each; its mobs all spawned in the "ambient" pool, and here the
 * monsters spawn as monsters and the rest as ambient creatures, as the earlier batches do.
 * <p>
 * Not ported yet: OreSpawn's other spawns for these mobs, in its Utopia dimension (Leaf Monster, Ender Reaper, Fairy,
 * Dungeon Beast), its Crystal dimension and Islands, and the spawners in its Fairy trees, Ender Knight dungeon, Ender Reaper
 * graveyard and Leaf Monster dungeon; those come with the dimensions and structures. Caged versions (CritterCage) come
 * with the cages.
 */
public final class Spooky {
	public static final EntityType<Ghost> GHOST = ModEntities.register("ghost", Ghost::new, MobCategory.AMBIENT, 0.5F, 1.5F, 8);
	public static final EntityType<GhostSkelly> GHOST_PUMPKIN_SKELLY = ModEntities.register("ghost_pumpkin_skelly", GhostSkelly::new,
		MobCategory.AMBIENT, 1.5F, 2.0F, 8);
	public static final EntityType<EnderKnight> ENDER_KNIGHT = ModEntities.register("ender_knight", EnderKnight::new, MobCategory.MONSTER, 0.6F,
		2.9F, 8);
	public static final EntityType<EnderReaper> ENDER_REAPER = ModEntities.register("ender_reaper", EnderReaper::new, MobCategory.MONSTER, 0.7F,
		2.9F, 8);
	public static final EntityType<Fairy> FAIRY = ModEntities.register("fairy", Fairy::new, MobCategory.AMBIENT, 0.4F, 0.8F, 8);
	public static final EntityType<DungeonBeast> DUNGEON_BEAST = ModEntities.register("dungeon_beast", DungeonBeast::new, MobCategory.MONSTER,
		1.15F, 1.1F, 8);
	public static final EntityType<LeafMonster> LEAF_MONSTER = ModEntities.register("leaf_monster", LeafMonster::new, MobCategory.MONSTER, 1.0F,
		2.5F, 8);
	public static final EntityType<Triffid> TRIFFID = ModEntities.register("triffid", Triffid::new, MobCategory.MONSTER, 2.0F, 4.0F, 8);

	/** Green Goo (OreSpawn's ItemSalt, a plain item): the Triffid's drop. */
	public static final Item GREEN_GOO = ModItems.item("green_goo", new Item.Properties());

	// Ancient Dried ... Spawn Eggs (OreGenericEgg): crafted with a water bucket into the spawn egg.
	public static final Block ENDER_KNIGHT_EGG_ORE = eggOre("ender_knight", "oreenderknight");
	public static final Block ENDER_REAPER_EGG_ORE = eggOre("ender_reaper", "oreenderreaper");
	public static final Block FAIRY_EGG_ORE = eggOre("fairy", "orefairy");
	public static final Block DUNGEON_BEAST_EGG_ORE = eggOre("dungeon_beast", "oredungeonbeast");
	public static final Block LEAF_MONSTER_EGG_ORE = eggOre("leaf_monster", "oreleafmonster");
	public static final Block TRIFFID_EGG_ORE = eggOre("triffid", "oretriffid");

	private Spooky() {
	}

	private static Block eggOre(String mob, String texture) {
		return ModBlocks.eggOre(mob, texture, OreSpawn.id(mob + "_spawn_egg"));
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(GHOST, Ghost.createAttributes());
		FabricDefaultAttributeRegistry.register(GHOST_PUMPKIN_SKELLY, GhostSkelly.createAttributes());
		FabricDefaultAttributeRegistry.register(ENDER_KNIGHT, EnderKnight.createAttributes());
		FabricDefaultAttributeRegistry.register(ENDER_REAPER, EnderReaper.createAttributes());
		FabricDefaultAttributeRegistry.register(FAIRY, Fairy.createAttributes());
		FabricDefaultAttributeRegistry.register(DUNGEON_BEAST, DungeonBeast.createAttributes());
		FabricDefaultAttributeRegistry.register(LEAF_MONSTER, LeafMonster.createAttributes());
		FabricDefaultAttributeRegistry.register(TRIFFID, Triffid.createAttributes());

		// 1.7.10 spawned every non-water mob on top of a solid block.
		var ground = SpawnPlacementTypes.ON_GROUND;
		var height = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
		SpawnPlacements.register(GHOST, ground, height, Ghost::checkSpawn);
		SpawnPlacements.register(GHOST_PUMPKIN_SKELLY, ground, height, Ghost::checkSpawn);
		SpawnPlacements.register(ENDER_KNIGHT, ground, height, EnderKnight::checkSpawn);
		SpawnPlacements.register(ENDER_REAPER, ground, height, EnderReaper::checkSpawn);
		SpawnPlacements.register(FAIRY, ground, height, Fairy::checkSpawn);
		SpawnPlacements.register(DUNGEON_BEAST, ground, height, DungeonBeast::checkSpawn);
		SpawnPlacements.register(LEAF_MONSTER, ground, height, LeafMonster::checkSpawn);
		SpawnPlacements.register(TRIFFID, ground, height, Triffid::checkSpawn);

		// OreSpawn's spawn tables, "biome:weight:min:max". The Triffid has none.
		ModEntities.spawns(LEAF_MONSTER, "jungle:5:2:6 forest:5:1:2 junglehills:3:2:4 foresthills:3:1:2 birch:3:3:6 birchhills:2:3:6 megataiga:2:2:5 taiga:2:2:5");
		ModEntities.spawns(ENDER_KNIGHT, "xhills:4:2:4 xhillsedge:4:2:4 forest:4:2:4 foresthills:4:2:4 junglehills:4:2:4 plains:2:2:4 river:2:2:4 desert:2:2:4 roofed:20:2:4");
		ModEntities.spawns(ENDER_REAPER, "xhills:2:1:2 xhillsedge:2:1:2 forest:1:1:2 foresthills:2:1:2 junglehills:2:1:2 plains:1:1:2 river:1:1:2 desert:1:1:2 roofed:38:2:4");
		ModEntities.spawns(FAIRY, "roofed:25:2:4");
		ModEntities.spawns(DUNGEON_BEAST, "roofed:20:2:4");
		// OreSpawn only added the ghosts' spawns when the game was started on October 31.
		LocalDate today = LocalDate.now();
		if (today.getMonth() == Month.OCTOBER && today.getDayOfMonth() == 31) {
			String halloween = "beach:15:3:6 xhills:15:3:6 xhillsedge:15:3:6 forest:15:3:6 foresthills:15:3:6 jungle:15:3:6 junglehills:15:3:6"
				+ " plains:15:3:6 river:15:3:6 desert:15:3:6 birch:15:3:6 birchhills:15:3:6 megataiga:15:3:6 taiga:15:3:6 savanna:15:3:6"
				+ " savannaplat:15:3:6 mesaF:15:3:6 mesaplat:15:3:6 mesa:15:3:6 coldtaigahills:15:3:6 coldtaiga:15:3:6 roofed:15:3:6";
			ModEntities.spawns(GHOST_PUMPKIN_SKELLY, halloween);
			ModEntities.spawns(GHOST, halloween);
		}
	}
}
