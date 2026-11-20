package orespawn.entity.spooky;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import orespawn.OreSpawn;
import orespawn.entity.Ignored;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Predicate;

/** Small things the Spooky group's mobs share. */
final class SpookyHelpers {
	private SpookyHelpers() {
	}

	/** OreSpawn's MyUtils.isIgnoreable: Ignored's list plus this group's ghosts, which were on it too. */
	static boolean ignorable(LivingEntity e) {
		return Ignored.byHunters(e) || e instanceof Ghost;
	}

	/**
	 * True if e is one of OreSpawn's creatures with one of these ids. Used for the mobs a hunter leaves alone, so it works
	 * before those mobs are ported.
	 */
	static boolean isOreSpawn(LivingEntity e, Set<String> ids) {
		Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
		return id.getNamespace().equals(OreSpawn.MOD_ID) && ids.contains(id.getPath());
	}

	/** A creative or spectator player, which OreSpawn's hunters skip. */
	static boolean untouchable(LivingEntity e) {
		return e instanceof Player p && (p.isCreative() || p.isSpectator());
	}

	/**
	 * OreSpawn's findSomethingToAttack: everything living in the box, nearest first (GenericTargetSorter), and the first
	 * that passes the test.
	 */
	static @Nullable LivingEntity closest(Mob mob, AABB box, Predicate<LivingEntity> suitable) {
		return mob.level().getEntitiesOfClass(LivingEntity.class, box).stream().sorted(Comparator.comparingDouble(mob::distanceToSqr))
			.filter(suitable).findFirst().orElse(null);
	}

	/** 1.7.10's looting level for drops: only a player's weapon counts. */
	static int looting(ServerLevel level, DamageSource source) {
		if (!(source.getEntity() instanceof Player player)) return 0;
		return EnchantmentHelper.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING),
			player);
	}

	/** 1.7.10's default dropFewItems count: 0-2, plus 0 to looting more. */
	static int fewItems(RandomSource random, int looting) {
		int n = random.nextInt(3);
		if (looting > 0) n += random.nextInt(looting + 1);
		return n;
	}
}
