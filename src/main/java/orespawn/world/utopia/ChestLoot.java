package orespawn.world.utopia;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * ItemMagicApple's chest contents (its {@code WeightedRandomChestContent} list) and 1.7.10's fill: each draw picks an entry by
 * weight and puts min-max of it in a random slot of the 27 (more than a stack's worth becomes that many single items,
 * as Forge's ChestGenHooks did; later draws can land on earlier slots). Items other groups own are looked up by id
 * when a chest is filled; one that isn't ported yet leaves its draw empty.
 */
public final class ChestLoot {
	/** One WeightedRandomChestContent: ids (first registered wins), min, max, weight. */
	public record Entry(String[] ids, int min, int max, int weight) {}

	private static Entry e(int min, int max, int weight, String... ids) {
		return new Entry(ids, min, max, weight);
	}

	/** ItemMagicApple's chests in the huge trees. */
	public static final List<Entry> MAGIC_APPLE = List.of(
		e(1, 2, 3, "minecraft:ender_pearl"),
		e(1, 5, 15, "minecraft:diamond"),
		e(1, 3, 10, "minecraft:blaze_rod"),
		e(1, 10, 7, "empty_critter_cage"),
		e(1, 2, 6, "caged_girlfriend"),
		e(1, 10, 16, "minecraft:iron_ingot"),
		e(1, 6, 16, "minecraft:gold_ingot"),
		e(1, 6, 6, "uranium_nugget"),
		e(1, 4, 6, "titanium_nugget"),
		e(1, 8, 20, "minecraft:bread"),
		e(1, 8, 20, "minecraft:apple"),
		e(1, 16, 20, "minecraft:cookie"),
		e(1, 8, 20, "minecraft:cooked_beef"),
		e(1, 8, 20, "minecraft:cooked_chicken"),
		e(1, 8, 20, "minecraft:cooked_cod"),
		e(1, 8, 20, "minecraft:cooked_porkchop"),
		e(1, 4, 20, "minecraft:pumpkin_pie"),
		e(1, 16, 20, "minecraft:carrot"),
		e(1, 16, 20, "minecraft:potato"),
		e(1, 4, 6, "sun_fish"),
		e(1, 8, 6, "fire_fish"),
		e(1, 4, 16, "bag_of_popcorn"),
		e(1, 1, 20, "minecraft:iron_pickaxe"),
		e(1, 1, 20, "minecraft:iron_sword"),
		e(1, 1, 5, "minecraft:diamond_pickaxe"),
		e(1, 1, 5, "minecraft:diamond_sword"),
		e(1, 1, 20, "minecraft:bow"),
		e(1, 64, 20, "minecraft:arrow"),
		e(1, 1, 2, "ultimate_pickaxe", "the_ultimate_pickaxe"),
		e(1, 1, 1, "ultimate_sword", "the_ultimate_sword"),
		e(1, 1, 5, "ultimate_fishing_rod", "the_ultimate_fishing_rod"),
		e(1, 1, 20, "minecraft:iron_chestplate"),
		e(1, 1, 20, "minecraft:iron_helmet"),
		e(1, 1, 20, "minecraft:iron_leggings"),
		e(1, 1, 20, "minecraft:iron_boots"),
		e(1, 1, 5, "minecraft:diamond_chestplate"),
		e(1, 1, 5, "minecraft:diamond_helmet"),
		e(1, 1, 5, "minecraft:diamond_leggings"),
		e(1, 1, 5, "minecraft:diamond_boots"),
		e(1, 1, 5, "minecraft:golden_apple"));

	private ChestLoot() {
	}

	/** WeightedRandomChestContent.generateChestContents(rand, list, chest, count) into 27 slots. */
	public static ItemStack[] fill(RandomSource random, List<Entry> list, int count) {
		ItemStack[] slots = new ItemStack[27];
		java.util.Arrays.fill(slots, ItemStack.EMPTY);
		int total = 0;
		for (Entry entry : list) {
			total += entry.weight();
		}
		for (int i = 0; i < count; i++) {
			int roll = random.nextInt(total);
			Entry picked = list.get(list.size() - 1);
			for (Entry entry : list) {
				roll -= entry.weight();
				if (roll < 0) {
					picked = entry;
					break;
				}
			}
			int n = picked.min() + random.nextInt(picked.max() - picked.min() + 1);
			Item item = Lookup.item(picked.ids());
			if (item == Items.AIR) {
				continue;
			}
			ItemStack stack = new ItemStack(item);
			if (stack.getMaxStackSize() >= n) {
				stack.setCount(n);
				slots[random.nextInt(slots.length)] = stack;
			} else {
				for (int k = 0; k < n; k++) {
					slots[random.nextInt(slots.length)] = new ItemStack(item);
				}
			}
		}
		return slots;
	}
}
