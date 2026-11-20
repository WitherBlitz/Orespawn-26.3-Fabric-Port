package orespawn.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Tools;
import orespawn.entity.tools.Elevator;
import orespawn.entity.tools.RockBase;
import orespawn.entity.tools.ThrownCage;

import java.util.ArrayList;
import java.util.List;

/**
 * The Tools group: a thrown Critter Cage cages a pig and the caged pig comes back out (with the empty cage); a Zoo Cage
 * builds its glass cage; a rock put down is a Small Rock with 1 health that leaves its item when broken; a Hoverboard
 * floats above the ground and can be ridden.
 */
public class ToolsTest implements FabricClientGameTest {
	private final List<String> problems = new ArrayList<>();

	private static void log(String line) {
		System.out.println("OS-TOOLS " + line);
	}

	private void check(boolean ok, String what) {
		log((ok ? "ok   " : "FAIL ") + what);
		if (!ok) problems.add(what);
	}

	private static UseOnContext useOn(ServerPlayer p, Item item, BlockPos pos) {
		p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
		return new UseOnContext(p, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false));
	}

	private static boolean itemNear(ServerLevel level, Item item, double x, double y, double z) {
		return level.getEntitiesOfClass(ItemEntity.class, new AABB(x, y, z, x, y, z).inflate(3.0)).stream().anyMatch(e -> e.getItem().is(item));
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			TestServerContext server = sp.getServer();
			for (String cmd : List.of("gamerule spawn_mobs false", "time set noon", "weather clear", "difficulty easy", "gamerule advance_time false",
				"forceload add 0 0 64 64", "fill 16 99 16 60 99 60 minecraft:stone", "fill 16 100 16 60 112 60 minecraft:air",
				"tp @p 30.5 100 20.5")) {
				server.runCommand(cmd);
			}
			context.waitTicks(5);

			// A Critter Cage misses 2 times in 10, so throw up to 12.
			server.computeOnServer(s -> {
				Pig pig = EntityTypes.PIG.spawn(s.overworld(), new BlockPos(30, 100, 30), EntitySpawnReason.COMMAND);
				pig.setNoAi(true);
				return pig;
			});
			boolean caught = false;
			for (int i = 0; i < 12 && !caught; i++) {
				server.computeOnServer(s -> {
					ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
					ThrownCage cage = new ThrownCage(s.overworld(), p, new ItemStack(Tools.EMPTY_CRITTER_CAGE));
					cage.setPos(30.5, 103.0, 30.5);
					cage.setDeltaMovement(0.0, -0.5, 0.0);
					return s.overworld().addFreshEntity(cage);
				});
				context.waitTicks(15);
				caught = server.computeOnServer(s -> itemNear(s.overworld(), Tools.CAGED.get("caged_pig"), 30.5, 100.0, 30.5));
			}
			check(caught, "a thrown Critter Cage cages a pig");
			boolean pigGone = server.computeOnServer(s -> s.overworld().getEntitiesOfClass(Pig.class, new AABB(new BlockPos(30, 100, 30)).inflate(4.0)).isEmpty());
			check(pigGone, "the caged pig is gone from the world");
			server.runCommand("kill @e[type=item]");

			String released = server.computeOnServer(s -> {
				ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
				Tools.CAGED.get("caged_pig").useOn(useOn(p, Tools.CAGED.get("caged_pig"), new BlockPos(40, 99, 30)));
				boolean pig = !s.overworld().getEntitiesOfClass(Pig.class, new AABB(new BlockPos(40, 100, 30)).inflate(2.0)).isEmpty();
				boolean cage = itemNear(s.overworld(), Tools.EMPTY_CRITTER_CAGE, 40.5, 100.0, 30.5);
				return (pig ? "" : "no pig; ") + (cage ? "" : "no empty cage");
			});
			check(released.isEmpty(), "a caged pig used on a block lets the pig out and drops the empty cage: " + released);

			String zoo = server.computeOnServer(s -> {
				ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
				p.teleportTo(50.5, 100.0, 50.5);
				Tools.EXTRA_SMALL_ZOO_CAGE.useOn(useOn(p, Tools.EXTRA_SMALL_ZOO_CAGE, new BlockPos(51, 99, 50)));
				ServerLevel l = s.overworld();
				StringBuilder b = new StringBuilder();
				if (!l.getBlockState(new BlockPos(52, 100, 50)).is(Blocks.GLASS)) b.append("no glass wall; ");
				if (!l.getBlockState(new BlockPos(50, 99, 50)).is(Blocks.QUARTZ_BLOCK)) b.append("no quartz floor; ");
				if (!l.getBlockState(new BlockPos(50, 102, 50)).is(Blocks.QUARTZ_BLOCK)) b.append("no quartz roof; ");
				if (!l.getBlockState(new BlockPos(50, 100, 50)).isAir()) b.append("not hollow; ");
				return b.toString();
			});
			check(zoo.isEmpty(), "an Extra Small Zoo Cage builds a 5x5 glass cage with a quartz floor and roof: " + zoo);

			String rock = server.computeOnServer(s -> {
				ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
				p.teleportTo(30.5, 100.0, 40.5);
				Tools.ROCKS.getFirst().useOn(useOn(p, Tools.ROCKS.getFirst(), new BlockPos(30, 99, 44)));
				List<RockBase> rocks = s.overworld().getEntitiesOfClass(RockBase.class, new AABB(new BlockPos(30, 100, 44)).inflate(2.0));
				if (rocks.isEmpty()) return "no rock put down";
				RockBase r = rocks.getFirst();
				if (r.rockType() != 1 || r.getMaxHealth() != 1.0F) return "type " + r.rockType() + ", health " + r.getMaxHealth();
				r.hurtServer(s.overworld(), s.overworld().damageSources().playerAttack(p), 5.0F);
				return itemNear(s.overworld(), Tools.ROCKS.getFirst(), 30.5, 100.0, 44.5) ? "" : "no Small Rock dropped";
			});
			check(rock.isEmpty(), "a Small Rock put down has 1 health and leaves its rock when broken: " + rock);

			server.computeOnServer(s -> {
				ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
				p.teleportTo(40.5, 100.0, 40.5);
				return Tools.HOVERBOARD.useOn(useOn(p, Tools.HOVERBOARD, new BlockPos(44, 99, 44)));
			});
			context.waitTicks(60);
			String board = server.computeOnServer(s -> {
				List<Elevator> boards = s.overworld().getEntitiesOfClass(Elevator.class, new AABB(new BlockPos(44, 100, 44)).inflate(3.0));
				if (boards.isEmpty()) return "no hoverboard";
				Elevator e = boards.getFirst();
				if (e.getY() < 100.3 || e.getY() > 102.5) return "it sits at y " + e.getY();
				ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
				p.startRiding(e);
				return p.getVehicle() == e ? "" : "can't ride it";
			});
			check(board.isEmpty(), "a Hoverboard floats above the ground and can be ridden: " + board);
		}
		if (!problems.isEmpty()) throw new AssertionError("OreSpawn Tools test failed:\n - " + String.join("\n - ", problems));
		log("ALL PASSED");
	}
}
