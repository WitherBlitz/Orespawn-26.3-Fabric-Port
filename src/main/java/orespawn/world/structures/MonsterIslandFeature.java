package orespawn.world.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import orespawn.OreSpawn;
import orespawn.world.structures.Build.Loot;

import java.util.List;

/**
 * OreSpawn's Monster Island (OreSpawnWorld.addMonsterIsland, GenericDungeon.makeMonsterIsland): a small sand island in
 * the ocean with a leafy log, four Sea Viper or (half the time) Sea Monster spawners, and two chests.
 */
public record MonsterIslandFeature() implements Feature {
	public static final MapCodec<MonsterIslandFeature> CODEC = MapCodec.unit(MonsterIslandFeature::new);

	/** GenericDungeon.MonsterIslandContentsList. */
	static final List<Loot> LOOT = List.of(Loot.of("creeper_repellent", 4, 10, 35), Loot.of("kraken_repellent", 4, 10, 35),
		Loot.of(Items.INK_SAC, 6, 16, 25), Loot.of(Items.BONE, 6, 16, 25), Loot.of(Items.STRING, 6, 16, 25),
		Loot.of(Items.PORKCHOP, 3, 10, 35), Loot.of(Items.BEEF, 3, 10, 35), Loot.of(Items.CHICKEN, 3, 10, 35),
		Loot.of(Items.COD, 3, 10, 35), Loot.of(Items.ROTTEN_FLESH, 3, 10, 35), Loot.of(Items.EXPERIENCE_BOTTLE, 4, 10, 35),
		Loot.of("raw_bacon", 6, 16, 35), Loot.of("raw_peacock", 6, 16, 35), Loot.of(Items.OAK_LOG, 6, 16, 25));

	@Override
	public MapCodec<MonsterIslandFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(final WorldGenLevel level, final ChunkGenerator generator, final RandomSource random, final BlockPos origin) {
		return Build.onSurface(level, random, origin, 4, 100, false, Build.WATER, (l, r, x, y, z) -> build(l, r, x, y - 1, z));
	}

	/** {@code y} is the ocean's top water block. */
	static void build(WorldGenLevel level, RandomSource random, int x, int y, int z) {
		String monster = random.nextInt(2) == 0 ? "sea_monster" : "sea_viper";
		for (int i = -5; i <= 5; i++) {
			int k = 3;
			if (i == -5 || i == 5) k = 1;
			if (i == -4 || i == 4 || i == -3 || i == 3) k = 2;
			for (int j = -k; j <= k; j++) {
				Build.set(level, x + i, y, z + j, Blocks.SAND);
				Build.set(level, x + i, y - 1, z + j, Blocks.STONE);
			}
		}
		WaterDragonLairFeature.tree(level, random, x, y, z, OreSpawn.id(monster));
		Build.fill(Build.chest(level, x, y + 1, z - 1), random, LOOT, 4 + random.nextInt(5));
		Build.fill(Build.chest(level, x, y + 1, z + 1), random, LOOT, 4 + random.nextInt(5));
	}
}
