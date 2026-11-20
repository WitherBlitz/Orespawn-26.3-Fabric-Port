package orespawn.entity.dinosaurs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

/**
 * OreSpawn's scan_it loop (Baryonyx, Camarasaurus, Velocity Raptor): looks for food on the faces of growing boxes around
 * a point (shell i is i blocks out sideways and at most 2 up or down), stopping at the first shell with any, and keeps
 * the closest block found (squared distance as OreSpawn measured it). Past {@code skipFrom} every other shell is skipped.
 */
final class Grazing {
	record Food(BlockPos pos, int distSq) {}

	private BlockPos best;
	private int closest = 99999;

	private Grazing() {
	}

	static @Nullable Food find(Level level, BlockPos origin, int shells, int skipFrom, Predicate<BlockState> food) {
		Grazing g = new Grazing();
		for (int i = 1; i < shells; i++) {
			if (g.shell(level, origin, i, Math.min(i, 2), i, food)) break;
			if (i >= skipFrom) i++;
		}
		return g.best == null ? null : new Food(g.best, g.closest);
	}

	private boolean shell(Level level, BlockPos o, int dx, int dy, int dz, Predicate<BlockState> food) {
		boolean found = false;
		for (int i = -dy; i <= dy; i++) {
			for (int j = -dz; j <= dz; j++) {
				found |= check(level, o.offset(dx, i, j), dx * dx + j * j + i * i, food);
				found |= check(level, o.offset(-dx, i, j), dx * dx + j * j + i * i, food);
			}
		}
		for (int i = -dx; i <= dx; i++) {
			for (int j = -dz; j <= dz; j++) {
				found |= check(level, o.offset(i, dy, j), dy * dy + j * j + i * i, food);
				found |= check(level, o.offset(i, -dy, j), dy * dy + j * j + i * i, food);
			}
		}
		for (int i = -dx; i <= dx; i++) {
			for (int j = -dy; j <= dy; j++) {
				found |= check(level, o.offset(i, j, dz), dz * dz + j * j + i * i, food);
				found |= check(level, o.offset(i, j, -dz), dz * dz + j * j + i * i, food);
			}
		}
		return found;
	}

	/** Counts only when it's closer than the best so far, as OreSpawn did. */
	private boolean check(Level level, BlockPos pos, int d, Predicate<BlockState> food) {
		if (d >= closest || !food.test(level.getBlockState(pos))) return false;
		closest = d;
		best = pos;
		return true;
	}
}
