package orespawn.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import orespawn.content.Oddities;
import orespawn.entity.oddities.Dragon;
import orespawn.entity.oddities.RubberDucky;
import orespawn.entity.oddities.Spyro;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Oddities group: OreSpawn's health and armour; raw fish tames a Rubber Ducky, which sits and stands on command and
 * is freed with a dead bush; a diamond grows a tamed Baby Dragon into a tamed Dragon; an empty hand mounts a tamed
 * Dragon and its rider steers it; Mothra, the Vortex and the Jumpy Bug drop what only they drop.
 */
public class OdditiesTest implements FabricClientGameTest {
	private final List<String> problems = new ArrayList<>();

	/** OreSpawn's health and armour (get_mobstats defaults, or the creature's own). */
	private static final Map<EntityType<? extends Mob>, float[]> STATS = new LinkedHashMap<>();

	static {
		STATS.put(Oddities.LEONOPTERYX, new float[] {250, 16});
		STATS.put(Oddities.BABY_DRAGON, new float[] {200, 5});
		STATS.put(Oddities.DRAGON, new float[] {200, 14});
		STATS.put(Oddities.STINKY, new float[] {100, 6});
		STATS.put(Oddities.MOTHRA, new float[] {150, 8});
		STATS.put(Oddities.RUBY_BIRD, new float[] {2, 0});
		STATS.put(Oddities.ROTATOR, new float[] {35, 8});
		STATS.put(Oddities.VORTEX, new float[] {150, 10});
		STATS.put(Oddities.CRIMINAL, new float[] {100, 18});
		STATS.put(Oddities.T_SHIRT, new float[] {1, 0});
		STATS.put(Oddities.COIN, new float[] {1, 0});
		STATS.put(Oddities.EASTER_BUNNY, new float[] {10, 0});
		STATS.put(Oddities.RUBBER_DUCKY, new float[] {5, 1});
		STATS.put(Oddities.SPIT_BUG, new float[] {100, 12});
		STATS.put(Oddities.JUMPY_BUG, new float[] {200, 15});
	}

	private static void log(String line) {
		System.out.println("OS-ODDITIES " + line);
	}

	private void check(boolean ok, String what) {
		log((ok ? "ok   " : "FAIL ") + what);
		if (!ok) problems.add(what);
	}

	private static <T extends Mob> T spawn(ServerLevel level, EntityType<T> type, double x, double y, double z) {
		T mob = type.spawn(level, BlockPos.containing(x, y, z), EntitySpawnReason.COMMAND);
		if (mob != null) {
			mob.setPersistenceRequired();
			mob.setNoAi(true);
		}
		return mob;
	}

	private static String dropped(ServerLevel level, AABB box, List<Item> wanted) {
		List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box);
		StringBuilder b = new StringBuilder();
		for (Item want : wanted) {
			if (items.stream().noneMatch(e -> e.getItem().is(want))) b.append(want).append(' ');
		}
		return b.toString();
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			TestServerContext server = sp.getServer();
			for (String cmd : List.of("gamerule spawn_mobs false", "time set noon", "weather clear", "difficulty easy", "gamerule advance_time false",
				"forceload add 0 0 112 112", "gamerule entity_cramming 0")) {
				server.runCommand(cmd);
			}
			int g = server.computeOnServer(s -> s.overworld().getHeight(Heightmap.Types.MOTION_BLOCKING, 32, 32));

			String stats = server.computeOnServer(s -> {
				StringBuilder b = new StringBuilder();
				int i = 0;
				for (var e : STATS.entrySet()) {
					Mob m = spawn(s.overworld(), e.getKey(), 8 + 7 * i++, g + 1, 8);
					if (m == null) b.append(BuiltInRegistries.ENTITY_TYPE.getKey(e.getKey())).append(" didn't spawn; ");
					else if (m.getMaxHealth() != e.getValue()[0] || m.getAttributeValue(Attributes.ARMOR) != e.getValue()[1]) {
						b.append(BuiltInRegistries.ENTITY_TYPE.getKey(e.getKey())).append(' ').append(m.getMaxHealth()).append('/')
							.append(m.getAttributeValue(Attributes.ARMOR)).append("; ");
					}
				}
				return b.toString();
			});
			check(stats.isEmpty(), "Oddities have OreSpawn's health and armour: " + stats);
			server.runCommand("kill @e[type=!player]");
			context.waitTicks(5);
			server.runCommand("kill @e[type=item]");

