package orespawn.world.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import orespawn.OreSpawn;
import orespawn.world.structures.Build.Loot;

import java.util.List;

/**
 * OreSpawn's bouncy castle (OreSpawnWorld.addBouncyCastle, GenericDungeon.makeBouncyCastle): a 9 x 9 box of bouncy Lava
 * Foam in the desert with red terracotta corners (1.7.10's stained clay 14) and a doorway on its north side; inside, three
 * spawners (Silverfish, Rat, Scorpion) on each of three walls and a chest.
 * <p>
 * Not ported yet: Lava Foam belongs to another group; until it's registered the castle isn't built (it would be only its
 * corners).
 */
public record BouncyCastleFeature() implements Feature {
	public static final MapCodec<BouncyCastleFeature> CODEC = MapCodec.unit(BouncyCastleFeature::new);

	/** GenericDungeon.BouncyContentsList. */
	static final List<Loot> LOOT = List.of(Loot.of(Items.ROTTEN_FLESH, 6, 16, 35), Loot.of(Items.COD, 6, 16, 25),
		Loot.of(Items.BONE, 6, 16, 25), Loot.of(Items.STRING, 6, 16, 25), Loot.of(Items.POPPY, 6, 16, 25),
		Loot.of(Items.DANDELION, 6, 16, 25), Loot.of(Items.ENDER_PEARL, 2, 4, 20));

	@Override
	public MapCodec<BouncyCastleFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(final WorldGenLevel level, final ChunkGenerator generator, final RandomSource random, final BlockPos origin) {
		if (Build.block("lava_foam").isAir()) return false;
		return Build.onSurface(level, random, origin, 4, 100, false, Build.SAND, (l, r, x, y, z) -> build(l, r, x, y - 1, z));
	}

	/** {@code y} is the sand block. */
	static void build(WorldGenLevel level, RandomSource random, int x, int y, int z) {
		BlockState foam = Build.block("lava_foam");
		int size = 4;
		int height = 5;
		for (int i = -size; i <= size; i++) {
			for (int j = -size; j <= size; j++) {
				for (int k = 0; k < height; k++) {
					BlockState blk = Build.AIR;
					if (k == height - 1 || k == 0) blk = foam;
					if (i == -size || i == size) blk = foam;
					if (j == -size || j == size) blk = foam;
					if ((i == -size || i == size) && (j == -size || j == size)) blk = Blocks.DYED_TERRACOTTA.red().defaultBlockState();
					if ((k == 1 || k == 2) && i == 0 && j == -size) blk = Build.AIR;
					Build.set(level, x + i, y + k, z + j, blk);
				}
			}
		}
		Identifier silverfish = Identifier.withDefaultNamespace("silverfish");
		Identifier rat = OreSpawn.id("rat");
		Identifier scorpion = OreSpawn.id("scorpion");
		Build.spawner(level, random, x - 1, y + 3, z + size - 1, silverfish);
		Build.spawner(level, random, x, y + 3, z + size - 1, rat);
		Build.spawner(level, random, x + 1, y + 3, z + size - 1, scorpion);
		Build.spawner(level, random, x + size - 1, y + 3, z - 1, silverfish);
		Build.spawner(level, random, x + size - 1, y + 3, z, rat);
		Build.spawner(level, random, x + size - 1, y + 3, z + 1, scorpion);
		Build.spawner(level, random, x - size + 1, y + 3, z - 1, silverfish);
		Build.spawner(level, random, x - size + 1, y + 3, z, rat);
		Build.spawner(level, random, x - size + 1, y + 3, z + 1, scorpion);
		Build.fill(Build.chest(level, x + size - 1, y + 3, z + size - 1), random, LOOT, 6 + random.nextInt(5));
	}
}
