package orespawn.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Portals;
import orespawn.entity.ants.Ant;
import orespawn.entity.ants.RedAnt;
import orespawn.entity.ants.Termite;
import orespawn.world.ModWorldgen;
import orespawn.world.OreSpawnDimensions;
import orespawn.world.OreSpawnTeleporter;

import java.util.ArrayList;
import java.util.List;

/**
 * The Portals group: the ants' health; a nest lets out 2-7 ants on a random tick (none with a block on top); breaking a
 * Red Ant Troll Block lets out 15-20 red ants; the teleporter's arrival spot is two air blocks over ground; a black ant
 * takes an empty-handed player to Utopia and back (or, while Utopia isn't in the game, leaves them where they are); a
 * termite won't take a player carrying things; the Random Teleport Block moves a player at least 8 blocks; the
 * group's worldgen data is loaded.
 */
public class PortalsTest implements FabricClientGameTest {
	private final List<String> problems = new ArrayList<>();

	private static void log(String line) {
		System.out.println("OS-PORTALS " + line);
	}

	private void check(boolean ok, String what) {
		log((ok ? "ok   " : "FAIL ") + what);
		if (!ok) problems.add(what);
	}

	private static ServerPlayer player(MinecraftServer s) {
		return s.getPlayerList().getPlayers().getFirst();
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			TestServerContext server = sp.getServer();
			for (String cmd : List.of("gamerule spawn_mobs false", "time set noon", "weather clear", "difficulty easy", "gamerule advance_time false",
				"forceload add -32 -32 96 96")) {
				server.runCommand(cmd);
			}
			int g = server.computeOnServer(s -> s.overworld().getHeight(Heightmap.Types.MOTION_BLOCKING, 0, 0));

			String health = server.computeOnServer(s -> {
				StringBuilder b = new StringBuilder();
				for (EntityType<? extends Mob> t : List.of(Portals.ANT, Portals.RED_ANT, Portals.RAINBOW_ANT, Portals.UNSTABLE_ANT, Portals.TERMITE)) {
					Mob m = t.create(s.overworld(), EntitySpawnReason.COMMAND);
					b.append(m == null ? "none" : String.valueOf((int) m.getMaxHealth())).append(' ');
				}
				return b.toString().trim();
			});
			check(health.equals("1 2 1 1 5"), "ants have OreSpawn's health (black, red, rainbow, unstable, termite): " + health);

			int[] nest = server.computeOnServer(s -> {
				ServerLevel level = s.overworld();
				BlockPos pos = new BlockPos(10, g + 5, 10);
				AABB around = new AABB(pos).inflate(3);
				level.setBlockAndUpdate(pos, Portals.ANT_NEST.defaultBlockState());
				level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
				level.getBlockState(pos).randomTick(level, pos, level.getRandom());
				int open = level.getEntitiesOfClass(Ant.class, around).size();
				level.getEntitiesOfClass(Ant.class, around).forEach(Ant::discard);
				level.setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
				level.getBlockState(pos).randomTick(level, pos, level.getRandom());
				return new int[] {open, level.getEntitiesOfClass(Ant.class, around).size()};
			});
			check(nest[0] >= 2 && nest[0] <= 7 && nest[1] == 0, "an ant nest lets out 2-7 ants, none with a block on it: " + nest[0] + ", " + nest[1]);

			int trolls = server.computeOnServer(s -> {
				ServerLevel level = s.overworld();
				BlockPos pos = new BlockPos(20, g + 3, 20);
				level.setBlockAndUpdate(pos, Portals.RED_ANT_TROLL_BLOCK.defaultBlockState());
				ServerPlayer p = player(s);
				p.teleportTo(20.5, g + 4, 22.5);
				p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
				p.gameMode.destroyBlock(pos);
				return level.getEntitiesOfClass(RedAnt.class, new AABB(pos).inflate(3)).size();
			});
			check(trolls >= 15 && trolls <= 20, "breaking a Red Ant Troll Block lets out 15-20 red ants: " + trolls);
			server.runCommand("kill @e[type=!player]");

			String spot = server.computeOnServer(s -> {
				ServerLevel level = s.overworld();
				Vec3 a = OreSpawnTeleporter.arrival(level, 200, 200);
				BlockPos at = BlockPos.containing(a);
				boolean ok = level.getBlockState(at).isAir() && level.getBlockState(at.above()).isAir() && !level.getBlockState(at.below()).isAir();
				return (ok ? "" : "bad ") + at.toShortString() + " on " + level.getBlockState(at.below());
			});
			check(!spot.startsWith("bad"), "the teleporter's arrival spot is two air blocks over ground: " + spot);

			boolean utopia = server.computeOnServer(s -> s.getLevel(OreSpawnDimensions.UTOPIA) != null);
			boolean went = server.computeOnServer(s -> {
				ServerPlayer p = player(s);
				p.teleportTo(30.5, g + 1, 30.5);
				p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
				Ant ant = Portals.ANT.spawn(s.overworld(), new BlockPos(31, g + 1, 30), EntitySpawnReason.COMMAND);
				if (ant != null) ant.interact(p, InteractionHand.MAIN_HAND, ant.position());
				return p.level().dimension() == OreSpawnDimensions.UTOPIA;
			});
			context.waitTicks(20);
			if (utopia) {
				check(went, "right-clicking a black ant with an empty hand takes you to Utopia");
				boolean back = server.computeOnServer(s -> {
					ServerPlayer p = player(s);
					OreSpawnTeleporter.travel(p, OreSpawnDimensions.UTOPIA);
					return p.level().dimension() == Level.OVERWORLD;
				});
				context.waitTicks(20);
				check(back, "travel() from Utopia takes you back to the overworld");
			} else {
				check(!went, "with no Utopia registered yet, a black ant leaves you where you are");
			}

			boolean stayed = server.computeOnServer(s -> {
				ServerPlayer p = player(s);
				p.teleportTo(s.overworld(), 30.5, g + 1, 30.5, java.util.Set.of(), 0.0F, 0.0F, true);
				p.getInventory().setItem(5, new ItemStack(Items.DIRT));
				p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
				Termite termite = Portals.TERMITE.spawn(s.overworld(), new BlockPos(31, g + 1, 30), EntitySpawnReason.COMMAND);
				if (termite != null) termite.interact(p, InteractionHand.MAIN_HAND, termite.position());
				boolean here = p.level().dimension() == Level.OVERWORLD;
				p.getInventory().clearContent();
				return here;
			});
			check(stayed, "a termite won't take a player who's carrying something");
			server.runCommand("kill @e[type=!player]");

			double moved = server.computeOnServer(s -> {
				ServerLevel level = s.overworld();
				BlockPos center = new BlockPos(60, g + 10, 60);
				for (int dx = -24; dx <= 24; dx++) {
					for (int dz = -24; dz <= 24; dz++) {
						level.setBlockAndUpdate(center.offset(dx, 0, dz), Blocks.STONE.defaultBlockState());
						level.setBlockAndUpdate(center.offset(dx, 1, dz), Blocks.AIR.defaultBlockState());
						level.setBlockAndUpdate(center.offset(dx, 2, dz), Blocks.AIR.defaultBlockState());
					}
				}
				level.setBlockAndUpdate(center, Portals.RANDOM_TELEPORT_BLOCK.defaultBlockState());
				ServerPlayer p = player(s);
				p.teleportTo(60.5, g + 11, 60.5);
				Vec3 before = p.position();
				Portals.RANDOM_TELEPORT_BLOCK.stepOn(level, center, level.getBlockState(center), p);
				return p.position().subtract(before).horizontalDistance();
			});
			check(moved >= 8.0, "the Random Teleport Block moves a player at least 8 blocks: " + moved);

			List<String> missing = server.computeOnServer(s -> {
				List<String> out = new ArrayList<>();
				var placed = s.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
				for (String name : List.of("ant_nests", "nether_ant_nests", "red_ant_troll_block", "termite_troll_block")) {
					if (placed.get(ModWorldgen.key(name)).isEmpty()) out.add(name);
				}
				return out;
			});
			check(missing.isEmpty(), "the nests' and troll blocks' placed features are loaded: missing " + missing);
		}
		if (!problems.isEmpty()) throw new AssertionError("OreSpawn Portals test failed:\n - " + String.join("\n - ", problems));
		log("ALL PASSED");
	}
}
