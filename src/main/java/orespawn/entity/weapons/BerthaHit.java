package orespawn.entity.weapons;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Weapons;

/**
 * OreSpawn's BerthaHit: the invisible "reach" of Big Bertha, Slice, the Royal Guardian Sword and the Attitude
 * Adjuster. Every swing of one throws it from 2 blocks in front of the player's eyes at twice a snowball's speed; the
 * first thing it touches within reach of the swinger (9 blocks for Bertha and Slice, 10 for the Royal sword, 8 for the
 * Attitude Adjuster) takes the weapon's material damage as a player hit (496, 746 or 82) and is flung away from the
 * player (Bertha and Slice also set it on fire for 10 s). The Attitude Adjuster's hit also explodes (1.5 on a
 * creature, 2.1 on a block, with fire; blocks break only with mobGriefing). Players, tamed pets and the
 * Girlfriend/Boyfriend are never hit (OreSpawn's BigBerthaPvp, off by default).
 */
public class BerthaHit extends ThrowableProjectile {
	public static final int BERTHA = 0;
	public static final int ROYAL = 2;
	public static final int HAMMY = 3;

	private int hitType = BERTHA;

	public BerthaHit(EntityType<? extends BerthaHit> type, Level level) {
		super(type, level);
	}

	/** Bertha.onEntitySwing: the hit thrown on every swing. */
	public static void swing(ServerLevel level, Player p, int hitType) {
		BerthaHit hit = new BerthaHit(Weapons.BERTHA_HIT, level);
		hit.setOwner(p);
		double yaw = Math.toRadians(p.getYHeadRot());
		hit.snapTo(p.getX() - 2.0 * Math.sin(yaw), p.getY() + 1.55, p.getZ() + 2.0 * Math.cos(yaw), p.getYHeadRot(), p.getXRot());
		Vec3 dir = Vec3.directionFromRotation(p.getXRot(), p.getYRot());
		hit.shoot(dir.x, dir.y, dir.z, 1.5F, 0.1F);
		hit.setDeltaMovement(hit.getDeltaMovement().scale(2.0));
		hit.hitType = hitType;
		level.addFreshEntity(hit);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		if (!(level() instanceof ServerLevel level) || !(getOwner() instanceof Player owner)) return;
		Entity e = result.getEntity();
		if (WeaponHooks.pvpProtected(e) || e == owner) return;
		double reach = switch (hitType) {
			case ROYAL -> 101.0;
			case HAMMY -> 64.0;
			default -> 81.0;
		};
		if (distanceToSqr(owner) >= reach) return;
		float damage = hitType == ROYAL ? 746.0F : hitType == HAMMY ? 82.0F : 496.0F;
		e.hurtServer(level, damageSources().playerAttack(owner), damage);
		if (hitType == BERTHA) e.igniteForSeconds(10.0F);
		double ks = hitType == ROYAL ? 1.5 : hitType == HAMMY ? 1.25 : 2.25;
		double inAir = hitType == ROYAL ? 0.25 : hitType == HAMMY ? 0.65 : 0.35;
		if (!e.isAlive()) inAir *= 2.0;
		double angle = Math.atan2(e.getZ() - owner.getZ(), e.getX() - owner.getX());
		e.push(Math.cos(angle) * ks, inAir, Math.sin(angle) * ks);
		if (hitType == HAMMY) level.explode(null, getX(), getY(), getZ(), 1.5F, true, Level.ExplosionInteraction.MOB);
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		super.onHitBlock(result);
		if (level() instanceof ServerLevel level && hitType == HAMMY && getOwner() != null && distanceToSqr(getOwner()) < 64.0) {
			level.explode(null, getX(), getY(), getZ(), 2.1F, true, Level.ExplosionInteraction.MOB);
		}
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		if (!level().isClientSide()) discard();
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("HitType", hitType);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		hitType = input.getIntOr("HitType", BERTHA);
	}
}
