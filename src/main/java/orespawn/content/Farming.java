package orespawn.content;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import orespawn.OreSpawn;
import orespawn.block.farming.CritterPlantBlock;
import orespawn.block.farming.DuctTapeBlock;
import orespawn.block.farming.ExperienceSaplingBlock;
import orespawn.block.farming.OreSpawnCropBlock;
import orespawn.block.farming.OreSpawnLeavesBlock;
import orespawn.block.farming.OreSpawnTorchBlock;
import orespawn.block.farming.OreSpawnWallTorchBlock;
import orespawn.block.farming.PizzaBlock;
import orespawn.block.farming.StalkCropBlock;
import orespawn.block.farming.Torches;
import orespawn.item.farming.ExperienceTreeSeedItem;
import orespawn.item.farming.FarmlandSeedItem;
import orespawn.item.farming.FruitTreeSeedItem;
import orespawn.item.farming.MagicAppleItem;
import orespawn.registry.ModBlocks;
import orespawn.registry.ModItems;
import orespawn.world.ModWorldgen;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * OreSpawn's farming: its crops and seeds, the creature plants, the apple, cherry, peach and experience trees, its
 * foods and fish, the Magic Apple, the Extreme Torch, the Creeper and Kraken Repellents, Duct Tape, and the wild plants
 * OreSpawnWorld scattered over the overworld (and mosquito plants in the Nether).
 * <p>
 * Foods keep OreSpawn's hunger and saturation (1.7.10's ItemFood(hunger, saturationModifier), the same meaning as 26.3's
 * FoodProperties). The fish and the sweets OreSpawn made always edible (ItemSunFish and friends) are always edible.
 * Crops, plants and trees are in orespawn.block.farming; their loot tables, models and worldgen are written by
 * tools/farming_assets.py, their names, items, recipes and tags come from tools/content/farming.json.
 * <p>
 * Not ported here: the crystal dimension's plants, trees and Crystal Apple (another group); OreSpawn's Love (heart)
 * food, which the Girlfriend drops; Lava Eel armor; apple trees and giant trees in the world (OreSpawn grew them only
 * in its Utopia and VillageMania dimensions, not the overworld); the Duplicator Tree log among the wild vegetables.
 */
public final class Farming {
	// ---- Crops (blocks without items: their seeds plant them) ----
	public static final Block CORN_PLANT = plant("corn_plant", p -> new StalkCropBlock(StalkCropBlock.Kind.CORN, () -> Farming.CORN, p), stalk());
	public static final Block TOMATO_PLANT = plant("tomato_plant", p -> new StalkCropBlock(StalkCropBlock.Kind.TOMATO, () -> Farming.TOMATO, p),
		stalk());
	public static final Block LETTUCE_PLANT = plant("lettuce_plant", p -> new StalkCropBlock(StalkCropBlock.Kind.LETTUCE, () -> Farming.LETTUCE, p),
		stalk());
	public static final Block QUINOA_PLANT = plant("quinoa_plant", p -> new StalkCropBlock(StalkCropBlock.Kind.QUINOA, () -> Farming.QUINOA, p),
		stalk());
	public static final Block RADISH_PLANT = plant("radish_plant", p -> new OreSpawnCropBlock(() -> Farming.RADISH, p), crop());
	public static final Block RICE_PLANT = plant("rice_plant", p -> new OreSpawnCropBlock(() -> Farming.RICE, p), crop());
	public static final Block STRAWBERRY_PLANT = plant("strawberry_plant", p -> new OreSpawnCropBlock(() -> Farming.STRAWBERRY_SEED, p), crop());
	public static final Block BUTTERFLY_PLANT = plant("butterfly_plant", p -> new CritterPlantBlock(() -> Farming.BUTTERFLY_SEED,
		OreSpawn.id("butterfly"), CritterPlantBlock.When.DAY, true, 7, 1, 1, p), crop());
	public static final Block MOTH_PLANT = plant("moth_plant", p -> new CritterPlantBlock(() -> Farming.MOTH_SEED, OreSpawn.id("moth"),
		CritterPlantBlock.When.NIGHT, true, 7, 1, 1, p), crop());
	public static final Block FIREFLY_PLANT = plant("firefly_plant", p -> new CritterPlantBlock(() -> Farming.FIREFLY_SEED,
		OreSpawn.id("firefly"), CritterPlantBlock.When.NIGHT, true, 6, 2, 6, p), crop());
	public static final Block MOSQUITO_PLANT = plant("mosquito_plant", p -> new CritterPlantBlock(() -> Farming.MOSQUITO_SEED,
		OreSpawn.id("mosquito"), CritterPlantBlock.When.ANY, false, 7, 2, 6, p), crop());

