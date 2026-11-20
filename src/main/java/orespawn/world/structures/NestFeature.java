package orespawn.world.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import orespawn.OreSpawn;
import orespawn.world.structures.Build.Loot;

import java.util.List;

/**
 * OreSpawn's forest nests (OreSpawnWorld.addANest): half the time a Bee hive (GenericDungeon.makeSmallBeeHive), a sponge
 * hive held up on a mossy cobblestone stalk with three Bee spawners and a chest inside; otherwise a Mantis hive
 * (makeMantisHive), a stepped pit walled with gold and emerald ore, chests on its steps and three Mantis spawners at
 * the bottom.
 */
public record NestFeature() implements Feature {
	public static final MapCodec<NestFeature> CODEC = MapCodec.unit(NestFeature::new);

	/** GenericDungeon.beeContentsList. */
	static final List<Loot> BEE_LOOT = List.of(Loot.of(Items.SUGAR, 2, 8, 15), Loot.of(Items.DANDELION, 4, 8, 15),
		Loot.of(Items.GOLD_NUGGET, 5, 15, 15), Loot.of(Items.PAPER, 2, 8, 15), Loot.of("fairy_sword", 1, 1, 10),
		Loot.of("pink_tourmailine_helmet", 1, 1, 10), Loot.of("pink_tourmailine_chestplate", 1, 1, 10),
		Loot.of("pink_tourmailine_leggings", 1, 1, 10), Loot.of("pink_tourmailine_boots", 1, 1, 10), Loot.of("butter_candy", 2, 8, 15),
		Loot.of("experience_orb_catcher", 4, 10, 10), Loot.of("bee_spawn_egg", 2, 8, 15));
	/** GenericDungeon.mantisContentsList. */
	static final List<Loot> MANTIS_LOOT = List.of(Loot.of("mantis_claw", 1, 1, 10), Loot.of(Items.GOLD_NUGGET, 4, 8, 15),
		Loot.of("uranium_nugget", 1, 3, 5), Loot.of("titanium_nugget", 1, 3, 5), Loot.of("mantis_spawn_egg", 2, 4, 20),
		Loot.of("tigers_eye_helmet", 1, 1, 10), Loot.of("tigers_eye_chestplate", 1, 1, 10), Loot.of("tigers_eye_leggings", 1, 1, 10),
		Loot.of("tigers_eye_boots", 1, 1, 10), Loot.of(Items.ROTTEN_FLESH, 6, 16, 25), Loot.of(Items.DIAMOND, 1, 3, 15));

	@Override
	public MapCodec<NestFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(final WorldGenLevel level, final ChunkGenerator generator, final RandomSource random, final BlockPos origin) {
		// Five columns, through open air from y 128 down to grass.
		return Build.onSurface(level, random, origin, 5, 128, true, Build.GRASS, (l, r, x, y, z) -> {
			if (r.nextInt(2) == 0) beeHive(l, r, x, y, z);
			else mantisHive(l, r, x, y, z);
		});
	}

	/** GenericDungeon.makeSmallBeeHive; {@code y} is the air block above the grass. */
	static void beeHive(WorldGenLevel level, RandomSource random, int x, int y, int z) {
		int width = 7;
		int height = 21;
		int floor = height * 2 / 3;
		for (int i = -3; i < width + 3; i++) {
			for (int j = floor; j < height; j++) {
				for (int k = -3; k < width + 3; k++) Build.set(level, x + i, y + j, z + k, Blocks.AIR);
			}
		}
		for (int i = 0; i < width; i++) {
			for (int k = 0; k < width; k++) {
				Build.set(level, x + i, y + floor, z + k, Blocks.SPONGE);
				int stalk = random.nextInt(height / 3) * 2 - Math.abs(i - width / 2) - Math.abs(k - width / 2);
				if (stalk < 1) stalk = 1;
				if (i == width / 2 && k == width / 2) stalk = floor;
				for (int n = 0; n < stalk; n++) Build.set(level, x + i, y + floor - n, z + k, Blocks.MOSSY_COBBLESTONE);
			}
		}
		int j = floor;
		for (int layer = 0; layer < height / 6; layer++) {
			j++;
			for (int i = 0; i < width; i++) {
				for (int k = 0; k < width; k++) {
					boolean wall = k == 0 || i == 0 || k == width - 1 || i == width - 1;
					Build.set(level, x + i, y + j, z + k, wall ? Blocks.SPONGE : Blocks.AIR);
				}
			}
			j++;
			for (int i = -1; i < width + 1; i++) {
				for (int k = -1; k < width + 1; k++) {
					boolean wall = k == -1 || i == -1 || k == width || i == width;
					Build.set(level, x + i, y + j, z + k, wall ? Blocks.SPONGE : Blocks.AIR);
				}
			}
		}
		j++;
		for (int i = 0; i < width; i++) {
			for (int k = 0; k < width; k++) Build.set(level, x + i, y + j, z + k, Blocks.SPONGE);
		}
		j = floor + 1;
		for (int i = -1; i < 1; i++) {
			for (int k = 2; k < 4; k++) {
				Build.set(level, x + i, y + j, z + k, Blocks.AIR);
				Build.set(level, x + i, y + j + 1, z + k, Blocks.AIR);
				Build.set(level, x + i, y + j + 2, z + k, Blocks.AIR);
			}
		}
		for (int n = 0; n < 3; n++) Build.spawner(level, random, x + 1, y + n + j, z + 1, OreSpawn.id("bee"));
		Container chest = Build.chest(level, x + width / 2, y + j, z + width / 2, Direction.EAST);
		Build.fill(chest, random, BEE_LOOT, 7 + random.nextInt(5));
	}

	/** GenericDungeon.makeMantisHive; {@code y} is the air block above the grass. */
	static void mantisHive(WorldGenLevel level, RandomSource random, int x, int y, int z) {
		int width = 13;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < 20; j++) {
				for (int k = 0; k < width; k++) Build.set(level, x + i, y + j, z + k, Blocks.AIR);
			}
		}
		int off = 0;
		while (width > 0) {
			for (int i = 0; i < width; i++) {
				for (int k = 0; k < width; k++) {
					Block blk;
					if (k != 0 && k != width - 1 && i != 0 && i != width - 1) blk = Blocks.AIR;
					else blk = (off & 1) != 0 ? Blocks.EMERALD_ORE : Blocks.GOLD_ORE;
					Build.set(level, x + i + off, y - off, z + k + off, blk);
				}
			}
			if (width <= 11 && width >= 7) mantisChests(level, random, x + off, y - off, z + off, width);
			off++;
			width -= 2;
		}
		off--;
		for (int j = 4; j < 7; j++) Build.spawner(level, random, x + off, y + j - off, z + off, OreSpawn.id("mantis"));
	}

	/** GenericDungeon.fill_mantishive_chests: one chest just inside each side of a step, facing in. */
	private static void mantisChests(WorldGenLevel level, RandomSource random, int x, int y, int z, int width) {
		Build.fill(Build.chest(level, x + 1, y, z + width / 2, Direction.EAST), random, MANTIS_LOOT, 3 + random.nextInt(7));
		Build.fill(Build.chest(level, x + width - 2, y, z + width / 2, Direction.WEST), random, MANTIS_LOOT, 3 + random.nextInt(7));
		Build.fill(Build.chest(level, x + width / 2, y, z + 1, Direction.SOUTH), random, MANTIS_LOOT, 3 + random.nextInt(7));
		Build.fill(Build.chest(level, x + width / 2, y, z + width - 2, Direction.NORTH), random, MANTIS_LOOT, 3 + random.nextInt(7));
	}
}
