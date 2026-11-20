package orespawn.block.crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.OreSpawn;

import java.util.Optional;

/**
 * OreSpawn's Crystalized Rats and Crystalized Fairies (OreBasicStone): Kyanite with creatures frozen inside. When a
 * player breaks one, 1-10 Rats or 1-6 Fairies burst out (looked up by id, so nothing comes out while the creature isn't
 * registered); the block still drops itself.
 */
public class CrystalizedBlock extends CrystalBlock {
	private final String creature;
	private final int most;

	public CrystalizedBlock(String creature, int most, Properties properties) {
		super(properties);
		this.creature = creature;
		this.most = most;
	}

	@Override
	public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
		super.destroy(level, pos, state);
		if (!(level instanceof ServerLevel server)) return;
		Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id(this.creature));
		if (type.isEmpty()) return;
		RandomSource r = server.getRandom();
		int count = 1 + r.nextInt(this.most);
		for (int i = 0; i < count; i++) {
			Entity entity = type.get().create(server, EntitySpawnReason.TRIGGERED);
			if (entity == null) continue;
			entity.snapTo(pos.getX() + 0.5 + (r.nextFloat() - r.nextFloat()) * 0.2, pos.getY() + 0.01,
				pos.getZ() + 0.5 + (r.nextFloat() - r.nextFloat()) * 0.2, r.nextFloat() * 360.0F, 0.0F);
			server.addFreshEntity(entity);
		}
	}
}
