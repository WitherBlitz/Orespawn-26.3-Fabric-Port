package orespawn.item.weapons;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import orespawn.content.Weapons;
import orespawn.entity.weapons.UltimateFishHook;
import orespawn.item.AutoEnchantItem.Ench;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * OreSpawn's Ultimate Fishing Rod (3000 uses, Unbreaking II): casts an {@link UltimateFishHook}, which fishes in lava
 * too. Reeling in costs what the hook says (1 for a catch, 3 with something hooked, 2 lying on the ground).
 */
public class UltimateFishingRodItem extends FishingRodItem {
	private static final List<Ench> ENCHANTMENTS = List.of(new Ench(Enchantments.UNBREAKING, 2));

	public UltimateFishingRodItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player.fishing != null) {
			if (!level.isClientSide()) stack.hurtAndBreak(player.fishing.retrieve(stack), player, hand);
		} else {
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F,
				0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
			if (level instanceof ServerLevel server) {
				int lure = (int) (EnchantmentHelper.getFishingTimeReduction(server, stack, player) * 20.0F);
				int luck = EnchantmentHelper.getFishingLuckBonus(server, stack, player);
				Projectile.spawnProjectile(UltimateFishHook.cast(Weapons.ULTIMATE_FISH_HOOK, player, level, luck, lure), server, stack);
			}
			player.awardStat(Stats.ITEM_USED.get(this));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void onCraftedBy(ItemStack stack, Player player) {
		super.onCraftedBy(stack, player);
		WeaponItems.autoEnchant(stack, player.level(), ENCHANTMENTS);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
		super.inventoryTick(stack, level, owner, slot);
		WeaponItems.autoEnchant(stack, level, ENCHANTMENTS);
	}
}
