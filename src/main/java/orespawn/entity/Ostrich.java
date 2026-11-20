package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's ostrich: a fast, tameable mount. Any player with an empty main hand (within 4 blocks) climbs on, tamed or
 * not, and steers it by looking: forward speeds it up to 0.75 blocks a tick, back slows it and backs it up (at most 0.25),
 * and there's no strafing. It runs its own physics while ridden, as OreSpawn's did: everything that isn't air in a patch
 * ahead of it (wider and taller the faster it goes, starting at ground level) lifts it, so it climbs over things and
 * bounds higher the faster it runs; and the jump key (OreSpawn's "UP/FAST" key, Left Alt; vanilla's jump key here) throws
 * it up by 1 plus 6 times its speed, once per press, then again only after the key has been let go for 20 ticks (in the
 * air too). Neither it nor its rider takes fall damage from it. Feed it an apple: half the time a wild one is tamed and
 * healed; a tamed one is healed. Click your own with any item to make it sit (only on sand, gravel, dirt, farmland or a
 * grass block; it buries its head) or stand; give it a dead bush to set it free. Immune to cactus. Wild ones drop 0-2
 * feathers (loot table), tamed ones 2-6 poppies.
 *
 * <p>Riding is done the 26.3 way (getControllingPassenger, getRiddenInput, tickRidden, and travel while ridden), on the
 * rider's client. The way it runs follows OreSpawn's server yaw (which overshoots the rider's look a little when turning),
 * while it's drawn turning a tenth of the way to the rider's look each tick, as OreSpawn's client drew it. OreSpawn's
 * attackEntityFrom always returned false (arrows bounced off, hits gave no knockback though they did damage); this one
 * returns the real result. Not ported yet: OreSpawn's "cannon fodder" hats (carrot, potato or quinoa activation, corn cob
 * cloning, the guard behavior and its two other textures),
 * and EntityAIMoveIndoors (villager doors, which 26.3 doesn't have).
 */
public class Ostrich extends TamableAnimal {
	/** ClientProxyOreSpawn's model speed (new ModelOstrich(0.65F)); the wing beat runs at it. */
	private static final float WING_SPEED = 0.65F;
	/** OreSpawn's server rotationYaw while ridden: the way it runs. */
	private float rideYaw;
	/** OreSpawn's deltasmooth: the throttle, eased in a tenth at a time. */
	private float deltaSmooth;
	/** OreSpawn's didjump: ticks left before the jump key works again. */
	private int jumpCooldown;
	/** Client only: OreSpawn's RenderInfo.rf1, the head's smoothed turn while ridden. */
	private float headTurn;
	/** Client only: OreSpawn's RenderInfo.ri1, whether the wings flap this beat. */
	private boolean flapping;

	public Ostrich(EntityType<? extends Ostrich> type, Level level) {
		super(type, level);
		this.xpReward = 10;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return TamableAnimal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 25.0).add(Attributes.MOVEMENT_SPEED, 0.38)
			.add(Attributes.ATTACK_DAMAGE, 6.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
		goalSelector.addGoal(1, new BreedGoal(this, 1.0));
		goalSelector.addGoal(2, new FollowOwnerGoal(this, 2.0, 10.0F, 2.0F));
		goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Monster.class, 8.0F, 1.0, 1.9F));
		goalSelector.addGoal(4, new TemptGoal(this, 1.2F, s -> s.is(Items.APPLE), false));
		goalSelector.addGoal(5, new PanicGoal(this, 1.5));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		goalSelector.addGoal(7, new LookAtPlayerGoal(this, Mob.class, 5.0F));
		goalSelector.addGoal(8, WanderGoal.wander(this, 1.0));
		goalSelector.addGoal(9, new RandomLookAroundGoal(this));
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		if (random.nextInt(250) == 0) heal(1.0F);
	}

	/** OreSpawn ran no AI at all while someone rode it. */
	@Override
	protected boolean isImmobile() {
		return super.isImmobile() || getControllingPassenger() instanceof Player;
	}

	@Override
	public @Nullable LivingEntity getControllingPassenger() {
		return getFirstPassenger() instanceof Player player ? player : super.getControllingPassenger();
	}

	@Override
	protected void addPassenger(Entity passenger) {
		super.addPassenger(passenger);
		rideYaw = getYRot();
	}

	/** Ostrich.updateRiderPosition: 1.4 up for OreSpawn's player offset (the rider's feet 0.9 above its own), 0.15 back. */
	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
		return new Vec3(0.0, 1.5 * scale, -0.15 * scale).yRot(-getYRot() * Mth.DEG_TO_RAD);
	}

	/** Only forward and back: OreSpawn read the rider's moveForward and nothing else. */
	@Override
	protected Vec3 getRiddenInput(Player controller, Vec3 selfInput) {
		return new Vec3(0.0, 0.0, controller.zza);
	}

	@Override
	protected void tickRidden(Player controller, Vec3 riddenInput) {
		super.tickRidden(controller, riddenInput);
		if (isLocalInstanceAuthoritative()) {
			steer(controller, riddenInput.z);
			setYRot(getYRot() + Mth.wrapDegrees(controller.getYRot() - getYRot()) * 0.1F);
		}
		yBodyRot = yHeadRot = getYRot();
	}

	/** Ostrich.onLivingUpdate while ridden, up to the move: works out the motion travel() then makes. */
	private void steer(Player rider, double forward) {
		Vec3 motion = getDeltaMovement();
		double mx = Mth.clamp(motion.x, -2.0, 2.0);
		double my = motion.y;
		double mz = Mth.clamp(motion.z, -2.0, 2.0);
		double velocity = Math.sqrt(mx * mx + mz * mz);

		// Anything that isn't air in the patch ahead (from the block under it up) lifts it.
		int dist = 1 + (int) (velocity * 10.0);
		double aheadX = -Mth.sin(rideYaw * Mth.DEG_TO_RAD), aheadZ = Mth.cos(rideYaw * Mth.DEG_TO_RAD);
		double lift = 0.0;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int baseY = Mth.floor(getY()) - 1;
		for (int k = 0; k < dist; k++) {
			for (int i = 1; i < dist * 2; i++) {
				pos.set(Mth.floor(getX() + i * aheadX), baseY + k, Mth.floor(getZ() + i * aheadZ));
				if (!level().getBlockState(pos).isAir()) lift += 0.075;
			}
		}
		my += lift;
		if (my > 4.0) my = 4.0;

		// The way it runs swings toward the rider's look (OreSpawn's formula overshoots it a little).
		float riderYaw = rider.getYRot();
		double relative = (Mth.positiveModulo(riderYaw, 360.0F) - Mth.positiveModulo(rideYaw, 360.0F)) % 180.0;
		if (relative < 0.0) relative += 180.0;
		if (relative > 90.0) relative -= 180.0;
		if (velocity > 0.01) {
			rideYaw = riderYaw + (float) (relative * Mth.clamp(Math.abs(1.85 - velocity), 0.01, 0.9));
		} else {
			rideYaw = riderYaw;
		}
		setXRot(2.0F * (float) velocity);

		// Its speed, negative when it's moving away from where the rider looks.
		double speed = velocity;
		double moving = Math.atan2(mz, mx);
		double looking = Math.toRadians((riderYaw + 90.0F) % 360.0F);
		double off = Math.abs(moving - looking) % (Math.PI * 2.0);
		if (off > Math.PI) off -= Math.PI * 2.0;
		off = Math.abs(off);
		if (Math.abs(speed) < 0.01) off = 0.0;
		if (off > 1.5) speed = -speed;

		if (rider.isJumping()) {
			if (jumpCooldown == 0) {
				my += 1.0 + velocity * 6.0;
				jumpCooldown = 20;
			}
		} else if (jumpCooldown > 0) {
			jumpCooldown--;
		}

		if (Math.abs(forward) > 0.001) {
			double maxSpeed;
			if (forward > 0.0) {
				maxSpeed = 0.75;
				if (deltaSmooth < 0.0F) deltaSmooth = 0.0F;
				deltaSmooth = Math.min(deltaSmooth + 0.0045F, 0.045F);
			} else {
				maxSpeed = 0.25;
				if (deltaSmooth > 0.0F) deltaSmooth = 0.0F;
				deltaSmooth = Math.max(deltaSmooth - 0.003F, -0.03F);
			}
			speed = Mth.clamp(speed + deltaSmooth, -maxSpeed, maxSpeed);
		}
		double heading = Math.toRadians(rideYaw + 90.0F);
		setDeltaMovement(Math.cos(heading) * speed, my, Math.sin(heading) * speed);
	}

	/** Ridden, it moves by OreSpawn's own physics (no friction, its own gravity and drag); otherwise as usual. */
	@Override
	public void travel(Vec3 input) {
		if (getControllingPassenger() instanceof Player) {
			move(MoverType.SELF, getDeltaMovement());
			Vec3 motion = getDeltaMovement();
			setDeltaMovement(motion.x * 0.95, (motion.y - 0.25) * 0.85, motion.z * 0.95);
		} else {
			super.travel(input);
		}
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			if (isVehicle()) {
				float turn = Mth.wrapDegrees(getYRot() - yRotO) * 20.0F;
				// OreSpawn eased it 1/60 per frame; 1/20 per tick is the same at 60 frames a second.
				headTurn = Mth.clamp(headTurn + (turn - headTurn) / 20.0F, -50.0F, 50.0F);
			}
			// Each time the wings pass their rest point on the way up, a 1 in 3 chance they flap this beat.
			if (Mth.cos((tickCount - 1) * WING_SPEED) < 0.0F && Mth.cos(tickCount * WING_SPEED) >= 0.0F) {
				flapping = random.nextInt(3) == 1;
			}
		}
	}

	/** Horizontal distance moved this tick; OreSpawn's model swung the legs by it. */
	public float legSpeed() {
		return (float) Math.sqrt(Mth.square(getX() - xo) + Mth.square(getZ() - zo));
	}

	/** Whether the wings flap this beat (client side). */
	public boolean wingsFlapping() {
		return flapping;
	}

	/** The head's yaw for the model, in degrees: its smoothed turn while ridden, otherwise half the look. */
	public float headYaw(float netHeadYaw) {
		return isVehicle() ? headTurn : netHeadYaw / 2.0F;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypes.CACTUS)) return false;
		return super.hurtServer(level, source, damage);
	}

	@Override
	protected int getFireImmuneTicks() {
		return 100;
	}

	/** Where a tamed ostrich will sit: OreSpawn's sand, gravel, dirt, farmland and grass blocks. */
	private static boolean canSitOn(BlockState state) {
		return state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.GRAVEL) || state.is(Blocks.DIRT)
			|| state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL) || state.is(Blocks.FARMLAND) || state.is(Blocks.GRASS_BLOCK);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player.distanceToSqr(this) >= 16.0) return super.mobInteract(player, hand);
		if (stack.is(Items.APPLE)) {
			if (!level().isClientSide()) {
				if (!isTame()) {
					if (random.nextInt(2) == 0) {
						tame(player);
						heal(getMaxHealth() - getHealth());
						level().broadcastEntityEvent(this, (byte) 7);
					} else {
						level().broadcastEntityEvent(this, (byte) 6);
					}
				} else if (isOwnedBy(player)) {
					heal(getMaxHealth() - getHealth());
				}
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		if (isTame() && isOwnedBy(player) && stack.is(Items.DEAD_BUSH)) {
			if (!level().isClientSide()) {
				setTame(false, true);
				setOwner(null);
				setOrderedToSit(false);
				level().broadcastEntityEvent(this, (byte) 6);
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		InteractionResult result = super.mobInteract(player, hand);
		if (result.consumesAction()) return result;
		if (!stack.isEmpty() && isTame() && isOwnedBy(player) && !stack.is(Items.NAME_TAG)) {
			if (!level().isClientSide()) {
				if (isOrderedToSit()) {
					setOrderedToSit(false);
				} else if (canSitOn(level().getBlockState(blockPosition().below()))) {
					setOrderedToSit(true);
				}
			}
			return InteractionResult.SUCCESS;
		}
		if (stack.isEmpty() && hand == InteractionHand.MAIN_HAND) {
			if (!level().isClientSide()) {
				setOrderedToSit(false);
				setInSittingPose(false);
				player.startRiding(this);
			}
			return InteractionResult.SUCCESS;
		}
		return result;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("cryo_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("cryo_death");
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	public float getVoicePitch() {
		return (random.nextFloat() - random.nextFloat()) * 0.1F + (isBaby() ? 1.5F : 1.0F);
	}

	/** Ostrich.dropFewItems: a wild one drops its loot table (feathers), a tamed one 2-6 poppies instead. */
	@Override
	protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKilled) {
		if (!isTame()) super.dropFromLootTable(level, source, playerKilled);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		if (isTame()) {
			int n = 2 + random.nextInt(5);
			for (int i = 0; i < n; i++) spawnAtLocation(level, Items.POPPY);
		}
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		if (isBaby()) {
			setPersistenceRequired();
			return false;
		}
		if (isVehicle()) return false;
		return !isPersistenceRequired() && !isTame();
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return orespawn.entity.Breeding.crystalApple(stack);
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return ModEntities.OSTRICH.create(level, EntitySpawnReason.BREEDING);
	}

	/** Ostrich.getCanSpawnHere: at y 50 or higher, by day, 1 time in 4, and no other ostrich within 16 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (pos.getY() < 50 || !level.getLevel().isBrightOutside() || random.nextInt(4) != 1) return false;
		return level.getEntitiesOfClass(Ostrich.class, new AABB(pos).inflate(16.0, 6.0, 16.0)).isEmpty();
	}
}
