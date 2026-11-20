package orespawn.world.utopia;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import orespawn.content.Utopia;

import java.util.List;
import java.util.Optional;

/**
 * {@code orespawn:utopia_big}: Utopia's huge trees (OreSpawnWorld.addHugeTree: ItemMagicApple's square, circular,
 * round, gold-and-emerald and obsidian-and-ruby trees) and its wind and sky trees (addOtherTrees). Every Utopia chunk is
 * a candidate (its structure set spaces them 1 apart); {@link UtopiaPlan} decides with OreSpawn's odds whether the
 * chunk grows one. Each tree is a piece that covers what the tree built, and every chunk it reaches places its own
 * part while it decorates, so the trees can be as big as OreSpawn made them.
 */
public final class UtopiaStructure extends Structure {
	public static final MapCodec<UtopiaStructure> CODEC = simpleCodec(UtopiaStructure::new);

	public UtopiaStructure(Structure.StructureSettings settings) {
		super(settings);
	}

	@Override
	protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		if (!(context.chunkGenerator() instanceof UtopiaChunkGenerator generator)) {
			return Optional.empty();
		}
		ChunkPos chunk = context.chunkPos();
		UtopiaTerrain terrain = generator.terrain(context.seed());
		List<UtopiaPlan.Build> builds = UtopiaPlan.of(terrain, context.seed(), chunk.x(), chunk.z()).builds();
		if (builds.isEmpty()) {
			return Optional.empty();
		}
		UtopiaPlan.Build first = builds.get(0);
		return Optional.of(new GenerationStub(new BlockPos(first.x(), first.y(), first.z()), builder -> {
			for (UtopiaPlan.Build build : builds) {
				BufferWorld world = UtopiaBuilds.get(terrain, build);
				if (!world.isEmpty()) {
					builder.addPiece(new Piece(build, world.bounds()));
				}
			}
		}));
	}

	@Override
	public StructureType<?> type() {
		return Utopia.BIG_STRUCTURE;
	}

	/** One big build; saves only its plan (the blocks are rebuilt from it). */
	public static final class Piece extends StructurePiece {
		private final UtopiaPlan.Build build;

		Piece(UtopiaPlan.Build build, BoundingBox box) {
			super(Utopia.BIG_PIECE, 0, box);
			this.build = build;
		}

		public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
			super(Utopia.BIG_PIECE, tag);
			UtopiaPlan.Kind kind;
			try {
				kind = UtopiaPlan.Kind.valueOf(tag.getStringOr("Kind", "SQUARE"));
			} catch (IllegalArgumentException e) {
				kind = UtopiaPlan.Kind.SQUARE;
			}
			this.build = new UtopiaPlan.Build(kind, tag.getIntOr("X", 0), tag.getIntOr("Y", 0), tag.getIntOr("Z", 0), tag.getIntOr("TreeType", 0),
				tag.getIntOr("Radius", 6), tag.getIntOr("NoCritters", 0) != 0, tag.getIntOr("AppleLeaves", 0) != 0, tag.getLongOr("Seed", 0L));
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
			tag.putString("Kind", this.build.kind().name());
			tag.putInt("X", this.build.x());
			tag.putInt("Y", this.build.y());
			tag.putInt("Z", this.build.z());
			tag.putInt("TreeType", this.build.treeType());
			tag.putInt("Radius", this.build.radius());
			tag.putInt("NoCritters", this.build.noCritters() ? 1 : 0);
			tag.putInt("AppleLeaves", this.build.appleLeaves() ? 1 : 0);
			tag.putLong("Seed", this.build.seed());
		}

		@Override
		public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
				BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
			if (generator instanceof UtopiaChunkGenerator utopia) {
				UtopiaBuilds.get(utopia.terrain(level.getSeed()), this.build).place(level, chunkBB, random);
			}
		}
	}
}
