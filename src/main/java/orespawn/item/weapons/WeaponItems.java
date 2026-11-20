package orespawn.item.weapons;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import orespawn.item.AutoEnchantItem.Ench;

import java.util.List;

/** Small helpers the Weapons group's items share. */
public final class WeaponItems {
	private WeaponItems() {
	}

	/** The stack's level of {@code enchantment}. */
	public static int level(Level level, ItemStack stack, ResourceKey<Enchantment> enchantment) {
		return EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment), stack);
	}

	/** AutoEnchantItem's rule for items that can't extend it: add them all when the first is missing. */
	public static void autoEnchant(ItemStack stack, Level level, List<Ench> enchantments) {
		if (enchantments.isEmpty() || level(level, stack, enchantments.getFirst().enchantment()) > 0) return;
		var lookup = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		for (Ench e : enchantments) stack.enchant(lookup.getOrThrow(e.enchantment()), e.level());
	}
}
