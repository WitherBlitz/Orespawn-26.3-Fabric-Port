package orespawn.entity.sea;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;

/**
 * The death drops OreSpawn's sea creatures share: its dropItemRand (items land scattered around the body) and the roll
 * on a set of tools and armour that may come enchanted, often past the usual maximum levels (1.7.10's addEnchantment
 * didn't check them, and 26.3 keeps whatever level it's given).
 */
final class SeaLoot {
	/** The nine pieces cases 3-11 of the roll pick from: sword, shovel, pickaxe, axe, hoe, helmet, chestplate, leggings, boots. */
	record GearSet(Item sword, Item shovel, Item pickaxe, Item axe, Item hoe, Item helmet, Item chestplate, Item leggings, Item boots) {
	}

	static final GearSet IRON = new GearSet(Items.IRON_SWORD, Items.IRON_SHOVEL, Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_HOE,
		Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
	static final GearSet GOLD = new GearSet(Items.GOLDEN_SWORD, Items.GOLDEN_SHOVEL, Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_HOE,
		Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);

	private SeaLoot() {
	}

	/** dropItemRand: one item at the mob, x and z each nudged by nextInt(spread) - nextInt(spread), {@code up} blocks higher. */
	static ItemStack drop(Mob mob, ServerLevel level, ItemStack stack, int spread, double up) {
		RandomSource r = level.getRandom();
		ItemEntity item = new ItemEntity(level, mob.getX() + r.nextInt(spread) - r.nextInt(spread), mob.getY() + up,
			mob.getZ() + r.nextInt(spread) - r.nextInt(spread), stack);
		level.addFreshEntity(item);
		return stack;
	}

	/** dropItemRand as most sea creatures had it: within a block of the body, a block up. */
	static void drop(Mob mob, ServerLevel level, ItemLike item) {
		drop(mob, level, new ItemStack(item), 2, 1.0);
	}

	/**
	 * Cases 3-11 of the drop roll: one piece of the set, enchanted the way OreSpawn did it (each enchantment its own
	 * chance, mostly 1 in 6 for levels 1-5, Unbreaking 1 in 2 for levels 2-5). Returns false for any other case.
	 */
	static boolean gear(Mob mob, ServerLevel level, int roll, GearSet set) {
		RandomSource r = level.getRandom();
		ItemStack is;
		switch (roll) {
			case 3 -> {
				is = new ItemStack(set.sword());
				maybe(level, is, r, Enchantments.SHARPNESS, 6, 1, 5);
				maybe(level, is, r, Enchantments.BANE_OF_ARTHROPODS, 6, 1, 5);
				maybe(level, is, r, Enchantments.KNOCKBACK, 6, 1, 5);
				maybe(level, is, r, Enchantments.LOOTING, 6, 1, 5);
				maybe(level, is, r, Enchantments.UNBREAKING, 2, 2, 4);
				maybe(level, is, r, Enchantments.FIRE_ASPECT, 6, 1, 5);
				maybe(level, is, r, Enchantments.SHARPNESS, 6, 1, 5);
			}
			case 4, 6, 7 -> {
				is = new ItemStack(roll == 4 ? set.shovel() : roll == 6 ? set.axe() : set.hoe());
				maybe(level, is, r, Enchantments.UNBREAKING, 2, 2, 4);
				maybe(level, is, r, Enchantments.EFFICIENCY, 6, 1, 5);
			}
			case 5 -> {
				is = new ItemStack(set.pickaxe());
				maybe(level, is, r, Enchantments.UNBREAKING, 2, 2, 4);
				maybe(level, is, r, Enchantments.EFFICIENCY, 6, 1, 5);
				maybe(level, is, r, Enchantments.FORTUNE, 6, 1, 5);
			}
			case 8, 9, 10 -> {
				is = new ItemStack(roll == 8 ? set.helmet() : roll == 9 ? set.chestplate() : set.leggings());
				maybe(level, is, r, Enchantments.PROTECTION, 6, 1, 5);
				maybe(level, is, r, Enchantments.BLAST_PROTECTION, 6, 1, 5);
				maybe(level, is, r, Enchantments.FIRE_PROTECTION, 6, 1, 5);
				maybe(level, is, r, Enchantments.PROJECTILE_PROTECTION, 6, 1, 5);
				maybe(level, is, r, Enchantments.UNBREAKING, 2, 2, 4);
				if (roll == 8) {
					maybe(level, is, r, Enchantments.RESPIRATION, 6, 1, 2);
					maybe(level, is, r, Enchantments.AQUA_AFFINITY, 6, 1, 5);
				}
			}
			case 11 -> {
				is = new ItemStack(set.boots());
				maybe(level, is, r, Enchantments.FEATHER_FALLING, 6, 5, 5);
				maybe(level, is, r, Enchantments.UNBREAKING, 2, 2, 4);
			}
			default -> {
				return false;
			}
		}
		drop(mob, level, is, 2, 1.0);
		return true;
	}

	/** 1 time in {@code chance}: the enchantment at base + nextInt(spread). */
	private static void maybe(ServerLevel level, ItemStack is, RandomSource r, ResourceKey<Enchantment> key, int chance, int base, int spread) {
		if (r.nextInt(chance) != 1) return;
		Holder<Enchantment> e = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
		is.enchant(e, base + r.nextInt(spread));
	}
}
