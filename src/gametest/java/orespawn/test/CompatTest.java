package orespawn.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * OreSpawn with the pack's locked features, run only when those mods are loaded (the AllMods run):
 * <ul>
 * <li>Epic Siege's "mobs grab better gear": a zombie walks to an OreSpawn sword and chestplate on the ground and puts
 * them on.</li>
 * <li>Knockdown: a Bomb-Omb blowing up next to a survival player (OreSpawn's own explosion) knocks them down.</li>
 * </ul>
 */
public class CompatTest implements FabricClientGameTest {
	private final List<String> problems = new ArrayList<>();
	private ClientGameTestContext context;

	private static void log(String line) {
		System.out.println("OS-COMPAT " + line);
	}

	private void check(boolean ok, String what) {
		log((ok ? "ok   " : "FAIL ") + what);
		if (!ok) problems.add(what);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		this.context = context;
		boolean siege = FabricLoader.getInstance().isModLoaded("epicsiegemod");
		boolean knockdown = FabricLoader.getInstance().isModLoaded("knockdown");
		if (!siege && !knockdown) {
			log("skipped: Epic Siege and Knockdown aren't loaded");
			return;
		}
		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			TestServerContext server = sp.getServer();
			for (String cmd : List.of("gamerule spawn_mobs false", "time set midnight", "weather clear", "difficulty easy",
				"gamerule advance_time false", "gamerule mob_griefing true", "forceload add -32 -32 32 32")) {
				server.runCommand(cmd);
			}
			int g = server.computeOnServer(s -> s.overworld().getHeight(Heightmap.Types.MOTION_BLOCKING, 0, 0));
			if (siege) checkGearUp(server, g);
			if (knockdown) checkKnockdown(server, g);
		}
		if (!problems.isEmpty()) throw new AssertionError("OreSpawn compatibility test failed:\n - " + String.join("\n - ", problems));
		log("ALL PASSED");
	}

	private void checkGearUp(TestServerContext server, int g) {
		server.runCommand("gamemode spectator @p");
		server.runCommand(String.format(Locale.ROOT, "tp @p 3.5 %d -10.5", g + 6));
		server.runCommand(String.format(Locale.ROOT, "summon minecraft:zombie 0.5 %d 0.5 {PersistenceRequired:1b,CanPickUpLoot:1b}", g));
		server.runCommand(String.format(Locale.ROOT, "summon item 6.5 %d 0.5 {Item:{id:\"orespawn:ruby_sword\",count:1},PickupDelay:0s}", g));
		server.runCommand(String.format(Locale.ROOT, "summon item 0.5 %d 6.5 {Item:{id:\"orespawn:ruby_chestplate\",count:1},PickupDelay:0s}", g));
		int ticks = waitUntil(server, 600, s -> {
			Zombie z = zombie(s);
			return z != null && id(z.getItemBySlot(EquipmentSlot.MAINHAND).getItem()).equals("orespawn:ruby_sword")
				&& id(z.getItemBySlot(EquipmentSlot.CHEST).getItem()).equals("orespawn:ruby_chestplate");
		});
		String gear = server.computeOnServer(s -> {
			Zombie z = zombie(s);
			return z == null ? "no zombie" : id(z.getItemBySlot(EquipmentSlot.MAINHAND).getItem()) + ", " + id(z.getItemBySlot(EquipmentSlot.CHEST).getItem());
		});
		check(ticks >= 0, "Epic Siege: a zombie walks to an OreSpawn ruby sword and chestplate 6 blocks away and puts them on (" + ticks
			+ " ticks; it has " + gear + ")");
		server.runOnServer(s -> {
			List<Entity> all = new ArrayList<>();
			s.overworld().getAllEntities().forEach(all::add);
			for (Entity e : all) if (!(e instanceof ServerPlayer)) e.discard();
		});
	}

	private void checkKnockdown(TestServerContext server, int g) {
		server.runCommand(String.format(Locale.ROOT, "tp @p 20.5 %d 0.5 90 0", g));
		server.runCommand("gamemode survival @p");
		server.runOnServer(s -> player(s).setHealth(player(s).getMaxHealth()));
		context.waitTicks(10);
		server.runCommand(String.format(Locale.ROOT, "summon orespawn:bomb_omb 23.5 %d 0.5", g));
		int ticks = waitUntil(server, 400, s -> knockedDown(player(s)));
		float health = server.computeOnServer(s -> player(s).getHealth());
		check(ticks >= 0, "Knockdown: a Bomb-Omb blowing up next to a survival player knocks them down (" + ticks + " ticks, health now "
			+ health + ")");
		server.runCommand("gamemode spectator @p");
		server.runCommand("kill @e[type=orespawn:bomb_omb]");
	}

	private static String id(net.minecraft.world.item.Item item) {
		return BuiltInRegistries.ITEM.getKey(item).toString();
	}

	private static ServerPlayer player(MinecraftServer s) {
		return s.getPlayerList().getPlayers().getFirst();
	}

	private static Zombie zombie(MinecraftServer s) {
		List<? extends Zombie> all = s.overworld().getEntities(net.minecraft.world.entity.EntityTypes.ZOMBIE, z -> z.isAlive());
		return all.isEmpty() ? null : all.getFirst();
	}

	/** Knockdown's own API, looked up by name (Knockdown is only on the AllMods run's classpath). */
	private static boolean knockedDown(Entity entity) {
		try {
			return (boolean) Class.forName("knockdown.api.KnockdownApi").getMethod("isKnockedDown", Entity.class).invoke(null, entity);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Knockdown's API isn't there", e);
		}
	}

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
}
