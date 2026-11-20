package orespawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.registry.ModItems;

import java.util.List;

/**
 * The Ultimate Pickaxe (OreSpawn's {@code UltimatePickaxe}): iron and gold ore have a 1 in 2 chance of also dropping
 * 1-2 iron or gold ingots, and stone has a 1 in 100 chance of dropping a diamond, emerald, amethyst, ruby, uranium
 * nugget or titanium nugget (each 1 in 10 of those times; the other 4 in 10 give nothing).
 */
public class UltimatePickaxeItem extends AutoEnchantItem {
	public UltimatePickaxeItem(Properties properties, List<Ench> enchantments) {
		super(properties, enchantments);
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
		boolean result = super.mineBlock(stack, level, state, pos, owner);
		if (level.isClientSide()) return result;
		var random = level.getRandom();
		if ((state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)) && random.nextInt(2) != 0) {
			drop(level, pos, Items.IRON_INGOT, 1 + random.nextInt(2));
		}
		if ((state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE)) && random.nextInt(2) != 0) {
			drop(level, pos, Items.GOLD_INGOT, 1 + random.nextInt(2));
		}
		if (state.is(Blocks.STONE) && random.nextInt(100) == 2) {
			Item bonus = switch (random.nextInt(10)) {
				case 0 -> Items.DIAMOND;
				case 1 -> Items.EMERALD;
				case 2 -> ModItems.AMETHYST;
				case 3 -> ModItems.RUBY;
				case 4 -> ModItems.URANIUM_NUGGET;
				case 5 -> ModItems.TITANIUM_NUGGET;
				default -> null;
			};
			if (bonus != null) drop(level, pos, bonus, 1);
		}
		return result;
	}

	private static void drop(Level level, BlockPos pos, Item item, int count) {
		Block.popResource(level, pos, new ItemStack(item, count));
	}
}
