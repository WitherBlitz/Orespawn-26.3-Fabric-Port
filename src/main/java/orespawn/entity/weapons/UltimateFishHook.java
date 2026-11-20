package orespawn.entity.weapons;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.item.weapons.UltimateFishingRodItem;
import orespawn.registry.ModItems;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * OreSpawn's UltimateFishHook, the bobber of The Ultimate Fishing Rod. It fishes in lava as well as water and never
 * burns. A bite comes sooner than vanilla's (OreSpawn waited 50-300 ticks for a fish to come, less 5 s per level of
 * Lure, then 100-200 for it to reach the hook, and the bite lasts 10-30 ticks). What it catches, as OreSpawn chose:
 * <ul>
 * <li>in lava: a Sunspot Urchin, Lava Eel, Sun Fish, Spark Fish or Fire Fish (weights 25, 10, 15, 10, 15);</li>
 * <li>otherwise junk with a 10% chance (2.5% less per level of Luck of the Sea, 1% less per Lure), treasure with 5%
 * (1% more per Luck, 1% less per Lure), and otherwise a fish: half the time vanilla's, half the time OreSpawn's own Blue,
 * Pink, Rock, Wood or Grey Fish (weights 25, 10, 15, 10, 15).</li>
 * </ul>
 * The junk, treasure and vanilla fish come from 26.3's own fishing loot tables (OreSpawn used vanilla's lists of its
 * day). OreSpawn's fish and lava fish belong to other content groups; until those exist the catch falls back to a
 * vanilla fish. Reeling in costs the rod 1 for a catch, 3 for a hooked entity and 2 if the hook lies on the ground.
 * <p>
 * Not ported: OreSpawn also made the caught item resist fire while it flew out of the lava (no 26.3 equivalent for one
 * item entity); lava fish that should survive lava need to be fire resistant items.
 */
