package orespawn.world.utopia;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * The world as OreSpawn's generators saw it ({@code World.getBlock / setBlock}, {@code world.rand}): either the real
 * world while a feature places ({@link LevelWorld}) or a block buffer over the terrain for builds too big for one
 * chunk's feature step ({@link BufferWorld}). OreSpawn's code is ported against this so it runs the same either way.
 */
public abstract class GenWorld {
	protected final RandomSource random;

	protected GenWorld(RandomSource random) {
		this.random = random;
	}

	/** world.rand (OreSpawn also used its own OreSpawnRand; both are this). */
	public RandomSource random() {
		return this.random;
	}

	public abstract BlockState get(int x, int y, int z);

	protected abstract void put(int x, int y, int z, BlockState state);

	protected abstract boolean inHeight(int y);

	/** setBlock / setBlockFast. A null state (a block whose owner isn't ported yet) places nothing. */
	public void set(int x, int y, int z, @Nullable BlockState state) {
		if (state != null && this.inHeight(y)) {
			this.put(x, y, z, settle(state));
		}
	}

	public Block block(int x, int y, int z) {
		return this.get(x, y, z).getBlock();
	}

	/** isAirBlock. */
	public boolean isAir(int x, int y, int z) {
		return this.get(x, y, z).isAir();
	}

	/** A chest, filled with these 27 slots (null: empty). */
	public abstract void chest(int x, int y, int z, Direction facing, ItemStack @Nullable [] items);

	/** A mob spawner for this creature (empty if it isn't registered). */
	public abstract void spawner(int x, int y, int z, @Nullable EntityType<?> type);

	/** Spawns a creature, as OreSpawn's spawnCreature did (no spawn-egg setup). */
	public abstract void spawn(EntityType<?> type, double x, double y, double z, float yaw, @Nullable Consumer<Entity> init);

	/**
	 * 1.7.10 leaves only checked for decay when something next to them changed. Placed leaves start at distance 1, so
	 * they stay until a neighbour update makes them work their real distance out, then decay as usual if no log is near.
	 */
	static BlockState settle(BlockState state) {
		if (state.getBlock() instanceof LeavesBlock && state.hasProperty(LeavesBlock.DISTANCE)) {
			return state.setValue(LeavesBlock.DISTANCE, 1);
		}
		return state;
	}
}
