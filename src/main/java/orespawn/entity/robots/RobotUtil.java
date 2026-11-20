package orespawn.entity.robots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.OreSpawn;
import orespawn.entity.Ignored;
import orespawn.entity.weapons.LaserBall;

import java.util.Comparator;
import java.util.function.Predicate;

/** What OreSpawn's robots share: their target search and sort, their aim check, knockback and their scattered drops. */
public final class RobotUtil {
	private RobotUtil() {
	}

	/** GenericTargetSorter: nearest first, creepers count as twice as close and big things as closer by their size. */
	public static double sortKey(Entity self, Entity e) {
		double d = self.distanceToSqr(e);
		if (e instanceof Creeper) d /= 2.0;
		double weight = e.getBbHeight() * e.getBbWidth();
		if (weight > 1.0) d /= weight;
		return d;
	}

	/** findSomethingToAttack: the first suitable living thing in the box around {@code self}, by GenericTargetSorter. */
	public static LivingEntity find(Mob self, double xz, double y, Predicate<LivingEntity> suitable) {
		return self.level().getEntitiesOfClass(LivingEntity.class, self.getBoundingBox().inflate(xz, y, xz), suitable).stream()
			.min(Comparator.comparingDouble(e -> sortKey(self, e))).orElse(null);
	}

	/** The robots' isSuitableTarget: alive and seen, not a monster, nor an ignorable bug, nor a creative player. */
	public static boolean isPrey(Mob self, LivingEntity e) {
		if (e == self || !e.isAlive() || Ignored.byHunters(e)) return false;
		if (!self.getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Monster) return false;
		return !(e instanceof Player p) || !(p.isCreative() || p.isSpectator());
	}

	/** How far (radians, 0 to pi) the direction to {@code e} is from where a mob facing {@code yaw} looks. */
	public static double offAngle(Entity self, Entity e, float yaw) {
		double rr = Math.atan2(e.getZ() - self.getZ(), e.getX() - self.getX());
		double rhdir = Math.toRadians((yaw + 90.0F) % 360.0F);
		double pi = 3.1415926545;
		double rdd = Math.abs(rr - rhdir) % (pi * 2.0);
		if (rdd > pi) rdd -= pi * 2.0;
		return Math.abs(rdd);
	}

	/** Entity.addVelocity, synced to players too. */
	public static void knock(Entity target, double x, double y, double z) {
		target.push(x, y, z);
		target.syncVelocity = true;
	}

	/** The knockback OreSpawn's big robots give with a hit: ks sideways and inair up (twice as high for players). */
	public static void knockAway(Entity self, Entity target, double ks, double inair) {
		float f3 = (float) Math.atan2(target.getZ() - self.getZ(), target.getX() - self.getX());
		if (!target.isAlive() || target instanceof Player) inair *= 2.0;
		knock(target, Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
	}

	/** dropItemRand: a stack dropped a block above, up to a block off to each side. */
	public static void drop(ServerLevel level, Entity self, ItemLike item, int count) {
		if (item.asItem() == Items.AIR) return;
		var r = level.getRandom();
		ItemEntity e = new ItemEntity(level, self.getX() + r.nextInt(2) - r.nextInt(2), self.getY() + 1.0, self.getZ() + r.nextInt(2) - r.nextInt(2),
			new ItemStack(item, count));
		level.addFreshEntity(e);
	}

	/** The robots' parts drop: {@code rolls} rolls of 1 in 15 for each of these (redstone block twice), else nothing. */
	public static void parts(ServerLevel level, Entity self, int rolls) {
		ItemLike[] parts = {Items.REDSTONE, Items.REPEATER, Items.COMPARATOR, Items.REDSTONE_BLOCK, Items.DISPENSER, Items.STICKY_PISTON,
			Items.PISTON, Items.LEVER, Items.REDSTONE_BLOCK, Items.LIGHT_WEIGHTED_PRESSURE_PLATE};
		for (int i = 0; i < rolls; i++) {
			int k = level.getRandom().nextInt(15);
			if (k < parts.length) drop(level, self, parts[k], 1);
		}
	}

	/** getCanSpawnHere's check that the 3x2 column above the spot is open (air or tall grass), for {@code height} blocks. */
	public static boolean clearAbove(LevelReader level, BlockPos pos, int height) {
		for (int k = -1; k < 1; k++) {
			for (int j = -1; j <= 1; j++) {
				for (int i = 1; i < height; i++) {
					BlockState s = level.getBlockState(pos.offset(j, i, k));
					if (!s.isAir() && !s.is(Blocks.SHORT_GRASS) && !s.is(Blocks.FERN) && !s.is(Blocks.DEAD_BUSH)) return false;
				}
			}
		}
		return true;
	}

	/** Air, or one of the liquids the leg robots and islands look through. */
	public static boolean airOrLiquid(BlockState s) {
		return s.isAir() || s.is(Blocks.WATER) || s.is(Blocks.LAVA);
	}

	/** Robot2.destroyBlock's list: blocks the Robo-Pounder never smashes (plus anything unbreakable). */
	public static boolean smashable(LevelReader level, BlockPos pos, BlockState s) {
		if (s.isAir() || s.getDestroySpeed(level, pos) < 0.0F) return false;
		return !(s.is(Blocks.OBSIDIAN) || s.is(Blocks.BEDROCK) || s.is(Blocks.QUARTZ_BLOCK) || s.is(Blocks.SPAWNER) || s.is(Blocks.REDSTONE_BLOCK)
			|| s.is(Blocks.IRON_BLOCK) || s.is(Blocks.CHEST));
	}

	public static BlockPos floor(double x, double y, double z) {
		return new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
	}

	/** Another content group's entity type, by its id (null until that group is ported). */
	public static EntityType<?> lazyType(String id) {
		return BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id(id)).orElse(null);
	}

	public static boolean isType(Entity e, String id) {
		return BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).equals(OreSpawn.id(id));
	}

	/**
	 * A robot's laser shot from (x, y, z) at {@code target}, aimed as OreSpawn's robots aimed (a little high, for the
	 * drop): the Weapons group's Robot Laser Charge (orespawn:laser_ball), which spares the robots themselves. Null
	 * (no shot) until that group registers it. Robots' shots have no owner, as in OreSpawn.
	 */
	@SuppressWarnings("unchecked")
	public static LaserBall laser(Level level, double x, double y, double z, float yaw, Entity target, float speed, float spread) {
		EntityType<?> type = lazyType("laser_ball");
		if (type == null) return null;
		LaserBall ball = new LaserBall((EntityType<? extends LaserBall>) type, level);
		ball.setPos(x, y, z);
		ball.setYRot(yaw);
		double dx = target.getX() - x;
		double dy = target.getY() - y;
		double dz = target.getZ() - z;
		float lift = (float) Math.sqrt(dx * dx + dz * dz) * 0.2F;
		ball.shoot(dx, dy + lift, dz, speed, spread);
		return ball;
	}

	/** Another content group's block, by its id (null until that group is ported). */
	public static Block lazyBlock(String id) {
		return BuiltInRegistries.BLOCK.getOptional(OreSpawn.id(id)).orElse(null);
	}
}
