package orespawn.item.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.content.Tools;
import orespawn.registry.ModItems;

/**
 * OreSpawn's Sifter ({@code ItemSifter}): used on a block under water, or on sand, gravel, dirt or grass, it now and
 * then turns up something (1 time in 160 tries per item under water, 1 in 60 per item otherwise): fish, junk, bits of
 * ore, gems, shoes; old iron armour and saddles in sand; flint, salt, leads and name tags in gravel; and so on. Every use
 * wears it by 1 (600 uses), whatever the block. Finds pop up within a block of the spot.
 * <p>
 * 1.7.10's blocks: sand includes red sand and dirt coarse dirt and podzol. OreSpawn's fish, Moth Scales and flowers come
 * from other groups and turn up nothing until those exist. As in OreSpawn, a dead flower find also gives wheat.
 */
public class SifterItem extends Item {
	public SifterItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		RandomSource r = level.getRandom();
		boolean water = level.getFluidState(pos).is(FluidTags.WATER) || level.getFluidState(pos.above()).is(FluidTags.WATER);
		if (water) {
			water(level, pos, r);
		} else if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
			sand(level, pos, r);
		} else if (state.is(Blocks.GRAVEL)) {
			gravel(level, pos, r);
		} else if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL)) {
			dirt(level, pos, r);
		} else if (state.is(Blocks.GRASS_BLOCK)) {
			grass(level, pos, r);
		}
		if (context.getPlayer() != null) context.getItemInHand().hurtAndBreak(1, context.getPlayer(), context.getHand());
		return InteractionResult.SUCCESS;
	}

	private static void water(Level level, BlockPos pos, RandomSource r) {
		switch (r.nextInt(160)) {
			case 0, 34 -> drop(level, pos, Items.COD);
			case 1 -> drop(level, pos, ModItems.lazy("green_fish"));
			case 2 -> drop(level, pos, ModItems.lazy("blue_fish"));
			case 3 -> drop(level, pos, ModItems.lazy("pink_fish"));
			case 4 -> drop(level, pos, ModItems.lazy("rock_fish"));
			case 5 -> drop(level, pos, ModItems.lazy("wood_fish"));
			case 6 -> drop(level, pos, ModItems.lazy("grey_fish"));
			case 7, 14, 35 -> drop(level, pos, Items.GLASS_BOTTLE);
			case 8, 26 -> drop(level, pos, Items.IRON_INGOT);
			case 9, 27 -> drop(level, pos, Items.GOLD_NUGGET);
			case 10, 30 -> drop(level, pos, Tools.RED_HEELS);
			case 11, 31 -> drop(level, pos, Tools.BLACK_HEELS);
			case 12, 32 -> drop(level, pos, Tools.SLIPPERS);
			case 13, 33 -> drop(level, pos, Tools.BOOTS);
			case 15, 36 -> drop(level, pos, Items.BONE);
			case 16, 37 -> drop(level, pos, Items.STONE);
			case 17, 39 -> drop(level, pos, Items.BUCKET);
			case 18, 40 -> drop(level, pos, Items.WATER_BUCKET);
			case 19 -> drop(level, pos, r.nextInt(3) == 1 ? Items.EMERALD : Items.GRAVEL);
			case 20 -> drop(level, pos, r.nextInt(3) == 1 ? ModItems.RUBY : Items.GRAVEL);
			case 21 -> drop(level, pos, r.nextInt(3) == 1 ? ModItems.AMETHYST : Items.GRAVEL);
			case 22 -> drop(level, pos, ModItems.lazy("moth_scale"));
			case 23 -> drop(level, pos, ModItems.URANIUM_NUGGET);
			case 24 -> drop(level, pos, ModItems.TITANIUM_NUGGET);
			case 25 -> drop(level, pos, r.nextInt(2) == 1 ? Items.DIAMOND : Items.GRAVEL);
			case 28 -> drop(level, pos, Items.REDSTONE);
			case 29 -> drop(level, pos, Items.COAL);
			case 38 -> drop(level, pos, Items.STONE_BUTTON);
			default -> {
			}
		}
	}

	private static void sand(Level level, BlockPos pos, RandomSource r) {
		ItemLike[] finds = {Items.IRON_HORSE_ARMOR, Items.SHEARS, Items.CARROT_ON_A_STICK, Items.POISONOUS_POTATO, Items.ITEM_FRAME, Items.BONE,
			Items.COMPASS, Items.GLASS_BOTTLE, Items.SADDLE, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
			Items.SAND};
		pick(level, pos, r, finds);
	}

	private static void gravel(Level level, BlockPos pos, RandomSource r) {
		ItemLike[] finds = {Items.FLINT, ModItems.SALT, Items.FLINT_AND_STEEL, Items.SPIDER_EYE, Items.ITEM_FRAME, Items.FEATHER, Items.STRING,
			Items.GLASS_BOTTLE, Items.LEAD, Items.NAME_TAG, Items.SAND, Items.GRAVEL};
		pick(level, pos, r, finds);
	}

	private static void dirt(Level level, BlockPos pos, RandomSource r) {
		ItemLike[] finds = {Items.STRING, ModItems.SALT, Items.SHEARS, Items.STICK, Items.BOWL, Items.FLOWER_POT, Items.OAK_SIGN, Items.BRICK,
			Items.PAPER, Items.BONE, Items.GLASS_BOTTLE, Items.SAND, Items.GRAVEL, Items.DIRT};
		pick(level, pos, r, finds);
	}

	private static void grass(Level level, BlockPos pos, RandomSource r) {
		int i = r.nextInt(60);
		ItemLike[] finds = {Items.DANDELION, Items.POPPY, ModItems.lazy("pink_flower"), ModItems.lazy("blue_flower"), ModItems.lazy("black_flower"),
			ModItems.lazy("dead_flower"), Items.WHEAT, Items.PUMPKIN_SEEDS, Items.MELON_SEEDS, Items.CARROT, Items.POTATO, Items.DEAD_BUSH,
			Items.GRAVEL, Items.DIRT, Items.GRASS_BLOCK};
		if (i < finds.length) drop(level, pos, finds[i]);
		if (i == 5) drop(level, pos, Items.WHEAT);
	}

	/** One roll of 60: the first finds are each 1 in 60, the rest nothing. */
	private static void pick(Level level, BlockPos pos, RandomSource r, ItemLike[] finds) {
		int i = r.nextInt(60);
		if (i < finds.length) drop(level, pos, finds[i]);
	}

	private static void drop(Level level, BlockPos pos, ItemLike item) {
		ItemStack stack = new ItemStack(item);
		if (stack.isEmpty()) return;
		RandomSource r = level.getRandom();
		level.addFreshEntity(new ItemEntity(level, pos.getX() + r.nextInt(2) - r.nextInt(2) + 0.5, pos.getY() + 1.1,
			pos.getZ() + r.nextInt(2) - r.nextInt(2) + 0.5, stack));
	}
}
