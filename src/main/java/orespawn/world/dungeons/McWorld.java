package orespawn.world.dungeons;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import orespawn.OreSpawn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

/**
 * {@link DWorld} on a 26.3 level: runs OreSpawn's dungeon code against a buffer, then writes the result.
 * <p>
 * While the dungeon code runs, every block it sets is kept here (it reads them back as it goes, as it would from the
 * world); {@link #commit()} then writes the blocks, fixes up fence, pane, wall, stair and chest connections (1.7.10
 * worked those out when drawing; 26.3 keeps them in the block state), fills the chests, sets the spawners and spawns
 * the creatures. Blocks are placed without neighbour updates, as OreSpawn's setBlockFast did.
 * <p>
 * During world generation a feature may only write to the 3x3 chunks around the one being decorated, and only read
 * those. What falls outside goes to {@link Deferred}, which places it when that chunk is loaded; reads outside see this
 * dungeon's own blocks, or else the terrain the chunk generator would make there (its base noise, without surface
 * blocks or features). Outside world generation (a Random Dungeon item, a command) everything is written at once, like
 * OreSpawn did.
 */
public final class McWorld extends DWorld {
	/** setBlock flags: tell clients, but don't update neighbours' shapes or drop unsupported blocks. */
	private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

	private final WorldGenLevel level;
	private final ServerLevel server;
	private final RandomSource random;
	private final WorldGenRegion region;
	private final Map<BlockPos, Placed> placed = new LinkedHashMap<>();
	private final Map<BlockPos, List<Consumer<Container>>> chests = new LinkedHashMap<>();
	private final Map<BlockPos, String> spawners = new LinkedHashMap<>();
	private final List<Ent> entities = new ArrayList<>();
	private final Long2ObjectOpenHashMap<NoiseColumn> columns = new Long2ObjectOpenHashMap<>();

	/** A block the dungeon code set: the 1.7.10 block and metadata, and the 26.3 state (null for an unported block). */
	private record Placed(LB block, int meta, BlockState state) {
	}

	private McWorld(WorldGenLevel level, RandomSource random) {
		super(new Random(random.nextLong()));
		this.level = level;
		this.server = level.getLevel();
		this.random = random;
		this.region = level instanceof WorldGenRegion r ? r : null;
	}

	/** Runs some dungeon code on a level (a ServerLevel, or the region of a feature being placed) and writes the result. */
	public static <T> T run(WorldGenLevel level, RandomSource random, java.util.function.Function<DWorld, T> code) {
		McWorld world = new McWorld(level, random);
		T result = code.apply(world);
		world.commit();
		return result;
	}

	// ---- DWorld ----

	@Override
	public boolean setBlockFast(int x, int y, int z, LB block, int meta, int flags) {
		return this.setBlock(x, y, z, block, meta, flags);
	}

	@Override
	public boolean setBlock(int x, int y, int z, LB block, int meta, int flags) {
		if (y < this.level.getMinY() || y >= this.level.getMaxY() + 1) {
			return false;
		}
		BlockPos pos = new BlockPos(x, y, z);
		Placed old = this.placed.put(pos, new Placed(block, meta, Legacy.state(block, meta)));
		if (old == null || old.block != block) {
			// A new block: the chest or spawner that was there goes with the old one.
			this.chests.remove(pos);
			this.spawners.remove(pos);
		}
		return true;
	}

	@Override
	public boolean setBlockMetadataWithNotify(int x, int y, int z, int meta, int flags) {
		LB block = this.getBlock(x, y, z);
		return block != LB.AIR && block != LB.OTHER && this.setBlock(x, y, z, block, meta, flags);
	}

	@Override
	public LB getBlock(int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		Placed p = this.placed.get(pos);
		if (p != null) {
			return p.block;
		}
		return Legacy.block(this.stateAt(pos));
	}

