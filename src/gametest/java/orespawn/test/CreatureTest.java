package orespawn.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.entity.Beaver;
import orespawn.entity.Chipmunk;
import orespawn.entity.Cricket;
import orespawn.entity.Frog;
import orespawn.entity.ModEntities;
import orespawn.entity.Moth;
import orespawn.entity.Rat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Creature batch 1 (apple cows, butterfly, moth, firefly, bird, chipmunk, cricket, frog, beaver, cassowary, rat):
 * each spawns with OreSpawn's health and has a renderer; spawn eggs and egg ores make them; chipmunks tame and sit;
 * a beaver fells a tree; a frog eats a cricket; a rat bites; a moth goes to a torch at night; birds and butterflies fly;
 * the golden cows drop their golden apples; the biomes list their spawns. Screenshots of each creature and variant.
 */
public class CreatureTest implements FabricClientGameTest {
	private final List<String> problems = new ArrayList<>();
	private ClientGameTestContext context;
	private final boolean allMods = System.getenv("ORESPAWN_ALLMODS") != null;

	/** OreSpawn's health for each creature (mygetMaxHealth / mob stats); the cows are vanilla cows. */
	private static final Map<String, Float> HEALTH = new LinkedHashMap<>();

	static {
		HEALTH.put("apple_cow", 10.0F);
		HEALTH.put("golden_apple_cow", 10.0F);
		HEALTH.put("enchanted_golden_apple_cow", 10.0F);
		HEALTH.put("butterfly", 2.0F);
		HEALTH.put("moth", 2.0F);
		HEALTH.put("firefly", 1.0F);
		HEALTH.put("bird", 2.0F);
		HEALTH.put("chipmunk", 5.0F);
		HEALTH.put("cricket", 3.0F);
		HEALTH.put("frog", 8.0F);
		HEALTH.put("beaver", 15.0F);
		HEALTH.put("cassowary", 10.0F);
		HEALTH.put("rat", 5.0F);
	}

	private static void log(String line) {
		System.out.println("OS-CREATURE " + line);
	}

