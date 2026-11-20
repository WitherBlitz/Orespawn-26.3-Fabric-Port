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
import orespawn.content.Sea;
import orespawn.entity.sea.Hydrolisc;
import orespawn.entity.sea.WaterBall;
import orespawn.entity.sea.WaterDragon;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Sea group (Attack Squid, Hammerhead, Irukandji, Sea Monster, Sea Viper, Cloud Shark, Hydrolisc, Water Dragon,
 * Crystal Urchin): OreSpawn's health and armour; raw fish tames a Water Dragon, which then sits and stands on command
 * and is freed with a dead bush; no hit hurts a Hydrolisc for more than 10; a thrown WaterDragon Charge flies as a
 * water ball; a Sea Monster drops its scale, an item frame and raw cod.
 */
public class SeaTest implements FabricClientGameTest {
	private final List<String> problems = new ArrayList<>();

	/** OreSpawn's health and armour (get_mobstats defaults; the Hydrolisc's own). */
	private static final Map<EntityType<? extends Mob>, float[]> STATS = new LinkedHashMap<>();

	static {
		STATS.put(Sea.ATTACK_SQUID, new float[] {10, 0});
		STATS.put(Sea.HAMMERHEAD, new float[] {240, 20});
		STATS.put(Sea.IRUKANDJI, new float[] {1, 0});
		STATS.put(Sea.SEA_MONSTER, new float[] {110, 8});
		STATS.put(Sea.SEA_VIPER, new float[] {160, 12});
		STATS.put(Sea.CLOUD_SHARK, new float[] {15, 5});
		STATS.put(Sea.HYDROLISC, new float[] {100, 10});
		STATS.put(Sea.WATER_DRAGON, new float[] {150, 8});
		STATS.put(Sea.CRYSTAL_URCHIN, new float[] {25, 4});
	}

	private static void log(String line) {
		System.out.println("OS-SEA " + line);
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

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			TestServerContext server = sp.getServer();
			for (String cmd : List.of("gamerule spawn_mobs false", "time set noon", "weather clear", "difficulty easy", "gamerule advance_time false",
				"forceload add 0 0 64 64", "gamerule entity_cramming 0")) {
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
						b.append(BuiltInRegistries.ENTITY_TYPE.getKey(e.getKey())).append(' ').append(m.getMaxHealth()).append('/').append(m.getAttributeValue(Attributes.ARMOR))
							.append("; ");
					}
				}
				return b.toString();
			});
			check(stats.isEmpty(), "Sea creatures have OreSpawn's health and armour: " + stats);
			server.runCommand("kill @e[type=!player]");

			String taming = server.computeOnServer(s -> {
				ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
				p.teleportTo(32.5, g + 1, 30.5);
				WaterDragon d = spawn(s.overworld(), Sea.WATER_DRAGON, 32.5, g + 1, 32.5);
				for (int i = 0; i < 40 && !d.isTame(); i++) {
					p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COD));
					d.mobInteract(p, InteractionHand.MAIN_HAND);
				}
				if (!d.isTame() || !d.isOwnedBy(p)) return "raw fish never tamed it";
				p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
				d.mobInteract(p, InteractionHand.MAIN_HAND);
				if (!d.isOrderedToSit() || !d.isInSittingPose()) return "a stick didn't make it stay";
				d.mobInteract(p, InteractionHand.MAIN_HAND);
				if (d.isOrderedToSit()) return "a second click didn't let it move";
				p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DEAD_BUSH));
				d.mobInteract(p, InteractionHand.MAIN_HAND);
				return d.isTame() ? "a dead bush didn't free it" : "";
			});
			check(taming.isEmpty(), "raw fish tames a Water Dragon; it stays on command; a dead bush frees it: " + taming);
			server.runCommand("kill @e[type=!player]");

			float hydro = server.computeOnServer(s -> {
				Hydrolisc h = spawn(s.overworld(), Sea.HYDROLISC, 32.5, g + 1, 36.5);
				h.hurtServer(s.overworld(), s.overworld().damageSources().generic(), 50.0F);
				return h.getHealth();
			});
			check(hydro == 90.0F, "a 50-damage hit takes only 10 from a Hydrolisc (health " + hydro + ")");
			server.runCommand("kill @e[type=!player]");

			boolean thrown = server.computeOnServer(s -> {
				ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
				p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Sea.WATERDRAGON_CHARGE));
				Sea.WATERDRAGON_CHARGE.use(s.overworld(), p, InteractionHand.MAIN_HAND);
				return !s.overworld().getEntitiesOfClass(WaterBall.class, p.getBoundingBox().inflate(4.0)).isEmpty();
			});
			check(thrown, "a WaterDragon Charge is thrown as a water ball");

			server.computeOnServer(s -> spawn(s.overworld(), Sea.SEA_MONSTER, 40.5, g + 1, 40.5));
			server.runCommand("kill @e[type=orespawn:sea_monster]");
			context.waitTicks(5);
			String drops = server.computeOnServer(s -> {
				List<ItemEntity> items = s.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(40.5, g + 1, 40.5, 40.5, g + 1, 40.5).inflate(4.0));
				StringBuilder b = new StringBuilder();
				for (Item want : List.of(Sea.SEA_MONSTER_SCALE, Items.ITEM_FRAME, Items.COD)) {
					if (items.stream().noneMatch(e -> e.getItem().is(want))) b.append(want).append(' ');
				}
				return b.toString();
			});
			check(drops.isEmpty(), "a Sea Monster drops its scale, an item frame and raw cod: missing " + drops);
		}
		if (!problems.isEmpty()) throw new AssertionError("OreSpawn Sea test failed:\n - " + String.join("\n - ", problems));
		log("ALL PASSED");
	}
}
