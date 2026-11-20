package orespawn.entity.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LinearInterpolationHandler;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import orespawn.OreSpawn;
import orespawn.content.Tools;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;

import java.util.List;

/**
 * OreSpawn's Hoverboard (class {@code Elevator}, entity "Hoverboard"): a board that floats about a block above whatever
 * is under it (water and plants included). Forward speeds it up in the way its rider looks (up to 0.85 blocks a tick),
 * back brakes and then reverses slowly (up to 0.35); it swings round to its rider's facing, overshooting and settling
 * (hardly at all at full boost), and rises over the ground ahead. A crash into a wall faster than 0.75 wrecks it into 6-15 sticks
 * and 2 diamonds. Riding over grass sometimes scuffs it to dirt or flattens tall grass (mob griefing). Now and then, at
 * speed, it sputters for a couple of seconds, slowing down with bangs and smoke. Ten colours, cycled by using The
 * Ultimate Sword on it. Players break it as they would a boat (a creative player at once); while it's ridden nothing else
 * can hurt it.
 * <p>
 * The server moves it, as OreSpawn did, from the rider's facing and forward/back keys; riders' clients follow it.
 * OreSpawn's "UP/FAST" key (left Alt), which raised its top speed to 1.85, is the sprint key here, since the shared rider
 * key isn't ported. Not ported: the standing rider pose (26.3 draws riders seated); the rider sits on the board instead.
 */
public class Elevator extends VehicleEntity {
	private static final EntityDataAccessor<Integer> EXPLODING = SynchedEntityData.defineId(Elevator.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(Elevator.class, EntityDataSerializers.INT);
	private int exploding;
	private int playing;

	public Elevator(EntityType<? extends Elevator> type, Level level) {
		super(type, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(EXPLODING, 0);
		builder.define(COLOR, 1);
	}

	public int getColor() {
		return entityData.get(COLOR);
	}

	public int getExploding() {
		return entityData.get(EXPLODING);
	}

	@Override
	protected Item getDropItem() {
		return Tools.HOVERBOARD;
	}

	@Override
	public ItemStack getPickResult() {
		return new ItemStack(Tools.HOVERBOARD);
	}

	@Override
	protected InterpolationHandler createInterpolationHandler() {
		return LinearInterpolationHandler.create(this, 3);
	}

	@Override
	public boolean isPickable() {
		return !isRemoved();
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return getPassengers().isEmpty();
	}

	/** The rider sits on the board (OreSpawn's rider stood with its feet on it). */
	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
		return Vec3.ZERO;
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (isVehicle() && !(source.getEntity() instanceof Player)) return false;
		if (source.is(DamageTypes.IN_WALL)) return false;
		return super.hurtServer(level, source, damage);
	}

	@Override
	public void animateHurt(float yaw) {
		setHurtDir(-getHurtDir());
		setHurtTime(10);
		setDamage(getDamage() * 11.0F);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
		ItemStack held = player.getItemInHand(hand);
		if (held.is(ModItems.byName("ultimate_sword")) && player.distanceToSqr(this) < 16.0) {
			if (!level().isClientSide()) entityData.set(COLOR, getColor() >= 10 ? 1 : getColor() + 1);
			return InteractionResult.SUCCESS;
		}
		if (getFirstPassenger() instanceof Player rider && rider != player) return InteractionResult.SUCCESS;
		if (!level().isClientSide()) player.startRiding(this);
		return InteractionResult.SUCCESS;
	}

	@Override
	public void tick() {
		super.tick();
		if (getHurtTime() > 0) setHurtTime(getHurtTime() - 1);
		if (getDamage() > 0.0F) setDamage(getDamage() - 1.0F);
		Entity rider = getFirstPassenger();
		Vec3 m = getDeltaMovement();
		double velocity = Math.sqrt(m.x * m.x + m.z * m.z);
		if (level().isClientSide()) {
			if (velocity > 0.15 && rider != null) trail(velocity);
			if (getExploding() > 0 && rider != null) sputter();
			return;
		}
		ServerLevel level = (ServerLevel) level();
		if (playing > 0) playing--;
		if (rider != null && playing == 0 && random.nextInt(80) == 1) {
			level.playSound(null, rider.getX(), rider.getY(), rider.getZ(), ModSounds.get("hover"), SoundSource.NEUTRAL, 0.45F, 1.0F);
			playing = 55;
		}
		if (exploding > 0) exploding--;
		if (exploding == 0 && velocity > 0.65 && random.nextInt(20000) == 1) {
			exploding = 45;
			playing = 50;
		}
		entityData.set(EXPLODING, exploding);
		if (exploding > 0 && rider != null && random.nextInt(10) == 1) {
			level.playSound(null, rider.getX(), rider.getY(), rider.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 0.55F,
				0.75F + random.nextFloat());
		}
		hover(level, rider != null);
		if (rider instanceof Player player) {
			steer(player, velocity);
		} else {
			setDeltaMovement(0.0, getDeltaMovement().y, 0.0);
		}
		move(MoverType.SELF, getDeltaMovement());
		if (horizontalCollision && velocity > 0.75) {
			discard();
			int sticks = 6 + random.nextInt(10);
			for (int i = 0; i < sticks; i++) spawnAtLocation(level, new ItemStack(Items.STICK));
			for (int i = 0; i < 2; i++) spawnAtLocation(level, new ItemStack(Items.DIAMOND));
			return;
		}
		setDeltaMovement(getDeltaMovement().multiply(0.98, 0.94, 0.98));
		List<Entity> near = level.getEntities(this, getBoundingBox().inflate(0.25, 0.0, 0.25));
		for (Entity e : near) {
			if (e != rider && e.isPushable() && !isGirlfriendOrBoyfriend(e)) e.push(this);
		}
	}

	/** Floats 0.75 above the ground (1.25 when ridden): anything not air below pushes it up, otherwise it sinks slowly. */
	private void hover(ServerLevel level, boolean ridden) {
		double gh = ridden ? 1.25 : 0.75;
		BlockPos below = BlockPos.containing(getX(), getY() - gh, getZ());
		BlockState state = level.getBlockState(below);
		if (!state.isAir()) {
			setDeltaMovement(getDeltaMovement().add(0.0, 0.06, 0.0));
			setPos(getX(), getY() + 0.1, getZ());
			if (ridden && level.getGameRules().get(GameRules.MOB_GRIEFING)) {
				BlockPos now = BlockPos.containing(getX(), getY() - gh, getZ());
				if ((state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN)) && random.nextInt(200) == 1) {
					level.setBlockAndUpdate(now, Blocks.AIR.defaultBlockState());
				}
				if (state.is(Blocks.GRASS_BLOCK) && random.nextInt(200) == 1) level.setBlockAndUpdate(now, Blocks.DIRT.defaultBlockState());
			}
		} else {
			setDeltaMovement(getDeltaMovement().add(0.0, -0.01, 0.0));
		}
	}

