package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * What OreSpawn's three worms (WormSmall, WormMedium, WormLarge) have in common. A worm moves straight through the
 * ground (no clipping) and lives buried in it: gravity pulls it down, and while the block it probes above itself is
 * solid it's pushed back up, so it hovers just under the surface. Near a player it rises until its feet are clear and
 * attacks, then sinks again. If the block it probes is anything but grass, dirt or stone (or open air; short grass
 * and ferns count as air), it's gone at once. It never despawns, never pushes or is pushed by other mobs, takes no
 * suffocation damage, makes no step sounds and doesn't trigger pressure plates. A named (persistent) worm can't pass
 * through the ground.
 */
public abstract class Worm extends Monster {
	/** How much of its vertical speed it keeps each tick (OreSpawn's onUpdate). */
	private final double damping;

	protected Worm(EntityType<? extends Worm> type, Level level, double damping) {
		super(type, level);
		this.damping = damping;
		this.noPhysics = true;
	}

	@Override
	public void tick() {
		if (isPersistenceRequired()) noPhysics = false;
		super.tick();
		if (!level().isClientSide()) setDeltaMovement(getDeltaMovement().multiply(1.0, damping, 1.0));
	}

	/** Open as OreSpawn saw it: air, or 1.7.10's tall grass (short grass and ferns now). */
	protected static boolean isOpen(BlockState state) {
		return state.isAir() || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN);
	}

	/**
	 * Ground a worm can be in: 1.7.10's grass, dirt (with its coarse dirt and podzol kinds) and stone, plus the
	 * granite, diorite and andesite that became stone kinds in 1.8.
	 */
	protected static boolean isEarth(BlockState state) {
		return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL)
			|| state.is(Blocks.STONE) || state.is(Blocks.GRANITE) || state.is(Blocks.DIORITE) || state.is(Blocks.ANDESITE);
	}

	/** The block at this worm's x and z and the given height. */
	protected BlockState probe(int y) {
		return level().getBlockState(new BlockPos(Mth.floor(getX()), y, Mth.floor(getZ())));
	}

	/**
	 * One step of digging: if the probed block isn't open, the worm is pushed up (and dies if it's not in earth).
	 * Returns false if the worm died.
	 */
	protected boolean dig(int y, double pushMotion, double pushPos) {
		BlockState state = probe(y);
		if (isOpen(state)) return true;
		if (!isEarth(state)) {
			discard();
			return false;
		}
		setDeltaMovement(getDeltaMovement().add(0.0, pushMotion, 0.0));
		setPos(getX(), getY() + pushPos, getZ());
		return true;
	}

	/** End of OreSpawn's onLivingUpdate while buried: a little extra sink, and no sideways motion. */
	protected void holdStill() {
		setDeltaMovement(0.0, getDeltaMovement().y - 0.01, 0.0);
		setZza(0.0F);
	}

	/** OreSpawn's pointAtEntity: turns body and head toward the entity. */
	protected void pointAt(Entity e) {
		float yaw = (float) (Mth.atan2(e.getZ() - getZ(), e.getX() - getX()) * 180.0 / Math.PI) - 90.0F;
		setYRot(yaw);
		setYHeadRot(yaw);
	}

	/** The nearest player (spectators aside) whose box meets this worm's box grown by the given amounts. */
	protected @Nullable Player nearestPlayer(double dx, double dy, double dz) {
		return nearest(Player.class, getBoundingBox().inflate(dx, dy, dz));
	}

	protected <T extends Entity> @Nullable T nearest(Class<T> type, AABB box) {
		return level().getEntitiesOfClass(type, box, e -> e != this && !e.isSpectator()).stream()
			.min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
	}

	/** The nearest non-creative player in the box, or null (also null if the nearest one is creative, as in OreSpawn). */
	protected @Nullable Player nearestVictim(double dx, double dy, double dz) {
		Player p = nearestPlayer(dx, dy, dz);
		return p != null && p.isCreative() ? null : p;
	}

	/**
	 * Pulls the item off the player's slot, wears it down by its remaining durability divided by {@code divisor} (at
	 * least 1) and throws it up out of the ground nearby. Returns false if the slot was empty.
	 */
	protected boolean strip(ServerLevel level, Player player, EquipmentSlot slot, int divisor) {
		ItemStack stack = player.getItemBySlot(slot);
		if (stack.isEmpty()) return false;
		player.setItemSlot(slot, ItemStack.EMPTY);
		int wear = stack.getMaxDamage() - stack.getDamageValue();
		wear = wear > divisor ? wear / divisor : 1;
		stack.hurtAndBreak(wear, level, null, broken -> {
		});
		if (!stack.isEmpty()) {
			level.addFreshEntity(new ItemEntity(level, getX() + random.nextInt(5) - random.nextInt(5), getY() + 3.0,
				getZ() + random.nextInt(5) - random.nextInt(5), stack));
		}
		return true;
	}

	/** OreSpawn's dropItemRand for worms: one item thrown up from a random spot around the worm. */
	protected void dropAround(ServerLevel level, ItemStack stack, int spread) {
		level.addFreshEntity(new ItemEntity(level, getX() + random.nextInt(spread) - random.nextInt(spread),
			getY() + 2.5 + random.nextInt(spread), getZ() + random.nextInt(spread) - random.nextInt(spread), stack));
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypes.IN_WALL)) return false;
		return super.hurtServer(level, source, damage);
	}

	/** OreSpawn's getCanSpawnHere replaced the monster light check; each worm's checkSpawn has the whole rule. */
	@Override
	public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
		return true;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return false;
	}

	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	protected void pushEntities() {
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	@Override
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected float getSoundVolume() {
		return 0.5F;
	}
}
