package orespawn.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.TrapezoidInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import orespawn.OreSpawn;
import orespawn.block.EggOreBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Blocks, ported from OreSpawn 1.7.10-20.3 ({@code OreSpawnMain}, {@code OreGenericEgg}, {@code OreRuby} ...).
 * <p>
 * Egg ores ("Ancient Dried ... Spawn Egg") are soft, dirt-like blocks that drop themselves; crafting one with a water
 * bucket gives that mob's spawn egg. This milestone has the 29 for vanilla mobs; OreSpawn's own mobs' eggs come with
 * those mobs. Drops, models, recipes and worldgen are data files written by {@code tools/gen_assets.py}, which keeps
 * the same lists as this class.
 */
public final class ModBlocks {
	/** One egg ore: the mob it holds (an entity id), OreSpawn's texture name for it, the spawn egg it crafts into. */
	public record EggOre(String mob, String texture, Identifier spawnEgg, Block block) {}

	public static final Map<String, Block> BLOCKS = new LinkedHashMap<>();
	private static final List<EggOre> EGG_ORE_LIST = new ArrayList<>();
	public static final List<EggOre> EGG_ORES = Collections.unmodifiableList(EGG_ORE_LIST);

	/** Vanilla mobs with an OreSpawn egg ore: modern entity id, then OreSpawn's texture name. */
	public static final List<String[]> VANILLA_EGGS = List.of(
		new String[] {"spider", "orespider"}, new String[] {"bat", "orebat"}, new String[] {"cow", "orecow"},
		new String[] {"pig", "orepig"}, new String[] {"squid", "oresquid"}, new String[] {"chicken", "orechicken"},
		new String[] {"creeper", "orecreeper"}, new String[] {"skeleton", "oreskeleton"}, new String[] {"zombie", "orezombie"},
		new String[] {"slime", "oreslime"}, new String[] {"ghast", "oreghast"}, new String[] {"zombified_piglin", "orezombiepigman"},
		new String[] {"enderman", "oreenderman"}, new String[] {"cave_spider", "orecavespider"}, new String[] {"silverfish", "oresilverfish"},
		new String[] {"magma_cube", "oremagmacube"}, new String[] {"witch", "orewitch"}, new String[] {"sheep", "oresheep"},
		new String[] {"wolf", "orewolf"}, new String[] {"mooshroom", "oremooshroom"}, new String[] {"ocelot", "oreocelot"},
		new String[] {"blaze", "oreblaze"}, new String[] {"wither_skeleton", "orewitherskeleton"},
		new String[] {"ender_dragon", "oreenderdragon"}, new String[] {"snow_golem", "oresnowgolem"},
		new String[] {"iron_golem", "oreirongolem"}, new String[] {"wither", "orewitherboss"}, new String[] {"villager", "orevillager"},
		new String[] {"horse", "orehorse"});
	/** OreSpawn's own mobs with an egg ore, as they're ported: entity id (orespawn:), then OreSpawn's texture name. */
	public static final List<String[]> CREATURE_EGGS = List.of(
		new String[] {"apple_cow", "oreredcow"}, new String[] {"golden_apple_cow", "oregoldcow"},
		new String[] {"enchanted_golden_apple_cow", "oreenchantedcow"}, new String[] {"bird", "orecockateil"},
		new String[] {"chipmunk", "orechipmunk"}, new String[] {"cricket", "orecricket"}, new String[] {"frog", "orefrog"},
		new String[] {"beaver", "orebeaver"}, new String[] {"cassowary", "orecassowary"}, new String[] {"rat", "orerat"},
		new String[] {"peacock", "orepeacock"}, new String[] {"stink_bug", "orestink"}, new String[] {"dragonfly", "oredragonfly"},
		new String[] {"gazelle", "oregazelle"}, new String[] {"ostrich", "oreostrich"}, new String[] {"lizard", "orelizard"},
		new String[] {"crab", "orecrab"}, new String[] {"scorpion", "orescorpion"}, new String[] {"small_worm", "oresmallworm"},
		new String[] {"medium_worm", "oremediumworm"}, new String[] {"large_worm", "orelargeworm"}, new String[] {"gold_fish", "oregoldfish"},
		new String[] {"flounder", "oreflounder"}, new String[] {"skate", "oreskate"}, new String[] {"whale", "orewhale"});

