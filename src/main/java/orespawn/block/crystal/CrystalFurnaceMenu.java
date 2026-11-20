package orespawn.block.crystal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.level.Level;
import orespawn.content.Crystal;

/**
 * OreSpawn's ContainerCrystalFurnace: input (56, 17), fuel (56, 53) and output (116, 35) over the player's inventory,
 * and three synced numbers (cook time, burn time left, the fuel's burn time). Shift-clicking sends smeltable items to
 * the input and fuel to the fuel slot, as OreSpawn's did; the fuel slot takes anything put in by hand, as OreSpawn's did.
 */
public class CrystalFurnaceMenu extends AbstractContainerMenu {
	private final Container furnace;
	private final ContainerData data;
	private final Level level;

	/** The client's copy. */
	public CrystalFurnaceMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, new SimpleContainer(3), new SimpleContainerData(3));
	}

	public CrystalFurnaceMenu(int containerId, Inventory inventory, Container furnace, ContainerData data) {
		super(Crystal.CRYSTAL_FURNACE_MENU, containerId);
		checkContainerSize(furnace, 3);
		checkContainerDataCount(data, 3);
		this.furnace = furnace;
		this.data = data;
		this.level = inventory.player.level();
		this.addSlot(new Slot(furnace, 0, 56, 17));
		this.addSlot(new Slot(furnace, 1, 56, 53));
		this.addSlot(new ResultSlot(inventory.player, furnace, 2, 116, 35));
		this.addStandardInventorySlots(inventory, 8, 84);
		this.addDataSlots(data);
	}

	public int cookTime() {
		return this.data.get(0);
	}

	public int burnTime() {
		return this.data.get(1);
	}

	public int currentItemBurnTime() {
		return this.data.get(2);
	}

	@Override
	public boolean stillValid(Player player) {
		return this.furnace.stillValid(player);
	}

	private boolean canSmelt(ItemStack stack) {
		return this.level.recipeAccess().propertySet(RecipePropertySet.FURNACE_INPUT).test(stack);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack clicked = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			clicked = stack.copy();
			if (index == 2) {
				if (!this.moveItemStackTo(stack, 3, 39, true)) return ItemStack.EMPTY;
				slot.onQuickCraft(stack, clicked);
			} else if (index != 1 && index != 0) {
				if (this.canSmelt(stack)) {
					if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
				} else if (CrystalFurnaceBlockEntity.isFuel(stack)) {
					if (!this.moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
				} else if (index < 30) {
					if (!this.moveItemStackTo(stack, 30, 39, false)) return ItemStack.EMPTY;
				} else if (index < 39 && !this.moveItemStackTo(stack, 3, 30, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(stack, 3, 39, false)) {
				return ItemStack.EMPTY;
			}
			if (stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
			if (stack.getCount() == clicked.getCount()) return ItemStack.EMPTY;
			slot.onTake(player, stack);
		}
		return clicked;
	}

	/** SlotFurnace: take only, and taking gives the smelting experience. */
	private static final class ResultSlot extends Slot {
		private final Player player;
		private int removeCount;

		ResultSlot(Player player, Container container, int slot, int x, int y) {
			super(container, slot, x, y);
			this.player = player;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}

		@Override
		public ItemStack remove(int amount) {
			if (this.hasItem()) this.removeCount += Math.min(amount, this.getItem().getCount());
			return super.remove(amount);
		}

		@Override
		public void onTake(Player player, ItemStack carried) {
			this.checkTakeAchievements(carried);
			super.onTake(player, carried);
		}

		@Override
		protected void onQuickCraft(ItemStack picked, int count) {
			this.removeCount += count;
			this.checkTakeAchievements(picked);
		}

		@Override
		protected void checkTakeAchievements(ItemStack carried) {
			carried.onCraftedBy(this.player, this.removeCount);
			if (this.player instanceof ServerPlayer serverPlayer && this.container instanceof CrystalFurnaceBlockEntity furnace) {
				furnace.awardUsedRecipesAndPopExperience(serverPlayer);
			}
			this.removeCount = 0;
		}
	}
}
