package orespawn.item.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import orespawn.content.Tools;

/**
 * OreSpawn's Instant Survival Garden: used on a block straight north, south, east or west of you, it clears a 15 wide,
 * 18 long, 10 high space starting at that block, grasses its floor, and plants 16 rows of fresh crops across it: radish,
 * lettuce, carrots, water, potatoes, wheat, tomatoes, water, corn, strawberries, sugar cane on sand, water and melons,
 * with a grass strip on each side. OreSpawn's own crops (radish, lettuce, tomato, corn and strawberry) come from the crops
 * group; until that exists their rows are bare farmland.
 */
public class InstantGardenItem extends Item {
	public InstantGardenItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null) return InteractionResult.PASS;
		BlockPos clicked = context.getClickedPos();
		int[] d = Placement.lineFromPlayer(player, clicked);
		if (d == null) return InteractionResult.PASS;
		Level level = context.getLevel();
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.5F);
		int dx = d[0], dz = d[1];
		int x = clicked.getX(), y = Mth.floor(player.getY()), z = clicked.getZ();
		int height = 10, width = 7, length = 18;
		for (int i = 0; i < height; i++) {
			for (int k = 0; k < length; k++) {
				for (int j = -width; j <= width; j++) {
					set(level, x + k * dx + j * dz, y + i, z + k * dz + j * dx, Blocks.AIR);
					if (i == 0) set(level, x + k * dx + j * dz, y - 1, z + k * dz + j * dx, Blocks.GRASS_BLOCK);
				}
			}
		}
		Block[] crops = {null, Tools.lazyBlock("radish_plant"), Tools.lazyBlock("lettuce_plant"), Blocks.CARROTS, null, Blocks.POTATOES,
			Blocks.WHEAT, Tools.lazyBlock("tomato_plant"), null, Tools.lazyBlock("corn_plant"), Tools.lazyBlock("strawberry_plant"), null, null,
			Blocks.MELON_STEM};
		for (int k = 1; k < length - 1; k++) {
			for (int j = -width; j <= width; j++) {
				int row = j + width;
				int bx = x + k * dx + j * dz, bz = z + k * dz + j * dx;
				if (row == 4 || row == 8 || row == 12) {
					set(level, bx, y - 1, bz, Blocks.WATER);
					set(level, bx, y - 2, bz, Blocks.COBBLESTONE);
				} else if (row == 11) {
					set(level, bx, y - 2, bz, Blocks.COBBLESTONE);
					set(level, bx, y - 1, bz, Blocks.SAND);
					set(level, bx, y, bz, Blocks.SUGAR_CANE);
				} else if (row >= 1 && row <= 13) {
					set(level, bx, y - 1, bz, Blocks.FARMLAND);
					if (crops[row] != null) set(level, bx, y, bz, crops[row]);
				}
			}
		}
		context.getItemInHand().consume(1, player);
		return InteractionResult.SUCCESS;
	}

	/** OreSpawn set these with flag 2: the clients see them, but neighbours aren't updated. */
	private static void set(Level level, int x, int y, int z, Block block) {
		level.setBlock(new BlockPos(x, y, z), block.defaultBlockState(), Block.UPDATE_CLIENTS);
	}
}
