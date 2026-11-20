package orespawn.content;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.levelgen.GenerationStep;
import orespawn.OreSpawn;
import orespawn.entity.ModEntities;
import orespawn.entity.tools.Elevator;
import orespawn.entity.tools.RockBase;
import orespawn.entity.tools.RockFeature;
import orespawn.entity.tools.Shoes;
import orespawn.entity.tools.ThrownCage;
import orespawn.entity.tools.ThrownRock;
import orespawn.item.tools.CritterCageItem;
import orespawn.item.tools.CritterCages;
import orespawn.item.tools.ExperienceCatcherItem;
import orespawn.item.tools.HoverboardItem;
import orespawn.item.tools.InstantGardenItem;
import orespawn.item.tools.InstantShelterItem;
import orespawn.item.tools.MinersDreamItem;
import orespawn.item.tools.RockItem;
import orespawn.item.tools.ShoeItem;
import orespawn.item.tools.SifterItem;
import orespawn.item.tools.WrenchItem;
import orespawn.item.tools.ZooCageItem;
import orespawn.item.tools.ZooKeeperItem;
import orespawn.registry.ModItems;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OreSpawn's tools and gadgets: the Critter Cage and its caged creatures, the ZooKeeper Shard and Zoo Cages, the Instant
 * Survival Shelter and Garden, the Miner's Dream, the Experience Orb Catcher, the Wrench, the Sifter, the Hoverboard,
 * the throwable shoes (and Game Controller) and the rocks, with the entities they make: thrown cages, shoes and rocks,
 * rocks lying on the ground and the Hoverboard. Also the worldgen feature that scatters rocks ({@code orespawn:rocks}).
 * <p>
 * A caged item exists only for creatures registered before this class, so {@link #init} must run after every creature
 * group. Not ported: the Random Dungeon item (it needs the structures group's dungeons) and the Crystal dimension's
 * cage recipe (Crystal Sticks and Pink Crystal Ingots).
 */
public final class Tools {
	// ---- Critter Cage (stacks of 16) ----
	public static final Item EMPTY_CRITTER_CAGE = ModItems.register("empty_critter_cage",
		new CritterCageItem(ModItems.props("empty_critter_cage").stacksTo(16), null));
	/** Caged item id -> item, for the creatures that exist ({@link CritterCages}). */
	public static final Map<String, Item> CAGED = registerCages();

	// ---- Zoo ----
	public static final Item ZOOKEEPER_SHARD = ModItems.register("zookeeper_shard", new ZooKeeperItem(ModItems.props("zookeeper_shard").durability(1)));
	public static final Item EXTRA_SMALL_ZOO_CAGE = zooCage("extra_small_zoo_cage", 3);
	public static final Item SMALL_ZOO_CAGE = zooCage("small_zoo_cage", 5);
	public static final Item MEDIUM_ZOO_CAGE = zooCage("medium_zoo_cage", 9);
	public static final Item LARGE_ZOO_CAGE = zooCage("large_zoo_cage", 13);
	public static final Item EXTRA_LARGE_ZOO_CAGE = zooCage("extra_large_zoo_cage", 17);

	// ---- Building gadgets (stacks of 16) ----
	public static final Item INSTANT_SURVIVAL_SHELTER = ModItems.register("instant_survival_shelter",
		new InstantShelterItem(ModItems.props("instant_survival_shelter").stacksTo(16)));
	public static final Item INSTANT_SURVIVAL_GARDEN = ModItems.register("instant_survival_garden",
		new InstantGardenItem(ModItems.props("instant_survival_garden").stacksTo(16)));
	public static final Item MINERS_DREAM = ModItems.register("miners_dream", new MinersDreamItem(ModItems.props("miners_dream").stacksTo(16)));

	// ---- Other tools ----
	public static final Item EXPERIENCE_ORB_CATCHER = ModItems.register("experience_orb_catcher",
		new ExperienceCatcherItem(ModItems.props("experience_orb_catcher").stacksTo(16)));
	public static final Item WRENCH = ModItems.register("wrench", new WrenchItem(ModItems.props("wrench").durability(100)));
	public static final Item SIFTER = ModItems.register("sifter", new SifterItem(ModItems.props("sifter").durability(600)));
	public static final Item HOVERBOARD = ModItems.register("hoverboard", new HoverboardItem(ModItems.props("hoverboard").stacksTo(1)));

	// ---- Throwables ----
	public static final Item RED_HEELS = shoe("red_heels");
	public static final Item BLACK_HEELS = shoe("black_heels");
	public static final Item SLIPPERS = shoe("slippers");
	public static final Item BOOTS = shoe("boots");
	public static final Item GAME_CONTROLLER = shoe("game_controller");
	/** OreSpawn's rock types 1-12, in order. */
	public static final List<Item> ROCKS = List.of(rock("small_rock", 1), rock("big_rock", 2), rock("flame_rock", 3), rock("poison_rock", 4),
		rock("slowness_rock", 5), rock("weakness_rock", 6), rock("painful_rock", 7), rock("explosive_rock", 8), rock("flame_crystal", 9),
		rock("poison_crystal", 10), rock("slowness_crystal", 11), rock("explosive_crystal", 12));

	// ---- Entities (OreSpawn tracked these to 64 blocks, the Rock to 32 and the Hoverboard to 128) ----
	public static final EntityType<ThrownCage> ENTITY_CAGE = ModEntities.misc("entity_cage", ThrownCage::new, 0.25F, 0.25F, 4, 10);
	public static final EntityType<Shoes> SHOES = ModEntities.misc("shoes", Shoes::new, 0.25F, 0.25F, 4, 10);
	public static final EntityType<ThrownRock> ENTITY_THROWN_ROCK = ModEntities.misc("entity_thrown_rock", ThrownRock::new, 0.25F, 0.25F, 4, 10);
	/** OreSpawn's RockBase ("Rock"): 0.25 x 0.15. No spawn egg (OreSpawn's was a plain coloured one, with no texture). */
	public static final EntityType<RockBase> ROCK = ModEntities.misc("rock", RockBase::new, 0.25F, 0.15F, 2, 3);
	/** OreSpawn's Elevator ("Hoverboard"): 1.25 x 1.0, sent every tick since the server steers it. */
	public static final EntityType<Elevator> HOVERBOARD_ENTITY = ModEntities.misc("hoverboard", Elevator::new, 1.25F, 1.0F, 8, 1);

	private static final ResourceKey<Level> CRYSTAL_DIMENSION = ResourceKey.create(Registries.DIMENSION, OreSpawn.id("crystal"));

	private Tools() {
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(ROCK, RockBase.createAttributes());
		for (Item rock : ROCKS) DispenserBlock.registerProjectileBehavior(rock);
		Registry.register(BuiltInRegistries.FEATURE_TYPE, OreSpawn.id("rocks"), RockFeature.CODEC);
		// OreSpawnWorld's overworld branch: addRocks only where the chunk's biome is River, Extreme Hills or Desert.
		BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.RIVER, Biomes.WINDSWEPT_HILLS, Biomes.DESERT),
			GenerationStep.Decoration.VEGETAL_DECORATION, ResourceKey.create(Registries.PLACED_FEATURE, OreSpawn.id("rocks")));
		// The ZooKeeper Shard and Wrench act on a left click instead of hitting (OreSpawn's onLeftClickEntity).
		AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (player.isSpectator()) return InteractionResult.PASS;
			Item held = player.getItemInHand(hand).getItem();
			if (held instanceof ZooKeeperItem shard) return shard.leftClickEntity(player, level, hand, entity);
			if (held instanceof WrenchItem wrench) return wrench.leftClickEntity(player, level, hand, entity);
			return InteractionResult.PASS;
		});
	}

	/** OreSpawn's DimensionID5 (the Crystal dimension), once another group adds it as {@code orespawn:crystal}. */
	public static boolean inCrystalDimension(Level level) {
		return level.dimension().equals(CRYSTAL_DIMENSION);
	}

	/** A block another group registers, or null until it exists. */
	public static @Nullable Block lazyBlock(String id) {
		return BuiltInRegistries.BLOCK.getOptional(OreSpawn.id(id)).orElse(null);
	}

	private static Map<String, Item> registerCages() {
		Map<String, Item> map = new LinkedHashMap<>();
		for (CritterCages.Cage cage : CritterCages.ALL) {
			if (!CritterCages.exists(cage)) continue;
			map.put(cage.item(), ModItems.register(cage.item(), new CritterCageItem(ModItems.props(cage.item()).stacksTo(16), cage)));
		}
		return Collections.unmodifiableMap(map);
	}

	private static Item zooCage(String id, int size) {
		return ModItems.register(id, new ZooCageItem(ModItems.props(id).stacksTo(16), size));
	}

	private static Item shoe(String id) {
		return ModItems.register(id, new ShoeItem(ModItems.props(id)));
	}

	private static Item rock(String id, int type) {
		return ModItems.register(id, new RockItem(ModItems.props(id), type));
	}
}
