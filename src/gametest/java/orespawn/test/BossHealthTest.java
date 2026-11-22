package orespawn.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import orespawn.entity.ModEntities;
import orespawn.entity.bosses.BossUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * OreSpawn's mobs with health over 26.3's own max_health cap of 1024 (Mobzilla 4000, The King 7000, The Queen 6000, the
 * adult Prince 3000, the teen Prince 1500, the Robot Spider 1500):
 * <ul>
 * <li>each has its OreSpawn health, or the cap if that's lower: OreSpawn health with AttributeFix (which raises the cap),
 * 1024 without it;</li>
 * <li>its hit-and-heal scale times its OreSpawn health is its max health, so it takes as many hits as in OreSpawn either
 * way (scale 1 with AttributeFix);</li>
 * <li>one saved under the old 1024 cap, with half its health, loads with half its (new) max health.</li>
 * </ul>
 */
public class BossHealthTest implements FabricClientGameTest {
	private static final Map<String, Float> OVER_CAP = new LinkedHashMap<>();

	static {
		OVER_CAP.put("mobzilla", 4000.0F);
		OVER_CAP.put("the_king", 7000.0F);
		OVER_CAP.put("the_queen", 6000.0F);
		OVER_CAP.put("the_young_adult_prince", 3000.0F);
		OVER_CAP.put("the_young_prince", 1500.0F);
		OVER_CAP.put("robot_spider", 1500.0F);
	}

	private final List<String> problems = new ArrayList<>();

	private static void log(String line) {
		System.out.println("OS-BOSSHEALTH " + line);
	}

	private void check(boolean ok, String what) {
		log((ok ? "ok   " : "FAIL ") + what);
		if (!ok) problems.add(what);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		boolean attributeFix = FabricLoader.getInstance().isModLoaded("attributefix");
		double cap = BossUtil.healthCap();
		log("max_health cap " + cap + (attributeFix ? " (AttributeFix loaded)" : " (no AttributeFix)"));
		check(attributeFix ? cap >= 7000.0 : cap == 1024.0, "max_health's cap is " + cap + ", " + (attributeFix ? "at least 7000 with AttributeFix" : "1024 without AttributeFix"));
		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			TestServerContext server = sp.getServer();
			for (String cmd : List.of("gamerule spawn_mobs false", "time set noon", "difficulty peaceful", "difficulty easy", "forceload add -64 -64 64 64")) {
				server.runCommand(cmd);
			}
			for (Map.Entry<String, Float> e : OVER_CAP.entrySet()) {
				String result = server.computeOnServer(s -> {
					ServerLevel level = s.overworld();
					EntityType<? extends Mob> type = ModEntities.TYPES.get(e.getKey());
					if (type == null) return "FAIL no entity type orespawn:" + e.getKey();
					float oreSpawn = e.getValue();
					float want = (float) Math.min(cap, oreSpawn);
					int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, 0, 0) + 1;
					Mob mob = type.spawn(level, new BlockPos(0, y, 0), EntitySpawnReason.COMMAND);
					if (mob == null) return "FAIL didn't spawn";
					mob.setNoAi(true);
					float max = mob.getMaxHealth();
					float scale = BossUtil.healthScale(oreSpawn);
					StringBuilder out = new StringBuilder();
					boolean ok = Math.abs(max - want) < 0.01F && Math.abs(mob.getHealth() - want) < 0.01F && Math.abs(scale * oreSpawn - max) < 0.5F;
					out.append(ok ? "ok   " : "FAIL ").append(String.format(Locale.ROOT, "max health %.0f (OreSpawn %.0f, expected %.0f), health %.0f, hit scale %.4f",
						max, oreSpawn, want, mob.getHealth(), scale));
					// A save from before: under the 1024 cap, half health, no saved max (or a saved max of 1024).
					float savedMax = Math.min(1024.0F, oreSpawn);
					for (boolean tagged : new boolean[] {false, true}) {
						TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
						mob.saveWithoutId(output);
						CompoundTag tag = output.buildResult();
						tag.putFloat(LivingEntity.TAG_HEALTH, savedMax / 2.0F);
						if (tagged) tag.putFloat("OreSpawnMaxHealth", savedMax);
						else tag.remove("OreSpawnMaxHealth");
						Mob loaded = type.create(level, EntitySpawnReason.LOAD);
						loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
						boolean half = Math.abs(loaded.getHealth() - loaded.getMaxHealth() / 2.0F) < 0.5F;
						ok &= half;
						out.append(String.format(Locale.ROOT, "; old save%s at %.0f/%.0f loads at %.0f/%.0f", tagged ? " (max saved)" : "",
							savedMax / 2.0F, savedMax, loaded.getHealth(), loaded.getMaxHealth()));
						loaded.discard();
					}
					mob.discard();
					return (ok ? "ok   " : "FAIL ") + out.substring(5);
				});
				log(e.getKey() + ": " + result);
				if (result.startsWith("FAIL")) problems.add(e.getKey() + ": " + result);
			}
		}
		if (!problems.isEmpty()) throw new AssertionError("OreSpawn boss health test failed:\n - " + String.join("\n - ", problems));
		log("ALL PASSED");
	}
}