	/** OreSpawn's riding code: lift over what's ahead, turn after the rider, then speed up, brake or coast. */
	private void steer(Player rider, double velocity) {
		double obstruction = 0.0;
		int reach = 3 + (int) (velocity * 8.0);
		for (int k = 1; k < reach; k++) {
			for (int i = 1; i < reach * 2; i++) {
				double dx = i * Math.cos(Math.toRadians(getYRot() + 90.0F));
				double dz = i * Math.sin(Math.toRadians(getYRot() + 90.0F));
				if (!level().getBlockState(BlockPos.containing(getX() + dx, Math.floor(getY()) - k, getZ() + dz)).isAir()) obstruction += 0.05;
			}
		}
		setDeltaMovement(getDeltaMovement().add(0.0, obstruction * 0.11, 0.0));
		setPos(getX(), getY() + obstruction * 0.11, getZ());

		double riderYaw = rider.getYRot() % 360.0;
		while (riderYaw < 0.0) riderYaw += 360.0;
		double yaw = getYRot() % 360.0;
		while (yaw < 0.0) yaw += 360.0;
		double relative = (riderYaw - yaw) % 180.0;
		while (relative < 0.0) relative += 180.0;
		if (relative > 90.0) relative -= 180.0;
		float newYaw;
		if (velocity > 0.01) {
			double lag = Math.abs(1.85 - velocity);
			if (lag < 0.01) lag = 0.01;
			if (lag > 0.9) lag = 0.9;
			newYaw = rider.getYRot() + (float) (relative * lag);
		} else {
			newYaw = rider.getYRot();
		}
		setRot(newYaw, 10.0F * (float) velocity);

		Vec3 m = getDeltaMovement();
		double speed = Math.sqrt(m.x * m.x + m.z * m.z);
		if (exploding != 0) speed = Math.max(0.0, speed - 0.05);
		double heading = Math.atan2(m.z, m.x);
		double facing = Math.toRadians((rider.getYRot() + 90.0F) % 360.0F);
		Input input = rider instanceof ServerPlayer sp ? sp.getLastClientInput() : Input.EMPTY;
		float im = input.forward() == input.backward() ? 0.0F : input.forward() ? 1.0F : -1.0F;
		double maxSpeed = input.sprint() ? 1.85 : 0.85;
		double off = Math.abs(heading - facing) % (Math.PI * 2.0);
		if (off > Math.PI) off -= Math.PI * 2.0;
		off = Math.abs(off);
		if (Math.abs(speed) < 0.01) off = 0.0;
		if (off > 1.5) speed = -speed;
		if (Math.abs(im) > 0.001F) {
			double delta;
			if (im > 0.0F) {
				delta = 0.025;
				if (maxSpeed > 1.0) delta += 0.15;
			} else {
				maxSpeed = 0.35;
				delta = -0.02;
			}
			speed += delta;
			if (speed >= 0.0) {
				speed = Math.min(speed, maxSpeed);
			} else {
				speed = Math.max(speed, -maxSpeed);
			}
		}
		double dir = Math.toRadians(getYRot() + (speed >= 0.0 ? 90.0F : 270.0F));
		double s = Math.abs(speed);
		setDeltaMovement(Math.cos(dir) * s, getDeltaMovement().y, Math.sin(dir) * s);
	}

