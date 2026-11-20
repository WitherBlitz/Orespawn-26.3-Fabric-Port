package orespawn.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import orespawn.world.OreSpawnDimensions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * All six of OreSpawn's dimensions: each loads with its own chunk generator, a 3 x 3 patch of chunks generates (with its
 * features and structures) without an error and has ground, and a screenshot is taken standing on it.
 */
public class DimensionsTest implements FabricClientGameTest {
	private final List<String> problems = new ArrayList<>();
	private final boolean allMods = System.getenv("ORESPAWN_ALLMODS") != null;

	private static final Map<String, ResourceKey<Level>> DIMENSIONS = new java.util.LinkedHashMap<>();

	static {
		DIMENSIONS.put("utopia", OreSpawnDimensions.UTOPIA);
		DIMENSIONS.put("mining", OreSpawnDimensions.MINING);
		DIMENSIONS.put("village_mania", OreSpawnDimensions.VILLAGE_MANIA);
		DIMENSIONS.put("islands", OreSpawnDimensions.ISLANDS);
		DIMENSIONS.put("crystal", OreSpawnDimensions.CRYSTAL);
		DIMENSIONS.put("chaos", OreSpawnDimensions.CHAOS);
	}

	private static void log(String line) {
		System.out.println("OS-DIMENSIONS " + line);
	}

	private void check(boolean ok, String what) {
		log((ok ? "ok   " : "FAIL ") + what);
		if (!ok) problems.add(what);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext sp = context.worldBuilder().setUseConsistentSettings(false).create()) {
			TestServerContext server = sp.getServer();
			server.runCommand("gamerule spawn_mobs false");
			server.runCommand("gamemode spectator @p");
			context.runOnClient(mc -> {
				if (!mc.gui.hud.isHidden()) mc.gui.hud.toggle();
			});
			int n = 0;
			for (var d : DIMENSIONS.entrySet()) {
				String name = d.getKey();
				String result = server.computeOnServer(s -> {
					ServerLevel level = s.getLevel(d.getValue());
					if (level == null) return "no dimension";
					String gen = level.getChunkSource().getGenerator().getClass().getSimpleName();
					int columns = 0;
					int highest = Integer.MIN_VALUE;
					for (int cx = -1; cx <= 1; cx++) {
						for (int cz = -1; cz <= 1; cz++) {
							LevelChunk chunk = level.getChunk(cx, cz);
							for (int x = 0; x < 16; x++) {
								for (int z = 0; z < 16; z++) {
									int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
									if (top > level.getMinY()) columns++;
									highest = Math.max(highest, top);
								}
							}
						}
					}
					return gen + " ground in " + columns + " of 2304 columns, top y " + highest;
				});
				check(!result.equals("no dimension") && !result.startsWith("NoiseBasedChunkGenerator") && !result.contains(" ground in 0 "),
					name + " loads with its own generator and has ground: " + result);
				if (result.equals("no dimension")) continue;
				int top = server.computeOnServer(s -> s.getLevel(d.getValue()).getHeight(Heightmap.Types.MOTION_BLOCKING, 8, 8));
				server.runCommand(String.format(Locale.ROOT, "execute in %s run tp @p 8.5 %d 8.5 -30 25", d.getValue().identifier(), top + 12));
				context.waitTicks(40);
				sp.getConnection().waitForChunksRender(true, 2400);
				context.waitTicks(20);
				Path path = context.takeScreenshot(String.format(Locale.ROOT, "dim_%d_%s", ++n, name));
				log("screenshot " + path);
				copy(path);
			}
			server.runCommand("execute in minecraft:overworld run tp @p 0 100 0");
			context.waitTicks(20);
		}
		if (!problems.isEmpty()) throw new AssertionError("OreSpawn dimensions test failed:\n - " + String.join("\n - ", problems));
		log("ALL PASSED");
	}

	private void copy(Path path) {
		try {
			Path root = Path.of(System.getProperty("user.dir")).toAbsolutePath();
			while (root != null && !Files.exists(root.resolve("build.gradle"))) root = root.getParent();
			Path dir = root.resolve("screenshots").resolve(allMods ? "allmods" : "plain");
			Files.createDirectories(dir);
			Files.copy(path, dir.resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			log("couldn't copy screenshot: " + e);
		}
	}
}
