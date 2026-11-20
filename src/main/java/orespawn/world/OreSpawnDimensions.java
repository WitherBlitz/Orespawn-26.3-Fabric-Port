package orespawn.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import orespawn.OreSpawn;

/**
 * OreSpawn's six dimensions (its DimensionID to DimensionID6), as 26.3 levels. Each dimension's group writes its
 * {@code data/orespawn/dimension/<id>.json} and {@code dimension_type/<id>.json} and registers its chunk generator; the
 * Portals group owns travel ({@code OreSpawnTeleporter}). Who takes you there in OreSpawn: the black ant to Utopia, the
 * red ant to the Mining dimension, the rainbow ant to Village Mania, the unstable ant to the Islands, the termite to the
 * Crystal world and the butterfly to Chaos; each takes you back to the overworld from its own dimension.
 */
public final class OreSpawnDimensions {
	/** DimensionID: "Dimension-Utopia". */
	public static final ResourceKey<Level> UTOPIA = key("utopia");
	/** DimensionID2: "Dimension-Extreme", OreSpawn's Mining dimension. */
	public static final ResourceKey<Level> MINING = key("mining");
	/** DimensionID3: "Dimension-VillageMania". */
	public static final ResourceKey<Level> VILLAGE_MANIA = key("village_mania");
	/** DimensionID4: "Dimension-Islands". */
	public static final ResourceKey<Level> ISLANDS = key("islands");
	/** DimensionID5: "Dimension-Crystal". */
	public static final ResourceKey<Level> CRYSTAL = key("crystal");
	/** DimensionID6: "Dimension-Chaos". */
	public static final ResourceKey<Level> CHAOS = key("chaos");

	private OreSpawnDimensions() {
	}

	private static ResourceKey<Level> key(String name) {
		return ResourceKey.create(Registries.DIMENSION, OreSpawn.id(name));
	}
}
