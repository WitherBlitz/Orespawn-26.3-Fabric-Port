package orespawn.client.content;

import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSources;
import orespawn.content.Farming;

import java.util.List;

/**
 * Farming's client setup: the tints 1.7.10 gave these blocks. OreSpawn's leaves were BlockLeaves (foliage colour) and
 * its reed-like crops and the Experience Tree Sapling were BlockReed (grass colour, as sugar cane still is). Their
 * models carry the tint index; the leaves' items use the default foliage colour.
 */
public final class FarmingClient {
	private FarmingClient() {
	}

	public static void init() {
		BlockColorRegistry.register(List.of(BlockTintSources.foliage()), Farming.APPLE_LEAVES, Farming.CHERRY_LEAVES, Farming.PEACH_LEAVES,
			Farming.SCARY_LEAVES, Farming.EXPERIENCE_LEAVES);
		BlockColorRegistry.register(List.of(BlockTintSources.sugarCane()), Farming.CORN_PLANT, Farming.TOMATO_PLANT, Farming.LETTUCE_PLANT,
			Farming.QUINOA_PLANT, Farming.EXPERIENCE_TREE_SAPLING);
	}
}
