package orespawn.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import orespawn.OreSpawn;
import orespawn.world.OreSpawnDimensions;
import orespawn.world.chaos.ChaosChunkGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * The Chaos group: orespawn:chaos loads with its generator, y 0-255 and the overworld's clock; its chunks are floating
 * stone with grass and dirt between y 16 and 127, nothing under y 16, and oak logs from its trees; the biome is
 * orespawn:chaos, 0.8 warm; the spawn lists hold the Chaos creatures that are registered, and no vanilla ones.
 */
public class ChaosTest implements FabricClientGameTest {
	private final List<String> problems = new ArrayList<>();

	private static void log(String line) {
		System.out.println("OS-CHAOS " + line);
	}

	private void check(boolean ok, String what) {
		log((ok ? "ok   " : "FAIL ") + what);
		if (!ok) problems.add(what);
	}

	private static List<String> ids(List<Weighted<MobSpawnSettings.SpawnerData>> list) {
		List<String> out = new ArrayList<>();
		for (Weighted<MobSpawnSettings.SpawnerData> w : list) out.add(EntityType.getKey(w.value().type()).getPath());
		return out;
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			TestServerContext server = sp.getServer();
			server.runCommand("gamerule spawn_mobs false");

			String setup = server.computeOnServer(s -> {
				ServerLevel level = s.getLevel(OreSpawnDimensions.CHAOS);
				if (level == null) return "no dimension";
				return (level.getChunkSource().getGenerator() instanceof ChaosChunkGenerator ? "" : "other generator ")
					+ level.getMinY() + ".." + level.getMaxY() + " clock " + (level.getDefaultClockTime() == s.overworld().getDefaultClockTime());
			});
			check(setup.equals("0..255 clock true"), "the Chaos dimension loads with its generator, y 0-255, the overworld's clock: " + setup);
			if (setup.equals("no dimension")) {
				throw new AssertionError("OreSpawn Chaos test failed:\n - " + String.join("\n - ", problems));
			}

			String terrain = server.computeOnServer(s -> {
				ServerLevel level = s.getLevel(OreSpawnDimensions.CHAOS);
				int stone = 0;
				int grass = 0;
				int dirt = 0;
				int low = 0;
				int logs = 0;
				for (int cx = -1; cx <= 1; cx++) {
					for (int cz = -1; cz <= 1; cz++) {
						LevelChunk chunk = level.getChunk(cx, cz);
						for (int x = 0; x < 16; x++) {
							for (int z = 0; z < 16; z++) {
								BlockPos column = new BlockPos(chunk.getPos().getMinBlockX() + x, 0, chunk.getPos().getMinBlockZ() + z);
								for (int y = 0; y < 256; y++) {
									BlockState state = level.getBlockState(column.atY(y));
									if (state.isAir()) continue;
									if (y < 16 && !state.is(Blocks.WATER) && !state.is(Blocks.LAVA)) low++;
									if (state.is(Blocks.STONE)) stone++;
									if (state.is(Blocks.GRASS_BLOCK)) grass++;
									if (state.is(Blocks.DIRT)) dirt++;
									if (state.is(Blocks.OAK_LOG)) logs++;
								}
							}
						}
					}
				}
				StringBuilder b = new StringBuilder();
				if (stone < 9 * 256 * 20) b.append("little stone: ").append(stone).append("; ");
				if (grass < 9 * 256 / 2) b.append("little grass: ").append(grass).append("; ");
				if (dirt < grass) b.append("less dirt than grass: ").append(dirt).append("; ");
				if (low > 0) b.append(low).append(" blocks under y 16; ");
				if (logs == 0) b.append("no trees; ");
				return b.toString();
			});
			check(terrain.isEmpty(), "Chaos chunks: floating stone with grass over dirt, nothing under y 16, trees: " + terrain);

			String biome = server.computeOnServer(s -> {
				ServerLevel level = s.getLevel(OreSpawnDimensions.CHAOS);
				var holder = level.getBiome(new BlockPos(0, 90, 0));
				return holder.unwrapKey().map(k -> k.identifier().toString()).orElse("?") + " " + holder.value().getBaseTemperature();
			});
			check(biome.equals("orespawn:chaos 0.8"), "the Chaos biome is orespawn:chaos, 0.8 warm: " + biome);

			String spawns = server.computeOnServer(s -> {
				ServerLevel level = s.getLevel(OreSpawnDimensions.CHAOS);
				var generator = level.getChunkSource().getGenerator();
				BlockPos pos = new BlockPos(0, 90, 0);
				List<String> ambient = ids(generator.getMobsAt(level, level.structureManager(), MobCategory.AMBIENT, pos).unwrap());
				List<String> creature = ids(generator.getMobsAt(level, level.structureManager(), MobCategory.CREATURE, pos).unwrap());
				List<String> monster = ids(generator.getMobsAt(level, level.structureManager(), MobCategory.MONSTER, pos).unwrap());
				List<String> water = ids(generator.getMobsAt(level, level.structureManager(), MobCategory.WATER_CREATURE, pos).unwrap());
				StringBuilder b = new StringBuilder();
				var types = s.registryAccess().lookupOrThrow(Registries.ENTITY_TYPE);
				for (String id : List.of("butterfly", "cliff_racer", "cloud_shark", "baryonyx")) {
					if (types.containsKey(OreSpawn.id(id)) && !ambient.contains(id)) b.append("ambient lacks ").append(id).append("; ");
				}
				for (String id : List.of("apple_cow", "beaver")) {
					if (types.containsKey(OreSpawn.id(id)) && !creature.contains(id)) b.append("creature lacks ").append(id).append("; ");
				}
				for (String id : List.of("rat", "bomb_omb", "crystal_urchin", "hammerhead", "wtf")) {
					if (types.containsKey(OreSpawn.id(id)) && !monster.contains(id)) b.append("monster lacks ").append(id).append("; ");
				}
				if (!water.isEmpty()) b.append("has water creatures ").append(water).append("; ");
				if (monster.contains("zombie") || creature.contains("cow")) b.append("has vanilla mobs; ");
				return b.toString();
			});
			check(spawns.isEmpty(), "the Chaos spawn lists hold OreSpawn's Chaos creatures only: " + spawns);
		}
		if (!problems.isEmpty()) throw new AssertionError("OreSpawn Chaos test failed:\n - " + String.join("\n - ", problems));
		log("ALL PASSED");
	}
}
