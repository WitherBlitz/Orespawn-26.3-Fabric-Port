package orespawn.world.utopia;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * {@link GenWorld} that records a build instead of placing it: reads see what the build placed so far, else Utopia's
 * terrain. Huge trees, the wind and sky trees and the King's and Queen's altars run on one of these once (it is
 * deterministic: same seed, same blocks), then each chunk they cover writes its own share when it decorates. That is
 * how 26.3 places things wider than the 3x3 chunks a feature may touch, which 1.7.10 did by writing into neighbouring
 * chunks.
 */
public final class BufferWorld extends GenWorld {
	private record ChestFill(Direction facing, ItemStack @Nullable [] items) {}

	private record Spawn(EntityType<?> type, double x, double y, double z, float yaw, @Nullable Consumer<Entity> init) {}

	private final UtopiaTerrain terrain;
	private final Long2ObjectOpenHashMap<BlockState> blocks = new Long2ObjectOpenHashMap<>();
	private final Long2ObjectOpenHashMap<ChestFill> chests = new Long2ObjectOpenHashMap<>();
	private final Long2ObjectOpenHashMap<EntityType<?>> spawners = new Long2ObjectOpenHashMap<>();
	private final List<Spawn> spawns = new ArrayList<>();
	private final Long2ObjectOpenHashMap<LongArrayList> byChunk = new Long2ObjectOpenHashMap<>();
	private int minX = Integer.MAX_VALUE;
	private int minY = Integer.MAX_VALUE;
	private int minZ = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int maxY = Integer.MIN_VALUE;
	private int maxZ = Integer.MIN_VALUE;

	public BufferWorld(UtopiaTerrain terrain, RandomSource random) {
		super(random);
		this.terrain = terrain;
	}

	private void grow(int x, int y, int z) {
		this.minX = Math.min(this.minX, x);
		this.minY = Math.min(this.minY, y);
		this.minZ = Math.min(this.minZ, z);
		this.maxX = Math.max(this.maxX, x);
		this.maxY = Math.max(this.maxY, y);
		this.maxZ = Math.max(this.maxZ, z);
	}

	@Override
	protected boolean inHeight(int y) {
		return y >= 0 && y < UtopiaTerrain.HEIGHT;
	}

	@Override
	public BlockState get(int x, int y, int z) {
		BlockState placed = this.blocks.get(BlockPos.asLong(x, y, z));
		return placed != null ? placed : this.terrain.stateAt(x, y, z);
	}

	@Override
	protected void put(int x, int y, int z, BlockState state) {
		long key = BlockPos.asLong(x, y, z);
		if (this.blocks.put(key, state) == null) {
			this.byChunk.computeIfAbsent(ChunkPos.pack(x >> 4, z >> 4), k -> new LongArrayList()).add(key);
			this.grow(x, y, z);
		}
	}

	@Override
	public void chest(int x, int y, int z, Direction facing, ItemStack @Nullable [] items) {
		if (this.inHeight(y)) {
			this.put(x, y, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
			this.chests.put(BlockPos.asLong(x, y, z), new ChestFill(facing, items));
		}
	}

	@Override
	public void spawner(int x, int y, int z, @Nullable EntityType<?> type) {
		if (this.inHeight(y)) {
			this.put(x, y, z, Blocks.SPAWNER.defaultBlockState());
			if (type != null) {
				this.spawners.put(BlockPos.asLong(x, y, z), type);
			}
		}
	}

	@Override
	public void spawn(EntityType<?> type, double x, double y, double z, float yaw, @Nullable Consumer<Entity> init) {
		this.spawns.add(new Spawn(type, x, y, z, yaw, init));
		this.grow((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
	}

	public boolean isEmpty() {
		return this.blocks.isEmpty() && this.spawns.isEmpty();
	}

	public BoundingBox bounds() {
		return new BoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
	}

	/** Writes this build's blocks, chests, spawners and creatures that fall inside {@code area}. */
	public void place(WorldGenLevel level, BoundingBox area, RandomSource random) {
		LevelWorld out = new LevelWorld(level, random);
		for (int cx = area.minX() >> 4; cx <= area.maxX() >> 4; cx++) {
			for (int cz = area.minZ() >> 4; cz <= area.maxZ() >> 4; cz++) {
				LongArrayList keys = this.byChunk.get(ChunkPos.pack(cx, cz));
				if (keys == null) {
					continue;
				}
				for (int n = 0; n < keys.size(); n++) {
					long key = keys.getLong(n);
					int x = BlockPos.getX(key);
					int y = BlockPos.getY(key);
					int z = BlockPos.getZ(key);
					if (!area.isInside(x, y, z)) {
						continue;
					}
					ChestFill chest = this.chests.get(key);
					BlockState state = this.blocks.get(key);
					if (chest != null && state.is(Blocks.CHEST)) {
						ItemStack[] items = null;
						if (chest.items() != null) {
							items = new ItemStack[chest.items().length];
							for (int i = 0; i < items.length; i++) {
								items[i] = chest.items()[i] == null ? ItemStack.EMPTY : chest.items()[i].copy();
							}
						}
						out.chest(x, y, z, chest.facing(), items);
					} else if (state.is(Blocks.SPAWNER)) {
						out.spawner(x, y, z, this.spawners.get(key));
					} else {
						out.set(x, y, z, state);
					}
				}
			}
		}
		for (Spawn s : this.spawns) {
			if (area.isInside((int) Math.floor(s.x()), (int) Math.floor(s.y()), (int) Math.floor(s.z()))) {
				out.spawn(s.type(), s.x(), s.y(), s.z(), s.yaw(), s.init());
			}
		}
	}
}