	/** Smoke and red dust streaming from the board's ends, and spray over water. */
	private void trail(double velocity) {
		double c = Math.cos(Math.toRadians(getYRot() + 270.0F));
		double sn = Math.sin(Math.toRadians(getYRot() + 270.0F));
		int below;
		BlockState ground = Blocks.AIR.defaultBlockState();
		for (below = 1; below < 10; below++) {
			ground = level().getBlockState(BlockPos.containing(getX(), Math.floor(getY()) - below, getZ()));
			if (!ground.isAir()) break;
		}
		Vec3 m = getDeltaMovement();
		for (int j = 0; j < 1.0 + velocity * 10.0; j++) {
			double side = random.nextFloat() * 2.0F - 1.0F;
			double end = (random.nextInt(2) * 2 - 1) * 0.7;
			ParticleOptions particle = random.nextBoolean() ? ParticleTypes.SMOKE : DustParticleOptions.REDSTONE;
			if (random.nextBoolean()) {
				level().addParticle(particle, getX() - c * side * 0.8 + sn * end, getY() - 0.25, getZ() - sn * side * 0.8 - c * end, m.x, m.y, m.z);
			} else {
				level().addParticle(particle, getX() + c + sn * side * 0.7, getY() - 0.225, getZ() + sn - c * side * 0.7, m.x, m.y, m.z);
			}
			if (ground.is(Blocks.WATER)) {
				for (int k = 0; k < 5; k++) {
					level().addParticle(ParticleTypes.SPLASH, getX() + random.nextFloat(), Math.floor(getY()) - below + 1.25, getZ() + random.nextFloat(),
						m.x / 2.0, m.y + velocity, m.z / 2.0);
				}
			}
		}
	}

	/** The sputter's bangs and smoke, at whole-block spots around the board. */
	private void sputter() {
		Vec3 m = getDeltaMovement();
		for (int i = 0; i < 15; i++) {
			level().addParticle(ParticleTypes.POOF, around(getX(), 4.0F), around(getY(), 4.0F), around(getZ(), 4.0F), m.x, 0.0, m.z);
			level().addParticle(ParticleTypes.EXPLOSION, around(getX(), 2.0F), around(getY(), 2.0F), around(getZ(), 2.0F), m.x, 0.0, m.z);
			level().addParticle(ParticleTypes.SMOKE, around(getX(), 5.0F), around(getY(), 5.0F), around(getZ(), 5.0F), m.x, 0.0, m.z);
			level().addParticle(ParticleTypes.LARGE_SMOKE, around(getX(), 3.0F), around(getY(), 3.0F), around(getZ(), 3.0F), m.x, 0.0, m.z);
		}
	}

	private int around(double v, float spread) {
		return (int) (v + (random.nextFloat() - random.nextFloat()) * spread);
	}

	private static boolean isGirlfriendOrBoyfriend(Entity e) {
		var id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
		return id.equals(OreSpawn.id("girlfriend")) || id.equals(OreSpawn.id("boyfriend"));
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.putInt("HoverColor", getColor());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		entityData.set(COLOR, Mth.clamp(input.getIntOr("HoverColor", 1), 1, 10));
	}
}
