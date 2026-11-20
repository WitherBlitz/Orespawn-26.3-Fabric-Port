package orespawn.world.crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.OreSpawn;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * The world around a chunk being decorated (or a live level, for the fairy trees built by hand). OreSpawn's populate
 * could write anywhere (loading more chunks as it went); 26.3 lets a chunk's decoration write only into it and its eight
 * neighbours, so blocks beyond that are left out (and read as air), which only trims the largest fairy castle trees.
 */
final class RegionVoxels implements Voxels {
	private final WorldGenLevel level;
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	RegionVoxels(WorldGenLevel level) {
		this.level = level;
	}

	WorldGenLevel level() {
		return this.level;
	}

	private boolean ok(int x, int y, int z) {
		if (this.level.isOutsideBuildHeight(y)) return false;
		this.pos.set(x, y, z);
		return !(this.level instanceof WorldGenRegion region) || region.isWithinWriteZone(this.pos);
	}

	@Override
	public BlockState state(int x, int y, int z) {
		return this.ok(x, y, z) ? this.level.getBlockState(this.pos) : Blocks.AIR.defaultBlockState();
	}

	@Override
	public void set(int x, int y, int z, BlockState state) {
		if (this.ok(x, y, z)) this.level.setBlock(this.pos.immutable(), state, 2);
	}

	/** A chest facing that way (1.7.10 metadata 2-5: north, south, west, east), or null if it's out of reach. */
	@Nullable Container chest(int x, int y, int z, int meta) {
		if (!this.ok(x, y, z)) return null;
		Direction facing = switch (meta) {
			case 3 -> Direction.SOUTH;
			case 4 -> Direction.WEST;
			case 5 -> Direction.EAST;
			default -> Direction.NORTH;
		};
		this.set(x, y, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
		BlockEntity be = this.level.getBlockEntity(new BlockPos(x, y, z));
		return be instanceof Container container ? container : null;
	}

	/** A mob spawner for an OreSpawn creature (empty while that creature isn't registered). */
	void spawner(int x, int y, int z, String creature) {
		if (!this.ok(x, y, z)) return;
		this.set(x, y, z, Blocks.SPAWNER.defaultBlockState());
		if (this.level.getBlockEntity(new BlockPos(x, y, z)) instanceof SpawnerBlockEntity spawner) {
			entityType(creature).ifPresent(type -> spawner.setEntityId(type, this.level.getRandom()));
		}
	}

	static Optional<EntityType<?>> entityType(String creature) {
		return BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id(creature));
	}
}