	@Override
	public String getBiomeName(int x, int z) {
		int y = this.readable(x, z) ? this.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) : this.level.getSeaLevel();
		return Legacy.biomeName(this.level.getBiome(new BlockPos(x, y, z)));
	}

	@Override
	public Chest chestAt(int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		Placed p = this.placed.get(pos);
		if (p == null ? !(this.stateAt(pos).getBlock() instanceof ChestBlock) : p.block != LB.CHEST) {
			return null;
		}
		return new ChestRef(pos);
	}

	@Override
	public Spawner spawnerAt(int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		Placed p = this.placed.get(pos);
		if (p == null ? !this.stateAt(pos).is(Blocks.SPAWNER) : p.block != LB.MOB_SPAWNER) {
			return null;
		}
		return new Spawner() {
			@Override
			public void setEntityName(String name) {
				McWorld.this.spawners.put(pos, name);
			}
		};
	}

	@Override
	public boolean spawnEntityInWorld(Ent entity) {
		this.entities.add(entity);
		return true;
	}

	@Override
	public void placeDoorBlock(int x, int y, int z, int dir, LB door) {
		// ItemDoor.placeDoorBlock: hinge on the left (mirrored) if the door's left side has more solid blocks, or a door.
		int dx = 0;
		int dz = 0;
		if (dir == 0) dz = 1;
		if (dir == 1) dx = -1;
		if (dir == 2) dz = -1;
		if (dir == 3) dx = 1;
		int left = (this.isNormalCube(x - dx, y, z - dz) ? 1 : 0) + (this.isNormalCube(x - dx, y + 1, z - dz) ? 1 : 0);
		int right = (this.isNormalCube(x + dx, y, z + dz) ? 1 : 0) + (this.isNormalCube(x + dx, y + 1, z + dz) ? 1 : 0);
		boolean doorLeft = this.getBlock(x - dx, y, z - dz) == door || this.getBlock(x - dx, y + 1, z - dz) == door;
		boolean doorRight = this.getBlock(x + dx, y, z + dz) == door || this.getBlock(x + dx, y + 1, z + dz) == door;
		boolean mirror = doorLeft && !doorRight || right > left;
		this.setBlock(x, y, z, door, dir, 2);
		this.setBlock(x, y + 1, z, door, 8 | (mirror ? 1 : 0), 2);
		// 26.3 keeps facing and hinge in both halves.
		Placed lower = this.placed.get(new BlockPos(x, y, z));
		if (lower.state != null && lower.state.hasProperty(DoorBlock.HINGE)) {
			BlockState base = lower.state.setValue(DoorBlock.HINGE, mirror ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT);
			this.placed.put(new BlockPos(x, y, z), new Placed(door, dir, base));
			this.placed.put(new BlockPos(x, y + 1, z), new Placed(door, 8 | (mirror ? 1 : 0), base.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)));
		}
	}

	private boolean isNormalCube(int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		Placed p = this.placed.get(pos);
		BlockState state = p != null ? (p.state == null ? Blocks.STONE.defaultBlockState() : p.state) : this.stateAt(pos);
		return state.isRedstoneConductor(this.level, pos) && state.isCollisionShapeFullBlock(this.level, pos);
	}

	/** A chest: its position and the things to do to it once it's placed. */
	private final class ChestRef extends Chest {
		final BlockPos pos;
		final List<Consumer<Container>> ops;

		ChestRef(BlockPos pos) {
			this.pos = pos;
			this.ops = McWorld.this.chestOps(pos);
		}

		@Override
		public void setInventorySlotContents(int slot, Stack stack) {
			Item item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(stack.item())).orElse(null);
			if (item != null) {
				this.ops.add(c -> c.setItem(slot, new ItemStack(item, stack.count())));
			}
		}
	}

	private List<Consumer<Container>> chestOps(BlockPos pos) {
		return this.chests.computeIfAbsent(pos, p -> new ArrayList<>());
	}

	// ---- reading the level ----

	/** Whether this column can be read: always outside world generation, else only the 3x3 chunks of the region. */
	private boolean readable(int x, int z) {
		if (this.region == null) {
			return true;
		}
		var center = this.region.getCenter();
		return Math.abs((x >> 4) - center.x()) <= 1 && Math.abs((z >> 4) - center.z()) <= 1;
	}

	private boolean writable(BlockPos pos) {
		return this.region == null || this.region.isWithinWriteZone(pos);
	}

	private BlockState stateAt(BlockPos pos) {
		if (this.readable(pos.getX(), pos.getZ())) {
			return this.level.getBlockState(pos);
		}
		// Outside the region: the terrain the generator makes there.
		long key = BlockPos.asLong(pos.getX(), 0, pos.getZ());
		NoiseColumn column = this.columns.get(key);
		if (column == null) {
			var source = this.server.getChunkSource();
			column = source.getGenerator().getBaseColumn(pos.getX(), pos.getZ(), this.server, source.randomState());
			this.columns.put(key, column);
		}
		return column.getBlock(pos.getY());
	}

	// ---- writing ----

	private void commit() {
		List<BlockPos> written = new ArrayList<>();
		Map<Long, List<CompoundTag>> deferred = new HashMap<>();
		for (var e : this.placed.entrySet()) {
			BlockState state = e.getValue().state;
			if (state == null) {
				continue;
			}
			BlockPos pos = e.getKey();
			if (this.writable(pos)) {
				this.level.setBlock(pos, state, FLAGS);
				if (!state.getFluidState().isEmpty() && !state.getFluidState().isSource()) {
					this.level.scheduleTick(pos, state.getFluidState().getType(), 0);
				}
				written.add(pos);
			} else {
				deferred.computeIfAbsent(chunkKey(pos), k -> new ArrayList<>()).add(Deferred.block(pos, state));
			}
		}
		connect(this.level, written);
		for (var e : this.chests.entrySet()) {
			if (this.writable(e.getKey())) {
				if (this.level.getBlockEntity(e.getKey()) instanceof Container c) {
					e.getValue().forEach(op -> op.accept(c));
				}
			} else {
				// Deferred chests keep their contents as a list of items rolled now.
				var holder = new net.minecraft.world.SimpleContainer(27);
				e.getValue().forEach(op -> op.accept(holder));
				deferred.computeIfAbsent(chunkKey(e.getKey()), k -> new ArrayList<>()).add(Deferred.chest(e.getKey(), holder, this.server));
			}
		}
		for (var e : this.spawners.entrySet()) {
			if (this.writable(e.getKey())) {
				setSpawner(this.level, e.getKey(), e.getValue(), this.random);
			} else {
				deferred.computeIfAbsent(chunkKey(e.getKey()), k -> new ArrayList<>()).add(Deferred.spawner(e.getKey(), e.getValue()));
			}
		}
		for (Ent ent : this.entities) {
			BlockPos pos = BlockPos.containing(ent.x, ent.y, ent.z);
			if (this.writable(pos)) {
				spawn(this.level, ent);
			} else {
				deferred.computeIfAbsent(chunkKey(pos), k -> new ArrayList<>()).add(Deferred.entity(ent));
			}
		}
		deferred.forEach((chunk, ops) -> Deferred.add(this.server, chunk, ops));
	}

	private static long chunkKey(BlockPos pos) {
		return net.minecraft.world.level.ChunkPos.pack(pos.getX() >> 4, pos.getZ() >> 4);
	}

	/** Gives fences, panes, walls, stairs and chests their connections; never removes a block the dungeon placed. */
	static void connect(WorldGenLevel level, List<BlockPos> positions) {
		// During world generation only the 3x3 chunks around the decorated one can be read, so a block on the outer edge
		// (whose neighbour lies a chunk further out) keeps the shape it was placed with.
		var center = level instanceof WorldGenRegion r ? r.getCenter() : null;
		for (BlockPos pos : positions) {
			if (center != null && (Math.abs(((pos.getX() - 1) >> 4) - center.x()) > 1 || Math.abs(((pos.getX() + 1) >> 4) - center.x()) > 1
				|| Math.abs(((pos.getZ() - 1) >> 4) - center.z()) > 1 || Math.abs(((pos.getZ() + 1) >> 4) - center.z()) > 1)) {
				continue;
			}
			// In a running world, a neighbour in a chunk that isn't loaded would be loaded (or generated) on the spot.
			if (level instanceof ServerLevel server && !neighboursLoaded(server, pos)) {
				continue;
			}
			BlockState state = level.getBlockState(pos);
			BlockState shaped = Block.updateFromNeighbourShapes(state, level, pos);
			if (shaped != state && !shaped.isAir() && shaped.getBlock() == state.getBlock()) {
				level.setBlock(pos, shaped, FLAGS);
			}
		}
	}

	private static boolean neighboursLoaded(ServerLevel level, BlockPos pos) {
		for (int dx = -1; dx <= 1; dx += 2) {
			for (int dz = -1; dz <= 1; dz += 2) {
				if (level.getChunkSource().getChunkNow((pos.getX() + dx) >> 4, (pos.getZ() + dz) >> 4) == null) {
					return false;
				}
			}
		}
		return true;
	}

	static void setSpawner(WorldGenLevel level, BlockPos pos, String name, RandomSource random) {
		EntityType<?> type = Legacy.entityType(name);
		if (type == null) {
			OreSpawn.LOGGER.debug("OreSpawn dungeon spawner: {} isn't ported yet; left empty", name);
		} else if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
			spawner.setEntityId(type, random);
		}
	}

	static void spawn(WorldGenLevel level, Ent ent) {
		EntityType<?> type = Legacy.entityType(ent.name);
		if (type == null) {
			OreSpawn.LOGGER.debug("OreSpawn dungeon: {} isn't ported yet; not spawned", ent.name);
			return;
		}
		Entity entity = type.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
		if (entity == null) {
			return;
		}
		entity.snapTo(ent.x, ent.y, ent.z, ent.yaw, ent.pitch);
		if (entity instanceof Mob mob) {
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.STRUCTURE, null);
			if (ent.persistent) {
				mob.setPersistenceRequired();
			}
		}
		level.addFreshEntityWithPassengers(entity);
	}

	/** Turns the 1.7.10 names the dungeon code uses into 26.3 blocks, states, biomes and creatures. */
	static final class Legacy {
		private static Map<Integer, BlockState[]> states;
		private static Map<Block, LB> blocks;

		private Legacy() {
		}

		private static synchronized void load() {
			if (states != null) {
				return;
			}
			Map<Integer, BlockState[]> s = new java.util.TreeMap<>();
			Map<Block, LB> b = new HashMap<>();
			for (String[] row : LegacyStates.TABLE) {
				int id = Integer.parseInt(row[0]);
				BlockState[] metas = new BlockState[16];
				for (int meta = 0; meta < 16; meta++) {
					metas[meta] = parse(row[meta + 1]);
				}
				s.put(id, metas);
			}
			// Meta 0 first, so each 26.3 block reads back as the 1.7.10 block it mostly came from.
			for (int meta = 0; meta < 16; meta++) {
				for (var e : s.entrySet()) {
					b.putIfAbsent(e.getValue()[meta].getBlock(), LB.byId(e.getKey()));
				}
			}
			for (LB lb : LB.modBlocks()) {
				BuiltInRegistries.BLOCK.getOptional(OreSpawn.id(lb.name)).ifPresent(block -> b.putIfAbsent(block, lb));
			}
			b.put(Blocks.WATER, LB.WATER);
			b.put(Blocks.LAVA, LB.LAVA);
			b.put(Blocks.AIR, LB.AIR);
			b.put(Blocks.CAVE_AIR, LB.AIR);
			b.put(Blocks.VOID_AIR, LB.AIR);
			blocks = b;
			states = s;
		}

		/** "minecraft:oak_log[axis=y]" -> the state; properties 26.3 changed (wall sides...) keep their defaults. */
		private static BlockState parse(String text) {
			int bracket = text.indexOf('[');
			Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(bracket < 0 ? text : text.substring(0, bracket)));
			BlockState state = block.defaultBlockState();
			if (bracket >= 0) {
				for (String kv : text.substring(bracket + 1, text.length() - 1).split(",")) {
					String[] p = kv.split("=");
					Property<?> property = block.getStateDefinition().getProperty(p[0]);
					if (property != null) {
						state = with(state, property, p[1]);
					}
				}
			}
			return state;
		}

		private static <T extends Comparable<T>> BlockState with(BlockState state, Property<T> property, String value) {
			Optional<T> v = property.getValue(value);
			return v.isPresent() ? state.setValue(property, v.get()) : state;
		}

		/** The 26.3 state for a 1.7.10 block and metadata; null for an OreSpawn block that isn't ported yet. */
		static BlockState state(LB block, int meta) {
			load();
			if (block.isVanilla()) {
				return states.get(block.id)[meta & 15];
			}
			Block b = BuiltInRegistries.BLOCK.getOptional(OreSpawn.id(block.name)).orElse(null);
			if (b == null) {
				return null;
			}
			BlockState state = b.defaultBlockState();
			// Crops: OreSpawn's metadata is their growth stage.
			if (meta > 0 && b.getStateDefinition().getProperty("age") instanceof IntegerProperty age) {
				state = state.setValue(age, Math.min(meta, java.util.Collections.max(age.getPossibleValues())));
			}
			return state;
		}

		static LB block(BlockState state) {
			load();
			if (state.isAir()) {
				return LB.AIR;
			}
			LB lb = blocks.get(state.getBlock());
			if (lb == null && state.getFluidState().is(Fluids.WATER)) {
				return LB.WATER;
			}
			return lb != null ? lb : LB.OTHER;
		}

		/** OreSpawnWorld compares biome names: the 1.7.10 biome each 26.3 one came from. */
		static String biomeName(Holder<Biome> biome) {
			String path = biome.unwrapKey().map(k -> k.identifier().getPath()).orElse("");
			return switch (path) {
				case "ocean", "lukewarm_ocean", "warm_ocean", "cold_ocean" -> "Ocean";
				case "deep_ocean", "deep_lukewarm_ocean", "deep_cold_ocean" -> "Deep Ocean";
				case "frozen_ocean", "deep_frozen_ocean" -> "FrozenOcean";
				case "plains", "sunflower_plains", "meadow" -> "Plains";
				case "desert" -> "Desert";
				case "windswept_hills", "windswept_gravelly_hills" -> "Extreme Hills";
				case "windswept_forest" -> "Extreme Hills+";
				case "forest", "flower_forest" -> "Forest";
				case "taiga" -> "Taiga";
				case "old_growth_pine_taiga", "old_growth_spruce_taiga" -> "Mega Taiga";
				case "snowy_taiga" -> "Cold Taiga";
				case "swamp", "mangrove_swamp" -> "Swampland";
				case "river" -> "River";
				case "frozen_river" -> "FrozenRiver";
				case "snowy_plains" -> "Ice Plains";
				case "ice_spikes" -> "Ice Plains Spikes";
				case "mushroom_fields" -> "MushroomIsland";
				case "beach" -> "Beach";
				case "stony_shore" -> "Stone Beach";
				case "snowy_beach" -> "Cold Beach";
				case "jungle", "bamboo_jungle" -> "Jungle";
				case "sparse_jungle" -> "JungleEdge";
				case "birch_forest", "old_growth_birch_forest" -> "Birch Forest";
				case "dark_forest" -> "Roofed Forest";
				case "savanna", "windswept_savanna" -> "Savanna";
				case "savanna_plateau" -> "Savanna Plateau";
				case "badlands", "eroded_badlands" -> "Mesa";
				case "wooded_badlands" -> "Mesa Plateau F";
				case "nether_wastes", "crimson_forest", "warped_forest", "soul_sand_valley", "basalt_deltas" -> "Hell";
				case "the_end", "end_highlands", "end_midlands", "small_end_islands", "end_barrens" -> "Sky";
				default -> path;
			};
		}

		/** 1.7.10's names for the vanilla creatures the dungeons place. */
		private static final Map<String, String> VANILLA = Map.ofEntries(
			Map.entry("EnderCrystal", "end_crystal"), Map.entry("Silverfish", "silverfish"), Map.entry("Blaze", "blaze"),
			Map.entry("Skeleton", "skeleton"), Map.entry("Zombie", "zombie"), Map.entry("Spider", "spider"),
			Map.entry("CaveSpider", "cave_spider"), Map.entry("Creeper", "creeper"), Map.entry("Enderman", "enderman"),
			Map.entry("PigZombie", "zombified_piglin"), Map.entry("LavaSlime", "magma_cube"), Map.entry("Ghast", "ghast"),
			Map.entry("Witch", "witch"), Map.entry("Slime", "slime"), Map.entry("Bat", "bat"), Map.entry("WitherBoss", "wither"),
			Map.entry("EnderDragon", "ender_dragon"), Map.entry("VillagerGolem", "iron_golem"), Map.entry("SnowMan", "snow_golem"));

		/**
		 * The entity type for one of OreSpawn's entity names ("Ruby Bird" is orespawn:ruby_bird, "WTF?" orespawn:wtf), or a
		 * vanilla one; null if it isn't registered (its group isn't ported yet).
		 */
		static EntityType<?> entityType(String name) {
			String vanilla = VANILLA.get(name);
			if (vanilla != null) {
				return BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace(vanilla));
			}
			for (String id : List.of(snake(name, true), snake(name, false))) {
				var type = BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id(id));
				if (type.isPresent()) {
					return type.get();
				}
			}
			return null;
		}

		/** "Velocity Raptor" -> velocity_raptor; "CaveFisher" -> cave_fisher (or cavefisher without splitting words). */
		static String snake(String name, boolean splitCamel) {
			String s = splitCamel ? name.replaceAll("([a-z])([A-Z])", "$1_$2") : name;
			return s.replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_+|_+$", "").toLowerCase(java.util.Locale.ROOT);
		}
	}
}
