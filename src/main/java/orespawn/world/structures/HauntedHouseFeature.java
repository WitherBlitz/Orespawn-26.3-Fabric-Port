package orespawn.world.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import orespawn.OreSpawn;
import orespawn.registry.ModBlocks;

/**
 * OreSpawn's haunted house (OreSpawnWorld.addHauntedHouse, GenericDungeon.makeHauntedHouse): a 7 x 7 plank hut with a
 * cobblestone floor, a glass band under the roof and a doorway on its east side; inside, a furnace, a crafting table, a
 * chest of survival gear and a stack of three spawners: Rat, Ghost and Ghost Pumpkin Skelly.
 */
public record HauntedHouseFeature() implements Feature {
	public static final MapCodec<HauntedHouseFeature> CODEC = MapCodec.unit(HauntedHouseFeature::new);

	@Override
	public MapCodec<HauntedHouseFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(final WorldGenLevel level, final ChunkGenerator generator, final RandomSource random, final BlockPos origin) {
		// Five columns, through open air from y 100 down to grass.
		return Build.onSurface(level, random, origin, 5, 100, true, Build.GRASS, HauntedHouseFeature::build);
	}

	/** {@code y} is the air block above the grass. */
	static void build(WorldGenLevel level, RandomSource random, int x, int y, int z) {
		int size = 3;
		int height = 3;
		for (int i = -size; i <= size; i++) {
			for (int j = -size; j <= size; j++) {
				for (int k = 0; k <= height + 1; k++) {
					Block blk;
					if (k == height + 1) blk = Blocks.OAK_PLANKS;
					else if (k == 0) blk = Blocks.COBBLESTONE;
					else if (i != size && j != size && i != -size && j != -size) blk = Blocks.AIR;
					else if (k == height) blk = Blocks.GLASS;
					else if ((k == 1 || k == 2) && i == size && j == 0) blk = Blocks.AIR;
					else blk = Blocks.OAK_PLANKS;
					Build.set(level, x + i, y + k, z + j, blk);
				}
			}
		}
		Build.set(level, x + 2, y + 1, z + 2, Blocks.FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, Direction.NORTH));
		Build.set(level, x + 1, y + 1, z + 2, Blocks.CRAFTING_TABLE);
		Container chest = Build.chest(level, x, y + 1, z + 2, Direction.NORTH);
		survivalKit(chest, random, true);
		Build.spawner(level, random, x, y + 1, z, OreSpawn.id("rat"));
		Build.spawner(level, random, x, y + 2, z, OreSpawn.id("ghost"));
		Build.spawner(level, random, x, y + 3, z, OreSpawn.id("ghost_pumpkin_skelly"));
	}

	/** The haunted house's fixed chest slots 0-13, each filled half the time; the igloo's has no salt. */
	static void survivalKit(Container chest, RandomSource random, boolean salt) {
		Build.maybe(chest, random, 0, new ItemStack(Items.COMPASS));
		Build.maybe(chest, random, 1, new ItemStack(Items.MAP));
		Build.maybe(chest, random, 2, new ItemStack(Items.COOKED_PORKCHOP, 8));
		Build.maybe(chest, random, 3, new ItemStack(Items.TORCH, 32));
		Build.maybe(chest, random, 4, new ItemStack(Items.COAL, 16));
		Build.maybe(chest, random, 5, new ItemStack(Items.BED.red()));
		Build.maybe(chest, random, 6, new ItemStack(Items.BED.red()));
		Build.maybe(chest, random, 7, new ItemStack(Items.OAK_DOOR));
		Build.maybe(chest, random, 8, new ItemStack(Items.IRON_PICKAXE));
		Build.maybe(chest, random, 9, new ItemStack(Items.IRON_SWORD));
		Build.maybe(chest, random, 10, new ItemStack(Items.IRON_AXE));
		Build.maybe(chest, random, 11, new ItemStack(Items.BUCKET));
		if (salt) Build.maybe(chest, random, 12, new ItemStack(ModBlocks.SALT_ORE, 4));
		Build.maybe(chest, random, 13, new ItemStack(Items.CHEST));
	}
}
