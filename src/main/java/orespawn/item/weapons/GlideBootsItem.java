package orespawn.item.weapons;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import orespawn.item.AutoEnchantItem;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Boots that let their wearer glide (ItemOreSpawnArmor.onArmorTick, with OreSpawn's RoyalGlideEnable on as by
 * default): a player in Royal Guardian or Peacock Feather boots never falls faster than 0.1 blocks a tick, in Queen
 * Scale boots 0.25, and never takes fall damage. The fall speed is the client's to limit (WeaponsClient does it); the
 * server clears the fall distance.
 */
public class GlideBootsItem extends AutoEnchantItem {
	private final double maxFallSpeed;

	public GlideBootsItem(Properties properties, List<Ench> enchantments, double maxFallSpeed) {
		super(properties, enchantments);
		this.maxFallSpeed = maxFallSpeed;
	}

	public double maxFallSpeed() {
		return maxFallSpeed;
	}

	/** Slows a player wearing glide boots: called each tick for the local player. */
	public static void glide(Player player) {
		if (player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof GlideBootsItem boots) {
			var v = player.getDeltaMovement();
			if (v.y < -boots.maxFallSpeed) player.setDeltaMovement(v.x, -boots.maxFallSpeed, v.z);
			player.resetFallDistance();
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
		super.inventoryTick(stack, level, owner, slot);
		if (slot == EquipmentSlot.FEET && owner instanceof Player player) player.resetFallDistance();
	}
}
