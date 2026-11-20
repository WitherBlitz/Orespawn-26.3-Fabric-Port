package orespawn.entity.robots;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import orespawn.registry.ModSounds;

/**
 * What OreSpawn's Robot Spider and Robot Red Ant share (their classes were near copies): a walking machine whose body
 * floats on its legs (it rises whenever there's ground close below and sinks slowly otherwise), that a player rides by
 * clicking it with an empty hand or anything but an iron ingot, and that an iron ingot repairs (up to 100 health).
 * <p>
 * Ridden, it stands taller, faces where the rider looks, speeds up with the forward key and backs up with the back key
 * (OreSpawn's top speeds, both doubled by moving twice a tick as it did), climbs where the ground ahead rises, and
 * attacks on its own: it stamps on things a short way off and bites whatever is in front of it. Unridden it only looks
 * about. It takes no damage from walls, cactus, fire, magic or hunger, never falls, and never despawns.
 * <p>
 * OreSpawn turned the robot by an over-damped amount each tick while its client smoothed it to the rider's facing;
 * here it simply faces the rider's way. Riders sit (26.3 always draws riders seated).
 */
public abstract class LeggedRobot extends Mob {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(LeggedRobot.class, EntityDataSerializers.BOOLEAN);
	/** The legs' state, client side only. */
	public final RobotLegs legs;
	protected int rideTicker;
	private int playing;

	protected LeggedRobot(EntityType<? extends LeggedRobot> type, Level level, RobotLegs.Config legConfig) {
		super(type, level);
		this.legs = new RobotLegs(legConfig);
		setPersistenceRequired();
	}

	// ---- What differs between the spider and the ant ----

	/** The body's height above the ground unridden (gh) and ridden. */
	protected abstract double hoverHeight();

	protected abstract double riddenHoverHeight();

	/** Top speed ridden forward (backwards is always 0.25). */
	protected abstract double maxSpeed();

	/** The ground-ahead scan: how far out (times its depth) and how much each block lifts. */
	protected abstract int scanReach();

	protected abstract double scanLift();

	/** getMountedYOffset and the rider's distance behind the middle. */
	protected abstract double mountedHeight(Entity rider);

	protected abstract float riderBehind();

	protected abstract float rideSoundVolume();

	protected abstract float mountSoundVolume();

	/** Whether this player may ride it (the ant only when it's from a kit). */
	protected boolean canBeRiddenBy(Player player) {
		return true;
	}

	/** onUpdate's attacks while someone rides it, server side. */
	protected abstract void riddenAttacks(ServerLevel level);

