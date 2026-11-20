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
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import orespawn.OreSpawn;
import orespawn.block.EggOreBlock;
import orespawn.block.bosses.KingSpawnerBlock;
import orespawn.block.bosses.QueenSpawnerBlock;
import orespawn.entity.ModEntities;
import orespawn.entity.bosses.BetterFireball;
import orespawn.entity.bosses.BossHead;
import orespawn.entity.bosses.Godzilla;
import orespawn.entity.bosses.Kraken;
import orespawn.entity.bosses.PurplePower;
import orespawn.entity.bosses.TheKing;
import orespawn.entity.bosses.ThePrince;
import orespawn.entity.bosses.ThePrinceAdult;
import orespawn.entity.bosses.ThePrinceTeen;
import orespawn.entity.bosses.ThePrincess;
import orespawn.entity.bosses.TheQueen;
import orespawn.registry.ModBlocks;
import orespawn.registry.ModItems;

/**
 * OreSpawn's bosses: Mobzilla, The King, The Queen, the royal children (The Prince, who grows into The Young Prince and
 * then The Young Adult Prince, and The Princess) and The Kraken, with the giants' head hitboxes (OreSpawn's
 * GodzillaHead / KingHead / QueenHead), their fireballs and purple power bolts, the scales and tooth they drop, the
 * King and Queen spawner blocks, and their egg ores
 * (Mobzilla, The King and The Queen also have "Spawn Egg Part" ores, nine of which craft into the full egg ore).
 * <p>
 * Sizes are OreSpawn's (its PlayNicely option, off by default, shrank the giants to a quarter; it isn't ported).
 * Tracking ranges: OreSpawn tracked them to 128 blocks; 10 chunks here, as vanilla does for its own bosses.
 */
public final class Bosses {
	// ---- Creatures (each also gets <id>_spawn_egg) ----
	public static final EntityType<Godzilla> MOBZILLA = ModEntities.register("mobzilla", Godzilla::new, MobCategory.MONSTER, 9.9F, 25.0F, 10);
	public static final EntityType<TheKing> THE_KING = ModEntities.register("the_king", TheKing::new, MobCategory.MONSTER, 22.0F, 24.0F, 10);
	public static final EntityType<TheQueen> THE_QUEEN = ModEntities.register("the_queen", TheQueen::new, MobCategory.MONSTER, 22.0F, 24.0F, 10);
	public static final EntityType<ThePrince> THE_PRINCE = ModEntities.register("the_prince", ThePrince::new, MobCategory.CREATURE, 0.75F, 1.25F,
		10);
	public static final EntityType<ThePrinceTeen> THE_YOUNG_PRINCE = ModEntities.register("the_young_prince", ThePrinceTeen::new,
		MobCategory.CREATURE, 3.25F, 4.25F, 10);
	public static final EntityType<ThePrinceAdult> THE_YOUNG_ADULT_PRINCE = ModEntities.register("the_young_adult_prince", ThePrinceAdult::new,
		MobCategory.CREATURE, 6.25F, 10.25F, 10);
	public static final EntityType<ThePrincess> THE_PRINCESS = ModEntities.register("the_princess", ThePrincess::new, MobCategory.CREATURE, 0.75F,
		1.25F, 10);
	public static final EntityType<Kraken> THE_KRAKEN = ModEntities.register("the_kraken", Kraken::new, MobCategory.MONSTER, 4.0F, 15.0F, 10);

	// ---- Head hitboxes (OreSpawn's MobzillaHead / KingHead / QueenHead: 9.9 or 19.9 wide, 10 high) ----
	public static final EntityType<BossHead> MOBZILLA_HEAD = ModEntities.misc("mobzilla_head", BossHead::new, 9.9F, 10.0F, 10, 2);
	public static final EntityType<BossHead> KING_HEAD = ModEntities.misc("king_head", BossHead::new, 19.9F, 10.0F, 10, 2);
	public static final EntityType<BossHead> QUEEN_HEAD = ModEntities.misc("queen_head", BossHead::new, 19.9F, 10.0F, 10, 2);

	// ---- Projectiles ----
	/** OreSpawn's BetterFireball (Mobzilla's, The King's; also used by OreSpawn's Dragon, Mothra and Brutalfly). */
	public static final EntityType<BetterFireball> BETTER_FIREBALL = ModEntities.misc("better_fireball", BetterFireball::new, 1.0F, 1.0F, 4, 10);
	/** OreSpawn's PurplePower (The King's, The Queen's and The Princess's purple bolts; a flying Mob, as OreSpawn's was). */
	public static final EntityType<PurplePower> PURPLE_POWER = ModEntities.misc("purple_power", PurplePower::new, 0.75F, 0.75F, 4, 1);

	// ---- Items only they drop ----
	public static final Item MOBZILLA_SCALE = ModItems.item("mobzilla_scale", new Item.Properties());
	public static final Item THE_QUEEN_SCALE = ModItems.item("the_queen_scale", new Item.Properties());
	public static final Item KRAKEN_TOOTH = ModItems.item("kraken_tooth", new Item.Properties());

