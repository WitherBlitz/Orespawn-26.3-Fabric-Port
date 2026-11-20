package orespawn.item.gadgets;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import orespawn.item.AutoEnchantItem;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * OreSpawn's Nether Tracker (ItemNetherLost): held in the main hand in the Nether, it turns the netherrack under the
 * player into a block of quartz each tick, leaving a trail to find the way back. It carries Sharpness II, added when
 * it's crafted and again whenever it's carried without it. Stacks to 1, 3000 durability (nothing wears it down).
 * <p>
 * Change from OreSpawn: it truncated the player's coordinates towards zero, so at negative x or z it changed the wrong
 * block; here it's always the block under the player's feet.
 */
public class NetherTrackerItem extends AutoEnchantItem {
	public NetherTrackerItem(Properties properties) {
		super(properties, List.of(new Ench(Enchantments.SHARPNESS, 2)));
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
		super.inventoryTick(stack, level, owner, slot);
		if (slot != EquipmentSlot.MAINHAND || !(owner instanceof Player) || level.dimension() != Level.NETHER) return;
		BlockPos below = owner.blockPosition().below();
		if (level.getBlockState(below).is(Blocks.NETHERRACK)) level.setBlockAndUpdate(below, Blocks.QUARTZ_BLOCK.defaultBlockState());
	}
}
