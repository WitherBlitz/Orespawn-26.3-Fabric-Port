package orespawn.content;

/**
 * Every content group's registration, in order. Each group (a batch of creatures, a set of items, a dimension...) has
 * its own class here with a {@code static void init()} that registers its entity types, spawn eggs, items, blocks, egg
 * ores, attributes, spawn rules and natural spawns through ModEntities / ModItems / ModBlocks. Groups add one line here.
 */
public final class Content {
	private Content() {
	}

	public static void init() {
		Dinosaurs.init();
		Bugs.init();
		Spooky.init();
		Structures.init();
		Sea.init();
		Portals.init();
		Terrors.init();
		Islands.init();
		Farming.init();
		Oddities.init();
		Robots.init();
		Weapons.init();
		Dungeons.init();
		Mining.init();
		Crystal.init();
		Chaos.init();
		VillageMania.init();
		Utopia.init();
		Bosses.init();
		Gadgets.init();
		Leftovers.init();
		// Last: the critter cages are registered only for creatures that exist by now.
		Tools.init();
	}
}
