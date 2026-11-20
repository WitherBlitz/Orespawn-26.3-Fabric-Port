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

/**
 * OreSpawn's Zoo Cages ({@code ZooCage}): used on any block, it builds a glass cage around you with a quartz floor and
 * roof, replacing whatever was there. OreSpawn's sizes 3, 5, 9, 13 and 17 give cages 5, 7, 11, 15 and 19 blocks
 * across with 2, 3, 5, 7 and 9 blocks of room inside.
 */
public class ZooCageItem extends Item {
	private final int size;

	public ZooCageItem(Properties properties, int size) {
		super(properties);
		this.size = size;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null) return InteractionResult.PASS;
		Level level = context.getLevel();
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.5F);
		int half = size / 2 + 1;
		int x = Mth.floor(player.getX()), y = Mth.floor(player.getY()) - 1, z = Mth.floor(player.getZ());
		for (int i = -half; i <= half; i++) {
			for (int j = -half; j <= half; j++) {
				for (int k = 0; k <= half + 1; k++) {
					Block block;
					if (k == half + 1 || k == 0) {
						block = Blocks.QUARTZ_BLOCK;
					} else if (i != half && j != half && i != -half && j != -half) {
						block = Blocks.AIR;
					} else {
						block = Blocks.GLASS;
					}
					level.setBlockAndUpdate(new BlockPos(x + i, y + k, z + j), block.defaultBlockState());
				}
			}
		}
		context.getItemInHand().consume(1, player);
		return InteractionResult.SUCCESS;
	}
}
