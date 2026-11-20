package orespawn.block.portals;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * OreSpawn's AntBlock: the ant nests (black, red, rainbow and unstable ants, and termites). On each random tick, unless
 * it's raining, a nest with air above it lets out 2 to 7 of its ants. A nest breaks instantly and drops itself. It's
 * tinted with the biome's grass colour on every face (1.7.10 tinted all faces of any block that wasn't vanilla grass),
 * and bone meal works on it as on a grass block, since OreSpawn's AntBlock was one. It never spreads.
 */
public class AntNestBlock extends Block implements BonemealableBlock {
	private final Supplier<? extends EntityType<? extends Mob>> ant;

	public AntNestBlock(Properties properties, Supplier<? extends EntityType<? extends Mob>> ant) {
		super(properties);
		this.ant = ant;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (level.isRaining() || !level.getBlockState(pos.above()).isAir()) return;
		int howMany = random.nextInt(6) + 2;
		for (int i = 0; i < howMany; i++) release(level, ant.get(), pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5);
	}

	/** AntBlock.spawnCreature: one mob at (x, y, z), facing a random way, making its living sound. */
	public static void release(ServerLevel level, EntityType<? extends Mob> type, double x, double y, double z) {
		Mob mob = type.create(level, EntitySpawnReason.TRIGGERED);
		if (mob == null) return;
		mob.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
		level.addFreshEntity(mob);
		mob.playAmbientSound();
	}

	private static GrassBlock grass() {
		return (GrassBlock) Blocks.GRASS_BLOCK;
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
		return grass().isValidBonemealTarget(level, pos, state, source);
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
		return grass().isBonemealSuccess(level, random, pos, state, source);
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
		grass().performBonemeal(level, random, pos, state, source);
	}

	@Override
	public BonemealableBlock.Type getType() {
		return grass().getType();
	}
}
