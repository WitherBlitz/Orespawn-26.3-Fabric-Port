package orespawn.block.crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.OreSpawn;

import java.util.Optional;

/**
 * OreSpawn's Crystal Termite Nest (CrystalAntBlock): on every random tick, unless it's raining, 2-7 Termites
 * ({@code orespawn:termite}, looked up by id) crawl out on top of it if there's air there.
 */
public class CrystalTermiteNestBlock extends CrystalBlock {
	public CrystalTermiteNestBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (level.isRaining() || !level.isEmptyBlock(pos.above())) return;
		Optional<EntityType<?>> termite = BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id("termite"));
		if (termite.isEmpty()) return;
		int count = random.nextInt(6) + 2;
		for (int i = 0; i < count; i++) {
			Entity entity = termite.get().create(level, EntitySpawnReason.TRIGGERED);
			if (entity == null) continue;
			entity.snapTo(pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
			level.addFreshEntity(entity);
		}
	}
}
