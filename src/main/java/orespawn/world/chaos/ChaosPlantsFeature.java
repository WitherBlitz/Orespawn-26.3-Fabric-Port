package orespawn.world.chaos;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Feature type {@code orespawn:chaos_plants}: one try of OreSpawnWorld's addButterfliesAndMoths ({@code "critters"}) or
 * addVeggies ({@code "veggies"}) in its Chaos branch, at a spot {@link SurfaceScanPlacement} found (air over grass).
 * <ul>
 * <li>critters: a butterfly, moth or firefly plant, one of the three at random.</li>
 * <li>veggies: one in six each carrots, potatoes, a radish plant or a lettuce plant; one in sixty a melon stem; one in
 * three hundred a block of Duplicator Tree Wood (OreSpawn's enableduplicatortree defaults to on).</li>
 * </ul>
 * Everything is planted as it starts (age 0), on the grass itself, as OreSpawn did. OreSpawn's own plants (Farming
 * group) are looked up by id when placed; if one isn't registered, wheat (or an oak log for the duplicator wood) goes
 * there instead.
 */
public record ChaosPlantsFeature(Plants plants) implements Feature {
	public static final MapCodec<ChaosPlantsFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Plants.CODEC.fieldOf("plants").forGetter(ChaosPlantsFeature::plants)).apply(i, ChaosPlantsFeature::new));

	public enum Plants implements StringRepresentable {
		CRITTERS("critters"),
		VEGGIES("veggies");

		public static final StringRepresentable.EnumCodec<Plants> CODEC = StringRepresentable.fromEnum(Plants::values);
		private final String name;

		Plants(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}

	@Override
	public MapCodec<ChaosPlantsFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		if (!level.isEmptyBlock(origin) || !level.getBlockState(origin.below()).is(Blocks.GRASS_BLOCK)) {
			return false;
		}
		BlockState state = switch (this.plants) {
			case CRITTERS -> switch (random.nextInt(3)) {
				case 0 -> orespawn(Blocks.WHEAT, "butterfly_plant");
				case 1 -> orespawn(Blocks.WHEAT, "moth_plant");
				default -> orespawn(Blocks.WHEAT, "firefly_plant");
			};
			case VEGGIES -> switch (random.nextInt(6)) {
				case 0 -> Blocks.CARROTS.defaultBlockState();
				case 1 -> Blocks.POTATOES.defaultBlockState();
				case 2 -> orespawn(Blocks.WHEAT, "radish_plant");
				case 3 -> orespawn(Blocks.WHEAT, "lettuce_plant");
				case 4 -> random.nextInt(10) == 0 ? Blocks.MELON_STEM.defaultBlockState() : null;
				default -> random.nextInt(50) == 1 ? orespawn(Blocks.OAK_LOG, "duplicator_tree_wood", "duplicator_tree_log") : null;
			};
		};
		if (state == null) {
			return false;
		}
		level.setBlock(origin, state, Block.UPDATE_CLIENTS);
		return true;
	}

	/** The first of OreSpawn's blocks by these ids that's registered, else the vanilla fallback. */
	private static BlockState orespawn(Block fallback, String... ids) {
		for (String id : ids) {
			Identifier key = Identifier.fromNamespaceAndPath("orespawn", id);
			if (BuiltInRegistries.BLOCK.containsKey(key)) {
				return BuiltInRegistries.BLOCK.getValue(key).defaultBlockState();
			}
		}
		return fallback.defaultBlockState();
	}
}
