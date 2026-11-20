package orespawn.entity.tools;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import orespawn.content.Tools;

/**
 * OreSpawn's {@code OreSpawnWorld.addRocks}: in one chunk in five, 3-12 rocks lie on the surface. Each picks a spot in
 * the chunk, looks down from y 110 to 41 through air, and is put on the first grass or sand it finds (OreSpawn's
 * Crystal Grass too, once that block exists). The rocks choose their kind on their first tick ({@link RockBase}).
 * Placed once per chunk (no count or spread modifiers; a biome filter is enough).
 */
public record RockFeature() implements Feature {
	public static final MapCodec<RockFeature> CODEC = MapCodec.unit(RockFeature::new);

	@Override
	public MapCodec<RockFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		if (random.nextInt(5) != 0) return false;
		int howMany = 3 + random.nextInt(10);
		boolean placed = false;
		Block crystalGrass = Tools.lazyBlock("crystal_grass");
		for (int i = 0; i < howMany; i++) {
			int x = origin.getX() + random.nextInt(16);
			int z = origin.getZ() + random.nextInt(16);
			for (int y = 110; y > 40 && level.getBlockState(new BlockPos(x, y, z)).isAir(); y--) {
				BlockState below = level.getBlockState(new BlockPos(x, y - 1, z));
				if (below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.SAND) || below.is(Blocks.RED_SAND) || crystalGrass != null && below.is(crystalGrass)) {
					RockBase rock = Tools.ROCK.create(level.getLevel(), EntitySpawnReason.CHUNK_GENERATION);
					if (rock != null) {
						rock.snapTo(x + 0.5, y + 0.01, z + 0.5, random.nextFloat() * 360.0F, 0.0F);
						level.addFreshEntity(rock);
						placed = true;
					}
					break;
				}
			}
		}
		return placed;
	}
}
