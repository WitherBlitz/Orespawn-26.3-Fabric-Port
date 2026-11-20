package orespawn.world.utopia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * {@link GenWorld} on a real level. During worldgen (a {@link WorldGenRegion}) it only reads and writes the 3x3 chunks
 * a feature may touch, so OreSpawn's generators, which in 1.7.10 spilled freely into neighbouring chunks, quietly stop
 * at that edge instead of tripping 26.3's far-chunk check. On a live level (a sapling growing) everything is open.
 */
public final class LevelWorld extends GenWorld {
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private final WorldGenLevel level;
	private final boolean bounded;
	private final int minCx;
	private final int maxCx;
	private final int minCz;
	private final int maxCz;
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	public LevelWorld(WorldGenLevel level, RandomSource random) {
		super(random);
		this.level = level;
		if (level instanceof WorldGenRegion region) {
			ChunkPos center = region.getCenter();
			this.bounded = true;
			this.minCx = center.x() - 1;
			this.maxCx = center.x() + 1;
			this.minCz = center.z() - 1;
			this.maxCz = center.z() + 1;
		} else {
			this.bounded = false;
			this.minCx = this.maxCx = this.minCz = this.maxCz = 0;
		}
	}

	public WorldGenLevel level() {
		return this.level;
	}

	private boolean reachable(int x, int z) {
		if (!this.bounded) {
			return true;
		}
		int cx = x >> 4;
		int cz = z >> 4;
		return cx >= this.minCx && cx <= this.maxCx && cz >= this.minCz && cz <= this.maxCz;
	}

	@Override
	protected boolean inHeight(int y) {
		return y >= this.level.getMinY() && y <= this.level.getMaxY();
	}

	@Override
	public BlockState get(int x, int y, int z) {
		if (!this.reachable(x, z) || !this.inHeight(y)) {
			return AIR;
		}
		return this.level.getBlockState(this.pos.set(x, y, z));
	}

	@Override
	protected void put(int x, int y, int z, BlockState state) {
		if (this.reachable(x, z)) {
			this.level.setBlock(this.pos.set(x, y, z), state, 2);
		}
	}

	@Override
	public void chest(int x, int y, int z, Direction facing, ItemStack @Nullable [] items) {
		if (!this.reachable(x, z) || !this.inHeight(y)) {
			return;
		}
		BlockPos at = new BlockPos(x, y, z);
		this.level.setBlock(at, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
		if (items != null && this.level.getBlockEntity(at) instanceof Container chest) {
			for (int i = 0; i < items.length && i < chest.getContainerSize(); i++) {
				if (items[i] != null && !items[i].isEmpty()) {
					chest.setItem(i, items[i]);
				}
			}
		}
	}

	@Override
	public void spawner(int x, int y, int z, @Nullable EntityType<?> type) {
		if (!this.reachable(x, z) || !this.inHeight(y)) {
			return;
		}
		BlockPos at = new BlockPos(x, y, z);
		this.level.setBlock(at, Blocks.SPAWNER.defaultBlockState(), 2);
		BlockEntity be = this.level.getBlockEntity(at);
		if (type != null && be instanceof SpawnerBlockEntity spawner) {
			spawner.setEntityId(type, this.random);
		}
	}

	@Override
	public void spawn(EntityType<?> type, double x, double y, double z, float yaw, @Nullable Consumer<Entity> init) {
		if (!this.reachable((int) Math.floor(x), (int) Math.floor(z))) {
			return;
		}
		Entity entity = type.create(this.level.getLevel(), EntitySpawnReason.STRUCTURE);
		if (entity == null) {
			return;
		}
		entity.snapTo(x, y, z, yaw, 0.0F);
		if (init != null) {
			init.accept(entity);
		}
		this.level.addFreshEntityWithPassengers(entity);
	}
}
