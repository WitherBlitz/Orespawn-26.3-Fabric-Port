package orespawn.item.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import orespawn.registry.ModBlocks;

/**
 * OreSpawn's Instant Survival Shelter: used on a block straight north, south, east or west of you, it builds a 7x7 oak
 * plank hut around you with a cobblestone floor, a row of glass windows under the roof and a doorway (no door) on the
 * side you clicked. Along one side wall, facing into the room, are a furnace, a crafting table and a chest of
 * supplies: compass, map, 8 raw porkchops, 32 torches, 16 coal, 2 beds, a door, iron pickaxe, sword and axe, a bucket, 4
 * salt ore and another chest. Whatever was in the way is replaced.
 */
public class InstantShelterItem extends Item {
	public InstantShelterItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null) return InteractionResult.PASS;
		int[] d = Placement.lineFromPlayer(player, context.getClickedPos());
		if (d == null) return InteractionResult.PASS;
		Level level = context.getLevel();
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.5F);
		int dx = d[0], dz = d[1];
		int x = Mth.floor(player.getX()), y = Mth.floor(player.getY()) - 1, z = Mth.floor(player.getZ());
		int size = 3, height = 3;
		for (int i = -size; i <= size; i++) {
			for (int j = -size; j <= size; j++) {
				for (int k = 0; k <= height + 1; k++) {
					Block block;
					if (k == height + 1) {
						block = Blocks.OAK_PLANKS;
					} else if (k == 0) {
						block = Blocks.COBBLESTONE;
					} else if (i != size && j != size && i != -size && j != -size) {
						block = Blocks.AIR;
					} else if (k == height) {
						block = Blocks.GLASS;
					} else if (i == dx * size && j == dz * size) {
						block = Blocks.AIR;
					} else {
						block = Blocks.OAK_PLANKS;
					}
					level.setBlockAndUpdate(new BlockPos(x + i, y + k, z + j), block.defaultBlockState());
				}
			}
		}
		// OreSpawn's metadata 2-5: the furniture faces into the room.
		Direction facing = dx < 0 ? Direction.SOUTH : dx > 0 ? Direction.NORTH : dz < 0 ? Direction.EAST : Direction.WEST;
		place(level, x, y, z, dx, dz, 2, Blocks.FURNACE.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing));
		place(level, x, y, z, dx, dz, 1, Blocks.CRAFTING_TABLE.defaultBlockState());
		BlockPos chestPos = place(level, x, y, z, dx, dz, 0, Blocks.CHEST.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing));
		if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
			ItemStack[] supplies = {new ItemStack(Items.COMPASS), new ItemStack(Items.MAP), new ItemStack(Items.PORKCHOP, 8),
				new ItemStack(Items.TORCH, 32), new ItemStack(Items.COAL, 16), new ItemStack(Items.BED.red()), new ItemStack(Items.BED.red()),
				new ItemStack(Items.OAK_DOOR), new ItemStack(Items.IRON_PICKAXE), new ItemStack(Items.IRON_SWORD), new ItemStack(Items.IRON_AXE),
				new ItemStack(Items.BUCKET), new ItemStack(ModBlocks.SALT_ORE, 4), new ItemStack(Items.CHEST)};
			for (int slot = 0; slot < supplies.length; slot++) chest.setItem(slot, supplies[slot]);
		}
		context.getItemInHand().consume(1, player);
		return InteractionResult.SUCCESS;
	}

	/** OreSpawn's furniture row: {@code along} blocks towards the doorway, two blocks to the side, one up. */
	private static BlockPos place(Level level, int x, int y, int z, int dx, int dz, int along, BlockState state) {
		BlockPos pos = new BlockPos(x + along * dx + 2 * dz, y + 1, z + along * dz + 2 * dx);
		level.setBlockAndUpdate(pos, state);
		return pos;
	}
}