public class UltimateFishHook extends FishingHook {
	private static final EntityDataAccessor<Integer> HOOKED = SynchedEntityData.defineId(UltimateFishHook.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> BITING = SynchedEntityData.defineId(UltimateFishHook.class, EntityDataSerializers.BOOLEAN);
	private static final List<Catch> LAVA_FISH = List.of(new Catch("sunspot_urchin", 25), new Catch("lava_eel", 10), new Catch("sun_fish", 15),
		new Catch("spark_fish", 10), new Catch("fire_fish", 15));
	private static final List<Catch> ORESPAWN_FISH = List.of(new Catch("blue_fish", 25), new Catch("pink_fish", 10), new Catch("rock_fish", 15),
		new Catch("wood_fish", 10), new Catch("grey_fish", 15));

	private record Catch(String id, int weight) {}

	private enum State {
		FLYING, HOOKED_IN_ENTITY, BOBBING
	}

	private int luckLevel;
	private int lureTicks;
	private State state = State.FLYING;
	private int life;
	private int ticksInAir;
	private int outOfLiquidTime;
	private int nibble;
	private int timeUntilLured;
	private int timeUntilHooked;
	private float fishAngle;
	private @Nullable Entity hooked;

	public UltimateFishHook(EntityType<? extends UltimateFishHook> type, Level level) {
		super(type, level);
	}

	/** Cast from {@code player}'s eyes the way vanilla's bobber is (OreSpawn copied vanilla's cast). */
	public static UltimateFishHook cast(EntityType<? extends UltimateFishHook> type, Player player, Level level, int luck, int lureTicks) {
		UltimateFishHook hook = new UltimateFishHook(type, level);
		hook.luckLevel = Math.max(0, luck);
		hook.lureTicks = Math.max(0, lureTicks);
		hook.setOwner(player);
		float xRot = player.getXRot();
		float yRot = player.getYRot();
		float yCos = Mth.cos(-yRot * (float) (Math.PI / 180.0) - (float) Math.PI);
		float ySin = Mth.sin(-yRot * (float) (Math.PI / 180.0) - (float) Math.PI);
		float xCos = -Mth.cos(-xRot * (float) (Math.PI / 180.0));
		float xSin = Mth.sin(-xRot * (float) (Math.PI / 180.0));
		hook.snapTo(player.getX() - ySin * 0.3, player.getEyeY(), player.getZ() - yCos * 0.3, yRot, xRot);
		Vec3 move = new Vec3(-ySin, Mth.clamp(-(xSin / xCos), -5.0F, 5.0F), -yCos);
		double dist = move.length();
		var r = hook.random;
		move = move.multiply(0.6 / dist + r.triangle(0.5, 0.0103365), 0.6 / dist + r.triangle(0.5, 0.0103365), 0.6 / dist + r.triangle(0.5, 0.0103365));
		hook.setDeltaMovement(move);
		hook.setYRot((float) (Mth.atan2(move.x, move.z) * 180.0F / (float) Math.PI));
		hook.setXRot((float) (Mth.atan2(move.y, move.horizontalDistance()) * 180.0F / (float) Math.PI));
		hook.yRotO = hook.getYRot();
		hook.xRotO = hook.getXRot();
		return hook;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(HOOKED, 0);
		builder.define(BITING, false);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
		if (HOOKED.equals(accessor)) {
			int id = entityData.get(HOOKED);
			hooked = id > 0 ? level().getEntity(id - 1) : null;
		}
		if (BITING.equals(accessor) && entityData.get(BITING)) {
			setDeltaMovement(getDeltaMovement().x, -0.4F * Mth.nextFloat(random, 0.6F, 1.0F), getDeltaMovement().z);
		}
		super.onSyncedDataUpdated(accessor);
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public @Nullable Entity getHookedIn() {
		return hooked;
	}

	private void setHooked(@Nullable Entity e) {
		hooked = e;
		entityData.set(HOOKED, e == null ? 0 : e.getId() + 1);
	}

	private static boolean isLiquid(FluidState fluid) {
		return fluid.is(FluidTags.WATER) || fluid.is(FluidTags.LAVA);
	}

	private boolean shouldStop(Player owner) {
		if (owner.canInteractWithLevel()) {
			boolean holding = owner.getMainHandItem().getItem() instanceof UltimateFishingRodItem
				|| owner.getOffhandItem().getItem() instanceof UltimateFishingRodItem;
			if (holding && distanceToSqr(owner) <= 1024.0) return false;
		}
		discard();
		return true;
	}

	@Override
	public void tick() {
		baseTick();
		Player owner = getPlayerOwner();
		if (owner == null) {
			discard();
			return;
		}
		if (!level().isClientSide() && shouldStop(owner)) return;
		ticksInAir++;
		if (onGround()) {
			if (++life >= 1200) {
				discard();
				return;
			}
		} else {
			life = 0;
		}
		BlockPos pos = blockPosition();
		FluidState fluid = level().getFluidState(pos);
		float liquidHeight = isLiquid(fluid) ? fluid.getHeight(level(), pos) : 0.0F;
		boolean inLiquid = liquidHeight > 0.0F;
		if (state == State.FLYING) {
			if (hooked != null) {
				setDeltaMovement(Vec3.ZERO);
				state = State.HOOKED_IN_ENTITY;
				return;
			}
			if (inLiquid) {
				setDeltaMovement(getDeltaMovement().multiply(0.3, 0.2, 0.3));
				state = State.BOBBING;
				return;
			}
			HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
			hitTargetOrDeflectSelf(hit);
		} else if (state == State.HOOKED_IN_ENTITY) {
			if (hooked != null) {
				if (!hooked.isRemoved() && hooked.canInteractWithLevel() && hooked.level().dimension() == level().dimension()) {
					setPos(hooked.getX(), hooked.getY(0.8), hooked.getZ());
				} else {
					setHooked(null);
					state = State.FLYING;
				}
			}
			return;
		} else {
			Vec3 move = getDeltaMovement();
			double force = getY() + move.y - pos.getY() - liquidHeight;
			if (Math.abs(force) < 0.01) force += Math.signum(force) * 0.1;
			setDeltaMovement(move.x * 0.9, move.y - force * random.nextFloat() * 0.2, move.z * 0.9);
			if (inLiquid) {
				outOfLiquidTime = Math.max(0, outOfLiquidTime - 1);
				if (entityData.get(BITING)) setDeltaMovement(getDeltaMovement().add(0.0, -0.1 * random.nextFloat() * random.nextFloat(), 0.0));
				if (level() instanceof ServerLevel level) catchingFish(level, pos);
			} else {
				outOfLiquidTime = Math.min(10, outOfLiquidTime + 1);
			}
		}
		if (!isLiquid(fluid) && !onGround() && hooked == null) setDeltaMovement(getDeltaMovement().add(0.0, -getDefaultGravity(), 0.0));
		move(MoverType.SELF, getDeltaMovement());
		applyEffectsFromBlocks();
		updateRotation();
		if (state == State.FLYING && (onGround() || horizontalCollision)) setDeltaMovement(Vec3.ZERO);
		setDeltaMovement(getDeltaMovement().scale(0.92));
		reapplyPosition();
	}

	@Override
	protected boolean canHitEntity(Entity e) {
		if (e == getOwner() && ticksInAir < 5) return false;
		return e.canBeHitByProjectile() || e.isAlive() && e instanceof ItemEntity;
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		if (!level().isClientSide()) setHooked(result.getEntity());
	}

	private void catchingFish(ServerLevel level, BlockPos pos) {
		int speed = 1;
		BlockPos above = pos.above();
		if (random.nextFloat() < 0.25F && level.isRainingAt(above)) speed++;
		if (random.nextFloat() < 0.5F && !level.canSeeSky(above)) speed--;
		if (nibble > 0) {
			if (--nibble <= 0) {
				timeUntilLured = 0;
				timeUntilHooked = 0;
				entityData.set(BITING, false);
			}
		} else if (timeUntilHooked > 0) {
			timeUntilHooked -= speed;
			if (timeUntilHooked > 0) {
				fishAngle += (float) random.triangle(0.0, 9.188);
				float angle = fishAngle * (float) (Math.PI / 180.0);
				float sin = Mth.sin(angle);
				float cos = Mth.cos(angle);
				double fx = getX() + sin * timeUntilHooked * 0.1F;
				double fy = Mth.floor(getY()) + 1.0F;
				double fz = getZ() + cos * timeUntilHooked * 0.1F;
				if (isLiquid(level.getFluidState(BlockPos.containing(fx, fy - 1.0, fz)))) {
					if (random.nextFloat() < 0.15F) level.sendParticles(ParticleTypes.BUBBLE, fx, fy - 0.1F, fz, 1, sin, 0.1, cos, 0.0);
					float px = sin * 0.04F;
					float pz = cos * 0.04F;
					level.sendParticles(ParticleTypes.FISHING, fx, fy, fz, 0, pz, 0.01, -px, 1.0);
					level.sendParticles(ParticleTypes.FISHING, fx, fy, fz, 0, -pz, 0.01, px, 1.0);
				}
			} else {
				playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.4F);
				double y = getY() + 0.5;
				int n = (int) (1.0F + getBbWidth() * 20.0F);
				level.sendParticles(ParticleTypes.BUBBLE, getX(), y, getZ(), n, getBbWidth(), 0.0, getBbWidth(), 0.2F);
				level.sendParticles(ParticleTypes.FISHING, getX(), y, getZ(), n, getBbWidth(), 0.0, getBbWidth(), 0.2F);
				nibble = Mth.nextInt(random, 10, 30);
				entityData.set(BITING, true);
			}
		} else if (timeUntilLured > 0) {
			timeUntilLured -= speed;
			float tease = 0.15F;
			if (timeUntilLured < 20) tease += (20 - timeUntilLured) * 0.05F;
			else if (timeUntilLured < 40) tease += (40 - timeUntilLured) * 0.02F;
			else if (timeUntilLured < 60) tease += (60 - timeUntilLured) * 0.01F;
			if (random.nextFloat() < tease) {
				float angle = Mth.nextFloat(random, 0.0F, 360.0F) * (float) (Math.PI / 180.0);
				float dist = Mth.nextFloat(random, 25.0F, 60.0F);
				double fx = getX() + Mth.sin(angle) * dist * 0.1;
				double fy = Mth.floor(getY()) + 1.0F;
				double fz = getZ() + Mth.cos(angle) * dist * 0.1;
				if (isLiquid(level.getFluidState(BlockPos.containing(fx, fy - 1.0, fz)))) {
					level.sendParticles(ParticleTypes.SPLASH, fx, fy, fz, 2 + random.nextInt(2), 0.1F, 0.0, 0.1F, 0.0);
				}
			}
			if (timeUntilLured <= 0) {
				fishAngle = Mth.nextFloat(random, 0.0F, 360.0F);
				timeUntilHooked = Mth.nextInt(random, 100, 200);
			}
		} else {
			timeUntilLured = Mth.nextInt(random, 50, 300) - lureTicks;
		}
	}

	@Override
	public int retrieve(ItemStack rod) {
		Player owner = getPlayerOwner();
		if (!(level() instanceof ServerLevel level) || owner == null || shouldStop(owner)) return 0;
		int damage = 0;
		if (hooked != null) {
			pullEntity(hooked);
			level.broadcastEntityEvent(this, (byte) 31);
			damage = 3;
		} else if (nibble > 0) {
			for (ItemStack stack : pickCatch(level, owner, rod)) {
				ItemEntity item = new ItemEntity(level, getX(), getY() + 1.25, getZ(), stack);
				double dx = owner.getX() - getX();
				double dy = owner.getY() - getY();
				double dz = owner.getZ() - getZ();
				item.setDeltaMovement(dx * 0.1, dy * 0.1 + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08, dz * 0.1);
				level.addFreshEntity(item);
				level.addFreshEntity(new ExperienceOrb(level, owner.getX(), owner.getY() + 0.5, owner.getZ() + 0.5, random.nextInt(6) + 1));
				if (stack.is(ItemTags.FISHES)) owner.awardStat(Stats.FISH_CAUGHT, 1);
			}
			damage = 1;
		}
		if (onGround()) damage = 2;
		discard();
		return damage;
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 31 && hooked != null) {
			pullEntity(hooked);
			return;
		}
		super.handleEntityEvent(id);
	}

