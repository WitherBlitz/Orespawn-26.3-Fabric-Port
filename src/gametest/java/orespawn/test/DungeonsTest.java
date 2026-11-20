package orespawn.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Dungeons;
import orespawn.world.dungeons.OreSpawnDungeons;

import java.util.ArrayList;
import java.util.List;

/**
 * The Dungeons group: every item OreSpawn's dungeon code (run from the original file) puts in chests is registered;
 * OreSpawn's mob dungeon builds with its spawner, filled chest and mossy floor; the Ender castle builds whole
 * outside world generation; the Random Dungeon item plants its spawner, which builds a dungeon and is gone.
 */
public class DungeonsTest implements FabricClientGameTest {
	private final List<String> problems = new ArrayList<>();

	private static void log(String line) {
		System.out.println("OS-DUNGEONS " + line);
	}

	private void check(boolean ok, String what) {
		log((ok ? "ok   " : "FAIL ") + what);
		if (!ok) problems.add(what);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			TestServerContext server = sp.getServer();
			for (String cmd : List.of("gamerule spawn_mobs false", "time set noon", "weather clear", "forceload add 0 0 160 160")) {
				server.runCommand(cmd);
			}

			// Every OreSpawn item the original dungeon code names (orespawn_fields.tsv) is registered, but the Girlfriend's and
			// Boyfriend's, which the port leaves out; a missing one would quietly drop out of its chests.
			String missing = server.computeOnServer(s -> {
				StringBuilder b = new StringBuilder();
				try (var in = DungeonsTest.class.getResourceAsStream("/orespawn/original/orespawn_fields.tsv")) {
					for (String line : new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).lines().toList()) {
						String[] f = line.split("\t");
						if (line.startsWith("#") || f.length < 3 || !f[1].equals("item") || f[2].contains("girlfriend") || f[2].contains("boyfriend")) continue;
						if (!net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(Identifier.parse(f[2]))) b.append(f[2]).append(' ');
					}
				} catch (java.io.IOException e) {
					throw new java.io.UncheckedIOException(e);
				}
				return b.toString();
			});
			check(missing.isEmpty(), "every item the original dungeon code puts in chests is registered: missing " + missing);

			String generic = server.computeOnServer(s -> {
				ServerLevel level = s.overworld();
				BlockPos o = new BlockPos(16, 100, 16);
				OreSpawnDungeons.genericDungeon(level, o, RandomSource.create(1));
				if (!level.getBlockState(o).is(Blocks.MOSSY_COBBLESTONE)) return "no mossy floor";
				if (!(level.getBlockEntity(o.offset(6, 1, 6)) instanceof SpawnerBlockEntity)) return "no spawner in the middle";
				if (!(level.getBlockEntity(o.offset(6, 1, 1)) instanceof ChestBlockEntity chest)) return "no chest";
				return chest.isEmpty() ? "the chest is empty" : "";
			});
			check(generic.isEmpty(), "the mob dungeon has its floor, spawner and a filled chest: " + generic);

			String castle = server.computeOnServer(s -> {
				ServerLevel level = s.overworld();
				BlockPos o = new BlockPos(64, 120, 64);
				OreSpawnDungeons.enderCastle(level, o, RandomSource.create(2));
				int blocks = 0;
				for (BlockPos p : BlockPos.betweenClosed(o.offset(-4, 0, -4), o.offset(26, 16, 26))) {
					if (!level.getBlockState(p).isAir()) blocks++;
				}
				boolean farCorner = false;
				for (BlockPos p : BlockPos.betweenClosed(o.offset(20, 0, 20), o.offset(26, 16, 26))) {
					farCorner |= !level.getBlockState(p).isAir();
				}
				// Measured: the whole castle is about 4300 blocks (it varies with the spawner blocks it rolls); far corner = not clipped.
				return blocks < 4000 ? "only " + blocks + " blocks" : farCorner ? "" : "nothing at the far corner";
			});
			check(castle.isEmpty(), "the Ender castle builds whole: " + castle);

			String item = server.computeOnServer(s -> {
				ServerLevel level = s.overworld();
				ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
				BlockPos ground = new BlockPos(130, 90, 130);
				level.setBlockAndUpdate(ground, Blocks.STONE.defaultBlockState());
				level.setBlockAndUpdate(ground.above(), Blocks.AIR.defaultBlockState());
				p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Dungeons.RANDOM_DUNGEON));
				Dungeons.RANDOM_DUNGEON.useOn(new UseOnContext(p, InteractionHand.MAIN_HAND,
					new BlockHitResult(Vec3.atCenterOf(ground), Direction.UP, ground, false)));
				var planted = level.getBlockState(ground.above());
				if (!planted.is(Dungeons.RANDOM_DUNGEON_SPAWNER)) return "no spawner planted";
				planted.randomTick(level, ground.above(), level.getRandom());
				return level.getBlockState(ground.above()).is(Dungeons.RANDOM_DUNGEON_SPAWNER) ? "the spawner didn't build" : "";
			});
			check(item.isEmpty(), "the Random Dungeon plants a spawner that builds a dungeon: " + item);
		}
		if (!problems.isEmpty()) throw new AssertionError("OreSpawn Dungeons test failed:\n - " + String.join("\n - ", problems));
		log("ALL PASSED");
	}
}
