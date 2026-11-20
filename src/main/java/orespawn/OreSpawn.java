package orespawn;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import orespawn.registry.ModBlocks;
import orespawn.entity.ModEntities;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import orespawn.world.ModWorldgen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OreSpawn 1.7.10-20.3 ported to 26.3 for the Destiny pack: its egg ores, ores, gear and (batch by batch) its creatures.
 */
public final class OreSpawn implements ModInitializer {
	public static final String MOD_ID = "orespawn";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		// Fails at once, saying what to download, if the original OreSpawn 1.7.10 file is missing.
		orespawn.original.OriginalOreSpawn.zip();
		ModSounds.init();
		ModBlocks.init();
		ModItems.init();
		ModEntities.init();
		orespawn.content.Content.init();
		ModWorldgen.init();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("orespawn"), FabricCreativeModeTab.builder()
			.title(Component.translatable("itemGroup.orespawn"))
			.icon(() -> new ItemStack(ModBlocks.byName("zombie_egg_ore")))
			.displayItems((params, output) -> {
				ModBlocks.BLOCKS.values().forEach(output::accept);
				ModItems.ITEMS.values().forEach(output::accept);
				ModEntities.SPAWN_EGGS.values().forEach(output::accept);
			})
			.build());
		LOGGER.info("OreSpawn: {} egg ores, {} blocks, {} items", ModBlocks.EGG_ORES.size(), ModBlocks.BLOCKS.size(), ModItems.ITEMS.size());
	}
}
