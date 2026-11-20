package orespawn.world.utopia;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.registry.ModBlocks;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runs one of Utopia's big builds ({@link UtopiaPlan.Build}) into a {@link BufferWorld}, as OreSpawnWorld.addHugeTree
 * and addOtherTrees called them, and keeps recent results: the structure start needs the build's extent, and every
 * chunk it covers then writes its share, so the same build is asked for many times while those chunks generate. A
 * build is a function of the world seed and its plan, so recomputing one (after a restart) gives the same blocks.
 */
public final class UtopiaBuilds {
	private record Key(long worldSeed, UtopiaPlan.Build build) {}

	private static final Map<Key, BufferWorld> CACHE = new LinkedHashMap<>(16, 0.75F, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Key, BufferWorld> eldest) {
			return this.size() > 32;
		}
	};

	private UtopiaBuilds() {
	}

	public static BufferWorld get(UtopiaTerrain terrain, UtopiaPlan.Build build) {
		Key key = new Key(terrain.seed(), build);
		synchronized (CACHE) {
			BufferWorld world = CACHE.get(key);
			if (world == null) {
				world = run(terrain, build);
				CACHE.put(key, world);
			}
			return world;
		}
	}

	private static BufferWorld run(UtopiaTerrain terrain, UtopiaPlan.Build b) {
		BufferWorld w = new BufferWorld(terrain, RandomSource.create(b.seed()));
		HugeTrees.Mat leaf = b.appleLeaves() ? new HugeTrees.Mat(UtopiaBlocks.appleLeaves()) : HugeTrees.LEAVES;
		BlockState moss = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
		switch (b.kind()) {
			case SQUARE -> HugeTrees.bigSquareTree(w, b.x(), b.y(), b.z(), HugeTrees.LOGS, leaf, moss, b.treeType(), b.radius(), b.noCritters(),
				HugeTrees.Guard.NONE);
			case EMERALD -> HugeTrees.bigSquareTree(w, b.x(), b.y(), b.z(), new HugeTrees.Mat(Blocks.GOLD_BLOCK.defaultBlockState()),
				new HugeTrees.Mat(Blocks.EMERALD_BLOCK.defaultBlockState()), Blocks.DIAMOND_BLOCK.defaultBlockState(), -1, b.radius(), true,
				HugeTrees.Guard.KING);
			case RUBY -> HugeTrees.bigSquareTree(w, b.x(), b.y(), b.z(), new HugeTrees.Mat(Blocks.OBSIDIAN.defaultBlockState()),
				new HugeTrees.Mat(ModBlocks.RUBY_BLOCK.defaultBlockState()), ModBlocks.AMETHYST_BLOCK.defaultBlockState(), -1, b.radius(), true,
				HugeTrees.Guard.QUEEN);
			case CIRCULAR -> HugeTrees.bigCircularTree(w, b.x(), b.y(), b.z(), HugeTrees.LOGS, leaf, moss, b.treeType(), b.radius(), b.noCritters());
			case ROUND -> HugeTrees.bigRoundTree(w, b.x(), b.y(), b.z(), HugeTrees.LOGS, leaf, b.treeType(), b.radius());
			case WIND -> Trees.windTree(w, b.x(), b.y(), b.z(), 0);
			case SKY -> Trees.skyTree(w, b.x(), b.y(), b.z());
		}
		return w;
	}
}
