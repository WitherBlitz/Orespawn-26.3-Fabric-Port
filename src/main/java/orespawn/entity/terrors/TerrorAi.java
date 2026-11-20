package orespawn.entity.terrors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.OreSpawn;
import orespawn.registry.ModItems;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * What the Terrors group's creatures share, as OreSpawn wrote it in each of them: picking a target (its
 * GenericTargetSorter: nearest first, creepers counted at half the distance and big things nearer by their size), the
 * bat-style flight target, its knock-back, its "dropItemRand" scattered drops and plain one-of-three drops, and its
 * scan_it block search.
 */
public final class TerrorAi {
	private TerrorAi() {
	}

	/** GenericTargetSorter's distance: creepers count half as far, and anything bigger than 1 x 1 nearer by its size. */
	static double sortDistance(Entity from, Entity e) {
		double d = from.distanceToSqr(e);
		if (e instanceof Creeper) d /= 2.0;
		double weight = e.getBbHeight() * e.getBbWidth();
		if (weight > 1.0) d /= weight;
		return d;
	}

	/**
	 * findSomethingToAttack: the first suitable living thing in the box around the mob, in GenericTargetSorter's order
	 * (armor stands, which 1.7.10 didn't have, are never chosen).
	 */
	static @Nullable LivingEntity findTarget(Mob mob, double xz, double y, Predicate<LivingEntity> suitable) {
		List<LivingEntity> list = mob.level().getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(xz, y, xz));
		list.sort(Comparator.comparingDouble(e -> sortDistance(mob, e)));
		for (LivingEntity e : list) {
			if (e != mob && e.isAlive() && !(e instanceof ArmorStand) && suitable.test(e)) return e;
		}
		return null;
	}

	/** OreSpawn never went after players in creative mode (nor, now, spectators). */
	static boolean protectedPlayer(LivingEntity e) {
		return e instanceof Player p && (p.isCreative() || p.isSpectator());
	}

	/**
	 * Whether {@code e} is one of OreSpawn's own creatures with one of these ids (for creatures other groups port; it
	 * simply never matches until they exist).
	 */
	static boolean isOreSpawn(Entity e, Set<String> ids) {
		Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
		return id.getNamespace().equals(OreSpawn.MOD_ID) && ids.contains(id.getPath());
	}

	/** OreSpawn's canSeeTarget: nothing solid on the line from 0.75 above the mob to the block's corner. */
	static boolean canSee(Mob mob, BlockPos p) {
		Vec3 from = new Vec3(mob.getX(), mob.getY() + 0.75, mob.getZ());
		return mob.level().clip(new ClipContext(from, Vec3.atLowerCornerOf(p), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob)).getType()
			== HitResult.Type.MISS;
	}

	/**
	 * The flyers' new flight target: up to 50 tries of a spot {@code minXZ} to {@code minXZ + spreadXZ - 1} blocks away on
	 * each axis (either way) and {@code nextInt(spreadY) - offsetY} up or down, until one is air the mob can see.
	 */
	static BlockPos newFlightTarget(Mob mob, int minXZ, int spreadXZ, int spreadY, int offsetY) {
		RandomSource r = mob.getRandom();
		BlockPos target = mob.blockPosition();
		for (int tries = 50; tries > 0; tries--) {
			int dz = r.nextInt(spreadXZ) + minXZ;
			int dx = r.nextInt(spreadXZ) + minXZ;
			if (r.nextInt(2) == 0) dz = -dz;
			if (r.nextInt(2) == 0) dx = -dx;
			target = new BlockPos(mob.getBlockX() + dx, mob.getBlockY() + r.nextInt(spreadY) - offsetY, mob.getBlockZ() + dz);
			if (mob.level().isEmptyBlock(target) && canSee(mob, target)) break;
		}
		return target;
	}

	/** OreSpawn's fling after a hit: {@code ks} away from the attacker and {@code up} up (twice that for players and the dead). */
	static void fling(Entity attacker, Entity target, double ks, double up) {
		float a = (float) Math.atan2(target.getZ() - attacker.getZ(), target.getX() - attacker.getX());
		if (!target.isAlive() || target instanceof Player) up *= 2.0;
		target.push(Math.cos(a) * ks, up, Math.sin(a) * ks);
	}

	/** Hitting the Ender Dragon: OreSpawn hurt its head (1 time in {@code headOdds}) or its body with explosion damage. */
	static void hitDragon(ServerLevel level, EnderDragon dragon, int headOdds, float damage) {
		String wanted = dragon.getRandom().nextInt(headOdds) == 1 ? "head" : "body";
		for (EnderDragonPart part : dragon.getSubEntities()) {
			if (part.name.equals(wanted)) {
				dragon.hurt(level, part, level.damageSources().explosion(null, null), damage);
				return;
			}
		}
	}

	/** dropItemRand: drops the stack 1 block up and up to {@code n - 1} blocks (times {@code mult}) off to each side. */
	static void dropAround(Mob mob, ServerLevel level, ItemStack stack, int n, float mult) {
		if (stack.isEmpty()) return;
		RandomSource r = mob.getRandom();
		double x = mob.getX() + (r.nextInt(n) - r.nextInt(n)) * mult;
		double z = mob.getZ() + (r.nextInt(n) - r.nextInt(n)) * mult;
		level.addFreshEntity(new ItemEntity(level, x, mob.getY() + 1.0, z, stack));
	}

	/** EntityLiving.dropFewItems: 0-2 of the item, plus up to the killing player's Looting level. */
	static void dropFew(Mob mob, ServerLevel level, DamageSource source, @Nullable Item item) {
		if (item == null || item == Items.AIR) return;
		int n = mob.getRandom().nextInt(3);
		int looting = looting(level, source);
		if (looting > 0) n += mob.getRandom().nextInt(looting + 1);
		for (int i = 0; i < n; i++) mob.spawnAtLocation(level, item);
	}

	/** The Looting level of the player who killed it (1.7.10 counted only players). */
	static int looting(ServerLevel level, DamageSource source) {
		if (!(source.getEntity() instanceof Player killer)) return 0;
		return EnchantmentHelper.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING),
			killer);
	}

	/** ItemStack.addEnchantment as OreSpawn used it (levels may go past vanilla's maximum). */
	static void enchant(ServerLevel level, ItemStack stack, ResourceKey<Enchantment> key, int lvl) {
		if (!stack.isEmpty()) stack.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key), lvl);
	}

	/**
	 * OreSpawn's treasure gear, as the Basilisk (emerald) and Cephadrome (ruby) drop it: {@code kind} is the gear's
	 * material prefix, {@code roll} which piece (3 sword, 4 shovel, 5 pickaxe, 6 axe, 7 hoe, 8 helmet, 9 chestplate,
	 * 10 leggings, 11 boots), each enchanted at random the way OreSpawn did. Empty for any other roll.
	 */
	static ItemStack treasureGear(ServerLevel level, RandomSource r, String kind, int roll) {
		ItemStack s;
		switch (roll) {
			case 3 -> {
				s = new ItemStack(ModItems.byName(kind + "_sword"));
				maybe(level, r, s, 6, Enchantments.SHARPNESS, 1, 5);
				maybe(level, r, s, 6, Enchantments.BANE_OF_ARTHROPODS, 1, 5);
				maybe(level, r, s, 6, Enchantments.KNOCKBACK, 1, 5);
				maybe(level, r, s, 6, Enchantments.LOOTING, 1, 5);
				maybe(level, r, s, 2, Enchantments.UNBREAKING, 2, 4);
				maybe(level, r, s, 6, Enchantments.FIRE_ASPECT, 1, 5);
				maybe(level, r, s, 6, Enchantments.SHARPNESS, 1, 5);
			}
			case 4, 5, 6, 7 -> {
				String tool = roll == 4 ? "_shovel" : roll == 5 ? "_pickaxe" : roll == 6 ? "_axe" : "_hoe";
				s = new ItemStack(ModItems.byName(kind + tool));
				maybe(level, r, s, 2, Enchantments.UNBREAKING, 2, 4);
				maybe(level, r, s, 6, Enchantments.EFFICIENCY, 1, 5);
				if (roll == 5) maybe(level, r, s, 6, Enchantments.FORTUNE, 1, 5);
			}
			case 8, 9, 10 -> {
				String piece = roll == 8 ? "_helmet" : roll == 9 ? "_chestplate" : "_leggings";
				s = new ItemStack(ModItems.byName(kind + piece));
				maybe(level, r, s, 6, Enchantments.PROTECTION, 1, 5);
				maybe(level, r, s, 6, Enchantments.BLAST_PROTECTION, 1, 5);
				maybe(level, r, s, 6, Enchantments.FIRE_PROTECTION, 1, 5);
				maybe(level, r, s, 6, Enchantments.PROJECTILE_PROTECTION, 1, 5);
				maybe(level, r, s, 2, Enchantments.UNBREAKING, 2, 4);
				if (roll == 8) {
					maybe(level, r, s, 6, Enchantments.RESPIRATION, 1, 2);
					maybe(level, r, s, 6, Enchantments.AQUA_AFFINITY, 1, 5);
				}
			}
			case 11 -> {
				s = new ItemStack(ModItems.byName(kind + "_boots"));
				maybe(level, r, s, 6, Enchantments.FEATHER_FALLING, 5, 5);
				maybe(level, r, s, 2, Enchantments.UNBREAKING, 2, 4);
			}
			default -> s = ItemStack.EMPTY;
		}
		return s;
	}

	/** 1 time in {@code odds} (OreSpawn's nextInt(odds) == 1) adds the enchantment at {@code base + nextInt(spread)}. */
	private static void maybe(ServerLevel level, RandomSource r, ItemStack s, int odds, ResourceKey<Enchantment> e, int base, int spread) {
		if (r.nextInt(odds) == 1) enchant(level, s, e, base + r.nextInt(spread));
	}

	/** An OreSpawn sound that may not be registered (OreSpawn's "MothraWings" has capitals, which 26.3 ids can't). */
	static @Nullable SoundEvent optionalSound(String name) {
		return BuiltInRegistries.SOUND_EVENT.getValue(OreSpawn.id(name));
	}

	static void play(Entity e, @Nullable SoundEvent sound, float volume, float pitch) {
		if (sound != null) e.playSound(sound, volume, pitch);
	}

	/** Whether every block in the box (x from x0 to x1, z from z0 to z1, y from y0 to y1 above pos, all inclusive) is air. */
	static boolean airAbove(LevelReader level, BlockPos pos, int x0, int x1, int z0, int z1, int y0, int y1) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int k = z0; k <= z1; k++) {
			for (int j = x0; j <= x1; j++) {
				for (int i = y0; i <= y1; i++) {
					if (!level.isEmptyBlock(p.set(pos.getX() + j, pos.getY() + i, pos.getZ() + k))) return false;
				}
			}
		}
		return true;
	}

	/**
	 * A MoveControl that leaves the mob's forward push alone while it has nowhere to walk to. In 1.7.10 these flyers set
	 * moveForward after the move helper had run, so it pushed them along; 26.3 runs the move control afterwards, which
	 * would zero it.
	 */
	static MoveControl<Mob> keepForward(Mob mob) {
		return new MoveControl<>(mob) {
			@Override
			public void tick() {
				if (this.operation == MoveControl.Operation.WAIT) return;
				super.tick();
			}
		};
	}

	/**
	 * scan_it: looks over the faces of the box {@code dx, dy, dz} around (x, y, z) for a matching block and keeps the one
	 * nearest the centre in {@code best} (x, y, z, distance squared; start it at 99999). Returns whether this shell found
	 * a nearer one.
	 */
	static boolean scanShell(Level level, int x, int y, int z, int dx, int dy, int dz, Predicate<BlockState> match, int[] best) {
		boolean found = false;
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int i = -dy; i <= dy; i++) {
			for (int j = -dz; j <= dz; j++) {
				found |= consider(level, p.set(x + dx, y + i, z + j), dx * dx + j * j + i * i, match, best);
				found |= consider(level, p.set(x - dx, y + i, z + j), dx * dx + j * j + i * i, match, best);
			}
		}
		for (int i = -dx; i <= dx; i++) {
			for (int j = -dz; j <= dz; j++) {
				found |= consider(level, p.set(x + i, y + dy, z + j), dy * dy + j * j + i * i, match, best);
				found |= consider(level, p.set(x + i, y - dy, z + j), dy * dy + j * j + i * i, match, best);
			}
		}
		for (int i = -dx; i <= dx; i++) {
			for (int j = -dy; j <= dy; j++) {
				found |= consider(level, p.set(x + i, y + j, z + dz), dz * dz + j * j + i * i, match, best);
				found |= consider(level, p.set(x + i, y + j, z - dz), dz * dz + j * j + i * i, match, best);
			}
		}
		return found;
	}

	private static boolean consider(Level level, BlockPos p, int d, Predicate<BlockState> match, int[] best) {
		if (d >= best[3] || !match.test(level.getBlockState(p))) return false;
		best[0] = p.getX();
		best[1] = p.getY();
		best[2] = p.getZ();
		best[3] = d;
		return true;
	}
}
