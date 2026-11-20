package orespawn.entity.sea;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import orespawn.OreSpawn;
import orespawn.entity.Ignored;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * How the Sea group's hunters pick targets, as OreSpawn did (GenericTargetSorter, MyUtils). Creatures of other content
 * groups are recognised by their entity id ({@code orespawn:<OreSpawn name in snake_case>}), so this works before and
 * after those groups are ported, without depending on their classes.
 */
final class SeaTargets {
	/** MyUtils.isAttackableNonMob: OreSpawn's big creatures, royalty, pets and villagers count as fair game. */
	private static final Set<String> ATTACKABLE = Set.of("mothra", "leonopteryx", "dragon", "baby_dragon", "the_prince", "the_young_prince",
		"the_young_adult_prince", "the_princess", "the_king", "king_head", "the_queen", "queen_head", "purple_power", "wtf", "cephadrome",
		"water_dragon", "girlfriend", "boyfriend", "stinky");
	/** MyUtils.isIgnoreable beyond Ignored.byHunters: rocks, ants and termites, Mothra (a butterfly), bugs, ghosts, the hoverboard. */
	private static final Set<String> IGNORED = Set.of("rock", "ant", "red_ant", "rainbow_ant", "unstable_ant", "termite", "mothra", "mosquito",
		"dragonfly", "ghost", "ghost_pumpkin_skelly", "hoverboard");

	private SeaTargets() {
	}

	/** Whether the entity is OreSpawn's creature with one of these ids. */
	static boolean is(Entity e, String... ids) {
		Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
		if (!key.getNamespace().equals(OreSpawn.MOD_ID)) return false;
		for (String id : ids) {
			if (key.getPath().equals(id)) return true;
		}
		return false;
	}

	private static boolean in(Entity e, Set<String> ids) {
		Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
		return key.getNamespace().equals(OreSpawn.MOD_ID) && ids.contains(key.getPath());
	}

	/** MyUtils.isAttackableNonMob. */
	static boolean attackableNonMob(LivingEntity e) {
		return e instanceof Monster || e instanceof Villager || in(e, ATTACKABLE);
	}

	/** MyUtils.isIgnoreable. */
	static boolean ignoreable(LivingEntity e) {
		return Ignored.byHunters(e) || in(e, IGNORED);
	}

	static boolean creative(LivingEntity e) {
		return e instanceof Player p && (p.isCreative() || p.isSpectator());
	}

	/**
	 * OreSpawn's findSomethingToAttack: the living things in the mob's box grown by (xz, y, xz), nearest first (its
	 * GenericTargetSorter), and the first one {@code ok} accepts.
	 */
	static @Nullable LivingEntity nearest(Mob mob, double xz, double y, Predicate<LivingEntity> ok) {
		List<LivingEntity> list = mob.level().getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(xz, y, xz));
		list.sort(Comparator.comparingDouble(mob::distanceToSqr));
		for (LivingEntity e : list) {
			if (ok.test(e)) return e;
		}
		return null;
	}

	/** The checks every isSuitableTarget starts with: not itself, alive, and in sight. */
	static boolean basic(Mob mob, LivingEntity e) {
		return e != mob && e.isAlive() && mob.getSensing().hasLineOfSight(e);
	}
}
