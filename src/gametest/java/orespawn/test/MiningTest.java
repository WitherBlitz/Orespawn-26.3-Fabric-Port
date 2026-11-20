package orespawn.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import orespawn.OreSpawn;
import orespawn.world.OreSpawnDimensions;
import orespawn.world.mining.MiningChunkGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * The Mining group: the orespawn:mining dimension loads with its generator, y 0-255 and the overworld's clock; its
 * chunks have bedrock at the bottom, stone, and a grass or stone surface in Extreme Hills' height range; the biome is
 * 0.8 warm; the monster and ambient spawn lists hold OreSpawn's Mining creatures that are registered; the ant nests'
 * placed feature is loaded.
 */
public class MiningTest implements FabricClientGameTest {
	private final List<String> problems = new ArrayList<>();

	private static void log(String line) {
		System.out.println("OS-MINING " + line);
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
				ServerLevel level = s.getLevel(OreSpawnDimensions.MINING);
				if (level == null) return "no dimension";
				return (level.getChunkSource().getGenerator() instanceof MiningChunkGenerator ? "" : "other generator ")
					+ level.getMinY() + ".." + level.getMaxY() + " clock " + (level.getDefaultClockTime() == s.overworld().getDefaultClockTime());
			});
			check(setup.equals("0..255 clock true"), "the Mining dimension loads with its generator, y 0-255, the overworld's clock: " + setup);
			if (setup.equals("no dimension")) {
				throw new AssertionError("OreSpawn Mining test failed:\n - " + String.join("\n - ", problems));
			}

			String terrain = server.computeOnServer(s -> {
				ServerLevel level = s.getLevel(OreSpawnDimensions.MINING);
				StringBuilder b = new StringBuilder();
				int stone = 0;
				int lowest = 999;
				int highest = 0;
				for (int[] c : new int[][] {{0, 0}, {7, -3}, {-12, 20}}) {
					LevelChunk chunk = level.getChunk(c[0], c[1]);
					for (int x = 0; x < 16; x++) {
						for (int z = 0; z < 16; z++) {
							BlockPos pos = new BlockPos(chunk.getPos().getMinBlockX() + x, 0, chunk.getPos().getMinBlockZ() + z);
							if (!level.getBlockState(pos).is(Blocks.BEDROCK)) b.append("no bedrock at ").append(pos.toShortString()).append("; ");
							int top = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
							lowest = Math.min(lowest, top);
							highest = Math.max(highest, top);
							for (int y = 5; y < 60; y++) {
								BlockState state = level.getBlockState(pos.atY(y));
								if (state.is(Blocks.STONE)) stone++;
							}
						}
					}
				}
				if (stone < 3 * 256 * 20) b.append("little stone: ").append(stone).append("; ");
				if (lowest < 30 || highest > 200) b.append("surface ").append(lowest).append("..").append(highest).append("; ");
				return b.toString();
			});
			check(terrain.isEmpty(), "Mining chunks: bedrock at y 0, stone below, surface in Extreme Hills' range: " + terrain);

			float temperature = server.computeOnServer(s -> {
				ServerLevel level = s.getLevel(OreSpawnDimensions.MINING);
				return level.getBiome(new BlockPos(0, 80, 0)).value().getBaseTemperature();
			});
			check(temperature == 0.8F, "the Mining biome is 0.8 warm, as OreSpawn made Extreme Hills: " + temperature);

			String spawns = server.computeOnServer(s -> {
				ServerLevel level = s.getLevel(OreSpawnDimensions.MINING);
				var generator = level.getChunkSource().getGenerator();
				BlockPos pos = new BlockPos(0, 70, 0);
				List<String> monsters = ids(generator.getMobsAt(level, level.structureManager(), MobCategory.MONSTER, pos).unwrap());
				List<String> ambient = ids(generator.getMobsAt(level, level.structureManager(), MobCategory.AMBIENT, pos).unwrap());
				StringBuilder b = new StringBuilder();
				for (String id : List.of("alosaurus", "t_rex", "nastysaurus", "pointysaurus", "wtf", "alien", "cave_fisher", "cryolophosaurus",
					"baby_dragon")) {
					if (s.registryAccess().lookupOrThrow(Registries.ENTITY_TYPE).containsKey(OreSpawn.id(id)) && !monsters.contains(id)) {
						b.append("monster list lacks ").append(id).append("; ");
					}
				}
				for (String id : List.of("velocity_raptor", "dragonfly", "camarasaurus", "baryonyx")) {
					if (s.registryAccess().lookupOrThrow(Registries.ENTITY_TYPE).containsKey(OreSpawn.id(id)) && !ambient.contains(id)) {
						b.append("ambient list lacks ").append(id).append("; ");
					}
				}
				if (!monsters.contains("zombie") || !ambient.contains("bat")) b.append("vanilla mobs missing; ");
				return b.toString();
			});
			check(spawns.isEmpty(), "the Mining spawn lists hold OreSpawn's Mining creatures and Extreme Hills' mobs: " + spawns);

			boolean ants = server.computeOnServer(s -> s.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE)
				.get(ResourceKey.create(Registries.PLACED_FEATURE, OreSpawn.id("mining_ant_nests"))).isPresent());
			check(ants, "the Mining ant nests' placed feature is loaded");
		}
		if (!problems.isEmpty()) throw new AssertionError("OreSpawn Mining test failed:\n - " + String.join("\n - ", problems));
		log("ALL PASSED");
	}
}
