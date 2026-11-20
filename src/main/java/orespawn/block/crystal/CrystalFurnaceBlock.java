package orespawn.block.crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import orespawn.content.Crystal;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Crystal Furnace (CrystalFurnace, on and off): a furnace that smelts anything a furnace smelts in 150
 * ticks, burns Crystal Energy for 20000 ticks, and lights up (light 9) while burning, with flames and smoke inside its
 * see-through frame. It shows the same front lit or not, as OreSpawn's did. See {@link CrystalFurnaceBlockEntity}.
 */
public class CrystalFurnaceBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	public CrystalFurnaceBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CrystalFurnaceBlockEntity furnace) {
			player.openMenu((MenuProvider) furnace);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CrystalFurnaceBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level instanceof ServerLevel server
			? createTickerHelper(type, Crystal.CRYSTAL_FURNACE_ENTITY, (l, p, s, e) -> CrystalFurnaceBlockEntity.serverTick(server, p, s, e))
			: null;
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		Containers.updateNeighboursAfterDestroy(state, level, pos);
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
	}

	/** Flames and smoke somewhere inside the frame (OreSpawn's random offsets, not vanilla's front face). */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT)) return;
		double x = pos.getX() + 0.5;
		double y = pos.getY() + random.nextFloat() * 6.0F / 16.0F;
		double z = pos.getZ() + 0.5;
		double a = random.nextFloat() * 0.6F - 0.3F;
		double b = random.nextFloat() * 0.6F - 0.3F;
		Direction facing = state.getValue(FACING);
		double px = switch (facing) {
			case WEST -> x - a;
			case EAST -> x + a;
			default -> x + b;
		};
		double pz = switch (facing) {
			case NORTH -> z - a;
			case SOUTH -> z + a;
			default -> z + b;
		};
		level.addParticle(ParticleTypes.SMOKE, px, y, pz, 0.0, 0.0, 0.0);
		level.addParticle(ParticleTypes.FLAME, px, y, pz, 0.0, 0.0, 0.0);
	}

	@Override
	protected int getLightDampening(BlockState state) {
		return 0;
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}
}
