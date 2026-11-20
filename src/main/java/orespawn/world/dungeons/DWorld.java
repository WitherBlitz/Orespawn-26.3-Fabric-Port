package orespawn.world.dungeons;

import java.util.Random;

/**
 * The part of 1.7.10's World that OreSpawn's dungeon code uses, so that code can be kept as it was. {@link McWorld}
 * is the 26.3 one. Method names are 1.7.10's (MCP), so the ported code reads like the original.
 */
public abstract class DWorld {
	/** World.rand. */
	public final Random rand;
	/** World.isRemote: dungeons are only ever built on the server. */
	public final boolean isRemote = false;

	protected DWorld(Random rand) {
		this.rand = rand;
	}

	/** OreSpawnMain.setBlockFast: places a block without updating its neighbours. */
	public abstract boolean setBlockFast(int x, int y, int z, LB block, int meta, int flags);

	/** World.setBlock(x, y, z, block, meta, flags). */
	public abstract boolean setBlock(int x, int y, int z, LB block, int meta, int flags);

	/** World.setBlock(x, y, z, block): metadata 0, flags 3. */
	public boolean setBlock(int x, int y, int z, LB block) {
		return this.setBlock(x, y, z, block, 0, 3);
	}

	/** World.setBlockMetadataWithNotify: the same block with other metadata. */
	public abstract boolean setBlockMetadataWithNotify(int x, int y, int z, int meta, int flags);

	/** World.getBlock: the 1.7.10 block there ({@link LB#AIR} for anything the dungeon code doesn't know). */
	public abstract LB getBlock(int x, int y, int z);

	/** World.isAirBlock. */
	public boolean isAirBlock(int x, int y, int z) {
		return this.getBlock(x, y, z) == LB.AIR;
	}

	/** World.getBiomeGenForCoords(x, z).biomeName: the 1.7.10 name of the biome there ("Plains", "Ocean"...). */
	public abstract String getBiomeName(int x, int z);

	/** The chest at a position (World.getTileEntity as a TileEntityChest), or null. */
	public abstract Chest chestAt(int x, int y, int z);

	/** The mob spawner at a position (World.getTileEntity as a TileEntityMobSpawner), or null. */
	public abstract Spawner spawnerAt(int x, int y, int z);

	/** EntityList.createEntityByName: an entity to place, by OreSpawn's (or vanilla 1.7.10's) entity name. */
	public Ent createEntityByName(String name) {
		return new Ent(name);
	}

	/** World.spawnEntityInWorld. */
	public abstract boolean spawnEntityInWorld(Ent entity);

	/** ItemDoor.placeDoorBlock: a two-block door, its hinge on the side with more solid blocks (as 1.7.10 did). */
	public abstract void placeDoorBlock(int x, int y, int z, int dir, LB door);

	/** A chest's inventory. */
	public abstract static class Chest {
		/** TileEntityChest.setInventorySlotContents. */
		public abstract void setInventorySlotContents(int slot, Stack stack);
	}

	/** A mob spawner. */
	public abstract static class Spawner {
		/** MobSpawnerBaseLogic.setEntityName: OreSpawn's (or vanilla 1.7.10's) entity name. */
		public abstract void setEntityName(String name);
	}

	/** An item stack by item id ("minecraft:compass", "orespawn:ruby"...). */
	public record Stack(String item, int count) {
	}

	/** An entity waiting to be spawned: its 1.7.10 name and where. */
	public static final class Ent {
		public final String name;
		public double x;
		public double y;
		public double z;
		public float yaw;
		public float pitch;
		public boolean persistent;

		public Ent(String name) {
			this.name = name;
		}

		/** Entity.setLocationAndAngles. */
		public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.yaw = yaw;
			this.pitch = pitch;
		}

		/** EntityLiving.func_110163_bv: never despawns. */
		public void setPersistent() {
			this.persistent = true;
		}
	}
}
