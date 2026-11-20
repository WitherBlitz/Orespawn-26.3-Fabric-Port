package orespawn.entity.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Bosses;

/**
 * OreSpawn's BetterFireball, the fireball Mobzilla and The King spit (OreSpawn's Dragon, Mothra and Brutalfly use it
 * too). It flies like a ghast's fireball (0.1 acceleration, 0.95 drag, 0.8 in water) for up to 30 seconds and passes
 * through its shooter, other BetterFireballs, Mobzilla's head and the royal family. Hitting something: 10 damage (5 for
 * a small one) as fireball damage and 5 seconds of fire; anything over 30 in width x height that isn't one of the giants
 * first loses half its health. Hitting a block sets fire to the face it hit. Unless small, it then explodes (power 1,
 * 2 when big, 4 when really big; with fire, and breaking blocks only when mob griefing is on). With setNotMe it ignores
 * players, OreSpawn's Dragons and Mothras (and fizzles on players and Dragons).
 * <p>
 * Clients saw it as a vanilla large fireball in 1.7.10 (it wasn't registered), so it renders as one here, whatever its
 * size.
 */
public class BetterFireball extends Fireball {
	private static final int LIFETIME = 600;
	private int explosionPower = 1;
	private boolean small;
	private boolean notMe;

	public BetterFireball(EntityType<? extends BetterFireball> type, Level level) {
		super(type, level);
	}

	/** OreSpawn's BetterFireball(world, shooter, dx, dy, dz): starts at the shooter and speeds up along (dx, dy, dz). */
	public BetterFireball(Level level, LivingEntity shooter, double dx, double dy, double dz) {
		super(Bosses.BETTER_FIREBALL, shooter, new Vec3(dx, dy, dz), level);
	}

	/** Ignores players, Dragons and Mothras. */
	public void setNotMe() {
		notMe = true;
	}

	public void setBig() {
		explosionPower = 2;
	}

	public void setReallyBig() {
		explosionPower = 4;
	}

	/** 5 damage, no explosion, a 0.3125 hitbox. */
	public void setSmall() {
		small = true;
		refreshDimensions();
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return small ? EntityDimensions.scalable(0.3125F, 0.3125F) : super.getDimensions(pose);
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide() && tickCount >= LIFETIME) discard();
	}

	@Override
	protected boolean canHitEntity(Entity e) {
		if (e == getOwner() || e instanceof BetterFireball || BossUtil.isRoyalty(e)) return false;
		if (e instanceof BossHead h && h.getType() == Bosses.MOBZILLA_HEAD) return false;
		if (notMe && (e instanceof Player || BossUtil.isType(e, "dragon") || BossUtil.isType(e, "mothra"))) return false;
		return super.canHitEntity(e);
	}

	@Override
	protected void onHit(HitResult result) {
		if (!(level() instanceof ServerLevel level)) return;
		if (result instanceof EntityHitResult hit) {
			Entity e = hit.getEntity();
			if (e instanceof BetterFireball || BossUtil.isType(e, "mothra")) return;
			if (notMe && (e instanceof Player || BossUtil.isType(e, "dragon"))) {
				discard();
				return;
			}
			if (e instanceof Mob mob && BossUtil.isLargeUnknown(mob)) mob.setHealth(mob.getHealth() / 2.0F);
			e.hurtServer(level, damageSources().fireball(this, getOwner()), small ? 5.0F : 10.0F);
			e.igniteForSeconds(5.0F);
		} else if (result instanceof BlockHitResult hit) {
			BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
			if (level.isEmptyBlock(pos)) level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
		}
		if (!small) level.explode(null, getX(), getY(), getZ(), explosionPower, true, Level.ExplosionInteraction.MOB);
		discard();
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("ExplosionPower", explosionPower);
		output.putBoolean("Small", small);
		output.putBoolean("NotMe", notMe);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		explosionPower = input.getIntOr("ExplosionPower", 1);
		small = input.getBooleanOr("Small", false);
		notMe = input.getBooleanOr("NotMe", false);
		refreshDimensions();
	}
}
