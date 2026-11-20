package orespawn.world.dungeons;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import orespawn.OreSpawn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The parts of a dungeon that world generation couldn't place yet: a feature may only write to the 3x3 chunks around
 * the chunk being decorated, and OreSpawn's bigger dungeons (the Ender castle, and the castles, altars and mazes of its
 * dimensions) reach further. Their blocks, chests, spawners and creatures wait here, per chunk, and are placed at the end
 * of the dimension's next tick once that chunk is loaded (never inside the chunk-load event itself: the chunk isn't
 * fully loaded there yet, and placing a block in it would wait on itself and freeze the server). Saved with the
 * dimension, so nothing is lost if the game stops first.
 */
public final class Deferred extends SavedData {
	private static final Codec<Deferred> CODEC = CompoundTag.CODEC.xmap(Deferred::load, Deferred::save);
	private static final SavedDataType<Deferred> TYPE = new SavedDataType<>(OreSpawn.id("pending_dungeon_blocks"), Deferred::new, CODEC,
		DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
	/** Each loaded dimension's pending parts (world generation threads add to them). */
	private static final Map<ResourceKey<Level>, Deferred> LEVELS = new ConcurrentHashMap<>();

	/** How many ticks a chunk is waited for after its parts are ready, before it's left to its next load. */
	private static final int WAIT_TICKS = 40;

	private final Map<Long, Queue<CompoundTag>> pending = new ConcurrentHashMap<>();
	/** Chunks whose parts can go in as soon as the chunk is fully loaded, with the ticks left to wait for that. */
	private final Map<Long, Integer> ready = new ConcurrentHashMap<>();

	public static void init() {
		ServerLevelEvents.LOAD.register((server, level) -> LEVELS.put(level.dimension(), level.getDataStorage().computeIfAbsent(TYPE)));
		ServerLevelEvents.UNLOAD.register((server, level) -> LEVELS.remove(level.dimension()));
		ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
			Deferred d = LEVELS.get(level.dimension());
			long key = chunk.getPos().pack();
			if (d != null && d.pending.containsKey(key)) {
				d.ready.put(key, WAIT_TICKS);
			}
		});
		ServerTickEvents.END_LEVEL_TICK.register(level -> {
			Deferred d = LEVELS.get(level.dimension());
			if (d != null && !d.ready.isEmpty()) {
				d.applyReady(level);
			}
		});
	}

	/** Places the parts of each ready chunk that is fully loaded now (main thread, end of the dimension's tick). */
	private void applyReady(ServerLevel level) {
		for (Long chunk : List.copyOf(this.ready.keySet())) {
			ChunkPos pos = ChunkPos.unpack(chunk);
			if (level.getChunkSource().getChunkNow(pos.x(), pos.z()) != null) {
				this.ready.remove(chunk);
				this.apply(level, chunk);
			} else {
				this.ready.computeIfPresent(chunk, (k, ticks) -> ticks > 1 ? ticks - 1 : null);
			}
		}
	}

	/** Queues a chunk's parts (any thread); placed on the next tick if that chunk is loaded, else when it loads. */
	static void add(ServerLevel level, long chunk, List<CompoundTag> ops) {
		Deferred d = LEVELS.get(level.dimension());
		if (d == null) {
			OreSpawn.LOGGER.warn("OreSpawn dungeon: {} isn't loaded; {} parts in chunk {} dropped", level.dimension(), ops.size(),
				ChunkPos.unpack(chunk));
			return;
		}
		d.pending.computeIfAbsent(chunk, k -> new ConcurrentLinkedQueue<>()).addAll(ops);
		d.setDirty();
		d.ready.put(chunk, WAIT_TICKS);
	}

	/** Places a chunk's waiting parts (main thread, the chunk fully loaded). */
	private void apply(ServerLevel level, long chunk) {
		Queue<CompoundTag> ops = this.pending.remove(chunk);
		if (ops == null) {
			return;
		}
		this.setDirty();
		var blocks = level.holderLookup(Registries.BLOCK);
		var registries = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
		List<BlockPos> written = new ArrayList<>();
		List<CompoundTag> later = new ArrayList<>();
		for (CompoundTag op : ops) {
			if (op.getStringOr("k", "").equals("b")) {
				BlockPos pos = BlockPos.of(op.getLongOr("p", 0L));
				BlockState state = NbtUtils.readBlockState(blocks, op.getCompoundOrEmpty("s"));
				level.setBlock(pos, state, net.minecraft.world.level.block.Block.UPDATE_CLIENTS | net.minecraft.world.level.block.Block.UPDATE_KNOWN_SHAPE);
				written.add(pos);
			} else {
				later.add(op);
			}
		}
		McWorld.connect(level, written);
		for (CompoundTag op : later) {
			BlockPos pos = BlockPos.of(op.getLongOr("p", 0L));
			switch (op.getStringOr("k", "")) {
				case "c" -> {
					if (level.getBlockEntity(pos) instanceof Container chest) {
						ListTag items = op.getListOrEmpty("items");
						for (int i = 0; i < items.size(); i++) {
							CompoundTag it = items.getCompoundOrEmpty(i);
							int slot = it.getIntOr("slot", 0);
							ItemStack.CODEC.parse(registries, it.get("stack")).result().ifPresent(stack -> chest.setItem(slot, stack));
						}
					}
				}
				case "s" -> McWorld.setSpawner(level, pos, op.getStringOr("e", ""), level.getRandom());
				case "e" -> {
					DWorld.Ent ent = new DWorld.Ent(op.getStringOr("e", ""));
					ent.setLocationAndAngles(op.getDoubleOr("x", 0), op.getDoubleOr("y", 0), op.getDoubleOr("z", 0), op.getFloatOr("yaw", 0), 0);
					if (op.getBooleanOr("persistent", false)) {
						ent.setPersistent();
					}
					McWorld.spawn(level, ent);
				}
				default -> {
				}
			}
		}
	}

	// ---- parts ----

	static CompoundTag block(BlockPos pos, BlockState state) {
		CompoundTag t = new CompoundTag();
		t.putString("k", "b");
		t.putLong("p", pos.asLong());
		t.put("s", NbtUtils.writeBlockState(state));
		return t;
	}

	static CompoundTag chest(BlockPos pos, SimpleContainer contents, ServerLevel level) {
		var registries = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
		CompoundTag t = new CompoundTag();
		t.putString("k", "c");
		t.putLong("p", pos.asLong());
		ListTag items = new ListTag();
		for (int slot = 0; slot < contents.getContainerSize(); slot++) {
			ItemStack stack = contents.getItem(slot);
			if (!stack.isEmpty()) {
				int s = slot;
				ItemStack.CODEC.encodeStart(registries, stack).result().ifPresent(tag -> {
					CompoundTag it = new CompoundTag();
					it.putInt("slot", s);
					it.put("stack", tag);
					items.add(it);
				});
			}
		}
		t.put("items", items);
		return t;
	}

	static CompoundTag spawner(BlockPos pos, String entity) {
		CompoundTag t = new CompoundTag();
		t.putString("k", "s");
		t.putLong("p", pos.asLong());
		t.putString("e", entity);
		return t;
	}

	static CompoundTag entity(DWorld.Ent ent) {
		CompoundTag t = new CompoundTag();
		t.putString("k", "e");
		t.putString("e", ent.name);
		t.putDouble("x", ent.x);
		t.putDouble("y", ent.y);
		t.putDouble("z", ent.z);
		t.putFloat("yaw", ent.yaw);
		t.putBoolean("persistent", ent.persistent);
		return t;
	}

	// ---- saving ----

	private static Deferred load(CompoundTag tag) {
		Deferred d = new Deferred();
		for (String key : tag.keySet()) {
			Queue<CompoundTag> ops = new ConcurrentLinkedQueue<>();
			ListTag list = tag.getListOrEmpty(key);
			for (int i = 0; i < list.size(); i++) {
				ops.add(list.getCompoundOrEmpty(i));
			}
			try {
				d.pending.put(Long.parseLong(key), ops);
			} catch (NumberFormatException ignored) {
				// Not ours.
			}
		}
		return d;
	}

	private CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		this.pending.forEach((chunk, ops) -> {
			ListTag list = new ListTag();
			for (Tag op : ops) {
				list.add(op);
			}
			tag.put(Long.toString(chunk), list);
		});
		return tag;
	}
}
