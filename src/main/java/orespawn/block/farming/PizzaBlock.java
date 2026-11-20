package orespawn.block.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * OreSpawn's BlockPizza: a 4-pixel-high cake of six slices. Right or left clicking it eats a slice (4 hunger, 0.2
 * saturation) when you're hungry; the sixth slice finishes it. It stands only on full, solid blocks and breaks with
 * nothing dropped. OreSpawn left its hardness at 0, so a left click both ate a slice and broke it; it has the cake's
 * 0.5 here so a left click just eats.
 */
public class PizzaBlock extends Block {
	public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 5);
	private static final VoxelShape[] SHAPES = new VoxelShape[6];
	private static final VoxelShape[] COLLISION = new VoxelShape[6];

	static {
		for (int bite = 0; bite < 6; bite++) {
			SHAPES[bite] = Block.box(1 + bite * 2, 0.0, 1.0, 15.0, 4.0, 15.0);
			COLLISION[bite] = Block.box(1 + bite * 2, 0.0, 1.0, 15.0, 3.0, 15.0);
		}
	}

	public PizzaBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(BITES, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BITES);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES[state.getValue(BITES)];
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return COLLISION[state.getValue(BITES)];
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		return use(state, level, pos, player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
	}

	@Override
	protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
		if (!level.isClientSide()) use(state, level, pos, player);
	}

	/** What a click does; true if it did something. */
	protected boolean use(BlockState state, Level level, BlockPos pos, Player player) {
		if (!player.canEat(false)) return false;
		if (level.isClientSide()) return true;
		player.getFoodData().eat(4, 0.2F);
		level.gameEvent(player, GameEvent.EAT, pos);
		takeSlice(state, level, pos);
		return true;
	}

	protected static void takeSlice(BlockState state, Level level, BlockPos pos) {
		int bites = state.getValue(BITES) + 1;
		if (bites >= 6) {
			level.removeBlock(pos, false);
		} else {
			level.setBlock(pos, state.setValue(BITES, bites), Block.UPDATE_CLIENTS);
		}
	}

	/** BlockPizza.canBlockStay: the block below is a normal cube. */
	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return level.getBlockState(below).isRedstoneConductor(level, below);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour,
			BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		return directionToNeighbour == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
			: super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
	}
}
