package orespawn.item;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Gear that enchants itself, as OreSpawn's did: when crafted, and again whenever it's carried without its first
 * enchantment (OreSpawn's {@code onCreated} and {@code onUpdate}). The levels go past vanilla's maximums where
 * OreSpawn's did (the Ultimate Pickaxe has Fortune V).
 */
public class AutoEnchantItem extends Item {
	public record Ench(ResourceKey<Enchantment> enchantment, int level) {}

	private final List<Ench> enchantments;

	public AutoEnchantItem(Properties properties, List<Ench> enchantments) {
		super(properties);
		this.enchantments = enchantments;
	}

	public List<Ench> enchantments() {
		return enchantments;
	}

	/** Adds the enchantments if the first one is missing. Returns whether anything changed. */
	public boolean applyEnchantments(ItemStack stack, Level level) {
		if (enchantments.isEmpty()) return false;
		var lookup = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		Holder<Enchantment> first = lookup.getOrThrow(enchantments.getFirst().enchantment());
		if (EnchantmentHelper.getItemEnchantmentLevel(first, stack) > 0) return false;
		for (Ench e : enchantments) stack.enchant(lookup.getOrThrow(e.enchantment()), e.level());
		return true;
	}

	@Override
	public void onCraftedBy(ItemStack stack, Player player) {
		super.onCraftedBy(stack, player);
		applyEnchantments(stack, player.level());
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
		super.inventoryTick(stack, level, owner, slot);
		applyEnchantments(stack, level);
	}
}
