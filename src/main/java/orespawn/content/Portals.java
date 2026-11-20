package orespawn.content;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import orespawn.OreSpawn;
import orespawn.block.portals.AntNestBlock;
import orespawn.block.portals.RandomTeleportBlock;
import orespawn.block.portals.TrollBlock;
import orespawn.entity.ModEntities;
import orespawn.entity.ants.Ant;
import orespawn.entity.ants.RainbowAnt;
import orespawn.entity.ants.RedAnt;
import orespawn.entity.ants.Termite;
import orespawn.entity.ants.UnstableAnt;
import orespawn.registry.ModBlocks;
import orespawn.world.AntNestsFeature;
import orespawn.world.ModWorldgen;
import orespawn.world.NetherAntNestsFeature;

import java.util.List;
import java.util.function.Supplier;

/**
 * The Portals group: travel between the overworld and OreSpawn's dimensions (orespawn.world.OreSpawnTeleporter), the
 * ants and termite that take you there, their nests and troll blocks, and the Random Teleport Block. Ants have no
 * natural spawns (they come out of nests and troll blocks); the nests and troll blocks generate in the overworld and the
 * Nether as OreSpawnWorld placed them. Tracking ranges are OreSpawn's: 16 blocks for ants, 32 for the termite.
 */
public final class Portals {
	public static final EntityType<Ant> ANT = ModEntities.register("ant", Ant::new, MobCategory.CREATURE, 0.1F, 0.1F, 1);
	public static final EntityType<RedAnt> RED_ANT = ModEntities.register("red_ant", RedAnt::new, MobCategory.CREATURE, 0.2F, 0.2F, 1);
	public static final EntityType<RainbowAnt> RAINBOW_ANT = ModEntities.register("rainbow_ant", RainbowAnt::new, MobCategory.CREATURE, 0.1F,
		0.1F, 1);
	public static final EntityType<UnstableAnt> UNSTABLE_ANT = ModEntities.register("unstable_ant", UnstableAnt::new, MobCategory.CREATURE,
		0.1F, 0.1F, 1);
	public static final EntityType<Termite> TERMITE = ModEntities.register("termite", Termite::new, MobCategory.CREATURE, 0.2F, 0.2F, 2);

	public static final Block ANT_NEST = nest("ant_nest", () -> ANT);
	public static final Block RED_ANT_NEST = nest("red_ant_nest", () -> RED_ANT);
	public static final Block RAINBOW_ANT_NEST = nest("rainbow_ant_nest", () -> RAINBOW_ANT);
	public static final Block UNSTABLE_ANT_NEST = nest("unstable_ant_nest", () -> UNSTABLE_ANT);
	public static final Block TERMITE_NEST = nest("termite_nest", () -> TERMITE);
	// OreBasicStone: hardness 2.5, resistance 14.
	public static final Block RED_ANT_TROLL_BLOCK = ModBlocks.block("red_ant_troll_block", p -> new TrollBlock(p, () -> RED_ANT),
		rock().strength(2.5F, 14.0F));
	public static final Block TERMITE_TROLL_BLOCK = ModBlocks.block("termite_troll_block", p -> new TrollBlock(p, () -> TERMITE),
		rock().strength(2.5F, 14.0F));
	// RTPBlock: OreSpawn set no hardness, so it breaks instantly.
	public static final Block RANDOM_TELEPORT_BLOCK = ModBlocks.block("random_teleport_block", RandomTeleportBlock::new, rock().instabreak());

	private Portals() {
	}

	public static void init() {
		Registry.register(BuiltInRegistries.FEATURE_TYPE, OreSpawn.id("ant_nests"), AntNestsFeature.CODEC);
		Registry.register(BuiltInRegistries.FEATURE_TYPE, OreSpawn.id("nether_ant_nests"), NetherAntNestsFeature.CODEC);

		FabricDefaultAttributeRegistry.register(ANT, Ant.createAttributes());
		FabricDefaultAttributeRegistry.register(RED_ANT, RedAnt.createAttributes());
		FabricDefaultAttributeRegistry.register(RAINBOW_ANT, Ant.createAttributes());
		FabricDefaultAttributeRegistry.register(UNSTABLE_ANT, Ant.createAttributes());
		FabricDefaultAttributeRegistry.register(TERMITE, Termite.createAttributes());
		for (EntityType<? extends Ant> type : List.of(ANT, RED_ANT, RAINBOW_ANT, UNSTABLE_ANT, TERMITE)) {
			SpawnPlacements.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Ant::checkSpawn);
		}

		// OreSpawnWorld: addAnts(4) on the overworld's surface (last, as OreSpawn's generator ran after the rest),
		// addNetherAnts on the Nether's netherrack, and the troll blocks in the overworld's stone.
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
			ModWorldgen.key("ant_nests"));
		BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(), GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
			ModWorldgen.key("nether_ant_nests"));
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES,
			ModWorldgen.key("red_ant_troll_block"));
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES,
			ModWorldgen.key("termite_troll_block"));
	}

	/** AntBlock: OreSpawn set no hardness (it breaks instantly) and kept Block's default stone sound. */
	private static Block nest(String id, Supplier<? extends EntityType<? extends Mob>> ant) {
		return ModBlocks.block(id, p -> new AntNestBlock(p, ant),
			BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).instabreak().sound(SoundType.STONE).randomTicks());
	}

	/** Material.rock: needs a pickaxe to drop anything. */
	private static BlockBehaviour.Properties rock() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops();
	}
}