			String ducky = server.computeOnServer(s -> {
				ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
				p.teleportTo(32.5, g + 1, 30.5);
				RubberDucky d = spawn(s.overworld(), Oddities.RUBBER_DUCKY, 32.5, g + 1, 32.5);
				for (int i = 0; i < 60 && !d.isTame(); i++) {
					p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COD));
					d.mobInteract(p, InteractionHand.MAIN_HAND);
				}
				if (!d.isTame() || !d.isOwnedBy(p)) return "raw fish never tamed it";
				p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
				d.mobInteract(p, InteractionHand.MAIN_HAND);
				if (!d.isOrderedToSit()) return "a click didn't make it sit";
				d.mobInteract(p, InteractionHand.MAIN_HAND);
				if (d.isOrderedToSit()) return "a second click didn't make it stand";
				p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DEAD_BUSH));
				d.mobInteract(p, InteractionHand.MAIN_HAND);
				return d.isTame() ? "a dead bush didn't free it" : "";
			});
			check(ducky.isEmpty(), "raw fish tames a Rubber Ducky; it sits and stands on command; a dead bush frees it: " + ducky);
			server.runCommand("kill @e[type=!player]");

			String grow = server.computeOnServer(s -> {
				ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
				p.teleportTo(52.5, g + 1, 30.5);
				Spyro baby = spawn(s.overworld(), Oddities.BABY_DRAGON, 52.5, g + 1, 32.5);
				for (int i = 0; i < 60 && !baby.isTame(); i++) {
					p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEEF));
					baby.mobInteract(p, InteractionHand.MAIN_HAND);
				}
				if (!baby.isTame()) return "raw beef never tamed the Baby Dragon";
				p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND));
				baby.mobInteract(p, InteractionHand.MAIN_HAND);
				if (!baby.isRemoved()) return "the diamond didn't change it";
				List<Dragon> grown = s.overworld().getEntitiesOfClass(Dragon.class, new AABB(52.5, g + 1, 32.5, 52.5, g + 1, 32.5).inflate(3.0));
				return grown.size() == 1 && grown.getFirst().isOwnedBy(p) ? "" : "no tamed Dragon took its place";
			});
			check(grow.isEmpty(), "a diamond grows a tamed Baby Dragon into a tamed Dragon: " + grow);
			server.runCommand("kill @e[type=!player]");

			String riding = server.computeOnServer(s -> {
				ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
				p.teleportTo(72.5, g + 1, 30.5);
				Dragon d = spawn(s.overworld(), Oddities.DRAGON, 72.5, g + 1, 32.5);
				for (int i = 0; i < 100 && !d.isTame(); i++) {
					p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEEF));
					d.mobInteract(p, InteractionHand.MAIN_HAND);
				}
				if (!d.isTame()) return "raw beef never tamed it";
				p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
				d.mobInteract(p, InteractionHand.MAIN_HAND);
				if (p.getVehicle() != d) return "an empty hand didn't mount it";
				if (d.activity() == 0) return "it didn't take off";
				return d.getControllingPassenger() == p ? "" : "its rider doesn't steer it";
			});
			check(riding.isEmpty(), "an empty hand mounts a tamed Dragon, which takes off, and its rider steers it: " + riding);
			server.runCommand("kill @e[type=!player]");
			context.waitTicks(5);
			server.runCommand("kill @e[type=item]");

			server.computeOnServer(s -> spawn(s.overworld(), Oddities.MOTHRA, 20.5, g + 3, 80.5));
			server.computeOnServer(s -> spawn(s.overworld(), Oddities.VORTEX, 60.5, g + 1, 80.5));
			server.computeOnServer(s -> spawn(s.overworld(), Oddities.JUMPY_BUG, 100.5, g + 1, 80.5));
			server.runCommand("kill @e[type=orespawn:mothra]");
			server.runCommand("kill @e[type=orespawn:vortex]");
			server.runCommand("kill @e[type=orespawn:jumpy_bug]");
			context.waitTicks(5);
			String drops = server.computeOnServer(s -> dropped(s.overworld(), new AABB(20.5, g + 3, 80.5, 20.5, g + 3, 80.5).inflate(12.0),
				List.of(Oddities.MOTH_SCALE, Items.NETHER_STAR, Items.GOLD_NUGGET)) + dropped(s.overworld(),
				new AABB(60.5, g + 1, 80.5, 60.5, g + 1, 80.5).inflate(12.0), List.of(Oddities.VORTEX_EYE, Items.ITEM_FRAME)) + dropped(s.overworld(),
				new AABB(100.5, g + 1, 80.5, 100.5, g + 1, 80.5).inflate(8.0), List.of(Oddities.JUMPY_BUG_SCALE, Items.ITEM_FRAME)));
			check(drops.isEmpty(), "Mothra drops Moth Scales, a nether star and gold; the Vortex its eye; the Jumpy Bug its scale: missing " + drops);
		}
		if (!problems.isEmpty()) throw new AssertionError("OreSpawn Oddities test failed:\n - " + String.join("\n - ", problems));
		log("ALL PASSED");
	}
}
