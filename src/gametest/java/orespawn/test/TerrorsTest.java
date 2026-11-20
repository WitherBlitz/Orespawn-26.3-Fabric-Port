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
import orespawn.content.Terrors;
import orespawn.entity.terrors.Cephadrome;
import orespawn.entity.terrors.GammaMetroid;
import orespawn.entity.terrors.PitchBlack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Terrors group: OreSpawn's health and armour (a Nightmare's grow with its size); iron ingots tame a WTF?, which
 * then sits and stands on command and is freed with a dead bush; an empty hand mounts a Cephadrome and its rider steers
 * it; a Nightmare drops its scale and an item frame, a Basilisk its scale.
 */
public class TerrorsTest implements FabricClientGameTest {
	private final List<String> problems = new ArrayList<>();

	/** get_mobstats defaults (and the Cephadrome's and Cliff Racer's own): health, armour. */
	private static final Map<EntityType<? extends Mob>, float[]> STATS = new LinkedHashMap<>();

	static {
		STATS.put(Terrors.CREEPING_HORROR, new float[] {10, 2});
		STATS.put(Terrors.TERRIBLE_TERROR, new float[] {10, 3});
		STATS.put(Terrors.LURKING_TERROR, new float[] {30, 5});
		STATS.put(Terrors.KYUUBI, new float[] {125, 10});
		STATS.put(Terrors.WTF, new float[] {100, 12});
		STATS.put(Terrors.ALIEN, new float[] {100, 8});
		STATS.put(Terrors.BASILISK, new float[] {200, 15});
		STATS.put(Terrors.CEPHADROME, new float[] {300, 16});
		STATS.put(Terrors.CLIFF_RACER, new float[] {5, 0});
	}

	private static void log(String line) {
		System.out.println("OS-TERRORS " + line);
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

	private String dropped(ServerLevel level, AABB box, List<Item> wanted) {
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
				"forceload add 0 0 96 96", "gamerule entity_cramming 0")) {
				server.runCommand(cmd);
			}
			int g = server.computeOnServer(s -> s.overworld().getHeight(Heightmap.Types.MOTION_BLOCKING, 32, 32));

			String stats = server.computeOnServer(s -> {
				StringBuilder b = new StringBuilder();
				int i = 0;
				for (var e : STATS.entrySet()) {
					Mob m = spawn(s.overworld(), e.getKey(), 8 + 6 * i++, g + 1, 8);
					if (m == null) b.append(BuiltInRegistries.ENTITY_TYPE.getKey(e.getKey())).append(" didn't spawn; ");
					else if (m.getMaxHealth() != e.getValue()[0] || m.getAttributeValue(Attributes.ARMOR) != e.getValue()[1]) {
						b.append(BuiltInRegistries.ENTITY_TYPE.getKey(e.getKey())).append(' ').append(m.getMaxHealth()).append('/')
							.append(m.getAttributeValue(Attributes.ARMOR)).append("; ");
					}
				}
				PitchBlack n = spawn(s.overworld(), Terrors.NIGHTMARE, 70, g + 1, 8);
				float size = n.getPitchBlackScale();
				if (n.getMaxHealth() != (int) (250 * size) || n.getAttributeValue(Attributes.ARMOR) != 10 + (int) (2 * size)
					|| Math.abs(n.getBbWidth() - 2.5F * size) > 0.01F) {
					b.append("nightmare of size ").append(size).append(": ").append(n.getMaxHealth()).append('/')
						.append(n.getAttributeValue(Attributes.ARMOR)).append(", width ").append(n.getBbWidth()).append("; ");
				}
				return b.toString();
			});
			check(stats.isEmpty(), "Terrors have OreSpawn's health and armour (the Nightmare's by its size): " + stats);
			server.runCommand("kill @e[type=!player]");

			String taming = server.computeOnServer(s -> {
				ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
				p.teleportTo(32.5, g + 1, 30.5);
				GammaMetroid w = spawn(s.overworld(), Terrors.WTF, 32.5, g + 1, 32.5);
				for (int i = 0; i < 60 && !w.isTame(); i++) {
					p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT));
					w.mobInteract(p, InteractionHand.MAIN_HAND);
				}
				if (!w.isTame() || !w.isOwnedBy(p)) return "iron ingots never tamed it";
				p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
				w.mobInteract(p, InteractionHand.MAIN_HAND);
				if (!w.isOrderedToSit()) return "a click didn't make it sit";
				w.mobInteract(p, InteractionHand.MAIN_HAND);
				if (w.isOrderedToSit()) return "a second click didn't make it stand";
				p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DEAD_BUSH));
				w.mobInteract(p, InteractionHand.MAIN_HAND);
				return w.isTame() ? "a dead bush didn't free it" : "";
			});
			check(taming.isEmpty(), "iron tames a WTF?; it sits and stands on command; a dead bush frees it: " + taming);
			server.runCommand("kill @e[type=!player]");

			String riding = server.computeOnServer(s -> {
				ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
				p.teleportTo(52.5, g + 1, 50.5);
				Cephadrome c = spawn(s.overworld(), Terrors.CEPHADROME, 52.5, g + 1, 52.5);
				p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
				c.mobInteract(p, InteractionHand.MAIN_HAND);
				if (p.getVehicle() != c) return "an empty hand didn't mount it";
				return c.getControllingPassenger() == p ? "" : "its rider doesn't steer it";
			});
			check(riding.isEmpty(), "an empty hand mounts a Cephadrome, and its rider steers it: " + riding);
			server.runCommand("kill @e[type=!player]");
			context.waitTicks(5);
			server.runCommand("kill @e[type=item]");

			server.computeOnServer(s -> spawn(s.overworld(), Terrors.NIGHTMARE, 72.5, g + 1, 72.5));
			server.computeOnServer(s -> spawn(s.overworld(), Terrors.BASILISK, 20.5, g + 1, 72.5));
			server.runCommand("kill @e[type=orespawn:nightmare]");
			server.runCommand("kill @e[type=orespawn:basilisk]");
			context.waitTicks(5);
			String drops = server.computeOnServer(s -> dropped(s.overworld(), new AABB(72.5, g + 1, 72.5, 72.5, g + 1, 72.5).inflate(20.0),
				List.of(Terrors.NIGHTMARE_SCALE, Items.ITEM_FRAME)) + dropped(s.overworld(),
				new AABB(20.5, g + 1, 72.5, 20.5, g + 1, 72.5).inflate(6.0), List.of(Terrors.BASILISK_SCALE, Items.EMERALD)));
			check(drops.isEmpty(), "a Nightmare drops its scale and an item frame, a Basilisk its scale and emeralds: missing " + drops);
		}
		if (!problems.isEmpty()) throw new AssertionError("OreSpawn Terrors test failed:\n - " + String.join("\n - ", problems));
		log("ALL PASSED");
	}
}
