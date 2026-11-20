package orespawn.item.weapons;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import orespawn.entity.weapons.BerthaHit;
import orespawn.item.AutoEnchantItem;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * OreSpawn's Bertha class: Big Bertha, Slice, the Royal Guardian Sword and the Attitude Adjuster. Huge swords (9000
 * uses) that hit what they touch and, on every swing (hitting, mining or swinging at air), also throw a
 * {@link BerthaHit} at whatever is in front of the player; each swing costs one use. They won't hit players, tamed
 * pets or the Girlfriend/Boyfriend (OreSpawn's BigBerthaPvp, off by default). Big Bertha and Slice carry Knockback V,
 * Bane of Arthropods I and Fire Aspect I, the Royal Guardian Sword Unbreaking V; the Attitude Adjuster none.
 * <p>
 * 26.3 has no item hook for a swing, so the client tells the server when its player starts one
 * ({@link WeaponSwingPayload}); the server allows one hit per 3 ticks, a vanilla swing's half length.
 */
public class BerthaItem extends AutoEnchantItem {
	private static final Map<ServerPlayer, Long> LAST_SWING = new WeakHashMap<>();
	private final int hitType;

	public BerthaItem(Properties properties, List<Ench> enchantments, int hitType) {
		super(properties, enchantments);
		this.hitType = hitType;
	}

	/** Bertha.onEntitySwing. */
	public void swing(ServerPlayer player, ItemStack stack) {
		ServerLevel level = player.level();
		long now = level.getGameTime();
		Long last = LAST_SWING.get(player);
		if (last != null && now - last < 3 && now >= last) return;
		LAST_SWING.put(player, now);
		BerthaHit.swing(level, player, hitType);
		stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
	}
}
