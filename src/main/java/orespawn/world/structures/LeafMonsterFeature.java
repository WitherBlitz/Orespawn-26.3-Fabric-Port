package orespawn.world.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import orespawn.OreSpawn;
import orespawn.world.structures.Build.Loot;

import java.util.List;

/**
 * OreSpawn's Leaf Monster tree (OreSpawnWorld.addLeafMonster, GenericDungeon.makeLeafMonsterDungeon): a hollow 4 x 4 log
 * trunk with a ladder up to a leaf-walled platform ten blocks up, a Leaf Monster spawner in each corner, a chest, and a
 * leafy crown.
 */
public record LeafMonsterFeature() implements Feature {
	public static final MapCodec<LeafMonsterFeature> CODEC = MapCodec.unit(LeafMonsterFeature::new);

	/** GenericDungeon.LeafMonsterContentsList (flower pots and saplings are in it twice). */
	static final List<Loot> LOOT = List.of(Loot.of(Items.FLOWER_POT, 6, 16, 35), Loot.of(Items.OAK_SAPLING, 6, 16, 35),
		Loot.of(Items.FLOWER_POT, 6, 16, 35), Loot.of(Items.OAK_SAPLING, 6, 16, 35), Loot.of(Items.OAK_LEAVES, 6, 16, 25),
		Loot.of(Items.DIRT, 6, 16, 25), Loot.of(Items.OAK_LOG, 6, 16, 25), Loot.of("poison_sword", 1, 1, 15),
		Loot.of(Items.ROTTEN_FLESH, 6, 16, 25));

	@Override
	public MapCodec<LeafMonsterFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(final WorldGenLevel level, final ChunkGenerator generator, final RandomSource random, final BlockPos origin) {
		return Build.onSurface(level, random, origin, 4, 100, false, Build.GRASS, LeafMonsterFeature::build);
	}

	/** {@code y} is the air block above the grass. */
	static void build(WorldGenLevel level, RandomSource random, int x, int y, int z) {
		for (int i = -2; i < 6; i++) {
			for (int k = -3; k < 2; k++) {
				for (int j = 0; j < 4; j++) Build.set(level, x + i, y + j, z + k, Blocks.AIR);
			}
		}
		// Roots: logs fill any air or grass plants under the trunk.
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int i = 0; i < 4; i++) {
			for (int k = 0; k < 4; k++) {
				for (int j = -1; j > -5; j--) {
					BlockState below = level.getBlockState(pos.set(x + i, y + j, z + k));
					if (below.isAir() || below.is(Blocks.SHORT_GRASS) || below.is(Blocks.FERN)) Build.set(level, x + i, y + j, z + k, Blocks.OAK_LOG);
				}
			}
		}
		for (int i = 0; i < 4; i++) {
			for (int k = 0; k < 4; k++) {
				for (int j = 0; j < 10; j++) {
					boolean middle = i == 1 || i == 2;
					BlockState blk = Blocks.OAK_LOG.defaultBlockState();
					if (j < 2 && (k == 0 || k == 1) && middle) blk = Build.AIR;
					if (k == 1 && middle) blk = Build.AIR;
					if (k != 2 || !middle) Build.set(level, x + i, y + j, z + k, blk);
				}
			}
		}
		BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH);
		for (int i = 1; i <= 2; i++) {
			for (int j = 0; j < 10; j++) Build.set(level, x + i, y + j, z + 2, ladder);
		}
		Build.set(level, x + 1, y + 2, z - 1, Build.LEAVES);
		Build.set(level, x + 2, y + 2, z - 1, Build.LEAVES);
		for (int i = -3; i < 7; i++) {
			for (int k = -3; k < 7; k++) {
				if (i < 0 || i > 3 || k < 0 || k > 3) {
					boolean edge = i == -3 || i == 6 || k == -3 || k == 6;
					Build.set(level, x + i, y + 9, z + k, edge ? Build.LEAVES : Blocks.OAK_LOG.defaultBlockState());
				}
			}
		}
		for (int i = -3; i < 7; i++) {
			for (int k = -3; k < 7; k++) {
				boolean edge = i == -3 || i == 6 || k == -3 || k == 6;
				for (int j = 10; j < 13; j++) Build.set(level, x + i, y + j, z + k, edge ? Build.LEAVES : Build.AIR);
			}
		}
		for (int i = -2; i < 6; i++) {
			for (int k = -2; k < 6; k++) {
				BlockState blk = Build.AIR;
				if (i == -2 || i == 5 || k == -2 || k == 5) blk = Blocks.OAK_LOG.defaultBlockState();
				if (i == -1 || i == 4 || k == -1 || k == 4) blk = Build.LEAVES;
				Build.set(level, x + i, y + 13, z + k, blk);
			}
		}
		for (int i = -1; i < 5; i++) {
			for (int k = -1; k < 5; k++) Build.set(level, x + i, y + 14, z + k, Build.LEAVES);
		}
		for (int i = 0; i < 4; i++) {
			for (int k = 0; k < 4; k++) Build.set(level, x + i, y + 15, z + k, Blocks.OAK_LOG);
		}
		for (int i = 1; i < 3; i++) {
			for (int k = 1; k < 3; k++) Build.set(level, x + i, y + 16, z + k, Build.LEAVES);
		}
		Build.spawner(level, random, x - 2, y + 10, z - 2, OreSpawn.id("leaf_monster"));
		Build.spawner(level, random, x + 5, y + 10, z + 5, OreSpawn.id("leaf_monster"));
		Build.spawner(level, random, x - 2, y + 10, z + 5, OreSpawn.id("leaf_monster"));
		Build.spawner(level, random, x + 5, y + 10, z - 2, OreSpawn.id("leaf_monster"));
		Container chest = Build.doubleChest(level, x + 1, y + 10, z + 5, true);
		Build.fill(chest, random, LOOT, 12 + random.nextInt(5));
	}
}
