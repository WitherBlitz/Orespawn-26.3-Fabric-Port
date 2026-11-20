package orespawn.item.weapons;

import orespawn.item.AutoEnchantItem;

import java.util.List;

/**
 * OreSpawn's UltimateSword class as used for the Battle Axe and the Queen Scale Battle Axe (and the Chainsaw, see
 * {@link ChainsawItem}): 3000 uses, and they won't hit players, tamed pets or the Girlfriend/Boyfriend (OreSpawn's
 * UltimateSwordPvp, off by default). The Battle Axe carries Looting III and Unbreaking III; the Queen Scale Battle Axe
 * the Ultimate Sword's set (Sharpness, Smite and Bane of Arthropods V, Knockback III, Looting III, Unbreaking III, Fire
 * Aspect II).
 */
public class UltimateSwordItem extends AutoEnchantItem {
	public UltimateSwordItem(Properties properties, List<Ench> enchantments) {
		super(properties, enchantments);
	}
}
