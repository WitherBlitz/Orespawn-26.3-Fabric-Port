package orespawn.client.content;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.entity.Mob;
import orespawn.OreSpawn;
import orespawn.client.OreSpawnClient;
import orespawn.client.model.LegacyCowModel;
import orespawn.content.Crystal;

/**
 * The Crystal group's client setup: the Crystal Apple Cow (RenderEnchantedCow with ModelCow, shadow 0.7, OreSpawn's
 * crystal_cow.png, no glint) and the Crystal Furnace's screen. The Crystal Workbench uses the vanilla crafting screen.
 */
public final class CrystalClient {
	private CrystalClient() {
	}

	public static void init() {
		// The Apple Cows' layer, which OreSpawnClient registers before the content groups run.
		ModelLayerLocation cow = new ModelLayerLocation(OreSpawn.id("legacy_cow"), "main");
		OreSpawnClient.<Mob>mob(Crystal.CRYSTAL_APPLE_COW, cow, LegacyCowModel::new, 0.7F, 1.0F,
			(e, s) -> s.texture = OreSpawnClient.tex("crystal_cow.png"), null);
		MenuScreens.register(Crystal.CRYSTAL_FURNACE_MENU, CrystalFurnaceScreen::new);
	}
}