	private void check(boolean ok, String what) {
		log((ok ? "ok   " : "FAIL ") + what);
		if (!ok) problems.add(what);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		this.context = context;
		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			TestServerContext server = sp.getServer();
			for (String cmd : List.of("gamerule spawn_mobs false", "time set noon", "weather clear", "difficulty easy", "gamerule advance_time false",
				"forceload add 32 -64 160 64", "gamerule entity_cramming 0")) {
				server.runCommand(cmd);
			}
			int g = server.computeOnServer(s -> s.overworld().getHeight(Heightmap.Types.MOTION_BLOCKING, 64, 0));
			checkSpawnAndHealth(server, g);
			checkRecipesAndEggs(server, g);
			checkChipmunk(server, g);
			checkFlying(server, g);
			checkFrog(server, g);
			checkRat(server, g);
			checkBeaver(server, g);
			checkMoth(server, g);
			checkCowDrops(server, g);
			checkRagdolls(server, sp, g);
			shots(server, sp, g);
		}
		try (TestSingleplayerContext sp = context.worldBuilder().setUseConsistentSettings(false).create()) {
			checkBiomeSpawns(sp.getServer());
		}
		if (!problems.isEmpty()) throw new AssertionError("OreSpawn creature test failed:\n - " + String.join("\n - ", problems));
		log("ALL PASSED");
	}

	private static ServerPlayer player(MinecraftServer s) {
		return s.getPlayerList().getPlayers().getFirst();
	}

	@SuppressWarnings("unchecked")
	private static <T extends Mob> T spawn(ServerLevel level, EntityType<T> type, double x, double y, double z) {
		T mob = type.spawn(level, BlockPos.containing(x, y, z), EntitySpawnReason.COMMAND);
		if (mob != null) mob.setPersistenceRequired();
		return mob;
	}

	// ---- Spawning ----

	/** The spawn checks' own area, away from the other checks (the giants crush blocks). */
	private static final int AREA_X = 400, AREA_Z = 400, AREA_END_X = 600;

	/** Where each creature stands in the spawn checks: in rows along x, each as far from the last as its width needs. */
	private static Map<String, Vec3> layout(int g) {
		Map<String, Vec3> out = new LinkedHashMap<>();
		double x = AREA_X, z = AREA_Z, rowDepth = 0;
		for (var e : ModEntities.TYPES.entrySet()) {
			double gap = Math.max(3.0, e.getValue().getDimensions().width() + 2.0);
			if (x + gap > AREA_END_X) {
				x = AREA_X;
				z += rowDepth;
				rowDepth = 0;
			}
			out.put(e.getKey(), new Vec3(x + gap / 2, g + 1, z + gap / 2));
			x += gap;
			rowDepth = Math.max(rowDepth, gap);
		}
		return out;
	}

	private void checkSpawnAndHealth(TestServerContext server, int g) {
		Map<String, Vec3> spots = layout(g);
		double endZ = spots.values().stream().mapToDouble(Vec3::z).max().orElse(AREA_Z) + 30;
		server.runCommand(String.format(Locale.ROOT, "forceload add %d %d %d %d", AREA_X - 16, AREA_Z - 16, AREA_END_X + 16, (int) endZ));
		Vec3 middle = new Vec3((AREA_X + AREA_END_X) / 2.0, g + 1, (AREA_Z + endZ) / 2.0);
		server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f", middle.x, g + 1, middle.z));
		context.waitTicks(20);
		// With their AI off, so this checks spawning, ticking and staying, not who wins a fight. Each is followed by its id,
		// since some move on their own even without AI (Ender Knights teleport in sunlight, the baby dragon flies).
		Map<String, java.util.UUID> uuids = new LinkedHashMap<>();
		Map<String, Integer> netIds = new LinkedHashMap<>();
		Map<String, String> bad = server.computeOnServer(s -> {
			Map<String, String> out = new TreeMap<>();
			for (var e : ModEntities.TYPES.entrySet()) {
				Vec3 p = spots.get(e.getKey());
				Mob mob = spawn(s.overworld(), e.getValue(), p.x, p.y, p.z);
				if (mob == null) {
					out.put(e.getKey(), "didn't spawn");
					continue;
				}
				mob.setNoAi(true);
				uuids.put(e.getKey(), mob.getUUID());
				netIds.put(e.getKey(), mob.getId());
				Float want = HEALTH.get(e.getKey());
				if (want != null && mob.getMaxHealth() != want) out.put(e.getKey(), "health " + mob.getMaxHealth() + ", OreSpawn's is " + want);
			}
			return out;
		});
		check(bad.isEmpty(), "all " + ModEntities.TYPES.size() + " creatures spawn with OreSpawn's health: " + bad);
		context.waitTicks(40);
		List<String> died = server.computeOnServer(s -> {
			List<String> out = new ArrayList<>();
			for (var e : uuids.entrySet()) {
				Entity x = s.overworld().getEntity(e.getValue());
				if (x == null || !x.isAlive()) out.add(e.getKey());
			}
			return out;
		});
		check(died.isEmpty(), "every creature is still there 2 seconds after spawning (no crash, no instant despawn): missing " + died);
		// The client: creatures with short tracking ranges (ants: 1 chunk) only reach it up close, so go to any not seen yet.
		List<String> noRenderer = new ArrayList<>();
		for (var e : netIds.entrySet()) {
			int id = e.getValue();
			java.util.function.Function<net.minecraft.client.Minecraft, String> seen = mc -> {
				Entity x = mc.level.getEntity(id);
				if (x == null) return "absent";
				return mc.getEntityRenderDispatcher().getRenderer(x) == null ? "no renderer" : "";
			};
			String result = context.computeOnClient(seen::apply);
			// Fliers and teleporters (the Ender Knight jumps away in sunlight) keep moving, so rather than chasing one into
			// chunks the client hasn't got yet, it's brought next to the player, whose chunks the client already has.
			if (result.equals("absent")) {
				java.util.UUID uuid = uuids.get(e.getKey());
				Vec3 spot = spots.get(e.getKey());
				server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %.1f %.1f", spot.x, spot.y + 1.0, spot.z - 4.0));
				context.waitTicks(20);
				for (int tries = 0; tries < 3; tries++) {
					server.runOnServer(s -> {
						Entity x = s.overworld().getEntity(uuid);
						var player = s.getPlayerList().getPlayers().getFirst();
						if (x != null) x.teleportTo(player.getX(), player.getY(), player.getZ() + 4.0);
					});
					context.waitTicks(10);
					result = context.computeOnClient(seen::apply);
					if (!result.equals("absent")) break;
				}
			}
			// Still not there (one that teleports or flies off on its own can slip away again): a fresh one, made next to the
			// player with its AI off, must reach the client.
			if (result.equals("absent")) {
				EntityType<? extends Mob> type = ModEntities.TYPES.get(e.getKey());
				int fresh = server.computeOnServer(s -> {
					var player = s.getPlayerList().getPlayers().getFirst();
					Mob mob = spawn(s.overworld(), type, player.getX(), player.getY(), player.getZ() + 4.0);
					if (mob == null) return -1;
					mob.setNoAi(true);
					return mob.getId();
				});
				context.waitTicks(10);
				result = fresh < 0 ? "absent" : context.computeOnClient(mc -> {
					Entity x = mc.level.getEntity(fresh);
					if (x == null) return "absent";
					return mc.getEntityRenderDispatcher().getRenderer(x) == null ? "no renderer" : "";
				});
				if (result.isEmpty()) log("note: " + e.getKey() + " moved out of reach; a fresh one reached the client");
			}
			if (!result.isEmpty()) noRenderer.add(e.getKey() + " (" + (result.equals("absent") ? "not on the client" : result) + ")");
		}
		check(noRenderer.isEmpty(), "every creature reaches the client and has a renderer: missing " + noRenderer);
		server.runCommand("kill @e[type=!player]");
		checkAiSoak(server, g, spots, middle);
	}

	/**
	 * Every creature again, now with its AI on, for 5 seconds, with the player watching as a spectator: an exception in
	 * any creature's AI, attacks or effects would stop the server here. Fights and the giants' block crushing are expected.
	 */
	private void checkAiSoak(TestServerContext server, int g, Map<String, Vec3> spots, Vec3 middle) {
		String mode = server.computeOnServer(s -> player(s).gameMode.getGameModeForPlayer().getName());
		server.runCommand("gamemode spectator @p");
		server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f", middle.x, g + 30, middle.z));
		server.runOnServer(s -> {
			for (var e : ModEntities.TYPES.entrySet()) {
				Vec3 p = spots.get(e.getKey());
				spawn(s.overworld(), e.getValue(), p.x, p.y, p.z);
			}
		});
		context.waitTicks(100);
		int alive = server.computeOnServer(s -> s.overworld().getEntitiesOfClass(Mob.class, new AABB(AREA_X - 16, g - 20, AREA_Z - 16, AREA_END_X + 16,
			g + 80, AREA_Z + 400)).size());
		log("AI soak: 5 seconds with all " + ModEntities.TYPES.size() + " creatures' AI on, server still running; " + alive + " mobs left");
		check(alive > 0, "the server keeps running with every creature's AI on for 5 seconds");
		server.runCommand("kill @e[type=!player]");
		server.runCommand("gamemode " + mode + " @p");
		server.runCommand("tp @p 84 " + (g + 1) + " 44");
		context.waitTicks(10);
	}

	// ---- Egg ores and spawn eggs ----

	private void checkRecipesAndEggs(TestServerContext server, int g) {
		String result = server.computeOnServer(s -> {
			ServerPlayer p = player(s);
			ServerLevel level = s.overworld();
			StringBuilder b = new StringBuilder();
			BlockPos floor = new BlockPos(70, g, 70);
			p.teleportTo(70.5, g + 1, 72.5);
			for (String id : List.of("rat", "bird", "apple_cow")) {
				ItemStack egg = new ItemStack(ModEntities.SPAWN_EGGS.get(id));
				p.setItemInHand(InteractionHand.MAIN_HAND, egg);
				egg.useOn(new UseOnContext(p, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(floor).add(0, 0.5, 0), Direction.UP, floor,
					false)));
				int n = level.getEntitiesOfClass(Mob.class, new AABB(floor).inflate(3), m -> m.getType() == ModEntities.TYPES.get(id)).size();
				b.append(id).append('=').append(n).append(' ');
			}
			return b.toString().trim();
		});
		check(result.equals("rat=1 bird=1 apple_cow=1"), "OreSpawn's spawn eggs hatch their creatures: " + result);
		server.runCommand("kill @e[type=!player]");
	}

	// ---- Chipmunk taming ----

	private void checkChipmunk(TestServerContext server, int g) {
		String result = server.computeOnServer(s -> {
			ServerPlayer p = player(s);
			Chipmunk c = spawn(s.overworld(), ModEntities.CHIPMUNK, 80.5, g + 1, 80.5);
			c.setNoAi(true);
			p.teleportTo(80.5, g + 1, 82.0);
			int apples = 0;
			while (!c.isTame() && apples < 30) {
				p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE));
				p.interactOn(c, InteractionHand.MAIN_HAND, c.position());
				apples++;
			}
			boolean tame = c.isTame() && c.isOwnedBy(p);
			p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
			p.interactOn(c, InteractionHand.MAIN_HAND, c.position());
			boolean sat = c.isOrderedToSit();
			p.interactOn(c, InteractionHand.MAIN_HAND, c.position());
			boolean stood = !c.isOrderedToSit();
			p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DEAD_BUSH));
			p.interactOn(c, InteractionHand.MAIN_HAND, c.position());
			boolean freed = !c.isTame();
			return "tame=" + tame + " after " + apples + " apples, sat=" + sat + " stood=" + stood + " freed=" + freed;
		});
		check(result.startsWith("tame=true") && result.endsWith("sat=true stood=true freed=true"),
			"a chipmunk tames with apples, sits and stands for its owner, and a dead bush frees it: " + result);
		server.runCommand("kill @e[type=!player]");
	}

	// ---- Flying ----

	private void checkFlying(TestServerContext server, int g) {
		server.runOnServer(s -> {
			spawn(s.overworld(), ModEntities.BIRD, 90.5, g + 3, 40.5);
			spawn(s.overworld(), ModEntities.BUTTERFLY, 94.5, g + 3, 40.5);
		});
		double[] highest = {0, 0};
		for (int t = 0; t < 200; t += 5) {
			context.waitTicks(5);
			double[] h = server.computeOnServer(s -> {
				double bird = -99, fly = -99;
				for (Mob m : s.overworld().getEntitiesOfClass(Mob.class, new AABB(60, g - 5, 10, 130, g + 60, 70))) {
					if (m.getType() == ModEntities.BIRD) bird = m.getY() - g;
					if (m.getType() == ModEntities.BUTTERFLY) fly = m.getY() - g;
				}
				return new double[] {bird, fly};
			});
			highest[0] = Math.max(highest[0], h[0]);
			highest[1] = Math.max(highest[1], h[1]);
		}
		log(String.format(Locale.ROOT, "highest above the ground in 10 s: bird %.1f, butterfly %.1f", highest[0], highest[1]));
		check(highest[0] > 2.0 && highest[1] > 2.0, "birds and butterflies fly about (highest above ground: bird " + highest[0] + ", butterfly "
			+ highest[1] + ")");
		server.runCommand("kill @e[type=!player]");
	}

	// ---- Frog eats a cricket ----

	private void checkFrog(TestServerContext server, int g) {
		server.runOnServer(s -> {
			s.overworld().setBlockAndUpdate(new BlockPos(100, g, 100), Blocks.GRASS_BLOCK.defaultBlockState());
			spawn(s.overworld(), ModEntities.FROG, 100.5, g + 1, 100.5);
			Cricket c = spawn(s.overworld(), ModEntities.CRICKET, 102.5, g + 1, 100.5);
			c.setNoAi(true);
		});
		int ticks = waitUntil(server, 600, s -> s.overworld().getEntitiesOfClass(Cricket.class, new AABB(90, g - 5, 90, 112, g + 10, 112)).isEmpty());
		check(ticks >= 0, "a frog hunts and eats a cricket (" + ticks + " ticks)");
		server.runCommand("kill @e[type=!player]");
	}

	// ---- Rat bites ----

	private void checkRat(TestServerContext server, int g) {
		server.runOnServer(s -> {
			spawn(s.overworld(), ModEntities.RAT, 110.5, g + 1, 40.5);
			Pig pig = spawn(s.overworld(), net.minecraft.world.entity.EntityTypes.PIG, 112.5, g + 1, 40.5);
			pig.setNoAi(true);
		});
		int ticks = waitUntil(server, 600, s -> s.overworld().getEntitiesOfClass(Pig.class, new AABB(100, g - 5, 30, 125, g + 10, 50))
			.stream().anyMatch(p -> p.getHealth() < p.getMaxHealth()));
		check(ticks >= 0, "a rat goes for a pig nearby (" + ticks + " ticks)");
		server.runCommand("kill @e[type=!player]");
	}

	// ---- Beaver fells a tree ----

	private void checkBeaver(TestServerContext server, int g) {
		server.runCommand("gamerule mob_griefing true");
		server.runOnServer(s -> {
			ServerLevel level = s.overworld();
			for (int y = 1; y <= 5; y++) level.setBlockAndUpdate(new BlockPos(122, g + y, 120), Blocks.OAK_LOG.defaultBlockState());
			Beaver b = spawn(level, ModEntities.BEAVER, 120.5, g + 1, 120.5);
			b.setHealth(b.getMaxHealth() - 2.0F);
		});
		int ticks = waitUntil(server, 1200, s -> {
			ServerLevel level = s.overworld();
			for (int y = 1; y <= 5; y++) if (level.getBlockState(new BlockPos(122, g + y, 120)).is(Blocks.OAK_LOG)) return false;
			return true;
		});
		int logs = server.computeOnServer(s -> s.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(110, g - 5, 110, 135, g + 20, 135))
			.stream().filter(i -> i.getItem().is(Items.OAK_LOG)).mapToInt(i -> i.getItem().getCount()).sum());
		check(ticks >= 0 && logs == 5, "a hungry beaver fells a 5-log tree into log items (" + ticks + " ticks, " + logs + " logs dropped)");
		server.runCommand("kill @e[type=!player]");
	}

	// ---- Moth goes to a torch at night ----

	private void checkMoth(TestServerContext server, int g) {
		server.runCommand("time set midnight");
		BlockPos torch = new BlockPos(140, g + 1, 40);
		server.runOnServer(s -> {
			s.overworld().setBlockAndUpdate(torch, Blocks.TORCH.defaultBlockState());
			spawn(s.overworld(), ModEntities.MOTH, 149.5, g + 3, 40.5);
		});
		double[] closest = {99};
		for (int t = 0; t < 400; t += 5) {
			context.waitTicks(5);
			double d = server.computeOnServer(s -> s.overworld().getEntitiesOfClass(Moth.class, new AABB(torch).inflate(30)).stream()
				.mapToDouble(m -> Math.sqrt(m.distanceToSqr(Vec3.atCenterOf(torch)))).min().orElse(99));
			closest[0] = Math.min(closest[0], d);
		}
		check(closest[0] < 3.0, String.format(Locale.ROOT, "at night a moth flies to a torch 9 blocks away (closest %.1f blocks)", closest[0]));
		server.runCommand("kill @e[type=!player]");
		server.runCommand("time set noon");
	}

	// ---- Cow drops ----

	private void checkCowDrops(TestServerContext server, int g) {
		String drops = server.computeOnServer(s -> {
			ServerLevel level = s.overworld();
			StringBuilder b = new StringBuilder();
			for (EntityType<? extends Mob> type : List.of(ModEntities.GOLDEN_APPLE_COW, ModEntities.ENCHANTED_GOLDEN_APPLE_COW)) {
				Mob cow = spawn(level, type, 60.5, g + 1, 100.5);
				cow.hurtServer(level, level.damageSources().playerAttack(player(s)), 1000.0F);
				int golden = 0, enchanted = 0;
				for (ItemEntity i : level.getEntitiesOfClass(ItemEntity.class, new AABB(60, g - 2, 100, 61, g + 3, 101).inflate(3))) {
					if (i.getItem().is(Items.GOLDEN_APPLE)) golden += i.getItem().getCount();
					if (i.getItem().is(Items.ENCHANTED_GOLDEN_APPLE)) enchanted += i.getItem().getCount();
					i.discard();
				}
				b.append(BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath()).append(": golden ").append(golden).append(", enchanted ")
					.append(enchanted).append("; ");
			}
			return b.toString();
		});
		check(drops.equals("golden_apple_cow: golden 1, enchanted 0; enchanted_golden_apple_cow: golden 2, enchanted 1; "),
			"the golden cows drop OreSpawn's apples: " + drops);
	}

	// ---- Ragdolls (Wither's Combat) ----

	/** The creatures in witherscombatmod:ragdoll_from_model (written by tools/gen_assets.py from each spec's "ragdoll"). */
	private static final net.minecraft.tags.TagKey<EntityType<?>> FROM_MODEL = net.minecraft.tags.TagKey.create(Registries.ENTITY_TYPE,
		Identifier.fromNamespaceAndPath("witherscombatmod", "ragdoll_from_model"));

	/** The tagged creatures, plus four insects that must leave no body. */
	private static List<String> ragdollCandidates() {
		List<String> out = new ArrayList<>();
		for (var e : ModEntities.TYPES.entrySet()) {
			if (e.getValue().builtInRegistryHolder().is(FROM_MODEL)) out.add(e.getKey());
		}
		out.addAll(List.of("butterfly", "moth", "firefly", "cricket"));
		return out;
	}

	/** Whether the loaded Wither's Combat takes ragdoll kinds from entity type tags (its 2026-11-20 hook). */
	private static boolean ragdollTags() {
		try {
			Class.forName("bettercombat.ragdoll.Gore").getField("FROM_MODEL");
			return true;
		} catch (ReflectiveOperationException | LinkageError e) {
			return false;
		}
	}

	private void checkRagdolls(TestServerContext server, TestSingleplayerContext sp, int g) {
		if (!FabricLoader.getInstance().isModLoaded("witherscombatmod")) {
			log("Wither's Combat isn't loaded: no ragdoll check");
			return;
		}
		boolean hook = ragdollTags();
		server.runCommand("gamemode spectator @p");
		context.runOnClient(mc -> {
			if (!mc.gui.hud.isHidden()) mc.gui.hud.toggle();
		});
		List<String> candidates = server.computeOnServer(s -> ragdollCandidates());
		int tagged = candidates.size() - 4;
		int rows = (candidates.size() + 9) / 10;
		look(server, new Vec3(149.0, g + 4.5 + rows * 0.6, 108.0 + rows * 2.5), new Vec3(149.0, g + 0.3, 100.5 + rows * 1.2));
		context.waitTicks(20);
		server.runOnServer(s -> {
			ServerLevel level = s.overworld();
			int i = 0;
			for (String id : candidates) {
				Mob mob = spawn(level, ModEntities.TYPES.get(id), 141.5 + 1.6 * (i % 10), g, 99.5 + 2.5 * (i / 10));
				i++;
				if (mob == null) continue;
				mob.setNoAi(true);
				mob.hurtServer(level, level.damageSources().playerAttack(player(s)), 100000.0F);
			}
		});
		context.waitTicks(60);
		Map<String, Integer> bodies = server.computeOnServer(s -> {
			Map<String, Integer> out = new TreeMap<>();
			for (Entity e : s.overworld().getEntitiesOfClass(Entity.class, new AABB(130, g - 3, 90, 175, g + 12, 100 + rows * 2.5 + 10),
				x -> BuiltInRegistries.ENTITY_TYPE.getKey(x.getType()).toString().equals("witherscombatmod:ragdoll"))) {
				out.merge("bodies", 1, Integer::sum);
			}
			return out;
		});
		int count = bodies.getOrDefault("bodies", 0);
		log("Wither's Combat " + (hook ? "with" : "without") + " the ragdoll tag hook: " + count + " bodies from " + candidates.size()
			+ " creatures (" + tagged + " tagged)");
		if (hook) {
			check(count == tagged, "the " + tagged + " tagged creatures each leave a Wither's Combat ragdoll, the insects none: "
				+ count + " bodies");
			shot(sp, "cr_30_ragdolls");
		}
		server.runCommand("kill @e[type=!player,type=!witherscombatmod:ragdoll]");
	}

	// ---- Biome spawn lists ----

	private void checkBiomeSpawns(TestServerContext server) {
		String result = server.computeOnServer(s -> {
			var biomes = s.registryAccess().lookupOrThrow(Registries.BIOME);
			StringBuilder b = new StringBuilder();
			for (String[] want : new String[][] {{"forest", "butterfly"}, {"forest", "bird"}, {"jungle", "firefly"}, {"dark_forest", "rat"},
				{"plains", "apple_cow"}, {"mushroom_fields", "enchanted_golden_apple_cow"}, {"river", "beaver"}, {"swamp", "frog"},
				{"old_growth_pine_taiga", "cassowary"}, {"forest", "chipmunk"}, {"plains", "cricket"}, {"forest", "moth"}}) {
				Biome biome = biomes.getOrThrow(ResourceKey.create(Registries.BIOME, Identifier.withDefaultNamespace(want[0]))).value();
				EntityType<?> type = ModEntities.TYPES.get(want[1]);
				boolean listed = biome.getAttributes().applyModifier(net.minecraft.world.attribute.EnvironmentAttributes.NATURAL_MOB_SPAWNS, net.minecraft.world.level.biome.MobSpawnSettings.EMPTY).getMobsToSpawn(type.getCategory()).unwrap().stream().anyMatch(w -> w.value().type() == type);
				if (!listed) b.append(want[1]).append(" not in ").append(want[0]).append("; ");
			}
			return b.toString();
		});
		check(result.isEmpty(), "OreSpawn's creatures are in their biomes' spawn lists: " + result);
	}

	// ---- Screenshots ----

	private interface ServerCheck {
		boolean test(MinecraftServer s);
	}

	private int waitUntil(TestServerContext server, int maxTicks, ServerCheck check) {
		for (int t = 0; t <= maxTicks; t += 5) {
			if (server.computeOnServer(check::test)) return t;
			context.waitTicks(5);
		}
		return -1;
	}

	/** Puts the camera (the player's eyes, 1.62 above the feet) at {@code from}, looking at {@code at}. */
	private void look(TestServerContext server, Vec3 from, Vec3 at) {
		Vec3 d = at.subtract(from);
		float yaw = (float) (Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0);
		float pitch = (float) -Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)));
		server.runCommand(String.format(Locale.ROOT, "tp @p %.2f %.2f %.2f %.1f %.1f", from.x, from.y - 1.62, from.z, yaw, pitch));
		context.waitTicks(2);
	}

	private void shot(TestSingleplayerContext sp, String name) {
		context.waitTicks(10);
		sp.getConnection().waitForChunksRender(true, 2400);
		context.waitTicks(5);
		Path path = context.takeScreenshot(name);
		log("screenshot " + path);
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

	/** Takes every creature of a type out of the world without killing it. */
	private void remove(TestServerContext server, net.minecraft.world.entity.EntityType<?> type) {
		server.runOnServer(s -> {
			for (Entity x : List.copyOf(s.overworld().getEntities(type, t -> true))) x.discard();
		});
	}

	/** Stops any rain and waits (up to 6 seconds) for it to fade from the sky. */
	private void clearSky(TestServerContext server) {
		if (server.computeOnServer(s -> !s.overworld().isRaining() && s.overworld().getRainLevel(1.0F) == 0.0F)) return;
		server.runCommand("weather clear");
		waitUntil(server, 120, s -> s.overworld().getRainLevel(1.0F) == 0.0F && s.overworld().getThunderLevel(1.0F) == 0.0F);
	}

	/** Summons a creature with no AI facing the camera; nbt may set its variant. */
	private void pose(TestServerContext server, String id, double x, int g, double z, String nbt) {
		server.runCommand(String.format(Locale.ROOT, "summon orespawn:%s %.2f %d %.2f {NoAI:1b,PersistenceRequired:1b,Rotation:[0f,0f]%s}", id, x, g, z,
			nbt.isEmpty() ? "" : "," + nbt));
	}

	private void shots(TestServerContext server, TestSingleplayerContext sp, int g) {
		server.runCommand("gamemode spectator @p");
		context.runOnClient(mc -> {
			if (!mc.gui.hud.isHidden()) mc.gui.hud.toggle();
		});
		// Each creature is removed rather than killed, so no loot, death puffs or ragdolls pile up at the spot, and the sky
		// is cleared first (the Kraken brings rain and thunder, as in OreSpawn).
		server.runOnServer(s -> {
			List<Entity> all = new ArrayList<>();
			s.overworld().getAllEntities().forEach(all::add);
			for (Entity x : all) if (!(x instanceof net.minecraft.world.entity.player.Player)) x.discard();
		});
		clearSky(server);
		int i = 0;
		for (var e : ModEntities.TYPES.entrySet()) {
			String id = e.getKey();
			double x = 100, z = -40;
			remove(server, e.getValue());
			pose(server, id, x + 0.5, g, z + 0.5, "");
			float h = e.getValue().getDimensions().height(), w = e.getValue().getDimensions().width();
			double dist = Math.max(0.9, Math.max(h, w) * 1.8 + 0.5);
			look(server, new Vec3(x + 0.5 + dist * 0.4, g + h * 0.8 + dist * 0.35, z + 0.5 + dist), new Vec3(x + 0.5, g + h * 0.4, z + 0.5));
			shot(sp, String.format(Locale.ROOT, "cr_%03d_%s", i + 1, id));
			remove(server, e.getValue());
			clearSky(server);
			i++;
		}
		// Variants: four butterflies, four moths, six birds.
		for (int k = 0; k < 4; k++) pose(server, "butterfly", 40.5 + k * 0.9, g, -60.5, "ButterflyType:" + k);
		look(server, new Vec3(41.85, g + 1.1, -58.4), new Vec3(41.85, g + 0.2, -60.5));
		shot(sp, "cr_20_butterflies");
		for (int k = 0; k < 4; k++) pose(server, "moth", 50.5 + k * 1.2, g, -60.5, "ButterflyType:" + k);
		look(server, new Vec3(52.3, g + 1.4, -57.9), new Vec3(52.3, g + 0.2, -60.5));
		shot(sp, "cr_21_moths");
		for (int k = 0; k < 6; k++) pose(server, "bird", 60.5 + k * 0.9, g, -60.5, "BirdType:" + k);
		look(server, new Vec3(62.75, g + 1.1, -58.4), new Vec3(62.75, g + 0.2, -60.5));
		shot(sp, "cr_22_birds");
		// Night: fireflies blinking and the luna moth's swirl.
		server.runCommand("time set midnight");
		for (int k = 0; k < 5; k++) pose(server, "firefly", 70.5 + k * 0.8, g, -60.5, "");
		pose(server, "moth", 74.5, g, -61.5, "ButterflyType:0");
		look(server, new Vec3(72.5, g + 1.2, -58.2), new Vec3(72.5, g + 0.3, -60.8));
		context.waitTicks(15);
		shot(sp, "cr_23_night_fireflies_and_luna_moth");
		server.runCommand("time set noon");
	}
}
