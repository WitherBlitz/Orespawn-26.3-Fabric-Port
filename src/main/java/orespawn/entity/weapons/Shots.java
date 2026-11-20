package orespawn.entity.weapons;

import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Thrown projectiles the way 1.7.10's EntityThrowable(world, thrower) aimed them. */
public final class Shots {
	private Shots() {
	}

	/**
	 * A projectile at the thrower's eyes (0.16 to the side and 0.1 lower, as EntityThrowable did), aimed where it looks,
	 * at {@code speed} blocks a tick (EntityThrowable's 1.5 unless a weapon changes it). Not added to the level yet.
	 */
	public static <T extends Projectile> T aimed(EntityType<T> type, Level level, LivingEntity owner, float speed, float uncertainty) {
		T p = type.create(level, EntitySpawnReason.TRIGGERED);
		if (p == null) throw new IllegalStateException("Can't create " + type);
		double yaw = Math.toRadians(owner.getYRot());
		p.snapTo(owner.getX() - Math.cos(yaw) * 0.16, owner.getEyeY() - 0.1, owner.getZ() - Math.sin(yaw) * 0.16, owner.getYRot(), owner.getXRot());
		p.setOwner(owner);
		Vec3 d = Vec3.directionFromRotation(owner.getXRot(), owner.getYRot());
		p.shoot(d.x, d.y, d.z, speed, uncertainty);
		return p;
	}

	/** ItemRayGun / ItemThunderStaff: moved to 1 block right of the player's head (45 degrees round), 1.55 up, and 3 times as fast. */
	public static void fromGunBarrel(Projectile p, LivingEntity owner) {
		double yaw = Math.toRadians(owner.getYHeadRot() + 45.0F);
		p.snapTo(owner.getX() - Math.sin(yaw), owner.getY() + 1.55, owner.getZ() + Math.cos(yaw), owner.getYHeadRot(), owner.getXRot());
		p.setDeltaMovement(p.getDeltaMovement().scale(3.0));
	}
}
