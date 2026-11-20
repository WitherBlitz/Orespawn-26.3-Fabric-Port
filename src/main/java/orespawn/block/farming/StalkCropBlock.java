package orespawn.block.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * OreSpawn's sugar-cane-like crops: BlockCorn, BlockTomato, BlockQuinoa and BlockLettuce. OreSpawn made each of them
 * four blocks (Plant1 to Plant4); here they're one block with {@code stage} 0-3 for those four, and {@code age} for
 * the 1.7.10 metadata that counted random ticks between growth steps.
 * <ul>
 * <li>Corn and tomatoes grow from their top block (stage 0, or a bare stage 1 stalk): when its age reaches the
 * threshold, the plant either grows one block (the top becomes stalk, a new top goes above) while it's shorter than a
 * height rolled 4-7 (corn) or 3-5 (tomatoes) on every tick, or ripens its stalk one step (stage 1 to 2 to 3).</li>
 * <li>Quinoa grows the same way (height 2-4), but ripens its top block instead (stage 0 to 2 to 3).</li>
 * <li>Lettuce is a single block that goes through its four stages, one every five random ticks.</li>
 * </ul>
 * Only fully ripe (stage 3) blocks drop anything: corn 1-2, tomatoes 2-5, lettuce 2-4, quinoa 3-5 (loot tables). They
 * stand on grass, dirt or farmland (quinoa also on Crystal Grass) or on themselves, have no collision, break
 * instantly, and pop off when what holds them up goes. Like 1.7.10's reeds they're tinted with the grass colour.
 * Their items plant them only on farmland ({@link orespawn.item.farming.FarmlandSeedItem}).
 */
public class StalkCropBlock extends Block {
	public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 3);
	public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
	private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 16.0);

	public enum Kind { CORN, TOMATO, QUINOA, LETTUCE }

	private final Kind kind;
	private final Supplier<? extends ItemLike> crop;

	public StalkCropBlock(Kind kind, Supplier<? extends ItemLike> crop, BlockBehaviour.Properties properties) {
		super(properties);
		this.kind = kind;
		this.crop = crop;
		registerDefaultState(stateDefinition.any().setValue(STAGE, 0).setValue(AGE, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(STAGE, AGE);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockState below = level.getBlockState(pos.below());
		if (below.isAir()) return false;
		return below.is(this) || Soil.ground(below) || kind == Kind.QUINOA && below.is(Soil.CRYSTAL_GRASS);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour,
			BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		if (!state.canSurvive(level, pos)) ticks.scheduleTick(pos, this, 1);
		return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.canSurvive(level, pos)) level.destroyBlock(pos, true);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		switch (kind) {
			case CORN -> growTall(state, level, pos, random, 4 + random.nextInt(4), 6, true);
			case TOMATO -> growTall(state, level, pos, random, 3 + random.nextInt(3), 5, false);
			case QUINOA -> growQuinoa(state, level, pos, random);
			case LETTUCE -> growLettuce(state, level, pos);
		}
	}

	/** BlockCorn / BlockTomato.updateTick. */
	private void growTall(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, int maxHeight, int threshold, boolean corn) {
		int stage = state.getValue(STAGE);
		if (stage != 0 && stage != 1) return;
		if (!level.getBlockState(pos.above()).isAir()) return;
		int age = state.getValue(AGE);
		int height = 1;
		boolean ripening = false;
		for (int i = 1; i < 10; i++) {
			BlockState below = level.getBlockState(pos.below(i));
			if (!below.is(this)) break;
			height++;
			if (below.getValue(STAGE) >= 2) ripening = true;
		}
		if (ripening) maxHeight = height;
		if (age < threshold - maxHeight / 3) {
			level.setBlock(pos, state.setValue(AGE, Math.min(15, age + 1)), Block.UPDATE_CLIENTS);
			return;
		}
		if (height < maxHeight) {
			level.setBlock(pos.above(), defaultBlockState(), Block.UPDATE_CLIENTS);
			level.setBlock(pos, defaultBlockState().setValue(STAGE, 1), Block.UPDATE_CLIENTS);
			return;
		}
		// Ripen the stalk one step: corn skips its bottom block (i < maxHeight - 1), tomatoes don't.
		int from = corn ? 1 : 0;
		int to = corn ? maxHeight - 1 : maxHeight;
		for (int i = from; i < to; i++) {
			BlockPos p = pos.below(i);
			BlockState s = level.getBlockState(p);
			if (!s.is(this)) continue;
			int st = s.getValue(STAGE);
			if (st == 1 || st == 2) level.setBlock(p, defaultBlockState().setValue(STAGE, st + 1), Block.UPDATE_CLIENTS);
		}
		BlockState now = level.getBlockState(pos);
		if (now.is(this)) level.setBlock(pos, now.setValue(AGE, 0), Block.UPDATE_CLIENTS);
	}

	/** BlockQuinoa.updateTick: its top (stage 0, or an already ripening stage 2) grows or ripens. */
	private void growQuinoa(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int stage = state.getValue(STAGE);
		if (stage != 0 && stage != 2) return;
		int maxHeight = 2 + random.nextInt(3);
		if (!level.getBlockState(pos.above()).isAir()) return;
		int age = state.getValue(AGE);
		int height = 1;
		boolean ripening = false;
		for (int i = 1; i < 10; i++) {
			BlockState below = level.getBlockState(pos.below(i));
			if (!below.is(this)) break;
			height++;
			if (below.getValue(STAGE) >= 2) ripening = true;
		}
		if (ripening) maxHeight = height;
		if (age < 5 - maxHeight / 3) {
			level.setBlock(pos, state.setValue(AGE, Math.min(15, age + 1)), Block.UPDATE_CLIENTS);
		} else if (height < maxHeight) {
			level.setBlock(pos.above(), defaultBlockState(), Block.UPDATE_CLIENTS);
			level.setBlock(pos, defaultBlockState().setValue(STAGE, 1), Block.UPDATE_CLIENTS);
		} else {
			level.setBlock(pos, defaultBlockState().setValue(STAGE, stage == 0 ? 2 : 3), Block.UPDATE_CLIENTS);
		}
	}

	/** BlockLettuce.updateTick. */
	private void growLettuce(BlockState state, ServerLevel level, BlockPos pos) {
		int age = state.getValue(AGE);
		int stage = state.getValue(STAGE);
		if (age < 4) {
			level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
		} else if (stage < 3) {
			level.setBlock(pos, defaultBlockState().setValue(STAGE, stage + 1), Block.UPDATE_CLIENTS);
		}
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(crop.get());
	}
}
