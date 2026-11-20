package orespawn.entity.spooky;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

/**
 * OreSpawn's Ender Reaper: a winged, scythe-swinging enderman, the Ender Knight's bigger cousin (OreSpawn's class was a
 * copy of the knight's). It notices a staring player from 81 blocks. 90 health, 18 damage, 8 armor. Drops 0-1 Eyes of
 * Ender (more with Looting). Spawns at night in the dark at y 30 or higher, never within 16 blocks of another reaper.
 */
public class EnderReaper extends EnderKnight {
	public EnderReaper(EntityType<? extends EnderReaper> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return attributes(90.0, 0.37, 18.0, 8.0);
	}

	@Override
	protected double stareRange() {
		return 81.0;
	}

	@Override
	protected Item dropItem() {
		return Items.ENDER_EYE;
	}

	/** EnderReaper.getCanSpawnHere: the knight's rule, and no other reaper within 16 blocks (8 up or down). */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!EnderKnight.checkSpawn(type, level, reason, pos, random)) return false;
		AABB box = type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(16.0, 8.0, 16.0);
		return level.getEntitiesOfClass(EnderReaper.class, box, e -> true).isEmpty();
	}
}
