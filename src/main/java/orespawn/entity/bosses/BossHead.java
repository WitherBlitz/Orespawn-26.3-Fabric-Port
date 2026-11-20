package orespawn.entity.bosses;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import orespawn.content.Bosses;
import org.jspecify.annotations.Nullable;

/**
 * The giants' head hitbox: OreSpawn's GodzillaHead, KingHead and QueenHead, separate entities that sit in front of and
 * above their boss's body and pass every hit on to it. (26.3's own multipart entities only work for the Ender Dragon, so
 * these are real, unsaved entities like OreSpawn's.) Mobzilla's head (9.9 wide, 10 high) is 16 blocks up and 17
 * forward, along the boss's head rotation; The King's and The Queen's (19.9 wide) are 12 up and 30 forward.
 * <p>
 * The boss spawns its head ({@link #spawn}) and the head follows it on both sides (the client knows its boss through a
 * synced id), so the hitbox never lags. Hits from the boss itself, its kind or another head of its kind are ignored, and
 * so is suffocation, as OreSpawn's heads did. A head whose boss is gone or dead disappears.
 */
public class BossHead extends Entity {
	private static final EntityDataAccessor<Integer> PARENT = SynchedEntityData.defineId(BossHead.class, EntityDataSerializers.INT);
	private @Nullable Mob parent;

	public BossHead(EntityType<? extends BossHead> type, Level level) {
		super(type, level);
		this.noPhysics = true;
	}

	/** Spawns a head of the given type for a boss (server side), placed where it belongs. */
	public static @Nullable BossHead spawn(ServerLevel level, EntityType<BossHead> type, Mob boss) {
		BossHead head = type.create(level, EntitySpawnReason.MOB_SUMMONED);
		if (head == null) return null;
		head.parent = boss;
		head.entityData.set(PARENT, boss.getId());
		head.follow(boss);
		level.addFreshEntity(head);
		return head;
	}

	/** The boss this head belongs to, if it's loaded. */
	public @Nullable Mob parent() {
		if (parent == null || parent.isRemoved()) {
			parent = level().getEntity(entityData.get(PARENT)) instanceof Mob m ? m : null;
		}
		return parent;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(PARENT, -1);
	}

	@Override
	public void tick() {
		super.tick();
		Mob p = parent();
		if (p == null || p.isRemoved() || !p.isAlive()) {
			if (!level().isClientSide()) discard();
			return;
		}
		follow(p);
	}

	private void follow(Mob p) {
		boolean mobzilla = getType() == Bosses.MOBZILLA_HEAD;
		double up = mobzilla ? 16.0 : 12.0;
		double forward = mobzilla ? 17.0 : 30.0;
		double yaw = Math.toRadians(p.getYHeadRot());
		setPos(p.getX() - forward * Math.sin(yaw), p.getY() + up, p.getZ() + forward * Math.cos(yaw));
		setYRot(p.getYRot());
		setDeltaMovement(p.getDeltaMovement());
	}

	/** The boss's own kind: the same boss class, or another head of the same type. */
	private boolean sameKind(@Nullable Entity e, Mob p) {
		return e != null && (e.getClass() == p.getClass() || e instanceof BossHead h && h.getType() == getType());
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (isInvulnerableToBase(source) || source.is(DamageTypes.IN_WALL)) return false;
		Mob p = parent();
		if (p == null || sameKind(source.getEntity(), p) || sameKind(source.getDirectEntity(), p)) return false;
		return p.hurtServer(level, source, damage);
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public boolean is(Entity other) {
		return this == other || parent == other;
	}

	@Override
	public @Nullable ItemStack getPickResult() {
		Mob p = parent();
		return p == null ? null : p.getPickResult();
	}

	@Override
	public boolean shouldBeSaved() {
		return false;
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
	}
}
