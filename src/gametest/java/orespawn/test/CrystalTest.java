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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import orespawn.OreSpawn;
import orespawn.block.crystal.CrystalFurnaceBlockEntity;
import orespawn.content.Crystal;
import orespawn.world.OreSpawnDimensions;
import orespawn.world.crystal.CrystalChunkGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * The Crystal group: the orespawn:crystal dimension loads with its generator, y 0-255 and the overworld's clock; its
 * chunks have bedrock at y 0, Kyanite, Crystal Grass on top and the bedrock maze at y 24-28; sky light passes through
 * the Kyanite; the biome is 0.8 warm and lists the Crystal creatures that are registered (and no vanilla ones); the
 * Crystal Furnace smelts with Crystal Energy in 150 ticks and lights up.
 */
public class CrystalTest implements FabricClientGameTest {
	private final List<String> problems = new ArrayList<>();

	private static void log(String line) {
		System.out.println("OS-CRYSTAL " + line);
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
			server.runCommand("time set noon");

			String setup = server.computeOnServer(s -> {
				ServerLevel level = s.getLevel(OreSpawnDimensions.CRYSTAL);
				if (level == null) return "no dimension";
				return (level.getChunkSource().getGenerator() instanceof CrystalChunkGenerator ? "" : "other generator ")
					+ level.getMinY() + ".." + level.getMaxY() + " clock " + (level.getDefaultClockTime() == s.overworld().getDefaultClockTime());
			});
			check(setup.equals("0..255 clock true"), "the Crystal dimension loads with its generator, y 0-255, the overworld's clock: " + setup);
			if (setup.equals("no dimension")) {
				throw new AssertionError("OreSpawn Crystal test failed:\n - " + String.join("\n - ", problems));
			}

			// Generate the chunks first and give the light engine a moment before reading sky light.
			server.runOnServer(s -> {
				ServerLevel level = s.getLevel(OreSpawnDimensions.CRYSTAL);
				for (int[] c : new int[][] {{0, 0}, {7, -3}, {-12, 20}}) level.getChunk(c[0], c[1]);
			});
			context.waitTicks(40);
			String terrain = server.computeOnServer(s -> {
				ServerLevel level = s.getLevel(OreSpawnDimensions.CRYSTAL);
				StringBuilder b = new StringBuilder();
				StringBuilder why = new StringBuilder();
				int kyanite = 0;
				int grass = 0;
				int mazeFloor = 0;
				int mazeCeiling = 0;
				int dark = 0;
				for (int[] c : new int[][] {{0, 0}, {7, -3}, {-12, 20}}) {
					LevelChunk chunk = level.getChunk(c[0], c[1]);
					for (int x = 0; x < 16; x++) {
						for (int z = 0; z < 16; z++) {
							BlockPos pos = new BlockPos(chunk.getPos().getMinBlockX() + x, 0, chunk.getPos().getMinBlockZ() + z);
							if (!level.getBlockState(pos).is(Blocks.BEDROCK)) b.append("no bedrock at ").append(pos.toShortString()).append("; ");
							if (level.getBlockState(pos.atY(24)).is(Blocks.BEDROCK)) mazeFloor++;
							if (level.getBlockState(pos.atY(28)).is(Blocks.BEDROCK)) mazeCeiling++;
							for (int y = 29; y < 128; y++) {
								BlockState state = level.getBlockState(pos.atY(y));
								if (state.is(Crystal.KYANITE)) kyanite++;
								if (state.is(Crystal.CRYSTAL_GRASS)) grass++;
							}
							int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
							// Kyanite lets the sky through: just above the maze's ceiling is as bright as the surface.
							// Only columns where everything above passes sky light straight down (not under the sea, a tree or an ore).
							boolean open = true;
							for (int y = 36; y <= top && open; y++) open = level.getBlockState(pos.atY(y)).propagatesSkylightDown();
							if (open && top > 40 && level.getBrightness(LightLayer.SKY, pos.atY(35)) < 15) {
								dark++;
								if (dark <= 3) {
									why.append(pos.getX()).append(",").append(pos.getZ()).append(" light ").append(level.getBrightness(LightLayer.SKY, pos.atY(35)))
										.append(" top ").append(top).append(":");
									for (int y = 36; y <= top; y++) {
										BlockState st = level.getBlockState(pos.atY(y));
										if (!st.isAir() && !st.is(Crystal.KYANITE)) why.append(' ').append(y).append('=').append(st.getBlock().getDescriptionId());
									}
									why.append(" | ");
								}
							}
						}
					}
				}
				if (kyanite < 3 * 256 * 10) b.append("little kyanite: ").append(kyanite).append("; ");
				if (grass == 0) b.append("no crystal grass; ");
				if (mazeFloor < 3 * 256 - 3 || mazeCeiling < 3 * 256 - 12) b.append("maze floor ").append(mazeFloor).append(" ceiling ").append(mazeCeiling).append("; ");
				if (dark > 0) b.append(dark).append(" columns dark under the kyanite (").append(why).append("); ");
				return b.toString();
			});
			check(terrain.isEmpty(), "Crystal chunks: bedrock at y 0, Kyanite and Crystal Grass, the maze at y 24-28, sky light through Kyanite: " + terrain);

			float temperature = server.computeOnServer(s -> {
				ServerLevel level = s.getLevel(OreSpawnDimensions.CRYSTAL);
				return level.getBiome(new BlockPos(0, 80, 0)).value().getBaseTemperature();
			});
			check(temperature == 0.8F, "the Crystal biome is 0.8 warm, as OreSpawn made it: " + temperature);

			String spawns = server.computeOnServer(s -> {
				ServerLevel level = s.getLevel(OreSpawnDimensions.CRYSTAL);
				var generator = level.getChunkSource().getGenerator();
				BlockPos pos = new BlockPos(0, 70, 0);
				List<String> all = new ArrayList<>();
				for (MobCategory category : MobCategory.values()) {
					all.addAll(ids(generator.getMobsAt(level, level.structureManager(), category, pos).unwrap()));
				}
				StringBuilder b = new StringBuilder();
				for (String id : List.of("crystal_apple_cow", "fairy", "peacock", "mantis", "rotator", "vortex", "crystal_urchin", "dungeon_beast", "rat",
					"butterfly", "bird", "moth", "whale", "crab", "flounder", "irukandji", "skate", "frog")) {
					if (s.registryAccess().lookupOrThrow(Registries.ENTITY_TYPE).containsKey(OreSpawn.id(id)) && !all.contains(id)) {
						b.append("lacks ").append(id).append("; ");
					}
				}
				if (all.contains("zombie") || all.contains("cow")) b.append("has vanilla mobs; ");
				return b.toString();
			});
			check(spawns.isEmpty(), "the Crystal spawn lists hold OreSpawn's Crystal creatures only: " + spawns);

			BlockPos furnacePos = server.computeOnServer(s -> s.getPlayerList().getPlayers().get(0).blockPosition().offset(2, 0, 0));
			server.runOnServer(s -> {
				ServerLevel level = s.overworld();
				level.setBlockAndUpdate(furnacePos, Crystal.CRYSTAL_FURNACE.defaultBlockState());
				if (level.getBlockEntity(furnacePos) instanceof CrystalFurnaceBlockEntity furnace) {
					furnace.setItem(0, new ItemStack(Items.RAW_IRON, 2));
					furnace.setItem(1, new ItemStack(Crystal.CRYSTAL_ENERGY));
				}
			});
			context.waitTicks(60);
			String lit = server.computeOnServer(s -> {
				BlockState state = s.overworld().getBlockState(furnacePos);
				return state.is(Crystal.CRYSTAL_FURNACE) ? state.getValue(BlockStateProperties.LIT) + " light " + state.getLightEmission() : "gone";
			});
			check(lit.equals("true light 9"), "the Crystal Furnace burns Crystal Energy and glows (light 9): " + lit);
			context.waitTicks(110);
			String out = server.computeOnServer(s -> s.overworld().getBlockEntity(furnacePos) instanceof CrystalFurnaceBlockEntity furnace
				? furnace.getItem(2).getCount() + " " + furnace.getItem(2).getItem() + ", fuel left " + furnace.getItem(1).getCount() : "no furnace");
			check(out.startsWith("1 ") && out.contains("iron_ingot") && out.endsWith("fuel left 0"),
				"the Crystal Furnace smelts one raw iron in 150 ticks on one Crystal Energy: " + out);
		}
		if (!problems.isEmpty()) throw new AssertionError("OreSpawn Crystal test failed:\n - " + String.join("\n - ", problems));
		log("ALL PASSED");
	}
}