	// ---- Trees ----
	// BlockAppleLeaves / BlockExperienceLeaves / BlockScaryLeaves: hardness 0.2 (cherry and peach 0.15), grass sound.
	public static final Block APPLE_LEAVES = ModBlocks.block("apple_leaves", p -> new OreSpawnLeavesBlock(OreSpawnLeavesBlock.Kind.APPLE, p),
		leaves(0.2F));
	// BlockDuplicatorLog: hardness 0.2, wood sound, grows on random ticks.
	public static final Block DUPLICATOR_TREE_WOOD = ModBlocks.block("duplicator_tree_wood", orespawn.block.farming.DuplicatorLogBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.2F).sound(SoundType.WOOD).randomTicks());
	// BlockSkyTreeLog: hardness 0.2, wood sound; breaking one fells the whole tree.
	public static final Block SKY_TREE_WOOD = ModBlocks.block("sky_tree_wood", orespawn.block.farming.SkyTreeLogBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.2F).sound(SoundType.WOOD));
	public static final Block CHERRY_LEAVES = ModBlocks.block("cherry_leaves", p -> new OreSpawnLeavesBlock(OreSpawnLeavesBlock.Kind.CHERRY, p),
		leaves(0.15F));
	public static final Block PEACH_LEAVES = ModBlocks.block("peach_leaves", p -> new OreSpawnLeavesBlock(OreSpawnLeavesBlock.Kind.PEACH, p),
		leaves(0.15F));
	public static final Block SCARY_LEAVES = ModBlocks.block("scary_leaves", p -> new OreSpawnLeavesBlock(OreSpawnLeavesBlock.Kind.SCARY, p),
		leaves(0.2F));
	public static final Block EXPERIENCE_LEAVES = ModBlocks.block("experience_leaves",
		p -> new OreSpawnLeavesBlock(OreSpawnLeavesBlock.Kind.EXPERIENCE, p), leaves(0.2F));
	public static final Block EXPERIENCE_TREE_SAPLING = ModBlocks.block("experience_tree_sapling", ExperienceSaplingBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.GRASS)
			.pushReaction(PushReaction.POPPED));

	// ---- Torches: BlockTorch, light 1.0 (Extreme Torch) and 0.8 (the repellents) ----
	public static final Block EXTREME_TORCH = plant("extreme_torch", p -> new OreSpawnTorchBlock(null, p), torch(15, false));
	public static final Block EXTREME_TORCH_WALL = plant("extreme_torch_wall", p -> new OreSpawnWallTorchBlock(null, p),
		wall(EXTREME_TORCH, torch(15, false)));
	public static final Block CREEPER_REPELLENT = plant("creeper_repellent", p -> new OreSpawnTorchBlock(Torches.Repel.CREEPERS, p), torch(12, true));
	public static final Block CREEPER_REPELLENT_WALL = plant("creeper_repellent_wall", p -> new OreSpawnWallTorchBlock(Torches.Repel.CREEPERS, p),
		wall(CREEPER_REPELLENT, torch(12, true)));
	public static final Block KRAKEN_REPELLENT = plant("kraken_repellent", p -> new OreSpawnTorchBlock(Torches.Repel.KRAKENS, p), torch(12, true));
	public static final Block KRAKEN_REPELLENT_WALL = plant("kraken_repellent_wall", p -> new OreSpawnWallTorchBlock(Torches.Repel.KRAKENS, p),
		wall(KRAKEN_REPELLENT, torch(12, true)));

	// ---- Pizza and Duct Tape (OreSpawn set no step sound, so stone's) ----
	public static final Block PIZZA = plant("pizza", PizzaBlock::new, BlockBehaviour.Properties.of().forceSolidOn().strength(0.5F)
		.pushReaction(PushReaction.POPPED));
	public static final Block DUCT_TAPE = plant("duct_tape", DuctTapeBlock::new, BlockBehaviour.Properties.of().forceSolidOn().strength(0.5F)
		.pushReaction(PushReaction.POPPED));

	// ---- Seeds and crops (ItemSeedFood / ItemSeeds: they plant on farmland) ----
	public static final Item CORN = seed("corn", CORN_PLANT, food(6, 0.75F));
	public static final Item TOMATO = seed("tomato", TOMATO_PLANT, food(4, 0.55F));
	public static final Item LETTUCE = seed("lettuce", LETTUCE_PLANT, food(3, 0.45F));
	public static final Item QUINOA = seed("quinoa", QUINOA_PLANT, food(7, 0.85F));
	public static final Item RADISH = seed("radish", RADISH_PLANT, food(2, 0.45F));
	public static final Item RICE = seed("rice", RICE_PLANT, food(5, 0.65F));
	public static final Item STRAWBERRY_SEED = seed("strawberry_plant", STRAWBERRY_PLANT, null);
	public static final Item BUTTERFLY_SEED = seed("butterfly_plant", BUTTERFLY_PLANT, null);
	public static final Item MOTH_SEED = seed("moth_plant", MOTH_PLANT, null);
	public static final Item FIREFLY_SEED = seed("firefly_plant", FIREFLY_PLANT, null);
	public static final Item MOSQUITO_SEED = seed("mosquito_plant", MOSQUITO_PLANT, null);

	// ---- Fruit and tree seeds ----
	public static final Item STRAWBERRY = ModItems.item("strawberry", new Item.Properties().food(food(2, 0.65F)));
	public static final Item CHERRIES = ModItems.item("cherries", new Item.Properties().food(food(3, 0.45F)));
	public static final Item PEACH = ModItems.item("peach", new Item.Properties().food(food(4, 0.55F)));
	public static final Item APPLE_TREE_SEED = ModItems.register("apple_tree_seed",
		new FruitTreeSeedItem(() -> APPLE_LEAVES, ModItems.props("apple_tree_seed").stacksTo(16)));
	public static final Item CHERRY_PIT = ModItems.register("cherry_pit",
		new FruitTreeSeedItem(() -> CHERRY_LEAVES, ModItems.props("cherry_pit").stacksTo(16)));
	public static final Item PEACH_PIT = ModItems.register("peach_pit", new FruitTreeSeedItem(() -> PEACH_LEAVES, ModItems.props("peach_pit").stacksTo(16)));
	public static final Item EXPERIENCE_TREE_SEED = ModItems.register("experience_tree_seed",
		new ExperienceTreeSeedItem(ModItems.props("experience_tree_seed").stacksTo(1)));
	public static final Item MAGIC_APPLE = ModItems.register("omg_no_dont_do_it", new MagicAppleItem(ModItems.props("omg_no_dont_do_it").stacksTo(1)
		.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));

	// ---- Foods (ItemPopcorn: plain) ----
	public static final Item POPCORN = ModItems.item("popcorn", new Item.Properties().food(food(1, 0.5F)));
	public static final Item BUTTERED_POPCORN = ModItems.item("buttered_popcorn", new Item.Properties().food(food(2, 0.6F)));
	public static final Item BUTTERED_AND_SALTED_POPCORN = ModItems.item("buttered_and_salted_popcorn", new Item.Properties().food(food(3, 0.75F)));
	public static final Item BAG_OF_POPCORN = ModItems.item("bag_of_popcorn", new Item.Properties().food(food(10, 1.25F)));
	public static final Item BUTTER = ModItems.item("butter", new Item.Properties().food(food(1, 0.5F)));
	public static final Item CHEESE = ModItems.item("cheese", new Item.Properties().food(food(4, 0.5F)));
	public static final Item RAW_CORN_DOG = ModItems.item("raw_corn_dog", new Item.Properties().food(food(4, 0.6F)));
	public static final Item CORN_DOG = ModItems.item("corn_dog", new Item.Properties().food(food(16, 2.5F)));
	public static final Item RAW_BACON = ModItems.item("raw_bacon", new Item.Properties().food(food(8, 1.0F)));
	public static final Item GARDEN_SALAD = ModItems.item("garden_salad", new Item.Properties().food(food(10, 0.95F)));
	public static final Item BLT_SANDWICH = ModItems.item("blt_sandwich", new Item.Properties().food(food(12, 0.95F)));
	public static final Item A_CRABBY_PATTY = ModItems.item("a_crabby_patty", new Item.Properties().food(food(16, 2.35F)));

	// ---- ItemSunFish foods: always edible, with effects (ticks) ----
	public static final Item BACON = effectFood("bacon", 14, 1.5F, new MobEffectInstance(MobEffects.REGENERATION, 2000, 0),
		new MobEffectInstance(MobEffects.STRENGTH, 2000, 0));
	public static final Item BUTTER_CANDY = effectFood("butter_candy", 4, 0.5F, new MobEffectInstance(MobEffects.SPEED, 2000, 0),
		new MobEffectInstance(MobEffects.JUMP_BOOST, 2000, 0));
	public static final Item SUN_FISH = effectFood("sun_fish", 6, 0.6F, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0));
	// ItemFireFish / ItemSparkFish / ItemLavaEel: always edible, fire resistance.
	public static final Item FIRE_FISH = effectFood("fire_fish", 4, 0.6F, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, 0));
	public static final Item SPARK_FISH = effectFood("spark_fish", 1, 0.2F, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0));
	public static final Item LAVA_EEL = effectFood("lava_eel", 2, 0.6F, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
	// ItemGenericFish: a 1 in 4 chance of a second of hunger; they cook into cooked cod.
	public static final Item GREEN_FISH = fish("green_fish", 3, 0.5F);
	public static final Item BLUE_FISH = fish("blue_fish", 4, 0.4F);
	public static final Item PINK_FISH = fish("pink_fish", 4, 0.6F);
	public static final Item ROCK_FISH = fish("rock_fish", 3, 0.7F);
	public static final Item WOOD_FISH = fish("wood_fish", 5, 0.7F);
	public static final Item GREY_FISH = fish("grey_fish", 5, 0.5F);

	// ---- Block items ----
	public static final Item PIZZA_ITEM = ModItems.register("pizza", new BlockItem(PIZZA, ModItems.props("pizza").stacksTo(1).useItemDescriptionPrefix()));
	public static final Item DUCT_TAPE_ITEM = ModItems.register("duct_tape",
		new BlockItem(DUCT_TAPE, ModItems.props("duct_tape").stacksTo(1).useItemDescriptionPrefix()));
	public static final Item EXTREME_TORCH_ITEM = torchItem("extreme_torch", EXTREME_TORCH, EXTREME_TORCH_WALL);
	public static final Item CREEPER_REPELLENT_ITEM = torchItem("creeper_repellent", CREEPER_REPELLENT, CREEPER_REPELLENT_WALL);
	public static final Item KRAKEN_REPELLENT_ITEM = torchItem("kraken_repellent", KRAKEN_REPELLENT, KRAKEN_REPELLENT_WALL);

	/** The wild plants' placed features (data/orespawn/worldgen), with OreSpawnWorld's 1.7.10 biome names as 26.3 biomes. */
	private static final List<String[]> WILD = List.of(
		new String[] {"strawberry_plant_patch", "forest", "birch_forest"},              // addStrawberries: Forest(Hills), Birch Forest (Hills)
		new String[] {"corn_plant_patch", "plains"},                                     // addCorn: Plains
		new String[] {"tomato_plant_patch", "plains"},                                   // addTomatoes: Plains
		new String[] {"radish_plant_veggies", "river", "swamp"},                         // addVeggies: River, Swampland
		new String[] {"butterfly_plant_critters", "forest", "river", "jungle", "swamp", "birch_forest", "dark_forest"},
		new String[] {"mosquito_plant_patch", "jungle", "swamp"});                       // addMosquitos: Jungle, Swampland

	private Farming() {
	}

	public static void init() {
		for (String[] wild : WILD) {
			ResourceKey<Biome>[] biomes = Arrays.stream(wild, 1, wild.length)
				.map(b -> ResourceKey.create(Registries.BIOME, Identifier.withDefaultNamespace(b))).toArray(ResourceKey[]::new);
			BiomeModifications.addFeature(BiomeSelectors.includeByKey(biomes), GenerationStep.Decoration.VEGETAL_DECORATION, ModWorldgen.key(wild[0]));
		}
		// addNetherMosquitos: on netherrack anywhere in the Nether.
		BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(), GenerationStep.Decoration.VEGETAL_DECORATION,
			ModWorldgen.key("mosquito_plant_nether"));
	}

	// ---- Helpers ----

	/** Registers a block without an item (crops, wall torches, and blocks whose item is registered separately). */
	private static Block plant(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
		Identifier id = OreSpawn.id(name);
		return Registry.register(BuiltInRegistries.BLOCK, id, factory.apply(props.setId(ResourceKey.create(Registries.BLOCK, id))));
	}

	/** BlockCrops: no collision, random ticks, breaks at once. */
	private static BlockBehaviour.Properties crop() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.CROP)
			.pushReaction(PushReaction.POPPED);
	}

	/** BlockReed: the same, with grass's sound. */
	private static BlockBehaviour.Properties stalk() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.GRASS)
			.pushReaction(PushReaction.POPPED);
	}

	/** BlockLeaves, as 26.3's leaves: see-through, no suffocation, only ocelots and parrots spawn on them. */
	private static BlockBehaviour.Properties leaves(float hardness) {
		return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(hardness).randomTicks().sound(SoundType.GRASS).noOcclusion()
			.isValidSpawn((state, level, pos, type) -> type == EntityTypes.OCELOT || type == EntityTypes.PARROT)
			.isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos, box) -> false).ignitedByLava()
			.pushReaction(PushReaction.POPPED).isRedstoneConductor((state, level, pos) -> false);
	}

	private static BlockBehaviour.Properties torch(int light, boolean ticks) {
		BlockBehaviour.Properties p = BlockBehaviour.Properties.of().noCollision().instabreak().lightLevel(state -> light).sound(SoundType.WOOD)
			.pushReaction(PushReaction.POPPED);
		return ticks ? p.randomTicks() : p;
	}

	/** A wall torch drops and is named as its standing torch. */
	private static BlockBehaviour.Properties wall(Block standing, BlockBehaviour.Properties props) {
		return props.overrideLootTable(standing.getLootTable()).overrideDescription(standing.getDescriptionId());
	}

	private static FoodProperties food(int nutrition, float saturation) {
		return ModItems.food(nutrition, saturation);
	}

	private static Item seed(String name, Block crop, FoodProperties food) {
		Item.Properties props = ModItems.props(name).useItemDescriptionPrefix();
		if (food != null) props.food(food);
		return ModItems.register(name, new FarmlandSeedItem(crop, props));
	}

	private static Item effectFood(String name, int nutrition, float saturation, MobEffectInstance... effects) {
		FoodProperties food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).alwaysEdible().build();
		Consumable consumable = Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(List.of(effects))).build();
		return ModItems.item(name, new Item.Properties().food(food, consumable));
	}

	private static Item fish(String name, int nutrition, float saturation) {
		Consumable consumable = Consumables.defaultFood()
			.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HUNGER, 20, 0), 0.25F)).build();
		return ModItems.item(name, new Item.Properties().food(food(nutrition, saturation), consumable));
	}

	private static Item torchItem(String name, Block standing, Block wall) {
		return ModItems.register(name, new StandingAndWallBlockItem(standing, wall, Direction.DOWN, ModItems.props(name).useBlockDescriptionPrefix()));
	}
}
