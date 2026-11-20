package orespawn.world.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import orespawn.OreSpawn;
import orespawn.registry.ModItems;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * What OreSpawn's overworld structures share: OreSpawnWorld's search for a spot (its add* methods) and GenericDungeon's
 * block, spawner and chest helpers. OreSpawn's own blocks, items and creatures are looked up by id when a structure is
 * built, since other content groups own them: a missing item leaves its chest slot empty, a missing creature leaves its
 * spawner empty.
 */
final class Build {
	/** OreSpawn built with setBlockFast / setBlock flag 2: no neighbour updates. */
	private static final int FLAGS = Block.UPDATE_CLIENTS;

	static final BlockState AIR = Blocks.AIR.defaultBlockState();
	/** OreSpawn's leaves (meta 0) never decayed unless a nearby log was broken; persistent is the closest 26.3 state. */
	static final BlockState LEAVES = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);

	static final Predicate<BlockState> WATER = s -> s.is(Blocks.WATER);
	static final Predicate<BlockState> GRASS = s -> s.is(Blocks.GRASS_BLOCK);
	/** 1.7.10's Blocks.sand, both its sand and red sand. */
	static final Predicate<BlockState> SAND = s -> s.is(Blocks.SAND) || s.is(Blocks.RED_SAND);

	private Build() {
	}

	/** Builds a structure at the spot found ({@code y} is the air block above the ground block). */
	@FunctionalInterface
	interface Builder {
		void build(WorldGenLevel level, RandomSource random, int x, int y, int z);
	}

	/**
	 * OreSpawnWorld's add* search: up to {@code tries} random columns of the chunk, each scanned down from {@code top} to
	 * y 41 for an air block with {@code ground} under it. With {@code airOnly} the scan stops at the first block that
	 * isn't air (its "posY > 40 && isAirBlock" loops); otherwise it carries on through solid blocks, but no lower than the
	 * ground's surface: 26.3's caves and aquifers often have air over water or sand, which would put a pond or a castle
	 * underground where OreSpawn's terrain rarely did. Where 26.3's terrain rises above {@code top}, the scan starts at
	 * the surface instead, so the same spots are found.
	 */
	static boolean onSurface(WorldGenLevel level, RandomSource random, BlockPos origin, int tries, int top, boolean airOnly,
			Predicate<BlockState> ground, Builder builder) {
		for (int i = 0; i < tries; i++) {
			int x = origin.getX() + random.nextInt(16);
			int z = origin.getZ() + random.nextInt(16);
			int y = surface(level, x, z, top, airOnly, ground);
			if (y != Integer.MIN_VALUE) {
				builder.build(level, random, x, y, z);
				return true;
			}
		}
		return false;
	}

	/** The scan of {@link #onSurface} for one column: the air block's y, or {@code Integer.MIN_VALUE}. */
	static int surface(WorldGenLevel level, int x, int z, int top, boolean airOnly, Predicate<BlockState> ground) {
		int start = Math.max(top, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z));
		// The lowest air block that can be on the surface sits on the top solid block or water (leaves don't count, so
		// spots under a tree's crown are still found, as OreSpawn found them).
		int bottom = Math.max(40, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int y = start; y > bottom; y--) {
			boolean air = level.getBlockState(pos.set(x, y, z)).isAir();
			if (airOnly && !air) break;
			if (air && ground.test(level.getBlockState(pos.set(x, y - 1, z)))) return y;
		}
		return Integer.MIN_VALUE;
	}

	static void set(WorldGenLevel level, int x, int y, int z, BlockState state) {
		level.setBlock(new BlockPos(x, y, z), state, FLAGS);
	}

	static void set(WorldGenLevel level, int x, int y, int z, Block block) {
		set(level, x, y, z, block.defaultBlockState());
	}

	/** Water OreSpawn placed with block updates (flowing_water, or water with flag 3): it spreads. */
	static void flowingWater(WorldGenLevel level, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		if (level.setBlock(pos, Blocks.WATER.defaultBlockState(), FLAGS)) level.scheduleTick(pos, Fluids.WATER, 0);
	}

	/** An OreSpawn block by id, or air while the group that owns it isn't ported. */
	static BlockState block(String id) {
		return BuiltInRegistries.BLOCK.getValue(OreSpawn.id(id)).defaultBlockState();
	}

	/** A mob spawner for {@code mob}; it stays empty if that creature isn't registered (not ported yet). */
	static void spawner(WorldGenLevel level, RandomSource random, int x, int y, int z, Identifier mob) {
		BlockPos pos = new BlockPos(x, y, z);
		level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), FLAGS);
		Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(mob);
		if (type.isPresent() && level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
			spawner.setEntityId(type.get(), random);
		}
	}

	/** A single chest facing the way 1.7.10's BlockChest.onBlockAdded turned it: away from a lone opaque neighbour. */
	static @Nullable Container chest(WorldGenLevel level, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		boolean n = opaque(level, pos.north()), s = opaque(level, pos.south()), w = opaque(level, pos.west()), e = opaque(level, pos.east());
		Direction facing = Direction.SOUTH;
		if (s && !n) facing = Direction.NORTH;
		if (w && !e) facing = Direction.EAST;
		if (e && !w) facing = Direction.WEST;
		return chest(level, x, y, z, facing);
	}

	/** A single chest OreSpawn turned itself (setBlockMetadata 2-5). */
	static @Nullable Container chest(WorldGenLevel level, int x, int y, int z, Direction facing) {
		BlockPos pos = new BlockPos(x, y, z);
		level.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), FLAGS);
		return level.getBlockEntity(pos) instanceof ChestBlockEntity chest ? chest : null;
	}

	/**
	 * Two chests side by side at x and x + 1, which 1.7.10 always joined into a double chest (facing south when nothing
	 * opaque is around). OreSpawn filled only one half: returns the block entity at x ({@code first}) or x + 1.
	 */
	static @Nullable Container doubleChest(WorldGenLevel level, int x, int y, int z, boolean first) {
		BlockState chest = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
		BlockPos west = new BlockPos(x, y, z);
		BlockPos east = west.east();
		// Facing south, a LEFT chest joins its west neighbour and a RIGHT chest its east one.
		level.setBlock(west, chest.setValue(ChestBlock.TYPE, ChestType.RIGHT), FLAGS);
		level.setBlock(east, chest.setValue(ChestBlock.TYPE, ChestType.LEFT), FLAGS);
		return level.getBlockEntity(first ? west : east) instanceof ChestBlockEntity c ? c : null;
	}

	private static boolean opaque(WorldGenLevel level, BlockPos pos) {
		return level.getBlockState(pos).isSolidRender();
	}

	/**
	 * 1.7.10's WeightedRandomChestContent.generateChestContents: {@code count} weighted picks, each min..max of its item
	 * (as single items if that's more than a stack holds), each stack put into a random slot.
	 */
	static void fill(@Nullable Container chest, RandomSource random, List<Loot> loot, int count) {
		if (chest == null) return;
		int total = 0;
		for (Loot l : loot) total += l.weight();
		for (int n = 0; n < count; n++) {
			int r = random.nextInt(total);
			Loot pick = loot.getLast();
			for (Loot l : loot) {
				r -= l.weight();
				if (r < 0) {
					pick = l;
					break;
				}
			}
			int amount = pick.min() + random.nextInt(pick.max() - pick.min() + 1);
			Item item = pick.item().get();
			if (item == Items.AIR) continue;
			ItemStack stack = new ItemStack(item, amount);
			if (amount > stack.getMaxStackSize()) {
				for (int i = 0; i < amount; i++) put(chest, random, new ItemStack(item));
			} else {
				put(chest, random, stack);
			}
		}
	}

	/** One of OreSpawn's fixed chest slots, filled half the time. */
	static void maybe(@Nullable Container chest, RandomSource random, int slot, ItemStack stack) {
		if (random.nextInt(2) == 0 && chest != null && !stack.isEmpty()) chest.setItem(slot, stack);
	}

	private static void put(Container chest, RandomSource random, ItemStack stack) {
		chest.setItem(random.nextInt(chest.getContainerSize()), stack);
	}

	/** One WeightedRandomChestContent: item, min and max count, weight. */
	record Loot(Supplier<Item> item, int min, int max, int weight) {
		static Loot of(Item item, int min, int max, int weight) {
			return new Loot(() -> item, min, max, weight);
		}

		/** An OreSpawn item another group owns (air, so skipped, until it's ported). */
		static Loot of(String orespawnId, int min, int max, int weight) {
			return new Loot(() -> ModItems.lazy(orespawnId), min, max, weight);
		}
	}
}
