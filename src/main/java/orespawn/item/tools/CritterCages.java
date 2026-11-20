package orespawn.item.tools;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import orespawn.OreSpawn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Which creature each of OreSpawn's caged items holds ({@code EntityCage.onImpact} for catching, {@code CritterCage.onItemUse}
 * for letting it out), in OreSpawn's order.
 * <p>
 * One line per caged item: its id, the chance (in percent) that a hit creature breaks free and the cage falls empty,
 * how many caged items one catch gives (OreSpawn gave 2 bats, 2 silverfish, 2 dragonflies, 4 birds and 6 attack squid),
 * and the entity ids it catches. The first id is the one it lets out. A caged item is only registered when its
 * creature exists, so an OreSpawn creature ported later needs nothing here as long as its id matches the one below;
 * otherwise fix that one line. The item's texture and name are in {@code tools/content/tools.json}.
 * <p>
 * 1.7.10 had one class for several of today's mobs, and its cage caught them all: zombie villagers were zombies,
 * donkeys, mules and skeleton and zombie horses were horses, and cats were ocelots. Mobs added after 1.7.10 have no
 * OreSpawn cage; a cage that hits one is lost, as it was with any creature OreSpawn didn't list.
 */
public final class CritterCages {
	/** One caged item: what it catches (first: what it lets out), the escape chance and how many items a catch gives. */
	public record Cage(String item, int failPercent, int count, boolean untamedOnly, List<Identifier> entities) {
		public Identifier releases() {
			return entities.getFirst();
		}

		/** Girlfriends and Boyfriends can only be caged while nobody has tamed them. */
		public boolean accepts(Entity entity) {
			return !untamedOnly || !(entity instanceof TamableAnimal t && t.isTame());
		}
	}

