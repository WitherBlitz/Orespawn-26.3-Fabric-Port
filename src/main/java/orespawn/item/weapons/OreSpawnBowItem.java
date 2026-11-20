package orespawn.item.weapons;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import orespawn.content.Weapons;
import orespawn.entity.weapons.IrukandjiArrow;
import orespawn.entity.weapons.UltimateArrow;
import orespawn.item.AutoEnchantItem;

import java.util.List;

/**
 * OreSpawn's two bows, drawn like a bow (enchantability 50):
 * <ul>
 * <li>The Ultimate Bow (UltimateBow, 1000 uses; Power V, Flame III, Punch II, Infinity): needs no arrows, and fires an
 * {@link UltimateArrow} at full speed however briefly it's drawn. One shot in 4 is a critical; Punch knocks back and
 * Flame sets the arrow alight for 100 s.</li>
 * <li>The Skate String Bow (SkateBow, 300 uses): fires {@link IrukandjiArrow}s, using one from the inventory (none in
 * creative or with Infinity). Its pull grows past a normal bow's: (t^2 + 2t) / 3 for t seconds drawn, up to 1.75, and
 * a pull under 0.1 doesn't fire; the arrow flies at 1.5 times that. One shot in 20 is a critical; Punch and Flame work
 * as on the Ultimate Bow.</li>
 * </ul>
 */
public class OreSpawnBowItem extends AutoEnchantItem {
	private final boolean ultimate;

	public OreSpawnBowItem(Properties properties, List<Ench> enchantments, boolean ultimate) {
		super(properties, enchantments);
		this.ultimate = ultimate;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user) {
		return 72000;
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
		if (!(entity instanceof Player player)) return false;
		var random = level.getRandom();
		AbstractArrow arrow;
		if (ultimate) {
			UltimateArrow a = UltimateArrow.shoot(level, player);
			if (random.nextInt(4) == 1) a.setCritArrow(true);
			int punch = WeaponItems.level(level, stack, Enchantments.PUNCH);
			if (punch > 0) a.setPunch(punch);
			arrow = a;
		} else {
			boolean free = player.hasInfiniteMaterials() || WeaponItems.level(level, stack, Enchantments.INFINITY) > 0;
			int ammo = findAmmo(player.getInventory());
			if (!free && ammo < 0) return false;
			float f = (getUseDuration(stack, player) - remainingTime) / 20.0F;
			f = (f * f + f * 2.0F) / 3.0F;
			if (f < 0.1F) return false;
			if (f > 1.75F) f = 1.75F;
			IrukandjiArrow a = IrukandjiArrow.shoot(level, player, f);
			if (random.nextInt(20) == 1) a.setCritArrow(true);
			int punch = WeaponItems.level(level, stack, Enchantments.PUNCH);
			if (punch > 0) a.setPunch(punch);
			if (!free) player.getInventory().getItem(ammo).shrink(1);
			arrow = a;
		}
		if (WeaponItems.level(level, stack, Enchantments.FLAME) > 0) arrow.igniteForSeconds(100.0F);
		stack.hurtAndBreak(1, player, player.getUsedItemHand());
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
			1.0F / (random.nextFloat() * 0.4F + 1.2F) + 0.5F);
		if (level instanceof ServerLevel server) server.addFreshEntity(arrow);
		return true;
	}

	/** The first Irukandji Arrow in the inventory (consumeInventoryItem's order), or -1. */
	private static int findAmmo(Inventory inv) {
		for (int i = 0; i < inv.getContainerSize(); i++) {
			if (inv.getItem(i).is(Weapons.IRUKANDJI_ARROW)) return i;
		}
		return -1;
	}
}
