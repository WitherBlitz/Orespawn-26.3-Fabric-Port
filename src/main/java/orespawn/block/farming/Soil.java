package orespawn.block.farming;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.OreSpawn;

/**
 * The ground OreSpawn's plants accept. Crystal Grass belongs to the crystal dimension's group, so it's matched by id
 * (nothing matches until that group registers it).
 */
public final class Soil {
	/** OreSpawn's CrystalGrass: its canSustainPlant said yes to every plant. */
	public static final ResourceKey<Block> CRYSTAL_GRASS = ResourceKey.create(Registries.BLOCK, OreSpawn.id("crystal_grass"));

	private Soil() {
	}

	/** Where a 1.7.10 crop could stand (Forge's Crop plant type: farmland only), plus Crystal Grass. */
	public static boolean farmland(BlockState state) {
		return state.is(BlockTags.SUPPORTS_CROPS) || state.is(CRYSTAL_GRASS);
	}

	/** OreSpawn's grass/dirt/farmland checks (1.7.10's dirt block also covered coarse dirt and podzol). */
	public static boolean ground(BlockState state) {
		return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL)
			|| state.is(Blocks.FARMLAND);
	}
}
