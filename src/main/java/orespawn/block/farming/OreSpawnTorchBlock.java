package orespawn.block.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * A standing OreSpawn torch: the Extreme Torch ({@code repel} null; it summons a Cephadrome when set on an Eye-of-Ender
 * Block) or a Creeper/Kraken Repellent (it pushes those away every 10 ticks). Both give off smoke, flame and red dust;
 * the repellents' flame sits higher. See {@link Torches}.
 */
public class OreSpawnTorchBlock extends TorchBlock {
	private final Torches.@Nullable Repel repel;

	public OreSpawnTorchBlock(Torches.@Nullable Repel repel, BlockBehaviour.Properties properties) {
		super(ParticleTypes.FLAME, properties);
		this.repel = repel;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		Torches.particles(level, pos, repel == null ? 0.0 : 0.21, 0, 0);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
		super.setPlacedBy(level, pos, state, by, itemStack);
		if (repel == null) Torches.summonCephadrome(level, pos);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		if (repel != null) level.scheduleTick(pos, this, Torches.REPEL_TICKS);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (repel == null) return;
		Torches.repel(level, pos, repel);
		level.scheduleTick(pos, this, Torches.REPEL_TICKS);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		tick(state, level, pos, random);
	}
}