	// ---- Blocks ----
	/** BlockRuby: Material.rock, hardness 4, resistance 4, light 0.4. Nine Mobzilla Scales. */
	public static final Block MOBZILLA_SCALE_BLOCK = ModBlocks.block("mobzilla_scale_block", Block::new, BlockBehaviour.Properties.of()
		.mapColor(MapColor.COLOR_GREEN).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(4.0F, 4.0F)
		.lightLevel(s -> 6));
	/** KingSpawnerBlock / QueenSpawnerBlock: BlockReed-like (no collision, breaks instantly), light 0.9. */
	public static final Block THE_KING_SPAWNER_BLOCK = ModBlocks.block("the_king_spawner_block", KingSpawnerBlock::new, spawnerProps());
	public static final Block THE_QUEEN_SPAWNER_BLOCK = ModBlocks.block("the_queen_spawner_block", QueenSpawnerBlock::new, spawnerProps());

	// ---- Egg ores (OreSpawn's OreGenericEgg). Kraken, Mobzilla and the three part ores are in OreSpawn's ore
	// generation; The King's and The Queen's full egg ores only come from nine parts. ----
	public static final Block THE_KRAKEN_EGG_ORE = ModBlocks.eggOre("the_kraken", "orekraken", OreSpawn.id("the_kraken_spawn_egg"));
	public static final Block MOBZILLA_EGG_ORE = ModBlocks.eggOre("mobzilla", "oregodzilla", OreSpawn.id("mobzilla_spawn_egg"));
	public static final Block THE_KING_EGG_ORE = ModBlocks.eggOre("the_king", "oretheking", OreSpawn.id("the_king_spawn_egg"));
	public static final Block THE_QUEEN_EGG_ORE = ModBlocks.eggOre("the_queen", "orethequeen", OreSpawn.id("the_queen_spawn_egg"));
	public static final Block MOBZILLA_EGG_PART_ORE = ModBlocks.block("mobzilla_egg_part_ore", EggOreBlock::new, eggOreProps());
	public static final Block THE_KING_EGG_PART_ORE = ModBlocks.block("the_king_egg_part_ore", EggOreBlock::new, eggOreProps());
	public static final Block THE_QUEEN_EGG_PART_ORE = ModBlocks.block("the_queen_egg_part_ore", EggOreBlock::new, eggOreProps());

	private Bosses() {
	}

	/** Same as ModBlocks.eggOre's blocks: OreGenericEgg is Material.ground, hardness 0.5, resistance 1, gravel sound. */
	private static BlockBehaviour.Properties eggOreProps() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F, 1.0F).sound(SoundType.GRAVEL);
	}

	private static BlockBehaviour.Properties spawnerProps() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(s -> 13)
			.randomTicks().pushReaction(PushReaction.POPPED);
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(MOBZILLA, Godzilla.createAttributes());
		FabricDefaultAttributeRegistry.register(THE_KING, TheKing.createAttributes());
		FabricDefaultAttributeRegistry.register(THE_QUEEN, TheQueen.createAttributes());
		FabricDefaultAttributeRegistry.register(THE_PRINCE, ThePrince.createAttributes());
		FabricDefaultAttributeRegistry.register(THE_YOUNG_PRINCE, ThePrinceTeen.createAttributes());
		FabricDefaultAttributeRegistry.register(THE_YOUNG_ADULT_PRINCE, ThePrinceAdult.createAttributes());
		FabricDefaultAttributeRegistry.register(THE_PRINCESS, ThePrincess.createAttributes());
		FabricDefaultAttributeRegistry.register(THE_KRAKEN, Kraken.createAttributes());
		FabricDefaultAttributeRegistry.register(PURPLE_POWER, PurplePower.createAttributes());

		var ground = SpawnPlacementTypes.ON_GROUND;
		var air = SpawnPlacementTypes.NO_RESTRICTIONS;
		var height = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
		SpawnPlacements.register(MOBZILLA, ground, height, Godzilla::checkSpawn);
		SpawnPlacements.register(THE_KING, air, height, TheKing::checkSpawn);
		SpawnPlacements.register(THE_QUEEN, air, height, TheQueen::checkSpawn);
		SpawnPlacements.register(THE_PRINCE, ground, height, ThePrince::checkSpawn);
		SpawnPlacements.register(THE_YOUNG_PRINCE, ground, height, ThePrinceTeen::checkSpawn);
		SpawnPlacements.register(THE_YOUNG_ADULT_PRINCE, ground, height, ThePrinceAdult::checkSpawn);
		SpawnPlacements.register(THE_PRINCESS, ground, height, ThePrincess::checkSpawn);
		SpawnPlacements.register(THE_KRAKEN, air, height, Kraken::checkSpawn);
		// No natural spawns in 26.3's biomes: Mobzilla spawned only in OreSpawn's Utopian Plains biome ("UtopianPlains:2:1:1",
		// added by the Village Mania group by id); The Kraken comes from Attack Squids killed by a player (the Sea group);
		// the others from spawner blocks, eggs and growing up.
	}
}
