package orespawn.world.crystal;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Random;

/**
 * The chest contents of the Crystal dimension (OreSpawn's WeightedRandomChestContent lists: Trees.CrystalChestContentsList
 * and GenericDungeon's CrystalBattleTower lists), filled the way 1.7.10's generateChestContents did: each roll picks an
 * entry by weight, makes a stack of min-max (single items if that's over a stack), and puts it in a random slot.
 * Items are looked up by id when a chest is filled; entries whose item isn't registered yet give nothing.
 */
public final class CrystalLoot {
	/** One WeightedRandomChestContent: item id, min, max, weight. */
	public record Entry(String item, int min, int max, int weight) {
	}

	private static Entry e(String item, int min, int max, int weight) {
		return new Entry(item.contains(":") ? item : "orespawn:" + item, min, max, weight);
	}

	/** Trees.CrystalChestContentsList: fairy trees, the maze chests. */
	public static final Entry[] CRYSTAL = {
		e("crystal_termite_nest", 1, 5, 10), e("red_crystal_flower", 1, 10, 10), e("blue_crystal_flower", 1, 10, 10),
		e("green_crystal_flower", 1, 10, 10), e("yellow_crystal_flower", 1, 10, 10), e("crystal_planks", 1, 10, 10),
		e("crystal_workbench", 1, 1, 10), e("crystal_furnace", 1, 1, 10), e("tigers_eye_block", 1, 10, 5), e("kyanite", 1, 10, 10),
		e("crystalized_rats", 1, 10, 10), e("crystalized_fairies", 1, 10, 10), e("crystal_energy", 1, 10, 10), e("crystal_grass", 1, 10, 10),
		e("pink_tourmaline", 1, 10, 10), e("crystal_torch", 1, 10, 10), e("red_crystal_tree_leaves", 1, 10, 10),
		e("yellow_crystal_tree_leaves", 1, 10, 10), e("blue_crystal_tree_leaves", 1, 10, 10), e("crystal_tree_wood", 1, 10, 10),
		e("tigers_eye", 1, 10, 5), e("crystal_wood_sword", 1, 1, 10), e("crystal_wood_axe", 1, 1, 10), e("crystal_wood_shovel", 1, 1, 10),
		e("crystal_wood_pickaxe", 1, 1, 10), e("crystal_wood_hoe", 1, 1, 10), e("pink_tourmaline_sword", 1, 1, 10),
		e("pink_tourmaline_axe", 1, 1, 10), e("pink_tourmaline_shovel", 1, 1, 10), e("pink_tourmaline_pickaxe", 1, 1, 10),
		e("pink_tourmaline_hoe", 1, 1, 10), e("tigers_eye_sword", 1, 1, 5), e("tigers_eye_axe", 1, 1, 5), e("tigers_eye_shovel", 1, 1, 5),
		e("tigers_eye_pickaxe", 1, 1, 5), e("tigers_eye_hoe", 1, 1, 5), e("kyanite_sword", 1, 1, 10), e("kyanite_axe", 1, 1, 10),
		e("kyanite_shovel", 1, 1, 10), e("kyanite_pickaxe", 1, 1, 10), e("kyanite_hoe", 1, 1, 10), e("tigers_eye_ingot", 1, 5, 5),
		e("pink_tourmaline_ingot", 1, 5, 10), e("crystal_apple", 1, 5, 10), e("peacock_feather", 1, 5, 10), e("cooked_peacock", 1, 10, 20),
		e("raw_peacock", 1, 10, 20), e("rice", 1, 10, 20), e("quinoa", 1, 10, 20), e("pink_tourmailine_helmet", 1, 1, 10),
		e("pink_tourmailine_chestplate", 1, 1, 10), e("pink_tourmailine_leggings", 1, 1, 10), e("pink_tourmailine_boots", 1, 1, 10),
		e("tigers_eye_helmet", 1, 1, 5), e("tigers_eye_chestplate", 1, 1, 5), e("tigers_eye_leggings", 1, 1, 5), e("tigers_eye_boots", 1, 1, 5),
		e("peacock_feather_helmet", 1, 1, 10), e("peacock_feather_chestplate", 1, 1, 10), e("peacock_feather_leggings", 1, 1, 10),
		e("peacock_feather_boots", 1, 1, 10), e("rotator_spawn_egg", 1, 5, 10), e("vortex_spawn_egg", 1, 5, 10),
		e("peacock_spawn_egg", 1, 5, 10), e("dungeon_beast_spawn_egg", 1, 5, 10), e("fairy_spawn_egg", 1, 5, 10), e("rat_spawn_egg", 1, 5, 10),
		e("flounder_spawn_egg", 1, 5, 10), e("whale_spawn_egg", 1, 5, 10), e("irukandji_spawn_egg", 1, 5, 10), e("skate_spawn_egg", 1, 5, 10),
		e("crystal_urchin_spawn_egg", 1, 5, 10), e("ghost_spawn_egg", 1, 5, 10), e("ghost_pumpkin_skelly_spawn_egg", 1, 5, 10),
		e("skate_string_bow", 1, 1, 2), e("irukandji_arrow", 5, 10, 2), e("dead_irukandji", 2, 8, 5), e("the_ultimate_bow", 1, 1, 2),
		e("ultimate_sword", 1, 1, 2), e("minecraft:iron_ingot", 1, 4, 10), e("minecraft:oak_log", 1, 4, 10), e("minecraft:golden_apple", 1, 5, 2)};

