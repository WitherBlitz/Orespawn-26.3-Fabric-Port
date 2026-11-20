package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's moth (EntityLunaMoth): luna, eye, dark and fire moths, out at night, drawn to the nearest torch (or
 * Extreme Torch) within 14
 * blocks (a 1 in 10 chance each tick while not picking a new spot). The luna moth glows with the charged-creeper
 * swirl. OreSpawn scanned shells of blocks outward; this looks only in the chunk sections that hold a torch at all
 * (from their palettes), so a moth in the wild costs next to nothing.
 */
public class Moth extends Butterfly {
	public Moth(EntityType<? extends Moth> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Butterfly.createAttributes();
	}

	@Override
	protected int spread() {
		return 10;
	}

	private static boolean isTorch(BlockState state) {
		return state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH) || state.is(orespawn.content.Farming.EXTREME_TORCH)
			|| state.is(orespawn.content.Farming.EXTREME_TORCH_WALL);
	}

	@Override
	protected @Nullable BlockPos attraction(ServerLevel level) {
		if (level.isBrightOutside() || random.nextInt(10) != 0) return null;
		BlockPos me = blockPosition();
		int r = 14;
		BlockPos best = null;
		double bestD = Double.MAX_VALUE;
		for (int sx = SectionPos.blockToSectionCoord(me.getX() - r); sx <= SectionPos.blockToSectionCoord(me.getX() + r); sx++) {
			for (int sz = SectionPos.blockToSectionCoord(me.getZ() - r); sz <= SectionPos.blockToSectionCoord(me.getZ() + r); sz++) {
				if (!level.hasChunk(sx, sz)) continue;
				var chunk = level.getChunk(sx, sz);
				for (int sy = SectionPos.blockToSectionCoord(me.getY() - r); sy <= SectionPos.blockToSectionCoord(me.getY() + r); sy++) {
					int index = level.getSectionIndexFromSectionY(sy);
					if (index < 0 || index >= chunk.getSectionsCount()) continue;
					LevelChunkSection section = chunk.getSection(index);
					if (section.hasOnlyAir() || !section.maybeHas(Moth::isTorch)) continue;
					for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
						if (!isTorch(section.getBlockState(x, y, z))) continue;
						BlockPos p = new BlockPos(SectionPos.sectionToBlockCoord(sx) + x, SectionPos.sectionToBlockCoord(sy) + y,
							SectionPos.sectionToBlockCoord(sz) + z);
						if (Math.abs(p.getX() - me.getX()) > r || Math.abs(p.getY() - me.getY()) > r || Math.abs(p.getZ() - me.getZ()) > r) continue;
						double d = p.distSqr(me);
						if (d < bestD) {
							bestD = d;
							best = p;
						}
					}
				}
			}
		}
		return best == null ? null : best.above();
	}

	/** EntityLunaMoth.getCanSpawnHere: in air, at night, at y 50 or higher. */
	public static boolean checkMothSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return level.isEmptyBlock(pos) && !level.getLevel().isBrightOutside()
			&& (pos.getY() >= 50 || level.getLevel().dimension() == orespawn.world.OreSpawnDimensions.ISLANDS);
	}
}