	/** onUpdate's exhaust particles, client side. */
	protected abstract void exhaust();

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new LookAtPlayerGoal(this, Player.class, 12.0F) {
			@Override
			public boolean canUse() {
				return !isVehicle() && super.canUse();
			}
		});
		goalSelector.addGoal(1, new RandomLookAroundGoal(this) {
			@Override
			public boolean canUse() {
				return !isVehicle() && super.canUse();
			}
		});
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
	}

	public boolean attacking() {
		return entityData.get(ATTACKING);
	}

	protected void setAttacking(boolean b) {
		entityData.set(ATTACKING, b);
	}

	/** Used by the Spider Driver (and the ant's own AI) to steer it. */
	public void goThisWay(double mx, double mz) {
		Vec3 m = getDeltaMovement();
		setDeltaMovement(mx, m.y, mz);
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, net.minecraft.core.BlockPos pos) {
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE)
			|| source.is(DamageTypes.MAGIC) || source.is(DamageTypes.STARVE)) {
			return false;
		}
		return super.hurtServer(level, source, amount);
	}

	@Override
	public LivingEntity getControllingPassenger() {
		return getFirstPassenger() instanceof Player p ? p : null;
	}

	private boolean playerDriven() {
		return getControllingPassenger() != null;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (!canBeRiddenBy(player)) return InteractionResult.SUCCESS;
		ItemStack stack = player.getItemInHand(hand);
		if (stack.is(Items.IRON_INGOT) && player.distanceToSqr(this) < 25.0) {
			if (!level().isClientSide()) {
				float f = Math.min(getMaxHealth() - getHealth(), 100.0F);
				if (f > 0.0F) heal(f);
			}
			stack.consume(1, player);
			return InteractionResult.SUCCESS;
		}
		if (getFirstPassenger() instanceof Player p && p != player) return InteractionResult.SUCCESS;
		if (!level().isClientSide() && !isVehicle() && player.distanceToSqr(this) < 16.0) {
			player.setYRot(getYRot());
			player.startRiding(this);
			level().playSound(null, getX(), getY(), getZ(), ModSounds.get("robotspidermount"), SoundSource.NEUTRAL, mountSoundVolume(), 1.0F);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void positionRider(Entity passenger, MoveFunction move) {
		float f = riderBehind() + (float) Math.cos(rideTicker * 0.33F) * 0.05F;
		double y = getY() + mountedHeight(passenger) - (passenger instanceof Player ? 0.5 : 0.0);
		Vec3 off = passenger.getVehicleAttachmentPoint(this);
		move.accept(passenger, getX() - f * Math.sin(Math.toRadians(getYRot())) - off.x, y - off.y, getZ() + f * Math.cos(Math.toRadians(getYRot())) - off.z);
	}

	@Override
	protected void tickRidden(Player controller, Vec3 riddenInput) {
		super.tickRidden(controller, riddenInput);
		setRot(controller.getYRot(), 0.0F);
		yRotO = yBodyRot = yHeadRot = getYRot();
	}

	@Override
	public void tick() {
		super.tick();
		if (level() instanceof ServerLevel server && isVehicle() && level().getDifficulty() != Difficulty.PEACEFUL) riddenAttacks(server);
		if (level().isClientSide()) {
			exhaust();
			legs.update(this);
		}
	}

	@Override
	public void aiStep() {
		if (!isAlive()) {
			super.aiStep();
			return;
		}
		// OreSpawn ran no AI and no ordinary movement while something other than a player rode it (a Spider Driver).
		if (level().isClientSide() || !isVehicle() || playerDriven()) super.aiStep();
		rideTicker += random.nextInt(3);
		if (playing > 0) playing--;
		if (!level().isClientSide()) {
			if (isVehicle() && playing == 0 && random.nextInt(80) == 1) {
				level().playSound(null, getX(), getY(), getZ(), ModSounds.get("robotspider"), SoundSource.NEUTRAL, rideSoundVolume(), 1.0F);
				playing = 125;
			}
			if (!playerDriven()) {
				clampMotion();
				hover(isVehicle());
				moveTwice();
			}
		}
	}

	@Override
	public void travel(Vec3 input) {
		if (!(getControllingPassenger() instanceof Player rider)) {
			super.travel(input);
			return;
		}
		Vec3 m = getDeltaMovement();
		double velocity = Math.sqrt(m.x * m.x + m.z * m.z);
		clampMotion();
		hover(true);
		// Climb where the ground ahead rises.
		double obstruction = 0.0;
		int reach = 3 + (int) (velocity * 6.0);
		for (int k = 1; k < reach; k++) {
			for (int i = 1; i < reach * scanReach(); i++) {
				for (int j = -90; j <= 90; j += 30) {
					double a = Math.toRadians(getYRot() + 90.0F + j);
					if (!RobotUtil.airOrLiquid(level().getBlockState(RobotUtil.floor(getX() + i * Math.cos(a), getY() - k, getZ() + i * Math.sin(a))))) {
						obstruction += scanLift();
					}
				}
			}
		}
		m = getDeltaMovement();
		setDeltaMovement(m.x, m.y + obstruction * 0.05, m.z);
		setPos(getX(), getY() + obstruction * 0.05, getZ());
		// Speed along its facing, from the rider's forward and back keys.
		m = getDeltaMovement();
		double v = Math.sqrt(m.x * m.x + m.z * m.z);
		double rdv = Math.abs(Math.atan2(m.z, m.x) - Math.toRadians((rider.getYRot() + 90.0F) % 360.0F)) % (Math.PI * 2.0);
		if (rdv > Math.PI) rdv -= Math.PI * 2.0;
		rdv = Math.abs(rdv);
		if (Math.abs(v) < 0.01) rdv = 0.0;
		if (rdv > 1.5) v = -v;
		float im = rider.zza;
		if (Math.abs(im) > 0.001F) {
			double max = maxSpeed();
			if (im > 0.0F) {
				v += 0.05;
			} else {
				max = 0.25;
				v -= 0.05;
			}
			v = v >= 0.0 ? Math.min(v, max) : Math.max(v, -max);
		}
		double dir = Math.toRadians(getYRot() + (v >= 0.0 ? 90.0F : 270.0F));
		setDeltaMovement(Math.cos(dir) * Math.abs(v), m.y, Math.sin(dir) * Math.abs(v));
		move(MoverType.SELF, getDeltaMovement());
		setDeltaMovement(getDeltaMovement().scale(0.98));
		moveTwice();
	}

	private void clampMotion() {
		Vec3 m = getDeltaMovement();
		setDeltaMovement(Math.clamp(m.x, -1.25, 1.25), Math.clamp(m.y, -0.85, 0.85), Math.clamp(m.z, -1.25, 1.25));
	}

	/** The body floats: up while there's ground within its height below, slowly down otherwise. */
	private void hover(boolean ridden) {
		Vec3 m = getDeltaMovement();
		if (ridden) {
			if (!RobotUtil.airOrLiquid(level().getBlockState(RobotUtil.floor(getX(), getY() - riddenHoverHeight(), getZ())))) {
				setDeltaMovement(m.x, m.y + 0.06, m.z);
				setPos(getX(), getY() + 0.03, getZ());
			} else {
				setDeltaMovement(m.x, m.y - 0.02, m.z);
			}
			return;
		}
		double gh = hoverHeight();
		BlockState s = level().getBlockState(RobotUtil.floor(getX(), (float) getY() - (float) gh + 1.0F, getZ()));
		if (s.isAir()) s = level().getBlockState(RobotUtil.floor(getX(), (float) getY() - (float) gh, getZ()));
		if (!RobotUtil.airOrLiquid(s)) {
			setDeltaMovement(m.x, m.y + 0.15, m.z);
			setPos(getX(), getY() + 0.15, getZ());
		} else {
			setDeltaMovement(m.x, m.y - 0.002, m.z);
		}
	}

	/** onLivingUpdate's own extra move each tick, with its drag. */
	private void moveTwice() {
		move(MoverType.SELF, getDeltaMovement());
		Vec3 m = getDeltaMovement();
		setDeltaMovement(m.x * 0.8, m.y * 0.98, m.z * 0.8);
	}

	/** feetattackEntityAsMob and attackEntityAsMob: damage, then a shove away (twice as high for players). */
	protected boolean strike(ServerLevel level, LivingEntity target, float damage, double ks, double inair) {
		boolean ret = target.hurtServer(level, damageSources().mobAttack(this), damage);
		if (ret) RobotUtil.knockAway(this, target, ks, inair);
		return ret;
	}

	/** Exhaust from a jet {@code f} blocks out at {@code angle} off its back, blowing along {@code blow}. */
	protected void jet(float f, float angle, float blow, double up) {
		var r = random;
		float dx = (float) (f * Math.cos(Math.toRadians(getYRot() - angle)));
		float dz = (float) (f * Math.sin(Math.toRadians(getYRot() - angle)));
		float bx = (float) (f * Math.cos(Math.toRadians(getYRot() - blow)));
		float bz = (float) (f * Math.sin(Math.toRadians(getYRot() - blow)));
		double x = getX() + dx, y = getY() + up, z = getZ() + dz;
		if (r.nextInt(jetRate(0)) == 0) {
			level().addParticle(ParticleTypes.FLAME, x, y, z, bx / f + (r.nextFloat() - r.nextFloat()) / 20.0F, (r.nextFloat() - r.nextFloat()) / 10.0F,
				bz / f + (r.nextFloat() - r.nextFloat()) / 20.0F);
		}
		if (r.nextInt(jetRate(1)) == 0) {
			level().addParticle(ParticleTypes.SMOKE, x, y, z, bx / f + (r.nextFloat() - r.nextFloat()) / 20.0F, (r.nextFloat() - r.nextFloat()) / 10.0F,
				bz / f + (r.nextFloat() - r.nextFloat()) / 20.0F);
		}
		if (r.nextInt(jetRate(2)) == 0) {
			level().addParticle(ParticleTypes.FIREWORK, x, y, z, bx / f + (r.nextFloat() - r.nextFloat()) / 20.0F, (r.nextFloat() - r.nextFloat()) / 5.0F,
				bz / f + (r.nextFloat() - r.nextFloat()) / 20.0F);
		}
	}

	/** 1 in this many ticks for the flame (0), smoke (1) and spark (2) of each jet. */
	protected abstract int jetRate(int kind);
}
