package orespawn.block.crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.content.Crystal;

/**
 * OreSpawn's Crystal Workbench (CrystalWorkbench, ContainerCrystalWorkbench, CrystalWorkbenchGUI): a 3 x 3 crafting
 * table. OreSpawn's container and screen were copies of the vanilla crafting table's, so it opens {@link Menu} (the
 * crafting grid, kept open only while this workbench is there) in the vanilla crafting screen.
 */
public class CrystalWorkbenchBlock extends CraftingTableBlock {
	private static final Component TITLE = Component.translatable("container.crafting");

	public CrystalWorkbenchBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
		return new SimpleMenuProvider((id, inventory, player) -> new Menu(id, inventory, ContainerLevelAccess.create(level, pos)), TITLE);
	}

	@Override
	protected int getLightDampening(BlockState state) {
		return 0;
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}

	/** ContainerCrystalWorkbench: the crafting table's menu, checking for the Crystal Workbench. */
	public static class Menu extends CraftingMenu {
		private final ContainerLevelAccess access;

		public Menu(int containerId, Inventory inventory, ContainerLevelAccess access) {
			super(containerId, inventory, access);
			this.access = access;
		}

		@Override
		public boolean stillValid(Player player) {
			return stillValid(this.access, player, Crystal.CRYSTAL_WORKBENCH);
		}
	}
}
