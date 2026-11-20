package orespawn.world.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import orespawn.OreSpawn;

import java.util.function.Predicate;

/**
 * OreSpawn's igloo (OreSpawnWorld.addIgloo, GenericDungeon.makeIgloo): a round hut of snow and ice rings with an oak
 * door on its west side, Rat, Ghost and Ghost Pumpkin Skelly spawners inside, and a chest of survival gear.
 * <p>
 * OreSpawn looked for a snow block under open air, but 1.7.10's Ice Plains are grass under a snow layer, so its igloo
 * hardly ever appeared; a snow layer counts as well here (26.3's snowy plains are the same).
 */
public record IglooFeature() implements Feature {
	public static final MapCodec<IglooFeature> CODEC = MapCodec.unit(IglooFeature::new);

	static final Predicate<BlockState> SNOW = s -> s.is(Blocks.SNOW_BLOCK) || s.is(Blocks.SNOW);

	@Override
	public MapCodec<IglooFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(final WorldGenLevel level, final ChunkGenerator generator, final RandomSource random, final BlockPos origin) {
		return Build.onSurface(level, random, origin, 4, 100, false, SNOW, (l, r, x, y, z) -> build(l, r, x, y - 2, z));
	}

	/** {@code y} is one below the snow. Casts and float steps are OreSpawn's, so the rings match. */
	static void build(WorldGenLevel level, RandomSource random, int x, int y, int z) {
		for (float deg = 0.0F; deg < 360.0F; deg += 5.0F) {
			int px = ringX(x, 6.0F, deg);
			int pz = ringZ(z, 6.0F, deg);
			Build.set(level, px, y + 1, pz, Blocks.SNOW_BLOCK);
			Build.set(level, px, y + 2, pz, Blocks.ICE);
			Build.set(level, px, y + 3, pz, Blocks.SNOW_BLOCK);
		}
		ring(level, x, y + 4, z, 5.0F, 5.0F, Blocks.ICE);
		ring(level, x, y + 5, z, 4.0F, 5.0F, Blocks.SNOW_BLOCK);
		ring(level, x, y + 5, z, 3.0F, 10.0F, Blocks.ICE);
		ring(level, x, y + 5, z, 2.0F, 15.0F, Blocks.SNOW_BLOCK);
		ring(level, x, y + 5, z, 1.0F, 15.0F, Blocks.ICE);
		int doorX = (int) (x - 6.0F + 0.5F);
		int doorZ = (int) (z + 0.5F);
		Build.set(level, doorX, y, doorZ, Blocks.OAK_PLANKS);
		Build.set(level, doorX, y + 1, doorZ, Blocks.AIR);
		Build.set(level, doorX, y + 2, doorZ, Blocks.AIR);
		// ItemDoor.placeDoorBlock with rotation 2: facing west, hinge on the left.
		BlockState door = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST);
		Build.set(level, doorX, y + 1, doorZ, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
		Build.set(level, doorX, y + 2, doorZ, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
		Build.spawner(level, random, x + 2, y + 1, z - 4, OreSpawn.id("rat"));
		Build.spawner(level, random, x - 1, y + 1, z + 1, OreSpawn.id("ghost"));
		Build.spawner(level, random, x + 3, y + 1, z + 4, OreSpawn.id("ghost_pumpkin_skelly"));
		Container chest = Build.chest(level, x - 3, y + 1, z - 3);
		HauntedHouseFeature.survivalKit(chest, random, false);
		Build.maybe(chest, random, 14, new ItemStack(Items.GOLD_NUGGET, 6));
		Build.maybe(chest, random, 15, new ItemStack(Items.GOLD_NUGGET, 8));
		Build.maybe(chest, random, 16, new ItemStack(Items.GOLD_NUGGET, 10));
	}

	private static void ring(WorldGenLevel level, int x, int y, int z, float radius, float step, Block block) {
		for (float deg = 0.0F; deg < 360.0F; deg += step) Build.set(level, ringX(x, radius, deg), y, ringZ(z, radius, deg), block);
	}

	private static int ringX(int x, float radius, float deg) {
		return (int) (x + (float) (radius * Math.cos(Math.toRadians(deg))) + 0.5F);
	}

	private static int ringZ(int z, float radius, float deg) {
		return (int) (z + (float) (radius * Math.sin(Math.toRadians(deg))) + 0.5F);
	}
}
