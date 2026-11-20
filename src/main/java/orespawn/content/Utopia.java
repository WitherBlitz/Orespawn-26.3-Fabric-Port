package orespawn.content;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import orespawn.OreSpawn;
import orespawn.world.utopia.UtopiaChunkGenerator;
import orespawn.world.utopia.UtopiaFeature;
import orespawn.world.utopia.UtopiaSpawns;
import orespawn.world.utopia.UtopiaStructure;

/**
 * OreSpawn's Utopia dimension (DimensionID, "Dimension-Utopia"; {@code orespawn:utopia}, see
 * {@link orespawn.world.OreSpawnDimensions#UTOPIA}): WorldProviderOreSpawn, ChunkProviderOreSpawn,
 * BiomeGenUtopianPlains and OreSpawnWorld's Utopia branch.
 * <p>
 * Registered here: the chunk generator {@code orespawn:utopia} ({@link UtopiaChunkGenerator}), the feature type
 * {@code orespawn:utopia} ({@link UtopiaFeature}), the structure type and piece {@code orespawn:utopia_big}
 * ({@link UtopiaStructure}: the huge, wind and sky trees) and the biome's creatures ({@link UtopiaSpawns#UTOPIA}).
 * The dimension, its type, the biome (OreSpawn's temperature 0.7 and rainfall 0.5, so its grass and leaf colours, and
 * the sky colour 1.7.10 worked out from that temperature), carvers, placed features, structure and structure set are
 * data files from tools/content/utopia.json.
 * <p>
 * Sky, light and time as WorldProviderOreSpawn had them: a surface world with the overworld's sky, sun, moon, stars,
 * fog and light, clouds at 1.7.10's height 128, beds that work, and the overworld's clock (Utopia always had the
 * overworld's time). Everyone sleeping in Utopia moves that shared clock to morning, which is what its setWorldTime
 * override did. Getting there is the Portals group's (the black ant, OreSpawnTeleporter).
 */
public final class Utopia {
	public static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, OreSpawn.id("utopia"));
	public static final StructureType<UtopiaStructure> BIG_STRUCTURE = Registry.register(BuiltInRegistries.STRUCTURE_TYPE,
		OreSpawn.id("utopia_big"), () -> UtopiaStructure.CODEC);
	public static final StructurePieceType BIG_PIECE = Registry.register(BuiltInRegistries.STRUCTURE_PIECE, OreSpawn.id("utopia_big"),
		UtopiaStructure.Piece::new);

	private Utopia() {
	}

	public static void init() {
		Registry.register(BuiltInRegistries.CHUNK_GENERATOR, OreSpawn.id("utopia"), UtopiaChunkGenerator.CODEC);
		Registry.register(BuiltInRegistries.FEATURE_TYPE, OreSpawn.id("utopia"), UtopiaFeature.CODEC);
		UtopiaSpawns.register(BIOME);
	}
}
