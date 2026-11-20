package orespawn.world.dungeons;

import orespawn.original.OriginalDungeons;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * OreSpawn's dungeons in the End (OreSpawnWorld.generateEnd), once per chunk: one of four rolled (1 in 4 each), each
 * then 1 in 25 (the Ender castle 1 in 50): the Ender Knight dungeon, the Ender Reaper graveyard, the Ender Dragon
 * hospital or the Ender castle, on end stone with room above it. The End's ants (also in generateEnd) are the Bugs
 * group's. Placed after the rest of the chunk's decoration, as OreSpawn's world generator ran after vanilla's.
 */
public record EndDungeonsFeature() implements Feature {
	public static final MapCodec<EndDungeonsFeature> CODEC = MapCodec.unit(EndDungeonsFeature::new);

	@Override
	public MapCodec<EndDungeonsFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int chunkX = origin.getX() & ~15;
		int chunkZ = origin.getZ() & ~15;
		return McWorld.run(level, random, w -> {
			int i = w.rand.nextInt(4);
			if (i == 0) {
				OriginalDungeons.place("addEndKnights", w, w.rand, chunkX, chunkZ);
			}
			if (i == 1) {
				OriginalDungeons.place("addEndReapers", w, w.rand, chunkX, chunkZ);
			}
			if (i == 2) {
				OriginalDungeons.place("addHospital", w, w.rand, chunkX, chunkZ);
			}
			if (i == 3) {
				OriginalDungeons.place("addEnderCastle", w, w.rand, chunkX, chunkZ);
			}
			return true;
		});
	}
}