	/** The battle tower's ground floor (the Rat spawners). */
	public static final Entry[] TOWER_RAT = {
		e("minecraft:cooked_porkchop", 3, 10, 35), e("minecraft:beef", 3, 10, 35), e("minecraft:cooked_chicken", 3, 10, 35),
		e("minecraft:cooked_cod", 3, 10, 35), e("blt_sandwich", 4, 10, 35), e("garden_salad", 4, 10, 35), e("corn_dog", 4, 10, 35)};
	/** The Dungeon Beast floor (ink sacs: 1.7.10's dye, damage 0). */
	public static final Entry[] TOWER_DUNGEON_BEAST = {
		e("minecraft:ink_sac", 6, 16, 25), e("squidzooka", 1, 1, 25), e("minecraft:gold_nugget", 5, 15, 15), e("minecraft:rotten_flesh", 6, 16, 25)};
	/** The Crystal Urchin floor. */
	public static final Entry[] TOWER_URCHIN = {
		e("pink_tourmailine_helmet", 1, 1, 10), e("pink_tourmailine_chestplate", 1, 1, 10), e("pink_tourmailine_leggings", 1, 1, 10),
		e("pink_tourmailine_boots", 1, 1, 10), e("fairy_sword", 1, 1, 15)};
	/** The Rotator floor. */
	public static final Entry[] TOWER_ROTATOR = {
		e("tigers_eye_helmet", 1, 1, 10), e("tigers_eye_chestplate", 1, 1, 10), e("tigers_eye_leggings", 1, 1, 10), e("tigers_eye_boots", 1, 1, 10),
		e("rat_sword", 1, 1, 15)};
	/** The Vortex floor, and the round rotator's chest. */
	public static final Entry[] TOWER_VORTEX = {
		e("crystal_energy", 6, 10, 10), e("crystal_energy", 6, 10, 10), e("tigers_eye_sword", 1, 1, 10), e("tigers_eye_block", 4, 8, 15),
		e("poison_sword", 1, 1, 15)};

	private CrystalLoot() {
	}

	public static Item item(String id) {
		return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
	}

	/** WeightedRandomChestContent.generateChestContents. */
	public static void fill(Container chest, Random random, Entry[] list, int rolls) {
		int total = 0;
		for (Entry entry : list) total += entry.weight;
		for (int r = 0; r < rolls; r++) {
			int pick = random.nextInt(total);
			Entry chosen = list[list.length - 1];
			for (Entry entry : list) {
				pick -= entry.weight;
				if (pick < 0) {
					chosen = entry;
					break;
				}
			}
			int count = chosen.min + random.nextInt(chosen.max - chosen.min + 1);
			Item item = item(chosen.item);
			if (item == Items.AIR) continue;
			int max = new ItemStack(item).getMaxStackSize();
			if (count > max) {
				for (int i = 0; i < count; i++) chest.setItem(random.nextInt(chest.getContainerSize()), new ItemStack(item));
			} else {
				chest.setItem(random.nextInt(chest.getContainerSize()), new ItemStack(item, count));
			}
		}
	}
}