	static {
		for (String[] egg : VANILLA_EGGS) eggOre(egg[0], egg[1], Identifier.withDefaultNamespace(egg[0] + "_spawn_egg"));
		for (String[] egg : CREATURE_EGGS) eggOre(egg[0], egg[1], OreSpawn.id(egg[0] + "_spawn_egg"));
	}

	// OreRuby / OreAmethyst: hardness 10, resistance 4, drop 1-2 gems and 5-13 xp.
	public static final Block RUBY_ORE = block("ruby_ore", p -> new DropExperienceBlock(TrapezoidInt.of(5, 13, 0), p), rock(10.0F, 4.0F));
	public static final Block AMETHYST_ORE = block("amethyst_ore", p -> new DropExperienceBlock(TrapezoidInt.of(5, 13, 0), p), rock(10.0F, 4.0F));
	// OreUranium (hardness 10, resistance 1) and OreTitanium (15, 5) drop themselves; they smelt into nuggets.
	public static final Block URANIUM_ORE = block("uranium_ore", Block::new, rock(10.0F, 1.0F));
	public static final Block TITANIUM_ORE = block("titanium_ore", Block::new, rock(15.0F, 5.0F));
	// OreSalt: hardness 5, resistance 2, drops itself; smelts into 8 salt.
	public static final Block SALT_ORE = block("salt_ore", orespawn.block.SaltOreBlock::new, rock(5.0F, 2.0F));

	// BlockRuby (ruby and amethyst blocks, light 0.4), BlockUranium (light 0.2), BlockTitanium (light 0.5).
	public static final Block RUBY_BLOCK = block("ruby_block", Block::new, rock(4.0F, 4.0F).mapColor(MapColor.FIRE).lightLevel(s -> 6));
	public static final Block AMETHYST_BLOCK = block("amethyst_block", Block::new, rock(4.0F, 4.0F).mapColor(MapColor.COLOR_PURPLE).lightLevel(s -> 6));
	public static final Block URANIUM_BLOCK = block("uranium_block", Block::new, rock(5.0F, 5.0F).mapColor(MapColor.COLOR_LIGHT_GREEN).lightLevel(s -> 3));
	public static final Block TITANIUM_BLOCK = block("titanium_block", Block::new, rock(5.0F, 5.0F).mapColor(MapColor.METAL).lightLevel(s -> 7));

	private ModBlocks() {
	}

	public static void init() {
		// Static initialisers do the work; this makes sure they run before items and worldgen.
	}

	public static Block byName(String name) {
		Block block = BLOCKS.get(name);
		if (block == null) throw new IllegalArgumentException("No OreSpawn block " + name);
		return block;
	}

	/** OreGenericEgg: Material.ground, hardness 0.5, resistance 1, gravel sound; 50% chance of 5-9 xp. */
	public static Block eggOre(String mob, String texture, Identifier spawnEgg) {
		Block block = block(mob + "_egg_ore", EggOreBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.DIRT)
			.strength(0.5F, 1.0F).sound(SoundType.GRAVEL));
		EGG_ORE_LIST.add(new EggOre(mob, texture, spawnEgg, block));
		return block;
	}

	/** Material.rock: needs a pickaxe to drop anything. */
	private static BlockBehaviour.Properties rock(float hardness, float resistance) {
		return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
			.strength(hardness, resistance);
	}

	/** Registers a block and its item (used by every content group). */
	public static Block block(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
		Identifier id = OreSpawn.id(name);
		Block block = Registry.register(BuiltInRegistries.BLOCK, id, factory.apply(props.setId(ResourceKey.create(Registries.BLOCK, id))));
		Registry.register(BuiltInRegistries.ITEM, id,
			new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(ResourceKey.create(Registries.ITEM, id))));
		BLOCKS.put(name, block);
		return block;
	}
}
