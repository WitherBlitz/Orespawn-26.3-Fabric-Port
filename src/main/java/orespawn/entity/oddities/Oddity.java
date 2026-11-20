package orespawn.entity.oddities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.OreSpawn;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/** Small pieces OreSpawn's Oddities share: target search, drops, block scans, flight steering, stand-in fireballs. */
public final class Oddity {
	private Oddity() {
	}

	/** GenericTargetSorter + findSomethingToAttack: the nearest living thing within the box that passes {@code ok}. */
	public static @Nullable LivingEntity nearest(Mob mob, double xz, double y, Predicate<LivingEntity> ok) {
		List<LivingEntity> list = mob.level().getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(xz, y, xz));
		list.sort(Comparator.comparingDouble(mob::distanceToSqr));
		for (LivingEntity e : list) {
			if (ok.test(e)) return e;
		}
		return null;
	}

	/**
	 * Whether the entity is one of OreSpawn's creatures with these ids, for OreSpawn's "not that one" target lists; it
	 * works for creatures other groups port later without referring to their classes.
	 */
	public static boolean is(Entity e, String... ids) {
		var key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
		if (!OreSpawn.MOD_ID.equals(key.getNamespace())) return false;
		for (String id : ids) {
			if (id.equals(key.getPath())) return true;
		}
		return false;
	}

	public static boolean creative(LivingEntity e) {
		return e instanceof Player p && (p.isCreative() || p.isSpectator());
	}

	/** canSeeTarget: nothing solid between 0.75 above the mob's feet and the point. */
	public static boolean canSee(Entity mob, double x, double y, double z) {
		Vec3 from = new Vec3(mob.getX(), mob.getY() + 0.75, mob.getZ());
		return mob.level().clip(new ClipContext(from, new Vec3(x, y, z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob)).getType()
			== HitResult.Type.MISS;
	}

	public static boolean canSee(Entity mob, BlockPos p) {
		return canSee(mob, p.getX(), p.getY(), p.getZ());
	}

	/** dropItemRand: an item dropped at a random spot within {@code spread} blocks, {@code up} above the mob. */
	public static void dropRand(ServerLevel level, Entity e, ItemStack stack, int spread, double up) {
		if (stack.isEmpty()) return;
		var r = e.getRandom();
		double x = e.getX() + r.nextInt(spread) - r.nextInt(spread);
		double z = e.getZ() + r.nextInt(spread) - r.nextInt(spread);
		ItemEntity item = new ItemEntity(level, x, e.getY() + up, z, stack);
		item.setDefaultPickUpDelay();
		level.addFreshEntity(item);
	}

	public static void dropRand(ServerLevel level, Entity e, ItemLike item, int spread, double up) {
		dropRand(level, e, new ItemStack(item), spread, up);
	}

	/** 1.7.10's dropFewItems count: 0-2, plus 0 to the killer's Looting level. */
	public static int fewItems(Mob mob, DamageSource source) {
		int n = mob.getRandom().nextInt(3);
		if (source.getEntity() instanceof LivingEntity killer) {
			var looting = killer.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING);
			int l = EnchantmentHelper.getEnchantmentLevel(looting, killer);
			if (l > 0) n += mob.getRandom().nextInt(l + 1);
		}
		return n;
	}

	/** An OreSpawn sound that may not be registered (OreSpawn's "MothraWings" has capitals, which 26.3 ids can't). */
	public static @Nullable SoundEvent optionalSound(String name) {
		return BuiltInRegistries.SOUND_EVENT.getValue(OreSpawn.id(name));
	}

	public static void play(Entity e, @Nullable SoundEvent sound, float volume, float pitch) {
		if (sound != null) e.playSound(sound, volume, pitch);
	}

	/** "random.bow" with OreSpawn's pitch. */
	public static void bowSound(Entity e) {
		e.playSound(SoundEvents.ARROW_SHOOT, 0.75F, 1.0F / (e.getRandom().nextFloat() * 0.4F + 0.8F));
	}

	/**
	 * scan_it: looks over the faces of the box {@code dx, dy, dz} around (x, y, z) for a matching block and keeps the one
	 * nearest the centre in {@code best} (x, y, z, distance squared). Returns whether this shell found one.
	 */
	public static boolean scanShell(Level level, int x, int y, int z, int dx, int dy, int dz, Predicate<BlockState> match, int[] best) {
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
		if (!match.test(level.getBlockState(p)) || d >= best[3]) return false;
		best[0] = p.getX();
		best[1] = p.getY();
		best[2] = p.getZ();
		best[3] = d;
		return true;
	}

	/**
	 * OreSpawn's widening search (Spyro, Dragon, Stinky, Rubber Ducky): shells 1 to {@code max - 1} with height capped
	 * at {@code capY}, every other shell from {@code skipFrom} on. Returns {x, y, z, dist²}, dist² 99999 for none.
	 */
	public static int[] search(Level level, int x, int y, int z, int max, int capY, int skipFrom, Predicate<BlockState> match) {
		int[] best = {0, 0, 0, 99999};
		for (int i = 1; i < max; i++) {
			int j = Math.min(i, capY);
			if (scanShell(level, x, y, z, i, j, i, match, best)) break;
			if (i >= skipFrom) i++;
		}
		return best;
	}

	/**
	 * The flight steering every OreSpawn flier uses: velocity eases towards {@code hs} (horizontal) and {@code vs}
	 * (vertical) in the direction of the target point by {@code ha} and {@code va}; the body turns 1/{@code turnDiv} of
	 * the way to where it's going.
	 */
	public static void steer(Mob mob, double tx, double ty, double tz, double hs, double vs, double ha, double va, float turnDiv) {
		Vec3 m = mob.getDeltaMovement();
		double mx = m.x + (Math.signum(tx - mob.getX()) * hs - m.x) * ha;
		double my = m.y + (Math.signum(ty - mob.getY()) * vs - m.y) * va;
		double mz = m.z + (Math.signum(tz - mob.getZ()) * hs - m.z) * ha;
		mob.setDeltaMovement(mx, my, mz);
		float yaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0F;
		mob.setYRot(mob.getYRot() + Mth.wrapDegrees(yaw - mob.getYRot()) / turnDiv);
	}

	/** Small (blaze) fireball from (x, y, z) at the target point, as OreSpawn's EntitySmallFireball shots. */
	public static void smallFireball(Mob shooter, double x, double y, double z, double tx, double ty, double tz) {
		SmallFireball f = new SmallFireball(shooter.level(), shooter, new Vec3(tx - x, ty - y, tz - z));
		f.snapTo(x, y, z, shooter.getYRot(), 0.0F);
		shooter.level().addFreshEntity(f);
	}

	/** OreSpawn's BetterFireball aimed at the target point (see {@link #betterFireball}), with "random.fuse". */
	public static void bigFireball(Mob shooter, double x, double y, double z, double tx, double ty, double tz, boolean notMe) {
		Projectile f = betterFireball(shooter, x, y, z, new Vec3(tx - x, ty - y, tz - z), notMe, false, 0.1);
		shooter.level().addFreshEntity(f);
		shooter.playSound(SoundEvents.TNT_PRIMED, 1.0F, 1.0F / (shooter.getRandom().nextFloat() * 0.4F + 0.8F));
	}

	/**
	 * OreSpawn's BetterFireball, from the group that ports it ({@code orespawn:better_fireball}, looked up by id so this
	 * group doesn't depend on its class), speeding up along {@code dir} by {@code accel} a tick. Without it, a vanilla
	 * stand-in: a ghast fireball of blast 1, or for a small one a blaze fireball (both behave much like it).
	 * Not added to the level yet.
	 */
	public static Projectile betterFireball(Mob shooter, double x, double y, double z, Vec3 dir, boolean notMe, boolean small, double accel) {
		Projectile p = orespawnProjectile("better_fireball", shooter, x, y, z);
		if (p instanceof AbstractHurtingProjectile f) {
			if (notMe) call(f, "setNotMe");
			if (small) call(f, "setSmall");
			f.accelerationPower = accel;
			f.setDeltaMovement(dir.normalize().scale(accel));
			return f;
		}
		AbstractHurtingProjectile f = small ? new SmallFireball(shooter.level(), shooter, dir) : new LargeFireball(shooter.level(), shooter, dir, 1);
		f.accelerationPower = accel;
		f.setDeltaMovement(dir.normalize().scale(accel));
		f.snapTo(x, y, z, shooter.getYRot(), 0.0F);
		return f;
	}

	/**
	 * One of OreSpawn's projectiles another group ports (Acid, Ice Ball, Water Ball, BetterFireball), by its id, owned by
	 * the shooter and placed at (x, y, z); null if that group isn't in yet. Not added to the level yet.
	 */
	public static @Nullable Projectile orespawnProjectile(String id, Mob shooter, double x, double y, double z) {
		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id(id)).orElse(null);
		if (type == null) return null;
		Entity e = type.create(shooter.level(), EntitySpawnReason.TRIGGERED);
		if (!(e instanceof Projectile p)) return null;
		p.setOwner(shooter);
		p.snapTo(x, y, z, shooter.getYRot(), shooter.getXRot());
		return p;
	}

	/** Calls one of a projectile's no-argument setters (setNotMe, setSmall, setSpecial) if it has it. */
	public static void call(Object o, String method) {
		try {
			o.getClass().getMethod(method).invoke(o);
		} catch (ReflectiveOperationException ignored) {
			// That group's projectile doesn't have this option.
		}
	}

	/** EntityThrowable.setThrowableHeading, then into the level. */
	public static void throwAt(Projectile p, double dx, double dy, double dz, float speed, float inaccuracy) {
		p.shoot(dx, dy, dz, speed, inaccuracy);
		p.level().addFreshEntity(p);
	}

	/** spawnCreature: a plain new creature of that type at the spot, facing a random way. */
	public static <T extends Mob> @Nullable T spawn(ServerLevel level, EntityType<T> type, double x, double y, double z) {
		T e = type.create(level, EntitySpawnReason.MOB_SUMMONED);
		if (e == null) return null;
		e.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
		level.addFreshEntity(e);
		return e;
	}

	/**
	 * Stands in for OreSpawn skipping its goal AI (super.updateAITasks) while a creature flies: a top-priority goal that
	 * holds movement, looking and jumping so the walking goals stay idle.
	 */
	public static class HoldGoals extends Goal {
		private final Mob mob;
		private final BooleanSupplier when;

		public HoldGoals(Mob mob, BooleanSupplier when) {
			this.mob = mob;
			this.when = when;
			setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
		}

		@Override
		public boolean canUse() {
			return when.getAsBoolean();
		}

		@Override
		public void start() {
			mob.getNavigation().stop();
		}

		@Override
		public boolean isInterruptable() {
			return false;
		}
	}
}
