package orespawn.content;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import orespawn.block.EggOreBlock;
import orespawn.block.leftovers.DayNightFlowerBlock;
import orespawn.block.leftovers.LavaFoamBlock;
import orespawn.registry.ModBlocks;
import orespawn.world.ModWorldgen;

/**
 * The last OreSpawn blocks ({@code OreSpawnMain}): the four day-and-night flowers, the Ender-Pearl and Eye-of-Ender
 * blocks, and Lava Foam. Their models, drops, recipes, names and the Lava Foam veins are in
 * {@code tools/content/leftovers.json}.
 * <ul>
 * <li>Pink, Blue, Black and Dead Flowers ({@code MyBlockFlower}: Material.plants, no hardness, grass sound). OreSpawn
 * never generated them in the overworld: they come from the Greenhouse dungeon, floating islands, the Sifter and
 * dungeon chests, and had no recipes.</li>
 * <li>Ender-Pearl Block and Eye-of-Ender Block ({@code OreGenericEgg}: Material.ground, hardness 0.5, resistance 1,
 * gravel sound; drop themselves and half the time 5-9 experience, like the egg ores). Nine ender pearls or eyes of ender
 * make one, and one gives the nine back. OreSpawn placed them only in structures (Ender Castle, floating islands) and
 * dungeon chests; an Extreme Torch on an Eye-of-Ender Block is what the Torches group checks for.</li>
 * <li>Lava Foam ({@code Lavafoam}: Material.rock, hardness 5, resistance 5, slipperiness 1.1). OreSpawnWorld.generateNether
 * put 15-24 veins of 6 in netherrack at y 10-117 in every Nether chunk.</li>
 * </ul>
 */
public final class Leftovers {
	public static final Block PINK_FLOWER = ModBlocks.block("pink_flower",
		p -> new DayNightFlowerBlock(p, false, () -> Leftovers.BLACK_FLOWER), flower());
	public static final Block BLUE_FLOWER = ModBlocks.block("blue_flower",
		p -> new DayNightFlowerBlock(p, false, () -> Leftovers.DEAD_FLOWER), flower());
	public static final Block BLACK_FLOWER = ModBlocks.block("black_flower",
		p -> new DayNightFlowerBlock(p, true, () -> Leftovers.PINK_FLOWER), flower());
	public static final Block DEAD_FLOWER = ModBlocks.block("dead_flower",
		p -> new DayNightFlowerBlock(p, true, () -> Leftovers.BLUE_FLOWER), flower());

	public static final Block ENDER_PEARL_BLOCK = ModBlocks.block("ender_pearl_block", EggOreBlock::new, genericEgg());
	public static final Block EYE_OF_ENDER_BLOCK = ModBlocks.block("eye_of_ender_block", EggOreBlock::new, genericEgg());

	public static final Block LAVA_FOAM = ModBlocks.block("lava_foam", LavaFoamBlock::new, BlockBehaviour.Properties.of()
		.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 5.0F)
		.friction(1.1F).isRedstoneConductor((s, l, p) -> true).isSuffocating((s, l, p) -> true));

	private Leftovers() {
	}

	public static void init() {
		BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(), GenerationStep.Decoration.UNDERGROUND_ORES,
			ModWorldgen.key("lava_foam"));
	}

	/** MyBlockFlower: Material.plants (destroyed by pistons), no hardness, no collision, grass sound, random ticks. */
	private static BlockBehaviour.Properties flower() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS)
			.pushReaction(PushReaction.POPPED).randomTicks();
	}

	/** OreGenericEgg: Material.ground, hardness 0.5, resistance 1, gravel sound. */
	private static BlockBehaviour.Properties genericEgg() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F, 1.0F).sound(SoundType.GRAVEL);
	}
}
