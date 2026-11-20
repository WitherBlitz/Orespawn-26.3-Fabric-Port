package orespawn.world.utopia;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

/**
 * Runtime lookups for blocks, items and creatures other content groups own (looked up by id when Utopia generates, so
 * this group never refers to their classes). Ids without a namespace are {@code orespawn:}; several ids may be given
 * when the owning group's choice isn't known yet, the first registered one wins.
 */
public final class Lookup {
	private Lookup() {
	}

	private static Identifier id(String id) {
		return id.indexOf(':') >= 0 ? Identifier.parse(id) : Identifier.fromNamespaceAndPath("orespawn", id);
	}

	/** The first registered block, or null. */
	public static @Nullable Block block(String... ids) {
		for (String s : ids) {
			Identifier id = id(s);
			if (BuiltInRegistries.BLOCK.containsKey(id)) {
				return BuiltInRegistries.BLOCK.getValue(id);
			}
		}
		return null;
	}

	/** The first registered block's default state, else the fallback (which may be null: then nothing is placed). */
	public static @Nullable BlockState state(@Nullable Block fallback, String... ids) {
		Block block = block(ids);
		if (block != null) {
			return block.defaultBlockState();
		}
		return fallback == null ? null : fallback.defaultBlockState();
	}

	/**
	 * One of OreSpawn's four-block crops (corn, tomato and lettuce were {@code <name>Plant1}-{@code 4}): the Farming
	 * group's single block with its {@code stage} property (0-3) set to {@code plant - 1}. Null (nothing placed) if it
	 * isn't registered.
	 */
	public static @Nullable BlockState stage(String id, int plant) {
		Block block = block(id);
		if (block == null) {
			return null;
		}
		BlockState state = block.defaultBlockState();
		for (Property<?> property : state.getProperties()) {
			if (property instanceof IntegerProperty ip && ip.getName().equals("stage")) {
				java.util.List<Integer> values = ip.getPossibleValues();
				return state.setValue(ip, values.get(Math.max(0, Math.min(values.size() - 1, plant - 1))));
			}
		}
		return state;
	}

	/** The first registered item, or air. */
	public static Item item(String... ids) {
		for (String s : ids) {
			Identifier id = id(s);
			if (BuiltInRegistries.ITEM.containsKey(id)) {
				return BuiltInRegistries.ITEM.getValue(id);
			}
		}
		return Items.AIR;
	}

	/** The first registered entity type, or null. */
	public static @Nullable EntityType<?> entity(String... ids) {
		for (String s : ids) {
			Identifier id = id(s);
			if (BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
				return BuiltInRegistries.ENTITY_TYPE.getValue(id);
			}
		}
		return null;
	}
}
