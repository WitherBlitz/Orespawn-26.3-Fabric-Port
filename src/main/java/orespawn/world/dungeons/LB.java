package orespawn.world.dungeons;

import java.util.HashMap;
import java.util.Map;

/**
 * A 1.7.10 block as OreSpawn's dungeon code names it: a vanilla block by its 1.7.10 numeric id (its metadata picks the
 * 26.3 state, see {@link LegacyStates}), or one of OreSpawn's own blocks by its id here (looked up when placed; blocks
 * of groups that aren't ported yet are skipped). Written by tools/dungeons/port_dungeons.py.
 */
public final class LB {
	private static final Map<Integer, LB> BY_ID = new HashMap<>();
	private static final Map<String, LB> BY_MOD_ID = new HashMap<>();

	/** The 1.7.10 block id, or -1 for one of OreSpawn's blocks. */
	public final int id;
	/** The 1.7.10 name (vanilla) or the orespawn: id path (OreSpawn's). */
	public final String name;

	/** What {@link DWorld#getBlock} returns for a 26.3 block none of the constants below stands for. */
	public static final LB OTHER = new LB(-2, "other");
	public static final LB AIR = vanilla(0, "air");
	public static final LB STONE = vanilla(1, "stone");
	public static final LB GRASS = vanilla(2, "grass");
	public static final LB DIRT = vanilla(3, "dirt");
	public static final LB COBBLESTONE = vanilla(4, "cobblestone");
	public static final LB PLANKS = vanilla(5, "planks");
	public static final LB BEDROCK = vanilla(7, "bedrock");
	public static final LB FLOWING_WATER = vanilla(8, "flowing_water");
	public static final LB WATER = vanilla(9, "water");
	public static final LB LAVA = vanilla(11, "lava");
	public static final LB SAND = vanilla(12, "sand");
	public static final LB GRAVEL = vanilla(13, "gravel");
	public static final LB GOLD_ORE = vanilla(14, "gold_ore");
	public static final LB IRON_ORE = vanilla(15, "iron_ore");
	public static final LB COAL_ORE = vanilla(16, "coal_ore");
	public static final LB LOG = vanilla(17, "log");
	public static final LB LEAVES = vanilla(18, "leaves");
	public static final LB SPONGE = vanilla(19, "sponge");
	public static final LB GLASS = vanilla(20, "glass");
	public static final LB LAPIS_ORE = vanilla(21, "lapis_ore");
	public static final LB LAPIS_BLOCK = vanilla(22, "lapis_block");
	public static final LB SANDSTONE = vanilla(24, "sandstone");
	public static final LB GOLDEN_RAIL = vanilla(27, "golden_rail");
	public static final LB STICKY_PISTON = vanilla(29, "sticky_piston");
	public static final LB WEB = vanilla(30, "web");
	public static final LB TALLGRASS = vanilla(31, "tallgrass");
	public static final LB DEADBUSH = vanilla(32, "deadbush");
	public static final LB PISTON_EXTENSION = vanilla(36, "piston_extension");
	public static final LB YELLOW_FLOWER = vanilla(37, "yellow_flower");
	public static final LB RED_FLOWER = vanilla(38, "red_flower");
	public static final LB BROWN_MUSHROOM = vanilla(39, "brown_mushroom");
	public static final LB RED_MUSHROOM = vanilla(40, "red_mushroom");
	public static final LB GOLD_BLOCK = vanilla(41, "gold_block");
	public static final LB IRON_BLOCK = vanilla(42, "iron_block");
	public static final LB STONE_SLAB = vanilla(44, "stone_slab");
	public static final LB BOOKSHELF = vanilla(47, "bookshelf");
	public static final LB MOSSY_COBBLESTONE = vanilla(48, "mossy_cobblestone");
	public static final LB OBSIDIAN = vanilla(49, "obsidian");
	public static final LB TORCH = vanilla(50, "torch");
	public static final LB FIRE = vanilla(51, "fire");
	public static final LB MOB_SPAWNER = vanilla(52, "mob_spawner");
	public static final LB CHEST = vanilla(54, "chest");
	public static final LB DIAMOND_BLOCK = vanilla(57, "diamond_block");
	public static final LB CRAFTING_TABLE = vanilla(58, "crafting_table");
	public static final LB WHEAT = vanilla(59, "wheat");
	public static final LB FARMLAND = vanilla(60, "farmland");
	public static final LB FURNACE = vanilla(61, "furnace");
	public static final LB WOODEN_DOOR = vanilla(64, "wooden_door");
	public static final LB LADDER = vanilla(65, "ladder");
	public static final LB RAIL = vanilla(66, "rail");
	public static final LB LEVER = vanilla(69, "lever");
	public static final LB IRON_DOOR = vanilla(71, "iron_door");
	public static final LB REDSTONE_TORCH = vanilla(76, "redstone_torch");
	public static final LB STONE_BUTTON = vanilla(77, "stone_button");
	public static final LB ICE = vanilla(79, "ice");
	public static final LB SNOW = vanilla(80, "snow");
	public static final LB REEDS = vanilla(83, "reeds");
	public static final LB FENCE = vanilla(85, "fence");
	public static final LB NETHERRACK = vanilla(87, "netherrack");
	public static final LB SOUL_SAND = vanilla(88, "soul_sand");
	public static final LB GLOWSTONE = vanilla(89, "glowstone");
	public static final LB TRAPDOOR = vanilla(96, "trapdoor");
	public static final LB STONEBRICK = vanilla(98, "stonebrick");
	public static final LB IRON_BARS = vanilla(101, "iron_bars");
	public static final LB GLASS_PANE = vanilla(102, "glass_pane");
	public static final LB WATERLILY = vanilla(111, "waterlily");
	public static final LB NETHER_BRICK = vanilla(112, "nether_brick");
	public static final LB NETHER_BRICK_FENCE = vanilla(113, "nether_brick_fence");
	public static final LB END_STONE = vanilla(121, "end_stone");
	public static final LB DRAGON_EGG = vanilla(122, "dragon_egg");
	public static final LB LIT_REDSTONE_LAMP = vanilla(124, "lit_redstone_lamp");
	public static final LB EMERALD_ORE = vanilla(129, "emerald_ore");
	public static final LB ENDER_CHEST = vanilla(130, "ender_chest");
	public static final LB EMERALD_BLOCK = vanilla(133, "emerald_block");
	public static final LB CARROTS = vanilla(141, "carrots");
	public static final LB POTATOES = vanilla(142, "potatoes");
	public static final LB REDSTONE_BLOCK = vanilla(152, "redstone_block");
	public static final LB QUARTZ_BLOCK = vanilla(155, "quartz_block");
	public static final LB QUARTZ_STAIRS = vanilla(156, "quartz_stairs");
	public static final LB STAINED_HARDENED_CLAY = vanilla(159, "stained_hardened_clay");
	public static final LB CARPET = vanilla(171, "carpet");
	public static final LB AMETHYST_BLOCK = mod("amethyst_block");
	public static final LB APPLE_LEAVES = mod("apple_leaves");
	public static final LB BLUE_FLOWER = mod("blue_flower");
	public static final LB BUTTERFLY_PLANT = mod("butterfly_plant");
	public static final LB CORN_PLANT = mod("corn_plant");
	public static final LB CREEPER_REPELLENT = mod("creeper_repellent");
	public static final LB CRYSTAL_ENERGY = mod("crystal_energy");
	public static final LB CRYSTAL_FURNACE = mod("crystal_furnace");
	public static final LB CRYSTAL_GRASS = mod("crystal_grass");
	public static final LB CRYSTAL_PLANKS = mod("crystal_planks");
	public static final LB CRYSTAL_TORCH = mod("crystal_torch");
	public static final LB CRYSTAL_WORKBENCH = mod("crystal_workbench");
	public static final LB ENDERMAN_EGG_ORE = mod("enderman_egg_ore");
	public static final LB ENDER_DRAGON_EGG_ORE = mod("ender_dragon_egg_ore");
	public static final LB ENDER_KNIGHT_EGG_ORE = mod("ender_knight_egg_ore");
	public static final LB ENDER_PEARL_BLOCK = mod("ender_pearl_block");
	public static final LB ENDER_REAPER_EGG_ORE = mod("ender_reaper_egg_ore");
	public static final LB EXTREME_TORCH = mod("extreme_torch");
	public static final LB EYE_OF_ENDER_BLOCK = mod("eye_of_ender_block");
	public static final LB KYANITE = mod("kyanite");
	public static final LB LAVA_FOAM = mod("lava_foam");
	public static final LB LETTUCE_PLANT = mod("lettuce_plant");
	public static final LB MOTH_PLANT = mod("moth_plant");
	public static final LB PINK_FLOWER = mod("pink_flower");
	public static final LB PINK_TOURMALINE = mod("pink_tourmaline");
	public static final LB PINK_TOURMALINE_BLOCK = mod("pink_tourmaline_block");
	public static final LB QUINOA_PLANT = mod("quinoa_plant");
	public static final LB RADISH_PLANT = mod("radish_plant");
	public static final LB RANDOM_TELEPORT_BLOCK = mod("random_teleport_block");
	public static final LB RED_ANT_NEST = mod("red_ant_nest");
	public static final LB RICE_PLANT = mod("rice_plant");
	public static final LB RUBY_BLOCK = mod("ruby_block");
	public static final LB RUBY_ORE = mod("ruby_ore");
	public static final LB SCARY_LEAVES = mod("scary_leaves");
	public static final LB STRAWBERRY_PLANT = mod("strawberry_plant");
	public static final LB TIGERS_EYE = mod("tigers_eye");
	public static final LB TOMATO_PLANT = mod("tomato_plant");
	public static final LB WATER_DRAGON_EGG_ORE = mod("water_dragon_egg_ore");

	private LB(int id, String name) {
		this.id = id;
		this.name = name;
	}

	private static LB vanilla(int id, String name) {
		LB lb = new LB(id, name);
		BY_ID.put(id, lb);
		return lb;
	}

	private static LB mod(String id) {
		LB lb = new LB(-1, id);
		BY_MOD_ID.put(id, lb);
		return lb;
	}

	/** A vanilla block (its 26.3 state comes from {@link LegacyStates}), not one of OreSpawn's. */
	public boolean isVanilla() {
		return this.id >= 0;
	}

	/** One of OreSpawn's blocks, placed by its id. */
	public boolean isModBlock() {
		return this.id == -1;
	}

	/** The constant for a 1.7.10 block id, or null if the dungeon code never uses it. */
	public static LB byId(int id) {
		return BY_ID.get(id);
	}

	/** The constant for one of OreSpawn's blocks, or null if the dungeon code never uses it. */
	public static LB byModId(String id) {
		return BY_MOD_ID.get(id);
	}

	public static Iterable<LB> vanillaBlocks() {
		return BY_ID.values();
	}

	public static Iterable<LB> modBlocks() {
		return BY_MOD_ID.values();
	}

	@Override
	public String toString() {
		return this.name;
	}
}
