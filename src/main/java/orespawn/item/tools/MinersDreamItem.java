package orespawn.item.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.content.Tools;

/**
 * OreSpawn's Miner's Dream: used on a block straight north, south, east or west of you, it bores a tunnel 11 wide, 5
 * high and 64 long from that block, at your feet's level. Only stone, dirt, gravel, netherrack, end stone, Kyanite and
 * water and lava are removed, so ores and everything else are left standing in it. The ceiling is sealed with
 * cobblestone (Kyanite in the Crystal dimension) where it was air, sand, gravel, water or lava, unless the whole row was
 * open sky; every 5 blocks along the middle an Extreme Torch (a Crystal Torch on Kyanite) goes on a stone, dirt, gravel,
 * netherrack, end stone or bedrock floor.
 * <p>
 * "Stone" is 26.3's overworld base stone (stone, granite, diorite, andesite, tuff and deepslate), which 1.7.10's plain
 * stone has become; dirt includes coarse dirt and podzol and sand red sand, as their 1.7.10 blocks did. The torches
 * come from other groups and are skipped until those exist.
 */
public class MinersDreamItem extends Item {
	public MinersDreamItem(Properties properties) {
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
		int height = 5, width = 5, length = 64;
		Block kyanite = Tools.lazyBlock("kyanite");
		Block roof = Tools.inCrystalDimension(level) && kyanite != null ? kyanite : Blocks.COBBLESTONE;
		for (int i = 0; i < height; i++) {
			for (int k = 0; k < length; k++) {
				int solid = 0;
				for (int j = -width; j <= width; j++) {
					int bx = x + k * dx + j * dz, bz = z + k * dz + j * dx;
					BlockState state = level.getBlockState(new BlockPos(bx, y + i, bz));
					if (state.is(BlockTags.BASE_STONE_OVERWORLD) || isDirt(state) || state.is(Blocks.GRAVEL) || state.is(Blocks.WATER)
						|| state.is(Blocks.LAVA) || state.is(Blocks.NETHERRACK) || state.is(Blocks.END_STONE) || kyanite != null && state.is(kyanite)) {
						set(level, bx, y + i, bz, Blocks.AIR);
					}
					if (i == height - 1) {
						BlockState above = level.getBlockState(new BlockPos(bx, y + i + 1, bz));
						if (!above.isAir()) solid++;
						if (above.isAir() || above.is(Blocks.GRAVEL) || above.is(Blocks.SAND) || above.is(Blocks.RED_SAND) || above.is(Blocks.WATER)
							|| above.is(Blocks.LAVA)) {
							set(level, bx, y + i + 1, bz, roof);
						}
					}
				}
				if (i == height - 1 && solid == 0) {
					for (int j = -width; j <= width; j++) set(level, x + k * dx + j * dz, y + i + 1, z + k * dz + j * dx, Blocks.AIR);
				}
			}
		}
		Block torch = Tools.lazyBlock("extreme_torch"), crystalTorch = Tools.lazyBlock("crystal_torch");
		for (int k = 0; k < length; k += 5) {
			BlockPos at = new BlockPos(x + k * dx, y, z + k * dz);
			BlockState floor = level.getBlockState(at.below());
			if (torch != null && (floor.is(BlockTags.BASE_STONE_OVERWORLD) || isDirt(floor) || floor.is(Blocks.GRAVEL) || floor.is(Blocks.NETHERRACK)
				|| floor.is(Blocks.END_STONE) || floor.is(Blocks.BEDROCK)) && level.isEmptyBlock(at)) {
				level.setBlock(at, torch.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
			if (crystalTorch != null && kyanite != null && floor.is(kyanite) && level.isEmptyBlock(at)) {
				level.setBlock(at, crystalTorch.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}
		context.getItemInHand().consume(1, player);
		return InteractionResult.SUCCESS;
	}

	private static boolean isDirt(BlockState state) {
		return state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL);
	}

	/** OreSpawn set these with flag 2: the clients see them, but neighbours aren't updated. */
	private static void set(Level level, int x, int y, int z, Block block) {
		level.setBlock(new BlockPos(x, y, z), block.defaultBlockState(), Block.UPDATE_CLIENTS);
	}
}
