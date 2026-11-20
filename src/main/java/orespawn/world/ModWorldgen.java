package orespawn.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import orespawn.OreSpawn;

import java.util.List;

/**
 * Adds the ores to every overworld and Nether biome. The features themselves are data files
 * ({@code data/orespawn/worldgen}), so their counts and heights can be tuned without code. Rates follow OreSpawn's
 * {@code OreSpawnWorld}: egg ores, amethyst, uranium, titanium and salt in the overworld's stone, ruby in the
 * Nether's netherrack. Egg ores are one feature that picks a random mob for each vein.
 */
public final class ModWorldgen {
	public static final List<String> OVERWORLD = List.of("egg_ores", "amethyst_ore", "uranium_ore", "titanium_ore", "salt_ore");
	public static final List<String> NETHER = List.of("ruby_ore_nether");

	private ModWorldgen() {
	}

	public static ResourceKey<PlacedFeature> key(String name) {
		return ResourceKey.create(Registries.PLACED_FEATURE, OreSpawn.id(name));
	}

	public static void init() {
		for (String name : OVERWORLD) {
			BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, key(name));
		}
		for (String name : NETHER) {
			BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(), GenerationStep.Decoration.UNDERGROUND_ORES, key(name));
		}
	}
}
