package orespawn.content;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import orespawn.OreSpawn;
import orespawn.block.crystal.CrystalBlock;
import orespawn.block.crystal.CrystalFlowerBlock;
import orespawn.block.crystal.CrystalFurnaceBlock;
import orespawn.block.crystal.CrystalFurnaceBlockEntity;
import orespawn.block.crystal.CrystalFurnaceMenu;
import orespawn.block.crystal.CrystalLeavesBlock;
import orespawn.block.crystal.CrystalLogBlock;
import orespawn.block.crystal.CrystalOreBlock;
import orespawn.block.crystal.CrystalSaplingBlock;
import orespawn.block.crystal.CrystalTermiteNestBlock;
import orespawn.block.crystal.CrystalTorchBlock;
import orespawn.block.crystal.CrystalWorkbenchBlock;
import orespawn.block.crystal.CrystalizedBlock;
import orespawn.entity.ModEntities;
import orespawn.entity.crystal.CrystalCow;
import orespawn.registry.ModBlocks;
import orespawn.registry.ModItems;
import orespawn.world.crystal.CrystalChunkGenerator;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * OreSpawn's Crystal dimension (DimensionID5, "Dimension-Crystal") and everything in it: its blocks, the crystal
 * tools and materials, the Crystal Apple, the Crystal Furnace and Workbench, the Crystal Apple Cow, the terrain
 * generator and the dimension's creature list. The dimension itself is {@code data/orespawn/dimension/crystal.json}
 * ({@link orespawn.world.OreSpawnDimensions#CRYSTAL}); the Portals group takes players there.
 * <p>
 * Hardness and resistance are OreSpawn's setHardness / setResistance values, as ModBlocks takes them. Elsewhere: the
 * Pink Tourmaline and Tiger's Eye armor (the Weapons group), the crystal rocks and the Rock creature (the Tools group),
 * rice and quinoa (the Farming group), and the dimension's creatures other than the cow (their own groups).
 */
public final class Crystal {
	// ---- Blocks (OreSpawnMain) ----
	/** CrystalStone (OreBasicStone 2, 10). */
	public static final Block KYANITE = ModBlocks.block("kyanite", CrystalBlock::new,
		rock(MapColor.COLOR_LIGHT_BLUE, 2.0F, 10.0F, true));
	/** CrystalGrass (0.6, 2): Material.grass, so no tool needed. */
	public static final Block CRYSTAL_GRASS = ModBlocks.block("crystal_grass", CrystalBlock::new,
		crystal(MapColor.GRASS, 0.6F, 2.0F, SoundType.STONE, true));
	/** CrystalCoal (OreCrystal: light 0.6, 6, 20). */
	public static final Block CRYSTAL_ENERGY = ModBlocks.block("crystal_energy",
		p -> new CrystalOreBlock(CrystalOreBlock.Sparkle.ENERGY, 3, 1.5F, p), rock(MapColor.COLOR_PURPLE, 6.0F, 20.0F, false).lightLevel(s -> 9));
	/** CrystalCrystal (OreCrystalCrystal: light 0.4, 12, 40). */
	public static final Block PINK_TOURMALINE = ModBlocks.block("pink_tourmaline",
		p -> new CrystalOreBlock(CrystalOreBlock.Sparkle.PINK, 10, 1.0F, p), rock(MapColor.COLOR_PINK, 12.0F, 40.0F, false).lightLevel(s -> 6));
	/** TigersEye (OreCrystalCrystal: light 0.5, 15, 60). */
	public static final Block TIGERS_EYE = ModBlocks.block("tigers_eye",
		p -> new CrystalOreBlock(CrystalOreBlock.Sparkle.TIGER, 0, 0.0F, p), rock(MapColor.COLOR_ORANGE, 15.0F, 60.0F, false).lightLevel(s -> 7));
	/** CrystalRat (OreBasicStone 2.5, 14). */
	public static final Block CRYSTALIZED_RATS = ModBlocks.block("crystalized_rats", p -> new CrystalizedBlock("rat", 10, p),
		rock(MapColor.COLOR_LIGHT_BLUE, 2.5F, 14.0F, true));
	/** CrystalFairy (OreBasicStone 2.5, 14). */
	public static final Block CRYSTALIZED_FAIRIES = ModBlocks.block("crystalized_fairies", p -> new CrystalizedBlock("fairy", 6, p),
		rock(MapColor.COLOR_LIGHT_BLUE, 2.5F, 14.0F, true));
	/** MyCrystalPinkBlock (BlockCrystal: 4, 4, light 0.4). */
	public static final Block PINK_TOURMALINE_BLOCK = ModBlocks.block("pink_tourmaline_block", CrystalBlock::new,
		rock(MapColor.COLOR_PINK, 4.0F, 4.0F, false).lightLevel(s -> 6));
	/** MyTigersEyeBlock (BlockCrystal: 4, 4, light 0.4). */
	public static final Block TIGERS_EYE_BLOCK = ModBlocks.block("tigers_eye_block", CrystalBlock::new,
		rock(MapColor.COLOR_ORANGE, 4.0F, 4.0F, false).lightLevel(s -> 6));
	/** CrystalTermiteBlock (CrystalAntBlock: Material.grass, no hardness, random ticks). */
	public static final Block CRYSTAL_TERMITE_NEST = ModBlocks.block("crystal_termite_nest", CrystalTermiteNestBlock::new,
		crystal(MapColor.GRASS, 0.0F, 0.0F, SoundType.STONE, false).randomTicks());
	/** CrystalPlanksBlock (CrystalWood 1.5, 4: Material.wood). Burns like planks. */
	public static final Block CRYSTAL_PLANKS = block("crystal_planks", CrystalBlock::new, wood(1.5F, 4.0F, SoundType.STONE), Crystal::woodFuel);
	/** MyCrystalTreeLog (BlockCrystalTreeLog: 0.2, wood sound). Burns like a log. */
	public static final Block CRYSTAL_TREE_WOOD = block("crystal_tree_wood", CrystalLogBlock::new, wood(0.2F, 0.2F, SoundType.WOOD),
		Crystal::woodFuel);
	/** CrystalWorkbenchBlock (CrystalWorkbench 1, 5: Material.wood). */
	public static final Block CRYSTAL_WORKBENCH = block("crystal_workbench", CrystalWorkbenchBlock::new, wood(1.0F, 5.0F, SoundType.STONE),
		Crystal::woodFuel);
	/** CrystalFurnaceBlock / CrystalFurnaceOnBlock (2, 10; light 0.6 when lit). */
	public static final Block CRYSTAL_FURNACE = ModBlocks.block("crystal_furnace", CrystalFurnaceBlock::new,
		rock(MapColor.COLOR_LIGHT_BLUE, 2.0F, 10.0F, false).lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 9 : 0));

	// Saplings (BlockCrystalPlant, a BlockReed: no hardness, random ticks) and the leaves they grow.
	public static final Block RED_CRYSTAL_TREE_SAPLING = ModBlocks.block("red_crystal_tree_sapling",
		p -> new CrystalSaplingBlock(CrystalSaplingBlock.Tree.RED, p), plant(SoundType.STONE).randomTicks());
	public static final Block YELLOW_CRYSTAL_TREE_SAPLING = ModBlocks.block("yellow_crystal_tree_sapling",
		p -> new CrystalSaplingBlock(CrystalSaplingBlock.Tree.YELLOW, p), plant(SoundType.STONE).randomTicks());
	public static final Block BLUE_CRYSTAL_TREE_SAPLING = ModBlocks.block("blue_crystal_tree_sapling",
		p -> new CrystalSaplingBlock(CrystalSaplingBlock.Tree.BLUE, p), plant(SoundType.STONE).randomTicks());
	/** MyCrystalLeaves (0.2), MyCrystalLeaves2 and 3 (0.25): all "Crystal Tree Leaves" in OreSpawn. */
	public static final Block RED_CRYSTAL_TREE_LEAVES = ModBlocks.block("red_crystal_tree_leaves",
		p -> new CrystalLeavesBlock(() -> RED_CRYSTAL_TREE_SAPLING, p), leaves(0.2F));
	public static final Block YELLOW_CRYSTAL_TREE_LEAVES = ModBlocks.block("yellow_crystal_tree_leaves",
		p -> new CrystalLeavesBlock(() -> YELLOW_CRYSTAL_TREE_SAPLING, p), leaves(0.25F));
	public static final Block BLUE_CRYSTAL_TREE_LEAVES = ModBlocks.block("blue_crystal_tree_leaves",
		p -> new CrystalLeavesBlock(() -> BLUE_CRYSTAL_TREE_SAPLING, p), leaves(0.25F));

	// Flowers (MyBlockFlower: no hardness, grass sound).
	public static final Block RED_CRYSTAL_FLOWER = ModBlocks.block("red_crystal_flower", CrystalFlowerBlock::new, plant(SoundType.GRASS));
	public static final Block GREEN_CRYSTAL_FLOWER = ModBlocks.block("green_crystal_flower", CrystalFlowerBlock::new, plant(SoundType.GRASS));
	public static final Block BLUE_CRYSTAL_FLOWER = ModBlocks.block("blue_crystal_flower", CrystalFlowerBlock::new, plant(SoundType.GRASS));
	public static final Block YELLOW_CRYSTAL_FLOWER = ModBlocks.block("yellow_crystal_flower", CrystalFlowerBlock::new, plant(SoundType.GRASS));

	/** CrystalTorch (BlockCrystalTorch, light 0.99), standing; {@link #CRYSTAL_WALL_TORCH} is the same torch on a wall. */
	public static final Block CRYSTAL_TORCH = rawBlock("crystal_torch", CrystalTorchBlock::new, torch());
	public static final Block CRYSTAL_WALL_TORCH = rawBlock("crystal_wall_torch", CrystalTorchBlock.Wall::new,
		torch().overrideLootTable(CRYSTAL_TORCH.getLootTable()).overrideDescription(CRYSTAL_TORCH.getDescriptionId()));

	static {
		Identifier id = OreSpawn.id("crystal_torch");
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
		BlockItem torch = new StandingAndWallBlockItem(CRYSTAL_TORCH, CRYSTAL_WALL_TORCH, Direction.DOWN,
			new Item.Properties().useBlockDescriptionPrefix().setId(key));
		torch.registerBlocks(Item.BY_BLOCK, torch);
		Registry.register(BuiltInRegistries.ITEM, key, torch);
		ModBlocks.BLOCKS.put("crystal_torch", CRYSTAL_TORCH);
	}

	// ---- Items ----
	/** CrystalSticks ("Crystal Shards"). */
	public static final Item CRYSTAL_SHARDS = ModItems.item("crystal_shards", new Item.Properties());
	public static final Item PINK_TOURMALINE_INGOT = ModItems.item("pink_tourmaline_ingot", new Item.Properties());
	public static final Item TIGERS_EYE_INGOT = ModItems.item("tigers_eye_ingot", new Item.Properties());
	/** MyCrystalApple (ItemSunFish 5, 0.85, always edible): Regeneration and Strength for 2.5 minutes. */
	public static final Item CRYSTAL_APPLE = ModItems.register("crystal_apple", new Item(ModItems.props("crystal_apple").food(
		new FoodProperties.Builder().nutrition(5).saturationModifier(0.85F).alwaysEdible().build(),
		crystalAppleEffects())));

	// get_weaponstats(name, harvest level, uses, efficiency, damage, enchantability); ModItems' conventions.
	public static final ToolMaterial CRYSTAL_WOOD_TOOL = tool("crystal_wood", BlockTags.INCORRECT_FOR_IRON_TOOL, 300, 3, 2, 15);
	public static final ToolMaterial KYANITE_TOOL = tool("kyanite", BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 800, 6, 5, 45);
	public static final ToolMaterial PINK_TOURMALINE_TOOL = tool("pink_tourmaline", BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1100, 10, 7, 65);
	public static final ToolMaterial TIGERS_EYE_TOOL = tool("tigers_eye", BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1600, 12, 8, 75);

	static {
		toolSet("crystal_wood", CRYSTAL_WOOD_TOOL);
		toolSet("kyanite", KYANITE_TOOL);
		toolSet("pink_tourmaline", PINK_TOURMALINE_TOOL);
		toolSet("tigers_eye", TIGERS_EYE_TOOL);
	}

	// ---- The furnace's block entity and menu ----
	public static final BlockEntityType<CrystalFurnaceBlockEntity> CRYSTAL_FURNACE_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
		OreSpawn.id("crystal_furnace"), new BlockEntityType<>(CrystalFurnaceBlockEntity::new, Set.of(CRYSTAL_FURNACE)));
	public static final MenuType<CrystalFurnaceMenu> CRYSTAL_FURNACE_MENU = Registry.register(BuiltInRegistries.MENU, OreSpawn.id("crystal_furnace"),
		new MenuType<>(CrystalFurnaceMenu::new, FeatureFlags.VANILLA_SET));

	// ---- The Crystal Apple Cow ("Crystal Apple Cow": an EntityCow, 0.9 x 1.3) and its egg ore ----
	public static final EntityType<CrystalCow> CRYSTAL_APPLE_COW = ModEntities.register("crystal_apple_cow", CrystalCow::new, MobCategory.CREATURE,
		0.9F, 1.3F, 10);
	public static final Block CRYSTAL_APPLE_COW_EGG_ORE = ModBlocks.eggOre("crystal_apple_cow", "orecrystalcow",
		OreSpawn.id("crystal_apple_cow_spawn_egg"));

	/** The dimension's one biome, OreSpawn's "Crystal" (worldgen/biome/crystal.json). */
	public static final ResourceKey<Biome> CRYSTAL_BIOME = ResourceKey.create(Registries.BIOME, OreSpawn.id("crystal"));

	/**
	 * BiomeGenUtopianPlains.setCrystalCreatures: the only creatures of the Crystal biome (OreSpawn cleared vanilla's), as
	 * id, weight, min, max. Each takes its own spawn category; creatures not registered yet are skipped.
	 */
	private static final List<Object[]> SPAWNS = List.of(
		new Object[] {"crystal_apple_cow", 1, 1, 4}, new Object[] {"fairy", 10, 4, 8}, new Object[] {"peacock", 5, 4, 8},
		new Object[] {"mantis", 1, 1, 1}, new Object[] {"rotator", 4, 1, 2}, new Object[] {"vortex", 3, 1, 2},
		new Object[] {"crystal_urchin", 15, 2, 4}, new Object[] {"dungeon_beast", 30, 4, 6}, new Object[] {"rat", 40, 4, 6},
		new Object[] {"butterfly", 10, 2, 4}, new Object[] {"bird", 4, 1, 2}, new Object[] {"moth", 4, 1, 2}, new Object[] {"whale", 1, 1, 2},
		new Object[] {"crab", 1, 1, 2}, new Object[] {"flounder", 5, 6, 8}, new Object[] {"irukandji", 4, 2, 3}, new Object[] {"skate", 2, 3, 6},
		new Object[] {"frog", 1, 3, 5});

	private Crystal() {
	}

	public static void init() {
		CrystalChunkGenerator.register();
		FabricDefaultAttributeRegistry.register(CRYSTAL_APPLE_COW, AbstractCow.createAttributes());
		SpawnPlacements.register(CRYSTAL_APPLE_COW, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CrystalCow::checkSpawn);
		// Looked up when the biomes load, after every group has registered its creatures.
		BiomeModifications.create(OreSpawn.id("crystal_creatures")).add(ModificationPhase.ADDITIONS, BiomeSelectors.includeByKey(CRYSTAL_BIOME),
			context -> {
				for (Object[] s : SPAWNS) {
					BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id((String) s[0])).ifPresent(type -> {
						if (type.getCategory() != MobCategory.MISC) {
							context.getMobSpawnSettings().addSpawn(type, (Integer) s[1], (Integer) s[2], (Integer) s[3]);
						}
					});
				}
			});
	}

	// ---- Helpers ----

	/** A crystal block: light passes through, nothing suffocates in it, and only the dense ones hide their neighbours. */
	private static BlockBehaviour.Properties crystal(MapColor color, float hardness, float resistance, SoundType sound, boolean occludes) {
		BlockBehaviour.Properties p = BlockBehaviour.Properties.of().mapColor(color).strength(hardness, resistance).sound(sound)
			.isSuffocating((s, l, pos) -> false).isViewBlocking((s, l, pos, box) -> false);
		return occludes ? p : p.noOcclusion();
	}

	/** Material.rock: needs a pickaxe to drop anything. */
	private static BlockBehaviour.Properties rock(MapColor color, float hardness, float resistance, boolean occludes) {
		return crystal(color, hardness, resistance, SoundType.STONE, occludes).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops();
	}

	/** Material.wood. */
	private static BlockBehaviour.Properties wood(float hardness, float resistance, SoundType sound) {
		return crystal(MapColor.WOOD, hardness, resistance, sound, false).instrument(NoteBlockInstrument.BASS);
	}

	private static BlockBehaviour.Properties plant(SoundType sound) {
		return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().instabreak().sound(sound).pushReaction(PushReaction.POPPED);
	}

	private static BlockBehaviour.Properties leaves(float hardness) {
		return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(hardness).randomTicks().sound(SoundType.GRASS).noOcclusion()
			.isValidSpawn((s, l, pos, type) -> false).isSuffocating((s, l, pos) -> false).isViewBlocking((s, l, pos, box) -> false)
			.pushReaction(PushReaction.POPPED);
	}

	private static BlockBehaviour.Properties torch() {
		return BlockBehaviour.Properties.of().noCollision().instabreak().lightLevel(s -> 14).sound(SoundType.STONE).pushReaction(PushReaction.POPPED);
	}

	/** Material.wood blocks burn in a furnace (and the Crystal Furnace) for 300 ticks, as 1.7.10's did. */
	private static Item.Properties woodFuel(Item.Properties p) {
		return p.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
	}

	/** Registers a block with no item. */
	private static Block rawBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
		Identifier id = OreSpawn.id(name);
		return Registry.register(BuiltInRegistries.BLOCK, id, factory.apply(props.setId(ResourceKey.create(Registries.BLOCK, id))));
	}

	/** ModBlocks.block, with extra properties on the block's item. */
	private static Block block(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props,
			UnaryOperator<Item.Properties> item) {
		Block block = rawBlock(name, factory, props);
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, OreSpawn.id(name));
		BlockItem blockItem = new BlockItem(block, item.apply(new Item.Properties().useBlockDescriptionPrefix().setId(key)));
		blockItem.registerBlocks(Item.BY_BLOCK, blockItem);
		Registry.register(BuiltInRegistries.ITEM, key, blockItem);
		ModBlocks.BLOCKS.put(name, block);
		return block;
	}

	private static Consumable crystalAppleEffects() {
		return Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(List.of(new MobEffectInstance(MobEffects.REGENERATION, 3000, 0),
			new MobEffectInstance(MobEffects.STRENGTH, 3000, 0)))).build();
	}

	/** As ModItems.tool: 1.7.10's material damage becomes 26.3's bonus + 1. */
	private static ToolMaterial tool(String name, net.minecraft.tags.TagKey<Block> incorrect, int uses, float efficiency, int damage, int enchant) {
		return new ToolMaterial(incorrect, uses, efficiency, damage + 1, enchant, ModItems.repairTag(name));
	}

	/** As ModItems.toolSet: CrystalSword, CrystalPickaxe, CrystalShovel, CrystalAxe and CrystalHoe of one material. */
	private static void toolSet(String name, ToolMaterial m) {
		ModItems.register(name + "_sword", new Item(ModItems.props(name + "_sword").sword(m, 3.0F, -2.4F)));
		ModItems.register(name + "_pickaxe", new Item(ModItems.props(name + "_pickaxe").pickaxe(m, 1.0F, -2.8F)));
		ModItems.register(name + "_shovel", new Item(ModItems.props(name + "_shovel").shovel(m, 0.0F, -3.0F)));
		ModItems.register(name + "_axe", new Item(ModItems.props(name + "_axe").axe(m, 2.0F, -3.0F)));
		ModItems.register(name + "_hoe", new Item(ModItems.props(name + "_hoe").hoe(m, -m.attackDamageBonus(), 0.0F)));
	}
}
