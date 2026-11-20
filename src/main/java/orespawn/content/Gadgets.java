package orespawn.content;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import orespawn.item.gadgets.NetherTrackerItem;
import orespawn.item.gadgets.StepItem;
import orespawn.registry.ModItems;

/**
 * OreSpawn's last few items: the instant builders (Insta-Bridge, Stairs going Up, Stairs going Down; stacks of 16),
 * the Nether Tracker, the Rose Sword and the Worm Tooth (a Large Worm drop, used for Big Bertha's blade).
 * <p>
 * The Rose Sword is OreSpawn's EmeraldSword class on the Emerald material ({@code toolEMERALD}: 1300 uses, efficiency
 * 10, damage 6, enchantability 75), made like the Weapons group's swords: 4 + 6 damage (+1 for the player's own), 26.3's
 * sword speed, no repair material (OreSpawn's custom materials had none).
 */
public final class Gadgets {
	public static final Item INSTA_BRIDGE = ModItems.register("insta_bridge", new StepItem(ModItems.props("insta_bridge").stacksTo(16), 0));
	public static final Item STAIRS_GOING_UP = ModItems.register("stairs_going_up",
		new StepItem(ModItems.props("stairs_going_up").stacksTo(16), 1));
	public static final Item STAIRS_GOING_DOWN = ModItems.register("stairs_going_down",
		new StepItem(ModItems.props("stairs_going_down").stacksTo(16), -1));
	public static final Item NETHER_TRACKER = ModItems.register("nether_tracker",
		new NetherTrackerItem(ModItems.props("nether_tracker").durability(3000)));
	public static final Item ROSE_SWORD = ModItems.register("rose_sword", new Item(ModItems.props("rose_sword").sword(
		new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1300, 10, 6 + 1, 75, ModItems.repairTag("weapons")), 3.0F, -2.4F)));
	public static final Item WORM_TOOTH = ModItems.item("worm_tooth", new Item.Properties());

	private Gadgets() {
	}

	public static void init() {
		// Static initialisers do the work.
	}
}
