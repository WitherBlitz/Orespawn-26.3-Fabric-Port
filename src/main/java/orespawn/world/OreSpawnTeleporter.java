package orespawn.world;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * OreSpawn's OreSpawnTeleporter: how its creatures take a player between the overworld and OreSpawn's dimensions. There's
 * no portal. The player arrives at the same x and z (scaled like vanilla travel, so 8 times further out when leaving the
 * Nether), on the first standing spot found scanning down from the sky: two blocks of air over a solid block (or over
 * short grass or a fern on a solid block). If that column has no such spot at its top (water, say), it tries columns
 * nearby, further out as it goes, up to 1000 of them; if none works, it uses the first two air blocks over anything at
 * the start. Tamed pets that aren't sitting, within 24 blocks (12 up or down), come along.
 * <p>
 * Changes from 1.7.10: the scan starts at y 180 as OreSpawn's did, or higher where the terrain is higher (26.3's mountains
 * pass 180, and OreSpawn's start would land you in a cave inside them). Columns are found with floor() rather than
 * OreSpawn's (int) cast, which put you one block off the scanned column at negative coordinates. The search gives up
 * after 5 seconds (each new column can mean generating a chunk, and 26.3's watchdog stops a server stuck too long).
 */
public final class OreSpawnTeleporter {
	private static final int RETRY = Integer.MIN_VALUE;
	private static final int EXHAUSTED = Integer.MIN_VALUE + 1;
	private static final long SEARCH_NANOS = 5_000_000_000L;

	private OreSpawnTeleporter() {
	}

	/**
	 * Sends the player to {@code dimension}, or back to the overworld if they're already in it, as OreSpawn's ants,
	 * termite and butterfly did. If that dimension isn't in the game (not registered yet), it tells the player and does
	 * nothing.
	 */
	public static void travel(ServerPlayer player, ResourceKey<Level> dimension) {
		ServerLevel from = player.level();
		ResourceKey<Level> target = from.dimension() == dimension ? Level.OVERWORLD : dimension;
		ServerLevel to = from.getServer().getLevel(target);
		if (to == null) {
			player.sendSystemMessage(Component.translatable("message.orespawn.dimension_missing"));
			return;
		}
		double scale = DimensionType.getTeleportationScale(from.dimensionType(), to.dimensionType());
		BlockPos start = to.getWorldBorder().clampToBounds(player.getX() * scale, player.getY(), player.getZ() * scale);
		Vec3 arrival = arrival(to, start.getX(), start.getZ());

		Vec3 old = player.position();
		List<TamableAnimal> pets = from.getEntitiesOfClass(TamableAnimal.class,
			new AABB(old.x - 24.0, old.y - 12.0, old.z - 24.0, old.x + 24.0, old.y + 12.0, old.z + 24.0),
			pet -> !pet.isOrderedToSit() && pet.isOwnedBy(player));
		float yRot = player.getYRot();
		player.teleport(new TeleportTransition(to, arrival, Vec3.ZERO, yRot, 0.0F, TeleportTransition.DO_NOTHING));
		for (TamableAnimal pet : pets) {
			pet.teleport(new TeleportTransition(to, arrival, Vec3.ZERO, yRot, 0.0F, TeleportTransition.DO_NOTHING));
		}
	}

	/** OreSpawnTeleporter.justPutMe: where something leaving for (x, z) in {@code level} arrives. */
	public static Vec3 arrival(ServerLevel level, int x, int z) {
		RandomSource random = level.getRandom();
		long deadline = System.nanoTime() + SEARCH_NANOS;
		int posX = x;
		int posZ = z;
		int posY = EXHAUSTED;
		for (int i = 0; i < 1000 && System.nanoTime() < deadline; i++) {
			posY = scanColumn(level, posX, posZ);
			if (posY != RETRY) break;
			posX = x + random.nextInt(3 + i / 5) - random.nextInt(3 + i / 5);
			if (i > 100) posX += random.nextInt(2 + i / 5) - random.nextInt(2 + i / 5);
			if (i > 500) posX += random.nextInt(2 + i / 5) - random.nextInt(2 + i / 5);
			posZ = z + random.nextInt(3 + i / 5) - random.nextInt(3 + i / 5);
			if (i > 100) posZ += random.nextInt(2 + i / 5) - random.nextInt(2 + i / 5);
			if (i > 500) posZ += random.nextInt(2 + i / 5) - random.nextInt(2 + i / 5);
		}
		if (posY == RETRY || posY == EXHAUSTED) {
			// No standing spot: the first two air blocks over anything, at the start.
			posX = x;
			posZ = z;
			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			posY = top(level, posX, posZ);
			while (posY > bottom(level) && (!level.getBlockState(pos.set(posX, posY + 1, posZ)).isAir()
				|| !level.getBlockState(pos.set(posX, posY, posZ)).isAir() || level.getBlockState(pos.set(posX, posY - 1, posZ)).isAir())) {
				posY--;
			}
		}
		return new Vec3(posX + 0.5, posY, posZ + 0.5);
	}

	/**
	 * One column of justPutMe's search, from the top down: the y to stand at, RETRY when the column's first opening has
	 * nothing solid under it (or it's buried under three ground blocks after an opening), EXHAUSTED when it reaches the
	 * bottom (OreSpawn then stopped searching).
	 */
	private static int scanColumn(ServerLevel level, int x, int z) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int inARow = 0;
		boolean airFound = false;
		for (int y = top(level, x, z); y > bottom(level); y--) {
			BlockState above = level.getBlockState(pos.set(x, y + 1, z));
			if (!above.isAir()) {
				if (isGround(above)) inARow++;
				if (airFound && inARow >= 3) return RETRY;
				continue;
			}
			inARow = 0;
			if (!level.getBlockState(pos.set(x, y, z)).isAir()) continue;
			airFound = true;
			BlockState below = level.getBlockState(pos.set(x, y - 1, z));
			if (below.isAir()) continue;
			if (below.isSolid()) return y;
			if ((below.is(Blocks.SHORT_GRASS) || below.is(Blocks.FERN)) && level.getBlockState(pos.set(x, y - 2, z)).isSolid()) return y - 1;
			return RETRY;
		}
		return EXHAUSTED;
	}

	/** OreSpawn started at y 180; here, at 180 or the column's surface if that's higher, below the build limit. */
	private static int top(ServerLevel level, int x, int z) {
		LevelChunk chunk = level.getChunk(x >> 4, z >> 4);
		int surface = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15) + 1;
		return Math.min(level.getMaxY() - 1, Math.max(180, surface));
	}

	/** OreSpawn stopped at y 1, one above 1.7.10's floor. */
	private static int bottom(ServerLevel level) {
		return level.getMinY() + 1;
	}

	/** OreSpawnTeleporter.isGroundBlock. */
	private static boolean isGround(BlockState state) {
		return state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL) || state.is(Blocks.GRASS_BLOCK)
			|| state.is(Blocks.STONE) || state.is(Blocks.END_STONE) || state.is(Blocks.NETHERRACK) || state.is(Blocks.COBBLESTONE)
			|| state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.SANDSTONE) || state.is(Blocks.CHISELED_SANDSTONE)
			|| state.is(Blocks.CUT_SANDSTONE) || state.is(Blocks.FARMLAND);
	}
}
