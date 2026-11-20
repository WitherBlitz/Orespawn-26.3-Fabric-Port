package orespawn.entity.crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import orespawn.entity.AppleCow;

/**
 * OreSpawn's Crystal Apple Cow (CrystalCow, a RedCow): an Apple Cow of the Crystal dimension that never despawns, breeds
 * its own kind with wheat, and drops 0-2 Crystal Apples (more with Looting) besides the Apple Cow's apples, leather and
 * beef (its loot table). It spawns, rarely, in the Crystal dimension; like any 1.7.10 animal it needs grass under it
 * to spawn naturally, but OreSpawn's chunk-generation spawning didn't check that, so new Crystal chunks get them.
 */
public class CrystalCow extends AppleCow {
	public CrystalCow(EntityType<? extends CrystalCow> type, Level level) {
		super(type, level);
	}

	@Override
	public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
		return reason == EntitySpawnReason.CHUNK_GENERATION || super.checkSpawnRules(level, reason);
	}

	/** EntityAnimal.getCanSpawnHere (grass and light above 8), skipped by SpawnerAnimals.performWorldGenSpawning. */
	public static boolean checkSpawn(EntityType<? extends Animal> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (reason == EntitySpawnReason.CHUNK_GENERATION || EntitySpawnReason.isSpawner(reason)) return true;
		return Animal.checkAnimalSpawnRules(type, level, reason, pos, random);
	}
}
