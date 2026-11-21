package orespawn.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import orespawn.client.content.HeldWeaponRenderer;
import orespawn.client.model.ChainsawModel;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * OreSpawn's 3D weapons in the hand: each one's four hand views (first and third person, either hand) use its 3D model
 * (HeldWeaponRenderer, OreSpawn's own model read from the original file), not the flat icon, also with every other
 * mod of the pack loaded; the Chainsaw's chain runs; and a first- and a third-person screenshot of each.
 */
public class HeldWeaponsTest implements FabricClientGameTest {
	private static final List<String> WEAPONS = List.of("big_bertha", "slice", "royal_guardian_sword", "attitude_adjuster", "battle_axe",
		"queen_scale_battle_axe", "chainsaw", "squidzooka");
	private static final List<ItemDisplayContext> HANDS = List.of(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
		ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, ItemDisplayContext.THIRD_PERSON_LEFT_HAND);

	private final List<String> problems = new ArrayList<>();
	private final boolean allMods = System.getenv("ORESPAWN_ALLMODS") != null;
	private ClientGameTestContext context;
	private int shots;

	private static void log(String line) {
		System.out.println("OS-HELD " + line);
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
			for (String cmd : List.of("gamerule spawn_mobs false", "time set noon", "weather clear", "gamerule advance_time false", "gamemode creative @p")) {
				server.runCommand(cmd);
			}
			int g = server.computeOnServer(s -> s.overworld().getHeight(Heightmap.Types.MOTION_BLOCKING, 0, 0));
			server.runCommand(String.format(Locale.ROOT, "tp @p 0.5 %d 0.5 0 0", g));
			context.runOnClient(mc -> {
				if (mc.gui.hud.isHidden()) mc.gui.hud.toggle(); // hiding the HUD also hides the hand
			});
			for (String w : WEAPONS) {
				server.runCommand("item replace entity @p weapon.mainhand with orespawn:" + w);
				context.waitTicks(5);
				String flat = context.computeOnClient(mc -> {
					ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("orespawn", w)));
					List<String> out = new ArrayList<>();
					for (ItemDisplayContext hand : HANDS) {
						ItemStackRenderState state = new ItemStackRenderState();
						mc.getItemModelResolver().updateForTopItem(state, stack, hand, mc.level, mc.player, 0);
						if (!usesHeldRenderer(state)) out.add(hand.getSerializedName());
					}
					return String.join(" ", out);
				});
				check(flat.isEmpty(), w + " uses its 3D model in every hand view: flat in [" + flat + "]");
				context.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
				shot(sp, "held_" + w + "_first_person");
				context.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
				shot(sp, "held_" + w + "_third_person");
			}
			context.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
			// The Chainsaw's chain runs along the blade (OreSpawn's own animation).
			String chain = context.computeOnClient(mc -> {
				ChainsawModel model = new ChainsawModel(ChainsawModel.createBodyLayer().bakeRoot());
				model.setupAnim(0.0F);
				ModelPart tooth = model.root().getChild("tooth0");
				float z0 = tooth.z;
				model.setupAnim(0.25F);
				return z0 + " -> " + tooth.z;
			});
			String[] z = chain.split(" -> ");
			check(!z[0].equals(z[1]), "the Chainsaw's chain moves: a tooth goes " + chain + " in a quarter second");
		}
		if (!problems.isEmpty()) throw new AssertionError("OreSpawn held weapons test failed:\n - " + String.join("\n - ", problems));
		log("ALL PASSED");
	}

	/** Whether one of the render state's layers draws with HeldWeaponRenderer. */
	private static boolean usesHeldRenderer(ItemStackRenderState state) {
		try {
			Field layersField = ItemStackRenderState.class.getDeclaredField("layers");
			Field countField = ItemStackRenderState.class.getDeclaredField("activeLayerCount");
			layersField.setAccessible(true);
			countField.setAccessible(true);
			Object layers = layersField.get(state);
			int count = countField.getInt(state);
			for (int i = 0; i < count; i++) {
				Object layer = Array.get(layers, i);
				Field special = layer.getClass().getDeclaredField("specialRenderer");
				special.setAccessible(true);
				if (special.get(layer) instanceof HeldWeaponRenderer) return true;
			}
			return false;
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private void shot(TestSingleplayerContext sp, String name) {
		context.waitTicks(10);
		sp.getConnection().waitForChunksRender(true, 2400);
		context.waitTicks(10);
		Path path = context.takeScreenshot(String.format(Locale.ROOT, "%02d_%s", ++shots, name));
		log("screenshot " + path);
		try {
			Path root = Path.of(System.getProperty("user.dir")).toAbsolutePath();
			while (root != null && !Files.exists(root.resolve("build.gradle"))) root = root.getParent();
			Path dir = root.resolve("screenshots").resolve(allMods ? "allmods" : "plain");
			Files.createDirectories(dir);
			Files.copy(path, dir.resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			log("couldn't copy " + path + ": " + e);
		}
	}
}