	private List<ItemStack> pickCatch(ServerLevel level, Player owner, ItemStack rod) {
		if (isInLava() || level.getFluidState(blockPosition()).is(FluidTags.LAVA)) return weighted(level, owner, rod, LAVA_FISH);
		int lure = lureTicks / 100;
		float junk = Mth.clamp(0.1F - luckLevel * 0.025F - lure * 0.01F, 0.0F, 1.0F);
		float treasure = Mth.clamp(0.05F + luckLevel * 0.01F - lure * 0.01F, 0.0F, 1.0F);
		float f = random.nextFloat();
		if (f < junk) return loot(level, owner, rod, BuiltInLootTables.FISHING_JUNK);
		if (f - junk < treasure) return loot(level, owner, rod, BuiltInLootTables.FISHING_TREASURE);
		return random.nextFloat() < 0.5F ? loot(level, owner, rod, BuiltInLootTables.FISHING_FISH) : weighted(level, owner, rod, ORESPAWN_FISH);
	}

	private List<ItemStack> weighted(ServerLevel level, Player owner, ItemStack rod, List<Catch> table) {
		int total = table.stream().mapToInt(Catch::weight).sum();
		int roll = random.nextInt(total);
		for (Catch c : table) {
			roll -= c.weight();
			if (roll < 0) {
				Item item = ModItems.lazy(c.id());
				return item == Items.AIR ? loot(level, owner, rod, BuiltInLootTables.FISHING_FISH) : List.of(new ItemStack(item));
			}
		}
		return List.of();
	}

	private List<ItemStack> loot(ServerLevel level, Player owner, ItemStack rod, ResourceKey<LootTable> table) {
		LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, position())
			.withParameter(LootContextParams.TOOL, rod).withParameter(LootContextParams.THIS_ENTITY, this).withLuck(luckLevel + owner.getLuck())
			.create(LootContextParamSets.FISHING);
		return level.getServer().reloadableRegistries().getLootTable(table).getRandomItems(params);
	}
}
