package orespawn.client.content;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import orespawn.block.crystal.CrystalFurnaceBlockEntity;
import orespawn.block.crystal.CrystalFurnaceMenu;

/**
 * OreSpawn's CrystalFurnaceGUI: the vanilla furnace screen (title centred, no recipe book), its flame shrinking as the
 * fuel burns down and its arrow filling over the Crystal Furnace's 150 ticks, with OreSpawn's arithmetic.
 */
public class CrystalFurnaceScreen extends AbstractContainerScreen<CrystalFurnaceMenu> {
	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
	private static final Identifier LIT_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/lit_progress");
	private static final Identifier BURN_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/burn_progress");

	public CrystalFurnaceScreen(CrystalFurnaceMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		int xo = this.leftPos;
		int yo = this.topPos;
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
		if (this.menu.burnTime() > 0) {
			int total = this.menu.currentItemBurnTime() == 0 ? CrystalFurnaceBlockEntity.COOK_TIME : this.menu.currentItemBurnTime();
			int lit = Math.min(12, this.menu.burnTime() * 12 / total);
			int height = lit + 2;
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LIT_PROGRESS_SPRITE, 14, 14, 0, 14 - height, xo + 56, yo + 36 + 14 - height, 14, height);
		}
		int width = Math.min(24, this.menu.cookTime() * 24 / CrystalFurnaceBlockEntity.COOK_TIME + 1);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BURN_PROGRESS_SPRITE, 24, 16, 0, 0, xo + 79, yo + 34, width, 16);
	}
}
