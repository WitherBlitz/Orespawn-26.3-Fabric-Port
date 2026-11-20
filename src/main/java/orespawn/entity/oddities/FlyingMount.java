package orespawn.entity.oddities;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * What OreSpawn's two big rideable fliers (the Dragon and the Leonopteryx) share: while flying (activity not 0) they
 * leave walking physics alone and just move by their velocity, and their owner can ride them. A rider steers with
 * where they look: the mount swings round after them, forward speeds it up to its top speed, back slows it (to 0.35
 * backwards), the jump key climbs (OreSpawn's own fly-up key), and it lifts itself over ground and anything below
 * ahead of it.
 * <p>
 * In 26.3 the rider's game moves a ridden mount, so the riding flight runs there; the rest runs on the server.
 */
public abstract class FlyingMount extends TamableAnimal {
	private float deltaSmooth;
	/** The neck's swing while flying, smoothed per frame by the renderer (OreSpawn's RenderInfo.rf1). */
	public float renderNeck;

	protected FlyingMount(EntityType<? extends FlyingMount> type, Level level) {
		super(type, level);
	}

	/** Activity 0 walks, anything else flies. */
	public abstract int activity();

	protected abstract void setActivity(int a);

	/** How high above its feet OreSpawn seated the rider (getMountedYOffset). */
	protected abstract double riderHeight();

	/**
	 * Riding numbers: the ground check depth, top speed, forward acceleration (OreSpawn's deltav, with its bonus for top
	 * speeds over 1), and the climb per tick and per unit of speed.
	 */
	protected abstract double groundCheck();

	protected abstract double maxSpeed();

	protected abstract double accel();

	protected abstract double climb();

	protected abstract double climbPerSpeed();

	@Override
	public @Nullable LivingEntity getControllingPassenger() {
		return isTame() && getFirstPassenger() instanceof Player p ? p : super.getControllingPassenger();
	}

	/** updateRiderPosition: 0.65 forward, at the mounted height (26.3 seats the player 0.6 lower than the point). */
	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
		return new Vec3(0.0, riderHeight() + 0.1, 0.65);
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public void travel(Vec3 input) {
		if (getControllingPassenger() instanceof Player rider) {
			flyRidden(rider);
		} else if (activity() != 0) {
			move(MoverType.SELF, getDeltaMovement());
		} else {
			super.travel(input);
		}
	}

	@Override
	protected Vec3 getRiddenInput(Player controller, Vec3 selfInput) {
		return Vec3.ZERO;
	}

	/** How much is solid below and ahead of it, out to a distance that grows with speed (its obstruction_factor). */
	protected double obstruction(double velocity, int base, double perSpeed) {
		double factor = 0.0;
		int dist = base + (int) (velocity * perSpeed);
		double c = Math.cos(Math.toRadians(getYRot() + 90.0F)), s = Math.sin(Math.toRadians(getYRot() + 90.0F));
		for (int k = 1; k < dist; k++) {
			for (int i = 1; i < dist * 2; i++) {
				if (!level().isEmptyBlock(BlockPos.containing(getX() + i * c, getY() - k, getZ() + i * s))) factor += 0.05;
			}
		}
		return factor;
	}

	/** The ridden half of OreSpawn's onLivingUpdate. */
	private void flyRidden(Player rider) {
		Vec3 m = getDeltaMovement();
		double mx = Mth.clamp(m.x, -2.0, 2.0), my = m.y, mz = Mth.clamp(m.z, -2.0, 2.0);
		double velocity = Math.sqrt(mx * mx + mz * mz);
		if (!level().isEmptyBlock(BlockPos.containing(getX(), (float) getY() - (float) groundCheck(), getZ()))) {
			my += 0.03;
			setPos(getX(), getY() + 0.1, getZ());
		} else {
			my -= 0.018;
		}
		double obstruction = obstruction(velocity, 3, 7.0);
		my += obstruction * 0.07;
		setPos(getX(), getY() + obstruction * 0.07, getZ());
		if (my > 2.0) my = 2.0;

		// Swing round after the rider, more slowly the faster it goes.
		double riderYaw = ((rider.getYRot() % 360.0) + 360.0) % 360.0;
		double ownYaw = ((getYRot() % 360.0) + 360.0) % 360.0;
		double rel = ((riderYaw - ownYaw) % 180.0 + 180.0) % 180.0;
		if (rel > 90.0) rel -= 180.0;
		float yaw;
		if (velocity > 0.01) {
			double lag = Mth.clamp(Math.abs(1.85 - velocity), 0.01, 0.9);
			yaw = rider.getYRot() + (float) (rel * lag);
		} else {
			yaw = rider.getYRot();
		}
		setYRot(yaw);
		setXRot(2.0F * (float) velocity);
		yBodyRot = yaw;
		yHeadRot = yaw;

		double newVelocity = velocity;
		double rhm = Math.atan2(mz, mx);
		double rhdir = Math.toRadians((rider.getYRot() + 90.0F) % 360.0F);
		double rdv = Math.abs(rhm - rhdir) % (Math.PI * 2.0);
		if (rdv > Math.PI) rdv -= Math.PI * 2.0;
		rdv = Math.abs(rdv);
		if (Math.abs(newVelocity) < 0.01) rdv = 0.0;
		if (rdv > 1.5) newVelocity = -newVelocity;
		if (rider.isJumping()) my += climb() + velocity * climbPerSpeed();
		float im = rider.zza;
		double max = maxSpeed();
		if (Math.abs(im) > 0.001F) {
			double dv;
			if (im > 0.0F) {
				dv = accel();
				if (deltaSmooth < 0.0F) deltaSmooth = 0.0F;
				deltaSmooth = (float) Math.min(deltaSmooth + dv / 10.0, dv);
			} else {
				max = 0.35;
				dv = -0.02;
				if (deltaSmooth > 0.0F) deltaSmooth = 0.0F;
				deltaSmooth = (float) Math.max(deltaSmooth + dv / 10.0, dv);
			}
			newVelocity += deltaSmooth;
			newVelocity = Mth.clamp(newVelocity, -max, max);
		}
		double dir = Math.toRadians(getYRot() + (newVelocity >= 0.0 ? 90.0F : 270.0F));
		mx = Math.cos(dir) * Math.abs(newVelocity);
		mz = Math.sin(dir) * Math.abs(newVelocity);
		Vec3 motion = new Vec3(mx, my, mz);
		move(MoverType.SELF, motion);
		setDeltaMovement(mx * 0.985, my * 0.94, mz * 0.985);
	}
}
