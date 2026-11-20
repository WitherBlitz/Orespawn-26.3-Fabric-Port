package orespawn.world.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import orespawn.OreSpawn;
import orespawn.world.structures.Build.Loot;

import java.util.List;

/**
 * OreSpawn's Water Dragon lair (OreSpawnWorld.addWaterDragonLair, GenericDungeon.makeWaterDragonLair): a sand island in
 * the ocean with a leafy log and four Water Dragon spawners, a chest, a ring of glowstone, lapis and Water Dragon egg
 * ore pillars 10 blocks out, and a bedrock roof with an iron ring and cross.
 * <p>
 * Not ported yet: while the Water Dragon's egg ore isn't registered, lapis blocks (the ring's other block) take its place.
 */
public record WaterDragonLairFeature() implements Feature {
	public static final MapCodec<WaterDragonLairFeature> CODEC = MapCodec.unit(WaterDragonLairFeature::new);

	/** GenericDungeon.WaterDragonContentsList. */
	static final List<Loot> LOOT = List.of(Loot.of(Items.COD, 6, 16, 25), Loot.of("ultimate_axe", 1, 1, 15),
		Loot.of("ultimate_pickaxe", 1, 1, 15), Loot.of("ultimate_shovel", 1, 1, 15), Loot.of("experience_orb_catcher", 4, 10, 25),
		Loot.of(Items.IRON_BLOCK, 6, 16, 25), Loot.of(Items.ROTTEN_FLESH, 6, 16, 25));

	@Override
	public MapCodec<WaterDragonLairFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(final WorldGenLevel level, final ChunkGenerator generator, final RandomSource random, final BlockPos origin) {
		return Build.onSurface(level, random, origin, 4, 100, false, Build.WATER, (l, r, x, y, z) -> build(l, r, x, y - 1, z));
	}

	/** {@code y} is the ocean's top water block. Casts and float steps are OreSpawn's, so the circles match. */
	static void build(WorldGenLevel level, RandomSource random, int x, int y, int z) {
		BlockState eggOre = Build.block("water_dragon_egg_ore");
		if (eggOre.isAir()) eggOre = Blocks.LAPIS_BLOCK.defaultBlockState();
		float radius = 10.0F;
		for (float currad = 0.0F; currad < radius; currad += 0.33F) {
			for (float curdeg = 0.0F; curdeg < 360.0F; curdeg += 5.0F) {
				float curx = (float) (currad * Math.cos(Math.toRadians(curdeg)));
				float curz = (float) (currad * Math.sin(Math.toRadians(curdeg)));
				BlockState blk = Blocks.BEDROCK.defaultBlockState();
				if (currad > 5.0F && currad < 6.0F) blk = Blocks.IRON_BLOCK.defaultBlockState();
				Build.set(level, (int) (x + curx + 0.5F), y + 7, (int) (z + curz + 0.5F), blk);
			}
		}
		for (int i = 1; i < 10; i++) {
			Build.set(level, (int) (x + i + 0.5F), y + 7, (int) (z + 0.5F), Blocks.IRON_BLOCK);
			Build.set(level, (int) (x - i + 0.5F), y + 7, (int) (z + 0.5F), Blocks.IRON_BLOCK);
			Build.set(level, (int) (x + 0.5F), y + 7, (int) (z + i + 0.5F), Blocks.IRON_BLOCK);
			Build.set(level, (int) (x + 0.5F), y + 7, (int) (z - i + 0.5F), Blocks.IRON_BLOCK);
		}
		Build.set(level, (int) (x + 0.5F), y + 7, (int) (z + 0.5F), Build.AIR);
		Build.set(level, (int) (x + 1 + 0.5F), y + 7, (int) (z + 0.5F), Blocks.GLOWSTONE);
		Build.set(level, (int) (x - 1 + 0.5F), y + 7, (int) (z + 0.5F), Blocks.GLOWSTONE);
		Build.set(level, (int) (x + 0.5F), y + 7, (int) (z + 1 + 0.5F), Blocks.GLOWSTONE);
		Build.set(level, (int) (x + 0.5F), y + 7, (int) (z - 1 + 0.5F), Blocks.GLOWSTONE);
		float ring = 10.0F;
		for (float curdeg = 0.0F; curdeg < 360.0F; curdeg += 5.0F) {
			float curx = (float) (ring * Math.cos(Math.toRadians(curdeg)));
			float curz = (float) (ring * Math.sin(Math.toRadians(curdeg)));
			int px = (int) (x + curx + 0.5F);
			int pz = (int) (z + curz + 0.5F);
			Build.set(level, px, y + 1, pz, Blocks.GLOWSTONE);
			Build.set(level, px, y + 2, pz, random.nextInt(2) == 0 ? eggOre : Blocks.LAPIS_BLOCK.defaultBlockState());
			Build.set(level, px, y + 3, pz, random.nextInt(2) == 0 ? eggOre : Blocks.LAPIS_BLOCK.defaultBlockState());
			Build.set(level, px, y + 4, pz, Blocks.GLOWSTONE);
			Build.set(level, px, y + 5, pz, Blocks.BEDROCK);
			Build.set(level, px, y + 6, pz, Blocks.BEDROCK);
		}
		for (int i = -3; i <= 3; i++) {
			for (int j = -3; j <= 3; j++) {
				Build.set(level, x + i, y, z + j, Blocks.SAND);
				Build.set(level, x + i, y - 1, z + j, Blocks.STONE);
			}
		}
		tree(level, random, x, y, z, OreSpawn.id("water_dragon"));
		Build.fill(Build.chest(level, x, y + 1, z - 1), random, LOOT, 4 + random.nextInt(5));
	}

	/** The leafy log with a spawner on each side that the lair and Monster Island share. */
	static void tree(WorldGenLevel level, RandomSource random, int x, int y, int z, Identifier mob) {
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) Build.set(level, x + i, y + 3, z + j, Build.LEAVES);
		}
		Build.set(level, x, y + 4, z, Build.LEAVES);
		Build.set(level, x, y + 3, z, Blocks.OAK_LOG);
		Build.set(level, x, y + 2, z, Blocks.OAK_LOG);
		Build.set(level, x, y + 1, z, Blocks.OAK_LOG);
		Build.set(level, x + 1, y + 3, z + 1, Blocks.OAK_LOG);
		Build.set(level, x - 1, y + 3, z - 1, Blocks.OAK_LOG);
		Build.set(level, x + 1, y + 3, z - 1, Blocks.OAK_LOG);
		Build.set(level, x - 1, y + 3, z + 1, Blocks.OAK_LOG);
		Build.spawner(level, random, x + 1, y + 3, z, mob);
		Build.spawner(level, random, x - 1, y + 3, z, mob);
		Build.spawner(level, random, x, y + 3, z + 1, mob);
		Build.spawner(level, random, x, y + 3, z - 1, mob);
	}
}
