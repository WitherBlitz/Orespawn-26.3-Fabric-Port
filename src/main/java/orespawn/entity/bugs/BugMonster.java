package orespawn.entity.bugs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.OreSpawn;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * What OreSpawn's big bugs share (each was its own EntityMob in 1.7.10): the "attacking" flag their models read,
 * removal in Peaceful (EntityMob.onUpdate), drops scattered around the body (their dropItemRand), the order they pick
 * targets in (GenericTargetSorter), the random gear the bosses drop, and helpers for their spawn checks.
 * <p>
 * OreSpawn's "PlayNicely" option is taken as its default, off.
 */
public abstract class BugMonster extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(BugMonster.class, EntityDataSerializers.BOOLEAN);

	protected BugMonster(EntityType<? extends BugMonster> type, Level level) {
		super(type, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
	}

	public boolean attacking() {
		return entityData.get(ATTACKING);
	}

	protected void setAttacking(boolean attacking) {
		entityData.set(ATTACKING, attacking);
	}

	/** EntityMob.onUpdate: gone in Peaceful. */
	@Override
	public void checkDespawn() {
		if (level().getDifficulty() == Difficulty.PEACEFUL) discard();
		else super.checkDespawn();
	}

	/**
	 * For the flyers: OreSpawn set moveForward after 1.7.10's move helper had run, so it stayed; 26.3's MoveControl runs
	 * after customServerAiStep and would zero it, so they get one that does nothing (they never use navigation).
	 */
	protected void keepOwnSteering() {
		this.moveControl = new MoveControl(this) {
			@Override
			public void tick() {
			}
		};
	}

	/** Their getCanSpawnHere replaced EntityCreature's light/path test entirely, so their checkSpawn alone decides. */
	@Override
	public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
		return true;
	}

	/** func_70647_i: their voices don't vary in pitch. */
	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/**
	 * An OreSpawn sound event looked up by name, or null if the pack doesn't have it. Used for OreSpawn's "Beebuzz" and
	 * "MothraWings", whose capitals 26.3 ids can't hold: they're expected lowercased ("beebuzz", "mothrawings").
	 */
	protected static @Nullable SoundEvent optionalSound(String name) {
		return BuiltInRegistries.SOUND_EVENT.getValue(OreSpawn.id(name));
	}

	protected static boolean isCreativeOrSpectator(LivingEntity e) {
		return e instanceof Player p && (p.isCreative() || p.isSpectator());
	}

	/** dropItemRand: the stack lands up to spread - 1 blocks away on x and z, a block up. Missing items (air) are skipped. */
	protected void dropRand(ServerLevel level, ItemStack stack, int spread) {
		if (stack.isEmpty()) return;
		ItemEntity item = new ItemEntity(level, getX() + random.nextInt(spread) - random.nextInt(spread), getY() + 1.0,
			getZ() + random.nextInt(spread) - random.nextInt(spread), stack);
		level.addFreshEntity(item);
	}

	protected void dropRand(ServerLevel level, Item item, int spread) {
		dropRand(level, new ItemStack(item), spread);
	}

	/** GenericTargetSorter: distance squared, halved for creepers, divided by width x height when that's over 1. */
	private double targetWeight(LivingEntity e) {
		double d = distanceToSqr(e);
		if (e instanceof Creeper) d /= 2.0;
		double size = e.getBbHeight() * e.getBbWidth();
		return size > 1.0 ? d / size : d;
	}

	/** findSomethingToAttack: everything alive in the box, best first by GenericTargetSorter, and the first that suits. */
	protected @Nullable LivingEntity findTarget(double dx, double dy, double dz, Predicate<LivingEntity> suitable) {
		List<LivingEntity> all = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(dx, dy, dz), e -> true);
		all.sort(Comparator.comparingDouble(this::targetWeight));
		for (LivingEntity e : all) {
			if (suitable.test(e)) return e;
		}
		return null;
	}

	/** canSeeTarget: nothing solid between (x, y + 0.75, z) and the block's corner. */
	protected boolean canSee(BlockPos p) {
		Vec3 from = new Vec3(getX(), getY() + 0.75, getZ());
		return level().clip(new ClipContext(from, Vec3.atLowerCornerOf(p), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType()
			== HitResult.Type.MISS;
	}

	/**
	 * MyCanSee (Molenoid, CaterKiller): steps from a point {@code ahead} blocks in front of it and {@code up} above its feet
	 * towards the target's middle, a block or less at a time, and only air and {@code passable} blocks may be in the way.
	 */
	protected boolean canSeeThrough(LivingEntity e, double ahead, double up, Predicate<BlockState> passable) {
		float sx = (float) (getX() - ahead * Math.sin(Math.toRadians(getYRot())));
		float sy = (float) (getY() + up);
		float sz = (float) (getZ() + ahead * Math.cos(Math.toRadians(getYRot())));
		float dx = (float) ((e.getX() - sx) / 10.0);
		float dy = (float) ((e.getY() + e.getBbHeight() / 2.0F - sy) / 10.0);
		float dz = (float) ((e.getZ() - sz) / 10.0);
		int steps = 10;
		if (Math.abs(dx) > 1.0) {
			dy /= Math.abs(dx);
			dz /= Math.abs(dx);
			steps = (int) (steps * Math.abs(dx));
			dx = Mth.clamp(dx, -1.0F, 1.0F);
		}
		if (Math.abs(dy) > 1.0) {
			dx /= Math.abs(dy);
			dz /= Math.abs(dy);
			steps = (int) (steps * Math.abs(dy));
			dy = Mth.clamp(dy, -1.0F, 1.0F);
		}
		if (Math.abs(dz) > 1.0) {
			dy /= Math.abs(dz);
			dx /= Math.abs(dz);
			steps = (int) (steps * Math.abs(dz));
			dz = Mth.clamp(dz, -1.0F, 1.0F);
		}
		for (int i = 0; i < steps; i++) {
			sx += dx;
			sy += dy;
			sz += dz;
			BlockState state = level().getBlockState(BlockPos.containing(sx, sy, sz));
			if (!state.isAir() && !passable.test(state)) return false;
		}
		return true;
	}

	/** The knock OreSpawn's bugs give after a hit (Entity.addVelocity), twice as high on players and removed things. */
	protected void knock(Entity target, double ks, double up) {
		float f3 = (float) Math.atan2(target.getZ() - getZ(), target.getX() - getX());
		if (target.isRemoved() || target instanceof Player) up *= 2.0;
		target.push(Math.cos(f3) * ks, up, Math.sin(f3) * ks);
	}

	/** spawnCreature: a new mob at x, y, z facing a random way, which then makes its sound. */
	protected static @Nullable Mob summon(ServerLevel level, @Nullable EntityType<?> type, double x, double y, double z) {
		if (type == null || !(type.create(level, EntitySpawnReason.MOB_SUMMONED) instanceof Mob mob)) return null;
		mob.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
		mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
		level.addFreshEntity(mob);
		mob.playAmbientSound();
		return mob;
	}

	/**
	 * The gear OreSpawn's bosses drop: 1-5 rolls of 20, where 0 gives {@code roll0}, 1 the gem, 2 a diamond block, 3-11
	 * the tools and armor ({@code gear}: sword, shovel, pickaxe, axe, hoe, helmet, chestplate, leggings, boots) with random
	 * enchantments (levels past vanilla's maximums, as OreSpawn's were), 12 {@code roll12}, and the rest nothing.
	 */
	protected void dropGear(ServerLevel level, @Nullable Item roll0, Item gem, Item[] gear, @Nullable Item roll12) {
		int rolls = 1 + random.nextInt(5);
		for (int n = 0; n < rolls; n++) {
			int r = random.nextInt(20);
			ItemStack is = switch (r) {
				case 0 -> roll0 == null ? ItemStack.EMPTY : new ItemStack(roll0);
				case 1 -> new ItemStack(gem);
				case 2 -> new ItemStack(Blocks.DIAMOND_BLOCK);
				case 12 -> roll12 == null ? ItemStack.EMPTY : new ItemStack(roll12);
				default -> r < 12 ? new ItemStack(gear[r - 3]) : ItemStack.EMPTY;
			};
			if (is.isEmpty()) continue;
			switch (r) {
				case 3 -> {
					ench(level, is, 6, Enchantments.SHARPNESS, 1, 5);
					ench(level, is, 6, Enchantments.BANE_OF_ARTHROPODS, 1, 5);
					ench(level, is, 6, Enchantments.KNOCKBACK, 1, 5);
					ench(level, is, 6, Enchantments.LOOTING, 1, 5);
					ench(level, is, 2, Enchantments.UNBREAKING, 2, 4);
					ench(level, is, 6, Enchantments.FIRE_ASPECT, 1, 5);
					ench(level, is, 6, Enchantments.SHARPNESS, 1, 5);
				}
				case 4, 6, 7 -> {
					ench(level, is, 2, Enchantments.UNBREAKING, 2, 4);
					ench(level, is, 6, Enchantments.EFFICIENCY, 1, 5);
				}
				case 5 -> {
					ench(level, is, 2, Enchantments.UNBREAKING, 2, 4);
					ench(level, is, 6, Enchantments.EFFICIENCY, 1, 5);
					ench(level, is, 6, Enchantments.FORTUNE, 1, 5);
				}
				case 8, 9, 10 -> {
					ench(level, is, 6, Enchantments.PROTECTION, 1, 5);
					ench(level, is, 6, Enchantments.BLAST_PROTECTION, 1, 5);
					ench(level, is, 6, Enchantments.FIRE_PROTECTION, 1, 5);
					ench(level, is, 6, Enchantments.PROJECTILE_PROTECTION, 1, 5);
					ench(level, is, 2, Enchantments.UNBREAKING, 2, 4);
					if (r == 8) {
						ench(level, is, 6, Enchantments.RESPIRATION, 1, 2);
						ench(level, is, 6, Enchantments.AQUA_AFFINITY, 1, 5);
					}
				}
				case 11 -> {
					ench(level, is, 6, Enchantments.FEATHER_FALLING, 5, 5);
					ench(level, is, 2, Enchantments.UNBREAKING, 2, 4);
				}
				default -> {
				}
			}
			dropRand(level, is, 5);
		}
	}

	/** One of OreSpawn's "if (rand.nextInt(odds) == 1) addEnchantment(e, base + rand.nextInt(spread))". */
	private void ench(ServerLevel level, ItemStack stack, int odds, ResourceKey<Enchantment> key, int base, int spread) {
		if (random.nextInt(odds) == 1) {
			stack.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key), base + random.nextInt(spread));
		}
	}

	// ---- spawn check helpers (getCanSpawnHere looked at blocks relative to the spawn position) ----

	/** Every block at pos + (x, y, z) for x in [x0, x1), y in [y0, y1), z in [z0, z1) is air. */
	protected static boolean allAir(LevelAccessor level, BlockPos pos, int x0, int x1, int y0, int y1, int z0, int z1) {
		for (int z = z0; z < z1; z++) {
			for (int x = x0; x < x1; x++) {
				for (int y = y0; y < y1; y++) {
					if (!level.getBlockState(pos.offset(x, y, z)).isAir()) return false;
				}
			}
		}
		return true;
	}

	/** No other mob of this class within the new mob's box grown by dx, dy, dz (findNearestEntityWithinAABB). */
	protected static boolean noneNear(ServerLevelAccessor level, EntityType<?> type, Class<? extends Entity> cls, BlockPos pos, double dx, double dy,
			double dz) {
		AABB box = type.getDimensions().makeBoundingBox(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(dx, dy, dz);
		return level.getEntitiesOfClass(cls, box, e -> true).isEmpty();
	}
}
