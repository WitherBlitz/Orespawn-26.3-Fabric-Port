package orespawn.entity.weapons;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import orespawn.OreSpawn;

import java.util.Set;

/**
 * What OreSpawn's weapons need to know about creatures other content groups own, looked up by entity id so this group
 * doesn't depend on them.
 */
public final class WeaponHooks {
	/** MyUtils.isRoyalty: The King, The Queen, their heads, the royal children and Purple Power. */
	private static final Set<String> ROYALTY = Set.of("the_prince", "the_young_prince", "the_young_adult_prince", "the_princess", "the_king",
		"king_head", "the_queen", "queen_head", "purple_power");

	private WeaponHooks() {
	}

	/** Whether the entity is OreSpawn's creature {@code id} (its snake_case OreSpawn name). */
	public static boolean is(Entity e, String id) {
		Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
		return key.getNamespace().equals(OreSpawn.MOD_ID) && key.getPath().equals(id);
	}

	public static boolean isRoyalty(Entity e) {
		if (!(e instanceof LivingEntity)) return false;
		Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
		return key.getNamespace().equals(OreSpawn.MOD_ID) && ROYALTY.contains(key.getPath());
	}

	/**
	 * OreSpawn's "no PvP" rule for its giant weapons (BigBerthaPvp and UltimateSwordPvp, both off by default): they
	 * don't hurt players, the Girlfriend and Boyfriend, or tamed pets.
	 */
	public static boolean pvpProtected(Entity e) {
		if (e instanceof Player || is(e, "girlfriend") || is(e, "boyfriend")) return true;
		return e instanceof TamableAnimal t && t.isTame();
	}
}