	public static final List<Cage> ALL = List.of(
		vanilla("caged_spider", 0, 1, "spider"),
		vanilla("caged_bat", 0, 2, "bat"),
		vanilla("caged_cow", 0, 1, "cow"),
		vanilla("caged_pig", 0, 1, "pig"),
		vanilla("caged_squid", 0, 1, "squid"),
		vanilla("caged_chicken", 0, 1, "chicken"),
		vanilla("caged_creeper", 0, 1, "creeper"),
		vanilla("caged_skeleton", 0, 1, "skeleton"),
		vanilla("caged_zombie", 0, 1, "zombie", "zombie_villager"),
		vanilla("caged_slime", 0, 1, "slime"),
		vanilla("caged_ghast", 20, 1, "ghast"),
		vanilla("caged_zombiepigman", 0, 1, "zombified_piglin"),
		vanilla("caged_enderman", 20, 1, "enderman"),
		vanilla("caged_cave_spider", 0, 1, "cave_spider"),
		vanilla("caged_silverfish", 0, 2, "silverfish"),
		vanilla("caged_magma_cube", 0, 1, "magma_cube"),
		vanilla("caged_witch", 0, 1, "witch"),
		vanilla("caged_sheep", 0, 1, "sheep"),
		vanilla("caged_wolf", 0, 1, "wolf"),
		vanilla("caged_mooshroom", 0, 1, "mooshroom"),
		vanilla("caged_ocelot", 0, 1, "ocelot", "cat"),
		vanilla("caged_blaze", 0, 1, "blaze"),
		vanilla("caged_wither_skeleton", 0, 1, "wither_skeleton"),
		vanilla("caged_ender_dragon", 50, 1, "ender_dragon"),
		vanilla("caged_snow_golem", 0, 1, "snow_golem"),
		vanilla("caged_iron_golem", 0, 1, "iron_golem"),
		vanilla("caged_wither_boss", 20, 1, "wither"),
		creature("caged_apple_cow", 0, 1, false, "apple_cow"),
		creature("caged_golden_apple_cow", 0, 1, false, "golden_apple_cow"),
		creature("caged_enchanted_golden_apple_cow", 0, 1, false, "enchanted_golden_apple_cow"),
		creature("caged_mothra", 40, 1, false, "mothra"),
		creature("caged_alosaurus", 40, 1, false, "alosaurus"),
		creature("caged_cryosaurus", 0, 1, false, "cryolophosaurus"),
		creature("caged_camarasaurus", 0, 1, false, "camarasaurus"),
		creature("caged_velocity_raptor", 0, 1, false, "velocity_raptor"),
		creature("caged_hydrolisc", 0, 1, false, "hydrolisc"),
		creature("caged_basilisk", 60, 1, false, "basilisk"),
		creature("caged_dragonfly", 0, 2, false, "dragonfly"),
		creature("caged_emperor_scorpion", 70, 1, false, "emperor_scorpion"),
		creature("caged_scorpion", 0, 1, false, "scorpion"),
		creature("caged_cave_fisher", 0, 1, false, "cave_fisher"),
		creature("caged_baby_dragon", 0, 1, false, "baby_dragon"),
		creature("caged_baryonyx", 0, 1, false, "baryonyx"),
		creature("caged_wtf", 0, 1, false, "wtf"),
		creature("caged_bird", 0, 4, false, "bird"),
		creature("caged_kyuubi", 30, 1, false, "kyuubi"),
		creature("caged_alien", 50, 1, false, "alien"),
		creature("caged_attack_squid", 0, 6, false, "attack_squid"),
		creature("caged_water_dragon", 60, 1, false, "water_dragon"),
		creature("caged_cephadrome", 70, 1, false, "cephadrome"),
		creature("caged_kraken", 95, 1, false, "the_kraken"),
		creature("caged_lizard", 20, 1, false, "lizard"),
		creature("caged_dragon", 70, 1, false, "dragon"),
		creature("caged_bee", 30, 1, false, "bee"),
		vanilla("caged_horse", 0, 1, "horse", "donkey", "mule", "skeleton_horse", "zombie_horse"),
		creature("caged_firefly", 0, 1, false, "firefly"),
		creature("caged_chipmunk", 0, 1, false, "chipmunk"),
		creature("caged_gazelle", 0, 1, false, "gazelle"),
		creature("caged_ostrich", 0, 1, false, "ostrich"),
		creature("caged_jumpy_bug", 60, 1, false, "jumpy_bug"),
		creature("caged_spit_bug", 30, 1, false, "spit_bug"),
		creature("caged_stink_bug", 0, 1, false, "stink_bug"),
		creature("caged_creeping_horror", 0, 1, false, "creeping_horror"),
		creature("caged_terrible_terror", 0, 1, false, "terrible_terror"),
		creature("caged_cliff_racer", 0, 1, false, "cliff_racer"),
		creature("caged_triffid", 60, 1, false, "triffid"),
		creature("caged_nightmare", 70, 1, false, "nightmare"),
		creature("caged_lurking_terror", 0, 1, false, "lurking_terror"),
		creature("caged_small_worm", 0, 1, false, "small_worm"),
		creature("caged_medium_worm", 0, 1, false, "medium_worm"),
		creature("caged_large_worm", 50, 1, false, "large_worm"),
		creature("caged_cassowary", 0, 1, false, "cassowary"),
		creature("caged_cloud_shark", 0, 1, false, "cloud_shark"),
		creature("caged_gold_fish", 0, 1, false, "gold_fish"),
		creature("caged_leaf_monster", 0, 1, false, "leaf_monster"),
		creature("caged_ender_knight", 30, 1, false, "ender_knight"),
		creature("caged_ender_reaper", 20, 1, false, "ender_reaper"),
		creature("caged_beaver", 0, 1, false, "beaver"),
		creature("caged_crystal_urchin", 0, 1, false, "crystal_urchin"),
		creature("caged_flounder", 0, 1, false, "flounder"),
		creature("caged_skate", 0, 1, false, "skate"),
		creature("caged_rotator", 0, 1, false, "rotator"),
		creature("caged_peacock", 0, 1, false, "peacock"),
		creature("caged_fairy", 0, 1, false, "fairy"),
		creature("caged_dungeon_beast", 0, 1, false, "dungeon_beast"),
		creature("caged_vortex", 30, 1, false, "vortex"),
		creature("caged_rat", 0, 1, false, "rat"),
		creature("caged_whale", 20, 1, false, "whale"),
		creature("caged_irukandji", 0, 1, false, "irukandji"),
		creature("caged_t_rex", 40, 1, false, "t_rex"),
		creature("caged_hercules_beetle", 50, 1, false, "hercules_beetle"),
		creature("caged_mantis", 30, 1, false, "mantis"),
		creature("caged_stinky", 0, 1, false, "stinky"),
		creature("caged_easter_bunny", 0, 1, false, "easter_bunny"),
		creature("caged_caterkiller", 70, 1, false, "caterkiller"),
		creature("caged_molenoid", 50, 1, false, "molenoid"),
		creature("caged_sea_monster", 30, 1, false, "sea_monster"),
		creature("caged_sea_viper", 40, 1, false, "sea_viper"),
		creature("caged_leonopteryx", 70, 1, false, "leonopteryx"),
		creature("caged_hammerhead", 70, 1, false, "hammerhead"),
		creature("caged_rubber_ducky", 0, 1, false, "rubber_ducky"),
		creature("caged_crystal_cow", 0, 1, false, "crystal_apple_cow"),
		vanilla("caged_villager", 0, 1, "villager"),
		creature("caged_criminal", 0, 1, false, "criminal"),
		creature("caged_brutalfly", 50, 1, false, "brutalfly"),
		creature("caged_nastysaurus", 70, 1, false, "nastysaurus"),
		creature("caged_pointysaurus", 20, 1, false, "pointysaurus"),
		creature("caged_cricket", 0, 1, false, "cricket"),
		creature("caged_frog", 0, 1, false, "frog"),
		creature("caged_spider_driver", 0, 1, false, "spider_driver"),
		creature("caged_crab", 0, 1, false, "crab"));

	private static Map<EntityType<?>, Cage> byType;

	private CritterCages() {
	}

	private static Cage vanilla(String item, int failPercent, int count, String... ids) {
		return new Cage(item, failPercent, count, false, Arrays.stream(ids).map(Identifier::withDefaultNamespace).toList());
	}

	private static Cage creature(String item, int failPercent, int count, boolean untamedOnly, String id) {
		return new Cage(item, failPercent, count, untamedOnly, List.of(OreSpawn.id(id)));
	}

	/** Whether the creature this cage lets out exists (vanilla, or an OreSpawn creature already registered). */
	public static boolean exists(Cage cage) {
		return BuiltInRegistries.ENTITY_TYPE.containsKey(cage.releases());
	}

	/** The cage that catches this entity, or null if OreSpawn had none for it. */
	public static Cage forEntity(Entity entity) {
		if (byType == null) {
			Map<EntityType<?>, Cage> map = new HashMap<>();
			for (Cage cage : ALL) {
				for (Identifier id : cage.entities()) BuiltInRegistries.ENTITY_TYPE.getOptional(id).ifPresent(t -> map.putIfAbsent(t, cage));
			}
			byType = map;
		}
		return byType.get(entity.getType());
	}
}
