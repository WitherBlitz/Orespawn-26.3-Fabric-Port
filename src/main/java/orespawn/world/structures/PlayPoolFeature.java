package orespawn.world.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import orespawn.OreSpawn;
import orespawn.world.structures.Build.Loot;

import java.util.List;

/**
 * OreSpawn's Attack Squid play pool (OreSpawnWorld.addPlayPool, GenericDungeon.makePlayPool): over the ocean, a row of
 * four Attack Squid spawners 16 blocks up with a chest on them and water on top that spills off both ends.
 */
public record PlayPoolFeature() implements Feature {
	public static final MapCodec<PlayPoolFeature> CODEC = MapCodec.unit(PlayPoolFeature::new);

	/** GenericDungeon.SquidContentsList. */
	static final List<Loot> LOOT = List.of(Loot.of(Items.INK_SAC, 6, 16, 25), Loot.of("squidzooka", 1, 1, 15),
		Loot.of(Items.GOLD_NUGGET, 5, 15, 15), Loot.of(Items.ROTTEN_FLESH, 6, 16, 25));

	@Override
	public MapCodec<PlayPoolFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(final WorldGenLevel level, final ChunkGenerator generator, final RandomSource random, final BlockPos origin) {
		// The air block above the ocean's surface.
		return Build.onSurface(level, random, origin, 4, 100, false, Build.WATER, PlayPoolFeature::build);
	}

	static void build(WorldGenLevel level, RandomSource random, int x, int y, int z) {
		for (int i = 0; i < 4; i++) Build.spawner(level, random, x + i, y + 16, z, OreSpawn.id("attack_squid"));
		Container chest = Build.doubleChest(level, x + 1, y + 17, z, true);
		Build.fill(chest, random, LOOT, 3 + random.nextInt(5));
		for (int i = 0; i < 4; i++) Build.flowingWater(level, x + i, y + 18, z);
		Build.flowingWater(level, x - 1, y + 18, z);
		Build.flowingWater(level, x + 4, y + 18, z);
	}
}
