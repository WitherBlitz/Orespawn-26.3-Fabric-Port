package orespawn.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import orespawn.OreSpawn;

/** OreSpawn's breeding item for most of its animals: the Crystal Apple (item tag orespawn:crystal_apple). */
public final class Breeding {
	public static final TagKey<Item> CRYSTAL_APPLE = TagKey.create(Registries.ITEM, OreSpawn.id("crystal_apple"));

	private Breeding() {
	}

	public static boolean crystalApple(ItemStack stack) {
		return stack.is(CRYSTAL_APPLE);
	}
}
